package com.kodak.kodak_kioskconnect_n2r;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.ImageFetcherTN;
import com.example.android.bitmapfun.util.ImageResizerTN;
import com.example.android.bitmapfun.util.Utils;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.EncryptUtil;
import com.kodak.utils.RSSLocalytics;

/*import com.kodak.common.BroadcastReceiverHelper;*/

public class ImageSelectionActivity extends FragmentActivity {
	/*private final String TAG = this.getClass().getSimpleName();
	private GridView photos;
	private Cursor imagecursor;
	private int image_column_index;
	private final int mSpacing = 30;
	protected final int mMonthLimit = 60;
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	protected int mLatestMonthWithPictures = -mMonthLimit - 1;
	int selectedCount;
	int width = 0;
	int height = 0;
	int highlightPosition = -1;
	int iMin, iMax;
	
	public ImageAdapter imageAdapter;
	public BigImageAdapter bigImageAdapter;
	private Button selectAll;
	private Button deselectAll;
	private Button next;
	private Button startOver;
	private Button viewModeButton;
	private Button shareButton;
	private Button info;
	private Button settings;
	
	private TextView headerBarText;
	private TextView selectedTextView;
	TextView totalSelectedTV;
	
	private static final String IMAGE_CACHE_DIR = "selectionthumbs";
	private final String SCREEN_NAME = "Image Selection";
	private String FROM_IMAGESELECTION = "FROMIMAGESELECTION";
	private static String PACKAGE_NAME;
	private String[] uris;
	String highlightURI = "";
	String sSelectRange;
	String packageName;
	
	private Gallery photosGallery;
	ProgressDialog dialog;	
	public Activity act = this;	
	private Display display;
	private ImageSelectionDatabase mImageSelectionDatabase = null;
	RelativeLayout selectionBar;
	ImageCheckBoxView lastView;

	BitmapFactory.Options options = new Options();
	double outWidth = 0.0;
	double outHeight = 0.0;
	
	private ImageResizerTN mImageWorker;
	
	Bitmap draw = null;
	
	SharedPreferences prefs;
	SharedPreferences mSharedPreferences;
	
	
	private PopupWindow shareMenu;
	private InfoDialog.InfoDialogBuilder connectBuilder;
	

	 BroadcastReceiverHelper broadcastReceiver; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mImageSelectionDatabase = new ImageSelectionDatabase(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.imageselection);
		Localytics.onActivityCreate(this);
		Localytics.recordLocalyticsPageView(this, SCREEN_NAME);
		PrintHelper.handleUncaughtException(ImageSelectionActivity.this, this);
		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);
		prefs = PreferenceManager
				.getDefaultSharedPreferences(ImageSelectionActivity.this);
		display = getWindowManager().getDefaultDisplay();
		info = (Button) findViewById(R.id.infoButton);
		photos = (GridView) findViewById(R.id.picturesGrid);
		photosGallery = (Gallery) findViewById(R.id.picturesGallery);
		viewModeButton = (Button) findViewById(R.id.modeSwitchBtn);
		shareButton = (Button) findViewById(R.id.shareButton);
		headerBarText = (TextView) findViewById(R.id.headerBarText);
		selectAll = (Button) findViewById(R.id.selectall);
		deselectAll = (Button) findViewById(R.id.deselectall);
		next = (Button) findViewById(R.id.nextButton);
		startOver = (Button) findViewById(R.id.backButton);
		settings = (Button) findViewById(R.id.settingsButton);
		selectionBar = (RelativeLayout) findViewById(R.id.selectionbar);
		selectedTextView = (TextView) findViewById(R.id.totalSelectedTextView);
		totalSelectedTV = (TextView) findViewById(R.id.totalSelectedTextView);
		selectedTextView.setTypeface(PrintHelper.tfb);
		headerBarText.setTypeface(PrintHelper.tf);
		startOver.setTypeface(PrintHelper.tf);
		startOver.setText(getString(R.string.albums));
		next.setTypeface(PrintHelper.tf);
		totalSelectedTV.setTypeface(PrintHelper.tfb);
		next.setEnabled(false);
		width = display.getWidth();
		height = display.getHeight();
		setupCursor();
		setupAdapter();
		setupEvents();
		packageName = this.getPackageName();
		
		 * if(packageName.contains("wmc")){
		 * shareButton.setVisibility(View.VISIBLE); }
		 

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		photos.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (imageAdapter != null
								&& imageAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(photos
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (photos.getWidth() / numColumns)
										- mImageThumbSpacing;
								imageAdapter.setNumColumns(numColumns);
								imageAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
									Log.d(TAG,
											"onCreateView - numColumns set to "
													+ numColumns);
								}
							}
						}
					}
				});
		photos.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				ImageAdapter tempAdapter = ((ImageAdapter) photos.getAdapter());
				if (tempAdapter != null) {
					if (scrollState != 0) {
						tempAdapter.isScrolling = true;
					} else {
						tempAdapter.isScrolling = false;
						tempAdapter.notifyDataSetChanged();
					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		final float scale = getBaseContext().getResources().getDisplayMetrics().density;
		// pixels = (int) (PrintHelper.thumbnailSize * scale + 0.5f);
		// pixels += (6 * scale);
		// int numcolumns = (int) ((width - (6 * scale)) / pixels);
		// int overspray = (int) ((width - (6 * scale)) % pixels);
		// photos.setNumColumns(numcolumns);
		// photos.setPadding(overspray, 0, overspray, 0);
		photosGallery.setSpacing((int) (mSpacing * scale + 0.5f));
		if (PrintHelper.mLoggingEnabled)
			Log.d(TAG, "Button " + PrintHelper.albumid + " pressed");
		ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);
		// Allocate a third of the per-app memory limit to the bitmap memory
		// cache. This value
		// should be chosen carefully based on a number of factors. Refer to the
		// corresponding
		// Android Training class for more discussion:
		// http://developer.android.com/training/displaying-bitmaps/
		// In this case, we aren't using memory for much else other than this
		// activity and the
		// ImageDetailActivity so a third lets us keep all our sample image
		// thumbnails in memory
		// at once.
		cacheParams.memCacheSize = 1024 * 1024 * Utils.getMemoryClass(this) / 3;
		// The ImageWorker takes care of loading images into our ImageView
		// children asynchronously
		mImageWorker = new ImageFetcherTN(this, mImageThumbSize);
		if (PrintHelper.imageSelectionThumbWorkerUrlsAdapter != null) {
			mImageWorker
					.setAdapter(PrintHelper.imageSelectionThumbWorkerUrlsAdapter);
		}
		mImageWorker.setLoadingImage(R.drawable.imagewait96x96);
		if (cacheParams != null) {
			mImageWorker.setImageCache(ImageCache.findOrCreateCache(
					ImageSelectionActivity.this, cacheParams));
		}

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(ImageSelectionActivity.this);
		//iMin = mSharedPreferences.getInt(PrintHelper.sMinNumberOfImages, 0);// -
																			// (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID,
																			// "").equals("")
																			// ?
																			// 0
																			// :
																			// 1);
		//iMax = mSharedPreferences.getInt(PrintHelper.sMaxNumberOfImages, 0);// -
																			// (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID,
																			// "").equals("")
																			// ?
																			// 0
																			// :
																			// 1);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		int iMin = photobook==null ? 0 : photobook.minNumberOfImages;
		int iMax = photobook==null ? 0 : photobook.maxNumberOfImages;
		sSelectRange = iMin + "-" + iMax;

		if (getIntent().getBooleanExtra("share_success", false)) {
			showSuccessDialog();
		}
	}

	public void showSuccessDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
				this);
		builder.setTitle("");
		builder.setMessage(getString(R.string.share_upload_complete));
		builder.setPositiveButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {
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
		builder.setCancelable(false);
		builder.create().show();
	}

	public void setupCursor() {
		final String[] columns = { MediaColumns.DATA, BaseColumns._ID };
		final String orderBy = BaseColumns._ID;
		String selector = null;
		if (PrintHelper.SHOW_PNG) {
			selector = ImageColumns.BUCKET_ID + "=" + PrintHelper.albumid
					+ " AND (" + ImageColumns.MIME_TYPE + " = "
					+ DatabaseUtils.sqlEscapeString("image/jpeg") + " OR "
					+ ImageColumns.MIME_TYPE + " = "
					+ DatabaseUtils.sqlEscapeString("image/jpg") + " OR "
					+ ImageColumns.MIME_TYPE + " = "
					+ DatabaseUtils.sqlEscapeString("image/png") + ")";
		} else {
			selector = ImageColumns.BUCKET_ID + "=" + PrintHelper.albumid
					+ " AND (" + ImageColumns.MIME_TYPE + " = "
					+ DatabaseUtils.sqlEscapeString("image/jpeg") + " OR "
					+ ImageColumns.MIME_TYPE + " = "
					+ DatabaseUtils.sqlEscapeString("image/jpg") + ")";
		}
		imagecursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				selector, null, orderBy);
		image_column_index = imagecursor.getColumnIndex(BaseColumns._ID);
		int data_column_index = imagecursor.getColumnIndex(MediaColumns.DATA);
		if (PrintHelper.mLoggingEnabled)
			Log.d(TAG, "Count=" + imagecursor.getCount());
		PrintHelper.count = imagecursor.getCount();
		PrintHelper.imageFilePaths = new ArrayList<String>();
		PrintHelper.uriEncodedPaths = new ArrayList<String>();
		// bits = new Bitmap[imagecursor.getCount()];
		uris = new String[imagecursor.getCount()];
		int i = 0;

		imagecursor.moveToFirst();
		while (i < imagecursor.getCount()) {
			PrintHelper.imageFilePaths.add(imagecursor
					.getString(data_column_index));
			int id = imagecursor.getInt(image_column_index);
			Uri uri = Uri.withAppendedPath(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					Integer.toString(id));
			PrintHelper.uriEncodedPaths.add(uri.toString());
			// The app crashed after add/remove picture and back to Thumbnail
			// view, so should add items into selectedFileNames.
			PrintHelper.selectedFileNames.put(uri.toString(),
					imagecursor.getString(data_column_index));
			i++;
			imagecursor.moveToNext();
		}
	}

	public void setupAdapter() {
		imageAdapter = new ImageAdapter(this);
		bigImageAdapter = new BigImageAdapter();
		setStateView();
	}

	public void setupEvents() {
		info.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent;
				myIntent = new Intent(ImageSelectionActivity.this,
						HelpActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(HelpActivity.HELP_LOCATION, SCREEN_NAME);
				myIntent.putExtras(bundle);
				startActivity(myIntent);
			}
		});
		// Only update the gallery when we stop flinging
		photosGallery.setCallbackDuringFling(false);
		photosGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parentview, View v,
					int position, long id) {
				selectionBar.setVisibility(android.view.View.VISIBLE);
				BigImageCheckBoxView icbv = (BigImageCheckBoxView) v;
				highlightPosition = position;
				highlightURI = icbv.uriEncodedPath;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				selectionBar.setVisibility(android.view.View.INVISIBLE);
			}
		});
		photosGallery.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.equals(MotionEvent.ACTION_UP))
					selectionBar.setVisibility(android.view.View.INVISIBLE);
				else
					selectionBar.setVisibility(android.view.View.VISIBLE);
				return false;
			}
		});
		photosGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mSharedPreferences.edit()
						.putBoolean(PrintHelper.HAS_ACCEPT_BLANK_PAGE, false)
						.commit();
				Photobook photobook = AppContext.getApplication().getPhotobook();
				photobook.hasAcceptBlankPage = false;
				final float scale = getBaseContext().getResources()
						.getDisplayMetrics().density;
				BigImageCheckBoxView icbv = (BigImageCheckBoxView) v;
				BitmapFactory.Options options = new Options();
				String uri = icbv.uriEncodedPath.toString();
				if (PrintHelper.selectedHash == null) {
					PrintHelper.selectedHash = new HashMap<String, String>();
				}
				if (!PrintHelper.selectedHash.containsKey(uri.toString())) {
					PrintHelper.selectedHash.put(uri.toString(), "0");
				}
				if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
					GreetingCardManager manager = GreetingCardManager
							.getGreetingCardManager(ImageSelectionActivity.this);
					if (PrintHelper.selectedHash.containsKey(uri)
							&& PrintHelper.selectedHash.get(uri).equals("0")) {
						if (!manager.getEditLayer().getImageURI().equals("")) {
							PrintHelper.selectedHash.put(manager.getEditLayer()
									.getImageURI(), "0");
						}
						manager.getEditLayer().setImageURI(new String(uri));
						PrintHelper.selectedHash.put(uri, "1");
						selectedCount++;
						if (manager.getAlbumMap4Click().containsKey(
								manager.getEditLayer().holeIndex
										+ PrintHelper.editedPageIndex)) {
							Log.i(TAG,
									"zzzzzz albummap contain the key holeindex = "
											+ (manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex));
							if (!manager
									.getAlbumMap4Click()
									.get(manager.getEditLayer().holeIndex
											+ PrintHelper.editedPageIndex)
									.equals(PrintHelper.albumid)
									&& manager
											.getAlbumMap4Click()
											.get(manager.getEditLayer().holeIndex
													+ PrintHelper.editedPageIndex) != null) {
								Log.i(TAG,
										"zzzzzz albumid from map is "
												+ manager
														.getAlbumMap4Click()
														.get(manager
																.getEditLayer().holeIndex
																+ PrintHelper.editedPageIndex)
												+ " , but "
												+ "PrintHelper.albumid = "
												+ PrintHelper.albumid);
								String formerCount = PrintHelper.albumSelected.get(""
										+ manager
												.getAlbumMap4Click()
												.get(manager.getEditLayer().holeIndex
														+ PrintHelper.editedPageIndex));
								if (formerCount == null
										|| formerCount.trim().equals("")) {
									formerCount = "1";
								}
								int former = Integer.parseInt(formerCount);
								former--;
								PrintHelper.albumSelected.put(
										""
												+ manager
														.getAlbumMap4Click()
														.get(manager
																.getEditLayer().holeIndex
																+ PrintHelper.editedPageIndex),
										"" + former);
								for (int i = 0; i < PrintHelper.mAlbumButton
										.size(); i++) {
									if (manager
											.getAlbumMap4Click()
											.get(manager.getEditLayer().holeIndex
													+ PrintHelper.editedPageIndex) == PrintHelper.mAlbumButton
											.get(i).getId()) {
										PrintHelper.mAlbumButton.get(i).selected = ""
												+ former;
									}
								}
								manager.getAlbumMap4Click().put(
										manager.getEditLayer().holeIndex
												+ PrintHelper.editedPageIndex,
										PrintHelper.albumid);
							} else {
								selectedCount--;
							}
						} else {
							Log.i(TAG,
									"zzzzzz albummap didn't contain the key holeindex = "
											+ (manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex)
											+ " , will put in with PrintHelper.albumid = "
											+ PrintHelper.albumid);
							manager.getAlbumMap4Click().put(
									manager.getEditLayer().holeIndex
											+ PrintHelper.editedPageIndex,
									PrintHelper.albumid);
						}
						Log.e(TAG,
								manager.getEditLayer().hashCode()
										+ " editLayer imageURI:"
										+ manager.getEditLayer().imageURI);
						Intent intent = new Intent(ImageSelectionActivity.this,
								GreetingCardProductActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					return;
				}
				Bitmap img = null;
				Bitmap rotated = null;
				Bitmap scaledImg = null;
				String fileName = Utils.getFilePath(icbv.uriEncodedPath,
						ImageSelectionActivity.this);

				img = PrintHelper.loadThumbnailImage(uri,
						MediaStore.Images.Thumbnails.MINI_KIND, options,
						ImageSelectionActivity.this);
				try {
					ExifInterface exif = new ExifInterface(fileName);
					if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
						Matrix matrix = new Matrix();
						matrix.postRotate(90);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);

					} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
						Matrix matrix = new Matrix();
						matrix.postRotate(270);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);
					} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180) {
						Matrix matrix = new Matrix();
						matrix.postRotate(180);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);
					}
					if (rotated != null) {
						img = null;
						img = rotated;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (PrintHelper.selectedHash.get(uri).equals("1")) {
					selectedCount--;
					PrintHelper.selectedHash.put(uri, "0");
					//PrintHelper.selectedImageUrls.remove(uri);
					EncryptUtil.removeLast(PrintHelper.selectedImageUrls, uri);
					//PrintHelper.selectedImages.remove(uri);
					//PrintHelper.uploadQueue.remove(uri);
					EncryptUtil.removeLast(PrintHelper.uploadQueue, uri);
					PrintHelper.uploadQueue
							.remove(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
									+ uri);
					EncryptUtil.removeLast(PrintHelper.uploadQueue, PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
							+ uri);
					PrintHelper.uploadedImageIDs
							.remove(PrintHelper.selectedFileNames.get(uri));
					if (PrintHelper.uploadShare2WmcQueue != null)
						PrintHelper.uploadShare2WmcQueue.remove(uri);
					icbv.setChecked(false);
					// Bitmap img = PrintHelper.loadThumbnailImage(uri,
					// MediaStore.Images.Thumbnails.MINI_KIND, options,
					// ImageSelectionActivity.this);

					if (img.getHeight() < (240 * scale)) {
						scaledImg = Bitmap.createScaledBitmap(img,
								(int) (img.getWidth() * (240 * scale) / img
										.getHeight()), (int) (img.getHeight()
										* (240 * scale) / img.getHeight()),
								true);
					}
					// Log.d(TAG, "original dimensions: " + img.getWidth() +
					// "x"
					// + img.getHeight());
					if (scaledImg == null) {
						if (img != null) {
							icbv.setImageBitmap(img);
						}
					} else {
						Log.d(TAG, "scaled dimensions: " + scaledImg.getWidth()
								+ "x" + scaledImg.getHeight());
						icbv.setImageBitmap(scaledImg);
					}
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
							int count = 0;
							while (count < PrintHelper.cartChildren.get(i)
									.size()) {
								CartItem tempItem = PrintHelper.cartChildren
										.get(i).get(count);
								if (tempItem.uri.equals(uri)) {
									PrintHelper.cartChildren.get(i).remove(
											count);
								} else {
									count++;
								}
							}
						}
					}
					
					 * CartItem itemExists = null; for (CartItem item :
					 * PrintHelper.items) { if (item.uri.equals(uri)) {
					 * itemExists = item; break; } } if(itemExists != null) {
					 * PrintHelper.items.remove(itemExists); }
					 
				} else {
					if (iMin != 0 && iMax != 0 && AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
							&& PrintHelper.selectedImageUrls.size() == iMax) {
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
								ImageSelectionActivity.this);
						builder.setTitle(getString(R.string.selectimages_max));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.create().show();
						return;
					}

					selectedCount++;
					PrintHelper.imageFileUriAlbumName.put(uri,
							PrintHelper.albumName);
					PrintHelper.selectedHash.put(uri, "1");
					//PrintHelper.selectedImages.put(uri, new SelectedImage(
					//		ImageSelectionActivity.this, uri));
					icbv.setChecked(true);

					if (img.getHeight() < (240 * scale)) {
						scaledImg = Bitmap.createScaledBitmap(img,
								(int) (img.getWidth() * (240 * scale) / img
										.getHeight()), (int) (img.getHeight()
										* (240 * scale) / img.getHeight()),
								true);
					}
					Bitmap bit = BitmapFactory.decodeResource(getResources(),
							R.drawable.selectedcheckbox);
					if (scaledImg == null) {
						if (img != null) {
							Bitmap test = overlay(img, bit);
							if (icbv.getChecked() && test != null) {
								icbv.setImageBitmap(test);
							} else {
								icbv.setImageBitmap(img);
							}
						}
					} else {
						Bitmap test = overlay(scaledImg, bit);
						if (icbv.getChecked() && test != null) {
							icbv.setImageBitmap(test);
						} else {
							icbv.setImageBitmap(img);
						}
					}

					String filename = PrintHelper.selectedFileNames.get(uri)
							.toString();
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						CartItem item = new CartItem(
								ImageSelectionActivity.this);
						item.uri = icbv.uriEncodedPath.toString();
						item.filename = filename;
						item.quantity = 1;
						item.roi = null;
						item.price = Double.parseDouble(PrintHelper.products
								.get(PrintHelper.defaultPrintSizeIndex)
								.getMinPrice());
						item.width = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getWidth();
						item.height = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getHeight();
						item.name = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex).getName();
						item.shortName = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex)
								.getShortName();
						item.productDescriptionId = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex)
								.getId();
						// PrintHelper.items.add(item);
						PrintHelper.cartChildren.get(
								PrintHelper.defaultPrintSizeIndex).add(item);
						if (!filename.equals("")) {
							PrintHelper.uploadQueue.add(uri);
							if (PrintHelper.uploadShare2WmcQueue != null)
								PrintHelper.uploadShare2WmcQueue.add(uri);
						} else {
							Log.e(TAG, "Error getting filename");
						}
						PrintHelper.selectedImageUrls.add(uri);
					}
					next.setEnabled(true);
					deselectAll.setEnabled(true);

					if (iMin != 0 && iMax != 0 && AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
							&& (PrintHelper.selectedImageUrls.size() >= iMax)) {
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
								ImageSelectionActivity.this);
						builder.setTitle(getString(R.string.selectimages_max));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.create().show();
					}
				}

				highlightPosition = position;
				highlightURI = uri;
				icbv.invalidate();
				int selectedCount = 0;
				for (String bool : PrintHelper.uriEncodedPaths) {
					String value = PrintHelper.selectedHash.get(bool.toString());
					if (value != null && value.equals("1"))
						selectedCount++;
				}
				headerBarText.setText(PrintHelper.albumName + " ( "
						+ selectedCount + " / " + PrintHelper.count + " )");
				totalSelectedTV.setText("" + selectedCount + " "
						+ getString(R.string.selected));
				sendConvertBroadcast();
			}
		});
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = null;
				if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
					myIntent = new Intent(ImageSelectionActivity.this,
							NewSettingActivity.class);
					Bundle b = new Bundle();
					b.putString(NewSettingActivity.SETTINGS_LOCATION, SCREEN_NAME);
					myIntent.putExtras(b);
					startActivity(myIntent);
				} else {
					myIntent = new Intent(ImageSelectionActivity.this,
							WiFiSettingsActivity.class);
					startActivity(myIntent);
				}
			}
		});
		viewModeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setStateView();
			}
		});
		startOver.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				 * AlertDialog.Builder dlg = new AlertDialog.Builder(
				 * ImageSelectionActivity.this); dlg.setTitle("Are you Sure?");
				 * dlg.setMessage("You will lose all changes to this order!");
				 * dlg.setPositiveButton("Start Over", new
				 * DialogInterface.OnClickListener() {
				 * 
				 * @Override public void onClick(DialogInterface dialog, int
				 * which) { mImageSelectionDatabase.handleDeleteAllUris();
				 
				if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
					Intent intent = new Intent(ImageSelectionActivity.this,
							AlbumSelectionActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();

				} else {
					dialog = ProgressDialog.show(ImageSelectionActivity.this,
							"", getString(R.string.savingtaggedimages), true,
							true);
					Thread setupCart = new Thread() {
						@Override
						public void run() {
							try {
								PrintHelper.selectedImageUrls.clear();
								//PrintHelper.selectedImages.clear();
								mImageSelectionDatabase
										.handleDeleteAllUrisWiFi();
								PrintHelper.wifiURIs.clear();
								for (Map.Entry<String, String> entry : PrintHelper.selectedHash
										.entrySet()) {
									if (entry.getValue().equals("1")) {
										String uri = entry.getKey().toString();
										String filename = "";
										if (PrintHelper.selectedFileNames == null) {
											Log.e(TAG,
													"ImageSelectionActivity: selectedFileNames is null");
										} else {
											try {
												filename = PrintHelper.selectedFileNames
														.get(entry.getKey())
														.toString();
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}
										// Add the selected image to the
										// database
										boolean added = mImageSelectionDatabase
												.handleAddUriWIFI(uri, filename);
										if (added)
											PrintHelper.wifiURIs.add(uri);
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							enteringForm.sendEmptyMessage(1);
						}
					};
					setupCart.start();
				}
			}
		});
		selectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread selectall = new Thread() {
					@Override
					public void run() {
						selectedCount = PrintHelper.count;
						selectionHandler.sendEmptyMessage(1);
					}
				};
				startOver.setEnabled(false);
				next.setEnabled(false);
				selectall.start();
				for (int i = 0; i < PrintHelper.uriEncodedPaths.size(); i++) {
					PrintHelper.imageFileUriAlbumName.put(
							PrintHelper.uriEncodedPaths.get(i),
							PrintHelper.albumName);
					PrintHelper.selectedHash.put(
							PrintHelper.uriEncodedPaths.get(i), "1");
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						String filename = "";
						if (PrintHelper.selectedFileNames == null) {
							Log.e(TAG,
									"ImageSelectionActivity: selectedFileNames is null");
						} else {
							try {
								filename = PrintHelper.selectedFileNames.get(
										PrintHelper.uriEncodedPaths.get(i))
										.toString();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (!filename.equals("")) {
							PrintHelper.uploadQueue
									.add(PrintHelper.uriEncodedPaths.get(i));
							if (PrintHelper.uploadShare2WmcQueue != null)
								PrintHelper.uploadShare2WmcQueue
										.add(PrintHelper.uriEncodedPaths.get(i));
						} else {
							Log.e(TAG, "Error getting filename");
						}
					}
				}
				selectAll.setEnabled(false);
				deselectAll.setEnabled(true);
				next.setEnabled(true);
				if (prefs.getBoolean("analytics", false)) {
					try {
						PrintHelper.mTracker.trackEvent("Thumbnails",
								"Select_All", "", 0);
						PrintHelper.mTracker.dispatch();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		deselectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread deselectall = new Thread() {
					@Override
					public void run() {
						selectedCount = 0;
						selectionHandler.sendEmptyMessage(2);
					}
				};
				startOver.setEnabled(false);
				next.setEnabled(false);
				deselectall.start();
				for (int i = 0; i < PrintHelper.uriEncodedPaths.size(); i++) {
					PrintHelper.selectedHash.put(
							PrintHelper.uriEncodedPaths.get(i), "0");
					PrintHelper.uploadQueue.remove(PrintHelper.uriEncodedPaths
							.get(i));
					EncryptUtil.removeLast(PrintHelper.uploadQueue, PrintHelper.uriEncodedPaths
							.get(i));
					PrintHelper.uploadQueue
							.remove(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
									+ PrintHelper.uriEncodedPaths.get(i));
					EncryptUtil.removeLast(PrintHelper.uploadQueue,PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
							+ PrintHelper.uriEncodedPaths.get(i));
					PrintHelper.uploadedImageIDs
							.remove(PrintHelper.selectedFileNames
									.get(PrintHelper.uriEncodedPaths.get(i)));
					if (PrintHelper.uploadShare2WmcQueue != null)
						PrintHelper.uploadShare2WmcQueue
								.remove(PrintHelper.uriEncodedPaths.get(i));
				}
				if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
					for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
						int count = 0;
						while (count < PrintHelper.cartChildren.get(i).size()) {
							CartItem tempItem = PrintHelper.cartChildren.get(i)
									.get(count);
							if (tempItem.uri.equals(PrintHelper.uriEncodedPaths
									.get(i))) {
								PrintHelper.cartChildren.get(i).remove(count);
							} else {
								count++;
							}
						}
					}
				}
				selectAll.setEnabled(true);
				deselectAll.setEnabled(false);
				next.setEnabled(false);
				if (prefs.getBoolean("analytics", false)) {
					try {
						PrintHelper.mTracker.trackEvent("Thumbnails",
								"Deselect_All", "", 0);
						PrintHelper.mTracker.dispatch();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				next.setEnabled(false);
				if (!Connection.isConnected(ImageSelectionActivity.this)
						&& !AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
					next.setEnabled(true);
					if (connectBuilder != null) {
						return;
					}
					connectBuilder = new InfoDialog.InfoDialogBuilder(
							ImageSelectionActivity.this);
					connectBuilder.setTitle("");
					connectBuilder
							.setMessage(getString(R.string.nointernetconnection));
					connectBuilder.setPositiveButton(getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									connectBuilder = null;
								}
							});
					connectBuilder.setNegativeButton(
							getString(R.string.share_upload_retry),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									connectBuilder = null;
									next.performClick();
								}
							});
					connectBuilder.setCancelable(false);
					connectBuilder.create().show();

					return;
				}

				boolean cantNavigate = true;
				try {
					for (Map.Entry<String, String> entry : PrintHelper.selectedHash
							.entrySet()) {
						if (entry.getValue().equals("1")) {
							cantNavigate = false;
							break;
						}
					}
				} catch (Exception ex) {
					if (PrintHelper.mLoggingEnabled) {
						Log.e(TAG, "Error setting total selected");
						ex.printStackTrace();
					}
				}
				SharedPreferences mSharedPreferences = PreferenceManager
						.getDefaultSharedPreferences(ImageSelectionActivity.this);
				//int iMin = mSharedPreferences.getInt(
						//PrintHelper.sMinNumberOfImages, 0);// -
															// (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID,
															// "").equals("") ?
															// 0 : 1);
				//int iMax = mSharedPreferences.getInt(
						//PrintHelper.sMaxNumberOfImages, 0);// -
															// (mSharedPreferences.getString(PrintHelper.sPhotoBookTitleID,
															// "").equals("") ?
															// 0 : 1);
				Photobook photobook = AppContext.getApplication().getPhotobook();
				int iMin = photobook==null ? 0 : photobook.minNumberOfImages;
				int iMax = photobook==null ? 0 : photobook.maxNumberOfImages;
				// String sSelectRange = iMin + "-" + iMax;
				PrintProduct photoBookProduct = null;
				if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
					for (PrintProduct product : PrintHelper.products) {
						if (product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage)&& product.getId().equals(photobook.proDescId)) {
							photoBookProduct = product;
							break;
						}
					}
				}
				if (iMin != 0
						&& iMax != 0
						&& AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
						&& (PrintHelper.selectedImageUrls.size() < iMin || PrintHelper.selectedImageUrls
								.size() > iMax))
					cantNavigate = true;
				if (cantNavigate) {
					next.setEnabled(true);
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
							ImageSelectionActivity.this);
					if (iMin != 0
							&& iMax != 0
							&& AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
							&& (PrintHelper.selectedImageUrls.size() < iMin || PrintHelper.selectedImageUrls
									.size() > iMax)) {
						// builder.setTitle(getString(R.string.selected_images_range).replace("%%",
						// sSelectRange));
						builder.setTitle(String.format(
								getString(R.string.selected_images_range),
								iMin, iMax, photoBookProduct.getName()));
					} else
						builder.setTitle(getString(R.string.selectatleastoneimage));
					builder.setMessage("");
					builder.setPositiveButton(getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.setNegativeButton("",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									finish();
								}
							});
					builder.create().show();
				} else {
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						boolean isDuplex = photobook==null ? false : photobook.isDuplex;
						if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
								&& isDuplex
								&& !photobook.hasAcceptBlankPage) {
							int totalNum = PrintHelper.selectedImageUrls.size();
							if (totalNum % 2 != 0) {
								next.setEnabled(true);
								showBlankPageWarning();
								return;
							}
						}
						if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
							PictureUploadService2.isDoneSelectPics = true;
							// Intent myIntent = new
							// Intent(ImageSelectionActivity.this,
							// QuickbookCoverViewActivity.class);
							Intent myIntent = new Intent(
									ImageSelectionActivity.this,
									QuickBookFlipperActivity.class);
							startActivity(myIntent);
							finish();
						} else {
							Bundle bundle = new Bundle();
							bundle.putBoolean(FROM_IMAGESELECTION, true);
							Intent myIntent = new Intent(
									ImageSelectionActivity.this,
									ShoppingCartActivity.class);
							myIntent.putExtras(bundle);
							finish();
							startActivity(myIntent);
						}
					} else {
						dialog = ProgressDialog.show(
								ImageSelectionActivity.this, "",
								getString(R.string.savingtaggedimages), true,
								true);
						Thread setupCart = new Thread() {
							@Override
							public void run() {
								try {
									PrintHelper.selectedImageUrls.clear();
									//PrintHelper.selectedImages.clear();
									mImageSelectionDatabase
											.handleDeleteAllUrisWiFi();
									PrintHelper.wifiURIs.clear();
									for (Map.Entry<String, String> entry : PrintHelper.selectedHash
											.entrySet()) {
										if (entry.getValue().equals("1")) {
											String uri = entry.getKey()
													.toString();
											String filename = "";
											if (PrintHelper.selectedFileNames == null) {
												Log.e(TAG,
														"ImageSelectionActivity: selectedFileNames is null");
											} else {
												try {
													filename = PrintHelper.selectedFileNames
															.get(entry.getKey())
															.toString();
												} catch (Exception ex) {
													ex.printStackTrace();
												}
											}
											// Add the selected image to the
											// database
											boolean added = mImageSelectionDatabase
													.handleAddUriWIFI(uri,
															filename);
											if (added)
												PrintHelper.wifiURIs.add(uri);
										}
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								enteringForm.sendEmptyMessage(0);
							}
						};
						setupCart.start();
					}
				}
			}
		});

		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Share
				if (PrintHelper.uploadShare2WmcQueue.size() == 0) {
					return;
				}
				ShareListView shareList = (ShareListView) ImageSelectionActivity.this
						.getLayoutInflater().inflate(R.layout.sharelistview,
								null);

				int vHeight = getWindowManager().getDefaultDisplay()
						.getHeight();
				int vWidth = getWindowManager().getDefaultDisplay().getWidth();

				int smWidth = 400;
				int smHeight = shareList.getAdapter().getCount() * 75;
				if (smHeight > vHeight) {
					smHeight = vHeight;
				}

				shareMenu = new PopupWindow(shareList, smWidth, smHeight);
				shareMenu.setOutsideTouchable(true);
				shareMenu.setFocusable(true);
				shareMenu.setBackgroundDrawable(new BitmapDrawable());
				shareList.setPopupWindow(shareMenu);
				int w = shareButton.getLeft() - (vWidth / 2 + 200);
				int h = (vHeight - smHeight) / 2 - 10;
				if (h < 0) {
					h = 0;
				}
				shareMenu.showAtLocation((View) shareButton.getParent()
						.getParent(), Gravity.CENTER_VERTICAL, w, h);
			}

		});

		photos.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mSharedPreferences.edit()
						.putBoolean(PrintHelper.HAS_ACCEPT_BLANK_PAGE, false)
						.commit();
				Photobook photobook = AppContext.getApplication().getPhotobook();
				photobook.hasAcceptBlankPage = false;
				long start = SystemClock.elapsedRealtime();
				int id1 = view.getId();
				ImageCheckBoxView icbv = (ImageCheckBoxView) view;
				String uriEncodedPath = icbv.uriEncodedPath;

				if (PrintHelper.selectedHash == null) {
					PrintHelper.selectedHash = new HashMap<String, String>();
				}
				// Make sure this image key is in the selectedHash list
				if (!PrintHelper.selectedHash.containsKey(uriEncodedPath
						.toString())) {
					PrintHelper.selectedHash.put(uriEncodedPath.toString(), "0");
				}

				// Greeting Card part start
				if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
					GreetingCardManager manager = GreetingCardManager
							.getGreetingCardManager(ImageSelectionActivity.this);
					if (PrintHelper.selectedHash.containsKey(uriEncodedPath)
							&& PrintHelper.selectedHash.get(uriEncodedPath)
									.equals("0")) {
						if (!manager.getEditLayer().getImageURI().equals("")) {
							PrintHelper.selectedHash.put(manager.getEditLayer()
									.getImageURI(), "0");
						}
						manager.getEditLayer().setImageURI(
								new String(uriEncodedPath));
						PrintHelper.selectedHash.put(uriEncodedPath, "1");
						selectedCount++;
						if (manager.getAlbumMap4Click().containsKey(
								manager.getEditLayer().holeIndex
										+ PrintHelper.editedPageIndex)) {
							Log.i(TAG,
									"zzzzzz albummap contain the key holeindex = "
											+ (manager.getEditLayer().holeIndex + PrintHelper.editedPageIndex));
							if (!manager
									.getAlbumMap4Click()
									.get(manager.getEditLayer().holeIndex
											+ PrintHelper.editedPageIndex)
									.equals(PrintHelper.albumid)
									&& manager
											.getAlbumMap4Click()
											.get(manager.getEditLayer().holeIndex
													+ PrintHelper.editedPageIndex) != null) {
								if (!PrintHelper.albumSelected.isEmpty()) {
									String formerCount = PrintHelper.albumSelected
											.get(""
													+ manager
															.getAlbumMap4Click()
															.get(manager
																	.getEditLayer().holeIndex
																	+ PrintHelper.editedPageIndex));
									Log.i(TAG, "698 formerCount = "
											+ formerCount);
									if (formerCount == null
											|| formerCount.trim().equals("")) {
										formerCount = "1";
									}
									int former = Integer.parseInt(formerCount);
									former--;
									PrintHelper.albumSelected
											.put(""
													+ manager
															.getAlbumMap4Click()
															.get(manager
																	.getEditLayer().holeIndex
																	+ PrintHelper.editedPageIndex),
													"" + former);
									for (int i = 0; i < PrintHelper.mAlbumButton
											.size(); i++) {
										if (manager
												.getAlbumMap4Click()
												.get(manager.getEditLayer().holeIndex
														+ PrintHelper.editedPageIndex) == PrintHelper.mAlbumButton
												.get(i).getId()) {
											PrintHelper.mAlbumButton.get(i).selected = ""
													+ former;
										}
									}
								}
								manager.getAlbumMap4Click().put(
										manager.getEditLayer().holeIndex
												+ PrintHelper.editedPageIndex,
										PrintHelper.albumid);
							} else {
								selectedCount--;
							}
						} else {
							manager.getAlbumMap4Click().put(
									manager.getEditLayer().holeIndex
											+ PrintHelper.editedPageIndex,
									PrintHelper.albumid);
						}
						Log.e(TAG,
								manager.getEditLayer().hashCode()
										+ " editLayer imageURI:"
										+ manager.getEditLayer().imageURI);
						Intent intent = new Intent(ImageSelectionActivity.this,
								GreetingCardProductActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					return;
				}
				// Greeting Card part end

				if (PrintHelper.selectedHash.get(uriEncodedPath.toString())
						.equals("1")) {
					selectedCount--;
					PrintHelper.selectedHash.put(uriEncodedPath.toString(), "0");
					PrintHelper.selectedImageUrls.remove(uriEncodedPath);
					EncryptUtil.removeLast(PrintHelper.selectedImageUrls, uriEncodedPath);
					PrintHelper.uploadQueue.remove(uriEncodedPath.toString());
					EncryptUtil.removeLast(PrintHelper.uploadQueue, uriEncodedPath.toString());
//					PrintHelper.uploadQueue
//							.remove(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
//									+ uriEncodedPath.toString());
					EncryptUtil.removeLast(PrintHelper.uploadQueue, PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
							+ uriEncodedPath.toString());
					PrintHelper.uploadedImageIDs
							.remove(PrintHelper.selectedFileNames
									.get(uriEncodedPath.toString()));
					if (PrintHelper.uploadShare2WmcQueue != null)
						PrintHelper.uploadShare2WmcQueue.remove(uriEncodedPath
								.toString());
					//PrintHelper.selectedImages.remove(uriEncodedPath);
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
							int count = 0;
							while (count < PrintHelper.cartChildren.get(i)
									.size()) {
								CartItem tempItem = PrintHelper.cartChildren
										.get(i).get(count);
								if (tempItem.uri.equals(uriEncodedPath)) {
									PrintHelper.cartChildren.get(i).remove(
											count);
								} else {
									count++;
								}
							}
						}
					}
					selectAll.setEnabled(true);
					icbv.setChecked(false);
				} else {
					if (iMin != 0 && iMax != 0 && AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
							&& PrintHelper.selectedImageUrls.size() == iMax) {
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
								ImageSelectionActivity.this);
						builder.setTitle(getString(R.string.selectimages_max));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.create().show();
						return;
					}
					selectedCount++;
					PrintHelper.imageFileUriAlbumName.put(uriEncodedPath,
							PrintHelper.albumName);
					PrintHelper.selectedHash.put(uriEncodedPath, "1");
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						String filename = "";
						if (PrintHelper.selectedFileNames == null) {
							Log.e(TAG,
									"ImageSelectionActivity: selectedFileNames is null");
						} else {
							try {
								filename = PrintHelper.selectedFileNames.get(
										uriEncodedPath).toString();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						if (!filename.equals("")) {
							PrintHelper.uploadQueue.add(uriEncodedPath);
							if (PrintHelper.uploadShare2WmcQueue != null)
								PrintHelper.uploadShare2WmcQueue
										.add(uriEncodedPath);
						} else {
							Log.e(TAG, "Error getting filename");
						}
					}
					icbv.setChecked(true);
					next.setEnabled(true);
					deselectAll.setEnabled(true);
					String filename = PrintHelper.selectedFileNames.get(
							uriEncodedPath).toString();
					PrintHelper.selectedImageUrls.add(uriEncodedPath);
					//PrintHelper.selectedImages.put(uriEncodedPath,
					//		new SelectedImage(ImageSelectionActivity.this,
					//				uriEncodedPath));

					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						CartItem item = new CartItem(
								ImageSelectionActivity.this);
						if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()){
							item.productType = AppConstants.BOOK_TYPE;
						}else if (AppContext.getApplication().getFlowType().isPrintWorkFlow()){
							item.productType = AppConstants.PRINT_TYPE;
						}
						item.uri = uriEncodedPath;
						item.filename = filename;
						item.quantity = 1;
						item.roi = null;
						item.price = Double.parseDouble(PrintHelper.products
								.get(PrintHelper.defaultPrintSizeIndex)
								.getMinPrice());
						item.width = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getWidth();
						item.height = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getHeight();
						item.name = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex).getName();
						item.shortName = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex)
								.getShortName();
						item.productDescriptionId = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex).getId();
						// PrintHelper.items.add(item);
						if (PrintHelper.cartChildren
								.get(PrintHelper.defaultPrintSizeIndex) == null) {
							PrintHelper.cartChildren.add(
									PrintHelper.defaultPrintSizeIndex,
									new ArrayList<CartItem>());
						}
						PrintHelper.cartChildren.get(
								PrintHelper.defaultPrintSizeIndex).add(item);
					}

					if (iMin != 0 && iMax != 0 && AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
							&& (PrintHelper.selectedImageUrls.size() >= iMax)) {
						InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(
								ImageSelectionActivity.this);
						builder.setTitle(getString(R.string.selectimages_max));
						builder.setMessage("");
						builder.setPositiveButton(getString(R.string.OK),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.create().show();
					}

				}
				if (lastView != null) {
					lastView.showHighlight = false;
				}
				icbv.showHighlight = true;
				lastView = icbv;
				highlightPosition = id1;
				highlightURI = icbv.uriEncodedPath;
				headerBarText.setText(PrintHelper.albumName + " ( "
						+ selectedCount + " / " + PrintHelper.count + " )");
				imageAdapter.notifyDataSetChanged();
				long finish = SystemClock.elapsedRealtime();
				Log.i(TAG, "Time to touch image = " + (finish - start));
				start = SystemClock.elapsedRealtime();
				finish = SystemClock.elapsedRealtime();
				Log.i(TAG, "Time to create item = " + (finish - start));
				sendConvertBroadcast();
			}
		});
	}

	private void showBlankPageWarning() {
		if (connectBuilder == null) {
			connectBuilder = new InfoDialog.InfoDialogBuilder(
					ImageSelectionActivity.this);
		}
		connectBuilder.setTitle("");
		connectBuilder.setMessage(getString(R.string.qb_page_blank_warning));
		connectBuilder.setPositiveButton(getString(R.string.OK),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						connectBuilder = null;
						Photobook photobook = AppContext.getApplication().getPhotobook();
						photobook.hasAcceptBlankPage = true;
					}
				});
		connectBuilder.create().show();
	}

	@Override
	protected void onDestroy() {
		 unregisterReceiver(broadcastReceiver); 
		photosGallery.setAdapter(null);
		photos.setAdapter(null);
		imageAdapter = null;
		bigImageAdapter = null;
		photosGallery = null;
		photos = null;
		super.onDestroy();
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
	}

	private Handler enteringForm = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				next.setEnabled(true);
				dialog.dismiss();
				int viewTouched = msg.what;
				switch (viewTouched) {
				case 0: {
					Bundle bundle = new Bundle();
					bundle.putBoolean(FROM_IMAGESELECTION, true);
					Intent myIntent = new Intent(ImageSelectionActivity.this,
							ManageTaggedImagesActivity.class);
					myIntent.putExtras(bundle);
					startActivity(myIntent);
					finish();
					break;
				}
				case 1: {
					Intent myIntent = new Intent(ImageSelectionActivity.this,
							AlbumSelectionActivity.class);
					myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(myIntent);
					finish();
					break;
				}
				default:
					break;
				}
				photos.setAdapter(null);
			} catch (Exception ex) {
			}
			try {
				dialog.dismiss();
			} catch (Exception ex) {
			}
		}
	};

	public void setStateView() {
		if (photos.getVisibility() == android.view.View.GONE) {
			if (prefs.getBoolean("analytics", false)) {
				try {
					PrintHelper.mTracker.trackPageView("ThumbnailScreen");
					PrintHelper.mTracker.dispatch();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			photosGallery.setVisibility(android.view.View.GONE);
			photos.setVisibility(android.view.View.VISIBLE);

			// only for dmcombinedapp
			PACKAGE_NAME = getApplicationContext().getPackageName();
			boolean isDMC = PACKAGE_NAME
					.contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
			boolean isKM = PACKAGE_NAME
					.contains(MainMenu.KODAK_COMBINED_PACKAGE_NAME);
			Log.d(TAG, "onCreateView - package name: " + PACKAGE_NAME);
			if ((isKM || isDMC) && AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
				selectAll.setVisibility(View.INVISIBLE);
				deselectAll.setVisibility(View.INVISIBLE);
			} else {
				selectAll.setVisibility(android.view.View.VISIBLE);
				deselectAll.setVisibility(android.view.View.VISIBLE);
			}

			photos.setAdapter(imageAdapter);
			photos.setSelection(highlightPosition);
			imageAdapter.notifyDataSetChanged();
			Bitmap bit = BitmapFactory.decodeResource(getResources(),
					R.drawable.toolscoverflowup);
			Drawable d = new BitmapDrawable(bit);
			viewModeButton.setBackgroundDrawable(d);
			selectAll.setEnabled(true);
			deselectAll.setEnabled(true);
		} else {
			if (prefs.getBoolean("analytics", false)) {
				try {
					PrintHelper.mTracker.trackPageView("CoverFlowScreen");
					PrintHelper.mTracker.dispatch();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			photosGallery.setVisibility(android.view.View.VISIBLE);
			photos.setVisibility(android.view.View.GONE);
			selectAll.setVisibility(android.view.View.GONE);
			deselectAll.setVisibility(android.view.View.GONE);
			photosGallery.setAdapter(new BigImageAdapter());
			photosGallery.setSelection(highlightPosition, true);
			Bitmap bit = BitmapFactory.decodeResource(getResources(),
					R.drawable.toolsthumbnailup);
			Drawable d = new BitmapDrawable(bit);
			viewModeButton.setBackgroundDrawable(d);
			viewModeButton.setPadding(0, 0, 0, 0);
			selectAll.setEnabled(false);
			deselectAll.setEnabled(false);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
				dialog = ProgressDialog.show(ImageSelectionActivity.this, "",
						getString(R.string.savingtaggedimages), true, true);
				Thread setupCart = new Thread() {
					@Override
					public void run() {
						try {
							PrintHelper.selectedImageUrls.clear();
							//PrintHelper.selectedImages.clear();
							mImageSelectionDatabase.handleDeleteAllUrisWiFi();
							PrintHelper.wifiURIs.clear();
							for (Map.Entry<String, String> entry : PrintHelper.selectedHash
									.entrySet()) {
								if (entry.getValue().equals("1")) {
									String uri = entry.getKey().toString();
									String filename = "";
									if (PrintHelper.selectedFileNames == null) {
										Log.e(TAG,
												"ImageSelectionActivity: selectedFileNames is null");
									} else {
										try {
											filename = PrintHelper.selectedFileNames
													.get(entry.getKey())
													.toString();
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}
									// Add the selected image to the
									// database
									boolean added = mImageSelectionDatabase
											.handleAddUriWIFI(uri, filename);
									if (added)
										PrintHelper.wifiURIs.add(uri);
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						enteringForm.sendEmptyMessage(1);
					}
				};
				setupCart.start();
			} else {
				if (shareMenu != null && shareMenu.isShowing()) {
					shareMenu.dismiss();
					return true;
				}
				Intent intent = new Intent(ImageSelectionActivity.this,
						AlbumSelectionActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
		return true;
	};

	@Override
	public void onResume() {
		Localytics.onActivityResume(this);
		PictureUploadService2.isDoneSelectPics = false;
		PictureUploadService2.isDoneUploadThumbnails = false;
		try {
			if (prefs.getBoolean("analytics", false)) {
				if (AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
					PrintHelper.mTracker.setCustomVar(2, "Workflow",
							"Wifi_At_Kiosk", 3);
				} else {
					PrintHelper.mTracker.setCustomVar(2, "Workflow",
							"Prints_To_Store", 3);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		draw = PrintHelper.readBitMap(this, R.drawable.image_wait_6x8);
		// draw = BitmapFactory.decodeResource(getResources(),
		// R.drawable.imagewait96x96);

		Log.i("quickbook", "inQuickbook:" + AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()
				+ ", hasQuickbook: " + PrintHelper.hasQuickbook);
		if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow() && PrintHelper.hasQuickbook) {
			next.setVisibility(View.VISIBLE);
			// because of spec changed, change the text back to next
			next.setText(getString(R.string.next));
			startOver.setVisibility(android.view.View.VISIBLE);
			startOver.setText(getString(R.string.Back));
		} else if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow() && !PrintHelper.hasQuickbook) {
			next.setVisibility(View.VISIBLE);
			next.setText(getString(R.string.next));
			startOver.setVisibility(android.view.View.VISIBLE);
			startOver.setText(getString(R.string.Back));
		} else if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			next.setText(getString(R.string.cart));
			settings.setVisibility(android.view.View.VISIBLE);
		} else {
			next.setText(getString(R.string.selected_set));
			if (PrintHelper.infoEnabled) {
				if (Connection.isConnectedWifi(ImageSelectionActivity.this)) {
					info.setVisibility(android.view.View.INVISIBLE);
				} else {
					info.setVisibility(android.view.View.VISIBLE);
				}
			} else {
				info.setVisibility(View.INVISIBLE);
			}
		}
		
		 * if (!Connection.isConnected(ImageSelectionActivity.this) &&
		 * !AppContext.getApplication().getFlowType().isWifiWorkFlow()) { InfoDialog.InfoDialogBuilder builder = new
		 * InfoDialog.InfoDialogBuilder(ImageSelectionActivity.this);
		 * builder.setTitle("");
		 * builder.setMessage(getString(R.string.nointernetconnection));
		 * builder.setPositiveButton(getString(R.string.OK), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); } }); builder.setNegativeButton("", new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); } }); builder.setCancelable(false);
		 * builder.create().show(); }
		 
		onResumeCheckConnection();
		if (mImageSelectionDatabase == null)
			mImageSelectionDatabase = new ImageSelectionDatabase(this);
		mImageSelectionDatabase.open();
		selectedCount = 0;
		for (int i = 0; i < PrintHelper.uriEncodedPaths.size(); i++) {
			try {
				if (PrintHelper.selectedHash.get(PrintHelper.uriEncodedPaths
						.get(i).toString()) == "1") {
					selectedCount++;
				}
			} catch (Exception ex) {
				if (PrintHelper.mLoggingEnabled)
					Log.e(TAG, "Error incrementing selected count" + "");
				ex.printStackTrace();
			}
		}
		headerBarText.setText(PrintHelper.albumName + " ( " + selectedCount
				+ " / " + PrintHelper.count + " )");
		totalSelectedTV.setVisibility(android.view.View.GONE);
		totalSelectedTV.setText("" + selectedCount + " "
				+ getString(R.string.selected));
		 if (mImageSelectionDatabase.getSelectedCount() > 0) 
		next.setEnabled(true);
		Boolean reachable = imagecursor.moveToPosition(0);
		if (reachable) {
			
			 * try { int id = imagecursor.getInt(image_column_index); Uri uri =
			 * Uri
			 * .withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			 * Integer.toString(id)); highlightURI = uri.toString();
			 * highlightPosition = 0; } catch (Exception ex) { Log.e("TAG",
			 * ex.getMessage()); }
			 
		}
		
		 * for (int i = 0; i < PrintHelper.imagesSelected.length; i++)
		 * PrintHelper.imagesSelected[i] = mImageSelectionDatabase
		 * .isUriSelected(PrintHelper.uriEncodedPaths[i]);
		 

		// below: if back to this activity, will scroll to the last selected
		// image directly
		
		 * if(photos!=null){ int position = 0;
		 * if(PrintHelper.selectedImageUrls!=null &&
		 * PrintHelper.selectedImageUrls.size()>0 && PrintHelper.uriEncodedPaths
		 * !=null){ String lastSelectedUri =
		 * PrintHelper.selectedImageUrls.get(PrintHelper
		 * .selectedImageUrls.size()-1); for(int j=0;
		 * j<PrintHelper.uriEncodedPaths.size(); j++){
		 * if(PrintHelper.uriEncodedPaths.get(j).equals(lastSelectedUri)){
		 * position = j; break; } } photos.setSelection(position); } }
		 * if(photosGallery!=null){ int position = 0;
		 * if(PrintHelper.selectedImageUrls!=null &&
		 * PrintHelper.selectedImageUrls.size()>0 && PrintHelper.uriEncodedPaths
		 * !=null){ String lastSelectedUri =
		 * PrintHelper.selectedImageUrls.get(PrintHelper
		 * .selectedImageUrls.size()-1); for(int j=0;
		 * j<PrintHelper.uriEncodedPaths.size(); j++){
		 * if(PrintHelper.uriEncodedPaths.get(j).equals(lastSelectedUri)){
		 * position = j; break; } } photosGallery.setSelection(position); } }
		 
		if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
			next.setVisibility(View.INVISIBLE);
			selectAll.setVisibility(View.INVISIBLE);
			deselectAll.setVisibility(View.INVISIBLE);
			// viewModeButton.setVisibility(View.INVISIBLE);
			headerBarText
					.setText(getString(R.string.image_selection_title_for_card));
		}
		photos.setSelection(imageAdapter.getCount() - 1);
		super.onResume();
	}

	// added by Robin.Qian
	// The code below make the gridView scroll to the last row
	boolean isFirst = true;

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (isFirst) {
			isFirst = false;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					photos.setSelection(imageAdapter.getCount() - 1);
				}
			}, 100);
		}
	}

	private void onResumeCheckConnection() {
		if (!Connection.isConnected(ImageSelectionActivity.this)
				&& !AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
			if (connectBuilder != null) {
				return;
			}
			connectBuilder = new InfoDialog.InfoDialogBuilder(
					ImageSelectionActivity.this);
			connectBuilder.setTitle("");
			connectBuilder.setMessage(getString(R.string.nointernetconnection));
			connectBuilder.setPositiveButton(getString(R.string.OK),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							connectBuilder = null;
						}
					});
			connectBuilder.setNegativeButton(
					getString(R.string.share_upload_retry),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							connectBuilder = null;
							onResumeCheckConnection();
						}
					});
			connectBuilder.setCancelable(false);
			connectBuilder.create().show();
		}
	}

	@Override
	public void onPause() {
		Localytics.onActivityPause(this);
		Log.i(TAG, "imageimage the adapter is remove on pause");
		mImageSelectionDatabase.close();
		Log.i(TAG, "countof selected PrintHelper.albumid = "
				+ PrintHelper.albumid + " , and selectedCount = "
				+ selectedCount);
		try {
			if (PrintHelper.albumSelected == null) {
				PrintHelper.albumSelected = new HashMap<String, String>();
			}
			PrintHelper.albumSelected.put("" + PrintHelper.albumid, ""
					+ selectedCount);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			for (int i = 0; i < PrintHelper.mAlbumButton.size(); i++) {
				if (PrintHelper.albumid == PrintHelper.mAlbumButton.get(i)
						.getId()) {
					PrintHelper.mAlbumButton.get(i).selected = ""
							+ selectedCount;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);
		ImageCache mCache = ImageCache.currentCache(this, cacheParams);
		if (mCache != null) {
			mCache.clearCaches();
		}
		super.onPause();
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle) {
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		{
		}
	}

	public class ImageAdapter extends BaseAdapter {
		Context mContext;
		protected boolean isScrolling;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context) {
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		*//**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 *//*
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			if (mImageWorker != null) {
				mImageWorker.setImageSize(height);
				notifyDataSetChanged();
			}
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}

		@Override
		public int getCount() {
			return PrintHelper.count;
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageCheckBoxView icbv = null;
			if (convertView == null) {
				icbv = new ImageCheckBoxView(ImageSelectionActivity.this);
				convertView = icbv;
				convertView.setTag(icbv);
			} else {
				icbv = (ImageCheckBoxView) convertView.getTag();
			}
			icbv.setId(position);

			if (!isScrolling && mImageWorker != null) {
				ImageAdapter tempAdapter = ((ImageAdapter) photos.getAdapter());
				mImageWorker.loadImage(position, icbv);
				tempAdapter.notifyDataSetChanged();
			} else {
				icbv.setImageBitmap(draw);
				// edit.setEnabled(false);
				// delete.setEnabled(false);
			}
			Boolean reachable = imagecursor.moveToPosition(position);
			if (reachable) {
				try {
					int id = imagecursor.getInt(image_column_index);
					Uri uri = Uri.withAppendedPath(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							Integer.toString(id));
					icbv.uriEncodedPath = uri.toString();
					if (!icbv.uriEncodedPath.equals(highlightURI)
							&& position != highlightPosition) {
						icbv.showHighlight = false;
					} else {
						icbv.showHighlight = true;
					}
					uris[position] = uri.toString();
					icbv.setId(position);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			} else {
				if (PrintHelper.mLoggingEnabled)
					Log.e(TAG, "imagecursor.moveToPosition for position "
							+ position + " failed");
			}
			
			 * } else { icbv.setImageBitmap(bits[position]); icbv.uriEncodedPath
			 * = uris[position]; }
			 
			try {
				// Make sure we are updating the checked state independent from
				// where we load the image from
				if (PrintHelper.selectedHash != null) {
					if (icbv.uriEncodedPath != null) {
						if (PrintHelper.selectedHash
								.containsKey(icbv.uriEncodedPath.toString())) {
							// if (PrintHelper.mLoggingEnabled)
							// Log.d(TAG, "Selected hash contains: " +
							// icbv.uriEncodedPath.toString());
							if (PrintHelper.selectedHash.get(
									icbv.uriEncodedPath.toString()).equals("1")) {
								// if (PrintHelper.mLoggingEnabled)
								// Log.d(TAG, "Selected hash = " +
								// PrintHelper.selectedHash.get(icbv.uriEncodedPath.toString()).toString()
								// + " set checked: " +
								// icbv.uriEncodedPath.toString());
								icbv.setChecked(true);
							} else {
								// if (PrintHelper.mLoggingEnabled)
								// Log.d(TAG, "Selected hash = " +
								// PrintHelper.selectedHash.get(icbv.uriEncodedPath.toString()).toString()
								// + " set unchecked: " +
								// icbv.uriEncodedPath.toString());
								icbv.setChecked(false);
							}
						} else {
							icbv.setChecked(false);
							if (PrintHelper.mLoggingEnabled)
								Log.e(TAG,
										"Selected Hash doesn't contain key: "
												+ icbv.uriEncodedPath
														.toString());
						}
					} else {
						if (PrintHelper.mLoggingEnabled)
							Log.d(TAG, "uri is null!");
					}
				} else {
					if (PrintHelper.mLoggingEnabled)
						Log.d(TAG, "PrintHelper.selecteHash is null!");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e(TAG, "Error updating checkbox");
			}
			icbv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			icbv.setLayoutParams(mImageViewLayoutParams);
			icbv.invalidate();
			convertView.setBackgroundColor(Color.WHITE);
			return convertView;
		}
	}

	public void decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			outWidth = o.outWidth;
			outHeight = o.outHeight;
		} catch (FileNotFoundException e) {
		}
	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public class BigImageAdapter extends BaseAdapter {
		private int lastPositoin = 100;
		private View lastConvertView = null;

		public BigImageAdapter() {
		}

		@Override
		public int getCount() {
			return PrintHelper.count;
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if (lastPositoin == position) {
				// lastConvertView.setBackgroundColor(Color.WHITE);
				return lastConvertView;
			}
			BigImageCheckBoxView icbv = null;
			if (convertView == null) {
				icbv = new BigImageCheckBoxView(ImageSelectionActivity.this);
				convertView = icbv;
				convertView.setTag(icbv);
			} else {
				convertView.getTag();
			}
			Boolean reachable = imagecursor.moveToPosition(position);
			if (reachable) {

				Bitmap img = null;
				Bitmap rotated = null;

				try {
					int id = imagecursor.getInt(image_column_index);
					Uri uri = Uri.withAppendedPath(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							Integer.toString(id));
					icbv.uriEncodedPath = uri.toString();
					// Make sure this image key is in the selectedHash list
					if (!PrintHelper.selectedHash.containsKey(uri.toString())) {
						PrintHelper.selectedHash.put(uri.toString(), "0");
					}
					if (PrintHelper.selectedHash.get(uri.toString())
							.equals("1")) {
						icbv.setChecked(true);
					}

					String fileName = Utils.getFilePath(icbv.uriEncodedPath,
							ImageSelectionActivity.this);

					img = PrintHelper.loadThumbnailImage(icbv.uriEncodedPath,
							MediaStore.Images.Thumbnails.MINI_KIND, options,
							ImageSelectionActivity.this);
					ExifInterface exif = new ExifInterface(fileName);
					if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
						Matrix matrix = new Matrix();
						matrix.postRotate(90);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);

					} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
						Matrix matrix = new Matrix();
						matrix.postRotate(270);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);
					}else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180){
						Matrix matrix = new Matrix();
						matrix.postRotate(180);
						rotated = Bitmap.createBitmap(img, 0, 0,
								img.getWidth(), img.getHeight(), matrix, true);
					}

					if (rotated != null) {
						img = null;
						img = rotated;
					}

					Bitmap bit = BitmapFactory.decodeResource(getResources(),
							R.drawable.selectedcheckbox);
					final float scale = getBaseContext().getResources()
							.getDisplayMetrics().density;
					Bitmap scaledImg = null;
					if (img.getHeight() < (240 * scale)) {
						scaledImg = Bitmap.createScaledBitmap(img,
								(int) (img.getWidth() * (240 * scale) / img
										.getHeight()), (int) (img.getHeight()
										* (240 * scale) / img.getHeight()),
								true);
					}
					// Log.d(TAG, "original dimensions: " + img.getWidth() + "x"
					// + img.getHeight());
					if (scaledImg == null) {
						if (img != null) {
							Bitmap test = overlay(img, bit);
							if (icbv.getChecked() && test != null) {
								icbv.setImageBitmap(test);
							} else {
								icbv.setImageBitmap(img);
							}
						}
					} else {
						Log.d(TAG, "scaled dimensions: " + scaledImg.getWidth()
								+ "x" + scaledImg.getHeight());
						Bitmap test = overlay(scaledImg, bit);
						if (icbv.getChecked() && test != null) {
							icbv.setImageBitmap(test);
						} else {
							icbv.setImageBitmap(scaledImg);
						}
					}
					icbv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					icbv.setLayoutParams(new Gallery.LayoutParams(
							Gallery.LayoutParams.WRAP_CONTENT,
							Gallery.LayoutParams.MATCH_PARENT));
					icbv.setId(position);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			} else {
				if (PrintHelper.mLoggingEnabled)
					Log.e(TAG, "imagecursor.moveToPosition for position "
							+ position + " failed");
			}
			lastConvertView = convertView;
			lastPositoin = position;
			// convertView.setBackgroundColor(Color.WHITE);
			return convertView;
		}

		public float getScale(boolean focused, int offset) {
			return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
		}
	}

	public Bitmap overlayThumb(Bitmap bmp1, boolean checked, boolean highlight) {
		Paint mCropPaint = new Paint();
		mCropPaint.setStyle(Paint.Style.STROKE);
		mCropPaint.setStrokeWidth(10f);
		mCropPaint
				.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		mCropPaint.setARGB(255, 251, 186, 6);
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(),
				bmp1.getHeight(), bmp1.getConfig());
		// Bitmap bit = BitmapFactory.decodeResource(getResources(),
		// R.drawable.selectedcheckbox);
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		
		 * if(checked) canvas.drawBitmap(bit, new Matrix(), null); if(highlight)
		 * canvas.drawRect(0.0f,0.0f,canvas.getWidth(),canvas.getHeight(),
		 * mCropPaint);
		 
		return bmOverlay;
	}

	public Handler selectionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// if don't clear, the selected images will be duplicated.
			PrintHelper.selectedImageUrls.clear();
			if (msg.what == 1) {
				headerBarText.setText(PrintHelper.albumName + " ( "
						+ selectedCount + " / " + PrintHelper.count + " )");
				totalSelectedTV.setText("" + selectedCount + " "
						+ getString(R.string.selected));
			}
			if (msg.what == 2) {
				headerBarText.setText(PrintHelper.albumName + " ( 0 / "
						+ PrintHelper.count + " )");
				totalSelectedTV.setText("0 " + getString(R.string.selected));
			}
			startOver.setEnabled(true);
			next.setEnabled(true);
			imageAdapter.notifyDataSetChanged();
			for (Map.Entry<String, String> entry : PrintHelper.selectedHash
					.entrySet()) {
				String uri = entry.getKey();
				String selected = entry.getValue();
				if (selected.equals("1")) {
					String filename = PrintHelper.selectedFileNames.get(
							entry.getKey()).toString();
					PrintHelper.selectedImageUrls.add(uri);
					//PrintHelper.selectedImages.put(uri, new SelectedImage(
					//		ImageSelectionActivity.this, uri));
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						boolean exist = false;
						for (ArrayList<CartItem> childrenList : PrintHelper.cartChildren) {
							for (CartItem cartItem : childrenList) {
								if (cartItem.uri.equals(uri)) {
									exist = true;
									break;
								}
							}
							if (exist) {
								break;
							}
						}
						if (exist) {
							continue;
						}
						CartItem item = new CartItem(
								ImageSelectionActivity.this);
						item.uri = uri;
						item.filename = filename;
						item.quantity = 1;
						item.roi = null;
						item.price = Double.parseDouble(PrintHelper.products
								.get(PrintHelper.defaultPrintSizeIndex)
								.getMinPrice());
						item.width = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getWidth();
						item.height = ""
								+ PrintHelper.products.get(
										PrintHelper.defaultPrintSizeIndex)
										.getHeight();
						item.name = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex).getName();
						item.shortName = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex)
								.getShortName();
						item.productDescriptionId = PrintHelper.products.get(
								PrintHelper.defaultPrintSizeIndex)
								.getId();
						// PrintHelper.items.add(item);
						PrintHelper.cartChildren.get(
								PrintHelper.defaultPrintSizeIndex).add(item);
					}
				} else {
					// PrintHelper.items.remove(itemExists);
					PrintHelper.selectedImageUrls.remove(uri);
					//PrintHelper.selectedImages.remove(uri);
					PrintHelper.uploadQueue.remove(uri);
					EncryptUtil.removeLast(PrintHelper.uploadQueue, uri);
					
//					PrintHelper.uploadQueue
//							.remove(PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
//									+ uri);
					EncryptUtil.removeLast(PrintHelper.uploadQueue, PictureUploadService2.FIRST_UPLOAD_THUMBNAILS
							+ uri);
					PrintHelper.uploadedImageIDs
							.remove(PrintHelper.selectedFileNames.get(uri));
					if (PrintHelper.uploadShare2WmcQueue != null)
						PrintHelper.uploadShare2WmcQueue.remove(uri);
					if (!AppContext.getApplication().getFlowType().isWifiWorkFlow()) {
						for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
							int count = 0;
							while (count < PrintHelper.cartChildren.get(i)
									.size()) {
								CartItem tempItem = PrintHelper.cartChildren
										.get(i).get(count);
								if (tempItem.uri.equals(uri)) {
									PrintHelper.cartChildren.get(i).remove(
											count);
								} else {
									count++;
								}
							}
						}
					}
				}
			}
		}
	};

	private void sendConvertBroadcast() {
		Intent mIntent = new Intent("CONVERT");
		sendBroadcast(mIntent);

	}*/
	public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
		if (bmp1 == null)
			Log.e("ImageSelectionActivity", "bmp1 was null");
		if (bmp2 == null)
			Log.e("ImageSelectionActivity", "bmp2 was null");
		Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(),
				bmp1.getHeight(), bmp1.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp1, new Matrix(), null);
		canvas.drawBitmap(bmp2, 0, 0, null);
		return bmOverlay;
	}
}
