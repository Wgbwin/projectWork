package com.kodak.rss.core.util;

import android.content.Context;

public class DimensionUtil {
	/**  
     * convert from dip to px
     */   
    public static int dip2px(Context context, float dpValue) {   
        final float scale = context.getResources().getDisplayMetrics().density;   
        return (int) (dpValue * scale + 0.5f);   
    }   
   
    /**  
     * convert from px to dip 
     */   
    public static int px2dip(Context context, float pxValue) {   
        final float scale = context.getResources().getDisplayMetrics().density;   
        return (int) (pxValue / scale + 0.5f);   
    } 
}
