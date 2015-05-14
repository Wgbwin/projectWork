package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSGetter {
	private Context con;  
    private double mLatitude = 0;  
    private double mLongitude = 0;  
    private LocationManager mLocationManager;  
  
    public GPSGetter(Context con) {  
        this.con = con;  
    }  
  
    public static GPSGetter mGps;  
  
    public static GPSGetter getGpsInstance(Context mCon) {  
        if (mGps == null) {  
            mGps = new GPSGetter(mCon);  
        }  
        return mGps;  
    }  
  
    /** Get the locationManager object and set GPS update listener. */  
    public void openGps() {  
        mLocationManager = (LocationManager) con  
                .getSystemService(Context.LOCATION_SERVICE);  
        /** 
         * Register the listener with the Location Manager to receive 
         * locationupdates 
         */  
        mLocationManager.requestLocationUpdates(  
                LocationManager.NETWORK_PROVIDER, 6000, 0, locationListener);  
    }  
  
    /** Close the gps service. */  
    public void closeGps() {  
        if (mLocationManager != null) {  
            mLocationManager.removeUpdates(locationListener);  
        }  
    }  
  
    /** Get the latitude location. */  
    public double getLatitude() {  
        return mLatitude;  
    }  
  
    /** Get the longitude location. */  
    public double getLongitude() {  
        return mLongitude;  
    }  
  
    private void updateWithNewLocation(Location location) {  
        if (location != null) {  
            mLatitude = location.getLatitude();  
            mLongitude = location.getLongitude();  
        }  
    }  
  
    private final LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location); 
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}  
       
    };  
}
