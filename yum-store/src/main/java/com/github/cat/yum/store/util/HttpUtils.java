package com.github.cat.yum.store.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpUtils {
	
	private static Logger log = LoggerFactory.getLogger(HttpUtils.class);

	public static void dowloadFile(String baseUrl, String file, File target) throws ClientProtocolException, IOException {
		String url = baseUrl;
		if(baseUrl.endsWith("/")){
			url += file;
		}
		else{
			url += "/" + file;
		}
		
		log.info("download file :" + url);
		
		//throw new IOException("test");
		
		HttpClient client = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(url);  
        httpget.setHeader("Connection", "close");
        
		HttpResponse response = client.execute(httpget);
		int code = response.getStatusLine().getStatusCode();
		if(code != 200){
			throw new IOException("server response code is :" + code);
		}
		HttpEntity entity = response.getEntity();  
        InputStream is = entity.getContent();  
        FileUtils.copyInputStreamToFile(is, target);
        log.info("download file :" + url + " success");
	}

}
