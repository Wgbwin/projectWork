package com.kodak.rss.tablet.thread;

import java.util.List;

import android.os.Handler;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.webservice.WebService;

public class ToolRedEyeEditTask implements Runnable{

	private List<ImageInfo> chosenList;
	private Handler editHandler;
	private WebService service = null;	
			
	public ToolRedEyeEditTask(List<ImageInfo> chosenList, Handler editHandler,WebService service) {
		super();
		this.chosenList = chosenList;
		this.editHandler = editHandler;
		this.service = service;
	}

	@Override
	public void run() {
		ImageInfo imageInfo = null;
		for(int i = 0; i < chosenList.size();i++){					
			if (chosenList.get(i).isCurrentChosen) {	
				imageInfo = chosenList.get(i);
			}
		}							
		Object[] array = new Object[2];
		array[0] = imageInfo;
		editHandler.obtainMessage(0, array).sendToTarget();															
		if (imageInfo.imageOriginalResource != null) {	
			boolean succeed;
			try {
				if (imageInfo.isUseRedEye){
					service.setAutoRedEyeTask(imageInfo.imageOriginalResource.id, false);								
				}else {
					service.setAutoRedEyeTask(imageInfo.imageOriginalResource.id, true);
				}
				succeed = true;
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				succeed = false;
			}
			if (succeed) {
				imageInfo.isUseRedEye = !imageInfo.isUseRedEye;
			}
			array[1] = succeed;
			editHandler.obtainMessage(1, array).sendToTarget();		
		}
	}

}
