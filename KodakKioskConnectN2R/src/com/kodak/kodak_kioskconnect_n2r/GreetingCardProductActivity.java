package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.example.android.bitmapfun.util.Utils;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.DrawableImageView;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.EditImage;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.EditTestInputDialog;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPage;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardPageLayer;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardProduct;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.Rotate3dAnimation;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.ZoomableRelativeLayout;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.RSSLocalytics;

public class GreetingCardProductActivity extends BaseActivity {
	private final String TAG = GreetingCardProductActivity.class.getSimpleName();
	private Button btBack;
	private Button btNext;
	private TextView tvTitle;
	private TextView tvPreview;

	private Handler zoomHandler;
	private ImageView bt_one, bt_two, bt_three, bt_four;
	private LinearLayout viewParentLayout;
	private LinearLayout mButtonLayout;
	private Bitmap firstBitmap, secondBitmap, thirdBitmap, fourthBitmap;
	private Bitmap firstPreview, secondPreview, thirdPreview, fourthPreview;
	private DrawableImageView firstImg, secondImg, thirdImg, fourthImg;
	private View secondView;
	private Animation mTranslateAnimation;
	private RefreshHandler refreshHandler;
	private PopupWindow mPopupWindow;
	private PopupWindow mPopupUploadFailed;
	private EditImage mEditImage = null;

	private GreetingCardManager manager;
	private GreetingCardProduct product;
	private WaitingDialog waitingDialog;
	private ZoomableRelativeLayout mZoomableLayout;
	private RelativeLayout firstLayout, secondLayout, layoutNavigation, layoutBottom;
	private int screenWidth = -1;
	private int screenHeight = -1;
	private int duplexDirection = -1;
	private int stepFrom = 1;
	private int model = 1;
	private int mlayoutWidth, mlayoutHeight;
	private int selectIndex = -1;
	private int maxLines = 2;

	private final int SINGLECLICK = 0;
	private final int SLIPTOLEFT = 1;
	private final int SLIPTOTOP = 2;
	private final int SLIPTORIGHT = 3;
	private final int SLIPTOBOTTOM = 4;
	private final int RESIZE = 10;
	private final int CLICKTOSEND = 7;
	private final int EDITFLAG = 16;
	private final int PREVIEWFLAG = 17;
	private final int REVALIDATE = 19;
	private final int DOWNDIRECTION = 7;
	private final int UPDIRECTION = 8;
	private final int LEFTDIRECTION = 9;
	private final int RIGHTDIRECTION = 10;

	private final int INITNORMAL = 11;
	private final int INITSACLE = 12;
	private final int BIGGER = 11;
	private final int SMALLER = 12;

	private LinkedList<GreetingCardPage> mCurrentArrayPages = new LinkedList<GreetingCardPage>();
	private boolean needClearData = false;
	private boolean isDownloadingPreviewText = false;
	private boolean isDownloadingPreviewPre = false;
	private boolean isTextTooLong = false; // check the text input is too long											// or not.
	private boolean isEditGreetingCart = false;
	
	private EditTestInputDialog.EditTestInputDialogBuilder connectBuilder;
	private final String SCREEN_NAME = "GC Preview";
	private final String EVENT_SUMMARY = "GC Edit Summary";
	private final String KEY_PIC_ADD = "GC Pictures Added";
	private final String KEY_TEX_ADD = "GC Text Added";
	private final String KEY_PRE_USED = "GC Preview Used";
	private final String KEY_ROTATE = "GC Rotate Used";
	private final String KEY_EFFECT_ADD = "GC Effects Used";
	private final String KEY_PIC_DELETE = "GC Pictures Deleted";
	private final String KEY_PIC_REPLACE = "GC Pictures Replaced";
	private final String YES = "yes";
	private final String NO = "no";
	
	private static HashMap<String, String> attr = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		// setContentView(R.layout.greetingcard_product);
		setContentLayout(R.layout.greetingcard_product_field);
		getViews();
		initData();
		setEvents();
		resetEditText();
		resetStepButton();
		initScaleView(0, 0, INITNORMAL);
		if(getIntent()==null || !getIntent().getBooleanExtra("selectedPhoto", false)){
			initLocalytics();
		}
		
