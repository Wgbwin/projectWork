package com.kodakalaris.kodakmomentslib.thread;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig.Property;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Catalog;
import com.kodakalaris.kodakmomentslib.exception.KMConfigMatchException;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.interfaces.IInitialTaskListener;
import com.kodakalaris.kodakmomentslib.manager.KMConfigImageDownloader;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.util.CumulusDataUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

/**
 * 
 * @author Kane
 *
 */
public class InitialDataTaskGroup extends AsyncTask<Void, Integer, Object> {
	private static final String TAG = InitialDataTaskGroup.class.getSimpleName();
	
	public static final int SUCCESS = 0;
	public static final int ERROR_INVALID_COUNTRY_CODE = 1;
	public static final int ERROR_EULA_OUTDATE = 2;
	
	private Context mContext;
	private IInitialTaskListener mListener;
	private boolean mIsRunning = false;
	
	public InitialDataTaskGroup(Context context, IInitialTaskListener listener){
		this.mContext = context;
		this.mListener = listener;
	}
	
	@Override
	protected void onPreExecute() {
		mIsRunning = true;
		// TODO show waiting dialog or other progress UI
	}

	@Override
	protected Object doInBackground(Void... params) {
		try {
			KM2Application app = KM2Application.getInstance();
			final GeneralAPI generalAPI = new GeneralAPI(mContext);
			// 1st step: get authorization token
			if(SharedPreferrenceUtil.authorizationToken(mContext).equals("")){
				generalAPI.getAuthorizationToken();
			}
			
			// 2st step: get supported countries
			if(app.getCountries() == null || app.getCountries().size() == 0){
				generalAPI.getCountriesTask();
			}
			// TODO set default country as US for now. Kane
			final String currentCountryCode = app.getCountryCodeUsed();
			if(!CumulusDataUtil.isCountryCodeValid(currentCountryCode)){
				return ERROR_INVALID_COUNTRY_CODE;
			}
			//TODO get countryInfo  Simon
			if (app.getCountryInfoList()==null||app.getCountryInfoList().size()==0) {
				generalAPI.getCountryInfoTask(currentCountryCode);
			}
			//3rd step: get carousel configurations
			getConfigsFromServer(generalAPI, Property.WELCOME_CAROUSEL);
			List<KMConfig> welcomeConfigs = KMConfigManager.getInstance().getConfigs(Property.WELCOME_CAROUSEL);
			if (welcomeConfigs != null && !welcomeConfigs.isEmpty()) {
				KMConfigImageDownloader.getInstance().download1stImageSync(welcomeConfigs.get(0), null);
			}
			publishProgress(PROGRESS_WELCOME_CONFIG);
			
			getConfigsFromServer(generalAPI, Property.HOME_CAROUSEL);
			List<KMConfig> homeConfigs = KMConfigManager.getInstance().getConfigs(Property.HOME_CAROUSEL);
			if (homeConfigs != null && !homeConfigs.isEmpty()) {
				KMConfigImageDownloader.getInstance().download1stImageSync(homeConfigs.get(0), null);
			}
			
			getConfigsFromServer(generalAPI, Property.HOME_RIBBON_CAROUSEL);
			List<KMConfig> homeRibbonConfigs = KMConfigManager.getInstance().getConfigs(Property.HOME_RIBBON_CAROUSEL);
			if (homeRibbonConfigs != null && !homeRibbonConfigs.isEmpty()) {
				KMConfigImageDownloader.getInstance().download1stImageSync(homeRibbonConfigs.get(0), null);
			}
			
			getConfigsFromServer(generalAPI, Property.PRINTS_WORKFLOW_CAROUSEL);
			
			new DownloadKMConfigsImagesTask().start();//new thread for download config image
			
			// 4th step: get catalogs
			if(app.getCatalogs() == null || app.getCatalogs().size() == 0){
				List<Catalog> catalogs = generalAPI.getMSRPCatalog3Task(mContext.getString(R.string.cumulus_support_products), "", "");
				// To initial the catalogs set in KM2Application
				app.setCatalogs(catalogs);
			}
			
			// 5th step: check EULA
			generalAPI.checkEulaTask();
			// TODO enable check later
			/*if(!SharedPreferrenceUtil.getBoolean(mContext, DataKey.EULA_ACCEPTED)){
				return ERROR_EULA_OUTDATE;
			}*/
			
			// 6th step: get color effects
			if(app.getColorEffects() == null || app.getColorEffects().size() == 0){
				generalAPI.getAvailableColorEffect2Task();
			}
			
		} catch (WebAPIException e){
			return e;
		}
		return SUCCESS;
	}
	
	private final int PROGRESS_WELCOME_CONFIG = 1;
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values[0] == PROGRESS_WELCOME_CONFIG) {
			mListener.onWelcomeConfigObtained();
		}
	}

	@Override
	protected void onPostExecute(Object result) {
		if(result instanceof WebAPIException){
			mListener.onInitialDataFailed((WebAPIException) result);
		} else {
			switch ((Integer) result) {
			case SUCCESS:
				mListener.onInitialDataSucceed();
				break;
			case ERROR_INVALID_COUNTRY_CODE:
				mListener.onCountryCodeInvalided();
				break;
			case ERROR_EULA_OUTDATE:
				mListener.onEulaOutdate();
				break;
			}
		}
		// TODO dismiss waiting dialog
		// TODO when error occurred, show proper message
		mIsRunning = false;
	}
	
	public boolean isTaskRunning(){
		return mIsRunning;
	}
	
	private void getConfigsFromServer(GeneralAPI api, Property property) throws WebAPIException{
		try {
			List<KMConfig> configs = api.getCarouselConfigTask(property);
			KMConfigManager.getInstance().setConfig(property, configs);
		} catch (KMConfigMatchException e) {
			Log.i("getConfigsFromServer", property.serverId + " matches, not need to update");
		} catch (WebAPIException e) {
			throw e;
		}
	} 

}
