package com.kodak.rss.core.n2r.webservice;

import android.app.Activity;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.kodak.rss.core.util.Log;
/**
 * 
 * @author bigtotoro
 *
 */
public class LocationService {
	String TAG = "LocationService";
	private LocationManager locationManager;
	private Location currentLocation;
	private OnLocationChangedListener locationChangedListener;
	private boolean isRegisterdListner = false;
	public LocationService(Activity context){
		locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);;
	}

	/**
	 * net and gps location. 
	 * note: when gps locate successful, LocationListener's onLocationChanged method will be invoked.
	 * @param locationChangedListener
	 */
	public void registerLocationProvider(OnLocationChangedListener locationChangedListener) {
		this.locationChangedListener = locationChangedListener;
		// new a Criteria object
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		criteria.setSpeedRequired(false);

		//get the best provider by the criteria
		String currentProvider = locationManager.getBestProvider(criteria, true);
		Log.d("Location", "currentProvider: " + currentProvider);
		if (currentProvider != null) {
			/*currentLocation = locationManager.getLastKnownLocation(currentProvider);
			if(currentLocation!=null){
				locationChangedListener.onLocationChanged(currentLocation);
			}*/
			isRegisterdListner = true;
			locationManager.requestLocationUpdates(currentProvider, 0, 0, locationListener);
			locationManager.addGpsStatusListener(gpsListener);
		}else{
			Log.e(TAG, "LOCATION PROVIDER NULL ...");
		}
	}
	public void unRegisterLocationProvider(){
		if(locationManager!=null && isRegisterdListner){
			locationManager.removeUpdates(locationListener);
			locationManager.removeGpsStatusListener(gpsListener);
			isRegisterdListner = false;
		}
	}
	
	/**
	 * For the reason that people usually do the work when is on location changed, so it's simple and easy define an interface with one function
	 * @author bigtotoro
	 */
	public interface OnLocationChangedListener{
		void onLocationChanged(Location location);
	}
	
	/**
	 * define a default locationListener. it belong to net location
	 */
	private LocationListener locationListener = new LocationListener() {
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		@Override
		public void onProviderEnabled(String provider) {
		}
		
		@Override
		public void onProviderDisabled(String provider) {
		}
		
		@Override
		public void onLocationChanged(Location location) {
			if(locationChangedListener!=null){
				if(location!=null&&!equalLocation(currentLocation, location)){
					currentLocation = location;
					locationChangedListener.onLocationChanged(location);
				}
			}
		}
		/* Determine whether two locations are equal. */
		private boolean equalLocation(Location location1, Location location2){
			if(location1==null){
				return false;
			}
			Double.compare(location1.getLongitude(), location2.getLongitude());
			if(location1!=null&&location2!=null){
				if(Double.compare(location1.getLatitude(), location2.getLatitude())==0&&Double.compare(location1.getLongitude(), location2.getLongitude())==0){
					return true;
				}
			}
			return false;
		}
	};
	
	/**
	 * gps listener , i
	 */
	private GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
        }
	};
}