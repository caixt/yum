package com.github.cat.yum.store.sqlite;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;

import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.model.Store;

public class DataSourcePool {
	
	private static Map<String, BasicDataSource> dsMap = new HashMap<>();

	public static synchronized DataSource getPool(Store store){
		String key = YumStore.cachedir.getAbsolutePath() + File.separator + store.host + File.separator
				+ store.os + File.separator + store.releasever + File.separator + store.basearch;
		BasicDataSource ds = dsMap.get(key);
		if(ds != null){
			return ds;
		}
		File dbFile = new File(key);
		if(!dbFile.exists()){
			try {
				FileUtils.forceMkdir(dbFile);
			} catch (IOException e) {
				throw new YumException(e);
			}
		}
		ds = new BasicDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:" + key + File.separator + "db.sqlite");
		ds.setInitialSize(3);
		ds.setMaxActive(10);
		ds.setMaxIdle(10);
		ds.setPoolPreparedStatements(true);
		dsMap.put(key, ds);
		return ds;
	}
}
