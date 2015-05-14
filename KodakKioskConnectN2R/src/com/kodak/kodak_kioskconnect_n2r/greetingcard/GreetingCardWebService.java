package com.kodak.kodak_kioskconnect_n2r.greetingcard;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.ImageInfo;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;

/**
 * 
 * @author Kane Jin
 * @see PrintMakerWebService
 *
 */
public class GreetingCardWebService extends PrintMakerWebService{
	private final String TAG = GreetingCardWebService.class.getSimpleName();
	
	private Context context;
	private GreetingCardParser parser;
	
	public GreetingCardWebService(Context c, String serviceName){
		super(c, serviceName);
		context = c.getApplicationContext();
		parser = new GreetingCardParser();
	}
	
	/**
	 * 
	 * @return
	 */
	public GreetingCardTheme[] getGreetingCardThemes(String desIds){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String currentCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		if(currentCountryCode.equals("")){
			Log.e(TAG, "currentCountryCode == null, getGreetingCardThemes");
			currentCountryCode = Locale.getDefault().getCountry();
		}
		// TODO language: hard code here
		String getThemesURL = mGetGreetingCardThemesURL + "&language=" + this.language + "&countryCode=" + currentCountryCode + "&productDescIds=" + desIds;
		String result = httpGetTask(getThemesURL, "getGreetingCardThemes");
		if(result!=null){
			return parser.parseGreetingCardThemes(result);
		}
		return null;
	}
	
	/**
	 * 
	 * @param language
	 * @param filters
	 * @return
	 */
	public GreetingCard[] getGreetingCardCategory(String language, String filters){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String currentCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		if(currentCountryCode.equals("")){
			Log.e(TAG, "currentCountryCode == null, getGreetingCardCategory");
			currentCountryCode = Locale.getDefault().getCountry();
		}
		// TODO language: hard code here
		String getCategoryURL = mGetGreetingCardCategoryURL + "&language=" + this.language;
		getCategoryURL += "&filters=" + filters.replace(" ", "%20");
		String result = httpGetTask(getCategoryURL, "getGreetingCardCategory");
		if(result!=null){
			return parser.parseGreetingCards(this.language, result);
		}
		return null;
	}
	
	 /** 
	  * @param productType
	  * @param language
	  * @return
	  */
	public GreetingCardCatalogData[] getGreetingCardCatalogData(
			String productType) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String currentCountryCode = prefs.getString(
				MainMenu.SelectedCountryCode, "");
		if (currentCountryCode.equals("")) {
			Log.e(TAG, "currentCountryCode == null, getGreetingCardCatalogData");
			currentCountryCode = Locale.getDefault().getCountry();
		}

		productType = "DuplexMyGreeting,Greeting%20Cards";
		String getCategoryDataURL = "";
		if (!AppContext.getApplication().isContinueShopping()) {
			getCategoryDataURL = mRetailerCatalogServiceURL
					+ "/msrp/catalog3?productTypes=" + productType
					+ "&languageCultureName=" + this.language + "&countryCode="
					+ currentCountryCode;
		} else {
			String retailerId = "";
			// When continue shopping, choose Pick up in Store
			if (PrintHelper.orderType == 1) {
				retailerId = prefs.getString("selectedRetailerId", "");
			}
			// When continue shopping, choose Home Delivery
			else if (PrintHelper.orderType == 2) {
				retailerId = prefs.getString("retailerIdPayOnline", "");
			}
			getCategoryDataURL = mRetailerCatalogServiceURL + "/" + retailerId
					+ "/catalog3?productTypes=" + productType
					+ "&languageCultureName=" + this.language + "&countryCode="
					+ currentCountryCode;
		}

