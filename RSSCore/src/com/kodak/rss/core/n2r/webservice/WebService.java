package com.kodak.rss.core.n2r.webservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.kodak.rss.RssApp;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.content.Theme;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.bean.prints.StandardPrint;
import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.n2r.bean.project.ProjectSearchResult;
import com.kodak.rss.core.n2r.bean.project.Resource;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.CountryInfo;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.shoppingcart.Cart;
import com.kodak.rss.core.n2r.bean.shoppingcart.CartItem;
import com.kodak.rss.core.n2r.bean.shoppingcart.NewOrder;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.bean.text.Font;
import com.kodak.rss.core.n2r.bean.text.TextBlock;
import com.kodak.rss.core.n2r.bean.upload.ImageResource;
import com.kodak.rss.core.n2r.parse.Parse;
import com.kodak.rss.core.util.DeviceInfoUtil;
import com.kodak.rss.core.util.DoubleToStringUtil;
import com.kodak.rss.core.util.FileUtil;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.mobile.R;

public class WebService {
	protected final String TAG = this.getClass().getSimpleName();

	protected HttpGet httpGet = null;
	protected HttpPost httpPost = null;
	protected HttpPut httpPut = null;
	protected HttpResponse response = null;
	protected HttpContext localContext;
	protected HttpDeleteWithBody httpDelete = null;

	protected Context mContext;

	protected SharedPreferences prefs;

	private String projectServiceUrl = "";
	private String authorizationTokenUrl = "";	
	private String retailerCatalogURL = "";
	private String imageEditingUrl = "";
	private String storeLocatorServiceURL = "";
	private String wifiLocatorServiceURL = "";
	private String uploadServiceURL = "";	
	private String shoppingCartServiceURL = "";
	private String standardServicePrintsURL = "";
	protected String photobookServiceURL = "";
	protected String greetingCardURL = "";
	protected String collageURL = "";
	protected String calendarURL = "";
	protected String contentURL = "";
	private String textEditURL = "";
	private String app_id = "";
	private String app_password = "";
	protected Parse mParse;
	protected String localLanguage = "";
	private String maximumDistance = "";
	private String maximumStores = "0";
	
	private String versionName = "";
	
	protected int connTryTimes = 3;
	
	private final static int connection_timeout = 10000;
	private final static int sokect_timeout = 30000;
	private final String FACEBOOK_EXT = ".Facebook";
	private final String CUMULUS_EXT = ".Cumulus";
	
	protected String countryCode = "";

