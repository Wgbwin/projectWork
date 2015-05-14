package com.kodakalaris.kodakmomentslib.activity.imageselection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMImageView;

public abstract class ImageSelector {
	protected List<PhotoInfo> mTempSelectedPhotos;
	protected List<KMImageView> mTempSelectedPhotoImageViewList;
	

	public ImageSelector(){
		mTempSelectedPhotos = new ArrayList<PhotoInfo>();
		mTempSelectedPhotoImageViewList = new ArrayList<KMImageView>();
	}
	
	
	public List<PhotoInfo> getmTempSelectedPhotos() {
		return mTempSelectedPhotos;
	}

	public void setmTempSelectedPhotos(List<PhotoInfo> mTempSelectedPhotos) {
		this.mTempSelectedPhotos = mTempSelectedPhotos;
	}
	
	public boolean addPhotoToTempSelectedList(PhotoInfo photo){
		boolean success = false ;
		if(mTempSelectedPhotos==null){
			mTempSelectedPhotos = new ArrayList<PhotoInfo>() ;
		}
		
		if(mTempSelectedPhotos.contains(photo)){
			return success ;
		}
		Iterator<PhotoInfo> itor = mTempSelectedPhotos.iterator();
		boolean isPhotoExist = false;
		while (itor.hasNext()) {
			PhotoInfo photoInfo = (PhotoInfo) itor.next();
			if(photo.equalsNotConsiderDesId(photoInfo)){
				isPhotoExist = true;
				break ;
			}
			
		}
		if(isPhotoExist){
			return success ;
		}
		success = this.mTempSelectedPhotos.add(photo) ;
		
		
		return success ;
	}
	
	public void removePhotoFromTempSelectedList(PhotoInfo photo){
		
		synchronized (mTempSelectedPhotos) {
			if(mTempSelectedPhotos!=null && mTempSelectedPhotos.size()>0){
				Iterator<PhotoInfo> itor = mTempSelectedPhotos.iterator();
				while (itor.hasNext()) {
					PhotoInfo photoInfo = (PhotoInfo) itor.next();
					if(photo.equalsNotConsiderDesId(photoInfo)){
						itor.remove();
					}
					
				}
			}
			
		}
		
	}
	
	
	public List<KMImageView> getmTempSelectedPhotoImageViewList() {
		return mTempSelectedPhotoImageViewList;
	}


	public void setmTempSelectedPhotoImageViewList(
			List<KMImageView> mTempSelectedPhotoImageViewList) {
		this.mTempSelectedPhotoImageViewList = mTempSelectedPhotoImageViewList;
	}
	
	
	public void addKMImageViewToList(KMImageView imageView){
		if(mTempSelectedPhotoImageViewList==null){
			mTempSelectedPhotoImageViewList = new ArrayList<KMImageView>() ;
		}
		if(!mTempSelectedPhotoImageViewList.contains(imageView)){
			mTempSelectedPhotoImageViewList.add(imageView);
		}
		
		
	}
	
	public boolean removeKMImageView(KMImageView imageView){
		return false;
	}
	
	public void removeImageViewChecked(PhotoInfo photo){
		if(mTempSelectedPhotoImageViewList==null){
			mTempSelectedPhotoImageViewList = new ArrayList<KMImageView>() ;
		}
		synchronized (mTempSelectedPhotoImageViewList) {
			if(mTempSelectedPhotoImageViewList.size()>0){
				Iterator<KMImageView> itor = mTempSelectedPhotoImageViewList.iterator();
				while(itor.hasNext()){
					KMImageView imageView = itor.next();
					PhotoInfo photoInView = imageView.getPhoto();
					if(photo.equalsNotConsiderDesId(photoInView)){
						imageView.setmSelected(photo.isSelected());
						itor.remove();
					}
				}
			}
		}
	}
	
	public void removeAllCheckedImageView(){
		if(mTempSelectedPhotoImageViewList==null){
			mTempSelectedPhotoImageViewList = new ArrayList<KMImageView>() ;
		}
		mTempSelectedPhotoImageViewList.clear();
	}
	
	public void destroy(){
		if(mTempSelectedPhotoImageViewList!=null){
			mTempSelectedPhotoImageViewList.clear();
			mTempSelectedPhotoImageViewList = null;
		}
		
		if(mTempSelectedPhotos!=null){
			mTempSelectedPhotos.clear();
			mTempSelectedPhotos=null;
		}
		
	}
	
	public abstract void selectPhoto(Context context,KMImageView imageView,PhotoInfo photo ) ;
	public abstract List<PhotoInfo> getSelectedPhotos(Context context);
}
