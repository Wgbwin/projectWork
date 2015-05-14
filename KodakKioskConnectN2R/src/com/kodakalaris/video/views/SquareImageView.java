package com.kodakalaris.video.views;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kodakalaris.video.BitmapOwner;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.SquareImageViewClickListener;
import com.kodakalaris.video.activities.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.R;

/*
 * This ImageView supports settings its dimensions as square
 * as specified in xml. Its also supports drag and drop, 
 * configureable in xml.
 */
public class SquareImageView extends ImageView implements OnDragListener, BitmapOwner {
	private static String TAG = SquareImageView.class.getSimpleName();
	private GestureDetector mDetector;
	private String mFilePath;
	private boolean isDragable = false;
	private boolean isDropable = false;
	public static final int RESOLUTION_THUMBNAIL = 0;
	public static final int RESOLUTION_HIGH = 1;
	public static final int RESOLUTION_HIGHER = 2;

	private static final int FIXED_DIMENTION_NOT_FIXED = 0;
	private static final int FIXED_DIMENTION_HORIZONTAL = 1;
	private static final int FIXED_DIMENTION_VERTICAL = 2;
	private static final int FIXED_DIMENTION_SMALLER = 3;
	private static final int FIXED_DIMENTION_LARGER = 4;

	private SquareImageViewClickListener mListener = null;
	private int mImageSize;
	private Context mContext;
	private int mFixedDimen;
	private int mWidth;
	private int mHeight;
	private float mDownEventStartX;
	private float mDownEventStartY;
	private int mNoImageResource;
	private Handler mHandler;
	private Runnable mLongClickRunnable;
	private PointF mStartLongPressAction;
	private int mImageType;
	private boolean handleTouchEvent = true;

