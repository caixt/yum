package com.github.cat.yum.store.base;

public enum Basearch {
	x86_64("x86_64"),i386("i686"),no("noarch");
	
	private String arch;
	
	
	private  Basearch(String arch){
		this.arch = arch;
	}
	
	public String getArch(){
		return arch;
	}
}
