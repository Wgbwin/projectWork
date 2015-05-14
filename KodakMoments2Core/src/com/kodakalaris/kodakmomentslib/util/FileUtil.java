package com.kodakalaris.kodakmomentslib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
	public static boolean createDirectoryIfNotExist(String dir){
		File file = new File(dir);
		return file.mkdirs();
	}
	
	public static String readTxt(String filePath) {
		StringBuilder sb = new StringBuilder();
		InputStreamReader read = null;
		try {  
            File file = new File(filePath);  
            if (file.isFile() && file.exists()) {  
               read = new InputStreamReader(new FileInputStream(file));  
               BufferedReader br = new BufferedReader(read);  
               String line = null;  
               while ((line = br.readLine()) != null) {  
                   sb.append(line);
                   sb.append("\n");
               }
               
               read.close();  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	if (read != null) {
        		try {
					read.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
		
		return sb.toString();
	}
	
	public static void writeTxt(String txt, String filePath) {
		writeTxt(txt, filePath, false);
	}
	
	public static void writeTxt(String txt, String filePath, boolean append) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(filePath, append);
			fw.write(txt,0,txt.length());    
			fw.flush(); 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean renameTo(String oldPath, String newPath, boolean relpaceIfExist) {
		File oldFile = new File(oldPath);
		if (!oldFile.exists()) {
			return false;
		}
		
		File newFile = new File(newPath);
		if (newFile.exists() && relpaceIfExist) {
			if (newFile.isFile() && oldFile.isFile()) {
				newFile.delete();
			} else if (newFile.isDirectory() && oldFile.isDirectory()) {
				deleteDirectory(newPath);
			}
		}
		
		return oldFile.renameTo(newFile);
		
	}
	
	public static boolean renameTo(String oldPath, String newPath) {
		return renameTo(oldPath, newPath, false);
	}
	
	public static boolean deleteFileIfExist(String path) {
		File file = new File(path);
		if (file.isFile()) {
			return file.delete();
		}
		
		return false;
	}
	
	
}
