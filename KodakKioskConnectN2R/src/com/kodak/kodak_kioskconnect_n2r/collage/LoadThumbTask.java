package com.kodak.kodak_kioskconnect_n2r.collage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.kodak.kodak_kioskconnect_n2r.QuickBookSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageSelectionActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class LoadThumbTask extends AsyncTask<Void, Void, Bitmap> {
	private static final String TAG = LoadThumbTask.class.getSimpleName();
	
	private Activity mActivity;
	private ImageView mImageView;
	private String mImageURL;
	
	public LoadThumbTask(Activity activity, ImageView imageView, String imageUrl){
		this.mActivity = activity;
		this.mImageView = imageView;
		this.mImageURL = imageUrl;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		byte[] imgData = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		int count = 0;
		try{
			byte[] data = null;
			while(imgData==null && count<5){
				URL url = new URL(mImageURL);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setReadTimeout(10 * 1000);
				is = conn.getInputStream();
				int length = (int) conn.getContentLength();
			
				if (length > 0) {
					data = new byte[length];
					byte[] buffer = new byte[4098];
					int readLen = 0;
					int destPos = 0;
					while ((readLen = is.read(buffer)) >= 0) {
						if (readLen > 0) {
							System.arraycopy(buffer, 0, data, destPos, readLen);
							destPos += readLen;
						} else {
							Log.w(TAG, "LoadThumbTask: download error.");
						}
					}
					imgData = data;
				}
				count++;
			}
			if(imgData != null){
				return BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			if(is!=null){
				try {
					is.close();
					is = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (conn != null) {
					conn.disconnect();
				}
			}
		} 
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		boolean needUpdate = false;
		String latestURL = "";
		if(mActivity instanceof CollageSelectionActivity){
			latestURL = ((CollageSelectionActivity)mActivity).getCurrentLoadingImageURL();
		} else if(mActivity instanceof QuickBookSelectionActivity){
			latestURL = ((QuickBookSelectionActivity)mActivity).getCurrentLoadingImageURL();
		}
		if(!latestURL.equals("") && latestURL.equals(mImageURL)){
			needUpdate = true;
		}
		if(needUpdate){
			mImageView.setImageBitmap(result);
		}
	}

}
