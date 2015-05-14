package com.kodak.rss.tablet;

import java.util.Stack;

import android.app.Activity;
import android.content.Intent;

import com.kodak.rss.core.util.FileUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.activities.ActivityState;
import com.kodak.rss.tablet.activities.ActivityStateWatcher;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.activities.BaseCaptureActivity;
import com.kodak.rss.tablet.activities.MainActivity;
import com.kodak.rss.tablet.activities.ShoppingCartActivity;
import com.kodak.rss.tablet.activities.SplashPageActivity;
import com.kodak.rss.tablet.activities.StartupActivity;

/**
 * @author Robin.Qian
 * Manager activities
 */
public class AppManager {
	private final static String TAG = "AppManager";
	public static AppManager instance;
	private Stack<Activity> stack;
	
	private AppManager(){
		stack = new Stack<Activity>();
	}
	
	public static AppManager getInstance(){
		if(instance == null){
			instance = new AppManager();
		}
		return instance;
	}
	
	public Activity currentActivity(){
		return stack.peek();
	}
	
	public boolean isAppInBackground(){
		boolean result = false;
		if(currentActivity() instanceof ActivityStateWatcher){
			ActivityStateWatcher act = (ActivityStateWatcher) currentActivity();
			result = ActivityState.STOPPED.equals(act.getActivityState());
		}
		
		return result;
	}
	
	public void addActivity(Activity activity){
		stack.push(activity);
	}
	
	public void removeActivity(Activity activity){
		stack.remove(activity);
	}
	
	public void setCurrentActivity(Activity activity){
		if(activity != stack.peek()){
			//move to top
			stack.remove(activity);
			stack.push(activity);
		}
	}
	
	public void finishAllActivity(){
		//when activity destroy, it will do stack.remove(activity)
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			activity.finish();
		}
	}
	
	public void exitApp(){
		RssTabletApp.getInstance().clearTempImageFolder();
		finishAllActivity();
		FileUtil.deleteAllFilesInFolder(RssTabletApp.getInstance().getTempFolderPath());
		System.exit(0);
	}
	
	public void goToHomeActivity(){
		Log.i(TAG, " Go to home activity");
		Activity act = stack.pop();
		if (act != null && !RssTabletApp.getInstance().isUseDoMore) {
			if (act instanceof BaseActivity) {
				((BaseActivity)act).stopUploadService();
			} else if (act instanceof BaseCaptureActivity) {
				((BaseCaptureActivity)act).stopUploadService();
			}
		}	
		boolean isHomeActivityAlive = false;
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			if(!(activity instanceof MainActivity)){
				activity.finish();
			}else{
				isHomeActivityAlive = true;
			}
		}
		
		if(!isHomeActivityAlive){
			Intent intent = new Intent(act,MainActivity.class);
			act.startActivity(intent);
		}
		act.finish();
	}
	
	public void goToStartupActivity() {
		Log.i(TAG, " Go to Startup activity");
		Activity act = stack.pop();
		if (act != null && !RssTabletApp.getInstance().isUseDoMore) {
			if (act instanceof BaseActivity) {
				((BaseActivity)act).stopUploadService();
			} else if (act instanceof BaseCaptureActivity) {
				((BaseCaptureActivity)act).stopUploadService();
			}
		}	
		boolean isTargetActivityAlive = false;
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			if(!(activity instanceof StartupActivity)){
				activity.finish();
			}else{
				isTargetActivityAlive = true;
			}
		}
		
		if(!isTargetActivityAlive){
			Intent intent = new Intent(act,StartupActivity.class);
			act.startActivity(intent);
		}
		act.finish();
	}
	
	public void goToShoppingActivity(){
		Log.i(TAG, " Go to shop activity");
		Activity act = stack.pop();				
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			if (!(activity instanceof MainActivity)) {
				if(!(activity instanceof ShoppingCartActivity)){
					activity.finish();
				}
			}			
		}			
		Intent intent = new Intent(act,ShoppingCartActivity.class);
		act.startActivity(intent);		
		act.finish();
	}
	
	
	public void restartAppWhenCrash(){
		Log.i(TAG,"restart app");	
		//clearData
		RssTabletApp.getInstance().clearDatasInCrash();

		Activity act = stack.pop();
		
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			activity.finish();
		}
		
		Intent intent = new Intent(act,SplashPageActivity.class);
		intent.putExtra(SplashPageActivity.INTENT_KEY_IS_CRASH_RESTART, true);
		act.startActivity(intent);
		act.finish();
		System.exit(0);
	}
	
}
