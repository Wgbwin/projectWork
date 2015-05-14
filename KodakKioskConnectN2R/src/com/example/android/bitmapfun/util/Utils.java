/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bitmapfun.util;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private Utils() {};

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    public static int getBitmapSize(Bitmap bitmap) {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }*/
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    public static boolean isExternalStorageRemovable() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }*/
        return false;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    public static long getUsableSpace(File path) {
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }*/
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * Get the memory class of this device (approx. per-app memory limit)
     *
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     *
     * @return
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if ActionBar is available.
     *
     * @return
     */
    public static boolean hasActionBar() {
        return false;
    }
    
    public static String getFilePath(String strUri, Context context){
    	String filePath = "";
    	Uri uri = Uri.parse(strUri);
		ContentResolver cr = context.getContentResolver();
		String[] poj = { MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(uri, poj, null, null, null);
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
    
    public static String compressThumbToJPG(String uri, ExifInterface exif, Context context){
		Log.e("Utils", "compressThumbToJPG[uri:" + uri + ", orientation:" + exif.getAttributeInt("Orientation", 0) + "]");
		String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
		File folder = new File(tempFolder);
		if(!folder.exists()){
			folder.mkdirs();
		}
		String path = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + ".jpg";
		File jpgFile = new File(path);
		if(!jpgFile.exists()){
			try {
				jpgFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(Utils.getFilePath(uri, context), options);
		if(options.outHeight>options.outWidth){
			options.inSampleSize = options.outHeight/400;
		} else {
			options.inSampleSize = options.outWidth/400;
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(Utils.getFilePath(uri, context), options);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(jpgFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ExifInterface tempExif = new ExifInterface(path);
			int orientation = -1;
			if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90){
				orientation = ExifInterface.ORIENTATION_ROTATE_90;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180){
				orientation = ExifInterface.ORIENTATION_ROTATE_180;
			} else if(exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270){
				orientation = ExifInterface.ORIENTATION_ROTATE_270;
			}
			tempExif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
			tempExif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}
    
    public static ExifInterface loadImageExif(String fileName) throws Exception{
    	return new ExifInterface(fileName);
    }
    
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= 11;
    }
}
