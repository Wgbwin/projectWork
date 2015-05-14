package com.kodakalaris.kodakmomentslib.adapter.mobile;


import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ImageView.ScaleType;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.util.ImageUtil;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;

public class SimpleCarouselAdapter extends PagerAdapter{
	private Context mContext;
	private int[] mImgIds;
	private String[] mImgPaths;
	private KMConfig mConfig;
	private ScaleType mScaleType = ScaleType.CENTER_CROP;
	private boolean mLoadingProgressEnabled = true;
	private int mContainerW, mContainerH;
	private OnPageClickListener mOnPageClickListener;
	private WeakMemoryCache mCache;
	
	public SimpleCarouselAdapter(Context context, int[] imgIds) {
		mContext = context;
		mImgIds = imgIds;
		init();
	}
	
	public SimpleCarouselAdapter(Context context, String[] imgPaths) {
		mContext = context;
		mImgPaths = imgPaths;
		init();
	}
	
	public SimpleCarouselAdapter(Context context, KMConfig config) {
		mContext = context;
		mConfig = config;
		init();
	}
	
	private void init() {
		mCache = new WeakMemoryCache();
	}
	
	@Override
	public int getCount() {
		if (mImgIds != null) {
			return mImgIds.length;
		} else if (mImgPaths != null) {
			return mImgPaths.length;
		} else if (mConfig != null) {
			return mConfig.configData.entries.size();
		} else {
			return 0;
		}
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		View v = LayoutInflater.from(mContext).inflate(R.layout.carousel_simple, null);
		ImageView imageView = (ImageView) v.findViewById(R.id.img_carousel);
		imageView.setScaleType(mScaleType);
		ProgressBar pbLoading = (ProgressBar) v.findViewById(R.id.pb_load);
		container.addView(v);
		
		Bitmap bm = null;
		if (mImgIds != null) {
			bm = loadBitmap(mImgIds[position]);
		} else if (mImgPaths != null) {
			bm = loadBitmap(mImgPaths[position]);
		} else if (mConfig != null) {
			synchronized (KMConfigManager.getInstance()) {
				KMConfigEntry entry = mConfig.configData.entries.get(position);
				String path = KMConfigManager.getInstance().getConfigImageFilePath(entry);
				bm = loadBitmap(path);
			}
		}
		
		pbLoading.setVisibility(mLoadingProgressEnabled && bm == null ? View.VISIBLE : View.INVISIBLE);
		if (bm != null) {
			imageView.setImageBitmap(bm);
		}
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnPageClickListener != null) {
					mOnPageClickListener.OnPageClicked(position);
				}
			}
		});
		
		return v;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
	}
	
	private Bitmap loadBitmap(String path) {
		Bitmap bm = mCache.get(path);
		
		if (bm == null) {
			if (!new File(path).exists()) {
				return null;
			}
			
			int maxW = mContainerW == 0 ? KM2Application.getInstance().getScreenW() : mContainerW;
			int maxH = mContainerH == 0 ? KM2Application.getInstance().getScreenH() : mContainerH;
			
			Options opts = new Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			
			//TODO : final value * 2 to avoid oom, not a good idea, try optimize it
			opts.inSampleSize = calculateInSampleSize(opts.outWidth, opts.outHeight, maxW, maxH) * 2;
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inJustDecodeBounds = false;
			
			bm = ImageUtil.decodeImageIgnorOom(path, opts);
			
			if (bm != null) {
				mCache.put(path, bm);
			}
		}
		
		return bm;
	}
	
	private Bitmap loadBitmap(int resId) {
		String key = String.valueOf(resId);
		Bitmap bm = mCache.get(key);
		
		if (bm == null) {
			bm =BitmapFactory.decodeResource(mContext.getResources(), resId);
			if (bm != null) {
				mCache.put(key, bm);
			}
		}
		
		return bm;
	}
	
	public void notifyImageChanged(KMConfigEntry entry) {
		String path = KMConfigManager.getInstance().getConfigImageFilePath(entry);
		mCache.remove(path);
	}
	
	public void clearMemoryCache() {
		mCache.clear();
	}
	
	public void setImageScaleType(ScaleType scaleType) {
		mScaleType = scaleType;
	}
	
	public void setContainerSize(int w, int h) {
		mContainerW = w;
		mContainerH = h;
	}
	
	public void setLoadingProgressEnabled(boolean enabled) {
		mLoadingProgressEnabled = enabled;
	}
	
	public void setOnPageClickListener(OnPageClickListener onPageClickListener) {
		mOnPageClickListener = onPageClickListener;
	}
	
	private int calculateInSampleSize(int imgWidth, int imgHeight, int reqWidth, int reqHeight) {
		if (imgWidth < reqWidth || imgHeight < reqHeight) {
			return 1;
		}
		
		int inSampleSize = 1;
		int stretchW = (int) Math.floor((float) imgWidth / (float) reqWidth);
		int stretchH = (int) Math.floor((float) imgHeight / (float) reqHeight);
		
		inSampleSize = stretchW < stretchH ? stretchW : stretchH;
		
		return inSampleSize;
	}
	
	public static interface OnPageClickListener {
		void OnPageClicked(int position);
	}
	
}
