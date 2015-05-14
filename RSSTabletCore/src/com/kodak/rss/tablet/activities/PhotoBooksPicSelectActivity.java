package com.kodak.rss.tablet.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.util.JudgeImageFileTypeUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.thread.LoadPhotoBookThemesTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SelectImageView;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.dialog.DialogShowPic;
import com.kodak.rss.tablet.view.dialog.DialogShowPic.onDialogListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

@SuppressLint("ResourceAsColor")
public class PhotoBooksPicSelectActivity extends BaseHaveISMActivity implements OnClickListener{	
			
	private SourcePanel panel;	
	private ImageView dispalyImage;	
	private TextView propmtTitleText;	
	private TextView propmtSelectedPicActionText;
	private TextView propmtSelectedPicResultText;
	private TextView selectedNumText;
	private TextView selectedNumPromptText;
	private TextView lackNumText;
	private TextView needNumText;
	private TextView goodNumText;
	private TextView moreNumText;
	private SearchButton searchButton;	
	
	private boolean isExistShowPicDialog =false;
			
	private RelativeLayout animLayer;
	private int displayImageX,displayImageY,displayImageW,displayImageH;	
	
	private Photobook currentPhotoBook;
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();	

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic_select_photo_books);				
		initView();
		initViewDate();
		initData();					
		RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_IMAGE_SELECTION_SCREEN);	
		
		int orgHeight = (int) (Math.floor(((19*screenWidth)/20 -48*dm.density)/6)*1.15+3*dm.density);
		if (panel.maxContentHeight >= orgHeight) {
			panel.setOpenContentHeight(orgHeight);		
		}else {
			panel.setOpenAndClose();
		}

		PictureUploadService.flowType = AppConstants.bookType;		
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
		currentPhotoBook.isTempStopUpload = true;
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.setpanelContentHeight(400 +app.statusBarHeight);//360 400
				
		dispalyImage = (ImageView) findViewById(R.id.dispalyImage);
		animLayer = (RelativeLayout) findViewById(R.id.anim_layer);
		
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);    		
        findViewById(R.id.magnify_button).setOnClickListener(this);   
		findViewById(R.id.previous_button).setOnClickListener(this);
		findViewById(R.id.continue_button).setOnClickListener(this);
							
		propmtSelectedPicActionText= (TextView) findViewById(R.id.selected_pic_action_prompt);
		propmtSelectedPicResultText= (TextView) findViewById(R.id.selected_pic_result_prompt);
		selectedNumText = (TextView) findViewById(R.id.selectedNum);
		selectedNumPromptText = (TextView) findViewById(R.id.selectedNumPrompt);
		
		lackNumText = (TextView) findViewById(R.id.lackNum);
		needNumText = (TextView) findViewById(R.id.needNum);
		goodNumText = (TextView) findViewById(R.id.goodNum);
		moreNumText = (TextView) findViewById(R.id.moreNum);	
		
		setSelectPhotoNum();
		
		facebookGraphicsHandler.setOnGetIamgeOnFacebookListener(new OnGetIamgeOnFacebookListener() {
			
			@Override
			public void onGetIamgeOnFacebook(View view, FbkPhoto fbkPhoto, int position) {				
				if (fbkPhoto == null) return;
				int[] location = new int[2];
				view.getLocationOnScreen(location);						
				if (location[0] == 0 && location[1]== 0){
					facebookGraphicsHandler.fbkImageAdapter.notifyDataSetChanged();											
				}
				view.getLocationOnScreen(location);	
				if (location[0] == 0 && location[1]== 0) return;
				ImageInfo initImageInfo = new ImageInfo();
				String key = fbkPhoto.ID;				
				initImageInfo.id = key;
				initImageInfo.isfromNative = false;
				initImageInfo.fromSource = "Facebook";
				initImageInfo.bucketDisplayName = fbkPhoto.bucketName;
				initImageInfo.downloadThumbnailUrl = fbkPhoto.getThumbnailLink();
				initImageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();	
				initImageInfo.origHeight = fbkPhoto.origHeight;
				initImageInfo.origWidth = fbkPhoto.origWidth;
				if (initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0) {
					//for facebook only upload the original image,for stay same : set the thumb is original
					initImageInfo.uploadThumbnailUrl = initImageInfo.downloadOriginalUrl;	
				}
				int pos = getPositionInList(currentPhotoBook.chosenpics, key);
				if (pos != -1) {					
					initImageInfo.thumbnailUrl = currentPhotoBook.chosenpics.get(pos).thumbnailUrl;
					currentPhotoBook.chosenpics.remove(pos);
					startAddOrRemoveAnimation(view, initImageInfo, false,0,0);
					facebookGraphicsHandler.fbkImageAdapter.chiceDeleteState(position);	
				}else {																	
					if (currentPhotoBook.chosenpics.size() >= maxselectPhotoSize) {
						String arriveMaxPrompt = TextUtil.formatHighlightText(getString(R.string.arrive_max_prompt),getResources().getColor(R.color.white), String.valueOf(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId))).toString();
						new InfoDialog.Builder(PhotoBooksPicSelectActivity.this).setMessage(arriveMaxPrompt)
						.setPositiveButton(getText(R.string.d_ok), null).create()
						.show();	
						return;
					}
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
					}else {
						URI pictureURI = null;
						try {
							pictureURI = new URI(initImageInfo.downloadOriginalUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}
						if (pictureURI == null) return;																																												
						currentPhotoBook.chosenpics.add(initImageInfo);	
						
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
						app.imageDownloader.downloadProfilePicture(key, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);	
					}else {					
						initImageInfo.thumbnailUrl = thumbnailPath;
						if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
							initImageInfo.uploadThumbnailUrl = thumbnailPath;	
						}
					}
					startAddOrRemoveAnimation(view, initImageInfo, true,0,0);
					facebookGraphicsHandler.fbkImageAdapter.chiceSelectState(position);	
				}				
				setSelectPhotoNum();			
			}

			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {
				if (fbkPhotos == null) return;
				int size = fbkPhotos.size();				
				Map<View,ImageInfo> map = new HashMap<View,ImageInfo>();					
				for (int i = 0; i < size; i++) {
					FbkPhoto fbkPhoto = (FbkPhoto) fbkPhotos.get(i);
					String key = fbkPhoto.ID;			
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos == -1) {
						ImageInfo initImageInfo = new ImageInfo();
						initImageInfo.isfromNative = false;	
						initImageInfo.fromSource = "Facebook";
						initImageInfo.id = key;	
						initImageInfo.bucketDisplayName = fbkPhoto.bucketName;
						initImageInfo.downloadThumbnailUrl = fbkPhoto.getThumbnailLink();
						initImageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();							
						initImageInfo.origHeight = fbkPhoto.origHeight;
						initImageInfo.origWidth = fbkPhoto.origWidth;
						if (initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0) {
							initImageInfo.uploadThumbnailUrl = initImageInfo.downloadOriginalUrl;	
						}							
						genMap(map, i, initImageInfo);					
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
						}else {
							URI pictureURI = null;
							try {
								pictureURI = new URI(fbkPhoto.getOriginalLink());
							} catch (URISyntaxException e) {
								pictureURI = null;
							}
							if (pictureURI == null) continue;																			
							currentPhotoBook.chosenpics.add(initImageInfo);	
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
								continue;
							};				
							app.imageDownloader.downloadProfilePicture(key, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);	
						}else {							
							initImageInfo.thumbnailUrl = thumbnailPath;	
							if (!(initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0)) {
								initImageInfo.uploadThumbnailUrl = thumbnailPath;	
							}							
						}
						facebookGraphicsHandler.fbkImageAdapter.chiceSelectState(i);	
					}					    		
				}
				setSelectPhotoNum();			
				startAddOrRemoveAnimation(map, true);
			}

			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {
				if (fbkPhotos == null) return;
				int size = fbkPhotos.size();
				
				Map<View,ImageInfo> map = new HashMap<View,ImageInfo>();		
				for (int i = size -1; i >= 0; i--) {								
					String key = fbkPhotos.get(i).ID;			
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos != -1) {
						ImageInfo imageInfo = currentPhotoBook.chosenpics.get(pos);
						genMap(map, i, imageInfo);
						currentPhotoBook.chosenpics.remove(pos);
						facebookGraphicsHandler.fbkImageAdapter.chiceDeleteState(i);							
					}							    		
				}
				setSelectPhotoNum();			
				startAddOrRemoveAnimation(map, false);
			}				
		});		
		
		nativeGraphicsHandler.setOnGetIamgeOnNativeListener(new OnGetImageOnNativeListener() {
			
			@Override
			public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets, int position) {
				int[] location = new int[2];
				view.getLocationOnScreen(location);						
				if (location[0] == 0 && location[1]== 0){
					nativeGraphicsHandler.imageAdapter.notifyDataSetChanged();											
				}
				view.getLocationOnScreen(location);	
				if (location[0] == 0 && location[1]== 0) return;				
				int keyId  = imageBuckets.keyAt(position);
				String key = String.valueOf(keyId);
				if (!(nativeGraphicsHandler.imageAdapter.dirtyList != null && nativeGraphicsHandler.imageAdapter.dirtyList.contains(keyId))) {
					String value = imageBuckets.valueAt(position)[0];
					String bucketDisplayName = imageBuckets.valueAt(position)[1];
					ImageInfo initImageInfo = new ImageInfo();						
					initImageInfo.id = key;
					initImageInfo.thumbnailUrl = value;
					initImageInfo.editUrl = value;
					initImageInfo.originalUrl = value;
					initImageInfo.fromSource = "Photos";
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos != -1) {
						currentPhotoBook.chosenpics.remove(pos);
						startAddOrRemoveAnimation(view, initImageInfo, false,0,0);
						nativeGraphicsHandler.imageAdapter.chiceDeleteState(position);	
					}else {						
						if (currentPhotoBook.chosenpics.size() >= maxselectPhotoSize) {
							String arriveMaxPrompt = TextUtil.formatHighlightText(getString(R.string.arrive_max_prompt),getResources().getColor(R.color.white), String.valueOf(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId))).toString();
							new InfoDialog.Builder(PhotoBooksPicSelectActivity.this).setMessage(arriveMaxPrompt)
							.setPositiveButton(getText(R.string.d_ok), null).create()
							.show();	
							return;
						}
						Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, key);					
						ImageInfo imageInfo = new ImageInfo(key, value, uri.toString(),uri.toString());
						imageInfo.editUrl = value;
						imageInfo.bucketDisplayName = bucketDisplayName;
						imageInfo.fromSource = "Photos";
						currentPhotoBook.chosenpics.add(imageInfo);
						startAddOrRemoveAnimation(view, initImageInfo, true,0,0);
						nativeGraphicsHandler.imageAdapter.chiceSelectState(position);		
					}				
					setSelectPhotoNum();					
				}													
			}
			
			@Override
			public void onGetAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {
				if (imageBuckets == null) return;
				int size = imageBuckets.size();				
				Map<View, ImageInfo> map = new HashMap<View, ImageInfo>();
				for (int i = 0; i < size; i++) {
					int keyId  = imageBuckets.keyAt(i);
					String key = String.valueOf(keyId);					
					if ((nativeGraphicsHandler.imageAdapter.dirtyList != null && nativeGraphicsHandler.imageAdapter.dirtyList.contains(keyId))) continue;
					String value = imageBuckets.valueAt(i)[0];
					String bucketDisplayName = imageBuckets.valueAt(i)[1];
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos == -1) {
						if (!(nativeGraphicsHandler.imageAdapter.goodList != null && nativeGraphicsHandler.imageAdapter.goodList.contains(key))) {
							String[] filePath = imageBuckets.valueAt(i);
							if (filePath != null && filePath.length >0) {
								boolean isWebP =  JudgeImageFileTypeUtil.isFilter(filePath[0]);
								if (isWebP) {
									if (nativeGraphicsHandler.imageAdapter.dirtyList == null) {
										nativeGraphicsHandler.imageAdapter.dirtyList = new ArrayList<Integer>(2);
									}
									nativeGraphicsHandler.imageAdapter.dirtyList.add(keyId);
									continue;
								}else {
									if (nativeGraphicsHandler.imageAdapter.goodList == null) {
										nativeGraphicsHandler.imageAdapter.goodList = new ArrayList<Integer>(2);
									}
									nativeGraphicsHandler.imageAdapter.goodList.add(keyId);
								}						
							}	
						}
						Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, key);
						ImageInfo imageInfo = new ImageInfo(key,value,uri.toString(),uri.toString());
						imageInfo.editUrl = value;
						imageInfo.bucketDisplayName = bucketDisplayName;
						imageInfo.fromSource = "Photos";
						genMap(map, i, imageInfo);					
						currentPhotoBook.chosenpics.add(imageInfo);
						nativeGraphicsHandler.imageAdapter.chiceSelectState(i);		
					}					    		
				}
				setSelectPhotoNum();			
				startAddOrRemoveAnimation(map, true);
			}
			
			@Override
			public void onDeleteAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {
				if (imageBuckets == null) return;
				int size = imageBuckets.size();
				
				Map<View, ImageInfo> map = new HashMap<View, ImageInfo>();
				for (int i = size -1; i >= 0; i--) {			
					String key = imageBuckets.keyAt(i).toString();					
					int pos = getPositionInList(currentPhotoBook.chosenpics, key);
					if (pos != -1) {						
						ImageInfo imageInfo = currentPhotoBook.chosenpics.get(pos);	
						genMap(map, i, imageInfo);
						currentPhotoBook.chosenpics.remove(pos);
						nativeGraphicsHandler.imageAdapter.chiceDeleteState(i);		
					}							    		
				}
				setSelectPhotoNum();
				startAddOrRemoveAnimation(map, false);
			}
		});		
	}
	
	private void genMap(Map<View, ImageInfo> map,int i,ImageInfo imageInfo){
		if (map == null) return;
		if (imageInfo == null) return;		
		int fristVPos = photoGridView.getFirstVisiblePosition();		
		int visiableNum = photoGridView.getChildCount();
		int pos = i - fristVPos;
		if (pos >= 0 && pos < visiableNum) {
			View childView = photoGridView.getChildAt(pos);
			if (childView != null) {
				map.put(childView, imageInfo);
			}	
		}
	}

	@Override
	public void startOver() {	
		clearDownDataRequest();
		photoGridView.setAdapter(null);
		super.startOver();		
	}
	
	@Override
	public void judgeHaveItems(){}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);		
		if(v.getId()==R.id.previous_button){
			android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {					
					ProductInfo delInfo = null;
					for(ProductInfo pInfo : app.products) {
						if (pInfo != null && AppConstants.bookType.equals(pInfo.productType)) {				
							if (pInfo.correspondId.equals(currentPhotoBook.id) ) {												
								delInfo = pInfo;
								break;												
							}
						}
					}									
					app.products.remove(delInfo);
					app.chosenBookList.remove(currentPhotoBook);					
					photoGridView.setAdapter(null);
					clearDownDataRequest();
					PhotoBooksPicSelectActivity.this.finish();					
				}		
			};		
			new InfoDialog.Builder(this).setMessage(R.string.privious_layout_content)
			.setPositiveButton(getText(R.string.d_no), null)
			.setNegativeButton(R.string.d_yes, yesOnClickListener).create()
			.show();		
		}else if(v.getId()==R.id.continue_button){			
			int size = currentPhotoBook.chosenpics.size();
			List delList = new ArrayList<ImageInfo>();
			for (ImageInfo imageInfo : currentPhotoBook.chosenpics) {
				if (imageInfo != null && !imageInfo.isfromNative && imageInfo.uploadThumbnailUrl == null) {						
					String thumbnailPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, true);
					if (thumbnailPath == null) {
						URI pictureURI = null;
						try {
							pictureURI = new URI(imageInfo.downloadThumbnailUrl);
						} catch (URISyntaxException e) {
							pictureURI = null;
						}
						if (pictureURI == null) {
							delList.add(imageInfo);
						}else {
							app.imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);
						}
					}else {												
						imageInfo.thumbnailUrl = thumbnailPath;
						imageInfo.uploadThumbnailUrl = thumbnailPath;
					}												
				}
			}
			currentPhotoBook.chosenpics.removeAll(delList);
			size = currentPhotoBook.chosenpics.size();
			if (size >= currentPhotoBook.minNumberOfImages) {				
				String facebookId = SharedPreferrenceUtil.getFacebookUserId(PhotoBooksPicSelectActivity.this);
				if(PhotoBookProductUtil.isBackCoverPageBlank(currentPhotoBook) && !"".equals(facebookId)){
					boolean isHave =false;
					for (ImageInfo imageInfo : currentPhotoBook.chosenpics) {
						if (imageInfo != null && facebookId.equals(imageInfo.id)) {
							isHave = true;
							break;
						}
					}
					if (!isHave) {
						ImageInfo initImageInfo = new ImageInfo();
						initImageInfo.isfromNative = false;	
						initImageInfo.fromSource = "Facebook";
						initImageInfo.id = facebookId;	
						initImageInfo.bucketDisplayName = "";
						initImageInfo.downloadThumbnailUrl = AppConstants.SCOPE + facebookId + "/picture"+"?type="+ "normal" ;
						initImageInfo.downloadOriginalUrl = AppConstants.SCOPE + facebookId + "/picture"+"?type="+ "large" ;											
						String originalPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, facebookId, false);
						boolean isDelte = false;
						if (originalPath != null) {							
							initImageInfo.editUrl = originalPath;
							initImageInfo.originalUrl = originalPath;
							initImageInfo.uploadOriginalUrl = originalPath;												
							currentPhotoBook.chosenpics.add(initImageInfo);
						}else {
							URI pictureURI = null;
							try {
								pictureURI = new URI(initImageInfo.downloadOriginalUrl);
							} catch (URISyntaxException e) {
								pictureURI = null;
								isDelte = true;
							}	
							if (pictureURI != null) {
								currentPhotoBook.chosenpics.add(initImageInfo);							
								app.imageDownloader.downloadProfilePicture(facebookId, pictureURI, null, 0, true,false,FilePathConstant.bookType,currentPhotoBook.id);
							}													
						}
						if (!isDelte) {
							String thumbnailPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, facebookId, true);
							if (thumbnailPath == null) {
								URI pictureURI = null;
								try {
									pictureURI = new URI(initImageInfo.downloadThumbnailUrl);
								} catch (URISyntaxException e) {
									pictureURI = null;
								}
								if (pictureURI != null) {
									app.imageDownloader.downloadProfilePicture(facebookId, pictureURI, null, 0, true,true,FilePathConstant.bookType,currentPhotoBook.id);	
								}else {
									currentPhotoBook.chosenpics.remove(initImageInfo);		
								}						
							}else {							
								initImageInfo.thumbnailUrl = thumbnailPath;						
								initImageInfo.uploadThumbnailUrl = thumbnailPath;	
							}
						}						
					}	
				}
				currentPhotoBook.isTempStopUpload = false;
				startUploadService();							
				String theme = currentPhotoBook.proDescId;
				LoadPhotoBookThemesTask loadThemes = new LoadPhotoBookThemesTask(PhotoBooksPicSelectActivity.this);
				loadThemes.execute(theme);																				
			}else {
				String propmtPhotobookContinue = TextUtil.formatHighlightText(getString(R.string.add_more_prompt),getResources().getColor(R.color.white), String.valueOf(currentPhotoBook.minNumberOfImages)).toString();	
				new InfoDialog.Builder(this).setMessage(propmtPhotobookContinue)
				.setPositiveButton(getText(R.string.d_ok), null).create()
				.show();	
			}						
		}else if(v.getId()==R.id.magnify_button){
			if (currentPhotoBook.chosenpics != null && currentPhotoBook.chosenpics.size() > 0 && !isExistShowPicDialog) {				
				DialogShowPic dialogShowPic = new DialogShowPic();				
				dialogShowPic.setObjectList(PhotoBooksPicSelectActivity.this,currentPhotoBook.chosenpics,mMemoryCache);
				String yourPhotos = getString(R.string.your_photos);
				dialogShowPic.createDialog(PhotoBooksPicSelectActivity.this, yourPhotos, new onDialogListener() {						
						@Override
						public void onDone() {
							isExistShowPicDialog = false;
						}
					});				
				isExistShowPicDialog = true;	
			}		
		}else if(v.getId()==R.id.source_name){
			if (panel.mContentHeight > 0) {
				RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_IMAGE_SELECTION_SCREEN);
			}	
			panel.setOpenAndClose();
		}
	}
	
	private void initViewDate(){				
		propmtTitleText = (TextView) findViewById(R.id.propmt_title);			
		propmtTitleText.setText(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId));	
		maxselectPhotoSize = currentPhotoBook.maxNumberOfImages;
		int needMax = (int) (currentPhotoBook.minNumberOfImages*1.5f);		
		lackNumText.setText("0 - " + (currentPhotoBook.minNumberOfImages-1)); 
		needNumText.setText(currentPhotoBook.minNumberOfImages + " - " + (needMax-1)); 
		goodNumText.setText(needMax+ " - " + currentPhotoBook.idealNumberOfImagesPerBaseBook); 				
		moreNumText.setText((int)(currentPhotoBook.idealNumberOfImagesPerBaseBook+1)+ " - "+maxselectPhotoSize); 
		
		RssEntry rssEntry = PhotoBookProductUtil.getRssEntry(currentPhotoBook.proDescId);
		if (rssEntry != null && rssEntry.proDescription != null) {
			Bitmap bitmap = null;						
			String thumUrl = FilePathConstant.getLoadFilePath(FilePathConstant.bookType, rssEntry.proDescription.id, true);
			if (thumUrl == null) {
				URI pictureURI = PhotoBookProductUtil.getURI(rssEntry);	
				if (pictureURI != null) {
					this.imageDownloader = new ImageUseURIDownloader(this,pendingRequests);	
					this.imageDownloader.setSaveType(FilePathConstant.bookType);					
					this.imageDownloader.setOnProcessImageResponseListener(new onProcessImageResponseListener() {					
						@Override
						public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {
							if (response == null || pendingRequests == null) return;							
							String pendKey = response.getRequest().isThumbnail()? FilePathConstant.thumbnail+profileId:profileId;
							if (productId != null) {
								pendKey = productId + pendKey;
							}	
							pendingRequests.remove(pendKey);							
							if (response.getError() != null) {								
							}else{			
								Bitmap bitmap = response.getBitmap();
								MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);								
								if ( bitmap != null && view != null && view instanceof ImageView) {											
									((ImageView) view).setImageBitmap(bitmap);										
								}														
							}		
						}
					});
					this.imageDownloader.downloadProfilePicture(rssEntry.proDescription.id, pictureURI, dispalyImage,0,true,true);						
				}
			}else {
				bitmap = BitmapFactory.decodeFile(thumUrl);		
			    MemoryCacheUtil.putBitmap(mMemoryCache, rssEntry.proDescription.id, bitmap);													
			}			
			if (bitmap != null) {
				dispalyImage.setImageBitmap(bitmap);
			}		
		}			
	}
	
	private void setSelectPhotoNum(){		
		String propmtSelectedPicActionContent = "";
		String propmtSelectedPicResultContent = "";
		int size = currentPhotoBook.chosenpics.size();			
		int needMax = (int)(currentPhotoBook.minNumberOfImages*1.5f);				
		propmtSelectedPicActionContent = getString(R.string.imageSelNumLackActionPrompt);
		propmtSelectedPicResultContent = TextUtil.formatHighlightText(getString(R.string.add_more_prompt),getResources().getColor(R.color.yellow), String.valueOf(currentPhotoBook.minNumberOfImages)).toString();							
		selectedNumText.setTextColor(getResources().getColor(R.color.red));
		selectedNumPromptText.setTextColor(getResources().getColor(R.color.red));		
		if (size >= currentPhotoBook.minNumberOfImages && size <= (needMax-1)) {        				
			propmtSelectedPicActionContent = getString(R.string.imageSelNumNeedPrompt);			
			propmtSelectedPicResultContent = getString(R.string.imageSelNumNeedResultPrompt);
			selectedNumText.setTextColor(getResources().getColor(R.color.yellow));
			selectedNumPromptText.setTextColor(getResources().getColor(R.color.yellow));			
		}else if (size > needMax-1 && size <= currentPhotoBook.idealNumberOfImagesPerBaseBook) {			
			propmtSelectedPicActionContent = getString(R.string.imageSelNumGoodPrompt);
			propmtSelectedPicResultContent = TextUtil.formatHighlightText(getString(R.string.imageSelNumGoodResultPrompt),getResources().getColor(R.color.yellow), String.valueOf(needMax),String.valueOf(currentPhotoBook.idealNumberOfImagesPerBaseBook)).toString();
			selectedNumText.setTextColor(getResources().getColor(R.color.chartreuse));
			selectedNumPromptText.setTextColor(getResources().getColor(R.color.chartreuse));		
		}else if (size > currentPhotoBook.idealNumberOfImagesPerBaseBook) {			
			propmtSelectedPicActionContent = getString(R.string.imageSelNumMorePrompt);
			propmtSelectedPicResultContent = TextUtil.formatHighlightText(getString(R.string.imageSelNumMoreResultPrompt),getResources().getColor(R.color.yellow), String.valueOf(maxselectPhotoSize)).toString();
			selectedNumText.setTextColor(getResources().getColor(R.color.yellow2));
			selectedNumPromptText.setTextColor(getResources().getColor(R.color.yellow2));		
		}			
		selectedNumText.setText(String.valueOf(size));
		propmtSelectedPicActionText.setText(propmtSelectedPicActionContent);
		propmtSelectedPicResultText.setText(propmtSelectedPicResultContent);		
	}
	
	private String LastAnimationID;;
	private void startAddOrRemoveAnimation(View view, ImageInfo imageInfo,boolean add,int position,final int end){
		if(view == null) return;		
		final ImageView iv = new ImageView(this);
		Bitmap bitmap = null;
		View selView = view.findViewById(R.id.photoContent);
		if(selView != null && selView instanceof SelectImageView){
			bitmap = ((SelectImageView)selView).getImageBitmap();
		}
		
		if (bitmap != null) {
			iv.setImageBitmap(bitmap);		
		}else{
			iv.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.imagewait60x60));
		}
		
		int[] location = new int[2];
		view.getLocationInWindow(location);
		int x = location[0];
		int y = location[1];
		int w = view.getWidth();
		int h = view.getHeight();
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
		params.leftMargin = x;
		params.topMargin = y;
		
		if (! (displayImageW > 0)) {
	    	int[] locationDisplayImage = new int[2];
			dispalyImage.getLocationOnScreen(locationDisplayImage);	
			displayImageX = locationDisplayImage[0];
			displayImageY = locationDisplayImage[1];
			displayImageW = dispalyImage.getWidth();
			displayImageH = dispalyImage.getHeight(); 	
		}
		
		TranslateAnimation ta;
		ScaleAnimation sa;
		if(add){
			ta = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.ABSOLUTE, displayImageX-x,
					TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.ABSOLUTE, displayImageY-y);
			sa = new ScaleAnimation(1, (float)displayImageW/w, 1, (float)displayImageH/h,Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);					
		}else{
			ta = new TranslateAnimation(TranslateAnimation.ABSOLUTE, displayImageX-x, TranslateAnimation.RELATIVE_TO_SELF, 0,
					TranslateAnimation.ABSOLUTE, displayImageY-y, TranslateAnimation.RELATIVE_TO_SELF, 0);
			sa = new ScaleAnimation((float)displayImageW/w, 1, (float)displayImageH/h, 1,Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		}
		
		final AnimationSet animSet = new AnimationSet(true);				
		animSet.addAnimation(sa);
		animSet.addAnimation(ta);
		animSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animSet.setDuration(500);
//		animSet.setFillAfter(true);			
					
		if (position == end) LastAnimationID = animSet.toString();
		
		if (end == 0) animLayer.removeAllViews();
	
		animLayer.addView(iv,params);
		iv.startAnimation(animSet);
		
		animSet.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if (iv != null) {
					iv.setVisibility(View.GONE);
				}
				String animaionID = animation.toString();
				if (LastAnimationID != null && animaionID.equalsIgnoreCase(LastAnimationID)) {
					if (animLayer == null) return;
					if (end == 0) animLayer.removeAllViews();									
				}
			}
		});
		
	}
	
	private void startAddOrRemoveAnimation(Map<View, ImageInfo> map, boolean add){
		animLayer.removeAllViews();
		if (map == null) return;
		int size = map.size();
		if (size == 0) return;	
		int index = 0;
		for(Map.Entry<View, ImageInfo> entry : map.entrySet()){
			index++;
			if (entry != null && entry.getKey() != null && entry.getValue() != null) {
				startAddOrRemoveAnimation(entry.getKey(), entry.getValue(), add,index,size-1);
			}			
		}
	}
	
}
