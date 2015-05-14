package com.kodak.rss.tablet.view.collage;

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
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.collage.AlternateLayout;
import com.kodak.rss.core.n2r.bean.collage.Element;
import com.kodak.rss.tablet.R;

public class AlternateLayoutImageView extends ImageView {

	private Paint mSelectionPaint = null;
	private Paint mElementPaint = null;
	private Bitmap selectedCheckbox = null;
	private AlternateLayout mAlternateLayout;
	private List<RectF> mElementsRectFs;

	private Paint borderPaint = null;
	
	public AlternateLayoutImageView(Context context) {
		super(context);
		this.setMinimumHeight(96);
		init();
	}

	public AlternateLayoutImageView(Context context, AttributeSet set) {
		super(context, set);
		init();
	}
	
	private void init(){
		mSelectionPaint = new Paint();
		mSelectionPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mSelectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));		
		mElementPaint = new Paint();
		mElementPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mElementPaint.setColor(Color.GRAY);
		
		borderPaint = new Paint();
		borderPaint.setColor(Color.WHITE);
		borderPaint.setStrokeWidth(1f);

		this.setAdjustViewBounds(true);
		selectedCheckbox = BitmapFactory.decodeResource(getResources(),R.drawable.checkbox_sel);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		if (mAlternateLayout != null && mElementsRectFs != null && mElementsRectFs.size() > 0) {
			for (RectF rectF : mElementsRectFs) {
				canvas.drawRect(rectF, mElementPaint);
				canvas.drawLine(rectF.left, rectF.top, rectF.left, rectF.bottom, borderPaint);
				canvas.drawLine(rectF.right, rectF.top, rectF.right, rectF.bottom, borderPaint);		
				canvas.drawLine(rectF.left, rectF.top, rectF.right, rectF.top, borderPaint);			
				canvas.drawLine(rectF.left, rectF.bottom, rectF.right, rectF.bottom, borderPaint);
			}
		}

		if (mAlternateLayout != null && mAlternateLayout.isCheck) {
			if (selectedCheckbox == null) selectedCheckbox = BitmapFactory.decodeResource(getResources(),R.drawable.checkbox_sel);
			try {
				canvas.drawBitmap(selectedCheckbox, 0, 0, this.mSelectionPaint);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void setmAlternateLayout(AlternateLayout mAlternateLayout,int width ,int height) {
		this.mAlternateLayout = mAlternateLayout;
		mElementsRectFs = null;
		if(mAlternateLayout!= null && mAlternateLayout.elements!=null && mAlternateLayout.elements.size()>0){
			mElementsRectFs = new ArrayList<RectF>() ;
			float layoutWidth  = width;
			float layoutHeight = height;
			float roiX;
			float roiY;
			float roiW;
			float roiH;
			ROI mRoi = null ;
			RectF rectF = null ;
			for (Element element : mAlternateLayout.elements) {
				mRoi = element.location ;
				roiX = (float)(layoutWidth * mRoi.x / mRoi.ContainerW);
				roiY = (float)(layoutHeight * mRoi.y / mRoi.ContainerH);
				roiW = (float)(layoutWidth * mRoi.w / mRoi.ContainerW);
				roiH = (float)(layoutHeight * mRoi.h / mRoi.ContainerH);
				rectF = new RectF(roiX, roiY, roiX+roiW, roiY+roiH) ;
				mElementsRectFs.add(rectF) ;				
			}
		}
		invalidate() ;
	}
	
}
