package com.kodak.rss.tablet.activities;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.aphidmobile.flip.FlipViewController.ViewFlipListener;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.content.Theme.BackGround;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.ImageResources;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.StringUtils;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.PhotoBooksProductAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductBackgroundsAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductMainAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductPagesAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductPicturesAdapter;
import com.kodak.rss.tablet.adapter.PhotoBooksProductRearrangeSimplexAdapter;
import com.kodak.rss.tablet.handler.PhotoBookDragTaskHandler;
import com.kodak.rss.tablet.handler.PhotoBookEditTaskHandler;
import com.kodak.rss.tablet.handler.PhotoBookRearTaskHandler;
import com.kodak.rss.tablet.thread.BackageSetPageThemeTask;
import com.kodak.rss.tablet.thread.LoadColorEffectSourcesTask;
import com.kodak.rss.tablet.thread.PhotoBookEditTask;
import com.kodak.rss.tablet.thread.UploadImagesSaveProjectTask;
import com.kodak.rss.tablet.thread.WaitUploadAddSingleImageToPageTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.view.DragGridView;
import com.kodak.rss.tablet.view.DragPopGridView;
import com.kodak.rss.tablet.view.DragRelativeLayoutView;
import com.kodak.rss.tablet.view.EditImageView;
import com.kodak.rss.tablet.view.PhotoBookEditLayer;
import com.kodak.rss.tablet.view.PhotoBookEditPopView;
import com.kodak.rss.tablet.view.PhotoBookGiveUpitemsView;
import com.kodak.rss.tablet.view.PhotoBookLayout;
import com.kodak.rss.tablet.view.PhotoBookMainPageView;
import com.kodak.rss.tablet.view.PhotoEditView;
import com.kodak.rss.tablet.view.PhotobookSetTitleView;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.SourcePanel.OnOpenAndCloseListener;
import com.kodak.rss.tablet.view.SourcePanel.OnSizeChangeListener;
import com.kodak.rss.tablet.view.TextFontView;
import com.kodak.rss.tablet.view.dialog.DialogPhotoBookPageBackGroundOPtions;
import com.kodak.rss.tablet.view.dialog.DialogSaveProject;
import com.kodak.rss.tablet.view.dialog.InfoDialog;


@SuppressLint("HandlerLeak")
public class PhotoBooksProductActivity extends BaseNetActivity implements OnClickListener{	
	private String TAG = "PhotoBooksProductActivity :";	
	public SourcePanel panel;
	public RssTabletApp app;
	private Photobook currentPhotoBook;
	private int statusBarHeight;
	public ImageResources imageResources;
	private SearchButton searchButton;	
	public DragGridView dragPhotoGrid;
	private ListView listView;	
	private DragPopGridView dragPopGridView;	
	private TextView projectNameView;
	private TextView productNameView;
	private TextFontView<PhotobookPage,Layer> editFontView;
	private PhotobookSetTitleView setTitleView;
	
	public PhotoBooksProductPagesAdapter pagesAdapter;
	private PhotoBooksProductBackgroundsAdapter backgroundsAdapter;	
	public PhotoBooksProductRearrangeSimplexAdapter rearrangeSimplexAdapter;
	public PhotoBooksProductPicturesAdapter picturesAdapter;
	
	public PhotoBookLayout pbLayout;
	public PhotoBooksProductMainAdapter mainAdapter;
	public PhotoBookEditLayer layerEdit;
	public PhotoBookEditTaskHandler editTaskHandler = new PhotoBookEditTaskHandler(this);
	private PhotoBookDragTaskHandler handler = new PhotoBookDragTaskHandler(this);
	private PhotoBookRearTaskHandler rearHandler = new PhotoBookRearTaskHandler(this);
	public PhotoBookProductUtil pbRearrangeUtil;
	public InfoDialog dialogPleaseWait;
	public InfoDialog dialogPageNotEditable;
	
	private PhotoEditView photoEditView;
	
	private int grid_adapter_flag;
	public static final int BACKGROUNDS_ADAPTER_TPYE = 1,PICTURES_ADAPTER_TPYE = 2; 
	public static final int REQUEST_CODE = 2;

	public List<PhotobookPage> simplexPages;		
	private List<BackGround> backGroundList = null;
	public int maxNumberOfImages;		
	private String bookId;
	private float wHRatio;
	
	private Button pagesButton;
	private Button rearrangeButton;
	private Button backgroudsButton;
	public Button picturesButton;
	public int defaultContentHeight;
	private int rearrangeContentHeight;
	
	/**
	 * temp value, because now there is some problems for the image loader, if there more two same url to download, only the first will download.
	 * And title page will download at fisrt, but we can't let this happend before init title.
	 * If the bug for image loader has been fixed, you can consider to remove this value
	 */
	public boolean canDownloadTitlePage = false;
	
	public PhotobookPage currentPage;	
	public boolean isFromMyProject;
	public String projectName;
	
	public LruCache<String, Bitmap> mMemoryCache; 
	public LruCache<String, Bitmap> mBigMemoryCache;
		
