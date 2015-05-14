package com.kodak.rss.tablet.thread;

import java.util.List;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.util.ImageResources;
import com.kodak.rss.core.util.Log;

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
