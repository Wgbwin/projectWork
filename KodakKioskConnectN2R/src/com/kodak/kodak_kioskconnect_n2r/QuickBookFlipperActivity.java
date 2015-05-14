package com.kodak.kodak_kioskconnect_n2r;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.aphidmobile.flip.FlipViewController;
import com.kodak.flip.CropView;
import com.kodak.flip.IconImageView;
import com.kodak.flip.OnCropViewOnlyClickListener;
import com.kodak.flip.PhotoBookPage;
import com.kodak.flip.PhotoDefinition;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.quickbook.QBPageDownloader;
import com.kodak.quickbook.database.ThumbnailProvider;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.RSSLocalytics;

public class QuickBookFlipperActivity extends BaseActivity {
	private final String TAG = this.getClass().getSimpleName();

	private Button order;
	private Button startOver;
	private TextView headerText;
	private TextView tvArrange;
	private TextView tvEnterTitle;
	private FlipViewController flipperView;
	private ImageButton ibAddImage;
	private Duplex_FlipperViewAdapter fvDupAdapter;
	private Simplex_FlipperViewAdapter fvSimAdapter;
	private RelativeLayout mNavigatorLayout;
	//private SharedPreferences prefs;
	private int width;
	private boolean uploading = false, isFromArrange = false,isFromShoppingCart = false;
	private int pageWidth, pageHeight;
	public Bitmap btp = null;
	public Bitmap wait_image = null;
	public QBPageDownloader qbPageDownloader;
	public static boolean pageUploading = false;
	private boolean hasLocked = false;
	public static CropView lastEditView = null;
	private String photobookName = "";

	private ProcessDialog statusDialog;

	public Duplex_FlipperViewAdapter getFvAdapter() {
		return fvDupAdapter;
	}

	private PopupWindow mPopupWindow;
	private final int LEFTDIRECTION = 5;
	private final int RIGHTDIRECTION = 6;

	private ProgressBar mCurrentProgress;
	private TextView mCurrentEditing;
	//private final int INITWAIT = 1;
	private final int INITIMAGE = 2;
	private int popHeight = 0;

	private int currentIndex = -1;
	//private boolean isShowQuickBook = true;

	private final int DELAY_MILLIS = 1500;
	private final static int REQUEST_ENTER_TITLE = 1;
	private final String SCREEN_NAME = "PB Preview";
	private final String YES = "yes";
	private final String NO = "no";
	private final String EVENT = "PB Edit Summary";
	private final String KEY_PAGE_DELETE = "PB Pages Deleted";
	private final String KEY_PAGE_MOVED = "PB Rearrange Used";
	private final String KEY_PAGE_ADD = "PB Pictures Added";
	private final String KEY_IMAGE_ROTATE = "PB Rotate Used";
	private final String KEY_EFFECT_USED = "PB Effects Used";
	private static boolean localyticsPageDeleted = false;
	private static boolean localyticsPageMoved = false;
	private static boolean localyticsAddPage = false;
	private static boolean localyticsRotate = false;

