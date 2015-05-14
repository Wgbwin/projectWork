package com.kodakalaris.kodakmomentslib.activity.photoedit;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.AppConstants.ActivityTheme;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.mobile.GalleryAdapter;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.kodakalaris.kodakmomentslib.culumus.bean.product.ROI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.thread.SingleThreadPool;
import com.kodakalaris.kodakmomentslib.thread.edit.ColorEffectTask;
import com.kodakalaris.kodakmomentslib.thread.edit.LevelEditTask;
import com.kodakalaris.kodakmomentslib.thread.edit.RedEyeEditTask;
import com.kodakalaris.kodakmomentslib.thread.edit.RestoreData;
import com.kodakalaris.kodakmomentslib.thread.edit.RotateEditTask;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.ImageCropSurfaceView;
import com.kodakalaris.kodakmomentslib.widget.mobile.ZoomImageView;
import com.kodakalaris.kodakmomentslib.widget.mobile.ZoomRelativeLayout;

@SuppressWarnings("deprecation")
public class MPhotoEditActivity extends BasePhotoEditActivity {
	private String TAG = MPhotoEditActivity.class.getSimpleName();
	// Constant value/////////////////////////////////////////////
	public static final int START = 0;
	public final static int CONTINUE = 1;
	public final static int END = 2;
	public final static int ROTATE_END = 3;
	private final static int GalleryIdle = 4;
	private final static int ROTATE = 5;
	private final static int LEVEL = 6;
	private final static int REDEYE = 7;
	private final static int FILTER = 8;
	private final static int ROTATE_DEGREE = -90;
	private static int CURRENTTASK = -1;
	// Layout properties////////////////////////////////////////////
	private ImageView vImgBack;
	private TextView vTxtFilterName;
	private TextView vTxtSave;
	private ImageView vImgRotate;
	private ImageView vImgCrop;
	private ImageView vImgRedeye;
	private ImageView vImgFilters;
	private ImageView vImgLevels;
	private Gallery vGalleryFilters;
	private GalleryAdapter mAdapter;
	private ZoomImageView vImgEditPicture;
	private ZoomRelativeLayout vRelaLyEditContainer;
	private ImageCropSurfaceView vImgCropPicture;
	private RelativeLayout vProgressBarWait;
	private RelativeLayout vRealyCropContainer;
	private LinearLayout vlineLyEditContainer;
	private LinearLayout vLineLyFilterContainer;
	// Member variable//////////////////////////////////////////////
	private int num;
	private int loadTime;
	private int mImgShowWidth;
	private int mImgShowHeight;
	private int mPhoneWidth;
	private int mPhoneHeight;
	private int rotateDegree;
	private int recodeDegree;
	private int filterPosition;
	private int operateAreaHeight = 410;
	private float preTime;
	private float density;
	private boolean isEditPhoto;
	private boolean isPrintAreaChange;
	private boolean isReadyChange;
	private boolean isNeedSwap;
	private boolean isRotating;
	private boolean isShowDialog;
	private boolean isResotreData;
	private boolean isServerImage;
	private boolean isServerImage2;
	private boolean isNeedUpdateCropImage;
	private boolean isFilterFirstTime = true;
	private boolean isFilterFling = true;
	private PrintItem mPrintItem;
	private PrintItem mCurrentPrintItem;
	private PhotoInfo mPhotoInfo;
	private RssEntry mRssEntry;
	private ROI mRoi;
	private Intent mIntent;
	private LayoutParams mParams;
	private LayoutParams mProgressBarParams;
	private SingleThreadPool mSingleThreadPool;
	private GeneralAPI mGeneralAPI;
	private List<ColorEffect> colorEffects;

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (isFinishing())
				return;
			final int action = msg.what;
			Object msgObject = msg.obj;
			Object[] array = (Object[]) msgObject;
			switch (action) {
			case START:
				isRotating = false;
				showWaitingDialog();
				break;
			case CONTINUE:
				boolean succeed = (Boolean) array[1];
				if (isResotreData) {
					if (succeed) {
						dismissWaitingDialog();
					}
					MPhotoEditActivity.this.finish();
				} else {
					if (succeed) {
						vImgEditPicture.downloadImage((PhotoInfo) array[0],
								mHandler);
					} else {
						dismissWaitingDialog();
						showAlertDialog();
					}
				}

				break;
			case END:
				dismissWaitingDialog();
				if (!isPrintAreaChange) {
					isPrintAreaChange = true;
				}
				PhotoInfo photoInfo = (PhotoInfo) array[0];
				if (photoInfo != null) {
					if (!isServerImage) {
						isServerImage = true;
						mPrintItem.isServerImage = true;
					}
					mPrintItem.getImage().setPhotoEditPath(
							photoInfo.getPhotoEditPath());
					rotateDegree = 0;
					vImgCropPicture.rotateDegree = 0;
					vImgLevels.setSelected(mPrintItem.isUseEnhance);
					vImgRedeye.setSelected(mPrintItem.isUseRedEye);
					if (isRotating) {
						swapROIByRotateDegree(true);
						isRotating = false;
						isEditPhoto = true;
					} else {
						if (!isEditPhoto && isNeedSwap) {
							mPrintItem.setRoi(swapMPrintItemROI(mPrintItem
									.getRoi()));
							isEditPhoto = true;
							isNeedSwap = false;
							isServerImage = true;
							mCurrentPrintItem.isServerImage = true;
						}
						isNeedUpdateCropImage = true;
						vImgEditPicture.setImageBitmap(mPrintItem,
								vImgEditPicture.width, vImgEditPicture.height,
								rotateDegree);
					}
					if (filterPosition != num) {
						vGalleryFilters.setSelection(filterPosition);
						vTxtFilterName
								.setText(colorEffects.get(filterPosition).name);
					}

				}
				break;
			case ROTATE_END:
				boolean success = (Boolean) array[1];
				if (success) {
					if (array.length == 3) {
						if (array[2] != null) {
							if (isNeedSwap && !isServerImage) {
								isRotating = true;
								vImgEditPicture.downloadImage(
										(PhotoInfo) array[0], mHandler);
							} else {
								dismissWaitingDialog();
								swapROIByRotateDegree(false);
							}
							vImgRotate.setSelected(rotateDegree != 0);
						}
					}
				} else {
					dismissWaitingDialog();
					rotateDegree = (rotateDegree - 90) % 360;
					recodeDegree = (recodeDegree - 90) % 360;
					showAlertDialog();
				}
				break;
			case GalleryIdle:
				if (isFilterFirstTime && num == filterPosition) {
					isFilterFirstTime = false;
					return;
				}
				loadColorEffectImage(num);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_m_photo_edit);
		initView();// find widget from layout
		initInfo();// load info from source
		setEvents();// widget's event listener
	}

	private void initInfo() {
		mGeneralAPI = new GeneralAPI(this);
		mIntent = getIntent();
		mPrintItem = (PrintItem) mIntent.getExtras().getSerializable(
				"printItem");
		setInitROI();
		Log.i(TAG, "==>mPrintItem.roi:" + mPrintItem.getRoi().toString());
		isServerImage = mPrintItem.isServerImage;
		isServerImage2 = isServerImage;
		mRssEntry = mPrintItem.getEntry();
		mPhotoInfo = mPrintItem.getImage();
		rotateDegree = mPrintItem.rotateDegree;
		vImgCropPicture.rotateDegree = rotateDegree;
		isNeedSwap = mPhotoInfo.isNeedSwapWidthAndHeightForCalculate();
		if (isNeedSwap) {
			isNeedSwap = !isServerImage;
		}
		for (PrintItem item : PrintManager.getInstance(MPhotoEditActivity.this)
				.getPrintItems()) {
			if (item.isCheckedInstance) {
				mCurrentPrintItem = item;
			}
		}
		colorEffects = KM2Application.getInstance().getColorEffects();
		ColorEffect colorEffect = mPrintItem.colorEffect;
		if (colorEffect != null && colorEffects != null) {
			int posotion = 0;
			for (ColorEffect item : colorEffects) {
				if (colorEffect.id == item.id) {
					filterPosition = posotion;
					num = posotion;
					break;
				}
				posotion++;
			}
		}

		if (!KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			vTxtFilterName.setText(colorEffects.get(filterPosition).name);
			vImgLevels.setSelected(mPrintItem.isUseEnhance);
			vImgRedeye.setSelected(mPrintItem.isUseRedEye);
			vImgRotate.setSelected(rotateDegree != 0);
			galleryBindSource();
			vGalleryFilters.setSelection(filterPosition);
			if (filterPosition == 0) {
				vImgFilters
						.setImageResource(R.drawable.selector_photo_edit_filters);
			} else {
				vImgFilters
						.setImageResource(R.drawable.selector_photo_edit_filters2);
			}

		}

		setZoomImageViewSize(mPrintItem.getRoi());
		vImgEditPicture.setImageBitmap(mPrintItem, mImgShowWidth,
				mImgShowHeight, rotateDegree);
		vImgCropPicture.printItem = mPrintItem;

		mPhoneWidth = getResources().getDisplayMetrics().widthPixels;
		mPhoneHeight = getResources().getDisplayMetrics().heightPixels;
	}

	private void setInitROI() {
		try {
			mRoi = (ROI) mPrintItem.getRoi().clone();
		} catch (CloneNotSupportedException e) {
			Log.e(TAG, "mPrintItem.getRoi().clone() is wrong");
		}
	}

	private void setEvents() {
		vImgBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isPrintAreaChange || isROIChange(mRoi, mPrintItem.getRoi())) {
					showDailog();
				} else {
					MPhotoEditActivity.this.finish();
				}
			}
		});

		vTxtSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveData();
			}
		});

		vImgRotate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (vImgCrop.isSelected()) {
					vImgCropPicture.setRotate(true);
					if (!isPrintAreaChange) {
						isPrintAreaChange = true;
					}
					if (isPrintAreaChange && mPrintItem.getRoi().equals(mRoi)) {
						isPrintAreaChange = false;
					}
				} else {
					if (KM2Application.getInstance().getFlowType()
							.isPrintHubWorkFlow()) {
						// add function for printHub by bing
						vImgEditPicture.rotateImageBitmap();
						setZoomImageViewSize(vImgEditPicture.getRoi());
						vImgEditPicture.updateData(mImgShowWidth,
								mImgShowHeight);
						isNeedUpdateCropImage = true;

					} else {
						rotateDegree = (rotateDegree + 90) % 360;
						recodeDegree = (recodeDegree + 90) % 360;
						beginEditTask(new RotateEditTask(
								MPhotoEditActivity.this, mPrintItem, mHandler,
								mGeneralAPI, ROTATE_DEGREE), ROTATE);
					}

				}
			}
		});

		vImgCrop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (vImgCrop.isSelected()) {
					setWidgetEnable();
					vImgCrop.setSelected(false);
					vImgRotate.setSelected(rotateDegree != 0);
					vRelaLyEditContainer.setVisibility(View.VISIBLE);
					vRealyCropContainer.setVisibility(View.GONE);
					vImgCropPicture.setViewGone();
					// rotate the widget
					mParams = vImgEditPicture.getLayoutParams();
					setZoomImageViewSize(vImgCropPicture.saveROI());
					vImgEditPicture.updateData(vImgCropPicture.roi,
							mImgShowWidth, mImgShowHeight);
				} else {
					setWidgetDisabled();
					vImgCrop.setEnabled(true);
					vImgRotate.setEnabled(true);
					vImgRotate.setSelected(false);
					vImgCrop.setSelected(true);
					vImgCropPicture.setViewVisible();

					int rotateDegree = vImgEditPicture.getTotelRotateDegree();
					if (rotateDegree > 0) {
						vImgCropPicture.setRotateDegree(rotateDegree);
					}

					if (vImgCropPicture.img != null && isNeedUpdateCropImage) {
						vImgCropPicture.updateImage();
						isNeedUpdateCropImage = false;
					}
					vImgCropPicture.rect = null;
					vRelaLyEditContainer.setVisibility(View.GONE);
					vRealyCropContainer.setVisibility(View.VISIBLE);
				}
			}
		});

		vImgRedeye.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				beginEditTask(new RedEyeEditTask(MPhotoEditActivity.this,
						mPrintItem, mHandler, mGeneralAPI), REDEYE);
			}
		});

		vImgFilters.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (vImgFilters.isSelected()) {
					setWidgetEnable();
					vImgFilters.setSelected(false);
					vLineLyFilterContainer.setVisibility(View.GONE);
				} else {
					setWidgetDisabled();
					vImgFilters.setEnabled(true);
					vImgFilters.setSelected(true);
					vLineLyFilterContainer.setVisibility(View.VISIBLE);
				}
				changeZoomViewHeightAndWidth();
			}
		});
		vImgLevels.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				beginEditTask(new LevelEditTask(MPhotoEditActivity.this,
						mPrintItem, mHandler, mGeneralAPI), LEVEL);
			}
		});
		vGalleryFilters.setCallbackDuringFling(true);
		vGalleryFilters.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				vGalleryFilters.onTouchEvent(event);
				if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
					isReadyChange = true;
				} else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
					isReadyChange = false;
				}
				return true;
			}

		});
		vGalleryFilters.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				vTxtFilterName.setText(colorEffects.get(position).name);
				num = position;
				galleryWhetherStop();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void galleryWhetherStop() {
		new Thread(new Runnable() {
			public void run() {
				try {
					int index = 0;
					int count = 0;
					boolean isOk = false;
					while (isFilterFling) {
						index = num;
						Thread.sleep(500);
						if (isOk) {
							isOk = false;
						}
						if (index == num) {
							count++;
							isOk = true;
							mHandler.obtainMessage(GalleryIdle, null)
									.sendToTarget();
							break;
						}
						if (count == 1) {
							if (!isOk) {
								isFilterFling = false;
							} else {
								Thread.sleep(500);
							}
						} else if (count == 2) {
							isFilterFling = false;
						}
					}
					isFilterFling = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * when touch back button show the tip dialog
	 */
	public void showDailog() {
		GeneralAlertDialogFragment backDialog = new GeneralAlertDialogFragment(
				this, ActivityTheme.DARK, true);
		backDialog.setMessage(this.getString(R.string.image_backWarning));
		backDialog.setPositiveButton(this.getString(R.string.image_yes),
				new BaseGeneralAlertDialogFragment.OnClickListener() {
					@Override
					public void onClick(
							BaseGeneralAlertDialogFragment dialogFragment,
							View v) {
						dialogFragment.dismiss();
						saveData();
					}
				});
		backDialog.setNegativeButton(this.getString(R.string.image_no),
				new BaseGeneralAlertDialogFragment.OnClickListener() {

					@Override
					public void onClick(
							BaseGeneralAlertDialogFragment dialogFragment,
							View v) {
						mCurrentPrintItem.isCheckedInstance = false;
						if (isEditPhoto && !isServerImage2) {
							mCurrentPrintItem
									.setRoi(swapMPrintItemROI(mCurrentPrintItem
											.getRoi()));
							mCurrentPrintItem.getImage().setPhotoEditPath(
									mPrintItem.getImage().getPhotoEditPath());
						}
						isResotreData = true;
						dialogFragment.dismiss();
						mPrintItem.rotateDegree = recodeDegree;
						new Thread(new RestoreData(MPhotoEditActivity.this,
								mPrintItem, mHandler, mGeneralAPI)).start();
					}
				});
		backDialog.show(getSupportFragmentManager(), TAG);
	}

	private ROI swapMPrintItemROI(ROI roi) {
		ROI newROI = new ROI();
		double tempX = roi.x;
		double tempY = roi.y;
		double tempW = roi.w;
		double tempH = roi.h;
		double tempCW = roi.ContainerW;
		double tempCH = roi.ContainerH;
		newROI.x = tempY;
		newROI.y = tempX;
		newROI.w = tempH;
		newROI.h = tempW;
		newROI.ContainerW = tempCH;
		newROI.ContainerH = tempCW;
		return newROI;
	}

	public void swapROIByRotateDegree(boolean isNeedSwap) {
		ROI roi = vImgEditPicture.getROI();
		double tempX = roi.x;
		double tempY = roi.y;
		double tempW = roi.w;
		double tempH = roi.h;
		double tempCW = roi.ContainerW;
		double tempCH = roi.ContainerH;
		if (isNeedSwap) {
			this.isNeedSwap = false;
			mCurrentPrintItem.isServerImage = true;
		} else {
			roi.x = 1.0 - tempH - tempY;
			roi.y = tempX;
			roi.w = tempH;
			roi.h = tempW;
			roi.ContainerW = tempCH;
			roi.ContainerH = tempCW;
		}

		setZoomImageViewSize(roi);
		mPrintItem.setRoi(roi);
		vImgCropPicture.rotateDegree = rotateDegree;
		if (vImgCropPicture.img != null) {
			vImgCropPicture.img.recycle();
			vImgCropPicture.img = null;
		}
		vImgEditPicture.setImageBitmap(mPrintItem, mImgShowWidth,
				mImgShowHeight, rotateDegree);
	}

	private void beginEditTask(Runnable runnable, int currentTask) {
		if (mPrintItem == null) {
			return;
		}
		CURRENTTASK = currentTask;
		if (mSingleThreadPool == null) {
			mSingleThreadPool = new SingleThreadPool();
		}
		mSingleThreadPool.addHighPriorityTask(runnable);
	}

	private void loadColorEffectImage(int position) {
		if (!isFilterFirstTime) {
			if (position != filterPosition) {
				if (isReadyChange) {
					beginEditTask(new ColorEffectTask(MPhotoEditActivity.this,
							mPrintItem, mHandler, mGeneralAPI, colorEffects,
							position), FILTER);
					isReadyChange = false;
					filterPosition = position;
					if (filterPosition == 0) {
						vImgFilters
								.setImageResource(R.drawable.selector_photo_edit_filters);
					} else {
						vImgFilters
								.setImageResource(R.drawable.selector_photo_edit_filters2);
					}
				}
			}
		}
	}

	/**
	 * Description: initial the ZoomImageView's height and width according roi
	 * Add by: Kaly Jan 28, 2015 3:21:36 PM
	 */
	private void setZoomImageViewSize(ROI roi) {
		if (roi == null) {
			return;
		}
		int pageWidth = mRssEntry.proDescription.pageWidth;
		int pageHeight = mRssEntry.proDescription.pageHeight;
		density = getResources().getDisplayMetrics().density;
		if (pageWidth > pageHeight) {
			// come in if the dimension's data is wrong
			// like 6*4
			if (isNeedSwap) {
				mImgShowHeight = (int) (density * operateAreaHeight * 78 / 100);
				if (roi.w * roi.ContainerW >= roi.h * roi.ContainerH) {
					mImgShowWidth = mImgShowHeight * pageHeight / pageWidth;
				} else {
					int width1 = (int) (mImgShowHeight * pageWidth / pageHeight);
					int width2 = mImgShowWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
							.getDefaultDisplay().getWidth() * 78 / 100;
					if (width1 > width2) {
						mImgShowWidth = width2;
						mImgShowHeight = mImgShowWidth * pageHeight / pageWidth;
					} else {
						mImgShowWidth = width1;
					}
				}

			} else {
				mImgShowWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay().getWidth() * 78 / 100;
				if (roi.w * roi.ContainerW >= roi.h * roi.ContainerH) {
					mImgShowHeight = mImgShowWidth * pageHeight / pageWidth;
				} else {
					mImgShowHeight = (int) (mImgShowWidth * pageWidth / pageHeight);
				}
			}
		} else {
			// come in if the dimension's data is true like
			// 4*6
			if (isNeedSwap) {
				mImgShowHeight = (int) (density * operateAreaHeight * 78 / 100);
				if (roi.w * roi.ContainerW > roi.h * roi.ContainerH) {
					int width1 = mImgShowHeight * pageWidth / pageHeight;
					int width2 = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
							.getDefaultDisplay().getWidth() * 78 / 100;
					if (width1 > width2) {
						mImgShowWidth = width2;
						mImgShowHeight = mImgShowWidth * pageWidth / pageHeight;
					} else {
						mImgShowWidth = width1;
					}
				} else {
					int width1 = mImgShowHeight * pageHeight / pageWidth;
					int width2 = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
							.getDefaultDisplay().getWidth() * 78 / 100;
					if (width1 > width2) {
						mImgShowWidth = width2;
						mImgShowHeight = mImgShowWidth * pageWidth / pageHeight;
					} else {
						mImgShowWidth = width1;
					}
				}

			} else {
				mImgShowWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
						.getDefaultDisplay().getWidth() * 78 / 100;
				if (roi.w * roi.ContainerW > roi.h * roi.ContainerH) {
					mImgShowHeight = mImgShowWidth * pageWidth / pageHeight;
				} else {
					mImgShowHeight = mImgShowWidth * pageHeight / pageWidth;
				}
			}
		}
		setZoomViewLayoutParams(mImgShowWidth, mImgShowHeight);
	}

	private void changeZoomViewHeightAndWidth() {

		if (vLineLyFilterContainer.getVisibility() == View.VISIBLE
				&& mImgShowWidth < mImgShowHeight) {
			setZoomViewLayoutParams((int) (mImgShowWidth * 0.85),
					(int) (mImgShowHeight * 0.85));
			vImgEditPicture
					.updateData(vImgEditPicture.printItem.getRoi(),
							(int) (mImgShowWidth * 0.85),
							(int) (mImgShowHeight * 0.85));
		} else if (vLineLyFilterContainer.getVisibility() == View.GONE) {
			setZoomViewLayoutParams(mImgShowWidth, mImgShowHeight);
			vImgEditPicture.updateData(vImgEditPicture.printItem.getRoi(),
					mImgShowWidth, mImgShowHeight);
		}
	}

	private void setZoomViewLayoutParams(int mImgShowWidth, int mImgShowHeight) {
		mParams = vImgEditPicture.getLayoutParams();
		mParams.width = mImgShowWidth;
		mParams.height = mImgShowHeight;
		vImgEditPicture.setLayoutParams(mParams);
	}

	/**
	 * Description: save the picture's roi with the open,At Feb 2, 2015 1:40:56
	 * PM
	 */
	private void saveData() {
		if (vRelaLyEditContainer.getVisibility() == View.VISIBLE) {
			mPrintItem.setRoi(vImgEditPicture.getROI());
		}
		if (vRealyCropContainer.getVisibility() == View.VISIBLE) {
			mPrintItem.setRoi(vImgCropPicture.saveROI());
		}

		int degree = vImgEditPicture.getTotelRotateDegree();
		if (degree > 0) {
			rotateDegree = degree;
		}
		mPrintItem.rotateDegree = rotateDegree;

		if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			PrintHubManager.getInstance().updateRoi(mPrintItem.getRoi(),
					mPrintItem);
		} else {
			PrintManager.getInstance(this).updateRoi(mPrintItem.getRoi(),
					mPrintItem);
		}

		finish();
	}

	/**
	 * Description: set the five handle image button disabled
	 */
	private void setWidgetDisabled() {
		for (int i = 0; i < vlineLyEditContainer.getChildCount(); i++) {
			vlineLyEditContainer.getChildAt(i).setEnabled(false);
		}
	}

	/**
	 * Description: set the five handle image button enabled
	 */
	private void setWidgetEnable() {
		for (int i = 0; i < vlineLyEditContainer.getChildCount(); i++) {
			vlineLyEditContainer.getChildAt(i).setEnabled(true);
		}
	}

	private void showAlertDialog() {

		if (preTime == 0) {
			preTime = System.currentTimeMillis();
		} else {
			if (System.currentTimeMillis() - preTime < 1000 || loadTime > 1) {
				Toast.makeText(
						this,
						"Oops,the Wifi is weak now ,wait a minute and try again",
						Toast.LENGTH_SHORT).show();
				return;
			} else if (System.currentTimeMillis() - preTime > 1000 * 90) {
				preTime = 0;
			}
		}
		loadTime++;
		GeneralAlertDialogFragment lowResDialog = new GeneralAlertDialogFragment(
				this, ActivityTheme.DARK, true);
		lowResDialog.setMessage(this.getString(R.string.image_load_wrong));
		lowResDialog.setPositiveButton(this.getString(R.string.image_ok),
				new BaseGeneralAlertDialogFragment.OnClickListener() {

					@Override
					public void onClick(
							BaseGeneralAlertDialogFragment dialogFragment,
							View v) {
						switch (CURRENTTASK) {
						case ROTATE:
							rotateDegree = (rotateDegree + 90) % 360;
							recodeDegree = (recodeDegree + 90) % 360;
							beginEditTask(new RotateEditTask(
									MPhotoEditActivity.this, mPrintItem,
									mHandler, mGeneralAPI, ROTATE_DEGREE),
									ROTATE);
							break;
						case FILTER:
							beginEditTask(new ColorEffectTask(
									MPhotoEditActivity.this, mPrintItem,
									mHandler, mGeneralAPI, colorEffects,
									filterPosition), FILTER);
							break;
						case REDEYE:
							beginEditTask(new RedEyeEditTask(
									MPhotoEditActivity.this, mPrintItem,
									mHandler, mGeneralAPI), REDEYE);
							break;
						case LEVEL:
							beginEditTask(new LevelEditTask(
									MPhotoEditActivity.this, mPrintItem,
									mHandler, mGeneralAPI), LEVEL);
							break;
						}
					}
				});
		lowResDialog.show(getSupportFragmentManager(), TAG);
	}

	/**
	 * find views' id from layout
	 */
	private void initView() {
		vImgBack = (ImageView) findViewById(R.id.img_photo_edit_back);
		// vTxtEdit = (TextView) findViewById(R.id.txt_photo_edit_edit);
		vTxtFilterName = (TextView) findViewById(R.id.txt_item_photo_filter_picturename);
		vTxtSave = (TextView) findViewById(R.id.txt_photo_edit_save);
		vImgEditPicture = (ZoomImageView) findViewById(R.id.img_photo_edit_picture);
		vImgRotate = (ImageView) findViewById(R.id.img_photo_edit_rotate);
		vImgCrop = (ImageView) findViewById(R.id.img_photo_edit_crop);
		vImgRedeye = (ImageView) findViewById(R.id.img_photo_edit_redeye);
		vImgFilters = (ImageView) findViewById(R.id.img_photo_edit_filters);
		vImgFilters.setSelected(false);
		vImgLevels = (ImageView) findViewById(R.id.img_photo_edit_levels);
		vRelaLyEditContainer = (ZoomRelativeLayout) findViewById(R.id.realLy_photo_edit_container);
		vRealyCropContainer = (RelativeLayout) findViewById(R.id.realLy_photo_edit_container_crop);
		vLineLyFilterContainer = (LinearLayout) findViewById(R.id.lineLy_photo_edit_filter_container);
		vlineLyEditContainer = (LinearLayout) findViewById(R.id.lineLy_photo_edit_container);
		vImgCropPicture = (ImageCropSurfaceView) findViewById(R.id.img_photo_edit_crop_picture);
		vGalleryFilters = (Gallery) findViewById(R.id.gallery_photo_edit_pinkish_filter);
		vProgressBarWait = (RelativeLayout) findViewById(R.id.progressbar_photo_edit_wait);

		if (KM2Application.getInstance().getFlowType().isPrintHubWorkFlow()) {
			vImgRedeye.setVisibility(View.GONE);
			vImgFilters.setVisibility(View.GONE);
			vImgLevels.setVisibility(View.GONE);
		}
	}

	private void showWaitingDialog() {
		if (!isShowDialog) {
			dismissWaitingDialog();
			vProgressBarWait.setVisibility(View.VISIBLE);
			mProgressBarParams = vProgressBarWait.getLayoutParams();
			mProgressBarParams.width = mPhoneWidth;

			if (vLineLyFilterContainer.getVisibility() == View.VISIBLE) {
				mProgressBarParams.height = mPhoneHeight
						- ((int) (90 * density));
			} else {
				mProgressBarParams.height = mPhoneHeight;
			}
			vProgressBarWait.setLayoutParams(mProgressBarParams);
			vImgEditPicture.setEnabled(false);
			isShowDialog = true;
		}
	}

	private void dismissWaitingDialog() {
		vProgressBarWait.setVisibility(View.GONE);
		vImgEditPicture.setEnabled(true);
		isShowDialog = false;
	}

	private boolean isHighError(double x, double y) {
		return (int) (Math.abs(x - y)) >= 10;
	}

	private boolean isHighValueError(double x, double y) {
		return (Math.abs(x - y)) >= 0.01;
	}

	private boolean isROIChange(ROI originalROI, ROI newROI) {

		if (Double.doubleToLongBits(originalROI.ContainerH) != Double
				.doubleToLongBits(newROI.ContainerH))
			return true;
		if (isHighError((originalROI.x * originalROI.ContainerW),
				(newROI.x * newROI.ContainerW)))
			return isHighValueError(originalROI.x, newROI.x);

		if (isHighError((originalROI.y * originalROI.ContainerH),
				(newROI.y * newROI.ContainerH)))
			return isHighValueError(originalROI.y, newROI.y);
		if (isHighError((originalROI.w * originalROI.ContainerW),
				(newROI.w * newROI.ContainerW)))
			return isHighValueError(originalROI.w, newROI.w);
		if (isHighError((originalROI.h * originalROI.ContainerH),
				(newROI.h * newROI.ContainerH)))
			return isHighValueError(originalROI.h, newROI.h);
		return false;
	}

	private void galleryBindSource() {
		if (mAdapter == null) {
			mAdapter = new GalleryAdapter(MPhotoEditActivity.this, colorEffects);
			vGalleryFilters.setAdapter(mAdapter);
		} else {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		if (vImgEditPicture != null)
			vImgEditPicture.recycleInFinish();
		if (vImgCropPicture != null) {
			vImgCropPicture.setViewGone();
			vImgCropPicture.recycleInFinish();
			vImgCropPicture = null;
		}
		super.onDestroy();
	}
}
