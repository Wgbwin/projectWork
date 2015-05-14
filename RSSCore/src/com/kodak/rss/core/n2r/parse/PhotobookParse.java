package com.kodak.rss.core.n2r.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;

public class PhotobookParse extends Parse {
	
	public Photobook parsePhotoBook(String result) throws RssWebServiceException{
		checkError(result);
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Photobook.FLAG_PHOTOBOOK)){
				JSONObject jsBook = jsObj.getJSONObject(Photobook.FLAG_PHOTOBOOK);
				Photobook book = new Photobook();
				if(jsBook.has(Photobook.FLAG_ID)){
					book.id = jsBook.getString(Photobook.FLAG_ID);
				}
				if(jsBook.has(Photobook.FLAG_PRO_DESC_ID)){
					book.proDescId = jsBook.getString(Photobook.FLAG_PRO_DESC_ID);
				}
				if(jsBook.has(Photobook.FLAG_Theme)){
					book.theme = jsBook.getString(Photobook.FLAG_Theme);
				}
				if(jsBook.has(Photobook.FLAG_Pages)){
					JSONArray jsPages = jsBook.getJSONArray(Photobook.FLAG_Pages);
					book.pages = parsePhotobookPages(jsPages);
				}
				if(jsBook.has(Photobook.FLAG_IsDuplex)){
					book.isDuplex = jsBook.getBoolean(Photobook.FLAG_IsDuplex);
				}
				if(jsBook.has(Photobook.FLAG_MinNumberOfPages)){
					book.minNumberOfPages = jsBook.getInt(Photobook.FLAG_MinNumberOfPages);
				}
				if(jsBook.has(Photobook.FLAG_MaxNumberOfPages)){
					book.maxNumberOfPages = jsBook.getInt(Photobook.FLAG_MaxNumberOfPages);
				}
				if(jsBook.has(Photobook.FLAG_NumberOfPagesPerBaseBook)){
					book.numberOfPagesPerBaseBook = jsBook.getInt(Photobook.FLAG_NumberOfPagesPerBaseBook);
				}
				if(jsBook.has(Photobook.FLAG_MinNumberOfImages)){
					book.minNumberOfImages = jsBook.getInt(Photobook.FLAG_MinNumberOfImages);
				}
				if(jsBook.has(Photobook.FLAG_MaxNumberOfImages)){
					book.maxNumberOfImages = jsBook.getInt(Photobook.FLAG_MaxNumberOfImages);
				}
				if(jsBook.has(Photobook.FLAG_MaxNumberOfImagesPerAddedPage)){
					book.maxNumberOfImagesPerAddedPage = jsBook.getInt(Photobook.FLAG_MaxNumberOfImagesPerAddedPage);
				}
				if(jsBook.has(Photobook.FLAG_MaxNumberOfImagesPerBaseBook)){
					book.maxNumberOfImagesPerBaseBook = jsBook.getInt(Photobook.FLAG_MaxNumberOfImagesPerBaseBook);
				}
				if(jsBook.has(Photobook.FLAG_IdealNumberOfImagesPerBaseBook)){
					book.idealNumberOfImagesPerBaseBook = jsBook.getInt(Photobook.FLAG_IdealNumberOfImagesPerBaseBook);
				}
				if(jsBook.has(Photobook.FLAG_NumberOfImagesInBook)){
					book.numberOfImagesInBook = jsBook.getInt(Photobook.FLAG_NumberOfImagesInBook);
				}
				if(jsBook.has(Photobook.FLAG_NumberOfUnassignedImages)){
					book.numberOfUnassignedImages = jsBook.getInt(Photobook.FLAG_NumberOfUnassignedImages);
				}
				if(jsBook.has(Photobook.FLAG_SuggestedCaptionVisibility)){
					book.suggestedCaptionVisibility = jsBook.getBoolean(Photobook.FLAG_SuggestedCaptionVisibility);
				}

				if(jsBook.has(Photobook.FLAG_SequenceNumberOfIconicPage)){
					book.sequenceNumberOfIconicPage = jsBook.getInt(Photobook.FLAG_SequenceNumberOfIconicPage);
				}

				if(jsBook.has(Photobook.FLAG_CanSetTitle)){
					book.canSetTitle = jsBook.getBoolean(Photobook.FLAG_CanSetTitle);
				}

				if(jsBook.has(Photobook.FLAG_CanSetSubtitle)){
					book.canSetSubtitle = jsBook.getBoolean(Photobook.FLAG_CanSetSubtitle);
				}

				if(jsBook.has(Photobook.FLAG_CanSetAuthor)){
					book.canSetAuthor = jsBook.getBoolean(Photobook.FLAG_CanSetAuthor);
				}
				
				if(jsBook.has(Photobook.FLAG_Title)){
					book.title = jsBook.getString(Photobook.FLAG_Title);
				}
				
				if(jsBook.has(Photobook.FLAG_Subtitle)){
					book.subTitle = jsBook.getString(Photobook.FLAG_Subtitle);
				}
				
				if(jsBook.has(Photobook.FLAG_Author)){
					book.author = jsBook.getString(Photobook.FLAG_Author);
				}
				return book;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<PhotobookPage> parsePhotobookPages(String result) throws RssWebServiceException{
		checkError(result);
		List<PhotobookPage> pages = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(Photobook.FLAG_Pages)){
				JSONArray jsPages = jsObj.getJSONArray(Photobook.FLAG_Pages);
				pages = parsePhotobookPages(jsPages);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pages;
	}
	
	private List<PhotobookPage> parsePhotobookPages(JSONArray jsPages){
		List<PhotobookPage> pages = new ArrayList<PhotobookPage>();
		for(int i=0; i<jsPages.length(); i++){
			try {
				PhotobookPage page = parsePhotobookPage(jsPages.getJSONObject(i));
				pages.add(page);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return pages;
	}
	
	public PhotobookPage parsePhotobookPage(String result) throws RssWebServiceException{
		checkError(result);
		PhotobookPage page = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(PhotobookPage.FLAG_Page)){
				JSONObject jsPage = jsObj.getJSONObject(PhotobookPage.FLAG_Page);
				page = parsePhotobookPage(jsPage);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return page;
	}
	
	private PhotobookPage parsePhotobookPage(JSONObject jsonPage){
		PhotobookPage page = null;
		if(jsonPage!=null){
			try {
				page = new PhotobookPage();
				if(jsonPage.has(PhotobookPage.FLAG_BASE_URI)){
					page.baseURI = jsonPage.getString(PhotobookPage.FLAG_BASE_URI);
				}
				if(jsonPage.has(PhotobookPage.FLAG_ID)){
					page.id = jsonPage.getString(PhotobookPage.FLAG_ID);
				}
				if(jsonPage.has(PhotobookPage.FLAG_SEQUENCE_NUMBER)){
					page.sequenceNumber = jsonPage.getInt(PhotobookPage.FLAG_SEQUENCE_NUMBER);
				}
				if(jsonPage.has(PhotobookPage.FLAG_PageType)){
					page.pageType = jsonPage.getString(PhotobookPage.FLAG_PageType);
				}
				if(jsonPage.has(PhotobookPage.FLAG_LayoutType)){
					page.layoutType = jsonPage.getString(PhotobookPage.FLAG_LayoutType);
				}
				if(jsonPage.has(PhotobookPage.FLAG_WIDTH)){
					page.width = (float) jsonPage.getDouble(PhotobookPage.FLAG_WIDTH);
				}
				if(jsonPage.has(PhotobookPage.FLAG_HEIGHT)){
					page.height = (float) jsonPage.getDouble(PhotobookPage.FLAG_HEIGHT);
				}
				if(jsonPage.has(PhotobookPage.FLAG_MIN_NUM_OF_IMAGES)){
					page.minNumberOfImages = jsonPage.getString(PhotobookPage.FLAG_MIN_NUM_OF_IMAGES);
				}
				if(jsonPage.has(PhotobookPage.FLAG_MAX_NUM_OF_IMAGES)){
					page.maxNumberOfImages = jsonPage.getString(PhotobookPage.FLAG_MAX_NUM_OF_IMAGES);
				}
				if(jsonPage.has(PhotobookPage.FLAG_LAYERS)){
					page.layers = parseLayers(jsonPage.getJSONArray(PhotobookPage.FLAG_LAYERS),Integer.valueOf(page.maxNumberOfImages));
				}
				if(jsonPage.has(PhotobookPage.FLAG_MARGIN)){
					JSONObject jsonMargin = jsonPage.getJSONObject(PhotobookPage.FLAG_MARGIN);
					if(jsonMargin.has(PhotobookPage.FLAG_MARGIN_TOP)){
						page.margin[0] = (float) jsonMargin.getDouble(PhotobookPage.FLAG_MARGIN_TOP);
					}
					if(jsonMargin.has(PhotobookPage.FLAG_MARGIN_LEFT)){
						page.margin[1] = (float) jsonMargin.getDouble(PhotobookPage.FLAG_MARGIN_LEFT);
					}
					if(jsonMargin.has(PhotobookPage.FLAG_MARGIN_BOTTOM)){
						page.margin[2] = (float) jsonMargin.getDouble(PhotobookPage.FLAG_MARGIN_BOTTOM);
					}
					if(jsonMargin.has(PhotobookPage.FLAG_MARGIN_RIGHT)){
						page.margin[3] = (float) jsonMargin.getDouble(PhotobookPage.FLAG_MARGIN_RIGHT);
					}
				}
				if(jsonPage.has(PhotobookPage.FLAG_BackgroundImageBaseURI)){
					page.backgroundImageBaseURI = jsonPage.getString(PhotobookPage.FLAG_BackgroundImageBaseURI);
				}
				if(jsonPage.has(PhotobookPage.FLAG_BackgroundImageId)){
					page.backgroundImageId = jsonPage.getString(PhotobookPage.FLAG_BackgroundImageId);
				}
				if(jsonPage.has(PhotobookPage.FLAG_ThemeBackground)){
					page.themeBackGround = jsonPage.getString(PhotobookPage.FLAG_ThemeBackground);
				}
			} catch(JSONException je){
				je.printStackTrace();
			}
		}
		return page;
	}
}
