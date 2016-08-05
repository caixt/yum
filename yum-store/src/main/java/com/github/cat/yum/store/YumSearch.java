package com.github.cat.yum.store;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.sqlite.SqlUtils;

public class YumSearch {
	private static Logger log = LoggerFactory.getLogger(YumSearch.class);
	
	public static void search(File storeXml, String rpmName, String version, String arch) {
		YumStore yumStore = new YumStore(storeXml);
		yumStore.init();
		
		Entry search = new Entry();
		search.name = rpmName;
		if(!StringUtils.isBlank(version)){
			search.flags = "EQ";
			search.release = version;
		}
		
		if(!StringUtils.isBlank(arch)){
			search.name += "(" + arch + ")";
		}
		log.info("search start");
		SearchResult searchResult = yumStore.searchAndDownload(search);
		log.info("search success");
		log.info("zip start");
		String resultZip = SqlUtils.getUUId() + ".zip";
		if(YumStore.cachedir.exists()){
			resultZip = YumStore.cachedir.getAbsolutePath() + File.separator + resultZip;
		}
		ZipUtil.packEntries(searchResult.rpms.toArray(new File[]{}) , new File(resultZip));
		log.info("zip success");
		log.info("result zip file path:" + resultZip);
	}
}
