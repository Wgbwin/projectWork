package com.kodak.kodak_kioskconnect_n2r;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

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
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.AppConstants;
import com.AppContext;
import com.kodak.flip.PhotoBookPage;
import com.kodak.flip.PhotoDefinition;
import com.kodak.flip.SelectedImage;
import com.kodak.kodak_kioskconnect_n2r.Pricing.LineItem;
import com.kodak.kodak_kioskconnect_n2r.Pricing.UnitPrice;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.kodak_kioskconnect_n2r.bean.Parse;
import com.kodak.kodak_kioskconnect_n2r.bean.PhotoInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.content.Theme;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.Retailer;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;
import com.kodak.kodak_kioskconnect_n2r.bean.text.TextBlock;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.ImageUtil;

public class PrintMakerWebService {
	private static final String TAG = "PrintMakerWebService";
	DefaultHttpClient httpClient;
	protected HttpGet httpGet = null;
	protected HttpPost httpPost = null;
	protected HttpPut httpPut = null;
	JSONObject jsonObject = null;
	public HttpContext localContext;
	public HttpResponse response = null;
	// Cumulus Strings
	public String mWebServicesAppID = "";
	public String mWebServicesAppPassword = "";
	public String mAuthorizationServiceURL = "";
	private String mStoreLocatorServiceURL = "";
	private String mWifiLocatorServiceURL = "";
	private String mShoppingCartServiceURL = "";
	private String mUploadServiceURL = "";
	private String mServicePrintsURL = "";
	private String mStandardPrintsURL = "";
	protected String mImageEditingServiceURL = "";
	private String mImageServiceURL = "";
	public String mRetailerCatalogServiceURL = "";
	public String mRetailerCatalogURL = "";
	public static String mAuthorizationToken = "";
	private String mRetailerID = "";
	private String mCountryCode = "";
	private String mMaximumDistance = "";
	private String mMaximumStores = "";

	private String mPhotobookURL = "";
	private String versionName = "";
	protected String language = "";

	protected String mGetGreetingCardThemesURL = "";
	protected String mGetGreetingCardCategoryURL = "";
	protected String mContentURL = "";
	protected String mGreetingCardURL = "";
	protected String mTextBlockURL = "";
	
	protected String mCollageURL = "";

	// XML Escape Sequences
	final String mAmpersandRegularExpression = "&";
	final Pattern mAmpersandPattern = Pattern.compile(mAmpersandRegularExpression);
	Context mContext;
	protected int connTryTimes = 3;
	private final int connection_timeout = 10000;
	private final int sokect_timeout = 120000;

