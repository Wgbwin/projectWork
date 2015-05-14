package com.kodakalaris.kodakmomentslib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class URLUtils {
	public static String formatUrlParam(String param){
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return param;
		}
	}
}
