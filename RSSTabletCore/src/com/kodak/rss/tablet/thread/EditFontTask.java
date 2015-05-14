package com.kodak.rss.tablet.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class EditFontTask<P extends Page,L extends Layer> {	
	private static final String TAG = "EditFontTask";

	private SingleThreadPool pool;
	private PhotobookWebService webService;
	private CalendarWebService calendarWebService;
	private Handler handler;
	
	public EditFontTask(Context context){
		webService = new PhotobookWebService(context);
		calendarWebService = new CalendarWebService(context);
		pool = new SingleThreadPool();
	}
	
	public void startSetFontTask(P page, L layer, TextBlock textBlock, Handler handler, boolean showError){
		this.handler = handler;
		pool.addHighPriorityTask(new SetFontTask(page, layer, textBlock, showError));
	}
	
	public void startSetCaptionTask(P page, L layer, TextBlock textBlock, Handler handler,boolean showError){
		this.handler = handler;
		pool.addHighPriorityTask(new SetCaptionTask(page, layer, textBlock, showError));
	}
	
	private class SetFontTask implements Runnable{		
		private P page;
		private L layer;
		private TextBlock textBlock;
		private boolean showError;
		
		public SetFontTask(P page, L layer, TextBlock textBlock, boolean showError){
			this.page = page;
			this.layer = layer;
			this.textBlock = textBlock;
			this.showError = showError;
		}

		@Override
		public void run() {			
			try{
				TextBlock result = webService.updateTextBlockTask(textBlock);
				if(result != null){
					layer.updateTextBlockData(result);
					if (page instanceof GCPage) {
						for(int i = 0; i< ((GCPage)page).layers.size(); i++){
							if(layer.contentId.equals(((GCPage)page).layers.get(i))){
								((GCPage)page).layers.set(i, (GCLayer)layer);							
								break;
							}
						}
						((GCPage)page).setPageRefresh();
					} else if(page instanceof PhotobookPage) {
						for(int i=0; i<page.layers.length; i++){
							if(layer.contentId.equals(page.layers[i])){
								page.layers[i] = layer;
								break;
							}
						}
						((PhotobookPage)page).setPageRefresh();
					} else if(page instanceof CalendarPage) {
						for(int i=0; i<page.layers.length; i++){
							if(layer.contentId.equals(page.layers[i].contentId)){
								page.layers[i] = layer;
								break;
							}
						}
						((CalendarPage)page).setPageRefresh();
					} else if(page instanceof CollagePage) {
						for(int i=0; i<page.layers.length; i++){
							if(layer.contentId.equals(page.layers[i].contentId)){
								page.layers[i] = layer;
								break;
							}
						}
						((CollagePage)page).setPageRefresh();
					}					
					
					Message msg = handler.obtainMessage();
					msg.obj = page;
					msg.arg1 = 1;
					msg.sendToTarget();
				}
				
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				if(showError){
					Message msg = handler.obtainMessage();
					msg.obj = e;
					msg.sendToTarget();
				}
			} catch(RuntimeException e){
				Log.e(TAG,e);
			}
		}
		
	}
	
	private class SetCaptionTask implements Runnable{
		
		private P page;
		private L layer;
		private TextBlock textBlock;
		private boolean showError;
		
		public SetCaptionTask(P page, L layer, TextBlock textBlock,boolean showError){
			this.page = page;
			this.layer = layer;
			this.textBlock = textBlock;
			this.showError = showError;
		}
		@Override
		public void run() {
			//If run without try-catch, ExecutorService(thread pool) will catch exception auto without log!
			try{
				webService.setCaptionTask(layer.contentId, textBlock.formatText());
				if (page instanceof PhotobookPage) {
					PhotobookPage newPage = webService.layoutPageTask(page.id);
					if(newPage != null){
						PhotoBookProductUtil.updatePageInPhotobook(newPage, true);							
						newPage.setPageRefresh();
						Message msg = handler.obtainMessage();
						msg.obj = newPage;
						msg.sendToTarget();
					}
				} else if (page instanceof CalendarPage) {
					CalendarPage newPage = calendarWebService.layoutPageInCalendarTask(page.id);
					if(newPage != null){
						CalendarUtil.updatePageInCalendar(newPage, true);
						newPage.setPageRefresh();
						Message msg = handler.obtainMessage();
						msg.obj = newPage;
						msg.sendToTarget();
					}
					
				} 				
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				if(showError){
					Message msg = handler.obtainMessage();
					msg.obj = e;
					msg.sendToTarget();
				}
				
			} catch(RuntimeException e){
				Log.e(TAG, e);
			}
			
		}
		
	}
}
