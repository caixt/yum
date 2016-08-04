package com.github.cat.yum.store.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.cat.yum.store.base.YumException;

public class SqlUtils {
	
	private static DataSourcePool pool = DataSourcePool.getInstance();
	
	
	public static Connection getConnection(){
		Connection conn;
		try {
			conn = pool.getConnection();
			return conn;
		} catch (SQLException e) {
			throw new YumException("get Connection error", e);
		}
	}
	
	public static void inintTables() throws SQLException{
		Connection conn = pool.getConnection();
		conn.setAutoCommit(false);
		String[] sqls = new String[]{
				"CREATE TABLE store (key PRIMARY KEY, baseUrl);",
				"CREATE TABLE package (key PRIMARY KEY, storekey, name, algorithm, checkSum, location);",
				"CREATE TABLE provides (packagekey, name,  flags,  epoch,  version,  release);",
				"CREATE TABLE requires (packagekey, name,  flags,  epoch,  version,  release);",
				"CREATE TABLE files (packagekey, path, type)"
		};
		
		try{
			for(String sql : sqls){
				update(conn, sql);
			}
		}finally{
			conn.commit();
			conn.setAutoCommit(true);
			conn.close();
		}
		
//		stat.executeUpdate("CREATE store (key PRIMARY KEY, baseUrl);");	
//		stat.executeUpdate("CREATE TABLE packages (  pkgKey varchar(32) PRIMARY KEY,  pkgId TEXT,  name varchar(32),  "
//				+ "arch TEXT,  version TEXT,  epoch TEXT,  release TEXT,  summary TEXT,  description TEXT,  "
//				+ "url TEXT,  time_file INTEGER,  time_build INTEGER,  rpm_license TEXT,  rpm_vendor TEXT,  "
//				+ "rpm_group TEXT,  rpm_buildhost TEXT,  rpm_sourcerpm TEXT,  rpm_header_start INTEGER,  "
//				+ "rpm_header_end INTEGER,  rpm_packager TEXT,  size_package INTEGER,  size_installed INTEGER,  "
//				+ "size_archive INTEGER,  location_href TEXT,  location_base TEXT,  checksum_type TEXT);");
//		stat.executeUpdate("CREATE TABLE files (name TEXT,  type TEXT,  pkgKey INTEGER);");
//		stat.executeUpdate("CREATE TABLE provides (  name TEXT,  flags TEXT,  epoch TEXT,  version TEXT,  release TEXT,  pkgKey INTEGER );");
//		stat.executeUpdate("CREATE TABLE requires (  name TEXT,  flags TEXT,  epoch TEXT,  version TEXT,  release TEXT,  pkgKey INTEGER , pre BOOL DEFAULT FALSE);");
//		stat.executeUpdate( "CREATE TABLE db_info (dbversion INTEGER, checksum varchar(5));" );
//		stat.executeUpdate( "insert into db_info values('LiSi','aaaaaaaaaa');" );
	}
	
	
	
	public static <T> List<T> selectList(String sql, BeanListHandler<T> handler, Object ...params){
		try{
			QueryRunner qr = new QueryRunner(pool.getDataSource());
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(sql + " error", e);
		}
	}
	
	public static List<Map<String, Object>> selectList(String sql, MapListHandler handler, Object ...params){
		try{
			QueryRunner qr = new QueryRunner(pool.getDataSource());
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(sql + " error", e);
		}
	}
	
	public static <T> T selectList(String sql, BeanHandler<T> handler, Object ...params) {
		try{
			QueryRunner qr = new QueryRunner(pool.getDataSource());
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(sql + " error", e);
		}
	}
	
	public static Map<String, Object> selectList(String sql, MapHandler handler, Object ...params) {
		try{
			QueryRunner qr = new QueryRunner(pool.getDataSource());
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(sql + " error", e);
		}
	}
	
	public static int update(String sql, Object ...params) throws SQLException{
		QueryRunner qr = new QueryRunner(pool.getDataSource());
		return qr.update(sql, params);
	}
	
	public static int update(Connection conn, String sql, Object ...params) throws SQLException{
		QueryRunner qr = new QueryRunner();
		return qr.update(conn, sql, params);
	}
	
	public static int[] batch(Connection conn, String sql, Object[][] params) throws SQLException{
		QueryRunner qr = new QueryRunner();
		return qr.batch(conn, sql, params);
	}
	
	public static String getUUId(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}

