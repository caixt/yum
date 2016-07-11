package com.github.cat.yum.store.model;

import java.util.List;

public class RpmMetadata {
	  public String sha1Digest;
	  public String artifactRelativePath;
	  public long lastModified;
	  public long size;
	  public int headerStart;
	  public int headerEnd;
	  public String name;
	  public String architecture;
	  public String version;
	  public int epoch;
	  public String release;
	  public String summary;
	  public String description;
	  public String packager;
	  public String url;
	  public int buildTime;
	  public int installedSize;
	  public int archiveSize;
	  public String license;
	  public String vendor;
	  public String sourceRpm;
	  public String buildHost;
	  public String group;
	  public List<Entry> provide;
	  public List<Entry> require;
	  public List<Entry> conflict;
	  public List<Entry> obsolete;
	  public List<File> files;
	  public List<ChangeLog> changeLogs;
}
