package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.RpmMetadata;
import com.github.cat.yum.store.util.VersionStringUtils;
import com.github.cat.yum.store.util.YumUtil;

public class Main {
	
	@Test
	public void createRepoData () {
		File file = new File("yum-store" + File.separator + "centos" + File.separator + "7" + File.separator + "os" + File.separator + "x86_64");
		file = file.getAbsoluteFile();
		Assert.assertTrue(YumUtil.createRepoData(file));
	}
	
	@Test
	public void rpmcan() throws IOException {
		RpmScan scan = new RpmScan(new File("E:\\git\\cxt\\yum\\yum-store\\yum-store\\store\\unzip-6.0-15.el7.x86_64.rpm"));
		RpmMetadata rpmMetadata = scan.getRpmMetadata();
		for(Entry entry : rpmMetadata.require){
			System.out.println(entry.toString());
		}
	}
	
	
	@Test
	public void yumsearch() throws IOException, JDOMException, NoSuchAlgorithmException {
		//MessageDigest.getInstance("sha-256");
		YumSearch.search(new File("yum-store.xml"), "bash");
	}
	
	
	@Test
	public void test2(){
		System.out.println(VersionStringUtils.compare("1.12.1", "1.9.10"));
		System.out.println(VersionStringUtils.compare("1.12.10A", "1.12.8A"));
		System.out.println(VersionStringUtils.compare("1.8.1", "1.10A"));
	}
	
	
}
