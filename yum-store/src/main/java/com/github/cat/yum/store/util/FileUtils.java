package com.github.cat.yum.store.util;

import java.io.File;


public class FileUtils extends org.apache.commons.io.FileUtils{
	
	
	public static String getFileRelativePath(String parentPath, String childPath){
		if(!childPath.startsWith(parentPath)){
			throw new IllegalArgumentException(childPath + "is not " + parentPath + " child");
		}
		return childPath.substring(parentPath.length() + 1);
	}
	
	public static String getFileRelativePath(File parent, File child){
		String parentPath = parent.getAbsolutePath();
		String childPath = child.getAbsolutePath();
		return getFileRelativePath(parentPath, childPath);
	}
	
	public static String getFileRelativePath(String parentPath, File child){
		String childPath = child.getAbsolutePath();
		return getFileRelativePath(parentPath, childPath);
	}
	
	public static String getFileRelativePath(File parent, String childPath){
		String parentPath = parent.getAbsolutePath();
		return getFileRelativePath(parentPath, childPath);
	}

}
