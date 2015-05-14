package com.kodak.rss.tablet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.tablet.R;

public class FacebookPhotosAdapter extends CanZoomBaseAdapter{
	
	private LayoutInflater mInflater;	
	Bitmap photosOfYou;	
	Bitmap yoursPhotos;	
	Bitmap yoursFriends;	
	Bitmap yoursGroups;	
	
	public FacebookPhotosAdapter(Context context,LruCache<String, Bitmap> mMemoryCache) {
		super(context,mMemoryCache);			
		mInflater = LayoutInflater.from(context);
		photosOfYou = BitmapFactory.decodeResource(context.getResources(),R.drawable.photosofyou_up);	
		yoursPhotos = BitmapFactory.decodeResource(context.getResources(),R.drawable.yourphotos_up);		
		yoursFriends = BitmapFactory.decodeResource(context.getResources(),R.drawable.yourfriends_up);		
		yoursGroups = BitmapFactory.decodeResource(context.getResources(),R.drawable.yourgroups_up);		
	}

	@Override
	public int getCount() {
		return 4;
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
		FacebookPhotosViewHolder holder;		
		if (convertView == null) {			
			convertView = mInflater.inflate(R.layout.grid_facebook_photos_item, null);
			holder = new FacebookPhotosViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.fbk_photoContent);
			holder.photoName = (TextView) convertView.findViewById(R.id.photoName);
			convertView.setTag(holder);
		}else {
			holder = (FacebookPhotosViewHolder) convertView.getTag();
		}
				   
		if (position == 0) {
			holder.imageView.setImageBitmap(photosOfYou); 
			String fbkPhotosOfYou = mContext.getString(R.string.photos_of)+" "+mContext.getString(R.string.you);
			holder.photoName.setText(fbkPhotosOfYou);
		}else if (position == 1) {
			holder.imageView.setImageBitmap(yoursPhotos); 
			holder.photoName.setText(R.string.your_photos);
		}else if (position == 2) {
			holder.imageView.setImageBitmap(yoursFriends); 
			holder.photoName.setText(R.string.your_friends);
		}else if (position == 3) {
			holder.imageView.setImageBitmap(yoursGroups); 
			holder.photoName.setText(R.string.your_groups);
		}			            	        
		holder.imageView.setLayoutParams(mLayoutParams);      
		return convertView;
      } 
		
 }

class FacebookPhotosViewHolder {
	TextView photoName;	
	ImageView imageView;
}
	
