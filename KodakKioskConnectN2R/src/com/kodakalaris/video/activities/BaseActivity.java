package com.kodakalaris.video.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.AppManager;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.MediaStoreUtils;
import com.kodakalaris.video.activities.TMSSelectPhotosActivity.FilePersisterHelper;
import com.kodakalaris.video.storydoc_format.VideoGenParams;
import com.kodakalaris.video.storydoc_format.VideoGenParams.Vignette;

public class BaseActivity extends FragmentActivity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	static final String INSTANCE_STATE_KEY_VIDEO_PARAMATERS = "INSTANCE_STATE_KEY_VIDEO_PARAMATERS";
	protected static boolean USE_HORIZONTAL;
	protected static boolean DELETE_AUDIO_FILES;
	public static boolean USE_CUSTOM_FONTS;

	private LruCache<String, Bitmap> mBitmapCache;
	private LruCache<String, Bitmap> mThumbnailCache;
	protected VideoGenParams mVideoGenParams;
	private boolean mRestoreInOnResume = true;

	private LinearLayout ly_content;
	private RelativeLayout sideMenuHome_lay, tms_tellMyStroy_lay;
	private DrawerLayout mDrawerLayout;
	private Button slideMenuOpen;
	private Button slideMenuClose_btn;
	private TextView sideMenuVersion_tex;
	protected Button back_btn;
	protected Button next_btn;
	protected TextView headerBar_tex;
	private int resId = 0;
	private View contentView;
	private Context mContex;
	protected MediaPlayer mMediaPlayer;
	public static final String TMS_HOME="TMS Home";
	public static final String TMS_CHANGE_ORDER="TMS Change Order";
	private static HashMap<String, String>attr;
			

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.tms_base);
		RSSLocalytics.onActivityCreate(this);
		// add Activity to stack
		AppManager.getAppManager().addActivity(this);
		getBaseViews();
		initBaseData();
		setBaseEvents();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			// mVideoGenParams = (VideoGenParams)
			// VideoGenParams.readFromFileSystem(this,
			// savedInstanceState.getString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// outState.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS,
		// mVideoGenParams.mUUID.toString());
		super.onSaveInstanceState(outState);

	}

	public Bitmap getBitmapFromCache(String key) {
		if (key == null || key.equals("")) {
			return null;
		}
		Bitmap b = mBitmapCache.get(key);
		if (b != null) {
			// Log.w(TAG, "### Resued a bitmap ###");
		}
		// return null;
		return b;
	}

	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (mBitmapCache.put(key, bitmap) == null) {
			mBitmapCache.put(key, bitmap);
		}
	}

	public Bitmap removeBitmapFromCache(String key) {
		Bitmap b = mBitmapCache.get(key);
		mBitmapCache.remove(key);
		return b;
	}

	public Bitmap getThumbFromCache(String key) {
		if (key == null || key.equals("")) {
			return null;
		}
		Bitmap b = mThumbnailCache.get(key);
		if (b != null) {
			// Log.w(TAG, "### Resued a thumbnail ###:"+key);
		}
		return b;
	}

	public void addThumbToCache(String mFilePath, Bitmap bitmap) {
		if (mThumbnailCache.put(mFilePath, bitmap) == null) {
			mThumbnailCache.put(mFilePath, bitmap);
		}
	}

	@Override
	protected void onPause() {
		Log.e(TAG, "On pause called");
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		super.onPause();
		RSSLocalytics.onActivityPause(this);
		mBitmapCache.evictAll();
		if (mVideoGenParams != null) {
			mVideoGenParams.persistToFileSystem(this);
		}
		// mThumbnailCache.evictAll();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// We read the params in on create, so if we are just getting created
		// don't read them again because that just a waste of cpu cycles.
		if (mRestoreInOnResume && mVideoGenParams != null && mVideoGenParams.mUUID != null) {
			mVideoGenParams = VideoGenParams.readFromFileSystem(this, mVideoGenParams.mUUID.toString());
			// Log.e(TAG, "Vignetts Length:" +
			// mVideoGenParams.mVignettes.size());
		}
		mRestoreInOnResume = true;
		RSSLocalytics.onActivityResume(this);
	}

	@Override
	public void onTrimMemory(int level) {
		Log.w(TAG, "Trim Memory called with:" + level);
		if (level > TRIM_MEMORY_BACKGROUND) {
			mBitmapCache.evictAll();
			mThumbnailCache.evictAll();
		}
		super.onTrimMemory(level);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void removeViewTreeObserverVersionSafe(OnGlobalLayoutListener onGlobalLayoutListener, View view) {
		// if (Build.VERSION.SDK_INT >= 16) {
		// view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
		// } else {
		view.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
		// }

	}

	/**
	 * Without a leading "."
	 */
	public static String getFileExtension(String displayedPath) {
		// Log.i(TAG, "Getting File Extention for:" + displayedPath);
		return displayedPath.substring(displayedPath.lastIndexOf('.') + 1);
	}

	/**
	 * This is logic to save files to the phone storage. Since files can be
	 * swapped we need to ensure we don't overwrite the file.
	 * 
	 * This ensure file name numbers correspond to vignette index. This also
	 * compresses bitmaps converts them to jpeg format.
	 * 
	 * This also takes care of swapping the bitmaps in the bitmap cache.
	 * 
	 */
	protected synchronized void persistFiles(FilePersisterHelper helper) {
		// Log.e(TAG, "Starting persist");
		new File(mVideoGenParams.getAssetPath()).mkdirs();
		ArrayList<Pair<File, File>> filesMap = new ArrayList<Pair<File, File>>();
		ArrayList<Bitmap> cachedBitmaps = new ArrayList<Bitmap>();
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
			String displayedPath = helper.getCurrentPath(i);
			String currentlyStoredPath = helper.getOldPath(vig);
			// We can make 1 optimization. If an image has both paths the
			// same, than nothing has changed and we don't need
			// to copy anything.

			if (helper.areFilesinBitmapCache()) {
				// Log.e(TAG, "Removing from cache:" + displayedPath);
				Bitmap temp = BaseActivity.this.removeBitmapFromCache(displayedPath);
				cachedBitmaps.add(i, temp);
			} else {
				cachedBitmaps.add(i, null);
			}
			if (displayedPath.equals(currentlyStoredPath)) {
				filesMap.add(null);
				continue;
			}
			if (displayedPath.equals("")) {
				filesMap.add(new Pair<File, File>(null, new File(currentlyStoredPath)));
				continue;
			}
			String fileExtention;
			if (helper.compressFilesAsBitmaps()) {
				fileExtention = "jpg";
			} else {
				fileExtention = getFileExtension(displayedPath);
			}
			File tempFile = new File(mVideoGenParams.getAssetPath() + "temp-" + helper.getFilePrefix() + "-" + i + "." + fileExtention);
			filesMap.add(new Pair<File, File>(tempFile, new File(currentlyStoredPath)));

			InputStream in = null;
			OutputStream out = null;
			try {
				tempFile.delete();
				tempFile.createNewFile();
				out = new FileOutputStream(tempFile);
				if (helper.compressFilesAsBitmaps()) {
					boolean isPng = displayedPath.toUpperCase(Locale.ENGLISH).endsWith(".PNG");
					
					final int MAX_DIMENSION = AppConstants.TMS_IMAGE_MAX_DIMENSION;
					int[] resize = getSizeForResize(displayedPath, MAX_DIMENSION);
					
					//resize if necessary
					String tempResizedFilePath = null;
					boolean resizeSucceed = false;
					
					if (resize == null && !isPng) {
						//if don't need resize and convert png to jpg
						//just copy file
						Log.e(TAG, "Copying files");
						in = new FileInputStream(displayedPath);
						byte[] buf = new byte[1024];
						int len;
						while ((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}
					} else {
						if (resize != null) {//means need resize
							if (isPng) {
								//if is png, we need to compress it and then convert it to jpg
								//so we need another temp file
								tempResizedFilePath = getExternalFilesDir(null) + "/temp-tms-" + displayedPath.substring(displayedPath.lastIndexOf("/") + 1);
								File file = new File(tempResizedFilePath);
								file.delete();
								file.createNewFile();
							} else {
								tempResizedFilePath = tempFile.getAbsolutePath();
							}
							try {
								int result = ImageUtil.resizePic(displayedPath, tempResizedFilePath, resize[0], resize[1]);
								resizeSucceed = result >= 0;
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							if (!resizeSucceed) {
								// Log.e(TAG, "Bitmap decoding");
								BitmapFactory.Options o = new BitmapFactory.Options();
								// this doesn't make exactly a max dimension image.
								Bitmap b = null;
								try {
									o.inSampleSize = MediaStoreUtils.calculateInSampleSize(displayedPath, MAX_DIMENSION, MAX_DIMENSION);
									b = BitmapFactory.decodeFile(displayedPath, o);
								} catch (OutOfMemoryError e) {
									e.printStackTrace();
									Log.e(TAG, "decode image failed (sample " + MAX_DIMENSION +") because if Out of Memory");
									try {
										o.inSampleSize = MediaStoreUtils.calculateInSampleSize(displayedPath, MAX_DIMENSION / 2, MAX_DIMENSION / 2);
										b = BitmapFactory.decodeFile(displayedPath, o);
									} catch (OutOfMemoryError e2) {
										e.printStackTrace();
										Log.e(TAG, "decode image failed (sample " + MAX_DIMENSION / 2 + ") because if Out of Memory");
										try {
											o.inSampleSize = MediaStoreUtils.calculateInSampleSize(displayedPath, MAX_DIMENSION / 4, MAX_DIMENSION / 4);
											b = BitmapFactory.decodeFile(displayedPath, o);
										} catch (OutOfMemoryError e3) {
											e.printStackTrace();
											Log.e(TAG, "decode image failed (sample " + MAX_DIMENSION / 4 + ") because if Out of Memory");
											Log.e(TAG, "face dector failid because decode image oom");
										}
									}
								}
								// Log.e(TAG, "Bitmap half done");
								b.compress(CompressFormat.JPEG, 90, out);
								isPng = false;
								out.close();
							}
						}
						
						//png need to be convert to jpg
						if (isPng ) {
							boolean succeed = false;
							String loadFilePath = resize == null || (resize != null && !resizeSucceed) ? displayedPath : tempResizedFilePath;
							try {
								int result = ImageUtil.pngToJpg(loadFilePath, tempFile.getAbsolutePath());
								succeed = result >= 0;
								if (loadFilePath == tempResizedFilePath) {
									new File(loadFilePath).delete();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							if (!succeed) {
								Bitmap bm = BitmapFactory.decodeFile(displayedPath);
								bm.compress(CompressFormat.JPEG, 90, out);
								isPng = false;
								out.close();
							}
						}
						
					}
					
					
					
					
					ExifInterface interIn = new ExifInterface(displayedPath);
					ExifInterface interOut = new ExifInterface(tempFile.getAbsolutePath());
					// Append empty string since these can return null, and
					// writing null is not allowed.
					interOut.setAttribute(ExifInterface.TAG_ORIENTATION, interIn.getAttribute(ExifInterface.TAG_ORIENTATION) + "");
					interOut.setAttribute(ExifInterface.TAG_DATETIME, interIn.getAttribute(ExifInterface.TAG_DATETIME) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, interIn.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, interIn.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, interIn.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_LATITUDE, interIn.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, interIn.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, interIn.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, interIn.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) + "");
					interOut.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, interIn.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) + "");
					interOut.saveAttributes();

					// Log.e(TAG, "Bitmap decoding Done SampleSize:" +
					// o.inSampleSize);
				} else {
					Log.e(TAG, "Copying files");
					in = new FileInputStream(displayedPath);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
					if (out != null)
						out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Use this to view the temp files
		// try {
		// Thread.sleep(5000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }
		for (int i = 0; i < mVideoGenParams.mVignettes.size(); i++) {
			Pair<File, File> elem = filesMap.get(i);
			if (elem == null) {
				// Log.e(TAG, "All paths are the same:" + i);
				continue;
			}
			VideoGenParams.Vignette vig = mVideoGenParams.mVignettes.get(i);
			File tempFile = elem.first;
			File previousFile = elem.second;
			previousFile.delete();
			
			//The audio file need to be delete if exist and it is not swap files
			if (helper.compressFilesAsBitmaps()) {//in current logic, this means not swap
				File audioFile = new File(vig.mAudioPath);
				if (audioFile.exists() ) {
					audioFile.delete();
				}
				vig.mAudioPath = "";
			}
			
			if (tempFile == null) {
				// Log.e(TAG, "Clearing out file:" + i);
				
				helper.setNewPath("", vig);
			} else {
				// Log.e(TAG, "Copying temp file over:" + i);
				String outputFilePath = mVideoGenParams.getAssetPath() + helper.getFilePrefix() + "-" + i + "."
						+ getFileExtension(tempFile.getAbsolutePath());
				// Log.e(TAG, "tempFile:" + tempFile);
				// Log.e(TAG, "outputFilePath:" + outputFilePath);
				File outputFile = new File(outputFilePath);
				outputFile.delete();
				InputStream in = null;
				OutputStream out = null;
				try {
					outputFile.createNewFile();
					in = new FileInputStream(tempFile);
					out = new FileOutputStream(outputFile);
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (helper.areFilesinBitmapCache()) {
					Bitmap b = cachedBitmaps.get(i);
					if (b != null) {
						// Log.e(TAG, "Adding to cache:" + outputFilePath);
						BaseActivity.this.addBitmapToCache(outputFilePath, cachedBitmaps.get(i));
					}
				}
				// Log.e(TAG, "Swap done");
				tempFile.delete();
				helper.setNewPath(outputFile.getAbsolutePath(), vig);
			}
		}
	}
	
	/**
	 * @param path
	 * @param maxDimention
	 * @return [w,h]. If return null, it measns it don't need resize
	 */
	private int[] getSizeForResize(String path, int maxDimention) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		int w = -1, h = -1;
		if (opts.outWidth > opts.outHeight) {
			if (opts.outWidth > maxDimention) {
				w = maxDimention;
				h = w * opts.outHeight / opts.outWidth;
				if (h == 0) {
					h = 1;
				}
				
				return new int[]{w, h};
			}
		} else {
			if (opts.outHeight > maxDimention) {
				h = maxDimention;
				w = h * opts.outWidth / opts.outHeight;
				if (w == 0) {
					w = 1;
				}
				return new int[]{w, h};
			}
		}
		
		return null;
		
	}

	protected void swapFiles(final Vignette vig1, final Vignette vig2) {
		// Log.e(TAG, "Doing vignette swap");
		persistFiles(new FilePersisterHelper() {
			@Override
			public String getCurrentPath(int i) {
				if ((i == vig2.mIndex) || (i == vig1.mIndex)) {
					String result = mVideoGenParams.mVignettes.get(i).mAudioPath;
					// Log.e(TAG, "getCurrentPath" + i + ":" + result);
					return result;
				} else {
					// Log.e(TAG, "getCurrentPath empty");
					return "";
				}
			}

			@Override
			public String getOldPath(Vignette vig) {
				String result = "";
				if (vig == vig1) {
					result = vig2.mAudioPath;
				}
				if (vig == vig2) {
					result = vig1.mAudioPath;
				}
				// Log.e(TAG, "getOldPath" + vig.mIndex + ":" + result);
				return result;
			}

			@Override
			public void setNewPath(String path, Vignette vig) {
				// Log.e(TAG, "path" + vig.mIndex + ":" + path);
				vig.mAudioPath = path;
			}

			@Override
			public String getFilePrefix() {
				return "audio";
			}

			@Override
			public boolean compressFilesAsBitmaps() {
				return false;
			}

			@Override
			public boolean areFilesinBitmapCache() {
				return false;
			}
		});
		persistFiles(new FilePersisterHelper() {
			@Override
			public String getCurrentPath(int i) {
				if ((i == vig2.mIndex) || (i == vig1.mIndex)) {
					String result = mVideoGenParams.mVignettes.get(i).mImagePath;
					// Log.e(TAG, "getCurrentPath" + i + ":" + result);
					return result;
				} else {
					// Log.e(TAG, "getCurrentPath empty");
					return "";
				}
			}

			@Override
			public String getOldPath(Vignette vig) {
				String result = "";
				if (vig == vig1) {
					result = vig2.mImagePath;
				}
				if (vig == vig2) {
					result = vig1.mImagePath;
				}
				// Log.e(TAG, "getOldPath" + vig.mIndex + ":" + result);
				return result;
			}

			@Override
			public void setNewPath(String path, Vignette vig) {
				// Log.e(TAG, "path" + vig.mIndex + ":" + path);
				vig.mImagePath = path;
				if (BaseActivity.this instanceof TMSSelectPhotosActivity) {
					((TMSSelectPhotosActivity) BaseActivity.this).mSelectedImagesViews.get(vig.mIndex).setFilePath(path);
				} else if (BaseActivity.this instanceof AddAudioActivity) {
					((AddAudioActivity) BaseActivity.this).mSelectedImagesViews.get(vig.mIndex).setFilePath(path);
				}
			}

			@Override
			public String getFilePrefix() {
				return "image";
			}

			@Override
			public boolean compressFilesAsBitmaps() {
				return false;
			}

			@Override
			public boolean areFilesinBitmapCache() {
				return true;
			}
		});
		// RectF tempBounds = vig1.mEndBounds;
		// vig1.mEndBounds = vig2.mEndBounds;
		// vig2.mEndBounds = tempBounds;
		if(TMSSelectPhotosActivity.IsEditActivity){
			attr=new HashMap<String, String>();
			attr.put(TMS_CHANGE_ORDER, TMSSelectPhotosActivity.DURING_EDIT);
		}else{
			attr=new HashMap<String, String>();
		attr.put(TMS_CHANGE_ORDER, TMSSelectPhotosActivity.DURING_CREATE);}
		RSSLocalytics.recordLocalyticsEvents(this, TMS_CHANGE_ORDER, attr);
	}

	private void getBaseViews() {
		ly_content = (LinearLayout) findViewById(R.id.TMScontent);
		slideMenuOpen = (Button) findViewById(R.id.slideMenuOpen_btn);
		slideMenuClose_btn = (Button) findViewById(R.id.tms_slideMenuClose_btn);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.tmd_drawer_layout);
		sideMenuHome_lay = (RelativeLayout) findViewById(R.id.tms_sideMenuHome_lay);
		tms_tellMyStroy_lay = (RelativeLayout) findViewById(R.id.tms_tellMyStroy_lay);
		sideMenuVersion_tex = (TextView) findViewById(R.id.tms_sideMenuVersion_tex);
		back_btn = (Button) findViewById(R.id.back_btn);
		next_btn = (Button) findViewById(R.id.next_btn);
		headerBar_tex = (TextView) findViewById(R.id.headerBar_tex);
		relayoutViews();
	}

	private void relayoutViews() {
		LayoutParams backParams = (LayoutParams) back_btn.getLayoutParams();
		backParams.width = LayoutParams.WRAP_CONTENT;
		LayoutParams nextParams = (LayoutParams) next_btn.getLayoutParams();
		nextParams.width = LayoutParams.WRAP_CONTENT;

		back_btn.setText("");
		next_btn.setText("");

		findViewById(R.id.main_navbar).setBackgroundColor(Color.BLACK);
		findViewById(R.id.main_bottombar).setVisibility(View.GONE);
		((View) findViewById(R.id.main_navbar).getParent()).setBackgroundColor(Color.BLACK);
	}

	private void initBaseData() {
		mContex = BaseActivity.this;
		USE_HORIZONTAL = getResources().getBoolean(R.bool.use_horizonral_scrolling);
		DELETE_AUDIO_FILES = getResources().getBoolean(R.bool.delete_audio_files);
		USE_CUSTOM_FONTS = getResources().getBoolean(R.bool.use_custom_fonts);

		if (getIntent().getExtras() == null) {
			Log.e(TAG, "All instances of base activity must have a extras containing the GUID");
		} else {
			mRestoreInOnResume = false;
			mVideoGenParams = VideoGenParams.readFromFileSystem(this, getIntent().getExtras().getString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS));
		}

		// mVideoGenParams.persistToFileSystem(this);
		// VideoGenParams.readAllFromFileSystem(this);
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);// kb
		// final int memClass = ((ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() *
		// 1024;// kb
		// These seem to give the same value
		// Log.e(TAG, "Runtime:" + maxMemory + " Activity:" + memClass);
		mBitmapCache = new LruCache<String, Bitmap>(maxMemory / 6) {

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				// Log.i(TAG, "Entry removed B");
				super.entryRemoved(evicted, key, oldValue, newValue);
			}

			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				// TODO !!! important Kane
				// if (Build.VERSION.SDK_INT >= 19) {
				// return bitmap.getAllocationByteCount() / 1024;
				// } else {
				return bitmap.getByteCount() / 1024;
				// }
			}
		};
		mThumbnailCache = new LruCache<String, Bitmap>(maxMemory / 4) {

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				Log.i(TAG, "Entry removed:" + this.size());
				super.entryRemoved(evicted, key, oldValue, newValue);
			}

			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};
		// this.getActionBar().setDisplayShowCustomEnabled(true);
		// this.getActionBar().setDisplayShowTitleEnabled(false);
		// LayoutInflater inflator = LayoutInflater.from(this);
		// View v = inflator.inflate(R.layout.action_bar, null);
		// // assign the view to the actionbar
		// this.getActionBar().setCustomView(v);
		// //getActionBar().setDisplayShowHomeEnabled(false);
		// super.onPostCreate(savedInstanceState);
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			AppContext.getApplication().setScreenOrientationFlag(true);
		}else {
			AppContext.getApplication().setScreenOrientationFlag(false);
		}
		PackageInfo packageInfo;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String versionName = getString(R.string.mainMenuVersion) + " " + packageInfo.versionName;// + " " + getString(R.string.mainMenuCopyright);
			sideMenuVersion_tex.setText(versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void setBaseEvents() {
		slideMenuOpen.setOnClickListener(openMenu());

		slideMenuClose_btn.setOnClickListener(closeMenu());

		sideMenuHome_lay.setOnClickListener(gotoHome());
		tms_tellMyStroy_lay.setOnClickListener(gotoTelMyStroy());
		mDrawerLayout.setDrawerListener(new DrawerListener() {

			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onDrawerOpened(View arg0) {
				slideMenuOpen.setVisibility(View.GONE);

			}

			@Override
			public void onDrawerClosed(View arg0) {
				if (resId != R.layout.activity_previous_projects) {
					slideMenuOpen.setVisibility(View.VISIBLE);
				}

			}
		});
		next_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				RSSLocalytics.recordLocalyticsEvents(BaseActivity.this, BaseActivity.TMS_HOME);
				Intent i = new Intent();
				i.setClass(mContex, PreviousProjectsActivity.class);
				if(mVideoGenParams != null){
					Bundle options = new Bundle();
					options.putString(INSTANCE_STATE_KEY_VIDEO_PARAMATERS, mVideoGenParams.mUUID.toString());
					i.putExtras(options);
				}
				startActivity(i);
				
			}
		});
		
		back_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (resId == (R.layout.activity_previous_projects)) {
					finish();
				}
			}
		});
	}

	/***
	 * set the content layout
	 * 
	 * @param resId
	 * 
	 */
	@SuppressLint("InlinedApi")
	public void setContentLayout(int resId) {
		this.resId = resId;
		initTitleView();
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		contentView = inflater.inflate(resId, null);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		contentView.setLayoutParams(mLayoutParams);
		if (null != ly_content) {
			ly_content.removeAllViews();
			ly_content.addView(contentView);
		}
	}

	@Override
	protected void onDestroy() {
		AppManager.getAppManager().finishActivity(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if(this instanceof TMSSelectPhotosActivity){
			 super.onBackPressed() ;
		} else if(this instanceof AddTitleActivity){
			Intent intent = new Intent(this, MyStoriesActivity.class);
			AppManager.getAppManager().finishAllActivityExceptMainAndMyStory();
			startActivity(intent);
		} else {
			finish();
		}
		
	}

	private void initTitleView() {
		slideMenuOpen.setBackgroundResource(R.drawable.tms_hamburger_up);
		slideMenuClose_btn.setBackgroundResource(R.drawable.tms_hamburger_up);
		back_btn.setVisibility(View.INVISIBLE);
		next_btn.setVisibility(View.VISIBLE);
		next_btn.setBackgroundResource(R.drawable.tms_folder);
		headerBar_tex.setVisibility(View.VISIBLE);
		if (resId == R.layout.activity_my_stories) {
			headerBar_tex.setText(getString(R.string.TMS_my_stories_title));
		} else if (resId == R.layout.activity_previous_projects) {
			headerBar_tex.setText(getString(R.string.TMS_my_saved_project_title));
			next_btn.setVisibility(View.GONE);
			back_btn.setVisibility(View.VISIBLE);
			back_btn.setBackgroundResource(R.drawable.tms_folder);
			slideMenuOpen.setVisibility(View.GONE);
		}
	}

	protected void playAudioFile(String filePath) {
		Log.e(TAG, "Playing audio file preview:" + filePath);
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			mMediaPlayer.setDataSource(fileInputStream.getFD());
			fileInputStream.close();
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static RectF calculateStartBounds(String imagePath) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, o);
		// Matrix m = MediaStoreUtils.getMatrix(imagePath);
		ExifInterface exif;
		int orientation;
		try {
			exif = new ExifInterface(imagePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			orientation = ExifInterface.ORIENTATION_UNDEFINED;
			e.printStackTrace();
		}
		int imageWidth;
		int imageHeight;
		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
		case ExifInterface.ORIENTATION_ROTATE_270:
			imageWidth = o.outHeight;
			imageHeight = o.outWidth;
			break;
		default:
			imageWidth = o.outWidth;
			imageHeight = o.outHeight;
			break;
		}

		Log.i(TAG, "ImageSize W:" + imageWidth + " H:" + imageHeight);
		float min = Math.min(imageWidth, imageHeight);
		// float max = Math.max(imageWidth, imageHeight);
		float tempX;
		float tempY;
		if (imageWidth > imageHeight) {
			// If landscape, start bounds are 50 50
			tempX = (imageWidth - min) / imageWidth * 0.5f;
			tempY = (imageHeight - min) / imageHeight * 0.5f;
		} else {
			// if portrait, bounds are 20 80
			tempX = (imageWidth - min) / imageWidth * 0.2f;
			tempY = (imageHeight - min) / imageHeight * 0.2f;
		}

		float tempW = min / imageWidth;
		float tempH = min / imageHeight;
		// x,y,w,h

		RectF startBounds = new RectF(tempX, tempY, tempX + tempW, tempY + tempH);
		return startBounds;
	}

	private OnClickListener openMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// open
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		};
		return listener;

	}

	private OnClickListener closeMenu() {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
			}
		};
		return listener;

	}

	private OnClickListener gotoHome() {
		
		RSSLocalytics.recordLocalyticsEvents(this, BaseActivity.TMS_HOME);
		return jumpTo(MainMenu.class);

	}

	private OnClickListener gotoTelMyStroy() {
		return jumpTo(MyStoriesActivity.class);
	}

	@SuppressWarnings("rawtypes")
	private OnClickListener jumpTo(final Class cla) {
		OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// close
				mDrawerLayout.closeDrawer(Gravity.LEFT);
				/*InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(BaseActivity.this);
				builder.setTitle("");
				builder.setMessage(getString(R.string.Home_LoseAllWorkCart));
				builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						intent.setClass(BaseActivity.this, cla);
						AppManager.getAppManager().finishAllActivity();
						startActivity(intent);
					}
				});
				builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();*/
				Intent intent = new Intent();
				intent.setClass(BaseActivity.this, cla);
				AppManager.getAppManager().finishAllActivity();
				startActivity(intent);
			}
		};
		return listener;
	}
}