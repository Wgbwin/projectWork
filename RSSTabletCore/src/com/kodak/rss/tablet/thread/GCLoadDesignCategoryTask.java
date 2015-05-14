package com.kodak.rss.tablet.thread;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.GCCategorySelectActivity;

public class GCLoadDesignCategoryTask extends AsyncTask<String, Void, Object>{
	private static final String TAG = "GCLoadInfoTask:";	
	private Context mContext;	
	private View waitView;
	private GCCategory gcc;
	
	public GCLoadDesignCategoryTask(Context context,GCCategory gcc,View waitView){
		this.mContext = context;
		this.gcc = gcc;
		this.waitView = waitView;		
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.i(TAG, "Start load Design GCCategory...");
		waitView.setVisibility(View.VISIBLE);		
	}
	
	@Override
	protected Object doInBackground(String... params) {
		String designIds = gcc.id;
		String proDesIds = "";
		/*if (gcc.productIdentifiers != null) {
			for (String proDesId :  gcc.productIdentifiers) {
				proDesIds += proDesId + ",";
			}
		}*/
				
		List<Catalog> catalogs = RssTabletApp.getInstance().getCatalogList();
		for(Catalog catalog : catalogs){
			for(RssEntry entry : catalog.rssEntries){
				String productType = entry.proDescription.type;
				if(productType.equals(AppConstants.cardType_GC) || productType.equals(AppConstants.cardType_DMG)){
					proDesIds += entry.proDescription.id + ",";
				}
			}
		}
		if (proDesIds.endsWith(",")) {
			proDesIds = proDesIds.substring(0, proDesIds.length()-1);
		}
		GreetingCardWebService mService = new GreetingCardWebService(mContext);								
		List<GCCategory> categorys = null;
		try {
			categorys = mService.getGreetingCardForDesignsTask(designIds, proDesIds);
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return categorys;
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);	
		if(mContext!= null && !((Activity)mContext).isFinishing()){			
			if(waitView != null ){
				waitView.setVisibility(View.GONE);
			}
			if(result == null){
				Log.e(TAG, "load Themes failed.");				
			}else if(result instanceof RssWebServiceException){
				Log.e(TAG, "load categorys failed.");				
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else {
				Log.i(TAG, "load categorys succeed.");											
				if (mContext instanceof GCCategorySelectActivity) {
					((GCCategorySelectActivity)mContext).dealDesignCategory((List<GCCategory>) result);					
				}	
			}
		}
	}

}	