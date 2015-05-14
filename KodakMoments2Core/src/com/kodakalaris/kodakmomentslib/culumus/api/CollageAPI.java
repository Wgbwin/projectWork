package com.kodakalaris.kodakmomentslib.culumus.api;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.culumus.bean.collage.Collage;
import com.kodakalaris.kodakmomentslib.culumus.parse.CollageParse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public class CollageAPI extends GeneralAPI{

	private CollageParse mCollageParse;
	
	public CollageAPI(Context context) {
		super(context);
		mCollageParse = new CollageParse();
	}
	
	public Collage createCollageTask(String productId,String themeId,String backgroudId,boolean isPortrait) throws WebAPIException {
		int count = 0;
		Collage collage = null;
		String url = collageURL+ "?productId=" + productId + "&themeId=" + themeId + "&backgroudId="+ backgroudId + "&isPortrait="+isPortrait;
		
		while (collage == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, "", "createCollageTask");				
				collage = mCollageParse.parseCollage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return collage;
	}
	
	

}
