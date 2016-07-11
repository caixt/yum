package com.github.cat.yum.store.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFile {

	
    public static String getsum(File file, String algorithm) throws NoSuchAlgorithmException, IOException
    {
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
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
}
