package com.kodakalaris.kodakmomentslib.bean.items;

import java.io.Serializable;

import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;

/**
 * 
 * @author Kane
 *
 */
public abstract class ShoppingCartItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RssEntry entry;
	protected int count;
	protected String serverId;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public RssEntry getEntry() {
		return entry;
	}
	
	public String type(){
		return entry.proDescription.type;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public abstract String thumbUri();
}
