package com.kodakalaris.video.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodakalaris.video.fragments.ICommunicatingForTMS;

public class ViewPagerImageItemView extends ImageView{
	
	
   private GestureDetector mGestureDetector ;
   private ICommunicatingForTMS mListener ;
   private PhotoInfo photo ;
   public ViewPagerImageItemView(Context context) {
		super(context);
		mGestureDetector = new GestureDetector(context,new TMSOnGestureListener()) ;
        
		
	
	}
	
	
	public ViewPagerImageItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context,new TMSOnGestureListener()) ;

	}
	
	
	
	public void setICommunicatingForTMS (ICommunicatingForTMS mListener){
	   this.mListener = mListener ;
	}
	
    public PhotoInfo getPhoto() {
		return photo;
	}


	public void setPhoto(PhotoInfo photo) {
		this.photo = photo;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
	    mGestureDetector.onTouchEvent(event);
		return true ;
	}

	class TMSOnGestureListener extends GestureDetector.SimpleOnGestureListener{
    	
    	@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			// handelLongPress();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			// Log.i(TAG, "is scroll");
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

    	
    	
    	@Override
    	public boolean onDoubleTap(MotionEvent e) {
    		if(mListener!=null){
    			mListener.onViewPagerPhotoDoubleClick() ;
	    		return true;
    		}else {
               return false ;
    		}
    		
    		
    		
    	}
    	
    	
    	@Override
    	public boolean onSingleTapUp(MotionEvent e) {
    		return true ;
    	}
    	
    	@Override
    	public boolean onSingleTapConfirmed(MotionEvent e) {
    	// TODO Auto-generated method stub
//    		if(mListener!=null){
//    			mListener.onPhotoSelected(TMSImageCheckBoxView.this, photoInfo) ;
//	    		return true;
//    		}else {
//               return false ;
//    		}
    		
    		return true ;
    	}
    	
    	@Override
    	public boolean onDown(MotionEvent e) {
    		// TODO Auto-generated method stub
    		return true ;
    	}
    	
    	
    }
    
	

}
