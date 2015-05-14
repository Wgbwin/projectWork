package com.kodak.rss.tablet.thread;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import com.kodak.rss.core.util.LoadImageUtil;
import com.kodak.rss.core.util.SortableHashMap;

public class FindNativeAlbumsTask extends Thread{

	private Handler handler;
	private Context mContext;
	private static final Uri exUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private Cursor exCursor;
	private SortableHashMap<String, SortableHashMap<Integer, String[]>> collection;		

	public FindNativeAlbumsTask(Handler handler,Context mContext,SortableHashMap<String, SortableHashMap<Integer, String[]>> collection) {
		super();
		this.handler = handler;
		this.mContext = mContext;
		this.collection = collection;
	}

	@Override
	public void run() {		
		String sortOrder = LoadImageUtil.setSortOrder(0);				
		String[] imageStr = new String[]{MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA,MediaStore.Images.Media.BUCKET_ID,MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
		collection.clear();
		exCursor = mContext.getContentResolver().query(exUri, imageStr, null, null, sortOrder);
		if (exCursor != null) {
			if (exCursor.getCount() != 0) {
				LoadImageUtil.loadImageFromCursor(exCursor, collection);
			}
			exCursor.close();
		}
		handler.sendEmptyMessage(1);
	}

}
