package com.kodakalaris.kodakmomentslib;

import java.lang.Thread.UncaughtExceptionHandler;

import com.kodakalaris.kodakmomentslib.util.Log;

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
