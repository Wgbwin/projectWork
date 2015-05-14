package com.kodak.rss.tablet.view;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.animation.FlipAniamtion;
import com.kodak.rss.tablet.bean.Flippable;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageDownloader;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;

public class GCMainView extends RelativeLayout implements Flippable, DragTarget{
	private static final String TAG = "GreetingCardView";
	
	public static final int MODE_SINGLE_LANDSCAPE = 1;
	public static final int MODE_SINGLE_PORTRAIT = 2;
	public static final int MODE_DUPLEX_LANDSCAPE = 3;
	public static final int MODE_DUPLEX_PORTRAIT = 4;
	public static final int MODE_FOLDED_LANDSCAPE = 5;
	public static final int MODE_FOLDED_PORTRAIT = 6;
	
	private static final float LEFT_RIGHT_MARGIN = 170;
	private static final long FLIP_ANIMATION_DURATION = 500;
	
	private GreetingCard mGreetingCard;
	private int mMode = MODE_DUPLEX_LANDSCAPE;
	private int mStep = 1;
	private boolean mSwitched;
	
	private Context mContext;
	private GCDualCardView mCard1;
	private GCDualCardView mCard2;
	private View mViewShadowTop;
	private View mViewShadowLeftRight;
	private View mViewCenter;
	private OnCardGestureListener mOnGestureListener;
	
	private Point screenSize;
	private int mMaxWidth, mMaxHeight;// current max height and width
	private boolean mIsInLeftTop;
	private boolean mIsZoomIn;
	
	private Bitmap mWaitBitmap;
	private boolean[] mActivePages = new boolean[4];
	
