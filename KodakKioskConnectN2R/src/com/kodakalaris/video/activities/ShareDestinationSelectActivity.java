package com.kodakalaris.video.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.utils.RSSLocalytics;
import com.kodakalaris.video.storydoc_format.VideoGenParamsUploader;

public class ShareDestinationSelectActivity extends BaseActivity {
	
	private static final String TAG = ShareDestinationSelectActivity.class.getSimpleName();
	private ImageView msgIV = null;
	private ImageView galleryIV = null;
	private ImageView facebookIV = null;
	public static final String TMS_SHARE_DESTINATION="TMS Share Destination";
	public static final String FACEBOOK="Facebook";
	public static final String MESSAGE="Message";
	public static final String PHONE_GALLERY="Phone Gallery";
	private HashMap<String, String>attr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_via);
		//getView();
	}
	
	public void onFacebookButton(View v) {
		LocalyticsType(FACEBOOK);
		setResult(VideoGenParamsUploader.PARAM_UPLOAD_TYPE_FACEBOOK);
		finish();
	}

	public void onMessageButton(View v) {
		LocalyticsType(MESSAGE);
		setResult(VideoGenParamsUploader.PARAM_UPLOAD_TYPE_MMS);
		finish();
	}

	public void onGalleryButton(View v) {
		LocalyticsType(PHONE_GALLERY);
		setResult(VideoGenParamsUploader.PARAM_UPLOAD_TYPE_HD);
		finish();
	}
	private void LocalyticsType(String type){
		attr=new HashMap<String, String>();
		attr.put(TMS_SHARE_DESTINATION,type);
		RSSLocalytics.recordLocalyticsEvents(ShareDestinationSelectActivity.this, TMS_SHARE_DESTINATION, attr);
	}
	public void onCloseShareVia(View v) {
		this.finish();
	}

	public static ResolveInfo getSmsApp(Context context) {
		PackageManager pm = context.getPackageManager();
		Uri uri = Uri.parse("smsto:");
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		List<ResolveInfo> receivers = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if(receivers.size()>0){
			return receivers.get(0);
		}
		return null;
	}
	
	private ResolveInfo getGalleryApp(Context context){
		PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if(resolveInfos.size()>0){
        	for(ResolveInfo resolveInfo : resolveInfos){
        		String packageName = resolveInfo.activityInfo.packageName.toLowerCase(Locale.ENGLISH);
        		if(packageName.contains("gallery") || packageName.contains("album")){
        			return resolveInfo;
        		}
        	}
        }
		return null;
	}
	
	private ResolveInfo getFacebookApp(Context context){
		PackageManager packageManager = getPackageManager();  
	    Intent mIntent = new Intent(Intent.ACTION_MAIN, null);  
	    mIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
	    List<ResolveInfo> listAllApps = packageManager.queryIntentActivities(mIntent, 0);
	    for(ResolveInfo resolveInfo : listAllApps){
	    	if(resolveInfo.activityInfo.packageName.contains("com.facebook.katana")){
	    		return resolveInfo;
	    	}
	    }
	    return null;
	}

	private Drawable getIcon(Context context, ResolveInfo resInfo) {
		if(resInfo != null){
			return resInfo.loadIcon(getPackageManager());
		} else {
			return null;
		}
	}

	private void getView() {
		msgIV = (ImageView) findViewById(R.id.shareVia_msgIV);
		galleryIV = (ImageView) findViewById(R.id.shareVia_galleryIV);
		facebookIV = (ImageView) findViewById(R.id.shareVia_fbIV);
		Drawable msgDraw = getIcon(this, getSmsApp(this));
		if(msgDraw != null){
			msgIV.setBackground(msgDraw);
		}
		Drawable galleryDraw = getIcon(this, getGalleryApp(this));
		if(galleryDraw != null){
			galleryIV.setBackground(galleryDraw);
		}
		Drawable facebookDraw = getIcon(this, getFacebookApp(this));
		if(facebookDraw != null){
			facebookIV.setImageDrawable(facebookDraw);
		}
	};
	
}
