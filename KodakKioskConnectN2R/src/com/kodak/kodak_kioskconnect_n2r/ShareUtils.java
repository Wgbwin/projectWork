package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 
 * @author 10147995
 * This class is used to get Share apps from mobile phone
 */
public class ShareUtils {
	private String tag = ShareUtils.class.getSimpleName();
	private PackageManager pm;
	
	public ShareUtils(Context context){
		pm = context.getPackageManager();
	}
	
	public List<ShareItem> getShareItems(){
		List<ResolveInfo> apps = getShareApps();
		List<ShareItem> items = new ArrayList<ShareItem>();
		for(int i=0;i<apps.size();i++) {  
		    ResolveInfo resolve = apps.get(i);  
		    ShareItem shareItem = new ShareItem();  
		    shareItem.setIcon(resolve.loadIcon(pm));
		    shareItem.setName(resolve.loadLabel(pm).toString());
		    shareItem.setPackName(resolve.activityInfo.packageName);
		    shareItem.setActName(resolve.activityInfo.name);
		    Log.i(tag, "act name: " + resolve.activityInfo.name);
		    
		    Intent intent = pm.getLaunchIntentForPackage(resolve.activityInfo.packageName);
		    if(intent != null)
		    	items.add(shareItem);
		}  
		return items;
	}
	
	private List<ResolveInfo> getShareApps(){  
	    List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();    
	    Intent intent = new Intent(Intent.ACTION_SEND,null);   
	    intent.addCategory(Intent.CATEGORY_DEFAULT);
	    intent.setType("image/*");  
	    mApps = pm.queryIntentActivities(intent,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);    
	    return mApps;    
	}  
	

	public class ShareItem {
		private Drawable icon;
		private String name;
		private String packName;
		private String actName;
		public Drawable getIcon() {
			return icon;
		}
		public void setIcon(Drawable icon) {
			this.icon = icon;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPackName() {
			return packName;
		}
		public void setPackName(String packName) {
			this.packName = packName;
		}
		public String getActName() {
			return actName;
		}
		public void setActName(String actName) {
			this.actName = actName;
		}
		
		
	}
}


