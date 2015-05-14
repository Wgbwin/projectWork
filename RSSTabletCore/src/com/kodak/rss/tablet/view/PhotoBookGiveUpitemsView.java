package com.kodak.rss.tablet.view;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.PhotoLocationPo;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

/**
 * Purpose: 
 * Author: Bing Wang
 */
public class PhotoBookGiveUpitemsView extends LinearLayout{   
	private TextView giveUpPromptView;
	private TextView giveUpActionPromptView;
	private LinearLayout giveUpContent;
	public  HorizontalListView lView;	
	private RelativeLayout lGiveUpRLayout;
	private Context mContext;	
	private DisplayMetrics dm;	
	private LinearLayout.LayoutParams tParams;
	private LinearLayout.LayoutParams rParams;	
	public  List<Layer> giveLayerList;
	private LinearLayout.LayoutParams imagelayoutParams;	
	private Bitmap waitBitmap;
	private Photobook currentPhotoBook;
	private LruCache<String, Bitmap> mMemoryCache;
	public ImageAdapter imageAdapter;	
	public PhotoLocationPo selectedPo;
	public int imageWidth;
	public int itemWidth;
	
	public PhotoBookGiveUpitemsView(Context context) {
		super(context);						
		initView(context);				
	}	
	
	public PhotoBookGiveUpitemsView(Context context, AttributeSet attrs) {
		super(context,attrs);
		initView(context);
	}
	
	public PhotoBookGiveUpitemsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}	

	private void initView(Context context){
		this.mContext = context;		
		this.dm = context.getResources().getDisplayMetrics();
		inflate(context,R.layout.give_up_items_book, this);				
		giveUpPromptView = (TextView) findViewById(R.id.give_up_prompt);		
		lGiveUpRLayout = (RelativeLayout) findViewById(R.id.l_give_up);
		giveUpActionPromptView = (TextView) findViewById(R.id.give_up_action_prompt);
		giveUpContent = (LinearLayout) findViewById(R.id.l_give_up_content);
		lView = (HorizontalListView) findViewById(R.id.lView);		
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		imageAdapter = new ImageAdapter(mContext);				
		lView.setAdapter(imageAdapter);				
	}	

	public void setParams(int imageWidth ,int itemHeight,LruCache<String, Bitmap>cache){
		if (tParams != null && rParams != null) return;
		if (mMemoryCache == null) {
			mMemoryCache = cache;
		}
		this.itemWidth = dm.widthPixels/4;
		tParams = new LayoutParams(itemWidth, itemHeight);
		rParams = new LayoutParams(itemWidth*3, itemHeight);
		lGiveUpRLayout.setLayoutParams(rParams);
		giveUpPromptView.setLayoutParams(tParams);
		this.imageWidth = imageWidth;		
		imagelayoutParams = new LinearLayout.LayoutParams(imageWidth,itemHeight);	
		imagelayoutParams.setMargins((int)dm.density*10,(int)dm.density*5, 0, (int)dm.density*5);		
	}
	
	public void addLayer(Layer layer){
		if (layer == null)return;
		if (layer.contentId == null)return;
		if ("".equals(layer.contentId))return;
		if (giveLayerList == null) {
			giveLayerList = new ArrayList<Layer>(4);
		}
		giveLayerList.add(layer);		
		if (giveLayerList.size() > 0) {
			giveUpActionPromptView.setVisibility(View.GONE);
			giveUpContent.setVisibility(View.VISIBLE);
		}			
	}
	
	public void removeAll(){
		if (giveLayerList == null) return;			
		giveLayerList.clear();	
		giveLayerList = null;
		giveUpActionPromptView.setVisibility(View.VISIBLE);
		giveUpContent.setVisibility(View.GONE);	
	}
	
	public void removeLayer(String layerId){
		if (giveLayerList == null)  return;			
		if (layerId == null)return;	
		if ("".equals(layerId))return;
		Layer delLayer = null;
		for (Layer layer : giveLayerList) {
			if (layer != null && layer.contentId.equals(layerId)) {
				delLayer = layer;				
				break;
			} 
		}
		if (delLayer != null) {
			giveLayerList.remove(delLayer);	
		}
		
		if (giveLayerList.size() == 0) {
			giveUpActionPromptView.setVisibility(View.VISIBLE);
			giveUpContent.setVisibility(View.GONE);
		}else {
			imageAdapter.notifyDataSetChanged();
		}	
	}
	
	public void removeLayer(Layer layer){
		if (giveLayerList == null) {
			giveLayerList = new ArrayList<Layer>(4);
		}
		if (layer == null)return;		
		giveLayerList.remove(layer);				
		if (giveLayerList.size() == 0) {
			giveUpActionPromptView.setVisibility(View.VISIBLE);
			giveUpContent.setVisibility(View.GONE);
		}	
	}

	public class ImageAdapter extends BaseAdapter implements onProcessImageResponseListener{
		private ImageUseURIDownloader imageDownloader;
		private final Map<String, Request> pendingRequests = new HashMap<String, Request>();			
		private LayoutInflater mInflater;
		
		public ImageAdapter(Context mContext) {				
			this.mInflater = LayoutInflater.from(mContext);	
			this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);
			this.imageDownloader.setSaveType(FilePathConstant.bookType);			
		}
		
		@Override
		public int getCount() {
			int size = 0;
			if (giveLayerList != null) {
				size = giveLayerList.size();
			}	
			currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			return size;
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
			SelectImageView imageView = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.horizontalist_image_selectitem, null);
				imageView =  (SelectImageView) convertView.findViewById(R.id.photo);
				convertView.setTag(imageView);
			}else {
				imageView = (SelectImageView) convertView.getTag();
			}			
			Layer layer = giveLayerList.get(position);
						
			if (selectedPo != null && selectedPo.layer.contentId.equals(layer.contentId)){
				imageView.setImageBitmap(null);
			}else {
				ImageInfo pBImageInfo = PhotoBookProductUtil.getLayerImageInfo(layer,currentPhotoBook.chosenpics);
				Layer pBLayer =  PhotoBookProductUtil.getLayerInfo(layer, currentPhotoBook.chosenLayers);
				if (pBImageInfo != null) {
					String infoId = pBImageInfo.id;
					Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, infoId);	
					imageView.setTag(infoId);	
					imageView.setImageBitmap(mBitmap == null ? getWaitBitmap() : mBitmap);				
					if (mBitmap == null) {	
						if (pBImageInfo.isfromNative) {
							imageDownloader.downloadProfilePicture(infoId, null, imageView, position);
						}else {
							String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, infoId, true);
							if (thumbnailPath != null) {
								imageDownloader.downloadProfilePicture(infoId, thumbnailPath, imageView, position);			
							}else {
								URI pictureURI = null ;			
								try {
									pictureURI = new URI(pBImageInfo.downloadThumbnailUrl);
						    	} catch (URISyntaxException e) {
						    		pictureURI = null;
						    	}  
								if (pictureURI != null) {
									RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(pBImageInfo.id, pictureURI, imageView, position, true,true,FilePathConstant.bookType,currentPhotoBook.id);
							    	RssTabletApp.getInstance().setOnProcessResponseEndListener(new OnProcessResponseEndListener() {						
										@Override
										public void onProcessEnd(ImageInfo imageInfo,boolean isEdit) {
											if (isEdit) return;
											notifyDataSetChanged();
										}
									});			
								}			
							}
						}
					}				
				}else if (pBLayer != null){				
					String layerId = pBLayer.contentId;		
					Bitmap mBitmap = MemoryCacheUtil.getBitmap(mMemoryCache, layerId);				
					imageView.setTag(layerId);					
					imageView.setImageBitmap((mBitmap == null ? getWaitBitmap() : mBitmap)); 															
					if (mBitmap == null) {
						URI pictureURI = PhotoBookProductUtil.getURI(pBLayer,imagelayoutParams.width,imagelayoutParams.height);
						if (pictureURI != null) {
							imageDownloader.downloadProfilePicture(layerId, pictureURI, imageView, position, true, currentPhotoBook.id);
						}						
					}
				}
			}		
			imageView.setLayoutParams(imagelayoutParams);			
			return convertView;
		}

		@Override
		public void onProcess(Response response, String profileId, View view,int position, String flowType, String productId) {
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (currentPhotoBook == null || currentPhotoBook.pages == null) return;	
			if (productId != null && !productId.equals(currentPhotoBook.id)) return;
			if (response == null || imageDownloader == null) return;								
			if (response.getError() != null) {

			} else {
				Bitmap bitmap = response.getBitmap();
				MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);	   
				if (bitmap != null && view != null) {												
					if (view instanceof SelectImageView ) {
						if (profileId.equals(view.getTag().toString())) {
							if (view.getVisibility() == View.VISIBLE) {
								((SelectImageView) view).setImageBitmap(bitmap);
							}	
						}							
					} 				
				}
			}			
		}	
	}

	private Bitmap getWaitBitmap(){
		if (waitBitmap == null || waitBitmap.isRecycled()) {
            waitBitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.imagewait60x60);
    	}
		return waitBitmap;
	}

}
