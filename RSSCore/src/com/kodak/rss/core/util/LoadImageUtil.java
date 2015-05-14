package com.kodak.rss.core.util;

import android.database.Cursor;
import android.provider.MediaStore;

public class LoadImageUtil {

	private static final String DESC = " DESC", ASC = " ASC";
	
	public static String setSortOrder(int sortType) {
		String sortOrder ;
		switch (sortType) {
		case 0:
			sortOrder = MediaStore.Images.ImageColumns.DATE_MODIFIED + DESC;
			break;
		case 1:
			sortOrder = MediaStore.Images.ImageColumns.DATE_MODIFIED + ASC;
			break;
		case 2:
			sortOrder = MediaStore.Images.ImageColumns.DISPLAY_NAME + ASC;
			break;
		case 3:
			sortOrder = MediaStore.Images.ImageColumns.DISPLAY_NAME + DESC;
			break;
		default:
			sortOrder = MediaStore.Images.ImageColumns.DATE_ADDED + ASC;
			break;
		}		
		return sortOrder;
	}
	
	public static void loadImageFromCursor(Cursor c, SortableHashMap<String, SortableHashMap<Integer, String[]>> collection) {
		int columnIndexMimeType = c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
		int columnIndexId = c.getColumnIndex(MediaStore.Images.Media._ID);
		int columnIndexUrl = c.getColumnIndex(MediaStore.Images.Media.DATA);		
		int columnIndexBucketId = c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
		int columnIndexBucketDisplayName = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
		SortableHashMap<Integer, String[]> bucket;
		if (c.moveToFirst()) {
			do {
				if (null == c.getString(columnIndexMimeType)
						|| (!c.getString(columnIndexMimeType).trim().equals("image/jpeg") 
								&& !c.getString(columnIndexMimeType).trim().equals("image/jpg")
								&&!c.getString(columnIndexMimeType).trim().equals("image/png"))){
					continue;
				}
				String[] picInfo = new String[2];
				int imageId = c.getInt(columnIndexId);
				String imageBucketId = c.getString(columnIndexBucketId);
				picInfo[0] = c.getString(columnIndexUrl);
				picInfo[1] = c.getString(columnIndexBucketDisplayName);
				bucket = collection.get(imageBucketId);
				if (null == bucket) {
					bucket = new SortableHashMap<Integer, String[]>();
					collection.put(imageBucketId, bucket);
				}
				bucket.put(imageId, picInfo);
			} while (c.moveToNext());
		}
	}

	
}
