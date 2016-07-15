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
	
}
