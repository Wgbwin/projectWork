package com.kodak.rss.core.util;

import java.io.File;

public class FileUtil {
	/**
	 * get file name from the path
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath){
		if(filePath==null || filePath.length()==0)
			return "";
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}
	
	/**
	 * get file name without suffix from the path
	 * @param filePath
	 * @return
	 */
	public static String getFileNameWithoutSuffix(String filePath){
		String fileName = getFileName(filePath);
		
		if(filePath == null || filePath.length() == 0)
			return "";
		
		int index = fileName.lastIndexOf(".");
		if(index ==-1 || index==0){
			return fileName;
		}else{
			return fileName.substring(0, index);
		}
	}
	
	/**
	 * get the directory full path which contains the file
	 * @param filePath
	 * @return directory full path(not include File.separator at end)
	 */
	public static String getParentDirectoryPath(String filePath){
		if(filePath==null || filePath.length()==0)
			return "";
		
		File file = new File(filePath);
		if(file.isDirectory()){
			filePath = file.getAbsolutePath();
		}
		return filePath.substring(0,filePath.lastIndexOf(File.separator));
	}
	
	/**
	 * delete all files in folder(not include the folder)
	 * @param directory absolute full path
	 */
	public static void deleteAllFilesInFolder(String directory){
		File dir = new File(directory);
		if(dir.isDirectory()){
			File[] files = dir.listFiles();
			for(int i=0,size=files.length;i<size;i++){
				if(files[i].isDirectory()){
					deleteDirectory(files[i].getAbsolutePath());
				}else{
					files[i].delete();
				}
			}
		}
	}
	
	/**
	 * delete the directory including all files and directories in it
	 * @param directory
	 */
	public static void deleteDirectory(String directory){
		File dir = new File(directory);
		if(dir.isDirectory()){
			File[] files = dir.listFiles();
			for(int i=0,size=files.length;i<size;i++){
				if(files[i].isDirectory()){
					deleteDirectory(files[i].getAbsolutePath());
				}else{
					files[i].delete();
				}
			}
			
			dir.delete();
		}
	}
	
	/**
	 * create directory if no exist
	 * @param dir
	 * @return
	 */
	public static boolean createDirIfNotExist(String dir){
		File file = new File(dir);
		if(!file.exists()){
			return file.mkdirs();
		}
		return false;
	}
}
