package com.github.cat.yum.store.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import com.github.cat.yum.store.base.YumException;

public class SqlUtils {
	
	
	public static void inintTables(DataSource ds){
		Connection conn = null;
		try{
			conn = ds.getConnection();
			conn.setAutoCommit(false);
			String[] sqls = new String[]{
				"CREATE TABLE package (key PRIMARY KEY, arch, version, name, algorithm, checkSum, location);",
				"CREATE TABLE provides (packagekey, name,  flags,  epoch,  version,  release);",
				"CREATE TABLE requires (packagekey, name,  flags,  epoch,  version,  release);",
				"CREATE TABLE files (packagekey, path, type)",
				
		        "CREATE INDEX packages_key_index ON package (key);",
		        "CREATE INDEX provides_name_index on provides (name);",
		        "CREATE INDEX requires_name_index on requires (name);",
		        "CREATE INDEX files_path_index on files (path);"
			};
		
			for(String sql : sqls){
				update(conn, sql);
			}
			conn.commit();
		}catch(SQLException e){
			try {
				conn.rollback();
			} catch (SQLException e1) {
				throw new YumException(e1);
			}
			throw new YumException(e);
		}finally{
			try{
				if(conn != null){
					conn.setAutoCommit(true);
					conn.close();
				}
			}catch (SQLException ignore) {
				
			}
		}
	}
	
	
	
	public static <T> List<T> selectList(DataSource ds, String sql, BeanListHandler<T> handler, Object ...params){
		try{
			QueryRunner qr = new QueryRunner(ds);
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static List<Map<String, Object>> selectList(DataSource ds, String sql, MapListHandler handler, Object ...params){
		try{
			QueryRunner qr = new QueryRunner(ds);
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static <T> T select(DataSource ds, String sql, BeanHandler<T> handler, Object ...params) {
		try{
			QueryRunner qr = new QueryRunner(ds);
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static Map<String, Object> select(DataSource ds, String sql, MapHandler handler, Object ...params) {
		try{
			QueryRunner qr = new QueryRunner(ds);
			return qr.query(sql, handler, params); 
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static int update(DataSource ds, String sql, Object ...params){
		try{
			QueryRunner qr = new QueryRunner(ds);
			return qr.update(sql, params);
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static int update(Connection conn, String sql, Object ...params){
		try{
			QueryRunner qr = new QueryRunner();
			return qr.update(conn, sql, params);
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static int[] batch(Connection conn, String sql, Object[][] params){
		try{
			QueryRunner qr = new QueryRunner();
			return qr.batch(conn, sql, params);
		}catch(SQLException e){
			throw new YumException(e);
		}
	}
	
	public static String getUUId(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	

		
	public DataSource getDataSourcePool(String db){
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:" + db);
		ds.setInitialSize(1);
		ds.setMaxActive(1);
		ds.setMaxIdle(10);
		ds.setPoolPreparedStatements(true);
		return ds;
	}
		
		
}

