package com.kodak.rss.tablet.util.load;

import android.view.View;

public interface onProcessImageResponseListener {
	public void onProcess(Response response, String profileId, View view,int position,String flowType,String productId);
		
}
