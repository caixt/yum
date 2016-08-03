package com.github.cat.yum.store.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFile {

	
    public static String getsum(File file, String algorithm) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest messageDigest = MessageDigest.getInstance(formateAlgorithm(algorithm));
        FileInputStream fis = new FileInputStream(file);
  
        byte[] data = new byte[1024];
        int read = 0; 
        while ((read = fis.read(data)) != -1) {
        	messageDigest.update(data, 0, read);
        };
        byte[] hashBytes = messageDigest.digest();
  
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        String fileHash = sb.toString();
        
        fis.close();
        return fileHash;
    }
    
    
	private static String formateAlgorithm(String algorithm){
		switch (algorithm) {
			case "SHA224":
			case "sha224":
				return "SHA-224";
			case "SHA256":
			case "sha256":
				return "SHA-256";
			case "SHA384":
			case "sha384":
				return "SHA-384";
			case "SHA512":
			case "sha512":
				return "SHA-512";
			default:
				return algorithm;
		}
	}
}
