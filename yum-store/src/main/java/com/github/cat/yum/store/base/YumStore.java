package com.github.cat.yum.store.base;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
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
import com.github.cat.yum.store.sqlite.DataSourcePool;
import com.github.cat.yum.store.sqlite.SqlUtils;
import com.github.cat.yum.store.util.GZipUtils;
import com.github.cat.yum.store.util.HashFile;
import com.github.cat.yum.store.util.HttpUtils;
import com.github.cat.yum.store.util.VersionStringUtils;
import com.github.cat.yum.store.util.YumUtil;

public class YumStore {
	private static Logger log = LoggerFactory.getLogger(YumStore.class);
	
	public static File cachedir = new File("cache");
	private List<Store> stores;
	private String[] fileterRpmNames;
	

	public YumStore(File xml){
        Element root = getXmlRoot(xml);
        
        cachedir = new File(root.getAttributeValue("catchdir"));
        if(!cachedir.exists()){
        	cachedir.mkdirs();
        }
        cachedir =  cachedir.getAbsoluteFile();
        
        List<Element> elements = root.getChildren("store");
        stores = new ArrayList<>();
        for(Element element : elements){
        	Store store = new Store();
			store.baseUrl = element.getTextTrim();
			String host  = getHost(store.baseUrl);
			if(StringUtils.isBlank(host)){
				throw new YumException(store.baseUrl + " host is error");
			}
			store.host = host;
			String os = element.getAttributeValue("os");
			if(!StringUtils.isBlank(os)){
				store.os = os.trim();
			}
			String releasever = element.getAttributeValue("releasever");
			if(!StringUtils.isBlank(releasever)){
				store.releasever = releasever.trim();
			}
			String basearch = element.getAttributeValue("basearch");
			if(!StringUtils.isBlank(basearch)){
				try{
					store.basearch = Basearch.valueOf(basearch.trim());
				}catch(IllegalArgumentException e){
					throw new YumException("basearch : [i386|x86_64] not be " + basearch.trim());
				}
			}
			stores.add(store);
        }
        List<String> filters = new ArrayList<>();
        elements = root.getChildren("filter");
        for(Element element : elements){
        	filters.add(element.getTextTrim());
        }
        fileterRpmNames = filters.toArray(new String[]{});
	}
	
