package com.kodak.rss.tablet.thread.collage;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollageLayer;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseNetActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class AddCollagePageTextTask extends AsyncTask<Void, Void, Object> {
	
	private Context mContext;
	private InfoDialog waitingDialog;
	private String pageId;
	private TextBlock textBlock;
	
	public AddCollagePageTextTask(Context context, String pageId){
		this.mContext = context;
		this.pageId = pageId;
	}

	@Override
	protected void onPreExecute() {
		waitingDialog = new InfoDialog.Builder(mContext).setMessage(R.string.Common_Wait).setProgressBar(true).create();
		waitingDialog.show();
	}
	
	private boolean isEnd(){
		boolean isEnd = false;
		if (mContext instanceof CollageEditActivity) {
			isEnd  = ((CollageEditActivity)mContext).addImagesView.endTasks();	
		}						
		return isEnd;
	}
	
	@Override
	protected Object doInBackground(Void... params) {	
		while(!isEnd()) {
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {}						
		}
		
		CollageWebService service = new CollageWebService(mContext);
		if(RssTabletApp.getInstance().fonts == null){
			try {
				RssTabletApp.getInstance().fonts = service.getAvailableFontsTask(RssTabletApp.getInstance().getCurrentLanguage());
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}
		}
		Font font = RssTabletApp.getInstance().fonts.get(0);
		try {
			textBlock = service.createTextBlockTask(mContext.getString(R.string.Common_SampleText), font.name);
			if(textBlock == null){
				return null;
			}
			List<String> contents = new ArrayList<String>();
			contents.add(textBlock.id);
			CollagePage newPage = service.insertContentTask(pageId, contents, true);
			if (newPage != null) {
				CollageUtil.updatePageInCollage(newPage, true, true);	
				return newPage;
			} else {
				return false;
			}
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if(mContext!= null && !((Activity)mContext).isFinishing()){
			if(waitingDialog != null && waitingDialog.isShowing()){
				waitingDialog.dismiss();
			}
			if(result == null || result instanceof RssWebServiceException){
				if(mContext instanceof BaseNetActivity){
					((BaseNetActivity)mContext).showErrorWarning((RssWebServiceException) result);
				}
			} else if(result instanceof CollagePage){
				if (mContext instanceof CollageEditActivity) {
					CollageEditActivity activity = (CollageEditActivity)mContext;
					activity.notifyCollagePageChanged();
					CollagePage page = (CollagePage) result;
					CollageLayer layer = null;
					for(Layer tempLayer : page.layers){
						if(tempLayer.contentId.equals(textBlock.id)){
							layer = (CollageLayer)tempLayer;
							break;
						}
					}
					if(layer != null){
						activity.showFontEditView(false, page, layer, false);
					}
				}
			} else if(result instanceof Boolean){
				if(!(Boolean)result){
					// TODO: show add page text failed dialog
				}
			}
		}
	}

}
