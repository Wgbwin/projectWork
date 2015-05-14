package com.kodak.rss.core.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;

import com.kodak.rss.RssApp;
import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
/**
 * Purpose: 
 * Author: Slider Xiao
 * Created Time: Aug 14, 2013 1:28:39 PM 
 * Update By: Slider Xiao, Aug 14, 2013 1:28:39 PM
 */
public class ImageUtil {
	static String TAG = "ImageUtil";
	
	/**
	 * add frame for a bitmap
	 * @param bm
	 * @return
	 */
	public static Bitmap combinateFrame(Bitmap bitmap) {
		final int FRAME_SIZE = 5; 
		final int FRAME_ROUND_SIZE = 5;
		final int bitmapWidth = bitmap.getWidth();
		final int bitmapHeight = bitmap.getHeight();
		
		Bitmap newBitmap = null;
		try {
			newBitmap = Bitmap.createBitmap(bitmapWidth+FRAME_SIZE*2, bitmapHeight+FRAME_SIZE*2, Config.ARGB_8888);
		} catch (OutOfMemoryError  e) {
			Log.e(TAG, e);
			System.gc();
			return null;
		}
		
		Canvas canvas = new Canvas(newBitmap);
		canvas.drawColor(Color.TRANSPARENT);
		
		Paint paint = new Paint();
		paint.setColor(0xff27b0ff);
		RectF rect = new RectF(0, 0, bitmapWidth+FRAME_SIZE*2, bitmapHeight+FRAME_SIZE*2);
		canvas.drawRoundRect(rect, FRAME_ROUND_SIZE, FRAME_ROUND_SIZE, paint);
		
		canvas.drawBitmap( bitmap, FRAME_SIZE, FRAME_SIZE, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			bitmap = null;
		}
		return newBitmap;
	}
	
	/**
	 * requests the decoder to subsample the original image, returning a smaller image to save memory.
	 * it could receive too large image. and set default value of width,height with 100,100
	 * @param filePath
	 * @return
	 */
	public static Bitmap getImageLocal(String filePath){
	    return getImageLocal(filePath,BitmapUtil.REQUEST_WIDTH,BitmapUtil.REQUEST_HEIGHT);
	}
	
	public static Bitmap getImageLocal(String filePath, int reqWidth, int reqHeight){
		return getImageLocal(filePath, reqWidth, reqHeight, null);
	}
	
	/**
	 * Options will be applied when decode the bitmap.
	 * Note: You don't need to set options.inSampleSize and options.inJustDecodeBounds.
	 * options.inSampleSize is been calculated by reqWidth and reqHeight, and options.inJustDecodeBounds is false
	 * @param filePath
	 * @param reqWidth
	 * @param reqHeight
	 * @param options 
	 * @return
	 */
	public static Bitmap getImageLocal(String filePath, int reqWidth, int reqHeight, BitmapFactory.Options options){
		if(reqWidth==-1||reqHeight==-1){ // no subsample and no
			return BitmapFactory.decodeFile(filePath);
		}else {
			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options optionsInit = new BitmapFactory.Options();
		    optionsInit.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(filePath, optionsInit);
		    
		    // Calculate inSampleSize
		    optionsInit.inSampleSize = BitmapUtil.calculateInSampleSize(optionsInit, reqWidth, reqHeight);
		    Log.d(TAG, "options inSampleSize "+optionsInit.inSampleSize);
		    
		    if(options == null){
		    	options = optionsInit;
		    }else{
		    	options.inSampleSize = optionsInit.inSampleSize;
		    }
		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    return BitmapFactory.decodeFile(filePath, options);
		}
	}
	/**
	 * Purpose: calculate option for a bitmap, now define default value of width,height with 100,100
	 * Author: Slider Xiao
	 * Created Time: Mar 27, 2013 2:57:36 PM 
	 * Update By: Slider Xiao, Mar 27, 2013 2:57:36 PM
	 */
	static class BitmapUtil {
		public static final int REQUEST_WIDTH = 80;
		public static final int REQUEST_HEIGHT = 80;
		
