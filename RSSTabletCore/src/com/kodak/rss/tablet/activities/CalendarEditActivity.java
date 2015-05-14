package com.kodak.rss.tablet.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarLayer;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.core.util.StringUtils;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.CalendarPageAdapter;
import com.kodak.rss.tablet.adapter.CalendarPagesAdapter;
import com.kodak.rss.tablet.adapter.CalendarProductAdapter;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.CalendarCommonHandler;
import com.kodak.rss.tablet.handler.CalendarEditTaskHandler;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.thread.calendar.AddDateTextToPageTask;
import com.kodak.rss.tablet.thread.calendar.AddPageTextTask;
import com.kodak.rss.tablet.thread.calendar.CalendarEditTask;
import com.kodak.rss.tablet.thread.calendar.CalendarSetDateTaskGroup;
import com.kodak.rss.tablet.thread.calendar.MoveImagesInDiffPagesTask;
import com.kodak.rss.tablet.thread.calendar.RemoveImageInPageTask;
import com.kodak.rss.tablet.thread.calendar.ShuffleContentInPageTask;
import com.kodak.rss.tablet.thread.calendar.SkipShoppingCartTask;
import com.kodak.rss.tablet.thread.calendar.SwapContentsInCalendarPageTask;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.ProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.view.AddImagesLayout;
import com.kodak.rss.tablet.view.AniamtionDragHelper;
import com.kodak.rss.tablet.view.CalendarDateTextView;
import com.kodak.rss.tablet.view.CalendarEditLayer;
import com.kodak.rss.tablet.view.CalendarEditPopView;
import com.kodak.rss.tablet.view.CalendarMainPageView;
import com.kodak.rss.tablet.view.CalendarMainView;
import com.kodak.rss.tablet.view.EditImageView;
import com.kodak.rss.tablet.view.GCDragPopGridView;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.MainPageView.OnLayerDragListener;
import com.kodak.rss.tablet.view.PhotoBookEditPopView;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.rss.tablet.view.SearchButton;
import com.kodak.rss.tablet.view.SourcePanel;
import com.kodak.rss.tablet.view.TextFontView;
import com.kodak.rss.tablet.view.dialog.DialogSaveProject;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class CalendarEditActivity extends BaseHaveISMActivity implements OnClickListener{	
	private static final String TAG = "CalendarEditActivity";
	private Calendar currentCalendar;	
	private Button pagesButton;
	private Button sourceButton;
	private float wHRatio;
	private CalendarProductAdapter calendarProductAdapter;
	private GridView pagesGrid;
    public ViewGroup vgEditTools;
    private int statusBarHeight;
    private SourcePanel panel;
    private SearchButton searchButton;
    private View pageTextButton;
    private View shuffleButton;
    private View pageTopButton;
    private View pageBottomButton;
    public AddImagesLayout addImagesView;	
    private RelativeLayout animLayer;
    private CalendarEditTaskHandler editTaskHandler;
    public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
    private String calendarId;
    private ImageView deleteImageView;
    private AniamtionDragHelper animDragHelper;     
    private View photoToolView;
    public CalendarSetDateTaskGroup dateTaskGroup;
    private CalendarCommonHandler mCommonHandler;
    private CalendarDateTextView dateTextView;
    private TextFontView<Page, Layer> pageTextView;  
    public CalendarEditLayer calendarEditLayer;
    public CalendarMainView calendarMainView;
    public View zoomButton;
    public View calendarZoomInLayer;
    public View exitZoomInButton;   
   
    private TextView projectNameView;	
    public boolean isFromMyProject;
	public String projectName;   
	
	 private HashMap<String, String> attr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_edit_calendar);
		
		initView();
		initData();				
					
		PictureUploadService.flowType = AppConstants.calendarType;		
	}	
		
	public void initView(){		
		flowType = AppConstants.calendarType;	
		super.initView();
		
		calendarMainView = (CalendarMainView) findViewById(R.id.calendar_main_view);
		findViewById(R.id.cart_button).setOnClickListener(this);
		findViewById(R.id.save_button).setOnClickListener(this);
		
		projectNameView = (TextView) findViewById(R.id.project_name);		
		
		vgEditTools = (ViewGroup) findViewById(R.id.edit_tools);
		pageTextButton = findViewById(R.id.btn_pagetext);
		shuffleButton = findViewById(R.id.btn_shuffle);
		pageTopButton = findViewById(R.id.btn_page_top);
		pageBottomButton = findViewById(R.id.btn_page_bottom);
		zoomButton = findViewById(R.id.btn_zoom);
		exitZoomInButton = findViewById(R.id.btn_exit_zoom_in);
		calendarZoomInLayer = findViewById(R.id.calendar_zoom_in_layer);
		calendarEditLayer = (CalendarEditLayer) findViewById(R.id.edit_layer);
		
		pagesButton = (Button) findViewById(R.id.pages_button);
		sourceButton = (Button) findViewById(R.id.source_button);
		
		pagesGrid = (GridView) findViewById(R.id.pagesGrid);
		pagesGrid.setNumColumns(7);
		pagesGrid.setVerticalSpacing((int)dm.density*10);
		pagesGrid.setHorizontalSpacing((int)dm.density*10);
		
		statusBarHeight = app.statusBarHeight;
		panel = (SourcePanel) findViewById(R.id.bottomPanel);
		panel.initPanelContentHeight(50 +statusBarHeight);
				
		searchButton = (SearchButton) findViewById(R.id.search_button);
		searchButton.setSourcePanel(panel);		
		
		int orgHeight = dm.heightPixels/3;		
		panel.setOpenContentHeight(orgHeight);
		
		addImagesView = (AddImagesLayout) findViewById(R.id.add_image_view);	
		int addWidth = (int) (dm.widthPixels/7 -dm.density*10);	
		RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(addWidth, addWidth);
		rLP.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		addImagesView.setLayoutParams(rLP);							
		animLayer = (RelativeLayout) findViewById(R.id.anim_layer);	
		dateTextView = (CalendarDateTextView) findViewById(R.id.date_font_view);
		pageTextView = (TextFontView) findViewById(R.id.edit_font_view);
		deleteImageView = (ImageView) findViewById(R.id.delete_image);
		deleteImageView.setOnClickListener(this);
		
		photoToolView = findViewById(R.id.photos_tool);
		
		animDragHelper = new AniamtionDragHelper(CalendarEditActivity.this);
		animDragHelper.setAnimParentView(animLayer, calendarMainView);
		animDragHelper.setAnimationScaleHalf(true);
		
		if (getIntent() != null) {
			calendarId = getIntent().getStringExtra(AppConstants.calendarId);			
		}	
		
		if (calendarId != null && app.calendarList != null) {
			for (Calendar calendar : app.calendarList) {
				if (calendar != null) {
					if (calendar.id.equals(calendarId)) {
						calendar.isCurrentChosen = true;
						currentCalendar = calendar;
					}else {
						calendar.isCurrentChosen = false;
					}
				}
			}
		}
	}
	
	public void initData(){
		super.initData();	
		editTaskHandler = new CalendarEditTaskHandler(this);
		currentCalendar = CalendarUtil.getCurrentCalendar();	
		mCommonHandler = new CalendarCommonHandler(this);
		dateTaskGroup = new CalendarSetDateTaskGroup(this, mCommonHandler);

		if (getIntent() != null && getIntent().hasExtra(AppConstants.isFromMyProject)) {				
			isFromMyProject = getIntent().getBooleanExtra(AppConstants.isFromMyProject, false);
			if(isFromMyProject){
				projectName = getIntent().getStringExtra(AppConstants.projectName);
				currentCalendar.projectName = projectName;
			}
		}else if(!StringUtils.isEmpty(currentCalendar.projectName)){
			projectName = currentCalendar.projectName;
		}
		showProjectName(projectName);
		
		initAdapter();
	}	

	public void initAdapter(){
		wHRatio = PhotoBookProductUtil.getWHRation(currentCalendar.proDescId);	
		int downW = (int) ((dm.widthPixels - dm.density*70)*3/7f);						
		int downH = (int) (downW*wHRatio);
		calendarMainView.setCalendar(currentCalendar,downW,downH,mMemoryCache,pendingRequests);
		
		switch (currentCalendar.getCalendarType()) {
		case Calendar.Annual_Calendars:
			vgEditTools.setVisibility(View.INVISIBLE);
			break;
		case Calendar.Monthly_Simplex:
			vgEditTools.setVisibility(View.VISIBLE);
			pageTextButton.setVisibility(View.VISIBLE);
			shuffleButton.setVisibility(View.INVISIBLE);
			pageTopButton.setVisibility(View.INVISIBLE);
			pageBottomButton.setVisibility(View.INVISIBLE);
			calendarProductAdapter = new CalendarPageAdapter(CalendarEditActivity.this, wHRatio, mMemoryCache,pendingRequests);
			break;
		case Calendar.Monthly_Duplex:
			vgEditTools.setVisibility(View.VISIBLE);
			calendarProductAdapter = new CalendarPagesAdapter(CalendarEditActivity.this, wHRatio, mMemoryCache,pendingRequests);
			break;
		}

		if (CalendarUtil.isDisplayPages()) {	
			RSSLocalytics.recordLocalyticsPageView(this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_PAGES_IMAGE_SELECTION);
			calendarProductAdapter.setViewSynLoadBitmap(calendarMainView);
			calendarProductAdapter.selectedPostions = 0;
			pagesGrid.setAdapter(calendarProductAdapter);
			setButtonDisplay(pagesButton, true);
			pagesGrid.setVisibility(View.VISIBLE);
			photoToolView.setVisibility(View.GONE);
			photoGridView.setVisibility(View.GONE);
			pagesButton.setOnClickListener(this);
			sourceButton.setOnClickListener(this);			
			setEditButtonEable(0);
		}else {			
			pagesButton.setVisibility(View.GONE);
			pagesGrid.setVisibility(View.GONE);
			photoGridView.setVisibility(View.VISIBLE);
			photoToolView.setVisibility(View.VISIBLE);
		}

		setButtonDisplay(sourceButton, false);		
		((GCDragPopGridView)photoGridView).setAnimLayout(animLayer,calendarMainView);	
		((GCDragPopGridView)photoGridView).setAnimationScaleHalf(true);
		initAction();				
	}	
	
	private OnLayerClickListener<CalendarMainPageView, CalendarPage, CalendarLayer> onLayerClickListener = 
		new OnLayerClickListener<CalendarMainPageView, CalendarPage, CalendarLayer>() {

				@Override
				public void onLayerClick(final CalendarMainPageView pageView,final CalendarPage page, final CalendarLayer layer, final RectF layerRect) {				
					if (layer == null) return;
					if (layer.contentId == null) return;
					if ("".equals(layer.contentId)) return;
					Log.i(TAG, "Layer:" + layer.toString() + "  onclick");
					if (Layer.TYPE_IMAGE.equals(layer.type) || (Layer.TYPE_TEXT_BLOCK.equals(layer.type) && !CalendarUtil.isDaysGridPage(page))) {
						addLayerLocalInfo(layer);
						calendarMainView.enterLayerEditMode(pageView, layer);
						calendarEditLayer.showEditImageAndPop(pageView, layer);
					} else if(Layer.TYPE_TEXT_BLOCK.equals(layer.type) && CalendarUtil.isDaysGridPage(page)){
						 CalendarLayer gridLayer = CalendarUtil.getDaysGridLayer(page);
						if (gridLayer != null && gridLayer.sublayers != null && gridLayer.sublayers.length > 0) {							
							showEditDateView(page, gridLayer);
						}
					}
				}
			};
	
	private int deleteViewPositionX,deleteViewPositionY,deleteViewWidth,deleteViewHeight;
	private int pageGridPositionY;
	private OnLayerDragListener<CalendarMainPageView, CalendarPage, CalendarLayer> onLayerDragListener = 
			new OnLayerDragListener<CalendarMainPageView, CalendarPage, CalendarLayer>() {

				@Override
				public void onStartDrag(MotionEvent event,CalendarMainPageView pageView, CalendarPage page,CalendarLayer layer, Bitmap bitmap) {					
					if (page == null) return;
					if (layer == null) return;	
					if (layer.contentId == null) return;
					if (layer.type == null) return;
					if (CalendarLayer.TYPE_CALENDAR_GRID.equals(layer.type)) return;	
					if ("".equals(layer.contentId)) return;						
					if (CalendarUtil.isDaysGridPage(page) && CalendarLayer.TYPE_TEXT_BLOCK.equals(layer.type))return;	

					float rawX = event.getRawX();
					float rawY = event.getRawY();

					RectF rect = pageView.getLayerRect(layer);
					if (rect == null) return;
				
					int w = (int) (rect.right-rect.left);
					int h = (int) (rect.bottom-rect.top);
					animDragHelper.setWH(w,h);

					if (bitmap == null || bitmap.isRecycled() || page.isWantMainRefresh()) {
						String layerImageInfoPath = CalendarUtil.getLayerImageInfoPath(layer);
						bitmap = animDragHelper.getBitmap(layerImageInfoPath);						
					}					
					animDragHelper.createDragImage(bitmap, rawX, rawY);	
					
					int[] location = new int[2];
					deleteImageView.getLocationOnScreen(location);									
					deleteViewPositionX = location[0];
					deleteViewPositionY = location[1];
					deleteViewWidth = deleteImageView.getWidth();
					deleteViewHeight = deleteImageView.getHeight();
					
					if (CalendarUtil.isDuplex() && pagesGrid.getVisibility() == View.VISIBLE) {
						pagesGrid.getLocationOnScreen(location);	
						pageGridPositionY = location[1];
					}
				}

				@Override
				public void onDragging(MotionEvent event,CalendarMainPageView pageView, CalendarPage page,CalendarLayer layer, Bitmap bitmap) {					
					float rawX = event.getRawX();
					float rawY = event.getRawY();	

					if ((deleteViewPositionX-dm.density*30 <= rawX && rawX <= deleteViewPositionX+deleteViewWidth+dm.density*30) &&
							(deleteViewPositionY-dm.density*30 <= rawY && rawY <= deleteViewPositionY+deleteViewHeight+dm.density*30)) {						
						animDragHelper.onDrag(rawX, rawY,1);							
						int left = deleteViewPositionX - deleteViewWidth/2;
						int top = deleteViewPositionY - deleteViewHeight/2;
						int width = deleteViewWidth*2;
						int height = deleteViewHeight*2;
						animDragHelper.addTempImageView(left, top, width, height, R.drawable.trash_yellow);						
					}else {
						animDragHelper.deleteTempImageView();
						if (CalendarUtil.isDuplex() && pagesGrid.getVisibility() == View.VISIBLE && rawY > pageGridPositionY){
							int[] location = new int[2];
							pagesGrid.getLocationOnScreen(location);
							int position = pagesGrid.pointToPosition((int)(rawX-location[0]), (int)(rawY-location[1]));													
							if (position == AdapterView.INVALID_POSITION){
								animDragHelper.onDrag(rawX, rawY);	
							}else {
								animDragHelper.onDrag(rawX, rawY,position);	
								calendarProductAdapter.selectedPostions = position;						
								calendarProductAdapter.notifyDataSetChanged();									
							}
						}else {
							final Object[] result = calendarMainView.pointToPosition(rawX,rawY);
							if (result == null) {
								animDragHelper.onDrag(rawX, rawY);	
							}else {
								CalendarLayer toLayer = (CalendarLayer) result[2];							
								if (layer != null && layer.contentId != null && toLayer != null && toLayer.contentId != null && !layer.contentId.equals(toLayer.contentId)) {
									animDragHelper.onDrag(rawX, rawY, 1);	
								}else {
									animDragHelper.onDrag(rawX, rawY);	
								}
							}
							showFrame(result, !CalendarUtil.isDayGridCellLayer(layer));
						}						
					}									
				}

				@Override
				public void OnDrop(MotionEvent event,CalendarMainPageView pageView, CalendarPage page,CalendarLayer layer, Bitmap bitmap) {
					float rawX = event.getRawX();
					float rawY = event.getRawY();			
					animDragHelper.onStopDrag(rawX, rawY);	
					animDragHelper.deleteTempImageView();
					calendarMainView.hideAllFrames();
					if (CalendarUtil.isDuplex() && pagesGrid.getVisibility()== View.VISIBLE) {
						if (calendarProductAdapter.selectedPostions != calendarMainView.getPosition()) {
							calendarProductAdapter.selectedPostions = calendarMainView.getPosition();
						}						
					}	
					if (page == null) return;
					if (layer == null) return;
					if (layer.type == null) return;
					if (CalendarLayer.TYPE_CALENDAR_GRID.equals(layer.type)) return;
					if (CalendarUtil.isDaysGridPage(page) && CalendarLayer.TYPE_TEXT_BLOCK.equals(layer.type))return;
					String 	pageId = page.id;
					String 	contentId = layer.contentId;
					if (contentId == null || contentId.equals("")) return;	
					if ((deleteViewPositionX-dm.density*30 <= rawX && rawX <= deleteViewPositionX+deleteViewWidth+dm.density*30) &&
							(deleteViewPositionY-dm.density*30 <= rawY && rawY <= deleteViewPositionY+deleteViewHeight+dm.density*30)) {
						RemoveImageInPageTask removeTask = new RemoveImageInPageTask(CalendarEditActivity.this, pageId, contentId);
						removeTask.execute();
					}else {					
						if (CalendarUtil.isDisplayPages() && pagesGrid.getVisibility() == View.VISIBLE && rawY > pageGridPositionY){																									
							int[] location = new int[2];
							pagesGrid.getLocationOnScreen(location);
							int position = pagesGrid.pointToPosition((int)(rawX-location[0]), (int)(rawY-location[1]));						
							if (position == AdapterView.INVALID_POSITION)return;
							CalendarPage toPage = null;
							if (calendarProductAdapter instanceof CalendarPagesAdapter) {
								if (((CalendarPagesAdapter)calendarProductAdapter).pageItems == null) return;
								CalendarPage[] toPages = ((CalendarPagesAdapter)calendarProductAdapter).pageItems.get(position);
								toPage = toPages[0] == null ? toPages[1] : toPages[0];
							}else if (calendarProductAdapter instanceof CalendarPageAdapter) {
								if (((CalendarPageAdapter)calendarProductAdapter).pageItems == null) return;
								toPage = ((CalendarPageAdapter)calendarProductAdapter).pageItems.get(position);								
							}
							if (toPage == null) return;
							if (toPage.id.equals(pageId)) return;
							if (!CalendarUtil.isEditable(toPage)) return;	
							if (CalendarUtil.isEditableCoverPage(toPage) && layer.type.equals(CalendarLayer.TYPE_TEXT_BLOCK)) return;	
							
							if (CalendarUtil.isDuplex() && !CalendarUtil.isEditableCoverPage(toPage) && !CalendarUtil.isCanAddImageForPage(toPage)) {
								InfoDialog infoDialog = new InfoDialog.Builder(CalendarEditActivity.this).setMessage(R.string.ComposeCalendar_TooManyImages)
								.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();										
									}
								}).create();
								infoDialog.show();
								return;
							}
							if (CalendarUtil.isDuplex() && !CalendarUtil.isEditableCoverPage(toPage)){
								CalendarUtil.addImageToCalendarPage(toPage);	
							}							
							MoveImagesInDiffPagesTask moveTask = new MoveImagesInDiffPagesTask(CalendarEditActivity.this, page, toPage, contentId,null,null);
							moveTask.execute();																											
						}else {
							final Object[] result = calendarMainView.pointToPosition(rawX,rawY);
							if (result == null) return;
							CalendarPage toPage = (CalendarPage) result[1];
							CalendarLayer toLayer = (CalendarLayer) result[2];
							CalendarGridItemPO gridPo = (CalendarGridItemPO) result[3];	
							if (toPage == null) return;
							if (toPage.id == null) return;	
					
							if (toPage.id.equals(pageId)) {
								if (!CalendarUtil.isDaysGridPage(toPage)) {
									if (toLayer == null) return;
									if (toLayer.contentId == null) return;
									if (toLayer.type == null) return;
									if (CalendarLayer.TYPE_CALENDAR_GRID.endsWith(toLayer.type)) return;
									if (!"".equals(toLayer.contentId)) {
										if (!contentId.equals(toLayer.contentId)) {
											SwapContentsInCalendarPageTask swapTask = new SwapContentsInCalendarPageTask(CalendarEditActivity.this, pageId, toLayer.contentId, contentId);
											swapTask.execute();
										}
										return;
									}
								}else {	
									if (toLayer != null && toLayer.type != null && !toLayer.type.equals(CalendarLayer.TYPE_TEXT_BLOCK) && CalendarUtil.getCellIndexInLayer(toLayer) != -1 && !"".equals(toLayer.contentId) ) {
										if (!contentId.equals(toLayer.contentId)) {
											SwapContentsInCalendarPageTask swapTask = new SwapContentsInCalendarPageTask(CalendarEditActivity.this, pageId, toLayer.contentId, contentId);
											swapTask.execute();
										}										
										return;
									}								
								}								
							}
							
							if (gridPo != null && CalendarLayer.TYPE_TEXT_BLOCK.equals(layer.type)) return;
							if (gridPo != null && !CalendarUtil.isDaysGridPage(toPage)) return;
							
							//added by bing for RSSMOBILEPDC-2044 on 2014-11-17
							if (CalendarUtil.isDuplex() && gridPo == null && CalendarUtil.isDaysGridPage(toPage)) return;
													
							if (CalendarUtil.isDuplex() && !CalendarUtil.isEditableCoverPage(toPage) && !CalendarUtil.isDaysGridPage(toPage) && !CalendarUtil.isCanAddImageForPage(toPage)) {
								InfoDialog infoDialog = new InfoDialog.Builder(CalendarEditActivity.this).setMessage(R.string.ComposeCalendar_TooManyImages)
								.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();										
									}
								}).create();
								infoDialog.show();
								return;
							}
							if (CalendarUtil.isDuplex()&& gridPo == null && !CalendarUtil.isEditableCoverPage(toPage) ) {
								CalendarUtil.addImageToCalendarPage(toPage);	
							}								
							MoveImagesInDiffPagesTask moveTask = new MoveImagesInDiffPagesTask(CalendarEditActivity.this, page, toPage, contentId, gridPo,toLayer);								
							moveTask.execute();																								
						}										
					}								
				}
			};
			
	private OnEditItemClickListener<CalendarEditPopView, CalendarPage, CalendarLayer> onEditItemClickListener = new OnEditItemClickListener<CalendarEditPopView, CalendarPage, CalendarLayer>() {
		
		@Override
		public void onEditItemClick(CalendarEditPopView view, CalendarPage page,CalendarLayer layer, int itemId) {
			Log.i(TAG, layer == null ? "null" : layer.contentId + " : " + itemId);
			if (view.getType() == CalendarEditPopView.TYPE_COLOR_EFFECT) {				
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_EFFECTS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.COLOR_EFFECT, editTaskHandler, page, layer, itemId).start();
			} else {
				switch (itemId) {
				case CalendarEditPopView.LAYER_ENHANCE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_ENHANCE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.ENHANCE, editTaskHandler, page, layer, itemId==PhotoBookEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case CalendarEditPopView.LAYER_UNDO_ENHANCE:
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.ENHANCE, editTaskHandler, page, layer, itemId==PhotoBookEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case CalendarEditPopView.LAYER_RED_EYE:
				case CalendarEditPopView.LAYER_UNDO_RED_EYE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.RED_EYE, editTaskHandler, page, layer, itemId==PhotoBookEditPopView.LAYER_RED_EYE).start();
					break;
				case CalendarEditPopView.LAYER_REMOVE_IMAGE:
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.REMOVE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case CalendarEditPopView.LAYER_DELETE_CAPTION:
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.DELETE_CAPTION, editTaskHandler, page, layer).start();
					break;
				case CalendarEditPopView.LAYER_FLIP_HORIZONTAL:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_FLIP_HORIZONTAL_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer, itemId == CalendarEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case CalendarEditPopView.LAYER_FLIP_VERTICAL:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_FLIP_VERTICAL_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer, itemId == CalendarEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case CalendarEditPopView.PAGE_DELETE_PAGE_TEXT:
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.DELETE_PAGE_TEXT, editTaskHandler, page, layer).start();
					break;
				case CalendarEditPopView.LAYER_ENTER_CAPTION:
				case CalendarEditPopView.LAYER_EDIT_CAPTION:
					calendarEditLayer.setVisibility(View.INVISIBLE);
					showFontEditView(false, page, layer, true);
					break;
				case CalendarEditPopView.PAGE_EDIT_PAGE_TEXT:
					calendarEditLayer.setVisibility(View.INVISIBLE);
					showFontEditView(false, page, layer, false);
					break;
				}
			}
		}
	};
	
	private CalendarEditLayer.OnDoneClickListener onDoneClickListener = new CalendarEditLayer.OnDoneClickListener() {
		
		@Override
		public void OnDoneClick(CalendarEditLayer editLayer,
				CalendarMainPageView pageView, EditImageView ivEdit) {
			if (calendarEditLayer.isEditPage() || !ivEdit.isEdited()) {
				editLayer.dismiss();
				calendarMainView.exitEditMode();
			} else {
				//get old roi and angle
				CalendarLayer layer = (CalendarLayer) ivEdit.getLayer();
				ROI oldRoi = ProductUtil.getImageCropROI(layer);
				
				//get current roi
				ROI newRoi = ivEdit.getImageROI();
				
				boolean moved = newRoi != null && !newRoi.equals(oldRoi);
				
				if (moved) {
					new CalendarEditTask(CalendarEditActivity.this, CalendarEditTask.CROP_IMAGE, editTaskHandler, pageView.getPage(), layer, newRoi).start();
				} else {
					editLayer.dismiss();
					calendarMainView.exitEditMode();
				}
			}
		}
	};

	public void initAction(){
		((GCDragPopGridView)photoGridView).setOnDragListener(new AniamtionDragHelper.OnGridDragListener() {			
			@Override
			public void showEdit() {}
			
			@Override
			public void onStartDrag(float rawX, float rawY) {				
				calendarMainView.enterAddResouceMode();
			}
			
			@Override
			public void showFrameForLayer(Object[] result) {	
				showFrame(result, false);
			}
			
			@Override
			public void onDragging(float rawX, float rawY) {}
			
			@Override
			public void onStopDrag(float rawX, float rawY,Object[] result,ImageInfo dragImageInfo,Bitmap dragBitmap) {
				calendarMainView.exitAddResouceMode();
                calendarMainView.hideAllFrames();
				if (result == null) return;				
				if (dragImageInfo == null) return;	
				CalendarPage page = (CalendarPage) result[1];				
				if (!CalendarUtil.isEditable(page)) return;										
				CalendarLayer layer = (CalendarLayer) result[2];
				CalendarGridItemPO gridItemPo = (CalendarGridItemPO) result[3];
				
				String pageId = page.id;									        
				if (currentCalendar == null) {
					currentCalendar = CalendarUtil.getCurrentCalendar();	
				}
				
				ImageInfo info = dragImageInfo;										
				dragImageInfo = null;
				if (!info.isfromNative) {
					String thumbnailPath = FilePathConstant.getLoadFilePath(FilePathConstant.externalType, info.id, true);
					info.editUrl = thumbnailPath;
				}

				if (gridItemPo != null) {
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					CalendarUtil.addImageToCalendar(info);	
					startUploadService();
					addImagesView.addImageToPage(dragBitmap, pageId, info, gridItemPo,-1);
					return;
				}else {
					if (!CalendarUtil.isDuplex() || (CalendarUtil.isDuplex() && CalendarUtil.isEditableCoverPage(page))) {						
						int holeIndex = -1;
						if (layer != null ) {
							if (CalendarUtil.isTypeEqualLayer(layer,CalendarLayer.TYPE_IMAGE)){
								holeIndex = CalendarUtil.getHoleIndexInPage(layer, page.layers);
							}							
						}else {
							holeIndex = CalendarUtil.getFristImageLayerInPage(page);
						}
												
						if (holeIndex >= 0) {
							updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
							CalendarUtil.addImageToCalendar(info);	
							startUploadService();
							addImagesView.addImageToPage(dragBitmap, pageId, info, holeIndex);	
						}																
						return;
					}
					
					if (CalendarUtil.isDuplex()){
						if (!CalendarUtil.isCanAddImageForPage(page)) {
							InfoDialog infoDialog = new InfoDialog.Builder(CalendarEditActivity.this).setMessage(R.string.ComposeCalendar_TooManyImages)
							.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();										
								}
							}).create();
							infoDialog.show();
							return;
						}	
						updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
						CalendarUtil.addImageToCalendar(info);						
						CalendarUtil.addImageToCalendarPage(page);	
						startUploadService();				
						addImagesView.addImageToPage(dragBitmap, pageId, info,-1);					
					}
				}											
			}			
		});			
		
		pagesGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {	
				if (calendarProductAdapter == null) return;
				if (calendarProductAdapter.selectedPostions == position) return;
				setEditButtonEable(position);				
				calendarProductAdapter.selectedPostions = position;						
				calendarProductAdapter.notifyDataSetChanged();				
				calendarMainView.setPosition(position);			
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
									
				addImageToCalendarPageByOnClick(view, imageInfo);				
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
				
				addImageToCalendarPageByOnClick(view, imageInfo);		
			}
			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}
			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}			
		});	

		calendarMainView.setOnLayerDragListener(onLayerDragListener);
		
		calendarMainView.setOnLayerClickListener(onLayerClickListener);
		calendarEditLayer.setOnPopEditItemClickListener(onEditItemClickListener);
		calendarEditLayer.setOnDoneClickListener(onDoneClickListener);
		
		zoomButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				RSSLocalytics.recordLocalyticsPageView(CalendarEditActivity.this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_PREVIEW);
				calendarMainView.zoomIn();
			}
		});
		
		exitZoomInButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calendarMainView.zoomOut();
			}
		});
		
		pageTextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CalendarPage page = calendarMainView.getCurrentFocusedPage();
				if (CalendarUtil.isDaysGridPage(page)) {
					if(CalendarUtil.isDateAdded(page)){
						showEditDateView(page, CalendarUtil.getDaysGridLayer(page));
					} else {
						AddDateTextToPageTask addDateTextToPageTask = new AddDateTextToPageTask(CalendarEditActivity.this, currentCalendar.id, page);
						addDateTextToPageTask.execute();
					}
				} else {
					if(CalendarUtil.canAddContent(page)){
						AddPageTextTask addPageTask = new AddPageTextTask(CalendarEditActivity.this, page.id);
						addPageTask.execute();
					} else {
						InfoDialog infoDialog = new InfoDialog.Builder(CalendarEditActivity.this).setMessage(R.string.ComposeCalendar_TooManyTextBlocks)
						.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
									
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();										
							}
						}).create();
						infoDialog.show();
					}
				}
			}
		});
		
		shuffleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CalendarPage page = calendarMainView.getCurrentFocusedPage();				
				if (page != null && page.id != null && !CalendarUtil.isDaysGridPage(page) && page.layers != null && page.layers.length > 1) {
					ShuffleContentInPageTask shuffleTask = new ShuffleContentInPageTask(CalendarEditActivity.this, page.id);
					shuffleTask.execute();
				}
			}
		});
		
		pageTopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calendarMainView.moveToTop();
			}
		});
		
		pageBottomButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calendarMainView.moveToBottom();
			}
		});
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
	
	@Override
	public void startOver() {
		cancelRequest();
		addImagesView.setTasksNull();
		super.startOver();
	}		
	
	
	@Override
	public void judgeHaveItems(){			
		SkipShoppingCartTask skipTask = new SkipShoppingCartTask(CalendarEditActivity.this);
		skipTask.execute();
	}
	
	private boolean haveBlankPageWarningShown = false;
	public void skipShoppingCart(){
		final Calendar calendar = CalendarUtil.getCurrentCalendar();
		final int index4FirstNotFilledPage = CalendarUtil.findFirstNotFilledFixedPage(calendar);
		if (index4FirstNotFilledPage != -1) {
			new InfoDialog.Builder(CalendarEditActivity.this)
				.setMessage(R.string.ComposeCalendar_MinImagesNotSelected)
				.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int position = CalendarUtil.getPagePositionByPageIndex(calendar, index4FirstNotFilledPage);
						flipTo(position);
					}
				})
				.create()
				.show();
			return;
		}
		
		if (!haveBlankPageWarningShown) {
			final int index4FirstBlankNotFixedPage = CalendarUtil.findFirstBlankNotFixedPage(calendar);
			if (index4FirstBlankNotFixedPage != -1 ) {
				new InfoDialog.Builder(CalendarEditActivity.this)
				.setMessage(R.string.ComposeCalendar_NeedImage)
				.setPositiveButton(R.string.d_ok, null)
				.create()
				.show();
				
				haveBlankPageWarningShown = true;
				return;
			}
		}
		
		synchronized (calendar) {
			if (calendar.pages!= null && calendar.pages.size() != 0 && calendar.pages.get(0) != null && calendar.pages.get(0).isWantMainRefresh()) {
				CalendarPage page = calendar.pages.get(0);
				String dispalyPath =  FilePathConstant.getLoadFilePath(FilePathConstant.calendarType, page.id, false, page.getMainRefreshCount(),page.getMainRefreshSucCount());	
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
				RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_CALENDAR_EDIT_SUMMARY, attr);
			}
		}

		ProductInfo pInfo = CalendarUtil.getPInfo(currentCalendar.id,AppConstants.calendarType);			
		CalendarUtil.dealWithItem(CalendarEditActivity.this,currentCalendar,pInfo == null?1:pInfo.num);
		cancelRequest();	
		Intent mIntent = new Intent(CalendarEditActivity.this, ShoppingCartActivity.class);
		startActivity(mIntent);
		CalendarEditActivity.this.finish();	
		
	}
	
	private void cancelRequest(){	
		if (calendarProductAdapter != null) {
			calendarProductAdapter.cancelRequest();
		}		
		clearDownDataRequest();
		System.gc();		
	}	
	
	public void showEditDateView(CalendarPage page, CalendarLayer layer){
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(this, 47);//45+58+5
		int sideMargin = DimensionUtil.dip2px(this, 0);//0
		if(dateTaskGroup == null){
			dateTaskGroup = new CalendarSetDateTaskGroup(this, mCommonHandler);
		}
		if(dateTaskGroup!=null && !dateTaskGroup.isAlive() && !dateTaskGroup.destroy){
			dateTaskGroup.start();
		}
		dateTextView.setViewSize(point).setCalendarInfo(dateTaskGroup, page, layer)
		            .showTextFontView(false, topMargin, sideMargin);
		updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		calendarMainView.moveToTopLeft(page);
	}
	
	public void showFontEditView(boolean showAtLeft, CalendarPage page, CalendarLayer layer, boolean isCaption){
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(this, 47);//45+58+5
		int sideMargin = DimensionUtil.dip2px(this, 0);//10
		pageTextView.setIsCaption(isCaption).setViewSize(point).setEditPageInfo(page, layer).setTitleVisible(false);
		if(isCaption){
			pageTextView.setFontComponentsVisible(false);
		} else {
			pageTextView.setFontComponentsVisible(true);
		}
		pageTextView.showTextFontView(showAtLeft, topMargin, sideMargin);
		updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		calendarMainView.moveToTopLeft(page);
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		currentCalendar = CalendarUtil.getCurrentCalendar();	
		if(v.getId()==R.id.save_button){		
			boolean isEnd = addImagesView.endTasks();
			if (dateTaskGroup != null) {
				isEnd = isEnd && dateTaskGroup.isAllTasksFinished();
			}
			if (isEnd) {
				showSaveProject();
			}else {
				SkipShoppingCartTask skipTask = new SkipShoppingCartTask(CalendarEditActivity.this,true);
				skipTask.execute();
			}
		}else if(v.getId()==R.id.cart_button) {			
			judgeHaveItems();			
		}else if(v.getId()==R.id.pages_button) {
			RSSLocalytics.recordLocalyticsPageView(CalendarEditActivity.this, RSSTabletLocalytics.LOCALYTICS_PAGE_VIEW_CALENDAR_PAGES_IMAGE_SELECTION);			
			setButtonDisplay(pagesButton, true);
			setButtonDisplay(sourceButton, false);
			photoGridView.setVisibility(View.GONE);
			photoToolView.setVisibility(View.GONE);
			pagesGrid.setVisibility(View.VISIBLE);				
		}else if(v.getId()==R.id.source_button) {			
			setButtonDisplay(pagesButton, false);
			setButtonDisplay(sourceButton, true);
			photoGridView.setVisibility(View.VISIBLE);
			photoToolView.setVisibility(View.VISIBLE);
			pagesGrid.setVisibility(View.GONE);						
		}else if(v.getId()==R.id.delete_image){
			new InfoDialog.Builder(CalendarEditActivity.this).setMessage(R.string.ComposeCalendar_DragImageToRemove)
			.setPositiveButton(getText(R.string.d_ok), null).create()
			.show();
		}		
	}

	public void showSaveProject(){
		HashMap<String,String> map = new HashMap<String, String>();
		map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PROJECT_TYPE, RSSTabletLocalytics.LOCALYTICS_VALUE_PROJECT_SAVE_IN_CALENDAR);		
		RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_PROJECT_SAVE, map);	
		
		DialogSaveProject dialog = new DialogSaveProject(CalendarEditActivity.this,projectName,currentCalendar.id);
		Window window = dialog.getWindow();
		window.setBackgroundDrawable(new ColorDrawable(0));
		dialog.show();	
	}
	
	public void showProjectName(String name){
		if (name == null) return;
		if ("".equals(name)) return;		
		projectName = name;
		projectNameView.setText(getString(R.string.Common_ProjectName) + ": " + projectName);
		projectNameView.setVisibility(View.VISIBLE);
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

	private void setEditButtonEable(int position){		
		boolean isEable = true;
		if (isHiden(position)) {
			isEable = false;
		}		
		pageTextButton.setEnabled(isEable); 
		shuffleButton.setEnabled(isEable);
		pageTopButton.setEnabled(isEable);
		pageBottomButton.setEnabled(isEable);
	}
	
	private boolean isHiden(int position){
		boolean isHiden = false;
		if (calendarProductAdapter == null) return !isHiden;	
		if (position == 0 ) return !isHiden;
		if (position != calendarProductAdapter.itemSize -1) return isHiden;
		if (currentCalendar != null && currentCalendar.pages != null) {
			int size = currentCalendar.pages.size();
			if (size > 0) {
				CalendarPage page = currentCalendar.pages.get(size - 1);
				if ( page != null && page.pageType != null && CalendarPage.TYPE_BACK_COVER.equals(page.pageType) ) {
					return !isHiden;
				}
			}			
		}	
		return isHiden;
	}
	
	private void flipTo(int position) {
		setCPAdapterCurPosition(position);
		calendarMainView.setPosition(position);
	}
	
	public void notifyCalendarPagesChanged(){
		currentCalendar = CalendarUtil.getCurrentCalendar();
		if (calendarProductAdapter != null) {
			calendarProductAdapter.refresh();
		}				
		calendarMainView.refresh(currentCalendar);	
	}
	
	public void addLayerLocalInfo(Layer layer){
		RssTabletApp.getInstance().getProductLayerLocalInfos().putIfNotExist(layer, true);
	}
	
	public void setCPAdapterCurPosition(int position){		
		if (calendarProductAdapter == null) return;
		if (calendarProductAdapter.selectedPostions != position) {
			setEditButtonEable(position);	
			calendarProductAdapter.selectedPostions = position;						
			calendarProductAdapter.notifyDataSetChanged();						
		}
	}
	
	public void synAdapterNotifyDataSet(){		
		if (calendarProductAdapter != null) {
			calendarProductAdapter.notifyDataSetChanged();
		}					
	}
	
	private void addImageToCalendarPageByOnClick(View view,ImageInfo imageInfo){
		if (imageInfo == null) return;
		if (view == null) return;
		CalendarPage calendarPage = calendarMainView.getCurrentFocusedPage();
		if (calendarPage == null) return;						
		if (!CalendarUtil.isEditable(calendarPage)) return;
		
		String promptStr = "";
		if ((CalendarUtil.isDuplex() && CalendarUtil.isDaysGridPage(calendarPage))|| !CalendarUtil.isDisplayPages()) {
			promptStr = getResources().getString(R.string.ComposeCalendar_DragImagesToAdd);
		}		
		if ("".equals(promptStr) && CalendarUtil.isDuplex() && !CalendarUtil.isEditableCoverPage(calendarPage) && !CalendarUtil.isCanAddImageForPage(calendarPage)) {
			promptStr = getResources().getString(R.string.ComposeCalendar_TooManyImages);
		}						
		if (!"".equals(promptStr)) {			
			InfoDialog infoDialog = new InfoDialog.Builder(CalendarEditActivity.this).setMessage(promptStr)
			.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {								
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();										
				}
			}).create();
			infoDialog.show();
			return;
		}			
		
		if (CalendarUtil.isEditableCoverPage(calendarPage) || CalendarUtil.isSimplex()) {
			if (calendarPage.layers == null) return;
			if (calendarPage.layers.length == 0) return;			
			int index = CalendarUtil.getFristImageLayerInPage(calendarPage);
			if (index != -1) {
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				CalendarUtil.addImageToCalendar(imageInfo);	
				startUploadService();										
				addImagesView.addImageToPageUseAnimation(animLayer, view, calendarPage.id, imageInfo, index);				
			}
			return;
		}	
		updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
		CalendarUtil.addImageToCalendar(imageInfo);	
		CalendarUtil.addImageToCalendarPage(calendarPage);	
		startUploadService();								
		addImagesView.addImageToPageUseAnimation(animLayer, view, calendarPage.id, imageInfo,-1);
	}
	
    public boolean isWaitAddImageDone(String pageId){   	
    	return addImagesView.isWaitAddImageDone(pageId);
    }
    
    private void showFrame(Object[] result, boolean swap){
    	if (result == null) {
			calendarMainView.hideAllFrames();
		} else {
			CalendarMainPageView pageView = (CalendarMainPageView) result[0];
			CalendarPage page = (CalendarPage) result[1];
			CalendarLayer layer = (CalendarLayer) result[2];
			CalendarGridItemPO gridPo = (CalendarGridItemPO) result[3];
			
			if (currentCalendar.getCalendarType() == Calendar.Monthly_Duplex) {
				if (pageView != null && page != null) {
					boolean isEditableCoverPage = CalendarUtil.isEditableCoverPage(page);
					boolean isDaysGridPage = CalendarUtil.isDaysGridPage(page);
					if (gridPo != null) {
						pageView.showFrame(gridPo);
					} else if (isDaysGridPage){
						calendarMainView.hideAllFrames();
					} else if (isEditableCoverPage) {
						if (layer != null) {
							pageView.showFrame(layer);
						} else {
							calendarMainView.hideAllFrames();
						}
					} else if (layer != null && swap && !layer.isCalendarDaysGridLayer()) {
						pageView.showFrame(layer);
					} else if (!swap){
						pageView.showFrame();
					} else {
						pageView.hideAllFrames();
					}
				} else {
					calendarMainView.hideAllFrames();
				}
			} else if (currentCalendar.getCalendarType() == Calendar.Annual_Calendars) {
				if (layer != null && !layer.type.equals(CalendarLayer.TYPE_CALENDAR_GRID)) {
					pageView.showFrame(layer);
				} else {
					calendarMainView.hideAllFrames();
				}
			} else if (currentCalendar.getCalendarType() == Calendar.Monthly_Simplex) {
				if (layer != null && !layer.type.equals(CalendarLayer.TYPE_CALENDAR_GRID)) {
					pageView.showFrame(layer);
				} else if (gridPo != null) {
					pageView.showFrame(gridPo);
				} else {
					calendarMainView.hideAllFrames();
				}
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
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_ENHANCE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_FLIP_HORIZONTAL_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_FLIP_VERTICAL_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_EFFECTS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_MAGE_EDITS_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_CALENDAR_BACKGROUND_IMAGE, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
	}  
    
}
