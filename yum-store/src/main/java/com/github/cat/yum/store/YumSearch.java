package com.github.cat.yum.store;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.PackageRpmMetadata;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.util.VersionStringUtils;

public class YumSearch {
	public static void search(File storeXml, String... rpmName) {
		YumStore yumStore = new YumStore(storeXml);
		yumStore.initFile();
		Map<String, List<PackageRpmMetadata>> map = yumStore.initStore();
		List<Entry> serarch = new ArrayList<>();
		for(String name : rpmName){
			Entry entry = new Entry();
			entry.name = name;
			serarch.add(entry);
		}
		SearchResult result = new SearchResult();
		search(serarch, map, result);
		for(PackageRpmMetadata rpm : result.rpms){
			System.out.println(rpm.name);
		}
		
		
	}
	
	private static void search(List<Entry> serarchs, Map<String, List<PackageRpmMetadata>> map, SearchResult result){
		List<Entry> nextSearch = new ArrayList<>();
		for(Entry search : serarchs){
			List<PackageRpmMetadata> rpms = map.get(search.name);
			if(rpms == null || rpms.size() == 0){
				throw new RuntimeException("not find " + search.name + " provide");
			}
			PackageRpmMetadata rpm = verification(search, rpms);
			if(null == rpm){
				throw new RuntimeException("not find " + search.name + " provide");
			}
			nextSearch.addAll(result.addRpm(rpm));
		}
		if(nextSearch.size() > 0){
			search(nextSearch, map, result);
		}
	}
	
	
	private static PackageRpmMetadata verification(Entry require, List<PackageRpmMetadata> rpms){
		if(StringUtils.isBlank(require.flags)){
			return rpms.get(0);
		}
		for(PackageRpmMetadata rpm : rpms){
			if(verificationEntry(require, rpm.provide)){
				return rpm;
			}
		}
		return null;
	}
	
	
	public static boolean verificationEntry(Entry require, List<Entry> provides){
		for(Entry provide : provides){
			if(require.name.equals(provide.name)){
				if(StringUtils.isBlank(require.flags)){
					return true;
				}
				switch (require.flags) {
					case "LE":
						if(VersionStringUtils.compare(require.version, provide.version) <= 0){
							return true;
						}
					case "GE":
						if(VersionStringUtils.compare(require.version, provide.version) >= 0){
							return true;
						}
					case "EQ":
						if(VersionStringUtils.compare(require.version, provide.version) == 0){
							return true;
						}
					case "LT":
						if(VersionStringUtils.compare(require.version, provide.version) < 0){
							return true;
						}
					case "GT":
						if(VersionStringUtils.compare(require.version, provide.version) > 0){
							return true;
						}
					default:
						break;
					}
			}
		}
		return false;
	}
}
