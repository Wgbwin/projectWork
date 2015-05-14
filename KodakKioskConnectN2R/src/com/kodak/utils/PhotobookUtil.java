package com.kodak.utils;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.quickbook.database.ThumbnailProvider;

public class PhotobookUtil {

	/**
	 * Get photobook by id from List in AppContext
	 * @param photobookId
	 * @return
	 */
	public static Photobook getPhotobookFromList(String photobookId){
		if(photobookId==null || "".equals(photobookId)){
			return null;
		}
		List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
		if(photobooks != null){
			for(Photobook photobook : photobooks){
				if(photobookId.equals(photobook.id)){
					return photobook;
				}
			}
		}
		return null;
	}
	
	/**
	 * initial Photobook Database and List
	 * @param context
	 */
	public static void clearPhotobooksData(Context context){
		ThumbnailProvider.obtainInstance(context).clearMiniCach();
		ThumbnailProvider.obtainInstance(context).createMiniTable();
		AppContext.getApplication().getPhotobooks().clear();
	}
}
