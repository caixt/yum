package com.github.cat.yum.store.base;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.PackageRpmMetadata;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.model.Store;
import com.github.cat.yum.store.sqlite.SqlUtils;
import com.github.cat.yum.store.util.GZipUtils;
import com.github.cat.yum.store.util.HashFile;
import com.github.cat.yum.store.util.HttpUtils;
import com.github.cat.yum.store.util.VersionStringUtils;
import com.github.cat.yum.store.util.YumUtil;

public class YumStore {
	private static Logger log = LoggerFactory.getLogger(YumStore.class);
	
	private File cachedir = null;
	private List<Store> stores;
	

	public YumStore(File xml){
        Element root = getXmlRoot(xml);
        
        cachedir = new File(root.getAttributeValue("catchdir"));
        if(!cachedir.exists()){
        	cachedir.mkdirs();
        }
        cachedir =  cachedir.getAbsoluteFile();
        
        Set<String> keys = new HashSet<>();
        List<Element> elements = root.getChildren("store");
        stores = new ArrayList<>();
        for(Element element : elements){
        	Store store = new Store();
			store.key = element.getAttributeValue("key").trim();
			store.baseUrl = element.getTextTrim();
			if(keys.contains(store.key) || StringUtils.isBlank(store.key)){
				throw new YumException("store key must unique and not null");
			}
			stores.add(store);
        }
	}
	

