package com.github.cat.yum.store.filter;

import java.util.regex.Pattern;

public class PrivateFileDirFilter implements YumFileter{

	private static String[] REGEXS = new String[]{".*bin\\/.*", "^\\/etc\\/.*"};

	@Override
	public boolean filter(String name) {
		for(String regex : REGEXS){
			if(Pattern.matches(regex, name)){
				return false;
			}
		}
		return true;
	}

}