	// The serviceName should be the name of the Service you are going to be
	// using.
	public PrintMakerWebService(Context c, String serviceName) {
		// Setup web services related parameters
		try {
			mContext = c;
			PackageInfo packageInfo;
			try {
				packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
				versionName = packageInfo.versionName;
				if (!versionName.contains("VERSION") && versionName.length() > 10){
					versionName = versionName.substring(versionName.length()-10, versionName.length());
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			mWebServicesAppID = c.getResources().getString(R.string.cumulus_appid);
			mWebServicesAppPassword = c.getResources().getString(R.string.cumulus_password);
			mAuthorizationServiceURL = c.getResources().getString(R.string.cumulus_authorizationserviceurl) + mWebServicesAppID + "&scope=all";
			mRetailerID = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext()).getString("selectedRetailerId", "");
			mCountryCode = c.getResources().getString(R.string.cumulus_countrycode);
			mMaximumDistance = c.getResources().getString(R.string.cumulus_maximummiles);
			mMaximumStores = c.getResources().getString(R.string.cumulus_maximumstores);
			mStoreLocatorServiceURL = c.getResources().getString(R.string.cumulus_storelocatorserviceurl);
			mWifiLocatorServiceURL = c.getResources().getString(R.string.cumulus_wifilocatorserviceurl);
			mShoppingCartServiceURL = c.getResources().getString(R.string.cumulus_shoppingcartserviceurl);
			mUploadServiceURL = c.getResources().getString(R.string.cumulus_uploadserviceurl);
			mServicePrintsURL = c.getResources().getString(R.string.cumulus_serviceprintsurl);
			mStandardPrintsURL = c.getResources().getString(R.string.cumulus_standardprintsurl);
			mImageEditingServiceURL = c.getResources().getString(R.string.cumulus_imageeditingserviceurl);
			mImageServiceURL = c.getResources().getString(R.string.cumulus_imageserviceurl);
			mRetailerCatalogServiceURL = c.getResources().getString(R.string.cumulus_retailercatalogserviceurl);
			mPhotobookURL = c.getResources().getString(R.string.cumulus_get_photobook_info_url);
			mRetailerCatalogURL = c.getResources().getString(R.string.cumulus_kodakRetailerCatalogurl);

			mContentURL = c.getResources().getString(R.string.cumulus_get_greetingcard);
			mGetGreetingCardThemesURL = c.getResources().getString(R.string.cumulus_get_greetingcard) + "contentsearchstarters2?submitterId="
					+ mWebServicesAppID;
			mGetGreetingCardCategoryURL = c.getResources().getString(R.string.cumulus_get_greetingcard) + "info?submitterId=" + mWebServicesAppID;
			mGreetingCardURL = c.getResources().getString(R.string.cumulus_create_greetingcard);
			mTextBlockURL = c.getResources().getString(R.string.cumulus_text_block);
			mCollageURL = c.getResources().getString(R.string.cumulus_collage_url);

			String currentServer = mAuthorizationServiceURL.substring(mAuthorizationServiceURL.indexOf("https://") + 8,
					mAuthorizationServiceURL.indexOf("KodakAuthorizationService/Service.svc"));
			String firstName = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext()).getString("firstName", "");

			if ("RSS_Staging".equalsIgnoreCase(firstName)) {
				changeServer(currentServer, "mykodakmomentsstage.kodak.com/");
			} else if ("RSS_Production".equalsIgnoreCase(firstName)) {
				changeServer(currentServer, "mykodakmoments.kodak.com/");
			} else if ("RSS_Development".equalsIgnoreCase(firstName)) {
				changeServer(currentServer, "rssdev.kodak.com/");
			} else if ("RSS_ENV1".equalsIgnoreCase(firstName)) {
				changeServer(currentServer, "RSSDEV1.KODAK.COM/");
			} else if ("RSS_ENV2".equalsIgnoreCase(firstName)) {
				changeServer(currentServer, "RSSDEV2.KODAK.COM/");
			}
			language = Locale.getDefault().toString();
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
		HttpParams myParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(myParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(myParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(myParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(myParams, sokect_timeout);
		httpClient = new DefaultHttpClient(myParams);
		localContext = new BasicHttpContext();
	}

	private void changeServer(String oldServer, String newServer) {
		mAuthorizationServiceURL = mAuthorizationServiceURL.replace(oldServer, newServer);
		mStoreLocatorServiceURL = mStoreLocatorServiceURL.replace(oldServer, newServer);
		mWifiLocatorServiceURL = mWifiLocatorServiceURL.replace(oldServer, newServer);
		mShoppingCartServiceURL = mShoppingCartServiceURL.replace(oldServer, newServer);
		mUploadServiceURL = mUploadServiceURL.replace(oldServer, newServer);
		mServicePrintsURL = mServicePrintsURL.replace(oldServer, newServer);
		mStandardPrintsURL = mStandardPrintsURL.replace(oldServer, newServer);
		mImageEditingServiceURL = mImageEditingServiceURL.replace(oldServer, newServer);
		mImageServiceURL = mImageServiceURL.replace(oldServer, newServer);
		mRetailerCatalogServiceURL = mRetailerCatalogServiceURL.replace(oldServer, newServer);
		mPhotobookURL = mPhotobookURL.replace(oldServer, newServer);
		mRetailerCatalogURL = mRetailerCatalogURL.replace(oldServer, newServer);

		mGetGreetingCardThemesURL = mGetGreetingCardThemesURL.replace(oldServer, newServer);
		mGetGreetingCardCategoryURL = mGetGreetingCardCategoryURL.replace(oldServer, newServer);
		mGreetingCardURL = mGreetingCardURL.replace(oldServer, newServer);
		mTextBlockURL = mTextBlockURL.replace(oldServer, newServer);
		mContentURL = mContentURL.replace(oldServer, newServer);
		mCollageURL = mCollageURL.replace(oldServer, newServer);
		Log.d(TAG, "ChangeServer from " + oldServer + " to " + newServer + ". AuthorizationServiceURL: " + mAuthorizationServiceURL);
	}

	public String getCountries() {
		if (PrintHelper.countries == null) {
			PrintHelper.countries = new HashMap<String, String>();
		} else {
			PrintHelper.countries.clear();
		}
		String url = mRetailerCatalogURL + "countries?languageCultureName=" + language;
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("getCountries, status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "getCountries received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "getCountries received a " + response.getStatusLine().getStatusCode());
		}

		if (!result.equals("")) {
			try {
				JSONObject jsonObj = new JSONObject(result);
				if (jsonObj != null && jsonObj.has("Countries")) {
					JSONArray jsonCountries = jsonObj.getJSONArray("Countries");
					if (jsonCountries != null) {
						for (int i = 0; i < jsonCountries.length(); i++) {
							JSONObject jsonCountry = jsonCountries.getJSONObject(i);
							String countryCode = "", countryName = "";
							if (jsonCountry.has("CountryCode")) {
								countryCode = jsonCountry.getString("CountryCode");
							}
							if (jsonCountry.has("LocalizedCountryName")) {
								countryName = jsonCountry.getString("LocalizedCountryName");
							}
							if (!countryCode.equals("") && !countryName.equals("")) {
								PrintHelper.countries.put(countryCode, countryName);
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	Bitmap bm;

	/*************
	 * N2R Methods
	 * 
	 * @param storeID
	 ********************/
	public String SetStoreID(Context con, String storeID) {
		String retailerID = PreferenceManager.getDefaultSharedPreferences(con).getString("selectedRetailerId", "");
		if(AppContext.getApplication().isInStoreCloud()){
			storeID = AppConstants.IN_STORE_ID;
			retailerID = AppContext.getApplication().getInStoreCloudRetailerID();
		}
		String urlStr = mShoppingCartServiceURL + PrintHelper.cartID + "/store?retailerId=" + retailerID + "&storeId=" + storeID;
		Log.e(TAG, "SetStoreID url: " + urlStr);
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return "";
		}
		URI uri = null;
		try {
			uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			return "";

		}
		try {
			url = uri.toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return "";

		}

		// httpPut = new HttpPut(mShoppingCartServiceURL + PrintHelper.cartID +
		// "/store?retailerId=" + mRetailerID + "&storeId=" + storeID);
		httpPut = new HttpPut(url.toString());
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");
		String result = "";
		try {
			response = httpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				result = sb.toString();
				System.out.print("SetStoreID Status code: " + response.getStatusLine().getStatusCode() + " result: " + result);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200) {
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "SetStoreID() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "SetStoreID() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String GetRequiredContactInformation(Context con) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		SharedPreferences.Editor editor = prefs.edit();
		String selectedCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		String url = mRetailerCatalogServiceURL + (selectedCountryCode.equals("") ? "/" : "?countryCode=" + selectedCountryCode);
		httpGet = new HttpGet(url);
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		InputStream is = null;
		Log.d(TAG, "GetRequiredContactInformation url: " + url);
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		}
		String result = "";
		if (response == null) {
			result = "";
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print("GetRequiredContactInformation result: " + result);

				JSONObject jObject = new JSONObject(result);
				JSONArray retailers = jObject.getJSONArray("Retailers");

				PrintHelper.requiredContactInfos = new HashMap<String, ArrayList<Integer>>();
				editor.putString("retailerIdPayOnline", "");
				editor.putBoolean("ifCanShipToHome", false);
				editor.putBoolean("ifCanPayOnStore", false);
				editor.putBoolean("ifGetRequiredInfo", false);
				editor.putBoolean("ifCanFollowCLOLite", false);
				AppContext app = AppContext.getApplication();
				app.setIsInStoreCloud(false);
				app.setInStoreCloundRetailerID("");
				for (int i = 0; i < retailers.length(); i++) {
					ArrayList<Integer> requiredContactInfo = new ArrayList<Integer>();
					JSONObject obj = retailers.getJSONObject(i);
					String retailerId = obj.getString("Id");
					if (obj.getBoolean("PayOnline")) {
						editor.putString("retailerIdPayOnline", retailerId);
					}
					if(obj.getBoolean("InStore")){
						app.setIsInStoreCloud(true);
						app.setInStoreCloundRetailerID(retailerId);
					}
					if (obj.getBoolean("ShipToHome")) {
						editor.putBoolean("ifCanShipToHome", true);
						if (obj.getBoolean("CLOLite")) {
							editor.putBoolean("ifCanFollowCLOLite", true);
						}
					} else {
						editor.putBoolean("ifCanPayOnStore", true);
					}
					JSONArray arry = obj.getJSONArray("RequiredCustomerInfo");
					for (int j = 0; j < arry.length(); j++) {
						requiredContactInfo.add(Integer.parseInt(arry.get(j).toString()));
					}
					PrintHelper.requiredContactInfos.put(retailerId, requiredContactInfo);
					JSONObject obj2 = obj.getJSONObject("CartLimit");
					editor.putString(retailerId + "maxPriceStr", obj2.getString("PriceStr"));
					editor.putInt(retailerId + "maxPrice", obj2.getInt("Price"));
				}
				editor.commit();
				result = "success";
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "getPrintProducts() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "getPrintProducts() received a " + response.getStatusLine().getStatusCode());
		}
		if (result.equals("success")) {
			editor.putBoolean("ifGetRequiredInfo", true);
			editor.commit();
		}
		return result;
	}

	public List<Retailer> getRetailersOfferingProductsTask(String productDescriptionId) {
		int count = 0;
		List<Retailer> retailers = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String countryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		String url = mRetailerCatalogServiceURL + "/offering-products?productDescriptionIds=" + productDescriptionId + "&countryCode=" + countryCode;
		while (retailers == null && count < connTryTimes) {
			String result = httpGetTask(url, "getRetailersOfferingProductsTask");
			if (!"".equals(result)) {
				Parse mParse = new Parse();
				retailers = mParse.parseRetailers(result);
			}
			count++;
		}
		return retailers;
	}

	public String SetCustomerInformation(Context con) {
		boolean isShipToHome = false;
		StringEntity URL = null;
		httpPut = new HttpPut(mShoppingCartServiceURL + PrintHelper.cartID + "/customer");
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");
		String result = "", result1 = "";
		JSONObject customer = new JSONObject();
		JSONArray addressArray = new JSONArray();
		JSONObject address = new JSONObject();
		JSONObject addressParent = new JSONObject();
		JSONObject attributes = new JSONObject();
		if (PrintHelper.orderType == 2) {
			isShipToHome = true;
		}
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);

			String firstName = prefs.getString("firstName", "none");
			String lastName = prefs.getString("lastName", "none");
			String phone = prefs.getString("phone", "none");
			String email = prefs.getString("email", "none");

			String countryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
			if (countryCode.equals("")) {
				countryCode = "US";
			}
			String none = "none";

			String shipFirstName = prefs.getString("firstNameShip", "none");
			String shipLastName = prefs.getString("lastNameShip", "none");
			String shipEmail = prefs.getString("emailShip", "none");
			String shipPhone = prefs.getString("phoneShip", "none");
			String shipAddress1 = prefs.getString("addressOneShip", "none");
			String shipAddress2= prefs.getString("addressTwoShip", "none");
			String shipCity = prefs.getString("cityShip", "none");
			String shipStateProvince = prefs.getString("stateShip", "none");
			String shipZip = prefs.getString("zipcodeShip", "none");
			/*
			 * String shipFirstName = "asdf"; String shipLastName = "asdf";
			 * String shipAddress1 = "asdf"; String shipCity = "asdf"; String
			 * shipStateProvince = "NY"; String shipZip = "12345-6789";
			 */

			customer.put("FirstName", firstName);
			customer.put("LastName", lastName);
			customer.put("Phone1", phone);
			customer.put("Email", email);
			customer.put("Language", language);

			address.put("FirstName", isShipToHome ? shipFirstName : firstName);
			address.put("LastName", isShipToHome ? shipLastName : lastName);
			// 1: pick up in store,2:Home Delivery
			if (isShipToHome) {
				address.put("Email", shipEmail);
			}
			address.put("Address1", isShipToHome ? shipAddress1 : none);
			address.put("Address2", isShipToHome ? shipAddress2 : none);
			address.put("City", isShipToHome ? shipCity : none);
			address.put("StateProvince", isShipToHome ? shipStateProvince : none);
			address.put("PostalCode", isShipToHome ? shipZip : none);
			address.put("Country", countryCode);
			address.put("Phone", isShipToHome ? shipPhone : phone);

			addressParent.put("Id", 0);
			addressParent.put("Address", address);
			addressParent.put("AddressType", 0);
			addressArray.put(addressParent);

			if (isShipToHome) {
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
			attributes.put("Value", "FALSE");

			customer.put("Attributes", attributes);
			customer.put("Addresses", addressArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			String test = customer.toString();
			URL = new StringEntity(test, "UTF-8");
			URL.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		httpPut.setEntity(URL);
		try {
			response = httpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("SetCustomerInformation Status code: " + response.getStatusLine().getStatusCode() + " Result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "SetCustomerInformation() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "SetCustomerInformation() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}
	
	public List<ColorEffect> getAvailableColorEffect2Task(){
		int count = 0;
		List<ColorEffect> colorEffects = null;
		String url = mImageServiceURL + "/availableColorEffects2?language=" + language;
		while(colorEffects==null && count<connTryTimes){
			String result = httpGetTask(url, "getAvailableColorEffect2");
			if(!result.equals("")){
				colorEffects = parseColorEffects(result);
			}
			count ++;
		}
		return colorEffects;
	}
	
	public boolean setColorEffectTask(String contentId, int colorIndex){
		String url = mImageEditingServiceURL + contentId + "/colorEffect?value=" + colorIndex;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			String result = httpPutTask(url, "", "setColorEffectTask");
			succeed = parseSimpleError(result);
			count ++;
		}
		return succeed;	
	}
	
	public boolean setAutoRedEyeTask(String contentId, boolean autoRedEye) {
		String url = mImageEditingServiceURL + contentId + "/autoredeye?enable=" + autoRedEye;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			String result = httpPutTask(url, "", "setAutoRedEyeTask");
			succeed = parseSimpleError(result);
			count ++;
		}
		return succeed;
	}
	
	public boolean setKPTLevelTask(String contentId, int levelIndex) {
		String url = mImageEditingServiceURL + contentId + "/kpt?level=" + levelIndex;
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			String result = httpPutTask(url, "", "setKPTLevelTask");
			succeed = parseSimpleError(result);
			count ++;
		}
		return succeed;
	}
	
	public ROI flipImageTask(String contentId, boolean dirction) {
		String url = mImageEditingServiceURL + contentId + "/flip?direction=" + (dirction?"horizontal":"vertical");
		String result = "";
		ROI roi = null;
		int count = 0;
		while("".equals(result) && count<connTryTimes){
			result = httpPostTask(url, "", "flipImageTask");
			roi = parseROI(result);
			count ++;
		}
		return roi;
	}
	
	public TextBlock createTextBlockTask(String texts, String fontName) {
		String url = mTextBlockURL + "/textblocks/";
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
		Parse parse = new Parse();
		while(textBlock==null && count<connTryTimes){
			String result = httpPostTask(url, postData, "createTextBlockTask");
			List<TextBlock> textBlocks = parse.parseTextBlocks(result);
			if(textBlocks!=null && textBlocks.size()>0){
				textBlock = textBlocks.get(0);
			}
			count ++;
		}
		return textBlock;
	}

	public String CreateCart(Context con) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		String currentCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		if (currentCountryCode.equals("")) {
			Log.d(TAG, "currentCountryCode == null, getGreetingCardThemes");
			currentCountryCode = Locale.getDefault().getCountry();
			// TODO: hard code here for DM
			String appName = mContext.getPackageName();
			if (appName.contains(MainMenu.DM_COMBINED_PACKAGE_NAME)) {
				currentCountryCode = "DE";
			}
		}
		String id = "";
		StringEntity URL = null;
		String result = "", result1 = "";
		httpPost = new HttpPost(mShoppingCartServiceURL);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");
		JSONObject obj2 = new JSONObject();
		try {
			obj2.put("Language", language);
			obj2.put("Country", currentCountryCode);
			obj2.put("EnhancedPricing", "true");
			Log.d(TAG, "CreateCart Language[" + language + "] Country[" + currentCountryCode + "]");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			String test = obj2.toString();
			URL = new StringEntity(test, "UTF-8");
			URL.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		httpPost.setEntity(URL);
		Log.d(TAG, "CreateCart url:" + mShoppingCartServiceURL + obj2.toString());
		try {
			response = httpClient.execute(httpPost, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("CreateCart StatusCode: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 201) {
			result = result1;
			try {
				JSONObject jObject = new JSONObject(result);
				JSONObject cartJObject;
				cartJObject = jObject.getJSONObject("Cart");
				PrintHelper.cartID = cartJObject.get("Id").toString();
				id = PrintHelper.cartID;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "CreateCart() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			StringBuilder sb = new StringBuilder();
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			result = sb.toString();
			id = result;
		}
		return id;
	}
	
	public String removeAllProducts(){
		String result = "" ;
		String url = mShoppingCartServiceURL + PrintHelper.cartID + "/remove-all-items";
		result  = httpPutTask(url, "", "removeAllProducts");
		Parse paser = new Parse();
		Cart cart = paser.parseCart(result);
		if(cart!=null){
			return result ;
		}else {
			return "";
		}
		
	}
	
	public String removeProducts(Context context ,List<ProductInfo> productInfos){
		String id = "" ;
		String result = "" ;
		if(productInfos!=null && productInfos.size() >0){
			HttpDeleteWithBody httpDelete = null;
			StringEntity stringEntity = null;
			String url = mShoppingCartServiceURL + PrintHelper.cartID + "/item";
			httpDelete = new HttpDeleteWithBody(url);
			InputStream is = null;
			String token = getAuthorizationToken();
			httpDelete.addHeader("Authorization", "Basic " + token);
			httpDelete.addHeader("Accept", "application/json");
			httpDelete.addHeader("ContentType", "application/json");
			JSONArray array = new JSONArray();
			for (ProductInfo productInfo : productInfos) {
				if (productInfo.cartItemID.contains("Additional")){
					continue ;
				}
				JSONObject obj = new JSONObject();
				try {
					obj.put("ProductId", productInfo.ProductId);
					obj.put("ProductQuantity", productInfo.quantity) ;
					array.put(obj) ;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			try {
				String entityString = array.toString();
				stringEntity = new StringEntity(entityString, "UTF-8");
				stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpDelete.setEntity(stringEntity);
				
				response = httpClient.execute(httpDelete, localContext);
				if (response != null) {
					HttpEntity entity = response.getEntity();
					is = entity.getContent();

					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					result = sb.toString();
					Log.d(TAG, "RemoveProducts StatusCode: " + response.getStatusLine().getStatusCode() + " result: " + result);
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Error parsing data " + e.toString());
			}
			
		}
		
		return result ;
	}
	
	/**
	 * add by sunny 
	 * @param context
	 * @param products
	 * @return
	 */
	public String addItemsToCart(Context context , List<ProductInfo> products){
		if(products!=null && products.size()>0){
			String result = "" ;
			String result1 = "" ;
			StringEntity entityContent = null ;
			httpPut = new HttpPut(mShoppingCartServiceURL + PrintHelper.cartID);
			InputStream is = null;
			String token = getAuthorizationToken();
			httpPut.setHeader("Authorization", "Basic " + token);
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("ContentType", "application/json");
			JSONArray arry = new JSONArray();
			for (ProductInfo productInfo : products) {
				if (productInfo.cartItemID.contains("Additional")){
					continue;
				}
					
				JSONObject obj = new JSONObject();
				try {
					obj.put("ProductId",productInfo.ProductId);
					obj.put("ProductQuantity",productInfo.quantity+"");
					arry.put(obj);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String entityString = arry.toString();
			try {
				entityContent = new StringEntity(entityString, HTTP.UTF_8);
				entityContent.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpPut.setEntity(entityContent);
			
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
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (response != null) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					result1 = sb.toString();
					System.out.print("AddItemsToCart Status code: " + response.getStatusLine().getStatusCode() + " result: " + result1
							+ " Request Post Data: " + arry.toString() + "\n");
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
				Log.d(TAG, "AddItemsToCart() received a 401");
				mAuthorizationToken = "";
				getAuthorizationToken();
				result = "" ;
			} else {
				Log.d(TAG, "AddItemsToCart() received a " + response.getStatusLine().getStatusCode());
				result = "";
			}
			
			return result;
			
		}else {
			return "" ;
		}
		
	}
	
	/**
	 * @deprecated
	 * @param con
	 * @return
	 */
	public String AddItemsToCart(Context con) {
		String id = "";
		String result = "", result1 = "";
		StringEntity URL = null;
		httpPut = new HttpPut(mShoppingCartServiceURL + PrintHelper.cartID);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");
		JSONArray arry = new JSONArray();
		if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
			GreetingCardManager manager = GreetingCardManager.getGreetingCardManager(con);
			JSONObject obj2 = new JSONObject();
			try {
				obj2.put("ProductId", manager.getGreetingCardProduct().id);
				obj2.put("ProductQuantity", manager.getGreetingCardProduct().count);
				arry.put(obj2);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i < PrintHelper.cartGroups.size(); i++) {
			for (int j = 0; j < PrintHelper.cartChildren.get(i).size(); j++) {
				CartItem temp = PrintHelper.cartChildren.get(i).get(j);
				// TODO Hard code here.
				if (temp.cartItemID.contains("Additional"))
					continue;
				id = temp.serverID;
				JSONObject obj2 = new JSONObject();
				try {
					if (PrintHelper.inQuickbook) {
						/*obj2.put("ProductId", PreferenceManager.getDefaultSharedPreferences(con).getString(PrintHelper.sPhotoBookID, ""));
						id = PreferenceManager.getDefaultSharedPreferences(con).getString(PrintHelper.sPhotoBookID, "");*/
						Photobook photobook = AppContext.getApplication().getPhotobook();
						obj2.put("ProductId", photobook.id);
						id = photobook.id;
					} else {
						obj2.put("ProductId", id);
					}
					obj2.put("ProductQuantity", temp.quantity);
					arry.put(obj2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			String test = arry.toString();
			URL = new StringEntity(test, "UTF-8");
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("AddItemsToCart Status code: " + response.getStatusLine().getStatusCode() + " result: " + result1
						+ " Request Post Data: " + arry.toString() + "\n");
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
			Log.d(TAG, "AddItemsToCart() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
			id = result;
		} else {
			Log.d(TAG, "AddItemsToCart() received a " + response.getStatusLine().getStatusCode());
			id = result;
		}
		if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
			return result;
		}
		return id;
	}
	
	
	
	
	
	
	
	
	

	public String ConvertToOrder(Context con) {
		String result = "", result1 = "";
		boolean isTestOrder = PreferenceManager.getDefaultSharedPreferences(con).getBoolean(PrintHelper.INCLUDE_TEST_STORES, false);
		String url = mShoppingCartServiceURL + PrintHelper.cartID + "/convert-to-order2?isTestOrder=" + (isTestOrder ? "true" : "false");
		Log.d(TAG, "ConvertToOrder url: " + url);
		httpPost = new HttpPost(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

		HttpParams myParams2 = new BasicHttpParams();
		HttpProtocolParams.setVersion(myParams2, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(myParams2, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(myParams2, connection_timeout);
		HttpConnectionParams.setSoTimeout(myParams2, 600000);
		DefaultHttpClient httpClient2 = new DefaultHttpClient(myParams2);

		try {
			long start = System.currentTimeMillis();
			response = httpClient2.execute(httpPost, localContext);
			long end = System.currentTimeMillis();
			Log.d(TAG,"ConvertOrder respone's waiting time is : " + (end - start) + "ms");
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result1 = sb.toString();
				System.out.print("ConvertToOrder Status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
				parseOrderPricing(result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200) {
			result = result1;
			try {
				JSONObject jObject = new JSONObject(result);
				JSONObject jObject2 = jObject.getJSONObject("NewOrder");
				if (jObject2.has("OrderId"))
					PrintHelper.orderID = jObject2.getString("OrderId");
				else
					PrintHelper.orderID = "-1";
				/*
				 * if(jObject3.has("PriceStr")) PrintHelper.totalCost =
				 * jObject3.getString("PriceStr"); else PrintHelper.totalCost =
				 * "See Store";
				 */
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
				result = "";
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "ConvertToOrder() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "ConvertToOrder() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String PlaceOrder(Context con) {
		String id = "";
		httpGet = new HttpGet(mShoppingCartServiceURL + PrintHelper.cartID + "/place-order");
		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("ContentType", "application/json");
		try {
			response = httpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "PlaceOrder() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "PlaceOrder() received a " + response.getStatusLine().getStatusCode());
		}
		return id;
	}

	public float getPrice(String products) {
		int index = products.indexOf("=");
		String pID = products.substring(0, index);
		String pNO = products.substring(index + 1);
		String pMaxPrice = "0.0";
		for (PrintProduct product : PrintHelper.products) {
			if (product.getId().equals(pID)) {
				pMaxPrice = product.getMaxPrice();
				break;
			}
		}
		return Float.valueOf(pMaxPrice) * Integer.parseInt(pNO);
	}
	
	public Cart checkCouponsTask(String cartId, String couponCode){
		String url = mShoppingCartServiceURL + cartId + "/discount?discountCode=" + couponCode;
		Parse paser = new Parse();
		Cart cart = null;
		int count = 0;
		while(cart==null && count<connTryTimes){
			String result = httpPutTask(url, "", "checkCouponsTask");
			cart = paser.parseCart(result);
			count ++;
		}
		return cart;
	}

	public Cart PriceProducts2(Context con, String products, String retailerID) {
		String returnResult = "";
		String result = "", url = "";
		InputStream is = null;
		String currentCountryCode = PreferenceManager.getDefaultSharedPreferences(mContext).getString(MainMenu.SelectedCountryCode, "");
		Cart cart = null;
		try {
			String cartId = PrintHelper.cartID == null ? "" : PrintHelper.cartID;
			url = mShoppingCartServiceURL + "pricing3?cartId=" + cartId + "&retailerId=" + retailerID + "&language=" + language + "&products="	+ products + "&country=" + currentCountryCode;
			if(PrintHelper.couponCode!=null && !PrintHelper.couponCode.equals("")){
				url += "&discounts=" + PrintHelper.couponCode;
			}
			httpGet = new HttpGet(url);
			Log.d(TAG, "url: " + url + ", PriceProducts: " + products);
			String token = getAuthorizationToken();
			httpGet.setHeader("Authorization", "Basic " + token);
			httpGet.setHeader("Accept", "application/json");

			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			returnResult = sb.toString();
			System.out.print("PriceProducts(), result: " + returnResult);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			try {
				Parse parse = new Parse();
				cart = parse.parseCart(returnResult);
				boolean isTaxWillBeCalculatedByRetailer = cart.pricing.taxWillBeCalculatedByRetailer;
				SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(con);
				Editor mEditor = mSharedPreferences.edit();
				mEditor.putBoolean("TaxWillBeCalculatedByRetailer", isTaxWillBeCalculatedByRetailer);
				mEditor.commit();
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "PriceProducts() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "PriceProducts() received a " + response.getStatusLine().getStatusCode());
		}

		return cart;
	}

	private Pricing parsePricing(String result) {
		Pricing pricing = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			JSONObject jsObj2 = null;
			if(jsObj.has("Cart")){
				jsObj2 = jsObj.getJSONObject("Cart");
			} else {
				return null;
			}
			if (jsObj2.has(Pricing.FLAG_PRICING)) {
				JSONObject jsPri = jsObj2.getJSONObject(Pricing.FLAG_PRICING);
				pricing = new Pricing();
				if (jsPri.has(Pricing.FLAG_CURRENCY)) {
					pricing.currency = jsPri.getString(Pricing.FLAG_CURRENCY);
				}
				if (jsPri.has(Pricing.FLAG_CURRENCY_SYMBOL)) {
					pricing.currencySymbol = jsPri.getString(Pricing.FLAG_CURRENCY_SYMBOL);
				}
				if (jsPri.has(Pricing.FLAG_LINE_ITEMS)) {
					JSONArray jsLineItems = jsPri.getJSONArray(Pricing.FLAG_LINE_ITEMS);
					for (int i = 0; i < jsLineItems.length(); i++) {
						JSONObject jsItem = jsLineItems.getJSONObject(i);
						LineItem item = new LineItem();
						if (jsItem.has(Pricing.FLAG_LI_NAME)) {
							item.name = jsItem.getString(Pricing.FLAG_LI_NAME);
						}
						if (jsItem.has(Pricing.FLAG_LI_PRODUCT_DESCRIPTION_ID)) {
							item.productDescriptionId = jsItem.getString(Pricing.FLAG_LI_PRODUCT_DESCRIPTION_ID);
						}
						if (jsItem.has(Pricing.FLAG_LI_QUANTITY)) {
							item.quantity = jsItem.getInt(Pricing.FLAG_LI_QUANTITY);
						}
						if (jsItem.has(Pricing.FLAG_LI_UNIT_PRICE)) {
							JSONObject jsUp = jsItem.getJSONObject(Pricing.FLAG_LI_UNIT_PRICE);
							item.unitPrice = new UnitPrice();
							if (jsUp.has(UnitPrice.FLAG_PRICE)) {
								item.unitPrice.price = jsUp.getDouble(UnitPrice.FLAG_PRICE);
							}
							if (jsUp.has(UnitPrice.FLAG_PRICE_STR)) {
								item.unitPrice.priceStr = jsUp.getString(UnitPrice.FLAG_PRICE_STR);
							}
						}
						if (jsItem.has(Pricing.FLAG_LI_TOTAL)) {
							JSONObject jsUp = jsItem.getJSONObject(Pricing.FLAG_LI_TOTAL);
							item.totalPrice = new UnitPrice();
							if (jsUp.has(UnitPrice.FLAG_PRICE)) {
								item.totalPrice.price = jsUp.getDouble(UnitPrice.FLAG_PRICE);
							}
							if (jsUp.has(UnitPrice.FLAG_PRICE_STR)) {
								item.totalPrice.priceStr = jsUp.getString(UnitPrice.FLAG_PRICE_STR);
							}
						}
						PrintHelper.lineItems.add(item);
					}
				}
				if (jsPri.has(Pricing.FLAG_SUB_TOTAL)) {
					JSONObject jsSt = jsPri.getJSONObject(Pricing.FLAG_SUB_TOTAL);
					pricing.subTotal = new UnitPrice();
					if (jsSt.has(UnitPrice.FLAG_PRICE)) {
						pricing.subTotal.price = jsSt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if (jsSt.has(UnitPrice.FLAG_PRICE_STR)) {
						pricing.subTotal.priceStr = jsSt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if (jsPri.has(Pricing.FLAG_SAH_TOTAL)) {
					JSONObject jsGt = jsPri.getJSONObject(Pricing.FLAG_SAH_TOTAL);
					pricing.shipAndHandling = new UnitPrice();
					if (jsGt.has(UnitPrice.FLAG_PRICE)) {
						pricing.shipAndHandling.price = jsGt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if (jsGt.has(UnitPrice.FLAG_PRICE_STR)) {
						pricing.shipAndHandling.priceStr = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if (jsPri.has(Pricing.FLAG_GRAND_TOTAL)) {
					JSONObject jsGt = jsPri.getJSONObject(Pricing.FLAG_GRAND_TOTAL);
					pricing.grandTotal = new UnitPrice();
					if (jsGt.has(UnitPrice.FLAG_PRICE)) {
						pricing.grandTotal.price = jsGt.getDouble(UnitPrice.FLAG_PRICE);
					}
					if (jsGt.has(UnitPrice.FLAG_PRICE_STR)) {
						pricing.grandTotal.priceStr = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
				if (jsPri.has(Pricing.FLAG_TAX_BE_CAL_BY_RETAILER)) {
					pricing.taxWillBeCalculatedByRetailer = jsPri.getBoolean(Pricing.FLAG_TAX_BE_CAL_BY_RETAILER);
				}
				if (jsPri.has(Pricing.FLAG_TAXES_ARE_ESTIMATED)) {
					pricing.taxesAreEstimated = jsPri.getBoolean(Pricing.FLAG_TAXES_ARE_ESTIMATED);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pricing;
	}

	// get the order final price . add by song
	private void parseOrderPricing(String result) {
		JSONObject jsObj;
		try {
			jsObj = new JSONObject(result);
			if (jsObj.has(Pricing.FLAG_NEWORDER)) {
				JSONObject jsPri = jsObj.getJSONObject(Pricing.FLAG_NEWORDER);
				if (jsPri.has(Pricing.FLAG_GRAND_TOTAL)) {
					JSONObject jsGt = jsPri.getJSONObject(Pricing.FLAG_GRAND_TOTAL);
					if (jsGt.has(UnitPrice.FLAG_PRICE_STR)) {
						PrintHelper.totalCost = jsGt.getString(UnitPrice.FLAG_PRICE_STR);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public List<CountryInfo> getCountryInfoTask(String countryCodes) {
		int count = 0;
		List<CountryInfo> countryInfos = null;
		String url = mRetailerCatalogServiceURL + "/country-infos?countryCodes=" + countryCodes + "&languageCultureName=" + language;
		while (countryInfos == null && count < connTryTimes) {
			String result = httpGetTask(url, "getCountryInfoTask");
			if (!result.equals("")) {
				Parse mParse = new Parse();
				countryInfos = mParse.parseCountryInfo(result);
			}
			count++;
		}
		return countryInfos;
	}

	public String CloneImage(String imageId) {
		String id = "";
		String result = "";
		httpPost = new HttpPost(mImageEditingServiceURL + "/" + imageId + "/clone");
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("ContentType", "application/json");
		httpPost.setHeader("Accept", "application/json");
		try {
			response = httpClient.execute(httpPost, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() > 199 && response.getStatusLine().getStatusCode() < 300) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				Log.d(TAG, result.toString());
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}

			try {
				JSONObject obj1 = new JSONObject(result);
				JSONObject obj2 = obj1.getJSONObject("Image");
				id = obj2.getString("Id");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "CloneImage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "CloneImage() received a " + response.getStatusLine().getStatusCode());
		}
		return id;
	}

	public String getImageInfo(String imageId) {
		Log.d(TAG, "get Image Info for imageId=" + imageId);
		String result = "";
		httpGet = new HttpGet(mImageEditingServiceURL + "/" + imageId);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("ContentType", "application/json");
		httpGet.setHeader("Accept", "application/json");
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() > 199 && response.getStatusLine().getStatusCode() < 300) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				Log.d(TAG, result.toString());
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "getImageInfo() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "getImageInfo() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public boolean isUploadedImageSameResAsOriginal(String originalImageURL, String imageId) {
		boolean checkPassed = false;
		Log.d(TAG, "checking isUploadedImageSameResAsOriginal() for imageId=" + imageId + " original image:" + originalImageURL);

		int originalWidth = -1;
		int originalHeight = -1;
		ExifInterface exif;
		try {
			exif = new ExifInterface(originalImageURL);
			if (exif != null) {
				originalWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
				originalHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
				;
			}
		} catch (IOException e1) {
			Log.e(TAG, "Fail to get Exif information from original image", e1);
		}
		if (originalWidth <= 0 || originalHeight <= 0) {
			Log.w(TAG, "Incorrect width or height value for original image");
			return checkPassed;
		}
		Log.d(TAG, "Original image width=" + originalWidth + " height=" + originalHeight);

		String result = "";
		httpGet = new HttpGet(mImageEditingServiceURL + "/" + imageId);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("ContentType", "application/json");
		httpGet.setHeader("Accept", "application/json");
		response = null;
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (response == null) {
			Log.d(TAG, "Fail to get Image Info from server: NULL response");
		} else if (response.getStatusLine().getStatusCode() > 199 && response.getStatusLine().getStatusCode() < 300) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				Log.i(TAG, result.toString());

				JSONObject obj1 = new JSONObject(result);
				JSONObject obj2 = obj1.getJSONObject("ImageInfo");
				int widthNow = obj2.getInt("Width");
				int heightNow = obj2.getInt("Height");
				Log.d(TAG, "On Server: image width=" + widthNow + " height=" + heightNow);
				if ((originalWidth == widthNow && originalHeight == heightNow) || (originalWidth == heightNow && originalHeight == widthNow)) {
					checkPassed = true;
				}

			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.d(TAG, "get Image Info from server received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.d(TAG, "get Image Info from server received a " + response.getStatusLine().getStatusCode());
		}
		return checkPassed;
	}
	
	/**
	 *  add by Sunny 
	 * @param context
	 * @param printProductInfos
	 * @return
	 */
	public String createPrints(Context context ,List<ProductInfo> printProductInfos){
		String result = "" ;
		if(printProductInfos!=null && printProductInfos.size() > 0){
			StringEntity entityContent = null ;
			httpPost = new HttpPost( );
			InputStream is = null;
			String token = getAuthorizationToken();
			httpPost.setHeader("Authorization", "Basic " + token);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("ContentType", "application/json");
			String fileName;
			Bitmap bit = null;
			int outW;
			int outH;
			int tempInt;
			BitmapFactory.Options options = new Options();
			JSONArray arry = new JSONArray();	
		
			for (ProductInfo productInfo : printProductInfos) {
				if (!productInfo.productType.endsWith(AppConstants.PRINT_TYPE)){
					continue;
				}
				ROI roi = productInfo.roi;
				PhotoInfo photoInfo = productInfo.photoInfo;
				if(photoInfo.getPhotoSource().isFromPhone()){
					fileName = photoInfo.getPhotoPath() ;
					options.inJustDecodeBounds = true;
					bit = BitmapFactory.decodeFile(fileName, options);
					
					outW = options.outWidth;
					outH = options.outHeight;
					ExifInterface exif = null;
					try {
						exif = new ExifInterface(fileName);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if (exif != null && exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
						tempInt = outW;
						outW = outH;
						outH = tempInt;
					} else if (exif != null && exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
						tempInt = outW;
						outW = outH;
						outH = tempInt;
					}
				} else {
					outW = photoInfo.getWidth();
					outH = photoInfo.getHeight();
				}			
				
				if (roi == null) {
					double productWidth = Double.parseDouble(productInfo.width);
					double productHeight = Double.parseDouble(productInfo.height);
					double ratio = 1.0;
					if (productWidth > productHeight) {
						ratio = productWidth / productHeight;
					} else {
						ratio = productHeight / productWidth;
					}
					roi = PrintHelper.CalculateDefaultRoi(outW, outH, ratio);
					roi.x = roi.x / outW;
					roi.y = roi.y / outH;
					roi.w = roi.w / outW;
					roi.h = roi.h / outH;
					roi.ContainerH = outH;
					roi.ContainerW = outW;
				}
				String productDescription = productInfo.descriptionId;
				if (Integer.parseInt(productInfo.width) < Integer.parseInt(productInfo.height))
					productDescription = productInfo.descriptionId;

				List<PrintInfo> prints = AppContext.getApplication().getmPrints() ;
				for (PrintInfo printInfo : prints) {
					PhotoInfo photo = printInfo.getPhoto() ;
					if(photo.equals(productInfo.photoInfo)){
						productInfo.imageId = photo.getContentId() ;
					}
					
				}
				
				
				JSONObject obj2 = new JSONObject();
				try {
					obj2.put("X", (roi.x * outW));
					obj2.put("Y", (roi.y * outH));
					obj2.put("W", (roi.w * outW));
					obj2.put("H", (roi.h * outH));
					obj2.put("ContainerW", outW);
					obj2.put("ContainerH", outH);
					JSONObject obj = new JSONObject();
					obj.put("ProductDescriptionId", productDescription);
					obj.put("ImageId", productInfo.imageId);
					obj.put("ImageROI", obj2);
					arry.put(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			
			try {
				String test = arry.toString();
				entityContent = new StringEntity(test, "UTF-8");
				entityContent.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			// ensure that we are now pointing to the serviceprints service in case
			// we called clone image above
			httpPost = new HttpPost(mServicePrintsURL);
			httpPost.setHeader("Authorization", "Basic " + token);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("ContentType", "application/json");

			httpPost.setEntity(entityContent);
			
			try {
				Log.v(TAG, "CreatePrints Request Post Data:" + arry.toString());

				HttpParams mHttpParams = new BasicHttpParams();
				HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
				HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
				HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
				DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

				response = mDefaultHttpClient.execute(httpPost, localContext);
				if (response != null) {
					HttpEntity entity = response.getEntity();
					is = entity.getContent();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (response != null) {
				// Print the response string for test.
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					result = sb.toString();
					Log.v(TAG, "CreatePrints StatusCode: " + response.getStatusLine().getStatusCode() + " Request Post Data:" + arry.toString()
							+ " result: " + result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (response == null) {
			} else if (response.getStatusLine().getStatusCode() == 201) {
				try {
					JSONObject jObject = new JSONObject(result);
					JSONArray jArry = jObject.getJSONArray("StandardPrints");
					int count = 0;
					//TODO I don't know what the code below means,maybe cause issue add by Sunny
					for (ProductInfo productInfo : printProductInfos) {
						JSONObject jObject2 = jArry.getJSONObject(count);
						String id  = jObject2.getString("Id");
						productInfo.ProductId = id ;
						count++ ;
					}
					
					
				} catch (JSONException e) {
					Log.e("log_tag", "Error parsing data " + e.toString());
				}
			} else if (response.getStatusLine().getStatusCode() == 401) {
				Log.d(TAG, "CreatePrints() received a 401");
				mAuthorizationToken = "";
				getAuthorizationToken();
			} else {
				Log.d(TAG, "CreatePrints() received a " + response.getStatusLine().getStatusCode());
			}
//			mImageSelectionDatabase.close();
		}
		
		return result ; 
	}

	
	public String addImageFromWebTask(PhotoInfo image){
		String url = mImageEditingServiceURL.substring(0, mImageEditingServiceURL.lastIndexOf("/"));
		JSONArray jsArr = new JSONArray();
		JSONObject jsObj = new JSONObject();
		try {
			jsObj.put("originalURL", image.getSourceUrl());
			jsObj.put("pixelHeight", image.getHeight());
			jsObj.put("pixelWidth", image.getWidth());
			jsObj.put("supportsMetadata", false);
			jsArr.put(jsObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String result = httpPostTask(url, jsArr.toString(), "addImageFromWebTask");
		if(!"".equals(result)){
			JSONObject jsRes;
			try {
				jsRes = new JSONObject(result);
				if(jsRes.has("Images")){
					JSONArray jsImages = jsRes.getJSONArray("Images");
					if(jsImages.length()>0){
						JSONObject jsImage = jsImages.getJSONObject(0);
						if(jsImage.has("Id")){
							return jsImage.getString("Id");
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return "Error";
	}

	
	public String UploadPicture(Context context, PhotoInfo pi, boolean isSecondUpload){
		String filename = pi.getPhotoPath();
		String uri = pi.getLocalUri();
		Log.i(TAG, "start upload picture:" + filename + "  isSecondUpload:" + isSecondUpload);
		boolean isPNGFile = false;
		if (filename.toUpperCase().endsWith(".PNG")) {
			isPNGFile = true;
		}// add by song . if the file is .png will compress to .jpg
		String result = "";
		ContentResolver cr = context.getContentResolver();
		Uri url = Uri.parse(uri);
		InputStream is = null;
		InputStreamEntity entity = null;
		try {
			boolean newFile = false;
			String newFilePath = "";
			String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
			if (isPNGFile) {
				File folder = new File(tempFolder);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				String newLFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newL" + ".jpg";
				File newLFile = new File(newLFilePath);
				if (!newLFile.exists()) {
					Log.i(TAG, "UploadPicture() convert png to jpeg");
					int rr = ImageUtil.pngToJpg(filename, newLFilePath);
					Log.i(TAG, "convert png to jpeg finish. Result:" + rr);
				}

				if (!isSecondUpload) {
					String path = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newS" + ".jpg";

					File newSFile = new File(path);
					if (!newSFile.exists()) {
						compressPNGThumbToJPG(uri, PrintHelper.getThumbOfPNG(uri));
					}
					is = new FileInputStream(newSFile); // small file
				} else {
					// check if need to compress big image
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(newLFilePath, opts);
					int[] size = getCompressImageSize(opts.outWidth, opts.outHeight,pi);
					if (size == null) {
						// don't need to compress
						is = new FileInputStream(new File(newLFilePath));
					} else {
						// compress image
						String newLCFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newLC" + ".jpg";
						Log.i(TAG, "UploadPicture() PNG compress(resize) image from" + opts.outWidth + "x" + opts.outHeight + " to " + size[0]
								+ "x" + size[1]);
						int rr = ImageUtil.resizePic(newLFilePath, newLCFilePath, size[0], size[1]);
						Log.i(TAG, "Compress finish. Result:" + rr);

						is = new FileInputStream(new File(newLCFilePath));
					}

				}
			} else {
				if (!isSecondUpload) {
					ExifInterface exif = getFileExifInterface(uri);
					newFilePath = compressThumbToJPG(uri, exif);
					newFile = true;
				}
				if (newFile && !newFilePath.equals("")) {
					Log.w(TAG, "get InputStream from new file: " + newFilePath);
					is = new FileInputStream(new File(newFilePath));
				} else {
					Log.d(TAG, "UploadPicture() isSecondUpload: " + isSecondUpload + ", url: " + url);
					// check if need to compress big image
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(filename, opts);
					int[] size = getCompressImageSize(opts.outWidth, opts.outHeight,pi);
					if (size == null) {
						// don't need to compress,use original
						is = cr.openInputStream(url);
					} else {
						// compress image
						File folder = new File(tempFolder);
						if (!folder.exists()) {
							folder.mkdirs();
						}
						String cFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newLC" + ".jpg";
						Log.i(TAG, "UploadPicture() compress(resize) image from" + opts.outWidth + "x" + opts.outHeight + " to " + size[0] + "x"
								+ size[1]);
						int rr = ImageUtil.resizePic(filename, cFilePath, size[0], size[1]);
						Log.i(TAG, "Compress finish. Result:" + rr);

						is = new FileInputStream(cFilePath);
					}

				}
			}

			Log.i(TAG, "Picture Available Content Size: " + is.available());
			if(is.available()>0){
				entity = new InputStreamEntity(is, -1); 
				
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String id = "Error";

		if (entity == null) {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e(TAG, "UploadPicture() Abandon upload original image. entity == null");
			return id;
		}
		entity.setContentType("image/jpeg");
		entity.setChunked(false);
		String imageID = null;
		if (isSecondUpload && pi.getFlowType().isPhotoBookWorkFlow()) {
			for(Photobook photobook : AppContext.getApplication().getPhotobooks()){
				for (PhotoInfo photo : photobook.selectedImages) {
					if (photo.equals(pi)) {
						imageID = photobook.imageEditParams.get(photo)==null ? null : photobook.imageEditParams.get(photo).sUploadImageID;
						if (imageID == null) {
							try {
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							Log.e(TAG, "UploadPicture() Abandon upload original image. imageID == null");
							return id;
						}
						break;
					}
				}
			}
		}
		httpPost = new HttpPost(isSecondUpload && !TextUtils.isEmpty(imageID)  ? mUploadServiceURL + "?replace=" + imageID : mUploadServiceURL);
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "image/jpeg");
		httpPost.setEntity(entity);
		try {
			HttpParams mHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
			HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
			DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

			response = mDefaultHttpClient.execute(httpPost, localContext);
			if (response != null) {
				HttpEntity httpEntity = response.getEntity();
				is = httpEntity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				result = sb.toString();
				Log.e(TAG, "UploadPicture() StatusCode: " + response.getStatusLine().getStatusCode() + ", result: " + result);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (response == null) {
			Log.e(TAG, "UploadPicture() response == null");
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				JSONObject jObject = new JSONObject(result);
				JSONObject jObject2 = jObject.getJSONObject("Resource");
				id = jObject2.getString("Id");
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing data " + e.toString());
				id = "Error";
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "UploadPicture() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "UploadPicture() received a " + response.getStatusLine().getStatusCode() + ", id: " + id);
		}

		return id;
	}

	public String UploadPicture(Context context, String filename, String uri, boolean isSecondUpload,PhotoInfo pi) {
		Log.i(TAG, "start upload picture:" + filename + "  isSecondUpload:" + isSecondUpload);
		boolean isPNGFile = false;
		if (filename.toUpperCase().endsWith(".PNG")) {
			isPNGFile = true;
		}// add by song . if the file is .png will compress to .jpg
		String result = "";
		ContentResolver cr = context.getContentResolver();
		Uri url = Uri.parse(uri);
		InputStream is = null;
		InputStreamEntity entity = null;
		try {
			boolean newFile = false;
			String newFilePath = "";
			String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
			if (isPNGFile) {
				File folder = new File(tempFolder);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				String newLFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newL" + ".jpg";
				File newLFile = new File(newLFilePath);
				if (!newLFile.exists()) {
					Log.i(TAG, "UploadPicture() convert png to jpeg");
					int rr = ImageUtil.pngToJpg(filename, newLFilePath);
					Log.i(TAG, "convert png to jpeg finish. Result:" + rr);
				}

				if (!isSecondUpload) {
					String path = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newS" + ".jpg";

					File newSFile = new File(path);
					if (!newSFile.exists()) {
						compressPNGThumbToJPG(uri, PrintHelper.getThumbOfPNG(uri));
					}
					is = new FileInputStream(newSFile); // small file
				} else {
					// check if need to compress big image
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(newLFilePath, opts);
					int[] size = getCompressImageSize(opts.outWidth, opts.outHeight,pi);
					if (size == null) {
						// don't need to compress
						is = new FileInputStream(new File(newLFilePath));
					} else {
						// compress image
						String newLCFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newLC" + ".jpg";
						Log.i(TAG, "UploadPicture() PNG compress(resize) image from" + opts.outWidth + "x" + opts.outHeight + " to " + size[0]
								+ "x" + size[1]);
						int rr = ImageUtil.resizePic(newLFilePath, newLCFilePath, size[0], size[1]);
						Log.i(TAG, "Compress finish. Result:" + rr);

						is = new FileInputStream(new File(newLCFilePath));
					}

				}
			} else {
				if (!isSecondUpload) {
					ExifInterface exif = getFileExifInterface(uri);
					/*
					 * if(exif.getAttributeInt("Orientation", 0) ==
					 * ExifInterface.ORIENTATION_NORMAL ||
					 * exif.getAttributeInt("Orientation", 0) ==
					 * ExifInterface.ORIENTATION_ROTATE_90 ||
					 * exif.getAttributeInt("Orientation", 0) ==
					 * ExifInterface.ORIENTATION_ROTATE_180 ||
					 * exif.getAttributeInt("Orientation", 0) ==
					 * ExifInterface.ORIENTATION_ROTATE_270){
					 */
					// if(true){
					newFilePath = compressThumbToJPG(uri, exif);
					newFile = true;
					/*
					 * } else { final String[] PROJECTION = new String[]
					 * {BaseColumns._ID, MediaColumns.DATA}; Cursor c =
					 * context.getContentResolver
					 * ().query(Thumbnails.EXTERNAL_CONTENT_URI, PROJECTION,
					 * "image_id=" +
					 * Integer.parseInt(uri.substring(uri.lastIndexOf("/") + 1,
					 * uri.length())), null, null); c.moveToFirst(); url =
					 * (c.getCount() == 0 ? url :
					 * ContentUris.withAppendedId(Thumbnails
					 * .EXTERNAL_CONTENT_URI, c.getLong(0))); c.close(); }
					 */
				}
				if (newFile && !newFilePath.equals("")) {
					Log.w(TAG, "get InputStream from new file: " + newFilePath);
					is = new FileInputStream(new File(newFilePath));
				} else {
					Log.d(TAG, "UploadPicture() isSecondUpload: " + isSecondUpload + ", url: " + url);
					// check if need to compress big image
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(filename, opts);
					int[] size = getCompressImageSize(opts.outWidth, opts.outHeight,pi);
					if (size == null) {
						// don't need to compress,use original
						is = cr.openInputStream(url);
					} else {
						// compress image
						File folder = new File(tempFolder);
						if (!folder.exists()) {
							folder.mkdirs();
						}
						String cFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newLC" + ".jpg";
						Log.i(TAG, "UploadPicture() compress(resize) image from" + opts.outWidth + "x" + opts.outHeight + " to " + size[0] + "x"
								+ size[1]);
						int rr = ImageUtil.resizePic(filename, cFilePath, size[0], size[1]);
						Log.i(TAG, "Compress finish. Result:" + rr);

						is = new FileInputStream(cFilePath);
					}

				}
			}

			Log.i(TAG, "Picture Available Content Size: " + is.available());
			if(is.available()>0){
				entity = new InputStreamEntity(is, -1); 
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String id = "Error";

		if (entity == null) {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.e(TAG, "UploadPicture() Abandon upload original image. entity == null");
			return id;
		}
		entity.setContentType("image/jpeg");
		entity.setChunked(false);
		String imageID = null;
		if (isSecondUpload && PrintHelper.inQuickbook) {
			Photobook photobook = AppContext.getApplication().getPhotobook();
			//for (String uriString : PrintHelper.selectedImageUrls) {
			for (PhotoInfo photo : photobook.selectedImages) {
				if (photo.getLocalUri().equals(uri)) {
					//imageID = PrintHelper.selectedImages.get(uriString) == null ? null : PrintHelper.selectedImages.get(uriString).sUploadImageID;
					imageID = photobook.imageEditParams.get(photo)==null ? null : photobook.imageEditParams.get(photo).sUploadImageID;
					if (imageID == null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						Log.e(TAG, "UploadPicture() Abandon upload original image. imageID == null");
						return id;
					}
					break;
				}
			}
		}
		httpPost = new HttpPost(isSecondUpload && imageID != null ? mUploadServiceURL + "?replace=" + imageID : mUploadServiceURL);
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "image/jpeg");
		httpPost.setEntity(entity);
		try {
			HttpParams mHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
			HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
			DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

			response = mDefaultHttpClient.execute(httpPost, localContext);
			if (response != null) {
				HttpEntity httpEntity = response.getEntity();
				is = httpEntity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				result = sb.toString();
				Log.e(TAG, "UploadPicture() StatusCode: " + response.getStatusLine().getStatusCode() + ", result: " + result);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (response == null) {
			Log.e(TAG, "UploadPicture() response == null");
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				JSONObject jObject = new JSONObject(result);
				JSONObject jObject2 = jObject.getJSONObject("Resource");
				id = jObject2.getString("Id");
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing data " + e.toString());
				id = "Error";
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "UploadPicture() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "UploadPicture() received a " + response.getStatusLine().getStatusCode() + ", id: " + id);
		}

		return id;
	}

	private ExifInterface getFileExifInterface(String uri) {
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(getFilePath(uri));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return exif;
	}

	private String getFilePath(String strUri) {
		Uri uri = Uri.parse(strUri);
		ContentResolver cr = mContext.getContentResolver();
		String[] poj = { MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(uri, poj, null, null, null);
		String filePath = "";
		try {
			cursor.moveToFirst();
			filePath = cursor.getString(0);
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.close();
			}
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return filePath;
	}

	private String compressPNGThumbToJPG(String uri, Bitmap bit) {
		String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
		File folder = new File(tempFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String path = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + "newS" + ".jpg";
		File jpgFile = new File(path);
		if (!jpgFile.exists()) {
			try {
				jpgFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bit.compress(CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(jpgFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}

	private String compressThumbToJPG(String uri, ExifInterface exif) {
		Log.d(TAG, "compressThumbToJPG[uri:" + uri + ", orientation:" + exif.getAttributeInt("Orientation", 0) + "]");
		String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
		File folder = new File(tempFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		String path = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length()) + ".jpg";
		File jpgFile = new File(path);
		if (!jpgFile.exists()) {
			try {
				jpgFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(getFilePath(uri), options);
		if (options.outHeight > options.outWidth) {
			options.inSampleSize = options.outHeight / 400;
		} else {
			options.inSampleSize = options.outWidth / 400;
		}
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(getFilePath(uri), options);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(jpgFile);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			ExifInterface tempExif = new ExifInterface(path);
			int orientation = -1;
			if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
				orientation = ExifInterface.ORIENTATION_ROTATE_90;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_180) {
				orientation = ExifInterface.ORIENTATION_ROTATE_180;
			} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
				orientation = ExifInterface.ORIENTATION_ROTATE_270;
			}
			tempExif.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(orientation));
			tempExif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	/**
	 * get the compress size for image, if the result is null, it means no need
	 * to compress
	 * 
	 * @param width
	 * @param height
	 * @return int[]{width, height} . Null: don't need compress
	 */
	private int[] getCompressImageSize(int width, int height,PhotoInfo pi) {
		if (PrintHelper.products == null || PrintHelper.products.size() == 0) {
			return null;
		}

		int max = getMinImageSizeLongDim(pi);
		// get max size product
		if (max == 0){
			PrintProduct maxSizeProduct = PrintHelper.products.get(0);
			for (PrintProduct product : PrintHelper.products) {
				if (product.getWidth() * product.getHeight() > maxSizeProduct.getWidth() * maxSizeProduct.getHeight()) {
					maxSizeProduct = product;
				}
			}

			int DPI = 200;	
			int maxWidth = maxSizeProduct.getWidth() * DPI;
			int maxHeight = maxSizeProduct.getHeight() * DPI;
			max = maxWidth > maxHeight ? maxWidth : maxHeight;
		}	
		
		if (width > height) {
			if (height <= max) {
				return null;
			}

			return new int[] { (int) ((double) width * max / height), max };
		} else {
			if (width <= max) {
				return null;
			}

			return new int[] { max, (int) ((double) max * height / width) };
		}
	}

	// Alan Swire comment - As of August 17, 2012 this method is not currently
	// used
	// To do - checks for the HTTP result codes including 401
	public Drawable getProductPreview(String imageID, int maxWidth, int maxHeight) {
		Drawable myImage = null;
		if (maxWidth != 0 && maxHeight != 0)
			httpGet = new HttpGet(mStandardPrintsURL + imageID + "/preview?maxWidth=" + maxWidth + "&maxHeight=" + maxHeight);
		else {
			httpGet = new HttpGet(mImageEditingServiceURL + imageID + "/preview?maxWidth=200&maxHeight=200");
		}
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "image/jpeg");
		InputStream is = null;
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				myImage = Drawable.createFromStream(is, "Image");
				entity.consumeContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return myImage;
	}

	// Alan Swire comment - As of August 17, 2012 this method is not currently
	// used
	// To do - checks for the HTTP result codes including 401
	public Drawable getImagePreview(String imageID, int maxWidth, int maxHeight) {
		Drawable myImage = null;
		if (maxWidth != 0 && maxHeight != 0)
			httpGet = new HttpGet(mImageEditingServiceURL + imageID + "/preview?maxWidth=" + maxWidth + "&maxHeight=" + maxHeight);
		else {
			httpGet = new HttpGet(mImageEditingServiceURL + imageID + "/preview?maxWidth=200&maxHeight=200");
		}
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "image/jpeg");
		InputStream is = null;
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				myImage = Drawable.createFromStream(is, "Image");
				entity.consumeContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return myImage;
	}

	public String getPrintProducts(boolean isChangeCountryCode, String countryCode) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String currentCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
		String appName = mContext.getApplicationContext().getPackageName();
		if (currentCountryCode.equals("")) {
			Log.d(TAG, "currentCountryCode == null, getPrintProducts");
			currentCountryCode = Locale.getDefault().getCountry();
			// TODO: hard code here for DM
			if (appName.contains(MainMenu.DM_COMBINED_PACKAGE_NAME)) {
				currentCountryCode = "DE";
			}
		}
		InputStream is = null;
		try {
			String productTypes = "Print";
			if (appName.contains("wmc")) {
				productTypes = "Print,Quickbook,QuickbookPage";
			} else if (appName.contains(MainMenu.DM_COMBINED_PACKAGE_NAME)) {
				productTypes = "Print,Quickbook,QuickbookPage,DuplexMyGreeting,Greeting%20Cards,Collages";
			} else if (appName.contains("kodakprintmaker")) {
				productTypes = "print";
			} else if (appName.contains(MainMenu.KODAK_COMBINED_PACKAGE_NAME)) {
				productTypes = "Print,Quickbook,QuickbookPage,DuplexMyGreeting,Greeting%20Cards,Collages";
			}
			String mUrl = "";
			if(!AppContext.getApplication().isContinueShopping()){
				mUrl = mRetailerCatalogServiceURL + "/msrp/catalog3?productTypes=" + productTypes + "&languageCultureName=" + language
					+ "&countryCode=" + (isChangeCountryCode ? countryCode : currentCountryCode);
			} else {
				String retailerId = "";
				// When continue shopping, choose Pick up in Store
				if(PrintHelper.orderType == 1){
					retailerId = prefs.getString("selectedRetailerId", "");
					String storeId = prefs.getString("selectedStoreId", "");
					mUrl = mRetailerCatalogServiceURL + "/" + retailerId + "/catalog3?storeId=" + storeId + "&productTypes=" + productTypes + "&languageCultureName=" + language;
				}
				// When continue shopping, choose Home Delivery
				else if (PrintHelper.orderType == 2){
					retailerId = prefs.getString("retailerIdPayOnline", "");
					mUrl = mRetailerCatalogServiceURL + "/" +retailerId +  "/catalog2?productTypes=" + productTypes + "&languageCultureName=" + language;
				}
			}
			Log.e(TAG, "getPrintProducts Url: " + mUrl);
			httpGet = new HttpGet(mUrl);
			String token = getAuthorizationToken();
			httpGet.setHeader("Authorization", "Basic " + token);
			httpGet.setHeader("Accept", "application/json");
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
		String result = "";
		if (response == null) {
			result = "";
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print("getPrintProducts, result: " + result);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				return null;
			}

			try {
				JSONObject jObject = new JSONObject(result);
				JSONArray value2 = jObject.getJSONArray("Catalogs");
				JSONObject value3 = value2.getJSONObject(0);
				JSONArray entries = value3.getJSONArray("Entries");
				PrintHelper.products.clear();

				if (value3.has("CurrencySymbol")) {
					PrintHelper.currencySymbol = value3.getString("CurrencySymbol");
				}

				ArrayList<PrintProduct> photoBookProducts = new ArrayList<PrintProduct>();
				ArrayList<String> photoBookCartGroup = new ArrayList<String>();
				for (int i = 0; i < entries.length(); i++) {
					PrintProduct prod = new PrintProduct();
					JSONObject value4 = entries.getJSONObject(i);
					JSONObject obj;
					if (value4.has("ProductDescription")) {
						obj = value4.getJSONObject("ProductDescription");

						if (obj.has("Name"))
							prod.setName(obj.getString("Name"));

						if (obj.has("ShortName"))
							prod.setShortName(obj.getString("ShortName"));
						else if (obj.has("Name"))
							prod.setShortName(obj.getString("Name"));

						if (obj.has("Id"))
							prod.setId(obj.getString("Id"));

						if (obj.has("PageWidth"))
							prod.setWidth(obj.getInt("PageWidth"));

						if (obj.has("PageHeight"))
							prod.setHeight(obj.getInt("PageHeight"));

						if (obj.has("LgGlyphURL"))
							prod.setLgGlyphURL(obj.getString("LgGlyphURL"));

						if (obj.has("SmGlyphURL"))
							prod.setSmGlyphURL(obj.getString("SmGlyphURL"));

						if (obj.has("Type")) {
							prod.setType(obj.getString("Type"));
							if(obj.getString("Type").equalsIgnoreCase("QuickBookPage")){
								PrintHelper.AdditionalPageName = prod.getName();
							}
						}

						if (obj.has("Attributes")) {
							JSONArray attrArray = obj.getJSONArray("Attributes");
							for (int j = 0; j < attrArray.length(); j++) {
								JSONObject attrObj = attrArray.getJSONObject(j);
								if (attrObj.has("Name")) {
									String name = attrObj.getString("Name");
									if (name.equals("Marketing")) {
										if (attrObj.has("Value")) {
											String htmlMarketing = attrObj.getString("Value");
											prod.setHtmlMarketing(htmlMarketing);
										}
									} else if (name.equals("ShortMarketing")) {
										if (attrObj.has("Value")) {
											String htmlShortMarketing = attrObj.getString("Value");
											prod.setHtmlShortMarketing(htmlShortMarketing);
										}
									} else if (name.equals("QuantityIncrement")) {
										if (attrObj.has("Value")) {
											int htmlQuantityIncrement = attrObj.getInt("Value");
											prod.setQuantityIncrement(htmlQuantityIncrement);
										}
									} else if (name.equals("MinImageSizeLongDim")) {
										if (attrObj.has("Value")) {
											int MinImageSizeLongDim = attrObj.getInt("Value");
											prod.setMinImageSizeLongDim(MinImageSizeLongDim);
										}
									}
								}

							}
						}

					}

					if (value4.has("MaxUnitPrice")) {
						obj = value4.getJSONObject("MaxUnitPrice");
						prod.setMaxPrice(obj.getString("Price"));
						prod.setMaxPriceStr(obj.getString("PriceStr"));
					}
					if (value4.has("MinUnitPrice")) {
						obj = value4.getJSONObject("MinUnitPrice");
						prod.setMinPrice(obj.getString("Price"));
						prod.setMinPriceStr(obj.getString("PriceStr"));
					}

					photoBookProducts.add(prod);
					photoBookCartGroup.add(prod.getName());
					PrintHelper.cartChildren.add(new ArrayList<CartItem>());
					Log.e(TAG,
							"getPrintProducts ID: " + prod.getId() + " Type " + prod.getType() + " Name: " + prod.getName() + " ShortName: "
									+ prod.getShortName() + " Height: " + prod.getHeight() + " Width: " + prod.getWidth() + " MaxPrice: "
									+ prod.getMaxPrice() + " MinPrice: " + prod.getMinPrice() + " LGGlyphURL: " + prod.getLgGlyphURL());
				}
				ArrayList<PrintProduct> tempProducts = new ArrayList<PrintProduct>();
				ArrayList<String> tempCartGroup = new ArrayList<String>();
				boolean reOrder = true, orderPrint = true;
				while (reOrder) {
					if (orderPrint) {
						int index = -1;
						for (int i = 0; i < photoBookProducts.size(); i++) {
							if (photoBookProducts.get(i).getType().contains(PrintProduct.TYPE_PRINTS)) {
								index = i;
								break;
							}
						}
						if (index == -1) {
							orderPrint = false;
						} else {
							tempProducts.add(photoBookProducts.get(index));
							photoBookProducts.remove(index);
							tempCartGroup.add(photoBookCartGroup.get(index));
							photoBookCartGroup.remove(index);
						}
					} else {
						tempProducts.addAll(photoBookProducts);
						tempCartGroup.addAll(photoBookCartGroup);
						reOrder = false;
					}
				}

				for (int i = 0; i < tempProducts.size(); i++) {
					Log.w(TAG, "product type:" + tempProducts.get(i).getType() + ", name:" + tempProducts.get(i).getName() + ", cartGroup: "
							+ tempCartGroup.get(i));
				}

				PrintHelper.products.addAll(tempProducts);
				PrintHelper.cartGroups.addAll(tempCartGroup);
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "getPrintProducts() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else if (response.getStatusLine().getStatusCode() == 500 && !isChangeCountryCode) {
			Log.e(TAG, "getPrintProducts() received a 500");
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print("getPrintProducts, result: " + result);
				if (result.contains("INVALID_COUNTRY") && !isChangeCountryCode) {
					JSONObject jObject = new JSONObject(result);
					JSONArray value2 = jObject.getJSONArray("Countries");
					JSONObject value3 = value2.getJSONObject(0);
					String countrycode = value3.getString("CountryCode");
					Log.e(TAG, "INVALID_COUNTRY, use CountryCode: " + countrycode);
					if (!countrycode.equals(""))
						// getPrintProducts(true, countrycode);
						getPrintProducts(false, "");
				}
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				return null;
			}
		} else {
			Log.e(TAG, "getPrintProducts() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbCreatePhotoBook(Context con, String photobookTypeId) {
		String result = "", result1 = "";
		httpPost = new HttpPost(mPhotobookURL);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

		/*String postDataString = "{\"ProductType\":\""
				+ PreferenceManager.getDefaultSharedPreferences(mContext).getString(PrintHelper.selectedPhotoBookID, "") + "\"}";*/
		String postDataString = "{\"ProductType\":\"" + photobookTypeId + "\"}";

		Log.d(TAG, "CreatePhotoBook, post data: " + postDataString + " toke: " + token);
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("creatPhotoBook, status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			try {
				result = result1;
				JSONObject jObject = new JSONObject(result);
				JSONObject jObjectPhotoBook = jObject.getJSONObject("PhotoBook");
				JSONArray jObjectPages = jObjectPhotoBook.getJSONArray("Pages");
				Photobook photobook = new Photobook();
				photobook.minNumberOfImages = jObjectPhotoBook.getInt("MinNumberOfImages");
				photobook.maxNumberOfImages = jObjectPhotoBook.getInt("MaxNumberOfImages");
				photobook.isDuplex = jObjectPhotoBook.getBoolean("IsDuplex");
				photobook.idealNumberOfImagesPerBaseBook = jObjectPhotoBook.getInt("IdealNumberOfImagesPerBaseBook");
				photobook.maxNumberOfImagesPerBaseBook = jObjectPhotoBook.getInt("MaxNumberOfImagesPerBaseBook");
				photobook.numberOfPagesPerBaseBook = jObjectPhotoBook.getInt("NumberOfPagesPerBaseBook");
				photobook.id = jObjectPhotoBook.getString("Id");
				photobook.proDescId = jObjectPhotoBook.getString("ProductDescriptionId");
				photobook.canSetTitle = jObjectPhotoBook.getBoolean("CanSetTitle");
				photobook.canSetSubtitle = jObjectPhotoBook.getBoolean("CanSetSubtitle");
				photobook.canSetAuthor = jObjectPhotoBook.getBoolean("CanSetAuthor");

				for (int i = 0; i < jObjectPages.length(); i++) {
					JSONObject obj = jObjectPages.getJSONObject(i);
					if (obj.getString("PageType").equals("Title")){
						photobook.titlePageId = obj.getString("Id");
						photobook.width = (float) obj.getDouble("Width");
						photobook.height = (float) obj.getDouble("Height");
					}
				}
				AppContext.getApplication().setPhotobook(photobook);
				AppContext.getApplication().getPhotobooks().add(photobook);

			} catch (JSONException e) {
				Log.e(TAG, "Error parsing data " + e.toString());
				result = "";
			}
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "pbCreatePhotoBook() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "pbCreatePhotoBook() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}	
	
	public String buildPhotbookTask(){
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String url = mPhotobookURL + "/" + photobook.id + "/build";
		String imageIDs = getImagesJSONArray(photobook);
		String result = httpPostTask(url, imageIDs, "buildPhotbookTask");
		Log.i(TAG, "buildPhotbookTask result" + result);
		return result;
	}

	class ImageComparator implements Comparator<SelectedImage> {

		@Override
		public int compare(SelectedImage object1, SelectedImage object2) {

			long date1;
			long date2;
			try {
				date1 = object1.time;
				date2 = object2.time;
				long l = date1 - date2;
				if (l == 0)
					return 0;
				else if (l > 0)
					return 1;
				else
					return -1;
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}

		}
	}

	public String pbAddImageIDsToPhotoBook(Context con) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String mUrl = mPhotobookURL + "/" + photobook.id + "/photos";

		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

		JSONArray imageIDs = new JSONArray();
		try {
			ArrayList<SelectedImage> list = new ArrayList<SelectedImage>(photobook.imageEditParams.values());
			Collections.sort(list, new ImageComparator());
			for (SelectedImage mSelectedImage : list) {
				String id = mSelectedImage.sUploadImageID;
				if (id == null)
					continue;
				if (photobook.titleImageId.equals("")) {
					for (PhotoInfo photo : photobook.selectedImages) {
						String id1 = photobook.imageEditParams.get(photo).sUploadImageID;
						if (id1.equals(id)) {
							photobook.titleImageLocalUri = photo.getLocalUri();
							photobook.titleImagePath = photo.getPhotoPath();
							break;
						}
					}
					photobook.titleImageId = id;
				}
				if (id.equals(photobook.titleImageId))
					continue;
				JSONObject imageID = new JSONObject("{\"SourceImageId\":\"" + id + "\"}");
				imageIDs.put(imageID);
				Log.d(TAG, "mSelectedImage name: " + mSelectedImage.displayName + ", id: " + mSelectedImage.id + ", time: " + mSelectedImage.time);
			}
			/*
			 * for(String uri : PrintHelper.selectedImageUrls){ String id =
			 * PrintHelper.selectedImages.get(uri) == null ? null :
			 * PrintHelper.selectedImages.get(uri).sUploadImageID; if(id ==
			 * null) continue;
			 * if(prefs.getString(PrintHelper.sPhotoBookTitleImageID,
			 * "").equals("")){
			 * editor.putString(PrintHelper.sPhotoBookTitleImageID, id);
			 * editor.commit(); } JSONObject imageID = new
			 * JSONObject("{\"SourceImageId\":\"" + id + "\"}");
			 * imageIDs.put(imageID); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		String postDataString = imageIDs.toString();
		Log.e(TAG, "AddImageIDsToPhotobook UploadIDs: " + postDataString);
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("AddImageIDsToPhotobook(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "AddImageIDsToPhotobook() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "AddImageIDsToPhotobook() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}
	
	public boolean insertPageWithContent2Task(String bookId, List<String> contentIds){
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String previousPageId = "";
		if(photobook.photoBookPages != null){
			// get the page id of which you want to insert the page in front
			for(int i=photobook.photoBookPages.size()-1; i>=0; i--){
				try{
					Integer.parseInt(photobook.photoBookPages.get(i).sPhotoBookPageName);
					previousPageId = photobook.photoBookPages.get(i+1).sPhotoBookPageID;
					break;
				} catch (NumberFormatException nfe) {
					// need do nothing
				}
			}
		}
		String url = mPhotobookURL + "/" + bookId + "/insert-pages2?at=" + previousPageId;
		JSONArray jsArr1 = new JSONArray();
		JSONArray jsArr2 = new JSONArray();
		for(String contentId : contentIds){
			jsArr2.put(contentId);
		}
		jsArr1.put(jsArr2);
		boolean succeed = false;
		int count = 0;
		while(!succeed && count<connTryTimes){
			String result = httpPostTask(url, jsArr1.toString(), "insertPageWithContent2Task");
			parsePhotoBook(result);	
			succeed = parseSimpleError(result);
			count ++;
		}
		return succeed;
	}
	
	private String getImagesJSONArray(Photobook photobook){
		String imageIDs = "[";
		try {
			ArrayList<SelectedImage> list = new ArrayList<SelectedImage>(photobook.imageEditParams.values());
			Collections.sort(list, new ImageComparator());
			for (int i=0; i<list.size(); i++) {
				String id = list.get(i).sUploadImageID;
				if (id == null){
					continue;
				}
				if (photobook.titleImageId.equals("")) {
					for (PhotoInfo photo : photobook.selectedImages) {
						String id1 = photobook.imageEditParams.get(photo).sUploadImageID;
						if (id1.equals(id)) {
							photobook.titleImageLocalUri = photo.getLocalUri();
							photobook.titleImagePath = photo.getPhotoPath();
							break;
						}
					}
					photobook.titleImageId = id;
				}
				if (id.equals(photobook.titleImageId))
					continue;
				if(i != list.size()-1){
					imageIDs += "\"" + id + "\",";
				} else {
					imageIDs += "\"" + id + "\"";
				}
			}
			imageIDs += "]";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageIDs;
	}

	public String pbLayoutPhotoBook(Context con) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		//String mUrl = mPhotobookURL + "/" + prefs.getString(PrintHelper.sPhotoBookID, null) + "/layout";
		String mUrl = mPhotobookURL + "/" + photobook.id + "/layout";

		String result = "", result1 = "";
		HttpPut httpPut = new HttpPut(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
		try {
			response = mDefaultHttpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("LayoutPhotobook(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
			parsePhotoBook(result);
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "LayoutPhotobook() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "LayoutPhotobook() received a " + response.getStatusLine().getStatusCode());
		}

		return result;
	}
	
	public String layoutPageTask(String pageId) {
		String url = mPhotobookURL + "/pages/" + pageId + "/layout?sticky=true";
		Log.i(TAG, "layoutPageTask url: " + url);
		int count = 0;
		String result = "";
		while(result.equals("") && count<connTryTimes){
			result = httpPutTask(url, "", "layoutPageTask");
			count ++;
		}
		return result;
	}

	public ImageInfo parseImageInfo(String result) {
		ImageInfo imageInfo = null;
		try {
			JSONObject jsonObj = new JSONObject(result);
			if (jsonObj.has(ImageInfo.IMAGE_INFO)) {
				imageInfo = new ImageInfo();
				JSONObject jsonImgInfo = jsonObj.getJSONObject(ImageInfo.IMAGE_INFO);
				if (jsonImgInfo.has(ImageInfo.BASE_URI)) {
					imageInfo.baseURI = jsonImgInfo.getString(ImageInfo.BASE_URI);
				}
				if (jsonImgInfo.has(ImageInfo.ID)) {
					imageInfo.id = jsonImgInfo.getString(ImageInfo.ID);
				}
				if (jsonImgInfo.has(ImageInfo.WIDTH)) {
					imageInfo.width = jsonImgInfo.getInt(ImageInfo.WIDTH);
				}
				if (jsonImgInfo.has(ImageInfo.HEIGHT)) {
					imageInfo.height = jsonImgInfo.getInt(ImageInfo.HEIGHT);
				}
				if (jsonImgInfo.has(ImageInfo.ANGLE)) {
					imageInfo.angle = jsonImgInfo.getInt(ImageInfo.ANGLE);
				}
				if (jsonImgInfo.has(ImageInfo.CROP)) {
					ROI crop = new ROI();
					JSONObject jsonCrop = jsonImgInfo.getJSONObject(ImageInfo.CROP);
					if (jsonCrop.has(ImageInfo.CROP_X)) {
						crop.x = jsonCrop.getDouble(ImageInfo.CROP_X);
					}
					if (jsonCrop.has(ImageInfo.CROP_Y)) {
						crop.y = jsonCrop.getDouble(ImageInfo.CROP_Y);
					}
					if (jsonCrop.has(ImageInfo.CROP_W)) {
						crop.w = jsonCrop.getDouble(ImageInfo.CROP_W);
					}
					if (jsonCrop.has(ImageInfo.CROP_H)) {
						crop.h = jsonCrop.getDouble(ImageInfo.CROP_H);
					}
					if (jsonCrop.has(ImageInfo.CROP_CONTAINER_W)) {
						crop.ContainerW = jsonCrop.getDouble(ImageInfo.CROP_CONTAINER_W);
					}
					if (jsonCrop.has(ImageInfo.CROP_CONTAINER_H)) {
						crop.ContainerH = jsonCrop.getDouble(ImageInfo.CROP_CONTAINER_H);
					}
					imageInfo.crop = crop;
				}
				if (jsonImgInfo.has(ImageInfo.KPT_LEVEL)) {
					imageInfo.kptLevel = jsonImgInfo.getInt(ImageInfo.KPT_LEVEL);
				}
				if (jsonImgInfo.has(ImageInfo.COLOR_EFFECT)) {
					imageInfo.colorEffect = jsonImgInfo.getInt(ImageInfo.COLOR_EFFECT);
				}
				if (jsonImgInfo.has(ImageInfo.AUTO_RED_EYE)) {
					imageInfo.autoRedEye = jsonImgInfo.getBoolean(ImageInfo.AUTO_RED_EYE);
				}
				if (jsonImgInfo.has(ImageInfo.MANUAL_RED_EYE)) {
					imageInfo.manualRedEye = jsonImgInfo.getBoolean(ImageInfo.MANUAL_RED_EYE);
				}
				if (jsonImgInfo.has(ImageInfo.PET_EYE)) {
					imageInfo.petEye = jsonImgInfo.getBoolean(ImageInfo.PET_EYE);
				}
				if (jsonImgInfo.has(ImageInfo.CAPTION_LANGUAGE)) {
					imageInfo.captionLanguage = jsonImgInfo.getString(ImageInfo.CAPTION_LANGUAGE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			imageInfo = null;
		}
		return imageInfo;
	}

	public void parsePhotoBook(String result) {
		try {
			JSONObject jObject = new JSONObject(result);
			JSONObject jObjectPhotoBook = jObject.getJSONObject("PhotoBook");
			JSONArray jObjectPages = jObjectPhotoBook.getJSONArray("Pages");
			Photobook photobook = AppContext.getApplication().getPhotobook();
			// PrintHelper.photoBookPages.clear();
			ArrayList<PhotoBookPage> tempList = new ArrayList<PhotoBookPage>();
			int iPhotoPageNumber = 1;
			for (int i = 0; i < jObjectPages.length(); i++) {
				JSONObject obj = jObjectPages.getJSONObject(i);
				PhotoBookPage page = new PhotoBookPage();
				page.iMaxNumberOfImages = obj.getInt("MaxNumberOfImages");
				page.iSequenceNumber = obj.getInt("SequenceNumber");
				page.bPhotoBookPageEditable = obj.getString("PageType").equals("Standard");
				if (page.bPhotoBookPageEditable)
					page.sPhotoBookPageName = ++iPhotoPageNumber + "";
				else
					page.sPhotoBookPageName = obj.getString("PageType");
				page.sPhotoBookPageID = obj.getString("Id");
				String temp = obj.getString("BaseURI");
				int index = temp.indexOf("https://") + 8;
				page.sPhotoBookPageURL = "https://101KDKIOSPBAPP:101Kdk10SPBApp@" + temp.substring(index) + page.sPhotoBookPageID + "/preview";
				page.databaseID = page.iSequenceNumber + "";
				if (page.bPhotoBookPageEditable) {
					JSONArray jLayers = obj.getJSONArray("Layers");
					ArrayList<PhotoDefinition> mPhotoBookPageImages = new ArrayList<PhotoDefinition>();
					for (int j = 0; j < jLayers.length(); j++) {
						try {
							page.iNumOfInputImages = j + 1;
							PhotoDefinition mPhotoDefinition = new PhotoDefinition();
							JSONObject jLayer = jLayers.getJSONObject(j);
							JSONObject jLocation = jLayer.getJSONObject("Location");
							JSONArray jData = jLayer.getJSONArray("Data");
							JSONObject jCropRegion = null, jLoResWarning = null, jCaptionable = null;
							for (int k = 0; k < jData.length(); k++) {
								JSONObject jO = jData.getJSONObject(k);
								if (jO.getString("Name").equals("CropRegion")) {
									jCropRegion = jO;
								} else if (jO.getString("Name").equals("LoResWarning")) {
									jLoResWarning = jO;
								} else if (jO.getString("Name").equals("Captionable")) {
									jCaptionable = jO;
								}
							}
							JSONObject jROIVal = jCropRegion.getJSONObject("ROIVal");
							if (jLoResWarning == null) {
								mPhotoDefinition.isImageResWarning = false;
							} else {

								JSONObject jBoolVal = jLoResWarning.getJSONObject("BoolVal");
								mPhotoDefinition.isImageResWarning = jBoolVal.getBoolean("Value");
							}

							ROI mROI = new ROI();
							mPhotoDefinition.roi = mROI;
							mPhotoDefinition.roi.x = jLocation.getDouble("X");
							mPhotoDefinition.roi.y = jLocation.getDouble("Y");
							mPhotoDefinition.roi.w = jLocation.getDouble("W");
							mPhotoDefinition.roi.h = jLocation.getDouble("H");
							mPhotoDefinition.roi.ContainerW = jLocation.getDouble("ContainerW");
							mPhotoDefinition.roi.ContainerH = jLocation.getDouble("ContainerH");

							String hostName = jLayer.getString("ContentBaseURI")
									.substring(jLayer.getString("ContentBaseURI").indexOf("https://") + 8);
							mPhotoDefinition.photoURL = "https://101KDKIOSPBAPP:101Kdk10SPBApp@" + hostName + jLayer.getString("ContentId")
									+ "/preview?maxWidth=230&maxHeight=184";
							mPhotoDefinition.photoSuperHighResolutionImageURL = "https://101KDKIOSPBAPP:101Kdk10SPBApp@" + hostName
									+ jLayer.getString("ContentId") + "/preview?maxWidth=3000&maxHeight=3000";
							mPhotoDefinition.photoID = jLayer.getString("ContentId");
							for (PhotoInfo photo : photobook.selectedImages) {
								String id = photobook.imageEditParams.get(photo).sUploadImageID;
								if (id.equals(mPhotoDefinition.photoID)) {
									mPhotoDefinition.photoLocalURI = photo.getLocalUri();
									mPhotoDefinition.photoPath = photo.getPhotoPath();
									break;
								}
							}

							ROI mCropROI = new ROI();
							mPhotoDefinition.croproi = mCropROI;
							mPhotoDefinition.croproi.x = jROIVal.getDouble("X");
							mPhotoDefinition.croproi.y = jROIVal.getDouble("Y");
							mPhotoDefinition.croproi.w = jROIVal.getDouble("W");
							mPhotoDefinition.croproi.h = jROIVal.getDouble("H");
							mPhotoDefinition.croproi.ContainerW = jROIVal.getDouble("ContainerW");
							mPhotoDefinition.croproi.ContainerH = jROIVal.getDouble("ContainerH");

							mPhotoBookPageImages.add(mPhotoDefinition);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					page.PhotoBookPageImages = mPhotoBookPageImages;
				}
				if (photobook.photoBookPages != null) {
					for (PhotoBookPage tempPage : photobook.photoBookPages) {
						if (tempPage.sPhotoBookPageID.equals(page.sPhotoBookPageID)) {
							page = tempPage;
							break;
						}
					}
				}
				tempList.add(page);
			}
			if (photobook.photoBookPages != null) {
				photobook.photoBookPages.clear();
			} else {
				photobook.photoBookPages = new ArrayList<PhotoBookPage>();
			}
			photobook.photoBookPages.addAll(tempList);
			getImagesDegreeFromServer();
		} catch (Exception ex) {
			Log.e(TAG, "Error parsing data " + ex.toString());
			result = "";
		}
	}

	private void getImagesDegreeFromServer() {
		Log.d(TAG, "going to get images degree from server....");
		Photobook photobook = AppContext.getApplication().getPhotobook();
		for (PhotoBookPage page : photobook.photoBookPages) {
			boolean needCheck = PrintHelper.contentIdOfEditedImages != null && page.PhotoBookPageImages != null
					&& page.PhotoBookPageImages.size() != 0 && page.PhotoBookPageImages.get(0) != null;
			if (needCheck && PrintHelper.contentIdOfEditedImages.contains(page.PhotoBookPageImages.get(0).photoID)) {
				int count = 0;
				String result = "";
				while (count < 5 && result.equals("")) {
					result = getImageInfo(page.PhotoBookPageImages.get(0).photoID);
					count++;
				}
				if (!result.equals("")) {
					ImageInfo info = parseImageInfo(result);
					if (info != null) {
						page.PhotoBookPageImages.get(0).angle = info.angle;
					} else {
						Log.e(TAG, "ImageInfo is null!!!!!!!");
					}
				}
			}
		}
	}

	public String pbSetPhotoBookTitlePage(Context con) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String sCloneImageID2 = null;
		int iCount = 0;
		String sResult = "";
		while (iCount < 5 && sResult.equals("")) {
			//String imageId = prefs.getString(PrintHelper.sPhotoBookTitleImageID, "");
			String imageId = photobook.titleImageId;
			//sResult = pbInsertImageOnPhotoBookPage(con, prefs.getString(PrintHelper.sPhotoBookTitleID, ""), imageId);
			sResult = pbInsertImageOnPhotoBookPage(con, photobook.titlePageId, imageId);
			iCount++;
		}
		iCount = 0;
		sResult = "";
		while (iCount < 5 && sResult.equals("")) {
			//sResult = sCloneImageID2 = CloneImage(prefs.getString(PrintHelper.sPhotoBookTitleImageID, ""));
			sResult = sCloneImageID2 = CloneImage(photobook.titleImageId);
			iCount++;
		}
		iCount = 0;
		sResult = "";
		while (iCount < 5 && sResult.equals("")) {
			//sResult = pbSetBackgroundOnPhotoBookPage(con, prefs.getString(PrintHelper.sPhotoBookTitleID, ""), sCloneImageID2);
			sResult = pbSetBackgroundOnPhotoBookPage(con, photobook.titlePageId, sCloneImageID2);
			iCount++;
		}
		Log.e(TAG, "SetPhotobookTitlePage() Done.");

		return sResult;
	}

	public String pbSetTitle(Context con, String photobookId, String title, String author, String subtitle) {
		String result = "";
		try {
			String encodeTitle = URLEncoder.encode(title, "utf-8");
			String encodeAuthor = URLEncoder.encode(author, "utf-8");
			String encodeSubtitle = URLEncoder.encode(subtitle, "utf-8");
			String url = mPhotobookURL + "/" + photobookId + "/authortitlesubtitle?author=" + encodeAuthor + "&title=" + encodeTitle + "&subtitle="
					+ encodeSubtitle;
			result = httpPutTask(url, "", "pbSetTitle");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public String pbInsertImageOnPhotoBookPage(Context con, String photoBookPageID, String imageID) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		//String mUrl = mPhotobookURL + "/" + prefs.getString(PrintHelper.sPhotoBookID, null) + "/insert-photos-on-page?pageId=" + photoBookPageID;
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String mUrl = mPhotobookURL + "/" + photobook.id + "/insert-photos-on-page?pageId=" + photoBookPageID;

		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

		JSONArray imageIDs = new JSONArray();
		try {
			JSONObject jImageID = new JSONObject("{\"SourceImageId\":\"" + imageID + "\"}");
			imageIDs.put(jImageID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String postDataString = imageIDs.toString();
		Log.e(TAG, "InsertImageOnPhotoBookPage: " + mUrl + " " + postDataString);
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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("InsertImageOnPhotoBookPage(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "InsertImageOnPhotoBookPage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "InsertImageOnPhotoBookPage() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbSetBackgroundOnPhotoBookPage(Context con, String photoBookPageID, String imageID) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		//String mUrl = mPhotobookURL + "/" + prefs.getString(PrintHelper.sPhotoBookID, null) + "/set-page-background?pageId=" + photoBookPageID;
		String mUrl = mPhotobookURL + "/" + photobook.id + "/set-page-background?pageId=" + photoBookPageID;

		String result = "", result1 = "";
		HttpPut httpPut = new HttpPut(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");

		String postDataString = "{\"SourceImageId\":\"" + imageID + "\"}";
		Log.d(TAG, "SetBackgroundOnPhotoBookPage UploadIDs: " + postDataString);
		StringEntity requestEntity;
		requestEntity = null;
		try {
			requestEntity = new StringEntity(postDataString, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		requestEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPut.setEntity(requestEntity);

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

		try {
			response = mDefaultHttpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("SetBackgroundOnPhotoBookPage(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "SetBackgroundOnPhotoBookPage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "SetBackgroundOnPhotoBookPage() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbSetImageCrop(Context con, String ImageID, ROI newROI) {
		String mUrl = mImageEditingServiceURL + ImageID + "/crop";

		String result = "", result1 = "";
		HttpPut httpPut = new HttpPut(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");

		JSONObject obj = new JSONObject();
		try {
			obj.put("X", newROI.x);
			obj.put("Y", newROI.y);
			obj.put("W", newROI.w);
			obj.put("H", newROI.h);
			obj.put("ContainerW", newROI.ContainerW);
			obj.put("ContainerH", newROI.ContainerH);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "SetImageCrop post data: " + obj.toString());
		StringEntity requestEntity;
		requestEntity = null;
		try {
			requestEntity = new StringEntity(obj.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		requestEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		httpPut.setEntity(requestEntity);

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, connection_timeout);
		HttpConnectionParams.setSoTimeout(mHttpParams, sokect_timeout);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

		try {
			response = mDefaultHttpClient.execute(httpPut, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("SetImageCrop(), satus code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "SetImageCrop() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "SetImageCrop() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbRotateImageDegree(Context con, String ImageID, int iDegree) {
		String mUrl = mImageEditingServiceURL + ImageID + "/rotate?angle=" + iDegree;

		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("RotateImageDegree(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "RotateImageDegree() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "RotateImageDegree() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbRemovePageImage(Context con, String sImageID) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		//String mUrl = mPhotobookURL + "/" + prefs.getString(PrintHelper.sPhotoBookID, null) + "/remove-from-page?image=" + sImageID;
		String mUrl = mPhotobookURL + "/" + photobook.id + "/remove-from-page?image=" + sImageID;

		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		System.out.print("RemovePageImage(), ImageID: " + sImageID + ", url: " + mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("RemovePageImage(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "RemovePageImage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "RemovePageImage() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbDeletePhotoBookPage(Context con, String sPhotoBookPageID) {
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(con);
		Photobook photobook = AppContext.getApplication().getPhotobook();
		//String mUrl = mPhotobookURL + "/" + prefs.getString(PrintHelper.sPhotoBookID, null) + "/delete-page/?pageId=" + sPhotoBookPageID;
		String mUrl = mPhotobookURL + "/" + photobook.id + "/delete-page/?pageId=" + sPhotoBookPageID;

		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("DeletePhotobookPage(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "DeletePhotobookPage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "DeletePhotobookPage() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public String pbMovePhotoBookPage(Context con, int iFromPageIndex, int iToPageIndex) {
		Photobook photobook = AppContext.getApplication().getPhotobook();
		String mUrl = mPhotobookURL + "/" + photobook.id  + "/move-page?from=" + iFromPageIndex + "&to=" + iToPageIndex;
		
		String result = "", result1 = "";
		httpPost = new HttpPost(mUrl);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPost.setHeader("Authorization", "Basic " + token);
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("ContentType", "application/json");

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
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print("pbMovePhotobookPage(), status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
			} catch (Exception ex) {
				Log.e(TAG, "Error parsing data " + ex.toString());
				result = "";
			}
		}

		if (response == null) {
		} else if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
			result = result1;
		} else if (response.getStatusLine().getStatusCode() == 401) {
			Log.e(TAG, "pbMovePhotobookPage() received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, "pbMovePhotobookPage() received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}

	public boolean checkStores(String retailID, String storeID, String products) {
		long begin = 0, end = 0, diff = 0;
		begin = System.currentTimeMillis();

		String uri = mStoreLocatorServiceURL.substring(0, mStoreLocatorServiceURL.indexOf("store-locator?")) + "check-store?retailerid="
				+ mRetailerID + "&storeid=" + storeID + "&products=" + products;
		Log.d(TAG, "checkStores(), url: " + uri);
		httpGet = new HttpGet(uri);
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		InputStream is = null;
		try {
			response = httpClient.execute(httpGet, localContext);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		end = System.currentTimeMillis();
		diff = end - begin;
		Log.e(TAG, "Time for checkStores: " + (diff / 1000.00) + " seconds");
		String result = "";
		if (response == null) {
			return false;
		} else if (response.getStatusLine().getStatusCode() == 200) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print("checkStores() result: " + result);
				JSONObject jObject = new JSONObject(result);
				JSONObject value2 = jObject.getJSONObject("StoreAvailability");
				boolean isAvailable = value2.getBoolean("Available");
				return isAvailable;
			} catch (Exception e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
				return false;
			}
		} else if (response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 401) {
			Log.i(TAG, "Received a " + response.getStatusLine().getStatusCode() + " Invalidating authorization token");
			mAuthorizationToken = "";
		}
		return true;
	}

	public String FindStores(String zip, String latitude, String longitude, String products,double searchRadius ,boolean isSearch)
	{
		String ps = products; 
		List<Store> resultStores = null;
		long begin = 0, end = 0, diff = 0;
		begin = System.currentTimeMillis();
		String uri = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String currentCountryCode = prefs.getString(MainMenu.CurrentlyCountryCode, "");
		if(currentCountryCode.equals("")){
			Log.d(TAG, "currentCountryCode == null, FindStores");
			currentCountryCode = Locale.getDefault().getCountry();
		}
		if(latitude.equals("") || longitude.equals(""))
		{
			uri = mStoreLocatorServiceURL + "country=" + currentCountryCode + "&maximummiles=" + searchRadius + "&maximumstores=" + mMaximumStores + "&postalcode=" + (zip.replace(' ', '+')) + (ps.equals("") ? "" : "&products=" + ps);// + "&retailerid=" + getCurrentRetailerId();
			uri = uri + "&includeTestStores=" + (prefs.getBoolean(PrintHelper.INCLUDE_TEST_STORES, false)?"true":"false");
		}
		else
		{
			uri = mStoreLocatorServiceURL + "country=" + currentCountryCode + "&maximummiles=" + searchRadius + "&maximumstores=" + mMaximumStores + "&latitude=" + latitude + "&longitude=" + longitude + (ps.equals("") ? "" : "&products=" + ps);// + "&retailerid=" + getCurrentRetailerId();
			uri = uri + "&includeTestStores=" + (prefs.getBoolean(PrintHelper.INCLUDE_TEST_STORES, false)?"true":"false");
		}
		if(PrintHelper.wififlow){
			uri = mWifiLocatorServiceURL + "&maximummiles=" + searchRadius +  "&maximumstores=" + mMaximumStores + "&latitude=" + latitude + "&longitude=" + longitude + "&capabilities=connect_wifi";
		}
		Log.e(TAG, "FindStores(), url: " + uri);
		httpGet = new HttpGet(uri);
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		InputStream is = null;
		try
		{
			response = httpClient.execute(httpGet, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return e.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return e.toString();
		}
		end = System.currentTimeMillis();
		diff = end - begin;
		Log.e(TAG, "Time for FindStores: " + (diff / 1000.00) + " seconds");
		String result = "";
		if (response == null)
		{
			return "null";
		}
		else if (response.getStatusLine().getStatusCode() == 200)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF_8"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				System.out.print("FindStores() result: " + result);
			}
			catch (Exception ex)
			{
				Log.e(TAG, "Error parsing data " + ex.toString());
				return null;
			}
			try
			{
				JSONObject jObject = new JSONObject(result);
				JSONArray storeArray = jObject.getJSONArray("Stores");
//				if(PrintHelper.stores != null)
//					PrintHelper.stores.clear();
				
				if(storeArray!=null ){
					resultStores = new ArrayList<Store>() ;
					for (int i = 0; i < storeArray.length(); i++)
					{
						Store store = new Store();
						JSONObject value3 = storeArray.getJSONObject(i);
						// Create new Store object and assign it the following
						// values
						String phone = null;
						String baseURI = null;
						String miles = null;
						String id = null;
						String retailerBaseURI = null;
						String retailerID = null;
						String name = null;
						String email = null;
						String longitudeStr = null;
						String latitudeStr = null;
						String address1 = null;
						String city = null;
						String postalCode = null;
						String country = null;
						String stateProvince = null;
						boolean isATestStore = false;
						HashMap<Integer, String> hoursMap = new HashMap<Integer, String>();
						JSONObject address = null;
						if (value3.has("BaseURI"))
							baseURI = value3.getString("BaseURI");
						if (value3.has("Id"))
							id = value3.getString("Id");
						if (value3.has("RetailerBaseURI"))
							retailerBaseURI = value3.getString("RetailerBaseURI");
						if (value3.has("RetailerId"))
							retailerID = value3.getString("RetailerId");
						if (value3.has("Name"))
							name = value3.getString("Name");
						if (value3.has("Email"))
							email = value3.getString("Email");
						if (value3.has("Longitude"))
							longitudeStr = value3.getString("Longitude");
						if (value3.has("Latitude"))
							latitudeStr = value3.getString("Latitude");
						if (value3.has("Miles"))
							miles = value3.getString("Miles");
						if (value3.has("Phone"))
							phone = value3.getString("Phone");
						if(value3.has("IsATestStore"))
							isATestStore = value3.getBoolean("IsATestStore");
						if (value3.has("Address"))
						{
							address = value3.getJSONObject("Address");
							if (address.has("Address1"))
								address1 = address.getString("Address1");
							if (address.has("City"))
								city = address.getString("City");
							if (address.has("PostalCode"))
								postalCode = address.getString("PostalCode");
							if (address.has("Country"))
								country = address.getString("Country");
							if (address.has("StateProvince")){
								stateProvince = address.getString("StateProvince");
							}
						}
						if (value3.has("Hours")) {
							JSONArray hoursArray = value3.getJSONArray("Hours");
							for (int j = 0 ; j < hoursArray.length() ; j ++) {
								JSONObject hourObject = hoursArray.getJSONObject(j);
								StringBuffer hourBuffer = new StringBuffer();
								if (hourObject.has("Day")) {
									int day = hourObject.getInt("Day");
									switch (day) {
									case 6:
										hourBuffer.append(mContext.getString(R.string.sunday) + ": ");
										break;
									case 0:
										hourBuffer.append(mContext.getString(R.string.monday) + ": ");
										break;
									case 1:
										hourBuffer.append(mContext.getString(R.string.tuesday) + ": ");
										break;
									case 2:
										hourBuffer.append(mContext.getString(R.string.wednesday) + ": ");
										break;
									case 3:
										hourBuffer.append(mContext.getString(R.string.thursday) + ": ");
										break;
									case 4:
										hourBuffer.append(mContext.getString(R.string.friday) + ": ");
										break;
									case 5:
										hourBuffer.append(mContext.getString(R.string.saturday) + ": ");
										break;
									default:
										break;
									}
								}
								
								if (hourObject.has("Open")) {
									hourBuffer.append(hourObject.getString("Open"));
								}
								
								hourBuffer.append(" - ");
								
								if (hourObject.has("Close")) {
									hourBuffer.append(hourObject.getString("Close"));
								}
								
								hoursMap.put(j, hourBuffer.toString());
							}
						}
						store.phone = phone;
						store.baseURI = baseURI;
						store.miles = miles;
						store.id = id;
						store.retailerBaseURI = retailerBaseURI;
						store.retailerID = retailerID;
						store.name = name;
						store.email = email;
						store.longitude = longitudeStr;
						store.latitude = latitudeStr;
						store.address1 = address1;
						store.city = city;
						store.postalCode = postalCode;
						store.country = country;
						store.stateProvince = stateProvince;
						store.miles = miles;
						store.isATestStore = isATestStore;
						store.setHoursMap(hoursMap);
						resultStores.add(store);
					}
					
					PrintHelper.stores = resultStores ;
				}
			}
			catch (JSONException e)
			{
				Log.e("log_tag", "Error parsing data " + e.toString());
			}
		}
		else if (response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 401)
		{
			Log.i(TAG, "Received a " + response.getStatusLine().getStatusCode() + " Invalidating authorization token");
			mAuthorizationToken = "";
		}
		if (PrintHelper.stores == null || PrintHelper.stores.isEmpty())
			return "";
		else
			return result;
	}

	public String FindStores(String zip,double searchRadius)
	{
		return FindStores(zip, "", "", "",searchRadius , false);
	}

	public String getAuthorizationToken() {
		// Authorization Token never been set
		if (mAuthorizationToken.equals("")) {
			PrintHelper.appForbidden = false;
			PrintHelper.appInfoUrl = "";
			StringBuilder concatenatedAppIdPasswordSB = new StringBuilder();
			concatenatedAppIdPasswordSB.append(mWebServicesAppID);
			concatenatedAppIdPasswordSB.append(':');
			concatenatedAppIdPasswordSB.append(mWebServicesAppPassword);
			String concatenated = concatenatedAppIdPasswordSB.toString();
			String encodedUsernamePassword = Base64.encodeToString(concatenated.getBytes(), Base64.NO_WRAP);
			HttpPost httpPost = new HttpPost(mAuthorizationServiceURL);
			httpPost.setHeader("Authorization", "Basic " + encodedUsernamePassword);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("ContentType", "application/json");	
			//fixed for RSSMOBILEPDC-2061
			httpPost.addHeader("User-Agent",mWebServicesAppID+"/"+ versionName );
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
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (response == null) {
				Log.w(TAG, "response == null");
			} else {
				StatusLine sl = response.getStatusLine();
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
							try {
								JSONObject jObject = new JSONObject(sb.toString());
								JSONObject accessTokenResponse = (JSONObject) jObject.get("AccessTokenResponse");
								String accessToken = (String) accessTokenResponse.get("AccessToken");
								StringBuilder authorizationTokenStringBuilder = new StringBuilder();
								authorizationTokenStringBuilder.append(mWebServicesAppID);
								authorizationTokenStringBuilder.append(':');
								authorizationTokenStringBuilder.append(accessToken);
								mAuthorizationToken = Base64.encodeToString(authorizationTokenStringBuilder.toString().getBytes(), Base64.NO_WRAP);
							} catch (JSONException jsone) {
								jsone.printStackTrace();
							}
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
						PrintHelper.appForbidden = true;
						PrintHelper.appInfoUrl = redirectUrl;
						break;
					default:
						Log.e(TAG, "Unexpected response to Authorization Request: " + sl.getReasonPhrase());
						break;
					}
				}
			}
		}
		return mAuthorizationToken;
	}

	public String getCurrentRetailerId() {
		Log.d("getCurrentRetailerId", "getCurrentRetailerId 1");
		if (!mRetailerID.equals("")) {
			Log.e("getCurrentRetailerId", "getCurrentRetailerId , selected RetailerId: " + mRetailerID);
			return mRetailerID;
		} else {
			String result = "";
			String token = getAuthorizationToken();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			String selectedCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
			String url = mRetailerCatalogServiceURL + (selectedCountryCode.equals("") ? "/" : "?countryCode=" + selectedCountryCode);
			httpGet = new HttpGet(url);
			httpGet.setHeader("Authorization", "Basic " + token);
			httpGet.setHeader("Accept", "application/json");
			InputStream is = null;

			try {
				response = httpClient.execute(httpGet, localContext);
				if (response != null) {
					HttpEntity entity = response.getEntity();
					is = entity.getContent();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return e.toString();
			} catch (IOException e) {
				e.printStackTrace();
				return e.toString();
			}

			Log.e("getCurrentRetailerId", "done getCurrentRetailerId 1");
			if (response == null) {
				Log.e(TAG, "response == null");
			} else if (response.getStatusLine().getStatusCode() == 200) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"));
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					is.close();
					result = sb.toString();
				} catch (Exception ex) {
					Log.e(TAG, "Error parsing data " + ex.toString());
					return null;
				}

				try {
					JSONObject jObject = new JSONObject(result);
					JSONArray retailers = jObject.getJSONArray("Retailers");

					for (int i = 0; i < retailers.length(); i++) {
						JSONObject obj = retailers.getJSONObject(i);
						String id = obj.getString("Id");
						Log.i(TAG, "Retailer #" + i + " = " + id);
						if (i == 0) {
							mRetailerID = id;
						} else {
							Log.i(TAG, "Mulitple retailers returned, using first one. As ios did so.");
						}
					}
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing data " + e.toString());
				}
			} else if (response.getStatusLine().getStatusCode() == 401) {
				Log.e(TAG, "getRetailerId() received a 401");
				mAuthorizationToken = "";
				getAuthorizationToken();
			} else {
				Log.e(TAG, "getRetailerId() received a " + response.getStatusLine().getStatusCode());
			}
			return mRetailerID;
		}
	}

	/************* N2R Methods ********************/

	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";

		public String getMethod() {
			return METHOD_NAME;
		}

		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}

		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}

		public HttpDeleteWithBody() {
			super();
		}
	}	

	public int getConnTryTimes() {
		return connTryTimes;
	}

	public void setConnTryTimes(int connTryTimes) {
		this.connTryTimes = connTryTimes;
	}

	public String cloneImageTask(String contentId) {
		String url = mImageEditingServiceURL + contentId + "/clone";
		return httpPostTask(url, "", "cloneImageTask");

	}

	/*************
	 * Retailer ID Task add by song
	 * 
	 * @param
	 ********************/
	public String setRetailerId() {
		String payOnlineRetailerID = PreferenceManager.getDefaultSharedPreferences(mContext).getString("retailerIdPayOnline", "");
		String urlStr = mShoppingCartServiceURL + PrintHelper.cartID + "/retailer?id=" + payOnlineRetailerID;
		return httpPutTask(urlStr, "", "setRetailerId");
	}	
	
	public boolean parseSimpleError(String result){
		final String ERROR = "Error";
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has(ERROR)){
				String jsResult = jsObj.getString(ERROR);
				if(jsResult.equals("null")){
					return true;
				} else {
					return false;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * for RSSMOBILEPDC-1380
	 * get the order summary from server
	 * @param cartId
	 * @return Cart
	 */
	public Cart getCartTask(String cartId){
		Cart cart = null;
		String url = mShoppingCartServiceURL + cartId;
		int count = 0;
		while(cart==null && count<connTryTimes){
			count ++;
			String result = httpGetTask(url, "getCartTask");
			Parse mParse = new Parse();
			cart = mParse.parseCart(result);
		}
		return cart;
	}
	
	public List<Theme> getThemesTask(String proDesId){
		String url = mContentURL + "themes?productDescriptionId="+proDesId+"&language="+language;
		List<Theme> themes = null;
		int count = 0;
		Parse parse = new Parse();
		while(themes==null && count<connTryTimes){
			String result = httpGetTask(url, "getThemesTask");
			themes = parse.parseThemes(result);
			count ++;
		}
		return themes;
	}
	
	/**
	 * 
	 * @param url
	 * @param taskName
	 * @return
	 * 		if return is "" means the task failed
	 */
	protected String httpGetTask(String url, String taskName){
		url = url.replaceAll(" ", "%20");
		Log.e(TAG, taskName + " url: " + url);
		String result = "", result1="";
		httpGet = new HttpGet(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("ContentType", "application/json");

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
		HttpConnectionParams.setSoTimeout(mHttpParams, 30000);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
		try {
			response = mDefaultHttpClient.execute(httpGet, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print(taskName + ", status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
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
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		return "";
	}
	
	/**
	 * 
	 * @param url
	 * @param postDataString
	 * @param taskName
	 * @return
	 * 		if return is "" means the task failed
	 */
	protected String httpPostTask(String url, String postDataString, String taskName){
		url = url.replaceAll(" ", "%20");
		Log.e(TAG, taskName + " url: " + url + postDataString);
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
		HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
		HttpConnectionParams.setSoTimeout(mHttpParams, 30000);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);

		try {
			response = mDefaultHttpClient.execute(httpPost, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
				System.out.print(taskName + ", status code: " + response.getStatusLine().getStatusCode() + " result: " + result1);
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
			mAuthorizationToken = "";
			getAuthorizationToken();
		} else {
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		return "";
	}
	
	/**
	 * 
	 * @param url
	 * @param putDataString
	 * @param taskName
	 * @return
	 * 		if return is "" means the task failed
	 */
	protected String httpPutTask(String url, String putDataString, String taskName){
		url = url.replaceAll(" ", "%20");
		Log.e(TAG, taskName + " url: " + url + putDataString);
		String result = "", result1 = "";
		StringEntity URL = null;
		httpPut = new HttpPut(url);
		InputStream is = null;
		String token = getAuthorizationToken();
		httpPut.setHeader("Authorization", "Basic " + token);
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("ContentType", "application/json");
		
		try
		{
			URL = new StringEntity(putDataString, "UTF-8");
			URL.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		httpPut.setEntity(URL);
		try
		{
			HttpParams mHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
			HttpConnectionParams.setSoTimeout(mHttpParams, 30000);
			DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
			
			response = mDefaultHttpClient.execute(httpPut, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
				result1 = sb.toString();
				System.out.print(taskName + " Status code: " + response.getStatusLine().getStatusCode() + " result: " + result1+ putDataString + "\n");
			}
			catch (Exception ex)
			{
				Log.e(TAG, "Error parsing data " + ex.toString());
			}
		}
		
		if (response == null)
		{
		}
		else if (response.getStatusLine().getStatusCode() == 200)
		{
			result = result1;
		}
		else if (response.getStatusLine().getStatusCode() == 201)
		{
			result = result1;
		}
		else if (response.getStatusLine().getStatusCode() == 401)
		{
			Log.e(TAG, taskName + " received a 401");
			mAuthorizationToken = "";
			getAuthorizationToken();
		}
		else
		{
			Log.e(TAG, taskName + " received a " + response.getStatusLine().getStatusCode());
		}
		return result;
	}
	
	protected Bitmap httpGetDrawableTask(String url, String taskName){
		Log.e(TAG, taskName + " url: " + url);
		Bitmap bmp = null;
		httpGet = new HttpGet(PrintHelper.escapeURL(url));

		InputStream is = null;
		String token = getAuthorizationToken();
		httpGet.setHeader("Authorization", "Basic " + token);
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("ContentType", "application/json");

		HttpParams mHttpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(mHttpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(mHttpParams, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(mHttpParams, 10000);
		HttpConnectionParams.setSoTimeout(mHttpParams, 30000);
		DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient(mHttpParams);
		
		try
		{
			response = mDefaultHttpClient.execute(httpGet, localContext);
			if(response!=null){
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				
				int length = (int) entity.getContentLength();
				byte[] data = null;
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
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(data, 0, length, options);
				if (options.outHeight > options.outWidth) {
					options.inSampleSize = options.outHeight / 300;
				} else {
					options.inSampleSize = options.outWidth / 300;
				}
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Bitmap.Config.ALPHA_8;      
				options.inPurgeable = true;     
				options.inInputShareable = true;
				bmp = BitmapFactory.decodeByteArray(data, 0, length, options);
				entity.consumeContent();
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} finally {
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bmp;
	}
	
	private List<ColorEffect> parseColorEffects(String result){
		List<ColorEffect> colorEffects = null;
		try{
			JSONObject jsonObj = new JSONObject(result);
			JSONArray jsonEffects = jsonObj.getJSONArray(ColorEffect.FLAG_AVAILABLE_COLOR_EFFECTS);
			if(jsonEffects!=null){
				colorEffects = new ArrayList<ColorEffect>();
				for(int i=0; i<jsonEffects.length(); i++){
					JSONObject jsonEffect = jsonEffects.getJSONObject(i);
					ColorEffect colorEffect = new ColorEffect();
					if(jsonEffect.has(ColorEffect.FLAG_ID)){
						colorEffect.id = jsonEffect.getInt(ColorEffect.FLAG_ID);
					}
					if(jsonEffect.has(ColorEffect.FLAG_NAME)){
						colorEffect.name = jsonEffect.getString(ColorEffect.FLAG_NAME);
					}
					if(jsonEffect.has(ColorEffect.FLAG_GLYPH_PATH_URL)){
						colorEffect.glyphPathUrl = jsonEffect.getString(ColorEffect.FLAG_GLYPH_PATH_URL);
					}
					colorEffects.add(colorEffect);
				}
			}
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		return colorEffects;
	}
	
	public ROI parseROI(String result) {
		ROI roi = null;
		try {
			JSONObject jsObj = new JSONObject(result);
			if(jsObj.has("ROI")){
				JSONObject jsonRoi = jsObj.getJSONObject("ROI");
				roi = new ROI();
				if(jsonRoi.has(Layer.FLAG_LOCATION_X)){
					roi.x = jsonRoi.getDouble(Layer.FLAG_LOCATION_X);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_Y)){
					roi.y = jsonRoi.getDouble(Layer.FLAG_LOCATION_Y);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_W)){
					roi.w = jsonRoi.getDouble(Layer.FLAG_LOCATION_W);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_H)){
					roi.h = jsonRoi.getDouble(Layer.FLAG_LOCATION_H);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_W)){
					roi.ContainerW = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_W);
				}
				if(jsonRoi.has(Layer.FLAG_LOCATION_CONTAINER_H)){
					roi.ContainerH = jsonRoi.getDouble(Layer.FLAG_LOCATION_CONTAINER_H);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return roi;
	}
	
	private int getMinImageSizeLongDim(PhotoInfo photoInfo){
		int minImageSizeLongDim = 0;
		int lastPrintIndex = -1;
		if (photoInfo != null && photoInfo.getFlowType().isPrintWorkFlow()){
			for (int i = 0; i < PrintHelper.products.size(); i++) {
				if (PrintHelper.products.get(i) != null && PrintHelper.products.get(i).getType().equals(PrintProduct.TYPE_PRINTS)) {
					lastPrintIndex ++;
				}
			}
			if (lastPrintIndex>=0){
				minImageSizeLongDim = PrintHelper.products.get(lastPrintIndex).getMinImageSizeLongDim(); 
			}			
			if (minImageSizeLongDim !=0){
				return minImageSizeLongDim;
			}
		}else {
			for (PrintProduct pro : PrintHelper.products){
				if (pro.getId().equals(photoInfo.getDescIdByPro())){
					minImageSizeLongDim = pro.getMinImageSizeLongDim();
					if (minImageSizeLongDim !=0){
						return minImageSizeLongDim;
					}
				}
			}	
		}
		
		return minImageSizeLongDim;
	}

}
