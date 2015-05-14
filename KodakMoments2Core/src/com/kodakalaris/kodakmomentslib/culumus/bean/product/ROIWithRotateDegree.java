package com.kodakalaris.kodakmomentslib.culumus.bean.product;

import java.io.Serializable;

public class ROIWithRotateDegree implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int rotateDegree;
	private ROI roi;

	public int getRotateDegree() {
		return rotateDegree;
	}

	public void setRotateDegree(int rotateDegree) {
		this.rotateDegree = rotateDegree;
	}

	public ROI getRoi() {
		return roi;
	}

	public void setRoi(ROI roi) {
		this.roi = roi;
	}
}
