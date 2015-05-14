package com.kodak.kodak_kioskconnect_n2r.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.AppConstants.LoadImageType;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;

public class CollageFontAdapter extends BaseAdapter {

	private List<Font> fonts;
	private LayoutInflater mInflater;
	private ImageFetcher mImageFetcher;
	private Context mContext;
	private static final String IMAGE_CACHE_DIR = "collageFontImages";

	public CollageFontAdapter(Context context, List<Font> fonts) {
		this.mContext = context;
		this.fonts = fonts;
		mInflater = LayoutInflater.from(mContext);
		int mImageThumbSize = mContext.getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory	
		mImageFetcher = new ImageFetcher(mContext, mImageThumbSize);
		mImageFetcher.addImageCache(((FragmentActivity) mContext).getSupportFragmentManager(), cacheParams);
	}

	@Override
	public int getCount() {
		return fonts == null ? 0 : fonts.size();
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
	public View getView(int position, View convertView, final ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			holder = new Holder();
			convertView = mInflater.inflate(R.layout.font_textview_item, null);
			holder.textView = (TextView) convertView.findViewById(R.id.tv_text);
			holder.imageView = (ImageView) convertView.findViewById(R.id.iv_content);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		Font font = fonts.get(position);
		ImageView iv = holder.imageView;
		TextView tv = holder.textView;

		tv.setText(font.displayName);
		mImageFetcher.setFontText(tv);
		mImageFetcher.loadImage(font.sampleURL, font.sampleURL, iv, LoadImageType.WEB_FONT_IMAGE);
		return convertView;
	}

	class Holder {
		TextView textView;
		ImageView imageView;
	}

}
