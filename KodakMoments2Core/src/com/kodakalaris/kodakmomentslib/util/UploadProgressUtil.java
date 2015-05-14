package com.kodakalaris.kodakmomentslib.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;

public class UploadProgressUtil {
	
	public static List<PhotoInfo> getAllUploadImages(Context context){
		List<PhotoInfo> allPhotos = new ArrayList<PhotoInfo>();
		
		List<PhotoInfo> photos = PrintManager.getInstance(context).getmPrintPhotos();
		if(photos!=null && photos.size()>0){
			allPhotos.addAll(photos);
		}
		return allPhotos;
	}
	
	public static int getUploadPicSuccessNum(List<PhotoInfo> list){
		int mSuccessNum = 0;
		if (list == null) return 0 ;	
		Iterator<PhotoInfo> itor = list.iterator();
		synchronized (list) {	
			while (itor.hasNext()) {
				PhotoInfo photoInfo = (PhotoInfo) itor.next();
				if(photoInfo.getImageResource()!=null && photoInfo.getPhotoUploadingState().isUploadedSuccess()){
					mSuccessNum++;
				}
			}
		}
		
		return mSuccessNum;
	}
	
	
	public static long getLastUploadTime(List<PhotoInfo> list){
		long lastUploadTime = 0;
		if (list == null) return lastUploadTime ;	
		Iterator<PhotoInfo> itor = list.iterator();
		synchronized (list) {	
			while (itor.hasNext()) {
				PhotoInfo photoInfo = (PhotoInfo) itor.next();
				if(!photoInfo.getPhotoUploadingState().isInital()){
					if (lastUploadTime < photoInfo.getUploadOriginalTime()) {
						lastUploadTime = photoInfo.getUploadOriginalTime();
					}
				}
			}
		}
		if(lastUploadTime==0){
			lastUploadTime = System.currentTimeMillis();
		}
		
		return lastUploadTime;
	}
	
	
	
	public static PhotoInfo getCurrentUploadingPhoto(Context context){
		List<PhotoInfo> allPhotos = getAllUploadImages(context);
		if(allPhotos!=null && allPhotos.size()>0){
			for (PhotoInfo photoInfo : allPhotos) {
				if(photoInfo.getPhotoUploadingState().isUploading()){
					return photoInfo;
				}
					
			}
		}
		
		return null;
		
	}
	
	public static List<PhotoInfo> getUploadFailedPhotos(Context context){
		List<PhotoInfo> uploadFailedPhotos = new ArrayList<PhotoInfo>();
		List<PhotoInfo> allPhotos = getAllUploadImages(context);
		if(allPhotos!=null && allPhotos.size()>0){
			for (PhotoInfo photoInfo : allPhotos) {
				if(photoInfo.getPhotoUploadingState().isUploadedFailed()){
					uploadFailedPhotos.add(photoInfo);
				}
			}
		}
		return uploadFailedPhotos;
		
	}
	
	
	
}
