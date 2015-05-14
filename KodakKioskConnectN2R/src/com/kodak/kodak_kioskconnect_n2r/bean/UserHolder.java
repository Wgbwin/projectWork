package com.kodak.kodak_kioskconnect_n2r.bean;

import java.io.Serializable;
import java.util.List;

public class UserHolder implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5654608004995891139L;
	private List<User> users ;

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

}
