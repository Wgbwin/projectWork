package com.kodak.utils;

import java.io.File;

/**
 * I/O Utils 
 * @author sunny
 *
 */
public class FileUtils {
	
	
	
	/**
	 * delete all files in the named directory
	 * @param directoryPath
	 */
	public static void deleteAllFilesInDirectory(File directory){
		
		if(!directory.exists() || !directory.isDirectory()){
			return  ;
		}
		File[] fileList = directory.listFiles() ;
		File temp = null ;
		for (int i = 0; i < fileList.length; i++) {
			temp = fileList[i] ;
			if(!temp.isDirectory()){
				temp.delete();
			}else {
				deleteAllFilesInDirectory(temp);
				temp.delete();
			}
		}
		
	}
	
	
	/**
	 * calculate a directory's size
	 */
  public static long calculateDirectDirectorySize(File directory){
	  long totalSize =0 ;
	  if(!directory.exists() || !directory.isDirectory()){
			return  0 ;
		
	  }
	  
	  File[] fileList = directory.listFiles() ;
	  File temp = null ;
	  for (int i = 0; i < fileList.length; i++) {
		temp = fileList[i] ;
		if(!temp.isDirectory()){
			totalSize = totalSize +temp.length() ;
		}else {
			
			totalSize = totalSize +calculateDirectDirectorySize(temp) ; 
			
		}
	 }
	  
	  return totalSize ;
	  
  }
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
