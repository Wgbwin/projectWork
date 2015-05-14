package com.kodakalaris.kodakmomentslib.bean.items;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.kodakalaris.kodakmomentslib.util.Log;

/**
 * 
 * @author Kane
 * 
 */
public class PrintItem extends ShoppingCartItem {

	private static final String TAG = PrintItem.class.getSimpleName();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private PhotoInfo image;
	private ROI roi;

	public boolean isUseRedEye;
	public boolean isUseEnhance;
	public boolean isCheckedInstance;
	public boolean isServerImage;
	public ColorEffect colorEffect;
	public int rotateDegree;

	public PrintItem(PhotoInfo photoInfo, RssEntry entry) {
		this.image = photoInfo;
		this.entry = entry;
		roi = ImageUtil.calculateDefaultRoi(photoInfo, this);
		this.count = 1;
		if (roi == null) {
			Log.e(TAG, "ROI is null, terrible thing would be happen !!!!");
		}
	}

	public ROI getRoi() {
		return roi;
	}

	public void setRoi(ROI roi) {
		this.roi = roi;
	}

	public PhotoInfo getImage() {
		return image;
	}

	@Override
	public String thumbUri() {
		return image.getLocalUri();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime
				* result
				+ ((entry.proDescription == null) ? 0 : entry.proDescription
						.hashCode());
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
		PrintItem other = (PrintItem) obj;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image)) {
			return false;
		}
		if (!entry.proDescription.equals(other.entry.proDescription)) {
			return false;
		}
		return true;
	}

}
