package com.kodak.kodak_kioskconnect_n2r.collage;

import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;

import android.content.Context;
import android.os.AsyncTask;

public class CollageSwapContentAsyncTask extends AsyncTask<Void, Void, CollagePage> {
	
	private Context mContext;
	private CollagePage page;
	private String contentId1;
	private String contentId2;
	
	public CollageSwapContentAsyncTask(Context context, CollagePage page, String contentId1, String contentId2){
		this.mContext = context;
		this.page = page;
		this.contentId1 = contentId1;
		this.contentId2 = contentId2;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		CollageEditActivity activity = ((CollageEditActivity)mContext);
		activity.showWaiting();
	}

	@Override
	protected CollagePage doInBackground(Void... params) {
		CollageWebServices service = new CollageWebServices(mContext, "");
		CollagePage newPage = service.swapCollageContentTask(page.id, contentId1, contentId2);
		return newPage;
	}

	@Override
	protected void onPostExecute(CollagePage newPage) {
		CollageEditActivity activity = ((CollageEditActivity)mContext);
		activity.hideWaiting();
		if(newPage != null){
			CollageManager manager = CollageManager.getInstance();
			manager.updataCurrentCollagePage(newPage);
			((CollageEditActivity)mContext).notifyPageChaned();
		} else {
			activity.showNoRespondDialog();
		}
	}

}
