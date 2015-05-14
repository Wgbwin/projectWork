package com.kodak.rss.tablet.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.adapter.PhotoBooksProductAdapter;
import com.kodak.rss.tablet.bean.ExchangeTaskPo;
import com.kodak.rss.tablet.bean.PhotoLocationPo;
import com.kodak.rss.tablet.thread.RearrangeExchangeTasks;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * Purpose: Author: Bing Wang Created Time: Aug 4, 2014 10:20:43 AM
 */
public class DragRelativeLayoutView extends RelativeLayout{

	private PhotoBookGiveUpitemsView giveUpView;
	private ListView dragListView;

	private long dragResponseMS = 500;

	private Bitmap mDragBitmap;

	private int moveX;
	private int moveY;
	private float mRawX, mRawY;

	private Handler mHandler;
	private DisplayMetrics dm;

	private Context context;

	private int speed = 120;
	private int mDownScrollBorder;
	private int mUpScrollBorder;

	private Rect viewRect;
	private int[] glocation;
	private int gheight;

	private int fristVisiable;
	public int visiableNum;
	private List<PhotoLocationPo> moreParamsList;
	private Photobook mPhotobook;

	private PhotoBooksProductActivity activity;
	private DragHelper dragHelper;
	private RearrangeExchangeTasks rETasks;

	public DragRelativeLayoutView(Context context) {
		this(context, null);
	}

	public DragRelativeLayoutView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragRelativeLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	private PhotoLocationPo selectedPo;

	private void initView(Context context) {
		this.context = context;
		activity = (PhotoBooksProductActivity) context;
		this.dragHelper = new ReDragHelper(context,new SelectImageView(context));
	}

	private HorizontalListView lView;

	public void setHandler(Handler handler) {
		this.mHandler = handler;
		this.mPhotobook = PhotoBookProductUtil.getCurrentPhotoBook();
		rETasks = new RearrangeExchangeTasks(context,handler);
		giveUpView = (PhotoBookGiveUpitemsView) this.findViewById(R.id.give_up_items);
		dragListView = (ListView) this.findViewById(R.id.photoList);

		dm = getResources().getDisplayMetrics();		
		viewRect = new Rect();
		glocation = new int[2];	
		speed = dm.heightPixels/4;
	}
	
	public void setTasksNull() {
		if (rETasks != null) {
			rETasks.interrupt();
			rETasks = null;
		}
	}

	private Runnable mLongClickRunnable = new Runnable() {
		@Override
		public void run() {
			if (selectedPo != null && selectedPo.view != null) {
				if (!viewRect.contains((int)mRawX, (int)mRawY))return;
				if (selectedPo.hPosition == -1) {
					giveUpView.selectedPo = selectedPo;					
				}else {
					PhotoBooksProductAdapter rearrangeAdapter = (PhotoBooksProductAdapter)dragListView.getAdapter();
					rearrangeAdapter.selectedPo = selectedPo;
				}
				dragHelper.createDragImage(mDragBitmap);
			}
		}
	};

	private Rect getRectInView(View child) {
		Rect viewRect = new Rect();
		int[] childPosition = new int[2];
		child.getLocationOnScreen(childPosition);
		int left = childPosition[0];
		int right = left + child.getWidth();
		int top = childPosition[1];
		int bottom = top + child.getHeight();
		viewRect.set(left, top, right, bottom);
		return viewRect;
	}

	private float rawX,rawY;
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (giveUpView == null)return super.dispatchTouchEvent(ev);
		if (dragListView == null)return super.dispatchTouchEvent(ev);
		if (giveUpView.getVisibility() != View.VISIBLE)return super.dispatchTouchEvent(ev);
		if (dragListView.getVisibility() != View.VISIBLE)return super.dispatchTouchEvent(ev);

