package com.kodak.rss.tablet.util.load;

import java.io.File;

import android.content.Context;
import com.kodak.rss.tablet.RssTabletApp;

public class FilePathConstant {

	public static String externalType ="external";
	public static String printType ="print";		
	public static String bookType = "book";
	public static String cardType = "card";
	public static String calendarType = "calendar";
	public static String collageType = "collage";
	public static String projectType ="project";	
	public static String tempFolder = RssTabletApp.getInstance().getDataFolderPath() + "/kodakTemp";
	public static String tempSaveFolder = tempFolder+ "/.";
	public static String thumbnail = "thumbnail_";
	public static String postfix = ".jpg";
		
	/**
	 * result the File savePath
	 * null the file do not loaded*/
	public static String getLoadFilePath(String saveType,String profileId,boolean isThumbnail){		
		String fileSaveName = profileId+postfix;
		if (isThumbnail) {
			fileSaveName = thumbnail+fileSaveName;
		}
		String loadFilepath = tempSaveFolder+saveType;		
		File localResourceFile = new File(loadFilepath, fileSaveName);
		if (localResourceFile.exists()) {
			return localResourceFile.getAbsolutePath();
		}else {
			return null;
		}
	}

	public static String getLoadFilePath(String saveType,String profileId,boolean isThumbnail,int refreshCount,int refreshSucCount){				
		String path = null;
		for (int i = refreshCount; i >= refreshSucCount; i--) {
			String fileSaveName = null;
			if (i > 0) {
				String count = "_"+String.valueOf(i);
				fileSaveName = profileId+ count + postfix;
			}else {
				fileSaveName = profileId + postfix;
			}
			if (isThumbnail) {
				fileSaveName = thumbnail+fileSaveName;
			}
			String loadFilepath = tempSaveFolder+saveType;		
			File localResourceFile = new File(loadFilepath, fileSaveName);
			if (localResourceFile.exists()) {
				path = localResourceFile.getAbsolutePath();
				break;
			}			
		}
		return path;
	}
		
	public static double getTotalSize(File file) {	
		double total = 0;
		if (file.exists()) {
			if (file.isFile()){
				total += file.length();
			}else {
				File[] children = file.listFiles();      
				if (children != null) {
					for (File child : children){
						total += getTotalSize(child);
					}  
				}				   
			} 
		}				  
		return total;
    }
	
	//maxSize MB
	public static void isExternalExceed(Context context,double maxSize) {
		if (context == null) return;
		if (maxSize == 0) return;					
		File file = new File(FilePathConstant.tempFolder,"."+ externalType);
		double totalSize = FilePathConstant.getTotalSize(file);
		if (totalSize > maxSize) {
			LoaderUtil.clearCaches(context,"."+externalType);
		} 		
	}		
	
}
