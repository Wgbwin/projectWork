package com.kodakalaris.kodakmomentslib.culumus.api;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.bean.CalendarGridItemPO;
import com.kodakalaris.kodakmomentslib.bean.ImageInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.Calendar;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarContent;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarPage;
import com.kodakalaris.kodakmomentslib.culumus.bean.calendar.CalendarTheme;
import com.kodakalaris.kodakmomentslib.culumus.bean.text.TextBlock;
import com.kodakalaris.kodakmomentslib.culumus.parse.CalendarParse;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public class CalendarAPI extends GeneralAPI{

	private CalendarParse mCalendarParse;
	
	public CalendarAPI(Context context) {
		super(context);
		mCalendarParse = new CalendarParse();
	}
	
	public List<CalendarTheme> getCalendarsTask(String productDescriptionId,String language) throws WebAPIException {
		int count = 0;		
		String url = contentURL+"/content/calendars?productDescriptionId="+ productDescriptionId+ "&language="+language;
		List<CalendarTheme> calendarThemes= null;		
		while(calendarThemes ==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCalendarsTask");
				if (!"".equals(result)) {
					calendarThemes = mCalendarParse.parseCalendarThemes(result,productDescriptionId);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return calendarThemes;
	}
	
	public Calendar createCalendarTask(String productDescriptionId,String contentId,String language) throws WebAPIException {
		int count = 0;
		String url = calendarURL;
		Calendar calendar = null;
		
		java.util.Calendar now = java.util.Calendar.getInstance();  		
		int month = now.get(java.util.Calendar.MONTH) + 1;
		int year = now.get(java.util.Calendar.YEAR);
		if(month > 7) year++;	   
		int startingDayOfWeek = 0;
	    int numberOfMonths = 12;
	    int startingMonth = 1;
		
	    String useCountry = KM2Application.getInstance().getCountryCodeUsed(); 
	    if(!useCountry.equalsIgnoreCase("US") && !useCountry.equalsIgnoreCase("CA"))startingDayOfWeek = 1;

		JSONObject jsPost = new JSONObject();
		try {
			jsPost.put("ProductDescriptionId", productDescriptionId);
			jsPost.put("ContentId", contentId);
			jsPost.put("Language", language);
			jsPost.put("StartingMonth", startingMonth);
			jsPost.put("StartingYear", year);
			jsPost.put("StartingDayOfWeek", startingDayOfWeek);
			jsPost.put("NumberOfMonths", numberOfMonths);			
		}catch (JSONException e) {
			e.printStackTrace();
		}	
		while(calendar == null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsPost.toString(), "createCalendarTask");
				if (!"".equals(result)) {
					calendar = mCalendarParse.parseCalendar(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}
		return calendar;
	}
	
	public Calendar getCalendarTask(String calendarId) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/"+calendarId;
		Calendar calendar = null;
		while(calendar == null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCalendarTask");
				if (!"".equals(result)) {
					calendar = mCalendarParse.parseCalendar(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}
		return calendar;
	}
	
	public CalendarPage addContentToCalendarPageTask(String pageId, List<String> contentIds) throws WebAPIException {
		int count = 0;		
		String url = calendarURL+"/pages/"+ pageId+ "/content";
		CalendarPage calendarPage = null;
		String postData = "[";
		for(int i = 0; i< contentIds.size(); i++){
			if(i != contentIds.size()-1){
				postData += "\""+ contentIds.get(i) +"\""+ ",";
			} else {
				postData += "\""+ contentIds.get(i) +"\""+ "]";
			}
		}
		while (calendarPage == null &&  count < connTryTimes) {
			try {
				String result = httpPostTask(url, postData, "addContentToCalendarPageTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}					
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return calendarPage;
	}
	
	public CalendarPage removeContentInCalendarPageTask(String pageId,String contentId) throws WebAPIException {	
		int count = 0;	
		String url = calendarURL+"/pages/"+ pageId +"/remove-content?content="+contentId;
		CalendarPage calendarPage = null;
		while (calendarPage == null &&  count < connTryTimes) {
			try {
				String result = httpPutTask(url, "", "removeContentInCalendarPageTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}					
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count++;
		}
		return calendarPage;
	}
	
	public CalendarPage getCalendarPageTask(String pageId) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+ pageId ;
		CalendarPage calendarPage = null;
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCalendarPageTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}	
		return calendarPage;
	}
		
	public CalendarPage swapContentInCalendarTask(String pageId,String contentId1,String contentId2) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+pageId+"/swap-content?content1="+contentId1+"&content2="+contentId2;
		CalendarPage calendarPage = null;	
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpPostTask(url,"", "swapContentInCalendarTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}		
		return calendarPage;
	}
	
	public CalendarPage swapContent2InCalendarTask(String pageId,int holeIndex1,int holeIndex2) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+pageId+"/swap-content2?holeIndex1="+holeIndex1+"&holeIndex2="+holeIndex2;
		CalendarPage calendarPage = null;	
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpPostTask(url,"", "swapContent2InCalendarTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}		
		return calendarPage;
	}
	
	public CalendarPage[] addContentToCalendarGridsTask(String calendarId,List<CalendarGridItemPO> gridItemPos) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/"+ calendarId + "/content-for-calendar-cells";
		String postData = "";
		CalendarPage[] calendarPages = null;
		JSONArray jsArray = new JSONArray();
		try {	   
			for (CalendarGridItemPO gridItemPo : gridItemPos) {
				if (gridItemPo == null) continue;
				if (gridItemPo.imageInfos == null && gridItemPo.contentIds == null) continue;				
				JSONObject jsObj = new JSONObject();				
				jsObj.put("Year", gridItemPo.year);
			    jsObj.put("Month", gridItemPo.month); 
			    if (gridItemPo.day != -1) {
			    	jsObj.put("Day", gridItemPo.day);
				}else {
					jsObj.put("CellIndex", gridItemPo.holdIndex);
				}			    
			    JSONArray jsContentArray = new JSONArray();
			    if (gridItemPo.contentIds != null) {
			    	for (String contentId : gridItemPo.contentIds) {
					    if (contentId == null) continue;										   
					    jsContentArray.put(contentId);
					}			
				}else if (gridItemPo.imageInfos != null) {
					for (ImageInfo imageInfo : gridItemPo.imageInfos) {
					    if (imageInfo == null) continue;					
					    if (imageInfo.imageOriginalResource == null) continue;
					    String contentId = imageInfo.imageOriginalResource.id;
					    jsContentArray.put(contentId);
					}		
				}
			    			        			       
			    jsObj.put("ContentIds", jsContentArray);				
	    		jsArray.put(jsObj);		    		
			}			
	    	postData = jsArray.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		while(calendarPages == null && count<connTryTimes){
			try {
				String result = httpPutTask(url, postData, "addContentToCalendarGridsTask");
				if (!"".equals(result)) {				
					calendarPages = mCalendarParse.parseCalendarPages(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}		
		return calendarPages;
	}
	
	public CalendarPage[] addTextToCalendarGridsTask(String calendarId,List<CalendarGridItemPO> gridItemPos) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/"+ calendarId + "/text-for-calendar-cells";
		String postData = "";		
		CalendarPage[] calendarPages = null;
		
		JSONArray jsArray = new JSONArray();
		try {	   
			for (CalendarGridItemPO gridItemPo : gridItemPos) {
				JSONObject jsObj = new JSONObject();				
				jsObj.put("Year", gridItemPo.year);
			    jsObj.put("Month", gridItemPo.month); 
			    if (gridItemPo.day != -1) {
			    	jsObj.put("Day", gridItemPo.day);
				}else {
					jsObj.put("CellIndex", gridItemPo.holdIndex);
				}
			    jsObj.put("Text", gridItemPo.textContent);				
	    		jsArray.put(jsObj);		    		
			}			
	    	postData = jsArray.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		while(calendarPages == null && count<connTryTimes){
			try {
				String result = httpPutTask(url, postData, "addTextToCalendarGridsTask");
				if (!"".equals(result)) {				
					calendarPages = mCalendarParse.parseCalendarPages(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}				
		return calendarPages;
	}
	
	public List<CalendarContent> getCalendarContentsTask(String calendarId) throws WebAPIException {
		int count = 0;		
		String url = calendarURL+"/"+ calendarId + "/contents";

		List<CalendarContent> calendarContents= null;		
		while(calendarContents ==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCalendarContentsTask");
				if (!"".equals(result)) {
					calendarContents = mCalendarParse.parseCalendarContents(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return calendarContents;
	}
	
	public TextBlock getTextBlockForPage(String pageId,boolean isForCalendarGrid) throws WebAPIException {	
		int count = 0;		
		String url = calendarURL+"/page/"+pageId+"textblock?isForCalendarGrid="+isForCalendarGrid;
		TextBlock textBlock= null;		
		while(textBlock ==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCalendarContentsTask");
				if (!"".equals(result)) {
					textBlock = mCalendarParse.parseTextBlock(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return textBlock;
	}
	
	public CalendarPage layoutPageInCalendarTask(String pageId) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+ pageId +"/layout?sticky=true";
		CalendarPage calendarPage = null;	
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpPutTask(url,"", "layoutPageInCalendarTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}
		return calendarPage;
	}
	
	public CalendarPage insertContentOnPageTask(String pageId,int holeIndex,String contentId) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+ pageId +"/insert-content?holeIndex="+ holeIndex +"&contentId="+contentId;
		CalendarPage calendarPage = null;	
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpPostTask(url,"", "insertContentOnPageTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}		
		return calendarPage;
	}
	
	public CalendarPage shuffleContentCalendarPageTask(String pageId) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/pages/"+pageId+"/shuffle-content";
		CalendarPage calendarPage = null;	
		while(calendarPage == null && count<connTryTimes){
			try {
				String result = httpPostTask(url,"", "shuffleContentCalendarPageTask");
				if (!"".equals(result)) {
					calendarPage = mCalendarParse.parseCalendarPage(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}		
		return calendarPage;	
	}
	
	public CalendarPage[] setCalendarTitleTask(String calendarId,String title) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/"+calendarId+"/title?title="+title;
		CalendarPage[] calendarPages = null;	
		while(calendarPages == null && count<connTryTimes){
			try {
				String result = httpPutTask(url,"", "setCalendarTitleTask");
				if (!"".equals(result)) {
					calendarPages = mCalendarParse.parseCalendarPages(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}					
		return calendarPages;
	}
	
	public CalendarPage[] setCalendarAuthorTask(String calendarId,String author) throws WebAPIException {
		int count = 0;
		String url = calendarURL+"/"+calendarId+"/author?author="+author;	
		CalendarPage[] calendarPages = null;	
		while(calendarPages == null && count<connTryTimes){
			try {
				String result = httpPutTask(url,"", "setCalendarAuthorTask");
				if (!"".equals(result)) {
					calendarPages = mCalendarParse.parseCalendarPages(result);
				}				
			} catch (WebAPIException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++; 			
		}					
		return calendarPages;
	}

}
