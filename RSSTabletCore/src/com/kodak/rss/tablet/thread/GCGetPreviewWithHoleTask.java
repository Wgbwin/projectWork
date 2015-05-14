package com.kodak.rss.tablet.thread;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.greetingcard.GCLayer;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.GreetingCardWebService;
import com.kodak.rss.tablet.view.EditImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class GCGetPreviewWithHoleTask extends AsyncTask<Void, Void, Bitmap> {
	
	private Context mContext;
	private GCPage page;
	private Layer layer;
	private EditImageView ivEdit;
	
	public GCGetPreviewWithHoleTask(Context context, EditImageView ivEdit, GCPage page, Layer layer){
		mContext = context;
		this.page = page;
		this.layer = layer;
		this.ivEdit = ivEdit;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		GreetingCardWebService gcWebService = new GreetingCardWebService(mContext);
		String url = gcWebService.getCardPreviewWithHoleTask(page.id, 480, 320, page.getHoleIndex((GCLayer) layer));
		byte[] data = null;
		try {
			data = gcWebService.httpGetImageDataTask(url, "getCardPreviewWithHoleTask");
		} catch (RssWebServiceException e) {
			e.printStackTrace();
		}
		
		if(data != null){
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if(result!=null && ivEdit.isShown() && layer.contentId.equals(ivEdit.getLayer().contentId) ) {
			ivEdit.setMask(result);
		}
	}
	
	

}
