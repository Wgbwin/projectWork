package com.kodakalaris.video.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;

public class CustomProgressView extends FrameLayout{
	private ProgressBar vProgressBar ;
	private CircleProgressBar vCircleProgressBar ;
	private TextView vTextViewProgress ;
	
	public CustomProgressView(Context context) {
		this(context, null);
	}
	public CustomProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0) ;
	}
	
	public CustomProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.custom_progress_view, this);
		vProgressBar = (ProgressBar) view.findViewById(R.id.pbSharing) ;
		vCircleProgressBar = (CircleProgressBar) view.findViewById(R.id.circleprogressbar) ;
		vTextViewProgress = (TextView) view.findViewById(R.id.progress_text) ;
		
	}

	
	/**
	 * set circleProgressBar progress value
	 * @param value
	 */
	private void setProgress(int value){
		vCircleProgressBar.setProgress(value) ;
	}
	
	public void updateCircleProgressBar(int value){
		showCircleProgressBar(true) ;
		setProgress(value) ;
		setProgressValueText(value) ;
		
	}
	
	
	public void finishCircleProgressBar(){
		setProgress(0) ;
		setProgressValueText(0) ;
		showProgressText(false) ;
		showCircleProgressBar(false) ;
		
	}
	
	
	public void showProgressBar(int visible){
		vProgressBar.setVisibility(visible) ;
	}
	
	
	
	private void showProgressText(boolean isVisible){
		if(isVisible){
			vTextViewProgress.setVisibility(View.VISIBLE) ;
		}else {
			vTextViewProgress.setVisibility(View.GONE) ;
		}
	}
	
	private void setProgressValueText(int progress){
		showProgressText(true) ;
		vTextViewProgress.setText(progress+"%") ;
	}
	
	
	private void showCircleProgressBar(boolean isVisible){
		if(isVisible){
			
			vCircleProgressBar.setVisibility(View.VISIBLE) ;
			
		}else {
			vCircleProgressBar.setVisibility(View.GONE) ;
		}
	}
	
	
	
}
