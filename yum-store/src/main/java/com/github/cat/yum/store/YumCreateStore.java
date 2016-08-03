package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.util.FileUtils;
import com.github.cat.yum.store.util.YumUtil;

public class YumCreateStore {
	
	private static Logger log = LoggerFactory.getLogger(YumCreateStore.class);
	
	private File root = null;

	public YumCreateStore(File dir){
		if(!dir.exists() || !dir.isDirectory()){
			throw new YumException(dir + " is not directory or not exists");
		}
		root = null;
	}
	
	
	public void initRepodata(){
		String rootPath = root.getAbsolutePath();
		File repoDataDir = new File(rootPath + File.separator + YumUtil.REPOPATH);
		
		if(repoDataDir.exists()){
			try {
				FileUtils.deleteDirectory(repoDataDir);
				FileUtils.forceMkdir(repoDataDir);
			} catch (IOException e) {
				throw new YumException(repoDataDir + " delete or create fail ", e);
			}
		}
	    try {
	    	log.info("init repodata start.");
			YumUtil.createRepoData(root);
			log.info("init repodata success.");
		} catch (NoSuchAlgorithmException | IOException e) {
			log.info("init repodata error.");
			throw new YumException("create repodata error", e);
		}
	}
}
