package com.kodak.kodak_kioskconnect_n2r.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.R.attr;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.content.Theme;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageEditTask;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageEditTaskHandler;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageEditTestInputDialogWidget;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageEditTestInputDialogWidget.CollageEditTestInputListener;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageFontCreateTask;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageSwapContentAsyncTask;
import com.kodak.kodak_kioskconnect_n2r.collage.LoadColorEffectSourcesTask;
import com.kodak.kodak_kioskconnect_n2r.fragments.AlternateLayoutFragment;
import com.kodak.kodak_kioskconnect_n2r.fragments.CollageThemeFragment;
import com.kodak.kodak_kioskconnect_n2r.fragments.CollageThemeFragment.IOnCollageFragmentListener;
import com.kodak.kodak_kioskconnect_n2r.view.AnimationDragHelper;
import com.kodak.kodak_kioskconnect_n2r.view.CollageEditLayer;
import com.kodak.kodak_kioskconnect_n2r.view.CollageEditPopView;
import com.kodak.kodak_kioskconnect_n2r.view.CollageMainView;
import com.kodak.kodak_kioskconnect_n2r.view.CollagePageView;
import com.kodak.kodak_kioskconnect_n2r.view.EditImageView;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow;
import com.kodak.kodak_kioskconnect_n2r.view.InfoDialogWindow.Builder;
import com.kodak.kodak_kioskconnect_n2r.view.MainPageView.OnLayerClickListener;
import com.kodak.kodak_kioskconnect_n2r.view.MainPageView.OnLayerDragListener;
import com.kodak.kodak_kioskconnect_n2r.view.PhotoBookEditPopView;
import com.kodak.kodak_kioskconnect_n2r.view.ProductEditPopView.OnEditItemClickListener;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;
import com.kodak.utils.ImageResources;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.RSSLocalytics;
import com.kodak.utils.ProductUtil;

public class CollageEditActivity extends BaseActivity implements CollageEditTestInputListener,IOnCollageFragmentListener {
	private static final String TAG = "CollageEditActivity";
	private RelativeLayout bottomLayout, main_navbar;
	private LinearLayout vLinearLayoutLeftPart ;
	private ImageView collage_portrait_btn;
	private ImageView collage_landscape_btn;
	private ImageView collage_add_more_picture_btn;

	private ImageView collage_bigorsmall_btn;
	private ImageView collage_change_background_btn;
	private ImageView collage_change_layout_btn;
	private ImageView collage_change_text_btn;
	private ImageView collage_shuffle_btn;
	private Button btNext;
	private TextView vTextViewTitle;
	private RelativeLayout vRelativeLayoutContainer ;
	private LinearLayout vLayoutContainer ;
	private ImageView vImageTransparent ;
	
	private RelativeLayout collageEditTestInputView;
	private CollageEditTestInputDialogWidget collageEditTestInputDialogWidget;
	private Context context;
	
	private CheckUploadPhotosTask mCheckUploadPhotosTask ;
	
	private List<Theme> collageThemes ;
	private GetThemeTask mGetThemeTask ;
	private CollageWebServices mService ;
	private InfoDialogWindow dialog ;
	
	private int mImageThumbSize;

	private ImageFetcher mImageFetcher;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	
	private FragmentManager fragmentManager ;
	
	private final static int REQUES_ADD_PHOTOS= 1 ;
	
	public CollageMainView mCollageMainView;
	public CollageEditLayer collageEditLayer;
	public CollageEditTaskHandler editTaskHandler;
	private LoadColorEffectSourcesTask loadColorEffectSourcesTask;
	public ImageResources colorEffectResources;
	private ProgressBar myproBar;

	private AnimationDragHelper animDragHelper;
	private RelativeLayout animLayer;
	public View pbWaitEditPage;
	
