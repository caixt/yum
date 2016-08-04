package com.github.cat.yum.store.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jdom2.Element;
import com.github.cat.yum.store.filter.PrivateRequireFilter;
import com.github.cat.yum.store.filter.YumFileter;
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
		this.files = new ArrayList<File>();
		List<Element> files = format.getChildren("file", COMMONNAMESPACE);
		for(Element fileElement : files){
			File file = new File();
			file.path = fileElement.getTextTrim();
			String type = fileElement.getAttributeValue("type");
			if(!StringUtils.isBlank(type)){
				file.type = type;
			}
			this.files.add(file);
		}
	}
	
	
	private List<Entry> initEntry(List<Element> entryElements){
		YumFileter filter = new PrivateRequireFilter();
		List<Entry> entrys = new ArrayList<>();
		for(Element entryElement : entryElements){
			String name = entryElement.getAttributeValue("name");
			if(filter.filter(name)){
				continue;
			}
			Entry entry = new Entry();
			entry.name = name;
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