	private HashMap<String, String> attr;
	private Photobook photobook;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_ENTER_TITLE:
			if (resultCode == RESULT_OK) {

				PhotoBookPage titlePapge = null;
				for (PhotoBookPage photobookPage : photobook.photoBookPages) {
					if (photobookPage.sPhotoBookPageName.equalsIgnoreCase("Title")) {
						titlePapge = photobookPage;
						break;
					}
				}
				if (titlePapge != null) {
					ThumbnailProvider mProvider = ThumbnailProvider.obtainInstance(QuickBookFlipperActivity.this);
					mProvider.deleteMini(titlePapge.sPhotoBookPageID);
					titlePapge.isDownloading = false;
				}
				BaseAdapter adapter = (BaseAdapter) flipperView.getAdapter();
				adapter.notifyDataSetChanged();
				// use adapter.notifyDataSetChanged(); instead for
				// Rss-mobile-946,but it
				// seems that only could be a little better,QA said the issue is
				// still existed -- add by sunny on Feb 25th,2014
				// if(flipperView!=null && flipperView.getCards()!=null &&
				// flipperView.getCards().getFrontCards()!=null){
				// flipperView.postFlippedToView(flipperView.getCards().getFrontCards().getIndex());
				// }

			}

			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.quickbookflipper);
		setContentLayout(R.layout.quickbookflipperfields);
		if (savedInstanceState != null && savedInstanceState.containsKey("lock")) {
			hasLocked = savedInstanceState.getBoolean("lock");
		}
		initData();
		getViews();
		setEvents();
		
		if (isFromShoppingCart){
			displayQuickBook();
		}else{
			prepareQuickBook();
		}
		
	}

	private void refreshPopStatus() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			Log.i(TAG, "exception situation is executed!");
			mPopupWindow.dismiss();
		}
	}

	private void popEditWindow(int direction, int awidth, int height, final ArrayList<String> editItems, ViewHolder holder, FlipViewController fvc) {
		if (mPopupWindow != null) {
			mPopupWindow = null;
		}

		if (!editItems.isEmpty()) {
			PopEditAdapter popEditAdapter = new PopEditAdapter(editItems, holder, direction, fvc);
			ListView mPopListView = new ListView(QuickBookFlipperActivity.this);
			mPopListView.setBackgroundColor(Color.TRANSPARENT);
			mPopListView.setDivider(null);
			mPopListView.setAdapter(popEditAdapter);

			if (popHeight != 0) {
				mPopupWindow = new PopupWindow(mPopListView, 2 * width / 5, popHeight);
			} else {
				mPopupWindow = new PopupWindow(mPopListView, 2 * width / 5, height);
			}

			if (direction == RIGHTDIRECTION) {
				mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.windowdropdown5));
			} else {
				mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.windowdropdown4));
			}
			int heightMargin = mNavigatorLayout.getHeight() + startOver.getPaddingLeft() / 2;
			if (!pageUploading && !PrintHelper.isFlippingAnimation) {
				if (direction == RIGHTDIRECTION) {
					mPopupWindow.showAtLocation(flipperView, Gravity.LEFT | Gravity.TOP, width / 10, heightMargin);
					Log.i(TAG, "QQ holder.page.isEditing = " + holder.rightPage.isEditing());
				} else {
					mPopupWindow.showAtLocation(flipperView, Gravity.LEFT | Gravity.TOP, width / 2, heightMargin);
				}
			}
		}
	}

	class EditViewHolder {
		RelativeLayout mRelaEdit;
		TextView mTextView;
	}

	class PopEditAdapter extends BaseAdapter {

		ArrayList<String> editItems;
		ViewHolder holder;
		int direction;
		FlipViewController fvc;

		public PopEditAdapter(ArrayList<String> editItems, ViewHolder holder) {
			this.editItems = editItems;
			this.holder = holder;
		}

		public PopEditAdapter(ArrayList<String> editItems, ViewHolder holder, int direction) {
			this.editItems = editItems;
			this.holder = holder;
			this.direction = direction;
		}

		public PopEditAdapter(ArrayList<String> editItems, ViewHolder holder, int direction, FlipViewController fvc) {
			this.editItems = editItems;
			this.holder = holder;
			this.direction = direction;
			this.fvc = fvc;
		}

		@Override
		public int getCount() {
			return editItems.size();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			final EditViewHolder mEditViewHolder;
			if (convertView == null) {
				mEditViewHolder = new EditViewHolder();
				convertView = View.inflate(QuickBookFlipperActivity.this, R.layout.pop_edit_list, null);
				mEditViewHolder.mTextView = (TextView) convertView.findViewById(R.id.txt_deplay_specification);
				mEditViewHolder.mRelaEdit = (RelativeLayout) convertView.findViewById(R.id.rela_edit);
				RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.FILL_PARENT);
				if (direction == LEFTDIRECTION) {
					mLayoutParams.setMargins(25, 0, 0, 0);
				} else {
					mLayoutParams.setMargins(0, 0, 25, 0);
				}

				mEditViewHolder.mRelaEdit.setLayoutParams(mLayoutParams);
				convertView.setTag(mEditViewHolder);
			} else {
				mEditViewHolder = (EditViewHolder) convertView.getTag();
			}

			if (!editItems.isEmpty()) {
				String displayItem = editItems.get(position);
				mEditViewHolder.mTextView.setText(displayItem);

				mEditViewHolder.mRelaEdit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (position == 0) {
							localyticsRotate = true;
							if (direction == LEFTDIRECTION) {
								holder.leftPage.rotateBitmap();
							} else {
								holder.rightPage.rotateBitmap();
							}
						}
					}
				});
			}
			return convertView;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "onStart...");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop...");
	}

	@Override
	protected void onResume() {
		RSSLocalytics.onActivityResume(this);
		super.onResume();
		//isShowQuickBook = true;
		if (flipperView != null) {
			flipperView.onResume();
		}
		if (flipperView != null && flipperView.getCards() != null && flipperView.getCards().getFrontCards() != null) {
			flipperView.postFlippedToView(flipperView.getCards().getFrontCards().getIndex());
		}
	}

	@Override
	protected void onPause() {
		RSSLocalytics.onActivityPause(this);
		super.onPause();
		Log.i(TAG, "onPause.....");
		if (flipperView != null) {
			flipperView.onPause();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("lock", true);
		Log.i(TAG, "onsaveinstance");
		if (statusDialog != null && statusDialog.isShowing()) {
			PrintHelper.forward2Quick = true;
			hasLocked = false;
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart.....");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy.....");
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
		if (statusDialog != null && statusDialog.isShowing()) {
			if (!((Activity) this).isFinishing()) {
				statusDialog.dismiss();
			}

		}
		if (null != wait_image) {
			wait_image.recycle();
			wait_image = null;
		}
		if (null != btp) {
			btp.recycle();
			btp = null;
		}
		if (qbPageDownloader != null) {
			qbPageDownloader.cancelAllDownloads();
		}
		if (lastEditView != null) {
			lastEditView.recycleBitmap();
		}
		if (flipperView != null && flipperView.getCurrentView() != null) {
			flipperView.getCurrentView().recycleBitmap();
		}
		flipperView.destroyAdapter();
		System.gc();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			
			return false ;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void prepareQuickBook() {
		if (!isFromArrange) {
			if (!hasLocked || PrintHelper.forward2Quick) {
				if (statusDialog == null) {
					statusDialog = new ProcessDialog(this);
				}
				if (!((Activity) this).isFinishing()) {
					if (!statusDialog.isShowing()) {
						statusDialog.show();
						statusDialog.initDialog();
					}
				}
				ThumbnailProvider.obtainInstance(this).setWait_image(wait_image);
				if (!PictureUploadService2.isDoneUploadThumbnails) {
					uploading = true;
				}
				Log.d(TAG, "start loadPicture thread....");
				new Thread(checkIfDoneUploadPics).start();

			} else {
				waitingHandler.sendEmptyMessage(SHOW_QUICKBOOK);
			}
		} else {
			if (!PictureUploadService2.isDoneUploadThumbnails) {
				uploading = true;
			}
			Log.d(TAG, "start loadPicture thread....");
			new Thread(checkIfDoneUploadPics).start();
		}
	}

	Runnable checkIfDoneUploadPics = new Runnable() { // TODO

		@Override
		public void run() {
			try {
				waitingHandler.sendEmptyMessage(START_WAITING);
				if (!isFromArrange) {
					if (statusDialog != null) {
						statusDialog.statusHandler.sendEmptyMessage(ProcessDialog.PROGRESS_START);
					}
				}
				while (uploading && !PrintHelper.qbUploadThumbError) {
					try {
						Thread.sleep(500);
						waitingHandler.sendEmptyMessage(REFRESH_WAITING);
						if (statusDialog != null && statusDialog.isShowing()) {
							statusDialog.statusHandler.obtainMessage(ProcessDialog.PROGRESS_SENDING_IMAGE, PictureUploadService2.uploadingPhoto)
									.sendToTarget();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// TODO: need to add upload error handler
				if (PrintHelper.qbUploadThumbError) {
					PrintHelper.qbUploadThumbError = false;
					waitingHandler.sendEmptyMessage(UPLOADING_ERROR);
					return;
				}
				Log.d(TAG, "start downPictrue thread....");
				if (!isFromArrange) {
					new Thread(createAndLayoutPhotoBook).start();
				} else {
					int iPhotoPageNumber = 1;
					int iSe = 0;
					for (PhotoBookPage page : photobook.photoBookPages) {
						page.iSequenceNumber = ++iSe;
						if (page.bPhotoBookPageEditable) {
							page.sPhotoBookPageName = ++iPhotoPageNumber + "";
						}
						Log.e(TAG, "NO: " + page.iSequenceNumber + " PageID: " + page.sPhotoBookPageID + " PageName: " + page.sPhotoBookPageName
								+ " PageURL: " + page.sPhotoBookPageURL);
					}
					// PrintHelper.forward2Quick = false;
					waitingHandler.sendEmptyMessage(SHOW_QUICKBOOK);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					waitingHandler.sendEmptyMessage(STOP_WAITING);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};

	private int getAdditionalPageCount() {
		if (!PrintHelper.inQuickbook) {
			return 0;
		} else {
			if (photobook.additionalPageId.equals("")) {
				return 0;
			}
			int additionalPageCount = photobook.photoBookPages.size() - photobook.numberOfPagesPerBaseBook;
			for (int i = 0; i < photobook.photoBookPages.size(); i++) {
				if (photobook.photoBookPages.get(i).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)) {
					additionalPageCount--;
				}
			}
			return additionalPageCount;
		}
	}

	Runnable createAndLayoutPhotoBook = new Runnable() {

		@Override
		public void run() {
			try {
				PrintMakerWebService service = new PrintMakerWebService(QuickBookFlipperActivity.this, "");
				int count = 0;
				String result = "";
				AppContext app = AppContext.getApplication();
				if (statusDialog != null && statusDialog.isShowing()) {
					statusDialog.statusHandler.sendEmptyMessage(ProcessDialog.PROGRESS_ADDING_ITEM);
				}
				// insert image to photobook part
				if(photobook.photoBookPages!=null && photobook.photoBookPages.size()>0){
					localyticsAddPage = true;
					ArrayList<String> ids = new ArrayList<String>();
					for(PhotoInfo photo : photobook.selectedImages){
						boolean hasAdded = false;
						for(PhotoBookPage page : photobook.photoBookPages){
							if(page.PhotoBookPageImages!=null && page.PhotoBookPageImages.size()>0){
								for(PhotoDefinition def : page.PhotoBookPageImages){
									if(def!=null && (def.photoPath.equals(photo.getPhotoPath()) || photo.getPhotoPath().equals(photobook.titleImagePath))){
										hasAdded = true;
										break;
									}
								}
							}
						}
						if(!hasAdded){
							ids.add(photo.getContentId());
						}
					}
					if(ids.size() == 0){
						waitingHandler.sendEmptyMessage(SHOW_QUICKBOOK);
						return;
					}
					boolean succeed = false;
					for(String id : ids){
						ArrayList<String> contentId = new ArrayList<String>();
						contentId.add(id);
						succeed = service.insertPageWithContent2Task(photobook.id, contentId);
						if(!succeed){
							break;
						}
					}
					waitingHandler.sendEmptyMessage(SHOW_QUICKBOOK);
					if(succeed){
						Log.e(TAG, "insert pages with image succeed.");
					} else {
						Log.e(TAG, "insert pages with image failed.");
					}
					return;
				}
				count = 0;
				result = "";
				while (count < 5 && result.equals("") && (app.getPhotobook()!=null && !"".equals(app.getPhotobook().id))) {
					result = service.pbAddImageIDsToPhotoBook(QuickBookFlipperActivity.this);
					count++;
				}
				count = 0;
				result = "";
				while (count < 5 && result.equals("") && (app.getPhotobook()!=null && !"".equals(app.getPhotobook().id))) {
					result = service.pbLayoutPhotoBook(QuickBookFlipperActivity.this);
					count++;
				}
				Log.d(TAG, "done for AddImageIDs LayoutPhotobook");

				if (statusDialog != null && statusDialog.isShowing()) {
					statusDialog.statusHandler.sendEmptyMessage(ProcessDialog.PROGRESS_CREATING_TITLEPAGE);
				}
				if ((app.getPhotobook()!=null && !"".equals(app.getPhotobook().id))) {
					result = service.pbSetPhotoBookTitlePage(QuickBookFlipperActivity.this);
					Photobook photobook = app.getPhotobook();
					boolean isFirstToCreatePhotoBook = photobook.isFirstToCreatePhotoBook;
					if (!isFirstToCreatePhotoBook && photobook.canSetTitle && photobook.canSetSubtitle && photobook.canSetAuthor) {
						String titleInLocal = photobook.title;
						String authorInLocal = photobook.author;
						String subtitleInLocal = photobook.subTitle;
						service.pbSetTitle(QuickBookFlipperActivity.this, photobook.id, titleInLocal, authorInLocal, subtitleInLocal);
					}

				}

				Log.i(TAG, "453----- the result of pbSetPhotoBookTitlePage = " + result);
				// PrintHelper.forward2Quick = false;
				waitingHandler.sendEmptyMessage(SHOW_QUICKBOOK);
				PictureUploadService2.canUploadFullSize = true;
				Log.d(TAG, "Mark Upload Service Can start sending full size image");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				waitingHandler.sendEmptyMessage(STOP_WAITING);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	};

	private void showWaiting(float scale, float movex, float movey, float lastRotate) {
		// Log.d(TAG, "showWaiting....");
		if (mCurrentProgress != null) {
			mCurrentProgress.setVisibility(View.VISIBLE);
		}
		if(mCurrentEditing != null){
			mCurrentEditing.setVisibility(View.VISIBLE);
		}
		if (currentIndex != -1) {
			Log.i(TAG, "showWaiting scale sended = " + scale);
			currentIndex = -1;
		}
		order.setBackgroundResource(R.drawable.back_button);
	}

	private void hideWaiting() {
		if (mCurrentProgress != null) {
			mCurrentProgress.setVisibility(View.INVISIBLE);
		}
		if(mCurrentEditing != null){
			mCurrentEditing.setVisibility(View.INVISIBLE);
		}
		order.setEnabled(true);
		startOver.setEnabled(true);
		order.setBackgroundResource(R.drawable.next_button);
		tvArrange.setEnabled(true);
	}

	// include cover, inside cover, title, back inside, back
	private final int DUPLEX_OTHER_PAGENUM_ODD = 5;
	private final int DUPLEX_OTHER_PAGENUM_EVEN = 4;
	private final int SIMPLE_OTHER_PAGENUM = 4;

	private void displayQuickBook() {
		if (((Activity) this).isFinishing()) {
			Log.d(TAG, "isFinishing, do not displayQuickBook....");
			return;
		} else {
			Log.d(TAG, "displayQuickBook....");
		}
		Photobook photobook = AppContext.getApplication().getPhotobook();
		/*//if (PrintHelper.photoBookPages != null && prefs.getBoolean(PrintHelper.sIsDuplex, false)) {
		if (photobook.photoBookPages != null && photobook.isDuplex) {
			if ((PrintHelper.selectedImageUrls.size() % 2 == 0 && photobook.photoBookPages.size() != PrintHelper.selectedImageUrls.size()
					+ DUPLEX_OTHER_PAGENUM_EVEN)
					|| (PrintHelper.selectedImageUrls.size() % 2 == 1 && photobook.photoBookPages.size() != PrintHelper.selectedImageUrls.size()
							+ DUPLEX_OTHER_PAGENUM_ODD)) {
				showNorespondDialog();
				Log.i(TAG, "showNorespondDialogshowNorespondDialog from displayQuickBook " + "PrintHelper.photoBookPages.size() = "
						+ photobook.photoBookPages.size() + " , PrintHelper.selectedImageUrls.size()+4 = "
						+ (PrintHelper.selectedImageUrls.size() + 4));
				return;
			}
		} else if (PrintHelper.photoBookPages != null && !prefs.getBoolean(PrintHelper.sIsDuplex, false)
				&& PrintHelper.photoBookPages.size() != PrintHelper.selectedImageUrls.size() + SIMPLE_OTHER_PAGENUM) {
		else if (photobook.photoBookPages != null && !photobook.isDuplex
				&& photobook.photoBookPages.size() != PrintHelper.selectedImageUrls.size() + SIMPLE_OTHER_PAGENUM) {
			showNorespondDialog();
			Log.i(TAG, "showNorespondDialogshowNorespondDialog from displayQuickBook " + "PrintHelper.photoBookPages.size() = "
					+ photobook.photoBookPages.size() + " , PrintHelper.selectedImageUrls.size()+4 = " + (PrintHelper.selectedImageUrls.size() + 4));
			return;
		}*/

		System.gc();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		pageWidth = (int) photobook.width;
		pageHeight = (int) photobook.height;
		qbPageDownloader = QBPageDownloader.getInstance(this);
		if (!photobook.isDuplex) {
			fvSimAdapter = new Simplex_FlipperViewAdapter(this);
			flipperView.setAdapter(fvSimAdapter);
		} else {
			fvDupAdapter = new Duplex_FlipperViewAdapter(this);
			flipperView.setAdapter(fvDupAdapter);
		}
		flipperView.setVisibility(View.VISIBLE);
		// flip to title page directly

		flipperView.autoFlipAnimation(0);

		boolean isFirstToCreatePhotoBook = photobook.isFirstToCreatePhotoBook;
		if (isFirstToCreatePhotoBook) {
			photobook.isFirstToCreatePhotoBook = false;
			if (photobook.canSetTitle && photobook.canSetSubtitle && photobook.canSetAuthor) {
				waitingHandler.sendEmptyMessageDelayed(START_ENTER_TITLE, DELAY_MILLIS);
			}

		}

	}

	private void showNorespondDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				AppContext.getApplication().setPhotobook(null);
				Intent intent = new Intent(QuickBookFlipperActivity.this, ImageSelectionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				prepareQuickBook();
			}
		});
		builder.setCancelable(false);
		if (!QuickBookFlipperActivity.this.isFinishing()) {
			builder.create().show();
		}
	}

	private void showErrorDialg() {
		if (((Activity) this).isFinishing()) {
			Log.d(TAG, "isFinishing, do not displayQuickBook....");
			return;
		}
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (lastEditView != null) {
					lastEditView.netHandler.sendEmptyMessage(CropView.START_DOWNLOAD);
				}
				lastEditView = null;
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (lastEditView != null) {
					lastEditView.reUpload();
				}
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}

	public final static int START_WAITING = 100;
	public final static int STOP_WAITING = 101;
	public final static int REFRESH_WAITING = 102;
	public final static int SHOW_QUICKBOOK = 103;
	public final static int UPLOADING_ERROR = 104;
	public final static int UPLOAD_ROI_ERROR = 105;
	public final static int START_ENTER_TITLE = 106;
	public Handler waitingHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case START_WAITING:
				Float scale = 0.0f;
				Float movex = 0.0f;
				Float movey = 0.0f;
				Float lastRotate = 0.0f;
				if (msg.obj != null) {
					scale = ((Float[]) msg.obj)[0];
					movex = ((Float[]) msg.obj)[1];
					movey = ((Float[]) msg.obj)[2];
					lastRotate = ((Float[]) msg.obj)[3];
				}

				showWaiting(scale, movex, movey, lastRotate);
				break;
			case STOP_WAITING:
				hideWaiting();
				break;
			case REFRESH_WAITING:
				if (!PictureUploadService2.isDoneUploadThumbnails) {
					uploading = true;
				} else {
					uploading = false;
				}
				break;
			case SHOW_QUICKBOOK:
				if (!((Activity) QuickBookFlipperActivity.this).isFinishing()) {
					if (statusDialog != null && statusDialog.isShowing()) {
						statusDialog.dismiss();
					}
				}
				PrintHelper.forward2Quick = false;
				displayQuickBook();

				break;
			case UPLOADING_ERROR:
				if (!((Activity) QuickBookFlipperActivity.this).isFinishing()) {
					if (statusDialog != null && statusDialog.isShowing()) {
						try {
							statusDialog.dismiss();
							PrintHelper.forward2Quick = false;
						} catch (Exception e) {
						}

					}
				}
				//isShowQuickBook = false;
				showNorespondDialog();
				Log.i(TAG, "showNorespondDialogshowNorespondDialog from UPLOADING_ERROR");
				break;
			case UPLOAD_ROI_ERROR:
				showErrorDialg();
				break;
			case START_ENTER_TITLE:

				Intent intent = new Intent(QuickBookFlipperActivity.this, QuickBookEnterTitleActivity.class);
				Photobook photobook = AppContext.getApplication().getPhotobook();
				String photobookId = photobook==null ? "" : photobook.id;
				intent.putExtra("photobookid", photobookId);
				intent.putExtra("isAutoEnterTitle", true);
				startActivityForResult(intent, REQUEST_ENTER_TITLE);
				QuickBookFlipperActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
				break;
			}
		}

	};

	public void setupEvents() {
		

	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(QuickBookFlipperActivity.this);
			builder.setTitle(getString(R.string.lowResWarning));
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
				}
			});
			builder.create().show();
		}

	};

	public void downloadCancelled(String imageId) {
		for (PhotoBookPage page : photobook.photoBookPages) {
			if (page.sPhotoBookPageID.equals(imageId)) {
				page.isDownloading = false;
			}
		}
		Log.i(TAG, "download tasks cancelled! ID: " + imageId);
	}

	public void downloadFinish(String imageId) {
		for (PhotoBookPage page : photobook.photoBookPages) {
			if (page.sPhotoBookPageID.equals(imageId)) {
				page.isDownloading = false;
			}
		}
		if (flipperView != null && flipperView.getCards() != null && flipperView.getCards().getFrontCards() != null) {
			flipperView.postFlippedToView(flipperView.getCards().getFrontCards().getIndex());
		}
	}

	static class ViewHolder implements Serializable {
		RelativeLayout leftRelative;
		CropView leftPage;
		IconImageView leftEdit;
		IconImageView leftRotate;
		IconImageView leftWarning;
		TextView leftIndex;

		RelativeLayout rightRelative;
		CropView rightPage;
		IconImageView rightEdit;
		IconImageView rightRotate;
		IconImageView rightWarning;
		TextView rightIndex;

		ProgressBar mProgressBarLeft;
		ProgressBar mProgressBarRight;
		TextView mTxtEditingLeft;
		TextView mTxtEditingRight;
		int index;
	}

	public static final int SHOW_LEFT_WARNING = 1;
	public static final int SHOW_RIGHT_WARNING = 2;
	public static final int FADE_LEFT_WARNING = 3;
	public static final int FADE_RIGHT_WARNING = 4;
	public static final int REFRESH_POP_STATUS = 5;

	private class Duplex_FlipperViewAdapter extends BaseAdapter {
		boolean odd = false;
		private LayoutInflater mInflater;

		public Duplex_FlipperViewAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			int count;
			if (photobook.photoBookPages.size() == 0) {
				return 0;
			}
			if (photobook.photoBookPages.size() % 2 == 1) {
				odd = true;
				count = (photobook.photoBookPages.size() + 3) / 2;
			} else {
				odd = false;
				count = (photobook.photoBookPages.size() + 2) / 2;
			}
			
			return count;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private void setPhotoBookPage(int id, CropView view, TextView index) {
			view.setPrintSize(pageWidth, pageHeight);
			int total = getCount() * 2;
			Bitmap drawBitmap = null;
			PhotoBookPage page = null;
			String sIndex = "";
			if (id == 0 || id == total - 1) {
				drawBitmap = Bitmap.createBitmap(width / 2, width / 2, Config.ARGB_4444);
				Canvas canvas = new Canvas(drawBitmap);
				canvas.save();
			} else if (id == total - 4) {
				if (odd) {
					sIndex = (photobook.photoBookPages.size() - DUPLEX_OTHER_PAGENUM_ODD + 1) + "";
				} else {
					page = photobook.photoBookPages.get(id - 1);
					if (photobook.photoBookPages.get(id - 1).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)) {
						sIndex = "";
					} else {
						sIndex = photobook.photoBookPages.get(id - 1).sPhotoBookPageName;
					}
				}
			} else if (id == total - 3) {
				page = photobook.photoBookPages.get(photobook.photoBookPages.size() - 2);
				sIndex = getString(R.string.qb_page_inside_back_cover);
			} else if (id == total - 2) {
				page = photobook.photoBookPages.get(photobook.photoBookPages.size() - 1);
				sIndex = getString(R.string.qb_page_back_cover);
			} else {
				page = photobook.photoBookPages.get(id - 1);
				if (id - 1 == 0) {
					sIndex = getString(R.string.qb_page_cover);
				} else if (id - 1 == 1) {
					sIndex = getString(R.string.qb_page_inside_cover);
				} else if (id - 1 == 2) {
					sIndex = getString(R.string.qb_page_title);
				} else {
					sIndex = photobook.photoBookPages.get(id - 1).sPhotoBookPageName;
				}
			}

			if (drawBitmap != null) {
				view.setImageBitmap(drawBitmap, INITIMAGE);
			} else {
				view.setPhotoBookPage(page);
			}
			index.setText(sIndex);

		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final FlipViewController fvc = (FlipViewController) parent;
			int padding = startOver.getPaddingLeft();

			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.quickbook_layout, null);

				holder.leftRelative = (RelativeLayout) convertView.findViewById(R.id.leftRelative);
				holder.leftPage = (CropView) convertView.findViewById(R.id.image1);
				holder.leftEdit = (IconImageView) convertView.findViewById(R.id.leftEdit);
				holder.leftRotate = (IconImageView) convertView.findViewById(R.id.leftRotate);
				holder.leftWarning = (IconImageView) convertView.findViewById(R.id.leftWarning);
				holder.leftIndex = (TextView) convertView.findViewById(R.id.index1);

				holder.rightRelative = (RelativeLayout) convertView.findViewById(R.id.rightRelative);
				holder.rightPage = (CropView) convertView.findViewById(R.id.image2);
				holder.rightEdit = (IconImageView) convertView.findViewById(R.id.rightEdit);
				holder.rightRotate = (IconImageView) convertView.findViewById(R.id.rightRotate);
				holder.rightWarning = (IconImageView) convertView.findViewById(R.id.rightWarning);
				holder.rightIndex = (TextView) convertView.findViewById(R.id.index2);

				holder.mProgressBarLeft = (ProgressBar) convertView.findViewById(R.id.progressBar_left);
				holder.mProgressBarRight = (ProgressBar) convertView.findViewById(R.id.progressBar_right);
				holder.mTxtEditingLeft = (TextView) convertView.findViewById(R.id.txt_editing_left);
				holder.mTxtEditingRight = (TextView) convertView.findViewById(R.id.txt_editing_right);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (2 * position + 1 == 1 || 2 * position + 1 == getCount() * 2 - 1) {
				holder.rightPage.setNeedDrawShadow(false, CropView.RIGHTPAGE_SHADOW);
			} else {
				holder.rightPage.setNeedDrawShadow(true, CropView.RIGHTPAGE_SHADOW);
			}
			if (2 * position == 0 || 2 * position == getCount() * 2 - 2) {
				holder.leftPage.setNeedDrawShadow(false, CropView.LEFTPAGE_SHADOW);
			} else {
				holder.leftPage.setNeedDrawShadow(true, CropView.LEFTPAGE_SHADOW);
			}

			setPhotoBookPage(2 * position, holder.leftPage, holder.leftIndex);
			setPhotoBookPage(2 * position + 1, holder.rightPage, holder.rightIndex);

			holder.leftPage.setController(fvc);
			holder.rightPage.setController(fvc);

			holder.leftEdit.setVisibility(View.INVISIBLE);
			holder.leftRotate.setVisibility(View.INVISIBLE);
			holder.rightEdit.setVisibility(View.INVISIBLE);
			holder.rightRotate.setVisibility(View.INVISIBLE);

			for (int i = width / 2 - padding; i > 0; i--) {
				if (i % pageWidth == 0) {
					if (i * pageHeight % pageWidth == 0) {
						padding = width / 2 - i;
						break;
					}
				}
			}
			ViewGroup.LayoutParams leftRelLp = holder.leftRelative.getLayoutParams();
			leftRelLp.width = width / 2 - padding;
			leftRelLp.height = (int) (leftRelLp.width / pageWidth * pageHeight) + 50;
			holder.leftRelative.setLayoutParams(leftRelLp);

			ViewGroup.LayoutParams rightRelLp = holder.leftRelative.getLayoutParams();
			rightRelLp.width = width / 2 - padding;
			rightRelLp.height = (int) (rightRelLp.width / pageWidth * pageHeight) + 50;
			holder.rightRelative.setLayoutParams(rightRelLp);

			ViewGroup.LayoutParams leftLP = (LayoutParams) holder.leftPage.getLayoutParams();

			leftLP.width = width / 2 - padding;
			leftLP.height = (int) (leftLP.width / pageWidth * pageHeight);
			holder.leftPage.setLayoutParams(leftLP);
			ViewGroup.LayoutParams rightLP = (LayoutParams) holder.rightPage.getLayoutParams();

			rightLP.width = leftLP.width;
			rightLP.height = leftLP.height;
			popHeight = leftLP.height;
			holder.rightPage.setLayoutParams(rightLP);
			convertView.setPadding(padding, padding / 2, padding, padding);

			holder.leftPage.showWarning = SHOW_LEFT_WARNING;
			holder.leftPage.fadeWarning = FADE_LEFT_WARNING;
			holder.rightPage.showWarning = SHOW_RIGHT_WARNING;
			holder.rightPage.fadeWarning = FADE_RIGHT_WARNING;

			Handler warningHandler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case SHOW_LEFT_WARNING:
						holder.leftWarning.setVisibility(View.VISIBLE);
						break;
					case FADE_LEFT_WARNING:
						holder.leftWarning.setVisibility(View.INVISIBLE);
						break;
					case SHOW_RIGHT_WARNING:
						holder.rightWarning.setVisibility(View.VISIBLE);
						break;
					case FADE_RIGHT_WARNING:
						holder.rightWarning.setVisibility(View.INVISIBLE);
						break;
					case REFRESH_POP_STATUS:
						refreshPopStatus();
						break;
					}
				}

			};

			holder.leftPage.warningHandler = warningHandler;
			holder.rightPage.warningHandler = warningHandler;

			holder.leftPage.setmClickListener(new OnCropViewOnlyClickListener() {

				@Override
				public void onOnlyClick() {
					
				}
			});

			holder.leftPage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (pageUploading || PrintHelper.isFlippingAnimation) {
						return;
					} else {
						if (holder.rightPage.getEditMode()) {
							if (mPopupWindow != null && mPopupWindow.isShowing()) {
								mPopupWindow.dismiss();
							}
							mCurrentProgress = holder.mProgressBarRight;
							mCurrentEditing = holder.mTxtEditingRight;
							fvc.hasPageSelected = false;
							fvc.setCurrentView(null);
							holder.rightPage.setEditMode(false);
							holder.rightPage.setmIsEditingMode(false);
							return;
						} else if (holder.leftPage.getEditMode()) {
							if (mPopupWindow != null && mPopupWindow.isShowing()) {
								mPopupWindow.dismiss();
							}
							mCurrentProgress = holder.mProgressBarLeft;
							mCurrentEditing = holder.mTxtEditingLeft;
							fvc.hasPageSelected = false;
							fvc.setCurrentView(null);
							holder.leftPage.setEditMode(false);
							holder.leftPage.setmIsEditingMode(false);
							return;
						} else {
							Log.i(TAG, "pop position = " + position + " , PrintHelper.flipIndex  = " + PrintHelper.flipIndex);
							if (holder.leftPage.getPage() != null && holder.leftPage.getPage().bPhotoBookPageEditable
									&& !PrintHelper.isFlippingAnimation && position == PrintHelper.flipIndex) {
								if (holder.leftPage.ismIsEditingMode()) {
								} else {
									order.setEnabled(false);
									startOver.setEnabled(false);
									order.setBackgroundResource(R.drawable.back_button);
									fvc.hasPageSelected = true;
									fvc.setCurrentView(holder.leftPage);
									currentIndex = Integer.parseInt(holder.leftIndex.getText().toString());
									lastEditView = holder.leftPage;
									boolean hasBitmap = holder.leftPage.setEditMode(true);
									holder.leftPage.setmIsEditingMode(true);

									if (mPopupWindow == null || !mPopupWindow.isShowing()) {
										if (holder.leftPage.isEditing()) {
											ArrayList<String> editItems = new ArrayList<String>();
											editItems.add(getString(R.string.rotate));
											popEditWindow(LEFTDIRECTION, 220, 260, editItems, holder, fvc);
										}
									} else {
										mPopupWindow.dismiss();
									}
									
									if(!hasBitmap){
										mCurrentProgress = holder.mProgressBarRight;
										mCurrentEditing = null;
										mPopupWindow.dismiss();
										new DownloadBitmapFromWeb(QuickBookFlipperActivity.this, fvc, holder.leftPage).execute(holder.leftPage.getItem().photoInfo);
									}
								}
							}
						}
					}
				}
			});

			holder.leftWarning.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					handler.sendEmptyMessage(0);
				}
			});

			holder.rightPage.setmClickListener(new OnCropViewOnlyClickListener() {

				@Override
				public void onOnlyClick() {
					Log.i(TAG, "holder.rightPage.setmClickListener is executed");
				}
			});

			holder.rightPage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.i(TAG, "holder.rightPage.setOnClickListener is executed");
					if (pageUploading || PrintHelper.isFlippingAnimation) {
						return;
					} else {
						if (holder.leftPage.getEditMode()) {
							if (mPopupWindow != null && mPopupWindow.isShowing()) {
								mPopupWindow.dismiss();
							}
							mCurrentProgress = holder.mProgressBarLeft;
							mCurrentEditing = holder.mTxtEditingLeft;
							fvc.hasPageSelected = false;
							fvc.setCurrentView(null);
							holder.leftPage.setEditMode(false);
							holder.leftPage.setmIsEditingMode(false);
							return;
						} else if (holder.rightPage.getEditMode()) {
							if (mPopupWindow != null && mPopupWindow.isShowing()) {
								mPopupWindow.dismiss();
							}
							mCurrentProgress = holder.mProgressBarRight;
							mCurrentEditing = holder.mTxtEditingRight;
							fvc.hasPageSelected = false;
							fvc.setCurrentView(null);
							holder.rightPage.setEditMode(false);
							holder.rightPage.setmIsEditingMode(false);
							return;
						} else {
							Log.i(TAG, "pop position = " + position + " , PrintHelper.flipIndex  = " + PrintHelper.flipIndex);
							if (holder.rightPage.getPage() != null && holder.rightPage.getPage().bPhotoBookPageEditable
									&& !PrintHelper.isFlippingAnimation && position == PrintHelper.flipIndex) {
								if (holder.rightPage.ismIsEditingMode()) {
								} else {
									order.setEnabled(false);
									startOver.setEnabled(false);
									order.setBackgroundResource(R.drawable.back_button);
									fvc.hasPageSelected = true;
									fvc.setCurrentView(holder.rightPage);
									lastEditView = holder.rightPage;
									boolean hasBitmap = holder.rightPage.setEditMode(true);
									holder.rightPage.setmIsEditingMode(true);

									if (mPopupWindow == null || !mPopupWindow.isShowing()) {
										if (holder.rightPage.isEditing()) {
											ArrayList<String> editItems = new ArrayList<String>();
											editItems.add(getString(R.string.rotate));
											popEditWindow(RIGHTDIRECTION, 220, 260, editItems, holder, fvc);
										}
									} else {
										mPopupWindow.dismiss();
									}
									
									if(!hasBitmap){
										mCurrentProgress = holder.mProgressBarLeft;
										mCurrentEditing = null;
										new DownloadBitmapFromWeb(QuickBookFlipperActivity.this, fvc, holder.rightPage).execute(holder.rightPage.getItem().photoInfo);
									}
								}
							}
						}
					}
				}
			});

			holder.rightWarning.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					handler.sendEmptyMessage(0);
				}
			});

			return convertView;
		}

	}

	private class Simplex_FlipperViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public Simplex_FlipperViewAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (photobook.photoBookPages.size() == 0) {
				return 0;
			}
			return photobook.photoBookPages.size() - 1;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private void setPhotoBookPage(int id, CropView view, TextView index) {
			view.setPrintSize(pageWidth, pageHeight);
			int total = getCount() * 2;
			Bitmap drawBitmap = null;
			PhotoBookPage page = null;
			String sIndex = "";
			if (id == 0 || id == total - 1) {
				drawBitmap = Bitmap.createBitmap(width / 2, width / 2, Config.ARGB_4444);
				Canvas canvas = new Canvas(drawBitmap);
				canvas.save();
			} else if (id == 1 || id == 2) {
				page = photobook.photoBookPages.get(id - 1);
				if (id == 1) {
					sIndex = getString(R.string.qb_page_cover);
				} else if (id == 2) {
					sIndex = getString(R.string.qb_page_inside_cover);
				}
			} else if (id == total - 2) {
				page = photobook.photoBookPages.get(photobook.photoBookPages.size() - 1);
				sIndex = getString(R.string.qb_page_back_cover);
			} else if (id == total - 3) {
				page = photobook.photoBookPages.get(photobook.photoBookPages.size() - 2);
				sIndex = getString(R.string.qb_page_inside_back_cover);
			} else if (id % 2 == 0) {
				sIndex = "";
			} else {
				page = photobook.photoBookPages.get((id + 1) / 2);
				if ((id + 1) / 2 == 2) {
					sIndex = getString(R.string.qb_page_title);
				} else {
					sIndex = photobook.photoBookPages.get((id + 1) / 2).sPhotoBookPageName;
				}
			}

			if (drawBitmap != null) {
				view.setImageBitmap(drawBitmap, INITIMAGE);
			} else {
				view.setPhotoBookPage(page);
			}

			index.setText(sIndex);

		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final FlipViewController fvc = (FlipViewController) parent;
			int padding = startOver.getPaddingLeft();

			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.quickbook_layout, null);

				holder.leftRelative = (RelativeLayout) convertView.findViewById(R.id.leftRelative);
				holder.leftPage = (CropView) convertView.findViewById(R.id.image1);
				holder.leftWarning = (IconImageView) convertView.findViewById(R.id.leftWarning);
				holder.leftIndex = (TextView) convertView.findViewById(R.id.index1);

				holder.rightRelative = (RelativeLayout) convertView.findViewById(R.id.rightRelative);
				holder.rightPage = (CropView) convertView.findViewById(R.id.image2);
				holder.rightWarning = (IconImageView) convertView.findViewById(R.id.rightWarning);
				holder.rightIndex = (TextView) convertView.findViewById(R.id.index2);

				holder.mProgressBarLeft = (ProgressBar) convertView.findViewById(R.id.progressBar_left);
				holder.mProgressBarRight = (ProgressBar) convertView.findViewById(R.id.progressBar_right);
				holder.mTxtEditingLeft = (TextView) convertView.findViewById(R.id.txt_editing_left);
				holder.mTxtEditingRight = (TextView) convertView.findViewById(R.id.txt_editing_right);
				holder.index = position;

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.index = position;
			}

			if (2 * position + 1 == 1 || 2 * position + 1 == getCount() * 2 - 1) {
				holder.rightPage.setNeedDrawShadow(false, CropView.RIGHTPAGE_SHADOW);
			} else {
				holder.rightPage.setNeedDrawShadow(true, CropView.RIGHTPAGE_SHADOW);
			}
			if (2 * position == 2) {
				holder.leftPage.setNeedDrawShadow(true, CropView.LEFTPAGE_SHADOW);
			}
			setPhotoBookPage(2 * position, holder.leftPage, holder.leftIndex);
			setPhotoBookPage(2 * position + 1, holder.rightPage, holder.rightIndex);

			holder.leftPage.setController(fvc);
			holder.rightPage.setController(fvc);

			for (int i = width / 2 - padding; i > 0; i--) {
				if (i % pageWidth == 0) {
					if (i * pageHeight % pageWidth == 0) {
						padding = width / 2 - i;
						break;
					}
				}
			}
			ViewGroup.LayoutParams leftRelLp = holder.leftRelative.getLayoutParams();
			leftRelLp.width = width / 2 - padding;
			leftRelLp.height = (int) (leftRelLp.width / pageWidth * pageHeight) + 50;
			holder.leftRelative.setLayoutParams(leftRelLp);

			ViewGroup.LayoutParams rightRelLp = holder.leftRelative.getLayoutParams();
			rightRelLp.width = width / 2 - padding;
			rightRelLp.height = (int) (rightRelLp.width / pageWidth * pageHeight) + 50;
			holder.rightRelative.setLayoutParams(rightRelLp);

			ViewGroup.LayoutParams leftLP = (LayoutParams) holder.leftPage.getLayoutParams();

			leftLP.width = width / 2 - padding;
			leftLP.height = (int) (leftLP.width / pageWidth * pageHeight);
			holder.leftPage.setLayoutParams(leftLP);
			ViewGroup.LayoutParams rightLP = (LayoutParams) holder.rightPage.getLayoutParams();

			rightLP.width = leftLP.width;
			rightLP.height = leftLP.height;
			popHeight = leftLP.height;
			holder.rightPage.setLayoutParams(rightLP);
			convertView.setPadding(padding, padding / 2, padding, padding);

			holder.leftPage.showWarning = SHOW_LEFT_WARNING;
			holder.leftPage.fadeWarning = FADE_LEFT_WARNING;
			holder.rightPage.showWarning = SHOW_RIGHT_WARNING;
			holder.rightPage.fadeWarning = FADE_RIGHT_WARNING;

			Handler warningHandler = new Handler() {

				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case SHOW_LEFT_WARNING:
						holder.leftWarning.setVisibility(View.VISIBLE);
						break;
					case FADE_LEFT_WARNING:
						holder.leftWarning.setVisibility(View.INVISIBLE);
						break;
					case SHOW_RIGHT_WARNING:
						holder.rightWarning.setVisibility(View.VISIBLE);
						break;
					case FADE_RIGHT_WARNING:
						holder.rightWarning.setVisibility(View.INVISIBLE);
						break;
					case REFRESH_POP_STATUS:
						refreshPopStatus();
						break;
					}
				}

			};

			holder.leftPage.warningHandler = warningHandler;
			holder.rightPage.warningHandler = warningHandler;

			holder.leftPage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mPopupWindow != null && mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					}

					if (holder.rightPage.getEditMode()) {
						mCurrentProgress = holder.mProgressBarRight;
						mCurrentEditing = holder.mTxtEditingRight;
						fvc.hasPageSelected = false;
						fvc.setCurrentView(null);
						holder.rightPage.setEditMode(false);
						holder.rightPage.setmIsEditingMode(false);
						return;
					}

					if (holder.leftPage.getEditMode()) {
						fvc.hasPageSelected = false;
						fvc.setCurrentView(null);
					} else {
						fvc.hasPageSelected = false;
						fvc.setCurrentView(null);
						fvc.postFlippedToView(fvc.getCards().getFrontCards().getIndex());
					}
				}
			});

			holder.leftWarning.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					handler.sendEmptyMessage(0);
				}
			});

			holder.rightPage.setmClickListener(new OnCropViewOnlyClickListener() {

				@Override
				public void onOnlyClick() {

				}
			});

			holder.rightPage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (pageUploading || PrintHelper.isFlippingAnimation) {
						return;
					} else {
						if (holder.rightPage.getPage() != null && holder.rightPage.getPage().bPhotoBookPageEditable
								&& !PrintHelper.isFlippingAnimation && position == PrintHelper.flipIndex) {
							if (mPopupWindow != null && mPopupWindow.isShowing()) {
								mPopupWindow.dismiss();
							}
							if (holder.rightPage.ismIsEditingMode()) {
								mCurrentProgress = holder.mProgressBarRight;
								mCurrentEditing = holder.mTxtEditingRight;
								fvc.hasPageSelected = false;
								fvc.setCurrentView(null);
								holder.rightPage.setEditMode(false);
								holder.rightPage.setmIsEditingMode(false);
								return;
							} else {
								order.setEnabled(false);
								startOver.setEnabled(false);
								order.setBackgroundResource(R.drawable.back_button);
								fvc.hasPageSelected = true;
								fvc.setCurrentView(holder.rightPage);
								lastEditView = holder.rightPage;
								boolean hasBitmap = holder.rightPage.setEditMode(true);
								holder.rightPage.setmIsEditingMode(true);

								if (mPopupWindow == null || !mPopupWindow.isShowing()) {
									if (holder.rightPage.isEditing()) {
										ArrayList<String> editItems = new ArrayList<String>();
										editItems.add(getString(R.string.rotate));
										popEditWindow(RIGHTDIRECTION, 220, 260, editItems, holder, fvc);
									}
								} else {
									mPopupWindow.dismiss();
								}
								
								if(!hasBitmap){
									mCurrentProgress = holder.mProgressBarRight;
									mCurrentEditing = null;
									new DownloadBitmapFromWeb(QuickBookFlipperActivity.this, fvc, holder.rightPage).execute(holder.rightPage.getItem().photoInfo);
								}
							}
						}
					}
				}
			});

			holder.rightWarning.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					handler.sendEmptyMessage(0);
				}
			});
			return convertView;
		}

	}

	class ProcessDialog extends ProgressDialog {
		private TextView tvStatus;
		private ImageView ivUploading;
		private ProgressBar pbProgress;
		private Bitmap sendingImage;
		private boolean lock = false;

		public ProcessDialog(Context context) {
			super(context);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			Log.i(TAG, "onCreate...");
			setContentView(R.layout.quickbook_process_dialog);
			tvStatus = (TextView) findViewById(R.id.statusTV);
			ivUploading = (ImageView) findViewById(R.id.uploading_image);
			pbProgress = (ProgressBar) findViewById(R.id.statusPB);
			tvStatus.setTypeface(PrintHelper.tf);
			pbProgress.setMax(4);
			this.setCancelable(false);
		}

		private void initDialog() {
			initDialogSize();
			// setupEvents();
		}

		private void initDialogSize() {
			RelativeLayout dialog_LinearLayout = (RelativeLayout) findViewById(R.id.quickbook_process_dialog_relatLay);
			ViewGroup.LayoutParams dialogLp = dialog_LinearLayout.getLayoutParams();
			Display display = getWindowManager().getDefaultDisplay();
			dialogLp.height = display.getHeight() * 3 / 4;
			dialogLp.width = display.getWidth() * 2 / 3;
			dialog_LinearLayout.setLayoutParams(dialogLp);

			initImageViewSize(dialogLp.height, dialogLp.width);
		}

		private void initImageViewSize(int dialogWidth, int dialogHeight) {
			if (ivUploading != null) {
				ViewGroup.LayoutParams params = ivUploading.getLayoutParams();
				params.height = dialogHeight * 1 / 2;
				params.width = dialogWidth / 2;
				ivUploading.setLayoutParams(params);
			}
		}

		static final int PROGRESS_START = 0;
		static final int PROGRESS_SENDING_IMAGE = 1;
		static final int PROGRESS_ADDING_ITEM = 2;
		static final int PROGRESS_CREATING_TITLEPAGE = 3;
		Handler statusHandler = new Handler() {
			//private String currentURI = "";
			private PhotoInfo currentPhoto = null;

			@Override
			public void handleMessage(Message msg) {
				int action = msg.what;
				//String imageURI = "";
				PhotoInfo photoInfo = null;
				if (msg.obj != null)
					photoInfo = (PhotoInfo) msg.obj;

				switch (action) {
				case PROGRESS_START:
					// Log.w(TAG, "Status Handler: Progress Start....");
					tvStatus.setText(getString(R.string.animation_quickbook_wait));
					pbProgress.setProgress(1);
					ivUploading.setVisibility(View.GONE);
					break;
				case PROGRESS_SENDING_IMAGE:
					// Log.w(TAG, "Status Handler: Progress Sending Image....");
					if(photoInfo != null){
						/*int currentNumber = 0;
						for (int i = 0; i < photobook.selectedImages.size(); i++) {
							//if (imageURI.equals(photobook.selectedImages.get(i).getLocalUri())) {
							if(photoInfo.equals(photobook.selectedImages.get(i))){
								currentNumber = i + 1;
								break;
							}
						}*/
						refreshImage(photoInfo);
					}
					
					tvStatus.setText(getString(R.string.animation_quickbook_addingphotos));
					ivUploading.setVisibility(View.VISIBLE);
					pbProgress.setProgress(2);
					break;
				case PROGRESS_ADDING_ITEM:
					// Log.w(TAG, "Status Handler: Progress Adding Item....");
					tvStatus.setText(getString(R.string.animation_quickbook_create));
					ivUploading.setVisibility(View.GONE);
					pbProgress.setProgress(3);
					break;
				case PROGRESS_CREATING_TITLEPAGE:
					// Log.w(TAG,
					// "Status Handler: Progress Creating Titlepage....");
					tvStatus.setText(getString(R.string.animation_quickbook_titlepage));
					ivUploading.setVisibility(View.GONE);
					pbProgress.setProgress(4);
					break;
				}
			}

			private void refreshImage(PhotoInfo photo) {
				if (!photo.equals(currentPhoto)) {
					currentPhoto = photo;
					sendingImage = getUploadingImageThumb(currentPhoto);
					ivUploading.setImageBitmap(sendingImage);
				}
			}

			private Bitmap getUploadingImageThumb(PhotoInfo photo) {
				return ImageUtil.getBitmapOfPhotoInfo(photo, QuickBookFlipperActivity.this);
			}

		};

	}

	/**
	 * check the low warning for quick book if the low warning should show
	 * "PrintHelper.ifQuickBookLowWarningShow" it will used in the
	 * shoppingCarActivity
	 */
	public void checkQuickBookLowWarning() {
		photobook.isLowResWarningShow = false;
		ProductInfo itemTemp = null;
		int pageIndex = 0;// the index of photo.when the index is
							// 0,1,total-1,total-2:the photo will not check.
		int totalPage = photobook.photoBookPages.size();
		for (PhotoBookPage page : photobook.photoBookPages) {
			if (!photobook.isLowResWarningShow) {
				if (pageIndex > 1 && pageIndex < totalPage - 2) {
					PhotoInfo photoInfo = null;
					if (page != null && page.PhotoBookPageImages != null && page.PhotoBookPageImages.size() > 0
							&& page.PhotoBookPageImages.get(0) != null && page.PhotoBookPageImages.get(0).photoPath != null) {
						itemTemp = new ProductInfo(QuickBookFlipperActivity.this);
						itemTemp.width = pageWidth + "";
						itemTemp.height = pageHeight + "";
						for(PhotoInfo pi : photobook.selectedImages){
							if(page.PhotoBookPageImages.get(0).photoPath.equals(pi.getPhotoPath())){
								photoInfo = pi;
								break;
							}
						}
						itemTemp.roi = page.PhotoBookPageImages.get(0).croproi;
					}

					// this part to check whether show low res waring
					if (itemTemp != null && photoInfo!=null) {
						double scaleX = itemTemp.roi.w / itemTemp.roi.ContainerW;
						double scaleY = itemTemp.roi.h / itemTemp.roi.ContainerH;
						if (PrintHelper.isLowResWarning(itemTemp, photoInfo, scaleX, scaleY)) {
							photobook.isLowResWarningShow = true;
							break;
						}
					}
				}
			}
			pageIndex++;
		}
	}

	private String getFilePath(String strUri) {
		Uri uri = Uri.parse(strUri);
		ContentResolver cr = QuickBookFlipperActivity.this.getContentResolver();
		String[] poj = { MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(uri, poj, null, null, null);
		String filePath = "";
		try {
			cursor.moveToFirst();
			filePath = cursor.getString(0);
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return filePath;
	}

	@Override
	public void getViews() {
		DisplayMetrics dm = new DisplayMetrics();
		mNavigatorLayout = (RelativeLayout) findViewById(R.id.main_navbar);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = getWindowManager().getDefaultDisplay().getWidth();
		//deviceWidth = dm.widthPixels;
		//deviceHeight = dm.heightPixels;
		order = (Button) findViewById(R.id.next_btn);
		startOver = (Button) findViewById(R.id.back_btn);
		headerText = (TextView) findViewById(R.id.headerBar_tex);
		tvArrange = (TextView) findViewById(R.id.arrange_tex);
		tvEnterTitle = (TextView) findViewById(R.id.entertitle_tex);
		ibAddImage = (ImageButton) findViewById(R.id.pb_add_imagebtn);
		flipperView = (FlipViewController) findViewById(R.id.flipperView);
		
		order.setVisibility(View.VISIBLE);
		tvArrange.setVisibility(View.VISIBLE);
		tvEnterTitle.setVisibility(View.VISIBLE);
		flipperView.setVisibility(View.INVISIBLE);
		headerText.setVisibility(View.VISIBLE);
		ibAddImage.setVisibility(View.VISIBLE);
		
		headerText.setText(photobookName);
		order.setText(getString(R.string.cart));
	}

	@Override
	public void initData() {
		//prefs = PreferenceManager.getDefaultSharedPreferences(QuickBookFlipperActivity.this);
		photobook = AppContext.getApplication().getPhotobook();
		PrintHelper.flipIndex = -1;
		
		for (PhotoBookPage page : photobook.photoBookPages) {
			if (page != null) {
				page.isDownloading = false;
			}
		}
		if (getIntent() != null){
			localyticsPageDeleted = getIntent().getBooleanExtra("ldelete", false);
			localyticsPageMoved = getIntent().getBooleanExtra("lmoved", false);
			if(getIntent().getExtras() != null){
				isFromArrange = getIntent().getExtras().getBoolean("isFromArrange", false);
				isFromShoppingCart = getIntent().getExtras().getBoolean(AppConstants.IS_FORM_SHOPPINGCART, false);
			}
		} else {
			// if code run in this part, that means this screen is not from AddImage/Arrage
			localyticsAddPage = false;
			localyticsPageDeleted = false;
			localyticsPageMoved = false;
			localyticsRotate = false;
		}
		PrintHelper.hasQuickbook = true;
		
		String packName = getApplication().getPackageName();
		if (packName.contains("com.kodak.dm.rsscombinedapp")) {
			wait_image = PrintHelper.readBitMap(this, R.drawable.image_wait_6x8);
		} else {
			wait_image = PrintHelper.readBitMap(this, R.drawable.image_wait_4x6);
		}
		btp = PrintHelper.readBitMap(this, R.drawable.quickbook_6x4_back_page);
		
		List<PrintProduct> products = PrintHelper.products;
		if(products != null){
			for(PrintProduct pro : products){
				if(pro.getId().equals(photobook.proDescId)){
					photobookName = pro.getShortName();
				}
			}
		}
		
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		attr = new HashMap<String, String>();
	}

	@Override
	public void setEvents() {
		tvArrange.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(QuickBookFlipperActivity.this, ArrangeActivity.class);
				mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mIntent);
				finish();
			}
		});

		tvEnterTitle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(QuickBookFlipperActivity.this, QuickBookEnterTitleActivity.class);
				Photobook photobook = AppContext.getApplication().getPhotobook();
				String photobookId = photobook==null ? "" : photobook.id;
				intent.putExtra("photobookid", photobookId);
				startActivityForResult(intent, REQUEST_ENTER_TITLE);
				QuickBookFlipperActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

			}
		});

		ibAddImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (qbPageDownloader != null) {
					qbPageDownloader.cancelAllDownloads();
				}
				List<PhotoInfo> mTempPhotoList = AppContext.getApplication().getmTempSelectedPhotos();
				mTempPhotoList.clear() ;
				for(PhotoInfo photo : photobook.selectedImages){
					mTempPhotoList.add(photo);
				}
