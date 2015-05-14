package com.kodakalaris.photokinavideotest.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.kodakalaris.photokinavideotest.MediaStoreUtils;
import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.activities.SelectPhotosActivity.FilePersisterHelper;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams;
import com.kodakalaris.photokinavideotest.storydoc_format.VideoGenParams.Vignette;

public class BaseActivity extends Activity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	static final String INSTANCE_STATE_KEY_VIDEO_PARAMATERS = "INSTANCE_STATE_KEY_VIDEO_PARAMATERS";
	protected static boolean USE_HORIZONTAL;
	protected static boolean DELETE_AUDIO_FILES;
	public static boolean USE_CUSTOM_FONTS;

	private LruCache<String, Bitmap> mBitmapCache;
	private LruCache<String, Bitmap> mThumbnailCache;
	protected VideoGenParams mVideoGenParams;
	private boolean mRestoreInOnResume = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				if (Build.VERSION.SDK_INT >= 19) {
					return bitmap.getAllocationByteCount() / 1024;
				} else {
					return bitmap.getByteCount() / 1024;
				}
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
		super.onPause();
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
		if (Build.VERSION.SDK_INT >= 16) {
			view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
		} else {
			view.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
		}

	}
	/**
	 * Without a leading "."
	 * */
	public static String getFileExtension(String displayedPath) {
		// Log.i(TAG, "Getting File Extention for:" + displayedPath);
		return displayedPath.substring(displayedPath.lastIndexOf('.') + 1);
	}
	protected void persistFiles(FilePersisterHelper helper) {
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
					// Log.e(TAG, "Bitmap decoding");
					BitmapFactory.Options o = new BitmapFactory.Options();
					// TODO this doesn't make exactly a 1280 by 1280 image.
					// It needs to be exactly 1280 by 1280.
					o.inSampleSize = MediaStoreUtils.calculateInSampleSize(displayedPath, 1280, 1280);
					Bitmap b = BitmapFactory.decodeFile(displayedPath, o);
					// Log.e(TAG, "Bitmap half done");
					b.compress(CompressFormat.JPEG, 90, out);
					out.close();

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
			if (tempFile == null) {
				// Log.e(TAG, "Clearing out file:" + i);
				helper.setNewPath("", vig);
			} else {
				// Log.e(TAG, "Copying temp file over:" + i);
				String outputFilePath = mVideoGenParams.getAssetPath() + helper.getFilePrefix() + "-" + i + "." + getFileExtension(tempFile.getAbsolutePath());
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
				if (BaseActivity.this instanceof SelectPhotosActivity) {
					((SelectPhotosActivity) BaseActivity.this).mSelectedImagesViews.get(vig.mIndex).setFilePath(path);
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
	}
}