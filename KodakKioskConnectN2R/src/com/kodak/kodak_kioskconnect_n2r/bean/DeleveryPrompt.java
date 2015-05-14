package com.kodak.kodak_kioskconnect_n2r.bean;

public class DeleveryPrompt {
	private String productIdentifier ;
	private String deleveryPrompt ;
	public DeleveryPrompt(String productIdentifier, String deleveryPrompt) {
		super();
		this.productIdentifier = productIdentifier;
		this.deleveryPrompt = deleveryPrompt;
	}
	public String getProductIdentifier() {
		return productIdentifier;
	}
	public void setProductIdentifier(String productIdentifier) {
		this.productIdentifier = productIdentifier;
	}
	public String getDeleveryPrompt() {
		return deleveryPrompt;
	}
	public void setDeleveryPrompt(String deleveryPrompt) {
		this.deleveryPrompt = deleveryPrompt;
	}
	
	
	
	

}
