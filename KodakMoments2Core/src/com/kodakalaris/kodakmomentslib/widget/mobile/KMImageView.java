package com.kodakalaris.kodakmomentslib.widget.mobile;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.interfaces.IPhotoOperationInterface;

public class KMImageView extends RelativeLayout {
	private final static String TAG = KMImageView.class.getSimpleName();
	private ImageView vImageContent;
	private ImageView vImageTag;
	private ImageView vImageWatermark;
	private int tagImageSrcResId;
	private int tagImageBackgroundResId;
	private boolean tagImageAlwaysVisible;
	private boolean isLongClickable;
	private boolean isWatermarkEnable;
	private int tagImageVisible;

	private static final int TAG_VISIBLE = 0;
	private static final int TAG_INVISIBLE = 4;
	private static final int TAG_GONE = 8;

	private boolean mSelected;
	private boolean mSelectedDisabled;
	private GestureDetector mDetector;
	private IPhotoOperationInterface mPhotoOperationListener;
	private PhotoInfo photo;

	public KMImageView(Context context) {
		this(context, null);

	}

	public KMImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KMImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, R.layout.km_imageview, this);
		vImageContent = (ImageView) findViewById(R.id.image);
		vImageTag = (ImageView) findViewById(R.id.image_tag);
		vImageWatermark= (ImageView) findViewById(R.id.image_watermark_input);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.KMImageView);
		Drawable dSrc = a.getDrawable(R.styleable.KMImageView_tagImageSrc);
		Drawable dBackground = a
				.getDrawable(R.styleable.KMImageView_tagImageBackground);
		tagImageVisible = a.getInt(R.styleable.KMImageView_tagImageVisible, 0);
		tagImageAlwaysVisible = a.getBoolean(
				R.styleable.KMImageView_tagImageAlwaysVisible, false);
		isLongClickable = a.getBoolean(R.styleable.KMImageView_longClickable, false);
		isWatermarkEnable = a.getBoolean(R.styleable.KMImageView_watermarkEnable, false);
		a.recycle();

		if (dSrc != null) {
			vImageTag.setImageDrawable(dSrc);
		}

		if (dBackground != null) {
			vImageTag.setBackground(dBackground);
		}

		if (tagImageAlwaysVisible) {
			setTagImageVisibility(TAG_VISIBLE);
		} else {
			setTagImageVisibility(tagImageVisible);
		}
		mDetector = new GestureDetector(this.getContext(),
				new GestureListener());

	}

	public boolean ismSelected() {
		return mSelected;
	}

	public void setmSelected(boolean mSelected) {
		this.mSelected = mSelected;
		if (!tagImageAlwaysVisible) {
			if (mSelected) {
				vImageTag.setVisibility(View.VISIBLE);
			} else {
				vImageTag.setVisibility(View.GONE);
			}
		}

	}

	public boolean ismSelectedDisabled() {
		return mSelectedDisabled;
	}

	public void setmSelectedDisabled(boolean mSelectedDisabled) {
		this.mSelectedDisabled = mSelectedDisabled;
	}

	public IPhotoOperationInterface getmPhotoOperationListener() {
		return mPhotoOperationListener;
	}

	public void setmPhotoOperationListener(
			IPhotoOperationInterface mPhotoOperationListener) {
		this.mPhotoOperationListener = mPhotoOperationListener;
	}

	public PhotoInfo getPhoto() {
		return photo;
	}

	public void setPhoto(PhotoInfo photo) {
		this.photo = photo;
		
		if(isWatermarkEnable){
			if(photo.getPhotoSource().isFromPhone()){
				vImageWatermark.setVisibility(View.VISIBLE);
				vImageWatermark.setImageResource(R.drawable.icon_capture);
			}else {
				if(photo.getPhotoSource().isFromFaceBook()){
					vImageWatermark.setImageResource(R.drawable.icon_facebook);
				}else if(photo.getPhotoSource().isFromInstagram()){
					vImageWatermark.setImageResource(R.drawable.icon_instagram);
				}else if(photo.getPhotoSource().isFromDropBox()){
					vImageWatermark.setImageResource(R.drawable.icon_dropbox);
				}else if(photo.getPhotoSource().isFromFlickr()){
					vImageWatermark.setImageResource(R.drawable.icon_flickr);
				}
				vImageWatermark.setVisibility(View.VISIBLE);
				
			}
		}else {
			vImageWatermark.setVisibility(View.INVISIBLE);
		}
		
	}

	public void setTagImageResource(int resId) {
		tagImageSrcResId = resId;
		vImageTag.setImageResource(resId);
	}

	public void setTagImageBackgroundResource(int resId) {
		tagImageBackgroundResId = resId;
		vImageTag.setBackgroundResource(resId);

	}

	public void setTagImageVisibility(int visibility) {
		switch (visibility) {
		case TAG_VISIBLE:
			vImageTag.setVisibility(View.VISIBLE);
			break;
		case TAG_INVISIBLE:
			vImageTag.setVisibility(View.INVISIBLE);
			break;
		case TAG_GONE:
			vImageTag.setVisibility(View.GONE);
			break;
		}
	}

	public void setImageContentViewScaleType(ScaleType scaleType) {
		vImageContent.setScaleType(scaleType);
	}

	public ImageView getvImageContentView() {
		return vImageContent;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = mDetector.onTouchEvent(event);
		return result;
	}

	class GestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public void onLongPress(MotionEvent e) {
			if(isLongClickable){
				if (mPhotoOperationListener != null) {
					mPhotoOperationListener.onPhotoLongClick(KMImageView.this, photo);
				}
			}
			super.onLongPress(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (mPhotoOperationListener != null) {
				mPhotoOperationListener.onPhotoClick(KMImageView.this, photo);
				return true;
			} else {
				Log.e(TAG,
						"Single tap couldnt be handeled because mActivity is null");
				return false;
			}
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

	}
}
