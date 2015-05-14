package com.kodak.rss.core.util;

import java.io.IOException;
import java.io.RandomAccessFile;

public class JudgeImageFileTypeUtil {

	private static byte[] getByte(String filePath, int haveNum){
		byte buffer[] = new byte[24];
		RandomAccessFile af = null;
		try {
			af = new RandomAccessFile(filePath,"r");			
			af.read(buffer,0,haveNum);						
		} catch (IOException ioe) {
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (af != null) {
				try {
					af.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}			
		}
		return buffer;
	}

	private static boolean isWebP(byte[] data) {
        return data != null && data.length > 12 && data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
    }
	
	public static boolean isFilter(String filePath){
		boolean isFilter = false;
		if (filePath == null) return isFilter;
		byte[] data = getByte(filePath, 16);
		isFilter = isWebP(data);	
        return isFilter;
	}
	
}
