package com.kodak.rss.tablet.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;

public class PicMediaUtil {
	private String TAG = "PicMediaUtil";
	private Context context;
	private Cursor cursor;
	private int columnIndex;
	private int colunmPath;
	public PicMediaUtil(Context context){
		this.context = context;
		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
		
		// Create the cursor pointing to the SDCard
		ContentResolver contentResolver = context.getContentResolver();  
        cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,  
                null, null, null);
		// Get the column index of the image ID
		columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
		colunmPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	}
	
	public int getCount(){
		if(cursor!=null){
			return cursor.getCount();
		}
		return 0;
	}
	
	public BitmapBuddle getBitmap(int position,int scalWidth,int scalHeight){
		if(position>=cursor.getCount()||position<0){
			return new BitmapBuddle(null, position,null);
		}
		while(cursor.moveToPosition(position)){
			// Get the current value for the requested column
		    String path = cursor.getString(colunmPath);
		    if(path.toLowerCase().endsWith(".jpg")||path.toLowerCase().endsWith(".jpeg")){
		    	Log.d(TAG, "getBitmap path "+path);
		    	Bitmap bitmap = null;
		    	try {
		    		bitmap = ImageUtil.getImageLocal(path,scalWidth,scalHeight);	
				} catch (OutOfMemoryError e) {
					bitmap = null;
					System.gc();
				}		 		
		 		position++;
		 		return new BitmapBuddle(bitmap, position,path);
		    }else {
				position++;
			}
		}
		
	   return new BitmapBuddle(null, position,null);
	}
	
	public class BitmapBuddle{
		Bitmap bitmap;
		int position;
		String filePath;
		public BitmapBuddle(Bitmap bitmap, int position,String filePath) {
			super();
			this.bitmap = bitmap;
			this.position = position;
			this.filePath = filePath;
		}
		public Bitmap getBitmap() {
			return bitmap;
		}		
		public int getPosition() {
			return position;
		}
		public String getPath() {
			return filePath;
		}
	}
	
}
