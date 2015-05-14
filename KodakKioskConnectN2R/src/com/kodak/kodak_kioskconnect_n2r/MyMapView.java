package com.kodak.kodak_kioskconnect_n2r;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class MyMapView extends MapView{
	private GeoPoint lastMapCenter;
	private GeoPoint firstMapCenter;
    private boolean isTouchEnded;
    private boolean isFirstComputeScroll;
    
    private OnMapDragEndListener onMapDragEndListener;
    
    public static interface OnMapDragEndListener{
    	void onMapDragEnd(GeoPoint oldMapCenter, GeoPoint newMapCenter);
    }

	public MyMapView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
		this.lastMapCenter = new GeoPoint(0, 0);
        this.isTouchEnded = false;
        this.isFirstComputeScroll = true;
	}

	public MyMapView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		this.lastMapCenter = new GeoPoint(0, 0);
        this.isTouchEnded = false;
        this.isFirstComputeScroll = true;
	}

	public MyMapView(Context arg0, String arg1) {
		super(arg0, arg1);
		this.lastMapCenter = new GeoPoint(0, 0);
        this.isTouchEnded = false;
        this.isFirstComputeScroll = true;
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
        	if(!isFirstComputeScroll){
        		firstMapCenter = getMapCenter();
        	}
        	this.isTouchEnded = false;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
        	this.isTouchEnded = true;
        }
        else if (event.getAction() == MotionEvent.ACTION_MOVE){
        	this.isFirstComputeScroll = true;
        }
        return super.onTouchEvent(event);
    }
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (isTouchEnded &&
        	isFirstComputeScroll&&
            lastMapCenter.equals(getMapCenter()) &&
            !lastMapCenter.equals(firstMapCenter)) {
        	isFirstComputeScroll = false;
        	//make some delay to make sure the move is really end
        	new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
						if(onMapDragEndListener != null){
							getHandler().post(new Runnable() {
								
								@Override
								public void run() {
									onMapDragEndListener.onMapDragEnd(firstMapCenter,getMapCenter());
								}
							});
						}
				}
			}, 500);
        }
        else{
        	this.lastMapCenter = this.getMapCenter();
        }
    }
    
    /**
     * you move or fling the map, and when the map move end, the listener callback will run
     * @param onMapDragEndListener
     */
    public void setOnMapDragEndListener(OnMapDragEndListener onMapDragEndListener){
    	this.onMapDragEndListener = onMapDragEndListener;
    }
}
