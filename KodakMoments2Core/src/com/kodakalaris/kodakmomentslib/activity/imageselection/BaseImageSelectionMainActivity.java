package com.kodakalaris.kodakmomentslib.activity.imageselection;

import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;

public abstract class BaseImageSelectionMainActivity extends BaseNetActivity{
	protected ImageSelector mImageSelector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(KM2Application.getInstance().getFlowType().isPrintWorkFlow()){
			mImageSelector = new PrintImageSelector(this);
		}else if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()){
			mImageSelector = new PrintHubImageSelector(this);
		}
		
		setKMContentView();
		initViews();
		initData();
		setEvents();
		
		
	}
	
	protected abstract void setKMContentView();
	protected abstract void setEvents() ;
	protected abstract void initData() ;
	protected abstract void initViews() ;
	
	public ImageSelector getmImageSelector(){
		return mImageSelector;
	}
	


}
