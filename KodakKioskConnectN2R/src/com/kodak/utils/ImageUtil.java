package com.kodak.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore.Images;

import com.AppConstants;
import com.example.android.common.logger.Log;
import com.example.android.displayingbitmaps.util.ImageCache;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;


public class ImageUtil {
	private static final String TAG  = ImageUtil.class.getSimpleName() ;
	public static boolean isLoadLibrary = false;
	private static final int IO_BUFFER_SIZE = 8 * 1024;
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
	 * get bitmap of the photoInfo object
	 * @param photoInfo
	 * @param context
	 * @return
	 */
	public static Bitmap getBitmapOfPhotoInfo(PhotoInfo photoInfo ,Context context) {
		Bitmap bitmap = null ;
		if(photoInfo.getPhotoSource().isFromPhone()){
			bitmap = Images.Thumbnails.getThumbnail(
                    context.getContentResolver(), Long.parseLong(photoInfo.getPhotoId()) ,
                    Images.Thumbnails.MINI_KIND, null);
		}else if(photoInfo.getPhotoSource().isFromFaceBook()){
			File picture = new File(photoInfo.getPhotoPath()) ;
			
			if(picture.exists()){
				bitmap = decodeSampledBitmapFromFile(photoInfo.getPhotoPath(),
						AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
			}
			
			
		}
		
		return bitmap ;
	}
	
	
	/**
     * Download a bitmap from a URL and write the content to an output stream.
     *
     * @param urlString The URL to fetch
     * @return the bitmap
     */
	public static boolean downloadUrlToStream(PhotoInfo photoInfo ,Context context) {
		disableConnectionReuseIfNecessary();
		HttpURLConnection urlConnection = null;
	
		BufferedInputStream in = null;
		BufferedOutputStream outToKodakDir = null;
		OutputStream outputStreamToKodak = null;
		
		Bitmap bitmap = null; 
//		File fileDir = new File(context.getExternalCacheDir(), AppContext.KODAK_TEMP_PICTURE_WEB);
		File fileDir = ImageCache.getDiskCacheDir(context,  AppConstants.KODAK_TEMP_PICTURE_WEB) ;
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		String key = hashKeyForDisk(photoInfo.getThumbnailUrl()) ;
		File pictureFile = new File(fileDir, key+".tmp");
		Log.v("sunny", "filepath down : "+pictureFile.getPath());

		try {

			if (!pictureFile.exists()) {
				pictureFile.createNewFile();
			}else {
				bitmap = decodeSampledBitmapFromFile(photoInfo.getPhotoPath(),AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
			
			   if(bitmap!=null){
				   return true ;
			   }
			}

			outputStreamToKodak = new FileOutputStream(pictureFile);

			final URL url = new URL(photoInfo.getThumbnailUrl());
			urlConnection = (HttpURLConnection) url.openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
			

			outToKodakDir = new BufferedOutputStream(outputStreamToKodak, IO_BUFFER_SIZE);

			int b;
			while ((b = in.read()) != -1) {
			
				outToKodakDir.write(b) ;
			}
//			Thread.sleep(50) ;
			
//			bitmap = decodeSampledBitmapFromFile(photoInfo.getPhotoPath(),AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
			
			
			return true ;
		} catch (final IOException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e);
			return false  ;
		}  finally {
			if(bitmap!=null){
				bitmap.recycle(); 
			}
			
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if(outToKodakDir!=null){
					outToKodakDir.close() ;
				}
				
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
			}
		}
		
		
	}
	
	public static Bitmap downLoadBitmap(String urlString, String pathStr){

		URL url = null;
		InputStream ins = null;
		Bitmap bitmap = null;
		FileOutputStream fileOutputStream = null;
		HttpURLConnection urlConnection = null;
		String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER+  "/";
		File folder = new File(tempFolder);
		String priviewFilePath = tempFolder +pathStr + "Priview" + ".temp";
		File priviewFile = new File(priviewFilePath);
		try {
			url = new URL(urlString);
			urlConnection = (HttpURLConnection) url.openConnection();
			if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				ins = urlConnection.getInputStream();
				if (!folder.exists()) {
					folder.mkdirs();
				}
				
				fileOutputStream = new FileOutputStream(priviewFile); 				
				bitmap = BitmapFactory.decodeStream(ins);
				CompressFormat format= Bitmap.CompressFormat.PNG;  
				bitmap.compress(format, 100, fileOutputStream);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}

			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	
	}
	

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
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
	
	public static void saveBitmapToLocal(Bitmap bmp,String filePath) {
		File priviewFile = new File(filePath);
		FileOutputStream fileOutputStream = null;
        try {  
        	fileOutputStream = new FileOutputStream(priviewFile);  
        	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);  
        	fileOutputStream.close();  
        } catch (Exception e) {  
         	e.printStackTrace();  
        } finally {
        	if (fileOutputStream !=null){
        		try {
					fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	}
}
