package com.kodak.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;


public class ImageUtil {
	public static boolean isLoadLibrary = false;
	static {
		try {
			System.loadLibrary("rssimageutil");
			isLoadLibrary = true;
		} catch (UnsatisfiedLinkError e) {
			isLoadLibrary = false;
		}
	}

	public static int pngToJpg(String loadFilePath, String saveFilePath)
			throws Exception {
		int result = -1;
		if (isLoadLibrary) {
			result = png2Jpg(loadFilePath, saveFilePath);
		}
		return result;
	}
	
	public static int resizePic(String loadFilePath, String saveFilePath,int width,int height)
			throws Exception {
		int result = -1;
		if (isLoadLibrary) {
			result = resizePicNative(loadFilePath, saveFilePath,width,height);
			try{
				if(!saveFilePath.toUpperCase(Locale.ENGLISH).endsWith(".PNG")){
					//if is not png file, save exif info
					//only save orientation info
					ExifInterface oldExif = new ExifInterface(loadFilePath);
					int orientation = ExifInterface.ORIENTATION_UNDEFINED;
					if(oldExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) == ExifInterface.ORIENTATION_ROTATE_90){
						orientation = ExifInterface.ORIENTATION_ROTATE_90;
					} else if (oldExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) == ExifInterface.ORIENTATION_ROTATE_180){
						orientation = ExifInterface.ORIENTATION_ROTATE_180;
					} else if(oldExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED) == ExifInterface.ORIENTATION_ROTATE_270){
						orientation = ExifInterface.ORIENTATION_ROTATE_270;
					}
					
					if(orientation != ExifInterface.ORIENTATION_UNDEFINED){
						ExifInterface newExif = new ExifInterface(saveFilePath);
						newExif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
						newExif.saveAttributes();
					}
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	
	/**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @param cache The ImageCache used to find candidate bitmaps for use with inBitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

       

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
	
    public static int calculateInSampleSize(BitmapFactory.Options options,
            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }
	
    /**
     * A hashing method that changes a string (like a URL) into a hash suitable for using as a
     * disk filename.
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }
    
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

	public static native int png2Jpg(String loadFilePath, String saveFilePath);
	public static native int resizePicNative(String loadFilePath, String saveFilePath,int width,int height);
}
