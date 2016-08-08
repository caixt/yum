package com.github.cat.yum.store.model;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringUtils;

import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.sqlite.DataSourcePool;
import com.github.cat.yum.store.sqlite.SqlUtils;

public class SearchResult {
	public List<java.io.File> rpms;
	
	public List<Entry> provides;
	
	public List<File> files;
	
	public SearchResult(){
		rpms = new ArrayList<>();
		provides = new ArrayList<>();
		files = new ArrayList<>();
	}
	
	//rpm null,表示过滤,但提供provides和files
	public List<Entry> addRpm(Store store, String packagekey, java.io.File rpm){
		DataSource ds = DataSourcePool.getPool(store);
		provides.addAll(SqlUtils.selectList(ds, "select * from provides where packagekey=?", new BeanListHandler<>(Entry.class), packagekey));
		files.addAll(SqlUtils.selectList(ds, "select * from files where packagekey=?", new BeanListHandler<>(File.class), packagekey));
		if(null == rpm){
			return new ArrayList<>();
		}
		rpms.add(rpm);
		return SqlUtils.selectList(ds, "select * from requires where packagekey=?", new BeanListHandler<>(Entry.class), packagekey);
	}


	public boolean check(Entry search) {
		for(Entry entry : provides){
			if(search.name.equals(entry.name) && YumStore.verificationEntry(search, entry.version)){
				return true;
			}
		}
		if(StringUtils.isBlank(search.flags)){
			for(File file : files){
				if(search.name.equals(file.path)){
					return true;
				}
			}
			
		}
		return false;
	}


	
}
