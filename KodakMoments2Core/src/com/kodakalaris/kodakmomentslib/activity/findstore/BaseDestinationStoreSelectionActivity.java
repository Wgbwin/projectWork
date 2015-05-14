package com.kodakalaris.kodakmomentslib.activity.findstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;
import com.kodakalaris.kodakmomentslib.activity.printsreview.MPrintsReviewActivity;
import com.kodakalaris.kodakmomentslib.activity.shoppingcart.MShoppingCartActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.StateListAdapter;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.thread.DestinationMethodTask;
import com.kodakalaris.kodakmomentslib.thread.DestinationSearchStore;
import com.kodakalaris.kodakmomentslib.thread.SingleThreadPool;
import com.kodakalaris.kodakmomentslib.util.Log;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.KMMapFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.MEditText;
import com.kodakalaris.kodakmomentslib.widget.mobile.MStoreInfoTray;

public class BaseDestinationStoreSelectionActivity extends BaseNetActivity implements OnClickListener, LocationListener, OnMarkerClickListener, OnMyLocationButtonClickListener, ConnectionCallbacks, OnConnectionFailedListener {
	public Button btnSelectMethod;
	public LinearLayout lineLyAddress, lineLyOr, lineLySearch, lineErrorFirstName, lineErrorLastName, lineErrorAddress1, lineErrorAddress2, lineErrorCity, lineErrorPostalCode, lineErrorState;
	public TextView txtShipHome, txtPickStore;
	public static TextView txtState;
	public ArrayList<StoreInfo> stores = new ArrayList<StoreInfo>();
	public Handler mHandler;
	public static final int SEARCH_STORE_SUCCESS = 0x000001; // the returned store list size is large than 0
	public static final int SEARCH_STORE_FAILED = 0x000002; // the returned store list size is 0
	public static final int SEARCH_STORE_ERROR = 0x000003; // the response is not correct, maybe internal error or others
	public static final int SEARCH_STORE_START = 0x000004;
	public Geocoder mGeocoder = null;
	public String productDescriptionId = "";
	public boolean isWifiLocator = false;
	protected ImageView imagBack, imgSearch;
	protected Button btnSave;
	protected EditText etxtFirstName, etxtLastName, etxtAddress1, etxtAddress2, etxtTownOrCity, etxtPostalCode;
	protected MEditText etxtSearch;
	protected String strFirstName, strLastName, strAddress1, strAddress2, strTownOrCity, strPostalCode;
	protected final String SAVE_METHOD_STATUS = "find_store_status";
	protected final String defValue = "";
	protected String TAG = getClass().getSimpleName();
	protected LocationManager locMan;
	protected Location location;
	protected LocalCustomerInfo mLocalCustomerInfo;
	private Intent intent;
	protected ShoppingCartManager mShoppingCartManager;
	protected PopupWindow mPopupWindow;
	private String home = "";
	private String store = "";
	private String state = "";
	private KMMapFragment mapFragment;
	public GoogleMap googleMap;
	private SingleThreadPool pool;
	private double Latitude, Longitude;
	private List<Marker> markers;
	private LocationClient mLocationClient;
	private final int defaultZoom = 15;
	private MStoreInfoTray vStoreTray;
	private StateListAdapter mStateListAdapter = null;
	private View mUnderState;
	private ScrollView vSvScrollPart;
	private List<CountryInfo> mCountryInfos;
	private ArrayList<String> stateKey = new ArrayList<String>();
	private ArrayList<String> stateValue = new ArrayList<String>();
	private final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
private boolean isDeliverMethodChange=false;
	protected void initBaseDSSData() {
		getView();
		initData();
		setEvents();
	};

