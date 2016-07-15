package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;

import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.RpmMetadata;
import com.github.cat.yum.store.model.Store;
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
		RpmScan scan = new RpmScan(new File("D:\\git\\boltdog\\build\\boltdog\\store\\unzip-6.0-15.el7.x86_64.rpm"));
		RpmMetadata rpmMetadata = scan.getRpmMetadata();
		for(Entry entry : rpmMetadata.require){
			System.out.println(entry.toString());
		}
	}
	
	
	@Test
	public void yumsearch() throws IOException, JDOMException, NoSuchAlgorithmException {
		//MessageDigest.getInstance("sha-256");
		YumSearch.search(new File("yum-store.xml"), "unzip");
	}
	
	
	
	
	
}
