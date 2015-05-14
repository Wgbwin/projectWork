package com.kodak.kodak_kioskconnect_n2r.collage;

import java.util.List;

import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.utils.ImageResources;

public class LoadColorEffectSourcesTask extends Thread{
	
	private static final String TAG = "LoadColorEffectSourcesTask :";
	private List<ColorEffect> colorEffectList =null;;
	

	public LoadColorEffectSourcesTask(List<ColorEffect> colorEffectList) {
		super();
		this.colorEffectList = colorEffectList;		
	}

	@Override
	public void run() {		
		if (colorEffectList!= null && !colorEffectList.isEmpty()) {	
			for (int i = 0; i < colorEffectList.size(); i++) {
				ColorEffect ce = colorEffectList.get(i);
				if (ce != null) {																						
					try {						
						ImageResources.downloadPic(ce.name,ce.glyphPathUrl.replaceAll(" ", "%20"));														
					} catch (Exception e) {						
						Log.e(TAG, e.toString());
					}												
				}	
			}				
		}				
	}

}
