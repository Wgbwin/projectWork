package com.kodak.rss.tablet;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Intent;

import com.kodak.rss.core.util.Log;

public class AppCrashHandler implements UncaughtExceptionHandler{
	private static final String TAG = "AppCrashHandler";
	
	private Thread.UncaughtExceptionHandler defaultCrashHandler;
	
	public AppCrashHandler(){
		defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
	}
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e(TAG, "App crashed by uncaughtException", ex);
		
		AppManager.getInstance().restartAppWhenCrash();
	}

}
