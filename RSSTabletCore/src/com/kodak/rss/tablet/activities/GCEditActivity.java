package com.kodak.rss.tablet.activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SortableHashMap;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.facebook.FbkObject;
import com.kodak.rss.tablet.facebook.FbkPhoto;
import com.kodak.rss.tablet.handler.GCEditTaskHandler;
import com.kodak.rss.tablet.handler.GetFacebookGraphicsHandler.OnGetIamgeOnFacebookListener;
import com.kodak.rss.tablet.handler.GetNativeGraphicsHandler.OnGetImageOnNativeListener;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.thread.GCAddImageToCardTask;
import com.kodak.rss.tablet.thread.GCEditTask;
import com.kodak.rss.tablet.util.GreetingCardUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.AniamtionDragHelper;
import com.kodak.rss.tablet.view.EditImageView;
import com.kodak.rss.tablet.view.GCCartItemView;
import com.kodak.rss.tablet.view.GCDragPopGridView;
import com.kodak.rss.tablet.view.GCEditLayer;
import com.kodak.rss.tablet.view.GCEditPopView;
import com.kodak.rss.tablet.view.GCMainPageView;
import com.kodak.rss.tablet.view.GCMainView;
import com.kodak.rss.tablet.view.MainPageView.OnLayerClickListener;
import com.kodak.rss.tablet.view.PhotoBookEditPopView;
import com.kodak.rss.tablet.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.rss.tablet.view.TextFontView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class GCEditActivity extends BaseHaveISMActivity implements OnClickListener{	
	private static final String TAG = "GCEditActivity";	
		
	private View panelContent;
	private TextView showEditTxt;
	public GCMainView gCMainView;
	public GCEditLayer layerEdit;
	private GCEditTaskHandler editTaskHandler = new GCEditTaskHandler(this);
	private RadioGroup rdgSteps;
	private RadioButton rdoStep1;
	private RadioButton rdoStep2;
	private RadioButton rdoStep3;
	private RadioButton rdoStep4;
	public InfoDialog dialogPleaseWait;

	private String dispalyStr;
	private List<GCCategory> gccList;

	private View photosContainerView;	
	
	private RelativeLayout animLayer;	
	private GCCartItemView cartItemView;
	private String cardId;
	private GreetingCard currentCard; 
	private boolean isShowPreview;
	private TextFontView<GCPage,GCLayer> editFontView;
  
    // for Localytics
    private HashMap<String, String> attr;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);								
		setContentView(R.layout.activity_edit_card);		
		initView();
		initData();					
		
		PictureUploadService.flowType = AppConstants.cardType;
	}	
		
	public void initView(){		
		flowType = AppConstants.cardType;	
		super.initView();	
		gCMainView = (GCMainView) findViewById(R.id.gc_card);
		rdgSteps = (RadioGroup) findViewById(R.id.rdg_steps);
		findViewById(R.id.previous_button).setOnClickListener(this);
		findViewById(R.id.cart_button).setOnClickListener(this);
		findViewById(R.id.search_button).setOnClickListener(this);
		findViewById(R.id.boundary_drag).setOnClickListener(this);
		animLayer = (RelativeLayout) findViewById(R.id.anim_layer);	
		cartItemView = (GCCartItemView) findViewById(R.id.cartItem);	
		
		if (getIntent() != null) {
			cardId = getIntent().getStringExtra(AppConstants.cardId);
			dispalyStr = getIntent().getStringExtra("SSName");
			gccList = (List<GCCategory>) getIntent().getSerializableExtra("gCCategory");
		}	
		app = RssTabletApp.getInstance();		
		if (cardId != null && app.gCardList != null) {
			for (GreetingCard card: app.gCardList) {
				if (card != null ) {
					if (card.id.equals(cardId)) {
						card.isCurrentChosen = true;
						currentCard = card;
					}else {
						card.isCurrentChosen = false;
					}
				}				
			}			
		}
		if (currentCard == null) {
			currentCard = GreetingCardUtil.getCurrentGreetingCard();
		}
	
		final int cardMode = GreetingCardUtil.getGCMode(currentCard);
		setupButtonSteps(cardMode);
		setupGreetingCardView(currentCard);
		
		showEditTxt = (TextView) findViewById(R.id.show_edit);
		showEditTxt.setOnClickListener(this);
		layerEdit = (GCEditLayer) findViewById(R.id.layer_edit);
		layerEdit.setOnPopEditItemClickListener(onEditItemClickListener);
		layerEdit.setOnDoneClickListener(onDoneClickListener);
		
		
		photosContainerView = findViewById(R.id.photos_container);
		
		
		panelContent = findViewById(R.id.panelContent);		
		LayoutParams subParams = (LayoutParams) panelContent.getLayoutParams();
		subParams.height = dm.heightPixels/3;
		panelContent.setLayoutParams(subParams);
		editFontView = (TextFontView) findViewById(R.id.edit_font_view);
		((GCDragPopGridView)photoGridView).setAnimLayout(animLayer,gCMainView);			
	}
	
	public void initData(){
		super.initData();		
		((GCDragPopGridView)photoGridView).setOnDragListener(new AniamtionDragHelper.OnGridDragListener() {			
			@Override
			public void showEdit() {
				GCEditActivity.this.showEdit();				
			}
			
			@Override
			public void onStartDrag(float rawX, float rawY) {}		
			
			@Override
			public void showFrameForLayer(Object[] result) {
				GCMainPageView pageView = (GCMainPageView) result[0];
				GCLayer layer = (GCLayer) result[2];
				pageView.showFrameForLayer(layer);								
			}
			
			@Override
			public void onDragging(float rawX, float rawY) {}
			
			@Override
			public void onStopDrag(float rawX, float rawY, Object[] result,ImageInfo dragImageInfo,Bitmap dragBitmap) {					
				if (result == null) return;
				if (result.length < 2) return;
				if (dragImageInfo == null) return;	
				GCPage page = (GCPage) result[1];
				GCLayer layer = (GCLayer) result[2];
				GreetingCardUtil.addImageToCard(dragImageInfo);			
				String selectImageId = dragImageInfo.id;				
				dragImageInfo = null;				
				String pageId = page.id;
				int holeIndex = page.getHoleIndex(layer);								
		        startUploadService();
		        GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
				if (card == null) return;
				ImageInfo info = null;
				int index = card.chosenpics.size()-1;
				for (int i = index; i >= 0; i--) {
					ImageInfo imageInfo = card.chosenpics.get(i);
					if (imageInfo != null &&  selectImageId.equals(imageInfo.id)) {
						info = imageInfo;
						break;
					}
				}
				if (info == null) return;
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				GCAddImageToCardTask addImageTask = new GCAddImageToCardTask(GCEditActivity.this, pageId, holeIndex, info);
				addImageTask.execute();		        		        
			}			
		});		
		
		nativeGraphicsHandler.setOnGetIamgeOnNativeListener(new OnGetImageOnNativeListener() {

			@Override
			public void onGetImageOnNative(View view,SortableHashMap<Integer, String[]> imageBuckets,int position) {																																					
				new InfoDialog.Builder(GCEditActivity.this).setMessage(R.string.Card_Drag_Photo_Prompt)
				.setPositiveButton(getText(R.string.d_ok), null).create()
				.show();												
			}
			@Override
			public void onGetAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}
			@Override
			public void onDeleteAllImageOnNative(SortableHashMap<Integer, String[]> imageBuckets) {}			
		});
		
		facebookGraphicsHandler.setOnGetIamgeOnFacebookListener(new OnGetIamgeOnFacebookListener() {
			@Override
			public void onGetIamgeOnFacebook(View view,FbkPhoto fbkPhoto,int fbkPhotoPosition) {
				new InfoDialog.Builder(GCEditActivity.this).setMessage(R.string.Card_Drag_Photo_Prompt)
				.setPositiveButton(getText(R.string.d_ok), null).create()
				.show();						
			}
			@Override
			public void onGetAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}
			@Override
			public void onDeleteAllImageOnFacebook(ArrayList<FbkObject> fbkPhotos) {}			
		});	
		ProductInfo pInfo = GreetingCardUtil.getPInfo(currentCard.id,AppConstants.cardType);
		int quantity = RssTabletApp.getInstance().getQuantityIncrement(currentCard.proDescId);
		cartItemView.setControlValue(pInfo,quantity);
				
		initLocalyticsTrackData();
	}	
	
	private void initLocalyticsTrackData() {
		if(attr == null){
			attr = new HashMap<String, String>();
		}
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PIC_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PREVIEW_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_ROTATE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_EFFECT_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PIC_DELETED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
		attr.put(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PIC_REPLACED, RSSTabletLocalytics.LOCALYTICS_VALUE_NO);
	}
	
	private void updateLocalytics(String key, String value){
		if(attr==null){
			initLocalyticsTrackData();
		}
		attr.put(key, value);
	}
	
	private void setupButtonSteps(int cardMode) {
		rdoStep1 = (RadioButton) findViewById(R.id.rdo_step1);
		rdoStep2 = (RadioButton) findViewById(R.id.rdo_step2);
		rdoStep3 = (RadioButton) findViewById(R.id.rdo_step3);
		rdoStep4 = (RadioButton) findViewById(R.id.rdo_step4);
		
		switch (cardMode) {
		case GCMainView.MODE_SINGLE_LANDSCAPE:
			rdoStep1.setBackgroundResource(R.drawable.viewcardflatlandscapefront1);
			rdoStep1.setChecked(true);
			rdoStep1.setVisibility(View.GONE);
			rdoStep2.setVisibility(View.GONE);
			rdoStep3.setVisibility(View.GONE);
			rdoStep4.setVisibility(View.GONE);
			break;
		case GCMainView.MODE_SINGLE_PORTRAIT:
			rdoStep1.setBackgroundResource(R.drawable.viewcardflatportraitfront1);
			rdoStep1.setChecked(true);
			rdoStep1.setVisibility(View.GONE);
			rdoStep2.setVisibility(View.GONE);
			rdoStep3.setVisibility(View.GONE);
			rdoStep4.setVisibility(View.GONE);
			break;
		case GCMainView.MODE_DUPLEX_LANDSCAPE:
			rdoStep1.setBackgroundResource(R.drawable.viewcardflatlandscapefront1);
			rdoStep1.setChecked(true);
			rdoStep2.setBackgroundResource(R.drawable.viewcardflatlandscapeback2);
			rdoStep1.setVisibility(View.VISIBLE);
			rdoStep2.setVisibility(View.VISIBLE);
			rdoStep3.setVisibility(View.GONE);
			rdoStep4.setVisibility(View.GONE);
			break;
		case GCMainView.MODE_DUPLEX_PORTRAIT:
			rdoStep1.setBackgroundResource(R.drawable.viewcardflatportraitfront1);
			rdoStep1.setChecked(true);
			rdoStep2.setBackgroundResource(R.drawable.viewcardflatportraitback2);
			rdoStep1.setVisibility(View.VISIBLE);
			rdoStep2.setVisibility(View.VISIBLE);
			rdoStep3.setVisibility(View.GONE);
			rdoStep4.setVisibility(View.GONE);
			break;
		case GCMainView.MODE_FOLDED_LANDSCAPE:
			rdoStep1.setBackgroundResource(R.drawable.viewcardlandscapefront1);
			rdoStep1.setChecked(true);
			rdoStep2.setBackgroundResource(R.drawable.viewcardlandscapeinsidetop2);
			rdoStep3.setBackgroundResource(R.drawable.viewcardlandscapeinsidebottom3);
			rdoStep4.setBackgroundResource(R.drawable.viewcardlandscapeback4);
			rdoStep1.setVisibility(View.VISIBLE);
			rdoStep2.setVisibility(View.VISIBLE);
			rdoStep3.setVisibility(View.VISIBLE);
			rdoStep4.setVisibility(View.VISIBLE);
			break;
		case GCMainView.MODE_FOLDED_PORTRAIT:
			rdoStep1.setBackgroundResource(R.drawable.viewcardportraitfront1);
			rdoStep1.setChecked(true);
			rdoStep2.setBackgroundResource(R.drawable.viewcardportraitinside2);
			rdoStep3.setBackgroundResource(R.drawable.viewcardportraitback3);
			rdoStep1.setVisibility(View.VISIBLE);
			rdoStep2.setVisibility(View.VISIBLE);
			rdoStep3.setVisibility(View.VISIBLE);
			rdoStep4.setVisibility(View.GONE);
			break;

		default:
			break;
		}
		
		rdgSteps.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rdo_step1) {
					gCMainView.flipTo(1);
				} else if (checkedId == R.id.rdo_step2) {
					gCMainView.flipTo(2);
				} else if (checkedId == R.id.rdo_step3) {
					gCMainView.flipTo(3);
				} else if (checkedId == R.id.rdo_step4) {
					gCMainView.flipTo(4);
				}
			}
		});
	}
	
	private void setupGreetingCardView(GreetingCard card) {
		gCMainView.setGreetingCard(card, true);
		gCMainView.setOnPageLayerClickListener(new OnLayerClickListener<GCMainPageView, GCPage, GCLayer>() {
			
			@Override
			public void onLayerClick(GCMainPageView pageView, GCPage page, GCLayer layer, RectF layerRect) {
				showEdit();
				if(layer.type.equals(Layer.TYPE_IMAGE)){
					if("".equals(layer.contentId)){
						new InfoDialog.Builder(GCEditActivity.this)
						.setCancelable(false)
						.setCanceledOnTouchOutside(false)
						.setMessage(R.string.Card_Drag_Photo_Prompt)
						.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
									
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();	
								}
							})
						.create().show();
					} else {
						layerEdit.showEditImageAndPop(pageView, (GCPage) page, layer, layerRect);
					}
				} else if(layer.type.equals(Layer.TYPE_TEXT_BLOCK)){
					Log.i(TAG, "need show text input dialog");
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_TEXT_ADDED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					showFontEditView(false, (GCPage)page, layer);
				}
			}
		});
	}
	
	
	@Override
	public void startOver() {
		cancelRequest();
		super.startOver();
	}
	
	@Override
	public void previousDoMoreOver() {		
		cancelRequest();
		super.previousDoMoreOver();
	}
	
	@Override
	public void judgeHaveItems(){
		if (GreetingCardUtil.isOkAddPhotosToCardFristPage()) {
			RSSLocalytics.recordLocalyticsEvents(this, RSSTabletLocalytics.LOCALYTICS_EVENT_GC_EDIT_SUMMARY, attr);
			GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
			GreetingCardUtil.deleteUnselectPhoto(card);
			ProductInfo pInfo = GreetingCardUtil.getPInfo(card.id,AppConstants.cardType);			
			GreetingCardUtil.dealWithItem(GCEditActivity.this,card,pInfo == null?1:pInfo.num);
			cancelRequest();
			Intent mIntent = new Intent(GCEditActivity.this, ShoppingCartActivity.class);
			startActivity(mIntent);
			finish();			
		}else {
			new InfoDialog.Builder(GCEditActivity.this).setMessage(R.string.Card_Want_Add_Photo_Prompt)
			.setPositiveButton(getText(R.string.d_ok), null).create()
			.show();	
		}
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
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.previous_button){						
			android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();	
					if (card != null) {							
						GreetingCardUtil.dealWithItem(GCEditActivity.this,card,0);
						app.gCardList.remove(card);
					}				
					if (cardId == null || (cardId != null && "".equals(cardId))) {						
						cancelRequest();
						Intent mIntent = new Intent(GCEditActivity.this, GCCategorySelectActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("SSName", dispalyStr);
						bundle.putSerializable("gCCategory", (Serializable)gccList);
						mIntent.putExtras(bundle);		
						startActivity(mIntent);								
						GCEditActivity.this.finish();
					}else {
						if (app.isUseDoMore) {
							previousDoMoreOver();
						}else {
							startOver();	
						}						
					}			
				}
			};
			new InfoDialog.Builder(this).setMessage(getHomePrompt())
			.setPositiveButton(getText(R.string.d_no), null)
			.setNegativeButton(R.string.d_yes, yesOnClickListener)
			.create()
			.show();			
		}else if(v.getId()==R.id.show_edit){
			if (isShowPreview) {
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PREVIEW_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				showPreview();
			} else {
				showEdit();
			}

		} else if(v.getId()==R.id.cart_button) {
			judgeHaveItems();
		} else if (v.getId() == R.id.search_button || v.getId() == R.id.boundary_drag) {
			if (gCMainView.isZoomIn()) {
				zoomOutCard();
			} else {
				zoomInCard();
			}
		}
		
	}
	
	public void showEdit() {
		if (!isShowPreview) {
			showEditTxt.setText(R.string.card_show_preview);
			gCMainView.showEdit();
			isShowPreview = !isShowPreview;
		}
	}
	
	public void showPreview() {
		if (isShowPreview) {
			showEditTxt.setText(R.string.card_edit);
			gCMainView.showPreview();
			isShowPreview = !isShowPreview;
		}
	}
	
	public void notifyGCPagesChanged(String pageId){
		gCMainView.reDownloadAndRefreshCardimage(pageId);
	}

	public void addLayerLocalInfo(Layer layer) {
		RssTabletApp.getInstance().getProductLayerLocalInfos().put(layer,false);
	}
	
	private OnEditItemClickListener<GCEditPopView, GCPage, GCLayer> onEditItemClickListener = new OnEditItemClickListener<GCEditPopView, GCPage, GCLayer>() {
		
		@Override
		public void onEditItemClick(GCEditPopView view, final GCPage page,final GCLayer layer, int itemId) {
			Log.i(TAG,"CLICK ON " + itemId);
			GreetingCard card = GreetingCardUtil.getCurrentGreetingCard();
			//add layer local info if not added 
			if(layer != null){
				RssTabletApp.getInstance().getProductLayerLocalInfos().putIfNotExist(layer, false);
			}
			
			if(view.getType() != PhotoBookEditPopView.TYPE_COLOR_EFFECT){
				switch (itemId) {
				case GCEditPopView.LAYER_ROTATE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_ROTATE_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new GCEditTask(GCEditActivity.this, GCEditTask.ROTATE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case GCEditPopView.LAYER_FLIP_VERTICAL:
				case GCEditPopView.LAYER_FLIP_HORIZONTAL:
					new GCEditTask(GCEditActivity.this, GCEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case GCEditPopView.LAYER_ENHANCE:
				case GCEditPopView.LAYER_UNDO_ENHANCE:
					new GCEditTask(GCEditActivity.this, GCEditTask.ENHANCE, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case GCEditPopView.LAYER_RED_EYE:
				case GCEditPopView.LAYER_UNDO_RED_EYE:
					new GCEditTask(GCEditActivity.this, GCEditTask.RED_EYE, editTaskHandler, page, layer,itemId==PhotoBookEditPopView.LAYER_RED_EYE).start();
					break;
				case GCEditPopView.LAYER_REMOVE_IMAGE:
					updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_PIC_DELETED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
					new GCEditTask(GCEditActivity.this, GCEditTask.REMOVE_IMAGE, editTaskHandler, page, layer).start();
					break;
				}
			}else{
				updateLocalytics(RSSTabletLocalytics.LOCALYTICS_KEY_GC_EFFECT_USED, RSSTabletLocalytics.LOCALYTICS_VALUE_YES);
				new GCEditTask(GCEditActivity.this, GCEditTask.COLOR_EFFECT, editTaskHandler, page, layer,itemId).start();
			}
		}
	};
	
	private GCEditLayer.OnDoneClickListener onDoneClickListener = new GCEditLayer.OnDoneClickListener() {
		
		@Override
		public void OnDoneClick(GCEditLayer editLayer, 	GCMainPageView pageView, EditImageView ivEdit) {
			if(!ivEdit.isEdited()){
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
					new GCEditTask(GCEditActivity.this, GCEditTask.CROP_IMAGE, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newRoi).start();
				}else if(!moved && rotated){
					new GCEditTask(GCEditActivity.this, GCEditTask.ROTATE_CONTENT, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newAngle).start();
				}else if(moved && rotated){
					new GCEditTask(GCEditActivity.this, GCEditTask.CROP_AND_ROTATE, editTaskHandler, pageView.getPage(), ivEdit.getLayer(),newRoi,newAngle).start();
				}else{
					layerEdit.dismiss();
				}
				
			}
			
		}
	};
	
	public void moveCardToLeftTop() {
		if (gCMainView.isZoomIn()) {
			LayoutParams subParams = (LayoutParams) panelContent.getLayoutParams();
			subParams.height = dm.heightPixels/3;
			panelContent.setLayoutParams(subParams);
			gCMainView.zoomOutAndMoveToLeftTop();
		} else {
			gCMainView.moveToLeftTop();
		}
		
	}
	
	public void zoomInCard() {		
		TranslateAnimation ta = new TranslateAnimation(0, 0,0, dm.heightPixels/3);
		long duration = 300;
		ta.setDuration(duration);
		ta.setFillAfter(true);
		ta.setAnimationListener(new AnimationListener() {			
			@Override
			public void onAnimationStart(Animation animation) {}			
			@Override
			public void onAnimationRepeat(Animation animation) {}			
			@Override
			public void onAnimationEnd(Animation animation) {
				photosContainerView.clearAnimation();
				LayoutParams subParams = (LayoutParams) panelContent.getLayoutParams();
				subParams.height = 0;
				panelContent.setLayoutParams(subParams);
			}
		});	
		photosContainerView.startAnimation(ta);
		gCMainView.zoomIn(duration);
		
	}
	
	public void zoomOutCard() {
		TranslateAnimation ta = new TranslateAnimation(0, 0,dm.heightPixels/3, 0);
		long duration = 300;
		ta.setDuration(duration);
		LayoutParams subParams = (LayoutParams) panelContent.getLayoutParams();
		subParams.height = dm.heightPixels/3;
		panelContent.setLayoutParams(subParams);
		panelContent.requestLayout();
		photosContainerView.startAnimation(ta);
		gCMainView.zoomOut(duration);
		
		
	}
	
	public void changeCardStep(int step) {
		switch (step) {
		case 1:
			rdoStep1.setChecked(true);
			break;
		case 2:
			rdoStep2.setChecked(true);
			break;
		case 3:
			rdoStep3.setChecked(true);
			break;
		case 4:
			rdoStep4.setChecked(true);
			break;
		}
	}
	
	public void showFontEditView(boolean showAtLeft, GCPage page, GCLayer layer){
		moveCardToLeftTop();
		
		Point point = new Point();
		getWindowManager().getDefaultDisplay().getSize(point);
		int topMargin = DimensionUtil.dip2px(GCEditActivity.this, 47);//45+58+5
		int sideMargin = DimensionUtil.dip2px(GCEditActivity.this, 0);//10
		editFontView.setIsCaption(false).setViewSize(point).setEditPageInfo(page, layer).showTextFontView(showAtLeft, topMargin, sideMargin);
	}			
	
	private void cancelRequest(){
		gCMainView.cancelRequest();		
		System.gc();		
	}
	
	
	public void notifyGreetingCardChanged(){
		//If book cover is hollow and title page is changed, we need to refresh cover too.
		
	}
			
}
