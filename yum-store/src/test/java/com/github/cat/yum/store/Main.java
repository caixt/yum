package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import com.github.cat.yum.store.base.RpmScan;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.RpmMetadata;
import com.github.cat.yum.store.util.VersionStringUtils;


public class Main {
	
	@Test
	public void testSearch(){
		Launcher.main(new String[]{"retrive", "centos", "7", "x86_64", "unzip"});
	}
	
	@Test
	public void testCleanCache(){
		Launcher.main(new String[]{"cleanCache", "-c", "conf/yum-store.xml"});
	}
	
	@Test
	public void testCreateRepo() {
		Launcher.main(new String[]{"createRepo", "yum-store"});
	}
	
	@Test
	public void testHelp() {
		Launcher.main(new String[]{"retrive", "-help"});
	}
	
	@Test
	public void rpmcan() throws IOException {
		RpmScan scan = new RpmScan(new File("yum-store\\centos\\7\\os\\x86_64\\Packages\\unzip-6.0-15.el7.x86_64.rpm"));
		RpmMetadata rpmMetadata = scan.getRpmMetadata();
		for(Entry entry : rpmMetadata.require){
			System.out.println(entry.toString());
		}
	}
	
	
//	@Test
//	public void yumsearch() {
//		YumRetrive.(new File("conf/yum-store.xml"), "unzip","6.0","x86-64", null);
//	}
	
	@Test
	public void yumclean() {
		YumCleanCache.clean(new File("conf/yum-store.xml"));
	}
	
	
	@Test
	public void test2(){
		System.out.println(VersionStringUtils.compare("1.12.1", "1.9.10"));
		System.out.println(VersionStringUtils.compare("1.12.10A", "1.12.8A"));
		System.out.println(VersionStringUtils.compare("1.8.1", "1.10A"));
		System.out.println(VersionStringUtils.compare("1.8.1", null));
		System.out.println(VersionStringUtils.compare(null, "1.8.1"));
		System.out.println(VersionStringUtils.compare(null, null));
		
		List<Entry> list = new ArrayList<Entry>();
		Entry e = new Entry();
		e.version = "1.8.1";
		list.add(e);
		e = new Entry();
		e.version = "1.10.1";
		list.add(e);
		e = new Entry();
		e.version = null;
		list.add(e);
		
		
		 Collections.sort(list, new Comparator<Entry>() {
	            public int compare(Entry e1, Entry e2) {
	                return -VersionStringUtils.compare(e1.version, e2.version);
	            }
	        });
		 
		 for(int i = 0; i < list.size(); i++){
			 System.out.println(list.get(i).version);
		 }
	}
	
	@Test
	public void test3(){
		System.out.println(Pattern.matches("^rpmlib\\(.*", "rpmlib(CompressedFileNames)"));
	}
	
	@Test
	public void test4(){
		System.out.println(getHost("https://127.0.0.1/aaa"));
	}
	
	public static String getHost(String url){
		  if(url==null||url.trim().equals("")){
		   return "";
		  }
		  String host = "";
		  Pattern p =  Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
		  Matcher matcher = p.matcher(url);  
		  if(matcher.find()){
		   host = matcher.group();  
		  }
		  return host;
	}
}
