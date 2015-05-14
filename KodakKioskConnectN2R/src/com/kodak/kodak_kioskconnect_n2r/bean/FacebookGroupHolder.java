package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;
import java.util.List;

public class FacebookGroupHolder implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4044993251321622135L;
	private List<FacebookGroup> groups ;

	public List<FacebookGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<FacebookGroup> groups) {
		this.groups = groups;
	}

}