	private PrintProduct collageProduct = null ;
	private String KEY_Calendar_Text_Added =  "Calendar Text Added";
    public static final String VALUE_YES = "yes";
    public static final String VALUE_NO = "no";
    public static final String CollagePreview="CollagePreview";
    public static HashMap<String, String> attr =new HashMap<String, String>();
    public static final String CollageEditSummary="Collage Edit Summary";
    public static final String CollagePicturesAdded="Collage Pictures Added";
    public static final String CollageShuffleUsed="Collage Shuffle Used";
    public static final String CollageLandscapeUsed="Collage Landscape Used";
    public static final String CollagePortraitUsed="Collage Portrait Used";
    public static final String CollageEffectsUsed="Collage Effects Used";
    public static final String CollageImageEditwUsed="Collage Image Editw Used";
    public static final String CollageBackgroundImage="Collage Background Image";
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.collage_product_field);
		context = CollageEditActivity.this;
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(CollageEditActivity.this, CollagePreview);
		getViews();
		initData();
		setEvents();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUES_ADD_PHOTOS:
			if(resultCode==RESULT_OK){
				 mCheckUploadPhotosTask = new CheckUploadPhotosTask() ;
			     mCheckUploadPhotosTask.execute() ;
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void getViews() {
		vTextViewTitle = (TextView) findViewById(R.id.headerBar_tex);
		vTextViewTitle.setTypeface(PrintHelper.tf);
		collageEditTestInputView = (RelativeLayout) findViewById(R.id.collageEditTestInputDilog);
		bottomLayout = (RelativeLayout) findViewById(R.id.main_bottombar);
		main_navbar = (RelativeLayout) findViewById(R.id.main_navbar);
		bottomLayout.setVisibility(View.GONE);
		vLinearLayoutLeftPart = (LinearLayout) findViewById(R.id.buttonLeftLayout) ;
		collage_portrait_btn = (ImageView) findViewById(R.id.collage_portrait_btn);
		collage_landscape_btn = (ImageView) findViewById(R.id.collage_landscape_btn);
		collage_add_more_picture_btn = (ImageView) findViewById(R.id.collage_add_more_picture_btn);
		collage_bigorsmall_btn = (ImageView) findViewById(R.id.collage_bigorsmall_btn);
		collage_change_background_btn = (ImageView) findViewById(R.id.collage_change_background_btn);
		collage_change_layout_btn = (ImageView) findViewById(R.id.collage_change_layout_btn);
		collage_change_text_btn = (ImageView) findViewById(R.id.collage_change_text_btn);
		collage_shuffle_btn = (ImageView) findViewById(R.id.collage_shuffle_btn);
		btNext = (Button) findViewById(R.id.next_btn);
		btNext.setTypeface(PrintHelper.tf);
		btNext.setText(getString(R.string.cart));
		btNext.setVisibility(View.VISIBLE);	
		
		mCollageMainView=(CollageMainView) findViewById(R.id.collage_editView);
		vRelativeLayoutContainer = (RelativeLayout) findViewById(R.id.relative_theme) ;
		vLayoutContainer = (LinearLayout) findViewById(R.id.layout_theme_container) ;
		vImageTransparent = (ImageView) findViewById(R.id.image_transparent) ;
		collageEditLayer=(CollageEditLayer) findViewById(R.id.collageEditLayer);
		myproBar=(ProgressBar) findViewById(R.id.CollageEditActivity_progressBar);
		
		pbWaitEditPage = findViewById(R.id.waiting_container);
		animLayer = (RelativeLayout) findViewById(R.id.anim_layer);
		animDragHelper = new AnimationDragHelper(this);
		animDragHelper.setAnimParentView(animLayer, mCollageMainView);
		animDragHelper.setAnimationScaleHalf(true);
	}

	@Override
	public void initData() {
		collageEditTestInputDialogWidget =	(CollageEditTestInputDialogWidget) findViewById(R.id.collageEditTestInputDilog);
		mService = new CollageWebServices(this, "");
		Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
		if (currentCollage.page.isPortrait()) {
			collage_portrait_btn.setBackgroundResource(R.drawable.view_portrait_sel);
			collage_landscape_btn.setBackgroundResource(R.drawable.collage_landscape_button);
		}else {
			collage_portrait_btn.setBackgroundResource(R.drawable.collage_portrait_button);
			collage_landscape_btn.setBackgroundResource(R.drawable.view_landscape_sel);
		}
		
		for (PrintProduct product : PrintHelper.products) {
			
			if ( product.getId().equals(CollageManager.getInstance().getCurrentCollage().proDescId)) {
				collageProduct = product;
				 break;
			}
		}
		vTextViewTitle.setText(collageProduct.getName()) ;
		
		
		editTaskHandler = new CollageEditTaskHandler(this);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.imagewait96x96);
		mImageFetcher.addImageCache(this.getSupportFragmentManager(), cacheParams);
        boolean fromShoppingCard =  getIntent().getBooleanExtra(AppConstants.FROM_SHOPPINGCART, false);
        if (fromShoppingCard){
			LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
			loadCollageBitmap.execute() ;
        }else {
        	  mCheckUploadPhotosTask = new CheckUploadPhotosTask() ;
              mCheckUploadPhotosTask.execute() ;
        }  
        mGetThemeTask = new GetThemeTask() ;
        mGetThemeTask.execute() ;
        colorEffectResources = new ImageResources();
        if (loadColorEffectSourcesTask == null) {
			loadColorEffectSourcesTask = new LoadColorEffectSourcesTask(AppContext.getApplication().getColorEffects());
		}
        addAttr();
	}

	@Override
	public void setEvents() {
		collage_portrait_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(!CollageManager.getInstance().getCurrentCollage().page.isPortrait()){
					collage_portrait_btn.setBackgroundResource(R.drawable.view_portrait_sel);
					collage_landscape_btn.setBackgroundResource(R.drawable.collage_landscape_button);
	                RotateCollageTask rotateCollageTask = new RotateCollageTask(true) ;
					rotateCollageTask.execute();
					attr.put(CollagePortraitUsed,VALUE_YES);
				}
			}
		});
		collage_landscape_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(CollageManager.getInstance().getCurrentCollage().page.isPortrait()){
					collage_portrait_btn.setBackgroundResource(R.drawable.collage_portrait_button);
					collage_landscape_btn.setBackgroundResource(R.drawable.view_landscape_sel);
					RotateCollageTask rotateCollageTask = new RotateCollageTask(false) ;
					rotateCollageTask.execute();
					attr.put(CollageLandscapeUsed,VALUE_YES);
				}
			}
		});

		collage_add_more_picture_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if (CollageManager.getInstance().isPhotosNumberMaximum(CollageManager.getInstance().getCurrentCollage())){
					collageMaximumDialogShow(getString(R.string.ComposeCollage_TooManyImages));
				}else{
					Intent intent = new Intent (CollageEditActivity.this ,PhotoSelectMainFragmentActivity.class) ;
		            intent.putExtra(AppConstants.KEY_PRODUCT_ID,CollageManager.getInstance().getCurrentCollage().id) ;
		            intent.putExtra(AppConstants.KEY_PRODUCT_DECID, CollageManager.getInstance().getCurrentCollage().proDescId);
		            intent.putExtra(AppConstants.KEY_FOR_ADD_PICTURE, true) ;
		            startActivityForResult(intent, REQUES_ADD_PHOTOS) ;
		            attr.put(CollagePicturesAdded,VALUE_YES);
				}
				
				
              
			}
		});

		collage_change_background_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				vRelativeLayoutContainer.setVisibility(View.VISIBLE) ;
				vLinearLayoutLeftPart.setVisibility(View.INVISIBLE) ;
                if(fragmentManager==null){
                   fragmentManager = getSupportFragmentManager();
                }
                Fragment fragment = null ;
                CollageThemeFragment themeFragment  = null ;
                fragment =  fragmentManager.findFragmentByTag(CollageThemeFragment.class.getSimpleName()) ;
                if(fragment instanceof CollageThemeFragment){
                	themeFragment = (CollageThemeFragment) fragment ;
                }else {
                	themeFragment  = new CollageThemeFragment() ;
                }
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.add(R.id.layout_theme_container, themeFragment,themeFragment.getClass().getSimpleName()) ;
			    fragmentTransaction.commit() ;
			    btNext.setVisibility(View.INVISIBLE);
			    Animation animRightIn = AnimationUtils.loadAnimation(CollageEditActivity.this, R.anim.right_in );
			    vLayoutContainer.setAnimation(animRightIn);
				animRightIn.start();
				mCollageMainView.animateToLeft(null);
				attr.put(CollageBackgroundImage,VALUE_YES);
				
			}
		});

		collage_change_layout_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				vRelativeLayoutContainer.setVisibility(View.VISIBLE) ;
				vLinearLayoutLeftPart.setVisibility(View.INVISIBLE) ;
                if(fragmentManager==null){
                   fragmentManager = getSupportFragmentManager();
                }
                Fragment fragment = null ;
                AlternateLayoutFragment alternateLayoutFragment  = null ;
                fragment =  fragmentManager.findFragmentByTag(AlternateLayoutFragment.class.getSimpleName()) ;
                if(fragment instanceof AlternateLayoutFragment){
                	alternateLayoutFragment = (AlternateLayoutFragment) fragment ;
                }else {
                	alternateLayoutFragment  = new AlternateLayoutFragment() ;
                }
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			    fragmentTransaction.add(R.id.layout_theme_container, alternateLayoutFragment,alternateLayoutFragment.getClass().getSimpleName()) ;
			    fragmentTransaction.commit() ;
			    btNext.setVisibility(View.INVISIBLE);
			    Animation animRightIn = AnimationUtils.loadAnimation(CollageEditActivity.this, R.anim.right_in );
			    vLayoutContainer.setAnimation(animRightIn);
				animRightIn.start();
				mCollageMainView.animateToLeft(null);
			}
		});

		collage_change_text_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				attr.put(KEY_Calendar_Text_Added, VALUE_YES);
				new CollageFontCreateTask(context).execute();
				Collage collage = CollageManager.getInstance().getCurrentCollage();
				if (CollageManager.getInstance().isPhotosNumberMaximum(collage)){
					collageMaximumDialogShow(getString(R.string.ComposeCollage_TooManyTextBlocks));
				}else {
					new CreateTextInputPageTask(collage).execute();
				}			
			}
		});

		collage_shuffle_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
                ShuffleCollageTask shuffleCollageTask = new ShuffleCollageTask() ;
                shuffleCollageTask.execute() ;
                attr.put(CollageShuffleUsed,VALUE_YES);
			}
		});

		collage_bigorsmall_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mCollageMainView.isZoomIn()) {
					mCollageMainView.zoomOut(null);
				} else {
					mCollageMainView.zoomIn(null);
				}
			}
		});
		
		btNext.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RSSLocalytics.recordLocalyticsEvents(CollageEditActivity.this, CollageEditSummary, attr);
				Intent intent = new Intent(CollageEditActivity.this, ShoppingCartActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		vImageTransparent.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				removeFragmentsWithAnimition() ;
				mCollageMainView.animateToNormal(null);
			}
		}) ;
		mCollageMainView.setOnLayerClickListener(onLayerClickListener);
		mCollageMainView.setOnLayerDragListener(onLayerDragListener);
        collageEditLayer.setOnDoneClickListener(onDoneClickListener);
        collageEditLayer.setOnPopEditItemClickListener(onEditItemClickListener);
	}
	
	public CollageWebServices getCollageWebServices(){
		return this.mService ;
	}
	
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}
	
	
	public List<Theme> getCollageTheme(){
		return collageThemes ;
	}

	private OnLayerClickListener<CollagePageView, CollagePage, Layer> onLayerClickListener =  new OnLayerClickListener<CollagePageView, CollagePage, Layer>() {

		@Override
		public void onLayerClick(final CollagePageView pageView, final CollagePage page, final Layer layer, final RectF layerRect) {
			Log.i(TAG, "Layer:" + layer.toString() + "  onclick");
			Log.d(TAG, " onLayerClickListener on excute");
			if (Layer.TYPE_IMAGE.equals(layer.type) || Layer.TYPE_TEXT_BLOCK.equals(layer.type)) {
				//addLayerLocalInfo(layer);
				mCollageMainView.enterLayerEditMode(pageView, layer, new AnimationListener() {
								
					@Override
					public void onAnimationStart(Animation animation) { }
								
					@Override
					public void onAnimationRepeat(Animation animation) { }
								
					@Override
					public void onAnimationEnd(Animation animation) {
						collageEditLayer.showEditImageAndPop(pageView, layer);
						attr.put(CollageImageEditwUsed,VALUE_YES);
					}
				});
			}
		}
	};
				
	private OnLayerDragListener<CollagePageView, CollagePage, Layer> onLayerDragListener = new OnLayerDragListener<CollagePageView, CollagePage, Layer>() {

		@Override
		public void onStartDrag(MotionEvent event, CollagePageView pageView, CollagePage page, Layer layer, Bitmap bitmap) {
			float rawX = event.getRawX();
			float rawY = event.getRawY();
			
			RectF rect = pageView.getLayerRect(layer);
			if(rect == null) return;
			
			int w = (int) (rect.right-rect.left);
			int h = (int) (rect.bottom-rect.top);
			animDragHelper.setSize(w, h);
			
			if(bitmap==null){
				// TODO load the image
			}
			animDragHelper.createDragImage(bitmap, rawX, rawY);
		}

		@Override
		public void onDragging(MotionEvent event, CollagePageView pageView, CollagePage page, Layer layer, Bitmap bitmap) {
			final float rawX = event.getRawX();
			final float rawY = event.getRawY();
			animDragHelper.deleteTempImageView();
			final Object[] result = mCollageMainView.pointToPosition(rawX, rawY);
			if(result == null){
				animDragHelper.onDrag(rawX, rawY);
			} else {
				Layer toLayer = (Layer) result[2];
				if (layer != null && layer.contentId != null && toLayer != null && toLayer.contentId != null && !layer.contentId.equals(toLayer.contentId)) {
					animDragHelper.onDrag(rawX, rawY, 1);	
				}else {
					animDragHelper.onDrag(rawX, rawY);	
				}
			}
		}

		@Override
		public void OnDrop(MotionEvent event, CollagePageView pageView, CollagePage page, Layer layer, Bitmap bitmap) {
			float rawX = event.getRawX();
			float rawY = event.getRawY();
			animDragHelper.onStopDrag(rawX, rawY, bitmap);
			animDragHelper.deleteTempImageView();
			if(page==null || layer==null) return;
			String contentId = layer.contentId;
			if(contentId==null || contentId.equals("")) return;
			final Object[] result = mCollageMainView.pointToPosition(rawX, rawY);
			if(result == null) return;
			CollagePage toPage = (CollagePage) result[1];
			Layer toLayer = (Layer) result[2];
			if(toPage==null || toPage.id==null) return;
			if(layer!=null && layer.contentId!=null && toLayer!=null && toLayer.contentId!=null && !contentId.equals(toLayer.contentId)){
				CollageSwapContentAsyncTask swapTask = new CollageSwapContentAsyncTask(context, page, contentId, toLayer.contentId);
				swapTask.execute();
			}
		}
		
	};
				
	private CollageEditLayer.OnDoneClickListener onDoneClickListener = new CollageEditLayer.OnDoneClickListener() {
					
		@Override
		public void OnDoneClick(CollageEditLayer editLayer, CollagePageView pageView, EditImageView ivEdit) {
			Log.d(TAG, " onDoneClickListener on excute");
			if (collageEditLayer.isEditPage() || !ivEdit.isEdited()) {
				editLayer.dismiss();
				mCollageMainView.exitEditMode(null);
			} else {
				//get old roi and angle
				Layer layer = (Layer) ivEdit.getLayer();
				ROI oldRoi = ProductUtil.getImageCropROI(layer);
				int oldAngle=layer.angle;
				int newAngle=(int) ivEdit.getLayerAngel();
				//get current roi
				ROI newRoi = ivEdit.getImageROI();
				boolean moved = newRoi != null && !newRoi.equals(oldRoi);
				if (moved&&newAngle==oldAngle) {
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.CROP_IMAGE, editTaskHandler, pageView.getPage(), layer, newRoi).start();
					//TODO delete the code below if you implement the task
					editLayer.dismiss();
				} else {
					editLayer.dismiss();
					mCollageMainView.exitEditMode(null);
				}
				
				if (newAngle!=oldAngle&&moved) {
					layer.location=newRoi;
					layer.angle=newAngle;
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.CROP_AND_ROTATE, editTaskHandler, pageView.getPage(), layer,newRoi).start();
					editLayer.dismiss();
				}else {
					editLayer.dismiss();
					mCollageMainView.exitEditMode(null);
				}

				if (newAngle!=oldAngle&&!moved) {
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ROTATE_CONTENT, editTaskHandler, pageView.getPage(), layer, newAngle).start();
					editLayer.dismiss();
				}else {
					editLayer.dismiss();
					mCollageMainView.exitEditMode(null);
				}
			}
		}
	};
	private OnEditItemClickListener<CollageEditPopView, CollagePage, Layer> onEditItemClickListener = new OnEditItemClickListener<CollageEditPopView, CollagePage, Layer>() {
					
		@Override
		public void onEditItemClick(CollageEditPopView view, CollagePage page, Layer layer, int itemId) {
			Log.i(TAG, layer == null ? "null" : layer.contentId + " : " + itemId);
			if (view.getType() == CollageEditPopView.TYPE_COLOR_EFFECT) {
				new CollageEditTask(CollageEditActivity.this, CollageEditTask.COLOR_EFFECT, editTaskHandler, page, layer, itemId).start();
				attr.put(CollageEffectsUsed ,VALUE_YES);
			} else {
				switch (itemId) {
				case CollageEditPopView.LAYER_ENHANCE:
				case CollageEditPopView.LAYER_UNDO_ENHANCE:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ENHANCE, editTaskHandler, page, layer, itemId==PhotoBookEditPopView.LAYER_ENHANCE ? 1:0).start();
					break;
				case CollageEditPopView.LAYER_RED_EYE:
				case CollageEditPopView.LAYER_UNDO_RED_EYE:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.RED_EYE, editTaskHandler, page, layer, itemId==CollageEditPopView.LAYER_RED_EYE).start();
					break;
				case CollageEditPopView.LAYER_REMOVE_IMAGE:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.REMOVE_IMAGE, editTaskHandler, page, layer).start();
					break;
				case CollageEditPopView.LAYER_DELETE_CAPTION:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.DELETE_CAPTION, editTaskHandler, page, layer).start();
					break;
				case CollageEditPopView.LAYER_FLIP_HORIZONTAL:
				case CollageEditPopView.LAYER_FLIP_VERTICAL:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.FLIP_VERTICAL_OR_HORIZONTAL, editTaskHandler, page, layer, itemId == CollageEditPopView.LAYER_FLIP_HORIZONTAL).start();
					break;
				case CollageEditPopView.PAGE_DELETE_PAGE_TEXT:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.PAGE_DELETE_PAGE_TEXT, editTaskHandler, page, layer).start();
					break;
				case CollageEditPopView.LAYER_ENTER_CAPTION:
					break;
				case CollageEditPopView.LAYER_EDIT_CAPTION:
					break;
				case CollageEditPopView.PAGE_EDIT_PAGE_TEXT:
					collageEditLayer.dismiss();
					collageEditLayer.dismissEditProgress();
					mCollageMainView.exitEditMode(null);
					collageEditTestInputView.setVisibility(View.VISIBLE);
					main_navbar.setVisibility(View.GONE);
					showSoftInput();
					collageEditTestInputDialogWidget.showTextFontView(page,layer);
					break;
				case CollageEditPopView.LAYER_ROTATE:
					new CollageEditTask(CollageEditActivity.this, CollageEditTask.ROTATE_PAGE, editTaskHandler, page, layer).start();
					break;
				}
			}
		}
	};

	private void showSoftInput() {
		InputMethodManager inputMethodManager = (InputMethodManager) collage_change_text_btn.getContext().getSystemService( Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInputFromWindow(collage_change_text_btn.getWindowToken(), 2, InputMethodManager.RESULT_UNCHANGED_SHOWN);
		inputMethodManager.showSoftInput(collage_change_text_btn, InputMethodManager.SHOW_FORCED);
	}

	@Override
	public void updateUI() {
		collageEditTestInputView.setVisibility(View.GONE);
		main_navbar.setVisibility(View.VISIBLE);
	}

	private void removeFragmentsWithAnimition(){
		Animation animRightOut = AnimationUtils.loadAnimation(CollageEditActivity.this, R.anim.right_out) ;
		animRightOut.setFillAfter(true) ;
		animRightOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) { }
			
			@Override
			public void onAnimationRepeat(Animation animation) { }
			
			@Override
			public void onAnimationEnd(Animation animation) {
				vRelativeLayoutContainer.setVisibility(View.GONE) ;
				 btNext.setVisibility(View.VISIBLE);
				 vLinearLayoutLeftPart.setVisibility(View.VISIBLE) ;
			}
		}) ;
		vLayoutContainer.startAnimation(animRightOut) ;
		if(fragmentManager==null){
			fragmentManager = getSupportFragmentManager() ;
		}
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.layout_theme_container)) ;
		fragmentTransaction.commit() ;
	}


   @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

   class RotateCollageTask  extends AsyncTask<Void, Void, Collage>{
	   private boolean isPortrait ;
       
       public RotateCollageTask(boolean isPortrait){
    	   this.isPortrait = isPortrait; 
       }
       
       @Override
       protected void onPreExecute() {
    	   super.onPreExecute();
    	   showWaiting();
       }

       @Override
       protected Collage doInBackground(Void... params) {
    	   Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
    	   Collage newCollage = mService.rotateCollageTask(currentCollage.id, isPortrait) ;
    	   return newCollage ;
       }
		
       @Override
       protected void onPostExecute(Collage result) {
    	   super.onPostExecute(result);
    	   hideWaiting();
    	   if(result!=null){
    		   CollageManager.getInstance().updateCurrentCollage(result);
    		   LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
    		   loadCollageBitmap.execute() ;
    	   }else {
    		   showNoRespondDialog();
    	   }
       }
    }
   
	class CheckUploadPhotosTask extends AsyncTask<Void, Void, Boolean> {
		private String collageId;
		private boolean hasPhotoUnUploaded = true;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Collage collage =CollageManager.getInstance().getCurrentCollage();
			collageId = collage.id;
			if (dialog == null) {
				InfoDialogWindow.Builder builder = new Builder(CollageEditActivity.this);
				dialog = builder.setMessage(R.string.animation_quickbook_wait).setCancelable(false).create();
			} else {
				dialog.setMessage(R.string.animation_quickbook_wait);
			}
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			while (hasPhotoUnUploaded && !PrintHelper.uploadError) {
				List<PhotoInfo> list = AppContext.getApplication().getmUploadPhotoList();
				Log.v("sunny", "upload size: " + list.size());
				Iterator<PhotoInfo> itor = list.iterator();
				boolean flag = false;
				while (flag = itor.hasNext()) {
					PhotoInfo photo = itor.next();
					if (photo.getProductId().equals(collageId)) {
						hasPhotoUnUploaded = true;
						break;
					} else {
						hasPhotoUnUploaded = false;
						continue;
					}
				}
				if (!flag) {
					hasPhotoUnUploaded = false;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
			return !PrintHelper.uploadError;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(result){
				Collage collage = CollageManager.getInstance().getCurrentCollage();
				String collagePageId = collage.page.id;
				List<PhotoInfo> photosUnInsertedList = collage.page.getUnInsertedPhotos();
				Log.v("sunny", "upload unselected size: " + photosUnInsertedList.size());
				if (photosUnInsertedList != null && photosUnInsertedList.size() > 0) {
					InsertPhotosToPageTask insertPhotosTask = new InsertPhotosToPageTask(collagePageId, photosUnInsertedList);
					insertPhotosTask.execute();
				} else {
					
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					
					notifyPageChaned() ;
					
					
				}
			}else {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				PrintHelper.uploadError = false;
				showNoRespondDialog() ;
				notifyPageChaned() ;
				
			}
			
			
		}
	}

    class InsertPhotosToPageTask extends AsyncTask<Void, Void, CollagePage> {
	    private List<PhotoInfo> photosUnInsertedList ;
	    private String collagePageId ;
	    private List<String> contentIds ;
	    
		public InsertPhotosToPageTask(String pageId ,List<PhotoInfo> photosUnInsertedList){
			this.collagePageId = pageId ;
			this.photosUnInsertedList = photosUnInsertedList ;
		}
		
        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
			if(photosUnInsertedList!=null && photosUnInsertedList.size()>0){
				contentIds = new ArrayList<String>() ;
				for (PhotoInfo photo : photosUnInsertedList) {
					String contentId = photo.getContentId() ;
					contentIds.add(contentId) ;
				}
			}
			if(contentIds!=null && contentIds.size()>0){
				if(dialog==null){
	        		InfoDialogWindow.Builder builder = new Builder(CollageEditActivity.this);
	    			dialog = builder.setMessage(R.string.collage_insert_photos) .setCancelable(false).create() ;
	    			dialog.show() ;
	        	}else if(dialog.isShowing()){
	        		dialog.setMessage(R.string.collage_insert_photos) ;
	        	}
			}
        }
        
		@Override
		protected CollagePage doInBackground(Void... params) {
			CollagePage newCollagePage = null ;
			if(contentIds!=null && contentIds.size()>0){
				newCollagePage = mService.insertContentTask(collagePageId, contentIds) ;
			}
			return newCollagePage;
		}
		
		@Override
		protected void onPostExecute(CollagePage result) {
			super.onPostExecute(result);
			if(dialog!=null && dialog.isShowing()){
				dialog.dismiss() ;
			}
			if(result!=null){
				
				Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
				currentCollage.page.updatePhotoInsertStateAfterInsertSuccess(photosUnInsertedList) ;
				CollageManager.getInstance().updataCurrentCollagePage(result);
				
				
				LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
				loadCollageBitmap.execute() ;
				mCollageMainView.setCollage(currentCollage);
			}else{
				showNoRespondDialog() ;
			}
		}

	}
    
	// create Collage text input page.
	class CreateTextInputPageTask extends AsyncTask<Void, Void, CollagePage> {
		private Collage collage;
		private List<String> contentIds;
		public CreateTextInputPageTask(Collage collage) {
			this.collage = collage;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			contentIds = new ArrayList<String>();
			if (dialog == null) {
				InfoDialogWindow.Builder builder = new Builder(context);
				dialog = builder.setMessage(R.string.animation_quickbook_wait).setCancelable(false).create();
			} else {
				dialog.setMessage(R.string.animation_quickbook_wait);
			}
			dialog.show();
		}

		@Override
		protected CollagePage doInBackground(Void... params) {
			CollagePage newCollagePage = null;
			List<Font> fonts = null;
			String language = Locale.getDefault().toString();
			fonts = mService.getAvailableFontsTask(language);
			if (fonts !=null){
				AppContext.getApplication().setFonts(fonts);
				TextBlock textBlock = mService.createTextBlockTask(context.getString(R.string.Common_SampleText), fonts.get(0).name);
				if (textBlock != null && textBlock.id != null) {
					contentIds.add(textBlock.id);
					newCollagePage = mService.insertContentTask(collage.page.id, contentIds);
				}
			}			
			return newCollagePage;
		}

		@Override
		protected void onPostExecute(CollagePage result) {
			super.onPostExecute(result);
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			if (result != null) {
				int currentTextLayerNumber = collage.page.getTextLayerNumber();
				
				Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
				currentCollage.page.setTextLayerNumber(currentTextLayerNumber + 1);
				CollageManager.getInstance().updataCurrentCollagePage(result);
				

				LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask();
				loadCollageBitmap.execute();
				mCollageMainView.setCollage(currentCollage);
			} else {
				showNoRespondDialog();
			}
		}

	}
	
	// update text for collage
		class UpdateTextBolcksTask extends AsyncTask<Void, Void, CollagePage> {
			private TextBlock textBlock;
			private Layer layer;
			private Page page;

			public UpdateTextBolcksTask(Page page, Layer layer, TextBlock textBlock) {
				this.layer = layer;
				this.page = page;
				this.textBlock = textBlock;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				if (dialog == null) {
					InfoDialogWindow.Builder builder = new Builder(context);
					dialog = builder.setMessage(R.string.animation_quickbook_wait).setCancelable(false).create();
				} else {
					dialog.setMessage(R.string.animation_quickbook_wait);
				}
				dialog.show();
			}

			@Override
			protected CollagePage doInBackground(Void... params) {
				CollagePage newCollagePage = null;
				TextBlock result = mService.updateTextBlockTask(textBlock);
				if (result !=null){
					newCollagePage = mService.setCollagePageLayoutTask(page.id, "", true) ;			
				}
				return newCollagePage;
			}

			@Override
			protected void onPostExecute(CollagePage result) {
				super.onPostExecute(result);
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				if (result != null) {
					CollageManager.getInstance().updataCurrentCollagePage(result);
					Collage currentCollage = CollageManager.getInstance().getCurrentCollage();
					result.setPhotosInCollagePage(currentCollage.page.getPhotosInCollagePage());
					LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask();
					loadCollageBitmap.execute();
					mCollageMainView.setCollage(currentCollage);
				} else {
					showNoRespondDialog();
				}
			}

		}
	
	class LoadCollageAsyncTask extends AsyncTask<Void, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(Void... params ) {
			Bitmap bitmap = null ;
			Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
			String pathStr = currentCollage.id;
			String urlString="";
			urlString=currentCollage.page.baseURI+currentCollage.page.id+"/preview";
			bitmap = ImageUtil.downLoadBitmap(urlString,pathStr) ;
			return bitmap;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			myproBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			myproBar.setVisibility(View.INVISIBLE);
			if(null!=result){
				Collage currentCollage =  CollageManager.getInstance().getCurrentCollage() ;
				mCollageMainView.setCollage(currentCollage);
				mCollageMainView.setImageBitmap(result);
			}
		}
		
	}
	
	class GetThemeTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
			collageThemes = mService.getThemesTask(currentCollage.proDescId) ;
			if(collageThemes!=null && collageThemes.size()>0){
				for (Theme theme : collageThemes) {
					if(theme.id.equals(currentCollage.getTheme())){
						theme.setSelected(true) ;
					}else {
						theme.setSelected(false) ;
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
		
	}
	
	class SetThemeTask extends AsyncTask<Void, Void, Collage>{
        private Theme theme ;
		public SetThemeTask(Theme theme){
			this.theme = theme ;
		}
		
		@Override
		protected Collage doInBackground(Void... params) {
			Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
			Collage collage =  mService.setCollageThemeTask(currentCollage.id ,theme.id) ;
			return collage ;
		}
		
		@Override
		protected void onPostExecute(Collage result) {
			super.onPostExecute(result);
			if(result!=null){
				CollageManager.getInstance().updateCurrentCollage(result);
				LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
				loadCollageBitmap.execute() ;
			} else {
				showNoRespondDialog();
			}
		}
	}
	
    class SetCollagePageLayoutTask extends AsyncTask<Void, Void, CollagePage>{
        private AlternateLayout alternateLayout ;
        private boolean alternates ;
    	public SetCollagePageLayoutTask(AlternateLayout alternateLayout,boolean alternates){
    		this.alternateLayout = alternateLayout ;
    		this.alternates = alternates ;
    	}
    	
		@Override
		protected CollagePage doInBackground(Void... params) {
			Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
			CollagePage page  = null ;
			if(null!=alternateLayout){
			    page = mService.setCollagePageLayoutTask(currentCollage.page.id, alternateLayout.layoutId, alternates) ;
			}else {
				page = mService.setCollagePageLayoutTask(currentCollage.page.id, "", alternates) ;
			}
			
			return page;
		}
		
		@Override
		protected void onPostExecute(CollagePage result) {
			super.onPostExecute(result);
			if(result!=null){
				if(!alternates){
					result.alternateLayouts = CollageManager.getInstance().getCurrentCollage().page.alternateLayouts ;
				}
				CollageManager.getInstance().updataCurrentCollagePage(result) ;
				
				
				LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
				loadCollageBitmap.execute() ;
			}else {
				showNoRespondDialog() ;
			}
		}
    	
    }

    class ShuffleCollageTask  extends AsyncTask<Void, Void, CollagePage>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showWaiting();
		}

		@Override
		protected CollagePage doInBackground(Void... params) {
			Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
			CollagePage page = mService.shuffleCollageTask(currentCollage.page.id) ;
			return page;
		}
		
		@Override
		protected void onPostExecute(CollagePage result) {
			super.onPostExecute(result);
			hideWaiting();
			if(result!=null){
				CollageManager.getInstance().updataCurrentCollagePage(result) ;
				LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
				loadCollageBitmap.execute() ;
			}else {
				showNoRespondDialog() ;
			}
		}
    }
    
	@Override
	public void doneOnClick() {
		if (fragmentManager == null) {
			fragmentManager = getSupportFragmentManager();
		}
		removeFragmentsWithAnimition() ;
		mCollageMainView.animateToNormal(null);
	}

	@Override
	public void selectTheme(Theme theme) {
		SetThemeTask setThemeTask = new SetThemeTask(theme) ;
		setThemeTask.execute() ;
	}

	@Override
	public void selectPageLayout(AlternateLayout alternateLayout) {
		SetCollagePageLayoutTask setCollagePageLayoutTask = new SetCollagePageLayoutTask(alternateLayout,false) ;
		setCollagePageLayoutTask.execute() ;
	}
   
	public void notifyPageChaned() {
		LoadCollageAsyncTask loadCollageBitmap = new LoadCollageAsyncTask() ;
		loadCollageBitmap.execute() ;
	}
	
	public void addLayerLocalInfo(Layer layer){
		AppContext.getApplication().getProductLayerLocalInfos().putIfNotExist(layer, true);
	}
	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		if (loadColorEffectSourcesTask != null && !loadColorEffectSourcesTask.isAlive()) {
			loadColorEffectSourcesTask.start();
		}
		mImageFetcher.setExitTasksEarly(false);
		
	}
	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
		if (loadColorEffectSourcesTask != null) {
			loadColorEffectSourcesTask.interrupt();
			loadColorEffectSourcesTask = null;
		}
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}
	
	
	
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@Override
	public void updateTextCollage(Page page, Layer layer, TextBlock textBlock) {
		new UpdateTextBolcksTask(page, layer, textBlock).execute();
		
	}
	
	private void collageMaximumDialogShow(String message) {
		InfoDialogWindow dialog = null; 
		InfoDialogWindow.Builder builder = new Builder(CollageEditActivity.this);
		builder.setMessage(message).setCancelable(false);
		builder.setPositiveButton(context.getString(R.string.OK), null);
		dialog =builder.create();
		dialog.show();
	}
	
	public void showWaiting(){
		pbWaitEditPage.setVisibility(View.VISIBLE);
	}
	
	public void hideWaiting(){
		pbWaitEditPage.setVisibility(View.GONE);
	}
	private void addAttr(){
        attr.put(CollageEditSummary,VALUE_NO);
        attr.put(CollagePicturesAdded,VALUE_NO);
        attr.put(CollageShuffleUsed,VALUE_NO);
        attr.put(CollageLandscapeUsed,VALUE_NO);
        attr.put(CollagePortraitUsed,VALUE_NO);
        attr.put(CollageEffectsUsed,VALUE_NO);
        attr.put(CollageImageEditwUsed,VALUE_NO);
        attr.put(CollageBackgroundImage,VALUE_NO);
	}
}
