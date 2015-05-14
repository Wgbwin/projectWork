package com.kodakalaris.video.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.example.android.displayingbitmaps.util.RecyclingBitmapDrawable;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodakalaris.video.fragments.ICommunicatingForTMS;

public class TMSImageCheckBoxView extends ImageView {
	private Paint mSelectionPaint = null;
	private Paint mCropPaint = null;
	boolean mShowCropBox = false;
	protected String originalID;
	public String uriEncodedPath;
	Bitmap selectedCheckbox = null;
	Bitmap mTMSSelectedCheckbox = null ;
	Bitmap untag = null;
	boolean mTMSChecked = false ;
	private Paint mTransparentPaint = null;
	
    private PhotoInfo photoInfo;
    private AlbumInfo album ;
    private GestureDetector mGestureDetector ;
    private ICommunicatingForTMS mListener ;
    
	
	public TMSImageCheckBoxView(Context context)
	{
		super(context);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//		mSelectionPaint.setARGB(255, 255, 0, 0);
		
		mCropPaint = new Paint();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mCropPaint.setARGB(255, 251, 186, 6);
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setStyle(Paint.Style.STROKE);
		mTransparentPaint.setStrokeWidth(3f);
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mTransparentPaint.setARGB(0, 255, 0, 0);
		
		this.setMinimumHeight(96);
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);
		mTMSSelectedCheckbox =  BitmapFactory.decodeResource(getResources(), R.drawable.tms_image_selected);
		untag = BitmapFactory.decodeResource(getResources(), R.drawable.untag);
		
		mGestureDetector = new GestureDetector(context, new TMSOnGestureListener()) ;
		
	}
	
	public TMSImageCheckBoxView(Context context, AttributeSet set)
	{
		super(context, set);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//		mSelectionPaint.setARGB(128, 255, 0, 0);
		
		mCropPaint = new Paint();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(3f);
		mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mCropPaint.setARGB(128, 255, 0, 0);
		
		mTransparentPaint = new Paint();
		mTransparentPaint.setStyle(Paint.Style.STROKE);
		mTransparentPaint.setStrokeWidth(3f);
		mTransparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mTransparentPaint.setARGB(0, 255, 0, 0);
		
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);
		mTMSSelectedCheckbox =  BitmapFactory.decodeResource(getResources(), R.drawable.tms_image_selected);
		untag = BitmapFactory.decodeResource(getResources(), R.drawable.untag);
		mGestureDetector = new GestureDetector(context,new TMSOnGestureListener()) ;
		
		
	}
	
    public void setPhoto(PhotoInfo photo){
    	this.photoInfo = photo ;
    }
    
    public PhotoInfo getPhotoInfo(){
    	return photoInfo ;
    }
    
    public AlbumInfo getAlbum() {
		return album;
	}

	public void setAlbum(AlbumInfo album) {
		this.album = album;
	}

    
    public void setICommunicatingForTMS (ICommunicatingForTMS mListener){
    	this.mListener = mListener ;
    }
	
	public void setTMSChecked(boolean checked){
		this.mTMSChecked = checked ;
		invalidate();
	}

	
	public boolean getTMSChecked(){
		return this.mTMSChecked ;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{

		// Log.d(TAG, "Entered onDraw() for id=" + this.getId());
		super.onDraw(canvas);
		if(mTMSChecked){
			if(mTMSSelectedCheckbox!=null){
				int offset = (canvas.getClipBounds().right - mTMSSelectedCheckbox.getWidth());
				try {
					canvas.drawBitmap(mTMSSelectedCheckbox, offset, 0, this.mSelectionPaint);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
	}
	
	
	 @Override
	    protected void onDetachedFromWindow() {
	        // This has been detached from Window, so clear the drawable
	        setImageDrawable(null);

	        super.onDetachedFromWindow();
	    }

	    /**
	     * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
	     */
	    @Override
	    public void setImageDrawable(Drawable drawable) {
	        // Keep hold of previous Drawable
	        final Drawable previousDrawable = getDrawable();

	        // Call super to set new Drawable
	        super.setImageDrawable(drawable);

	        // Notify new Drawable that it is being displayed
	        notifyDrawable(drawable, true);

	        // Notify old Drawable so it is no longer being displayed
	        notifyDrawable(previousDrawable, false);
	    }

	    /**
	     * Notifies the drawable that it's displayed state has changed.
	     *
	     * @param drawable
	     * @param isDisplayed
	     */
	    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
	        if (drawable instanceof RecyclingBitmapDrawable) {
	            // The drawable is a CountingBitmapDrawable, so notify it
	            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
	        } else if (drawable instanceof LayerDrawable) {
	            // The drawable is a LayerDrawable, so recurse on each layer
	            LayerDrawable layerDrawable = (LayerDrawable) drawable;
	            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
	                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
	            }
	        }
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
	    			mListener.onPhotoDoubleClick(TMSImageCheckBoxView.this, album ,photoInfo) ;
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
	    		if(mListener!=null){
	    			mListener.onPhotoSelected(TMSImageCheckBoxView.this, photoInfo) ;
		    		return true;
	    		}else {
                   return false ;
	    		}
	    	}
	    	
	    	@Override
	    	public boolean onDown(MotionEvent e) {
	    		// TODO Auto-generated method stub
	    		return true ;
	    	}
	    	
	    	
	    }
	    

}
