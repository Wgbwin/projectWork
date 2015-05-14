package com.kodak.kodak_kioskconnect_n2r;

import com.example.android.displayingbitmaps.util.RecyclingBitmapDrawable;

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
import android.util.Log;
import android.widget.ImageView;

public class ImageCheckBoxView extends ImageView
{
	private boolean mChecked ;
	private boolean mCheckedDisabled ;
	private Paint mSelectionPaint = null;
	private boolean mWifiChecked = false;
	private Bitmap selectedCheckbox = null;
	private Bitmap selectedDisableCheckbox = null ;
	private	Bitmap mTMSSelectedCheckbox = null ;
	private Bitmap untag = null;
	private boolean mTMSChecked = false ;
	public ImageCheckBoxView(Context context)
	{
		super(context);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		
		
		this.setMinimumHeight(96);
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);
		selectedDisableCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.checkmark_used);
		mTMSSelectedCheckbox =  BitmapFactory.decodeResource(getResources(), R.drawable.tms_image_selected);
		untag = BitmapFactory.decodeResource(getResources(), R.drawable.untag);
	}

	public ImageCheckBoxView(Context context, AttributeSet set)
	{
		super(context, set);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		
		
		
		
		
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);
		mTMSSelectedCheckbox =  BitmapFactory.decodeResource(getResources(), R.drawable.tms_image_selected);
		selectedDisableCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.checkmark_used);
		untag = BitmapFactory.decodeResource(getResources(), R.drawable.untag);

	}



	public void setChecked(Boolean checked)
	{
		mChecked = checked;
		invalidate();
	}

	public void setWifiChecked(Boolean checked)
	{
		mWifiChecked = checked;
		invalidate();
	}
	
	public void setTMSChecked(boolean checked){
		this.mTMSChecked = checked ;
		invalidate();
	}

	public boolean getChecked()
	{
		return mChecked;
	}
	
	public boolean getTMSChecked(){
		return this.mTMSChecked ;
	}
	public boolean ismCheckedDisabled() {
		return mCheckedDisabled;
	}

	public void setmCheckedDisabled(boolean mCheckedDisabled) {
		this.mCheckedDisabled = mCheckedDisabled;
		invalidate();
	}

	//
	@Override
	protected void onDraw(Canvas canvas)
	{

		// Log.d(TAG, "Entered onDraw() for id=" + this.getId());
		super.onDraw(canvas);

		if (mChecked)
		{
			if(selectedCheckbox == null)
				Log.e("ImageCheckBoxView", "selectedCheckbox");
			try
			{
				if(mCheckedDisabled){
					canvas.drawBitmap(selectedDisableCheckbox, 0, 0, this.mSelectionPaint);
				}else {
					canvas.drawBitmap(selectedCheckbox, 0, 0, this.mSelectionPaint);
				}
				
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		
		if (mWifiChecked)
		{
			int offset = (canvas.getClipBounds().right - untag.getWidth());
			try
			{
				canvas.drawBitmap(untag, offset, 0, this.mSelectionPaint);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		if(mTMSChecked){
			if(mTMSSelectedCheckbox!=null){
				int offset = (canvas.getClipBounds().right - mTMSSelectedCheckbox.getWidth());
				canvas.drawBitmap(mTMSSelectedCheckbox, offset, 0, this.mSelectionPaint);
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





}
