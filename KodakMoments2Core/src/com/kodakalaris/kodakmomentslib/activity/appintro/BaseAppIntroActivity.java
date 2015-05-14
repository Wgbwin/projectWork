package com.kodakalaris.kodakmomentslib.activity.appintro;

import java.io.IOException;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.interfaces.IInitialTaskListener;
import com.kodakalaris.kodakmomentslib.service.LocationService;
import com.kodakalaris.kodakmomentslib.service.LocationService.OnLocationChangedListener;
import com.kodakalaris.kodakmomentslib.thread.InitialDataTaskGroup;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.CumulusDataUtil;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;

public abstract class BaseAppIntroActivity extends BaseNetActivity implements IInitialTaskListener{
	protected int[] mDefaultImgs = { R.drawable.image_welcome_1, R.drawable.image_welcome_2, R.drawable.image_welcome_3 };
	
	protected InitialDataTaskGroup mInitialDataTaskGroup;
	private boolean mLocated = false;
	private LocationService mLocationService;
	protected boolean mEulaAccepted = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEulaAccepted = SharedPreferrenceUtil.getBoolean(this, DataKey.EULA_ACCEPTED);
		if (!mEulaAccepted && !ConnectionUtil.isConnected(this)) {
			showNoNetworkDialog();
		} else if(mInitialDataTaskGroup == null){
			mInitialDataTaskGroup = new InitialDataTaskGroup(this, this);
			mInitialDataTaskGroup.execute();
		}
		
		if (mLocationService == null) {
			mLocationService = new LocationService(this);
		}
		
		if (!mLocated) {
			mLocationService.registerLocationProvider(mLocationChangedListener);
		}
	}
	
	protected boolean isInternetConnected(){
		return ConnectionUtil.is3gConnected(this) || ConnectionUtil.isConnectedCellular(this) || ConnectionUtil.isConnectedAnyWifi(this);
	}
	
	protected abstract void showCountrySelectionDialog();
	protected abstract void showEulaDialog();
	protected abstract void showErrorDialog(WebAPIException e);
	protected abstract void getDataCompleted();
	protected abstract void dismissSplashView();

	@Override
	protected void onPause() {
		super.onPause();
		if (mLocationService != null) {
			mLocationService.unRegisterLocationProvider();
		}
	}

	@Override
	public void onCountryCodeInvalided() {
		showCountrySelectionDialog();
	}

	@Override
	public void onEulaOutdate() {
		showEulaDialog();
	}

	@Override
	public void onInitialDataSucceed() {
		getDataCompleted();
	}

	@Override
	public void onInitialDataFailed(WebAPIException e) {
//		dismissSplashView();
		showErrorDialog(e);
//		if (!e.isAppObsolete()) {
//			getDataCompleted();
//		}
	}
	
	@Override
	public void onWelcomeConfigObtained() {
		dismissSplashView();
	}
	
	private OnLocationChangedListener mLocationChangedListener = new OnLocationChangedListener() {
		final String TAG = "LocationListener";
		
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG,"onLocationChanged Latitude" + location.getLatitude()+" Longitude "+location.getLongitude());
			mLocated = true;
			final int latitude = (int) location.getLatitude();
			final int longitude = (int) location.getLongitude();
			if (mLocationService != null) {
				mLocationService.unRegisterLocationProvider();
			}
			new Thread(new Runnable() {
				public void run() {
					Geocoder geoCoder = new Geocoder(BaseAppIntroActivity.this);
					List<Address> list;
					try {
						list = geoCoder.getFromLocation(latitude, longitude, 2);
						for (int i = 0; i < list.size(); i++) {
							Address address = list.get(i);
							String countryCode = address.getCountryCode();
							Log.d(TAG, "onLocationChanged countryCode: " + countryCode);
							if(countryCode != null && !countryCode.equals("")){
								SharedPreferrenceUtil.saveCurrentCountryCode(BaseAppIntroActivity.this, countryCode);
								if (CumulusDataUtil.isCountryCodeValid(countryCode) && KM2Application.getInstance().getCountryCodeUsed() == null) {
									KM2Application.getInstance().setCountryCodeUsed(countryCode);
								}
							}
						}
					} catch (IOException e) {
						Log.e(TAG, e);
					}
				}
			}).start();
		}
	};
	
	
}
