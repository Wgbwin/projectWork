package com.kodak.kodak_kioskconnect_n2r;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Coverflow extends Gallery {
	private final String TAG = Coverflow.class.getSimpleName();

	private Camera camera;
	private int maxZoom = -180;
	private int maxRotationAngle = 90;
	private boolean alphaMode = false;
	private int coverflowCenter = 0;

	public Coverflow(Context context) {
		super(context);
		this.initCoverFlow(context);
	}

	public Coverflow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initCoverFlow(context);
	}

	public Coverflow(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.initCoverFlow(context);
	}

	private void initCoverFlow(Context context) {
		this.camera = new Camera();
		this.setStaticTransformationsEnabled(true);
		Display display = ((Activity) context).getWindow().getWindowManager().getDefaultDisplay();
		int spacing = -(int) ((float)display.getWidth() / 8.0f);
		Log.i(TAG, "coverflow spacing:" + spacing);
		this.setSpacing(spacing);
	}

	public int getMaxZoom() {
		return maxZoom;
	}

	public void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}

	private int getCenter() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
	}

	private int getChildCenter(View child) {
		return child.getLeft() + child.getWidth() / 2;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int childCenter = getChildCenter(child);
        final int childWidth = child.getWidth();
        float rotationAngle = 0f;
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        if (childCenter == coverflowCenter) {
            transformImageBitmap(child, t, 0);
        } else {
            rotationAngle = (int) ((float)(coverflowCenter - childCenter) / childWidth * maxRotationAngle);
            if (Math.abs(rotationAngle) > maxRotationAngle) {
                rotationAngle = rotationAngle < 0 ? -maxRotationAngle : maxRotationAngle;
            }
            transformImageBitmap(child, t, rotationAngle);
        }
		return true;
	}
	
	private float zoomQuotiety = 1.5f;
	private void transformImageBitmap(View child, Transformation t, float rotationAngle) {
		camera.save();

		Matrix matrix = t.getMatrix();
		final int childHeight = child.getLayoutParams().height;
		final int childWidth = child.getLayoutParams().width;
		final float rotation = Math.abs(rotationAngle);

		float scale = 1.0f;
		final float zoomAmount = (float) (maxZoom + rotation * zoomQuotiety);
		camera.translate((float) (rotationAngle * 2.8 * scale), (float) (rotation * 0.15), zoomAmount);
		
		if(alphaMode){
			// TODO if alpha effect is needed, add code here
		}
		camera.getMatrix(matrix);
		matrix.preTranslate(-(childWidth / 2), -(childHeight / 2));
		matrix.postTranslate((childWidth / 2), (childHeight / 2));
		camera.restore();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		coverflowCenter = getCenter();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedPos = this.getSelectedItemPosition();
		int first = getFirstVisiblePosition();
		int postion = first + i;
		
		int order = 0;
		if(first+i == selectedPos){
			order = childCount - 1;
		} else if(postion < selectedPos){
			order = i;
		} else {
			order = (childCount-1) - (postion-selectedPos);
		}
		return order;
	}

}
