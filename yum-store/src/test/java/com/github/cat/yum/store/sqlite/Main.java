package com.github.cat.yum.store.sqlite;


import java.sql.SQLException;
import org.junit.Test;


public class Main {
	
	@Test
	public void test1 () throws SQLException {
		DataSourcePool pool = DataSourcePool.getInstance();
		System.out.println(pool.getConnection());
	}
	
	
	@Test
	public void test2 () throws SQLException {
		SqlUtils.inintTables();
	}
	
}
