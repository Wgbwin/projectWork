package com.kodak.rss.tablet.handler;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseHaveISMActivity;
import com.kodak.rss.tablet.adapter.ImageAdapter;
import com.kodak.rss.tablet.adapter.PhotosAdapter;
import com.kodak.rss.tablet.facebook.AdpaterConstant;
import com.kodak.rss.tablet.facebook.HandlerConstant;
import com.kodak.rss.tablet.util.GridViewParamSetUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;

public class GetNativeGraphicsHandler extends Handler{

	private Context mContenxt;
	private BaseHaveISMActivity activity;
	private GridViewParamSetUtil gridViewParamUtil;
	private OnGetImageOnNativeListener onGetImageOnNativeListener;
		
	private SortableHashMap<Integer, String[]> imageBuckets;
	
	public PhotosAdapter photosAdapter;
	public ImageAdapter imageAdapter;
	
	private int photosSize;
	private String displayStr;
	private int selectPhotosPostion;
	private String nativeAllPhotos = "/All Photos";
	
	public GetNativeGraphicsHandler(Context context,GridViewParamSetUtil gridViewParamUtil) {
		super();
		this.mContenxt = context;
		this.activity = (BaseHaveISMActivity) context;
		this.gridViewParamUtil = gridViewParamUtil;
		
		nativeAllPhotos = "/"+context.getString(R.string.native_all_photos);
	}
	
	public void setOnGetIamgeOnNativeListener(OnGetImageOnNativeListener listener){
		this.onGetImageOnNativeListener = listener;
	}
	
	@Override
	public void handleMessage(Message msg) {
		if (activity == null) return;
		if (activity.isFinishing()) return;	
		final int action = msg.what;
		switch (action) {
		case 0:
			if (activity.collection != null && activity.collection.size() > 0 && activity.adapterTpyeFlag == AdpaterConstant.SOURCES_ADAPTER_TPYE) {
				setBackViewVisible(false);	
				setBackAllViewVisible(false);
				photosAdapter = new PhotosAdapter(mContenxt,activity.collection,activity.mMemoryCache);				
				activity.adapterTpyeFlag = AdpaterConstant.SOURCES_ADAPTER_TPYE;								
				gridViewParamUtil.initGridViewMargin(2,activity.adapterTpyeFlag,activity.sourcesAdapter);	
				activity.panelContent.setOnTouchListener(gridViewParamUtil.gridViewController);
				activity.photoGridView.setOnTouchListener(gridViewParamUtil.gridViewController);
				activity.photoGridView.setAdapter(activity.sourcesAdapter);				
				activity.sourceNameButton.setText("");
			}
			break;
		case 1:
			setBackViewVisible(false);
			setBackAllViewVisible(false);
			photosAdapter = new PhotosAdapter(mContenxt,activity.collection,activity.mMemoryCache);
			photosSize = activity.collection.size();
			activity.adapterTpyeFlag = AdpaterConstant.SOURCES_ADAPTER_TPYE;						
			gridViewParamUtil.initGridViewMargin(2,activity.adapterTpyeFlag,activity.sourcesAdapter);	
			activity.panelContent.setOnTouchListener(gridViewParamUtil.gridViewController);
			activity.photoGridView.setOnTouchListener(gridViewParamUtil.gridViewController);
			activity.photoGridView.setAdapter(activity.sourcesAdapter);					
			activity.sourceNameButton.setText("");
			break;				
		}
	}	
	
	public void onItemClickOnNative(View view,int position){
		switch (activity.adapterTpyeFlag) {				
		case AdpaterConstant.SOURCES_ADAPTER_TPYE:			
			displayStr = activity.sourcesBucket.valueAt(position);	
			String SourceKey = activity.sourcesBucket.keyAt(position);
			if (SourceKey == null) break;
			if (AppConstants.FB_SOURCE.equalsIgnoreCase(SourceKey)) {				
				//Localytics
				if (!activity.selectFacebook) {
					activity.selectFacebook =true;
					HashMap<String,String> map = new HashMap<String, String>();
					map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_FACEBOOK_SOURCE, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_NATIVE_SOURCE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
					RSSLocalytics.recordLocalyticsEvents(activity, RSSTabletLocalytics.LOCALYTICS_EVENT_SELECT_SOURCE_TYPE, map);											
				}
				activity.facebookGraphicsHandler.sendEmptyMessage(HandlerConstant.GET_FBK_TOKEN);		
			} else if (AppConstants.NATIVE_SOURCE.equalsIgnoreCase(SourceKey)){				
				//Localytics
				if (!activity.selectNative) {
					activity.selectNative =true;
					HashMap<String,String> map = new HashMap<String, String>();
					map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_NATIVE_SOURCE, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_FACEBOOK_SOURCE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
					RSSLocalytics.recordLocalyticsEvents(activity, RSSTabletLocalytics.LOCALYTICS_EVENT_SELECT_SOURCE_TYPE, map);											
				}				
				setBackViewVisible(true);
				if (photosSize > 0) {
					setBackAllViewVisible(false);
					activity.adapterTpyeFlag = AdpaterConstant.PHOTOS_ADAPTER_TPYE;				
					gridViewParamUtil.initGridViewMargin(photosSize,activity.adapterTpyeFlag,photosAdapter);
					activity.photoGridView.setAdapter(photosAdapter);
					activity.sourceNameButton.setText(nativeAllPhotos);		
				}else {
					activity.popUpPrompt(R.string.photos_no_photos_find);
				}				
			}
			break;
		case AdpaterConstant.PHOTOS_ADAPTER_TPYE:
			setBackAllViewVisible(true);
			if (photosAdapter != null) {
				photosAdapter.cancelRequest();
			}	
			selectPhotosPostion = position;
			imageBuckets = activity.collection.valueAt(position);						
			imageAdapter = new ImageAdapter(mContenxt,imageBuckets,null,activity.flowType,activity.mMemoryCache);			
			imageAdapter.notifyDataSetChanged();					
			activity.adapterTpyeFlag = AdpaterConstant.IMAGE_ADAPTER_TPYE;					
			gridViewParamUtil.initGridViewMargin(imageBuckets.size(),activity.adapterTpyeFlag,imageAdapter);	
			activity.photoGridView.setAdapter(imageAdapter);
			
			if (activity.allSelectButton != null) {	
				Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				int photoSize = imageBuckets.size();
				int selectedPhotoSize = 0;
				for (int i = 0; i < photoSize; i++) {
					imageAdapter.isDeal[i] = true;
					int key = imageBuckets.keyAt(i);						
					int pos = activity.getPositionInList(currentPhotoBook.chosenpics, String.valueOf(key));
					if (pos != -1) {
						imageAdapter.isChice[i] = true;	
						selectedPhotoSize++;
					}			
				}						
				photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
				if (photoSize > activity.maxselectPhotoSize) {
					activity.isOutMaxSelectNum = true;
					activity.allSelectButton.setVisibility(View.GONE);
				}											
			}

			displayStr=nativeAllPhotos+"/"+ imageBuckets.valueAt(0)[1];
			activity.sourceNameButton.setText(displayStr);
			break;
		case AdpaterConstant.IMAGE_ADAPTER_TPYE:			
			onGetImageOnNativeListener.onGetImageOnNative(view,imageBuckets,position);
			break;
		}
	}

