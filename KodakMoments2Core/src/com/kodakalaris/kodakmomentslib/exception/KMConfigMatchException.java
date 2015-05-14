package com.kodakalaris.kodakmomentslib.exception;


public class KMConfigMatchException extends WebAPIException {
	private static final long serialVersionUID = 1L;
	public static final int MATCH_CODE = 304;
	
	public KMConfigMatchException() {
		super(WebAPIException.TYPE_SERVER, String.valueOf(MATCH_CODE), "data match", null);
	}
}
