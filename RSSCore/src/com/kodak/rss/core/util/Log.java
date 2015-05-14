package com.kodak.rss.core.util;

/**
 * Help the project to manage the Log.
 * Please use this Log class instead of using android.util.Log
 * @author Robin.Qian
 *
 */
public class Log {
	static boolean logEnabled = true;
	
	public static void v(String tag, String msg)
	{
		if (logEnabled) {
			android.util.Log.v(tag, msg);
		}
	}
	
	public static void d(String tag, String msg)
	{
		if (logEnabled) {
			android.util.Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg)
	{
		if (logEnabled) {
			android.util.Log.i(tag, msg);
		}
	}
	
	public static void w(String tag, Throwable t){
		if (logEnabled) {
			android.util.Log.w(tag, t);
		}
	}

	public static void w(String tag, String msg)
	{
		if (logEnabled) {
			android.util.Log.w(tag, msg);
		}
	}

	public static void w(String tag, String msg, Throwable t)
	{
		if (logEnabled) {
			android.util.Log.w(tag, msg, t);
		}
	}
	
	public static void e(String tag, String msg)
	{
		if (logEnabled) {
			android.util.Log.e(tag, msg);
		}
	}
	
	public static void e(String tag, String msg, Throwable t)
	{
		if (logEnabled) {
			android.util.Log.e(tag, msg, t);
		}
	}
	
	public static void e(String tag, Throwable t)
	{
		if (logEnabled) {
			android.util.Log.e(tag, "" , t);
		}
	}
}