	public SquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;// mContext will always be valid
		mHandler = new Handler();
		if (context instanceof SquareImageViewClickListener) {
			// mActivity will be null if context is not a
			// SquareImageViewClickListener.
			mListener = (SquareImageViewClickListener) context;
		}
		ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					// This gets called after the layout finishes laying
					// itself
					// out. That way we correctly know the size of the
					// view
					// getWidth and getHeight have the correct values
					// when displaying the image allowing us to scale
					// the bitmap
					// to the correct size of the view saving memory
					// when
					// rendering. large images and ensuring high quality
					// RESOLUTION_FULL images
					//
					BaseActivity.removeViewTreeObserverVersionSafe(this, SquareImageView.this);
					if (mImageType == IMAGE_TYPE_TOP_THREE) {
						setImageBitmapAndFilePath(getFilePath());
					}
				}
			});
		}
		mLongClickRunnable = new Runnable() {
			@Override
			public void run() {
				mHandler.removeCallbacks(mLongClickRunnable);
				// Log.w(TAG, "Long running");
				handelLongPress();
			}
		};
		init(attrs);
		this.setOnDragListener(this);

		class mListener extends GestureDetector.SimpleOnGestureListener {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

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
			public boolean onSingleTapUp(MotionEvent e) {
				// Log.i(TAG, "is tap");
				/*if (mListener != null) {
					mListener.onImageClick(SquareImageView.this, mFilePath);
					return true;
				} else {
					Log.e(TAG, "Single tap couldnt be handeled because mActivity is null");
					return false;
				}*/
				//return super.onSingleTapUp(e);
				return true ;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {

				// Log.i(TAG, "is tap confirmed");// too slow dont need because
				// we
				// dont need double clicks
				if (mListener != null) {
					mListener.onImageClick(SquareImageView.this, mFilePath);
					return true;
				} else {
					Log.e(TAG, "Single tap couldnt be handeled because mActivity is null");
					return false;
				}
//				return super.onSingleTapConfirmed(e);
			}

			@Override
			public void onShowPress(MotionEvent e) {
				// Log.i(TAG, "is tap");
				super.onShowPress(e);
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {

	    		if(mListener!=null){
	    			mListener.onImageDoubleClick(SquareImageView.this) ;
		    		return true;
	    		}else {
                   return false ;
	    		}
	    		
	    	
			}

		}
		mDetector = new GestureDetector(this.getContext(), new mListener());

	}

	public void handelLongPress() {
		if (SquareImageView.this.isDragable && mListener.areViewsDragable()) {
			// Log.i(TAG, "is long press");
			// Log.i(TAG, "W and H sec" + mListener.getShadowWidth() + ":" +
			// mListener.getShadowHeight());
			if (getFilePath() != null && getFilePath() != "") {
				final SquareImageView shadowView = (SquareImageView) mListener.getRootView().findViewById(R.id.select_photos_drag_shadow_view);
				//
				// shadowView.setAdjustViewBounds(false);
				shadowView.setLayoutParams(new RelativeLayout.LayoutParams(mListener.getShadowWidth(), mListener.getShadowHeight()));
				shadowView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						// Log.i(TAG, "globallayouting");
						// Ensure you call it only once :
						BaseActivity.removeViewTreeObserverVersionSafe(this, SquareImageView.this);

						Vibrator vibe = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
						vibe.vibrate(30);
						ClipData.Item item = new ClipData.Item(mFilePath);
						String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
						ClipData data = new ClipData("ClipDataFilePath", mimeTypes, item);
						View.DragShadowBuilder myShadow = new View.DragShadowBuilder(shadowView) {
							@Override
							public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
								super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
								// Log.i(TAG, "W and H final" +
								// mListener.getShadowWidth() + ":" +
								// mListener.getShadowHeight());
								shadowTouchPoint.set(mListener.getShadowWidth() / 2, mListener.getShadowHeight() / 2);
								shadowSize.set(mListener.getShadowWidth(), mListener.getShadowHeight());
							}

							@Override
							public void onDrawShadow(Canvas canvas) {
								// shadowView.draw(canvas);
								super.onDrawShadow(canvas);
							}

						};
						SquareImageView.this.startDrag(data, myShadow, SquareImageView.this, 0);
					}
				});

				shadowView.setImageBitmapAndFilePath(SquareImageView.this.getFilePath());

			}
		} else {
			Log.i(TAG, "View is not dragable");
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// Log.w(TAG, "Size Change:W:" + w + " H:" + h + " oW:" + oldw + " oh:"
		// + oldh);
		mWidth = w;
		mHeight = h;
		super.onSizeChanged(w, h, oldw, oldh);
		// setImageBitmapAndFilePath(getFilePath());
	}

	public static int IMAGE_TYPE_GRID = 1;
	public static int IMAGE_TYPE_TOP_THREE = 2;
	public static int IMAGE_TYPE_LARGE_RENDER = 3;
	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SquareImageView);

		isDragable = a.getBoolean(R.styleable.SquareImageView_isDragable, false);
		isDropable = a.getBoolean(R.styleable.SquareImageView_isDropable, false);
		mFixedDimen = a.getInt(R.styleable.SquareImageView_isSquare, FIXED_DIMENTION_NOT_FIXED);
		mNoImageResource = a.getResourceId(R.styleable.SquareImageView_emptyResource, 0);
		mImageSize = a.getInt(R.styleable.SquareImageView_resolution, RESOLUTION_THUMBNAIL);
		mImageType = a.getInt(R.styleable.SquareImageView_imageType, 0);
		handleTouchEvent = a.getBoolean(R.styleable.SquareImageView_handleTouchEvent, true);
		a.recycle();

	}
	public int getImageType() {
		return mImageType;
	}

	public String getFilePath() {
		return mFilePath;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		if (mFixedDimen == FIXED_DIMENTION_HORIZONTAL) {
			setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
		} else if (mFixedDimen == FIXED_DIMENTION_VERTICAL) {
			setMeasuredDimension(heightMeasureSpec, heightMeasureSpec);
		} else if (mFixedDimen == FIXED_DIMENTION_SMALLER) {
			setMeasuredDimension(Math.min(widthMeasureSpec, heightMeasureSpec), Math.min(widthMeasureSpec, heightMeasureSpec));
		} else if (mFixedDimen == FIXED_DIMENTION_LARGER) {
			setMeasuredDimension(Math.max(widthMeasureSpec, heightMeasureSpec), Math.max(widthMeasureSpec, heightMeasureSpec));
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
	public void setImageBitmapAndFilePath(String filePath) {
		this.mFilePath = filePath;
		MediaStoreUtils.doSetImageBitmap(this, filePath);
	}

	public void doOnBitmapReady(Bitmap fullRes) {
		if (fullRes == null) {
			mFilePath = null;
			setImageResource(mNoImageResource);
		} else {
			setImageBitmap(fullRes);
		}
		invalidate();
	}
	public int getNoImageResource() {
		return mNoImageResource;
	}
	public Bitmap getCachedBitmap(String filePath) {
		if (mContext instanceof BaseActivity) {
			return ((BaseActivity) mContext).getBitmapFromCache(filePath);
		} else {
			return null;
		}
	}
	public int getImageSize() {
		return mImageSize;
	}
	public int getImageWidth() {
		return getWidth();
	}
	public int getImageHeight() {
		return getHeight();
	}
	public void doOnBitmapFailure() {
		setImageResource(mNoImageResource);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!handleTouchEvent){
			return false;
		}
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN :
				mStartLongPressAction = new PointF(event.getX(), event.getY());
				// Log.w(TAG, "Long start");
				mHandler.postDelayed(mLongClickRunnable, 150);
				break;
			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP :
				// Log.w(TAG, "Long cancle");
				mHandler.removeCallbacks(mLongClickRunnable);
				break;
			case MotionEvent.ACTION_MOVE :
				float length = PointF.length(event.getX() - mStartLongPressAction.x, event.getY() - mStartLongPressAction.y);
				if (length > 20) {
					mHandler.removeCallbacks(mLongClickRunnable);
					// Log.w(TAG, "Long cancle distance");
				}
				break;
		}

		boolean result = mDetector.onTouchEvent(event);

		if (result) {
			handler.removeCallbacks(mLongPressed);
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mDownEventStartX = event.getX();
			mDownEventStartY = event.getY();
			handler.postDelayed(mLongPressed, 500);
		}
		if (event.getAction() == MotionEvent.ACTION_UP)
			handler.removeCallbacks(mLongPressed);
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (Math.abs(event.getX() - mDownEventStartX) > 100 || Math.abs(event.getY() - mDownEventStartY) > 100) {
				// Log.w(TAG,"Removing due to motion");
				handler.removeCallbacks(mLongPressed);
			}
		}
		//super.onTouchEvent(event);
		return result;
	}
	final Handler handler = new Handler();
	Runnable mLongPressed = new Runnable() {
		public void run() {
			// handelLongPress();
		}
	};
	private int mPosition;
	public boolean mHasRelativePath = false;

	@Override
	public boolean onDrag(View v, DragEvent e) {
		// @Override
		// public boolean onDragEvent(DragEvent event) {
		// Log.i(TAG, "Got a drag:" + e.getAction());

		switch (e.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED :
				// Log.i(TAG, "Got ACTION_DRAG_STARTED");
				break;
			case DragEvent.ACTION_DRAG_ENTERED :
				// Log.i(TAG, "Got ACTION_DRAG_ENTERED");
				break;
			case DragEvent.ACTION_DRAG_EXITED :
				// Log.i(TAG, "Got ACTION_DRAG_EXITED");
				break;
			case DragEvent.ACTION_DROP :
				// Log.i(TAG, "Got ACTION_DROP");
				ClipData data = e.getClipData();
				ClipData.Item item = data.getItemAt(0);
				String filePath = (String) item.getText();
				if (e.getLocalState() instanceof SquareImageView) {
					if (this.isDropable) {
						SquareImageView dropSource = (SquareImageView) e.getLocalState();
						if (dropSource.isDragable && dropSource.isDropable && this.isDragable && this.isDropable) {
							if (mListener.areViewsDragable()) {
								// also do a swap
								String s = this.getFilePath();
								dropSource.setImageBitmapAndFilePath(s);
								this.setImageBitmapAndFilePath(filePath);
								mListener.onImageDrop(dropSource, this, true, true);
							}
						} else {
							this.setImageBitmapAndFilePath(filePath);
							mListener.onImageDrop(dropSource, this, true, false);
						}
					} else {
						if (e.getLocalState() instanceof SquareImageView) {
							SquareImageView dropSource = (SquareImageView) e.getLocalState();
							Log.e(TAG, "Image drop on not dropable");
							mListener.onImageDrop(dropSource, this, false, false);
						}
					}
				}
				break;
			case DragEvent.ACTION_DRAG_ENDED :
				// Log.i(TAG, "Got ACTION_DRAG_ENDED");
			default :
				break;
		}
		return true;
	}

	public int getRealWidth() {
		return mWidth;

	}

	public int getRealHeight() {
		return mHeight;

	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;

	}

	public void setImagePosition(int position) {
		this.mPosition = position;
	}
	public int getImagePosition() {
		return mPosition;
	}
}