		public static int calculateInSampleSize(Options options){
			return calculateInSampleSize(options, REQUEST_WIDTH, REQUEST_HEIGHT);
		}
		public static int calculateInSampleSize(Options options,int reqWidth, int reqHeight){
			
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {
				// Calculate ratios of height and width to requested height and
				// width
				final int heightRatio = Math.round((float) height / (float) reqHeight);
				final int widthRatio = Math.round((float) width / (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will
				// guarantee
				// a final image with both dimensions larger than or equal to
				// the
				// requested height and width.
				inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			}
			
			return inSampleSize;
		}
	}
	
	/**
	 * Purpose: get Bitmap use url ,define the largest height and the largest width 
	 * Author: Bing Wang
	 * Created Time: Aug 23, 2013 2:57:36 PM 
	 */
	public static Bitmap getFitBitmap(String url, int maxWidth, int maxHeight) {
		Bitmap result;
		int width, height;
		File file = new File(url);
		if (!file.exists()) {			
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(url, opts);
		if (opts.outWidth == -1 || opts.outHeight == -1) {		
			return null;
		}
		width = opts.outWidth;
		height = opts.outHeight;
		
		int samplesize = 1;
		if (width > maxWidth) {
			samplesize = (int)Math.floor((float)width / (float)maxWidth + 0.8f);
		}
		if (height > maxHeight) {
			int temp = (int)Math.floor((float)height / (float)maxHeight + 0.8f);
			if (temp > samplesize) {
				samplesize = temp;
			}
		}
		if (samplesize >= 8) {
			samplesize = getTwoPower(samplesize);
		}
		
		opts.inSampleSize = samplesize;
		opts.inDither = true;
		opts.inJustDecodeBounds = false;
		try {
			result = BitmapFactory.decodeFile(url, opts);
			if (result != null) {
				
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	private static int getTwoPower(int number) {
		int i = 3;
		while (Math.pow(2, i) < number) {
			i++;
		}
		return (int) Math.round(Math.pow(2, i));
	}
	
	public static Bitmap getThumbnail(ContentResolver cr,int id) {		
		try {
			Log.i(TAG, "id:" + id);
			Bitmap bitmap = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_4444;//Bitmap.Config.ARGB_8888
			options.inJustDecodeBounds = false;				
			int sample = 3;
			options.inSampleSize = sample;
			bitmap = Images.Thumbnails.getThumbnail(cr, id,Images.Thumbnails.MINI_KIND, options);											
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Purpose: get a bitmap use the result of superposition of two images
	 * Author: Bing Wang
	 * Created Time: Aug 19, 2013 10:27:36 AM 
	 */
	public static Bitmap overlay(Bitmap bmp, Bitmap backBitmap){	
		int backWidth = backBitmap.getWidth();
		int backHeight = backBitmap.getHeight();
		Bitmap bmOverlay = Bitmap.createBitmap(backWidth, backHeight, backBitmap.getConfig());
		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(backBitmap, 0,0, null);		
		RectF rectF = new RectF(7, 9, backWidth-7,backWidth-9);
		
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		Bitmap newBmp = null;
		if (bmpWidth < bmpHeight) {			
			newBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth,bmpWidth);	
		}else {			
			newBmp = Bitmap.createBitmap(bmp, 0, 0, bmpHeight,bmpHeight);	
		}			
		canvas.drawBitmap(newBmp, null, rectF, null);			
		
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return bmOverlay;
	}
	
	/**
	 * @param source
	 * @param degrees
	 * @return
	 */
	public static Bitmap rotateBitmap(Bitmap source, float degrees){
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, (float)source.getWidth()/2, (float)source.getHeight()/2);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	/**
	 * Purpose: get the default ROI value 
	 * Author: Bing Wang
	 * Created Time: Sep 6, 2013 10:27:36 AM 
	 */
	public static ROI calculateDefaultRoi(double itemWidth, double itemHeight, double defaultRoiAspectRatio){
		boolean canRotateRoi = true;
		double verticalOffsetFactor = 0.2;
		double horizontalOffsetFactor = 0.5;
		ROI roi = new ROI();
		double defaultRoiX = 0.0;
		double defaultRoiY = 0.0;
		double defaultRoiWidth = 0.0;
		double defaultRoiHeight = 0.0;
	
		if ((itemWidth > 0.0) && (itemHeight > 0.0) && (defaultRoiAspectRatio != 0.0)){		
			double roiAR = defaultRoiAspectRatio;			
			double itemAR = itemWidth / itemHeight;
			
			if ((canRotateRoi) && (((itemAR > 1.0) && (roiAR < 1.0)) || ((itemAR < 1.0) && (roiAR > 1.0)))){			
				roiAR = 1.0 / roiAR;
			}
		
			defaultRoiWidth = itemWidth;
			defaultRoiHeight = defaultRoiWidth / roiAR;
		
			if (defaultRoiHeight > itemHeight){
				defaultRoiHeight = itemHeight;
				defaultRoiWidth = defaultRoiHeight * roiAR;
			}
			
			horizontalOffsetFactor = Math.min(1.0, Math.max(0.0, horizontalOffsetFactor));
			verticalOffsetFactor = Math.min(1.0, Math.max(0.0, verticalOffsetFactor));
			defaultRoiX = (itemWidth - defaultRoiWidth) * horizontalOffsetFactor;
			defaultRoiY = (itemHeight - defaultRoiHeight) * verticalOffsetFactor;
		}
		roi.x = defaultRoiX;
		roi.y = defaultRoiY;
		roi.w = defaultRoiWidth;
		roi.h = defaultRoiHeight;	
		return roi;
	}
	
	public static void calculateDefaultRoi(ImageInfo imageInfo,ProductInfo productInfo){		
		int origW = 0;
		int origH = 0;		
		origW = imageInfo.origWidth;
		origH = imageInfo.origHeight;
		if (!(origW > 0 && origH > 0)) {
			if (imageInfo.editUrl != null) {
				BitmapFactory.Options options = new Options();
				options.inJustDecodeBounds = true;						
				BitmapFactory.decodeFile(imageInfo.editUrl, options);
				origW = options.outWidth;
				origH = options.outHeight;
			}
		}
		if (!(origW > 0 && origH > 0)) return;		
		double ratio = 1.0;		
		if (productInfo.pageWidth <= 0 && productInfo.pageHeight <= 0) return ;		
		double productWidth = productInfo.pageWidth;
		double productHeight = productInfo.pageHeight;		
		if (productWidth > productHeight) {		
			ratio = productWidth / productHeight;
		} else {		
			ratio = productHeight / productWidth;
		}
		ROI tempRoi = ImageUtil.calculateDefaultRoi(origW, origH,ratio);
		tempRoi.x = tempRoi.x / origW;
		tempRoi.y = tempRoi.y / origH;
		tempRoi.w = tempRoi.w / origW;
		tempRoi.h = tempRoi.h / origH;
		productInfo.roi = tempRoi;
	}	
	
	public static boolean isLowResWarning(ImageInfo imageInfo,int[] category,double newWidth, double newHeight){		
		int dpi = 300;
		int neededWidthPixels = 0;
		int neededHeightPixels = 0;
		int[] fileInfo;
		
		double productWidth = category[0];
		double productHeight = category[1];
		
		File file = new File(imageInfo.editUrl);
		fileInfo = decodeFile(file);
		neededWidthPixels = (int) (dpi * productWidth);
		neededHeightPixels = (int) (dpi * productHeight);
		int cropW = (int) (newWidth * fileInfo[0]);
		int cropH = (int) (newHeight * fileInfo[1]);
		
		int smallNeedPixels = neededWidthPixels<neededHeightPixels?neededWidthPixels:neededHeightPixels;
		int smallCrop = cropW<cropH?cropW:cropH;
		if(smallCrop<smallNeedPixels){
		   return true;
		}else {
		   return false;
		}
	}
		
	public static int[]  decodeFile(File f){
		int[] fileInfo = new int[2];	 
		int outWidth = 0;
		int outHeight = 0;
		try{
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			outWidth = o.outWidth;
			outHeight = o.outHeight;
			try {
				ExifInterface exif = new ExifInterface(f.getPath());
				int attOri = exif.getAttributeInt("Orientation", 0);
				if(attOri == ExifInterface.ORIENTATION_ROTATE_90 || attOri == ExifInterface.ORIENTATION_ROTATE_270){
					int temp = outWidth;
					outWidth = outHeight;
					outHeight = temp;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}catch (FileNotFoundException e){
		}
		fileInfo[0] = outWidth;
		fileInfo[1] = outHeight;
		return fileInfo;
	}
	
	public static String compressThumbToJPG(Context context, String uri, ExifInterface exif,String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String path = folderPath + uri.substring(uri.lastIndexOf("/"), uri.length()) + ".jpg";
		File jpgFile = new File(path);
		if (!jpgFile.exists()) {
			try {
				jpgFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		String filePath = getFilePath(context, uri);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		if (options.outHeight > options.outWidth) {
			options.inSampleSize = options.outHeight / 400;
		} else {
			options.inSampleSize = options.outWidth / 400;
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(getFilePath(context, uri), options);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		if(isPngFile(filePath)){
			//set background white
			Bitmap newBitmap = createBitmapWithBackground(bitmap, Color.WHITE);
			bitmap.recycle();
			bitmap = newBitmap;
		}
		
		bitmap.compress(CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(jpgFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		addExifInterfaceToFile(path, exif);
		
		Log.w(TAG, "compressThumbToJPG path:" + path);
		return path;
	}
	
	public static String getFilePath(Context context, String strUri){
		Uri uri = Uri.parse(strUri);
		ContentResolver cr = context.getContentResolver();
		String[] poj = {MediaStore.Images.Media.DATA};
		Cursor cursor = cr.query(uri, poj, null, null, null);
		String filePath = "";
		try{
			cursor.moveToFirst();
			filePath = cursor.getString(0);
		} catch(Exception e){
			e.printStackTrace();
			if(cursor != null){
				cursor.close();
			}
		}
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
		return filePath;
	}
	
	public static ExifInterface getFileExifInterface(Context context, String uri){
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(getFilePath(context, uri));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exif;
	}
	
	public static int getExifOrientation(String filePath){
		int defaultValue = ExifInterface.ORIENTATION_UNDEFINED;
		
		try {
			ExifInterface exif = new ExifInterface(filePath);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, defaultValue);
			return orientation;
		} catch (IOException e) {
			e.printStackTrace();
			return defaultValue;
		}		
	}
	
	public static int getDegreesExifOrientation(String filePath){
		int rotate = 0;  							
		try {
			ExifInterface exif = new ExifInterface(filePath);
			if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
				rotate = 90;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180) {
				rotate = 180;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
				rotate = 270;
			}
		} catch (IOException e) {					
			e.printStackTrace();
		}
		return rotate;		
	}	
	
	public static boolean addExifInterfaceToFile(String path, ExifInterface exif){
		try {
			ExifInterface tempExif = new ExifInterface(path);
			int orientation = -1;
			if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
				orientation = ExifInterface.ORIENTATION_ROTATE_90;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180) {
				orientation = ExifInterface.ORIENTATION_ROTATE_180;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
				orientation = ExifInterface.ORIENTATION_ROTATE_270;
			}
			tempExif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
			tempExif.saveAttributes();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * get {width,height} of image
	 * @param filePath
	 * @return
	 */
	public static int[] getImageSize(String filePath){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		return new int[]{opts.outWidth,opts.outHeight};
	}
	
	public static boolean pngToJpg(String srcFile,String desFile){
		try {
			Log.i(TAG, "convert png("+srcFile+")->jpg("+desFile+")");
			int result = com.kodak.utils.ImageUtil.pngToJpg(srcFile, desFile);
			if(result < 0){
				Log.i(TAG, "convert faild png("+srcFile+")->jpg("+desFile+")");
				return false;
			}else{
				Log.i(TAG, "convert complete(result code:"+result+") png("+srcFile+")->jpg("+desFile+")");
				return true;
			}
		} catch (Exception e) {
			Log.i(TAG, "convert faild png("+srcFile+")->jpg("+desFile+")");
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean resizePic(String srcFile, String desFile, int width, int height){
		try {
			Log.i(TAG, "resize image("+srcFile+")-> image("+desFile+") with size:"+width+"x"+height);
			int result = com.kodak.utils.ImageUtil.resizePic(srcFile, desFile, width, height);
			if(result < 0){//faild
				Log.i(TAG, "resize image faild(result code:"+result+") ("+srcFile+")-> image("+desFile+")");
				return false;
			}else{//succeed
				Log.i(TAG, "resize image succeed(result code:"+result+") ("+srcFile+")-> image("+desFile+")");
				
				//save exif
				try{
					ExifInterface exif = new ExifInterface(srcFile);
					addExifInterfaceToFile(desFile, exif);
				}catch(IOException e){
					e.printStackTrace();
				}
				
				return true;
			}
			
		} catch (Exception e) {
			Log.i(TAG, "resize image faild ("+srcFile+")-> image("+desFile+")");
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getResizeFilePath(String srcPath,int[] size){
		return RssApp.getInstance().getTempImageFolderPath()+"/resize_"+FileUtil.getFileNameWithoutSuffix(srcPath)+"_"+size[0]+"x"+size[1] + ".jpg";
	}
	
	public static boolean isPngFile(String filePath){
		if(filePath == null || filePath.length()==0)
			return false;
		
		return filePath.toLowerCase(Locale.US).endsWith(".png");
	}
	
	/**
	 * some bitmap have TRANSPARENT area(such as png), we can use this method to get a new bitmap with background
	 * @param color
	 * @return bitmap
	 */
	public static Bitmap createBitmapWithBackground(Bitmap bitmap,int color){
		if(bitmap == null){
			return null;
		}
		
		Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawColor(color);
		canvas.drawBitmap(bitmap, 0, 0, null);
		return result;
	}
	
	public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) 
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
//        else 
            //does not have background drawable, then draw white background on the canvas
//            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
	
}