	public WebService(Context context) {
		this.mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		app_id = mContext.getResources().getString(R.string.cumulus_appid);
		app_password = mContext.getResources().getString(R.string.cumulus_password);
		projectServiceUrl = mContext.getResources().getString(R.string.cumulus_projectserviceurl);
		retailerCatalogURL = mContext.getResources().getString(R.string.cumulus_retailercatalogserviceurl);
		authorizationTokenUrl = mContext.getResources().getString(R.string.cumulus_authorizationserviceurl);
		imageEditingUrl = mContext.getResources().getString(R.string.cumulus_imageeditingserviceurl);
		storeLocatorServiceURL = mContext.getResources().getString(R.string.cumulus_storelocatorserviceurl);
		wifiLocatorServiceURL = mContext.getResources().getString(R.string.cumulus_wifilocatorserviceurl);
		uploadServiceURL = mContext.getResources().getString(R.string.cumulus_uploadserviceurl);			
		shoppingCartServiceURL = mContext.getResources().getString(R.string.cumulus_shoppingcartserviceurl);
		photobookServiceURL = mContext.getResources().getString(R.string.cumulus_photobookurl);
		greetingCardURL = mContext.getResources().getString(R.string.cumulus_greetingcardurl);
		collageURL = mContext.getResources().getString(R.string.cumulus_collageurl);		
		calendarURL = mContext.getResources().getString(R.string.cumulus_calendarurl);
		standardServicePrintsURL = mContext.getResources().getString(R.string.cumulus_serviceprintsurl);
		maximumDistance = mContext.getResources().getString(R.string.cumulus_maximummiles);
		maximumStores = mContext.getResources().getString(R.string.cumulus_maximumstores);
		contentURL = mContext.getResources().getString(R.string.cumulus_contenturl);
		textEditURL = mContext.getResources().getString(R.string.cumulus_textediturl);
		
		mParse = new Parse();
		localLanguage = Locale.getDefault().toString();
		countryCode = RssApp.getInstance().getCountrycodeCurrentUsed();
		
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = packageInfo.versionName;
			if (!versionName.contains("VERSION") && versionName.length() > 10){
				versionName = versionName.substring(versionName.length()-10, versionName.length());
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		String currentServer = mContext.getString(R.string.cumulus_check_internet);
		String firstName = SharedPreferrenceUtil.getString(context, SharedPreferrenceUtil.BACK_DOOR_NAME);
		if(firstName.equals("RSS_Staging")){
			changeServer(currentServer, "mykodakmomentsstage.kodak.com");
		}else if(firstName.equals("RSS_Production")){
			changeServer(currentServer, "mykodakmoments.kodak.com");
		}else if(firstName.equals("RSS_Development")){
			changeServer(currentServer, "rssdev.kodak.com");
		}else if("RSS_Env1".equalsIgnoreCase(firstName)){
			changeServer(currentServer, "rssdev1.kodak.com");
		}else if("RSS_Env2".equalsIgnoreCase(firstName)){
			changeServer(currentServer, "rssdev2.kodak.com");
		}
	}
	
	private void changeServer(String oldServer, String newServer){
		projectServiceUrl = projectServiceUrl.replace(oldServer, newServer);
		retailerCatalogURL = retailerCatalogURL.replace(oldServer, newServer);
		authorizationTokenUrl = authorizationTokenUrl.replace(oldServer, newServer);
		imageEditingUrl = imageEditingUrl.replace(oldServer, newServer);
		storeLocatorServiceURL = storeLocatorServiceURL.replace(oldServer, newServer);
		wifiLocatorServiceURL = wifiLocatorServiceURL.replace(oldServer, newServer);
		uploadServiceURL = uploadServiceURL.replace(oldServer, newServer);
		shoppingCartServiceURL = shoppingCartServiceURL.replace(oldServer, newServer);
		standardServicePrintsURL = standardServicePrintsURL.replace(oldServer, newServer);
		photobookServiceURL = photobookServiceURL.replace(oldServer, newServer);
		greetingCardURL = greetingCardURL.replace(oldServer, newServer);
		contentURL = contentURL.replace(oldServer, newServer);
		textEditURL = textEditURL.replace(oldServer, newServer);		
		collageURL = collageURL.replace(oldServer, newServer);
		calendarURL = calendarURL.replace(oldServer, newServer);
	}

	/* -------------------- KodakAuthorizationService -------------------- */
	public String getAuthorizationToken() throws RssWebServiceException {
		String token = SharedPreferrenceUtil.authorizationToken(mContext);
		if (token.equals("")) {
			String url = "";
			String ownerId = "";
			String secret = "";
			String facebookId = SharedPreferrenceUtil.getFacebookUserId(mContext);
			if(!"".equals(facebookId)){
				ownerId = facebookId + FACEBOOK_EXT;
				secret = SharedPreferrenceUtil.getFacebookToken(mContext);
			} else {
				ownerId = DeviceInfoUtil.getDeviceUUID(mContext) + CUMULUS_EXT;
				secret = "thisdoesnotmatter";
			}
			url = authorizationTokenUrl + "/token/owner?owner_id="+ownerId+"&owner_secret="+secret+"&scope=all";
			Log.e(TAG, "getAuthorizationToken url: " + url);
			StringBuilder concatenatedAppIdPasswordSB = new StringBuilder();
			concatenatedAppIdPasswordSB.append(app_id);
			concatenatedAppIdPasswordSB.append(':');
			concatenatedAppIdPasswordSB.append(app_password);
			String concatenated = concatenatedAppIdPasswordSB.toString();
			String encodedUsernamePassword = Base64.encodeToString(concatenated.getBytes(), Base64.NO_WRAP);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Authorization", "Basic " + encodedUsernamePassword);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("ContentType", "application/json");
			httpPost.setHeader("User-Agent",app_id + "/" + versionName);//fixed for RSSMOBILEPDC-2061
			InputStream is = null;
			HttpParams myParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(myParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(myParams, connection_timeout);
			HttpConnectionParams.setSoTimeout(myParams, sokect_timeout);
			DefaultHttpClient httpClient = new DefaultHttpClient(myParams);
			BasicHttpContext localContext = new BasicHttpContext();
			HttpResponse response = null;

			try {
				response = httpClient.execute(httpPost, localContext);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				throw RssWebServiceException.networkTimeout(e);
			}  catch (SocketException e) {
				e.printStackTrace();
				throw RssWebServiceException.network(e);
			}  catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (response == null) {
				Log.e(TAG, "response == null");
			} else {
				//StatusLine sl = response.getStatusLine();
				
				//chenged by bing wang for log
				StatusLine sl = null;
				try {
					 sl = response.getStatusLine();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
					sl = null;
				}
				
				if (sl == null) {
					Log.e(TAG, "Status Line == null");
				} else {
					switch (sl.getStatusCode()) {
					case 200:
						try {
							BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
							StringBuilder sb = new StringBuilder();
							String line = null;
							while ((line = reader.readLine()) != null) {
								sb.append(line + "\n");
							}
							is.close();
							String accessToken = mParse.parseAuthorizationToken(sb.toString()).accessToken;
							StringBuilder authorizationTokenStringBuilder = new StringBuilder();
							authorizationTokenStringBuilder.append(app_id);
							authorizationTokenStringBuilder.append(':');
							authorizationTokenStringBuilder.append(accessToken);
							token = Base64.encodeToString(authorizationTokenStringBuilder.toString().getBytes(),Base64.NO_WRAP);
							SharedPreferrenceUtil.saveAuthorizationToken(mContext, token);
						} catch (Exception ex) {
							Log.e(TAG, "Error parsing data " + ex.toString());
						}
						break;
					case 403:
						String redirectUrl = "";
						Header[] headers = response.getAllHeaders();
						if (headers != null && headers.length > 0) {
							for (Header h : headers) {
								if (h != null && "App-Info-URL".equals(h.getName())) {
									redirectUrl = h.getValue();
									break;
								}
							}
						}
						
						throw RssWebServiceException.appObsolete(redirectUrl == null ? "" : redirectUrl);
					default:
						Log.e(TAG, "Unexpected response to Authorization Request: "	+ sl.getReasonPhrase());
						break;
					}
				}
			}
		}

		return token;
	}
	/* -------------------- KodakRetailerCatalog -------------------- */
	public HashMap<String, String> getCountriesTask() throws RssWebServiceException{
		int count = 0;
		HashMap<String, String> countries = null;
		String url = retailerCatalogURL + "/countries?languageCultureName=" + localLanguage;
		while(countries==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCountriesTask");
				if(!result.equals("")){
					countries = mParse.parseCountries(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.COUNTRIES, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return countries;
	}
	
	public List<CountryInfo> getCountryInfoTask(String countryCodes) throws RssWebServiceException{
		int count = 0;
		List<CountryInfo> countryInfos = null;
		String url = retailerCatalogURL + "/retailers/country-infos?countryCodes=" + countryCodes + "&languageCultureName=" + localLanguage;
		while(countryInfos==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getCountryInfoTask");
				if(!result.equals("")){
					countryInfos = mParse.parseCountryInfo(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.COUNTRY_INFOS, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return countryInfos;
	}
	
	public List<Retailer> getRetailersTask() throws RssWebServiceException{
		int count = 0;
		List<Retailer> retailers = null;
		String url = retailerCatalogURL + "/retailers?countryCode=" + countryCode;
		while(retailers==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getRetailersTask");
				if(!result.equals("")){
					retailers = mParse.parseRetailers(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.RETAILERS, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return retailers;
	}
	
	// This api in iOS is named GetCatalogTask2
	/**
	 * <P>if both retailerId and storeId are null, that means is not in Do More mode
	 * <P>if only storeId is null, that means is in Do More mode and order type is Ship to Home
	 * <P>if both retailerId and storeId are not null, that means is in Do More mode and order type is Pick Up in Store
	 * @param productTypes
	 * @param retailerId
	 * @param storeId
	 * @return
	 * @throws RssWebServiceException 
	 */
	public List<Catalog> getMSRPCatalog3Task(String productTypes, String retailerId, String storeId) throws RssWebServiceException{
		int count = 0;
		List<Catalog> catalogs = null;
		String url = "";
		if((retailerId==null||"".equals(retailerId)) && (storeId==null||"".equals(storeId))){
			url = retailerCatalogURL + "/retailers/msrp/catalog3?productTypes=" + productTypes + "&languageCultureName=" + localLanguage + "&countryCode=" + countryCode;
		} else if(storeId == null || "".equals(storeId)){
			url = retailerCatalogURL + "/retailers/" +retailerId +  "/catalog2?productTypes=" + productTypes + "&languageCultureName=" + localLanguage;
		} else {
			url = retailerCatalogURL + "/retailers/" + retailerId + "/catalog3?storeId=" + storeId + "&productTypes=" + productTypes + "&languageCultureName=" + localLanguage;
		}
		while(catalogs==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getMSRPCatalogTask3");
				if(!result.equals("")){
					catalogs = mParse.parseCatalogs(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.CATALOGS, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return catalogs;
	}
	
	public List<Retailer> getRetailersOfferingProductsTask(String productDescriptionId) throws RssWebServiceException{
		int count = 0;
		List<Retailer> retailers = null;
		String url = retailerCatalogURL + "/retailers/offering-products?productDescriptionIds=" + productDescriptionId + "&countryCode=" + countryCode;
		while(retailers==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getRetailersOfferingProductsTask");
				if(!"".equals(result)){
					retailers = mParse.parseRetailers(result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return retailers;
	}
	
	
	/* -------------------- ProjectService -------------------- */
	/**
	 * 
	 * @param projectId
	 * @throws RssWebServiceException 
	 */
	public void deleteProjectTask(String projectId) throws RssWebServiceException{
		String userId = "";
		String facebookUserId = SharedPreferrenceUtil.getFacebookUserId(mContext);
		if(facebookUserId==null || "".equals(facebookUserId)){
			userId = DeviceInfoUtil.getDeviceUUID(mContext) + CUMULUS_EXT;
		} else {
			userId = facebookUserId + FACEBOOK_EXT;
		}
		String url = projectServiceUrl + "/projects/" + projectId + "/delete?userId=" + userId;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			String result;
			try {
				result = httpDeleteTask(url, "", "deleteProjectTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1>= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws RssWebServiceException 
	 */
	public List<Project> getProjectsTask()  throws RssWebServiceException{
		int startIndex = 0;
		List<Project> projects = new ArrayList<Project>();
		ProjectSearchResult searchResult = getProjectsSearchResult(startIndex);
		if(searchResult != null){
			if(searchResult.projects!=null && searchResult.projects.size()>0){
				projects.addAll(searchResult.projects);
			}
		} else {
			return null;
		}
		
		while(searchResult!=null && startIndex+searchResult.numberReturned<searchResult.totalMatchingProjects){
			startIndex += searchResult.numberReturned;
			searchResult = getProjectsSearchResult(startIndex);
			if(searchResult != null){
				if(searchResult.projects!=null && searchResult.projects.size()>0){
					projects.addAll(searchResult.projects);
				}
			} else {
				return null;
			}
		}
		
		return projects;
	}
	
	private ProjectSearchResult getProjectsSearchResult(int startIndex)  throws RssWebServiceException{
		String userId = "";
		String url = "";
		String facebookUserId = SharedPreferrenceUtil.getFacebookUserId(mContext);
		if(facebookUserId==null || "".equals(facebookUserId)){
			userId = DeviceInfoUtil.getDeviceUUID(mContext) + CUMULUS_EXT;
//			url = projectServiceUrl + "/projects/find?owner=" + userId + "&offset=" + startIndex;	
			url = projectServiceUrl + "/projects/find3?owner=" + userId + "&langCultName=" + localLanguage + "&countryCode=" + countryCode + "&offset=" + startIndex;
		} else {
			userId = facebookUserId + FACEBOOK_EXT;
			String firstName = SharedPreferrenceUtil.getFacebookFristName(mContext);
			String lastName = SharedPreferrenceUtil.getFacebookLastName(mContext);
//			url = projectServiceUrl + "/projects/find?owner=" + userId + "&authorFirstName=" + firstName + "&authorLastName=" + lastName + "&offset=" + startIndex;
			url = projectServiceUrl + "/projects/find3?owner=" + userId +"&langCultName=" + localLanguage + "&countryCode=" + countryCode +"&authorFirstName=" + firstName + "&authorLastName=" + lastName + "&offset=" + startIndex;
		}
		int count = 0;
		ProjectSearchResult projectResult = null;
		while(projectResult==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getProjectsTask");
				projectResult = mParse.parseProjectsSearchResult(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return projectResult;
	}
	
	/**
	 * 
	 * @param projectId
	 * @param facebookUserId
	 * @return
	 * @throws RssWebServiceException 
	 */
	public Resource loadProjectTask(String projectId) throws RssWebServiceException{
		String userId = "";
		String facebookUserId = SharedPreferrenceUtil.getFacebookUserId(mContext);
		if("".equals(facebookUserId)){
			userId = DeviceInfoUtil.getDeviceUUID(mContext) + CUMULUS_EXT;
		} else {
			userId = facebookUserId + FACEBOOK_EXT;
		}
		String url = projectServiceUrl + "/projects/" + projectId + "/load?userId=" + userId;
		Resource resource = null;
		int count = 0;
		while(resource==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "loadProjectTask");
				resource = mParse.parseResource(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		
		return resource;
	}
	
	public Project saveProjectTask(String resourceId, String projectName) throws RssWebServiceException{
		String url = projectServiceUrl + "/projects";
		String facebookUserId = SharedPreferrenceUtil.getFacebookUserId(mContext);
		String applicationName = mContext.getResources().getString(R.string.cumulus_appid);
		String userId = "";
		JSONObject jsPost = new JSONObject();
		try {
			jsPost.put("ResourceId", resourceId);
			jsPost.put("ApplicationName", applicationName);
			jsPost.put("ProjectName", projectName);
			jsPost.put("Public", true);
			jsPost.put("Overwrite", true);
			
			if(facebookUserId==null || "".equals(facebookUserId)){
				userId = DeviceInfoUtil.getDeviceUUID(mContext) + CUMULUS_EXT;
			} else {
				userId = facebookUserId + FACEBOOK_EXT;
				String facebookFirstName = SharedPreferrenceUtil.getFacebookFristName(mContext);
				String facebookLastName = SharedPreferrenceUtil.getFacebookLastName(mContext);
				jsPost.put("AuthorFirstName", facebookFirstName);
				jsPost.put("AuthorLastName", facebookLastName);
			}
			jsPost.put("OwnerId", userId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Project project = null;
		int count = 0;
		while(project==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsPost.toString(), "saveProjectTask");
				project = mParse.parseProject(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return project;
	}
	
	/* -------------------- KodakContent -------------------- */
	public List<Theme> getThemesTask(String prodcutDescriptionId, String language) throws RssWebServiceException {
		int count = 0;
		String url = contentURL + "/content/themes?productDescriptionId=" + prodcutDescriptionId + "&language=" + language;
		List<Theme> themes = null;
		while(themes==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getThemesTask");
				if(!"".equals(result)){
					themes = mParse.parseThemes(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.THEMES, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return themes;
	}
	
	/* -------------------- KodakImageEditing -------------------- */
	public List<ColorEffect> getAvailableColorEffect2Task() throws RssWebServiceException{
		int count = 0;
		List<ColorEffect> colorEffects = null;
		String url = imageEditingUrl + "/availableColorEffects2?language=" + localLanguage;
		while(colorEffects==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getAvailableColorEffect2");
				if(!result.equals("")){
					colorEffects = mParse.parseColorEffects(result);
					SharedPreferrenceUtil.setString(mContext, DataKey.COLOR_EFFECTS, result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return colorEffects;
	}
	
	public String cloneImageTask(String contentId) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + contentId + "/clone";
		String imageId = "";
		int count = 0;
		while(imageId.equals("") && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "cloneImageTask");
				imageId = mParse.parseImage(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return imageId;
	}
	
	public void setColorEffectTask(String contentId, int colorIndex) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + contentId + "/colorEffect?value=" + colorIndex;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setColorEffectTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
			
	}
	
	public void setAutoRedEyeTask(String contentId, boolean autoRedEye) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + contentId + "/autoredeye?enable=" + autoRedEye;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setAutoRedEyeTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	public void setKPTLevelTask(String contentId, int levelIndex) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + contentId + "/kpt?level=" + levelIndex;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setKPTLevelTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	public ROI rotateImageTask(String imageContentId, int degree) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + imageContentId +"/rotate?angle=" + degree;
		int count = 0;
		ROI roi = null;
		while(roi==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "rotateImageTask");
				roi = mParse.parseROI(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		
		return roi;
	}
	
	public void setCropTask(String imageContentId, ROI cropROI) throws RssWebServiceException {
		String url = imageEditingUrl + "/images/" + imageContentId + "/crop";
		JSONObject jsROI = null;
		try {
			jsROI = new JSONObject();
			jsROI.put("X", cropROI.x);
			jsROI.put("Y", cropROI.y);
			jsROI.put("W", cropROI.w);
			jsROI.put("H", cropROI.h);
			jsROI.put("ContainerW", cropROI.ContainerW);
			jsROI.put("ContainerH", cropROI.ContainerH);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String postData = jsROI.toString();
		int count = 0;
		boolean succeed = false;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, postData, "setCropTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	/**
	 * 
	 * @param contentId
	 * @param dirction true is horizontal, otherwise vertical
	 * @return
	 * @throws RssWebServiceException 
	 */
	public ROI flipImageTask(String contentId, boolean dirction) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + contentId + "/flip?direction=" + (dirction?"horizontal":"vertical");
		String result = "";
		ROI roi = null;
		int count = 0;
		while("".equals(result) && count<connTryTimes){
			try {
				result = httpPostTask(url, "", "flipImageTask");
				roi = mParse.parseROI(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return roi;
	}
	
	public void setCaptionTask(String imageContentId, String text) throws RssWebServiceException{
		String url = imageEditingUrl + "/images/" + imageContentId + "/caption";
		boolean succeed = false;
		int count = 0;
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("Text", text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsObj.toString(), "setCaptionTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	/* -------------------- KodakTextEdit -------------------- */
	public List<Font> getAvailableFontsTask(String language) throws RssWebServiceException{
		int count = 0;
		String url = textEditURL + "/textblocks/fonts?language=" + language;
		List<Font> fonts = null;
		while(fonts==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getAvailableFontsTask");
				fonts = mParse.parseFonts(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return fonts;
	}
	
	public TextBlock createTextBlockTask(String texts, String fontName) throws RssWebServiceException {
		String url = textEditURL + "/textblocks/";
		String postData = "";
		try {
			JSONObject jsTextBlock = new JSONObject();
			jsTextBlock.put("Text", texts);
			jsTextBlock.put("Color", "#ff000000");
			JSONObject jsFont = new JSONObject();
			jsFont.put("Name", fontName);
			jsFont.put("Size", 48);
			jsTextBlock.put("Font", jsFont);
			postData = new JSONArray().put(jsTextBlock).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		TextBlock textBlock = null;
		int count = 0;
		while(textBlock==null && count<connTryTimes){
			String result = httpPostTask(url, postData, "createTextBlockTask");
			List<TextBlock> textBlocks = mParse.parseTextBlocks(result);
			if(textBlocks!=null && textBlocks.size()>0){
				textBlock = textBlocks.get(0);
			}
			count ++;
		}
		return textBlock;
	}
	
	/**
	 * 
	 * @param contentId
	 * @param text
	 * @return
	 * @throws RssWebServiceException 
	 */
	public void setTextTask(String contentId, String text) throws RssWebServiceException{
		String url = textEditURL + "/textblocks/" + contentId + "/text";
		JSONObject jsText = new JSONObject();
		try {
			jsText.put("Text", text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int count = 0;
		boolean succeed = false;
		while(!succeed && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsText.toString(), "setTextTask");
				mParse.checkError(result);
				succeed = true;
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
	}
	
	public TextBlock updateTextBlockTask(TextBlock textBlock) throws RssWebServiceException{
		String url = textEditURL + "/textblocks/" + textBlock.id;
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("Alignment", textBlock.getFontAlignmentIndex(textBlock.alignment));
			jsObj.put("Color", textBlock.color);
			
			JSONObject jsFont = new JSONObject();
			jsFont.put("Name", textBlock.fontName);
			jsFont.put("Size", textBlock.fontSize.equalsIgnoreCase("Auto")||textBlock.fontSize.equals("") ? 0:Float.parseFloat(textBlock.fontSize));
			jsFont.put("SizeMin", textBlock.sizeMin==-1 ? 8:textBlock.sizeMin);
			if(!textBlock.smallTextSize){
				jsFont.put("SizeMax", textBlock.sizeMax==-1 ? 48:textBlock.sizeMax);
			} else {
				jsFont.put("SizeMax", textBlock.sizeMax==-1 ? 12:textBlock.sizeMax);
			}
			jsObj.put("Font", jsFont);
			
			jsObj.put("Justification", textBlock.getFontJustificationIndex(textBlock.justification));
			jsObj.put("Language", textBlock.language);
			jsObj.put("Text", textBlock.formatText());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		int count = 0;
		TextBlock tbResult = null;
		while(tbResult==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsObj.toString(), "updateTextBlockTask");
				tbResult = mParse.parseTextBlock(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return tbResult;
	}
	
	/* -------------------- KodakStandardPrints -------------------- */
	public List<StandardPrint> createStandardPrintsTask(List<ProductInfo> proInfos) throws RssWebServiceException{
		String url = standardServicePrintsURL;
		JSONArray jsProducts = new JSONArray();
		List<ProductInfo> tempProducts = new ArrayList<ProductInfo>();
		for(ProductInfo product : proInfos){
			if(product.num == 0 || !product.productType.equalsIgnoreCase(ProductInfo.PRO_TYPE_PRINT)){
				continue;
			}
			JSONObject jsObj = new JSONObject();
			try {
				jsObj.put("ProductDescriptionId", product.descriptionId);
				jsObj.put("ImageId", product.correspondId);
				JSONObject jsRoi = new JSONObject();
				jsRoi.put("X", DoubleToStringUtil.formatDouble(product.roi.x, 6));
				jsRoi.put("Y", DoubleToStringUtil.formatDouble(product.roi.y, 6));
				jsRoi.put("W", DoubleToStringUtil.formatDouble(product.roi.w, 6));
				jsRoi.put("H", DoubleToStringUtil.formatDouble(product.roi.h, 6));
				jsRoi.put("ContainerW", DoubleToStringUtil.formatDouble(product.roi.ContainerW, 6));
				jsRoi.put("ContainerH", DoubleToStringUtil.formatDouble(product.roi.ContainerH, 6));
				jsObj.put("ImageROI", jsRoi);
				tempProducts.add(product);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsProducts.put(jsObj);
		}
		if(jsProducts.length() == 0){
			return new ArrayList<StandardPrint>();
		}
		List<StandardPrint> standardPrints = null;
		int count = 0;
		while(standardPrints==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsProducts.toString(), "createStandardPrintsTask");
				standardPrints = mParse.parseStandardSevicePrints(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		// set productInfo cart item id, need to improve
		if(standardPrints!=null && standardPrints.size()==tempProducts.size()){
			for(int i=0; i<standardPrints.size(); i++){
				tempProducts.get(i).cartItemId = standardPrints.get(i).id;
				standardPrints.get(i).quantity = tempProducts.get(i).num;
			}
		}
		return standardPrints;
		
	}
	
	/* -------------------- KodakShoppingCart -------------------- */
	public Pricing priceProduct2Task(String cartId, String retailerId, String productsWithCount) throws RssWebServiceException{
		String url = shoppingCartServiceURL+"shopping-carts/pricing2?cartId="+cartId+"&retailerId="+retailerId+"&language="+localLanguage+"&products="+productsWithCount+"&country="+countryCode;
		Pricing pricing = null;
		int count = 0;
		while(pricing==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "priceProduct2Task");
				pricing = mParse.parsePricing(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return pricing;
	}
	
	/**
	 * add parameter for coupon
	 * change the result from Pricing to Cart
	 * */
	public Cart priceProduct3Task(String cartId, String retailerId, String productsWithCount,String discounts) throws RssWebServiceException{
		String url = shoppingCartServiceURL+"shopping-carts/pricing3?cartId="+cartId+"&retailerId="+retailerId+"&language="+localLanguage+"&products="+productsWithCount+"&country="+countryCode;
		if (discounts != null && !"".equals(discounts)) {
			url = url +"&discounts="+discounts;
		}

//		Pricing pricing = null;
		Cart cart = null;
		int count = 0;
		while(cart == null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "priceProduct3Task");
				cart = mParse.parseCart(result);
//				pricing = mParse.parsePricing3(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
//		return pricing;
		return cart;
	}
	
	public Cart createCartTask() throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/";
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("Language", localLanguage);
			jsObj.put("Country", countryCode);
			jsObj.put("EnhancedPricing", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsObj.toString(), "createCartTsk");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;
	}
	
	public Cart setStoreTask(String cartId, String retailerId, String storeId) throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/store?retailerId=" + retailerId + "&storeId=" + storeId;
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, "", "setStoreTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;
	}
	
	public Cart setCustomerTask(String cartId, boolean isShipToHome, LocalCustomerInfo customerInfo) throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/customer";
		JSONObject customer = new JSONObject();
		JSONObject attributes = new JSONObject();
		JSONArray addressArray = new JSONArray();
		JSONObject address = new JSONObject();
		JSONObject addressParent = new JSONObject();
		try
		{
			String firstName = customerInfo.getCusFirstName();
			String lastName = customerInfo.getCusLastName();
			String phone = customerInfo.getCusPhone();
			String email = customerInfo.getCusEmail();

			customer.put("FirstName", firstName);
			customer.put("LastName", lastName);
			customer.put("Phone1", phone);
			customer.put("Email",email);
			customer.put("Language", localLanguage);
			
			String none = "none";
			String shipFirstName = customerInfo.getShipFirstName();
			String shipLastName = customerInfo.getShipLastName();
			String shipAddress1 = customerInfo.getShipAddress1();
			String shipAddress2 = customerInfo.getShipAddress2();
			String shipCity = customerInfo.getShipCity();
			String shipStateProvince = customerInfo.getShipState();
			String shipZip = customerInfo.getShipZip();
			String shipCountry = countryCode;
			
			address.put("FirstName", isShipToHome ? shipFirstName : firstName);
			address.put("LastName", isShipToHome ? shipLastName : lastName);
			if(!isShipToHome){
				address.put("Email", email);
			}
			address.put("Address1", isShipToHome ? shipAddress1 : none);
			address.put("Address2", isShipToHome ? shipAddress2 : none);
			address.put("City", isShipToHome ? shipCity : none);
			address.put("StateProvince", isShipToHome ? shipStateProvince : none);
			address.put("PostalCode", isShipToHome ? shipZip : none);
			address.put("Country", shipCountry);
			address.put("Phone", phone);
			
			addressParent.put("Id", 0);
			addressParent.put("Address", address);
			addressParent.put("AddressType", 0);
			addressArray.put(addressParent);
			if(isShipToHome){
				JSONObject addressParent1 = new JSONObject();
				addressParent1.put("Id", 0);
				addressParent1.put("Address", address);
				addressParent1.put("AddressType", 1);
				addressArray.put(addressParent1);
				
				JSONObject addressParent2 = new JSONObject();
				addressParent2.put("Id", 0);
				addressParent2.put("Address", address);
				addressParent2.put("AddressType", 2);
				addressArray.put(addressParent2);
			}
			
			// TODO add Attributes
			attributes.put("Name", "");
			attributes.put("OptIn", "");
			attributes.put("Value", SharedPreferrenceUtil.getBoolean(mContext, SharedPreferrenceUtil.CDG360_DESIRED)?"TRUE":"FALSE");
			
			customer.put("Addresses", addressArray);
			customer.put("Attributes", attributes);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, customer.toString(), "setCustomerTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;
	}
	
	public Cart setRetailerIdTask(String cartId, String retailerId) throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/retailer?id=" + retailerId;
		int count = 0;
		Cart cart = null;
		while(count==0 && cart==null){
			try {
				String result = httpPutTask(url, "", "setRetailerIdTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;
	}
	
	public CartItem[] addPrintProductsTask(String cartId, List<ProductInfo> proInfos, List<StandardPrint> prints) throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId;
		JSONArray jsArray = new JSONArray();
		if(prints != null){
			for(StandardPrint print : prints){
				JSONObject jsObj = new JSONObject();
				try {
					jsObj.put("ProductId", print.id);
					jsObj.put("ProductQuantity", print.quantity);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsArray.put(jsObj);
			}
		}
			
		for(ProductInfo product : proInfos){
			if(product.num == 0 || product.productType.equalsIgnoreCase("Print")){
				continue;
			}
			JSONObject jsObj = new JSONObject();
			try {
				jsObj.put("ProductId", product.cartItemId);
				jsObj.put("ProductQuantity", product.num);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsArray.put(jsObj);
		}		
		CartItem[] items = null;
		int count = 0;
		while(items==null && count<connTryTimes){
			try {
				String result = httpPutTask(url, jsArray.toString(), "addProductsTask");
				items = mParse.parseCartItems(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return items;
	}
	
	// TODO: need to be updated 
	public Cart removeProductsTask(String cartId, List<ProductInfo> products, List<StandardPrint> prints) throws RssWebServiceException{
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/item";
		JSONArray jsArr = new JSONArray();
		if(prints != null){
			for(StandardPrint print : prints){
				JSONObject jsPro = new JSONObject();
				try {
					jsPro.put("ProductId", print.id);
					jsPro.put("ProductQuantity", print.quantity);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsArr.put(jsPro);
			}
		}
		for(ProductInfo proInfo : products){
			if(proInfo.productType.equalsIgnoreCase("print")){
				continue;
			}
			JSONObject jsPro = new JSONObject();
			try {
				jsPro.put("ProductId", proInfo.cartItemId);
				jsPro.put("ProductQuantity", proInfo.num);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsArr.put(jsPro);
		}
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			String result;
			count ++;
			try {
				result = httpDeleteTask(url, jsArr.toString(), "removeProductsTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e) {
				if(count >= connTryTimes){
					throw e;
				}
			}
		}
		return cart;
	}
	
	public Cart removeAllProductsTask(String cartId) throws RssWebServiceException {
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/remove-all-items";
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			String result = "";
			count ++;
			try {
				result = httpPutTask(url, "", "removeAllProductsTask");
				cart = mParse.parseCart(result);
			} catch(RssWebServiceException e){
				if(count >= connTryTimes){
					throw e;
				}
			}
		}
		return cart;
	}
	
	public NewOrder convertToOrderTask(String cartId) throws RssWebServiceException{
		String isTestOrder = SharedPreferrenceUtil.getBoolean(mContext, StoreInfo.IS_TEST_STORE) ? "true" : "false";
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/convert-to-order2?isTestOrder=" + isTestOrder;
		NewOrder newOrder = null;
		int count = 0;
		while(newOrder==null && count<connTryTimes){
			try {
				String result = httpPostTask(url, "", "convertToOrderTask");
				newOrder = mParse.parseNewOrder(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return newOrder;
	}
	
	public Cart getCartTask(String cartId) throws RssWebServiceException {
		Cart cart = null;
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId;
		int count = 0;
		while(cart==null && count<connTryTimes){
			try{
				String result = httpGetTask(url, "getCartTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e){
				if(count >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;
	}
	
	public Cart checkCouponCodeTask(String cartId, String disCountCode) throws RssWebServiceException {	
		Cart cart = null;
		String url = shoppingCartServiceURL + "shopping-carts/" + cartId + "/discount?discountCode=" + disCountCode;
		int count = 0;
		while(cart==null && count<connTryTimes){
			try{
				String result = httpPutTask(url, "","checkCouponCodeTask");
				cart = mParse.parseCart(result);
			} catch (RssWebServiceException e){
				if(count >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return cart;		
	}

	
	/* -------------------- KodakStoreLocator -------------------- */
	public List<StoreInfo> getStoresTask(String zip, boolean isWifiLocator) throws RssWebServiceException{
		return getStoresTask(zip, "", "", "", false, isWifiLocator);
	}
	
	public List<StoreInfo> getStoresTask(String zip, String latitude, String longitude, String products, boolean isSearch, boolean isWifiLocator) throws RssWebServiceException{
		int count = 0;
		List<StoreInfo> stores = null;
		String url = "";
		if(isWifiLocator){
			url = wifiLocatorServiceURL + "&maximummiles=" + maximumDistance + "&maximumstores=" + maximumStores + "&latitude=" + latitude + "&longitude=" + longitude + (products.equals("") ? "" : "&products=" + products) + "&capabilities=connect_wifi";
		} else {
			url = storeLocatorServiceURL + "store-locator?";
			if(latitude.equals("") || longitude.equals("")) {
				url = url + "country=" + countryCode + "&maximummiles=" + maximumDistance + "&maximumstores=" + maximumStores + "&postalcode=" + (zip.replace(' ', '+')) + (products.equals("") ? "" : "&products=" + products);
				url = url + "&includeTestStores=" + (prefs.getBoolean(StoreInfo.IS_TEST_STORE, false)?"true":"false");
			} else {
				url = url + "country=" + countryCode + "&maximummiles=" + maximumDistance + "&maximumstores=" + maximumStores + "&latitude=" + latitude + "&longitude=" + longitude + (products.equals("") ? "" : "&products=" + products);
				url = url + "&includeTestStores=" + (prefs.getBoolean(StoreInfo.IS_TEST_STORE, false)?"true":"false");
			}
		}
		while(stores==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "getStoresTask");
				if(!result.equals("")){
					stores = mParse.parseStoresInfo(result);
				}
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return stores;
	}
	
	public Boolean checkStoreTask(String retailerId, String storeId, String prodcuts) throws RssWebServiceException{
		String url = storeLocatorServiceURL + "check-store?retailerid=" + retailerId + "&storeid=" + storeId + "&products=" + prodcuts;
		
		Boolean valid = null;
		int count = 0;
		while(valid==null && count<connTryTimes){
			try {
				String result = httpGetTask(url, "checkStoreTask");
				valid = mParse.parseStoreAvailability(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return valid;
	}
	
	public void logAppReviewTask(){
		
	}
	
	/* -------------------- KodakUpload -------------------- */
	// TODO: upload
	/**changed by Bing Wang for upload facebook photo use file path
	 * @throws RssWebServiceException */
	public ImageResource uploadImageTask(String imageUri, String contentId, boolean isUploadThumb,boolean isUri,String productType, String proDescriptionId) throws RssWebServiceException{
		String url = uploadServiceURL + (contentId.equals("")?"":("?replace=" + contentId));//&size={sizeInBytes}&checksum={checksum}
		ImageResource imageRes = null;
		int count = 0;
		while(imageRes==null && count<connTryTimes){
			try {
				String result = httpPostImageTask(url, imageUri, isUploadThumb, "uploadImageTask",isUri,productType,proDescriptionId);
				imageRes = mParse.parseImageResource(result);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return imageRes;
	}
	/**upload photo to server use url
	 * https://mykodakmomentsstage.kodak.com/KodakImageEditing/Service.svc/web
	 * @throws RssWebServiceException */
	public ImageResource addImageFromWebTask(String sourceUrl, int imageHeight, int imageWidth) throws RssWebServiceException{
		String url = imageEditingUrl+ "/images";
		JSONArray jsArr = new JSONArray();
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("originalURL", sourceUrl);
			jsObj.put("pixelHeight", imageHeight);
			jsObj.put("pixelWidth", imageWidth);
			jsObj.put("supportsMetadata", false);//change for bing wang
			jsArr.put(jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		ImageResource imageRes = null;
		int count = 0;
		while(imageRes == null && count<connTryTimes){
			try {
				String result = httpPostTask(url, jsArr.toString(), "addImageFromWebTask");
				imageRes = mParse.parseImageResource(result,true);
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		return imageRes;		
	}

	
	public byte[] loadImagePreviewTask(String url) throws RssWebServiceException{
		byte[] data = null;
		int count = 0;
		
		while(data==null && count<connTryTimes){
			try {
				data = httpGetImageDataTask(url, "loadImagePreviewTask");
			} catch (RssWebServiceException e) {
				if(count+1 >= connTryTimes){
					throw e;
				}
			}
			count ++;
		}
		
		return data;
	}
	
	public void setConnTryTimes(int connTryTimes) {
		this.connTryTimes = connTryTimes;
	}

	protected String httpGetTask(String url, String taskName) throws RssWebServiceException {
		Log.e(TAG, taskName + " url: " + url);
		url = url.replaceAll(" ", "%20");
		String result = "", result1 = "";
		httpGet = new HttpGet(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("ContentType", "application/json");

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
		try {
			response = mDefaultHttpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		}

		if (response != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result1 = sb.toString();
				Log.i(TAG, taskName + ", status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
			return result;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, taskName + " received a 401");
			SharedPreferrenceUtil.saveAuthorizationToken(mContext, "");
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		return "";
	}

	//add productType and proDescriptionId by bing wang on 2014-12-23 for get minSizeForResize
	protected String httpPostImageTask(String url, String imageUri, boolean isUploadThumb, String taskName,boolean isUri,String productType, String proDescriptionId) throws RssWebServiceException{
		Log.e(TAG, taskName + " url: " + url + "[" + imageUri + "]");
		url = url.replaceAll(" ", "%20");
		InputStream is = null;
		httpPost = new HttpPost(url);
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "image/jpeg");
		
		InputStreamEntity entity = null;
		String newFilePath = "";
		
		//check if is png file
		String originalFilePath = isUri ? ImageUtil.getFilePath(mContext, imageUri) : imageUri;
		boolean isPng = ImageUtil.isPngFile(originalFilePath);
		try {
			if (isUri) {
				Uri uri = Uri.parse(imageUri);
				ContentResolver cr = mContext.getContentResolver();
				
				if(isUploadThumb){
					ExifInterface exif = ImageUtil.getFileExifInterface(mContext, imageUri);
					newFilePath = ImageUtil.compressThumbToJPG(mContext, imageUri, exif,RssApp.getInstance().getTempImageFolderPath());
				}
				
			}
			
			String filePath = originalFilePath;
			if(!newFilePath.equals("")){
				filePath = newFilePath;
			}else{
				//convert png to jpg
				if(isPng){
					newFilePath = RssApp.getInstance().getTempImageFolderPath()+"/convert_"+FileUtil.getFileNameWithoutSuffix(originalFilePath) + ".jpg";
					if(!new File(newFilePath).exists()){//havn't convert before
						boolean succeed =  ImageUtil.pngToJpg(originalFilePath, newFilePath);
						if(!succeed){//faild convert
							Log.i(TAG, "convert png (" + originalFilePath + ") to jpg faild");
							newFilePath = "";
						}else{
							filePath = newFilePath;
						}
					}else{
						filePath = newFilePath;
					}
				}
				
				//resize large image
				int[] originalSize = ImageUtil.getImageSize(filePath);
				int[] resize = RssApp.getInstance().getSizeForResize(originalSize[0], originalSize[1],productType,proDescriptionId);
				if(resize != null){//not null means need resize
					newFilePath = ImageUtil.getResizeFilePath(originalFilePath, resize);
					if(!new File(newFilePath).exists()){// havn't resized before
						boolean succeed = ImageUtil.resizePic(filePath, newFilePath, resize[0], resize[1]);
						if(succeed){
							filePath = newFilePath;
						}
					}else{
						filePath = newFilePath;
					}
				}
			}
			
			is = new FileInputStream(filePath);
			Log.i(TAG, "httpPostImageTask[path:" + filePath + ", length:" + is.available() + "]");
			if(is!=null){
				int length = is.available();
				if (length > 0) {
					entity = new InputStreamEntity(is, length); //entity = new InputStreamEntity(is, -1);
				}else {
					entity = null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			entity = null;
		}
		
		if(entity == null){
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return "";
		}
		
		entity.setContentType("image/jpeg");
		entity.setChunked(false);
		httpPost.setEntity(entity);
		String result = "";
		
		InputStream isResult = null;
		try {
			HttpParams mHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
			HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
			DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
			
			response = mDefaultHttpClient.execute(httpPost, localContext);
			if(response!=null){
				HttpEntity httpEntity = response.getEntity();
				isResult = httpEntity.getContent();
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(is!= null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (response != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(isResult, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				result = sb.toString();
				Log.e(TAG, taskName + " StatusCode: " + response.getStatusLine().getStatusCode() + ", result: " + result);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}finally{
				if(isResult != null){
					try {
						isResult.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (response == null) {
			Log.e(TAG, taskName + " response == null");
		} else if (response.getStatusLine().getStatusCode() == 200) {
			
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, taskName + " received a 401");
			SharedPreferrenceUtil.saveAuthorizationToken(mContext, "");
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param url
	 * @param postDataString
	 * @param taskName
	 * @return if return is "" means the task failed
	 * @throws RssWebServiceException 
	 */
	protected String httpPostTask(String url, String postDataString, String taskName) throws RssWebServiceException {
		Log.e(TAG, taskName + " url: " + url + " " + postDataString);
		url = url.replaceAll(" ", "%20");
		String result = "", result1 = "";
		httpPost = new HttpPost(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

		StringEntity requestEntity;
		requestEntity = null;
		try {
			requestEntity = new StringEntity(postDataString, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		requestEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPost.setEntity(requestEntity);

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

		try {
			response = mDefaultHttpClient.execute(httpPost, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		}		
		
		if (response != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result1 = sb.toString();
				Log.i(TAG, taskName + ", status code: "	+ response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}
		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
			return result;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, taskName + " received a 401");
			SharedPreferrenceUtil.saveAuthorizationToken(mContext, "");
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
			throw RssWebServiceException.server(response.getStatusLine().getStatusCode()+"", "");			
		}
		
		return "";
	}

	/**
	 * 
	 * @param url
	 * @param putDataString
	 * @param taskName
	 * @return if return is "" means the task failed
	 * @throws RssWebServiceException 
	 */
	protected String httpPutTask(String url, String putDataString, String taskName) throws RssWebServiceException {
		Log.e(TAG, taskName + " url: " + url + putDataString);
		url = url.replaceAll(" ", "%20");
		String result = "", result1 = "";
		StringEntity URL = null;
		httpPut = new HttpPut(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");

		try {
			URL = new StringEntity(putDataString, "UTF-8");
			URL.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		httpPut.setEntity(URL);
		try {
			HttpParams mHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
			HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
			DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

			response = mDefaultHttpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		}
		if (response != null) {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result1 = sb.toString();
				Log.i(TAG, taskName + " Status code: " + response.getStatusLine().getStatusCode() + " result: " + result1 + putDataString + "\n");
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, taskName + " received a 401");
			SharedPreferrenceUtil.saveAuthorizationToken(mContext, "");
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public byte[] httpGetImageDataTask(String url, String taskName) throws RssWebServiceException {
		Log.e(TAG, taskName + " url: " + url);
		url = url.replaceAll(" ", "%20");
		httpGet = new HttpGet(url);

		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("ContentType", "application/json");

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

		byte[] data = null;
		HttpEntity entity = null;
		try {
			response = mDefaultHttpClient.execute(httpGet, localContext);
			if (response != null) {
				entity = response.getEntity();
				is = entity.getContent();
				int length = (int) entity.getContentLength();
				if (length > 0) {
					data = new byte[length];
					byte[] buffer = new byte[4098];
					int readLen = 0;
					int destPos = 0;
					while ((readLen = is.read(buffer)) >= 0) {
						if (readLen > 0) {
							System.arraycopy(buffer, 0, data, destPos, readLen);
							destPos += readLen;
						} else {
							Log.w(TAG, "");
						}
					}
				}
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		} finally {
			try {
				if(is!=null){
					is.close();
				}
				/*if(entity!=null){
					entity.consumeContent();
				}*/
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	protected String httpDeleteTask(String url, String deleteData, String taskName) throws RssWebServiceException{
		Log.e(TAG, taskName + " url: " + url + deleteData);
		String result = "";
		HttpDeleteWithBody httpDelete = null;
		StringEntity stringEntity = null;
		httpDelete = new HttpDeleteWithBody(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpDelete.addHeader("Authorization", "Basic " + token);
		httpDelete.addHeader("Accept", "application/json");
		httpDelete.addHeader("ContentType", "application/json");
		
		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
		
		try { 
			stringEntity = new StringEntity(deleteData, "UTF-8");
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
		httpDelete.setEntity(stringEntity);
		
		try {
			response = mDefaultHttpClient.execute(httpDelete, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			throw RssWebServiceException.networkTimeout(e);
		}  catch (IOException e) {
			e.printStackTrace();
			throw RssWebServiceException.network(e);
		}
		
		if(response != null)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print(taskName + " StatusCode: " + response.getStatusLine().getStatusCode() + " result: " + result);
			}
			catch (Exception ex)
			{
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}
		
		return result;
	}
	
	public static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
	    public static final String METHOD_NAME = "DELETE";
	    public String getMethod() { return METHOD_NAME; }

	    public HttpDeleteWithBody(final String uri) {
	        super();
	        setURI(URI.create(uri));
	    }
	    public HttpDeleteWithBody(final URI uri) {
	        super();
	        setURI(uri);
	    }
	    public HttpDeleteWithBody() { super(); }
	}
}
