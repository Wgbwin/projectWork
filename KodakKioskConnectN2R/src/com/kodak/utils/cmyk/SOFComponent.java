package com.kodak.utils.cmyk;

import java.io.Serializable;

public class SOFComponent {

	public int id;
	public int hSub;
	public int vSub;
	public int qtSel;

	public SOFComponent(int id, int hSub, int vSub, int qtSel) {
		this.id = id;
		this.hSub = hSub;
		this.vSub = vSub;
		this.qtSel = qtSel;
	}

	@Override
	public String toString() {		
		Serializable idStr = (id >= 'a' && id <= 'z' || id >= 'A' && id <= 'Z') ? "'"+ (char) id + "'": id;
		return String.format("id: %s, sub: %d/%d, sel: %d", idStr, hSub, vSub,qtSel);
	}
}