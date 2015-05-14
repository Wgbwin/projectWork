package com.kodak.rss.tablet.activities;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.collage.AlternateLayout;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CollageEditListAdapter;
import com.kodak.rss.tablet.adapter.CollageEditListAdapter.OnSelectListener;
import com.kodak.rss.tablet.adapter.CollageProductAdapter;
import com.kodak.rss.tablet.adapter.CollageProductLayoutsAdapter;
import com.kodak.rss.tablet.adapter.CollageProductThemesAdapter;
import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.CollageEditTaskHandler;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.thread.calendar.SkipShoppingCartTask;
import com.kodak.rss.tablet.thread.collage.AddCollagePageTextTask;
import com.kodak.rss.tablet.thread.collage.CollageEditTask;
import com.kodak.rss.tablet.thread.collage.RotateCollageTask;
import com.kodak.rss.tablet.thread.collage.SetLayoutTask;
import com.kodak.rss.tablet.thread.collage.SetThemeTask;
import com.kodak.rss.tablet.thread.collage.ShuffleContentInCollagePageTask;
import com.kodak.rss.tablet.thread.collage.SwapContentsInCollagePageTask;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.ProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.AniamtionDragHelper;
import com.kodak.rss.tablet.view.EditImageView;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerDragListener;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.TextFontView;
import com.kodak.rss.tablet.view.collage.CollageEditLayer;
import com.kodak.rss.tablet.view.collage.CollageEditPopView;
import com.kodak.rss.tablet.view.collage.CollageMainView;
import com.kodak.rss.tablet.view.collage.CollagePageView;
import com.kodak.rss.tablet.view.collage.DealCollageImagesLayout;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class CollageEditActivity extends BaseHaveISMActivity implements OnClickListener{	

    public SourcePanel panel;	
	private View promptView;
	private SearchButton searchButton;
   	
	private Button cartButton;		
	private TextView printSizeText;		

	private View panelEditContent;	
	private ListView editListView;		
	private List<PrintEditInfo> editList;
	public CollageMainView collageMainView;
	public ProgressBar editBar;
	public CollageEditLayer collageEditLayer;
	private CollageEditTaskHandler editTaskHandler;
	private TextFontView<CollagePage, CollageLayer> pageTextView;
	
	private	Collage currentCollage;
	public float wHRatio;
	
	private AniamtionDragHelper animDragHelper; 
	private RelativeLayout animLayer;
	public DealCollageImagesLayout addImagesView;	
	private CollageEditListAdapter editListAdapter;
		
	private Button pictureButton;
	private Button layoutButton;
	private Button backgroudButton;
	private View photoToolView;
	
	private GridView toolGrid;
	private int grid_adapter_flag;
	public static final int THEME_TPYE = 1,LAYOUT_TPYE = 2;
	public static final int REQUEST_CODE = 2;
	
	public CollageProductThemesAdapter themesAdapter;
	public CollageProductLayoutsAdapter layoutsAdapter;
	
	private int downW, downH;
	private String collageId;
	private List<Theme> themeList;
	
	private HashMap<String, String> attr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_edit_collage);
		
		initView();
		initData();		
						
		PictureUploadService.flowType = AppConstants.collageType;	
		
		int orgHeight = (int) (Math.floor(((19*screenWidth)/20 -48*dm.density)/6)*1.15+3*dm.density);
		if (panel.maxContentHeight >= orgHeight) {
			panel.setOpenContentHeight(orgHeight);		
		}else {
			panel.setOpenAndClose();
		}
	}	
		
	@SuppressWarnings("unchecked")
	public void initView(){		
		flowType = AppConstants.collageType;	
		super.initView();
		
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.setMaxPanelEditContentHeight(50 + app.statusBarHeight);
		panel.setMaxContentHeight(170 + app.statusBarHeight);
				
		promptView = findViewById(R.id.prompt);			
		panelEditContent = findViewById(R.id.collage_content);	
		
		editListView = (ListView) findViewById(R.id.edit_list);
		editListView.setDivider(null);		
		collageMainView = (CollageMainView) findViewById(R.id.collageImage);			
		
		editBar = (ProgressBar) findViewById(R.id.edit_pbar);	
		collageEditLayer = (CollageEditLayer) findViewById(R.id.edit_layer);
		pageTextView = (TextFontView<CollagePage, CollageLayer>) findViewById(R.id.edit_font_view);
		
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);
		
		cartButton = (Button) findViewById(R.id.cart_button);
		cartButton.setOnClickListener(this);
		cartButton.setEnabled(false);
		
		printSizeText = (TextView) findViewById(R.id.print_size);

		addImagesView = (DealCollageImagesLayout) findViewById(R.id.add_image_view);	
		int addWidth = (int) (dm.widthPixels/7 -dm.density*10);	
		RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(addWidth, addWidth);
		rLP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		addImagesView.setLayoutParams(rLP);	
		animLayer = (RelativeLayout) findViewById(R.id.anim_layer);
		
		pictureButton = (Button) findViewById(R.id.source_button);
		layoutButton = (Button) findViewById(R.id.layouts_button);
		backgroudButton = (Button) findViewById(R.id.backgrouds_button);
		photoToolView =  findViewById(R.id.photos_tool);
		
		setButtonDisplay(pictureButton, true);
		setButtonDisplay(layoutButton, false);
		setButtonDisplay(backgroudButton, false);
		
		toolGrid =  (GridView) findViewById(R.id.toolGrid);
		toolGrid.setVisibility(View.GONE);
		setAbstractListViewParams(8);
		
		animDragHelper = new AniamtionDragHelper(CollageEditActivity.this);
		animDragHelper.setAnimParentView(animLayer, collageMainView);
		animDragHelper.setAnimationScaleHalf(false);
	}
	
	public void initData(){
		super.initData();
		
		if (getIntent() != null) {
			collageId = getIntent().getStringExtra(AppConstants.collageId);			
		}	
		
		if (collageId != null && app.collageList != null) {
			for (Collage collage : app.collageList) {
				if (collage != null) {
					if (collage.id.equals(collageId)) {
						collage.isCurrentChosen = true;
						currentCollage = collage;
					}else {
						collage.isCurrentChosen = false;
					}
				}
			}
		}

		editTaskHandler = new CollageEditTaskHandler(this);
		if (editList != null && editList.size() > 0) {
			editList.clear();
		}
		if (editList == null) {
			editList = new ArrayList<PrintEditInfo>(4);
		}		
		editList.add(new PrintEditInfo(R.drawable.pagetext, R.string.ComposePhotobook_AddJournalText, true, "text"));
		editList.add(new PrintEditInfo(R.drawable.shuffle, R.string.ComposeCollage_Shuffle, true, "shuffle"));
		editList.add(new PrintEditInfo(R.drawable.view_landscape, R.string.ComposeCollage_Landscape, true, "landscape"));
		editList.add(new PrintEditInfo(R.drawable.view_portrait, R.string.ComposeCollage_Portrait, true, "portrait"));
		
		currentCollage = CollageUtil.getCurrentCollage();
		
		String productName = ShoppingCartUtil.getProductName(app.getCatalogList(), currentCollage.proDescId);
		printSizeText.setText(productName);
		printSizeText.setVisibility(View.VISIBLE);
		
		wHRatio = CollageUtil.getWHRation(currentCollage);	
		int maxDownW = (int) (dm.widthPixels - dm.density*500);
		int maxDownH = (int) (dm.heightPixels - dm.density*140);
		downW = maxDownW;
		downH = (int) (downW*wHRatio);		
		if (downH > maxDownH) {
			downH = maxDownH;
			downW = (int) (downH/wHRatio);					
		}
		collageMainView.setCollage(currentCollage, downW, downH, mMemoryCache);

		initAdapter();
	}	

	public void initAdapter(){
		editListAdapter = new CollageEditListAdapter(CollageEditActivity.this, editList,new OnSelectListener() {			
			@Override
			public void onSelect(PrintEditInfo editInfo) {				
				if (editInfo == null) return;
				if (currentCollage == null) return;
				CollagePage collagePage = currentCollage.page;
				if (collagePage == null) return;
				String pageId = collagePage.id;
				String editName = editInfo.getName();
				if (editName.equals("text")){						
					if (CollageUtil.isArriveMax(collagePage.layers, currentCollage.chosenpics, collagePage.maxNumberOfImages)) {
						new InfoDialog.Builder(CollageEditActivity.this).setMessage(R.string.ComposeCollage_TooManyTextBlocks)
						.setPositiveButton(getText(R.string.d_ok), null).create()
						.show();	
						return;
					}

					AddCollagePageTextTask addCollagePageTextTask = new AddCollagePageTextTask(CollageEditActivity.this, pageId);
					addCollagePageTextTask.execute();
				}else if (editName.equals("shuffle")){
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_SHUFFLE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					ShuffleContentInCollagePageTask shuffleTask = new ShuffleContentInCollagePageTask(CollageEditActivity.this, pageId);
					shuffleTask.execute();
				}else if (editName.equals("landscape") || editName.equals("portrait")) {
					boolean isPortrait = editName.equals("portrait") ? true :false;
					if (isPortrait) {
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_LANDSCAPE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);	
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_PORTRAIT_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					}else {
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_PORTRAIT_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);	
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_LANDSCAPE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);	
					}
					RotateCollageTask rotateCollageTask = new RotateCollageTask(CollageEditActivity.this, currentCollage.id,  pageId, isPortrait);
					rotateCollageTask.execute();
				}				
			}
		});
		editListView.setAdapter(editListAdapter);
		
		themeList = CollageUtil.fillThemes(app.getThemes());
		themesAdapter = new CollageProductThemesAdapter(CollageEditActivity.this, themeList, wHRatio, mMemoryCache);
		layoutsAdapter = new CollageProductLayoutsAdapter(CollageEditActivity.this, wHRatio, mMemoryCache);
			
		initAction();
	}	

	public void initAction(){		
		pictureButton.setOnClickListener(this);
		layoutButton.setOnClickListener(this);	
		backgroudButton.setOnClickListener(this);
		
		toolGrid.setOnScrollListener(new OnScrollListener() {				
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {					
				CollageProductAdapter adapter = (CollageProductAdapter) toolGrid.getAdapter();	
				if (adapter == null) return; 
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){										    
				     adapter.prioritizeViewRange();
				}else {				
					adapter.lock = true;				
				}							
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				CollageProductAdapter adapter = (CollageProductAdapter) toolGrid.getAdapter();	
				if (adapter == null) return; 
				int end_index = firstVisibleItem + visibleItemCount;  
		        if (end_index >= totalItemCount) {  
		           end_index = totalItemCount - 1;  
		        } 	        
		        adapter.start_index = firstVisibleItem; 
		        adapter.end_index = end_index; 
			}
		});

		nativeGraphicsHandler.setOnGetIamgeOnNativeListener(new OnGetImageOnNativeListener() {

			@Override
			public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets,int position) {																																													
				int[] location = new int[2];
				view.getLocationOnScreen(location);						
				if (location[0] == 0 && location[1]== 0){
					nativeGraphicsHandler.imageAdapter.notifyDataSetChanged();											
				}
				view.getLocationOnScreen(location);	
				if (location[0] == 0 && location[1]== 0) return;				
				int keyId  = imageBuckets.keyAt(position);
				String key = String.valueOf(keyId);
				if (nativeGraphicsHandler.imageAdapter.dirtyList != null && nativeGraphicsHandler.imageAdapter.dirtyList.contains(keyId)) return;
				String value = imageBuckets.valueAt(position)[0];
				String bucketDisplayName = imageBuckets.valueAt(position)[1];				
				Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, key);
				ImageInfo  imageInfo = new ImageInfo(key, value, uri.toString(),uri.toString());
				imageInfo.editUrl = value;
				imageInfo.bucketDisplayName = bucketDisplayName;
				imageInfo.fromSource = "Photos";
									
				dealImageToCollagePage(view, imageInfo,position,true);				
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
				int[] location = new int[2];
				view.getLocationOnScreen(location);						
				if (location[0] == 0 && location[1]== 0){
					facebookGraphicsHandler.fbkImageAdapter.notifyDataSetChanged();											
				}
				view.getLocationOnScreen(location);	
				if (location[0] == 0 && location[1]== 0) return;
				
				String key = fbkPhoto.ID;
				ImageInfo imageInfo = new ImageInfo();
				imageInfo.isfromNative = false;		
				imageInfo.id = key;	
				imageInfo.fromSource = "Facebook";
				imageInfo.bucketDisplayName = fbkPhoto.bucketName;
				imageInfo.downloadOriginalUrl = fbkPhoto.getOriginalLink();
				imageInfo.origHeight = fbkPhoto.origHeight;
				imageInfo.origWidth = fbkPhoto.origWidth;
				if (imageInfo.origHeight > 0 && imageInfo.origWidth > 0) {
					imageInfo.uploadOriginalUrl = imageInfo.downloadOriginalUrl;	
				}
				String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, key, true);
				imageInfo.editUrl = thumbnailPath;
				
				dealImageToCollagePage(view, imageInfo,fbkPhotoPosition,false);		
			}
			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}
			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}			
		});	
		
		toolGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				if (currentCollage.page == null) return;
				if (currentCollage.page.id == null) return;
				setPromptViewGone();
				switch (grid_adapter_flag) {				
				case LAYOUT_TPYE:	
					if (currentCollage.page.alternateLayouts == null) return;	
					AlternateLayout alternateLayout =  currentCollage.page.alternateLayouts.get(position);
					if (alternateLayout == null) return;					
					String checkLayoutId = alternateLayout.layoutId;
					if (checkLayoutId == null) return;
					String oldLayoutId = CollageUtil.getCheckLayoutId();	
					if (oldLayoutId != null && checkLayoutId.equals(oldLayoutId)) return;											
					layoutsAdapter.oldCheckLayoutId = oldLayoutId;					
					CollageUtil.setLayoutsData(checkLayoutId);
					layoutsAdapter.notifyDataSetChanged();				
					SetLayoutTask setLayoutTask = new SetLayoutTask(CollageEditActivity.this, currentCollage.page.id, checkLayoutId,position==0);
					setLayoutTask.execute();					
					break;
				case THEME_TPYE:
					if (themeList == null) {
						themeList = CollageUtil.fillThemes(app.getThemes());
					}
					if (themeList == null) return;	
					Theme theme = themeList.get(position);
					if (theme == null) return;
					String checkThemeId = theme.id;
					if (checkThemeId == null) return;
					String oldThemeId = CollageUtil.getCheckThemeId();	
					if (oldThemeId != null && checkThemeId.equals(oldThemeId)) return;					
					themesAdapter.oldCheckThemeId = oldThemeId;					
					CollageUtil.setCheckTheme(checkThemeId);
					themesAdapter.notifyDataSetChanged();
					SetThemeTask setThemeTask = new SetThemeTask(CollageEditActivity.this, currentCollage.id, checkThemeId);
					setThemeTask.execute();									
					break;
				}
			}
		});	
		
		collageMainView.setOnLayerClickListener(new OnLayerClickListener<CollagePageView, CollagePage, Layer>() {
			@Override
			public void onLayerClick(CollagePageView pageView,CollagePage page, Layer layer, RectF layerRect) {
				if (layer == null) return;
				if (layer.contentId == null) return;
				if ("".equals(layer.contentId)) return;				
				addLayerLocalInfo(layer);				
				collageEditLayer.showEditImageAndPop(pageView, (CollageLayer)layer);								
			}
		});
		collageMainView.setOnLayerDragListener(onLayerDragListener);
		collageEditLayer.setOnPopEditItemClickListener(onEditItemClickListener);
		collageEditLayer.setOnDoneClickListener(onDoneClickListener);
	}	
	
	@Override
	protected void onResume() {
		super.onResume();		
		if (currentCollage != null && currentCollage.page != null) {
			if (currentCollage.page.layers != null && currentCollage.page.layers.length > 0) {
				setPromptViewGone();
			}else {
				bringForwardDownLoadPhoto();
			}
		}
	}
	
	private void bringForwardDownLoadPhoto(){
		if (collageMainView == null ) return;
		if (collageMainView.imageDownloader == null ) return;
		if (currentCollage == null ) return;		
		CollagePage mPage = currentCollage.page;
		if (mPage == null ) return;	
		URI pictureURI = CollageUtil.getURI(mPage, downW, downH);										
		collageMainView.imageDownloader.downloadProfilePicture(mPage.id, pictureURI, null, 0, true, currentCollage.id, mPage.getMainRefreshCount());		
	}	
	
	@Override
	protected void onPause() {				
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();						
	}
	
	@Override
	public void startOver() {
		cancelRequest();
		addImagesView.setTasksNull();
		super.startOver();
	}		
	
	@Override
	public void judgeHaveItems(){			
		SkipShoppingCartTask skipTask = new SkipShoppingCartTask(CollageEditActivity.this);
		skipTask.execute();
	}
	
	public void skipShoppingCart(){
		currentCollage = CollageUtil.getCurrentCollage();				
		if (CollageUtil.isBlank(currentCollage)) {
			new InfoDialog.Builder(CollageEditActivity.this)
			.setMessage(R.string.ComposeCalendar_MinImagesNotSelected)
			.setPositiveButton(R.string.d_ok, null)
			.create()
			.show();							
			return;
		}	

		synchronized (currentCollage) {
			if (currentCollage.page!= null && currentCollage.page.isWantMainRefresh()) {
				CollagePage page = currentCollage.page;
				String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.collageType, page.id, false, page.getMainRefreshCount(),page.getMainRefreshSucCount());	
				if (dispalyPath != null) {
					File downLoadedFile = new File(dispalyPath);
					if (downLoadedFile.exists()) {
						downLoadedFile.delete();
					}	
				}
			}
		}

		if (attr != null) {
			boolean isNotHaveUnd = true;			
			for (Map.Entry<String, String> entry : attr.entrySet()) {		
				if (entry != null) {
					String key =  entry.getKey();
					String value = entry.getValue();
					if (key != null && !"".equals(key)) {
						if (value == null || !(RSSTabletLocalytics.LOCALYTICS_VALUE_YES.equals(value) || RSSTabletLocalytics.LOCALYTICS_VALUE_NO.equals(value)) ) {
							isNotHaveUnd = false;
							break;
						}
					}
				}
			}			
			if(isNotHaveUnd){
				RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_COLLAGE_EDIT_SUMMARY, attr);
			}
		}
		
		ProductInfo pInfo = CollageUtil.getPInfo(currentCollage.id,AppConstants.collageType);			
		CollageUtil.dealWithItem(CollageEditActivity.this,currentCollage,pInfo == null?1:pInfo.num);
		cancelRequest();	
		Intent mIntent = new Intent(CollageEditActivity.this, ShoppingCartActivity.class);
		startActivity(mIntent);
		CollageEditActivity.this.finish();		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.cart_button){	
			judgeHaveItems();		
		}else if(v.getId()==R.id.source_button) {
			setButtonDisplay(pictureButton, true);
			setButtonDisplay(layoutButton, false);
			setButtonDisplay(backgroudButton, false);
			photoToolView.setVisibility(View.VISIBLE);
			photoGridView.setVisibility(View.VISIBLE);
			toolGrid.setVisibility(View.GONE);
		}else if(v.getId()==R.id.layouts_button) {
			setButtonDisplay(pictureButton, false);
			setButtonDisplay(layoutButton, true);
			setButtonDisplay(backgroudButton, false);
			photoToolView.setVisibility(View.GONE);
			photoGridView.setVisibility(View.GONE);
			toolGrid.setVisibility(View.VISIBLE);
			grid_adapter_flag = LAYOUT_TPYE;
			toolGrid.setAdapter(layoutsAdapter);
		}else if(v.getId()==R.id.backgrouds_button) {
			setButtonDisplay(pictureButton, false);
			setButtonDisplay(layoutButton, false);
			setButtonDisplay(backgroudButton, true);
			photoToolView.setVisibility(View.GONE);
			photoGridView.setVisibility(View.GONE);
			toolGrid.setVisibility(View.VISIBLE);
			grid_adapter_flag = THEME_TPYE;
			toolGrid.setAdapter(themesAdapter);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			if (data != null && data.getBooleanExtra("fromCrop", false)) {
				notifyCollagePageChanged();
			}
		}
	}
	
	private OnLayerDragListener<CollagePageView, CollagePage, Layer> onLayerDragListener = new OnLayerDragListener<CollagePageView, CollagePage, Layer>() {
			@Override
			public void onStartDrag(MotionEvent event,CollagePageView pageView, CollagePage page,Layer layer, Bitmap bitmap) {					
				if (page == null || layer == null) return;					
				if (layer.contentId == null || layer.type == null) return;								
				if ("".equals(layer.contentId)) return;											
				float rawX = event.getRawX();
				float rawY = event.getRawY();

				RectF rect = pageView.getLayerRect(layer);
				if (rect == null) return;
				
				int w = (int) (rect.right-rect.left);
				int h = (int) (rect.bottom-rect.top);
				animDragHelper.setWH(w,h);

				if (bitmap == null || bitmap.isRecycled() || page.isWantMainRefresh()) {
					String layerImageInfoPath = CollageUtil.getLayerImageInfoPath(layer);
					bitmap = animDragHelper.getBitmap(layerImageInfoPath);						
				}					
				animDragHelper.createDragImage(bitmap, rawX, rawY);	
			}

			@Override
			public void onDragging(MotionEvent event,CollagePageView pageView, CollagePage page,Layer layer, Bitmap bitmap) {					
				float rawX = event.getRawX();
				float rawY = event.getRawY();	
				animDragHelper.deleteTempImageView();				
				final Object[] result = collageMainView.pointToPosition(rawX,rawY);					
				animDragHelper.onDrag(rawX, rawY);					
				showFrame(result,layer);
			}

			@Override
			public void OnDrop(MotionEvent event,CollagePageView pageView, CollagePage page, Layer layer, Bitmap bitmap) {
				float rawX = event.getRawX();
				float rawY = event.getRawY();			
				animDragHelper.onStopDrag(rawX, rawY);	
				animDragHelper.deleteTempImageView();
				collageMainView.hideAllFrames();
					
				if (page == null || layer == null) return;					
				if (layer.type == null) return;					
				String pageId = page.id;
				String contentId = layer.contentId;
				if (contentId == null || contentId.equals("")) return;	
				final Object[] result = collageMainView.pointToPosition(rawX,rawY);
				if (result == null) return;
				Layer toLayer = (Layer) result[2];																			
				if (toLayer != null && toLayer.type != null && !"".equals(toLayer.contentId) ) {
					if (!contentId.equals(toLayer.contentId)) {
						SwapContentsInCollagePageTask swapTask = new SwapContentsInCollagePageTask(CollageEditActivity.this, pageId, toLayer.contentId, contentId);
						swapTask.execute();
					}										
					return;
				}								
			}																																																															
		};
	
	private OnEditItemClickListener<CollageEditPopView, CollagePage, CollageLayer> onEditItemClickListener = new OnEditItemClickListener<CollageEditPopView, CollagePage, CollageLayer>() {
		@Override
		public void onEditItemClick(CollageEditPopView view, CollagePage page,CollageLayer layer, int itemId) {
			if (view.getType() == CollageEditPopView.TYPE_COLOR_EFFECT) {
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_EFFECTS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				new CollageEditTask(CollageEditActivity.this, CollageEditTask.COLOR_EFFECT, editTaskHandler, page, layer, itemId).start();
			} else {
				switch (itemId) {
				case CollageEditPopView.LAYER_CROP:
					if (page != null && layer != null) {
						Intent intent = new Intent(CollageEditActivity.this, PhotoBookCropImageActivity.class);
						intent.putExtra("Layer", layer);
						intent.putExtra("page", page);
						startActivityForResult(intent, REQUEST_CODE);
						collageEditLayer.dismissEditProgress();
						collageEditLayer.dismiss();
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					}					
					break;
				case CollageEditPopView.LAYER_ENHANCE:					
				case CollageEditPopView.LAYER_UNDO_ENHANCE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ENHANCE, editTaskHandler, page, layer, itemId==CollageEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case CollageEditPopView.LAYER_RED_EYE:
				case CollageEditPopView.LAYER_UNDO_RED_EYE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.RED_EYE, editTaskHandler, page, layer, itemId==CollageEditPopView.LAYER_RED_EYE).start();
					break;
				case CollageEditPopView.LAYER_ROTATE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ROTATE_IMAGE, editTaskHandler, page, layer).start();
					break;	
				case CollageEditPopView.LAYER_REMOVE_IMAGE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.REMOVE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case CollageEditPopView.LAYER_SET_AS_BACKGROUND:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_BACKGROUND_IMAGE, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);					
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.SET_AS_BACKGROUND, editTaskHandler, page, layer).start();
					break;	
				case CollageEditPopView.LAYER_FLIP_HORIZONTAL:
				case CollageEditPopView.LAYER_FLIP_VERTICAL:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer, itemId == CollageEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case CollageEditPopView.PAGE_DELETE_PAGE_TEXT:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.DELETE_PAGE_TEXT, editTaskHandler, page, layer).start();
					break;
				case CollageEditPopView.PAGE_EDIT_PAGE_TEXT:	
					collageEditLayer.dismissEditProgress();
					collageEditLayer.dismiss();
					showFontEditView(false, page, layer, false );
					break;
				}
			}
		}
	};
	
	private CollageEditLayer.OnDoneClickListener onDoneClickListener = new CollageEditLayer.OnDoneClickListener() {
		@Override
		public void OnDoneClick(CollageEditLayer editLayer,CollagePageView pageView, EditImageView ivEdit) {
			if (editLayer.isEditPage() || !ivEdit.isEdited()) {
				editLayer.dismiss();
			} else {
				CollageLayer layer = (CollageLayer) ivEdit.getLayer();
				ROI oldRoi = ProductUtil.getImageCropROI(layer);
				float oldAngle = layer.angle;
				oldAngle = oldAngle % 360;
				if(oldAngle < 0 ){
					oldAngle = oldAngle + 360;
				}
				
				ROI newRoi = ivEdit.getImageROI();
				float newAngle = ivEdit.getLayerAngel();
								
				boolean moved = newRoi != null && !newRoi.equals(oldRoi);
				boolean rotated = newAngle != oldAngle;
				
				if(moved && !rotated ){
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.MOVE_IMAGE, editTaskHandler, pageView.getPage(), layer, newRoi).start();					
				}else if(!moved && rotated){
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ROTATE_CONTENT, editTaskHandler, pageView.getPage(), layer,newAngle).start();
				}else if(moved && rotated){
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.MOVE_AND_ROTATE, editTaskHandler, pageView.getPage(), layer,newRoi,newAngle).start();
				}else{
					editLayer.dismiss();
				}
			}
		}
	};

	private void dealImageToCollagePage(View view,ImageInfo imageInfo,int position,boolean isNative){
		if (imageInfo == null) return;
		if (view == null) return;
		CollagePage collagePage = currentCollage.page;
		if (collagePage == null) return;		
		
		ImageInfo haveImageInfo = null;
		if (currentCollage.chosenpics != null) {
			for (int i = 0; i < currentCollage.chosenpics.size(); i++) {				
				ImageInfo selectedImageInfo = currentCollage.chosenpics.get(i);			
				if (selectedImageInfo.id.equalsIgnoreCase(imageInfo.id)) {
					haveImageInfo = selectedImageInfo;				
					break;
				}
			}
		}

		if (haveImageInfo != null) {			
			if (isNative) {
				nativeGraphicsHandler.imageAdapter.chiceDeleteState(position);
			}else {
				facebookGraphicsHandler.fbkImageAdapter.chiceDeleteState(position);
			}
			
			synchronized (currentCollage) {
				if (currentCollage.chosenpics != null) {
					currentCollage.chosenpics.remove(haveImageInfo);
				}
			}
			addImagesView.dealImage(animLayer, view, collagePage.id, haveImageInfo, false);	
		}else {			
			if (CollageUtil.isArriveMax(collagePage.layers, currentCollage.chosenpics, collagePage.maxNumberOfImages)) {
				new InfoDialog.Builder(CollageEditActivity.this).setMessage(R.string.ComposeCollage_TooManyImages)
				.setPositiveButton(getText(R.string.d_ok), null).create()
				.show();	
				return;
			}
			setPromptViewGone();			
			CollageUtil.addImageToCollage(imageInfo);	
			updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			
			if (isNative) {
				nativeGraphicsHandler.imageAdapter.chiceSelectState(position,imageInfo.id);
			}else {
				facebookGraphicsHandler.fbkImageAdapter.chiceSelectState(position,imageInfo.id);
			}
			startUploadService();										
			addImagesView.dealImage(animLayer, view, collagePage.id, imageInfo, true);	
		}	
	}
	
	private void cancelRequest(){	
		if (themesAdapter != null) {
			themesAdapter.cancelRequest();
		}		
		clearDownDataRequest();
		System.gc();		
	}	
	
	private void setPromptViewGone(){
		if (promptView.getVisibility() == View.GONE) return;
		cartButton.setEnabled(true);
		promptView.setVisibility(View.GONE);		
		panelEditContent.setVisibility(View.VISIBLE);			
		panel.setpanelContentHeight(50+app.statusBarHeight);		
	}
	
	public void showEditPBar(){	
		editBar.setVisibility(View.VISIBLE);
	}

	public void notifyCollagePageChanged(){	
		editBar.setVisibility(View.GONE);
		currentCollage = CollageUtil.getCurrentCollage();					
		layoutsAdapter.refreash(wHRatio);					
		collageMainView.refresh(currentCollage);
				
		if (addImagesView.endTasks()) {
			
		}

		if (nativeGraphicsHandler.imageAdapter != null) {
			nativeGraphicsHandler.imageAdapter.refreash();
		}
		if (facebookGraphicsHandler.fbkImageAdapter != null) {
			facebookGraphicsHandler.fbkImageAdapter.refreash();
		}	
		
		editListAdapter.notifyDataSetChanged();
	}	
	
	public void notifyCollageChanged(Collage collage,boolean isUseNewLayout){
		CollageUtil.updateCollage(collage, isUseNewLayout);
		currentCollage = CollageUtil.getCurrentCollage();
		wHRatio = CollageUtil.getWHRation(currentCollage);	
		collageMainView.refresh(currentCollage,wHRatio);
		layoutsAdapter.refreash(wHRatio);
		themesAdapter.notifyDataSetChanged();
		
		editListAdapter.notifyDataSetChanged();
	}	

	private void setButtonDisplay(Button button,boolean isSelected){
		if (isSelected) {
			button.setSelected(true);
			button.setTextColor(0xFFFBBA06);
		}else {
			button.setSelected(false);
			button.setTextColor(Color.WHITE);
		}		
	}	
	
	private void setAbstractListViewParams(int numColumns){				
		toolGrid.setNumColumns(numColumns);
		toolGrid.setVerticalSpacing((int)dm.density*10);
		toolGrid.setHorizontalSpacing(0);							
	}
	
	public boolean isWaitAddImageDone(String pageId){   	
	    return addImagesView.isWaitAddImageDone(pageId);
	}
	
	public void showFontEditView(boolean showAtLeft, CollagePage page, CollageLayer layer, boolean isCaption){
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(this, 47);
		int sideMargin = DimensionUtil.dip2px(this, 0);
		pageTextView.setIsCaption(isCaption).setViewSize(point).setEditPageInfo(page, layer).setTitleVisible(false);
		if(isCaption){
			pageTextView.setFontComponentsVisible(false);
		} else {
			pageTextView.setFontComponentsVisible(true);
		}
		updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		pageTextView.showTextFontView(showAtLeft, topMargin, sideMargin);
		collageMainView.moveToTopLeft();	
	}
	
	public void addLayerLocalInfo(Layer layer){
		RssTabletApp.getInstance().getProductLayerLocalInfos().putIfNotExist(layer, true);
	}
	
	private void showFrame(Object[] result,Layer layer){
	    if (result == null || layer == null) {
	    	collageMainView.hideAllFrames();
		} else {
			CollagePageView pageView = (CollagePageView) result[0];
			CollagePage page = (CollagePage) result[1];
			Layer toLayer = (Layer) result[2];
							
			if (pageView != null && page != null) {					
				if (toLayer != null && layer.contentId != null 
					&& toLayer.contentId != null && !layer.contentId.equals(toLayer.contentId)) {
					pageView.showFrame(toLayer);
				}  else {
					pageView.hideAllFrame();
				}
			} else {
				collageMainView.hideAllFrames();
			}			
		}
	}
	
	 private void updateLocalytics(String key, String value){
			if(attr==null){
				initLocalyticsTrackData();
			}
			attr.put(key, value);
		}
		
	    private void initLocalyticsTrackData() {
			if(attr == null){
				attr = new HashMap<String, String>();
			}
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_SHUFFLE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_LANDSCAPE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_PORTRAIT_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_EFFECTS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_COLLAGE_BACKGROUND_IMAGE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}  

}
