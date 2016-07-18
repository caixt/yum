package com.github.cat.yum.store.filter;

import java.util.regex.Pattern;

public class PrivateFileFilter implements YumFileter{
	
	private static String[] REGEXS = new String[]{".*bin\\/.*", "^\\/etc\\/.*", "^\\/usr\\/lib\\/sendmail$"};

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
