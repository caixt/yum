package com.github.cat.yum.store.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
		
		HttpClient client = null;
		
		if(baseUrl.startsWith("https://")){
			SSLContext sslContext;
			try {
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				    //信任所有
				    public boolean isTrusted(X509Certificate[] chain,
				                    String authType) throws CertificateException {
				        return true;
				    } 

				}).build();
			} catch (Exception e) {
				throw new IOException(e);
			}
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
			client = HttpClientBuilder.create().setSSLSocketFactory(sslsf).build();
		}
		else{
			client = HttpClients.createDefault();
		}
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
