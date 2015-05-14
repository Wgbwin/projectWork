package com.kodak.kodak_kioskconnect_n2r.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppConstants.PhotoSource;
import com.AppContext;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.kodak.kodak_kioskconnect_n2r.CartItem;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PictureUploadService2;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumHolder;
import com.kodak.kodak_kioskconnect_n2r.bean.AlbumInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.fragments.AlbumSelectFragment;
import com.kodak.kodak_kioskconnect_n2r.fragments.FacebookSelectFragment;
import com.kodak.kodak_kioskconnect_n2r.fragments.ICommunicating;
import com.kodak.kodak_kioskconnect_n2r.fragments.PhotoSelectFragment;
import com.kodak.kodak_kioskconnect_n2r.view.TabIndicator;
import com.kodak.kodak_kioskconnect_n2r.view.TabIndicator.TabView;
import com.kodak.utils.AsyncTask;
import com.kodak.utils.RSSLocalytics;

public class PhotoSelectMainFragmentActivity extends BaseActivity implements ICommunicating {
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private static String TAG = PhotoSelectMainFragmentActivity.class.getSimpleName() ;
	private Button vBackButton;
	private Button vCartButton;
	private TabIndicator vTabIndicator;
	private TextView vTextViewTitle;
	private TextView vTextViewSelectedNum;

	private ProgressDialog vProgressDialog;
	private ImageButton vImageButtonSelectAll;
	private ImageButton vImageButtonInvertSelectAll;

	private List<AlbumInfo> mAlbums;
	public static final int QUERY_TOKEN = 34;
	private List<PhotoInfo> mAllPhotosInPhone; // all photos in local
//	private AlbumInfo mAllPhotosAlbum;
	private AlbumInfo mCameraAlbum ;
	// private FlowType flowType ;
	private String productId = "";
	private String descrId = "";
	private QueryPhotoHandler mQueryPhotoHandler;

	private FlowType flowType;
	public boolean forEdit;
	public static String EVENT_Source_Selection = "Source Selection";
	private int[] tabviewStringRes = { R.string.camera, R.string.albums, R.string.Facebook_Input };
	private int[] tabviewStringResForWifiWorkFlow = { R.string.camera, R.string.albums };
	
	public PrintProduct collageProduct = null ;
	
	private ImageSelectionDatabase imageSelDb;
    
	private String KEY_Facebook_Selected = "Facebook Selected";
	private String KEY_Photos_Selected = "Photos Selected";
	public static final String KEY_PHOTOS_CAMERA_ROLL = "Photos Camera Roll" ;
	public static final String KEY_PHOTOS_ALBUM = "Photos Albums" ;
	public static final String KEY_PHOTOS_EVENT = "Photos Events" ;
	public static final String KEY_FACEBOOK_PHOTOS_OF_YOU_SELECTED= "Facebook Photos of You Selected" ;
	public static final String KEY_FACEBOOK_YOUR_PHOTOS_SELECTED= "Facebook Your Photos Selected" ;
	public static final String KEY_FACEBOOK_YOUR_FRIENDS_SELECTED = "Facebook Your Friends Selected";

