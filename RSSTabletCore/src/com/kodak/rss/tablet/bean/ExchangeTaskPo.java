package com.kodak.rss.tablet.bean;

import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.PhotobookWebService;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

public class ExchangeTaskPo {

	public final static int AddPic = 1;
	public final static int RemovePic = 2;
	public final static int ExchangePic = 3;
	
	public int taskAction;
	
	public PhotobookPage selectPage;
	public Layer selectLayer;
	public int selectLayerPosition;// in selectPage layer[] position
	
	public PhotobookPage toPage;
	public int toLayerPosition;//in toPage layer[] position
		
	public Layer deleteLayer;//for titlePage coverPage		

	public ExchangeTaskPo(int taskAction, PhotobookPage selectPage,Layer selectLayer, int selectLayerPosition, PhotobookPage toPage,
			int toLayerPosition, Layer deleteLayer) {		
		this.taskAction = taskAction;
		this.selectPage = selectPage;
		this.selectLayer = selectLayer;
		this.selectLayerPosition = selectLayerPosition;
		this.toPage = toPage;
		this.toLayerPosition = toLayerPosition;
		this.deleteLayer = deleteLayer;		
	}		

	public Object run(PhotobookWebService pbService, String photoBookId) {					
		ArrayList<PhotobookPage> pageList = new ArrayList<PhotobookPage>();
		if (photoBookId == null) return pageList;
		if (selectLayer == null) return pageList;
		String selectLayerId = selectLayer.contentId;			
		if (selectLayerId == null) return pageList;

		switch (taskAction) {		
		case ExchangeTaskPo.AddPic:
			if (toPage == null ) return pageList;	
			if (selectLayer == null) return pageList;
			
			try{
				if (PhotoBookProductUtil.isTitlePage(toPage) || PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {	
					String delLayerId = null;
					if (deleteLayer != null) {
						delLayerId = deleteLayer.contentId;
					}										
					if (delLayerId != null && !"".equals(delLayerId)) {					
						PhotobookPage newPage = pbService.removePhotoFromPageTask(photoBookId, delLayerId);				
						PhotoBookProductUtil.updatePageData(pageList, newPage, true);													
					}																			
				}			
				PhotobookPage newPage = pbService.addImageToPageTask(photoBookId, toPage, selectLayerId);
				PhotoBookProductUtil.updatePageData(pageList, newPage, true);					
			} catch (RssWebServiceException e1) {				
				e1.printStackTrace();
				return e1;
			}			
			break;
		case ExchangeTaskPo.RemovePic:	
			if (selectPage == null) return pageList;					
			try {
				String selectPageId = selectPage.id;
				PhotobookPage newPage = pbService.removeContentFromPageTask(photoBookId, selectPageId, selectLayerId);
				PhotoBookProductUtil.updatePageData(pageList, newPage, true);		
							
			} catch (RssWebServiceException e1) {				
				e1.printStackTrace();
				return e1;
			}
			break;

		case ExchangeTaskPo.ExchangePic:
			if (toPage == null ) return pageList;	
			if (selectPage == null) return pageList;	
			String selectPageId = selectPage.id;
			String toPageId = toPage.id;
			try{
				if (PhotoBookProductUtil.isTitlePage(toPage) || PhotoBookProductUtil.isBackCoverPageBlank(toPage)) {		
					String delLayerId = null;
					if (deleteLayer != null) {
						delLayerId = deleteLayer.contentId;
					}							
					if (delLayerId != null && !"".equals(delLayerId)) {					
						PhotobookPage newPage = pbService.removePhotoFromPageTask(photoBookId, delLayerId);
						PhotoBookProductUtil.updatePageData(pageList, newPage, true);						
					}
					if (PhotoBookProductUtil.isInlayer(selectLayerId)) {					
						PhotobookPage newFromPage = pbService.removePhotoFromPageTask(photoBookId, selectLayerId);
						PhotoBookProductUtil.updatePageData(pageList, newFromPage, true);						
					}				       	
					PhotobookPage newToPage = null;				
					newToPage = pbService.addImageToPageTask(photoBookId, toPage, selectLayerId);
					PhotoBookProductUtil.updatePageData(pageList, newToPage, true);											
				}else if (PhotoBookProductUtil.getPhotobookPageEditable(toPage)) {									
					List<PhotobookPage> pages = pbService.moveContentTask(photoBookId, selectPageId, toPageId, selectLayerId);
					if (pages != null) {			
						for (int i = 0; i < pages.size(); i++) {
							PhotobookPage newPage = pages.get(i);						
							PhotoBookProductUtil.updatePageData(pageList, newPage, true);	
						}									
					} 
				}
			} catch (RssWebServiceException e1) {				
				e1.printStackTrace();
				return e1;
			}			
			break;
		}		
		return pageList;
	}	
			
}
