package com.kodakalaris.kodakmomentslib;

import java.util.Stack;

import android.app.Activity;
import android.content.Intent;

import com.kodakalaris.kodakmomentslib.activity.SplashActivity;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.home.THomeActivity;
import com.kodakalaris.kodakmomentslib.interfaces.ActivityStateWatcher;
import com.kodakalaris.kodakmomentslib.manager.KioskManager;
import com.kodakalaris.kodakmomentslib.manager.PrintHubManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.Log;

/**
 * @author Robin.Qian
 * Manager activities
 */
public class AppManager {
	private final static String TAG = "AppManager";
	private static AppManager instance;
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
			result = AppConstants.ActivityState.STOPPED.equals(act.getActivityState());
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
		finishAllActivity();
		System.exit(0);
	}
	
	public void goToStartupActivity() {
		restartFromTargetActivity(SplashActivity.class, true);
	}
	
	public void restartFromTargetActivity(Class clz, boolean resumeTargetIfExist) {
		Log.i(TAG, " go from " + clz.getSimpleName());
		
		if (resumeTargetIfExist) {
			Activity act = stack.pop();
			
			boolean isTargetActivityAlive = false;
			@SuppressWarnings("unchecked")
			Stack<Activity> temp = (Stack<Activity>) stack.clone();
			for(Activity activity: temp){
				if (activity.isFinishing()) {
					continue;
				}
				
				if(!(activity.getClass().getName().equals(clz.getClass().getName()))){
					activity.finish();
				}else{
					isTargetActivityAlive = true;
				}
			}
			
			if(!isTargetActivityAlive){
				Intent intent = new Intent(act,clz);
				act.startActivity(intent);
			}
			act.finish();
			
			
		} else {
			Activity current = currentActivity();
			Intent intent = new Intent(current,clz);
			
			finishAllActivity();
			current.startActivity(intent);
		}
	}
	
	public void restartAppWhenCrash(){
		Log.i(TAG,"restart app");	

		Activity act = stack.pop();
		
		@SuppressWarnings("unchecked")
		Stack<Activity> temp = (Stack<Activity>) stack.clone();
		for(Activity activity: temp){
			activity.finish();
		}
		
		Intent intent = new Intent(act,SplashActivity.class);
		intent.putExtra(SplashActivity.INTENT_KEY_IS_CRASH_RESTART, true);
		act.startActivity(intent);
		act.finish();
		System.exit(0);
	}
	
	public void startOver() {
		KioskManager.getInstance().startOver();
		PrintManager.getInstance(KM2Application.getInstance()).startOver();
		ShoppingCartManager.getInstance().startOver();
		PrintHubManager.getInstance().startOver();
		KM2Application.getInstance().startOver();
		
		if (KM2Application.getInstance().isIsTablet()) {
			restartFromTargetActivity(THomeActivity.class, false);
		} else {
			restartFromTargetActivity(MHomeActivity.class, false);
		}
	}
	
}
