package com.kodakalaris.video.storydoc_format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class VideoGenParams implements Parcelable {

	private static final String TAG = VideoGenParams.class.getSimpleName();
	public String mProjectTitle = null;
	public ArrayList<Vignette> mVignettes;
	String mCreatedByName;
	String mLastModifiedByName;
	String mCreatedTime;
	String mLastModifiedTime;
	int mCoreRevision;
	double mCoreVersion;
	public UUID mUUID;
	private String mProjectBasePath;
	public String mProjectSubTitleTimeDate;
	public String mProjectSubTitleLocation;
	public VideoGenParams(Context context) {
		UUID uuid = UUID.randomUUID();
		String createdBy = "My Kodak Moments Tell My Story V1.0";
		String lastModifiedBy = "Kodak Alaris Personalized Imaging";
		// String projectTitle = "Story1";

		String dateStamp = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
		String timeStamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime());
		String timeDate = dateStamp + "T" + timeStamp + "Z";
		// Log.e(TAG, "Time:" + timeDate);
		ArrayList<Vignette> vignettes = new ArrayList<Vignette>();
		for (int i = 0; i < 3; i++) {
			vignettes.add(i, new Vignette(i, "", "", "", 0, null, null));
		}
		init(getProjectBasePath(context), null, vignettes, uuid, createdBy, lastModifiedBy, 1, 1.0, timeDate, timeDate, null, null);
	}
	
	public VideoGenParams(String projectBasePath, String projectTitle, ArrayList<Vignette> vignettes, UUID uuid, String createdBy, String lastModifiedBy, int coreRevision,
			double coreVersion, String createdOn, String modifiedOn, String subTitle1, String subTitle2) {
		init(projectBasePath, projectTitle, vignettes, uuid, createdBy, lastModifiedBy, coreRevision, coreVersion, createdOn, modifiedOn, subTitle1, subTitle2);

	}
	
	private void init(String projectBasePath, String projectTitle , ArrayList<Vignette> vignettes, UUID uuid, String createdBy, String lastModifiedBy, int coreRevision,
			double coreVersion, String createdOn, String modifiedOn, String subTitle1, String subTitle2) {
		this.mProjectBasePath = projectBasePath;
		this.mProjectTitle = projectTitle;
		this.mVignettes = vignettes;
		this.mUUID = uuid;
		this.mCreatedByName = createdBy;
		this.mLastModifiedByName = lastModifiedBy;
		this.mCoreRevision = coreRevision;
		this.mCoreVersion = coreVersion;
		this.mCreatedTime = createdOn;
		this.mLastModifiedTime = modifiedOn;
		this.mProjectSubTitleTimeDate = subTitle1;
		this.mProjectSubTitleLocation = subTitle2;
		// Log.i(TAG,"Setting vignettes in constructor"+this.toString());
		// Log.i(TAG, "Len:" + mVignettes.size());
	}
	
	public static String getProjectBasePath(Context context) {
		//return Environment.getExternalStorageDirectory().getAbsolutePath() + "/tms_projects/";
		return context.getFilesDir().getAbsolutePath() + "/projects/";
	}
	/**
	 * These files are stored in:
	 * /data/data/com.kodak.kodak.rsscombinedapp/files/projects/
	 * 
	 * To get access to them open the "File Explorer" view in eclipse
	 * 
	 * Window -> Show View -> Other -> Android -> File Explorer
	 * 
	 * You must have ROOT access on the device!! Follow these steps:
	 * 
	 * http://stackoverflow.com/questions/4867379/android-eclipse-ddms-cant-
	 * access-data-data-on-phone-to-pull-files
	 * 
	 * Commands like this might be useful to get the project files onto a computer
	 * 
	 * ./adb shell "su -c 'cd /data/data/<package name>/files/projects ; chmod -R 777 *'"
	 * 
	 * ./adb pull /data/data/<package name>/files/projects/ /Users/<user name>/Documents/pulledProjects/
	 */
	public void persistToFileSystem(Context context) {
		try {
			Log.e(TAG, "Persisting:" + this.getProjectPath());
			new VideoGenParamsWriter().persist(this);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<VideoGenParams> readAllFromFileSystem(Context context) {
		try {
			return new VideoGenParamsReader().read(getProjectBasePath(context), context);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<VideoGenParams>();
	}
	
	public static VideoGenParams readFromFileSystem(Context context, String guid) {
		try {
			return new VideoGenParamsReader().read(getProjectBasePath(context), guid, context);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String persistToZipFile(Context context) {
		try {
			persistToFileSystem(context);
			String zipPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/KodakOutput/out-zip";
			new VideoGenParamsZipper().zip(getProjectPath(), zipPath);
			return zipPath;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static VideoGenParams makeCopy(Context context, VideoGenParams params){
		UUID uuid = UUID.randomUUID();
		copyFolder(params.getProjectPath(), getProjectBasePath(context) + uuid);
		VideoGenParams newParams = readFromFileSystem(context, uuid.toString());
		newParams.mUUID = uuid;
		newParams.mProjectTitle = params.mProjectTitle;
		newParams.mProjectSubTitleTimeDate = params.mProjectSubTitleTimeDate;
		newParams.mProjectSubTitleLocation = params.mProjectSubTitleLocation;
		for(int i=0; i<3; i++){
			Vignette vignette = newParams.mVignettes.get(i);
			vignette.mAudioPath = newParams.getAssetPath() + "audio-" + i + ".mp4";
			vignette.mImagePath = newParams.getAssetPath() + "image-" + i + ".jpg";
		}
		return newParams;
	}
	
	private static boolean copyFolder(String oldPath, String newPath) { 
		boolean isok = true;
	    try {
	    	(new File(newPath)).mkdirs(); 
	        File a = new File(oldPath); 
	        String[] file=a.list(); 
	        File temp=null; 
	        for (int i = 0; i < file.length; i++) { 
	        	if(oldPath.endsWith(File.separator)){ 
	        		temp=new File(oldPath+file[i]); 
	        	} else { 
	        		temp=new File(oldPath+File.separator+file[i]); 
	        	} 
	        	if(temp.isFile()){ 
	        		FileInputStream input = new FileInputStream(temp); 
	        		FileOutputStream output = new FileOutputStream(newPath + "/" + (temp.getName()).toString()); 
	        		byte[] b = new byte[1024 * 5]; 
	        		int len; 
	        		while ( (len = input.read(b)) != -1) { 
	        			output.write(b, 0, len); 
	        		} 
	        		output.flush(); 
	        		output.close(); 
	        		input.close(); 
	        	} 
	        	if(temp.isDirectory()){
	        		copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]); 
	        	} 
	        } 
	    } catch (Exception e) { 
	    	isok = false;
	    } 
	    return isok;
	}
	
	public static final int VALID = 0;
	public static final int INVALID_IMAGE_SELECTION = 1;
	public static final int INVALID_AUDIO_RECORD = 2;
	public static final int INVALID_NO_IMAGE_SELECTED = 3;
	
	public int validate(){
		int invalidImageNum = 0;
		for(Vignette vignette : mVignettes){
			if(!new File(vignette.mImagePath).exists()){
				invalidImageNum ++;
			}
		}
		if(invalidImageNum != 0){
			if(invalidImageNum == 3){
				return INVALID_NO_IMAGE_SELECTED;
			} else {
				return INVALID_IMAGE_SELECTION;
			}
		}
		for(Vignette vignette : mVignettes){
			if(!new File(vignette.mAudioPath).exists()){
				return INVALID_AUDIO_RECORD;
			}
		}
		return VALID;
	}
	
	public boolean allImagesSelected(){
		for(Vignette vignette : mVignettes){
			if(!new File(vignette.mImagePath).exists()){
				return false;
			}
		}
		return true;
	}

	public String getProjectPath() {
		return mProjectBasePath + mUUID.toString() + "/";
	}
	public String getAssetPath() {
		return getProjectPath() + "Assets" + "/";
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mProjectBasePath);
		Log.w(TAG, "Wrote from parcable basePath:" + mProjectBasePath);
		dest.writeString(mProjectTitle);
		dest.writeTypedList(mVignettes);
		dest.writeString(mUUID.toString());
		dest.writeString(mCreatedByName);
		dest.writeString(mLastModifiedByName);
		dest.writeInt(mCoreRevision);
		dest.writeDouble(mCoreVersion);
		dest.writeString(mCreatedTime);
		dest.writeString(mLastModifiedTime);
		dest.writeString(mProjectSubTitleTimeDate);
		dest.writeString(mProjectSubTitleLocation);

	}
	public static final Parcelable.Creator<VideoGenParams> CREATOR = new Parcelable.Creator<VideoGenParams>() {
		public VideoGenParams createFromParcel(Parcel in) {
			String projectBasePath = in.readString();
			Log.w(TAG, "Read from parcable basePath:" + projectBasePath);
			String projectTitle = in.readString();
			//ResultReceiver reviever = (ResultReceiver) in.readParcelable(VideoGenResultReceiver.class.getClassLoader());
			ArrayList<Vignette> vignettes = new ArrayList<Vignette>();
			in.readTypedList(vignettes, Vignette.CREATOR);
			String uuid = in.readString();
			String createdBy = in.readString();
			String lastModifiedBy = in.readString();
			int coreRevision = in.readInt();
			double coreVersion = in.readDouble();
			String createdOn = in.readString();
			String modifiedOn = in.readString();
			String projectSubTitleTimeDate = in.readString();
			String projectSubTitleLocation = in.readString();
			VideoGenParams params = new VideoGenParams(projectBasePath, projectTitle, vignettes, UUID.fromString(uuid), createdBy, lastModifiedBy, coreRevision, coreVersion, createdOn,
					modifiedOn, projectSubTitleTimeDate, projectSubTitleLocation);
			return params;
		}

		public VideoGenParams[] newArray(int size) {
			return new VideoGenParams[size];
		}
	};
	public static class Vignette implements Parcelable {
		public int mIndex;
		public String mImagePath;
		public String mImageId;
		public String mAudioPath;
		public int mLength;
		public RectF mStartBounds;
		public RectF mEndBounds;
		public Vignette(int index, String imagePath, String imageId, String audioPath, int length, RectF startBounds, RectF endBounds) {
			mIndex = index;
			mImagePath = imagePath;
			mImageId = imageId;
			mAudioPath = audioPath;
			mLength = length;
			mStartBounds = startBounds;
			mEndBounds = endBounds;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mIndex);
			dest.writeString(mImagePath);
			dest.writeString(mImageId);
			dest.writeString(mAudioPath);
			dest.writeInt(mLength);
			dest.writeParcelable(mStartBounds, flags);
			dest.writeParcelable(mEndBounds, flags);
		}
		public static final Parcelable.Creator<Vignette> CREATOR = new Parcelable.Creator<Vignette>() {
			public Vignette createFromParcel(Parcel in) {
				int index = in.readInt();
				String selectedImagePath = in.readString();
				String imageId = in.readString();
				String audioPath = in.readString();
				int length = in.readInt();
				RectF startBounds = (RectF) in.readParcelable(Rect.class.getClassLoader());
				RectF endBounds = (RectF) in.readParcelable(Rect.class.getClassLoader());
				return new Vignette(index, selectedImagePath, imageId, audioPath, length, startBounds, endBounds);
			}

			public Vignette[] newArray(int size) {
				return new Vignette[size];
			}
		};
	}
	public static void deleteProject(Context context, String guid) {
		deleteDirectory(new File(getProjectBasePath(context) + guid));
	}
	public static boolean deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		return (directory.delete());
	}
	public static void sortProjects(File[] projects) {
		Arrays.sort(projects, new Comparator<File>() {
			public int compare(File f1, File f2) {
				// Sort by last modified so that
				// recently looked at projects are at the top.
				return -1 * Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			}
		});

	}
}
