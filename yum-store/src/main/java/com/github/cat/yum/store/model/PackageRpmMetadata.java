package com.github.cat.yum.store.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import static com.github.cat.yum.store.util.YumUtil.COMMONNAMESPACE;
import static com.github.cat.yum.store.util.YumUtil.RPMNAMESPACE;

public class PackageRpmMetadata extends RpmMetadata {
	
	public String algorithm;
	
	public String checkSum;
	
	public String location;
	
	public PackageRpmMetadata(Element packageElement){
		name = packageElement.getChild("name", COMMONNAMESPACE).getText();
		Element checksum = packageElement.getChild("checksum", COMMONNAMESPACE);
		this.algorithm = checksum.getAttributeValue("type");
		this.checkSum = checksum.getText();
		this.location = packageElement.getChild("location", COMMONNAMESPACE).getAttributeValue("href");
		this.require = new ArrayList<>();
		Element format = packageElement.getChild("format", COMMONNAMESPACE);
		Element provides = format.getChild("provides", RPMNAMESPACE);
		this.provide = initEntry(provides.getChildren());
		Element requires = format.getChild("requires", RPMNAMESPACE);
		if(requires != null){
			this.require = initEntry(requires.getChildren());
		}
		else{
			this.require = new ArrayList<>();
		}
	}
	
	
	private List<Entry> initEntry(List<Element> entryElements){
		List<Entry> entrys = new ArrayList<>();
		for(Element entryElement : entryElements){
			Entry entry = new Entry();
			entry.name = entryElement.getAttributeValue("name");
			if(name.startsWith("rpmlib(")){
				continue;
			}
			String flags = entryElement.getAttributeValue("flags");
			if(!StringUtils.isBlank(flags)){
				entry.flags = flags;
			}
			String epoch = entryElement.getAttributeValue("epoch");
			if(!StringUtils.isBlank(epoch)){
				entry.epoch = epoch;
			}
			String ver = entryElement.getAttributeValue("ver");
			if(!StringUtils.isBlank(ver)){
				entry.version = ver;
			}
			String rel = entryElement.getAttributeValue("rel");
			if(!StringUtils.isBlank(rel)){
				entry.version = rel;
			}
			String pre = entryElement.getAttributeValue("pre");
			if(!StringUtils.isBlank(pre)){
				entry.pre = pre;
			}
			entrys.add(entry);
		}
		return entrys;
	}

}
