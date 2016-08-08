package com.github.cat.yum.store.sqlite;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Test;
import com.github.cat.yum.store.model.Store;


public class Main {
	
//	@Test
//	public void test1() throws SQLException {
//		DataSourcePool pool = DataSourcePool.getInstance();
//		System.out.println(pool.getConnection());
//	}
//	
//	
//	@Test
//	public void test2() throws SQLException {
//		SqlUtils.inintTables();
//	}
//	
//	@Test
//	public void test3() throws SQLException {
//		System.out.println(SqlUtils.select("select count(*) from sqlite_master", new MapHandler()));
//	}
//	
//	@Test
//	public void test4() throws SQLException {
//		DataSourcePool pool = DataSourcePool.getInstance();
//		Connection conn = pool.getConnection();
//		conn.setAutoCommit(false);
//		try{
//			System.out.println(SqlUtils.update(conn, "insert into db_info values(?,?)", 1,2));
//			System.out.println(SqlUtils.update(conn, "insert into db_info values(?,?)", 1,2));
//		}finally{
//			conn.commit();
//			conn.setAutoCommit(true);
//			conn.close();
//		}
//	}
//	
//	
//	@Test
//	public void test5() throws SQLException {
//		DataSourcePool pool = DataSourcePool.getInstance();
//		Connection conn = pool.getConnection();
//		conn.setAutoCommit(false);
//		try{
//			List<Object[]> data = new ArrayList<>();
//			data.add(new Object[]{1, 2});
//			data.add(new Object[]{1, 2});
//			SqlUtils.batch(conn, "insert into db_info values(?,?)", data.toArray(new Object[][]{}));
//		}finally{
//			conn.commit();
//			conn.setAutoCommit(true);
//			conn.close();
//		}
//	}
//	
//	@Test
//	public void test6(){
//		Store dbStore = SqlUtils.select("select key from store where key = ?", new BeanHandler<Store>(Store.class), "zju.edu.cn");
//		System.out.println(dbStore.key);
//	}
//	
//	
//	
//	@Test
//	public void test7(){
//		List<Map<String, Object>> datas =  SqlUtils.selectList("SELECT * FROM store order by key desc", new MapListHandler());
//		System.out.println(datas);
//	}
}
