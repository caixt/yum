package com.github.cat.yum.store.base;

public class YumException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
    public YumException(String message) {
        super(message);
    }
    
    public YumException(Throwable cause) {
        super(cause);
    }


//    public YumException(String message, Throwable cause) {
//        super(message, cause);
//    }

}
