package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.imageselection.ImageSelector;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.interfaces.IPhotoOperationInterface;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageSelectionKodakAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoInfo> mPhotos;
	private List<List<PhotoInfo>> mPhotoRows;

	private int mGuttersSpace;
    private IPhotoOperationInterface onPhotoOperationListener;
    private ImageSelector mImageSelector;
    
	public ImageSelectionKodakAdapter(Context context) {
		this.mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGuttersSpace = mContext.getResources().getDimensionPixelSize(
				R.dimen.list_item_space);
	}

	public ImageSelectionKodakAdapter(Context context, List<PhotoInfo> photos, IPhotoOperationInterface onPhotoOperationListener ,ImageSelector mImageSelector ) {
		this(context);
		this.mPhotos = photos;
		buildPhotosInRow(photos);
		this.onPhotoOperationListener = onPhotoOperationListener;
		this.mImageSelector = mImageSelector;
	}

	public void setDataSource(List<PhotoInfo> dataList) {
		mPhotos = dataList;
		if (mPhotos != null && getCount() > 0) {
			buildPhotosInRow(dataList);
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}

	private void buildPhotosInRow(List<PhotoInfo> photos) {
		if (mPhotoRows == null) {
			mPhotoRows = new ArrayList<List<PhotoInfo>>();
		} else {
			mPhotoRows.clear();
		}
		if (photos != null && photos.size() > 0) {
			double total = 0.0;
			List<PhotoInfo> subPhotoRowList = new ArrayList<PhotoInfo>();
			for (PhotoInfo photoInfo : photos) {
				double sizeFactor = photoInfo.getPhotoSizeType()
						.getSizeFactor();
				total += sizeFactor;
				if (total > 4) {
					List<PhotoInfo> subClone = new ArrayList<PhotoInfo>(
							subPhotoRowList);
					mPhotoRows.add(subClone);
					subPhotoRowList.clear();
					total = sizeFactor;
				}
				subPhotoRowList.add(photoInfo);
			}

			if (subPhotoRowList != null && subPhotoRowList.size() > 0) {
				mPhotoRows.add(subPhotoRowList);
			}
		}
	}

	@Override
	public int getCount() {
		if (mPhotoRows != null) {
			return mPhotoRows.size();
		} else {
			return 0;
		}
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
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_image_selection_list,
					parent, false);
			viewHolder = new ViewHolder();
			viewHolder.image01 = (KMImageView) convertView
					.findViewById(R.id.image01);
			viewHolder.image02 = (KMImageView) convertView
					.findViewById(R.id.image02);
			viewHolder.image03 = (KMImageView) convertView
					.findViewById(R.id.image03);
			viewHolder.image04 = (KMImageView) convertView
					.findViewById(R.id.image04);
			viewHolder.image01.setImageContentViewScaleType(ScaleType.CENTER_CROP);
			viewHolder.image02.setImageContentViewScaleType(ScaleType.CENTER_CROP);
			viewHolder.image03.setImageContentViewScaleType(ScaleType.CENTER_CROP);
			viewHolder.image04.setImageContentViewScaleType(ScaleType.CENTER_CROP);
			
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		List<PhotoInfo> subPhotosInRow = mPhotoRows.get(position);
		float sumoOfWidths = 0.0f;
		float sumOfHeights = 0.0f;
		for (PhotoInfo photoInfo : subPhotosInRow) {
			int width = photoInfo.getWidth();
			int height = photoInfo.getHeight();
			if(photoInfo.getOrientation()==90 || photoInfo.getOrientation()==270){
				int temp = width;
				width = height;
				height = temp;
			}
			
			
			sumoOfWidths += width;
			sumOfHeights +=height;
		}
		float scaleFactor = (KM2Application.getInstance().getScreenW() - (subPhotosInRow.size() - 1) * mGuttersSpace)/ sumoOfWidths;
		float rowHeight = (sumOfHeights / subPhotosInRow.size()) * scaleFactor;
		rowHeight = Math.min(rowHeight, KM2Application.getInstance().getScreenH() / 3);

		switch (subPhotosInRow.size()) {
		case 1:
			viewHolder.image01.setVisibility(View.VISIBLE);
			viewHolder.image02.setVisibility(View.GONE);
			viewHolder.image03.setVisibility(View.GONE);
			viewHolder.image04.setVisibility(View.GONE);
			break;
		case 2:
			viewHolder.image01.setVisibility(View.VISIBLE);
			viewHolder.image02.setVisibility(View.VISIBLE);
			viewHolder.image03.setVisibility(View.GONE);
			viewHolder.image04.setVisibility(View.GONE);
			break;
		case 3:
			viewHolder.image01.setVisibility(View.VISIBLE);
			viewHolder.image02.setVisibility(View.VISIBLE);
			viewHolder.image03.setVisibility(View.VISIBLE);
			viewHolder.image04.setVisibility(View.GONE);
			break;
		case 4:
			viewHolder.image01.setVisibility(View.VISIBLE);
			viewHolder.image02.setVisibility(View.VISIBLE);
			viewHolder.image03.setVisibility(View.VISIBLE);
			viewHolder.image04.setVisibility(View.VISIBLE);
			break;

		default:
			break;
		}
		// If there is only 1 image on the very last row, it will use max width of 1/3 of screen
		if(position == mPhotoRows.size()-1 && subPhotosInRow.size()==1
				&& !subPhotosInRow.get(0).getPhotoSizeType().isPanaroma()){
			
			PhotoInfo photoInfo = subPhotosInRow.get(0);
			float scaleFactorLast = (KM2Application.getInstance().getScreenW()/3)/sumoOfWidths ;
			float lastRowHeight = (sumOfHeights / subPhotosInRow.size()) * scaleFactorLast;
			lastRowHeight = Math.min(lastRowHeight, KM2Application.getInstance().getScreenH() / 3);				
			LinearLayout.LayoutParams iMageLayoutParams = new LayoutParams((int) (KM2Application.getInstance().getScreenW()/3), (int) lastRowHeight);
			viewHolder.image01.setLayoutParams(iMageLayoutParams);
			ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), viewHolder.image01.getvImageContentView());
			viewHolder.image01.setPhoto(photoInfo);
			viewHolder.image01.setmSelected(photoInfo.isSelected());
			viewHolder.image01.setmPhotoOperationListener(onPhotoOperationListener);
			if(photoInfo.isSelected()){
				mImageSelector.addKMImageViewToList(viewHolder.image01);
			}
			
			
		}else {
			for (int i = 0; i < subPhotosInRow.size(); i++) {
				PhotoInfo photoInfo = subPhotosInRow.get(i);
				int width = photoInfo.getWidth();
				int height = photoInfo.getHeight();
				
				if(photoInfo.getOrientation()==90 || photoInfo.getOrientation()==270){
					int temp = width;
					width = height;
					height = temp;
					
				}
				LinearLayout.LayoutParams iMageLayoutParams = new LayoutParams((int) (width * scaleFactor + 1), (int) rowHeight);
				switch (i) {
				case 0:
					viewHolder.image01.setLayoutParams(iMageLayoutParams);
					ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), viewHolder.image01.getvImageContentView());
					viewHolder.image01.setPhoto(photoInfo);
					viewHolder.image01.setmSelected(photoInfo.isSelected());
					viewHolder.image01.setmPhotoOperationListener(onPhotoOperationListener);
					if(photoInfo.isSelected()){
						mImageSelector.addKMImageViewToList(viewHolder.image01);
					}
					break;
	            case 1:
	            	iMageLayoutParams.setMargins(mGuttersSpace, 0, 0, 0);
	            	viewHolder.image02.setLayoutParams(iMageLayoutParams);
	            	ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), viewHolder.image02.getvImageContentView());
	            	viewHolder.image02.setPhoto(photoInfo);
	            	viewHolder.image02.setmSelected(photoInfo.isSelected());
					viewHolder.image02.setmPhotoOperationListener(onPhotoOperationListener);
					if(photoInfo.isSelected()){
						mImageSelector.addKMImageViewToList(viewHolder.image02);
					}
					break;
	            case 2:
	            	iMageLayoutParams.setMargins(mGuttersSpace, 0, 0, 0);
	            	viewHolder.image03.setLayoutParams(iMageLayoutParams);
	            	ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), viewHolder.image03.getvImageContentView());
	            	viewHolder.image03.setPhoto(photoInfo);
	            	viewHolder.image03.setmSelected(photoInfo.isSelected());
					viewHolder.image03.setmPhotoOperationListener(onPhotoOperationListener);
					if(photoInfo.isSelected()){
						mImageSelector.addKMImageViewToList(viewHolder.image03);
					}
	            	break;
	            case 3:
	            	iMageLayoutParams.setMargins(mGuttersSpace, 0, 0, 0);
	            	viewHolder.image04.setLayoutParams(iMageLayoutParams);
	            	ImageLoader.getInstance().displayImage("file://"+photoInfo.getPhotoPath(), viewHolder.image04.getvImageContentView());
	            	viewHolder.image04.setPhoto(photoInfo);
	            	viewHolder.image04.setmSelected(photoInfo.isSelected());
					viewHolder.image04.setmPhotoOperationListener(onPhotoOperationListener);
					if(photoInfo.isSelected()){
						mImageSelector.addKMImageViewToList(viewHolder.image04);
					}
	            	break;

				default:
					break;
				}
			}
		}
		
		return convertView;
	}

	class ViewHolder {
		KMImageView image01;
		KMImageView image02;
		KMImageView image03;
		KMImageView image04;
		
		
		
	}

}
