package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.util.AsyncImageLoader;
import com.kodak.rss.core.util.AsyncImageLoader.ImageDownloaderCallBack;
import com.kodak.rss.tablet.R;

public class PhotobookFontAdapter extends BaseAdapter {
	
	private List<Font> fonts;
	private LayoutInflater mInflater;
	private AsyncImageLoader asyncImageLoader;
	
	
	public PhotobookFontAdapter(Context context, List<Font> fonts,AsyncImageLoader asyncImageLoader){
		this.fonts = fonts;
		mInflater = LayoutInflater.from(context);
		this.asyncImageLoader = asyncImageLoader;
	}

	@Override
	public int getCount() {
		return fonts==null ? 0 : fonts.size();
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
	public View getView(int position, View convertView,final  ViewGroup parent) {
		Holder holder = null;
		if(convertView == null){
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.font_textview_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.tv_text);
			holder.imageView = (ImageView) convertView.findViewById(R.id.iv_content);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		final Font font = fonts.get(position);
		final ImageView iv = holder.imageView;
		final TextView tv = holder.textView;
		
		Bitmap bitmap = asyncImageLoader.getBitmapFromCache(font.sampleURL);
		if(bitmap != null){
			tv.setVisibility(View.INVISIBLE);
			iv.setImageBitmap(bitmap);
			iv.setVisibility(View.VISIBLE);
		}else{
			tv.setText(font.displayName);
			tv.setVisibility(View.VISIBLE);
			iv.setVisibility(View.INVISIBLE);
			iv.setTag(font.sampleURL);
			
			asyncImageLoader.loadImageAsync(holder.imageView, font.sampleURL, new ImageDownloaderCallBack() {
				
				@Override
				public void OnImageDownloaded(View view, Bitmap bitmap) {
					if(font.sampleURL.equals(view.getTag())){
						ImageView imageView = (ImageView) view;
						imageView.setImageBitmap(bitmap);
						imageView.setVisibility(View.VISIBLE);
						tv.setVisibility(View.INVISIBLE);
						notifyDataSetChanged();
					}
				}
			});
		}
		
		
		
		return convertView;
	}
	
	class Holder {
		TextView textView;
		ImageView imageView;
	}
	
}