		moveX = (int) ev.getX();
		moveY = (int) ev.getY();
		mRawX = ev.getRawX();
		mRawY = ev.getRawY();		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			rawX = mRawX;
			rawY = mRawY;
			viewRect.set(0, 0, 0, 0);
			selectedPo = null;
			giveUpView.selectedPo = null;
			mDragBitmap = null;
			mHandler.postDelayed(mLongClickRunnable, dragResponseMS);
			giveUpView.getLocationOnScreen(glocation);
			gheight = giveUpView.getHeight();
			lView = giveUpView.lView;						
			if (giveUpView.giveLayerList != null && giveUpView.giveLayerList.size() > 0 && lView != null 
					&& glocation[1] <= mRawY && mRawY < glocation[1] + gheight) {								
				int childCount = lView.getChildCount();				
				int fV = lView.getFirstVisiblePosition();	
				for (int i = 0; i < childCount; i++) {
					View child = lView.getChildAt(i);
					Rect viewRectChild = getRectInView(child);
					if (viewRectChild.contains((int) mRawX, (int) mRawY)) {
						int w = viewRectChild.right - viewRectChild.left;
						int h = viewRectChild.bottom - viewRectChild.top;						
						dragHelper.setWH(w,h);
						dragHelper.setXY(mRawX, mRawY);						
						viewRect = viewRectChild;																	
						int layerInPos = fV+i;	
						SelectImageView sv = null;
						if (giveUpView.giveLayerList != null && layerInPos < giveUpView.giveLayerList.size()) {
							sv = (SelectImageView) child.findViewById(R.id.photo);
							selectedPo = new PhotoLocationPo(-1, fV+i, viewRect.left, viewRect.top, w, h, giveUpView.giveLayerList.get(fV+i), sv);
						}
						mDownScrollBorder = (int) (dragListView.getHeight()/5 + dm.density*50);
						mUpScrollBorder = (int) (dragListView.getHeight()*4/5 + dm.density*20);					
						if (sv != null) {
							mDragBitmap = sv.getImageBitmap();
							if (mDragBitmap == null) {
								mDragBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
							}
						}
						break;
					}
				}
				return super.dispatchTouchEvent(ev);
			}
			
			int mDragPosition = dragListView.pointToPosition(moveX, moveY);			
			if (mDragPosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}
			final PhotoBooksProductAdapter rearrangeAdapter = (PhotoBooksProductAdapter)dragListView.getAdapter();
			rearrangeAdapter.selectedPo = null;
			float width = rearrangeAdapter.vlayoutParams.width + dm.density* 10;
			if (mPhotobook.isDuplex)width = 2 * width;
			if (0 <= mRawX && width >= mRawX) {
				rearrangeAdapter.selectedPostions[0] = mDragPosition;
				rearrangeAdapter.selectedPostions[1] = -1;				
				rearrangeAdapter.activity.currentPage = PhotoBookProductUtil.getCurrentPage(mDragPosition, rearrangeAdapter);
				int pos = PhotoBookProductUtil.getPageInListPosition(rearrangeAdapter.activity.currentPage);
				rearrangeAdapter.activity.pbLayout.pageTo(pos);
				rearrangeAdapter.notifyDataSetChanged();

				activity.pagesAdapter.selectedPostions[0] = 2 * pos;
				if (pos == activity.pagesAdapter.itemSize - 1) {
					activity.pagesAdapter.selectedPostions[1] = -1;
				} else {
					activity.pagesAdapter.selectedPostions[1] = 2 * pos + 1;
				}
				activity.pagesAdapter.notifyDataSetChanged();
				return true;
			}
						
			getSimplexPopVisiableItemsParam(dragListView);			
			for (int i = 0; i < moreParamsList.size(); i++) {
				PhotoLocationPo po = moreParamsList.get(i);
				if (po.layer != null && (po.x <= mRawX && (po.x + po.width) >= mRawX)&& (po.y <= mRawY && (po.y + po.height) >= mRawY)) {				
									
					dragHelper.setWH(po.width,po.height);
					dragHelper.setXY(mRawX, mRawY);

					selectedPo = po;
					break;
				}
			}
			if (selectedPo == null)return super.dispatchTouchEvent(ev);
			if (selectedPo.view == null)return super.dispatchTouchEvent(ev);
			int[] location = new int[2];
			selectedPo.view.getLocationOnScreen(location);
			if (location[0] < 0 || location[1] < 0) {
				selectedPo = null;
				rearrangeAdapter.selectedPo = null;
				return super.dispatchTouchEvent(ev);
			}

