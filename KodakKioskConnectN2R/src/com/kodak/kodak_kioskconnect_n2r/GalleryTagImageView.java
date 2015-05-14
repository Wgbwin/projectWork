package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class GalleryTagImageView extends ImageView
{
	Boolean mChecked = false;
	// private final int mBorderWidth = 48;
	boolean mShowCropBox = false;
	protected String originalID;
	protected String uriEncodedPath;
	boolean calculatePosition = true;
	int xOffset = 0;
	int yOffset = 0;
	Rect rect2 = null;
	Boolean mWifiChecked = false;
	ImageButton deleteButton;
	ImageView previewIV;
	View v;

	public GalleryTagImageView(Context context)
	{
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.gallerytagimage, null);
		// deleteButton = (ImageButton)v.findViewById(R.id.deleteImageButton);
		previewIV = (ImageView) v.findViewById(R.id.previewImageView);
		previewIV.setAdjustViewBounds(true);
	}

	public GalleryTagImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.gallerytagimage, null);
		// deleteButton = (ImageButton)v.findViewById(R.id.deleteImageButton);
		previewIV = (ImageView) v.findViewById(R.id.previewImageView);
		previewIV.setAdjustViewBounds(true);
	}

	public GalleryTagImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.gallerytagimage, null);
		// deleteButton = (ImageButton)v.findViewById(R.id.deleteImageButton);
		previewIV = (ImageView) v.findViewById(R.id.previewImageView);
		previewIV.setAdjustViewBounds(true);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	public void setChecked(Boolean checked)
	{
		mChecked = checked;
		previewIV.invalidate();
	}

	public Boolean getChecked()
	{
		return mChecked;
	}

	public void setWifiChecked(Boolean checked)
	{
		mWifiChecked = checked;
		previewIV.invalidate();
	}

	public Boolean getWifiChecked()
	{
		return mWifiChecked;
	}

	public void setScaleType(ImageView.ScaleType scale)
	{
		previewIV.setScaleType(scale);
	}

	public void setImageBitmap(Bitmap bit)
	{
		previewIV.setImageBitmap(bit);
	}
}
