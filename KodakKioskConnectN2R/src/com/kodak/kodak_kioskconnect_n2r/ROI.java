package com.kodak.kodak_kioskconnect_n2r;

import java.io.Serializable;

public class ROI implements Cloneable,Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8490210506177594624L;
	public double x;
	public double y;
	public double w;
	public double h;

	public double ContainerW;
	public double ContainerH;
	public ROI()
	{
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		ROI clone = (ROI) super.clone();
		return clone;
	}

	@Override
	public String toString() {
		String toString = "ROI[x:"+x+", y:"+y+", w:"+w+", h:"+h+", ContainerW:"+ContainerW+", ContainerH:"+ContainerH+"]";
		return toString;
	}
	
	
}
