package com.kodak.rss.tablet.util;

import java.util.Date;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.RelativeLayout.LayoutParams;

import com.kodak.rss.tablet.adapter.CanZoomBaseAdapter;
import com.kodak.rss.tablet.facebook.AdpaterConstant;

/**
 * Purpose: Set GridView Parameter and Zoom 
 * Author: Bing Wang
 * Created Time: Oct 10 2013 
 */
public class GridViewParamSetUtil {	

	private double defaultMargin = 24;//60
	private double minMargin = 12;
	private double leftMargin,topMargin;	
	private int currentColumnsNum;			
	private double imagesHSpace;
	private double columnWidth;	
	private int orgVSpace = 2;
	
	private double sourcesMargin;
	private double sourcesHSpace;
	
	private double orgColumnsWidth;
	private double maxcolumnWidth;
	private double minColumnWidth;

	Context mContext;
	GridView mGridView;
	CanZoomBaseAdapter adapter;
	private LayoutParams  GridView_lp;
	
	DisplayMetrics dm;
	int screenWidth;
	int screenHeight;
	
	private int adapterTpyeFlag = 1;
	
	public int highlightPosition = 0;
	
	public GridViewParamSetUtil(Context context,GridView gridView) {
		this.mContext  = context;
		this.mGridView = gridView;
			
		GridView_lp = (LayoutParams) gridView.getLayoutParams();
		dm = mContext.getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		defaultMargin = dm.density*defaultMargin;
		minMargin = dm.density*minMargin;
		imagesHSpace = screenWidth / 100;
		orgVSpace = (int) (dm.density * orgVSpace + 0.5f);
		topMargin = dm.density*orgVSpace;
		
		maxcolumnWidth = Math.floor((screenWidth - imagesHSpace -2*minMargin)/2);			
		orgColumnsWidth = Math.floor((screenWidth - 5*imagesHSpace -2*defaultMargin)/6);	
		minColumnWidth= Math.floor((screenWidth - 7*imagesHSpace -2*minMargin)/8);	
				
	}
	int mTotalNum;
	public void initGridViewMargin(int totalNum,final int adapterTpye,CanZoomBaseAdapter baseAdapter){	
		adapterTpyeFlag = adapterTpye;
		adapter = baseAdapter;		
		if(totalNum < 1 ) return;				
		mTotalNum = totalNum ;							
		
		int rowItemNum = 1;		
		if (totalNum >= 6) {
			rowItemNum = 6;
		}else {
			rowItemNum = totalNum;
		}				
		currentColumnsNum = rowItemNum;						
		switch (adapterTpyeFlag) {
			case AdpaterConstant.SOURCES_ADAPTER_TPYE:							
				sourcesHSpace = 3*imagesHSpace;											
				columnWidth = orgColumnsWidth;
				sourcesMargin = (screenWidth - (rowItemNum -1)* sourcesHSpace - rowItemNum*columnWidth)/2;													
				setGridViewParameter(topMargin, sourcesMargin, rowItemNum, columnWidth, sourcesHSpace);									
				adapter.setItemWidth((int) (columnWidth - dm.density*30));	//20
				break;				
		
			case AdpaterConstant.PHOTOS_ADAPTER_TPYE:
			case AdpaterConstant.IMAGE_ADAPTER_TPYE:
			case AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE:
			case AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE: 						
			case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE: 
			case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:
			case AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE: 				
			case AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE:
			case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE: 				
			case AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE: 				
			case AdpaterConstant.FBK_GROUP_ALBUMS_ADAPTER_TPYE:
			case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE: 	
				
				columnWidth = orgColumnsWidth;			
				leftMargin = (screenWidth - (currentColumnsNum -1)* imagesHSpace-currentColumnsNum*columnWidth)/2;																		
				setGridViewParameter(topMargin, leftMargin, currentColumnsNum, columnWidth, imagesHSpace);	
				mGridView.setVerticalSpacing(orgVSpace);
				
				switch (adapterTpyeFlag) {
				case AdpaterConstant.PHOTOS_ADAPTER_TPYE:				
				case AdpaterConstant.FBK_MAIN_ALBUMS_ADAPTER_TPYE: 
				case AdpaterConstant.FBK_FRIEND_ALBUMS_ADAPTER_TPYE: 	
				case AdpaterConstant.FBK_GROUP_ALBUMS_ADAPTER_TPYE: 	
					adapter.setItemWidth((int) (columnWidth - dm.density*40));
					break;
				case AdpaterConstant.FBK_PHOTOS_ADAPTER_TPYE:
				case AdpaterConstant.FBK_FRIENDS_ADAPTER_TPYE:				
				case AdpaterConstant.FBK_GROUPS_ADAPTER_TPYE:	
					adapter.setItemWidth((int) (columnWidth - dm.density*20));						
					break;	
				case AdpaterConstant.IMAGE_ADAPTER_TPYE:
				case AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE: 
				case AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE:				 
				case AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE: 
				case AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE:
					adapter.setItemWidth((int) columnWidth);	
					break;
				}
				break;
		}						
	}	
	