	private String SCREEN_NAME = "" ;
	public static final String VALUE_YES = "yes";
	public static final String VALUE_NO = "no";
	public static HashMap<String, String> attr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.fragment_activity_main_select_photo);
		getViews();
		initData();
		setEvents();
		String packageName = getApplicationContext().getPackageName();
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			initFacebook(savedInstanceState) ;
		}
		
		

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		String packageName = getApplicationContext().getPackageName();
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			 Session session = Session.getActiveSession();
		     Session.saveSession(session, outState);
		}
		
		 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		String packageName = getApplicationContext().getPackageName();
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			Session.getActiveSession().addCallback(statusCallback);
		}
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		String packageName = getApplicationContext().getPackageName();
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			Session.getActiveSession().removeCallback(statusCallback);
		}
		
		
	}
	
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		String packageName = getApplicationContext().getPackageName();
		if(packageName!=null && 
				( packageName.contains("com.kodak.kodak.rsscombinedapp") ||
						packageName.contains("com.kodak.dm.rsscombinedapp") )){
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
		
		
	}

	@Override
	public void getViews() {
		sideFacebook_lay.setVisibility(View.GONE) ;
		
		vBackButton = (Button) findViewById(R.id.back_btn);
		vCartButton = (Button) findViewById(R.id.next_btn);
		vTextViewTitle = (TextView) findViewById(R.id.headerBar_tex);
		vTabIndicator = (TabIndicator) findViewById(R.id.tab_indicator);
		vTextViewSelectedNum = (TextView) findViewById(R.id.versionCopyright_tex);

		vImageButtonSelectAll = (ImageButton) findViewById(R.id.select_all_imagebtn);
		vImageButtonInvertSelectAll = (ImageButton) findViewById(R.id.invert_select_all_imagebtn);

		vBackButton.setVisibility(View.VISIBLE);
		vCartButton.setVisibility(View.VISIBLE);
		vTextViewTitle.setVisibility(View.VISIBLE);
		vTextViewSelectedNum.setVisibility(View.VISIBLE);

		vBackButton.setTypeface(PrintHelper.tf);
		vCartButton.setTypeface(PrintHelper.tf);
		vTextViewTitle.setTypeface(PrintHelper.tf);
		vTextViewSelectedNum.setTypeface(PrintHelper.tf);

		flowType = AppContext.getApplication().getFlowType();
		if (flowType.isPrintWorkFlow()) {
			vCartButton.setText(R.string.cart);
			vImageButtonSelectAll.setVisibility(View.VISIBLE);
			vImageButtonInvertSelectAll.setVisibility(View.VISIBLE);
			vTextViewSelectedNum.setText(AppContext.getApplication().getmTempSelectedPhotos().size()
					+" "+getString(R.string.selected)) ;
			
			SCREEN_NAME = "Prt Image Source" ;
			
		} else if (flowType.isPhotoBookWorkFlow()) {
			vCartButton.setText(R.string.next);
			vImageButtonSelectAll.setVisibility(View.INVISIBLE);
			vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
			vTextViewSelectedNum.setText(AppContext.getApplication().getmTempSelectedPhotos().size()
					+" "+getString(R.string.selected)) ;
			SCREEN_NAME = "PB Image Source" ;
			
		} else if (flowType.isGreetingCardWorkFlow()) {

			vBackButton.setVisibility(View.INVISIBLE);
			vCartButton.setVisibility(View.INVISIBLE);

			vImageButtonSelectAll.setVisibility(View.INVISIBLE) ;
			vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE) ;
			vTextViewSelectedNum.setText(R.string.image_selection_title_for_card) ;

		}else if(flowType.isCollageWorkFlow()){
			
			vCartButton.setText(R.string.next);
			vImageButtonSelectAll.setVisibility(View.INVISIBLE);
			vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
			vTextViewSelectedNum.setText( 
					CollageManager.getInstance().getCurrentTotalPhotoNumberInCollage(CollageManager.getInstance().getCurrentCollage())
					+CollageManager.getInstance().getCurrentCollage().page.getTextLayerNumber() 
					+"/"+CollageManager.getInstance().getCurrentCollage().page.maxNumberOfImages+" "+getString(R.string.maximum)) ;
			
		}else if (flowType.isWifiWorkFlow()) {
			vCartButton.setText(R.string.selected_set);
			vBackButton.setVisibility(View.INVISIBLE);
			vImageButtonSelectAll.setVisibility(View.VISIBLE);
			vImageButtonInvertSelectAll.setVisibility(View.VISIBLE);
			vTextViewSelectedNum.setText(AppContext.getApplication().getmTempSelectedPhotos().size()
					+" "+getString(R.string.selected)) ;
		}
		
		if(!TextUtils.isEmpty(SCREEN_NAME)){
			RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME) ;
		}
		
		
	}

	@Override
	public void initData() {

		Intent mIntent = getIntent();
		if (mIntent != null) {
			productId = mIntent.getStringExtra(AppConstants.KEY_PRODUCT_ID);
			descrId = mIntent.getStringExtra(AppConstants.KEY_PRODUCT_DECID);
			forEdit = mIntent.getBooleanExtra(AppConstants.KEY_FOR_ADD_PICTURE, false);
			if(flowType.isCollageWorkFlow()){
				for (PrintProduct product : PrintHelper.products) {
					
					if ( product.getId().equals(CollageManager.getInstance().getCurrentCollage().proDescId)) {
						collageProduct = product;
						 break;
					}
				}
			}
			
			
		}
		initLocalyticsData();

		if (!flowType.isWifiWorkFlow()) {
			vTabIndicator.setTabStringResIds(tabviewStringRes);
		} else {
			imageSelDb = new ImageSelectionDatabase(this);
			vTabIndicator.setTabStringResIds(tabviewStringResForWifiWorkFlow);
		}

		mQueryPhotoHandler = new QueryPhotoHandler(this);
		startQueryPhotoPhone();
	}

	private void initLocalyticsData() {
		
		
		attr = new HashMap<String, String>();
		attr.put(KEY_Facebook_Selected, VALUE_NO);
		attr.put(KEY_Photos_Selected, VALUE_NO);
		attr.put(KEY_FACEBOOK_PHOTOS_OF_YOU_SELECTED, VALUE_NO) ;
		attr.put(KEY_FACEBOOK_YOUR_PHOTOS_SELECTED, VALUE_NO) ;
		attr.put( KEY_FACEBOOK_YOUR_FRIENDS_SELECTED, VALUE_NO) ;
		attr.put(KEY_PHOTOS_CAMERA_ROLL, VALUE_NO) ;
		attr.put(KEY_PHOTOS_ALBUM, VALUE_NO) ;
		attr.put(KEY_PHOTOS_EVENT, VALUE_NO) ;
		
		
		
	}
	
	private void initFacebook( Bundle savedInstanceState ){
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
//			 if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
//	               session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
//	          }
		}
	}

	@Override
	public void setEvents() {

		vBackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doBack();

			}

		});

		vCartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!flowType.isWifiWorkFlow()) {
					if (!Connection.isConnected(PhotoSelectMainFragmentActivity.this)) {
						showNoConnectionDialog(v);
					} else {
						RSSLocalytics.recordLocalyticsEvents(PhotoSelectMainFragmentActivity.this, EVENT_Source_Selection, attr);
						if (flowType.isPhotoBookWorkFlow()) {
							photobookWorkflowNext(vCartButton);
						} else if (flowType.isPrintWorkFlow()) {
							printWorkFlowNext();
						}else if (flowType.isCollageWorkFlow()){
							//TODO COLLAGE
							collageWorkFlowNext() ;
							
						}

					}

				} else {
					wifiWorkFlowNext();
				}

			}
		});

		vTabIndicator.setOnTabSelectListener(new TabListener());

		vImageButtonSelectAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.relativelayout_container);
				
				if (currentFragment instanceof PhotoSelectFragment) {
					((PhotoSelectFragment) currentFragment).selectAllEvent();
				}

			}
		});

		vImageButtonInvertSelectAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.relativelayout_container);
				if (currentFragment instanceof PhotoSelectFragment) {
					((PhotoSelectFragment) currentFragment).invertSelectAllEvent();
				}

			}
		});

	}

	private void photobookWorkflowNext(View next) {
		Photobook photobook = AppContext.getApplication().getPhotobook();
		int iMin = photobook == null ? 0 : photobook.minNumberOfImages;
		int iMax = photobook == null ? 0 : photobook.maxNumberOfImages;
		boolean cantNavigate = (iMin != 0 && iMax != 0 && (photobook.selectedImages.size() < iMin || photobook.selectedImages.size() > iMax));
		PrintProduct photoBookProduct = null;
		for (PrintProduct product : PrintHelper.products) {
			if (product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage)
					&& product.getId().equals(photobook.proDescId)) {
				photoBookProduct = product;
				break;
			}
		}
		if (cantNavigate) {
			next.setEnabled(true);
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);
			if (photobook.selectedImages.size() < iMin) {
				builder.setTitle(String.format(getString(R.string.selected_images_range), iMin, iMax, photoBookProduct.getName()));
			}
			builder.setMessage("");
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			builder.create().show();
		} else {
			if (photobook.isDuplex && !photobook.hasAcceptBlankPage) {
				int totalNum = photobook.selectedImages.size();
				if (totalNum % 2 != 0) {
					next.setEnabled(true);
					showBlankPageWarning();
					return;
				}
			}
			PictureUploadService2.isDoneSelectPics = true;
			PictureUploadService2.isDoneUploadThumbnails = true;
			if (photobook.selectedImages != null) {
				for (PhotoInfo photo : photobook.selectedImages) {
					if (!photo.isThumbnailUploaded()) {
						PictureUploadService2.isDoneUploadThumbnails = false;
						break;
					}
				}
			}
			Intent myIntent = new Intent(PhotoSelectMainFragmentActivity.this, QuickBookFlipperActivity.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			finish();
			AppContext.getApplication().getmTempSelectedPhotos().clear();
		}
	}

	private void showBlankPageWarning() {
		InfoDialog.InfoDialogBuilder dialogBuilder = new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);
		dialogBuilder.setTitle("");
		dialogBuilder.setMessage(getString(R.string.qb_page_blank_warning));
		dialogBuilder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Photobook photobook = AppContext.getApplication().getPhotobook();
				photobook.hasAcceptBlankPage = true;
			}
		});
		dialogBuilder.create().show();
	}

	private void printWorkFlowNext() {
		if (AppContext.getApplication().getmTempSelectedPhotos().isEmpty()) {

			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);
			builder.setTitle(getString(R.string.selectatleastoneimage));
			builder.setMessage("");
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		} else {

			Bundle bundle = new Bundle();
			bundle.putBoolean(AppConstants.KEY_FROM_IMAGESELECTION, true);
			Intent myIntent = new Intent(PhotoSelectMainFragmentActivity.this, ShoppingCartActivity.class);
			myIntent.putExtras(bundle);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			AppContext.getApplication().getmTempSelectedPhotos().clear();
			finish();

		}
	}

	private void wifiWorkFlowNext() {

		AddWifiImagesInDbTask addWifiImagesInDbTask = new AddWifiImagesInDbTask(new Runnable() {

			@Override
			public void run() {
				if (imageSelDb.getSelectedCountWiFi() > 0) {
					Intent intent = new Intent(PhotoSelectMainFragmentActivity.this, WifiTaggedImagesActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// intent.putExtra(WifiTaggedImagesActivity.INTENT_KEY_FROM_PHOTOS_SCREEN,
					// true);
					// intent.putExtra("album", mAlbum);
					startActivity(intent);
					finish();
				} else {
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);
					builder.setTitle(getString(R.string.selectatleastoneimage));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
				}

			}

		});

		addWifiImagesInDbTask.execute();
	}

	
	private void collageWorkFlowNext(){
		Collage currentCollage = CollageManager.getInstance().getCurrentCollage() ;
		int iMin = currentCollage.page == null ? 0 : currentCollage.page.minNumberOfImages;
		int iMax = currentCollage.page == null ? 0 : currentCollage.page.maxNumberOfImages;
		
		if(iMin!=0 || iMax!=0){
			
			InfoDialog.InfoDialogBuilder builder  = null;
			String errorContent = "" ;
			
			int  currentSelectTotal = 0 ;
			currentSelectTotal = CollageManager.getInstance().getCurrentTotalPhotoNumberInCollage(currentCollage) ;
			if(iMin!=0 && currentSelectTotal<iMin){
				
				errorContent = getString(R.string.collage_minimum_tips, iMin ,collageProduct.getName() ) ;
				builder= new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);				
				builder.setMessage(errorContent).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
					
				}).create().show() ;
				
				return  ;
				
			}
			
			if(iMax!=0 && currentSelectTotal > iMax){
                for (PrintProduct product : PrintHelper.products) {
					
					if ( product.getId().equals(currentCollage.proDescId)) {
						collageProduct = product;
						 break;
					}
				}
                
                errorContent = getString(R.string.collage_maximum_tips ,collageProduct.getName() ) ;
				builder= new InfoDialog.InfoDialogBuilder(PhotoSelectMainFragmentActivity.this);				
				builder.setMessage(errorContent).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
					
				}).create().show() ;
				
				return  ;
                
			}
			
		}
		
		if(forEdit){
			
			setResult(RESULT_OK) ;
			finish() ;
			AppContext.getApplication().getmTempSelectedPhotos().clear();
		}else {
			Intent myIntent = new Intent(PhotoSelectMainFragmentActivity.this, CollageEditActivity.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			finish();
			AppContext.getApplication().getmTempSelectedPhotos().clear();
		}
		
		
		
	}
	
	
	
	private void doBack() {
		AppContext.getApplication().setFacebookGroupHinted(false);

		if (forEdit) {

			if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
				Photobook photobook = AppContext.getApplication().getPhotobook();
				for (PhotoInfo photo : AppContext.getApplication().getmTempSelectedPhotos()) {
					if (!photobook.isImageAlreadyInPhotobook(photo)) {
						photobook.selectedImages.remove(photo);
						photobook.imageEditParams.remove(photo);
						AppContext.getApplication().removePhotoFromUploadQueue(photo) ;

					}
				}

				finish();
				AppContext.getApplication().getmTempSelectedPhotos().clear();

			}else if(AppContext.getApplication().getFlowType().isCollageWorkFlow()){
				
				Collage collage  =CollageManager.getInstance().getCurrentCollage() ;
				for (PhotoInfo photo : AppContext.getApplication().getmTempSelectedPhotos()) {
					AppContext.getApplication().removePhotoFromUploadQueue(photo) ;
					
					collage.removePhotoFromCollage(photo) ;
				}
				
				finish() ;
				AppContext.getApplication().getmTempSelectedPhotos().clear();
			}
			
			

		} else {

			if (!AppContext.getApplication().isContinueShopping()) {

				if (!AppContext.getApplication().getmTempSelectedPhotos().isEmpty()) {

					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
					builder.setTitle("");
					builder.setMessage(getString(R.string.losework));
					builder.setPositiveButton((R.string.yes), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							PrintHelper.StartOver();
							PrintHelper.clearDataForDoMore();
							finish();

						}
					});

					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}

					}).create().show();

				} else {

					finish();
				}

			} else { // continue shopping back
				// clear current product (according to tempselectedphotolist)
				if (flowType.isPrintWorkFlow()) {
					if (!AppContext.getApplication().getmTempSelectedPhotos().isEmpty()) {

						for (PhotoInfo photo : AppContext.getApplication().getmTempSelectedPhotos()) {

							AppContext.getApplication().removePhotoFromUploadQueue(photo);
							PrintInfo printInfo = new PrintInfo(photo);
							boolean success = AppContext.getApplication().removePrintFromPrintList(printInfo);

							if (PrintHelper.uploadShare2WmcQueue != null) {

								PrintHelper.uploadShare2WmcQueue.remove(photo.getLocalUri());
							}

							if (success) {
								for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
									int count = 0;
									while (count < PrintHelper.cartChildren.get(i).size()) {
										CartItem tempItem = PrintHelper.cartChildren.get(i).get(count);
										if (tempItem.photoInfo.equals(photo)) {
											PrintHelper.cartChildren.get(i).remove(count);
										} else {
											count++;
										}
									}
								}
							}

						}
					}

					finish();
					AppContext.getApplication().getmTempSelectedPhotos().clear();

				} else if (flowType.isPhotoBookWorkFlow()) {

					if (!AppContext.getApplication().getmTempSelectedPhotos().isEmpty()) {

						Photobook photobook = AppContext.getApplication().getPhotobook();

						for (PhotoInfo photo : AppContext.getApplication().getmTempSelectedPhotos()) {

							AppContext.getApplication().removePhotoFromUploadQueue(photo) ; 
//							AppContext.getApplication().getPhotobooks().remove(photobook);
//							photobook = null;
						}
						AppContext.getApplication().getPhotobooks().remove(photobook);
						AppContext.getApplication().setPhotobook(null) ;

					}

					finish();
					AppContext.getApplication().getmTempSelectedPhotos().clear();

				}else if(flowType.isCollageWorkFlow()){
					if (!AppContext.getApplication().getmTempSelectedPhotos().isEmpty()) {
						Collage collage = CollageManager.getInstance().getCurrentCollage() ;
						
						for (PhotoInfo photo : AppContext.getApplication().getmTempSelectedPhotos()) {
							AppContext.getApplication().removePhotoFromUploadQueue(photo) ; 
							
						}
						CollageManager.getInstance().removeCollageFromCollageList(collage) ;
						
						
						
					}
					
					
				}

			}

		}
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//			if (flowType.isWifiWorkFlow() || flowType.isGreetingCardWorkFlow()) {
//				return false;
//			} else if (flowType.isPhotoBookWorkFlow() || flowType.isPrintWorkFlow()) {
//				doBack();
//			}
//		}
//		return super.onKeyDown(keyCode, event);
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		FragmentManager fm = getSupportFragmentManager() ;
		int fragmentInStackCount = fm.getBackStackEntryCount() ;
		Log.v(TAG, "COUNT---BACK "+ fragmentInStackCount ) ;
		if(fragmentInStackCount == 0){
			if ((keyCode == KeyEvent.KEYCODE_BACK)) {
				if (flowType.isWifiWorkFlow() || flowType.isGreetingCardWorkFlow()) {
					return false;
				} else if (flowType.isPhotoBookWorkFlow() || flowType.isPrintWorkFlow()||flowType.isCollageWorkFlow()) {
					doBack();
				}
			}
			return super.onKeyDown(keyCode, event);
		}else {
			return super.onKeyDown(keyCode, event);
			
		}
		
		
	}

	public void showSelectNumberText(String selectPhotoText) {
		vTextViewSelectedNum.setText(selectPhotoText);
	}
	
	public int getCurrentTabIndex(){
		return vTabIndicator.getCurrentItem() ;
	}

	/**
	 * build parameters
	 * @param next
	 * @return
	 */
	public Bundle parseNextNode(String next){
		Bundle bundle = null ;
		if(next!=null && !"".equals(next)){
            try {
				URI url = new URI(next) ;
				bundle = new Bundle() ;
				
				List<NameValuePair> namePair = URLEncodedUtils.parse(url, "UTF-8") ;
				for (NameValuePair nameValuePair : namePair) {
					bundle.putString(nameValuePair.getName(), nameValuePair.getValue()) ;
				}
				
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return bundle;
		
		
	}
	
	/**
	 * query local photos
	 */
	private void startQueryPhotoPhone() {

		if (vProgressDialog == null) {
			vProgressDialog = new ProgressDialog(this);
			vProgressDialog.setCancelable(true);
			vProgressDialog.show();
		} else {
			if (!vProgressDialog.isShowing()) {
				vProgressDialog.show();
			}
		}
		mQueryPhotoHandler.cancelOperation(QUERY_TOKEN);
		mQueryPhotoHandler.startQuery(QUERY_TOKEN, null, Images.Media.EXTERNAL_CONTENT_URI, null, Images.Media.DATA + "  not like ?  and "
				+ Images.Media.MIME_TYPE + " in (\'image/jpeg\',\'image/jpg\',\'image/png\') "+ " and "+ Images.Media.SIZE +" > 0 ", new String[] { "%cache%" }, Images.Media._ID
				+ " DESC");
	}

	@Override
	public void onResume() {
		super.onResume();
		

	}

	@Override
	public void onPause() {
		super.onPause();
	
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		AppContext.getApplication().clearBitmapErrorList() ;
	}

	public class TabListener implements TabIndicator.ITabSelectListener {

		@Override
		public void onTabSelected(TabView tab) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			Fragment fragment = null;
			Bundle bundle = null;

			switch (tab.getIndex()) {
			case 0: //camera roll tab
				attr.put(KEY_Photos_Selected, VALUE_YES);
				bundle = new Bundle();
				bundle.putSerializable("album", mCameraAlbum);
				bundle.putBoolean("useResStringName", true) ;
				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
				fragment = PhotoSelectFragment.newInstance(bundle);
				if(flowType.isPrintWorkFlow() || flowType.isWifiWorkFlow()){
					vImageButtonSelectAll.setVisibility(View.VISIBLE);
					vImageButtonInvertSelectAll.setVisibility(View.VISIBLE);
				}else {
					vImageButtonSelectAll.setVisibility(View.INVISIBLE);
					vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
				}

				break;
			case 1://albums tab
				attr.put(KEY_Photos_Selected, VALUE_YES);
				bundle = new Bundle();
				AlbumHolder albumHolder = new AlbumHolder();
				albumHolder.setAlbums(mAlbums);
				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
				bundle.putSerializable("albumsHolder", albumHolder);
				bundle.putString(AppConstants.KEY_PRODUCT_ID, productId);
				fragment = AlbumSelectFragment.newInstance(bundle);
				vImageButtonSelectAll.setVisibility(View.INVISIBLE);
				vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);

				break;
				
			case 2: //facebook tab
				
				
				

				if (!Connection.isConnected(PhotoSelectMainFragmentActivity.this)) {
					
					showNoConnectionDialog() ;

				} else {

					attr.put(KEY_Facebook_Selected, VALUE_YES);
					Session session = Session.getActiveSession();
					String token = session.getAccessToken();
					Log.v("sunny", "sunny session1:  " + session.getState());
					if (session.isOpened() && !TextUtils.isEmpty(token)) {
						// we can access data about your facebook account
						Log.v("sunny", "sunny:token onclick " + token);
						//TODO
						
						bundle = new Bundle();
						bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.FACEBOOK);
						bundle.putString(AppConstants.KEY_PRODUCT_ID, productId);
						fragment = FacebookSelectFragment.newInstance(bundle);
						vImageButtonSelectAll.setVisibility(View.INVISIBLE);
						vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
						
					} else {

//						facebookLogin();
						bundle = new Bundle();
						bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.FACEBOOK);
						bundle.putString(AppConstants.KEY_PRODUCT_ID, productId);
						bundle.putBoolean("isNeedFacebookLogin", true) ;
						fragment = FacebookSelectFragment.newInstance(bundle);
						vImageButtonSelectAll.setVisibility(View.INVISIBLE);
						vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);

					}
				}
			
				break;

			default:
				break;
			}

			if (null != fragment) {
				if(fm.getBackStackEntryCount()>0){
					
					fm.popBackStack(fm.getBackStackEntryAt(0).getId(),
	                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
				
				ft.replace(R.id.relativelayout_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//				ft.addToBackStack(null) ;
			
				ft.commit();
				
			    Log.v(TAG, "COUNT tab : "+fm.getBackStackEntryCount()) ;
			   
			}

			 vTextViewTitle.setText(tab.getText());

		}

		@Override
		public void onTabReselected(TabView tab) {
			if(tab.getIndex()==1 ){
				FragmentManager fm = getSupportFragmentManager();
				Fragment fragment = fm.findFragmentById(R.id.relativelayout_container) ;
				if(!(fragment instanceof AlbumSelectFragment)){
					fm.popBackStack();
					
				}
			}else if(tab.getIndex()==2){
				
				FragmentManager fm = getSupportFragmentManager();
				Fragment fragment = fm.findFragmentById(R.id.relativelayout_container) ;
				if(!(fragment instanceof FacebookSelectFragment)){
					
					fm.popBackStack(fm.getBackStackEntryAt(0).getId(),
	                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
				
			}

		}

	}
	
	/**
	 * facebook login
	 */
	
	public void facebookLogin(){
		Session session = Session.getActiveSession();
		Log.v("sunny", "sunny session "+session.getState()) ;
		List<String> permissions = Arrays.asList("public_profile" , "user_friends", "user_photos","friends_photos","user_status","friends_status");
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback)
					 .setPermissions(permissions));
		} else {
			Session.openActiveSession(this, true, permissions ,statusCallback);
		
		}
	}
	
	

	/**
	 * get all photos in the phone
	 * 
	 * @author sunny
	 * 
	 */
	private final class QueryPhotoHandler extends AsyncQueryHandler {
		private final WeakReference<PhotoSelectMainFragmentActivity> mActivity;

		public QueryPhotoHandler(Context context) {
			super(context.getContentResolver());
			mActivity = new WeakReference<PhotoSelectMainFragmentActivity>((PhotoSelectMainFragmentActivity) context);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			final PhotoSelectMainFragmentActivity activity = mActivity.get();
			if (activity != null && !activity.isFinishing()) {
				if (cursor != null) {
					if (mAllPhotosInPhone != null) {
						mAllPhotosInPhone.clear();
						mAllPhotosInPhone = null;
					}

					if (mAlbums != null) {
						mAlbums.clear();
						mAlbums = null;
					}
					mAllPhotosInPhone = new ArrayList<PhotoInfo>();
					mAlbums = new ArrayList<AlbumInfo>();

					try {
						while (cursor.moveToNext()) {
							PhotoInfo photoInfo = new PhotoInfo();
							String photoPath = cursor.getString(cursor.getColumnIndex(Images.Media.DATA));
							String photoId = cursor.getLong(cursor.getColumnIndex(Images.Media._ID)) + "";
							String bucketId = cursor.getString(cursor.getColumnIndex(Images.Media.BUCKET_ID));
							String bucketName = cursor.getString(cursor.getColumnIndex(Images.Media.BUCKET_DISPLAY_NAME));
							String localUri = Uri.withAppendedPath(Images.Media.EXTERNAL_CONTENT_URI, photoId).toString();

							photoInfo.setPhotoSource(PhotoSource.PHONE);
							photoInfo.setPhotoId(photoId);
							photoInfo.setPhotoPath(photoPath);
							photoInfo.setBucketId(bucketId);
							photoInfo.setBucketName(bucketName);

							photoInfo.setFlowType(AppContext.getApplication().getFlowType());
							photoInfo.setLocalUri(localUri);
							photoInfo.setProductId(productId == null ? "" : productId);
							photoInfo.setDescIdByPro(descrId == null ? "" : descrId);

							AlbumInfo album = new AlbumInfo();
							album.setmAlbumId(bucketId);
							String mAlbumPath = photoPath.substring(0, photoPath.lastIndexOf(File.separator));
							album.setmAlbumPath(mAlbumPath);
							if (!mAlbums.contains(album)) {
								album.setmAlbumName(bucketName);
								mAlbums.add(album);
							}
							mAllPhotosInPhone.add(photoInfo);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}

					
					String cameraPath = "" ;
					File cameraAlbum = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) ;
					if(cameraAlbum!=null){
						cameraPath = cameraAlbum.getAbsolutePath() ;
					}
					
					if(TextUtils.isEmpty(cameraPath)){
						cameraPath = "DCIM/Camera" ;
					}
					
//					mAllPhotosAlbum = new AlbumInfo(); // just a virtual album
//														// to store all photos
//					mAllPhotosAlbum.setmPhotosInAlbum(mAllPhotosInPhone);
//					mAllPhotosAlbum.setmAlbumId(getString(R.string.camera));
//					mAllPhotosAlbum.setmAlbumName(getString(R.string.camera)) ;
//					mAllPhotosAlbum.setPhotoNum(mAllPhotosInPhone.size());

					List<PhotoInfo> photosInAlbum = null;
					if (mAlbums != null && mAlbums.size() > 0) {
						
						for (AlbumInfo album : mAlbums) {
							if(mCameraAlbum==null){
								String albumPath = album.getmAlbumPath() ;
								if(albumPath!=null && !"".equals(albumPath)){
									if(albumPath.contains(cameraPath)){
										mCameraAlbum = album ;
									}
								}
							}
							photosInAlbum = new ArrayList<PhotoInfo>();
							for (PhotoInfo photoInfo : mAllPhotosInPhone) {
								if (photoInfo.getBucketId().equals(album.getmAlbumId())) {
									photosInAlbum.add(photoInfo);
								}
							}
							album.setmPhotosInAlbum(photosInAlbum);
							album.setCoverId(photosInAlbum.get(0).getPhotoId());
							album.setCoverPath(photosInAlbum.get(0).getPhotoPath()) ;
							album.setPhotoNum(photosInAlbum.size());
						}
					}

					// mAdapter.setDataSource(mAlbums) ;

				}
			} else {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}

			if (vProgressDialog != null && vProgressDialog.isShowing()) {
				vProgressDialog.cancel();
			}

			// May be handler will be better
			if(mCameraAlbum==null){
				mCameraAlbum = new AlbumInfo() ;
				mCameraAlbum.setmAlbumId(getString(R.string.camera));
				mCameraAlbum.setmAlbumName(getString(R.string.camera)) ;
				
			}
			
			Bundle bundle = new Bundle();
			bundle.putSerializable("album", mCameraAlbum);
			bundle.putBoolean("useResStringName", true) ;
			bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.PHONE);
			FragmentManager fm =  PhotoSelectMainFragmentActivity.this.getSupportFragmentManager() ;
			FragmentTransaction ft = fm.beginTransaction();
			Fragment newFragment = PhotoSelectFragment.newInstance(bundle);
			ft.replace(R.id.relativelayout_container, newFragment);
			ft.commit();
			Log.v(TAG, "COUNT 1--"+fm.getBackStackEntryCount() ) ;

		}

	}

	private void addWifiImagesInDb() {
		List<PhotoInfo> list = AppContext.getApplication().getmTempSelectedPhotos();
		imageSelDb.handleDeleteAllUrisWiFi();
		if (list != null && list.size() > 0) {

			String[] uris = new String[list.size()];
			String[] paths = new String[list.size()];
			for (int i = 0; i < list.size(); i++) {
				PhotoInfo info = list.get(i);
				uris[i] = info.getLocalUri();
				paths[i] = info.getPhotoPath();
			}

			imageSelDb.handleAddUrisWIFI(uris, paths);
		}
	}

	private class AddWifiImagesInDbTask extends AsyncTask<Void, Void, Object> {
		Runnable onPostExecute;
		ProgressDialog dialogSaving;

		public AddWifiImagesInDbTask(Runnable onPostExecute) {
			this.onPostExecute = onPostExecute;
		}

		@Override
		protected void onPreExecute() {
			dialogSaving = ProgressDialog.show(PhotoSelectMainFragmentActivity.this, "", getString(R.string.savingtaggedimages), true, true);
		}

		@Override
		protected Object doInBackground(Void... params) {
			addWifiImagesInDb();
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			dialogSaving.dismiss();
			if (onPostExecute != null) {
				onPostExecute.run();
			}
		}

	}

	
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            //do something with the token 
        	Session sessionResult = Session.getActiveSession() ;
        	
        	String token = sessionResult.getAccessToken() ;
        	Log.v("sunny", "sunny token 2: "+token) ;
            
            Log.v("sunny", "sunny session.isOpen : "+sessionResult.isOpened()) ;
        	if(sessionResult.isOpened()){
        		//TODO
//        		Intent intent  = new  Intent(PhotoSelectMainFragmentActivity.this, FacebookSelectActivity.class) ;
//        		Bundle bundle = new Bundle();
//				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.FACEBOOK);
//				bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
//				intent.putExtra("bundle", bundle);
//    			startActivity(intent);
    			
    			
    			FragmentManager fm = getSupportFragmentManager();
    			FragmentTransaction ft = fm.beginTransaction();
    			Fragment fragment = null;
    			Bundle bundle =new Bundle();
    			bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, PhotoSource.FACEBOOK);
				bundle.putString(AppConstants.KEY_PRODUCT_ID, productId) ;
    			fragment = FacebookSelectFragment.newInstance(bundle);
    			
                if(fm.getBackStackEntryCount()>0){
					
					fm.popBackStack(fm.getBackStackEntryAt(0).getId(),
	                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
                ft.replace(R.id.relativelayout_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			
				ft.commit();
				
			    Log.v(TAG, "COUNT tab2 : "+fm.getBackStackEntryCount()) ;
                
    			
        	}
        }
    }
	
	@Override
	public void repalceWithNewFragment(Fragment newFragment) {
		// TODO Auto-generated method stub
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.relativelayout_container, newFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null) ;
		ft.commit();
		
		Log.v(TAG, "COUNT replace   "+fm.getBackStackEntryCount() ) ;
		
		
		if(newFragment instanceof PhotoSelectFragment){
			
			if(flowType.isPrintWorkFlow() || flowType.isWifiWorkFlow()){
				vImageButtonSelectAll.setVisibility(View.VISIBLE);
				vImageButtonInvertSelectAll.setVisibility(View.VISIBLE);
			}else {
				vImageButtonSelectAll.setVisibility(View.INVISIBLE);
				vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
			}
			
		}else {
			vImageButtonSelectAll.setVisibility(View.INVISIBLE);
			vImageButtonInvertSelectAll.setVisibility(View.INVISIBLE);
		}
		
		
		
		
		
	}

	@Override
	public void setTitleText(String tilte) {

		vTextViewTitle.setText(tilte) ;
	}
	
	
	public void setLocalyticsEventAttr(String key ,String value){
		if(attr==null){
			attr = new HashMap<String, String>() ;
		}
		attr.put(key, value) ;
		
		
		
	}

}
