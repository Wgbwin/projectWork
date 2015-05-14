package com.kodak.kodak_kioskconnect_n2r.view;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.R;

public abstract class BubbleViewOverlay <Item extends OverlayItem> extends ItemizedOverlay<Item> {
	private static final long INFLATION_TIME = 300;
	private static Handler handler = new Handler();
	
	private MapView mapView;
	private BubbleView<Item> bubbleView;

//	private View closeRegion;
	private int viewOffset;
	final MapController mc;
	private Item currentFocusedItem;
	private int currentFocusedIndex;
	
//	private boolean showClose = true;
	private boolean snapToCenter = true;
	
	private static boolean isInflating = false;
	
	
	public BubbleViewOverlay(Drawable defaultMarker,MapView mapView) {
		super(defaultMarker);
		this.mapView = mapView ;
		mc = mapView.getController() ;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Set the horizontal distance between the marker and the bottom of the information
	 * balloon. The default is 0 which works well for center bounded markers. If your
	 * marker is center-bottom bounded, call this before adding overlay items to ensure
	 * the balloon hovers exactly above the marker. 
	 * 
	 * @param pixels - The padding between the center point and the bottom of the
	 * information balloon.
	 */
	public void setBubbleBottomOffset(int pixels) {
		viewOffset = pixels;
	}
	public int getBubbleBottomOffset() {
		return viewOffset;
	}
	
	/**
	 * Override this method to handle a "tap" on a bubble. By default, does nothing 
	 * and returns false.
	 * 
	 * @param index - The index of the item whose bubble is tapped.
	 * @param item - The item whose bubble is tapped.
	 * @return true if you handled the tap, otherwise false.
	 */
	protected boolean onBubbleTap(int index, Item item) {
		return false;
	}
	
	/**
	 * Override this method to perform actions upon an item being tapped before 
	 * its bubble is displayed.
	 * 
	 * @param index - The index of the item tapped.
	 */
	protected void onBubbleOpen(int index) {}
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	@Override
	//protected final boolean onTap(int index) {
	public  boolean onTap(int index) {
		Log.v("findStores", "on map overlay_onTap") ;
		doOnTapEvent(index) ;
		
		return true;
	}
	
	
	public void doOnTapEvent(int index){
		handler.removeCallbacks(finishBalloonInflation);
		isInflating = true;
		handler.postDelayed(finishBalloonInflation, INFLATION_TIME);
		
		currentFocusedIndex = index;
		currentFocusedItem = createItem(index);
		setLastFocusedIndex(index);
		
		onBubbleOpen(index);
		createAndDisplayBubbleViewOverlay();
		
		if (snapToCenter) {
			animateTo(index, currentFocusedItem.getPoint());
		}
	
		
	}
	
	
	/**
	 * Animates to the given center point. Override to customize how the
	 * MapView is animated to the given center point
	 *
	 * @param index The index of the item to center
	 * @param center The center point of the item
	 */
	protected void animateTo(int index, GeoPoint center) {
		mc.animateTo(center);
	}
	
	
	/**
	 * Creates the balloon view. Override to create a sub-classed view that
	 * can populate additional sub-views.
	 */
	protected BubbleView<Item> createBubbleView() {
		return new BubbleView<Item>(getMapView().getContext(), getBubbleBottomOffset());
	}
	
	/**
	 * Expose map view to subclasses.
	 * Helps with creation of balloon views. 
	 */
	protected MapView getMapView() {
		return mapView;
	}
	
	/**
	 * Makes the bubble the topmost item by calling View.bringToFront().
	 */
	public void bringBubbleToFront() {
		if (bubbleView != null) {
			bubbleView.bringToFront();
		}
	}
	
	/**
	 * Sets the visibility of this overlay's bubble view to GONE and unfocus the item. 
	 */
	public void hideBubble() {
		if (bubbleView != null) {
			bubbleView.setVisibility(View.INVISIBLE);
		}
		currentFocusedItem = null;
	}

	/**
	 * Hides the balloon view for any other BalloonItemizedOverlay instances
	 * that might be present on the MapView.
	 * 
	 * @param overlays - list of overlays (including this) on the MapView.
	 */
	private void hideOtherBubbles(List<Overlay> overlays) {
		
		for (Overlay overlay : overlays) {
			if (overlay instanceof BubbleViewOverlay<?> && overlay != this) {
				((BubbleViewOverlay<?>) overlay).hideBubble();
			}
		}
		
	}
	
	
	public void hideAllBubbles() {
		if (!isInflating) {
			List<Overlay> mapOverlays = mapView.getOverlays();
			if (mapOverlays.size() > 1) {
				hideOtherBubbles(mapOverlays);
			}
			hideBubble();
		}
	}
	
	
	/**
	 * Sets the onTouchListener for the balloon being displayed, calling the
	 * overridden {@link #onBubbleTap} method.
	 */
	private OnTouchListener createBubbleTouchListener() {
		return new OnTouchListener() {
			
			float startX;
			float startY;
			
			public boolean onTouch(View v, MotionEvent event) {
				
				View l =  ((View) v.getParent()).findViewById(R.id.bubble_main_layout);
				Drawable d = l.getBackground();
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (d != null) {
						int[] states = {android.R.attr.state_pressed};
						if (d.setState(states)) {
							d.invalidateSelf();
						}
					}
					startX = event.getX();
					startY = event.getY();
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (d != null) {
						int newStates[] = {};
						if (d.setState(newStates)) {
							d.invalidateSelf();
						}
					}
					if (Math.abs(startX - event.getX()) < 40 && 
							Math.abs(startY - event.getY()) < 40 ) {
						// call overridden method
						onBubbleTap(currentFocusedIndex, currentFocusedItem);
					}
					return true;
				} else {
					return false;
				}
				
			}
		};
	}
	
	
	@Override
	public Item getFocus() {
		return currentFocusedItem;
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#setFocus(Item)
	 */
	@Override
	public void setFocus(Item item) {
		super.setFocus(item);	
		currentFocusedIndex = getLastFocusedIndex();
		currentFocusedItem = item;
		if (currentFocusedItem == null) {
			hideBubble();
		} else {
			createAndDisplayBubbleViewOverlay();
		}	
	}
	
	/**
	 * Creates and displays the balloon overlay by recycling the current 
	 * balloon or by inflating it from xml. 
	 * @return true if the balloon was recycled false otherwise 
	 */
	private boolean createAndDisplayBubbleViewOverlay(){
		boolean isRecycled;
		if (bubbleView == null) {
			bubbleView = createBubbleView();
//			clickRegion = (View) bubbleView.findViewById(R.id.balloon_inner_layout);
			bubbleView.setOnTouchListener(createBubbleTouchListener());
//			closeRegion = (View) balloonView.findViewById(R.id.balloon_close);
//			if (closeRegion != null) {
//				if (!showClose) {
//					closeRegion.setVisibility(View.GONE);
//				} else {
//					closeRegion.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							hideBalloon();	
//						}
//					});
//				}
//			}
//			if (showDisclosure && !showClose) {
//				View v = balloonView.findViewById(R.id.balloon_disclosure);
//				if (v != null) {
//					v.setVisibility(View.VISIBLE);
//				}
//			}
			isRecycled = false;
		} else {
			isRecycled = true;
		}
	
		bubbleView.setVisibility(View.INVISIBLE);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		if (mapOverlays.size() > 1) {
			hideOtherBubbles(mapOverlays);
		}
		
		if (currentFocusedItem != null)
			bubbleView.setData(currentFocusedItem);
		
		GeoPoint point = currentFocusedItem.getPoint();
		MapView.LayoutParams params = new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, point,
				MapView.LayoutParams.BOTTOM_CENTER);
		params.mode = MapView.LayoutParams.MODE_MAP;
		
		bubbleView.setVisibility(View.VISIBLE);
		
		if (isRecycled) {
			bubbleView.setLayoutParams(params);
		} else {
			mapView.addView(bubbleView, params);
		}
		
		return isRecycled;
	}
	
	public void setSnapToCenter(boolean snapToCenter) {
		this.snapToCenter = snapToCenter;
	}

	public static boolean isInflating() {
		return isInflating;
	}
	
	private static Runnable finishBalloonInflation = new Runnable() {
		public void run() {
			isInflating = false;
		}
	};
	
	
	public boolean isBubbleVisible(){
		boolean isBubbleVisible = false ;
		if(bubbleView!=null){
			if(bubbleView.getVisibility()==View.VISIBLE){
				isBubbleVisible = true ;
			}
		}
		
		return isBubbleVisible ;
	}
	

}