		String result = httpGetTask(getCategoryDataURL,
				"getGreetingCardCatalogData");
		if (result != null) {
			return parser.parseGreetingCardCatalogData(result);
		}
		return null;
	}
	
	public GreetingCard[] getContentForDesigns(String designType, String designIds, String descIds){
		String getDesignsUrl = mContentURL + "designs/members?designType=" + designType + "&designIds=" + designIds + "&productDescriptionIds=" + descIds + "&language=" + this.language;
		String result = httpGetTask(getDesignsUrl, "getContentForDesigns");
		if(result!=null){
			return parser.parseGreetingCards(this.language, result);
		}
		return null;
	}
	
	
	public GreetingCardProduct createGreetingCard(String language,
			String contentId, String productIdentifier) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String currentCountryCode = prefs.getString(
				MainMenu.SelectedCountryCode, "");
		if (currentCountryCode.equals("")) {
			Log.e(TAG, "currentCountryCode == null, createGreetingCard");
			currentCountryCode = Locale.getDefault().getCountry();
		}

		String getCategoryURL = mGreetingCardURL + "greetingcards"; 
		String postDataString = "{\"Language\":\"" + this.language
				+ "\",\"ContentId\": \"" + contentId
				+ "\",\"ProductIdentifier\":\"" + productIdentifier + "\"}";
		String result = httpPostTask(getCategoryURL, postDataString,
				"createGreetingCard");
		if (result != null) {
			return parser.parseGreetingCardProduct(result);
		}
		return null;
	}
	 
	/**
	 * 
	 * @param context 
	 * 				Context
	 * @param language 
	 *				language of selected Theme
	 * @param contentId 
	 * 				selected card id
	 * @return
	
	public GreetingCardProduct createGreetingCard(String language, String contentId){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String currentCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		if(currentCountryCode.equals("")){
			Log.e(TAG, "currentCountryCode == null, createGreetingCard");
			currentCountryCode = Locale.getDefault().getCountry();
		}
		
		String getCategoryURL = mGreetingCardURL + "greetingcards";
		// TODO: have not decided which language need to use
		String postDataString = "{\"Language\":\"" + this.language + "\",\"ContentId\": \"" + contentId + "\",}";
		String result = httpPostTask(getCategoryURL, postDataString, "createGreetingCard");
		if(result != null){
			return parser.parseGreetingCardProduct(result);
		}
		return null;
	}
	 */
	
	/**
	 * 
	 * @param pageId
	 * @param width
	 * @param height
	 * @return
	 */
	public Bitmap previewSampleTextCardPage(String pageId, int width, int height){
		String url = mGreetingCardURL + "greetingcards/pages/" + pageId + "/previewSampleText?maxWidth=" + width + "&maxHeight=" + height;
		Bitmap drawable = httpGetDrawableTask(url, "previewCardPage");
		return drawable;
	}
	
	public Bitmap previewCardPage(String pageId, int width, int height){
		String url = mGreetingCardURL + "greetingcards/pages/" + pageId + "/preview?maxWidth=" + width + "&maxHeight=" + height;
		Bitmap drawable = httpGetDrawableTask(url, "previewCardPage");
		return drawable;
	}
	
	/**
	 * 
	 * @param pageId
	 * @param width
	 * @param height
	 * @param holeIndex
	 */
	public Bitmap getCardPreviewWithHole(String pageId, int width, int height, int holeIndex){
		String url = mGreetingCardURL + "greetingcards/pages/" + pageId + "/previewwithhole?maxWidth=" + width + "&maxHeight=" + height + "&holeIndex" + holeIndex;
		Bitmap drawable = httpGetDrawableTask(url, "getCardPreviewWithHole");
		return drawable;
	}
	
	/**
	 * 
	 * @param pageId
	 * 			id of selected page.
	 * @param holeIndex
	 * 			selected layer's hole index 
	 * @param contentId
	 * 			upload image id
	 * @return
	 */
	public GreetingCardPage addImageToCard(String pageId, int holeIndex, String contentId){
		String url = mGreetingCardURL + "greetingcards/pages/"+pageId+"/insert-content?holeIndex="+holeIndex+"&contentId="+contentId;
		String result = httpPostTask(url, "", "addImageToCardTask");
		GreetingCardPage page = null;
		if(result!=null){
			page = parser.parseGreetingCardPage(result);
		}
		return page;
	}
	
	/**
	 * 
	 * @param pageId
	 * @param contentId
	 * @return
	 */
	public GreetingCardPage deleteImageFromCard(String pageId, String contentId){
		String url = mGreetingCardURL + "greetingcards/pages/" + pageId + "/remove-content?contentId=" + contentId;
		String result = httpPostTask(url, "", "deleteImageFromCardTask");
		GreetingCardPage page = null;
		if(result!=null){
			page = parser.parseGreetingCardPage(result);
		}
		return page;
	}
	
	/**
	 * 
	 * @param textBlockId
	 * @param text
	 * @return
	 * 		return true means put data successfully, else means put data failed.
	 */
	public boolean setTextBlockAndLayout(String textBlockId, String text){
		String url = mTextBlockURL + "textblocks/" + textBlockId + "/text";
		JSONObject object = new JSONObject();
		try {
			object.put("Text", text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String putDataString = object.toString();
		String result = httpPutTask(url, putDataString, "setTextBlockAndLayoutTask");
		if(!result.equals("")){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param pageId
	 * @param sticky
	 * @return
	 * 		return true means put data successfully, else means put data failed.
	 */
	public GreetingCardPage layoutCardPage(String pageId, String sticky){
		String url = mGreetingCardURL + "greetingcards/pages/" + pageId + "/layout" + (sticky!=null&&sticky.equals("")?"":"?sticky="+sticky);
		String result = httpPutTask(url, "", "layoutCardPageTask");
		GreetingCardPage page = null;
		if(!result.equals("")){
			page = parser.parseGreetingCardPage(result);
		}
		return page;
	}
	
	/**
	 * 
	 * @param imageContentId
	 * @param roi
	 * @return
	 * @see {@link PrintMakerWebService#pbSetImageCrop(Context, String, ROI)}
	 */
	public boolean setImageCrop(String imageContentId, ROI roi){
		boolean succeed = false;
		int count = 0;
		String result = "";
		while(result.equals("") && count<5){
			result = pbSetImageCrop(context, imageContentId, roi);
			count ++;
		}
		if(!result.equals("")){
			succeed = true;
		}
		return succeed;
	}
	
	/**
	 * 
	 * @param imageId
	 * @param degree
	 * @return
	 * @see {@link PrintMakerWebService#pbRotateImageDegree(Context, String, int)}
	 */
	public boolean rotateImage(String imageId, int degree){
		boolean succeed = false;
		int count = 0;
		String result = "";
		while(result.equals("") && count<5){
			result = pbRotateImageDegree(context, imageId, degree);
			count ++;
		}
		if(!result.equals("")){
			succeed = true;
		}
		return succeed;
	}
	
	/**
	 * 
	 * @param imageId
	 * @return
	 * @see {@link PrintMakerWebService#getImageInfo(String)}
	 */
	public ImageInfo getImageInfoObject(String imageId){
		String result = "";
		int count = 0;
		while(result.equals("") && count<5){
			result = getImageInfo(imageId);
			count ++;
		}
		ImageInfo imageInfo = parser.parseImageInfo(result);
		return imageInfo;
	}
	
}
