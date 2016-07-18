package com.github.cat.yum.store.filter;

public class PrivateRequireFilter implements YumFileter{

	@Override
	public boolean filter(String name) {
		if(name.startsWith("rpmlib(")){
			return true;
		}
		return false;
	}

}
