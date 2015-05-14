package com.kodak.rss.tablet.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.webservice.CollageWebService;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;

public class DealImagesToCollagePageTaskPo implements Serializable{

	private static final long serialVersionUID = 1L;

	public String pageId;
	public boolean isAdd;
	public List<ImageInfo> infoList;
	public boolean isInUse = false;		

	public DealImagesToCollagePageTaskPo(String pageId, ImageInfo imageInfo, boolean isAdd) {		
		this.pageId = pageId;
		this.isInUse = false;
		this.isAdd = isAdd;
		if (imageInfo == null) return;			
		if (infoList == null) {
			infoList = new ArrayList<ImageInfo>();
		}			
		infoList.add(imageInfo);											
	}
	
	public void dealImageInfo(ImageInfo imageInfo) {		
		if (imageInfo == null) return;	
		if (infoList == null) {
			infoList = new ArrayList<ImageInfo>();
		}			
		infoList.add(imageInfo);		
	}	

	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object run(CollageWebService mService) {
		this.isInUse = true;		
		return runForDealImagestoCollagePage(mService);		
	}	
	
	/**return result is maybe ImageInfo boolean RssWebServiceException*/
	public Object runForDealImagestoCollagePage(CollageWebService mService) {	
		if (infoList == null) return true;						
		int size = infoList.size();
		if (size == 0) return true;	
		
		if (isAdd) {
			boolean isUploadAll = true;	
			List<ImageInfo> addInfoList = CollageUtil.getAddList(infoList);
			size = addInfoList.size();
			if (size == 0) return true;	
			int sucNum = UploadProgressUtil.getUploadPicSuccessNum(addInfoList,false);
			ImageInfo failInfo = null;
			while (size > sucNum) {	
				failInfo = UploadProgressUtil.getUploadPicFailImageInfo(addInfoList,false);
				if (failInfo != null ) {						
					isUploadAll = false;
					break;
				}																
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {}
				addInfoList = CollageUtil.getAddList(infoList);
				size = addInfoList.size();
				sucNum = UploadProgressUtil.getUploadPicSuccessNum(addInfoList,false);
			}	
			if (!isUploadAll) return failInfo;	
			
			if (addInfoList == null || addInfoList.size() == 0) return true;			
			List<String> contentIds = new ArrayList<String>(2);
			for (ImageInfo imageInfo : addInfoList) {			
				contentIds.add(imageInfo.imageOriginalResource.id);
			}
			if (contentIds.size() == 0) return true;
			
			CollagePage newPage = null;
			try {
				newPage = mService.insertContentTask(pageId, contentIds,true);
				if (newPage != null) {
					CollageUtil.updatePageInCollage(newPage, true, true);	
				}		
			} catch (RssWebServiceException e) {
				e.printStackTrace();
				return e;
			}			
		}else {			
			for (ImageInfo info : infoList) {
				String contentId = CollageUtil.getContentId(info);
				if (contentId == null) continue;
				CollagePage newPage = null;
				try {
					newPage = mService.removeCollageContentTask(pageId, contentId,true);
					if (newPage != null) {
						CollageUtil.deleteImageInfo(Layer.TYPE_IMAGE,contentId);
						CollageUtil.updatePageInCollage(newPage, true, true);	
					}		
				} catch (RssWebServiceException e) {
					e.printStackTrace();
					return e;
				}
			}
		}
		return true;
	}	

}
