package com.github.cat.yum.store.model;

public class Entry {
	public String name;
	public String flags;
	public String epoch;
	public String version;
	public String release;
	public String pre;
	@Override
	public String toString() {
		return "Entry [name=" + name + ", flags=" + flags + ", epoch=" + epoch
				+ ", version=" + version + ", release=" + release + ", pre="
				+ pre + "]";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFlags() {
		return flags;
	}
	public void setFlags(String flags) {
		this.flags = flags;
	}
	public String getEpoch() {
		return epoch;
	}
	public void setEpoch(String epoch) {
		this.epoch = epoch;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getRelease() {
		return release;
	}
	public void setRelease(String release) {
		this.release = release;
	}
	public String getPre() {
		return pre;
	}
	public void setPre(String pre) {
		this.pre = pre;
	}
	
}