//				Intent mIntent = new Intent(QuickBookFlipperActivity.this, PhotoSourceSelectMainActivity.class);
				Intent mIntent = new Intent(QuickBookFlipperActivity.this, PhotoSelectMainFragmentActivity.class);
				mIntent.putExtra(AppConstants.KEY_FOR_ADD_PICTURE, true);
				mIntent.putExtra(AppConstants.KEY_PRODUCT_ID, photobook.id);
				mIntent.putExtra(AppConstants.KEY_PRODUCT_DECID, photobook.proDescId);
				startActivity(mIntent);
			}
		});

		order.setOnClickListener(new OnClickListener() {

			@SuppressLint("InlinedApi")
			@Override
			public void onClick(View v) {

				PrintProduct photoBookProduct = null, photoBookAdditionalPageProduct = null;
				for (PrintProduct product : PrintHelper.products) {
					if (product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage)
							&& product.getId().equals(photobook.proDescId)) {
						photoBookProduct = product;
					}
					if (product.getId().contains(PrintHelper.PhotoBook) && product.getId().contains(PrintHelper.AdditionalPage)
							&& product.getId().equals(photobook.additionalPageId)) {
						photoBookAdditionalPageProduct = product;
					}
				}
				Photobook photobook = AppContext.getApplication().getPhotobook();
				int iMin = photobook==null ? 0 : photobook.minNumberOfImages;
				int iMax = photobook==null ? 0 : photobook.maxNumberOfImages;
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(QuickBookFlipperActivity.this);
				if (iMin != 0 && iMax != 0 && (photobook.selectedImages.size() < iMin || photobook.selectedImages.size() > iMax)) {
					builder.setTitle(String.format(getString(R.string.selected_images_range), iMin, iMax, photoBookProduct.getName()));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();

					return;
				}

				// TODO Add cartItem
				for (ArrayList<CartItem> cartItem : PrintHelper.cartChildren) {
					cartItem.clear();
				}
				CartItem item = new CartItem(QuickBookFlipperActivity.this);
				item.photoInfo.setLocalUri("") ;
				item.photoInfo.setPhotoPath("");
				item.quantity = 1;
				item.roi = null;
				item.price = Double.parseDouble(photoBookProduct.getMinPrice());
				item.width = "" + photoBookProduct.getWidth();
				item.height = "" + photoBookProduct.getHeight();
				item.name = photoBookProduct.getName();
				item.cartItemID = photoBookProduct.getId();
				item.shortName = photoBookProduct.getShortName();
				item.productDescriptionId = photoBookProduct.getId();
				item.ProductId = photobook==null ? "" : photobook.id;
				item.quantity = item.quantityIncrement = photoBookProduct.getQuantityIncrement() == 0 ? 1
						: photoBookProduct.getQuantityIncrement();
				item.productType = AppConstants.BOOK_TYPE;
				PrintHelper.cartChildren.get(0).add(item);
				if (getAdditionalPageCount() > 0) {
					CartItem item1 = new CartItem(QuickBookFlipperActivity.this);
					item1.photoInfo.setLocalUri("");
					item1.photoInfo.setPhotoPath("");
					item1.quantity = getAdditionalPageCount();
					item1.roi = null;
					item1.price = Double.parseDouble(photoBookProduct.getMinPrice());
					item1.width = "" + photoBookProduct.getWidth();
					item1.height = "" + photoBookProduct.getHeight();
					item1.name = photoBookAdditionalPageProduct.getName();
					item1.cartItemID = photoBookAdditionalPageProduct.getId();
					item1.shortName = photoBookAdditionalPageProduct.getShortName();
					item1.productType = AppConstants.BOOK_TYPE;
					item1.quantity = item.quantityIncrement = photoBookAdditionalPageProduct.getQuantityIncrement() == 0 ? 1
							: photoBookAdditionalPageProduct.getQuantityIncrement();
					item1.productDescriptionId = photoBookAdditionalPageProduct.getId();
					PrintHelper.cartChildren.get(1).add(item1);
				}

				if (qbPageDownloader != null) {
					qbPageDownloader.cancelAllDownloads();
				}
				// Before jump to shopCarActivity. get the count of the low
				// warning for quick book."PrintHelper.quickBookLowWarningCount"
				checkQuickBookLowWarning();
				trackLocalyticsEvents();
				// TODO Saves Quick book and navigates to the Shopping cart
				// unexpanded screen for Quick book.
				Intent myIntent = new Intent(QuickBookFlipperActivity.this, ShoppingCartActivity.class);
				myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(myIntent);
			}
		});
		
	}
	
	private void trackLocalyticsEvents(){
		if(attr == null){
			attr = new HashMap<String, String>();
		}
		attr.put(KEY_IMAGE_ROTATE, localyticsRotate ? YES : NO);
		attr.put(KEY_PAGE_ADD, localyticsAddPage ? YES : NO);
		attr.put(KEY_PAGE_DELETE, localyticsPageDeleted ? YES : NO);
		attr.put(KEY_PAGE_MOVED, localyticsPageMoved ? YES : NO);
		attr.put(KEY_EFFECT_USED, NO);
		RSSLocalytics.recordLocalyticsEvents(this, EVENT, attr);
	}
	
	class DownloadBitmapFromWeb extends AsyncTask<PhotoInfo, Void, Bitmap> {
		
		private CropView view;
		private Context mContext;
		private FlipViewController parent;
		
		public DownloadBitmapFromWeb(Context context, FlipViewController parent, CropView view){
			this.view = view;
			this.mContext = context;
			this.parent = parent;
		}

		@Override
		protected void onPreExecute() {
			parent.setBlockOutsideTouch(true);
			showWaiting(0, 0, 0, 0);
		}
		
		@Override
		protected Bitmap doInBackground(PhotoInfo... params) {
			ImageUtil.downloadUrlToStream(params[0], mContext);
			return ImageUtil.getBitmapOfPhotoInfo(params[0], mContext);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			parent.setBlockOutsideTouch(false);
			view.setImageBitmap(result, INITIMAGE);
			hideWaiting();
		}
		
	}
	
	
}
