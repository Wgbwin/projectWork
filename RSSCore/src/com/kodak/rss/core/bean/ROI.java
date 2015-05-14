package com.kodak.rss.core.bean;

import java.io.Serializable;

public class ROI implements Cloneable, Serializable{
	private static final long serialVersionUID = 1L;
	public double x;
	public double y;
	public double w;
	public double h;

	public double ContainerW;
	public double ContainerH;
	public ROI(){
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
	
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof ROI){
			ROI roi = (ROI) o;
			return roi.x == x 
				&& roi.y == y
				&& roi.w == w
				&& roi.h == h
				&& roi.ContainerH == ContainerH
				&& roi.ContainerW == ContainerW
				;
		}
		return false;
	}
	
}
