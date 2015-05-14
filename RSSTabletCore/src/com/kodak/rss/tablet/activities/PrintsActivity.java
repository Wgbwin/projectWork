package com.kodak.rss.tablet.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.ProductDescription;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry.UnitPrice;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CartListAdapter;
import com.kodak.rss.tablet.adapter.PrintEditListAdapter;
import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.bean.StorePriceInfo;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.view.ImageEditView;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.SourcePanel.OnOpenAndCloseListener;
import com.kodak.rss.tablet.view.SourcePanel.OnSizeChangeListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PrintsActivity extends BaseHaveISMActivity implements OnClickListener{

	private CartListAdapter cartListAdapter;				
	private ImageEditView editImage;
	public  ListView CartListView;		
	private TextView printSizeText;		
	private ListView editListView;		
	private List<PrintEditInfo> editList;	
	private PrintEditListAdapter editListAdapter;
	public SourcePanel panel;	
	private View promptView;
	private SearchButton searchButton;
		
	private boolean isFristEdit = true; 		
	WebService service = null;	
				
	private String imageId = null;
	private String category = null;
	public List<StorePriceInfo> StorePriceInfoList;
			
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_prints);	
		if (getIntent() != null) {
			imageId = getIntent().getStringExtra(AppConstants.imageId);
			category = getIntent().getStringExtra("category");
			StorePriceInfoList = (List<StorePriceInfo>) getIntent().getSerializableExtra("storePrice");
		}
			
		initView();
		initData();	
		initViewAction();				
		RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_IMAGE_SELECTION_SCREEN);		
		
		int orgHeight = (int) (Math.floor(((19*screenWidth)/20 -48*dm.density)/6)*1.15+3*dm.density);
		if (panel.maxContentHeight >= orgHeight) {
			panel.setOpenContentHeight(orgHeight);		
		}else {
			panel.setOpenAndClose();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();				
		if (PictureUploadService.mTerminated) {
			PictureUploadService.mTerminated = false;		
		}		
		PictureUploadService.flowType = AppConstants.printType;
	}

	@Override
	protected void onPause() {				
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (editImage != null) editImage.recycleInFinish();		
	}
	
	private void initEditData(){
		if (editList != null && editList.size() > 0) {
			editList.clear();
		}
		if (editList == null) {
			editList = new ArrayList<PrintEditInfo>(4);
		}		
		editList.add(new PrintEditInfo(R.drawable.tools_photoenhance, R.string.prints_tool_enhance, false, "enhance"));
		editList.add(new PrintEditInfo(R.drawable.tools_redeye, R.string.prints_tool_redeye, false, "redeye"));
		editList.add(new PrintEditInfo(R.drawable.tools_color, R.string.prints_tool_color, false, "color"));
		editList.add(new PrintEditInfo(R.drawable.tools_rotate, R.string.prints_tool_rotate, false, "rotate"));
	}

	public void initView() {		
		flowType = AppConstants.printType;
		super.initView();
		if (app.chosenList == null) {
			app.chosenList = new ArrayList<ImageInfo>();
		}		
		
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.setpanelEditContentHeight(160 + app.statusBarHeight);
		editImage = (ImageEditView) findViewById(R.id.editImage);			
		promptView = findViewById(R.id.prompt);		

		RelativeLayout.LayoutParams editImageParams = (RelativeLayout.LayoutParams) editImage.getLayoutParams();
		int value = (int) (dm.density * 510 + 0.5f);
		editImageParams.width = screenWidth - value;
		editImage.setLayoutParams(editImageParams);
		editImage.maxCanvasHeight = panel.maxpanelEditContentHeight;
		editImage.maxCanvasWidth = screenWidth - value;

		CartListView = (ListView) findViewById(R.id.cartView_list);
		CartListView.setDivider(null);
		
		editListView = (ListView) findViewById(R.id.edit_list);
		editListView.setDivider(null);
		editListView.setVisibility(View.VISIBLE);
		initEditData();
						
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);
		
		findViewById(R.id.cart_button).setOnClickListener(this);
		printSizeText = (TextView) findViewById(R.id.print_size);

		editImage.progressBar = progressBar;
		editImage.editHandler = editHandler;					
		findViewById(R.id.previous_button).setOnClickListener(this);

		service = new WebService(PrintsActivity.this);
		progressBar.setVisibility(View.GONE);
						
		editListAdapter = new PrintEditListAdapter(PrintsActivity.this, editList,editHandler,editImage);
		editListView.setAdapter(editListAdapter);	
				
		if (imageId == null) {
			for (int i = 0; app.chosenList != null && i < app.chosenList.size(); i++) {								
				if (app.chosenList.get(i).isCurrentChosen) {
					ImageInfo imageInfo = app.chosenList.get(i);
					imageId = imageInfo.id;
					if (imageInfo.typeMap != null ) {
						List <ProductInfo> productInfoList = imageInfo.typeMap.get(AppConstants.printType);
						for (int j = 0; productInfoList != null && j < productInfoList.size(); j++) {						
							if (productInfoList.get(j).isCurrentChecked && productInfoList.get(j).num > 0) {
								category = productInfoList.get(j).category;
								break;
							}								
						}
						if (category == null) {
							for (int j = 0; productInfoList != null && j < productInfoList.size(); j++) {						
								if (productInfoList.get(j).num > 0) {
									category = productInfoList.get(j).category;
									break;
								}								
							}
						}						
					}	
				}	
			}
		}
		
		if (imageId != null) {			
			promptView.setVisibility(View.GONE);
			editImage.setVisibility(View.VISIBLE);
			CartListView.setVisibility(View.VISIBLE);																	
			panel.setpanelEditContentVisible(true);	
			isFristEdit = false;
			List <ProductInfo> productInfoList = null;
			ImageInfo imageInfo = null;
			for (int i = 0; app.chosenList != null && i < app.chosenList.size(); i++) {					
				app.chosenList.get(i).isCurrentChosen = false;
				if (imageId.equals(app.chosenList.get(i).id)) {
					imageInfo = app.chosenList.get(i);			
					imageInfo.isCurrentChosen = true;
					productInfoList = imageInfo.typeMap.get(AppConstants.printType);
				}	
			}
			
			int selectPos = 0;
			if (category != null) {
				for (int i = 0; productInfoList != null && i < productInfoList.size(); i++) {
					productInfoList.get(i).isCurrentChecked = false;
					if (category.equals(productInfoList.get(i).category)) {
						selectPos = i;
						productInfoList.get(i).isCurrentChecked = true;
						printSizeText.setText(category);
					}			
				}	
			}
					
			cartListAdapter = new CartListAdapter(PrintsActivity.this,productInfoList, editImage);
			CartListView.setAdapter(cartListAdapter);	
			CartListView.setSelection(selectPos);
			imageId = null;		
			setToolRotateButton(true);
			dealWithImageInfoState(imageInfo,0);
		}
		
		panel.setOnOpenAndCloseListener(new OnOpenAndCloseListener() {			
			@Override
			public void onOpenAndCloseStart(SourcePanel sourcePanel, int oldh, int newh) {}			
			@Override
			public void onOpenAndCloseEnd(SourcePanel sourcePanel, int oldh, int newh) {
				int value = (int) (dm.density*(160 + app.statusBarHeight) + 0.5f);
				int maxVisiHeight = dm.heightPixels - value - newh;					
				LinearLayout.LayoutParams elParams = (LinearLayout.LayoutParams) editListView.getLayoutParams();
				if (!(maxVisiHeight >= dm.density*200 && elParams.height >= dm.density*200)) {
					elParams.height = maxVisiHeight;
					editListView.setLayoutParams(elParams);
				}
									
				LinearLayout.LayoutParams clParams = (LinearLayout.LayoutParams) CartListView.getLayoutParams();
				if (cartListAdapter == null) return;					
				if (cartListAdapter.productBuckets == null) return;
				int contentH = (int) (dm.density*(cartListAdapter.productBuckets.size()*60));
				if (!(maxVisiHeight >= contentH && clParams.height >= contentH)) {
					clParams.height = maxVisiHeight;
					CartListView.setLayoutParams(clParams);	
				}				
			}
		});
		
		panel.setOnSizeChangeListener(new OnSizeChangeListener() {		
			@Override
			public void onSizeChanged(SourcePanel sourcePanel, int h) {}			
			@Override
			public void onSizeChangeStart(SourcePanel sourcePanel, int h) {}			
			@Override
			public void onSizeChangeEnd(SourcePanel sourcePanel, final int h) {			
				int value = (int) (dm.density*(160 + app.statusBarHeight) + 0.5f);
				int maxVisiHeight = dm.heightPixels - value - h;					
				LinearLayout.LayoutParams elParams = (LinearLayout.LayoutParams) editListView.getLayoutParams();
				if (!(maxVisiHeight >= dm.density*200 && elParams.height >= dm.density*200)) {
					elParams.height = maxVisiHeight;
					editListView.setLayoutParams(elParams);
				}
									
				LinearLayout.LayoutParams clParams = (LinearLayout.LayoutParams) CartListView.getLayoutParams();
				if (cartListAdapter == null) return;					
				if (cartListAdapter.productBuckets == null) return;
				int contentH = (int) (dm.density*(cartListAdapter.productBuckets.size()*60));
				if (!(maxVisiHeight >= contentH && clParams.height >= contentH)) {
					clParams.height = maxVisiHeight;
					CartListView.setLayoutParams(clParams);	
				}				
			}
		});		

		facebookGraphicsHandler.setOnGetIamgeOnFacebookListener(new OnGetIamgeOnFacebookListener() {			
			@Override
			public void onGetIamgeOnFacebook(View view,FbkPhoto fbkPhoto,int fbkPhotoPosition) {
				if (fbkPhoto == null) return;
				String id = fbkPhoto.ID;
				ImageInfo initImageInfo = onSelectedImage(id);	
				if (initImageInfo == null) {
					facebookGraphicsHandler.fbkImageAdapter.chiceDeleteState(fbkPhotoPosition);					
					isFristEdit = true;
					promptView.setVisibility(View.VISIBLE);
					editImage.setVisibility(View.GONE);
					progressBar.setVisibility(View.GONE);	
					CartListView.setVisibility(View.GONE);																	
					panel.setpanelEditContentVisible(false);				
					return;
				}				
				
				initImageInfo.isCurrentChosen = true;		
				if (!isExist) {														
					initImageInfo.isfromNative = false;
					initImageInfo.fromSource = "Facebook";										
					initImageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();
					initImageInfo.origHeight = fbkPhoto.origHeight;
					initImageInfo.origWidth = fbkPhoto.origWidth;
					if (initImageInfo.origHeight > 0 && initImageInfo.origWidth > 0) {
						initImageInfo.uploadOriginalUrl = initImageInfo.downloadOriginalUrl;	
					}					
					initImageInfo.downloadThumbnailUrl = fbkPhoto.getThumbnailLink();
					initImageInfo.bucketDisplayName = fbkPhoto.bucketName;
					app.chosenList.add(initImageInfo);	
					facebookGraphicsHandler.fbkImageAdapter.chiceSelectState(fbkPhotoPosition);
				}else {				
					if (!initImageInfo.id.equals(id)) {								
						facebookGraphicsHandler.fbkImageAdapter.chiceDeleteState(fbkPhotoPosition);								
					}
				}
				dealWithImageInfoState(initImageInfo,fbkPhotoPosition);					
			}

			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}
			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}			
		});	
		
		nativeGraphicsHandler.setOnGetIamgeOnNativeListener(new OnGetImageOnNativeListener() {
			
			@Override
			public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets,int position) {								
				int keyId  = imageBuckets.keyAt(position);
				String id = String.valueOf(keyId);
				if ((nativeGraphicsHandler.imageAdapter.dirtyList != null && nativeGraphicsHandler.imageAdapter.dirtyList.contains(keyId))) return;									
				progressBar.setVisibility(View.GONE);
				ImageInfo imageInfo = onSelectedImage(id);	
				if (imageInfo == null) {
					nativeGraphicsHandler.imageAdapter.chiceDeleteState(position);						
					isFristEdit = true;
					promptView.setVisibility(View.VISIBLE);
					editImage.setVisibility(View.GONE);					
					CartListView.setVisibility(View.GONE);																	
					panel.setpanelEditContentVisible(false);				
					return;
				}

				imageInfo.isCurrentChosen = true;		
				if(!isExist) {
					String value = imageBuckets.valueAt(position)[0];	
					String bucketDisplayName = imageBuckets.valueAt(position)[1];
					imageInfo.originalUrl = value;
					imageInfo.editUrl = value;	
					imageInfo.thumbnailUrl = value;	
					imageInfo.bucketDisplayName = bucketDisplayName;
					Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageInfo.id);
					imageInfo.uploadOriginalUrl = uri.toString();										
					imageInfo.isfromNative = true;
					imageInfo.fromSource = "Photos";
					app.chosenList.add(imageInfo);					
					nativeGraphicsHandler.imageAdapter.chiceSelectState(position);		
				}else {	
					if (!imageInfo.id.equals(id)) {												
						nativeGraphicsHandler.imageAdapter.chiceDeleteState(position);	
					}
				}
				dealWithImageInfoState(imageInfo,position);	
			}

			@Override
			public void onGetAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}
			@Override
			public void onDeleteAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}				
		});		
	}
	
	private void dealWithImageInfoState(ImageInfo imageInfo,int position){						
		progressBar.setVisibility(View.GONE);
		if (!imageInfo.isfromNative) {
			String originalPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, false);
			if (originalPath != null) {			
				imageInfo.originalUrl = originalPath;
				if (imageInfo.editUrl == null) {
					imageInfo.editUrl = originalPath;
				}				
				if (imageInfo.uploadOriginalUrl == null) {
					imageInfo.uploadOriginalUrl = originalPath;	
				}
			}else {
				URI pictureURI = null;
				try {
					pictureURI = new URI(imageInfo.downloadOriginalUrl);
				} catch (URISyntaxException e) {
					pictureURI = null;
				}
				if (pictureURI == null) return;					
				progressBar.setVisibility(View.VISIBLE);					
				app.imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, position, true,false,FilePathConstant.printType,null);
				app.setOnProcessResponseEndListener(new OnProcessResponseEndListener() {						
					@Override
					public void onProcessEnd(ImageInfo imageInfo,boolean isEdit) {
						if (!isEdit) return;
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);	
						}
						setImageEditState(imageInfo);								
					}
				});					
			}
		}		
		setImageEditState(imageInfo);			
	}

	private void setImageEditState(ImageInfo imageInfo){
		if (app.getColorEffectList() != null && !app.getColorEffectList().isEmpty()) {
			for (int i = 0; i < app.getColorEffectList().size(); i++) {	
				ColorEffect colorEffect = app.getColorEffectList().get(i);
				colorEffect.isChecked = false;
				if (imageInfo.colorEffects != null && imageInfo.colorEffects.name.equals(colorEffect.name)) {
					colorEffect.isChecked = true;
				}
			}						
			if (imageInfo.isCurrentChosen && imageInfo.editUrl != null && imageInfo.imageOriginalResource != null) {
				setTool(imageInfo, true);					
			}else {
				if (progressBar != null) {
					progressBar.setVisibility(View.VISIBLE);	
				}
			}											
		}
		if (imageInfo.isCurrentChosen) {
			if (editImage != null) {
				editImage.setImageInfo(imageInfo);										
				editImage.isChangeImage = true;
				editImage.refresh();	
			}			
		}else {
			List <ProductInfo> productInfoList = imageInfo.typeMap.get(AppConstants.printType);
			ProductInfo defaultProductInfo = null;
			for (int i = 0; i < productInfoList.size(); i++) {
				defaultProductInfo = productInfoList.get(i);
				if (defaultProductInfo.isCurrentChecked) {
					break;
				}	
			}
			ImageUtil.calculateDefaultRoi(imageInfo, defaultProductInfo);				
		}													
	}
			
	private void initViewAction(){	
		CartListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				for (int i = 0; i < cartListAdapter.productBuckets.size(); i++) {
					if (i == arg2) {						
						cartListAdapter.productBuckets.get(arg2).isCurrentChecked = true;
						printSizeText.setText(cartListAdapter.productBuckets.get(arg2).category);
					} else {
						cartListAdapter.productBuckets.get(i).isCurrentChecked = false;
					}
				}
				cartListAdapter.notifyDataSetChanged();
			}
		});
	}	

	@Override
	public void onClick(View v) {		
		super.onClick(v);
		if(v.getId()==R.id.cart_button){			
			if (app.chosenList.size()>0 && judgeSelectHavedPhotos()) {
				recycleInFinish();					
				Intent mIntent = new Intent(this, ShoppingCartActivity.class);
				startActivity(mIntent);
				clearDownDataRequest();
				finish();
			}else {	
				new InfoDialog.Builder(this).setMessage(R.string.cart_layout_content)
				.setPositiveButton(getText(R.string.d_ok), null).create()
				.show();		
			}						
		}else if (v.getId()==R.id.previous_button) {			
			if (app.isUseDoMore) {
				previousDoMoreOver();
			}else {
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startOver();				
					}		
				};		
				new InfoDialog.Builder(this).setMessage(R.string.privious_layout_content)
				.setPositiveButton(getText(R.string.d_no), null)
				.setNegativeButton(R.string.d_yes, yesOnClickListener).create()
				.show();
			}
						
		}else if (v.getId()==R.id.source_name) {
			if (panel.mContentHeight > 0) {
				RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_IMAGE_SELECTION_SCREEN);
			}			
			panel.setOpenAndClose();
		}
	}
	
	public void recycleInFinish() {
		CartListView.setAdapter(null);
		photoGridView.setAdapter(null);	
		editListView.setAdapter(null);						
	}		
			
	@Override
	public void startOver() {						
		previousOver();
		super.startOver();
	}
	
	@Override
	public void previousDoMoreOver() {		
		previousOver();
		super.previousDoMoreOver();
	}
	
	@Override
	public void dealWithUploadSuceess(String picUploadSuceessId,boolean isThumbnail, String flowType, String productId) {
		if (AppConstants.printType.equalsIgnoreCase(flowType)){
			ImageInfo sucImageInfo = getDealWithInfo(picUploadSuceessId, flowType, productId);
			if (sucImageInfo != null && sucImageInfo.editUrl != null && sucImageInfo.isCurrentChosen && app.getColorEffectList() != null && !app.getColorEffectList().isEmpty()) {										
				if (progressBar != null) {
					progressBar.setVisibility(View.GONE);	
				}
				setTool(sucImageInfo, true);				
			}						
		}					
	}
	
	private void previousOver(){			
		recycleInFinish();
		clearDownDataRequest();
	}
	
	@Override
	public void judgeHaveItems(){
		if (judgeSelectHavedPhotos()) {
			recycleInFinish();	
			Intent mIntent = new Intent(this, ShoppingCartActivity.class);
			startActivity(mIntent);
			finish();
		}else {
			popNoItemDialog();
		}		
	}

	public Handler editHandler  = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(PrintsActivity.this == null)return;
			if(isFinishing())return;
			if (app == null) app = RssTabletApp.getInstance();
			if(RssTabletApp.getInstance().chosenList == null)return;
			final int action = msg.what;
			Object msgObject = msg.obj;
			Object[] array =  (Object[]) msgObject;	
			ImageInfo imageInfo = null;
			for (int i = 0; i < app.chosenList.size(); i++) {				
				imageInfo = app.chosenList.get(i);			
				if (imageInfo.isCurrentChosen) {
					break;
				}
			}
			switch (action) {
			case 0:
				setTool(imageInfo, false);					
				progressBar.setVisibility(View.VISIBLE);
				break;
			case 1:
				boolean succeed = (Boolean) array[1];					
				if (succeed) {
					ImageInfo mImageInfo = (ImageInfo) array[0];	
					editImage.editAndDisplayImage(mImageInfo);
				}else {
					setTool(imageInfo, true);					
				}				
				break;
			case 2:				
				progressBar.setVisibility(View.GONE);
				setTool(imageInfo, true);	
				break;		
			}
		}
	};
			
	boolean isExist = false;
	private ImageInfo onSelectedImage(String id){	
		isExist = false;
		ImageInfo imageInfo = null;
		ImageInfo selectedImageInfo = null;
		setTool(null,false);		
		if (isFristEdit) {				
			isFristEdit = false;
			promptView.setVisibility(View.GONE);
			editImage.setVisibility(View.VISIBLE);
			CartListView.setVisibility(View.VISIBLE);																	
			panel.setpanelEditContentVisible(true);											   				   
		}
		SortableHashMap<String,List<ProductInfo>> typeMap = null;
		List <ProductInfo> productInfoList = null;

		for (int i = 0; i < app.chosenList.size(); i++) {				
			selectedImageInfo = app.chosenList.get(i);			
			if (selectedImageInfo.id.equalsIgnoreCase(id)) {
				isExist = true;				
				break;
			}
		}
		if (isExist) {			
			// the selectedImageInfo Id is different this Id ,then display the image contain this id,if equal then delete
			if (selectedImageInfo.isCurrentChosen) {
				ShoppingCartUtil.delItem(selectedImageInfo);
				app.chosenList.remove(selectedImageInfo);
				int size = app.chosenList.size();
				if (imageInfo == null && size > 0) {
					imageInfo = app.chosenList.get(size-1);					
					typeMap = imageInfo.typeMap;
					if (typeMap != null) {
						productInfoList = imageInfo.typeMap.get(AppConstants.printType);
					}
				}						
			}else {
				imageInfo = selectedImageInfo;				
				typeMap = imageInfo.typeMap;
				if (typeMap != null) {
					productInfoList = imageInfo.typeMap.get(AppConstants.printType);
				}
			}
		}
		
		if (imageInfo == null && isExist) {
			return imageInfo;
		}	
		
		for (int i = 0; i < app.chosenList.size(); i++) {				
			app.chosenList.get(i).isCurrentChosen = false;
		}

		if (typeMap == null) {						
			typeMap = new SortableHashMap<String, List<ProductInfo>>();
		}	
		if (productInfoList == null) {
			productInfoList = new ArrayList<ProductInfo>();
			int size;
			List<Catalog> catalogs = app.getCatalogList();
			if(catalogs != null && (size = catalogs.size())>0){
				for (int i = 0; i < size; i++) {
					Catalog catalog = catalogs.get(i);
					if (catalog.rssEntries != null) {									
						for (int j = 0; j < catalog.rssEntries.size(); j++) {
							RssEntry rssEntry = catalog.rssEntries.get(j);
							if (AppConstants.printType.equalsIgnoreCase(rssEntry.proDescription.type.trim())) {
								ProductInfo pInfo = new ProductInfo();
								ProductDescription proDescription = rssEntry.proDescription;								
								UnitPrice unitPrice = rssEntry.maxUnitPrice;											
								pInfo.price = unitPrice.priceStr;																						
								pInfo.category =proDescription.shortName;	
								pInfo.pageHeight = proDescription.pageHeight;
								pInfo.pageWidth = proDescription.pageWidth;
								pInfo.descriptionId = proDescription.id;								
								productInfoList.add(pInfo);
							}																	
						}									
					}
				}
			}
			if (productInfoList.size() > 0) {
				for (int i = 0; i < productInfoList.size(); i++) {
					productInfoList.get(i).quantityIncrement = RssTabletApp.getInstance().getQuantityIncrement(productInfoList.get(i).descriptionId);
				}
				productInfoList.get(0).num = productInfoList.get(0).quantityIncrement;
				productInfoList.get(0).isCurrentChecked = true;
				printSizeText.setText(productInfoList.get(0).category);
			}
			typeMap.put(AppConstants.printType, productInfoList);						
		}
		cartListAdapter = new CartListAdapter(PrintsActivity.this,productInfoList, editImage);
		CartListView.setAdapter(cartListAdapter);

		if (!isExist) {
			imageInfo = new ImageInfo();											
			imageInfo.id = id;	
			imageInfo.typeMap = typeMap;
			startUploadService();
		}		
		setToolRotateButton(true);
		ProductInfo dealProductInfo =null;
		for (int i = 0; i < productInfoList.size(); i++) {
			if (productInfoList.get(i)!= null && productInfoList.get(i).isCurrentChecked) {
				dealProductInfo = productInfoList.get(i);
				break;
			}
		}		
		ShoppingCartUtil.dealWithItem(imageInfo, dealProductInfo);
		return imageInfo;
	}
	
	private void setToolRotateButton(boolean isEnable){
		if (editList == null || editListAdapter == null) return;
		for (int i = 0; i < editList.size(); i++) {
			PrintEditInfo editInfo = editList.get(i);
			if (editInfo != null && editInfo.getName().equals("rotate")){
				editInfo.setEnabled(isEnable);				
				editListAdapter.notifyDataSetChanged();
				break;
			}
		}							
	}
	
	private void setTool(ImageInfo imageInfo,boolean isEnable){
		if (editList == null || editListAdapter == null) return;
		for (int i = 0; i < editList.size(); i++) {
			PrintEditInfo editInfo = editList.get(i);
			if (editInfo == null) continue;
			if (editInfo.getName().equals("color")){
				editInfo.setEnabled(isEnable);								
			}
			if (editInfo.getName().equals("redeye")){
				editInfo.setEnabled(isEnable);
				if (imageInfo != null && imageInfo.isUseRedEye) {
					editInfo.setTxt_id(R.string.prints_tool_undoedeye);		
				}else {
					editInfo.setTxt_id(R.string.prints_tool_redeye);	
				}				
			}
			if (editInfo.getName().equals("enhance")){
				editInfo.setEnabled(isEnable);
				if (imageInfo != null && imageInfo.isUseEnhance) {
					editInfo.setTxt_id(R.string.prints_tool_undoenhance);		
				}else {
					editInfo.setTxt_id(R.string.prints_tool_enhance);	
				}				
			}
		}
		editListAdapter.notifyDataSetChanged();		
	}
	
	private boolean judgeSelectHavedPhotos(){		
		if (app.products == null){
			app.products = new ArrayList<ProductInfo>();
			return false;
		} 			
		List<ProductInfo> delList = new ArrayList<ProductInfo>();
		for(ProductInfo pInfo : app.products ) {
			if (pInfo != null && pInfo.num < 1) {						
				delList.add(pInfo);
			}
		}
		app.products.removeAll(delList);			
		for(ProductInfo pInfo : app.products) {
			if (pInfo != null && pInfo.chosenImageList!= null && AppConstants.printType.equals(pInfo.productType)) {				
				ImageInfo imageInfo = pInfo.chosenImageList.get(0);
				if (imageInfo != null) {
					if (imageInfo.imageOriginalResource != null && pInfo.correspondId == null) {
						pInfo.correspondId = imageInfo.imageOriginalResource.id;
					}	
					pInfo.displayImageUrl = imageInfo.editUrl;	
					pInfo.downloadDisplayImageUrl=imageInfo.downloadOriginalUrl;
					
					if (pInfo.getRoi() == null) {
						ImageUtil.calculateDefaultRoi(imageInfo, pInfo);
					}
				}
			}					
		}	
		boolean isHave = true;
		if (app.products.size() <1) {
			isHave = false ;
		}		
		return isHave;
	}	
		
}