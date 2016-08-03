package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.base.YumStore;

public class YumCleanCache {
	private static Logger log = LoggerFactory.getLogger(YumCleanCache.class);
	
	public static void clean(File storeXml) {
		YumStore yumStore = new YumStore(storeXml);
		try {
			log.info("clean start.");
			yumStore.clean();
			log.info("clean sucess.");
		} catch (IOException e) {
			log.info("clean fail.");
			throw new YumException("clean cache fail ", e);
		}
	}
}
