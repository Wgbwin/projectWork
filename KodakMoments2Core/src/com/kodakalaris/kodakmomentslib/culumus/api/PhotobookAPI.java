package com.kodakalaris.kodakmomentslib.culumus.api;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.culumus.bean.content.ServerPhoto;
import com.kodakalaris.kodakmomentslib.culumus.bean.photobook.Photobook;
import com.kodakalaris.kodakmomentslib.culumus.bean.photobook.PhotobookPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.text.TextBlock;
import com.kodakalaris.kodakmomentslib.culumus.parse.PhotobookParse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.util.URLUtils;

public class PhotobookAPI extends GeneralAPI {
	private PhotobookParse mPhotobookParse;
	
	public static final String FUNC_MOVE_TO_TOP = "moveToTop";
	public static final String FUNC_MOVE_TO_BOTTOM = "moveToBottom";
	public static final String FUNC_MOVE_DOWN = "moveDown";
	public static final String FUNC_MOVE_UP = "moveUp";

	public PhotobookAPI(Context context) {
		super(context);
		mPhotobookParse = new PhotobookParse();
	}
	
	/**
	 * 
	 * @param bookId
	 * 			the id of created Photobook
	 * @param imageContentIds
	 * 			the content id of uploaded images
	 * @return
	 * 			true is adding photos to book successfully, otherwise failed.
	 * @throws WebAPIException 
	 */
	public void addPhotosToBookTask(String bookId, List<String> imageContentIds) throws WebAPIException{
		int count = 0;
		boolean succeed = false;
		String url = photobookServiceURL + "/" + bookId + "/photos";
		JSONArray postArray = new JSONArray();
		for(String contentId : imageContentIds){
			try {
				JSONObject postData = new JSONObject("{\"SourceImageId\":\"" + contentId + "\"}");
				postArray.put(postData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPostTask(url, postArray.toString(), "addPhotosToBookTask");
				mPhotobookParse.checkError(result);
				succeed = true;
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				id of current Photobook
	 * @param index
	 * 				the place you want to insert into
	 * @param resourceId
	 * 				content ids of images, right now can just set null
	 * @return
	 * 				a new layout Photobook
	 * @throws WebAPIException 
	 */
	public Photobook addPageToPhotobookTask(String photobookId, int index, List<String> resourceId) throws WebAPIException{
		String url = photobookServiceURL + "/" + photobookId + "/insert?at=" + index;
		int count = 0;
		Photobook book = null;
		while(book==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "[[]]", "addPageToPhotobookTask");
				book = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count + 1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return book;
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				the created photobook's id
	 * @param page
	 * 				the PhotobookPage which you want to insert image
	 * @param imageContentId
	 * 				the content id of which image that will be inserted
	 * @return
	 * 				a new Photobook structure
	 * @throws WebAPIException 
	 */
	public PhotobookPage addImageToPageTask(String photobookId, PhotobookPage page, String imageContentId) throws WebAPIException{
		int count = 0;
		PhotobookPage tempPage = null;
		String url = photobookServiceURL + "/" + photobookId + "/insert-photos-on-page?pageId=" + page.id;
		JSONArray jsArray = new JSONArray();
		JSONObject jsPost = new JSONObject();
		if(page.pageType.equals(PhotobookPage.TYPE_TITLE)){
			try {
				jsPost.put("SourceImageId", imageContentId);
				jsPost.put("ImageHoleIndex", 0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				jsPost.put("SourceImageId", imageContentId);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		jsArray.put(jsPost);
		String postString = jsArray.toString();
		while(tempPage==null && count<connTryTimes){
			try{
				String result = httpPostTask(url, postString, "addImageToPageTask");
				tempPage = mPhotobookParse.parsePhotobookPage(result);
			}catch(WebAPIException e){
				if(count + 1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return tempPage;
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				id of current Photobook
	 * @param themeId
	 * 				theme id
	 * @param imageContentIds
	 * 				content id of selected images
	 * @return
	 * 				a new Photobook
	 * @throws WebAPIException 
	 */
	public Photobook buildPhotobookTask(String photobookId, String themeId, List<String> imageContentIds) throws WebAPIException{
		String url = photobookServiceURL + "/" + photobookId + "/build";
		if(themeId!=null || !"".equals(themeId)){
			url += "?themeId=" + themeId;
		}
		String postData = "[";
		for(int i=0; i<imageContentIds.size(); i++){
			if(i!=imageContentIds.size()-1){
				postData += "\""+imageContentIds.get(i) +"\""+ ",";
			} else {
				postData += "\""+imageContentIds.get(i) +"\""+ "]";
			}
		}
		Photobook book = null;
		int count = 0;
		while(book==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, postData, "buildPhotobookTask");
				book = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return book;
	}
	
	/**
	 * 
	 * @param pageId 
	 * 				id of edited page
	 * @param functionName
	 * 				{@value FUNC_MOVE_TO_TOP, FUNC_MOVE_TO_BOTTOM, FUNC_MOVE_DOWN, FUNC_MOVE_UP}
	 * @param imageContentId
	 * 				content id of edited image
	 * @return
	 * @throws WebAPIException 
	 */
	public PhotobookPage changeZOrderTask(String pageId, String functionName, String imageContentId) throws WebAPIException{
		String url = photobookServiceURL + "/pages/"+pageId+"/"+functionName+"?contentId="+imageContentId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param productType
	 * 				it is product description id
	 * @return
	 * 				the created Photobook, null is failed.
	 * @throws WebAPIException 
	 */
	public Photobook createPhotobookTask(String productType) throws WebAPIException {
		int count = 0;
		Photobook photobook = null;
		String url = photobookServiceURL;
		JSONObject jsPost = new JSONObject();
		try {
			jsPost.put("ProductType", productType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while (photobook == null && count < connTryTimes) {
			try {
				String result = httpPostTask(url, jsPost.toString(), "createPhotobookTask");
				photobook = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}

		return photobook;
	}
	
	/**
	 * Remove the page from Photobook.
	 * @param bookId id of the Photobook.
	 * @param pageId id of the page which you want to delete.
	 * @return the new structure of Photobook.
	 * @throws WebAPIException 
	 */
	public Photobook deletePhotobookPageTask(String bookId, String pageId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/delete-page/?pageId=" + pageId;
		int count = 0;
		Photobook book = null;
		while(book==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "deletePhotobookPageTask");
				book = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return book;
	}
	
	/**
	 * Get structure of Photobook.
	 * @param bookId
	 * 			id of the Photobook which you want to get.
	 * @return
	 * 			Photobook. if null means failed.
	 * @throws WebAPIException 
	 */
	public Photobook getPhotobookTask(String bookId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId;
		int count = 0;
		Photobook book = null;
		while(book==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getPhotobookTask");
				book = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return book;
	}
	
	/**
	 * 
	 * @param bookId
	 * @return
	 * @throws WebAPIException
	 */
	public List<ServerPhoto> getPhotosInPhotobookTask(String bookId) throws WebAPIException {
		String url = photobookServiceURL + "/" + bookId + "/photos";
		int count = 0;
		List<ServerPhoto> photos = null;
		while(photos==null && count<connTryTimes){
			try{
				String result = httpGetTask(url, "getPhotosInPhotobookTask");
				photos = mPhotobookParse.parseServerPhotos(result);
			} catch(WebAPIException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return photos;
	}
	
	/**
	 * 
	 * @param pageId
	 * @return
	 * @throws WebAPIException 
	 */
	public TextBlock getTextBlockForPageTask(String pageId) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/textblock";
		int count = 0;
		TextBlock textBlock = null;
		while(textBlock==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getTextBlockForPageTask");
				textBlock = mPhotobookParse.parseTextBlock(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return textBlock;
	}
	
	/**
	 * Insert image into Photobook page.
	 * @param bookId Photobook id.
	 * @param pageId Photobook page id.
	 * @param contentIds ID of images which you want to insert into the page.
	 * @return
	 * 		New page which contains the inserted image.
	 * @throws WebAPIException 
	 */	
	public PhotobookPage insertContentTask(String bookId, String pageId, List<String> contentIds) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/insert-content-on-page?pageId=" + pageId;
		JSONArray jsonArr = new JSONArray();
		for(String contentId : contentIds){
			jsonArr.put(contentId);
		}
		
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsonArr.toString(), "insertContentTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * Insert one page which images at the position of pageId. 
	 * @param bookId Photobook id.
	 * @param previousPageId Photobook page id.
	 * @param contentIds image content ids.
	 * @return
	 * 		A list contains new Photobook pages.
	 * * changed code on List<PhotobookPage> to Photobook by bing wang on 2014-2-24 for parse the result 
	 * @throws WebAPIException 
	 */
	public Photobook insertPageWithContent2Task(String bookId, String previousPageId, List<String> contentIds) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/insert-pages2?at=" + previousPageId;
		JSONArray jsArr1 = new JSONArray();
		JSONArray jsArr2 = new JSONArray();
		for(String contentId : contentIds){
			jsArr2.put(contentId);
		}
		jsArr1.put(jsArr2);
		
		int count = 0;
		Photobook photobook = null;
		while(photobook==null&& count<connTryTimes){
			try {
				String result = httpPostTask(url, jsArr1.toString(), "insertPageWithContent2Task");
				photobook = mPhotobookParse.parsePhotoBook(result);			
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return photobook;
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				the created Photobook's id
	 * @return
	 * 				the layout Photobook
	 * @throws WebAPIException 
	 */
	public Photobook layoutPhotobookTask(String photobookId) throws WebAPIException{
		String url = photobookServiceURL + "/" + photobookId + "/layout";
		int count = 0;
		Photobook photobook = null;
		while(photobook==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "layoutPhotobookTask");
				photobook = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return photobook;
	}
	
	/**
	 * Move image from one page to another.
	 * @param bookId Photobook id.
	 * @param fromPageId Id of Photobook page which contains the image.
	 * @param toPageId Id of Photobook page which you want to insert the image to.
	 * @param contentId Content id of the image which you want to remove.
	 * @return
	 * 		A list contains new Photobook pages.
	 * @throws WebAPIException 
	 */
	public List<PhotobookPage> moveContentTask(String bookId, String fromPageId, String toPageId, String contentId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/move-content?frompage=" + fromPageId + "&topage=" + toPageId + "&content=" + contentId;
		int count = 0;
		List<PhotobookPage> pages = null;
		while((pages==null||pages.isEmpty()) && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "moveContentTask");
				pages = mPhotobookParse.parsePhotobookPages(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return pages;
	}
	
	/**
	 * Move Photobook page from its position to the position of toPage.
	 * @param bookId Photobook id.
	 * @param fromPageId Id of Photobook page which you want to move.
	 * @param toPageId Id of Photobook page which you want to move to.
	 * @return 
	 * 		A list contains new Photobook pages.
	 * changed code on List<PhotobookPage> to Photobook by bing wang on 2014-2-24 for parse the result 
	 * @throws WebAPIException 
	 */
	public Photobook movePage2Task(String bookId, String fromPageId, String toPageId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/move-page2?from=" + fromPageId + "&to=" + toPageId;
		int count = 0;
		Photobook photobook = null;
		while(photobook==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "movePage2Task");
				photobook = mPhotobookParse.parsePhotoBook(result);			
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return photobook;
	}
	
	/**
	 * Re-layout Photobook page.
	 * @param pageId Photobook page id.
	 * @return
	 * 		New structure of Photobook page.
	 * @throws WebAPIException 
	 */
	public PhotobookPage layoutPageTask(String pageId) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/layout?sticky=true";
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "layoutPageTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				id of current Photobook
	 * @param imageId
	 * 				content id of the image that you want to delete
	 * @return
	 * @throws WebAPIException 
	 */
	public PhotobookPage removePhotoFromPageTask(String photobookId, String imageId) throws WebAPIException{
		String url = photobookServiceURL + "/"+photobookId+"/remove-from-page?image="+imageId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * Remove image from Photobook.
	 * @param bookId Photobook id.
	 * @param contentId Image content id.
	 * @return
	 * @throws WebAPIException 
	 * 	
	 */
	public PhotobookPage removeContentFromBookTask(String bookId, String contentId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/remove-content?content=" + contentId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "removeContentFromBookTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * Remove image from Photobook page.
	 * @param bookId Photobook id.
	 * @param pageId Photobook page id.
	 * @param contentId Image content id.
	 * @return
	 * 		New Photobook page.
	 * @throws WebAPIException 
	 */
	public PhotobookPage removeContentFromPageTask(String bookId, String pageId, String contentId) throws WebAPIException{
		String url = photobookServiceURL + "/" + bookId + "/pages/" + pageId + "/remove-content?contentId=" + contentId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "removeContentFromPageTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * Rotate image which in the page.
	 * @param pageId Photobook page id.
	 * @param contentId Image content id.
	 * @param angle anlge which you want to rotate.
	 * @return
	 * 		true if succeed, otherwise false.
	 * @throws WebAPIException 
	 */
	public PhotobookPage rotateContentTask(String pageId, String contentId, float angle) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/rotate-content?content=" + contentId + "&angle=" + angle;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "rotateContentTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param photobookId
	 * @param author
	 * @param title
	 * @param subtitle
	 * @return
	 * 			Title Page
	 * @throws WebAPIException 
	 */
	public PhotobookPage setAuthorTitleSubtitleTask(String photobookId, String author, String title, String subtitle) throws WebAPIException{
		int count = 0;
		List<PhotobookPage> pages = null;
		String url = photobookServiceURL+"/"+photobookId+"/authortitlesubtitle?author="+URLUtils.formatUrlParam(author)+"&title="+URLUtils.formatUrlParam(title)+"&subtitle="+URLUtils.formatUrlParam(subtitle);
		while(pages==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setAuthorTitleSubtitleTask");
				pages = mPhotobookParse.parsePhotobookPages(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		if(pages != null && pages.size()>0){
			return pages.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 * @param photobookId
	 * @param pageId
	 * @param contentId
	 * @return
	 * @throws WebAPIException 
	 */
	public PhotobookPage setPageBackgroundTask(String photobookId, String pageId, String contentId) throws WebAPIException{
		String url = photobookServiceURL + "/" + photobookId + "/set-page-background?pageId=" + pageId;
		int count = 0;
		PhotobookPage page = null;
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("SourceImageId", contentId==null?"":contentId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsObj.toString(), "setPageBackgroundTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param pageId
	 * @param themeId
	 * @return
	 * @throws WebAPIException 
	 */
	public PhotobookPage setPageBackgroundFromThemeTask(String pageId, String themeId) throws WebAPIException{
		// TODO: have to follow iOS 
		String url = photobookServiceURL + "/pages/" + pageId + "/background?id=" + themeId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setPageBackgroundFromThemeTask");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param pageId
	 * @param themeId is the photobook themeId
	 * @param sourceId  is the selected themeId
	 * @return
	 * @throws WebAPIException 
	 */
	public PhotobookPage setPageBackgroundFromThemeTask2(String pageId, String themeId, String sourceId) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/background2?id=" + sourceId + "&themeId=" + themeId;
		int count = 0;
		PhotobookPage page = null;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setPageBackgroundFromThemeTask2");
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	/**
	 * 
	 * @param photobookId
	 * 				the created Photobook's id
	 * @param themeId
	 * 				id of the theme which you want to use
	 * @return
	 * 				a Photobook of which theme has been changed, null is failed.
	 * @throws WebAPIException 
	 */
	public Photobook setPhotobookThemeTask(String photobookId, String themeId) throws WebAPIException{
		String url = photobookServiceURL + "/" + photobookId + "/theme?themeId=" + themeId;
		int count = 0;
		Photobook photobook = null;
		while(photobook==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setPhotobookThemeTask");
				photobook = mPhotobookParse.parsePhotoBook(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		
		return photobook;
	}
	
	public void showCaptionsTask(String pageId, boolean show) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/caption-visibility?image=productedits.&visible=" + (show?"true":"false");
		int count = 0;
		boolean succeed = false;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "showCaptionsTask");
				mPhotobookParse.checkError(result);
				succeed = true;
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	public PhotobookPage swapContentTask(String pageId, String contentId1, String contentId2) throws WebAPIException{
		String url = photobookServiceURL + "/pages/" + pageId + "/swap-content?content1=" + contentId1 + "&content2=" + contentId2;
		int count = 0;
		PhotobookPage page = null;
		while(page == null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "swapContentTask");				
				page = mPhotobookParse.parsePhotobookPage(result);
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}

}
