package com.kodakalaris.kodakmomentslib.interfaces;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMImageView;

public interface IPhotoOperationInterface {
	
	
	void onPhotoClick(KMImageView imageView,PhotoInfo photo);
	
	void onPhotoLongClick(KMImageView imageView ,PhotoInfo photo);
	

}
