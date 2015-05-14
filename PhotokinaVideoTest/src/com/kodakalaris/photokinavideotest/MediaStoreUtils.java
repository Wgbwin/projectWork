package com.kodakalaris.photokinavideotest;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class MediaStoreUtils {
	private static final String TAG = MediaStoreUtils.class.getSimpleName();

	public static Bitmap getFullRes(Context context, String filePath, int width, int height, int resolution) {

		BitmapFactory.Options o = new BitmapFactory.Options();

		o.inSampleSize = calculateInSampleSize(filePath, width, height);
		if (resolution == SquareImageView.RESOLUTION_HIGH) {
			o.inSampleSize *= 2;// This greatly recuded memory at the expence of
								// image quality
		} else if (resolution == SquareImageView.RESOLUTION_HIGHER) {
			//o.inSampleSize /= 2;
		}
		// o.inPurgeable = true;
		// o.inInputShareable = true;
		Log.w(TAG, "Rendering with W and H:" + width + " " + height + " " + filePath);
		Bitmap fullRes = BitmapFactory.decodeFile(filePath, o);
		// This scales down any huge bitmaps so they don't use more pixels than
		// we are giving them to use.
		// imagecursor.close();
		if (fullRes == null) {
			return null;
		}
		fullRes = Bitmap.createBitmap(fullRes, 0, 0, fullRes.getWidth(), fullRes.getHeight(), getMatrix(filePath), true);

		return fullRes;
	}

	public static Bitmap getGridThumbnail(int id, SquareImageView holder, String filePath) {
		BitmapFactory.Options o = new BitmapFactory.Options();
		Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(holder.getContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, o);
		// Log.e(TAG, "W:" + b.getWidth() + " H:" + b.getHeight());
		return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), getMatrix(filePath), true);

	}

	public static Bitmap getMiniThumbnail(Context context, String filePath) {
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
		Cursor imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media.DATA + " LIKE ?", new String[]{filePath}, orderBy);

		imagecursor.moveToFirst();

		int id = imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.Media._ID));
		Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);
		imagecursor.close();

		thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), getMatrix(filePath), true);
		return thumbnail;
	}

	public static Matrix getMatrix(String filePath) {
		int orientation = getExifOrientation(filePath);
		Matrix matrix = new Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL :
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180 :
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL :
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE :
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90 :
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE :
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270 :
				matrix.setRotate(-90);
				break;
			default :
				// Do nothinng to the image
		}
		return matrix;

	}

	public static int getExifOrientation(String filePath) {
		ExifInterface exif;
		int orientation;
		try {
			exif = new ExifInterface(filePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
		} catch (IOException e) {
			orientation = ExifInterface.ORIENTATION_UNDEFINED;
			e.printStackTrace();
		}
		return orientation;
	}

	public static String getExifTimeDate(String filePath) {
		if (!new File(filePath).exists()) {
			return "";
		}
		ExifInterface exif;
		String dateTime;
		try {
			exif = new ExifInterface(filePath);
			dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
			if (dateTime != null) {
				// Fix for having ":" instead of "-" in date format
				String[] split = dateTime.split(" ");
				if (split.length > 2) {
					dateTime = split[0].replace(':', '-') + " " + split[1];
				}
			}
			// Log.e(TAG, "DateTime2:" + dateTime);
		} catch (Exception e) {
			dateTime = "";
			e.printStackTrace();
		}
		return dateTime == null ? "" : dateTime;
	}
	public static String getExifLatLong(Context context, String filePath) {
		// TODO this might need to be called on a background thread;
		if (!new File(filePath).exists()) {
			Log.e(TAG, "Can't file location of non exiting file");
			return "";
		}

		String location = "";
		try {
			ExifInterface exif = new ExifInterface(filePath);
			String attrLATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
			String attrLATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
			String attrLONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
			String attrLONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
			if ((attrLATITUDE != null) && (attrLATITUDE_REF != null) && (attrLONGITUDE != null) && (attrLONGITUDE_REF != null)) {
				Double lat = null;
				Double lng = null;
				if (attrLATITUDE_REF.equals("N")) {
					lat = convertToDegree(attrLATITUDE);
				} else {
					lat = 0 - convertToDegree(attrLATITUDE);
				}

				if (attrLONGITUDE_REF.equals("E")) {
					lng = convertToDegree(attrLONGITUDE);
				} else {
					lng = 0 - convertToDegree(attrLONGITUDE);
				}
				Geocoder gcd = new Geocoder(context, Locale.getDefault());
				List<Address> addresses = gcd.getFromLocation(lat.doubleValue(), lng.doubleValue(), 1);
				if (addresses.size() > 0) {
					Address a = addresses.get(0);
					location = a.getLocality() + " " + a.getAdminArea();
					Log.v(TAG, "getAddressLine0: " + a.getAddressLine(0));
					Log.v(TAG, "getAddressLine1: " + a.getAddressLine(1));
					Log.v(TAG, "getAddressLine2: " + a.getAddressLine(2));
					Log.v(TAG, "getCountryName: " + a.getCountryName());
					Log.v(TAG, "getFeatureName: " + a.getFeatureName());
					Log.v(TAG, "getLocality: " + a.getLocality());
					Log.v(TAG, "getPremises: " + a.getPremises());
					Log.v(TAG, "getSubAdminArea: " + a.getSubAdminArea());
					Log.v(TAG, "getAdminArea: " + a.getAdminArea());
					Log.v(TAG, "getSubLocality: " + a.getSubLocality());
					Log.v(TAG, "getSubThoroughfare: " + a.getSubThoroughfare());
					Log.v(TAG, "getThoroughfare: " + a.getThoroughfare());
				} else {
					Log.e(TAG, "No addresses");
				}

			} else {
				Log.e(TAG, "All location informatio not present");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location == null ? "" : location;

	}
	private static Double convertToDegree(String stringDMS) {
		Double result = null;
		String[] DMS = stringDMS.split(",", 3);

		String[] stringD = DMS[0].split("/", 2);
		Double D0 = Double.valueOf(stringD[0]);
		Double D1 = Double.valueOf(stringD[1]);
		Double FloatD = D0 / D1;

		String[] stringM = DMS[1].split("/", 2);
		Double M0 = Double.valueOf(stringM[0]);
		Double M1 = Double.valueOf(stringM[1]);
		Double FloatM = M0 / M1;

		String[] stringS = DMS[2].split("/", 2);
		Double S0 = Double.valueOf(stringS[0]);
		Double S1 = Double.valueOf(stringS[1]);
		Double FloatS = S0 / S1;

		result = FloatD + (FloatM / 60) + (FloatS / 3600);

		return result;
	}

	public static String getFilePath(Context context, int position) {
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
		final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
		Cursor imagecursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

		imagecursor.moveToPosition(position);
		// int id =
		// imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.Media._ID));
		int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
		String filePath = imagecursor.getString(dataColumnIndex);
		imagecursor.close();
		return filePath;
	}

	public static int calculateInSampleSize(String filePath, int reqWidth, int reqHeight) {
		// Raw height and width of image

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
			inSampleSize *= 2;
		}
		inSampleSize /= 2;
		if (inSampleSize < 1)
			inSampleSize = 1;

		// Log.e(TAG, "Sample Size:" + inSampleSize);
		// Log.e(TAG, "OH:" + height + " OW:" + width + " SH:" + height /
		// inSampleSize + " SW:" + width / inSampleSize + " DH:" + reqHeight +
		// " DW:" + reqWidth);
		return inSampleSize;
	}
	/*
	 * private static int calculateInSampleSize_orig(String filePath, int
	 * reqWidth, int reqHeight) { // Raw height and width of image
	 * 
	 * BitmapFactory.Options options = new BitmapFactory.Options();
	 * options.inJustDecodeBounds = true; BitmapFactory.decodeFile(filePath,
	 * options);
	 * 
	 * final int height = options.outHeight; final int width = options.outWidth;
	 * int inSampleSize = 1;
	 * 
	 * if (height > reqHeight || width > reqWidth) {
	 * 
	 * final int halfHeight = height / 2; final int halfWidth = width / 2;
	 * 
	 * // Calculate the largest inSampleSize value that is a power of 2 and //
	 * keeps both // height and width larger than the requested height and
	 * width. while ((halfHeight / inSampleSize) > reqHeight && (halfWidth /
	 * inSampleSize) > reqWidth) { inSampleSize *= 2; } } return inSampleSize; }
	 */

	public static void doSetImageBitmap(BitmapOwner owner, String filePath) {
		if (filePath == null || filePath.equals("")) {
			owner.doOnBitmapFailure();
			return;
		}
		File f = new File(filePath);
		if (!f.exists()) {
			// TODO remove this toast.
			Toast.makeText(owner.getContext(), "Image\n" + filePath + "\ndoes not exist", Toast.LENGTH_LONG).show();
			owner.doOnBitmapFailure();
			return;
		}
		if (owner.getImageSize() == SquareImageView.RESOLUTION_THUMBNAIL) {
			owner.doOnBitmapReady(MediaStoreUtils.getMiniThumbnail(owner.getContext(), filePath));
		} else if (owner.getImageSize() == SquareImageView.RESOLUTION_HIGH) {
			Bitmap fullRes = owner.getCachedBitmap(filePath);
			if (fullRes == null) {
				fullRes = MediaStoreUtils.getFullRes(owner.getContext(), filePath, owner.getImageWidth(), owner.getImageHeight(), owner.getImageSize());
			}
			owner.doOnBitmapReady(fullRes);
		} else {
			owner.doOnBitmapReady(MediaStoreUtils.getFullRes(owner.getContext(), filePath, owner.getImageWidth(), owner.getImageHeight(), owner.getImageSize()));
		}

	}
}
