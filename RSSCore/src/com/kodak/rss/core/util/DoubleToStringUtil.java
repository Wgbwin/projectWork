package com.kodak.rss.core.util;

import java.math.BigDecimal;

public class DoubleToStringUtil {

	public static String formatDouble(double d,int newScale){
		String bdVStr = null;
		BigDecimal bd = new BigDecimal(d);
		double bdValue = bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
		String bdStr = bdValue + "";
		if (bdStr.contains("E")) {
			String[] bdArray = bdStr.split("E");
			String bdHead = null;
			if (bdArray.length == 2 && !bdArray[1].isEmpty()) {
				if (bdArray[1].startsWith("-")) {
					String numStr = bdArray[1].substring(1);
					int num = Integer.valueOf(numStr);
					bdHead = bdArray[0].substring(0, 1);
					for (int i = 1; i < num; i++) {
						bdHead = "0" + bdHead;
					}
					bdHead = "0." + bdHead + bdArray[0].substring(2);
				} else {
					String numStr = bdArray[1].substring(0);
					int num = Integer.valueOf(numStr);
					int s =  bdArray[0].substring( bdArray[0].lastIndexOf(".") + 1).length();
					String bdEnd = "";
					if (s < num) {
						bdHead = bdArray[0].substring(2, 2+s);
						for (int i = s; i < num; i++) {
							bdHead = bdHead + "0";
						}						
					}else {
						bdHead = bdArray[0].substring(2, num + 2);
						bdEnd = bdArray[0].substring(num + 2);
					}
					bdHead = bdArray[0].substring(0, 1) + bdHead + "." + bdEnd;
				}
			}
			if (bdHead != null) {
				bdVStr = bdHead;
				int s = bdVStr.substring(bdVStr.lastIndexOf(".") + 1).length();	
				if (s <= newScale) {
					for (int i = s; i < newScale; i++) {
						bdVStr = bdVStr + "0";
					}					
				}else {
					bdVStr = bdVStr.substring(0, bdVStr.lastIndexOf("."))+ "." 
						+ bdVStr.substring(bdVStr.lastIndexOf(".") + 1,bdVStr.lastIndexOf(".") + 1 + newScale);	
				}				 
			}
		}
		if (bdVStr == null) {
			bdVStr = bdStr.substring(0, bdStr.lastIndexOf("."))+ "."+ bdStr.substring(bdStr.lastIndexOf(".") + 1);	
		}		
		return bdVStr;
	}			

}
