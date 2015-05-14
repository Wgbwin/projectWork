package com.kodakalaris.video.fragments;

import android.support.v4.app.Fragment;

import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodakalaris.video.views.TMSImageCheckBoxView;

public interface ICommunicatingForTMS {
	public void repalceWithNewFragment(Fragment newFragment) ;
	
	public void onPhotoSelected(TMSImageCheckBoxView imageView ,PhotoInfo photo) ;
	
	public void onPhotoDoubleClick(TMSImageCheckBoxView imageView ,AlbumInfo album , PhotoInfo photo) ;
	
	public void onViewPagerPhotoDoubleClick() ;
	
	

}
