package com.github.cat.yum.store.base;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.github.cat.yum.store.model.Store;
import com.github.cat.yum.store.util.GZipUtils;
import com.github.cat.yum.store.util.HashFile;
import com.github.cat.yum.store.util.HttpUtils;
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
	
	/**
	 * 初始化文件(包含下载repomd.xml,private.xml)
	 */
	public void initFile(){
		for(Store store : stores){
			File dir = new File(cachedir.getPath() + File.separator + store.key);
			if(!dir.exists()){
				dir.mkdir();
			}
			File repomdXml = new File(dir.getPath() + File.separator + "repomd" + ".xml");
			if(!repomdXml.exists()){
				try{
					log.info("download file " + store.baseUrl + YumUtil.REPOPATH + "/" + "repomd" + ".xml");
					HttpUtils.dowloadFile(store.baseUrl, YumUtil.REPOPATH + "/" + "repomd" + ".xml", repomdXml);
				}catch(IOException e){
					log.warn("download file " + store.baseUrl + YumUtil.REPOPATH + "/" + "repomd" + ".xml error", e);
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
			log.info("store " + store.key + " enable");
		}
	}
	
	/**
	 * 分析private.xml文件
	 * @return
	 */
	public Map<String, List<PackageRpmMetadata>> initStore(){
		Map<String, List<PackageRpmMetadata>> map = new HashMap<>();
		for(Store store : stores){
			if(!store.enable){
				continue;
			}
			File privateXml = new File(cachedir.getPath() + File.separator + store.key 
					+ File.separator + "private" + ".xml");
			Element root = getXmlRoot(privateXml);
			
			List<Element> packages = root.getChildren("package", YumUtil.COMMONNAMESPACE);
			for(Element packageElement : packages){
				PackageRpmMetadata rpmMetadata = new PackageRpmMetadata(packageElement, store);
				for(Entry entry : rpmMetadata.provide){
					 List<PackageRpmMetadata> datas = map.get(entry.name);
					 if(null == datas){
						 datas = new ArrayList<>();
					 }
					 datas.add(rpmMetadata);
					 map.put(entry.name, datas);
				}
				
				for(com.github.cat.yum.store.model.File file : rpmMetadata.files){
					//file 类型
					if(StringUtils.isBlank(file.type)){
						List<PackageRpmMetadata> datas = map.get(file.path);
						
						if(null == datas){
							 datas = new ArrayList<>();
						}
						datas.add(rpmMetadata);
						map.put(file.path, datas);
					}
				}
			}
		}
		return map;
	}
	
	public void clean() throws IOException {
		FileUtils.deleteDirectory(cachedir);
		
	}
	
	public File getTarget(Store store, String file){
		return new File(cachedir.getPath() + File.separator + store.key + File.separator + file);
	}
	
	
	public void download(Store store, String file, File target, String algorithm, String checkSum){
		try{
			if(target.exists() && StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				log.info("file:" + file + " exist");
				return ;
			}
			log.info("download file:" + file + " from " + store.baseUrl);
			HttpUtils.dowloadFile(store.baseUrl, file, target);
			if(StringUtils.equals(HashFile.getsum(target, algorithm), checkSum)){
				log.info("download file:" + file + " success");
				return ;
			}
			throw new YumException("file:" + file + " checkSum fail");
		}catch(NoSuchAlgorithmException | IOException e){
			throw new YumException("file:" + file + " download fail", e);
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

}
