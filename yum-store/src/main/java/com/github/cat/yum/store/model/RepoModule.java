package com.github.cat.yum.store.model;

public class RepoModule {
	
	private String rootPath;
	
	private String module;
	
	private String xmlCode;
	
	private Long xmlSize;
	
	private String xmlGzCode;
	
	private Long xmlGzSize;
	
	public RepoModule() {
		super();
	}
	
	public RepoModule(String rootPath, String module) {
		super();
		this.rootPath = rootPath;
		this.module = module;
	}

	public RepoModule(String module, String xmlCode, Long xmlSize, String xmlGzCode, Long xmlGzSize) {
		super();
		this.module = module;
		this.xmlCode = xmlCode;
		this.xmlSize = xmlSize;
		this.xmlGzCode = xmlGzCode;
		this.xmlGzSize = xmlGzSize;
	}

	public String getXmlCode() {
		return xmlCode;
	}

	public void setXmlCode(String xmlCode) {
		this.xmlCode = xmlCode;
	}

	public Long getXmlSize() {
		return xmlSize;
	}

	public void setXmlSize(Long xmlSize) {
		this.xmlSize = xmlSize;
	}

	public String getXmlGzCode() {
		return xmlGzCode;
	}

	public void setXmlGzCode(String xmlGzCode) {
		this.xmlGzCode = xmlGzCode;
	}

	public Long getXmlGzSize() {
		return xmlGzSize;
	}

	public void setXmlGzSize(Long xmlGzSize) {
		this.xmlGzSize = xmlGzSize;
	}
	
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

}
