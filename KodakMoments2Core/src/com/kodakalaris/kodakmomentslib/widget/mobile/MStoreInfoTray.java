package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.findstore.BaseDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.StoreListAdapter;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.DragablePanel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class MStoreInfoTray extends DragablePanel {
	private static final String TAG = MStoreInfoTray.class.getSimpleName();

	private Context mContext;
	public static  Gallery vGvImages;
	private ImageView vIvThumbnail, vBack, vNext;
	public static StoreListAdapter mAdapter;
	private List<StoreInfo> stores;
	private PhotoInfo mLastImage;
	private DisplayImageOptions mOptions;
	private int mTotalStoreSize;

	public MStoreInfoTray(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);

	}

	public MStoreInfoTray(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		setOnDrawerOpenListener(drawerOpenListener);
		setOnDrawerCloseListener(drawerCloseListener);
		setOnDrawerScrollListener(drawerScrollListener);
		mOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	public void initialize(final List<StoreInfo> stores) {
		this.stores = stores;
		vGvImages = (Gallery) findViewById(R.id.store_gv_images);
		vBack = (ImageView) findViewById(R.id.storeList_back);
		vNext = (ImageView) findViewById(R.id.storeList_next);
		mAdapter = new StoreListAdapter(mContext, stores);
		vGvImages.setAdapter(mAdapter);
		vGvImages.setCallbackDuringFling(false);
		vGvImages.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.e("selectedStorePositon", "" + position);
				ShoppingCartManager.getInstance().setmSelectedStorePosition(position);
				showStoreLocation(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				ShoppingCartManager.getInstance().setmSelectedStorePosition(0);
				showStoreLocation(0);
			}
		});
		mTotalStoreSize = stores.size();
		vNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = 0;
				position = ShoppingCartManager.getInstance().getmSelectedStorePosition();
				if (position != mTotalStoreSize - 1) {
					vGvImages.setSelection(position + 1, false);
					showStoreLocation(position + 1);
					ShoppingCartManager.getInstance().setmSelectedStorePosition(position + 1);
				}

			}
		});
		vBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = 0;
				position = ShoppingCartManager.getInstance().getmSelectedStorePosition();
				if (position != 0) {
					vGvImages.setSelection(position - 1, false);
					showStoreLocation(position - 1);
					ShoppingCartManager.getInstance().setmSelectedStorePosition(position - 1);
				} else {

				}
			}
		});

		refresh(stores);
		close();
	}

	OnDrawerOpenListener drawerOpenListener = new OnDrawerOpenListener() {

		@Override
		public void onDrawerOpened(Rect contentRect) {
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) vGvImages.getLayoutParams();
			vGvImages.layout(contentRect.left + params.leftMargin, contentRect.top, contentRect.right - params.rightMargin, contentRect.bottom);

		}
	};
	OnDrawerCloseListener drawerCloseListener = new OnDrawerCloseListener() {

		@Override
		public void onDrawerClosed() {

		}
	};
	OnDrawerScrollListener drawerScrollListener = new OnDrawerScrollListener() {

		@Override
		public void onScrollStarted() {

		}

		@Override
		public void onScrollEnded(boolean closed, Rect contentRect) {
			if (closed) {

			} else {
				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) vGvImages.getLayoutParams();
				vGvImages.layout(contentRect.left + params.leftMargin, contentRect.top, contentRect.right - params.rightMargin, contentRect.bottom);
			}
		}
	};

	private void showStoreLocation(int position) {
		if (((BaseDestinationStoreSelectionActivity) mContext).googleMap != null && stores != null && stores.size() != 0) {

			((BaseDestinationStoreSelectionActivity) mContext).googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stores.get(position).latitude, stores.get(position).longitude), 15));
		}
	}

	public void refresh(List<StoreInfo> stores) {
		this.stores = stores;
		if (stores != null && stores.size() > 0) {
			if (getVisibility() != View.VISIBLE) {
				setVisibility(View.VISIBLE);
			}

			mAdapter.notifyDataSetChanged();
			relayoutContent();
			refreshContent();
		} else {
			close();
			setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected boolean needInterceptTouchEvent(MotionEvent event) {
		Rect rect = new Rect();
		int[] location = new int[2];
		ImageView[] img = { vBack, vNext };
		for (int i = 0; i < img.length; i++) {
			img[i].getLocationInWindow(location);
			rect.left = location[0];
			rect.top = location[1];
			rect.right = rect.left + img[i].getWidth();
			rect.bottom = rect.top + img[i].getHeight();
			if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
				return false;
			}
		}

		return true;
	}

}
