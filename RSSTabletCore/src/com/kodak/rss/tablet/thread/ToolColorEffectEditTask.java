package com.kodak.rss.tablet.thread;

import java.util.List;

import android.os.Handler;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.webservice.WebService;

public class ToolColorEffectEditTask implements Runnable{

	private ImageInfo imageInfo;
	private Handler editHandler;
	private WebService service = null;
	private int colorIndex;
	private List<ColorEffect> colorEffectList;
			
	public ToolColorEffectEditTask(ImageInfo imageInfo, Handler editHandler,WebService service,List<ColorEffect> colorEffectList,int index) {
		super();
		this.imageInfo = imageInfo;
		this.editHandler = editHandler;
		this.service = service;
		this.colorEffectList = colorEffectList;
		this.colorIndex = index;
	}

	@Override
	public void run() {								
		Object[] array = new Object[2];
		array[0] = imageInfo;															
		if (imageInfo.imageOriginalResource != null) {	
			boolean succeed = false;
			try {
				service.setColorEffectTask(imageInfo.imageOriginalResource.id, colorIndex);
				succeed = true;
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			array[1] = succeed;
			if (succeed) {
				imageInfo.colorEffects = colorEffectList.get(colorIndex);
			}
			editHandler.obtainMessage(1, array).sendToTarget();													
		}					
	}

}