	private void setGridViewParameter(double topMargin,double leftMargin,int columnsNum,double columnWidth,double imagesHSpace){		
		if(topMargin > 0){
			GridView_lp.bottomMargin = (int) topMargin;
			GridView_lp.topMargin = (int) topMargin;
		}		
		GridView_lp.leftMargin = (int) leftMargin;
		GridView_lp.rightMargin = (int) leftMargin;
		mGridView.setLayoutParams(GridView_lp);																							
		mGridView.setNumColumns(columnsNum);
		mGridView.setColumnWidth((int) columnWidth);
		mGridView.setHorizontalSpacing((int) imagesHSpace);		
	}
	
	public OnTouchListener gridViewController = new OnTouchListener() {
		double starteddistance, scaleFactor = 1.0;
		long mulOnTouchtime = 0;			

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int pointCnt = event.getPointerCount();
			if (pointCnt > 1) {
				int action = (event.getAction() & MotionEvent.ACTION_MASK);				
				switch (action) {
				case MotionEvent.ACTION_MOVE:
					if (adapterTpyeFlag == AdpaterConstant.IMAGE_ADAPTER_TPYE || adapterTpyeFlag ==AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE ||adapterTpyeFlag ==AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE
							||adapterTpyeFlag ==AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE|| adapterTpyeFlag ==AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {	
						try {
							double currentDistance = spacing(event);
							scaleFactor = currentDistance / starteddistance;
							if ((scaleFactor - 1.0) == 0 ) {
								mulOnTouchtime = new Date().getTime();
								return true;
							}
							if (scaleFactor > 1) {
								scaleFactor = 2 * scaleFactor;
							}else {
								scaleFactor = 0.5* scaleFactor;
							}
							starteddistance = currentDistance;		
							leftMargin = leftMargin /scaleFactor;
							
							if (mTotalNum < 3) {
								double minMarginO = (screenWidth - (mTotalNum-1)*imagesHSpace - mTotalNum*maxcolumnWidth)/2;
								double maxMarginO = (screenWidth - (mTotalNum-1)*imagesHSpace - mTotalNum*minColumnWidth)/2;
								if (leftMargin < minMarginO-1 ) {
									leftMargin = minMarginO;
								}else if (leftMargin > maxMarginO+1 ){
									leftMargin = maxMarginO;
								}
							}else if (mTotalNum >= 3){
								double maxMarginO = 0;
								if (mTotalNum <= 8) {
									maxMarginO = (screenWidth - (mTotalNum-1)*imagesHSpace - mTotalNum*minColumnWidth)/2;	
								}								
								if (scaleFactor > 1.0  && currentColumnsNum == 2) {
									leftMargin = minMargin;
								}else if (scaleFactor < 1.0  && currentColumnsNum == 8){
									leftMargin = defaultMargin;
								}else {
									if (leftMargin < minMargin - 1 ) {
										if (currentColumnsNum > 2) {
											currentColumnsNum = currentColumnsNum - 1;
										}
										leftMargin = minMargin;
									}else if (leftMargin > defaultMargin + 1 ){
										if (mTotalNum <= 8) {
											if (mTotalNum >= currentColumnsNum + 1 && currentColumnsNum < 8) {
												currentColumnsNum = currentColumnsNum + 1;
												leftMargin = defaultMargin;
											}else if (leftMargin > maxMarginO+1 ){											
												leftMargin = maxMarginO;												
											}
										}else {
											if (mTotalNum >= currentColumnsNum + 1 && currentColumnsNum < 8) {
												currentColumnsNum = currentColumnsNum + 1;
											}
											leftMargin = defaultMargin;	
										}									
									}
								}
							}
	
							columnWidth = (screenWidth -2*leftMargin - (currentColumnsNum -1)*imagesHSpace)/ currentColumnsNum;																																									
							setGridViewParameter(topMargin,leftMargin,currentColumnsNum,columnWidth,imagesHSpace);																																																				
							mGridView.setSelection(highlightPosition);	
							adapter.setRange((int) columnWidth);						
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}else {
						return false;
					}
 				    break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_1_UP:
				case MotionEvent.ACTION_POINTER_2_UP:
					mulOnTouchtime = new Date().getTime();
					scaleFactor = 1.0;
					break;
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_1_DOWN:
				case MotionEvent.ACTION_POINTER_2_DOWN:
					if (adapterTpyeFlag == AdpaterConstant.IMAGE_ADAPTER_TPYE || adapterTpyeFlag ==AdpaterConstant.FBK_MAIN_IMAGES_ADAPTER_TPYE ||adapterTpyeFlag ==AdpaterConstant.FBK_FRIEND_IMAGES_ADAPTER_TPYE
							||adapterTpyeFlag ==AdpaterConstant.FBK_MAIN_PHOTOS_ADAPTER_TPYE|| adapterTpyeFlag ==AdpaterConstant.FBK_GROUP_IMAGES_ADAPTER_TPYE) {	
						starteddistance = spacing(event);
					}else {
						return false;
					}
				default:
					break;
				}				
				return true;
			} else {
				if ((new Date().getTime() - mulOnTouchtime) > 50) {
					return false;
				}
				return true;
			}
		}
	};

	private float spacing(MotionEvent event) {
		float x = 0;
		float y = 0;
		try {
			 x = event.getX(0) - event.getX(1);
			 y = event.getY(0) - event.getY(1);
		} catch (Exception e) {			
			 x = 0;
			 y = 1;
		}		
		return FloatMath.sqrt(x * x + y * y);
	}
		
}
