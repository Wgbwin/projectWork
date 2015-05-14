package com.kodak.rss.tablet.view.dialog;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.calendar.CalendarTheme;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.HorizontalListView;

public class DialogShowPic implements OnClickListener, OnKeyListener, onProcessImageResponseListener{

	private View dialogView;
	private Dialog dialog;
	private HorizontalListView mListView;
	private RelativeLayout dispalyImageL;
	private ImageView displayImage;
	private Context mContext;
	private onDialogListener listener;
	
	private int height,width,imageHeight,imageWidth,maxImageHeight;	
	private int lastSelectedPosition = -1;
	private int size;
	
	public ImageUseURIDownloader imageDownloader;		
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();	
	private LruCache<String, Bitmap> mMemoryCache; 
	
	public interface onDialogListener{
		void onDone();		
	}
	public Bitmap waitBitmap;	
	
	public SortableHashMap<Integer, String> mObjectMap;	
	public ArrayList<ImageInfo> mObjectList;
	public BackGround[] BackGrounds;	
	public CalendarTheme.BackGround[] cThemeBackGrounds;
	public ImageAdapter imageAdapter;

	public void createDialog(Context context, String promptContent, onDialogListener listener) {
		this.mContext = context;		
		this.listener = listener;
		LayoutInflater inflater = LayoutInflater.from(context);
		DisplayMetrics dm = context.getResources().getDisplayMetrics();		
		height = (int) (dm.heightPixels - dm.density*100);
		width  = (int) (dm.widthPixels - dm.density*100);
		if (dm.heightPixels < dm.widthPixels && (dm.heightPixels/dm.widthPixels) < 0.7) {
			width  = (int) (dm.widthPixels - dm.density*180);
		}		
		dialogView = inflater.inflate(R.layout.dialog_show_pic, null);		
		dialogView.findViewById(R.id.done_button).setOnClickListener(this);		
		
		dispalyImageL = (RelativeLayout) dialogView.findViewById(R.id.dispalyImageL);
		displayImage = (ImageView) dialogView.findViewById(R.id.dispalyImage);		
		
		mListView = (HorizontalListView) dialogView.findViewById(R.id.gallery);
		if (size < 2) {	
			mListView.setVisibility(View.GONE);
			LayoutParams lp = (LayoutParams) dispalyImageL.getLayoutParams(); 
			lp.weight = 19;
			dispalyImageL.setLayoutParams(lp);			
		}else {	
			imageAdapter = new ImageAdapter();
			mListView.setAdapter(imageAdapter);		
							
			mListView.setMaxCount(size);			
		}

		imageHeight = (int)((height-dm.density*85)*0.84);
		imageWidth = (int)(width - dm.density*50);	
		maxImageHeight = (int) (height - dm.density*70);		
		
		lastSelectedPosition = -1;
		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) {				
				displayPhoto(arg2,displayImage);				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {				
				displayPhoto(0,displayImage);		
			}
		});		

		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnKeyListener(this);
		dialog.show();
		
		dialog.getWindow().setContentView(dialogView);
		TextView content = (TextView)dialogView.findViewById(R.id.titleContent);
		content.setText(promptContent);
		
		ViewGroup.LayoutParams dialogLp = dialogView.getLayoutParams();
		dialogLp.height = height;
		dialogLp.width = width;
		dialogView.setLayoutParams(dialogLp);	
		
		this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);			
		this.imageDownloader.setIsThumbnail(false);

		int height = imageHeight;
		if (size < 2) {
			height = maxImageHeight;
		}				
		this.imageDownloader.setViewParameters(imageWidth, height);	
		
		displayPhoto(0,displayImage);		
	}
	
	public boolean isShowing(){
		return dialog != null && dialog.isShowing();
	}

	public void setBackGrounds(Context context,BackGround[] objects,LruCache<String, Bitmap> mMemoryCache){
		this.BackGrounds = objects;		
		size = this.BackGrounds.length;
		this.mMemoryCache = mMemoryCache;
	}
	
	public void setBackGrounds(Context context,CalendarTheme.BackGround[] objects,LruCache<String, Bitmap> mMemoryCache){
		this.cThemeBackGrounds = objects;		
		size = this.cThemeBackGrounds.length;
		this.mMemoryCache = mMemoryCache;
	}

	public void setObjectMap(SortableHashMap<Integer, String> objectMap,LruCache<String, Bitmap> mMemoryCache){
		mObjectMap = objectMap;
		size = mObjectMap.size();
		this.mMemoryCache = mMemoryCache;
	}
	
	public void setObjectList(Context context,ArrayList<ImageInfo> photobookschosenpics,LruCache<String, Bitmap> mMemoryCache){
		mObjectList = photobookschosenpics;
		size = mObjectList.size();
		this.mMemoryCache = mMemoryCache;		
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.done_button){
			if (dialog != null) {				
				dialog.dismiss();
				cancelRequest();
				System.gc();
				if(listener!=null){
					listener.onDone();
				}
			}
		}	
	}
	
	public void cancelRequest(){
		if (imageAdapter != null) {
			imageAdapter.cancelRequest();
		}		
		if (pendingRequests == null) return;
		if (imageDownloader == null) return;
		if (BackGrounds != null) {
			for (int i = 0; i < BackGrounds.length; i++) {
				BackGround backGround = BackGrounds[i];	
				if (backGround == null) continue;
				cancelRequest(backGround.id);						
			}
		}		
		if (cThemeBackGrounds != null) {
			for (int i = 0; i < cThemeBackGrounds.length; i++) {
				CalendarTheme.BackGround backGround = cThemeBackGrounds[i];	
				if (backGround == null) continue;
				cancelRequest(backGround.id);						
			}
		}		
		if (mObjectMap != null) {
			for (int i = 0; i < mObjectMap.size(); i++) {
				String imageId = String.valueOf(mObjectMap.keyAt(i));
				cancelRequest(imageId);				
			}
		}		
		if (mObjectList != null) {
			for (int i = 0; i < mObjectList.size(); i++) {
				ImageInfo info = mObjectList.get(i);
				if (info == null) continue;
				cancelRequest(info.id);				
			}
		}	
	}
	
	private void cancelRequest(String imageId){		
		if (imageId == null) return;				
		imageDownloader.cancelRequest(imageId, null, 0);		
	}

	private Bitmap getBitmapFromCache(String key,LruCache<String, Bitmap> memoryCache){
		Bitmap bitmap = null;
		if (key == null) return bitmap;
		bitmap = MemoryCacheUtil.getBitmap(memoryCache, key);			
		return bitmap;
	}

	private void displayPhoto(int position,ImageView imageView){
		Bitmap bitmap = null;
		if (BackGrounds != null) {
			if (lastSelectedPosition == position) return;				
			lastSelectedPosition = position;
			BackGround backGround = BackGrounds[position];	
			String groundId = backGround.id;
			String key = "orig_" + groundId;
			bitmap = getBitmapFromCache(key,mMemoryCache);
			String thumbnailPath =  FilePathConstant.getLoadFilePath(FilePathConstant.bookType, groundId, true);
			imageView.setImageBitmap((bitmap == null ? getWaitBitmap(false,thumbnailPath) : bitmap)); 								
			if (bitmap == null ) {
				URI pictureURI = null ;					    		   	    		
				try {
					pictureURI = new URI(backGround.imageURL);
				} catch (URISyntaxException e) {
				    pictureURI = null;
				}
				if (pictureURI != null) {														
					this.imageDownloader.setSaveType(FilePathConstant.bookType);																	
					imageView.setTag(groundId);	
					imageDownloader.downloadProfilePicture(groundId, pictureURI,imageView,position,true); 					
				}															
			}				
			return;
		}	
		
		if (cThemeBackGrounds != null) {
			if (lastSelectedPosition == position) return;				
			lastSelectedPosition = position;
			CalendarTheme.BackGround backGround = cThemeBackGrounds[position];	
			String groundId = backGround.id;
			String key = "orig_" + groundId;
			bitmap = getBitmapFromCache(key,mMemoryCache);
			String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.calendarType, groundId, true);
			imageView.setImageBitmap((bitmap == null ? getWaitBitmap(false,thumbnailPath) : bitmap)); 								
			if (bitmap == null ) {
				URI pictureURI = null ;					    		   	    		
				try {
					pictureURI = new URI(backGround.imageURL);
				} catch (URISyntaxException e) {
				    pictureURI = null;
				}
				if (pictureURI != null) {														
					this.imageDownloader.setSaveType(FilePathConstant.calendarType);																
					imageView.setTag(groundId);	
					imageDownloader.downloadProfilePicture(groundId, pictureURI,imageView,position,true); 					
				}														
			}				
			return;
		}	
				
		if (mObjectMap != null) {
			if (lastSelectedPosition == position) return;				
			lastSelectedPosition = position;
			String imageId = String.valueOf(mObjectMap.keyAt(position));
			String filePath = mObjectMap.valueAt(position);
			String key = "orig_"+ imageId;
			bitmap = getBitmapFromCache(key,mMemoryCache);				
			if (bitmap == null) {								
				bitmap = origBitmap(imageId, position, filePath, imageView);	
			}
			imageView.setImageBitmap((bitmap == null ? getWaitBitmap(false,filePath) : bitmap)); 
			return;
		}
			
		if (mObjectList != null) {
			if(lastSelectedPosition == position) return;			
			lastSelectedPosition = position;	
			ImageInfo info = mObjectList.get(position);
			if (info == null) return;
			String imageId  = info.id;
			String key = "orig_" + imageId;				
			bitmap = getBitmapFromCache(key,mMemoryCache);				
			if (bitmap == null) {
				String filePath = info.originalUrl;
				if (filePath == null && !info.isfromNative) {	
					String originalPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageId, false);
					if (originalPath != null) {
						info.editUrl = originalPath;	
						info.originalUrl = originalPath;
						info.uploadOriginalUrl = originalPath;
						filePath = info.originalUrl;						
					}															
				}
				if (filePath != null) {
					bitmap = origBitmap(imageId, position, filePath, imageView);		
				}else if (!info.isfromNative) {
					URI pictureURI = null;					    		   	    		
					try {
						pictureURI = new URI(info.downloadOriginalUrl);
					} catch (URISyntaxException e) {
						pictureURI = null;
					}																	
					if (pictureURI != null) {													
						this.imageDownloader.setSaveType(FilePathConstant.externalType);																							
						imageView.setTag(imageId);
						imageDownloader.downloadProfilePicture(imageId, pictureURI,imageView,position,true); 
					}
				}				
			}	
			String thumbnailPath = null;
			if (info.isfromNative) {
				thumbnailPath  = info.thumbnailUrl;	
			}else {
				thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageId, true);				
			}				
			imageView.setImageBitmap((bitmap == null ? getWaitBitmap(false,thumbnailPath) : bitmap)); 	
			return;	
		}		
	}
	
	private Bitmap origBitmap(String imageId,int position,String filePath,ImageView imageView){
		Bitmap origBitmap = null;
		if (filePath == null) return origBitmap;
		
		BitmapFactory.Options options = new Options();
		options.inJustDecodeBounds = true;				
		BitmapFactory.decodeFile(filePath, options);
		int origW = options.outWidth;
		int origH = options.outHeight;
		
		int height = imageHeight;
		if (size < 2) {
			height = maxImageHeight;
		}				
		if (origW <= imageWidth*1.5 && origH <= maxImageHeight*1.5) {
			String key = "orig_" + imageId;
			origBitmap = getOrigBitmap(filePath, key, options, height, mMemoryCache);
			return origBitmap;
		}

		imageView.setTag(imageId);						
		imageDownloader.downloadProfilePicture(imageId,filePath,imageView, position);		
		return origBitmap;
	}
		
	private Bitmap getOrigBitmap(String filePath,String key,BitmapFactory.Options options,int height,LruCache<String, Bitmap> memoryCache){
		Bitmap origBitmap = null;
		if (options == null) {
			options = new Options();
			options.inJustDecodeBounds = true;				
			BitmapFactory.decodeFile(filePath, options);
		}		
		int origW = options.outWidth;
		int origH = options.outHeight;
			
		int  downsample = 1;				
		if (origW > origH) {
			if(origH > maxImageHeight){				
				downsample = (int) Math.ceil((origH * 1.0)/ height);						
			}		
		} else {
			if(origW > imageWidth){								
				downsample = (int) Math.ceil((origW * 1.0)/ imageWidth);										
			}				
		}		
		options.inDither = false;							
		options.inPreferredConfig = Bitmap.Config.RGB_565;   
		options.inPurgeable = true;
		options.inInputShareable = true;							
		options.inJustDecodeBounds = false;
		options.inSampleSize = downsample;
		origBitmap = BitmapFactory.decodeFile(filePath, options);		
		
		int rotate = ImageUtil.getDegreesExifOrientation(filePath);  
		if(rotate > 0 && origBitmap != null) {   
			Bitmap rotateBitmap = ImageUtil.rotateBitmap(origBitmap,rotate);   
            if(rotateBitmap != null) {   
            	origBitmap.recycle();   
            	origBitmap = rotateBitmap;   
            }             
		}
		MemoryCacheUtil.putBitmap(memoryCache, key, origBitmap);				
		return origBitmap;
	}
		
	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {			
		if (response == null || imageDownloader == null) return;
		if (profileId == null) return;
		ImageInfo imageInfo = null;		
		if (mObjectList != null ) {
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();	
			if (currentPhotoBook == null) return;	
			if (currentPhotoBook.chosenpics == null) return;				
			for (ImageInfo info : currentPhotoBook.chosenpics) {			
				if (info != null && !info.isfromNative && profileId.equals(info.id)) {
					imageInfo =	info;
					break;																													
				}
			}		
		}		
		
		if (imageInfo != null && imageInfo.uploadOriginalUrl == null) {		
			String originalPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, profileId, false);
			imageInfo.originalUrl= originalPath;
			imageInfo.editUrl = originalPath;	
			imageInfo.uploadOriginalUrl = originalPath;			
		}		
		
		Bitmap bitmap = response.getBitmap();
		String key = "orig_" + profileId;	
		MemoryCacheUtil.putBitmap(mMemoryCache, key, bitmap);				
		if (bitmap != null && view != null) {										
			if (view instanceof ImageView ){
				if (view.getTag().toString().equals(profileId)) {
					if (view.getVisibility() == View.VISIBLE) {
						((ImageView)view).setImageBitmap(bitmap);	
					}		
				}																															
			}																				 									
		}		
	}

	public class ImageAdapter extends BaseAdapter implements onProcessImageResponseListener{		
		private LayoutInflater mInflater;
		private ImageUseURIDownloader imageThumDownloader;
		private final Map<String, Request> pendingThumRequests = new HashMap<String, Request>();	
		LayoutParams mLayoutParams;			
		
		public ImageAdapter() {			
			mInflater = LayoutInflater.from(mContext);					
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();	
			int mItemHeight = (int)((height-dm.density*70)*0.15);
			int mItemWidth = (int)((width - dm.density*50)*0.2);	
			this.mLayoutParams = new LayoutParams(mItemWidth, mItemHeight);
			this.mLayoutParams.rightMargin = (int) (dm.density*5);
			this.mLayoutParams.leftMargin = (int) (dm.density*5);
			
			this.imageThumDownloader = new ImageUseURIDownloader(mContext,pendingThumRequests,this);			
			this.imageThumDownloader.setIsThumbnail(true);	
			this.imageThumDownloader.setViewParameters(mItemWidth, mItemHeight);
		}

		public int getCount() {
			if (mObjectList != null) {
				size = mObjectList.size();
			}
			return size;
		}

		public void cancelRequest(){		
			if (pendingThumRequests == null) return;
			if (imageThumDownloader == null) return;
			if (BackGrounds != null) {
				for (int i = 0; i < BackGrounds.length; i++) {
					BackGround backGround = BackGrounds[i];	
					if (backGround == null) continue;
					cancelRequest(backGround.id);						
				}
			}		
			if (cThemeBackGrounds != null) {
				for (int i = 0; i < cThemeBackGrounds.length; i++) {
					CalendarTheme.BackGround backGround = cThemeBackGrounds[i];	
					if (backGround == null) continue;
					cancelRequest(backGround.id);						
				}
			}		
			if (mObjectMap != null) {
				for (int i = 0; i < mObjectMap.size(); i++) {
					String imageId = String.valueOf(mObjectMap.keyAt(i));
					cancelRequest(imageId);				
				}
			}		
			if (mObjectList != null) {
				for (int i = 0; i < mObjectList.size(); i++) {
					ImageInfo info = mObjectList.get(i);
					if (info == null) continue;
					cancelRequest(info.id);				
				}
			}	
		}
		
		private void cancelRequest(String imageId){		
			if (imageId == null) return;				
			imageThumDownloader.cancelRequest(imageId, null, 0);		
		}
	
		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {			
			ImageView mImageView = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.horizontalist_image_item, null);			
				mImageView = (ImageView) convertView.findViewById(R.id.photo);
				convertView.setTag(mImageView);
			} else {
				mImageView = (ImageView) convertView.getTag();
			}			
			displayThumbailPhoto(position, mImageView);	
			mImageView.setLayoutParams(mLayoutParams); 
			return convertView;
		}

		@Override
		public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {			
			if (response == null || imageThumDownloader == null) return;	
			if (profileId == null) return;	
			ImageInfo imageInfo = null;	
			if (mObjectList != null) {
				Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				if (currentPhotoBook == null) return;	
				if (currentPhotoBook.chosenpics == null) return;
				for (ImageInfo info : currentPhotoBook.chosenpics) {
					if (info != null && !info.isfromNative && profileId.equals(info.id)) {
						imageInfo =	info;
						break;																													
					}
				}		
			}
			if (imageInfo != null && imageInfo.uploadThumbnailUrl == null) {
				String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, profileId, true);
				imageInfo.thumbnailUrl = thumbnailPath;						
				imageInfo.uploadThumbnailUrl = thumbnailPath;
			}

			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);						
			if (bitmap != null && view != null) {										
				if (view instanceof ImageView ){
					if (view.getTag().toString().equals(profileId)) {
						if (view.getVisibility() == View.VISIBLE) {
							((ImageView)view).setImageBitmap(bitmap);	
						}		
					}																															
				}																				 
			}															
		}	
				
		private Bitmap getBitmapFromCache(LruCache<String, Bitmap> memoryCache,String imageId){
			Bitmap bitmap = null;
			if (imageId == null) return bitmap;
			bitmap = MemoryCacheUtil.getBitmap(memoryCache, imageId);					
			return bitmap;
		}

		private void displayThumbailPhoto(int position,ImageView imageView){
			Bitmap bitmap = null;
			if (BackGrounds != null) {
				BackGround backGround = BackGrounds[position];	
				String groundId = backGround.id;
				bitmap = getBitmapFromCache(mMemoryCache,groundId);
				imageView.setImageBitmap((bitmap == null ? getWaitBitmap(true,null) : bitmap)); 								
				if (bitmap == null ) {
					URI pictureURI = null ;					    		   	    		
					try {
						pictureURI = new URI(backGround.glyphURL);
					} catch (URISyntaxException e) {
					    pictureURI = null;
					}
					if (pictureURI != null) {						
						imageThumDownloader.setSaveType(FilePathConstant.bookType);
						imageView.setTag(groundId);	
						imageThumDownloader.downloadProfilePicture(groundId, pictureURI, imageView,position,true,true,null);   
					}															
				}				
				return;
			}	
			
			if (cThemeBackGrounds != null) {
				CalendarTheme.BackGround backGround = cThemeBackGrounds[position];	
				String groundId = backGround.id;
				bitmap = getBitmapFromCache(mMemoryCache,groundId);
				imageView.setImageBitmap((bitmap == null ? getWaitBitmap(true,null) : bitmap)); 								
				if (bitmap == null ) {
					URI pictureURI = null ;					    		   	    		
					try {
						pictureURI = new URI(backGround.glyphURL);
					} catch (URISyntaxException e) {
					    pictureURI = null;
					}
					if (pictureURI != null) {						
						imageThumDownloader.setSaveType(FilePathConstant.calendarType);					
						imageView.setTag(groundId);	
						imageThumDownloader.downloadProfilePicture(groundId, pictureURI, imageView,position,true,true,null);   
					}															
				}				
				return;
			}					

			if (mObjectMap != null) {
				int imageId = mObjectMap.keyAt(position);
				String key = String.valueOf(imageId);
				bitmap = getBitmapFromCache(mMemoryCache,key);
				imageView.setImageBitmap((bitmap == null ? getWaitBitmap(true,null) : bitmap)); 										
				if(bitmap == null) {								
					imageView.setTag(key);	
					imageThumDownloader.downloadProfilePicture(key, null, imageView,position);  
				}							
				return;
			}	
				
			if (mObjectList != null) {
				ImageInfo info = mObjectList.get(position);
				if (info == null) return;
				String imageId = info.id;
				bitmap = getBitmapFromCache(mMemoryCache,imageId);
				imageView.setImageBitmap((bitmap == null ? getWaitBitmap(true,null) : bitmap)); 	
				if (bitmap == null) {
					imageView.setTag(imageId);	
					if (info.isfromNative) {																		
						imageThumDownloader.downloadProfilePicture(imageId, null, imageView, position);								
					}else {	
						URI pictureURI = null;
						try {
							pictureURI = new URI(info.downloadThumbnailUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}									
						if (pictureURI != null) {
							imageThumDownloader.setSaveType(FilePathConstant.externalType);								
							imageThumDownloader.downloadProfilePicture(imageId, pictureURI, imageView, position,true);
						}
					}
				}												
				return;
			}										
		}				
	}
	
	private Bitmap getWaitBitmap(boolean isThum ,String filePath){
		Bitmap wBitmap =null;
		if (waitBitmap == null || waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}		
		if (isThum) {
			int imageHeightL = (int) (imageHeight*3f/17);
			wBitmap = Bitmap.createScaledBitmap(waitBitmap,imageHeightL,imageHeightL, true);
		}else {
			wBitmap = getDefaultBitmap(waitBitmap, filePath, size, imageHeight, maxImageHeight, imageWidth);
		}
		if (wBitmap == null) {
			wBitmap = waitBitmap;
		}
		return wBitmap;
	}
	
	private Bitmap getDefaultBitmap(Bitmap waitBitmap, String thumbnailPath,int size,int imageHeight,int maxImageHeight,int imageWidth){
		Bitmap mBitmap = null;			
		int imageHeightL = 0;
		int imageWidthL = 0;
		int origW = waitBitmap.getWidth();
		int origH = waitBitmap.getHeight();
		if (thumbnailPath != null) {
			BitmapFactory.Options options = new Options();
			options.inJustDecodeBounds = true;				
			BitmapFactory.decodeFile(thumbnailPath, options);
			origW = options.outWidth;
			origH = options.outHeight;
		}											
		if (origW > 1 && origH >1 ) {												
			if (size < 2) {
				imageHeightL = maxImageHeight;
				imageWidthL = maxImageHeight*origW/origH;
				imageWidthL = imageWidthL > imageWidth ? imageWidth : imageWidthL;
			}else {
				imageHeightL = imageHeight;
				imageWidthL = imageHeight*origW/origH;		
				imageWidthL = imageWidthL > imageWidth ? imageWidth : imageWidthL;
			}																																					
		}													
		if (imageWidthL > 0 && imageHeightL > 0) {											
			mBitmap = Bitmap.createScaledBitmap(waitBitmap,imageWidthL,imageHeightL, true);						 	 				
		}else {
			mBitmap = Bitmap.createScaledBitmap(waitBitmap,imageHeight,imageHeight, true);
		}
		return mBitmap;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {			
			return true;
		}		
		return false;		
	}	
		
}