	public void init(String os, String releasever, Basearch basearch){
		List<Object> enables = new ArrayList<>();
		for(Store store : stores){
			if(StringUtils.isBlank(store.os) && StringUtils.indexOf(store.baseUrl, "{os}") > 0){
				store.os = os;
			}
			if(StringUtils.isBlank(store.releasever) && StringUtils.indexOf(store.baseUrl, "{releasever}") > 0){
				store.releasever = releasever;
			}
			if(store.basearch == null && StringUtils.indexOf(store.baseUrl, "{basearch}") > 0){
				store.basearch = basearch;
			}
			
			if(StringUtils.isBlank(store.os) || StringUtils.isBlank(store.releasever) || null == store.basearch){
				log.warn("url:{}  disable.os:{},releasever:{},basearch{}");
				continue;
			}
			
			
			store.baseUrl = StringUtils.replace(store.baseUrl, "{os}", store.os);
			store.baseUrl = StringUtils.replace(store.baseUrl, "{releasever}", store.releasever);
			store.baseUrl = StringUtils.replace(store.baseUrl, "{basearch}", store.basearch.toString());
			
			DataSource ds = DataSourcePool.getPool(store);
			int count = (int) SqlUtils.select(ds, "select count(*) icount from sqlite_master", new MapHandler()).get("icount");
			if(count != 0){
				store.enable = true;
				enables.add(store);
				log.info("load store [" + store.baseUrl + "] from cache");  
				continue ;
			}
			
			File dir = new File(cachedir.getPath() + File.separator + store.host);
			if(!dir.exists()){
				dir.mkdir();
			}
			File repomdXml = new File(dir.getPath() + File.separator + store.os + File.separator + store.releasever 
					+ File.separator +  store.basearch + File.separator +"repomd" + ".xml");
			
			if(!repomdXml.exists()){
				try{
					HttpUtils.dowloadFile(store.baseUrl, YumUtil.REPOPATH + "/" + "repomd" + ".xml", repomdXml);
				}catch(IOException e){
					log.warn("[skip] download fail " + e.getMessage());
				}
			}
			if(!repomdXml.exists()){
				log.info("store " + store.baseUrl + " disable");
				continue;
			}
			File privateXml = new File(dir.getPath() + File.separator + store.os + File.separator + store.releasever 
					+ File.separator +  store.basearch + File.separator + "private" + ".xml");
			if(!privateXml.exists()){
				try{
					privateXml = downloadPrivateXml(store, dir, repomdXml);
					if(!privateXml.exists()){
						log.warn("private.xml not exists");
						log.info("store " + store.baseUrl + " disable");
						continue;
					}
				}catch(RuntimeException e){
					log.warn(e.getMessage());
					log.info("store " + store.baseUrl + " disable ");
					continue;
				}
			}
			SqlUtils.inintTables(ds);
			loadProvateXmlData(store, ds, privateXml);
			store.enable = true;
			enables.add(store);
			log.info("store " + store.baseUrl + " enable");
		}
		
		if(enables.size() == 0){
			throw new YumException("no store enable");
		}
	}
	
	
	private void loadProvateXmlData(Store store, DataSource ds, File privateXml){
		Connection conn = null;
		try{
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			String packageSql = "insert into package (key, name, arch, version, algorithm, checkSum, location) "
					+ "values (?,?,?,?,?,?,?);";
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
				packagesData.add(new Object[]{pk, rpmMetadata.name, rpmMetadata.architecture, rpmMetadata.version, 
						rpmMetadata.algorithm,	rpmMetadata.checkSum, rpmMetadata.location});
				
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
				throw new YumException(e1);
			}
			throw new YumException(e);
		}finally{
			if(conn != null){
				try{
					conn.setAutoCommit(true);
					conn.close();
				}catch(SQLException ignore){
					
				}
			}
		}
	}
	
	
	public void clean() throws IOException {
		FileUtils.deleteDirectory(cachedir);
	}
	
	
	private List<Entry> downloadRpmAddResult(String packagekey, Store store, SearchResult result){
		DataSource ds = DataSourcePool.getPool(store);
		Map<String, Object> rpm = SqlUtils.select(ds, "select * from package where key=?", new MapHandler(), packagekey);
		String name = (String)rpm.get("name");
		if(ArrayUtils.indexOf(fileterRpmNames, name) > -1){
			log.info("skip rpm :" + name);
			return result.addRpm(store, packagekey, null);
		}
		
		String location = (String)rpm.get("location");
		File target = new File(cachedir.getPath() + File.separator + store.host + File.separator 
				+ store.os + File.separator + store.releasever + File.separator + rpm.get("location"));
		String algorithm = (String)rpm.get("algorithm");
		String checkSum = (String)rpm.get("checkSum");
		try{
			if(target.exists() && StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				log.info("file:" + target + " exist");
				return result.addRpm(store, packagekey, target);
			}
			HttpUtils.dowloadFile(store.baseUrl, location, target);
			if(StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				return result.addRpm(store, packagekey, target);
			}
			throw new YumException("file:" + target + " checkSum fail");
		}catch(NoSuchAlgorithmException | IOException e){
			throw new YumException("download " + location + " fail " + e.getMessage());
		}
	}
	
	private File downloadPrivateXml(Store store, File dir, File repomdXml) {
		Element privateElement = getPrivateXmlPath(repomdXml);
		String href = privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim();
		File privateTepmXml = new File(dir.getPath() + File.separator + getFileNameFormHref(href));
		try {
			HttpUtils.dowloadFile(store.baseUrl, privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim(), privateTepmXml);
		} catch (IOException e) {
			throw new YumException(e);
		}
		Element  check = privateElement.getChild("checksum", YumUtil.REPONAMESPACE);
		String sum;
		try {
			sum = HashFile.getsum(privateTepmXml, check.getAttributeValue("type"));
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new YumException(e);
		}
		if(!sum.equals(check.getValue())){
			throw new YumException("download file and checked fail");
		}
		try {
			GZipUtils.decompress(privateTepmXml);
		} catch (IOException e) {
			throw new YumException(e);
		}
		privateTepmXml = new File(dir.getPath() + File.separator + privateTepmXml.getName().substring(0, privateTepmXml.getName().indexOf(".gz")));
		privateTepmXml.renameTo(new File(dir.getPath() + File.separator + "private" + ".xml"));
		privateTepmXml = new File(dir.getPath() + File.separator + "private" + ".xml");
		check = privateElement.getChild("open-checksum", YumUtil.REPONAMESPACE);
		try {
			sum = HashFile.getsum(privateTepmXml, check.getAttributeValue("type"));
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new YumException(e);
		}
		if(!sum.equals(check.getValue())){
			throw new YumException("download " + privateTepmXml + " and checked fail");
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
			throw new YumException(e);
		}
	}
	
	
	public SearchResult retrive(String rpmName, String version, Basearch basearch){
		SearchResult result = new SearchResult();
		List<Entry> entries = searchPackageAndDownload(rpmName, version, basearch, result);
		if(entries == null){
			throw new YumException("not find " + rpmName);
		}
		searchAndDownload(entries, result);
		return result;
	}
	
	
	public List<Entry> searchPackageAndDownload(String rpmName, String version, Basearch basearch, SearchResult result){
		for(Store store : stores){
			try{
				if(!store.enable){
					continue;
				}
				DataSource ds = DataSourcePool.getPool(store);
				if(!StringUtils.isBlank(version)){
					Map<String, Object> data = SqlUtils.select(ds, "select * from package where name=? and version=? and arch=?", new MapHandler(), rpmName, version, basearch.getArch());
					if(data == null){
						data = SqlUtils.select(ds, "select * from package where name=? and version=? and arch=?", new MapHandler(), rpmName, version,  Basearch.no.getArch());
					}
					if(data != null){
						return downloadRpmAddResult((String)data.get("key"), store, result);
					}
				}
				else{
					Map<String, Object> data = SqlUtils.select(ds, "select * from package where name=? and arch=?", new MapHandler(), rpmName, basearch.getArch());
					if(data == null){
						data = SqlUtils.select(ds, "select * from package where name=? and arch=?", new MapHandler(), rpmName, Basearch.no.getArch());
					}
					if(data != null){
						return downloadRpmAddResult((String)data.get("key"), store, result);
					}
				}
			}catch(RuntimeException e){
				log.warn("[skip] " + e.getMessage());
			}
		}
		return null;
	}
	
	
	private void searchAndDownload(List<Entry> serarchs, SearchResult result){
		List<Entry> nextSearch = new ArrayList<>();
		for(Entry search : serarchs){
			//判断是否已提供
			if(result.check(search)){
				continue;
			}
			boolean success = false;
			for(Store store : stores){
				if(!store.enable){
					continue;
				}
				DataSource ds = DataSourcePool.getPool(store);
				//provides 中查找
				List<Map<String, Object>> datas = SqlUtils.selectList(ds, "select * from provides where name=?", new MapListHandler(), search.name);
		
				for(Map<String, Object> data : datas){
					if(verificationEntry(search, (String) data.get("version"))){
						try{
							String packagekey = (String)data.get("packagekey");
							nextSearch.addAll(downloadRpmAddResult(packagekey, store, result));
							success = true;
							break;
						}catch(YumException e){
							log.warn("[skip]" + e.getMessage());
						}
					}
					
				}
				
				//files 中查找
				if(!success && StringUtils.isBlank(search.flags)){
					datas = SqlUtils.selectList(ds, "select * from files where path=?", new MapListHandler(), search.name);
					
					for(Map<String, Object> data : datas){
						try{
							String packagekey = (String)data.get("packagekey");
							nextSearch.addAll(downloadRpmAddResult(packagekey, store, result));
							success = true;
							break;
						}catch(YumException e){
							log.warn("[skip]" + e.getMessage());
						}
					}
				}
				
				if(success){
					break;
				}
			}
			if(!success){
				throw new RuntimeException("not find " + search.name + " provide");
			}
		}
		if(nextSearch.size() > 0){
			searchAndDownload(nextSearch, result);
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
	
	public static String getHost(String url) {
		if (url == null || url.trim().equals("")) {
			return "";
		}
		String host = "";
		Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
		Matcher matcher = p.matcher(url);
		if (matcher.find()) {
			host = matcher.group();
		}
		return host;
	}

}