		downLoadPreviewCard ();
	}

	private void initLocalytics() {
		attr.put(KEY_EFFECT_ADD, NO);
		attr.put(KEY_PIC_ADD, NO);
		attr.put(KEY_PIC_DELETE, NO);
		attr.put(KEY_PIC_REPLACE, NO);
		attr.put(KEY_PRE_USED, NO);
		attr.put(KEY_ROTATE, NO);
		attr.put(KEY_TEX_ADD, NO);
	}

	/**
	 * add a image to edit in mZoomableLayout
	 */
	private void addEditView(GreetingCardPageLayer layer) { // TODO:addEditView
		BitmapWorkerTask task = new BitmapWorkerTask(layer);
        task.execute();
	}

	/**
	 * launch a popup window at the moment that a image is added to edit
	 */
	private void popEditWindow(EditImage meEditImage, final ArrayList<String> editItems, int locationX, int locationY, int forward, int roiW) {
		if (mPopupWindow != null) {
			mPopupWindow = null;
		}

		if (!editItems.isEmpty()) {
			PopEditAdapter popEditAdapter = new PopEditAdapter(editItems, meEditImage, forward);
			ListView mPopListView = new ListView(GreetingCardProductActivity.this);
			mPopListView.setBackgroundColor(Color.TRANSPARENT);
			mPopListView.setDivider(null);
			mPopListView.setAdapter(popEditAdapter);

			mPopupWindow = new PopupWindow(mPopListView, (int) (screenWidth * 0.28), screenHeight / 2);
			if (forward == 1) {
				mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.windowdropdown3));
			} else if (forward == 2) {
				mPopupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.windowdropdown2));
			}

			if (forward == 1) {
				mPopupWindow.showAtLocation(mZoomableLayout, Gravity.LEFT | Gravity.TOP, locationX, locationY);
			} else if (forward == 2) {
				mPopupWindow.showAtLocation(mZoomableLayout, Gravity.LEFT | Gravity.TOP, locationX - screenWidth / 4 - roiW, locationY);
			}

			mZoomableLayout.setPageEdit(true);
		}
	}

	private void setScaleNew(int flag, ZoomableRelativeLayout mZoomLayout, GreetingCardPageLayer mTargetLayer) {

		int bitWidth = 0;
		int bitHeight = 0;
		if (mCurrentArrayPages.size() == 1) {
			bitWidth = mZoomLayout.getmCurrentArrayPages().get(0).getPageWidth();
		} else if (mCurrentArrayPages.size() == 2) {
			bitWidth = mZoomLayout.getmCurrentArrayPages().get(0).getPageWidth();
		}
		bitHeight = mZoomLayout.getmCurrentArrayPages().get(0).getPageHeight();

		int zoomOutWidth = 2 * bitWidth;
		int zoomOutHeight = 2 * bitHeight;

		LayoutParams mLayoutParams = null;
		LayoutParams mLayoutParams2 = null;
		if (flag == BIGGER) {
			mLayoutParams = new LayoutParams(2 * screenWidth, 2 * (screenHeight - 2 * layoutNavigation.getHeight()));
			mLayoutParams2 = new LayoutParams(zoomOutWidth, zoomOutHeight);
		} else if (flag == SMALLER) {
			mLayoutParams = new LayoutParams(screenWidth, screenHeight - 2 * layoutNavigation.getHeight());
			mLayoutParams2 = new LayoutParams(bitWidth, bitHeight);
		}
		if (mLayoutParams.width > screenWidth) {
			mLayoutParams.leftMargin = -(mLayoutParams.width - screenWidth) / 2;
			mLayoutParams.rightMargin = -(mLayoutParams.width - screenWidth) / 2;
		}
		if (mLayoutParams.height > screenHeight - 2 * layoutNavigation.getHeight()) {
			mLayoutParams.topMargin = -(mLayoutParams.height - screenHeight + 2 * layoutNavigation.getHeight()) / 2;
			mLayoutParams.bottomMargin = -(mLayoutParams.height - screenHeight + 2 * layoutNavigation.getHeight()) / 2;
		}

		mLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mLayoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
		mZoomLayout.setmScaleLayoutParams(mLayoutParams);
		mZoomLayout.setLayoutParams(mLayoutParams);

		for (int i = 0; i < mZoomLayout.getChildCount(); i++) {
			RelativeLayout childLayout = (RelativeLayout) mZoomLayout.getChildAt(i);
			if (childLayout.getVisibility() != View.GONE && childLayout != null) {
				childLayout.setLayoutParams(mLayoutParams2);
				RelativeLayout.LayoutParams mLayoutParams3 = new RelativeLayout.LayoutParams(mLayoutParams2.width, mLayoutParams2.height);
				// DrawableImageView mchildView = (DrawableImageView)
				// childLayout.getChildAt(0);
				if (childLayout.getChildAt(0) != null && childLayout.getChildAt(0).getVisibility() != View.GONE) {
					childLayout.getChildAt(0).setLayoutParams(mLayoutParams3);
				}
			}
		}

		if (flag == SMALLER) {
			initScaleView(bitWidth, bitHeight, INITSACLE);
		} else if (flag == BIGGER) {
			initScaleView(zoomOutWidth, zoomOutHeight, INITSACLE);
		}

		if (mTargetLayer != null) {
			if (mTargetLayer.type.equals("Image")) {
				if (mTargetLayer.contentId.trim().length() == 0) {
//					Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSourceSelectMainActivity.class);
					Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSelectMainFragmentActivity.class);
					mIntent.putExtra(AppConstants.KEY_PRODUCT_DECID, product.productDescriptionId);	
					startActivity(mIntent);
					mZoomLayout.setForwarding(false);
					finish();
				} else {
					addEditView(mTargetLayer);
				}
			} else if (mTargetLayer.type.equals(GreetingCardPageLayer.TYPE_TEXT_BLOCK)) {
				carTextEdit();
			}
		}
		PrintHelper.canValidate = true;
	}

	class EditViewHolder {
		RelativeLayout mRelaEdit;
		TextView mTextView;
		ImageView mIcon;
	}

	class PopEditAdapter extends BaseAdapter {

		ArrayList<String> editItems;
		EditImage mEditImage;
		int direction;

		public PopEditAdapter(ArrayList<String> editItems, EditImage mEditImage, int direction) {
			this.editItems = editItems;
			this.mEditImage = mEditImage;
			this.direction = direction;
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
				convertView = View.inflate(GreetingCardProductActivity.this, R.layout.pop_edit_list, null);
				mEditViewHolder.mTextView = (TextView) convertView.findViewById(R.id.txt_deplay_specification);
				mEditViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.img_checkmark);
				if (screenWidth < 850) {
					RelativeLayout.LayoutParams mIconParams = new RelativeLayout.LayoutParams(40, 50);
					mEditViewHolder.mIcon.setLayoutParams(mIconParams);
				}
				mEditViewHolder.mRelaEdit = (RelativeLayout) convertView.findViewById(R.id.rela_edit);
				RelativeLayout.LayoutParams mLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.FILL_PARENT);
				Log.i(TAG, "width ======= " + screenWidth);
				if (direction == 1) {
					mLayoutParams.setMargins(screenWidth < 900 ? 10 : 25, 0, 0, 0);
				} else {
					mLayoutParams.setMargins(0, 0, screenWidth < 900 ? 10 : 25, 0);
				}
				mEditViewHolder.mRelaEdit.setLayoutParams(mLayoutParams);
				convertView.setTag(mEditViewHolder);
			} else {
				mEditViewHolder = (EditViewHolder) convertView.getTag();
			}

			if (!editItems.isEmpty()) {
				final String displayItem = editItems.get(position);
				mEditViewHolder.mTextView.setText(displayItem);
				if (displayItem.equals(getString(R.string.rotate))) {
					mEditViewHolder.mIcon.setImageResource(R.drawable.toolsrotatepicup);
				} else if (displayItem.equals(getString(R.string.replace))) {
					mEditViewHolder.mIcon.setImageResource(R.drawable.toolsreplaceup);
				} else if (displayItem.equals(getString(R.string.delete))) {
					mEditViewHolder.mIcon.setImageResource(R.drawable.toolsdeleteup);
				}

				mEditViewHolder.mRelaEdit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (displayItem.equals(getString(R.string.rotate))) { // rotate
							mEditImage.rotateBitmap();
							mZoomableLayout.setOverWithSend(true);
						} else if (displayItem.equals(getString(R.string.replace))) { // replace
																						// the
																						// edited
																						// image
							attr.put(KEY_PIC_REPLACE, YES);
							SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GreetingCardProductActivity.this);
							prefs.edit().putString(PrintHelper.LAST_IMAGE_CONTENT_ID, manager.getEditLayer().contentId).commit();
							
//							Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSourceSelectMainActivity.class);
							Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSelectMainFragmentActivity.class);
							mIntent.putExtra(AppConstants.KEY_PRODUCT_DECID, product.productDescriptionId);	
							startActivity(mIntent);
							finish();
						} else if (displayItem.equals(getString(R.string.delete))) { // delete
																						// the
																						// edited
																						// image
							mZoomableLayout.removeView(mEditImage);
							attr.put(KEY_PIC_DELETE, YES);
							new Thread(new DeleteImageTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId)).start();
						}
					}
				});
			}
			return convertView;
		}

	}

	private Bitmap convertToSuit(GreetingCardPage page, int flag) {
		Bitmap result = product.getPagePreviewByPath(page.id, flag);
//		Bitmap targetBitmap = null;
//		if (product.getPagePreview(page.id, flag) != null) {
//			Log.i(TAG, "!!!!!!!!!!!!!! target will not be default and flag = " + flag);
//			targetBitmap = product.getPagePreview(page.id, flag);
//		} else {
//			Log.i(TAG, "!!!!!!!!!!!!!!  target will be default and flag = " + flag);
//			targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait_4x6);
//		}
//		int drawWidth = targetBitmap.getWidth();
//		int drawHeight = targetBitmap.getHeight();
//		int h = manager.getNaviHeight() == 0 ? layoutNavigation.getHeight() : manager.getNaviHeight();
//		if (manager.getNaviHeight() == 0) {
//			manager.setNaviHeight(h);
//		}
//		int layoutHeight = screenHeight - 3 * h;
//		int paramWidth = -1;
//		int paramHeight = -1;
//		paramHeight = layoutHeight;
//		paramWidth = layoutHeight * drawWidth / drawHeight;
//		if (paramWidth > screenWidth) {
//			paramWidth = screenWidth;
//			paramHeight = screenWidth * drawHeight / drawWidth;
//		}
//		float scaleWidth = ((float) paramWidth) / drawWidth;
//		float scaleHeight = ((float) paramHeight) / drawHeight;
//		Matrix matrix = new Matrix();
//		matrix.postScale(scaleWidth, scaleHeight);
//		if (model == 2 && drawWidth > screenWidth / 2) {
//			int newBitmapWidth = -1;
//			int newBitmapHeight = -1;
//			newBitmapWidth = drawWidth;
//			newBitmapHeight = drawHeight;
//			newBitmapHeight = (screenWidth / 2 * drawHeight) / drawHeight;
//			newBitmapWidth = screenWidth / 2;
//			scaleWidth = ((float) newBitmapWidth) / drawWidth;
//			scaleHeight = ((float) newBitmapHeight) / drawHeight;
//			matrix.postScale(scaleWidth, scaleHeight);
//		}
//		if (targetBitmap.isRecycled()) {
//			return result;
//		}
//		result = Bitmap.createBitmap(targetBitmap, 0, 0, drawWidth, drawHeight, matrix, true);
		return result;
	}

	private void layoutPageWithPreview(GreetingCardPage page, Bitmap bitmap, DrawableImageView mImgView, int flag) {
		if (page != null) {
			//Bitmap targetBitmap = product.getPagePreview(page.id, flag);
			Bitmap targetBitmap = product.getPagePreviewByPath(page.id, flag);
			bitmap = targetBitmap;
			Log.i(TAG, "rrrrrrrrrr page.id = " + page.id);
			if (targetBitmap == null) {
				Log.i(TAG, "!!!!!!!!!!!!!!  target will be default and flag = " + flag);
				targetBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait_4x6);
			}
			//Bitmap newBitmap = null;
			int drawWidth = targetBitmap.getWidth();
			int drawHeight = targetBitmap.getHeight();
			int h = manager.getNaviHeight() == 0 ? layoutNavigation.getHeight() : manager.getNaviHeight();
			if (manager.getNaviHeight() == 0) {
				manager.setNaviHeight(h);
			}

			int layoutHeight = screenHeight - 3 * h;
			int paramWidth = -1;
			int paramHeight = -1;

			paramHeight = layoutHeight;
			paramWidth = layoutHeight * drawWidth / drawHeight;
			if (paramWidth > screenWidth) {
				paramWidth = screenWidth;
				paramHeight = screenWidth * drawHeight / drawWidth;
			}
			
			if (targetBitmap != null) {
				LayoutParams params = new LayoutParams(paramWidth, paramHeight);
				mImgView.setLayoutParams(params);
				mImgView.setScaleType(ScaleType.FIT_XY);
				if (flag == EDITFLAG && tvPreview.getText().equals(getString(R.string.preview))) {
					mImgView.setImageBitmap(bitmap);
					mImgView.setmCurrentPage(page);
					mImgView.invalidate();
				} else if (flag == PREVIEWFLAG && tvPreview.getText().equals(getString(R.string.edit))) {
					mImgView.setImageBitmap(bitmap);
					mImgView.setmCurrentPage(page);
					mImgView.invalidate();
				}
			}
			if (mZoomableLayout.getmScaleFactor() != 1.0f) {
				mZoomableLayout.setmScaleFactor(1.0f);
			}
			if (manager.getLayoutWidth() == 0) {
				manager.setLayoutWidth(paramWidth);
			}
			if (manager.getLayoutHeight() == 0) {
				manager.setLayoutHeight(paramHeight);
			}

			initScaleView(paramWidth, paramHeight, INITNORMAL);
			page.setPageWidth(paramWidth);
			page.setPageHeight(paramHeight);
			mlayoutWidth = paramWidth;
			mlayoutHeight = paramHeight;

			mZoomableLayout.setOverWithSend(false);
			mZoomableLayout.setPageEdit(false);
			resetStepButton();
			addShadowView();
			refreshAllTextEffect();
		}
	}

	class ViewHolder {
		ImageView mImgThumbnail;
	}

	private final int GETPREVIEW_SUCCEED = 0;
	private final int GETPREVIEW_FAILED = 1;
	private final int START_WATING = 2;
	private final int TEXT_TOOLONG = 3;
	private final int UPLOAD_IMAGE_FAILED = 9;
	private final int EDIT_IMAGE_FAILED = 4;
	private final int DELETE_IMAGE_FAILED = 5;
	private final int REPLACE_IMAGE_FAILED = 6;
	private final int TASK_SUCCEED = 7;
	private final int TASK_DELETE_SUCCEED = 11;
	private final int ADD_IMAGE_TASK_FAILED = 8;
	private final int ADD_TEXT_SUCCESS = 10;

	class RefreshHandler extends Handler {
		private GreetingCardPage page;
		private Bitmap bitmap;
		private DrawableImageView mImg;
		private int flag;

		public GreetingCardPage getPage() {
			return page;
		}

		public void setPage(GreetingCardPage page) {
			this.page = page;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		public DrawableImageView getmImg() {
			return mImg;
		}

		public void setmImg(DrawableImageView mImg) {
			this.mImg = mImg;
		}

		public int getFlag() {
			return flag;
		}

		public void setFlag(int flag) {
			this.flag = flag;
		}

		public RefreshHandler(GreetingCardPage page, Bitmap bitmap, DrawableImageView mImg, int flag) {
			this.page = page;
			this.bitmap = bitmap;
			this.mImg = mImg;
			this.flag = flag;
		}

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			// PrintHelper.isDrawPath = true;
			switch (action) {
			case GETPREVIEW_SUCCEED:
				layoutPageWithPreview(getPage(), getBitmap(), getmImg(), getFlag());
				if (waitingDialog != null && waitingDialog.isShowing()) {
					waitingDialog.dismiss();
				}
				break;
			case GETPREVIEW_FAILED:
				if (waitingDialog != null && waitingDialog.isShowing()) {
					waitingDialog.dismiss();
				}
				break;
			case START_WATING:
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}
				if (waitingDialog != null && !waitingDialog.isShowing()) {
					waitingDialog.show();
				}
				break;
			case TEXT_TOOLONG:
				if (waitingDialog != null && waitingDialog.isShowing()) {
					waitingDialog.dismiss();
				}
				showTextTooLongDialog();
				break;
			case ADD_TEXT_SUCCESS:
				new Thread(new GetPreview(manager.getEditPage(), EDITFLAG)).start();
				break;
			case TASK_SUCCEED: // does not include the CreatePreviewTask
				new Thread(new GetPreview(manager.getEditPage(), EDITFLAG)).start();
				break;
			case TASK_DELETE_SUCCEED:
				if (manager.getAlbumMap4Click().containsKey(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex)) {
					for (int i = 0; i < PrintHelper.mAlbumButton.size(); i++) {
						if (manager.getAlbumMap4Click().get(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex) == PrintHelper.mAlbumButton
								.get(i).getId()) {
							int formerSelected = Integer.parseInt(PrintHelper.mAlbumButton.get(i).selected);
							Log.i(TAG, "zzzzzz init formerselect = " + formerSelected + " , alumid = " + PrintHelper.mAlbumButton.get(i).getId());
							formerSelected--;
							Log.i(TAG, "zzzzzz minused formerselect = " + formerSelected);
							PrintHelper.mAlbumButton.get(i).selected = "" + formerSelected;
						}
					}
					manager.getAlbumMap4Click().remove(manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex);
				}
				new Thread(new GetPreview(manager.getEditPage(), EDITFLAG)).start();
				break;
			case UPLOAD_IMAGE_FAILED:
				showUploadImageFailedDialog((Integer) msg.obj);
				break;
			case EDIT_IMAGE_FAILED:
				showTaskFailedDialog(EDIT_IMAGE_FAILED);
				break;
			case DELETE_IMAGE_FAILED:
				showTaskFailedDialog(DELETE_IMAGE_FAILED);
				break;
			case REPLACE_IMAGE_FAILED:
				showTaskFailedDialog(REPLACE_IMAGE_FAILED);
				break;
			case ADD_IMAGE_TASK_FAILED:
				showTaskFailedDialog(ADD_IMAGE_TASK_FAILED);
				break;
			}

		}
	}

	class GetPreview implements Runnable {
		private GreetingCardPage page;
		private int initPageIndex = -1;
		private int flag;

		public GetPreview(GreetingCardPage page, int flag) {
			this.page = page;
			this.flag = flag;
		}

		public GetPreview(GreetingCardPage page, int initPageIndex, int flag) {
			this.page = page;
			this.initPageIndex = initPageIndex;
			this.flag = flag;
		}

		@Override
		public void run() {
			Log.i(TAG, "zoe createPagePreview in GetPreview will be executed , " + "page.id = " + page.id + " , initPageIndex = " + initPageIndex);
			boolean succeed = manager.createPagePreview(page.id, 480, 320, flag);
			boolean succedd2 = manager.createPagePreview(page.id, 480, 320, PREVIEWFLAG);
			if (succeed && succedd2) {
				if (initPageIndex == -1) {
					readyForHandler(flag);
				} else {
					readyForHandler(initPageIndex, flag);
				}
				refreshHandler.obtainMessage(GETPREVIEW_SUCCEED).sendToTarget();
			} else {
				refreshHandler.obtainMessage(GETPREVIEW_FAILED).sendToTarget();
			}
		}

	}

	class GetAllPreview implements Runnable {

		private int flag;
		@Override
		public void run() {
			flag = EDITFLAG;
			for (int i = 0; i < product.pages.length; i++) {
				if (flag == EDITFLAG) {
					boolean succeed = manager.createPagePreview(product.pages[i].id, 480, 320, flag);
					if (succeed) {
						readyForHandler(i + 1, flag);
						refreshHandler.obtainMessage(GETPREVIEW_SUCCEED).sendToTarget();
					} else {
						refreshHandler.obtainMessage(GETPREVIEW_FAILED).sendToTarget();
					}
				}
			}
			flag = PREVIEWFLAG;
			for (int i = 0; i < product.pages.length; i++) {
				manager.createPagePreview(product.pages[i].id, 480, 320, flag);
			}
		}

	}

	class GetAllUnloadedPreview implements Runnable {

		private int flag;

		public GetAllUnloadedPreview(int flag) {
			this.flag = flag;
		}

		@Override
		public void run() {
			if (flag == EDITFLAG) {
				isDownloadingPreviewText = true;
			} else if (flag == PREVIEWFLAG) {
				isDownloadingPreviewPre = true;
			}
			for (int i = 0; i < product.pages.length; i++) {
				boolean preFlag = false;
				preFlag = flag == EDITFLAG ? isDownloadingPreviewText : isDownloadingPreviewPre;
				if (preFlag) {
					if (manager.isLoaded(product.pages[i].id, flag)) {
						continue;
					}
					boolean succeed = manager.createPagePreview(product.pages[i].id, 480, 320, flag);
					if (succeed) {
						readyForHandler(i + 1, flag);
						refreshHandler.obtainMessage(GETPREVIEW_SUCCEED).sendToTarget();
					} else {
						refreshHandler.obtainMessage(GETPREVIEW_FAILED).sendToTarget();
					}
				}
			}
			if (flag == EDITFLAG) {
				isDownloadingPreviewText = false;
			} else if (flag == PREVIEWFLAG) {
				isDownloadingPreviewPre = false;
			}
		}

	}

	private void checkAndGetUnloadedPreview(int flag) {
		boolean preFlag = false;
		preFlag = flag == EDITFLAG ? isDownloadingPreviewText : isDownloadingPreviewPre;
		if (!preFlag) {
			new Thread(new GetAllUnloadedPreview(flag)).start();
		}
	}

	class AddImageToCart implements Runnable {
		private int holeIndex;
		private String contentId = "";
		private Context context;
		private boolean needUploadImage = true;

		public AddImageToCart(Context context, int holeIndex, boolean needUploadImage) {
			this.holeIndex = holeIndex;
			this.context = context;
			this.needUploadImage = needUploadImage;
		}

		@Override
		public void run() {
			refreshHandler.sendEmptyMessage(START_WATING);
			PhotoInfo photo = manager.getEditLayer().getPhotoInfo();
			if (needUploadImage) {
				contentId = manager.uploadPicture(photo);
			}
			if (contentId.equals("") || contentId.equals("Error")) {
				refreshHandler.obtainMessage(UPLOAD_IMAGE_FAILED, ADD_IMAGE_TASK_FAILED).sendToTarget();
				return;
			}
			boolean succeed = manager.addImageToCardTask(holeIndex, contentId);
			if (succeed) {
				if (photo.getPhotoSource().isFromPhone()) {
					Log.i(TAG, "addImageToCardTask succeed.");
					ExifInterface exif = null;
					try {
						exif = Utils.loadImageExif(Utils.getFilePath(photo.getLocalUri(), context));
					} catch (Exception e) {
						e.printStackTrace();
					}
					manager.getEditLayer().imageThumbPath = Utils.compressThumbToJPG(photo.getLocalUri(), exif, context);
					if (photo.getPhotoPath().toUpperCase().endsWith(".PNG")) {
						String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
						manager.getEditLayer().imageThumbPath = tempFolder
								+ photo.getLocalUri().substring(photo.getLocalUri().lastIndexOf("/"), photo.getLocalUri().length()) + "newS" + ".jpg";
					}
				}
				refreshHandler.sendEmptyMessage(TASK_SUCCEED);
			} else {
				Log.i(TAG, "addImageToCardTask failed.");
				refreshHandler.sendEmptyMessage(ADD_IMAGE_TASK_FAILED);
			}

		}

	}

	class ReplaceImageTask implements Runnable {
		private String newImageContentId;
		private Context context;
		private int holeIndex;

		public ReplaceImageTask(Context context, int holeIndex, boolean needUploadImage) {
			this.context = context;
			this.holeIndex = holeIndex;
		}

		@Override
		public void run() {
			PhotoInfo photo = manager.getEditLayer().getPhotoInfo();
			refreshHandler.sendEmptyMessage(START_WATING);
			newImageContentId = manager.uploadPicture(photo);
			if (newImageContentId == null || newImageContentId.equals("")) {
				refreshHandler.obtainMessage(UPLOAD_IMAGE_FAILED, REPLACE_IMAGE_FAILED).sendToTarget();
				return;
			}
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String lastImageContentId = prefs.getString(PrintHelper.LAST_IMAGE_CONTENT_ID, "");
			boolean succeed = manager.replaceImageTask(lastImageContentId, newImageContentId, holeIndex);
			if (succeed) {
				if (photo.getPhotoSource().isFromPhone()) {
					ExifInterface exif = null;
					try {
						exif = Utils.loadImageExif(Utils.getFilePath(photo.getLocalUri(), context));
					} catch (Exception e) {
						e.printStackTrace();
					}
					prefs.edit().putString(PrintHelper.LAST_IMAGE_CONTENT_ID, "").commit();
					String fileName = Utils.getFilePath(photo.getLocalUri(), context);
					if (fileName.toUpperCase().endsWith(".PNG")) {
						String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
						manager.getEditLayer().imageThumbPath = tempFolder
								+ photo.getLocalUri().substring(photo.getLocalUri().lastIndexOf("/"), photo.getLocalUri().length()) + "newS" + ".jpg";
					} else {
						manager.getEditLayer().imageThumbPath = Utils.compressThumbToJPG(photo.getLocalUri(), exif, context);
					}
				}
				refreshHandler.sendEmptyMessage(TASK_SUCCEED);
			} else {
				refreshHandler.sendEmptyMessage(REPLACE_IMAGE_FAILED);
			}

		}

	}

	class SendEditInfoTask implements Runnable {
		private ROI roi;
		private String imageContentId = "";
		private Context context;
		private int degree;

		public SendEditInfoTask(Context context, String imageContentId, ROI roi, int rotatedDegree) {
			this.roi = roi;
			this.imageContentId = imageContentId;
			this.degree = rotatedDegree;
		}

		@Override
		public void run() {
			refreshHandler.sendEmptyMessage(START_WATING);
			// manager = GreetingCardManager.getGreetingCardManager(context);
			boolean succeed = false;

			ImageInfo imageInfo = manager.getImageInfo(imageContentId);
			if (imageInfo == null) {
				refreshHandler.sendEmptyMessage(EDIT_IMAGE_FAILED);
				return;
			}

			if (imageInfo.angle != Math.abs(manager.getEditLayer().degree)) {
				int diffDegree = imageInfo.angle - manager.getEditLayer().degree;
				succeed = manager.rotateImageTask((int) diffDegree, false);
				if (!succeed) {
					Log.w(TAG, "diffDegree:" + diffDegree + " -> failed!");
					refreshHandler.sendEmptyMessage(EDIT_IMAGE_FAILED);
					return;
				}
				Log.w(TAG, "diffDegree:" + diffDegree + " -> succeed!");
			}

			if (degree != -1) {
				attr.put(KEY_ROTATE, YES);
				succeed = manager.rotateImageTask(degree % 360, true);
			} else {
				succeed = true;
			}
			if (succeed) {
				succeed = manager.setImageCropTask(imageContentId, roi);
			} else {
				refreshHandler.sendEmptyMessage(EDIT_IMAGE_FAILED);
				return;
			}

			if (succeed) {
				refreshHandler.sendEmptyMessage(TASK_SUCCEED);
			} else {
				refreshHandler.sendEmptyMessage(EDIT_IMAGE_FAILED);
			}
		}

	}

	class DeleteImageTask implements Runnable {
		private String imageContentId;

		public DeleteImageTask(Context context, String imageContentId) {
			this.imageContentId = imageContentId;
		}

		@Override
		public void run() {
			mZoomableLayout.setForwarding(false);
			refreshHandler.sendEmptyMessage(START_WATING);
			boolean succeed = manager.deleteImageTask(imageContentId, true);
			if (succeed) {
				refreshHandler.sendEmptyMessage(TASK_DELETE_SUCCEED);
			} else {
				refreshHandler.sendEmptyMessage(DELETE_IMAGE_FAILED);
			}
		}

	}

	private void readyForHandler(int flag) {
		Log.i(TAG, " rrrrrr PrintHelper.model = " + PrintHelper.model + " , manager.getEditPageIndex() = " + manager.getEditPageIndex() + " , "
				+ "selectIndex = " + selectIndex);
		if (selectIndex == 0) {
			refreshHandler.setPage(product.pages[0]);
			refreshHandler.setBitmap(flag == EDITFLAG ? firstBitmap : firstPreview);
			refreshHandler.setmImg(firstImg);
		} else if (selectIndex == 1) {
			if (PrintHelper.model == 2 && manager.getEditPageIndex() == 3) {
				refreshHandler.setPage(product.pages[2]);
				refreshHandler.setBitmap(flag == EDITFLAG ? thirdBitmap : thirdPreview);
				refreshHandler.setmImg(thirdImg);
			} else {
				refreshHandler.setPage(product.pages[1]);
				refreshHandler.setBitmap(flag == EDITFLAG ? secondBitmap : secondPreview);
				refreshHandler.setmImg(secondImg);
			}

		} else if (selectIndex == 2) {
			refreshHandler.setPage(product.pages[2]);
			refreshHandler.setBitmap(flag == EDITFLAG ? thirdBitmap : thirdPreview);
			refreshHandler.setmImg(thirdImg);
		} else if (selectIndex == 3) {
			refreshHandler.setPage(product.pages[3]);
			refreshHandler.setBitmap(flag == EDITFLAG ? fourthBitmap : fourthPreview);
			refreshHandler.setmImg(fourthImg);
		}
		refreshHandler.setFlag(flag);
	}

	private void readyForHandler(int initPageIndex, int flag) {
		if (initPageIndex == 1) {
			refreshHandler.setPage(product.pages[0]);
			refreshHandler.setBitmap(flag == EDITFLAG ? firstBitmap : firstPreview);
			refreshHandler.setmImg(firstImg);
		} else if (initPageIndex == 2) {
			refreshHandler.setPage(product.pages[1]);
			refreshHandler.setBitmap(flag == EDITFLAG ? secondBitmap : secondPreview);
			refreshHandler.setmImg(secondImg);
		} else if (initPageIndex == 3) {
			refreshHandler.setPage(product.pages[2]);
			refreshHandler.setBitmap(flag == EDITFLAG ? thirdBitmap : thirdPreview);
			refreshHandler.setmImg(thirdImg);
		} else if (initPageIndex == 4) {
			refreshHandler.setPage(product.pages[3]);
			refreshHandler.setBitmap(flag == EDITFLAG ? fourthBitmap : fourthPreview);
			refreshHandler.setmImg(fourthImg);
		}
		refreshHandler.setFlag(flag);
	}

	private void showInvalidCardDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(GreetingCardProductActivity.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.greetingcard_min_image_not_selected));
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (PrintHelper.lastStepTo != 1) {
					bt_one.performClick();
				}
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showLoseWorkDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(GreetingCardProductActivity.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.losework));
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				PrintHelper.StartOver();
				if (manager.getAlbumMap4Click() != null) {
					manager.getAlbumMap4Click().clear();
				}
				manager.setNaviHeight(0);
				manager.setLayoutWidth(0);
				manager.setLayoutHeight(0);
				System.gc();
				needClearData = true;
				Intent intent = new Intent(GreetingCardProductActivity.this, GreetingCardSelectionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				Bundle b = new Bundle();
				b.putBoolean("isFromGreetingCard", true);
				intent.putExtras(b);
				startActivity(intent);
				finish();
			}
		});
		builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private void showUploadImageFailedDialog(final int taskType) {
		if (waitingDialog != null && waitingDialog.isShowing()) {
			waitingDialog.dismiss();
		}
		LayoutInflater mInflater = LayoutInflater.from(this);
		View contentView = mInflater.inflate(R.layout.upload_image_failed, null);
		mPopupUploadFailed = new PopupWindow(contentView, getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay()
				.getHeight(), true);
		Button btStartOver = (Button) contentView.findViewById(R.id.dialog_start_over);
		Button btRetry = (Button) contentView.findViewById(R.id.dialog_retry_button);
		final ImageView uploadingFailedImage = (ImageView) contentView.findViewById(R.id.upload_failed_image);
		final Bitmap uploadImage = PrintHelper.loadThumbnailImage(manager.getEditLayer().getPhotoInfo().getLocalUri(),
				MediaStore.Images.Thumbnails.MINI_KIND, null, this);
		uploadingFailedImage.setImageBitmap(uploadImage);
		btStartOver.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPopupUploadFailed.dismiss();
				if (uploadImage != null && !uploadImage.isRecycled()) {
					uploadImage.recycle();
				}
				Intent intent = new Intent(GreetingCardProductActivity.this, MainMenu.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

		btRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPopupUploadFailed.dismiss();
				if (uploadImage != null && !uploadImage.isRecycled()) {
					uploadImage.recycle();
				}
				Log.e(TAG, "taskType: " + taskType);
				if (taskType == ADD_IMAGE_TASK_FAILED) {
					new Thread(new AddImageToCart(GreetingCardProductActivity.this, manager.getEditLayer().holeIndex, true)).start();
				} else if (taskType == REPLACE_IMAGE_FAILED) {
					new Thread(new ReplaceImageTask(GreetingCardProductActivity.this, manager.getEditLayer().holeIndex, true)).start();
				}
			}
		});
		mPopupUploadFailed.showAtLocation(mZoomableLayout, Gravity.CENTER, 0, 0);
	}

	private void showTaskFailedDialog(final int errorCode) {
		if (waitingDialog != null && waitingDialog.isShowing()) {
			waitingDialog.dismiss();
		}
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(GreetingCardProductActivity.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.share_auth_bad_gateway));
		builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (errorCode) {
				case EDIT_IMAGE_FAILED:
					new Thread(new SendEditInfoTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId, mEditImage.getRoi(),
							mEditImage.getmRotateDegree())).start();
					break;
				case DELETE_IMAGE_FAILED:
					new Thread(new DeleteImageTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId)).start();
					break;
				case REPLACE_IMAGE_FAILED:
					new Thread(new ReplaceImageTask(GreetingCardProductActivity.this, manager.getEditLayer().holeIndex, false)).start();
					break;
				case ADD_IMAGE_TASK_FAILED:
					new Thread(new AddImageToCart(GreetingCardProductActivity.this, manager.getEditLayer().holeIndex, false)).start();
					break;
				}
			}
		});
		builder.create().show();
	}

	private void showTextTooLongDialog() {
		isTextTooLong = true;
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(GreetingCardProductActivity.this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.text_too_long));
		builder.setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				carTextEdit();
			}
		});
		builder.setNegativeButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				refreshHandler.obtainMessage(ADD_TEXT_SUCCESS).sendToTarget();
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mPopupWindow != null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
				mZoomableLayout.removeView(mEditImage);
				mZoomableLayout.setPageEdit(false);
				if (mZoomableLayout.isOverWithSend()) {
					new Thread(new SendEditInfoTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId, mEditImage.getRoi(),
							mEditImage.getmRotateDegree())).start();
				}
				mZoomableLayout.setForwarding(false);
			} else {
				if (isEditGreetingCart){
					return false;
				}
				showLoseWorkDialog();
				return super.onKeyDown(keyCode, event);
			}
			// }
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private boolean isCardValidForCart() {
		boolean valid = true;
		if (product != null && product.pages[0] != null) {
			for (GreetingCardPageLayer layer : product.pages[0].layers) {
				if (layer.type.equals(GreetingCardPageLayer.TYPE_IMAGE)) {
					if (layer.contentId == null || layer.contentId.equals("")) {
						valid = false;
						break;
					}
				}
			}
		}
		return valid;
	}

	private void changePopState() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
			mZoomableLayout.removeView(mEditImage);
			mZoomableLayout.setPageEdit(false);
			if (mZoomableLayout.isOverWithSend()) {
				new Thread(new SendEditInfoTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId, mEditImage.getRoi(),
						mEditImage.getmRotateDegree())).start();
			}
			mZoomableLayout.setForwarding(false);
		}
	}

	private AnimationListener animationListener(final int from, final int to) {
		AnimationListener aniListener = new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				secondLayout.setVisibility(View.GONE);
				firstLayout.setVisibility(View.GONE);
				clearShadowView();

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				final float centerX = firstLayout.getWidth() / 2.0f;
				final float centerY = firstLayout.getHeight() / 2.0f;
				if (from == 0 && to == 1) {
					firstLayout.setVisibility(View.VISIBLE);
				} else if (from == 1 && to == 2) {
					if (model == 1) {
						firstLayout.removeAllViews();
						firstLayout.addView(secondImg);
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						rotation.setDuration(200);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
					} else if (model == 2) {
						firstLayout.removeAllViews();
						firstLayout.addView(secondImg);
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
						showTwoViews();

					} else if (model == 4) {
						firstLayout.removeAllViews();
						secondLayout.removeAllViews();
						secondLayout.addView(secondImg);
						secondLayout.bringToFront();
						secondLayout.clearAnimation();
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = null;
						if (duplexDirection == DOWNDIRECTION) {
							rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						} else if (duplexDirection == UPDIRECTION) {
							rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						}

						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
					} else if (model == 5) {
						firstLayout.removeAllViews();
						secondLayout.removeAllViews();
						secondLayout.addView(secondImg);
						secondLayout.bringToFront();
						secondLayout.clearAnimation();
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = null;
						if (duplexDirection == RIGHTDIRECTION) {
							rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						} else if (duplexDirection == LEFTDIRECTION) {
							rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						}

						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
					}

				} else if (from == 1 && to == 3) {
					if (model == 1) {
						firstLayout.removeAllViews();
						firstLayout.addView(secondImg);
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);
						secondLayout.clearAnimation();

						mTranslateAnimation = new TranslateAnimation(0, 0, -firstLayout.getHeight() * 3 / 4, -firstLayout.getHeight());
						mTranslateAnimation.setDuration(0);
						mTranslateAnimation.setFillAfter(true);
						firstLayout.startAnimation(mTranslateAnimation);
					} else if (model == 2) {
						firstLayout.removeAllViews();
						firstLayout.addView(secondImg);
						secondLayout.removeAllViews();
						secondLayout.addView(fourthImg);
						secondLayout.bringToFront();
						firstLayout.clearAnimation();
						secondLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation;
						rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
						rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 4, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
					}
					secondLayout.bringToFront();

				} else if (from == 1 && to == 4) {
					secondLayout.bringToFront();
					secondLayout.removeAllViews();
					firstLayout.removeAllViews();
					firstLayout.clearAnimation();
					secondLayout.clearAnimation();
					firstLayout.setVisibility(View.VISIBLE);
					firstLayout.addView(secondImg);
					secondLayout.addView(fourthImg);
					secondLayout.setVisibility(View.VISIBLE);

					Rotate3dAnimation rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 360f, from, to, 2, model);
					rotation.setDuration(100);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					firstLayout.startAnimation(rotation);

					rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 360f, from, to, 4, model);
					rotation.setDuration(100);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					secondLayout.startAnimation(rotation);
					secondLayout.bringToFront();
				}

				else if (from == 2 && to == 1) {
					if (model == 1) {
						firstLayout.removeAllViews();
						firstLayout.addView(firstImg);
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 360f, from, to, 2, model);
						rotation.setDuration(0);
						rotation.setInterpolator(new DecelerateInterpolator());

						rotation.setFillAfter(true);

						firstLayout.startAnimation(rotation);
					} else if (model == 2) {
						firstLayout.removeAllViews();
						firstLayout.addView(firstImg);
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
					} else if (model == 4) {
						firstLayout.removeAllViews();
						firstLayout.addView(firstImg);
						firstLayout.bringToFront();
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = null;
						if (duplexDirection == DOWNDIRECTION) {
							rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						} else if (duplexDirection == UPDIRECTION) {
							rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						}
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
					} else if (model == 5) {
						firstLayout.removeAllViews();
						firstLayout.addView(firstImg);
						firstLayout.bringToFront();
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = null;
						if (duplexDirection == RIGHTDIRECTION) {
							rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 2, model);
						} else if (duplexDirection == LEFTDIRECTION) {
							rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 2, model);
						}
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
					}

				} else if (from == 2 && to == 3) {
					if (model == 1) {
						secondLayout.clearAnimation();
						secondLayout.setVisibility(View.VISIBLE);
					} else if (model == 2) {
						secondLayout.removeAllViews();
						secondLayout.addView(fourthImg);
						secondLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 4, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
					}
					secondLayout.bringToFront();
				} else if (from == 2 && to == 4) {
					secondLayout.removeAllViews();
					secondLayout.addView(fourthImg);
					Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, centerX, firstLayout.getHeight(), 180f, from, to, 4, model);
					rotation.setDuration(100);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					secondLayout.startAnimation(rotation);
					secondLayout.setVisibility(View.VISIBLE);
					secondLayout.bringToFront();
				} else if (from == 3 && to == 1) {
					if (model == 1) {
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
					} else if (model == 2) {
						firstLayout.removeAllViews();
						firstLayout.addView(firstImg);
						secondLayout.removeAllViews();
						secondLayout.addView(thirdImg);
						firstLayout.bringToFront();
						firstLayout.clearAnimation();
						secondLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.setVisibility(View.VISIBLE);

						Rotate3dAnimation rotation;
						rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 1, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						firstLayout.startAnimation(rotation);
						rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 3, model);
						rotation.setDuration(200);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
					}
					firstLayout.bringToFront();
				} else if (from == 3 && to == 2) {
					if (model == 1) {
						firstLayout.clearAnimation();
						firstLayout.setVisibility(View.VISIBLE);
					} else if (model == 2) {
						firstLayout.clearAnimation();
						secondLayout.clearAnimation();
						secondLayout.setVisibility(View.VISIBLE);
						firstLayout.setVisibility(View.VISIBLE);
						secondLayout.removeAllViews();
						secondLayout.addView(thirdImg);
						Rotate3dAnimation rotation;
						rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 3, model);
						rotation.setDuration(100);
						rotation.setInterpolator(new DecelerateInterpolator());
						rotation.setFillAfter(true);
						secondLayout.startAnimation(rotation);
						showTwoViews();
					}
					firstLayout.bringToFront();
				} else if (from == 3 && to == 4) {
					secondLayout.removeAllViews();
					secondLayout.addView(fourthImg);
					Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 0f, from, to, 4, model);
					rotation.setDuration(200);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					secondLayout.setAnimation(rotation);
					secondLayout.setVisibility(View.VISIBLE);
					secondLayout.bringToFront();
				} else if (from == 4 && to == 1) {
					secondLayout.removeAllViews();
					firstLayout.removeAllViews();

					firstLayout.setVisibility(View.VISIBLE);
					firstLayout.addView(firstImg);
					secondLayout.addView(thirdImg);
					secondLayout.setVisibility(View.VISIBLE);

					Rotate3dAnimation rotationFirstdLayout = new Rotate3dAnimation(-90, 0, centerX, centerY, 360f, from, to, 1, model);
					rotationFirstdLayout.setDuration(100);
					rotationFirstdLayout.setInterpolator(new DecelerateInterpolator());

					rotationFirstdLayout.setFillAfter(true);
					firstLayout.startAnimation(rotationFirstdLayout);

					Rotate3dAnimation rotation = new Rotate3dAnimation(-90, 0, centerX, centerY, 360f, from, to, 3, model);

					rotation.setInterpolator(new DecelerateInterpolator());
					rotation.setDuration(100);
					rotation.setFillAfter(true);
					secondLayout.startAnimation(rotation);
					firstLayout.bringToFront();
				} else if (from == 4 && to == 2) {
					secondLayout.removeAllViews();
					secondLayout.addView(thirdImg);
					secondLayout.setVisibility(View.VISIBLE);

					Rotate3dAnimation rotation = new Rotate3dAnimation(0, 0, firstLayout.getWidth(), firstLayout.getHeight(), 180f, from, to, 3,
							model);
					rotation.setDuration(100);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					secondLayout.setAnimation(rotation);

					firstLayout.setVisibility(View.VISIBLE);
					firstLayout.clearAnimation();
					firstLayout.bringToFront();
				} else if (from == 4 && to == 3) {
					secondLayout.removeAllViews();
					secondLayout.addView(thirdImg);
					Rotate3dAnimation rotation = new Rotate3dAnimation(90, 0, centerX, centerY, 0f, from, to, 3, model);
					rotation.setDuration(150);
					rotation.setInterpolator(new DecelerateInterpolator());

					rotation.setFillAfter(true);
					secondLayout.setAnimation(rotation);
					secondLayout.setVisibility(View.VISIBLE);

				}
				addShadowView();
				refreshAllTextEffect();
				invokeButtonAvailable(true);
			}
		};
		return aniListener;

	}

	private void stepTWOToOne() {
		PrintHelper.lastStepTo = 1;
		resetStepButton();
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		if (model == 1) {
			firstLayout.bringToFront();
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			AnimationSet set = null;
			Rotate3dAnimation rotation;
			set = new AnimationSet(true);
			mTranslateAnimation = new TranslateAnimation(0, 0, firstLayout.getHeight(), 0);
			set.addAnimation(mTranslateAnimation);

			rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			set.addAnimation(rotation);
			set.setDuration(200);
			set.setFillAfter(true);
			secondLayout.startAnimation(set);

			rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 2, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			rotation.setFillAfter(true);
			firstLayout.startAnimation(rotation);
		} else if (model == 2) {
			showOneView();
			firstLayout.bringToFront();
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			AnimationSet set = null;
			Rotate3dAnimation rotation;
			set = new AnimationSet(true);
			mTranslateAnimation = new TranslateAnimation(centerX, 0, 0, 0);
			set.addAnimation(mTranslateAnimation);

			rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			set.addAnimation(rotation);
			set.setDuration(350);
			set.setFillAfter(true);
			secondLayout.startAnimation(set);
			rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 2, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			rotation.setFillAfter(true);
			firstLayout.startAnimation(rotation);

		} else if (model == 4) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation = null;
			if (duplexDirection == DOWNDIRECTION) {
				rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			} else if (duplexDirection == UPDIRECTION) {
				rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			}
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			secondLayout.startAnimation(rotation);
		} else if (model == 5) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation = null;
			if (duplexDirection == RIGHTDIRECTION) {
				rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			} else if (duplexDirection == LEFTDIRECTION) {
				rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			}

			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			secondLayout.startAnimation(rotation);
		}

		selectIndex = 0;
		PrintHelper.editedPageIndex = 0;
		stepFrom = 1;

		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[0]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(1);
		manager.setEditPageIndex(1);
	}

	private void stepThreeToOne() {
		PrintHelper.lastStepTo = 1;
		resetStepButton();
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		if (model == 1) {
			firstLayout.bringToFront();
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			firstLayout.removeAllViews();
			firstLayout.addView(firstImg);
			firstLayout.clearAnimation();
			firstLayout.setVisibility(View.VISIBLE);
			rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setDuration(200);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			secondLayout.startAnimation(rotation);

			rotation = new Rotate3dAnimation(90, 0, centerX, 0, 360f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			rotation.setDuration(200);
			firstLayout.startAnimation(rotation);
		} else if (model == 2) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);
			rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 4, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			secondLayout.startAnimation(rotation);
		}

		selectIndex = 0;
		PrintHelper.editedPageIndex = 0;
		stepFrom = 1;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[0]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(1);
		manager.setEditPageIndex(1);
	}

	private void stepFourToOne() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 1;
		resetStepButton();
		firstLayout.bringToFront();
		final float centerX = firstLayout.getWidth() / 2.0f;
		final float centerY = firstLayout.getHeight() / 2.0f;
		Rotate3dAnimation rotation;
		rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 360f, stepFrom, PrintHelper.lastStepTo, 4, model);
		rotation.setDuration(100);

		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		firstLayout.startAnimation(rotation);
		rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 360f, stepFrom, PrintHelper.lastStepTo, 2, model);
		rotation.setDuration(100);
		rotation.setInterpolator(new DecelerateInterpolator());
		rotation.setFillAfter(true);
		secondLayout.startAnimation(rotation);
		/*
		 * final float centerX = firstLayout.getWidth() / 2.0f; final float
		 * centerY = firstLayout.getHeight() / 2.0f; Rotate3dAnimation rotation;
		 * rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 360f,
		 * stepFrom, PrintHelper.lastStepTo, 4, model);
		 * rotation.setDuration(100); rotation.setInterpolator(new
		 * DecelerateInterpolator());
		 * 
		 * rotation.setFillAfter(true);
		 * rotation.setAnimationListener(animationListener(stepFrom,
		 * PrintHelper.lastStepTo)); firstLayout.setAnimation(rotation);
		 * 
		 * rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 360f,
		 * stepFrom, PrintHelper.lastStepTo, 2, model); rotation.setDuration(0);
		 * rotation.setInterpolator(new DecelerateInterpolator());
		 * 
		 * rotation.setFillAfter(true); secondLayout.setAnimation(rotation);
		 */

		selectIndex = 0;
		PrintHelper.editedPageIndex = 0;
		stepFrom = 1;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[0]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(1);
		manager.setEditPageIndex(1);
	}

	private void stepOneToTwo() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 2;
		resetStepButton();
		if (model == 1) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			secondLayout.removeAllViews();
			secondLayout.addView(thirdImg);
			AnimationSet set = null;
			set = new AnimationSet(true);
			mTranslateAnimation = new TranslateAnimation(0, 0, 0, firstLayout.getHeight());
			set.addAnimation(mTranslateAnimation);
			rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			set.addAnimation(rotation);
			set.setDuration(450);
			set.setFillAfter(true);
			secondLayout.startAnimation(set);

			rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(200);
			rotation.setInterpolator(new DecelerateInterpolator());

			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);

			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		} else if (model == 2) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			secondLayout.removeAllViews();
			secondLayout.addView(thirdImg);
			mTranslateAnimation = new TranslateAnimation(-centerX, 0, 0, 0);
			mTranslateAnimation.setDuration(200);
			mTranslateAnimation.setFillAfter(true);
			secondLayout.startAnimation(mTranslateAnimation);

			rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());

			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);

			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mCurrentArrayPages.add(product.pages[2]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		} else if (model == 4) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation = null;
			if (duplexDirection == DOWNDIRECTION) {
				rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			} else if (duplexDirection == UPDIRECTION) {
				rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			}

			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);
			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		} else if (model == 5) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation = null;
			if (duplexDirection == RIGHTDIRECTION) {
				rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			} else if (duplexDirection == LEFTDIRECTION) {
				rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			}
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);
			if (duplexDirection == RIGHTDIRECTION) {
				rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			} else if (duplexDirection == LEFTDIRECTION) {
				rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			}

			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			secondLayout.startAnimation(rotation);
			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		}

		selectIndex = 1;
		PrintHelper.editedPageIndex = 1;
		stepFrom = 2;
		// mZoomableLayout.setmCurrentPage(product.pages[1]);
		manager.setEditPage(2);
		manager.setEditPageIndex(2);
	}

	private void stepThreeToTwo() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 2;
		resetStepButton();
		if (model == 1) {
			AnimationSet set = null;
			Rotate3dAnimation rotation;
			set = new AnimationSet(true);
			mTranslateAnimation = new TranslateAnimation(0, 0, -firstLayout.getHeight(), 0);
			mTranslateAnimation.setDuration(200);
			mTranslateAnimation.setFillAfter(true);
			mTranslateAnimation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(mTranslateAnimation);

			mTranslateAnimation = new TranslateAnimation(0, 0, 0, firstLayout.getHeight());
			mTranslateAnimation.setFillAfter(true);
			mTranslateAnimation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			rotation = new Rotate3dAnimation(0, 0, firstLayout.getWidth(), firstLayout.getHeight(), 45f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			set.addAnimation(mTranslateAnimation);
			set.addAnimation(rotation);
			set.setDuration(200);
			set.setFillAfter(true);
			secondLayout.startAnimation(set);

			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		} else if (model == 2) {

			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 4, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());

			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			mTranslateAnimation = new TranslateAnimation(0, -centerX, 0, 0);
			mTranslateAnimation.setDuration(200);
			mTranslateAnimation.setFillAfter(true);
			secondLayout.startAnimation(rotation);
			firstLayout.startAnimation(mTranslateAnimation);

			mCurrentArrayPages.clear();
			mCurrentArrayPages.add(product.pages[1]);
			mCurrentArrayPages.add(product.pages[2]);
			mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		}

		selectIndex = 1;
		PrintHelper.editedPageIndex = 1;
		stepFrom = 2;
		// mZoomableLayout.setmCurrentPage(product.pages[1]);
		manager.setEditPage(2);
		manager.setEditPageIndex(2);
	}

	private void stepFourToTwo() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 2;
		resetStepButton();
		final float centerX = firstLayout.getWidth() / 2.0f;
		Rotate3dAnimation rotation;
		rotation = new Rotate3dAnimation(0, -90, centerX, firstLayout.getHeight(), 180f, stepFrom, PrintHelper.lastStepTo, 4, model);
		rotation.setDuration(100);
		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		secondLayout.startAnimation(rotation);

		selectIndex = 1;
		PrintHelper.editedPageIndex = 1;
		stepFrom = 2;
		mZoomableLayout.setmCurrentPage(product.pages[1]);
		manager.setEditPage(2);
		manager.setEditPageIndex(2);
	}

	private void stepOneToThree() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 3;
		resetStepButton();
		if (model == 1) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			Rotate3dAnimation rotation;
			mTranslateAnimation = new TranslateAnimation(0, 0, 0, 0);
			mTranslateAnimation.setDuration(200);
			secondLayout.startAnimation(mTranslateAnimation);

			rotation = new Rotate3dAnimation(0, 90, centerX, 0, 360f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(200);
			rotation.setInterpolator(new DecelerateInterpolator());

			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);
		} else if (model == 2) {
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			Rotate3dAnimation rotation;
			rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(rotation);
			rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			secondLayout.startAnimation(rotation);
		}

		stepFrom = 3;
		mCurrentArrayPages.clear();
		if (model == 1) {
			manager.setEditPage(3);
			manager.setEditPageIndex(3);
			mCurrentArrayPages.add(product.pages[2]);
			selectIndex = 2;
			PrintHelper.editedPageIndex = 2;
		} else if (model == 2) {
			manager.setEditPage(4);
			manager.setEditPageIndex(4);
			mCurrentArrayPages.add(product.pages[3]);
			selectIndex = 3;
			PrintHelper.editedPageIndex = 3;
		}
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
	}

	private void stepTwoToThree() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 3;
		resetStepButton();
		if (model == 1) {
			mTranslateAnimation = new TranslateAnimation(0, 0, 0, -firstLayout.getHeight());
			mTranslateAnimation.setDuration(200);
			mTranslateAnimation.setFillAfter(true);
			mTranslateAnimation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			firstLayout.startAnimation(mTranslateAnimation);

			mTranslateAnimation = new TranslateAnimation(0, 0, firstLayout.getHeight(), 0);
			mTranslateAnimation.setDuration(200);
			mTranslateAnimation.setFillAfter(true);
			mTranslateAnimation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			secondLayout.startAnimation(mTranslateAnimation);
		} else if (model == 2) {
			showOneView();
			secondLayout.bringToFront();
			final float centerX = firstLayout.getWidth() / 2.0f;
			final float centerY = firstLayout.getHeight() / 2.0f;
			AnimationSet set = null;
			Rotate3dAnimation rotation;

			set = new AnimationSet(true);
			mTranslateAnimation = new TranslateAnimation(-centerX, 0, 0, 0);
			set.addAnimation(mTranslateAnimation);
			rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 2, model);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setFillAfter(true);
			set.addAnimation(rotation);
			set.setDuration(200);
			set.setFillAfter(true);
			firstLayout.startAnimation(set);

			rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 3, model);
			rotation.setDuration(100);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
			secondLayout.startAnimation(rotation);
		}

		stepFrom = 3;
		mCurrentArrayPages.clear();
		if (model == 1) {
			manager.setEditPage(3);
			manager.setEditPageIndex(3);
			mCurrentArrayPages.add(product.pages[2]);
			selectIndex = 2;
			PrintHelper.editedPageIndex = 2;
		} else if (model == 2) {
			manager.setEditPage(4);
			manager.setEditPageIndex(4);
			mCurrentArrayPages.add(product.pages[3]);
			selectIndex = 3;
			PrintHelper.editedPageIndex = 3;
		}
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
	}

	private void stepFourToThree() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 3;
		resetStepButton();
		final float centerX = firstLayout.getWidth() / 2.0f;
		final float centerY = firstLayout.getHeight() / 2.0f;
		Rotate3dAnimation rotation;
		mTranslateAnimation = new TranslateAnimation(0, 0, 0, -firstLayout.getHeight());
		mTranslateAnimation.setDuration(400);
		mTranslateAnimation.setFillAfter(true);

		firstLayout.startAnimation(mTranslateAnimation);

		rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 360f, stepFrom, PrintHelper.lastStepTo, 4, model);
		rotation.setDuration(200);
		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		secondLayout.startAnimation(rotation);

		selectIndex = 2;
		PrintHelper.editedPageIndex = 2;
		stepFrom = 3;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[2]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(3);
		manager.setEditPageIndex(3);
	}

	private void stepOneToFour() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 4;
		resetStepButton();
		final float centerX = firstLayout.getWidth() / 2.0f;
		final float centerY = firstLayout.getHeight() / 2.0f;
		Rotate3dAnimation rotation;
		rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 360f, stepFrom, PrintHelper.lastStepTo, 1, model);
		rotation.setDuration(100);

		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		firstLayout.startAnimation(rotation);
		rotation = new Rotate3dAnimation(0, -90, centerX, centerY, 360f, stepFrom, PrintHelper.lastStepTo, 3, model);
		rotation.setDuration(100);
		rotation.setInterpolator(new DecelerateInterpolator());
		rotation.setFillAfter(true);
		secondLayout.startAnimation(rotation);
		selectIndex = 3;
		PrintHelper.editedPageIndex = 3;
		stepFrom = 4;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[3]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(4);
		manager.setEditPageIndex(4);
	}

	private void stepTwoToFour() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 4;
		resetStepButton();
		final float centerX = firstLayout.getWidth() / 2.0f;
		Rotate3dAnimation rotation;
		secondLayout.bringToFront();
		rotation = new Rotate3dAnimation(0, 0, 0, 0, 180f, stepFrom, PrintHelper.lastStepTo, 2, model);
		rotation.setDuration(200);
		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		firstLayout.startAnimation(rotation);

		rotation = new Rotate3dAnimation(-180, -90, centerX, firstLayout.getHeight(), 180f, stepFrom, PrintHelper.lastStepTo, 3, model);
		rotation.setDuration(100);
		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		secondLayout.startAnimation(rotation);

		selectIndex = 3;
		PrintHelper.editedPageIndex = 3;
		stepFrom = 4;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[3]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(4);
		manager.setEditPageIndex(4);
	}

	private void stepThreeToFour() {
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			setScaleNew(SMALLER, mZoomableLayout, null);
			mZoomableLayout.setmScaleFactor(1.0f);
		}
		PrintHelper.lastStepTo = 4;
		resetStepButton();
		final float centerX = firstLayout.getWidth() / 2.0f;
		final float centerY = firstLayout.getHeight() / 2.0f;
		Rotate3dAnimation rotation;
		secondLayout.bringToFront();

		rotation = new Rotate3dAnimation(0, 0, firstLayout.getWidth(), firstLayout.getHeight(), 180f, stepFrom, PrintHelper.lastStepTo, 2, model);
		rotation.setDuration(600);
		rotation.setInterpolator(new DecelerateInterpolator());
		rotation.setFillAfter(true);
		firstLayout.startAnimation(rotation);

		rotation = new Rotate3dAnimation(0, 90, centerX, centerY, 0f, stepFrom, PrintHelper.lastStepTo, 1, model);
		rotation.setDuration(200);
		rotation.setInterpolator(new DecelerateInterpolator());

		rotation.setFillAfter(true);
		rotation.setAnimationListener(animationListener(stepFrom, PrintHelper.lastStepTo));
		secondLayout.startAnimation(rotation);

		selectIndex = 3;
		PrintHelper.editedPageIndex = 3;
		stepFrom = 4;
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[3]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		manager.setEditPage(4);
		manager.setEditPageIndex(4);
	}

	private void showTwoViews() {
		firstLayout.clearAnimation();
		secondLayout.clearAnimation();
		int paramWidth = 0;
		int paramHeight = 0;
		if (mZoomableLayout.getmScaleFactor() == 2.0f) {
			paramWidth = (int) (manager.getLayoutWidth() * mZoomableLayout.getmScaleFactor());
			paramHeight = (int) (manager.getLayoutHeight() * mZoomableLayout.getmScaleFactor());
		} else {
			paramWidth = manager.getLayoutWidth();
			paramHeight = manager.getLayoutHeight();
		}
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(paramWidth, paramHeight);
		Log.i(TAG, "showTwoViews param.width = " + param.width + " , param.Height = " + param.height);
		mZoomableLayout.removeView(firstLayout);
		mZoomableLayout.setGravity(Gravity.CENTER);
		mZoomableLayout.addView(firstLayout);
		param.addRule(RelativeLayout.RIGHT_OF, R.id.firstLayout);
		mZoomableLayout.removeView(secondLayout);
		mZoomableLayout.addView(secondLayout, param);
	}

	private void showOneView() {
		int w, h;
		if (mlayoutWidth == 0) {
			w = 200;
			h = 400;
		} else {
			w = mlayoutWidth;
			h = mlayoutHeight;
		}
		firstLayout.clearAnimation();
		secondLayout.clearAnimation();
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(firstLayout.getWidth(), firstLayout.getHeight());
		mZoomableLayout.removeView(firstLayout);
		mZoomableLayout.setGravity(Gravity.CENTER);
		mZoomableLayout.addView(firstLayout);
		mZoomableLayout.removeView(secondLayout);
		mZoomableLayout.addView(secondLayout, param);
	}

	private void resetStepButton() {
		if (model == 1) {
			bt_one.setImageResource(R.drawable.viewcardlandscapefront1unselect_button);
			bt_two.setImageResource(R.drawable.viewcardlandscapeinsidetop2unselect_button);
			bt_three.setImageResource(R.drawable.viewcardlandscapeinsidebottom3unselect_button);
			bt_four.setImageResource(R.drawable.viewcardlandscapeback4unselect_button);
		} else if (model == 2) {
			bt_one.setImageResource(R.drawable.viewcardportraitfront1unselect_button);
			bt_two.setImageResource(R.drawable.viewcardportraitinside2unselect_button);
			bt_three.setImageResource(R.drawable.viewcardportraitback3unselect_button);
			bt_four.setVisibility(View.INVISIBLE);
		} else if (model == 3) {
			bt_one.setVisibility(View.INVISIBLE);
			bt_two.setVisibility(View.INVISIBLE);
			bt_three.setVisibility(View.INVISIBLE);
			bt_four.setVisibility(View.INVISIBLE);
		} else if (model == 4) {
			bt_one.setImageResource(R.drawable.btnonebackgroundduplex4unselect);
			bt_two.setImageResource(R.drawable.btntwobackgroundduplex4unselect);
			bt_three.setVisibility(View.INVISIBLE);
			bt_four.setVisibility(View.INVISIBLE);
		} else if (model == 5) {
			bt_one.setImageResource(R.drawable.btnonebackgroundduplex5unselect);
			bt_two.setImageResource(R.drawable.btntwobackgroundduplex5unselect);
			bt_three.setVisibility(View.INVISIBLE);
			bt_four.setVisibility(View.INVISIBLE);
		}
		switch (PrintHelper.lastStepTo) {
		case 1:
			if (model == 1) {
				bt_one.setImageResource(R.drawable.viewcardlandscapefront1select_button);
			} else if (model == 2) {
				bt_one.setImageResource(R.drawable.viewcardportraitfront1select_button);
			} else if (model == 4) {
				bt_one.setImageResource(R.drawable.btnonebackgroundduplex4select);
			} else if (model == 5) {
				bt_one.setImageResource(R.drawable.btnonebackgroundduplex5select);
			}
			break;
		case 2:
			if (model == 1) {
				bt_two.setImageResource(R.drawable.viewcardlandscapeinsidetop2select_button);
			} else if (model == 2) {
				bt_two.setImageResource(R.drawable.viewcardportraitinside2select_button);
			} else if (model == 4) {
				bt_two.setImageResource(R.drawable.btntwobackgroundduplex4select);
			} else if (model == 5) {
				bt_two.setImageResource(R.drawable.btntwobackgroundduplex5select);
			}
			break;
		case 3:
			if (model == 1) {
				bt_three.setImageResource(R.drawable.viewcardlandscapeinsidebottom3select_button);
			} else if (model == 2) {
				bt_three.setImageResource(R.drawable.viewcardportraitback3select_button);
			}
			break;
		case 4:
			bt_four.setImageResource(R.drawable.viewcardlandscapeback4select_button);
			break;
		}

	}

	/**
	 * before one animation end. the button will be set unable.
	 * 
	 * @author song
	 * @created 2013-12-13
	 */
	private void invokeButtonAvailable(boolean enable) {

		if (enable) {
			switch (PrintHelper.lastStepTo) {
			case 1:
				bt_one.setEnabled(false);
				bt_two.setEnabled(true);
				bt_three.setEnabled(true);
				bt_four.setEnabled(true);
				break;
			case 2:
				bt_one.setEnabled(true);
				bt_two.setEnabled(false);
				bt_three.setEnabled(true);
				bt_four.setEnabled(true);
				break;
			case 3:
				bt_one.setEnabled(true);
				bt_two.setEnabled(true);
				bt_three.setEnabled(false);
				bt_four.setEnabled(true);
				break;
			case 4:
				bt_one.setEnabled(true);
				bt_two.setEnabled(true);
				bt_three.setEnabled(true);
				bt_four.setEnabled(false);
				break;
			}
		} else {
			bt_one.setEnabled(false);
			bt_two.setEnabled(false);
			bt_three.setEnabled(false);
			bt_four.setEnabled(false);
		}
	}

	private void carTextEdit() {
		// PrintHelper.isDrawPath = false;
		GreetingCardPageLayer layer = manager.getEditLayer();
		connectBuilder = new EditTestInputDialog.EditTestInputDialogBuilder(GreetingCardProductActivity.this, maxLines,layer);
		connectBuilder.setTitle(R.string.enter_mesage);
		connectBuilder.setMessage(layer.getTextInputVlaue());
		connectBuilder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GreetingCardPageLayer layer = manager.getEditLayer();
				dialog.dismiss();
				firstImg.invalidate();
				secondImg.invalidate();
				waitingDialog.show();

				layer.setEditedBefore(true);
				layer.setTextInputVlaue(PrintHelper.editTextForGreetingCard);
				attr.put(KEY_TEX_ADD, YES);
				inputTextBlock();
			}
		});
		connectBuilder.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				connectBuilder = null;
				resetStepButton();
				// PrintHelper.isDrawPath = true;
				if (isTextTooLong) {
					refreshHandler.obtainMessage(ADD_TEXT_SUCCESS).sendToTarget();
				}
			}
		});
		connectBuilder.setCancelable(false);
		connectBuilder.create().show();
		mZoomableLayout.setForwarding(false);
	}

	private void inputTextBlock() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				GreetingCardPageLayer layer = manager.getEditLayer();
				Boolean upLoadTextsuccess = manager.setTextBlockTask(layer.contentId, PrintHelper.editTextForGreetingCard);
				PrintHelper.editTextForGreetingCard = "";
				if (upLoadTextsuccess) {
					layer = manager.getEditLayer();
					if (!layer.toGetDisplayableText().equalsIgnoreCase("")
							&& !layer.toGetDisplayableText().equalsIgnoreCase(layer.getTextInputVlaue())) {
						refreshHandler.obtainMessage(TEXT_TOOLONG).sendToTarget();
						return;
					}
					new Thread(new GetPreview(manager.getEditPage(), EDITFLAG)).start();
				}
			}
		}).start();

	}

	private void initScaleView(int w, int h, int flag) {
		viewParentLayout.removeAllViews();
		firstLayout.clearAnimation();
		secondLayout.clearAnimation();
		firstLayout.setVisibility(View.VISIBLE);
		secondLayout.setVisibility(View.VISIBLE);
		firstLayout.removeAllViews();
		secondLayout.removeAllViews();
		final float centerX = manager.getLayoutWidth() / 2.0f;
		final float centerY = manager.getLayoutHeight() / 2.0f;
		mZoomableLayout.setZoomHandler(zoomHandler);
		mZoomableLayout.setScreenWidth(screenWidth);
		mZoomableLayout.setScreenHeight(screenHeight);
		mZoomableLayout.setParentWidth(screenWidth);
		mZoomableLayout.setParentHeight(screenHeight - 2 * layoutNavigation.getHeight());
		mZoomableLayout.setManager(manager);

		/**
		 * the situation that location to the last page when model equals two
		 */
		if (model == 2 && PrintHelper.lastStepTo == 3) {
			selectIndex = 3;
			PrintHelper.editedPageIndex = 3;
		} else {
			selectIndex = PrintHelper.lastStepTo - 1;
			PrintHelper.editedPageIndex = PrintHelper.lastStepTo - 1;
		}

		if (mZoomableLayout.getParamWidth() == 0 && mZoomableLayout.getParamHeight() == 0) {
			mZoomableLayout.setParamWidth(w);
			mZoomableLayout.setParamHeight(h);
		}
		mCurrentArrayPages.clear();
		mCurrentArrayPages.add(product.pages[PrintHelper.lastStepTo - 1]);
		mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
		if (model == 1) {

			Rotate3dAnimation rotation;
			firstLayout.bringToFront();
			if (w == 0 || h == 0) {
				w = 400;
				h = 200;
			}
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
			secondLayout.setLayoutParams(params);
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(w, h);
			firstLayout.setLayoutParams(params2);
			switch (PrintHelper.lastStepTo) {
			case 1:
				secondLayout.addView(thirdImg);
				firstLayout.addView(firstImg);

				rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 3, model);
				rotation.setDuration(0);
				rotation.setInterpolator(new DecelerateInterpolator());
				rotation.setFillAfter(true);
				secondLayout.startAnimation(rotation);
				break;
			case 2:
				firstLayout.addView(secondImg);
				secondLayout.addView(thirdImg);
				AnimationSet set = null;
				set = new AnimationSet(true);
				mTranslateAnimation = new TranslateAnimation(0, 0, 0, manager.getLayoutHeight());
				set.addAnimation(mTranslateAnimation);
				rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 0f, 1, 2, 3, model);
				rotation.setInterpolator(new DecelerateInterpolator());
				rotation.setFillAfter(true);
				set.addAnimation(rotation);
				set.setDuration(0);
				set.setFillAfter(true);
				secondLayout.startAnimation(set);
				break;
			case 3:
				firstLayout.addView(secondImg);
				secondLayout.addView(thirdImg);
				secondLayout.bringToFront();
				mTranslateAnimation = new TranslateAnimation(0, 0, 0, -manager.getLayoutHeight());
				mTranslateAnimation.setDuration(0);
				mTranslateAnimation.setFillAfter(true);
				firstLayout.startAnimation(mTranslateAnimation);
				break;
			case 4:
				firstLayout.addView(secondImg);
				secondLayout.addView(fourthImg);
				secondLayout.bringToFront();
				rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 360f, 1, 4, 2, model);
				rotation.setDuration(0);
				rotation.setInterpolator(new DecelerateInterpolator());

				rotation.setFillAfter(true);
				firstLayout.startAnimation(rotation);
				break;
			}
		} else if (model == 2) {
			bt_one.setImageResource(R.drawable.viewcardportraitfront1select_button);
			if (w == 0 || h == 0) {
				w = 200;
				h = 400;
			}
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
			firstLayout.setLayoutParams(params);
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(w, h);
			secondLayout.setLayoutParams(params2);

			Rotate3dAnimation rotation;
			switch (PrintHelper.lastStepTo) {
			case 1:
				firstLayout.addView(firstImg);
				secondLayout.addView(thirdImg);
				rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, stepFrom, PrintHelper.lastStepTo, 3, model);
				rotation.setDuration(200);
				rotation.setInterpolator(new DecelerateInterpolator());
				rotation.setFillAfter(true);
				secondLayout.startAnimation(rotation);
				break;
			case 2:
				mCurrentArrayPages.add(product.pages[PrintHelper.lastStepTo]);
				mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
				firstLayout.addView(secondImg);
				secondLayout.addView(thirdImg);
				showTwoViews();
				break;
			case 3:
				mCurrentArrayPages.clear();
				mCurrentArrayPages.add(product.pages[PrintHelper.lastStepTo]);
				mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);
				firstLayout.addView(secondImg);
				secondLayout.addView(fourthImg);
				secondLayout.bringToFront();
				rotation = new Rotate3dAnimation(0, 0, centerX, centerY, 310.0f, 2, 3, 2, model);
				rotation.setInterpolator(new DecelerateInterpolator());
				rotation.setFillAfter(true);
				firstLayout.startAnimation(rotation);
				break;
			}

		} else if (model == 3) {
			mButtonLayout.setVisibility(View.INVISIBLE);
			secondLayout.setVisibility(View.GONE);
			if (w == 0 || h == 0) {
				w = 200;
				h = 400;
			}
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
			firstLayout.setLayoutParams(params);
			firstLayout.addView(firstImg);
		} else if (model == 4 || model == 5) {
			if (w == 0 || h == 0) {
				if (model == 4) {
					w = 400;
					h = 200;
				} else {
					w = 200;
					h = 400;
				}
			}
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
			firstLayout.setLayoutParams(params);
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(w, h);
			secondLayout.setLayoutParams(params2);
			firstLayout.addView(firstImg);
			secondLayout.addView(secondImg);

			// secondLayout.setVisibility(View.GONE);
			if (PrintHelper.lastStepTo == 2 && flag != INITSACLE) {
				/*
				 * /// float centerX = firstLayout.getWidth() / 2.0f; // float
				 * centerY = firstLayout.getHeight() / 2.0f; Rotate3dAnimation
				 * rotation = null; rotation = new Rotate3dAnimation(0, -90,
				 * centerX, centerY, 0f, 0, 0, 1, model);
				 * rotation.setDuration(100); rotation.setInterpolator(new
				 * DecelerateInterpolator()); rotation.setFillAfter(true);
				 * rotation.setAnimationListener(profess4FlatDuplex(1, 2));
				 * firstLayout.startAnimation(rotation);
				 */
				secondLayout.bringToFront();
			}
			// mCurrentArrayPages.clear();
			// mCurrentArrayPages.add(product.pages[PrintHelper.lastStepTo -
			// 1]);
			// mZoomableLayout.setmCurrentArrayPages(mCurrentArrayPages);

			// RelativeLayout.LayoutParams params = new
			// RelativeLayout.LayoutParams(w,h);
			// firstLayout.setLayoutParams(params);
			// secondLayout.setLayoutParams(params);
			// firstLayout.addView(firstImg);
			// secondLayout.addView(secondImg);
			// secondLayout.setVisibility(View.GONE);
			//
			// if (PrintHelper.lastStepTo == 2) {
			// final float centerX1 = firstLayout.getWidth() / 2.0f;
			// final float centerY1 = firstLayout.getHeight() / 2.0f;
			// Rotate3dAnimation rotation = null;
			// if (duplexDirection == DOWNDIRECTION) {
			// rotation = new Rotate3dAnimation(0, -90, centerX1, centerY1, 0f,
			// 1, PrintHelper.lastStepTo, 1, model);
			// } else if (duplexDirection == UPDIRECTION) {
			// rotation = new Rotate3dAnimation(0, 90, centerX1, centerY1, 0f,
			// 1, PrintHelper.lastStepTo, 1, model);
			// }
			// rotation.setDuration(100);
			// rotation.setInterpolator(new DecelerateInterpolator());
			// rotation.setFillAfter(true);
			// rotation.setAnimationListener(animationListener(1, 2));
			// firstLayout.startAnimation(rotation);
			// }

		}
	}

	private void clearShadowView() {
		firstLayout.removeView(secondView);
		secondLayout.removeView(secondView);
	}

	private void addShadowView() {
		switch (PrintHelper.lastStepTo) {
		case 1:
			firstLayout.removeView(secondView);
			secondLayout.removeView(secondView);
			secondLayout.addView(secondView);
			secondView.layout(0, 0, secondLayout.getWidth(), secondLayout.getHeight());
			secondView.invalidate();
			break;
		case 2:
			firstLayout.removeView(secondView);
			secondLayout.removeView(secondView);
			secondLayout.addView(secondView);
			secondView.layout(0, 0, secondLayout.getWidth(), secondLayout.getHeight());
			secondView.invalidate();
			break;
		case 3:
			if (model == 1) {
				firstLayout.removeView(secondView);
				secondLayout.removeView(secondView);
				secondLayout.addView(secondView);
				secondView.layout(0, 0, secondLayout.getWidth(), secondLayout.getHeight());
				secondView.invalidate();
			} else {
				firstLayout.removeView(secondView);
				secondLayout.removeView(secondView);
				firstLayout.addView(secondView);
				secondView.layout(0, 0, firstLayout.getWidth(), firstLayout.getHeight());
				secondView.invalidate();
			}

			break;
		case 4:
			firstLayout.removeView(secondView);
			secondLayout.removeView(secondView);
			firstLayout.addView(secondView);
			secondView.layout(0, 0, firstLayout.getWidth(), firstLayout.getHeight());
			secondView.invalidate();
			break;
		}
	}

	private void refreshAllTextEffect() {
		switch (PrintHelper.lastStepTo) {
		case 1:
			refreshEachTextEffect(true, false, false, false);
			break;
		case 2:
			refreshEachTextEffect(false, true, true, false);
			break;
		case 3:
			if (model == 1) {
				refreshEachTextEffect(false, true, true, false);
			} else {
				refreshEachTextEffect(false, false, false, true);
			}
			break;
		case 4:
			refreshEachTextEffect(false, false, false, true);
			break;
		}
	}

	private void refreshEachTextEffect(boolean first, boolean second, boolean third, boolean fourth) {
		Log.i(TAG, "rrrrrrrrrrr refresh img first = " + first + " , second = " + second + " , third = " + third + " , fourth = " + fourth);
		if (firstImg != null) {
			firstImg.setShowing(first);
			firstImg.invalidate();
		}
		if (secondImg != null) {
			secondImg.setShowing(second);
			secondImg.invalidate();
		}
		if (thirdImg != null) {
			thirdImg.setShowing(third);
			thirdImg.invalidate();
		}
		if (fourthImg != null) {
			fourthImg.setShowing(fourth);
			fourthImg.invalidate();
		}
	}

	private void resetEditText() {
		if ((screenHeight - 480) > 60) {
			maxLines = maxLines + (screenHeight - 480) / 60;
			maxLines = maxLines > 3 ? 3 : maxLines;
		}
	}

	@Override
	public void getViews() {
		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		btBack = (Button) findViewById(R.id.back_btn);
		btNext = (Button) findViewById(R.id.next_btn);
		tvTitle = (TextView) findViewById(R.id.headerBar_tex);
		tvPreview = (TextView) findViewById(R.id.versionCopyright_tex);
		waitingDialog = new WaitingDialog(this, R.string.animation_quickbook_wait);
		layoutNavigation = (RelativeLayout) findViewById(R.id.main_navbar);
		mButtonLayout = (LinearLayout) findViewById(R.id.buttonModelOneLayout);
		mZoomableLayout = (ZoomableRelativeLayout) findViewById(R.id.layout_right);
		// add by song
		firstLayout = (RelativeLayout) findViewById(R.id.firstLayout);
		secondLayout = (RelativeLayout) findViewById(R.id.secondLayout);
		viewParentLayout = (LinearLayout) findViewById(R.id.viewParentLayout);
		firstImg = new DrawableImageView(GreetingCardProductActivity.this);
		secondImg = new DrawableImageView(GreetingCardProductActivity.this);
		thirdImg = new DrawableImageView(GreetingCardProductActivity.this);
		fourthImg = new DrawableImageView(GreetingCardProductActivity.this);
		bt_one = (ImageView) findViewById(R.id.bt_one);
		bt_two = (ImageView) findViewById(R.id.bt_two);
		bt_three = (ImageView) findViewById(R.id.bt_three);
		bt_four = (ImageView) findViewById(R.id.bt_four);
		secondView = findViewById(R.id.secondView);
	}

	@Override
	public void initData() {
		isEditGreetingCart = AppContext.getApplication().isEditGreetingCart();
		manager = GreetingCardManager.getGreetingCardManager(GreetingCardProductActivity.this);
		product = manager.getGreetingCardProduct();
		RelativeLayout.LayoutParams preLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		preLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		preLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		if (isEditGreetingCart){
			btBack.setVisibility(View.INVISIBLE);
		}else {
			btBack.setVisibility(View.VISIBLE);
		}
		btNext.setText(getString(R.string.cart));
		btNext.setVisibility(View.VISIBLE);	
		waitingDialog.setCancelable(false);
		tvTitle.setTypeface(PrintHelper.tf);
		tvTitle.setText(manager.getGreetingCardProductShortName(product.productDescriptionId));
		tvPreview.setVisibility(View.VISIBLE);
		tvPreview.setTypeface(PrintHelper.tf);
		tvPreview.setText(getString(R.string.preview));
		tvPreview.setLayoutParams(preLayoutParams);
		tvPreview.setTextColor(Color.rgb(0, 174, 239));

		mZoomableLayout.setZoomHandler(zoomHandler);
		mZoomableLayout.setClickable(true);
		mZoomableLayout.setFocusable(true);

		refreshHandler = new RefreshHandler(null, null, null, EDITFLAG);

		if (manager.getEditPage() == null) {
			manager.setEditPage(1);
			manager.setEditPageIndex(1);
		}

		boolean isLandscape = false;
		if (product.pages != null && product.pages.length > 0) {
			GreetingCardPage page = product.pages[0];
			if (page.width < page.height) {
				isLandscape = true;
			}
		}

		if (product.maxNumberOfPages == 1) {
			model = 3;
		} else if (product.maxNumberOfPages == 2) {

			if (isLandscape) {
				model = 5;
			} else {
				model = 4;
			}
		} else {
			if (isLandscape) {
				model = 2;
			} else {
				model = 1;
			}
		}

		PrintHelper.model = model;
		stepFrom = PrintHelper.lastStepTo;
	}

	@Override
	public void setEvents() {
		layoutNavigation.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				changePopState();
				return false;
			}
		});
		layoutBottom = (RelativeLayout) findViewById(R.id.selectionbar_newsetting);
		if (layoutBottom != null) {
			layoutBottom.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					changePopState();
					return false;
				}
			});
		}

		btBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					showLoseWorkDialog();
				} else {
					changePopState();
				}
			}
		});

		btNext.setOnClickListener(new OnClickListener() {

			@SuppressLint("InlinedApi")
			@Override
			public void onClick(View v) {
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					if (isCardValidForCart()) {
						RSSLocalytics.recordLocalyticsEvents(GreetingCardProductActivity.this, EVENT_SUMMARY, attr);
						Intent intent = new Intent(GreetingCardProductActivity.this, ShoppingCartActivity.class);
						startActivity(intent);
						finish();
					} else {
						showInvalidCardDialog();
					}
				} else {
					changePopState();
				}
			}
		});

		// add by song
		bt_one.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mZoomableLayout.getmScaleFactor() == 2.0f) {
					mZoomableLayout.setAnimaling(true);
					mZoomableLayout.scaleOutAnimation(false);
				}
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					invokeButtonAvailable(false);
					if (model != 4 && model != 5) {
						firstLayout.bringToFront();
					} else {
						secondLayout.bringToFront();
					}

					switch (stepFrom) {
					case 1:
						break;
					case 2:
						if (model == 4) {
							duplexDirection = UPDIRECTION;
						} else if (model == 5) {
							duplexDirection = LEFTDIRECTION;
						}
						stepTWOToOne();
						break;
					case 3:
						stepThreeToOne();
						break;
					case 4:
						stepFourToOne();
						break;
					}
				} else {
					changePopState();
				}
			}
		});

		bt_two.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mZoomableLayout.getmScaleFactor() == 2.0f) {
					mZoomableLayout.setAnimaling(true);
					mZoomableLayout.scaleOutAnimation(false);
				}
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					invokeButtonAvailable(false);
					switch (stepFrom) {
					case 1:
						if (model == 4) {
							duplexDirection = DOWNDIRECTION;
						} else if (model == 5) {
							duplexDirection = RIGHTDIRECTION;
						}
						stepOneToTwo();
						break;
					case 2:
						break;
					case 3:
						stepThreeToTwo();
						break;
					case 4:
						stepFourToTwo();
						break;
					}
				} else {
					changePopState();
				}
			}
		});

		bt_three.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mZoomableLayout.getmScaleFactor() == 2.0f) {
					mZoomableLayout.setAnimaling(true);
					mZoomableLayout.scaleOutAnimation(false);
				}
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					invokeButtonAvailable(false);
					switch (stepFrom) {
					case 1:
						stepOneToThree();
						break;
					case 2:
						stepTwoToThree();
						break;
					case 4:
						stepFourToThree();
						break;
					}
				} else {
					changePopState();
				}
			}
		});

		bt_four.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mZoomableLayout.getmScaleFactor() == 2.0f) {
					mZoomableLayout.setAnimaling(true);
					mZoomableLayout.scaleOutAnimation(false);
				}
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);
				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					invokeButtonAvailable(false);
					switch (stepFrom) {
					case 1:
						stepOneToFour();

						break;
					case 2:
						stepTwoToFour();

						break;
					case 3:
						stepThreeToFour();

						break;
					}
				} else {
					changePopState();
				}
			}
		});

		tvPreview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);

				if (!mZoomableLayout.isPageEdit() && (mPopupWindow == null || !mPopupWindow.isShowing())) {
					mZoomableLayout.setPreview(!mZoomableLayout.isPreview());
					firstImg.setCanDraw(!firstImg.isCanDraw());
					secondImg.setCanDraw(!secondImg.isCanDraw());
					thirdImg.setCanDraw(!thirdImg.isCanDraw());
					fourthImg.setCanDraw(!fourthImg.isCanDraw());

					if (firstImg.isCanDraw()) {
						Log.i(TAG, "!!!!!!! edit status");
						tvPreview.setText(getString(R.string.preview));
						attr.put(KEY_PRE_USED, YES);
						for (int i = 0; i < product.pages.length; i++) {
							if (i == 0) {
								firstImg.setImageBitmap(convertToSuit(product.pages[0], EDITFLAG));
							} else if (i == 1) {
								secondImg.setImageBitmap(convertToSuit(product.pages[1], EDITFLAG));
							} else if (i == 2) {
								thirdImg.setImageBitmap(convertToSuit(product.pages[2], EDITFLAG));
							} else if (i == 3) {
								fourthImg.setImageBitmap(convertToSuit(product.pages[3], EDITFLAG));
							}
						}
					} else {
						Log.i(TAG, "!!!!!! preview status, firstPreview = ? " + (firstPreview == null));
						tvPreview.setText(getString(R.string.edit));
						for (int i = 0; i < product.pages.length; i++) {
							if (i == 0) {
								firstImg.setImageBitmap(convertToSuit(product.pages[0], PREVIEWFLAG));
							} else if (i == 1) {
								secondImg.setImageBitmap(convertToSuit(product.pages[1], PREVIEWFLAG));
							} else if (i == 2) {
								thirdImg.setImageBitmap(convertToSuit(product.pages[2], PREVIEWFLAG));
							} else if (i == 3) {
								fourthImg.setImageBitmap(convertToSuit(product.pages[3], PREVIEWFLAG));
							}
						}
					}
					firstImg.invalidate();
					secondImg.invalidate();
					thirdImg.invalidate();
					fourthImg.invalidate();
				} else {
					changePopState();
				}
			}
		});

		zoomHandler = new Handler() {
			public void handleMessage(Message msg) {
				// download preview if the preview was downloaded failed
				checkAndGetUnloadedPreview(EDITFLAG);
				checkAndGetUnloadedPreview(PREVIEWFLAG);
				switch (msg.what) {
				case REVALIDATE:
					thirdImg.invalidate();
					secondImg.invalidate();
					firstImg.invalidate();
					fourthImg.invalidate();
					break;
				case SINGLECLICK:
					GreetingCardPageLayer layer = manager.getEditLayer();
					if (layer.type.equals(GreetingCardPageLayer.TYPE_IMAGE)) {
						GreetingCardPageLayer editLayer = manager.getEditLayer();
						if (editLayer.contentId.trim().length() == 0) {
//							Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSourceSelectMainActivity.class);
							Intent mIntent = new Intent(GreetingCardProductActivity.this, PhotoSelectMainFragmentActivity.class);
							mIntent.putExtra(AppConstants.KEY_PRODUCT_DECID, product.productDescriptionId);	
							startActivity(mIntent);
							finish();
						} else {
							if (mZoomableLayout.getmScaleFactor() == 2.0f) {
								setScaleNew(SMALLER, mZoomableLayout, layer);
							}
							addEditView(layer);
						}
					} else if (layer.type.equals(GreetingCardPageLayer.TYPE_TEXT_BLOCK)) {
						carTextEdit();
					}
					PrintHelper.canValidate = true;
					break;
				case SLIPTOLEFT:
					if (model == 2) {
						resetStepButton();
						if (selectIndex == 0) {
							bt_two.setImageResource(R.drawable.viewcardportraitinside2select_button);
							stepOneToTwo();
						} else if (selectIndex == 1) {
							bt_three.setImageResource(R.drawable.viewcardportraitback3select_button);
							stepTwoToThree();
						}
					} else if (model == 5) {
						duplexDirection = RIGHTDIRECTION;
						if (selectIndex == 1) {
							stepTWOToOne();
						} else {
							stepOneToTwo();
						}
					}
					break;
				case SLIPTORIGHT:
					if (model == 2) {
						resetStepButton();
						if (selectIndex == 1) {
							stepFrom = 2;
							bt_one.setImageResource(R.drawable.viewcardportraitfront1select_button);
							stepTWOToOne();
						} else if (selectIndex == 3) {
							bt_two.setImageResource(R.drawable.viewcardportraitinside2select_button);
							stepThreeToTwo();
						}
					} else if (model == 5) {
						duplexDirection = LEFTDIRECTION;
						if (selectIndex == 1) {
							stepTWOToOne();
						} else {
							stepOneToTwo();
						}
					}
					break;
				case SLIPTOTOP:
					if (model == 1) {
						resetStepButton();
						if (selectIndex == 0) {
							bt_two.setImageResource(R.drawable.viewcardlandscapeinsidetop2select_button);
							stepOneToTwo();
						} else if (selectIndex == 1) {
							bt_three.setImageResource(R.drawable.viewcardlandscapeinsidebottom3select_button);
							stepTwoToThree();
						} else if (selectIndex == 2) {
							bt_four.setImageResource(R.drawable.viewcardlandscapeback4select_button);
							stepThreeToFour();
						}
					} else if (model == 4) {
						duplexDirection = UPDIRECTION;
						if (selectIndex == 1) {
							stepTWOToOne();
						} else {
							stepOneToTwo();
						}
					}
					break;
				case SLIPTOBOTTOM:
					if (model == 1) {
						resetStepButton();
						if (selectIndex == 1) {
							bt_one.setImageResource(R.drawable.viewcardlandscapefront1select_button);
							stepFrom = 2;
							stepTWOToOne();
						} else if (selectIndex == 2) {
							bt_two.setImageResource(R.drawable.viewcardlandscapeinsidetop2select_button);
							stepThreeToTwo();
						} else if (selectIndex == 3) {
							bt_three.setImageResource(R.drawable.viewcardlandscapeinsidebottom3select_button);
							stepFourToThree();
						}
					} else if (model == 4) {
						duplexDirection = DOWNDIRECTION;
						if (selectIndex == 1) {
							stepTWOToOne();
						} else {
							stepOneToTwo();
						}
					}
					break;
				case RESIZE:
					float scale = mZoomableLayout.getmScaleFactor();
					if (scale == 2.0f) {
						setScaleNew(BIGGER, mZoomableLayout, null);
					} else {
						Bundle bundle2 = msg.getData();
						GreetingCardPageLayer mTargetLayer = (GreetingCardPageLayer) bundle2.getSerializable("targetLayer");
						setScaleNew(SMALLER, mZoomableLayout, mTargetLayer);
					}
					resetStepButton();
					break;
				case CLICKTOSEND:
					// if (mPopupWindow != null || mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
					mZoomableLayout.removeView(mEditImage);
					mZoomableLayout.setPageEdit(false);
					if (mZoomableLayout.isOverWithSend()) {
						new Thread(new SendEditInfoTask(GreetingCardProductActivity.this, manager.getEditLayer().contentId, mEditImage.getRoi(),
								mEditImage.getmRotateDegree())).start();
					}
					mZoomableLayout.setForwarding(false);
					// }
					break;
				}
			}
		};
	}

	private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
		GreetingCardPageLayer layer;
		ProgressDialog dialogWaitting;
		Bitmap mini = null;
		public BitmapWorkerTask(GreetingCardPageLayer layer) {
			this.layer = layer;
			mini = ImageUtil.getBitmapOfPhotoInfo(layer.getPhotoInfo(), GreetingCardProductActivity.this);
			dialogWaitting = new ProgressDialog(GreetingCardProductActivity.this);
			dialogWaitting.setCancelable(false);
			if (mini == null){
				dialogWaitting.show();	
			}
		}

		@Override
		protected Bitmap doInBackground(Object... params) {
			
			if (mini == null){
				ImageUtil.downloadUrlToStream(layer.getPhotoInfo(), GreetingCardProductActivity.this);
				mini = ImageUtil.getBitmapOfPhotoInfo(layer.getPhotoInfo(), GreetingCardProductActivity.this);
			}
			return mini;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap == null){
				return;
			}
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = 1;
			int w = mlayoutWidth;
			int h = mlayoutHeight;
			int moveX = (screenWidth - w) / 2;
			ROI mRoi = layer.location;
			float roiX = (float) (w * mRoi.x / mRoi.ContainerW);
			float roiY = (float) (h * mRoi.y / mRoi.ContainerH);
			float roiW = (float) (w * mRoi.w / mRoi.ContainerW);
			float roiH = (float) (h * mRoi.h / mRoi.ContainerH);
			mEditImage = new EditImage(GreetingCardProductActivity.this, bitmap, layer, (int) roiW, (int) roiH);
			RelativeLayout.LayoutParams mEditParams = new RelativeLayout.LayoutParams((int) roiW, (int) roiH);
			mEditParams.leftMargin = (int) roiX;
			mEditParams.topMargin = (int) roiY;
			mZoomableLayout.addView(mEditImage, mEditParams);
			mEditImage.setmZoomableLayout(mZoomableLayout);
			ArrayList<String> editItems = new ArrayList<String>();
			editItems.add(getString(R.string.rotate));
			editItems.add(getString(R.string.replace));
			editItems.add(getString(R.string.delete));

			int locationX = 0;
			int locationY = 0;
			Log.i(TAG, "zoelocation purase moveX = " + moveX + " , roiX = " + roiX + " , roiW = " + roiW);
			if (PrintHelper.model == 2) {
				if (manager.getEditPageIndex() == 2) {
					locationX = (int) (moveX + roiX + roiW - manager.getLayoutWidth() / 2);
				} else if (manager.getEditPageIndex() == 3) {
					locationX = (int) (moveX + roiX + roiW + manager.getLayoutWidth() / 2);
				} else {
					locationX = (int) (moveX + roiX + roiW);
				}
			} else {
				locationX = (int) (moveX + roiX + roiW);
			}
			Log.i(TAG, "zoelocation locationX = " + locationX + " , screenWidth = " + screenWidth + ", layoutWidth = " + manager.getLayoutWidth());
			locationY = (int) (roiY + roiH / 2 + screenHeight / 4 - h / 2);
			if (locationX < 3 * screenWidth / 4) {
				popEditWindow(mEditImage, editItems, locationX, locationY, 1, (int) roiW);
			} else {
				popEditWindow(mEditImage, editItems, locationX, locationY, 2, (int) roiW);
			}
			dialogWaitting.dismiss();
		}
	}
	
	private void downLoadPreviewCard (){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String lastImageContentId = prefs.getString(PrintHelper.LAST_IMAGE_CONTENT_ID, "");
		if (!lastImageContentId.equals("")) {
			new Thread(new ReplaceImageTask(this, manager.getEditLayer().holeIndex, true)).start();
			for (int i = 0; i < product.pages.length; i++) {
				if (i != (PrintHelper.lastStepTo - 1)) {
					Log.i(TAG, "zoe when back to resume ReplaceImageTask recover page index = " + (i + 1));
					if (i == 0) {
						layoutPageWithPreview(product.pages[0], firstBitmap, firstImg, EDITFLAG);
					} else if (i == 1) {
						layoutPageWithPreview(product.pages[1], secondBitmap, secondImg, EDITFLAG);
					} else if (i == 2) {
						layoutPageWithPreview(product.pages[2], thirdBitmap, thirdImg, EDITFLAG);
					} else if (i == 3) {
						layoutPageWithPreview(product.pages[3], fourthBitmap, fourthImg, EDITFLAG);
					}
				}
			}
		} else {
			if (manager.getEditLayer() != null && manager.getEditLayer().getPhotoInfo() != null && manager.getEditLayer().contentId.equals("")) {
				attr.put(KEY_PIC_ADD, YES);
				new Thread(new AddImageToCart(this, manager.getEditLayer().holeIndex, true)).start();
				for (int i = 0; i < product.pages.length; i++) {
					if (i != (PrintHelper.lastStepTo - 1)) {
						Log.i(TAG, "zoe when back to resume AddImageToCart recover page index = " + (i + 1));
						if (i == 0) {
							layoutPageWithPreview(product.pages[0], firstBitmap, firstImg, EDITFLAG);
						} else if (i == 1) {
							layoutPageWithPreview(product.pages[1], secondBitmap, secondImg, EDITFLAG);
						} else if (i == 2) {
							layoutPageWithPreview(product.pages[2], thirdBitmap, thirdImg, EDITFLAG);
						} else if (i == 3) {
							layoutPageWithPreview(product.pages[3], fourthBitmap, fourthImg, EDITFLAG);
						}
					}
				}
			} else {
				new Thread(new GetAllPreview()).start();
			}
		}
	}

}
