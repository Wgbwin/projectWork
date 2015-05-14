package com.kodak.rss.core.n2r.webservice;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;
import com.kodak.rss.core.n2r.parse.CollageParse;

//set alternates is true now,because don't known alternates display  
public class CollageWebService extends WebService{

	private CollageParse mCollageParse;
	
	public CollageWebService(Context context) {
		super(context);
		mCollageParse = new CollageParse();
	}
	
	public Collage createCollageTask(String productId,String themeId,String backgroudId,boolean isPortrait) throws RssWebServiceException {
		int count = 0;
		Collage collage = null;
		String url = collageURL + "?productId=" + productId + "&themeId=" + themeId + "&backgroudId="+ backgroudId + "&isPortrait="+isPortrait;
		
		while (collage == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, "", "createCollageTask");				
				collage = mCollageParse.parseCollage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return collage;
	}
	
	public Collage getCollageTask(String collageId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		Collage collage = null;
		String url = collageURL + "/" + collageId + "?alternates="+ isAlternates;

		while (collage == null && count < connTryTimes) {
			try {
				String result = httpGetTask(url, "getCollageTask");
				collage = mCollageParse.parseCollage(result);				
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return collage;
	}
	
	// The same as iOS task: ChangeZOrderCollageImageTask
	public CollagePage moveToTopTask(String pageId, String contentId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/moveToTop?contentId=" + contentId + "&alternates="+ isAlternates;
				
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "moveToTopTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}
			
	public CollagePage insertContentTask(String pageId, List<String> contents, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;		
		String url = collageURL + "/page/" + pageId + "/insert-content?alternates="+ isAlternates;
		JSONArray jsPostData = new JSONArray();
		for (String contentId : contents) {
			JSONObject jsContent = new JSONObject();
			try {
				jsContent.put("ContentId", contentId);
				jsContent.put("LayerIndex", 0);
				jsPostData.put(jsContent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		String postData = jsPostData.toString();
		
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, postData, "insertContentTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public CollagePage setCollagePageLayoutTask(String pageId, String layoutId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;	
		String url = "";
		if (!layoutId.equals("")) {
			url = collageURL + "/page/" + pageId + "/set-layout?layoutTitle=" + layoutId + "&alternates="+isAlternates;
		} else {
			url = collageURL + "/page/" + pageId + "/layout?alternates=false";
		}	
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "setCollagePageLayoutTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public CollagePage removeCollageContentTask(String pageId, String contentId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/remove-content?alternates="+ isAlternates;
		JSONArray jsPostData = new JSONArray();
		jsPostData.put(contentId);
		String postData = jsPostData.toString();
			
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, postData, "removeCollageContentTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public Collage rotateCollageTask(String collageId, boolean isPortrait, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		Collage collage = null;
		String url = collageURL+"/" + collageId + "/orientation?isPortrait=" + isPortrait + "&alternates="+ isAlternates;
		
		while (collage == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "rotateCollageTask");
				collage = mCollageParse.parseCollage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return collage;
	}

	public CollagePage rotateCollageContentTask(String pageId, String contentId, float angle, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/rotate-content?contentId=" + contentId + "&angle=" + angle + "&alternates="+ isAlternates;
		
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, "", "rotateCollageContentTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public CollagePage setCollageBackgroundImageTask(String pageId, String imageId, float opacity) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/background-image?imageId=" + imageId + "&opacity=" + opacity;
			
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "setCollageBackgroundImageTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public Collage setCollageThemeTask(String collageId, String themeId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		Collage collage = null;		
		String url = collageURL + "/" + collageId + "/theme?themeId=" + themeId + "&alternates="+ isAlternates;
		
		while (collage == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "setCollageThemeTask");
				collage = mCollageParse.parseCollage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return collage;
	}

	public CollagePage shuffleCollageTask(String pageId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/shuffle-content?alternates="+ isAlternates;
		
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, "", "shuffleCollageTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}
	
	public CollagePage layoutCollagePageTask(String pageId, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/layout?alternates="+ isAlternates;
		
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "LayoutCollagePageTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}

	public CollagePage swapCollageContentTask(String pageId, String contentId1, String contentId2, boolean isAlternates) throws RssWebServiceException {
		int count = 0;
		CollagePage page = null;
		String url = collageURL + "/page/" + pageId + "/swap-content?contentId1=" + contentId1 + "&contentId2=" + contentId2 + "&alternates="+ isAlternates;
			
		while (page == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, "", "swapCollageContentTask");
				page = mCollageParse.parseCollagePage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return page;
	}		
}
