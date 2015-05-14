package com.kodak.kodak_kioskconnect_n2r.bean;


import java.io.Serializable;

public class FacebookGroup implements Serializable ,Comparable<FacebookGroup>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4273475519553628685L;

	private String groupId ;
	
	private String groupName ;
	
	private boolean isAdministrator ;
	
	private Integer bookmarkOrder ;
	
	public FacebookGroup(){
		
	}
	

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isAdministrator() {
		return isAdministrator;
	}

	public void setAdministrator(boolean isAdministrator) {
		this.isAdministrator = isAdministrator;
	}

	public Integer getBookmarkOrder() {
		return bookmarkOrder;
	}

	public void setBookmarkOrder(int bookmarkOrder) {
		this.bookmarkOrder = bookmarkOrder;
	}


	@Override
	public int compareTo(FacebookGroup another) {
		// TODO Auto-generated method stub
		return this.getBookmarkOrder().compareTo(another.getBookmarkOrder());
	}
	

	

}