	public PhotoBookGiveUpitemsView giveUpView;
	private DragRelativeLayoutView dragRelativeLayoutView;
	private View upFlagView;
	private View downFlagView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_photo_books);
		isFromMyProject = false;
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		mBigMemoryCache = MemoryCacheUtil.generMemoryCache(16);

		if(savedInstanceState != null){
			try{
				canDownloadTitlePage = savedInstanceState.getBoolean("canDownloadTitlePage");
			}catch(Exception e){
				Log.e(TAG, e);
			}
		}
		
		if (getIntent() != null) {							
			bookId = getIntent().getStringExtra(AppConstants.bookId);
		}		
		app = RssTabletApp.getInstance();		
		if (bookId != null && app.chosenBookList != null) {
			for (Photobook book: app.chosenBookList) {
				if (book != null ) {
					if (book.id.equals(bookId)) {
						book.isCurrentChosen = true;
						currentPhotoBook = book;
					}else {
						book.isCurrentChosen = false;
					}
				}				
			}			
		}
		if (currentPhotoBook == null) {
			currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		}
		new LoadColorEffectSourcesTask(app.getColorEffectList()).start();		
		initView();
		initAction();
		if(getIntent().getBooleanExtra("justCreated", false)){
			projectNameView.setVisibility(View.INVISIBLE);
			showSetTitleView(true);
		}else{
			canDownloadTitlePage = true;
		}		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("canDownloadTitlePage", canDownloadTitlePage);
	}

	private void initView() {
		simplexPages  = PhotoBookProductUtil.getSimplexPage(currentPhotoBook.pages);
		imageResources = new ImageResources();				
		if (getIntent() != null && getIntent().hasExtra(AppConstants.isFromMyProject)) {				
			isFromMyProject = getIntent().getBooleanExtra(AppConstants.isFromMyProject, false);
			if(isFromMyProject){
				projectName = getIntent().getStringExtra(AppConstants.projectName);
				currentPhotoBook.projectName = projectName;
			}
		}else if(!StringUtils.isEmpty(currentPhotoBook.projectName)){
			projectName = currentPhotoBook.projectName;
		}

		if (currentPhotoBook.chosenpics == null) {
			currentPhotoBook.chosenpics = new ArrayList<ImageInfo>();			
		}			
		currentPhotoBook.chosenLayers = PhotoBookProductUtil.getChosenServerPhotoList(simplexPages, currentPhotoBook.chosenpics,currentPhotoBook.chosenLayers);	
		
		initLayerLocalInfos();
		
		dialogPleaseWait = new InfoDialog.Builder(this).setProgressBar(true).setMessage(R.string.Common_Wait).create();
		dialogPageNotEditable = new InfoDialog.Builder(this).setMessage(R.string.ComposePhotobook_NotEditable).setPositiveButton(R.string.d_ok, null).create();
		
		statusBarHeight = app.statusBarHeight;
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.initPanelContentHeight(50 +statusBarHeight);
		
		if (dm.density > 0) {
			defaultContentHeight = (int) ((dm.heightPixels/dm.density - 153 - statusBarHeight)*2f/3);
		}else {
			defaultContentHeight = (int) ((dm.heightPixels - 153 - statusBarHeight)*2f/3);
		}
		rearrangeContentHeight = 50 +statusBarHeight;

		findViewById(R.id.save_button).setOnClickListener(this);
		findViewById(R.id.cart_button).setOnClickListener(this);
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);
		
		pagesButton = (Button) findViewById(R.id.pages_button);
		rearrangeButton = (Button) findViewById(R.id.rearrange_button);
		backgroudsButton = (Button) findViewById(R.id.backgrounds_button);
		picturesButton = (Button) findViewById(R.id.pictures_button);				
		pagesButton.setOnClickListener(this);
		rearrangeButton.setOnClickListener(this);
		backgroudsButton.setOnClickListener(this);
		picturesButton.setOnClickListener(this);
		
		projectNameView = (TextView) findViewById(R.id.project_name);
		productNameView = (TextView) findViewById(R.id.product_name);
				
		if(!StringUtils.isEmpty(projectName)){
			showProjectName(projectName);
		}
		productNameView.setText(PhotoBookProductUtil.getProductName(currentPhotoBook.proDescId));		
		
		pbLayout = (PhotoBookLayout) findViewById(R.id.layout_photobook);
		pbLayout.setPhotobookSize(currentPhotoBook.pages.get(0).width, currentPhotoBook.pages.get(0).height);
		layerEdit = (PhotoBookEditLayer) findViewById(R.id.layer_edit);
		
		dragPopGridView = (DragPopGridView) findViewById(R.id.dragPopGrid);
		dragPopGridView.setHandler(handler);		
		dragPopGridView.setPhotoBookLayout(pbLayout);
		dragPopGridView.setVisibility(View.GONE);
		
		dragPhotoGrid = (DragGridView) findViewById(R.id.dragPhotoGrid);
		dragPhotoGrid.setHandler(handler);				
		listView =  (ListView) findViewById(R.id.photoList);		
		listView.setVisibility(View.GONE);
		
		giveUpView =  (PhotoBookGiveUpitemsView) findViewById(R.id.give_up_items);
		giveUpView.setVisibility(View.GONE);
		
		dragRelativeLayoutView = (DragRelativeLayoutView) findViewById(R.id.panelContent);
		dragRelativeLayoutView.setHandler(rearHandler);
		
		upFlagView = findViewById(R.id.upFlag);
		downFlagView = findViewById(R.id.downFlag);
		
		editFontView = (TextFontView) findViewById(R.id.edit_font_view);
		setTitleView = (PhotobookSetTitleView) findViewById(R.id.setTitle_view);
		
		photoEditView = (PhotoEditView) findViewById(R.id.photo_edit); 
		
		displayRearScrollPropmt(false);
		initAdpter();
	}
	
	private void  initAdpter(){	
		currentPage = currentPhotoBook.pages.get(0);		
		maxNumberOfImages = PhotoBookProductUtil.getPageMaxNumInPhotoBook(currentPhotoBook.pages);
		wHRatio = PhotoBookProductUtil.getWHRation(PhotoBookProductUtil.getCurrentPhotoBook().proDescId);	
		backGroundList = PhotoBookProductUtil.fillBackGrouds(app.getThemes());
		pagesAdapter = new PhotoBooksProductPagesAdapter(PhotoBooksProductActivity.this, currentPhotoBook, wHRatio,mMemoryCache);		
		backgroundsAdapter = new PhotoBooksProductBackgroundsAdapter(PhotoBooksProductActivity.this,backGroundList, wHRatio,mMemoryCache);				
		picturesAdapter = new PhotoBooksProductPicturesAdapter(PhotoBooksProductActivity.this,wHRatio,mMemoryCache);	
			
		pagesAdapter.selectedPostions[0] = 0;
		pagesAdapter.selectedPostions[1] = 1;
		
		if (currentPhotoBook.isDuplex) {			
			pbLayout.setType(PhotoBookLayout.TYPE_DOUBLE);				
		}else {			
			pbLayout.setType(PhotoBookLayout.TYPE_SINGLE);				
		}
		rearrangeSimplexAdapter = new PhotoBooksProductRearrangeSimplexAdapter(PhotoBooksProductActivity.this, wHRatio,mMemoryCache);	
		mainAdapter = new PhotoBooksProductMainAdapter(this, currentPhotoBook, wHRatio, layerEdit,mBigMemoryCache);
		layerEdit.setOnPopEditItemClickListener(onEditItemClickListener);
		layerEdit.setOnDoneClickListener(onDoneClickListener);
		pbLayout.setAdapter(mainAdapter);
							
	}	
	
	private void initAction(){
		setAbstractListViewParams(8,true);
		pagesButton.setSelected(true);
		pagesButton.setTextColor(0xFFFBBA06);
		dragPhotoGrid.setAdapter(pagesAdapter);	
		
		panel.setOnOpenAndCloseListener(new OnOpenAndCloseListener() {
			
			@Override
			public void onOpenAndCloseStart(SourcePanel sourcePanel, int oldh, int newh) {
				if(!pbLayout.getZoomStatus().isZoomIn){
					pbLayout.changeSize(getPhotoBookHeight());
				}else if(oldh==0 && oldh!=newh){
					pbLayout.zoomOut(true, getPhotoBookHeight());
				}
			}
			
			@Override
			public void onOpenAndCloseEnd(SourcePanel sourcePanel, int oldh, int newh) {
				if(newh == 0 && newh!= oldh){
					pbLayout.zoomIn(true);
				}
			}
		});
		
		panel.setOnSizeChangeListener(new OnSizeChangeListener() {
			
			@Override
			public void onSizeChanged(SourcePanel sourcePanel, int h) {
				if(h==0){
					pbLayout.zoomIn(false);
				}else{
					pbLayout.changeSize(getPhotoBookHeight());
				}
				
			}
			
			@Override
			public void onSizeChangeStart(SourcePanel sourcePanel, int h) {
			}
			
			@Override
			public void onSizeChangeEnd(SourcePanel sourcePanel, int h) {
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						pbLayout.requestLayout();
						pbLayout.refreshAllPages();
					}
				}, 100);
			}
		});
		
		dragPhotoGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {					
				int pos = position/2;
				pagesAdapter.selectedPostions[0] = 2*pos;		
				if (position == pagesAdapter.itemSize -1) {
					pagesAdapter.selectedPostions[1] = -1;
				}else {
					pagesAdapter.selectedPostions[1] = 2*pos+1;
				}				
				pagesAdapter.activity.pbLayout.pageTo(pos);			
				pagesAdapter.notifyDataSetChanged();
				currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				if (currentPhotoBook.isDuplex) {
					rearrangeSimplexAdapter.selectedPostions[0] = pos;								
					rearrangeSimplexAdapter.selectedPostions[1] = -1;
					rearrangeSimplexAdapter.notifyDataSetChanged();
				}else {
					rearrangeSimplexAdapter.selectedPostions[0] = pos - 1;								
					rearrangeSimplexAdapter.selectedPostions[1] = -1;
					rearrangeSimplexAdapter.notifyDataSetChanged();
				}					
			}
		});
		
		dragPopGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
				switch (grid_adapter_flag) {				
				case BACKGROUNDS_ADAPTER_TPYE:	
					if (position == 0) {
						Intent intent = new Intent(PhotoBooksProductActivity.this, PhotoBooksThemeSelectActivity.class);
						intent.putExtra(AppConstants.isFromPhotoBookProduct, true);
						intent.putExtra("wHRatio", wHRatio);
						intent.putExtra(AppConstants.isFromMyProject, isFromMyProject);
						startActivityForResult(intent, REQUEST_CODE);
					}else {						
						PhotobookPage[] currentPages = PhotoBookProductUtil.getCurrentPages(pbLayout.getCurrentPosition());
						if (currentPages != null) {	
							if (PhotoBookProductUtil.getPhotobookPageEditable(currentPages[0]) || PhotoBookProductUtil.getPhotobookPageEditable(currentPages[1])) {
								BackGround backgroud = backGroundList.get(position-1);	
								if (backgroud != null) {
									String backgroudId = backgroud.id;
									int lastIndex = backgroud.name.lastIndexOf(".");
									String mThemeId = backgroud.name.substring(0, lastIndex);									
									BackageSetPageThemeTask task = new BackageSetPageThemeTask(PhotoBooksProductActivity.this, currentPages, backgroudId,mThemeId);
									task.execute();
								}								
							}							
						}
					}
					break;
				case PICTURES_ADAPTER_TPYE:						
					if (position == 0) {
						Intent intent = new Intent(PhotoBooksProductActivity.this, PhotoBookPicSelectMoreActivity.class);						
						startActivityForResult(intent, REQUEST_CODE);						
						overridePendingTransition(R.anim.slide_down_to_up_anim, 0); 						
					}else {	
						if (currentPhotoBook.chosenLayers != null) {
							if (position <= currentPhotoBook.chosenLayers.size()) {
								onClickImage(position-1,false);							
							}else {
								int pos = position - currentPhotoBook.chosenLayers.size() - 1;							
								onClickImage(pos,true);
							}	
						}else {
							onClickImage(position-1,true);
						}
					}						
					break;
				}
			}
		});
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();		
		listView.setOnScrollListener(mScrollListener);
		dragPhotoGrid.setOnScrollListener(mScrollListener);	
		dragPopGridView.setOnScrollListener(mScrollListener);							
		pbLayout.setOnViewFlipListener(new ViewFlipListener() {
			
			@Override
			public void onViewFlipped(View view, int position) {				
				pagesAdapter.selectedPostions[0] = 2*position;		
				if (position == pagesAdapter.itemSize -1) {
					pagesAdapter.selectedPostions[1] = -1;
				}else {
					pagesAdapter.selectedPostions[1] = 2*position+1;
				}							
				pagesAdapter.notifyDataSetChanged();
				currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
				if (currentPhotoBook.isDuplex) {
					rearrangeSimplexAdapter.selectedPostions[0] = position;								
					rearrangeSimplexAdapter.selectedPostions[1] = -1;
					rearrangeSimplexAdapter.notifyDataSetChanged();
				}else {
					rearrangeSimplexAdapter.selectedPostions[0] = position - 1;								
					rearrangeSimplexAdapter.selectedPostions[1] = -1;
					rearrangeSimplexAdapter.notifyDataSetChanged();
				}	
			}
		});		
		pbLayout.setMaxSize(dm.widthPixels, (int)(dm.heightPixels - dm.density*153));
		panel.setMaxOpenContentHeight(defaultContentHeight);
		
	}	
	
	private void initLayerLocalInfos(){
		for(PhotobookPage page: currentPhotoBook.pages){
			if(page == null || page.layers==null)
				continue;
			for(int i=0;i<page.layers.length;i++){
				RssTabletApp.getInstance().getProductLayerLocalInfos().putIfNotExist(page.layers[i], isFromMyProject);
			}
		}
	}
	
	private void onClickImage(int position,boolean isNative){
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		int currentPos = pbLayout.getCurrentPosition();
		PhotobookPage[] pageItems = PhotoBookProductUtil.getPageItems(currentPhotoBook).get(currentPos);
		boolean isDisplayMove = PhotoBookProductUtil.isDiaplayMove(pageItems);
		if (isNative) {			
			ImageInfo info = currentPhotoBook.chosenpics.get(position);				
			boolean isInlayer = PhotoBookProductUtil.isInlayer(info.imageThumbnailResource);			
			if (isInlayer || isDisplayMove) {											
				photoEditView.setMoreImages(currentPos,info,null,null).showAt();		
			}else {
				currentPage = PhotoBookProductUtil.getCurrentPage(pbLayout.getCurrentPosition());
				if (PhotoBookProductUtil.getPhotobookPageEditable(currentPage)){															
					WaitUploadAddSingleImageToPageTask wTask = new WaitUploadAddSingleImageToPageTask(PhotoBooksProductActivity.this,currentPage,info,null);
					wTask.execute();	
				}													
			}
		}else {
			Layer layer = currentPhotoBook.chosenLayers.get(position);
			boolean isInlayer = PhotoBookProductUtil.isInPagelayer(layer);
			if (isInlayer || isDisplayMove) {							
				photoEditView.setMoreImages(currentPos,null,null,layer).showAt();	
			}else {
				currentPage = PhotoBookProductUtil.getCurrentPage(pbLayout.getCurrentPosition());
				if (PhotoBookProductUtil.getPhotobookPageEditable(currentPage)){															
					WaitUploadAddSingleImageToPageTask wTask = new WaitUploadAddSingleImageToPageTask(PhotoBooksProductActivity.this,currentPage,null,layer);
					wTask.execute();	
				}													
			}
		}	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();				
		bringForwardDownLoadPhoto();		
	}
	
	@Override
	protected void onPause() {			
		MemoryCacheUtil.evictAll(mMemoryCache);
		MemoryCacheUtil.evictAll(mBigMemoryCache);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();			
		mMemoryCache = null;
		mBigMemoryCache = null;
	}	
	
	private void showSetTitleView(boolean isCreate){
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(PhotoBooksProductActivity.this, 47);//45+58+5
		int sideMargin = DimensionUtil.dip2px(PhotoBooksProductActivity.this, 0);//0
		setTitleView.setCreatePhotobook(isCreate);
		((PhotobookSetTitleView) setTitleView.setViewSize(point)).showAtLeft(true, topMargin, sideMargin);
	}
	
	private void bringForwardDownLoadPhoto(){
		if (mainAdapter == null ) return;		
		int size = currentPhotoBook.pages.size();	
		if (size < 5) return;	
		int i = 3;					
		double width = mainAdapter.dm.widthPixels*0.7;
		double height = width*mainAdapter.wHRatio;	
		if (mainAdapter.imageDownloader.viewParameters == null) {
			mainAdapter.imageDownloader.setViewParameters((int)width,(int)height);
		}			
		for (; i < size; i++) {
			PhotobookPage page = currentPhotoBook.pages.get(i);
			if (page != null && page.id != null && !"".equals(page.id)) {
				String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.bookType, page.id, false,page.getMainRefreshCount(),page.getMainRefreshSucCount());
				if (dispalyPath == null && !PhotoBookProductUtil.isTitlePage(page)) {
					URI pictureURI = PhotoBookProductUtil.getURI(page,(int)width,(int)height);
					mainAdapter.imageDownloader.downloadProfilePicture(page.id, pictureURI, null,0,true,currentPhotoBook.id,page.getMainRefreshCount());
				}
				String dispalyTmPath =  FilePathConstant.getLoadFilePath(FilePathConstant.bookType, page.id, true,page.getThumbRefreshCount(),page.getThumbRefreshSucCount());
				if (dispalyTmPath == null) {
					URI pictureURI = PhotoBookProductUtil.getURI(page, pagesAdapter.vlayoutParams.width,pagesAdapter.vlayoutParams.height);
					pagesAdapter.imageDownloader.downloadProfilePicture(page.id, pictureURI, null,0,true,currentPhotoBook.id,page.getThumbRefreshCount());
				}
			}
		}
	}
	
	public void showFontEditView(boolean showAtLeft, PhotobookPage page, Layer layer, boolean isCaption){
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(PhotoBooksProductActivity.this, 47);//45+58+5
		int sideMargin = DimensionUtil.dip2px(PhotoBooksProductActivity.this, 0);//0
		editFontView.setIsCaption(isCaption).setViewSize(point).setEditPageInfo(page, layer)
		            .showTextFontView(showAtLeft, topMargin, sideMargin);
	}	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			if (data != null && data.getBooleanExtra("fromCrop", false)) {
				notifyPhotoBookPagesChanged();
			}else {
				switch (grid_adapter_flag) {				
				case BACKGROUNDS_ADAPTER_TPYE:	
					if (data != null && data.getBooleanExtra("isNotChangeTheme", false)) {
						notifyPhotoBookPagesChanged();	
					}else {
						notifyPhotoBookChanged();							
					}			
					break;
					
				case PICTURES_ADAPTER_TPYE:									
					picturesAdapter.refresh();
					final String[] addMoreImages = data.getStringArrayExtra(AppConstants.selectMoreImges);
					if (addMoreImages.length > 0 && photoEditView != null) {
						photoEditView.setMoreImages(pbLayout.getCurrentPosition(),null,addMoreImages,null).showAt();														
					}						
					break;	
				}					
			}			
		}
	}
	
	@Override
	public void startOver() {					
		clearCacheData();	
		super.startOver();
	}

	@Override
	public void judgeHaveItems(){
		PhotoBookProductUtil.dealWithItem(PhotoBooksProductActivity.this,wHRatio);
		PhotoBookProductUtil.deleteUnselectPhoto(currentPhotoBook);
		Intent mIntent = new Intent(PhotoBooksProductActivity.this, ShoppingCartActivity.class);
		startActivity(mIntent);
		clearCacheData();		
		PhotoBooksProductActivity.this.finish();
	}
	
	private void clearCacheData(){
		backgroundsAdapter.cancelRequest();
		picturesAdapter.cancelRequest();
		pagesAdapter.cancelRequest(true);		
		rearrangeSimplexAdapter.cancelRequest();		
		mainAdapter.cancelRequest(false);
		listView.setAdapter(null);
		dragPopGridView.setAdapter(null);	
		dragPhotoGrid.setAdapter(null);		
		System.gc();		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if(v.getId()==R.id.save_button){			
			HashMap<String,String> map = new HashMap<String, String>();
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PROJECT_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_PROJECT_SAVE_IN_PHOTOBOOK);		
			RSSLocalytics.recordLocalyticsEvents(PhotoBooksProductActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_PROJECT_SAVE, map);	
			int totelSize = currentPhotoBook.chosenpics.size();
			int uploadNum = UploadProgressUtil.getUploadPicSuccessNum(currentPhotoBook.chosenpics,false);
			if (uploadNum < totelSize ) {
				ShoppingCartUtil.judgeImageDownload(PhotoBooksProductActivity.this,true,true);
				ShoppingCartUtil.judgeImageUpload(PhotoBooksProductActivity.this,true,true);
				UploadImagesSaveProjectTask uploadImageSaveProjectTask = new UploadImagesSaveProjectTask(PhotoBooksProductActivity.this, projectName);
				uploadImageSaveProjectTask.execute();
			}else {
				DialogSaveProject dialog = new DialogSaveProject(PhotoBooksProductActivity.this,projectName,currentPhotoBook.id);
				Window window = dialog.getWindow();
				window.setBackgroundDrawable(new ColorDrawable(0));
				dialog.show();	
			}			
		}else if (v.getId()==R.id.cart_button) {			
			cartBtnClickListener.onClick(v);
		}else if (v.getId()==R.id.pages_button) {		
			setLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES);
			setViewVisiable(false, true, false);			
			setAbstractListViewParams(8,true);	
			pagesAdapter.start_index = 0;
			dragPhotoGrid.setAdapter(pagesAdapter);
			panel.setMaxOpenContentHeight(defaultContentHeight);			
		}else if (v.getId()==R.id.rearrange_button) {	
			setLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE);
			setViewVisiable(true, false, false);
			rearrangeSimplexAdapter.start_index = 0;
			listView.setAdapter(rearrangeSimplexAdapter);					
			panel.setMaxOpenContentHeight(rearrangeContentHeight);			
		}else if (v.getId()==R.id.backgrounds_button) {
			setLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND);
			setViewVisiable(false, false, true);				
			setAbstractListViewParams(8,false);	
			backgroundsAdapter.start_index = 0;
			dragPopGridView.setAdapter(backgroundsAdapter);
			panel.setMaxOpenContentHeight(defaultContentHeight);				
		}else if (v.getId()==R.id.pictures_button) {
			setLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES);
			setViewVisiable(false, false, false);						
			setAbstractListViewParams(8,false);	
			picturesAdapter.start_index = 0;
			dragPopGridView.setAdapter(picturesAdapter);
			panel.setMaxOpenContentHeight(defaultContentHeight);				
		}	
	}
		
	private void setAbstractListViewParams(int numColumns,boolean isDrag){		
		if (isDrag) {
			dragPhotoGrid.setNumColumns(numColumns);
			dragPhotoGrid.setVerticalSpacing((int)dm.density*10);
			dragPhotoGrid.setHorizontalSpacing(0);					
		}else {
			dragPopGridView.setNumColumns(numColumns);
			dragPopGridView.setVerticalSpacing((int)dm.density*20);
			dragPopGridView.setHorizontalSpacing((int)dm.density*10);			
		}	
	}

	private boolean fromRear = false;
	private void setPanel(Button button ,boolean isEnable){
		fromRear = panel.setHandleEnable(isEnable);		
		dragRelativeLayoutView.rearToOther(fromRear);
		
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
	
	public void notifyDragRelativeLayoutView(){
		dragRelativeLayoutView.setTasksNull();
	}

	private void setViewVisiable(boolean isListViewV,boolean isPhotoGridV,boolean isBackgroundbackground){
		if (isListViewV){
			setPanel(rearrangeButton,false);
			setButtonDisplay(rearrangeButton, true);		
			setButtonDisplay(pagesButton, false);
			setButtonDisplay(backgroudsButton, false);
			setButtonDisplay(picturesButton, false);				
			listView.setVisibility(View.VISIBLE);
			giveUpView.setVisibility(View.VISIBLE);			
			giveUpView.setParams(rearrangeSimplexAdapter.pageWidth,rearrangeSimplexAdapter.pageHeight+20,mMemoryCache);					
			giveUpView.bringToFront();
			dragPhotoGrid.setVisibility(View.GONE);						
			dragPopGridView.setVisibility(View.GONE);								
		}else if (isPhotoGridV){
			setPanel(pagesButton,true);
			setButtonDisplay(rearrangeButton, false);		
			setButtonDisplay(pagesButton, true);
			setButtonDisplay(backgroudsButton, false);
			setButtonDisplay(picturesButton, false);				
			listView.setVisibility(View.GONE);
			giveUpView.setVisibility(View.GONE);
			dragPhotoGrid.setVisibility(View.VISIBLE);						
			dragPopGridView.setVisibility(View.GONE);		
		}else {			
			setButtonDisplay(rearrangeButton, false);		
			setButtonDisplay(pagesButton, false);			
			listView.setVisibility(View.GONE);
			giveUpView.setVisibility(View.GONE);
			dragPhotoGrid.setVisibility(View.GONE);						
			dragPopGridView.setVisibility(View.VISIBLE);			
			if (isBackgroundbackground) {
				setPanel(backgroudsButton,true);
				setButtonDisplay(backgroudsButton, true);
				setButtonDisplay(picturesButton, false);				
				grid_adapter_flag = BACKGROUNDS_ADAPTER_TPYE;				
			}else {	
				setPanel(picturesButton,true);
				setButtonDisplay(backgroudsButton, false);
				setButtonDisplay(picturesButton, true);	
				grid_adapter_flag = PICTURES_ADAPTER_TPYE;				
			}			
		}
		pbLayout.refreshAllPages();	
	}
	
	private OnEditItemClickListener<PhotoBookEditPopView, PhotobookPage, Layer> onEditItemClickListener = 	new OnEditItemClickListener<PhotoBookEditPopView, PhotobookPage, Layer>() {
		
		@Override
		public void onEditItemClick(PhotoBookEditPopView view, final PhotobookPage page,final Layer layer, int itemId) {
			Log.i(TAG,"CLICK ON " + itemId);
			currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			//add layer local info if not added 
			if(layer != null){
				RssTabletApp.getInstance().getProductLayerLocalInfos().putIfNotExist(layer, isFromMyProject);
			}
			
			if(view.getType() != PhotoBookEditPopView.TYPE_COLOR_EFFECT){
				switch (itemId) {
				case PhotoBookEditPopView.LAYER_CROP:
					if (page != null && layer != null) {
						Intent intent = new Intent(PhotoBooksProductActivity.this, PhotoBookCropImageActivity.class);
						intent.putExtra("Layer", layer);
						intent.putExtra("page", page);
						startActivityForResult(intent, REQUEST_CODE);
						layerEdit.dismiss();
					}					
					break;
				case PhotoBookEditPopView.LAYER_ROTATE:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.ROTATE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case PhotoBookEditPopView.LAYER_FLIP_VERTICAL:
				case PhotoBookEditPopView.LAYER_FLIP_HORIZONTAL:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case PhotoBookEditPopView.LAYER_SET_AS_BACKGROUND:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.SET_AS_PAGE_BACKGROUND, editTaskHandler, page, layer).start();
					break;
				case PhotoBookEditPopView.LAYER_ENHANCE:
				case PhotoBookEditPopView.LAYER_UNDO_ENHANCE:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.ENHANCE, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case PhotoBookEditPopView.LAYER_RED_EYE:
				case PhotoBookEditPopView.LAYER_UNDO_RED_EYE:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.RED_EYE, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_RED_EYE).start();
					break;
				case PhotoBookEditPopView.LAYER_REMOVE_IMAGE:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.REMOVE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case PhotoBookEditPopView.PAGE_DELETE_PAGE_TEXT:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.DELETE_PAGE_TEXT, editTaskHandler, page, layer).start();
					break;
				case PhotoBookEditPopView.LAYER_SELECT_PAGE:
					layerEdit.selectCurrentPage();
					break;
				case PhotoBookEditPopView.LAYER_ENTER_CAPTION:
				case PhotoBookEditPopView.LAYER_EDIT_CAPTION:
					layerEdit.dismiss();
					showFontEditView( view.isShowLeft(), page, layer, true );
					break;
				case PhotoBookEditPopView.LAYER_DELETE_CAPTION:
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.DELETE_CAPTION, editTaskHandler, page, layer).start();
					break;
				case PhotoBookEditPopView.PAGE_ADD_PAGE:
					layerEdit.dismiss();
					if(currentPhotoBook.pages.size() >= currentPhotoBook.maxNumberOfPages && PhotoBookProductUtil.getFillerPageNum(currentPhotoBook) == 0){
						new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.photobook_full)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					}else{
						new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.ADD_PAGE, editTaskHandler, page, layer).start();
					}
					break;
				case PhotoBookEditPopView.PAGE_DELETE_PAGE:
					layerEdit.dismiss();
					//if is title page, show dialog
					if(PhotoBookProductUtil.isTitlePage(page)){
						new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.ComposePhotobook_NotDeletable)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					}else{
						new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.DELETE_PAGE, editTaskHandler, page, layer).start();
					}
					break;
				case PhotoBookEditPopView.PAGE_BACKGROUND_OPTIONS:
					DialogPhotoBookPageBackGroundOPtions dialogPbo = new DialogPhotoBookPageBackGroundOPtions(PhotoBooksProductActivity.this,currentPhotoBook.isDuplex);
					dialogPbo.setCanceledOnTouchOutside(false);
					dialogPbo.setCancelable(false);
					dialogPbo.setOnBtnClickListener(new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
							switch(which){
							case DialogPhotoBookPageBackGroundOPtions.PHOTOBOOK_COPY:
								PhotobookPage pageNearby4Cpoy = PhotoBookProductUtil.getNearbyPage(currentPhotoBook, page);
								if(pageNearby4Cpoy == null || !PhotoBookProductUtil.getPhotobookPageEditable(pageNearby4Cpoy)){
									dialogPageNotEditable.show();
								}else{
									new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.PAGE_BACKGROND_COPY, editTaskHandler, page, layer,pageNearby4Cpoy).start();
								}
								break;
							case DialogPhotoBookPageBackGroundOPtions.PHOTOBOOK_EXTEND:
								PhotobookPage pageNearby4Ext = PhotoBookProductUtil.getNearbyPage(currentPhotoBook, page);
								if(pageNearby4Ext == null || !PhotoBookProductUtil.getPhotobookPageEditable(pageNearby4Ext)){
									dialogPageNotEditable.show();
								}else{
									new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.PAGE_BACKGROND_EXTEND, editTaskHandler, page, layer,pageNearby4Ext).start();
								}
								break;
							case DialogPhotoBookPageBackGroundOPtions.PHOTOBOOK_REMOVE:
								new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.PAGE_BACKGROND_REMOVE, editTaskHandler, page, layer).start();
								break;
							case DialogPhotoBookPageBackGroundOPtions.PHOTOBOOK_CANCEL:
								break;
							}
						}
					});
					
					dialogPbo.show();
					layerEdit.dismiss();
					break;
				case PhotoBookEditPopView.PAGE_EDIT_TITLE:
					layerEdit.dismiss();
					showSetTitleView(false);
					break;
				case PhotoBookEditPopView.PAGE_ADD_PAGE_TEXT:
					if(PhotoBookProductUtil.getLayerCount(page) == Integer.parseInt(page.maxNumberOfImages)){
						layerEdit.dismiss();
						new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.ComposePhotobook_JournalTextLimit)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					}else{
						new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.ADD_PAGE_TEXT, editTaskHandler, page, layer, view.isShowLeft()).start();
					}
					break;
				case PhotoBookEditPopView.PAGE_EDIT_PAGE_TEXT:
					view.setVisibility(View.INVISIBLE);
					showFontEditView( view.isShowLeft(), page, layer, false );
					break;
				}
			}else{
				new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.COLOR_EFFECT, editTaskHandler, page, layer,itemId).start();
			}
		}
	};
	
	private PhotoBookEditLayer.OnDoneClickListener onDoneClickListener = new PhotoBookEditLayer.OnDoneClickListener() {
		
		@Override
		public void OnDoneClick(PhotoBookEditLayer editLayer,
				PhotoBookMainPageView pageView, EditImageView ivEdit) {
			if(editLayer.isEditPage() || !ivEdit.isEdited()){
				editLayer.dismiss();
			}else{
				//get old roi and angle
				Layer layer = ivEdit.getLayer();
				ROI oldRoi = PhotoBookProductUtil.getImageCropROI(layer);
				float oldAngle = layer.angle;
				//format oldAngle to 0-360
				oldAngle = oldAngle % 360;
				if(oldAngle < 0 ){
					oldAngle = oldAngle + 360;
				}
				
				//get current roi and angle(0-360)
				ROI newRoi = ivEdit.getImageROI();
				float newAngle = ivEdit.getLayerAngel();
				
				boolean moved = newRoi != null && !newRoi.equals(oldRoi);
				boolean rotated = newAngle != oldAngle;
				
				if(moved && !rotated ){
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.CROP_IMAGE, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newRoi).start();
				}else if(!moved && rotated){
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.ROTATE_CONTENT, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newAngle).start();
				}else if(moved && rotated){
					new PhotoBookEditTask(PhotoBooksProductActivity.this, PhotoBookEditTask.CROP_AND_ROTATE, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newRoi,newAngle).start();
				}else{
					layerEdit.dismiss();
				}
				
			}
			
		}
	};
	
	private int getPhotoBookHeight(){
		return panel.getPanelEditContentHeight() - DimensionUtil.dip2px(this, 45+58);
	}
	
	private void setLocalytics(String key){	
		if (key == null) return;
		HashMap<String,String> map = new HashMap<String, String>();
		if (key.equals(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES)) {
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}else if (key.equals(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE)) {
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}else if (key.equals(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND)) {
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		}else if (key.equals(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES)) {
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PAGES, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_REARRANGE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_BACKGROUND, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
			map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PB_EDIT_PICTURES, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		}
		RSSLocalytics.recordLocalyticsEvents(PhotoBooksProductActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_PB_EDIT, map);	
	}
	
	private OnClickListener cartBtnClickListener = new OnClickListener() {
		private boolean backCoverPageWarningShown = false;
		private boolean tooFewImagesWarningShown = false;
		private boolean blankPageWarningShown = false;
		
		@Override
		public void onClick(View v) {
			Photobook book = PhotoBookProductUtil.getCurrentPhotoBook();
			if(!backCoverPageWarningShown){
				if(PhotoBookProductUtil.isBackCoverPageBlank(book)){
					new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.ComposePhotobook_NoBackImageWarning)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					backCoverPageWarningShown = true;
					return;
				}
			}
			
			if(!tooFewImagesWarningShown){
				if(!PhotoBookProductUtil.hasEnoughImages(book)){
					new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.ComposePhotobook_TooFewImages)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					tooFewImagesWarningShown = true;
					return;
				}
			}
			
			if(!blankPageWarningShown){
				if(PhotoBookProductUtil.hasBlankPage(book)){
					new InfoDialog.Builder(PhotoBooksProductActivity.this)
							.setMessage(R.string.ComposePhotobook_ContainsBlankPage)
							.setPositiveButton(R.string.d_ok, null)
							.create()
							.show();
					blankPageWarningShown = true;
					return;
				}
			}
			
			judgeHaveItems();
		}
	};

	private OnScrollListener mScrollListener = new OnScrollListener() {					
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			PhotoBooksProductAdapter adapter = (PhotoBooksProductAdapter) view.getAdapter();
			if (adapter == null) return; 
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){										    
			     adapter.prioritizeViewRange();
			}else {				
				adapter.lock = true;				
			}							
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {		
			PhotoBooksProductAdapter adapter = (PhotoBooksProductAdapter) view.getAdapter();			
			if (adapter == null) return; 
			int end_index = firstVisibleItem + visibleItemCount;  
	        if (end_index >= totalItemCount) {  
	           end_index = totalItemCount - 1;  
	        } 	        
	        adapter.start_index = firstVisibleItem; 
	        adapter.end_index = end_index; 
		}
	};

	/**
	 * if app.photobook have been reassigned(app.photobook = newPhotobook),use this method to notifyPhotoBookChanged
	 */
	public void notifyPhotoBookChanged(){
		//let all pages to be refreshed
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		for(PhotobookPage page : currentPhotoBook.pages){
			if (PhotoBookProductUtil.getPhotobookPageEditable(page)) {
				page.setPageRefresh();
			}			
		}		
		simplexPages  = PhotoBookProductUtil.getSimplexPage(currentPhotoBook.pages);
		currentPhotoBook.chosenLayers = PhotoBookProductUtil.getChosenServerPhotoList(simplexPages, currentPhotoBook.chosenpics,currentPhotoBook.chosenLayers);			
		maxNumberOfImages = PhotoBookProductUtil.getPageMaxNumInPhotoBook(currentPhotoBook.pages);
		pagesAdapter.refresh();
		mainAdapter.notifyDataSetChanged();
		picturesAdapter.refresh();		
		rearrangeSimplexAdapter.notifyDataSetChanged();		
	}
	
	/**
	 * if app.photobook layout ,use this delete give up data
	 */
	public void removeGiveUpItems(){
		giveUpView.removeAll();
	}
	
	/**
	 * if app.photobook add image ,use this delete give up data
	 */
	public void removeGiveUpItem(String layerId){
		giveUpView.removeLayer(layerId);
	}
	
	public void displayRearScrollPropmt(boolean isDisplay){
		if (isDisplay) {
			upFlagView.setVisibility(View.VISIBLE);
			upFlagView.bringToFront();
			downFlagView.setVisibility(View.VISIBLE);
		}else {
			upFlagView.setVisibility(View.GONE);
			downFlagView.setVisibility(View.GONE);
		}		
	}
	
	/**
	 * if app.photobook havn't been reassigned(app.photobook = newPhotobook),use this method to notifyPhotoBookChanged
	 */
	public void notifyPhotoBookPagesChanged(){
		//If book cover is hollow and title page is changed, we need to refresh cover too.
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		if(PhotoBookProductUtil.isCoverHollow(currentPhotoBook)){
			PhotobookPage titlePage = PhotoBookProductUtil.getTitlePage(currentPhotoBook);
			if(titlePage != null && (titlePage.isWantMainRefresh()|| titlePage.isWantThumbRefresh())){
				currentPhotoBook.pages.get(0).setPageRefresh();
			}
		}
		simplexPages  = PhotoBookProductUtil.getSimplexPage(currentPhotoBook.pages);
		currentPhotoBook.chosenLayers = PhotoBookProductUtil.getChosenServerPhotoList(simplexPages, currentPhotoBook.chosenpics,currentPhotoBook.chosenLayers);			
		maxNumberOfImages = PhotoBookProductUtil.getPageMaxNumInPhotoBook(currentPhotoBook.pages);
		
		pagesAdapter.refresh();
		mainAdapter.notifyDataSetChanged();
		picturesAdapter.refresh();		
		rearrangeSimplexAdapter.notifyDataSetChanged();		
	}

	public void showProjectName(String name){
		projectName = name;
		projectNameView.setText(getString(R.string.Common_ProjectName) + ": " + projectName);
		projectNameView.setVisibility(View.VISIBLE);
	}
	
	public void addLayerLocalInfo(Layer layer){
		RssTabletApp.getInstance().getProductLayerLocalInfos().put(layer,isFromMyProject);
	}
		
}