	public void init(){
		for(Store store : stores){
			Store dbStore = SqlUtils.selectList("select * from store where key=?", new BeanHandler<Store>(Store.class), store.key);
			if(dbStore != null && dbStore.baseUrl.equals(store.baseUrl)){
				log.info("load store [" + store.key + "] from cache");  
				return ;
			}
			else if(dbStore != null && !dbStore.baseUrl.equals(store.baseUrl)){
				log.info("delete store [" + store.key + "] cache");
				SqlUtils.selectList("delete store where key=?", new BeanHandler<Store>(Store.class), store.key);
				File dir = new File(cachedir.getPath() + File.separator + store.key);
				if(dir.exists()){
					try {
						FileUtils.deleteDirectory(dir);
					} catch (IOException e) {
						throw new YumException("delete file:" + dir + "error", e);
					}
				}
			}
			
			
			File dir = new File(cachedir.getPath() + File.separator + store.key);
			if(!dir.exists()){
				dir.mkdir();
			}
			File repomdXml = new File(dir.getPath() + File.separator + "repomd" + ".xml");
			
			if(!repomdXml.exists()){
				try{
					log.info("download file " + store.baseUrl + "/" + YumUtil.REPOPATH + "/" + "repomd" + ".xml");
					HttpUtils.dowloadFile(store.baseUrl, YumUtil.REPOPATH + "/" + "repomd" + ".xml", repomdXml);
					log.info("download file " + store.baseUrl + "/" + YumUtil.REPOPATH + "/" + "repomd" + ".xml success");
				}catch(IOException e){
					log.warn("download file " + store.baseUrl + "/" + YumUtil.REPOPATH + "/" + "repomd" + ".xml error", e);
				}
			}
			if(!repomdXml.exists()){
				log.info("store " + store.key + " disable");
				continue;
			}
			File privateXml = new File(dir.getPath() + File.separator + "private" + ".xml");
			if(!privateXml.exists()){
				try{
					privateXml = downloadPrivateXml(store, dir, repomdXml);
					if(!privateXml.exists()){
						log.info("store " + store.key + " disable");
						continue;
					}
				}catch(RuntimeException e){
					log.info("store " + store.key + " disable");
					continue;
				}
			}
			store.enable = true;
			loadProvateXmlData(store, privateXml);
			log.info("store " + store.key + " enable");
		}
	}
	
	
	private void loadProvateXmlData(Store store, File privateXml){
		Connection conn = SqlUtils.getConnection();
		try{
			conn.setAutoCommit(false);
			SqlUtils.update(conn, "insert into store (key,baseUrl) values (?,?)", store.key, store.baseUrl);
			
			String packageSql = "insert into package (key, storekey, name, algorithm, checkSum, location) "
					+ "values (?,?,?,?,?,?);";
			String privateSql = "insert into provides (packagekey, name,  flags,  epoch,  version,  release) "
					+ "values (?,?,?,?,?,?);";
			String requireSql = "insert into requires (packagekey, name,  flags,  epoch,  version,  release) "
					+ "values (?,?,?,?,?,?);";
			String fileSql = "insert into files (packagekey, path, type) "
					+ "values (?,?,?);";
			List<Object[]> packagesData = new ArrayList<>();
			List<Object[]> privatesData = new ArrayList<>();
			List<Object[]> requiresData = new ArrayList<>();
			List<Object[]> filesData = new ArrayList<>();
			Element root = getXmlRoot(privateXml);
			List<Element> packages = root.getChildren("package", YumUtil.COMMONNAMESPACE);
			for(Element packageElement : packages){
				PackageRpmMetadata rpmMetadata = new PackageRpmMetadata(packageElement);
				String pk = SqlUtils.getUUId();
				packagesData.add(new Object[]{pk, store.key, rpmMetadata.name, rpmMetadata.algorithm,
						rpmMetadata.checkSum, rpmMetadata.location});
				
				for(Entry entry : rpmMetadata.provide){
					privatesData.add(new Object[]{pk, entry.name, entry.flags, entry.epoch, 
							entry.version, entry.release});
				}
				
				for(Entry entry : rpmMetadata.require){
					requiresData.add(new Object[]{pk, entry.name, entry.flags, entry.epoch, 
							entry.version, entry.release});
				}
				
				for(com.github.cat.yum.store.model.File file : rpmMetadata.files){
					//file 类型
					if(StringUtils.isBlank(file.type)){
						filesData.add(new Object[]{pk, file.path, file.type});
					}
				}
			}
			SqlUtils.batch(conn, packageSql, packagesData.toArray(new Object[][]{}));
			SqlUtils.batch(conn, privateSql, privatesData.toArray(new Object[][]{}));
			SqlUtils.batch(conn, requireSql, requiresData.toArray(new Object[][]{}));
			SqlUtils.batch(conn, fileSql, filesData.toArray(new Object[][]{}));
			SqlUtils.batch(conn, fileSql, filesData.toArray(new Object[][]{}));
			conn.commit();
		}catch(SQLException e){
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw new YumException("db rollback error", e);
			}
			throw new YumException("load private xml data fail", e);
		}finally{
			try{
				conn.setAutoCommit(true);
				conn.close();
			}catch(SQLException ignore){
				
			}
		}
	}
	
	
	public void clean() throws IOException {
		FileUtils.deleteDirectory(cachedir);
		
	}
	
	
	private File downloadRpm(String packagekey, Map<String, Store> stores){
		Map<String, Object> rpm = SqlUtils.selectList("select * from package where key=?", new MapHandler(), packagekey);
		Store store = stores.get(rpm.get("storekey"));
		String location = (String)rpm.get("location");
		File target = new File(cachedir.getPath() + File.separator + store.key + File.separator + location);
		String algorithm = (String)rpm.get("algorithm");
		String checkSum = (String)rpm.get("checkSum");
		try{
			if(target.exists() && StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				log.info("file:" + target + " exist");
				return target;
			}
			log.info("download file:" + store.baseUrl + "/" + location);
			HttpUtils.dowloadFile(store.baseUrl, location, target);
			if(StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				log.info("download file:" + location + " success");
				return target;
			}
			throw new YumException("file:" + target + " checkSum fail");
		}catch(NoSuchAlgorithmException | IOException e){
			throw new YumException("file:" + target + " download fail", e);
		}
	}
	
	private File downloadPrivateXml(Store store, File dir, File repomdXml) {
		Element privateElement = getPrivateXmlPath(repomdXml);
		String href = privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim();
		File privateTepmXml = new File(dir.getPath() + File.separator + getFileNameFormHref(href));
		try {
			HttpUtils.dowloadFile(store.baseUrl, privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim(), privateTepmXml);
		} catch (IOException e) {
			throw new YumException("download private.xml fail form " + store.baseUrl, e);
		}
		Element  check = privateElement.getChild("checksum", YumUtil.REPONAMESPACE);
		String sum;
		try {
			sum = HashFile.getsum(privateTepmXml, check.getAttributeValue("type"));
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new YumException("check " + privateTepmXml + " error", e);
		}
		if(!sum.equals(check.getValue())){
			throw new YumException("download file and checked fail");
		}
		try {
			GZipUtils.decompress(privateTepmXml);
		} catch (IOException e) {
			throw new YumException("decompress " + privateTepmXml + "", e);
		}
		privateTepmXml = new File(dir.getPath() + File.separator + privateTepmXml.getName().substring(0, privateTepmXml.getName().indexOf(".gz")));
		privateTepmXml.renameTo(new File(dir.getPath() + File.separator + "private" + ".xml"));
		privateTepmXml = new File(dir.getPath() + File.separator + "private" + ".xml");
		check = privateElement.getChild("open-checksum", YumUtil.REPONAMESPACE);
		try {
			sum = HashFile.getsum(privateTepmXml, check.getAttributeValue("type"));
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new YumException("check " + privateTepmXml + " error", e);
		}
		if(!sum.equals(check.getValue())){
			throw new YumException("download file and checked fail");
		}
		return privateTepmXml;
		
	}
	
	
	private Element getPrivateXmlPath(File repomdXml) {
		Element root = getXmlRoot(repomdXml);
		
        for(Element element : root.getChildren("data", YumUtil.REPONAMESPACE)){
        	String type = element.getAttributeValue("type");
			if("primary".equals(type)){
				return element;
			}
        }
		throw new YumException("not find <data type=\"primary\"></data> from" + repomdXml);
	}
	
	
	private String getFileNameFormHref(String href){
		if(href.indexOf('/') > 1){
			return href.substring(href.indexOf("/") + 1);
		}
		return href;
	}
	
	
	private Element getXmlRoot(File xml){
		SAXBuilder saxBuilder = new SAXBuilder();            
		try {
			Document doc = saxBuilder.build(xml);
			Element root = doc.getRootElement();
			return root;
		} catch (JDOMException | IOException e) {
			throw new YumException("xml read failed", e);
		}
	}
	
	
	public SearchResult searchAndDownload(List<Entry> serarchs){
		Map<String, Store> storeMap = new HashMap<>();
		for(Store store : stores){
			if(store.enable){
				storeMap.put(store.key, store);
			}
		}
		SearchResult result = new SearchResult();
		searchAndDownload(serarchs, result, storeMap);
		return result;
	}
	
	
	private void searchAndDownload(List<Entry> serarchs, SearchResult result, Map<String, Store> stores){
		List<Entry> nextSearch = new ArrayList<>();
		for(Entry search : serarchs){
			//判断是否已提供
			if(result.check(search)){
				continue;
			}
			
			boolean success = false;
			//provides 中查找
			List<Map<String, Object>> datas = SqlUtils.selectList("select * from provides where name=?", new MapListHandler(), search.name);
			
			for(Map<String, Object> data : datas){
				if(verificationEntry(search, (String) data.get("version"))){
					try{
						String packagekey = (String)data.get("packagekey");
						File rpm = downloadRpm(packagekey, stores);
						nextSearch.addAll(result.addRpm(packagekey, rpm));
						success = true;
						break;
					}catch(YumException e){
						log.warn(e.getMessage() + " skip...", e);
					}
				}
				
			}
			
			//files 中查找
			if(!success && StringUtils.isBlank(search.flags)){
				datas = SqlUtils.selectList("select * from files where path=?", new MapListHandler(), search.name);
				
				for(Map<String, Object> data : datas){
					try{
						String packagekey = (String)data.get("packagekey");
						File rpm = downloadRpm(packagekey, stores);
						nextSearch.addAll(result.addRpm(packagekey, rpm));
						success = true;
						break;
					}catch(YumException e){
						log.warn(e.getMessage() + " skip...", e);
					}
				}
			}
			
			
			if(!success){
				throw new RuntimeException("not find " + search.name + " provide");
			}
		}
		if(nextSearch.size() > 0){
			searchAndDownload(nextSearch, result, stores);
		}
	}
	
	
	public static boolean verificationEntry(Entry require, String provideVersion){
		if(StringUtils.isBlank(require.flags)){
			return true;
		}
		switch (require.flags) {
			case "LE":
				if(VersionStringUtils.compare(require.version, provideVersion) <= 0){
					return true;
				}
			case "GE":
				if(VersionStringUtils.compare(require.version, provideVersion) >= 0){
					return true;
				}
			case "EQ":
				if(VersionStringUtils.compare(require.version, provideVersion) == 0){
					return true;
				}
			case "LT":
				if(VersionStringUtils.compare(require.version, provideVersion) < 0){
					return true;
				}
			case "GT":
				if(VersionStringUtils.compare(require.version, provideVersion) > 0){
					return true;
				}
			default:
				break;
		}
		return false;
	}

}
