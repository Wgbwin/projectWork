package com.kodakalaris.kodakmomentslib.exception;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.util.Log;

/**
 * Exception for Web API
 * @author Robin
 *
 */
public class WebAPIException extends Exception{
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
	
	protected WebAPIException(int type, String code, String message,Exception exception){
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
	
	public static WebAPIException networkTimeout(Exception e){
		return new WebAPIException(TYPE_NETWORK_TIMEOUT, "", "",null);
	}
	
	public static WebAPIException network(Exception e){
		return new WebAPIException(TYPE_NETWORK, "", "", e);
	}
	
	public static WebAPIException server(String code, String message){
		return new WebAPIException(TYPE_SERVER, code, message,null);
	}
	
	public static WebAPIException appObsolete(String redirectUrl) {
		return new WebAPIException(TYPE_APP_OBSOLETE, "", redirectUrl, null);
	}
	
	public void handleException(Context context) {
		if (context instanceof BaseActivity) {
			((BaseActivity) context).showErrorWaring(this);
		}
	}
	
}