	private void getView() {
		btnSelectMethod = (Button) findViewById(R.id.btn_findStore_methodSelection);
		imagBack = (ImageView) findViewById(R.id.img_findStore_back);
		imgSearch = (ImageView) findViewById(R.id.iv_findStore_search);
		btnSave = (Button) findViewById(R.id.btn_findStore_save);
		lineLyAddress = (LinearLayout) findViewById(R.id.findStore_addressPart);
		lineLySearch = (LinearLayout) findViewById(R.id.findStore_searchPart);
		lineLyOr = (LinearLayout) findViewById(R.id.lineLy_findStore_or);
		etxtFirstName = (EditText) findViewById(R.id.etxt_findStore_firstName);
		etxtLastName = (EditText) findViewById(R.id.etxt_findStore_lastName);
		etxtAddress1 = (EditText) findViewById(R.id.etxt_findStore_address1);
		etxtAddress2 = (EditText) findViewById(R.id.etxt_findStore_address2);
		etxtTownOrCity = (EditText) findViewById(R.id.etxt_findStore_townOrCity);
		etxtPostalCode = (EditText) findViewById(R.id.etxt_findStore_postalCode);
		etxtSearch = (MEditText) findViewById(R.id.etxt_findStore_search);
		txtState = (TextView) findViewById(R.id.txt_findStore_state);
		mUnderState = findViewById(R.id.view_underState);
		vStoreTray = (MStoreInfoTray) findViewById(R.id.storeList_tray);
		mapFragment = (KMMapFragment) getFragmentManager().findFragmentById(R.id.fragment_findStore_map);
		vSvScrollPart = (ScrollView) findViewById(R.id.findstore_sv);
		lineErrorFirstName = (LinearLayout) findViewById(R.id.error_firstName);
		lineErrorLastName = (LinearLayout) findViewById(R.id.error_lastName);
		lineErrorAddress1 = (LinearLayout) findViewById(R.id.error_address1);
		lineErrorAddress2 = (LinearLayout) findViewById(R.id.error_address2);
		lineErrorCity = (LinearLayout) findViewById(R.id.error_townOrCity);
		lineErrorPostalCode = (LinearLayout) findViewById(R.id.error_postalCode);
		lineErrorState = (LinearLayout) findViewById(R.id.error_state);
	};

