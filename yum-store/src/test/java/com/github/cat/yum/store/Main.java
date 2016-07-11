package com.github.cat.yum.store;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.github.cat.yum.store.util.YumUtil;

public class Main {
	
	@Test
	public void createRepoData () {
		File file = new File("yum-store" + File.separator + "centos" + File.separator + "7" + File.separator + "os" + File.separator + "x86_64");
		file = file.getAbsoluteFile();
		Assert.assertTrue(YumUtil.createRepoData(file));
	}
}
