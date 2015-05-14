package com.kodakalaris.video.adapters;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.AppConstants.LoadImageType;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodakalaris.video.fragments.ICommunicatingForTMS;
import com.kodakalaris.video.views.ViewPagerImageItemView;

public class GridPreviewAdapter extends PagerAdapter{
	private List<PhotoInfo> photos ;
	private Context context ;
	private ImageFetcher mImageFetcher ;
	private ICommunicatingForTMS mListener ;
	public GridPreviewAdapter(Context context , List<PhotoInfo>  photos , ImageFetcher mImageFetcher ,ICommunicatingForTMS mListener  ){
        this.context = context ;
        this.photos = photos ;
        this.mImageFetcher = mImageFetcher ;
        this.mListener = mListener ;
	}
	

	@Override
	public int getCount() {
		if(photos!=null){
			return photos.size() ;
		}
		return 0;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		PhotoInfo photo = photos.get(position) ;
		final View v = layoutInflater.inflate(R.layout.tms_preview_photo_viewpager_item, container,false);
		ViewPagerImageItemView image = (ViewPagerImageItemView) v.findViewById(R.id.preview_image) ;
		image.setICommunicatingForTMS(mListener) ;
		mImageFetcher.loadImage(photo.getPhotoPath(), photo.getPhotoPath(), image, LoadImageType.FILE_PATH) ;
		container.addView(v);
		
		return v ;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView((View)object);
	}
	
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE ;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0==arg1;
	}

}