	private void initData() {
		pool = new SingleThreadPool();
		home = getResources().getString(R.string.FindStore_ShipToHome);
		store = getResources().getString(R.string.FindStore_pickUpInStore);
		// TODO get the true state and set them in state picker ,need to change the StateListAdapter
		mStateListAdapter = new StateListAdapter(BaseDestinationStoreSelectionActivity.this);
		mLocalCustomerInfo = new LocalCustomerInfo(BaseDestinationStoreSelectionActivity.this);
		getRetailerType();
		// TODO show state
		getStateList();
		// TODO show saved ship info
		if (ShoppingCartManager.getInstance().isShipToHome) {
			setSaveAddressValue();
		}
		// TODO select deliver method popup window
		showDeliverMethodType();
		// TODO show store location
		setupMapAndClient();
		initLocationService();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (mLocationClient != null && mLocationClient.isConnected()) {
					Location location = mLocationClient.getLastLocation();
					if (googleMap != null && location != null) {
						googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), defaultZoom));
						vStoreTray.refresh(stores);
					}
				}
			}
		}, 200);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (stores != null) {
					vStoreTray.initialize(stores);
				}
				switch (msg.what) {
				case SEARCH_STORE_SUCCESS:
					addMarkersToMap(stores);
					MStoreInfoTray.mAdapter.updateStores(stores);
					break;
				case SEARCH_STORE_START:
					clearMarkersFromMap();
					break;
				case SEARCH_STORE_FAILED:
					vStoreTray.refresh(stores);
					// TODO Temp tip
					Toast.makeText(BaseDestinationStoreSelectionActivity.this, "Could not find any store", Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
				}
			}
		};

	};

	private void setEvents() {
		btnSave.setOnClickListener(this);
		imagBack.setOnClickListener(this);
		btnSelectMethod.setOnClickListener(this);
		txtPickStore.setOnClickListener(this);
		txtShipHome.setOnClickListener(this);
		imgSearch.setOnClickListener(this);
		txtState.setOnClickListener(this);

		etxtSearch.setOnEditorActionListener(editorActionListener);
		etxtSearch.setHideListener(new MEditText.HideKeyboard() {
			@Override
			public void hideKeyboard() {
				vStoreTray.refresh(stores);
			}
		});
		mapFragment.setListener(new KMMapFragment.OnTouchListener() {
			@Override
			public void onTouch() {
				vSvScrollPart.requestDisallowInterceptTouchEvent(true);
			}
		});
		etxtSearch.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				vStoreTray.setVisibility(View.INVISIBLE);
				return false;
			}
		});

	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_findStore_save) {
			HideInputKeyboard();
			if (ShoppingCartManager.getInstance().isShipToHome) {
				if (!saveAddressValue()) {
					return;
				}
			} else {
				int position = 0;
				position = ShoppingCartManager.getInstance().getmSelectedStorePosition();
				if (stores != null && stores.size() > 0) {
					stores.get(position).saveAsSelectedAtLocal(BaseDestinationStoreSelectionActivity.this);
				}
			}

			intent = new Intent(BaseDestinationStoreSelectionActivity.this, MShoppingCartActivity.class);
			startActivity(intent);
			finish();

		} else if (v.getId() == R.id.img_findStore_back) {
			intent = new Intent(BaseDestinationStoreSelectionActivity.this, MPrintsReviewActivity.class);
			startActivity(intent);
			finish();
		} else if (v.getId() == R.id.btn_findStore_methodSelection) {
			mPopupWindow.showAsDropDown(v);
		} else if (v.getId() == R.id.tv_popup_deliverMethod_home) {
			if (!isDeliverMethodChange) {
				isDeliverMethodChange=true;
			}
			ShoppingCartManager.getInstance().isShipToHome = true;
			vStoreTray.setVisibility(View.GONE);
			btnSelectMethod.setText(home);
			setSaveAddressValue();
			lineLyAddress.setVisibility(View.VISIBLE);
			lineLySearch.setVisibility(View.GONE);
			mPopupWindow.dismiss();
		} else if (v.getId() == R.id.tv_popup_deliverMethod_store) {
			ShoppingCartManager.getInstance().isShipToHome = false;
			vStoreTray.refresh(stores);
			btnSelectMethod.setText(store);
			lineLyAddress.setVisibility(View.GONE);
			lineLySearch.setVisibility(View.VISIBLE);
			mPopupWindow.dismiss();
			if (isDeliverMethodChange && googleMap != null) {
					if (!TestStoreBackDoor()) {
						String searchKey = etxtSearch.getText().toString().trim();
						if (!"".equals(searchKey)) {
							pool.addHighPriorityTask(new DestinationSearchStore(BaseDestinationStoreSelectionActivity.this, searchKey));
						} else {
							LatLng latLng = googleMap.getCameraPosition().target;
							pool.addHighPriorityTask(new DestinationSearchStore(BaseDestinationStoreSelectionActivity.this, latLng.latitude + "", latLng.longitude + ""));
						}
					}
					
				isDeliverMethodChange=false;
			}
		} else if (v.getId() == R.id.iv_findStore_search) {
			HideInputKeyboard();
			if (!TestStoreBackDoor()) {
				String searchKey = etxtSearch.getText().toString().trim();
				if (!"".equals(searchKey)) {
					pool.addHighPriorityTask(new DestinationSearchStore(BaseDestinationStoreSelectionActivity.this, searchKey));
				} else {
					showInputKeyboard();
				}
			}
		} else if (v.getId() == R.id.txt_findStore_state) {
			mStateListAdapter.showPopWindow(mUnderState);
		}
	}

	OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				HideInputKeyboard();
				if (!TestStoreBackDoor()) {
					String searchKey = etxtSearch.getText().toString().trim();
					if (!"".equals(searchKey)) {
						pool.addHighPriorityTask(new DestinationSearchStore(BaseDestinationStoreSelectionActivity.this, searchKey));
					} else {
						if (googleMap != null) {
							LatLng latLng = googleMap.getCameraPosition().target;
							pool.addHighPriorityTask(new DestinationSearchStore(BaseDestinationStoreSelectionActivity.this, latLng.latitude + "", latLng.longitude + ""));
						}
					}
				}
			}
			return false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			intent = new Intent(BaseDestinationStoreSelectionActivity.this, MPrintsReviewActivity.class);
			startActivity(intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initLocationService() {
		String language = Locale.getDefault().getLanguage();
		String country = KM2Application.getInstance().getCountryCodeUsed();
		if (country == null) {
			country = "";
		}

		Locale locale = new Locale(language, country.equals("") ? Locale.getDefault().getCountry() : country);
		mGeocoder = new Geocoder(this, locale);
	}

	// Add markers to GoogleMap
	private void addMarkersToMap(List<StoreInfo> stores) {
		if (markers == null) {
			markers = new ArrayList<Marker>();
		}
		if (googleMap != null && stores != null && stores.size() > 0) {
			markers.clear();
			for (int i = 0; i < stores.size(); i++) {
				StoreInfo store = stores.get(i);
				try {
					MarkerOptions markerOptions = createMapMaker(store);
					markers.add(googleMap.addMarker(markerOptions));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stores.get(0).latitude, stores.get(0).longitude), defaultZoom));
		}

		vStoreTray.refresh(stores);

	}

	public static MarkerOptions createMapMaker(StoreInfo store) throws Exception {
		LatLng latlng = new LatLng(store.latitude, store.longitude);
		MarkerOptions markerOptions = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_location)).flat(true);
		return markerOptions;
	}

	private void setupMapAndClient() {
		if (googleMap == null) {
			googleMap = mapFragment.getMap();
			// mapFragment.setOnDragListener(this);
		}

		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, this);
			mLocationClient.connect();
		}

		if (googleMap != null) {
			googleMap.setOnMyLocationButtonClickListener(this);
			googleMap.setOnMarkerClickListener(this);

			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(false);

			// googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
		}
	}

	// Clear markers
	private void clearMarkersFromMap() {
		if (markers != null) {
			markers.clear();
			if (googleMap != null) {
				googleMap.clear();
			}

		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Longitude = location.getLongitude();
		Latitude = location.getLatitude();

	}

	@Override
	public boolean onMyLocationButtonClick() {
		if (googleMap != null) {

			Location location = googleMap.getMyLocation();
			if (location != null) {
				pool.addHighPriorityTask(new DestinationSearchStore(this, location.getLatitude() + "", location.getLongitude() + ""));
			}
		}
		return false;
	}

	// private int moveActionCount = 0;

	@Override
	public boolean onMarkerClick(Marker marker) {
		if (googleMap != null) {
			LatLng latLng = marker.getPosition();
			googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom));
			int position = getPositionByMarker(marker);
			if (position != -1 && MStoreInfoTray.vGvImages != null && MStoreInfoTray.mAdapter != null) {
				MStoreInfoTray.vGvImages.setSelection(position);
				MStoreInfoTray.mAdapter.refreshSelectedPosition(position);
			}
		}
		return false;

	}

	/**
	 * 
	 * @param marker
	 * @return the index of store which match the marker
	 */
	private int getPositionByMarker(Marker marker) {
		int position = -1;
		if (markers != null) {
			for (int i = 0; i < markers.size(); i++) {
				Marker cMarker = markers.get(i);
				if (cMarker.equals(marker)) {
					position = i;
					break;
				}
			}
		}
		return position;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "onConnectionFailed: " + result.toString());
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.i(TAG, "GoogleMap connected.");
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "GoogleMap disconnected.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mLocationClient != null && !mLocationClient.isConnected()) {
			mLocationClient.connect();
		}
		vStoreTray.refresh(stores);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		etxtSearch.clearFocus();
		HideInputKeyboard();
		vStoreTray.refresh(stores);
	}

	@Override
	protected void onStart() {
		super.onStart();
		vStoreTray.refresh(stores);

	}

	@Override
	protected void onDestroy() {
		pool.shutdown();
		stores.clear();
		super.onDestroy();
	}

	private boolean CheckAddress() {
		strFirstName = etxtFirstName.getText().toString();
		strLastName = etxtLastName.getText().toString();
		strAddress1 = etxtAddress1.getText().toString();
		strAddress2 = etxtAddress2.getText().toString();
		strTownOrCity = etxtTownOrCity.getText().toString();
		strPostalCode = etxtPostalCode.getText().toString();
		String tempShowState = txtState.getText().toString();
		if (strFirstName.length() > 0 && strLastName.length() > 0 && strAddress1.length() > 0 && strTownOrCity.length() > 0 && strPostalCode.length() > 0 && tempShowState.length() > 0) {
			return true;
		} else {
			showErrorMessage();
			return false;
		}

	}

	protected boolean saveAddressValue() {
		if (CheckAddress()) {
			mLocalCustomerInfo.setShipFirstName(strFirstName);
			mLocalCustomerInfo.setShipLastName(strLastName);
			mLocalCustomerInfo.setShipAddress1(strAddress1);
			mLocalCustomerInfo.setShipAddress2(strAddress2);
			mLocalCustomerInfo.setShipCity(strTownOrCity);
			mLocalCustomerInfo.setShipZip(strPostalCode);
			state = mShoppingCartManager.getmStateKeyList().get(mShoppingCartManager.getmStateNumberPicker());
			mLocalCustomerInfo.setShipState(state);
			mLocalCustomerInfo.save(BaseDestinationStoreSelectionActivity.this);
			return true;
		} else {
			return false;
		}
	}

	protected void getSaveAddressValue() {
		strFirstName = mLocalCustomerInfo.getShipFirstName();
		strLastName = mLocalCustomerInfo.getShipLastName();
		strAddress1 = mLocalCustomerInfo.getShipAddress1();
		strAddress2 = mLocalCustomerInfo.getShipAddress2();
		strTownOrCity = mLocalCustomerInfo.getShipCity();
		strPostalCode = mLocalCustomerInfo.getShipZip();
		state = mLocalCustomerInfo.getShipState();

	}

	protected void setSaveAddressValue() {
		getSaveAddressValue();
		if (strFirstName.length() > 0 && strLastName.length() > 0 && strAddress1.length() > 0 && strTownOrCity.length() > 0 && strPostalCode.length() > 0 && state.length() > 0) {
			etxtFirstName.setText(strFirstName);
			etxtLastName.setText(strLastName);
			etxtAddress1.setText(strAddress1);
			etxtAddress2.setText(strAddress2);
			etxtTownOrCity.setText(strTownOrCity);
			etxtPostalCode.setText(strPostalCode);
			String stateShow = "";
			for (int i = 0; i < stateKey.size(); i++) {
				if (state.equals(stateKey.get(i))) {
					stateShow = stateValue.get(i);
					break;
				}
			}
			txtState.setText(stateShow);
		}
	}

	private void getRetailerType() {
		mShoppingCartManager = ShoppingCartManager.getInstance();
		productDescriptionId = mShoppingCartManager.getProductDescriptionIDs();
		DestinationMethodTask mDestinationMethodTask = new DestinationMethodTask(this);
		Log.i(TAG, productDescriptionId);
		mDestinationMethodTask.execute(productDescriptionId);
	}

	private void showDeliverMethodType() {
		View popView = getLayoutInflater().inflate(R.layout.popup_deliver_method, null);
		mPopupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
		txtShipHome = (TextView) popView.findViewById(R.id.tv_popup_deliverMethod_home);
		txtPickStore = (TextView) popView.findViewById(R.id.tv_popup_deliverMethod_store);
		txtPickStore.setVisibility(View.GONE);
		txtShipHome.setVisibility(View.GONE);
	}

	private void showErrorMessage() {
		if ("".equals(strFirstName) && strFirstName.length() == 0) {
			lineErrorFirstName.setVisibility(View.VISIBLE);
			lineErrorLastName.setVisibility(View.INVISIBLE);
			lineErrorAddress1.setVisibility(View.INVISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.INVISIBLE);
			lineErrorPostalCode.setVisibility(View.INVISIBLE);
			lineErrorState.setVisibility(View.INVISIBLE);
			return;
		}
		if ("".equals(strLastName) && strLastName.length() == 0) {
			lineErrorFirstName.setVisibility(View.INVISIBLE);
			lineErrorLastName.setVisibility(View.VISIBLE);
			lineErrorAddress1.setVisibility(View.INVISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.INVISIBLE);
			lineErrorPostalCode.setVisibility(View.INVISIBLE);
			lineErrorState.setVisibility(View.INVISIBLE);
			return;
		}
		if ("".equals(strAddress1) && strAddress1.length() == 0) {
			lineErrorFirstName.setVisibility(View.INVISIBLE);
			lineErrorLastName.setVisibility(View.INVISIBLE);
			lineErrorAddress1.setVisibility(View.VISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.INVISIBLE);
			lineErrorPostalCode.setVisibility(View.INVISIBLE);
			lineErrorState.setVisibility(View.INVISIBLE);
			return;
		}
		if ("".equals(strTownOrCity) && strTownOrCity.length() == 0) {
			lineErrorFirstName.setVisibility(View.INVISIBLE);
			lineErrorLastName.setVisibility(View.INVISIBLE);
			lineErrorAddress1.setVisibility(View.INVISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.VISIBLE);
			lineErrorPostalCode.setVisibility(View.INVISIBLE);
			lineErrorState.setVisibility(View.INVISIBLE);
			return;
		}
		if ("".equals(strPostalCode) && strPostalCode.length() == 0) {
			lineErrorFirstName.setVisibility(View.INVISIBLE);
			lineErrorLastName.setVisibility(View.INVISIBLE);
			lineErrorAddress1.setVisibility(View.INVISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.INVISIBLE);
			lineErrorPostalCode.setVisibility(View.VISIBLE);
			lineErrorState.setVisibility(View.INVISIBLE);
			return;
		}
		String tempStateShow = txtState.getText().toString();
		Log.e(TAG, "tempStateShow==" + tempStateShow);
		if ("".equals(tempStateShow) && tempStateShow.length() == 0) {
			lineErrorFirstName.setVisibility(View.INVISIBLE);
			lineErrorLastName.setVisibility(View.INVISIBLE);
			lineErrorAddress1.setVisibility(View.INVISIBLE);
			lineErrorAddress2.setVisibility(View.INVISIBLE);
			lineErrorCity.setVisibility(View.INVISIBLE);
			lineErrorPostalCode.setVisibility(View.INVISIBLE);
			lineErrorState.setVisibility(View.VISIBLE);
			return;
		}
		lineErrorFirstName.setVisibility(View.INVISIBLE);
		lineErrorLastName.setVisibility(View.INVISIBLE);
		lineErrorAddress1.setVisibility(View.INVISIBLE);
		lineErrorAddress2.setVisibility(View.INVISIBLE);
		lineErrorCity.setVisibility(View.INVISIBLE);
		lineErrorPostalCode.setVisibility(View.INVISIBLE);
		lineErrorState.setVisibility(View.INVISIBLE);
	}

	private void HideInputKeyboard() {
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(BaseDestinationStoreSelectionActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		etxtSearch.clearFocus();
	}
	
	private void showInputKeyboard() {
		etxtSearch.requestFocus();
		InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void getStateList() {
		mShoppingCartManager = ShoppingCartManager.getInstance();
		mCountryInfos = KM2Application.getInstance().getCountryInfoList();
		if (mCountryInfos != null && mCountryInfos.size() > 0) {
			HashMap<String, String> stateHashMap = mCountryInfos.get(0).countrySubregions;
			Iterator ite = stateHashMap.entrySet().iterator();
			while (ite.hasNext()) {
				Map.Entry entry = (Map.Entry) ite.next();
				String keyString = (String) entry.getKey();
				String valueString = (String) entry.getValue();
				stateKey.add(keyString);
				stateValue.add(valueString);
			}
			mShoppingCartManager.setmStateKeyList(stateKey);
			mShoppingCartManager.setmStateValueList(stateValue);
		}
	}

	private boolean TestStoreBackDoor() {
		String storeName = etxtSearch.getText().toString().trim();
		if (storeName.equalsIgnoreCase(StoreInfo.TEST_STORE_ON)) {
			SharedPreferrenceUtil.setBoolean(BaseDestinationStoreSelectionActivity.this, StoreInfo.IS_TEST_STORE, true);
			// etxtSearch.setTextColor(getResources().getColor(R.color.kodak_red));
			return true;
		} else if (storeName.equalsIgnoreCase(StoreInfo.TEST_STORE_OFF)) {
			SharedPreferrenceUtil.setBoolean(BaseDestinationStoreSelectionActivity.this, StoreInfo.IS_TEST_STORE, false);
			// etxtSearch.setTextColor(getResources().getColor(R.color.near_black));
			return false;
		}
		return false;
	}
}
