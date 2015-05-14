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

package com.example.android.displayingbitmaps.util;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.os.Build;
import android.os.Build.VERSION_CODES;

import com.kodak.utils.cmyk.JudgeCMYKUtil;

/**
 * Class containing some static utility methods.
 */
public class Utils {
	private Utils() {
	};

	// @TargetApi(VERSION_CODES.HONEYCOMB)
	// public static void enableStrictMode() {
	// if (Utils.hasGingerbread()) {
	// StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
	// new StrictMode.ThreadPolicy.Builder()
	// .detectAll()
	// .penaltyLog();
	// StrictMode.VmPolicy.Builder vmPolicyBuilder =
	// new StrictMode.VmPolicy.Builder()
	// .detectAll()
	// .penaltyLog();
	//
	// if (Utils.hasHoneycomb()) {
	// threadPolicyBuilder.penaltyFlashScreen();
	// vmPolicyBuilder
	// .setClassInstanceLimit(ImageGridActivity.class, 1)
	// .setClassInstanceLimit(ImageDetailActivity.class, 1);
	// }
	// StrictMode.setThreadPolicy(threadPolicyBuilder.build());
	// StrictMode.setVmPolicy(vmPolicyBuilder.build());
	// }
	// }

	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed
		// behavior.
		return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
	}

	public static boolean hasJellyBean() {
		// return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
		return Build.VERSION.SDK_INT >= 16;
	}

	public static boolean hasKitKat() {
		// return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
		return Build.VERSION.SDK_INT >= 19;
	}

	/**
	 * fixed for RSSMOBILEPDC-1841
	 * @param filePath
	 * @param haveNum
	 * @author song
	 * @return
	 */
	
	private static byte[] getByte(String filePath, int haveNum){
		byte buffer[] = new byte[24];
		RandomAccessFile af = null;
		try {
			af = new RandomAccessFile(filePath,"r");			
			af.read(buffer,0,haveNum);						
		} catch (IOException ioe) {
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (af != null) {
				try {
					af.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}
		return buffer;
	}

	private static boolean isWebP(byte[] data) {
        return data != null && data.length > 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
    }
	
	private static boolean isJPEG(byte[] data) { //FF DB FF E1    FF DB FF E0
        return data != null && data.length > 8 && data[0] == -1 && data[1] == -40 && data[2] == -1 && (data[3] == -31 || data[3] == -32);
    }
	
	public static boolean isFilter(String filePath){
		boolean isFilter = false;
		if (filePath == null) return isFilter;
		byte[] data = getByte(filePath, 16);
		isFilter = isWebP(data);
		/*if (!isFilter && isJPEG(data)) {
			isFilter = JudgeCMYKUtil.isCMYK(filePath);
		}*/		
        return isFilter;
	}
	
	
}
