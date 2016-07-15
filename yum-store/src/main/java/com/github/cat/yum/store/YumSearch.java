package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import org.jdom2.JDOMException;
import com.github.cat.yum.store.model.Store;

public class YumSearch {
	public static void search(File storeXml, String rpmName) throws JDOMException, IOException{
		YumStore yumStore = new YumStore(storeXml);
		yumStore.initStore();
		
		for(Store store : yumStore.getStores()){
			if(!store.enable){
				continue;
			}
		}
		
		
	}
}