	private ImageUseURIDownloader imageDownloader;
	private final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	private onProcessImageResponseListener onResponseListener = new onProcessImageResponseListener() {		
		@Override
		public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
			if (response == null || pendingRequests == null) return;
			int refreshCount = response.getRequest().getRefreshCount();
			if (response.getError() == null) {
				Bitmap bitmap = response.getBitmap();
				if (bitmap != null) {
					GreetingCardUtil.refreshSucPageInCard(profileId, refreshCount);	
				}
				Bitmap mContentBitmap = bitmap == null ? mWaitBitmap : bitmap;
				if (view instanceof GCMainPageView) {
					((GCMainPageView)view).setImageBitmap(mContentBitmap);
					view.postInvalidate();
				}
			}							
		}
	};
	
	public GCMainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	public GCMainView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public GCMainView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
		inflate(context, R.layout.gc_main_view, this);
		mCard1 = (GCDualCardView) findViewById(R.id.card1);
		mCard2 = (GCDualCardView) findViewById(R.id.card2);
		mViewCenter = findViewById(R.id.view_center);
		mViewShadowTop = findViewById(R.id.shadow_top);
		mViewShadowLeftRight = findViewById(R.id.shadow_left_right);
		setVisibility(View.INVISIBLE);
		mActivePages[0] = true;
		
		Bitmap waitBitmap = getWaitBitmap();
		mCard1.getFirstView().setWaitBitmap(waitBitmap);
		mCard1.getSecondView().setWaitBitmap(waitBitmap);
		mCard2.getFirstView().setWaitBitmap(waitBitmap);
		mCard2.getSecondView().setWaitBitmap(waitBitmap);
		
		imageDownloader = new ImageUseURIDownloader(context,pendingRequests);	
		imageDownloader.setSaveType(FilePathConstant.cardType);	
		imageDownloader.setIsThumbnail(true);
		imageDownloader.setOnProcessImageResponseListener(onResponseListener);
		mCard1.getFirstView().setImageDownloader(imageDownloader);
		mCard1.getSecondView().setImageDownloader(imageDownloader);
		mCard2.getFirstView().setImageDownloader(imageDownloader);
		mCard2.getSecondView().setImageDownloader(imageDownloader);
		
		screenSize = new Point();
		((Activity) context).getWindowManager().getDefaultDisplay().getSize(screenSize);
		
	}
	
	private void setDownloadImageSize(GreetingCard greetingCard) {
		int maxHeight = getMaxAllowedHeight(true, false);
		int maxWidth = getMaxAllowedWidth(true, false);
		int[] size = GCDualCardView.getSize(maxHeight, maxHeight, maxWidth, greetingCard.pages[0].width, greetingCard.pages[0].height);
		
		for (int i = 0; i < 4 ; i++) {
			getPageView(i).setDownloadImageSize(size[0], size[1]);
		}
	}
	
	private Bitmap getWaitBitmap() {
		if (mWaitBitmap == null || mWaitBitmap.isRecycled()) {
			mWaitBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait232x174);
		}
		return mWaitBitmap;
	}
	
	/**
	 * invoke this method in UI thread
	 * @param greetingCard
	 */
	public void setGreetingCard(GreetingCard greetingCard, boolean init) {
		mGreetingCard = greetingCard;
		
		if (init) {
			mMode = GreetingCardUtil.getGCMode(greetingCard);
			setCardSize(greetingCard.pages[0].width, greetingCard.pages[0].height);
			setDownloadImageSize(greetingCard);
			setCardInCenter(mCard1);
			setCardInCenter(mCard2);
			
			postDelayed(new Runnable() {
				
				@Override
				public void run() {
					setVisibility(View.VISIBLE);
					setMode(mMode);
					for (int i = 0; i < mGreetingCard.pages.length; i++) {
						getPageView(i).setPage(mGreetingCard.pages[i]);
					}
					
					initGestureListener();
				}
			}, 500);
		} else {
			for (int i = 0; i < mGreetingCard.pages.length; i++) {
				getPageView(i).setPage(mGreetingCard.pages[i]);
			}
		}
		
	}
	
	public boolean isZoomIn() {
		return mIsZoomIn;
	}
	
	public void setOnPageLayerClickListener(OnLayerClickListener<GCMainPageView, GCPage, GCLayer> onLayerClickListener) {
		for (int i = 0; i < 4; i++) {
			getPageView(i).setOnLayerClickListener(onLayerClickListener);
		}
	}
	
	public void reDownloadAndRefreshCardimage(int index) {
		//TODO need optimize
		getPageView(index).setPage(mGreetingCard.pages[index]);
		
		GCMainPageView pageView = getPageView(index);
		pageView.notifyReDownloadImage();
	}
	
	public void reDownloadAndRefreshCardimage(String pageId) {
		for (int i = 0; i < mGreetingCard.pages.length; i++) {
			if (pageId.equals(mGreetingCard.pages[i].id)) {
				reDownloadAndRefreshCardimage(i);
			}
		}
	}
	
	/**
	 * @param xOnScreen
	 * @param yOnScreen
	 * @return Object{GCMainPageView, GCPage, GCLayer}
	 * 
	 * change name to pointToPosition
	 */
	public Object[] pointToPosition(float xOnScreen, float yOnScreen) {
		for (int i = 0; i < mActivePages.length; i++) {
			if (mActivePages[i]) {
				GCMainPageView pageView = getPageView(i);
				int[] location = new int[2];
				pageView.getLocationOnScreen(location);
				float relativeX = xOnScreen - location[0];
				float relativeY = yOnScreen - location[1];
				GCPage p = pageView.getPage();
				if (p == null) return null;
				for (int m = 0; m < p.layers.size(); m++) {
					GCLayer layer = p.layers.get(m);
					if (layer == null) continue;
					RectF rect = pageView.getLayerRect(layer);					
					if (GCLayer.TYPE_IMAGE.equals(layer.type) && rect.contains(relativeX, relativeY)) {
						Object[] result = new Object[]{pageView, p, layer};
						return result;
					}
				}
			}
		}		
		return null;
	}
	
	@Override
	public void hideAllFrames() {
		for (int i = 0; i < 4; i++) {
			getPageView(i).hideFrameForLayer();
		}
	}
	
	public void showPreview() {
		mCard1.getFirstView().showPreview();
		mCard1.getSecondView().showPreview();
		mCard2.getFirstView().showPreview();
		mCard2.getSecondView().showPreview();
	}
	
	public void showEdit() {
		mCard1.getFirstView().showEdit();
		mCard1.getSecondView().showEdit();
		mCard2.getFirstView().showEdit();
		mCard2.getSecondView().showEdit();
	}
	
	public void zoomIn(final long duration) {
		final int oldh = mCard1.getExactHeight();
		
		mIsZoomIn = true;
		changeSize(getMaxAllowedHeight());
		final int newh = getCardHeight();
		
		zoom(oldh, newh, duration);
	}
	
	public void zoomOut(final long duration) {
		final int oldh = mCard1.getExactHeight();
		
		mIsZoomIn = false;
		changeSize(getMaxAllowedHeight());
		final int newh = getCardHeight();
		
		zoom(oldh, newh, duration);
	}
	
	private void zoom(int oldh, int newh, long duration) {
		float scaleFrom = (float) oldh / newh;
		ScaleAnimation sa = new ScaleAnimation(scaleFrom, 1, scaleFrom, 1, Animation.ABSOLUTE, (float) mViewCenter.getX(), Animation.ABSOLUTE, getCardTopMargin());
		sa.setDuration(duration);
		startAnimation(sa);
	}
	
	private void changeSize(int height) {
		mMaxHeight = height;
		mMaxWidth = getMaxAllowedWidth();
		
		if (mMode == MODE_FOLDED_PORTRAIT) {
			mMaxWidth = mMaxWidth / 2;
		}
		
		int maxAllowedHeight = getMaxAllowedHeight();
		
		if (mMaxHeight > maxAllowedHeight) {
			mMaxHeight = maxAllowedHeight;
		}
		
		mCard1.setMaxWidth(mMaxWidth);
		mCard1.setMaxHeight(mMaxHeight);
		
		mCard2.setMaxWidth(mMaxWidth);
		mCard2.setMaxHeight(mMaxHeight);
		
		setStep(mStep);
	}
	
	public void zoomOutAndMoveToLeftTop() {
		mIsZoomIn = false;
		mIsInLeftTop = true;
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = screenSize.x / 2;
		requestLayout();
		
		changeSize(getMaxAllowedHeight());
		resetCardLayout();
	}
	
	/**
	 * when user input text for card, the card need to move to left top
	 */
	public void moveToLeftTop() {
		mIsInLeftTop = true;
		
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = screenSize.x / 2;
		requestLayout();
		
		changeSize(getMaxAllowedHeight());
		resetCardLayout();
	}
	
	/**
	 * when input text finished or canceled, cards should move to center
	 */
	public void moveToCenter() {
		mIsInLeftTop = false;
		
		LayoutParams params = (LayoutParams) getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		params.height = LayoutParams.MATCH_PARENT;
		requestLayout();
		
		changeSize(getMaxAllowedHeight());
		
		resetCardLayout();
	}
	
	public void resetCardLayout() {
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				setStep(mStep);
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						setStep(mStep);
					}
				}, 5);
			}
		}, 5);
	}
	
	/**
	 * Set the mode for greetingcard.
	 * Be careful, this method should be call when the layout has been init.
	 * @param mode
	 */
	private void setMode(int mode) {
		mMode = mode;
		
		updateActivePages(mStep);
		showInsideShadow4Page();
		
		mCard1.clearAnimation();
		mCard2.clearAnimation();
		
		if (mMode == MODE_SINGLE_LANDSCAPE || mMode == MODE_SINGLE_PORTRAIT || mMode == MODE_DUPLEX_LANDSCAPE || mMode == MODE_DUPLEX_PORTRAIT) {
			mCard1.setVisibility(View.VISIBLE);
			mCard2.setVisibility(View.GONE);
			mCard1.setFlipOrientation(true);
			mCard2.setFlipOrientation(true);
			setCardInCenter(mCard1);
		} else if (mMode == MODE_FOLDED_PORTRAIT) {
			mCard1.setVisibility(View.VISIBLE);
			mCard2.setVisibility(View.VISIBLE);
			mCard1.setFlipOrientation(true);
			mCard2.setFlipOrientation(true);
			
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			mCard1.setVisibility(View.VISIBLE);
			mCard2.setVisibility(View.VISIBLE);
			mCard1.setFlipOrientation(false);
			mCard2.setFlipOrientation(false);
			
		}
		setStep(1);
	}
	
	private void showInsideShadow4Page() {
		if (mMode == MODE_FOLDED_PORTRAIT) {
			getPageView(1).showShadow(GCMainPageView.INSIDE_SHADOW_RIGHT);
			getPageView(2).showShadow(GCMainPageView.INSIDE_SHADOW_LEFT);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			getPageView(1).showShadow(GCMainPageView.INSIDE_SHADOW_BOTTOM);
			getPageView(2).showShadow(GCMainPageView.INSIDE_SHADOW_TOP);
		}
	}
	
	private void initGestureListener() {
		mOnGestureListener = new OnCardGestureListener() {
			
			@Override
			public void onPinch(boolean out) {
				GCEditActivity activity = (GCEditActivity) mContext;
				if (out) {
					if (!mIsZoomIn) {
						activity.zoomInCard();
					}
				} else {
					if (mIsZoomIn) {
						activity.zoomOutCard();
					}
				}
			}
			
			@Override
			public void onPan(int direction) {
				GCEditActivity activity = (GCEditActivity) mContext;
				if (mMode == MODE_SINGLE_LANDSCAPE || mMode == MODE_SINGLE_PORTRAIT) {
					//do nothing in these modes
				} else if (mMode == MODE_DUPLEX_LANDSCAPE || mMode == MODE_DUPLEX_PORTRAIT) {
					switch (direction) {
					case PAN_LEFT:
					case PAN_RIGHT:
						activity.changeCardStep(mStep == 1 ? 2 : 1);
						break;
						
					}
				} else if (mMode == MODE_FOLDED_PORTRAIT) {
					if (mStep == 1) {
						switch (direction) {
						case PAN_LEFT:
							activity.changeCardStep(2);
							break;
						}
					} else if (mStep == 2) {
						switch (direction) {
						case PAN_LEFT:
							activity.changeCardStep(3);
							break;
						case PAN_RIGHT:
							activity.changeCardStep(1);
							break;
						}
						
					} else if (mStep == 3) {
						switch (direction) {
						case PAN_RIGHT:
							activity.changeCardStep(2);
							break;
						}
					}
					
				} else if (mMode == MODE_FOLDED_LANDSCAPE) {
					if (mStep == 1) {
						switch (direction) {
						case PAN_LEFT:
						case PAN_RIGHT:
							activity.changeCardStep(4);
							break;
						case PAN_TOP:
							activity.changeCardStep(2);
							break;
						}
					} else if (mStep == 2) {
						switch (direction) {
						case PAN_TOP:
							activity.changeCardStep(3);
							break;
						case PAN_BOTTOM:
							activity.changeCardStep(1);
							break;
						}
						
					} else if (mStep == 3) {
						switch (direction) {
						case PAN_LEFT:
						case PAN_RIGHT:
						case PAN_TOP:
							activity.changeCardStep(4);
							break;
						case PAN_BOTTOM:
							activity.changeCardStep(2);
							break;
						}
						
					} else if (mStep == 4) {
						switch (direction) {
						case PAN_LEFT:
						case PAN_RIGHT:
							activity.changeCardStep(1);
							break;
						case PAN_BOTTOM:
							activity.changeCardStep(3);
							break;
						}
						
					}
				}
				
			}
		};
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w != 0 && h != 0 && mMaxWidth == 0 && mMaxHeight == 0) {
			mMaxWidth = w;
			mMaxHeight = h;
			
			int maxAllowedHeight = getMaxAllowedHeight();
			int maxAllowedWidth = getMaxAllowedWidth();
			
			if (mMaxWidth > maxAllowedWidth) {
				mMaxWidth = maxAllowedWidth;
			}
			
			if (mMaxHeight > maxAllowedHeight) {
				mMaxHeight = maxAllowedHeight;
			}
			
			if (mMode == MODE_FOLDED_PORTRAIT) {
				mMaxWidth = mMaxWidth / 2;
			}
			
			mCard1.setMaxWidth(mMaxWidth);
			mCard1.setMaxHeight(mMaxHeight);
			
			mCard2.setMaxWidth(mMaxWidth);
			mCard2.setMaxHeight(mMaxHeight);
			
			setStep(mStep);
		}
	}
	
	private float mXDown;
	private float mYDown;
	private float mDistanceDown;
	private final int PAN_SLOP = 50;
	private final int PINCH_SLOP = 50;
	private boolean mIsReleased = false;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mXDown = ev.getX();
			mYDown = ev.getY();
			mDistanceDown = 0;
			mIsReleased = false;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			if (mIsReleased) {
				return true;
			}
			
			mDistanceDown = getPointerDistance(ev);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsReleased) {
				return true;
			}
			
			if (mOnGestureListener != null) {
				if (mDistanceDown > 0 && ev.getPointerCount() > 1) {//in pointer mode
					//check pinch
					float currentDis = getPointerDistance(ev);
					if (Math.abs(currentDis - mDistanceDown) > PINCH_SLOP) {
						mOnGestureListener.onPinch(currentDis > mDistanceDown);
						mIsReleased = true;
						return true;
					}
				} else {// not in pointer mode
					//check pan gesture
					float panX = ev.getX() - mXDown;
					float panY = ev.getY() - mYDown;
					float absPanX = Math.abs(panX);
					float absPanY = Math.abs(panY);
					if (absPanX > PAN_SLOP || absPanY > PAN_SLOP) {
						if (absPanX > absPanY) {
							mOnGestureListener.onPan(panX > 0 ? OnCardGestureListener.PAN_RIGHT : OnCardGestureListener.PAN_LEFT);
						} else {
							mOnGestureListener.onPan(panY > 0 ? OnCardGestureListener.PAN_BOTTOM : OnCardGestureListener.PAN_TOP);
						}
						
						mIsReleased = true;
						return true;
					}
				}
			}
			
			break;
		case MotionEvent.ACTION_UP:
			if (mDistanceDown > 0) {
				return true;
			}
			break;
		}
		return false;
	}
	
	private float getPointerDistance(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x*x + y*y);
	}
	
	private void setCardSize(float w, float h) {
		mCard1.setCardSize(w, h);
		mCard2.setCardSize(w, h);
	}
	
	/**
	 * @param index 
	 * @param bitmap
	 */
	public void setCardImage(int index, Bitmap bitmap) {
		switch (index) {
		case 0:
			mCard1.setImageBitmapForFront(bitmap);
			break;
		case 1:
			mCard1.setImageBitmapForBack(bitmap);
			break;
		case 2:
			mCard2.setImageBitmapForFront(bitmap);
			break;
		case 3:
			mCard2.setImageBitmapForBack(bitmap);
			break;
		default:
			Log.e(TAG, "Wrong index for card image!");
		}
	}
	
	@Override
	public void swapFrontAndBack() {
		
	}
	
	private void switchCard() {
		mSwitched = !mSwitched;
		Bitmap temp = mCard1.getImageBitmapForFront();
		mCard1.setImageBitmapForFront(mCard2.getImageBitmapForFront());
		mCard2.setImageBitmapForFront(temp);
		
		temp = mCard1.getImageBitmapForBack();
		mCard1.setImageBitmapForBack(mCard2.getImageBitmapForBack());
		mCard2.setImageBitmapForBack(temp);
		temp = null;
	}
	
	private GCMainPageView getPageView(int index) {
		switch (index) {
		case 0:
			return mCard1.getFirstView();
		case 1:
			return mCard1.getSecondView();
		case 2:
			return mCard2.getFirstView();
		case 3:
			return mCard2.getSecondView();
		default:
			return null;
		}
	}
	
	public void updateImage(int index) {
		switch (index) {
		case 0:
			mCard1.getFirstView().invalidate();
			break;
		case 1:
			mCard1.getSecondView().invalidate();
			break;
		case 2:
			mCard2.getFirstView().invalidate();
			break;
		case 3:
			mCard2.getSecondView().invalidate();
			break;
		}
	}
	
	public void flipTo(int step) {
		checkSwapState();
		final int from = mStep;
		final int to = step;
		if (from == to) {
			return;
		}
		
		mStep = step;
		
		if (from == 1 && to == 2) {
			flipFromOneToTwo();
		} else if (from == 2 && to == 1) {
			flipFromTwoToOne();
		} else if (from == 1 && to == 3) {
			flipFromOneToThree();
		} else if (from == 3 && to == 1) {
			flipFromThreeToOne();
		} else if (from == 2 && to == 3) {
			flipFromTwoToThree();
		} else if (from == 3 && to == 2) {
			flipFromThreeToTwo();
		} else if (from == 1 && to == 4) {
			flipFromOneToFour();
		} else if (from == 4 && to == 1) {
			flipFromFourToOne();
		} else if (from == 2 && to == 4) {
			flipFromTwoToFour();
		} else if (from == 4 && to == 2) {
			flipFromFourToTwo();
		} else if (from == 3 && to == 4) {
			flipFromThreeToFour();
		} else if (from == 4 && to == 3) {
			flipFromFourToThree();
		}
		
	}
	
	private void setStep(int step) {
		hideFoldedOutShadow();
		if (mMode == MODE_SINGLE_LANDSCAPE || mMode == MODE_SINGLE_PORTRAIT || mMode == MODE_DUPLEX_LANDSCAPE || mMode == MODE_DUPLEX_PORTRAIT) {
			mCard1.clearAnimation();
			setCardInCenter(mCard1);
		} else if (mMode == MODE_FOLDED_PORTRAIT) {
			mCard1.clearAnimation();
			mCard2.clearAnimation();
			if (step == 1) {
				mCard1.bringToFront();
				FlipAniamtion anim = new FlipAniamtion(mCard2, 0, 15, mCard2.getExactLeft(), -200, true, false);
				anim.setFillAfter(true);
				mCard2.startAnimation(anim);
				
				setCardInCenter(mCard1);
				setCardInCenter(mCard2);
				showFoldedOutShadow();
			} else if (step == 2) {
				setCardLeftToCenter(mCard1);
				setCardRightToCenter(mCard2);
				hideFoldedOutShadow();
			} else if (step == 3) {
				mCard2.bringToFront();
				
				FlipAniamtion anim = new FlipAniamtion(mCard1, 0, -15, getCardWidth(), -200, true, false);
				anim.setFillAfter(true);
				mCard1.startAnimation(anim);
				
				setCardInCenter(mCard1);
				setCardInCenter(mCard2);
				showFoldedOutShadow();
			}
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			mCard1.clearAnimation();
			mCard2.clearAnimation();
			
			if (step == 1) {
				mCard1.bringToFront();
				setCardInCenter(mCard1);
				setCardInCenter(mCard2);
				
				FlipAniamtion fa = new FlipAniamtion(mCard2, 0, -15, getCardWidth() + 200, mCard2.getExactTop(), false, false);
				fa.setFillAfter(true);
				mCard2.startAnimation(fa);
				
				showFoldedOutShadow();
			} else if (step == 2) {
				setCardInCenter(mCard1);
				setCardBelowCenter(mCard1, mCard2);
				
				FlipAniamtion fa = new FlipAniamtion(mCard2, 0, 60, getCardWidth() / 2, mCard2.getExactTop(), false, false);
				fa.setFillAfter(true);
				mCard2.startAnimation(fa);
				hideFoldedOutShadow();
			} else if (step == 3) {
				setCardInCenter(mCard2);
				setCardAboveCenter(mCard2, mCard1);
				
				FlipAniamtion fa = new FlipAniamtion(mCard1, 0, -30, getCardWidth() / 2, getCardHeight(), false, false);
				fa.setFillAfter(true);
				mCard1.startAnimation(fa);
				hideFoldedOutShadow();
			} else if (step == 4) {
				mCard2.bringToFront();
				setCardInCenter(mCard1);
				setCardInCenter(mCard2);
				
				FlipAniamtion fa = new FlipAniamtion(mCard1, 0, -15, mCard1.getExactLeft() - 200, mCard1.getExactTop(), false, false);
				fa.setFillAfter(true);
				mCard1.startAnimation(fa);
				showFoldedOutShadow();
			}
		}
		
		updateActivePages(step);
	}
	
	private void updateActivePages(int step) {
		switch (mMode) {
		case MODE_SINGLE_LANDSCAPE:
		case MODE_SINGLE_PORTRAIT:
			setActivePages(0);
			break;
		case MODE_DUPLEX_LANDSCAPE:
		case MODE_DUPLEX_PORTRAIT:
			if (step == 1) {
				setActivePages(0);
			} else if (step == 2) {
				setActivePages(1);
			}
			break;
		case MODE_FOLDED_PORTRAIT:
			if (step == 1) {
				setActivePages(0);
			} else if (step == 2) {
				setActivePages(1, 2);
			} else if (step == 3) {
				setActivePages(3);
			}
			break;
		case MODE_FOLDED_LANDSCAPE:
			if (step == 1) {
				setActivePages(0);
			} else if (step == 2) {
				setActivePages(1);
			} else if (step == 3) {
				setActivePages(2);
			} else if (step == 4) {
				setActivePages(3);
			}
			break;
		}
	}
	
	private void setActivePages(int... indexs) {
		mActivePages[0] = false;
		mActivePages[1] = false;
		mActivePages[2] = false;
		mActivePages[3] = false;
		if (indexs != null) {
			for (int i = 0; i < indexs.length; i++) {
				int index = indexs[i];
				mActivePages[index] = true;
			}
		}
		
		//Set touchable/untouchable for page
		for (int i = 0; i < mActivePages.length; i ++) {
			getPageView(i).setActive(mActivePages[i]);
		}
	}
	
	private void flipFromOneToTwo() {
		if (mMode == MODE_DUPLEX_LANDSCAPE || mMode == MODE_DUPLEX_PORTRAIT) {
			setStep(2);
			FlipAniamtion anim = new FlipAniamtion(mCard1, 180, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, true);
			anim.setDuration(FLIP_ANIMATION_DURATION);
			anim.setAnimationListener(generateAnimationListener(1, 2));
			mCard1.startAnimation(anim);
		} else if (mMode == MODE_FOLDED_PORTRAIT) {
			setStep(2);
			//Aninmation for card 1
			AnimationSet animSetCard1 = new AnimationSet(true);
			
			FlipAniamtion animFlipCard1 = new FlipAniamtion(mCard1, 180, 0, mCard1.getExactWidth(), mCard1.getExactCenterY(), true, true);
			
			TranslateAnimation animTranCard1 = new TranslateAnimation(-mCard1.getExactCenterX(), 0, 0, 0);
			
			animSetCard1.addAnimation(animFlipCard1);
			animSetCard1.addAnimation(animTranCard1);
			animSetCard1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animation for card 2
			AnimationSet animSetCard2 = new AnimationSet(true);
			
			FlipAniamtion animFlipCard2 = new FlipAniamtion(mCard2, 15, 0, 0, -200, true, false);
			
			TranslateAnimation animTranCard2 = new TranslateAnimation(- mCard2.getExactCenterX(), 0, 0, 0);
			
			animSetCard2.addAnimation(animFlipCard2);
			animSetCard2.addAnimation(animTranCard2);
			animSetCard2.setDuration(FLIP_ANIMATION_DURATION);
			animSetCard2.setAnimationListener(generateAnimationListener(1, 2));
			
			mCard1.startAnimation(animSetCard1);
			mCard2.startAnimation(animSetCard2);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(2);
			//Animation for card 1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, -180, 0, mCard1.getExactCenterX(), mCard1.getExactBottom(), false, true);
			TranslateAnimation ta1 = new TranslateAnimation(0, 0, -mCard1.getExactHeight(), 0);
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animation for card2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, -15, 60, mCard2.getExactRight() + 200, mCard2.getExactTop(), false, false);
			fa2.setCenterXYAfterDegrees(0, mCard2.getExactCenterX(), mCard2.getExactTop());
			TranslateAnimation ta2 = new TranslateAnimation(0, 0, -mCard2.getExactHeight(), 0);
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setFillAfter(true);
			animSet2.setAnimationListener(generateAnimationListener(1, 2));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromTwoToOne() {
		if (mMode == MODE_DUPLEX_LANDSCAPE || mMode == MODE_DUPLEX_PORTRAIT) {
			setStep(mStep);
			FlipAniamtion anim = new FlipAniamtion(mCard1, -180, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, true);
			anim.setDuration(FLIP_ANIMATION_DURATION);
			anim.setAnimationListener(generateAnimationListener(2, 1));
			mCard1.startAnimation(anim);
		} else if (mMode == MODE_FOLDED_PORTRAIT) {
			setStep(mStep);
			//Aninmation for card 1
			AnimationSet animSetCard1 = new AnimationSet(true);
			
			FlipAniamtion animFlipCard1 = new FlipAniamtion(mCard1, -180, 0, mCard1.getExactLeft(),  mCard1.getExactCenterY(), true, true);
			
			TranslateAnimation animTranCard1 = new TranslateAnimation(mCard1.getExactCenterX(), 0, 0, 0);
			
			animSetCard1.addAnimation(animFlipCard1);
			animSetCard1.addAnimation(animTranCard1);
			animSetCard1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animation for card 2
			AnimationSet animSetCard2 = new AnimationSet(true);
			FlipAniamtion animFlipCard2 = new FlipAniamtion(mCard2, 0, 15, mCard2.getExactLeft(), -200, true, false);
			TranslateAnimation animTranCard2 = new TranslateAnimation(mCard2.getExactCenterX(), 0, 0, 0);
			
			animSetCard2.addAnimation(animFlipCard2);
			animSetCard2.addAnimation(animTranCard2);
			animSetCard2.setFillAfter(true);
			animSetCard2.setDuration(FLIP_ANIMATION_DURATION);
			animSetCard2.setAnimationListener(generateAnimationListener(2, 1));
			
			mCard1.startAnimation(animSetCard1);
			mCard2.startAnimation(animSetCard2);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(1);
			//Animation for card 1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, 180, 0, mCard1.getExactCenterX(), mCard1.getExactTop(), false, true);
			TranslateAnimation ta1 = new TranslateAnimation(0, 0, mCard1.getExactHeight(), 0);
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animation for card2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 60, -15, mCard2.getExactCenterX(), mCard2.getExactTop(), false, false);
			fa2.setCenterXYAfterDegrees(0, mCard2.getExactRight() + 200, mCard2.getExactTop());
			TranslateAnimation ta2 = new TranslateAnimation(0, 0, mCard2.getExactHeight(), 0);
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setFillAfter(true);
			animSet2.setAnimationListener(generateAnimationListener(2, 1));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromOneToThree() {
		clearAnimation();
		if (mMode == MODE_FOLDED_PORTRAIT) {
			//animation for card1
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, 0, -90, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
			fa1.setDuration(FLIP_ANIMATION_DURATION / 2);
			
			//animation for card2
			AnimationSet animSet2 = new AnimationSet(false);
			FlipAniamtion fa2a = new FlipAniamtion(mCard2, 0, -90, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
			fa2a.setDuration(FLIP_ANIMATION_DURATION / 2);
			FlipAniamtion fa2b = new FlipAniamtion(mCard2, 0, 15, mCard2.getExactLeft(), -200, true, false);
			
			animSet2.addAnimation(fa2b);
			animSet2.addAnimation(fa2a);
			
			animSet2.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					hideFoldedOutShadow();
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setStep(3);
					checkSwapState();
					hideFoldedOutShadow();
					//animation for card1
					AnimationSet animSet1 = new AnimationSet(false);
					FlipAniamtion fa1a = new FlipAniamtion(mCard1, 90, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
					fa1a.setDuration(FLIP_ANIMATION_DURATION / 2);
					FlipAniamtion fa1b = new FlipAniamtion(mCard1, 0, -15, mCard1.getExactRight(), -200, true, false);
					
					animSet1.addAnimation(fa1b);
					animSet1.addAnimation(fa1a);
					animSet1.setFillAfter(true);
					
					//animation for card2
					FlipAniamtion fa2 = new FlipAniamtion(mCard2, 90, 0, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
					fa2.setDuration(FLIP_ANIMATION_DURATION / 2);
					
					animSet1.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
							
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							showFoldedOutShadow();
						}
					});
					
					mCard1.startAnimation(animSet1);
					mCard2.startAnimation(fa2);
				}
			});
			
			mCard1.startAnimation(fa1);
			mCard2.startAnimation(animSet2);
			
			
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(3);
			//Animation for card1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, -180, -30, mCard1.getExactCenterX(), mCard1.getExactBottom(), false, true);
			animSet1.addAnimation(fa1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			animSet1.setFillAfter(true);
			
			//Animation for card2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, -15, 0, mCard2.getExactRight() + 200, mCard2.getExactTop(), false, false);
			animSet2.addAnimation(fa2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setAnimationListener(generateAnimationListener(1, 3));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromThreeToOne() {
		if (mMode == MODE_FOLDED_PORTRAIT) {
			//Animation for card1
			AnimationSet animSet1 = new AnimationSet(false);
			FlipAniamtion fa1a = new FlipAniamtion(mCard1, 0, 90, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
			fa1a.setDuration(FLIP_ANIMATION_DURATION / 2);
			
			FlipAniamtion fa1b = new FlipAniamtion(mCard1, 0, -15, mCard1.getExactRight(), -200, true, false);
			
			animSet1.addAnimation(fa1b);
			animSet1.addAnimation(fa1a);
			
			//Animation for card2
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 0, 90, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
			fa2.setDuration(FLIP_ANIMATION_DURATION / 2);
			
			fa2.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					hideFoldedOutShadow();
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setStep(1);
					checkSwapState();
					hideFoldedOutShadow();
					//Animation for card1
					FlipAniamtion fa1 = new FlipAniamtion(mCard1, -90, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
					fa1.setDuration(FLIP_ANIMATION_DURATION / 2);
					
					//Animation for card2
					AnimationSet animSet2 = new AnimationSet(false);
					FlipAniamtion fa1a = new FlipAniamtion(mCard2, -90, 0, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false); 
					fa1a.setDuration(FLIP_ANIMATION_DURATION / 2);
					
					FlipAniamtion fa1b = new FlipAniamtion(mCard2, 0, 15, mCard2.getExactLeft(), -200, true, false);
					
					animSet2.addAnimation(fa1b);
					animSet2.addAnimation(fa1a);
					animSet2.setFillAfter(true);
					
					animSet2.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
							
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							showFoldedOutShadow();
						}
					});
					
					mCard1.startAnimation(fa1);
					mCard2.startAnimation(animSet2);
				}
			});
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(fa2);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(1);
			//Animation for card1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, 150, 0, mCard1.getExactCenterX(), mCard1.getExactTop(), false, true);
			animSet1.addAnimation(fa1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animation for card2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 0, -15, mCard2.getExactRight() + 200, mCard2.getExactTop(), false, false);
			animSet2.addAnimation(fa2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setFillAfter(true);
			animSet2.setAnimationListener(generateAnimationListener(3, 1));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromTwoToThree() {
		if (mMode == MODE_FOLDED_PORTRAIT) {
			setStep(3);
			//card left
			AnimationSet animSet1 = new AnimationSet(true);
			TranslateAnimation ta1 = new TranslateAnimation(-mCard1.getExactCenterX(), 0, 0, 0);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, 0, -15, mCard1.getExactRight(), -200, true, false);
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			animSet1.setFillAfter(true);
			//card right
			AnimationSet animSet2 = new AnimationSet(true);
			TranslateAnimation ta2 = new TranslateAnimation(-mCard2.getExactCenterX(), 0, 0, 0);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 180, 0, mCard2.getExactRight()-1, mCard2.getExactCenterY(), true, true);
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setAnimationListener(generateAnimationListener(2, 3));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(3);
			//Animation for card 1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, 0, -30, mCard1.getExactCenterX(), mCard1.getExactBottom(), false, false);
			TranslateAnimation ta1 = new TranslateAnimation(0, 0, mCard1.getExactHeight(), 0);
			
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			animSet1.setFillAfter(true);
			
			//Animtion for card 2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 60, 0, mCard2.getExactCenterX(), mCard2.getExactTop(), false, false);
			TranslateAnimation ta2 = new TranslateAnimation(0, 0, mCard2.getExactHeight(), 0);
			
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setAnimationListener(generateAnimationListener(2, 3));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromThreeToTwo() {
		if (mMode == MODE_FOLDED_PORTRAIT) {
			setStep(2);
			//card left
			AnimationSet animSet1 = new AnimationSet(true);
			TranslateAnimation ta1 = new TranslateAnimation(mCard1.getExactCenterX(), 0, 0, 0);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, -15, 0, mCard1.getExactRight(), -200, true, false);
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			//card right
			AnimationSet animSet2 = new AnimationSet(true);
			TranslateAnimation ta2 = new TranslateAnimation(mCard2.getExactCenterX(), 0, 0, 0);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, -180, 0, mCard2.getExactLeft(), mCard2.getExactCenterY(), true, true);
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setAnimationListener(generateAnimationListener(3, 2));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			setStep(2);
			//Animation for card 1
			AnimationSet animSet1 = new AnimationSet(true);
			FlipAniamtion fa1 = new FlipAniamtion(mCard1, -30, 0, mCard1.getExactCenterX(), mCard1.getExactBottom(), false, false);
			TranslateAnimation ta1 = new TranslateAnimation(0, 0, -mCard1.getExactHeight(), 0);
			
			animSet1.addAnimation(fa1);
			animSet1.addAnimation(ta1);
			animSet1.setDuration(FLIP_ANIMATION_DURATION);
			
			//Animtion for card 2
			AnimationSet animSet2 = new AnimationSet(true);
			FlipAniamtion fa2 = new FlipAniamtion(mCard2, 0, 60, mCard2.getExactCenterX(), mCard2.getExactTop(), false, false);
			TranslateAnimation ta2 = new TranslateAnimation(0, 0, -mCard2.getExactHeight(), 0);
			
			animSet2.addAnimation(fa2);
			animSet2.addAnimation(ta2);
			animSet2.setDuration(FLIP_ANIMATION_DURATION);
			animSet2.setFillAfter(true);
			animSet2.setAnimationListener(generateAnimationListener(3, 2));
			
			mCard1.startAnimation(animSet1);
			mCard2.startAnimation(animSet2);
		}
	}
	
	private void flipFromOneToFour() {
		//Animation for card1
		FlipAniamtion fa1 = new FlipAniamtion(mCard1, 0, -90, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
		fa1.setDuration(FLIP_ANIMATION_DURATION / 2);
		fa1.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				hideFoldedOutShadow();
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				checkSwapState(4);
				setStep(4);
				hideFoldedOutShadow();
				//animation for card1
				AnimationSet animSet1 = new AnimationSet(false);
				FlipAniamtion fa1a = new FlipAniamtion(mCard1, 90, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
				fa1a.setDuration(FLIP_ANIMATION_DURATION / 2);
				
				FlipAniamtion fa1b = new FlipAniamtion(mCard1, 0, -15, mCard2.getExactLeft() - 200, mCard2.getExactTop(), false, false);
				
				animSet1.addAnimation(fa1b);
				animSet1.addAnimation(fa1a);
				animSet1.setFillAfter(true);
				
				//animation for card 2
				FlipAniamtion fa2 = new FlipAniamtion(mCard2, 90, 0, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
				fa2.setDuration(FLIP_ANIMATION_DURATION / 2);
				
				animSet1.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						showFoldedOutShadow();
					}
				});
				
				mCard1.startAnimation(animSet1);
				mCard2.startAnimation(fa2);
				
			}
		});
		
		//Animation for card2
		AnimationSet animSet2 = new AnimationSet(false);
		FlipAniamtion fa2a = new FlipAniamtion(mCard2, 0, -90, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
		fa2a.setDuration(FLIP_ANIMATION_DURATION / 2);
		FlipAniamtion fa2b = new FlipAniamtion(mCard2, 0, -15, mCard2.getExactRight() + 200, mCard2.getExactTop(), false, false);
		
		animSet2.addAnimation(fa2b);
		animSet2.addAnimation(fa2a);
		mCard1.startAnimation(fa1);
		mCard2.startAnimation(animSet2);
	}
	
	private void flipFromFourToOne() {
		//Animation for card1
		AnimationSet animSet1 = new AnimationSet(false);
		FlipAniamtion fa1a = new FlipAniamtion(mCard1, 0, 90, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
		fa1a.setDuration(FLIP_ANIMATION_DURATION / 2);
		
		FlipAniamtion fa1b = new FlipAniamtion(mCard1, 0, -15, mCard1.getExactLeft() - 200, mCard1.getExactTop(), false, false);
		
		animSet1.addAnimation(fa1b);
		animSet1.addAnimation(fa1a);
		
		animSet1.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				hideFoldedOutShadow();
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				checkSwapState(1);
				setStep(1);
				hideFoldedOutShadow();
				//animation for card1
				FlipAniamtion fa1 = new FlipAniamtion(mCard1, -90, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, false);
				fa1.setDuration(FLIP_ANIMATION_DURATION / 2);
				
				//animation for card 2
				AnimationSet animSet2 = new AnimationSet(false);
				FlipAniamtion fa2a = new FlipAniamtion(mCard2, -90, 0, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
				fa2a.setDuration(FLIP_ANIMATION_DURATION / 2);
				
				FlipAniamtion fa2b = new FlipAniamtion(mCard2, 0, -15, mCard2.getExactRight() + 200, mCard2.getExactTop(), false, false);
				
				animSet2.addAnimation(fa2b);
				animSet2.addAnimation(fa2a);
				animSet2.setFillAfter(true);
				
				animSet2.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						showFoldedOutShadow();
					}
				});
				
				mCard1.startAnimation(fa1);
				mCard2.startAnimation(animSet2);
				
			}
		});
		
		//Animation for card2
		FlipAniamtion fa2 = new FlipAniamtion(mCard2, 0, 90, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, false);
		fa2.setDuration(FLIP_ANIMATION_DURATION / 2);
		
		mCard1.startAnimation(animSet1);
		mCard2.startAnimation(fa2);
	}
	
	private void flipFromTwoToFour() {
		setStep(4);
		//Animation for card1
		AnimationSet animSet1 = new AnimationSet(true);
		FlipAniamtion fa1a = new FlipAniamtion(mCard1, 180, 0, mCard1.getExactCenterX(), mCard1.getExactTop(), true, true);
		FlipAniamtion fa1b = new FlipAniamtion(mCard1, -180, -15, mCard1.getExactCenterX(), mCard1.getExactTop(), false, true);
		fa1b.setCenterXYAfterDegrees(-90, mCard1.getExactLeft() - 200, mCard1.getExactTop());
		TranslateAnimation ta1 = new TranslateAnimation(0, 0, mCard1.getExactHeight(), 0);
		animSet1.addAnimation(fa1b);
		animSet1.addAnimation(fa1a);
		animSet1.addAnimation(ta1);
		animSet1.setDuration(FLIP_ANIMATION_DURATION);
		animSet1.setFillAfter(true);
		
		//Animation for card2
		AnimationSet animSet2 = new AnimationSet(true);
		FlipAniamtion fa2a = new FlipAniamtion(mCard2, 180, 0, mCard2.getExactCenterX(), mCard2.getExactTop(), true, true);
		FlipAniamtion fa2b = new FlipAniamtion(mCard2, 60, 0, mCard2.getExactCenterX(), mCard2.getExactTop(), false, false);
		TranslateAnimation ta2 = new TranslateAnimation(0, 0, mCard2.getExactHeight(), 0);
		animSet2.addAnimation(fa2b);
		animSet2.addAnimation(fa2a);
		animSet2.addAnimation(ta2);
		animSet2.setDuration(FLIP_ANIMATION_DURATION);
		animSet2.setAnimationListener(generateAnimationListener(2, 4));
		
		mCard1.startAnimation(animSet1);
		mCard2.startAnimation(animSet2);
	}
	
	private void flipFromFourToTwo() {
		setStep(2);
		//Animation for card1
		AnimationSet animSet1 = new AnimationSet(true);
		FlipAniamtion fa1a = new FlipAniamtion(mCard1, -180, 0, mCard1.getExactCenterX(), mCard1.getExactBottom(), true, true);
		FlipAniamtion fa1b = new FlipAniamtion(mCard1, -195, 0, mCard1.getExactLeft() - 200, mCard1.getExactBottom(), false, true);
		fa1b.setCenterXYAfterDegrees(-180, mCard1.getExactCenterX(), mCard1.getExactBottom());
		TranslateAnimation ta1 = new TranslateAnimation(0, 0, -mCard1.getExactHeight(), 0);
		animSet1.addAnimation(fa1b);
		animSet1.addAnimation(fa1a);
		animSet1.addAnimation(ta1);
		animSet1.setDuration(FLIP_ANIMATION_DURATION);
		
		//Animation for card2
		AnimationSet animSet2 = new AnimationSet(true);
		FlipAniamtion fa2a = new FlipAniamtion(mCard2, -180, 0, mCard2.getExactCenterX(), mCard2.getExactTop(), true, true);
		FlipAniamtion fa2b = new FlipAniamtion(mCard2, 0, 60, mCard2.getExactCenterX(), mCard2.getExactTop(), false, false);
		TranslateAnimation ta2 = new TranslateAnimation(0, 0, -mCard2.getExactHeight(), 0);
		animSet2.addAnimation(fa2b);
		animSet2.addAnimation(fa2a);
		animSet2.addAnimation(ta2);
		animSet2.setDuration(FLIP_ANIMATION_DURATION);
		animSet2.setFillAfter(true);
		animSet2.setAnimationListener(generateAnimationListener(4, 2));
		
		mCard1.startAnimation(animSet1);
		mCard2.startAnimation(animSet2);
	}
	
	private void flipFromThreeToFour() {
		setStep(4);
		//Animation for card1
		AnimationSet animSet1 = new AnimationSet(true);
		FlipAniamtion fa1a = new FlipAniamtion(mCard1, 180, 0, mCard1.getExactCenterX(), mCard1.getExactCenterY(), true, true);
		FlipAniamtion fa1b = new FlipAniamtion(mCard1, -210, -15, mCard1.getExactCenterX(), mCard1.getExactTop(), false, true);
		fa1b.setCenterXYAfterDegrees(-90, mCard1.getExactLeft() - 200, mCard1.getExactTop());
		animSet1.addAnimation(fa1b);
		animSet1.addAnimation(fa1a);
		animSet1.setDuration(FLIP_ANIMATION_DURATION);
		animSet1.setFillAfter(true);
		
		//Animation for card2
		FlipAniamtion fa2 = new FlipAniamtion(mCard2, 180, 0, mCard2.getExactCenterX(), mCard2.getExactCenterY(), true, true);
		fa2.setDuration(FLIP_ANIMATION_DURATION);
		fa2.setAnimationListener(generateAnimationListener(3, 4));
		
		mCard1.startAnimation(animSet1);
		mCard2.startAnimation(fa2);
	}
	
	private void flipFromFourToThree() {
		setStep(3);
		//Animation for card1
		AnimationSet animSet1 = new AnimationSet(true);
		FlipAniamtion fa1a = new FlipAniamtion(mCard1, -180, 0, mCard1.getExactCenterX(), mCard1.getExactBottom(), true, true);
		FlipAniamtion fa1b = new FlipAniamtion(mCard1, 165, -30, mCard2.getExactLeft() - 200, mCard2.getExactBottom(), false, true);
		fa1b.setCenterXYAfterDegrees(90, mCard2.getExactCenterX(), mCard2.getExactBottom());
		animSet1.addAnimation(fa1a);
		animSet1.addAnimation(fa1b);
		animSet1.setDuration(FLIP_ANIMATION_DURATION);
		animSet1.setFillAfter(true);
		
		//Animation for card2
		FlipAniamtion fa2 = new FlipAniamtion(mCard2, -180, 0, mCard2.getExactCenterX(), mCard2.getExactTop(), true, true);
		fa2.setDuration(FLIP_ANIMATION_DURATION);
		fa2.setAnimationListener(generateAnimationListener(4, 3));
		
		mCard1.startAnimation(animSet1);
		mCard2.startAnimation(fa2);
	}
	
	
	private void setCardInCenter(GCDualCardView cardView) {
		LayoutParams params = (LayoutParams) cardView.getLayoutParams();
		params.leftMargin = 0;
		params.rightMargin = 0;
		params.topMargin = getCardTopMargin();
		params.bottomMargin = getCardBottomMargin();
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.LEFT_OF, 0);
		params.addRule(RelativeLayout.RIGHT_OF, 0);
		cardView.requestLayout();
	}
	
	private void setCardLeftToCenter(GCDualCardView cardView) {
		LayoutParams params = (LayoutParams) cardView.getLayoutParams();
		params.leftMargin = 0;
		params.rightMargin = 0;
		params.topMargin = getCardTopMargin();
		params.bottomMargin = getCardBottomMargin();
		params.addRule(RelativeLayout.CENTER_HORIZONTAL,0);
		if (cardView.getExactWidth() == cardView.getWidth()) {
			params.addRule(RelativeLayout.LEFT_OF, R.id.view_center);
		} else {
			params.addRule(RelativeLayout.LEFT_OF, 0);
			params.leftMargin = getWidth() / 2 - cardView.getExactWidth();
		}
		params.addRule(RelativeLayout.RIGHT_OF, 0);
		cardView.requestLayout();
	}
	
	private void setCardRightToCenter(GCDualCardView cardView) {
		LayoutParams params = (LayoutParams) cardView.getLayoutParams();
		params.leftMargin = 0;
		params.rightMargin = 0;
		params.topMargin = getCardTopMargin();
		params.bottomMargin = getCardBottomMargin();
		params.addRule(RelativeLayout.CENTER_HORIZONTAL,0);
		params.addRule(RelativeLayout.LEFT_OF, 0);
		params.addRule(RelativeLayout.RIGHT_OF, R.id.view_center);
		cardView.requestLayout();
	}
	
	private void setCardBelowCenter(GCDualCardView cardCenter, GCDualCardView cardBelow) {
		LayoutParams paramsCenter = (LayoutParams) cardCenter.getLayoutParams();
		LayoutParams paramsBelow = (LayoutParams) cardBelow.getLayoutParams();
		paramsBelow.leftMargin = paramsCenter.leftMargin;
		paramsBelow.topMargin = paramsCenter.topMargin + getCardHeight();
		paramsBelow.bottomMargin = - getCardHeight() + paramsCenter.bottomMargin;
		cardBelow.requestLayout();
	}
	
	private void setCardAboveCenter(GCDualCardView cardCenter, GCDualCardView cardAbove){
		LayoutParams paramsCenter = (LayoutParams) cardCenter.getLayoutParams();
		LayoutParams paramsBelow = (LayoutParams) cardAbove.getLayoutParams();
		paramsBelow.leftMargin = paramsCenter.leftMargin;
		paramsBelow.topMargin = -getCardHeight() + paramsCenter.topMargin;
		paramsBelow.bottomMargin = getCardHeight() + paramsCenter.bottomMargin;
		cardAbove.requestLayout();
	}
	
	private int getCardTopMargin() {
		return DimensionUtil.dip2px(mContext, 90);
	}
	
	private int getCardBottomMargin() {
		return DimensionUtil.dip2px(mContext, 20);
	}
	
	private int getCardHeight() {
		return mCard1.getSize(getMaxAllowedHeight(mIsZoomIn, mIsInLeftTop))[1];
	}
	
	private int getCardWidth() {
		return mCard1.getSize(getMaxAllowedHeight(mIsZoomIn, mIsInLeftTop))[0];
	}
	
	
	private int getMaxAllowedHeight(boolean isZoomIn, boolean isInLeftTop) {
		int imageSelectionAreaHeight = isZoomIn ? 0 : screenSize.y / 3;
		return screenSize.y - imageSelectionAreaHeight - DimensionUtil.dip2px(mContext, 50+20) - getCardTopMargin();
	}
	
	private int getMaxAllowedWidth(boolean isZoomIn, boolean isInLeftTop) {
		if (!mIsInLeftTop) {
			return screenSize.x - DimensionUtil.dip2px(mContext, LEFT_RIGHT_MARGIN) * 2;
		} else {
			return screenSize.x / 2;
		}
	}
	
	private int getMaxAllowedHeight() {
		return getMaxAllowedHeight(mIsZoomIn, mIsInLeftTop);
	}
	
	private int getMaxAllowedWidth() {
		return getMaxAllowedWidth(mIsZoomIn, mIsInLeftTop);
	}
	
	private AnimationListener generateAnimationListener(final int from, final int to) {
		return new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				hideFoldedOutShadow();
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				checkSwapState(to);
				showFoldedOutShadow();
			}
		};
	}
	
	private void showFoldedOutShadow() {
		if (mMode == MODE_FOLDED_PORTRAIT) {
			if (mStep == 1) {
				LayoutParams params = (LayoutParams) mViewShadowTop.getLayoutParams();
				params.bottomMargin = -getCardTopMargin();
				params.topMargin = - getCardTopMargin();
				params.height = 8;
				mViewShadowTop.bringToFront();
				mViewShadowTop.setVisibility(View.VISIBLE);
				mViewShadowTop.requestLayout();
			} else if (mStep == 3) {
				LayoutParams params = (LayoutParams) mViewShadowTop.getLayoutParams();
				params.bottomMargin = -getCardTopMargin();
				params.topMargin = - getCardTopMargin();
				params.height = 8;
				mViewShadowTop.bringToFront();
				mViewShadowTop.setVisibility(View.VISIBLE);
				mViewShadowTop.requestLayout();
			}
		} else if (mMode == MODE_FOLDED_LANDSCAPE) {
			if (mStep == 1) {
				LayoutParams params = (LayoutParams) mViewShadowLeftRight.getLayoutParams();
				params.addRule(RelativeLayout.RIGHT_OF, R.id.card1);
				params.addRule(RelativeLayout.LEFT_OF, 0);
				params.width = 8;
				mViewShadowLeftRight.setRotation(0);
				mViewShadowLeftRight.bringToFront();
				mViewShadowLeftRight.setVisibility(View.VISIBLE);
				mViewShadowLeftRight.requestLayout();
			} else if (mStep == 4) {
				LayoutParams params = (LayoutParams) mViewShadowLeftRight.getLayoutParams();
				params.addRule(RelativeLayout.RIGHT_OF, 0);
				params.addRule(RelativeLayout.LEFT_OF, R.id.card2);
				params.width = 8;
				mViewShadowLeftRight.setRotation(180);
				mViewShadowLeftRight.bringToFront();
				mViewShadowLeftRight.setVisibility(View.VISIBLE);
				mViewShadowLeftRight.requestLayout();
			}
		}
	}
	
	private void hideFoldedOutShadow() {
		mViewShadowLeftRight.setVisibility(View.INVISIBLE);
		mViewShadowTop.setVisibility(View.INVISIBLE);
	}
	
	private synchronized void checkSwapState(int step) {
		if (step == 1) {
			if(mCard1.isSwapped()) {
				mCard1.swapFrontAndBack();
			}
			
			if (mCard2.isSwapped()) {
				mCard2.swapFrontAndBack();
			}
		} else if (step == 2) {
			if (!mCard1.isSwapped()) {
				mCard1.swapFrontAndBack();
			}
			
			if (mCard2.isSwapped()) {
				mCard2.swapFrontAndBack();
			}
		} else if (step == 3) {
			if (mMode == MODE_FOLDED_PORTRAIT) {
				if (!mCard1.isSwapped()) {
					mCard1.swapFrontAndBack();
				}
				
				if( !mCard2.isSwapped()) {
					mCard2.swapFrontAndBack();
				}
			} else if (mMode == MODE_FOLDED_LANDSCAPE) {
				if (!mCard1.isSwapped()) {
					mCard1.swapFrontAndBack();
				}
				
				if (mCard2.isSwapped()) {
					mCard2.swapFrontAndBack();
				}
			}
		} else if (step ==4) {
			if (!mCard1.isSwapped()) {
				mCard1.swapFrontAndBack();
			}
			
			if( !mCard2.isSwapped()) {
				mCard2.swapFrontAndBack();
			}
		}
	}
	
	private void checkSwapState() {
		checkSwapState(mStep);
		
	}
	
	public void cancelRequest(){
		GreetingCard currentCard = GreetingCardUtil.getCurrentGreetingCard();
		if (currentCard == null) return;
		for (int i = 0; i < currentCard.pages.length; i++) {
			GCPage page = currentCard.pages[i];
			if (page == null) continue;			
			int refreshCount = page.getMainRefreshCount();				
			String pendKey = currentCard.id + FilePathConstant.thumbnail+ page.id;
			if (refreshCount > 0) {
				for (int j = 0 ; j < refreshCount; j++) {
					String tmpPendKey = null;
					if (j == 0) {
						tmpPendKey = pendKey;
					}else {
						tmpPendKey = j + pendKey;
					}
					Request request = pendingRequests.get(tmpPendKey);
					if (request != null) {
						ImageDownloader.cancelRequest(request);
						pendingRequests.remove(tmpPendKey);
					}	
				}			
				pendKey = refreshCount + pendKey;
			}			    
	        Request request = pendingRequests.get(pendKey);
	        if (request != null) {       	
	        	ImageDownloader.cancelRequest(request);       		
	        	pendingRequests.remove(pendKey);				          				        	
	        }        				
		}
	}
	
	private static interface OnCardGestureListener {
		static final int PAN_LEFT = 0;
		static final int PAN_TOP = 1;
		static final int PAN_RIGHT = 2;
		static final int PAN_BOTTOM = 3;
		
		void onPan(int direction);
		void onPinch(boolean out);
	}
	
}
