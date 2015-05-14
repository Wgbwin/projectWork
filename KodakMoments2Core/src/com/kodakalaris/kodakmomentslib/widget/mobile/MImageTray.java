package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.AppConstants.FlowType;
import com.kodakalaris.kodakmomentslib.activity.BaseActivity;
import com.kodakalaris.kodakmomentslib.activity.printsreview.MPrintsReviewActivity;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.interfaces.IPhotoOperationInterface;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.DragablePanel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 
 * @author Kane
 *
 */
public class MImageTray extends DragablePanel {
	private static final String TAG = MImageTray.class.getSimpleName();
	
	private Context mContext;
	
	private ImageView vIvThumbnail;
	private TextView vTvImageNum;
	private Button vBtnNext;
	private GridView vGvImages;
	private ImageTrayAdapter mAdapter;
	private List<PhotoInfo> mImages;
	private View vDisableScreen;
	private PhotoInfo mLastImage;
	private DisplayImageOptions mOptions;
	
	
	public MImageTray(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MImageTray(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mContext = context;
		setOnDrawerOpenListener(drawerOpenListener);
		setOnDrawerCloseListener(drawerCloseListener);
		setOnDrawerScrollListener(drawerScrollListener);
		mOptions = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
	}
	
	public void setShandow(View disableScreen){
		this.vDisableScreen = disableScreen;
		vDisableScreen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				close();
			}
		});
	}
	
	public void initialize(List<PhotoInfo> images){
		this.mImages = images;
		vIvThumbnail = (ImageView) findViewById(R.id.iv_image_thubmail);
		vTvImageNum = (TextView) findViewById(R.id.tv_images_number);
		vBtnNext = (Button) findViewById(R.id.btn_tray_next);
		vGvImages = (GridView) findViewById(R.id.gv_images);
		
		mAdapter = new ImageTrayAdapter(mContext);
		vGvImages.setAdapter(mAdapter);
		
		vBtnNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FlowType mFlowType = KM2Application.getInstance().getFlowType();
				if (mFlowType.isPrintWorkFlow()){
					PrintManager manager = PrintManager.getInstance(mContext);
					manager.createNewPrintItems(mImages);
				} else if (mFlowType.isPhotoBookWorkFlow()){
					
				}else if (mFlowType.isCollageWorkFlow()){
					
				}else if (mFlowType.isGreetingCardWorkFlow()){
					
				}else if (mFlowType.isKioskWorkFlow()){
					
				}else if (mFlowType.isPrintHubWorkFlow()){
					PrintHubManager manager = PrintHubManager.getInstance();
					manager.createNewPrintItems(mImages);
				}
				((BaseActivity) mContext).startUploadService();
			
				Intent myIntent = new Intent(mContext, MPrintsReviewActivity.class);
				mContext.startActivity(myIntent);
				((Activity)mContext).finish();
			}
		});
		
//		refresh(images);
		close();
	}
	
	public void refresh(List<PhotoInfo> images){
		this.mImages = images;
		if(images != null && images.size() > 0){
			if(getVisibility() != View.VISIBLE){
				setVisibility(View.VISIBLE);
			}
			PhotoInfo lastImage = images.get(images.size()-1);
			if(!lastImage.equalsNotConsiderDesId(mLastImage)){
				if(lastImage.getPhotoSource().isFromPhone()){
					ImageLoader.getInstance().displayImage("file://"+lastImage.getPhotoPath(), vIvThumbnail, mOptions);
					mLastImage = lastImage;
				}
			}
			vTvImageNum.setText(mContext.getString(R.string.ImageSelection_album_photos_count, images.size()));
			mAdapter.notifyDataSetChanged();
			relayoutContent();
			refreshContent();
		} else {
			close();
			setVisibility(View.INVISIBLE);
		}
	}
	
	private class ImageTrayAdapter extends BaseAdapter {
		
		private LayoutInflater mLayoutInflater;
		private IPhotoOperationInterface iPhotoOperationInterface;
		
		public ImageTrayAdapter(Context context){
			mLayoutInflater = LayoutInflater.from(context);
			iPhotoOperationInterface = (IPhotoOperationInterface) context;
		}

		@Override
		public int getCount() {
			return mImages.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if(convertView == null){
				holder = new Holder();
				convertView = mLayoutInflater.inflate(R.layout.item_m_imagetray, null);
				holder.kmImageView = (KMImageView) convertView.findViewById(R.id.kiv_thumb);
				holder.kmImageView.setImageContentViewScaleType(ScaleType.CENTER_CROP);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			// always out of bounds because position equals 0, but still get the image from images set
			// currently add a judgment that position<mImages.size() to fix above issue. but need to check why the issue occurred - Kane
			if(position < mImages.size()){
				PhotoInfo image = mImages.get(position);
				holder.kmImageView.setPhoto(image);
				holder.kmImageView.setmSelected(image.isSelected());
				holder.kmImageView.setmPhotoOperationListener(iPhotoOperationInterface);
				ImageLoader.getInstance().displayImage("file://"+image.getPhotoPath(), holder.kmImageView.getvImageContentView(), mOptions);
			}
			return convertView;
		}
		
		private class Holder{
			KMImageView kmImageView;
		}
		
	}
	
	OnDrawerScrollListener drawerScrollListener = new OnDrawerScrollListener() {
		
		@Override
		public void onScrollStarted() {
			vDisableScreen.setVisibility(View.VISIBLE);
		}
		
		@Override
		public void onScrollEnded(boolean closed, Rect contentRect) {
			if(closed){
				vDisableScreen.setVisibility(View.GONE);
			} else {
				RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) vGvImages.getLayoutParams();
				vGvImages.layout(contentRect.left+params.leftMargin, contentRect.top, contentRect.right-params.rightMargin, contentRect.bottom);
				vDisableScreen.setVisibility(View.VISIBLE);
			}
		}
	};
	
	OnDrawerCloseListener drawerCloseListener = new OnDrawerCloseListener() {
		
		@Override
		public void onDrawerClosed() {
			vDisableScreen.setVisibility(View.GONE);
		}
	};
	
	OnDrawerOpenListener drawerOpenListener = new OnDrawerOpenListener() {
		
		@Override
		public void onDrawerOpened(Rect contentRect) {
			RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) vGvImages.getLayoutParams();
			vGvImages.layout(contentRect.left+params.leftMargin, contentRect.top, contentRect.right-params.rightMargin, contentRect.bottom);
			vDisableScreen.setVisibility(View.VISIBLE);
		}
	};

	@Override
	protected boolean needInterceptTouchEvent(MotionEvent event) {
		Rect rect = new Rect();
		int[] location = new int[2];
		vBtnNext.getLocationOnScreen(location);
		rect.left = location[0];
		rect.top = location[1];
		rect.right = rect.left + vBtnNext.getWidth();
		rect.bottom = rect.top + vBtnNext.getHeight();
		if(rect.contains((int)event.getRawX(), (int)event.getRawY())){
			return false;
		}
		return true;
	}

}