			int left = location[0];
			int right = left + dragHelper.width;
			int top = location[1];
			int bottom = top + dragHelper.height;
			viewRect.set(left, top, right, bottom);

			mDownScrollBorder = (int) (dragListView.getHeight()/5 + dm.density*50);
			mUpScrollBorder = (int) (dragListView.getHeight()*4/5 + dm.density*20);

			mDragBitmap = ((SelectImageView) selectedPo.view).getImageBitmap();
			if (mDragBitmap == null) {
				mDragBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.imagewait60x60);
			}
			break;
		case MotionEvent.ACTION_MOVE:			
			dragHelper.setXY(mRawX, mRawY);	
			float space = dragHelper.spacing(mRawX,rawX,mRawY,rawY);
			rawX = mRawX;
			rawY = mRawY;			
			if (space > dm.density*5 || !dragHelper.isTouchInItem(moveX, moveY)) {
				mHandler.removeCallbacks(mLongClickRunnable);
			}
			if (dragHelper.mDragImageView != null) {
				dragHelper.onDrag(moveX, moveY, mRawX, mRawY);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			mHandler.removeCallbacks(mLongClickRunnable);
			mHandler.removeCallbacks(mScrollRunnable);		
			if (dragHelper.mDragImageView != null) {
				onStopDrag(moveX, moveY, mRawX, mRawY);
				return true;
			}								
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private Runnable mScrollRunnable = new Runnable() {
		@Override
		public void run() {
			if (mHandler == null)return;
			int scrollY;
			if (dragHelper.oy > mUpScrollBorder) {//moveY  mRawY 
				scrollY = -speed;
				mHandler.postDelayed(mScrollRunnable, 15);
			} else if (dragHelper.oy < mDownScrollBorder) {
				scrollY = speed;
				mHandler.postDelayed(mScrollRunnable, 15);
			} else {
				scrollY = 0;
				mHandler.removeCallbacks(mScrollRunnable);
				return;
			}			
			if (scrollY == 0) return;
			int mPos = dragListView.pointToPosition(moveX, moveY);
			if (mPos == AdapterView.INVALID_POSITION)return;		
			View view = dragListView.getChildAt(mPos - dragListView.getFirstVisiblePosition());
			if (view == null)return;			
			dragListView.smoothScrollToPositionFromTop(mPos, view.getTop() + scrollY);
		}
	};
	
	public void rearToOther(boolean fromRear){
		if (rETasks == null) {
			rETasks = new RearrangeExchangeTasks(context,mHandler);
		}
		rETasks.setSkipFlag(fromRear);
	}

	private void onStopDrag(int mDownX, int mDownY, float rawX, float rawY) {
		activity.displayRearScrollPropmt(false);
		dragHelper.removeDragImage();
		final PhotoBooksProductAdapter rearrangeAdapter = (PhotoBooksProductAdapter) dragListView.getAdapter();
		if (selectedPo == null) return;
		// put photo to give up drag
		if (glocation[1] <= rawY && rawY < glocation[1] + gheight) {
			if (selectedPo.hPosition == -1) {
				giveUpNotify();					
				selectedPo = null;
			} else {
				PhotobookPage selectPage = activity.simplexPages.get(selectedPo.hPosition);			
				if (selectPage == null) {
					rearrangeAdapterNotify(rearrangeAdapter);	
					return;
				}
				ArrayList<Layer> seleteLayerList = PhotoBookProductUtil.getImageTypeLayers(selectPage.layers);
				if (seleteLayerList == null|| (seleteLayerList != null && seleteLayerList.size() == 0)) {
					rearrangeAdapterNotify(rearrangeAdapter);	
					return;
				}

				Layer fromLayer = selectedPo.layer;
				int pos = PhotoBookProductUtil.findLayerPosition(selectPage,fromLayer);
				int dealPos = 0;
				if (giveUpView.giveLayerList != null) {
					dealPos = giveUpView.giveLayerList.size();
				}
//				RearrangeExchangePicTask task = new RearrangeExchangePicTask(context, selectPage, null, fromLayer, pos, dealPos,null, RearrangeExchangePicTask.RemovePic);
//				task.execute();
				if (rETasks == null) {
					rETasks = new RearrangeExchangeTasks(context,mHandler);
				}			
				rETasks.addTask(ExchangeTaskPo.RemovePic, selectPage, fromLayer, pos, null, dealPos, null);
						
				PhotoBookProductUtil.setLayerNull(fromLayer.contentId,selectPage.layers);				
				PhotoBookProductUtil.resetLayers(selectPage.layers);
				
				giveUpView.addLayer(fromLayer);

				selectedPo = null;
				giveUpNotify();		
				rearrangeAdapter.selectedPo = null;
				rearrangeAdapter.notifyDataSetChanged();
			}
			return;
		}

		// put photo to ListView
		int mPos = dragListView.pointToPosition(mDownX, mDownY);	
		if (mPos == AdapterView.INVALID_POSITION){				
			giveUpNotify();				
			rearrangeAdapterNotify(rearrangeAdapter);	
			return;
		}
		
		PhotobookPage page = null;		
		if (mPos != selectedPo.hPosition) {
			page = activity.simplexPages.get(mPos);
		}	
		if (page == null) {								
			giveUpNotify();								
			rearrangeAdapterNotify(rearrangeAdapter);				
			return;
		}			
			
		try {			
			Layer[] layers = page.layers;
			if (layers != null) {
				PhotoBookProductUtil.resetLayers(layers);
				int addSize = PhotoBookProductUtil.getIsNotNullNum(layers);
				ArrayList<Layer> layerList = PhotoBookProductUtil.getImageTypeLayers(page.layers);

				if (selectedPo.hPosition == -1) {
					if (!PhotoBookProductUtil.isTitlePage(page) && !PhotoBookProductUtil.isBackCoverPageBlank(page) && PhotoBookProductUtil.getIsNotNullNum(layers) > layers.length - 1) {
						new InfoDialog.Builder(context).setMessage(R.string.page_enough_prompt).setPositiveButton(context.getText(R.string.d_ok),null).create().show();		
						giveUpNotify();
					} else {
						Layer fromLayer = selectedPo.layer;
						int pos = selectedPo.position;						
						Layer delLayer  = null;
						if (PhotoBookProductUtil.isTitlePage(page) || PhotoBookProductUtil.isBackCoverPageBlank(page)) {
							if (addSize > 0 && layerList != null && layerList.size() > 0) {								
								delLayer = layerList.get(0);																		
							}
							addSize = 0;
						}
//						RearrangeExchangePicTask task = new RearrangeExchangePicTask(context, null, page, fromLayer, pos, addSize,delLayer, RearrangeExchangePicTask.AddPic);
//						task.execute();
						if (rETasks == null) {
							rETasks = new RearrangeExchangeTasks(context,mHandler);
						}						
						rETasks.addTask(ExchangeTaskPo.AddPic, null, fromLayer, pos, page, addSize, delLayer);

						layers[addSize] = fromLayer;
//						giveUpView.removeLayer(fromLayer);										
						if (PhotoBookProductUtil.isTitlePage(page) || PhotoBookProductUtil.isBackCoverPageBlank(page)) {
							giveUpView.addLayer(delLayer);
						}
						giveUpView.selectedPo = null;
						setNotityAnimation(fromLayer,pos);
					}						
				} else {
					PhotobookPage selectPage = activity.simplexPages.get(selectedPo.hPosition);						
					if (selectPage == null) {												
						rearrangeAdapterNotify(rearrangeAdapter);				
						return;						
					}
					ArrayList<Layer> seleteLayerList = PhotoBookProductUtil.getImageTypeLayers(selectPage.layers);
					if (seleteLayerList == null|| (seleteLayerList != null && seleteLayerList.size() == 0)) {											
						rearrangeAdapterNotify(rearrangeAdapter);														
						return;
					}
					if (PhotoBookProductUtil.isTitlePage(page) || PhotoBookProductUtil.isBackCoverPageBlank(page) ) {
						Layer fromLayer = selectedPo.layer;
						int pos = PhotoBookProductUtil.findLayerPosition(selectPage, fromLayer);
						Layer delLayer = null;
						if (layerList != null && layerList.size() > 0) {							
							delLayer = layerList.get(0);							
						}
						addSize = 0;
//						RearrangeExchangePicTask task = new RearrangeExchangePicTask(context, selectPage, page, fromLayer, pos,dealItem, delLayer,RearrangeExchangePicTask.ExchangePic);
//						task.execute();
						if (rETasks == null) {
							rETasks = new RearrangeExchangeTasks(context,mHandler);
						}						
						rETasks.addTask(ExchangeTaskPo.ExchangePic, selectPage, fromLayer, pos, page, addSize, delLayer);

						giveUpView.addLayer(delLayer);
						layers[addSize] = seleteLayerList.get(selectedPo.position);
						if (fromLayer != null) {
							PhotoBookProductUtil.setLayerNull(fromLayer.contentId, selectPage.layers);
						}
						PhotoBookProductUtil.resetLayers(selectPage.layers);
						giveUpNotify();						
					} else {
						if (PhotoBookProductUtil.getIsNotNullNum(layers) > layers.length - 1) {
							new InfoDialog.Builder(context).setMessage(R.string.page_enough_prompt).setPositiveButton(context.getText(R.string.d_ok),null).create().show();
						} else {
							Layer fromLayer = selectedPo.layer;
							int pos = PhotoBookProductUtil.findLayerPosition(selectPage,fromLayer);						
//							RearrangeExchangePicTask task = new RearrangeExchangePicTask(context, selectPage, page, fromLayer,pos, addSize, delLayer,RearrangeExchangePicTask.ExchangePic);
//							task.execute();
							if (rETasks == null) {
								rETasks = new RearrangeExchangeTasks(context,mHandler);
							}							
							rETasks.addTask(ExchangeTaskPo.ExchangePic, selectPage, fromLayer, pos, page, addSize, null);						

							layers[addSize] = seleteLayerList.get(selectedPo.position);
							
							PhotoBookProductUtil.setLayerNull(fromLayer.contentId,selectPage.layers);								
							PhotoBookProductUtil.resetLayers(selectPage.layers);
						}											
					}
				}
			}			
		} catch (Exception e) {
		}		
		rearrangeAdapterNotify(rearrangeAdapter);		
	}

	private void giveUpNotify(){	
		giveUpView.selectedPo = null;
		giveUpView.imageAdapter.notifyDataSetChanged();			
	}
	
	private void rearrangeAdapterNotify(PhotoBooksProductAdapter rearrangeAdapter){	
		selectedPo = null;
		rearrangeAdapter.selectedPo = null;
		rearrangeAdapter.notifyDataSetChanged();
	}
	
	
	public void getSimplexPopVisiableItemsParam(ListView dragListView) {
		moreParamsList = new ArrayList<PhotoLocationPo>(4);
		if (dragListView == null) return;
		fristVisiable = dragListView.getFirstVisiblePosition();
		visiableNum = dragListView.getChildCount();
		
		for (int i = 0; i < visiableNum; i++) {
			LinearLayout v = (LinearLayout) dragListView.getChildAt(i);
			Page page = activity.simplexPages.get(fristVisiable + i);
			if (v != null && page != null) {
				LinearLayout LView = (LinearLayout) v.findViewById(R.id.l_page_content);
				ArrayList<Layer> layerList = PhotoBookProductUtil.getImageTypeLayers(page.layers);
				if (LView != null && layerList != null) {
					for (int k = 0; k < LView.getChildCount(); k++) {
						View photoView = LView.getChildAt(k);
						int[] location = new int[2];
						photoView.getLocationOnScreen(location);
						if (location[0] == 0 && location[1] == 0)break;
						int width = photoView.getWidth();
						int height = photoView.getHeight();

						Layer layer = null;
						if (layerList.size() > k) {
							layer = layerList.get(k);
						}
						PhotoLocationPo po = new PhotoLocationPo(fristVisiable+ i, k, location[0], location[1], width,height, layer, photoView);
						po.isFront = 0;
						moreParamsList.add(po);
					}
				}
			}
		}		
	}
	
	private class ReDragHelper extends DragHelper{
		public ReDragHelper(Context context, View popView) {
			super(context, popView);			
		}

		@Override
		public void setCreateDragView(Bitmap bitmap, String... promptStr) {										
			((SelectImageView) mDragImageView).setImageBitmap(bitmap, true, true);	
			if (giveUpView.selectedPo != null) {				
				giveUpView.imageAdapter.notifyDataSetChanged();				
			}else {
				((BaseAdapter) dragListView.getAdapter()).notifyDataSetChanged();	
			}
			mHandler.removeCallbacks(mLongClickRunnable);
			activity.displayRearScrollPropmt(true);
//			if (mHandler != null) {
//				mHandler.post(mScrollRunnable);
//			}
		}

		@Override
		public void setOnDragView(int moveX, int moveY,float rawX,float rawY) {	
			if (mHandler != null) {
				mHandler.post(mScrollRunnable);
			}
		}		
	}
	
	//animation 
	private void setNotityAnimation(Layer layer,int selectPos){
		if (lView == null) return;								
		int childCount = lView.getChildCount();		
		boolean isHid = false;
		Rect rect = getRectInView(lView.getChildAt(0));
		if (rect != null && rect.left < giveUpView.itemWidth ) {			
			isHid = true;				
		}
		if (isHid) {	
			giveUpView.removeLayer(layer);
			giveUpView.imageAdapter.notifyDataSetChanged();		
			giveUpView.lView.setVisSelection();
			
//			int fromPos = fristPosition;
//			int toPos = selectPos - 1;			
//			for (int pos = fromPos; pos <= toPos; pos++) {
//					View childView = lView.getChildAt(pos-fristPosition);
//					if (childView != null) {
//						float fromX = 0;						
//						float toX = giveUpView.imageWidth;	
//						reSetAnimationListener(layer,fromX, toX, pos, toPos, childView,true);
//					}				
//			}									
		}else {
			int fromPos = selectPos + 1;
			int toPos = childCount - 1;
			if (fromPos > toPos) {
				giveUpView.removeLayer(layer);
				giveUpView.imageAdapter.notifyDataSetChanged();				
				return;
			}
			for (int pos = fromPos; pos <= toPos; pos++) {
				View childView = lView.getChildAt(pos);
				if (childView != null) {
					float fromX = 0;						
					float toX = -giveUpView.imageWidth;	
					reSetAnimationListener(layer,fromX, toX, pos, toPos, childView,false);
				}				
			}	
		}
	}
	
	private String LastAnimationID;
	private void reSetAnimationListener(final Layer fromLayer,float fromX,float toX, int position,int end,final View moveView,final boolean isLeftToRight){	
		Animation animation = getMoveAnimation(fromX, toX);
		moveView.startAnimation(animation);						
		if (position == end ) LastAnimationID = animation.toString();
		
		animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {}

			@Override
			public void onAnimationRepeat(Animation animation) {}

			@Override
			public void onAnimationEnd(Animation animation) {							
				moveView.clearAnimation();			
				String animaionID = animation.toString();
				if (animaionID.equalsIgnoreCase(LastAnimationID)) {
					giveUpView.removeLayer(fromLayer);
					giveUpView.imageAdapter.notifyDataSetChanged();		
					if (isLeftToRight) {																			
																
					}
				}											
			}
		});			
	}
		
	private Animation getMoveAnimation(float fromX, float toX){		
		TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, 0, 0);		
		translateAnimation.setFillAfter(true);		
		translateAnimation.setDuration(300);	//300
		return translateAnimation;
	}	
	

}
