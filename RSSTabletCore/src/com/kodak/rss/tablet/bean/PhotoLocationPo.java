package com.kodak.rss.tablet.bean;

import android.view.View;

import com.kodak.rss.core.n2r.bean.prints.Layer;

public class PhotoLocationPo {
	
	public PhotoLocationPo(int hPosition, int position, int x, int y, int width, int height,Layer layer,View photoView) {
		this.hPosition = hPosition;	
		this.position = position;	
		this.x = x;
		this.y = y;		
		this.width = width;
		this.height = height;	
		this.layer = layer;
		this.view = photoView;
		
	}
	
	public int hPosition;
	public int position;
	public int x;	
	public int y;	
	public int height;
	public int width;
	public View view;
	public Layer layer;
	
	/**isBack 1  isFront 0*/
	public int isFront;
	
}
