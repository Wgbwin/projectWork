package com.kodakalaris.kodakmomentslib.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.location.Address;
import android.support.v4.app.FragmentActivity;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.findstore.BaseDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.widget.WaitingDialog;

public class DestinationSearchStore implements Runnable {
	private String TAG = "DestinationSearchStore";
	private GeneralAPI webService;
	private String zip = "";
	private String latitude = "", longtitude = "";
	private WaitingDialog waitingDialog;
	private Context mContext;

	public DestinationSearchStore(Context context) {
		this(context, "", "", "");
	}

	public DestinationSearchStore(Context context, String zip) {
		this(context, zip, "", "");
	}

	public DestinationSearchStore(Context context, String latitude, String longtitude) {
		this(context, "", latitude, longtitude);
	}

	private DestinationSearchStore(Context context, String zip, String latitude, String longtitude) {
		this.mContext = context;
		this.webService = new GeneralAPI(context);
		this.zip = zip;
		this.latitude = latitude;
		this.longtitude = longtitude;
	}

	@Override
	public void run() {
		List<Address> addresses = null;

		synchronized (((BaseDestinationStoreSelectionActivity) mContext).stores) {
			((BaseDestinationStoreSelectionActivity) mContext).stores.clear();
			((BaseDestinationStoreSelectionActivity) mContext).mHandler.obtainMessage(BaseDestinationStoreSelectionActivity.SEARCH_STORE_START).sendToTarget();
			boolean isGeocoderAvailable = false;
			waitingDialog = new WaitingDialog(mContext, false);
			waitingDialog.initDialog(R.string.Common_please_wait);
			waitingDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), "");
			// This section is checking whether the Geocoder is available
			final String defaultCity = "Rochester";
			try {
				addresses = ((BaseDestinationStoreSelectionActivity) mContext).mGeocoder.getFromLocationName(defaultCity, 5);

				if (addresses != null && !addresses.isEmpty()) {
					isGeocoderAvailable = true;
				}
			} catch (IOException e) {
				isGeocoderAvailable = false;

			}

			if (!"".equals(zip)) {
				addresses = null;
				// if Geocoder is available, but the zip is invalid then do not get stores, just show error message
				if (isGeocoderAvailable) {
					try {
						addresses = ((BaseDestinationStoreSelectionActivity) mContext).mGeocoder.getFromLocationName(zip.trim(), 5);
					} catch (IOException e) {
						// do nothing
					}
					if (addresses == null || addresses.isEmpty()) {
						((BaseDestinationStoreSelectionActivity) mContext).mHandler.obtainMessage(BaseDestinationStoreSelectionActivity.SEARCH_STORE_ERROR).sendToTarget();
						return;
					}
				}
			}
			try {
				((BaseDestinationStoreSelectionActivity) mContext).stores = (ArrayList<StoreInfo>) webService.getStoresTask(zip, latitude, longtitude, ((BaseDestinationStoreSelectionActivity) mContext).productDescriptionId, true, ((BaseDestinationStoreSelectionActivity) mContext).isWifiLocator);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (((BaseDestinationStoreSelectionActivity) mContext).stores == null) {
				((BaseDestinationStoreSelectionActivity) mContext).mHandler.obtainMessage(BaseDestinationStoreSelectionActivity.SEARCH_STORE_ERROR).sendToTarget();
				waitingDialog.dismiss();
			} else if (((BaseDestinationStoreSelectionActivity) mContext).stores.size() == 0) {
				((BaseDestinationStoreSelectionActivity) mContext).mHandler.obtainMessage(BaseDestinationStoreSelectionActivity.SEARCH_STORE_FAILED).sendToTarget();
				waitingDialog.dismiss();
			} else {
				((BaseDestinationStoreSelectionActivity) mContext).mHandler.obtainMessage(BaseDestinationStoreSelectionActivity.SEARCH_STORE_SUCCESS).sendToTarget();
				waitingDialog.dismiss();
			}
		}
	}

}
