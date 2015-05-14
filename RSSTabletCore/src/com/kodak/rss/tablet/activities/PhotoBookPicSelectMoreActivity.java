package com.kodak.rss.tablet.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotoBookPicSelectMoreActivity extends BaseHaveISMActivity implements OnClickListener{	

	private TextView selectMorePromptView;							
	private View view;
	private Animation anim_down;	
	private ArrayList<String> addMoreImages;	
	private Photobook currentPhotoBook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);	
		LayoutInflater mInflater = LayoutInflater.from(this);	
		view = mInflater.inflate(R.layout.activity_pic_more_select_photo_books, null);
		setContentView(view);
		initView();	
		initData();			
			
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
	}
	
	@Override
	protected void onPause() {			
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();				
	}

	public void initView() {
		flowType = AppConstants.bookType;		 
		super.initView();				
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();		
		addMoreImages = new ArrayList<String>();		
		selectMorePromptView = (TextView) findViewById(R.id.select_more_prompt);	
		selectMorePromptView.setText(R.string.select_images_prompt);
		
		findViewById(R.id.done_button).setOnClickListener(this);		
		anim_down = AnimationUtils.loadAnimation(this, R.anim.slide_up_to_down_anim);
		anim_down.setAnimationListener(new AnimationListener() {		
			@Override
			public void onAnimationStart(Animation animation) {							
			}	
			@Override
			public void onAnimationEnd(Animation animation) {
				anim_down.setFillAfter(true);							
				int size = addMoreImages.size();
				String[] newAddImages = new String[size];
				for (int i = 0; i < size; i++) {
					newAddImages[i] = addMoreImages.get(i);
				}
				Intent intent = new Intent();	
				intent.putExtra(AppConstants.selectMoreImges, newAddImages);
				PhotoBookPicSelectMoreActivity.this.setResult(RESULT_OK,intent);					
				photoGridView.setAdapter(null);
				PhotoBookPicSelectMoreActivity.this.finish();				
			}
			@Override
			public void onAnimationRepeat(Animation animation) {					
			}
		});
		
		nativeGraphicsHandler.setOnGetIamgeOnNativeListener(new OnGetImageOnNativeListener() {

			@Override
			public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets,int position) {								
				int keyId  = imageBuckets.keyAt(position);
				String key = String.valueOf(keyId);		
				if (!(nativeGraphicsHandler.imageAdapter.dirtyList != null && nativeGraphicsHandler.imageAdapter.dirtyList.contains(keyId))) {
					String value = imageBuckets.valueAt(position)[0];
					String bucketDisplayName = imageBuckets.valueAt(position)[1];
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos == -1) {																					
						if (currentPhotoBook.chosenpics.size() >= currentPhotoBook.maxNumberOfImages) {
							String arriveMaxPrompt = TextUtil.formatHighlightText(getString(R.string.arrive_max_prompt),getResources().getColor(R.color.white), String.valueOf(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId))).toString();
							new InfoDialog.Builder(PhotoBookPicSelectMoreActivity.this).setMessage(arriveMaxPrompt)
							.setPositiveButton(getText(R.string.d_ok), null).create()
							.show();	
							return;
						}						
						Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, key);
						ImageInfo initImageInfo = new ImageInfo(key, value, uri.toString(),uri.toString());
						initImageInfo.editUrl = value;
						initImageInfo.bucketDisplayName = bucketDisplayName;
						initImageInfo.fromSource = "Photos";
						currentPhotoBook.chosenpics.add(initImageInfo);						
						addMoreImages.add(key);							
						nativeGraphicsHandler.imageAdapter.chiceSelectState(position);		
					}							
				}						
			}

			@Override
			public void onGetAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}
			@Override
			public void onDeleteAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}					
		});
		
		facebookGraphicsHandler.setOnGetIamgeOnFacebookListener(new OnGetIamgeOnFacebookListener() {

			@Override
			public void onGetIamgeOnFacebook(View view,FbkPhoto fbkPhoto,int fbkPhotoPosition) {
				if (fbkPhoto == null) return;
				String key = fbkPhoto.ID;
				int pos = getPositionInList(currentPhotoBook.chosenpics, key);
				if (pos == -1) {	
					if (currentPhotoBook.chosenpics.size() >= currentPhotoBook.maxNumberOfImages) {
						String arriveMaxPrompt = TextUtil.formatHighlightText(getString(R.string.arrive_max_prompt),getResources().getColor(R.color.white), String.valueOf(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId))).toString();
						new InfoDialog.Builder(PhotoBookPicSelectMoreActivity.this).setMessage(arriveMaxPrompt)
						.setPositiveButton(getText(R.string.d_ok), null).create()
						.show();	
						return;
					}	
					
					ImageInfo initImageInfo = new ImageInfo();
					initImageInfo.isfromNative = false;		
					initImageInfo.id = key;	
					initImageInfo.fromSource = "Facebook";
					initImageInfo.bucketDisplayName = fbkPhoto.bucketName;
					initImageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();
					initImageInfo.origHeight = fbkPhoto.origHeight;
					initImageInfo.origWidth = fbkPhoto.origWidth;
					if (initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0) {
						initImageInfo.uploadThumbnailUrl = initImageInfo.downloadOriginalUrl;	
					}	
					initImageInfo.downloadThumbnailUrl = fbkPhoto.getThumbnailLink();
					String originalPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, key, false);
					if (originalPath != null) {
						if (initImageInfo.editUrl == null) {
							initImageInfo.editUrl = originalPath;
						}	
						initImageInfo.originalUrl = originalPath;
						if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
							initImageInfo.uploadOriginalUrl = originalPath;		
						}												
						currentPhotoBook.chosenpics.add(initImageInfo);
						addMoreImages.add(initImageInfo.id);
					}else {
						URI pictureURI = null;
						try {
							pictureURI = new URI(initImageInfo.downloadOriginalUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}
						if (pictureURI == null) return;											
						currentPhotoBook.chosenpics.add(initImageInfo);
						addMoreImages.add(initImageInfo.id);	
						if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
							app.imageDownloader.downloadProfilePicture(key, pictureURI, null, 0, true,false,FilePathConstant.bookType,currentPhotoBook.id);	
						}														
					}					
					String thumbnailPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, key, true);
					if (thumbnailPath == null) {
						URI pictureURI = null;
						try {
							pictureURI = new URI(initImageInfo.downloadThumbnailUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}
						if (pictureURI == null) {
							currentPhotoBook.chosenpics.remove(initImageInfo);
							return;
						};
						if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
							app.imageDownloader.downloadProfilePicture(key, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);	
						}					
					}else {						
						initImageInfo.thumbnailUrl = thumbnailPath;	
						if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
							initImageInfo.uploadThumbnailUrl = thumbnailPath;	
						}							
					}
					facebookGraphicsHandler.fbkImageAdapter.chiceSelectState(fbkPhotoPosition);
				}				
			}

			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}
			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}			
		});	
	}
	
	@Override
	public void judgeHaveItems(){		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);		
		if(v.getId()==R.id.done_button){
			currentPhotoBook.isTempStopUpload = false;
			List delList = new ArrayList<ImageInfo>();
			for (ImageInfo imageInfo : currentPhotoBook.chosenpics) {
				if (imageInfo != null && !imageInfo.isfromNative && imageInfo.uploadThumbnailUrl == null) {						
					String thumbnailUrl =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, true);
					if (thumbnailUrl == null) {
						URI pictureURI = null;
						try {
							pictureURI = new URI(imageInfo.downloadThumbnailUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}
						if (pictureURI == null) {
							addMoreImages.remove(imageInfo.id);
							delList.add(imageInfo);							
						}else {
							app.imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);
						}
					}else {
						imageInfo.thumbnailUrl = thumbnailUrl;						
						imageInfo.uploadThumbnailUrl = thumbnailUrl;
					}												
				}
			}
			currentPhotoBook.chosenpics.removeAll(delList);
			if (addMoreImages.size() > 0) {
				startUploadService();
			}			
			view.startAnimation(anim_down);			
		}
	}
		
}