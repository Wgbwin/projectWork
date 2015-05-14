package com.kodak.rss.core.exception;

import com.kodak.rss.core.util.Log;

/**
 * Exception for rss webservice
 * @author Robin
 *
 */
public class RssWebServiceException extends Exception{
	private static final long serialVersionUID = 1L;
	private static final String TAG = "RssWebServiceException";
	
	public static final int TYPE_SERVER = 1;
	public static final int TYPE_NETWORK_TIMEOUT = 2;
	public static final int TYPE_NETWORK = 3;
	public static final int TYPE_APP_OBSOLETE = 4;
	
	private int type;
	private String code = "";
	private String message = "";
	private Exception exception;
	
	private RssWebServiceException(int type, String code, String message,Exception exception){
		this.type = type;
		this.code = code;
		this.message = message;
		this.exception = exception;
	}
	
	public int getType(){
		return type;
	}
	
	public String getCode(){
		return code;
	}
	
	public String getMessage(){
		return message;
	}
	
	public Exception getException(){
		return exception;
	}
	
	public boolean isNetworkWeak(){
		return type == TYPE_NETWORK_TIMEOUT || type == TYPE_NETWORK;
	}
	
	public boolean isServerError(){
		return type == TYPE_SERVER;
	}
	
	public boolean isAppObsolete() {
		return type == TYPE_APP_OBSOLETE;
	}
	
	@Override
	public void printStackTrace() {
		StringBuilder s = new StringBuilder();
		if(isNetworkWeak()){
			s.append("Network weak error");
		}
		
		if(isServerError()){
			s.append("Server Error\n");
			if(message != null && message.length()>0){
				s.append(message);
			}
		}
		Log.e(TAG, s.toString());
		super.printStackTrace();
	}
	
	public static RssWebServiceException networkTimeout(Exception e){
		return new RssWebServiceException(TYPE_NETWORK_TIMEOUT, "", "",null);
	}
	
	public static RssWebServiceException network(Exception e){
		return new RssWebServiceException(TYPE_NETWORK, "", "", e);
	}
	
	public static RssWebServiceException server(String code, String message){
		return new RssWebServiceException(TYPE_SERVER, code, message,null);
	}
	
	public static RssWebServiceException appObsolete(String redirectUrl) {
		return new RssWebServiceException(TYPE_APP_OBSOLETE, "", redirectUrl, null);
	}
	
}
