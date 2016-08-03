package com.github.cat.yum.store.sqlite;


import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;

public class DataSourcePool {
	
	private BasicDataSource ds = null;
	
	private static DataSourcePool dataSourcePool = null;
	
	private DataSourcePool(){
		ds = new BasicDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:yum-store.sqlite");
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
	
	public Connection getConnection() throws SQLException{
		return ds.getConnection();
	}
}
