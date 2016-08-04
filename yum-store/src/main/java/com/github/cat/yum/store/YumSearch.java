package com.github.cat.yum.store;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.PackageRpmMetadata;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.model.Store;
import com.github.cat.yum.store.sqlite.SqlUtils;
import com.github.cat.yum.store.util.VersionStringUtils;

public class YumSearch {
	private static Logger log = LoggerFactory.getLogger(YumSearch.class);
	
	public static void search(File storeXml, String... rpmName) {
		YumStore yumStore = new YumStore(storeXml);
		yumStore.init();
//		Map<String, List<PackageRpmMetadata>> map = yumStore.initStore();
		List<Entry> serarchs = new ArrayList<>();
		for(String name : rpmName){
			Entry entry = new Entry();
			entry.name = name;
			serarchs.add(entry);
		}
//		SearchResult result = new SearchResult();
		SearchResult searchResult = yumStore.searchAndDownload(serarchs);
		for(File file : searchResult.rpms){
			System.out.println(file);
		}
		
		
//		log.info("search success.");
//		for(PackageRpmMetadata rpm : result.rpms){
//			File target = yumStore.getTarget(rpm.store, rpm.location);
//			try{
//				yumStore.download(rpm.store, rpm.location, target, rpm.algorithm, rpm.checkSum);
//			}catch(YumException e){
//				throw new YumException("download file " + rpm.store.baseUrl + "/" + rpm.location + " error", e);
//			}
//		}
	}
	
//	private static void searchAndDownload(List<Entry> serarchs, SearchResult result, Map<String, Store> stores){
//		List<Entry> nextSearch = new ArrayList<>();
//		for(Entry search : serarchs){
//			//provides 中查找
//			List<Map<String, Object>> datas = SqlUtils.selectList("select * from provides where name=?", new MapListHandler(), search.name);
//			
//			if(datas == null || datas.size() == 0){
//				throw new RuntimeException("not find " + search.name + " provide");
//			}
//			for(Map<String, Object> data : datas){
//				if(verificationEntry(search, (String) data.get("version"))){
//					yumStore.download(rpm.store, rpm.location, target, rpm.algorithm, rpm.checkSum)
//				}
//				
//			}
//			verificationEntry(require, provideVersion);
//			
//			PackageRpmMetadata rpm = verification(search, rpms);
//			if(null == rpm){
//				throw new RuntimeException("not find " + search.name + " provide");
//			}
//			nextSearch.addAll(result.addRpm(rpm));
//		}
//		if(nextSearch.size() > 0){
//			search(nextSearch, map, result);
//		}
//	}
//	
//	
//	private static PackageRpmMetadata verification(Entry require, Map<String, Object> rpms){
//		if(StringUtils.isBlank(require.flags)){
//			return rpms.get(0);
//		}
//		for(PackageRpmMetadata rpm : rpms){
//			if(verificationEntry(require, rpm.provide)){
//				return rpm;
//			}
//		}
//		return null;
//	}
//	
//	
//	public static boolean verificationEntry(Entry require, String provideVersion){
//		if(StringUtils.isBlank(require.flags)){
//			return true;
//		}
//		switch (require.flags) {
//			case "LE":
//				if(VersionStringUtils.compare(require.version, provideVersion) <= 0){
//					return true;
//				}
//			case "GE":
//				if(VersionStringUtils.compare(require.version, provideVersion) >= 0){
//					return true;
//				}
//			case "EQ":
//				if(VersionStringUtils.compare(require.version, provideVersion) == 0){
//					return true;
//				}
//			case "LT":
//				if(VersionStringUtils.compare(require.version, provideVersion) < 0){
//					return true;
//				}
//			case "GT":
//				if(VersionStringUtils.compare(require.version, provideVersion) > 0){
//					return true;
//				}
//			default:
//				break;
//		}
//		return false;
//	}
}
