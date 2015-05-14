package com.kodak.rss.tablet.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.calendar.Calendar;
import com.kodak.rss.core.n2r.bean.calendar.CalendarGridItemPO;
import com.kodak.rss.core.n2r.bean.calendar.CalendarPage;
import com.kodak.rss.core.n2r.webservice.CalendarWebService;
import com.kodak.rss.tablet.util.CalendarUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;

public class AddImagesToPageTaskPo implements Serializable{

	private static final long serialVersionUID = 1L;

	public String pageId;	
	public List<ImageInfo> addImageInfoList;
	public List<CalendarGridItemPO> gridItemList;	
	public boolean isInUse = false;
	
	public ImageInfo mInfo; //for PageLayer
	public int holeIndex = -1; //for PageLayer

	public AddImagesToPageTaskPo(String pageId,ImageInfo imageInfo,int holeIndex) {		
		this.pageId = pageId;
		this.isInUse = false;
		this.mInfo = imageInfo;
		this.holeIndex = holeIndex;
	}		
	
	public AddImagesToPageTaskPo(String pageId,ImageInfo imageInfo,CalendarGridItemPO gridItemPo) {		
		this.pageId = pageId;
		this.isInUse = false;
		if (gridItemPo != null) {
			if (this.gridItemList == null) {
				gridItemList = new ArrayList<CalendarGridItemPO>();
			}
			int index = getPoPosition(gridItemList, gridItemPo);
			CalendarGridItemPO itemPo = null;
			if (index != -1) {
				itemPo = gridItemList.get(index);
			}else {
				itemPo = gridItemPo;
			}	
			if (itemPo == null) return;							
			if (itemPo.imageInfos == null) {
				itemPo.imageInfos = new ArrayList<ImageInfo>(5);
			}			
			itemPo.imageInfos.add(imageInfo);
			gridItemList.add(itemPo);
			addImageInfoList = null;
		}else {
			if (this.addImageInfoList == null) {
				addImageInfoList = new ArrayList<ImageInfo>();
			}
			addImageInfoList.add(imageInfo);
			gridItemList = null;
		}
	}	
	
	private int getPoPosition(List<CalendarGridItemPO> gridItemList,CalendarGridItemPO itemPo){
		int index = -1;
		if (itemPo == null) return index;
		if (gridItemList == null) return index;
		int size = gridItemList.size();
		if (size == 0) return index;	
		for (int i = 0; i < size; i++) {
			CalendarGridItemPO po = gridItemList.get(i);
			if (po == null) continue;
			if (po.year == itemPo.year && po.month == itemPo.month) {
				if ((po.day != -1 && po.day == itemPo.day) || (po.holdIndex != -1 && po.holdIndex == itemPo.holdIndex)){
					index = i;
					break;
				}				
			}			
		}		
		return index;
	}
	