	public void onClickBackOnNative(){		
		switch (activity.adapterTpyeFlag) {			
		case AdpaterConstant.PHOTOS_ADAPTER_TPYE:				
			setBackViewVisible(false);
			setBackAllViewVisible(false);
			if (photosAdapter != null) {
				photosAdapter.cancelRequest();
			}		
			activity.adapterTpyeFlag = AdpaterConstant.SOURCES_ADAPTER_TPYE;
			gridViewParamUtil.initGridViewMargin(2,activity.adapterTpyeFlag,activity.sourcesAdapter);	
			activity.photoGridView.setAdapter(activity.sourcesAdapter);			
			activity.sourceNameButton.setText("");
			break;
			
		case AdpaterConstant.IMAGE_ADAPTER_TPYE:
			setBackViewVisible(true);
			setBackAllViewVisible(false);
			if (imageAdapter != null) {
				imageAdapter.cancelRequest();
			}			
			activity.adapterTpyeFlag = AdpaterConstant.PHOTOS_ADAPTER_TPYE;
			gridViewParamUtil.initGridViewMargin(photosSize,activity.adapterTpyeFlag,photosAdapter);	
			activity.photoGridView.setAdapter(photosAdapter);
			activity.sourceNameButton.setText(nativeAllPhotos);
			activity.photoGridView.setSelection(selectPhotosPostion);
			break;
		}	
	}
		
	public interface OnGetImageOnNativeListener {		
		public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets,int position);		
		public void onGetAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets);
		public void onDeleteAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets);
	}
	
	public void onClickGetAll(){
		onGetImageOnNativeListener.onGetAllImageOnNative(imageBuckets);
	}
	
	public void onClickDeleteAll(){
		onGetImageOnNativeListener.onDeleteAllImageOnNative(imageBuckets);
		if (activity.allSelectButton != null) {
			activity.allSelectButton.setVisibility(View.VISIBLE);
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			int photoSize = imageBuckets.size();
			int selectedPhotoSize = 0;
			for (int i = 0; i < photoSize; i++) {
				int key = imageBuckets.keyAt(i);
				int pos = activity.getPositionInList(currentPhotoBook.chosenpics, String.valueOf(key));
				if (pos != -1) {
					selectedPhotoSize++;
				}			
			}						
			photoSize = photoSize + currentPhotoBook.chosenpics.size() - selectedPhotoSize;																		
			if (photoSize > activity.maxselectPhotoSize) {
				activity.isOutMaxSelectNum = true;
				activity.allSelectButton.setVisibility(View.GONE);
			}											
		}
	}
	
	private void setBackViewVisible(boolean isVisible){
		if (isVisible) {
			activity.backButton.setVisibility(View.VISIBLE);
			activity.backButtonName.setVisibility(View.VISIBLE);		
		}else {
			activity.backButton.setVisibility(View.GONE);
			activity.backButtonName.setVisibility(View.GONE);			
		}
	}
	
	private void setBackAllViewVisible(boolean isVisible){
		if (activity.allSelectButton != null) {
			activity.allSelectButton.setEnabled(true);
			activity.allDeleteButton.setEnabled(true);
			if (isVisible) {
				activity.allSelectButton.setVisibility(View.VISIBLE);				
				activity.allDeleteButton.setVisibility(View.VISIBLE);				
			}else {
				activity.allSelectButton.setVisibility(View.GONE);
				activity.allDeleteButton.setVisibility(View.GONE);			
			}			
		}
	}		
}
