package com.kodakalaris.kodakmomentslib.culumus.bean.product;

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(ContainerH);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(ContainerW);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(h);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(w);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ROI other = (ROI) obj;
		if (Double.doubleToLongBits(ContainerH) != Double
				.doubleToLongBits(other.ContainerH))
			return false;
		if (Double.doubleToLongBits(ContainerW) != Double
				.doubleToLongBits(other.ContainerW))
			return false;
		if (Double.doubleToLongBits(h) != Double.doubleToLongBits(other.h))
			return false;
		if (Double.doubleToLongBits(w) != Double.doubleToLongBits(other.w))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
	
}
