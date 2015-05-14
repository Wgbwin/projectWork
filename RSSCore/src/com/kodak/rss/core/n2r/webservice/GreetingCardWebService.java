package com.kodak.rss.core.n2r.webservice;

import java.util.List;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.SearchStarterCategory;
import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.bean.greetingcard.GCPage;
import com.kodak.rss.core.n2r.bean.greetingcard.GreetingCard;
import com.kodak.rss.core.n2r.parse.GreetingCardParse;

import android.content.Context;

public class GreetingCardWebService extends WebService{

	private GreetingCardParse mGCParse;
	
	public GreetingCardWebService(Context context) {
		super(context);
		mGCParse = new GreetingCardParse();
	}
	
	/**
	 * getContentSearchStartersByCategory in SOA tool <p>
	 * Used to get Categories
	 * change for RSSMOBILEPDC-1751
	 */
	public List<SearchStarterCategory> getGreetingCardsCategorizedTask(String proDesIds) throws RssWebServiceException {
		String url = contentURL + "/content/contentsearchstartersbycategory?language="+localLanguage+"&countryCode="+countryCode 
		+"&productDescIds="+proDesIds;
		int count = 0;
		List<SearchStarterCategory> searchStarterCategories = null;
		while(searchStarterCategories==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentSearchStartersByCategoryTask");
				searchStarterCategories = mGCParse.parseSearchStarterCategories(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return searchStarterCategories;
	}
	
	/**
	 * getContent in SOA tool <p>
	 * Used to get the themes in each category
	 * @param filters
	 */
	public List<GCCategory> getGreetingCardsForCategoryTask(String appId, String filters) throws RssWebServiceException {
		String url = contentURL + "/content/info?submitterId="+appId+"&language="+localLanguage+"&filters="+filters;
		int count = 0;
		List<GCCategory> contents = null;
		while(contents==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentTask");
				contents = mGCParse.parseGCCategory(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			} 
			count ++;
		}
		return contents;
	}
	
	/**
	 * 
	 * getContentForDesign in SOA tool
	 * @throws RssWebServiceException
	 */
	public List<GCCategory> getGreetingCardForDesignsTask(String designIds, String proDesIds) throws RssWebServiceException {
		String url = contentURL + "/content/designs/members?designType=CreativeDesign&designIds="+designIds+"&productDescriptionIds="+proDesIds+"&language="+localLanguage;
		List<GCCategory> designs = null;
		int count = 0;
		while(designs==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentForDesignsTask");
				designs = mGCParse.parseGCCategory(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return designs;
	}
	
	public GreetingCard createGreetingCardTask(String contentId, String productIdentifier) throws RssWebServiceException {
		String url = greetingCardURL + "/greetingcards"; 
		String postDataString = "{\"Language\":\"" + localLanguage
				+ "\",\"ContentId\": \"" + contentId
				+ "\",\"ProductIdentifier\":\"" + productIdentifier + "\"}";
		GreetingCard card = null;
		int count = 0;
		while(card==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, postDataString, "createGreetingCardTask");
				card = mGCParse.parseGreetingCard(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return card;
	}
	
	public GCPage addImageToCardTask(String pageId, int holeIndex, String contentId) throws RssWebServiceException {
		String url = greetingCardURL + "/greetingcards/pages/"+pageId+"/insert-content?holeIndex="+holeIndex+"&contentId="+contentId;
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "addImageToCardTask");;
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	public GCPage deleteImageFromCardTask(String pageId, String contentId) throws RssWebServiceException {
		String url = greetingCardURL + "/greetingcards/pages/" + pageId + "/remove-content?contentId=" + contentId;
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "deleteImageFromCardTask");
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	public GCPage layoutCardPageTask(String pageId) throws RssWebServiceException {
		String url = greetingCardURL + "/greetingcards/pages/" + pageId + "/layout";
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "layoutCardPageTask");
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (RssWebServiceException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	public String getCardPreviewWithHoleTask(String pageId, int width, int height, int holeIndex){
		return greetingCardURL + "/greetingcards/pages/" + pageId + "/previewwithhole?maxWidth=" + width + "&maxHeight=" + height + "&holeIndex" + holeIndex;
	}

}
