package com.kodakalaris.kodakmomentslib.culumus.api;

import java.util.List;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.culumus.bean.content.SearchStarterCategory;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GCCategory;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GCPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.greetingcard.GreetingCard;
import com.kodakalaris.kodakmomentslib.culumus.parse.GreetingCardParse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public class GreetingCardAPI extends GeneralAPI{

	private GreetingCardParse mGCParse;
	
	public GreetingCardAPI(Context context) {
		super(context);
		mGCParse = new GreetingCardParse();
	}
	
	/**
	 * getContentSearchStartersByCategory in SOA tool <p>
	 * Used to get Categories
	 * change for RSSMOBILEPDC-1751
	 */
	public List<SearchStarterCategory> getGreetingCardsCategorizedTask(String proDesIds) throws WebAPIException {
		String url = contentURL + "/content/contentsearchstartersbycategory?language="+localLanguage+"&countryCode="+app.getCountryCodeUsed() 
		+"&productDescIds="+proDesIds;
		int count = 0;
		List<SearchStarterCategory> searchStarterCategories = null;
		while(searchStarterCategories==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentSearchStartersByCategoryTask");
				searchStarterCategories = mGCParse.parseSearchStarterCategories(result);				
			} catch (WebAPIException e){
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
	public List<GCCategory> getGreetingCardsForCategoryTask(String appId, String filters) throws WebAPIException {
		String url = contentURL + "/content/info?submitterId="+appId+"&language="+localLanguage+"&filters="+filters;
		int count = 0;
		List<GCCategory> contents = null;
		while(contents==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentTask");
				contents = mGCParse.parseGCCategory(result);				
			} catch (WebAPIException e){
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
	 * @throws WebAPIException
	 */
	public List<GCCategory> getGreetingCardForDesignsTask(String designIds, String proDesIds) throws WebAPIException {
		String url = contentURL + "/content/designs/members?designType=CreativeDesign&designIds="+designIds+"&productDescriptionIds="+proDesIds+"&language="+localLanguage;
		List<GCCategory> designs = null;
		int count = 0;
		while(designs==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getContentForDesignsTask");
				designs = mGCParse.parseGCCategory(result);				
			} catch (WebAPIException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return designs;
	}
	
	public GreetingCard createGreetingCardTask(String contentId, String productIdentifier) throws WebAPIException {
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
			} catch (WebAPIException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return card;
	}
	
	public GCPage addImageToCardTask(String pageId, int holeIndex, String contentId) throws WebAPIException {
		String url = greetingCardURL + "/greetingcards/pages/"+pageId+"/insert-content?holeIndex="+holeIndex+"&contentId="+contentId;
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "addImageToCardTask");;
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (WebAPIException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	public GCPage deleteImageFromCardTask(String pageId, String contentId) throws WebAPIException {
		String url = greetingCardURL + "/greetingcards/pages/" + pageId + "/remove-content?contentId=" + contentId;
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "deleteImageFromCardTask");
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (WebAPIException e){
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return page;
	}
	
	public GCPage layoutCardPageTask(String pageId) throws WebAPIException {
		String url = greetingCardURL + "/greetingcards/pages/" + pageId + "/layout";
		GCPage page = null;
		int count = 0;
		while(page==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "layoutCardPageTask");
				page = mGCParse.parseGreetingCardPage(result);				
			} catch (WebAPIException e){
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
