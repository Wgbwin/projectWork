package com;

import java.util.Stack;

import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodakalaris.video.activities.MyStoriesActivity;

import android.app.Activity;

public class AppManager {
	private static Stack<Activity> activityStack;
	private volatile static AppManager instance;

	private AppManager() {
	}

	public static AppManager getAppManager() {
		if (instance == null) {
			synchronized(AppManager.class){
				if(instance == null){
					instance = new AppManager();
				}
				
			}
			
		}
		return instance;
	}

	/**
	 * add the activity to stack
	 */
	public void addActivity(Activity activity) {
		if (activityStack == null) {
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}

	/**
	 * get the current Activity
	 */
	public Activity currentActivity() {
		Activity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * finish the current activity
	 */
	public void finishActivity() {
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * finish the activity by the Activity name
	 */

	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	/**
	 * finish the activity by the class name
	 */
	public void finishActivity(Class<?> cls) {
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}

	}

	/**
	 * finish all of the acitvity
	 */
	public void finishAllActivity() {
		for (Activity activity : activityStack) {
			activity.finish();
		}
		activityStack.clear();
	}
	
	public void finishAllActivityExceptMainAndMyStory(){
		if(activityStack!=null){
			for(int i=0; i<activityStack.size(); i++){
				if(activityStack.get(i) instanceof MyStoriesActivity){
					// do not need to finish
				} else if(activityStack.get(i) instanceof MainMenu){
					// do not need to finish
				} else {
					finishActivity(activityStack.get(i));
					i --;
				}
			}
		}		
	}
	
	public boolean isActivityExist(Class<?> cls) {
		for (Activity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				return true;
			}
		}
		return false;
	}
}
