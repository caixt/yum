package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
				throw new IllegalArgumentException("store key must unique and not null");
			}
			stores.add(store);
        }
	}
	
	
	public void initFile(){
		for(Store store : stores){
			File dir = new File(cachedir.getPath() + File.separator + store.key);
			if(!dir.exists()){
				dir.mkdir();
			}
			File repomdXml = new File(dir.getPath() + File.separator + "repomd" + ".xml");
			if(!repomdXml.exists()){
				try{
					HttpUtils.dowloadFile(store.baseUrl, YumUtil.REPOPATH + "/" + "repomd" + ".xml", repomdXml);
				}catch(Exception e){
					log.warn("", e);
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
				}catch(Exception e){
					log.info("store " + store.key + " disable");
					continue;
				}
			}
			store.enable = true;
			log.info("store " + repomdXml + " enable");
		}
	}
	
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
				PackageRpmMetadata rpmMetadata = new PackageRpmMetadata(packageElement);
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

	public File getCachedir() {
		return cachedir;
	}

	public void setCachedir(File cachedir) {
		this.cachedir = cachedir;
	}

	public List<Store> getStores() {
		return stores;
	}

	public void setStores(List<Store> stores) {
		this.stores = stores;
	}
	
	private File downloadPrivateXml(Store store, File dir, File repomdXml) throws ClientProtocolException, IOException, NoSuchAlgorithmException, IllegalAccessException{
		Element privateElement = getPrivateXmlPath(repomdXml);
		String href = privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim();
		File privateTepmXml = new File(dir.getPath() + File.separator + getFileNameFormHref(href));
		HttpUtils.dowloadFile(store.baseUrl, privateElement.getChild("location", YumUtil.REPONAMESPACE).getAttributeValue("href").trim(), privateTepmXml);

	
		Element  check = privateElement.getChild("checksum", YumUtil.REPONAMESPACE);
		String sum = HashFile.getsum(privateTepmXml, getAlgorithm(check.getAttributeValue("type")));
		if(!sum.equals(check.getValue())){
			throw new IllegalAccessException("download file and checked fail");
		}
		GZipUtils.decompress(privateTepmXml);
		privateTepmXml = new File(dir.getPath() + File.separator + privateTepmXml.getName().substring(0, privateTepmXml.getName().indexOf(".gz")));
		privateTepmXml.renameTo(new File(dir.getPath() + File.separator + "private" + ".xml"));
		privateTepmXml = new File(dir.getPath() + File.separator + "private" + ".xml");
		check = privateElement.getChild("open-checksum", YumUtil.REPONAMESPACE);
		sum = HashFile.getsum(privateTepmXml, getAlgorithm(check.getAttributeValue("type")));
		if(!sum.equals(check.getValue())){
			throw new IllegalAccessException("download file and checked fail");
		}
		return privateTepmXml;
		
	}
	
	
	private Element getPrivateXmlPath(File repomdXml) {
		SAXBuilder saxBuilder = new SAXBuilder();            
		try {
			Document doc = saxBuilder.build(repomdXml);
			Element root = doc.getRootElement();
			
	        for(Element element : root.getChildren("data", YumUtil.REPONAMESPACE)){
	        	String type = element.getAttributeValue("type");
				if("primary".equals(type)){
					return element;
				}
	        }
		} catch (JDOMException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
		return null;
	}
	
	
	private String getFileNameFormHref(String href){
		if(href.indexOf('/') > 1){
			return href.substring(href.indexOf("/") + 1);
		}
		return href;
	}
	
	
	private String getAlgorithm(String algorithm){
		switch (algorithm) {
			case "SHA224":
			case "sha224":
				return "SHA-224";
			case "SHA256":
			case "sha256":
				return "SHA-256";
			case "SHA384":
			case "sha384":
				return "SHA-384";
			case "SHA512":
			case "sha512":
				return "SHA-512";
			default:
				return algorithm;
		}
	}
	
	public Element getXmlRoot(File xml){
		SAXBuilder saxBuilder = new SAXBuilder();            
		Document doc;
		try {
			doc = saxBuilder.build(xml);
			Element root = doc.getRootElement();
			return root;
		} catch (Exception e) {
			throw new RuntimeException("xml read failed", e);
		}
	}
}
