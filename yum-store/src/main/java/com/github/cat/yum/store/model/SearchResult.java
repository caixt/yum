package com.github.cat.yum.store.model;

import java.util.ArrayList;
import java.util.List;
import com.github.cat.yum.store.YumSearch;

public class SearchResult {
	public List<PackageRpmMetadata> rpms;
	
	public List<Entry> provides;
	
	public SearchResult(){
		rpms = new ArrayList<>();
		provides = new ArrayList<>();
	}
	
	
	public List<Entry> addRpm(PackageRpmMetadata rpm){
		List<Entry> requires = new ArrayList<>();
		if(rpms.contains(rpm)){
			return new ArrayList<>();
		}
		rpms.add(rpm);
		provides.addAll(rpm.provide);
		for(Entry require : rpm.require){
			boolean contain = YumSearch.verificationEntry(require, provides);
			if(!contain){
				requires.add(require);
			}
		}
		return requires;
	}
	
}
