package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.ROI;

@SuppressLint("DrawAllocation")
public class DrawableImageView extends ImageView{

	private Context mContext;
	private Paint mPaint;
	private Path mPath;
	private DashPathEffect mEffect;
	private int mColor;
	private float mPhase;
	private GreetingCardPage mCurrentPage;
	private float mScaleFactor = 1.0f;
	private boolean canDraw = true;
	private Bitmap copyBitmap;
	private GreetingCardPageLayer copyLayer;
	private boolean isShowing = false;
	
	public float getmScaleFactor() {
		return mScaleFactor;
	}

	public void setmScaleFactor(float mScaleFactor) {
		this.mScaleFactor = mScaleFactor;
	}
	public GreetingCardPage getmCurrentPage() {
		return mCurrentPage;
	}

	public void setmCurrentPage(GreetingCardPage mCurrentPage) {
		this.mCurrentPage = mCurrentPage;
	}
	
	public DrawableImageView(Context context) {
		this(context,null,0);
	}
	
	public DrawableImageView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	
	public boolean isCanDraw() {
		return canDraw;
	}

	public void setCanDraw(boolean canDraw) {
		this.canDraw = canDraw;
	}

	public Bitmap getCopyBitmap() {
		return copyBitmap;
	}

	public void setCopyBitmap(Bitmap copyBitmap) {
		this.copyBitmap = copyBitmap;
	}

	public GreetingCardPageLayer getCopyLayer() {
		return copyLayer;
	}

	public void setCopyLayer(GreetingCardPageLayer copyLayer) {
		this.copyLayer = copyLayer;
	}

	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}

	public DrawableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		setFocusable(true);
		setFocusableInTouchMode(true);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(4);
//		mPath = makeFollowPatdh();
		mColor = Color.rgb(0, 174, 239);
		//PrintHelper.isDrawPath = true;
		
	}
	 
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (isCanDraw() && getmCurrentPage() != null) {
			ROI mRoi =null;
			float layoutWidth ;
			float layoutHeight;
			float roiX ;
			float roiY ;
			float roiW ;
			float roiH ;
			Bitmap addBitmap = null;
			Path path;
			RectF bounds;
			GreetingCardPageLayer[] mCardPageLayers = getmCurrentPage().layers;
			String text = "";
			String defaultText = "";
			String sampleText = "";
			String textInput = ""; //the text will draw on the pic.
			GreetingCardPageLayer layer;
			if(isShowing){
				for (int i = 0 ; i < mCardPageLayers.length ; i ++) {
					layer = mCardPageLayers[i];
					mRoi = layer.location;
					layoutWidth = getWidth();
					layoutHeight = getHeight();
					roiX = (float) (layoutWidth * mRoi.x / mRoi.ContainerW);
					roiY = (float) (layoutHeight * mRoi.y / mRoi.ContainerH);
					roiW = (float) (layoutWidth * mRoi.w / mRoi.ContainerW);
					roiH = (float) (layoutHeight * mRoi.h / mRoi.ContainerH);
					if (layer.type.equals("Image") && layer.contentId.trim().length() == 0) {
						addBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.addimage);
						canvas.drawBitmap(addBitmap, (roiX + roiW / 2) - (addBitmap.getWidth() / 2), (roiY + roiH / 2) - (addBitmap.getHeight() / 2), null);
					} else if (layer.type.equals("TextBlock")) {
						path = new Path();
						path.moveTo(roiX, roiY);
						path.lineTo(roiX + roiW, roiY);
						path.lineTo(roiX + roiW, roiY + roiH);
						path.lineTo(roiX, roiY + roiH);
						path.lineTo(roiX, roiY);
						mPath = path;
						bounds = new RectF();
						mPath.computeBounds(bounds, false);
	//					canvas.translate(10 - bounds.left, 10 - bounds.top);
						mEffect = new DashPathEffect(new float[] { 20, 5, 20, 5 }, mPhase);
						mPhase += 0.1;
						if (PrintHelper.canValidate) {
						invalidate();
						}
						mPaint.setPathEffect(mEffect);
						mPaint.setColor(mColor);
						canvas.drawPath(mPath, mPaint);
					
						if(layer.isEditedBefore()){
							
						}else{
							text = layer.toGetText();
							defaultText = layer.toGetDefaultText();
							sampleText = layer.toGetSampleText();
							if(null != text && !"".equals(text)){
								textInput = text;
							}else{
								if(null != defaultText && !"".equals(defaultText)){
									textInput = defaultText;
								}else{
									textInput = sampleText;
								}
							}
							layer.setTextInputVlaue(textInput);
							layer.setTextInputDefaultValue(textInput);
						}											
						if(addBitmap != null){
							addBitmap.recycle();
							addBitmap = null;
						}
					
					}
				}
			} 
		}
		
	}
}
