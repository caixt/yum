package com.github.cat.yum.store.model;

import com.github.cat.yum.store.base.Basearch;

public class Store {
	public String host;
	
	public String baseUrl;
	
	public String os;
	
	public String releasever;
	
	public Basearch basearch;
	
	public boolean enable;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getReleasever() {
		return releasever;
	}

	public void setReleasever(String releasever) {
		this.releasever = releasever;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Basearch getBasearch() {
		return basearch;
	}

	public void setBasearch(Basearch basearch) {
		this.basearch = basearch;
	}
}
