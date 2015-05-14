package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;

public class User implements Serializable ,Comparable<User>{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2892680867244820486L;
	private String mUserId ;
    private String mName ;
    private String mProfilePictureUrl ; 
    
 
    
    public String getmUserId() {
		return mUserId;
	}

	public void setmUserId(String mUserId) {
		this.mUserId = mUserId;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmProfilePictureUrl() {
		return mProfilePictureUrl;
	}

	public void setmProfilePictureUrl(String mProfilePictureUrl) {
		this.mProfilePictureUrl = mProfilePictureUrl;
	}

	@Override
	public int compareTo(User another) {
		
		return this.getmName().compareTo(another.getmName());
	}

	
    
    
}