	public void addImageInfo(ImageInfo imageInfo,CalendarGridItemPO gridItemPo){
		if (imageInfo == null) return;		
		if (gridItemPo != null) {
			if (this.gridItemList == null) {
				gridItemList = new ArrayList<CalendarGridItemPO>();
			}
			int index = getPoPosition(gridItemList, gridItemPo);
			CalendarGridItemPO itemPo = null;
			if (index != -1) {
				itemPo = gridItemList.get(index);
			}else {
				itemPo = gridItemPo;
			}	
			if (itemPo == null) return;							
			if (itemPo.imageInfos == null) {
				itemPo.imageInfos = new ArrayList<ImageInfo>(5);
			}			
			itemPo.imageInfos.add(imageInfo);
			gridItemList.add(itemPo);
		}else {
			if (this.addImageInfoList == null) {
				addImageInfoList = new ArrayList<ImageInfo>();
			}
			addImageInfoList.add(imageInfo);
		}
	}

	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object run(CalendarWebService mService) {
		this.isInUse = true;
		if (addImageInfoList != null) {
			return runForCalendarPage(mService);
		}else if (gridItemList != null) {					
			return runForCalendarGrid(mService);
		}else {
			return runForCalendarPageLayer(mService, mInfo, holeIndex);
		}		
	}	
	
	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object runForCalendarPage(CalendarWebService mService) {				
		if (addImageInfoList == null) return true;						
		int size = addImageInfoList.size();
		if (size == 0) return true;	
		boolean isUploadAll = true;		
		int sucNum = UploadProgressUtil.getUploadPicSuccessNum(addImageInfoList,false);
		ImageInfo failInfo = null;
		while (size > sucNum) {	
			failInfo = UploadProgressUtil.getUploadPicFailImageInfo(addImageInfoList,false);
			if (failInfo != null ) {						
				isUploadAll = false;
				break;
			}																
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {}
			size = addImageInfoList.size();
			sucNum = UploadProgressUtil.getUploadPicSuccessNum(addImageInfoList,false);
		}	
		if (!isUploadAll) return failInfo;	
				
		List<String> contentIds = new ArrayList<String>(2);
		for (ImageInfo imageInfo : addImageInfoList) {			
			contentIds.add(imageInfo.imageOriginalResource.id);
		}
		
		CalendarPage newPage = null;
		try {
			newPage = mService.addContentToCalendarPageTask(pageId, contentIds);
			if (newPage != null) {
				CalendarUtil.updatePageInCalendar(newPage, true, contentIds.size());	
			}		
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return true;
	}	
	
	public List<ImageInfo> getWantAddImageInfos(List<CalendarGridItemPO> gridItemList){
		List<ImageInfo> wantAddImageInfoList = null;
		if (gridItemList == null) return wantAddImageInfoList;
		if (gridItemList.size() == 0) return wantAddImageInfoList;
		wantAddImageInfoList = new ArrayList<ImageInfo>(5);
		for (CalendarGridItemPO gridItemPo : gridItemList) {
			if (gridItemPo == null) continue;
			if (gridItemPo.imageInfos == null) continue;			
			wantAddImageInfoList.addAll(gridItemPo.imageInfos);
		}	
		return wantAddImageInfoList;
	}
		
	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object runForCalendarGrid(CalendarWebService mService) {			
		if (gridItemList == null) return true;	
		Calendar calendar = CalendarUtil.getCurrentCalendar();
		if (calendar == null) return true;	
		int size = gridItemList.size();
		if (size == 0) return true;	
		boolean isUploadAll = true;	
		List<ImageInfo> wantAddImageInfos = getWantAddImageInfos(gridItemList);
		if (wantAddImageInfos == null) return true;		
		int sucNum = UploadProgressUtil.getUploadPicSuccessNum(wantAddImageInfos,false);
		ImageInfo failInfo = null;
		while (size > sucNum) {	
			failInfo = UploadProgressUtil.getUploadPicFailImageInfo(wantAddImageInfos,false);
			if (failInfo != null ) {						
				isUploadAll = false;
				break;
			}																
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {}
			size = wantAddImageInfos.size();
			sucNum = UploadProgressUtil.getUploadPicSuccessNum(wantAddImageInfos,false);
		}	
		if (!isUploadAll) return failInfo;	
						
		
		CalendarPage[] newPages = null;
		try {
			newPages = mService.addContentToCalendarGridsTask(calendar.id, gridItemList);
			if (newPages != null) {
				for (CalendarPage newPage : newPages) {
					if (newPage == null) continue;
					CalendarUtil.updatePageInCalendar(newPage, true);	
				}			
			}		
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return true;
	}	
	
	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object runForCalendarPageLayer(CalendarWebService mService,ImageInfo mInfo,int holeIndex) {			
		if (mInfo == null) return true;	
		if (holeIndex <= -1) return true;				
		boolean isUploadSuccess = false;							
		while (mInfo.imageOriginalResource== null) {	
			if (mInfo.imageOriginalResource == null && mInfo.isHavedOriginalUpload) break;								
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {}				
		}							
		if (mInfo.imageOriginalResource != null) isUploadSuccess = true;				
		if (!isUploadSuccess) return mInfo;							
		String contentId = mInfo.imageOriginalResource.id;
		CalendarPage newPage = null;
		try {
			newPage = mService.insertContentOnPageTask(pageId,holeIndex, contentId);			
			if (newPage != null){
				CalendarUtil.updatePageInCalendar(newPage, true);		
			}												
		} catch (RssWebServiceException e) {
			e.printStackTrace();
			return e;
		}
		return true;
	}	

}
