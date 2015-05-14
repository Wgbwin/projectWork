package com.kodak.kodak_kioskconnect_n2r.bean;

public class ProductEditPopItem {
	public int id = -1;
	public int strResId;
	public int imageResId = -1;
	public boolean visible = true;
	public boolean entryAble = false;
	public int type;//see PhotoBookEditPopView.type TODO
	
	public ProductEditPopItem(int id,int type,int strResId, int imageResId) {
		this(id, type,strResId, imageResId, true,false);
	}

	public ProductEditPopItem(int id,int type, int strResId, int imageResId,
			boolean visible) {
		this(id, type, strResId, imageResId, visible, false);
	}

	public ProductEditPopItem(int id,int type, int strResId, int imageResId,
			boolean visible, boolean entryAble) {
		this.id = id;
		this.type = type;
		this.strResId = strResId;
		this.imageResId = imageResId;
		this.visible = visible;
		this.entryAble = entryAble;
	}
	
}
