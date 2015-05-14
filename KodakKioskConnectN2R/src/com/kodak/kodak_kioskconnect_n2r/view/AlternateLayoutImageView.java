package com.kodak.kodak_kioskconnect_n2r.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.example.android.displayingbitmaps.util.RecyclingBitmapDrawable;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout.Element;

public class AlternateLayoutImageView extends ImageView{
	boolean mChecked = false;
	private Paint mSelectionPaint = null;
	private Paint mElementPaint  = null ;
	private Bitmap selectedCheckbox = null;
	private AlternateLayout mAlternateLayout ;
	private List<RectF> mElementsRectFs ;
	
	
	public AlternateLayoutImageView(Context context)
	{
		super(context);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//		mSelectionPaint.setARGB(255, 255, 0, 0);
		
		mElementPaint = new Paint() ;
		mElementPaint.setStyle(Paint.Style.FILL_AND_STROKE) ;
		mElementPaint.setColor(Color.GRAY) ;
		
		this.setMinimumHeight(96);
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);
	}

	public AlternateLayoutImageView(Context context, AttributeSet set)
	{
		super(context, set);
		// mContext = context;
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
//		mSelectionPaint.setARGB(128, 255, 0, 0);
		mElementPaint = new Paint() ;
		mElementPaint.setStyle(Paint.Style.FILL_AND_STROKE) ;
		mElementPaint.setColor(Color.GRAY) ;
		
		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(), R.drawable.selectedcheckbox);

	}


	public void setChecked(boolean checked)
	{
		mChecked = checked;
		invalidate();
	}

	

	public boolean getChecked()
	{
		return mChecked;
	}
	
	

	//
	@Override
	protected void onDraw(Canvas canvas)
	{

		// Log.d(TAG, "Entered onDraw() for id=" + this.getId());
		super.onDraw(canvas);

		
		
		if(mAlternateLayout!=null && mElementsRectFs!=null && mElementsRectFs.size()>0){
			
			for (RectF rectF : mElementsRectFs) {
				canvas.drawRect(rectF, mElementPaint);
			}
			
		}
		
		
		if (mChecked)
		{
			if(selectedCheckbox == null)
				Log.e("ImageCheckBoxView", "selectedCheckbox");
			try
			{
				canvas.drawBitmap(selectedCheckbox, 0, 0, this.mSelectionPaint);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
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

		public AlternateLayout getmAlternateLayout() {
			return mAlternateLayout;
		}

		public void setmAlternateLayout(AlternateLayout mAlternateLayout,int width ,int height) {
			this.mAlternateLayout = mAlternateLayout;
			if(mAlternateLayout!=null && mAlternateLayout.elements!=null &&mAlternateLayout.elements.size()>0){
				mElementsRectFs = new ArrayList<RectF>() ;
				float layoutWidth  =width ;
				float layoutHeight = height;
				float roiX ;
				float roiY ;
				float roiW ;
				float roiH ;
				ROI mRoi = null ;
				RectF rectF = null ;
				for (Element element : mAlternateLayout.elements) {
					mRoi = element.location ;
					roiX = (float) (layoutWidth * mRoi.x / mRoi.ContainerW);
					roiY = (float) (layoutHeight * mRoi.y / mRoi.ContainerH);
					roiW = (float) (layoutWidth * mRoi.w / mRoi.ContainerW);
					roiH = (float) (layoutHeight * mRoi.h / mRoi.ContainerH);
					
					rectF = new RectF(roiX, roiY, roiX+roiW, roiY+roiH) ;
					mElementsRectFs.add(rectF) ;
					
				}
				
				
				
			}
			
			
			invalidate() ;
		}





}
