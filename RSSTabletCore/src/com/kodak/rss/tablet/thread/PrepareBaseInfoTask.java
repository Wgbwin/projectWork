package com.kodak.rss.tablet.thread;

import java.lang.ref.WeakReference;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.tablet.RssTabletApp;

import android.content.Context;

public class PrepareBaseInfoTask extends Thread{
	private WeakReference<Context> contextRef;
	private OnCompleteListener onCompleteListener;
	
	public PrepareBaseInfoTask(Context context){
		contextRef = new WeakReference<Context>(context);
	}
	
	public PrepareBaseInfoTask(Context context,OnCompleteListener onCompleteListener){
		this(context);
		this.onCompleteListener = onCompleteListener;
	}
	
	@Override
	public void run() {
		if(contextRef.get()!=null){
			WebService webService = new WebService(contextRef.get());
			RssWebServiceException ex = null;
			try {
				webService.getAuthorizationToken();
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				ex = e;
			}
			
			if(ex == null && (RssTabletApp.getInstance().getCountries()==null || RssTabletApp.getInstance().getCountries().size()==0)){
				try {
					webService.getCountriesTask();
				} catch (RssWebServiceException e) {
					e.printStackTrace();
					ex = e;
				}
			}
			
			synchronized (this) {
				if(onCompleteListener!=null){
					if(ex==null){
						onCompleteListener.onSucceed();
					}else{
						onCompleteListener.onFailed(ex);
					}
				}
			}
		}
	}
	
	public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
		this.onCompleteListener = onCompleteListener;
	}
	
	public interface OnCompleteListener{
		/**
		 * if complete without error, run this
		 */
		void onSucceed();
		/**
		 * if there is some error, run this
		 * @param e
		 */
		void onFailed(RssWebServiceException e);
	}
}
