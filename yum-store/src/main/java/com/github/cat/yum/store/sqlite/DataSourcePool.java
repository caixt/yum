package com.github.cat.yum.store.sqlite;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class DataSourcePool {
	
	public static String dbFilePath = "yum-store.sqlite";
	
	private BasicDataSource ds = null;
	
	private static DataSourcePool dataSourcePool = null;
	
	private DataSourcePool(){
		ds = new BasicDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:" + dbFilePath);
		ds.setInitialSize(3);
		ds.setMaxActive(10);
		ds.setMaxIdle(10);
		ds.setPoolPreparedStatements(true);
	}

	public static synchronized DataSourcePool getInstance(){
		if(dataSourcePool == null){
			return new DataSourcePool();
		}
		return dataSourcePool;
	}
	
	public DataSource getDataSource() {
		return ds;
	}
	
	public Connection getConnection() throws SQLException{
		return ds.getConnection();
	}
}
