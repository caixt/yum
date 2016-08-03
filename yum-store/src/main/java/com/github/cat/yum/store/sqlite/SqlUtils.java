package com.github.cat.yum.store.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class SqlUtils {
	
	private static DataSourcePool pool = DataSourcePool.getInstance();
	
	public static void inintTables() throws SQLException{
		Connection conn = pool.getConnection();
		Statement stat = conn.createStatement();
//		stat.executeUpdate("CREATE TABLE packages (  pkgKey varchar(32) PRIMARY KEY,  pkgId TEXT,  name varchar(32),  "
//				+ "arch TEXT,  version TEXT,  epoch TEXT,  release TEXT,  summary TEXT,  description TEXT,  "
//				+ "url TEXT,  time_file INTEGER,  time_build INTEGER,  rpm_license TEXT,  rpm_vendor TEXT,  "
//				+ "rpm_group TEXT,  rpm_buildhost TEXT,  rpm_sourcerpm TEXT,  rpm_header_start INTEGER,  "
//				+ "rpm_header_end INTEGER,  rpm_packager TEXT,  size_package INTEGER,  size_installed INTEGER,  "
//				+ "size_archive INTEGER,  location_href TEXT,  location_base TEXT,  checksum_type TEXT);");
//		stat.executeUpdate("CREATE TABLE files (name TEXT,  type TEXT,  pkgKey INTEGER);");
//		stat.executeUpdate("CREATE TABLE provides (  name TEXT,  flags TEXT,  epoch TEXT,  version TEXT,  release TEXT,  pkgKey INTEGER );");
//		stat.executeUpdate("CREATE TABLE requires (  name TEXT,  flags TEXT,  epoch TEXT,  version TEXT,  release TEXT,  pkgKey INTEGER , pre BOOL DEFAULT FALSE);");
		stat.executeUpdate( "CREATE TABLE db_info (dbversion INTEGER, checksum varchar(5));" );
		
		stat.executeUpdate( "insert into db_info values('LiSi','aaaaaaaaaa');" );
	}
	
	
	
	
	
	
	
	public static String getUUId(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}

