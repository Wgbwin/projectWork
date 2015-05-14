package com.kodak.rss.tablet.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.retailer.Retailer;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.IMEUtil;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.StoreListAdapter;
import com.kodak.rss.tablet.thread.SingleThreadPool;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.view.MapFragment;
import com.kodak.rss.tablet.view.MapWrapperLayout.OnDragListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class StoreSelectActivity extends BaseNetActivity implements 
	ConnectionCallbacks, 
	OnConnectionFailedListener, 
	LocationListener,
	OnMyLocationButtonClickListener,
	GoogleMap.OnMarkerClickListener,
	OnDragListener{
	private final String TAG = StoreSelectActivity.class.getSimpleName();
	
	private TextView tvTitle;
	private GoogleMap googleMap;
	private EditText etSearch;
	private Button btDone;
	private ImageView ivSearch;
	private ListView lvStores;
	private ArrayList<StoreInfo> stores = new ArrayList<StoreInfo>();
	private StoreListAdapter mAdapter;
	private String searchKey = "";
	private Geocoder mGeocoder = null;
	private ProgressBar pbWaitting;
	private TextView tvErrorMessage;
	
	private int selectedStorePosition = -1;
	private boolean isWifiLocator = false;
	private String productDescIds = "";
	
	private LocationClient mLocationClient;
	private List<Marker> markers;
	private final int defaultZoom = 15;
	
	private View mapViewContainer;
	private SingleThreadPool pool;
	private boolean isFromCart = false;
	private InfoDialog cloDialog;
	
	private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store_select_hd);
		if(getIntent() != null){
			isWifiLocator = getIntent().getBooleanExtra(StoreInfo.IS_WIFI_LOCATOR, false);
			isFromCart = getIntent().getBooleanExtra("fromCart", false);
		}
		productDescIds = ShoppingCartUtil.getProductDescriptionIDs(RssTabletApp.getInstance().products);
		pool = new SingleThreadPool();
		initViews();
		initLocationService();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(mLocationClient!=null && mLocationClient.isConnected()){
					Location location = mLocationClient.getLastLocation();
					if(location != null){
						googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()), defaultZoom));
						pool.addHighPriorityTask(new SearchStore(StoreSelectActivity.this, ""+location.getLatitude(), ""+location.getLongitude()));
					}
				}
			}
		}, 200);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mLocationClient!=null && !mLocationClient.isConnected()){
			mLocationClient.connect();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mLocationClient != null){
			mLocationClient.disconnect();
		}
	}
	
	@Override
	protected void onDestroy() {
		pool.shutdown();
		super.onDestroy();
	}
	
	@Override
	protected boolean hasSideMenu() {
		return false;
	}

	private void initViews(){
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		etSearch = (EditText) findViewById(R.id.et_store_search);
		TextUtil.addEmojiFilter(etSearch);
		btDone = (Button) findViewById(R.id.bt_Done);
		ivSearch = (ImageView) findViewById(R.id.iv_store_search);
		lvStores = (ListView)findViewById(R.id.lv_stores);
		mAdapter = new StoreListAdapter(this, stores);
		pbWaitting = (ProgressBar) findViewById(R.id.pb_waiting);
		tvErrorMessage = (TextView) findViewById(R.id.tv_error);
		mapViewContainer = findViewById(R.id.mapview_container);
		boolean isTestMode = SharedPreferrenceUtil.getBoolean(this, StoreInfo.IS_TEST_STORE);
		if(isTestMode){
			mapViewContainer.setBackgroundResource(R.drawable.map_testmode_background);
		} else {
			mapViewContainer.setBackgroundResource(R.drawable.map_background);
		}
		setupMapAndClient();
		
		tvTitle.setText(R.string.StoreFinder_Title);
		lvStores.setAdapter(mAdapter);
		etSearch.setOnEditorActionListener(editorActionListener);
		ivSearch.setOnClickListener(clickListener);
		btDone.setOnClickListener(clickListener);
		lvStores.setOnItemClickListener(itemClickListener);
	}
	
	private void initLocationService(){
		String language = Locale.getDefault().getLanguage();
		String country = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
		if(country == null){
			country = "";
		}
		Locale locale = new Locale(language, country.equals("")?Locale.getDefault().getCountry():country);
		mGeocoder = new Geocoder(this, locale);
	}
	
	private void setupMapAndClient(){
		if (googleMap == null) {
			MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.mv_store_mapview);
			googleMap = fragment.getMap();
			fragment.setOnDragListener(this);
        }
		
		if (mLocationClient == null) {
            mLocationClient = new LocationClient(getApplicationContext(), this, this);
            mLocationClient.connect();
        }
		
		if(googleMap != null){
			googleMap.setOnMyLocationButtonClickListener(this);
			googleMap.setOnMarkerClickListener(this);
			
			googleMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(false);
			
			googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
		}
	}
	
	// Add markers to GoogleMap
	private void addMarkersToMap(List<StoreInfo> stores){
		if(markers == null){
			markers = new ArrayList<Marker>();
		}
		if(stores!=null && stores.size()>0){
			markers.clear();
			for(int i=0; i<stores.size(); i++){
				StoreInfo store = stores.get(i);
				try {
					MarkerOptions markerOptions = createMapMaker(store);
					markers.add(googleMap.addMarker(markerOptions));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//add by bing for skip to the first store
			try {			
				StoreInfo store = stores.get(0);					
				LatLng latlng = new LatLng(store.latitude, store.longitude);
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8));//defaultZoom
				markers.get(0).showInfoWindow();
			} catch (Exception e) {				
				e.printStackTrace();
			}
		}
	}
	
	public static MarkerOptions createMapMaker(StoreInfo store) throws Exception{
		LatLng latlng = new LatLng(store.latitude, store.longitude);
		MarkerOptions markerOptions = new MarkerOptions().position(latlng)
														 .icon(BitmapDescriptorFactory.fromResource(R.drawable.pinpoint32x39))
														 .flat(true);
		return markerOptions;
	}
	
	// Clear markers
	private void clearMarkersFromMap(){
		if(markers != null){
			markers.clear();
			googleMap.clear();
		}
	}
	
	private String trimNearColon(String str){
		if(str.contains(":")){
			StringBuilder sb = new StringBuilder();
			String[] arr = str.split(":");
			for(int i=0;i<arr.length;i++){
				sb.append(arr[i].trim());
				if(i+1<arr.length){
					sb.append(":");
				}
			}
			return sb.toString();
		}else{
			return new String(str);
		}
	}

	
	OnClickListener clickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if(id == R.id.bt_Done){
				if(selectedStorePosition != -1 && !isWifiLocator){
					StoreInfo previousStore = StoreInfo.loadSelectedStore(StoreSelectActivity.this);
					if(previousStore==null || previousStore.id.equals(stores.get(selectedStorePosition).id)){
						RSSLocalytics.recordLocalyticsEvents(StoreSelectActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_STORE_CHANGED);
					}
					saveStore(StoreSelectActivity.this, stores.get(selectedStorePosition));
					SharedPreferrenceUtil.setString(StoreSelectActivity.this, SharedPreferrenceUtil.SELECTED_RETAILER_ID, stores.get(selectedStorePosition).retailerID);
				}
				IMEUtil.hideSoftKeyboard(StoreSelectActivity.this);
				finish();
				 
			}
			else if(id == R.id.iv_store_search){
				searchKey = etSearch.getText().toString().trim();
				if(!"".equals(searchKey)){
					pool.addHighPriorityTask(new SearchStore(StoreSelectActivity.this, searchKey));
				} else {
					LatLng latLng = googleMap.getCameraPosition().target;
					pool.addHighPriorityTask(new SearchStore(StoreSelectActivity.this, latLng.latitude+"", latLng.longitude+""));
				}
				IMEUtil.hideSoftKeyboard(StoreSelectActivity.this);
			}
		}
	};
	
	OnEditorActionListener editorActionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(v.getId() == R.id.et_store_search){
				if(actionId == EditorInfo.IME_ACTION_DONE){
					searchKey = v.getText().toString().trim();
					String tempSearchKey = trimNearColon(searchKey);
					if(tempSearchKey.equalsIgnoreCase(StoreInfo.TEST_STORE_ON)){
						SharedPreferrenceUtil.setBoolean(StoreSelectActivity.this, StoreInfo.IS_TEST_STORE, true);
						mapViewContainer.setBackgroundResource(R.drawable.map_testmode_background);
						v.setText("");
					} else if(tempSearchKey.equalsIgnoreCase(StoreInfo.TEST_STORE_OFF)){
						SharedPreferrenceUtil.setBoolean(StoreSelectActivity.this, StoreInfo.IS_TEST_STORE, false);
						mapViewContainer.setBackgroundResource(R.drawable.map_background);
						v.setText("");
					} else {
						pool.addHighPriorityTask(new SearchStore(StoreSelectActivity.this, searchKey));
					}
				}
			}
			return false;
		}
	};
	
	
	
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectedStorePosition = position;
			mAdapter.refreshSelectedPosition(selectedStorePosition);
			StoreInfo store = stores.get(selectedStorePosition);
			if(store != null){
				LatLng latlng = new LatLng(store.latitude, store.longitude);
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, defaultZoom));
				markers.get(position).showInfoWindow();
			}
		}
	};
	
	final int SEARCH_STORE_SUCCESS = 0x000001; // the returned store list size is large than 0
	final int SEARCH_STORE_FAILED = 0x000002;  // the returned store list size is 0
	final int SEARCH_STORE_ERROR = 0x000003;   // the response is not correct, maybe internal error or others
	final int SEARCH_STORE_START = 0x000004;
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEARCH_STORE_START:
				stores.clear();
				mAdapter.updateStores(stores);
				
				selectedStorePosition = -1;
				tvErrorMessage.setVisibility(View.GONE);
				pbWaitting.setVisibility(View.VISIBLE);
				clearMarkersFromMap();
				break;
			case SEARCH_STORE_SUCCESS:
				ArrayList<StoreInfo> findStores = null;
				Bundle bundle = msg.getData();
				if (bundle != null) {
					findStores = (ArrayList<StoreInfo>) bundle.getSerializable("findStores");
				}
				if (findStores != null) {
					stores = findStores;
				}				
				pbWaitting.setVisibility(View.GONE);				
				mAdapter.updateStores(stores);
				addMarkersToMap(stores);				
				break;
			case SEARCH_STORE_FAILED:
				pbWaitting.setVisibility(View.GONE);
				tvErrorMessage.setVisibility(View.VISIBLE);
				tvErrorMessage.setText(R.string.StoreFinder_NoStoresFound);
				if(isFromCart && !SharedPreferrenceUtil.getBoolean(StoreSelectActivity.this, SharedPreferrenceUtil.ACCEPT_CLOLITE)){
					List<Retailer> retailers = RssTabletApp.getInstance().getRetailers();
					for(Retailer retailer : retailers){
						if(retailer.cloLite){
							if(cloDialog == null){
								cloDialog = new InfoDialog.Builder(StoreSelectActivity.this).setMessage(R.string.StoreFinder_CLOLite)
									.setNegativeButton(R.string.d_yes, new DialogInterface.OnClickListener() {
								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										SharedPreferrenceUtil.setBoolean(StoreSelectActivity.this, SharedPreferrenceUtil.ACCEPT_CLOLITE, true);
										// 1 is need to show Home Delivery button in Shopping Cart screen
										setResult(1);
										finish();
									}
								})
								.setPositiveButton(R.string.d_no, new DialogInterface.OnClickListener() {
								
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								}).create();
							}
							if(!cloDialog.isShowing()){
								if(!isFinishing()){
								cloDialog.show();}
								
							}
						}
					}
				}
				break;
			case SEARCH_STORE_ERROR:
				pbWaitting.setVisibility(View.GONE);
				tvErrorMessage.setVisibility(View.VISIBLE);
				tvErrorMessage.setText(R.string.StoreFinder_ProblemFindingStore);
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "onConnectionFailed: " + result.toString());
	}

	@Override
	public void onConnected(Bundle bundle) {
		Log.i(TAG, "GoogleMap connected.");
		mLocationClient.requestLocationUpdates(REQUEST, this);  // LocationListener
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "GoogleMap disconnected.");
	}

	@Override
	public boolean onMyLocationButtonClick() {
		Location location = googleMap.getMyLocation();
		if(location != null){
			pool.addHighPriorityTask(new SearchStore(this, location.getLatitude()+"", location.getLongitude()+""));
		}
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}
	
	private int moveActionCount = 0;
	@Override
	public void onDrag(MotionEvent motionEvent) {
		int action = motionEvent.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			moveActionCount = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			moveActionCount ++;
			break;
		case MotionEvent.ACTION_UP:
			if(moveActionCount > 2){
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						LatLng latLng = googleMap.getCameraPosition().target;
						pool.addHighPriorityTask(new SearchStore(StoreSelectActivity.this, latLng.latitude+"", latLng.longitude+""));
					}
				}, 500);
			}
			break;
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		LatLng latLng = marker.getPosition();
		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoom));
		int position = getPositionByMarker(marker);
		if(position != -1){
			lvStores.smoothScrollToPosition(position);
			mAdapter.refreshSelectedPosition(position);
			selectedStorePosition = position;
		}
		return false;
	}
	
	/**
	 * 
	 * @param marker
	 * @return the index of store which match the marker
	 */
	private int getPositionByMarker(Marker marker){
		int position = -1;
		if(markers != null){
			for(int i=0; i<markers.size(); i++){
				Marker cMarker = markers.get(i);
				if(cMarker.equals(marker)){
					position = i;
					break;
				}
			}
		}
		return position;
	}
	
	class CustomInfoWindowAdapter implements InfoWindowAdapter{
		
		private View mInfoWindow;
		private View mInfoContents;
		
		public CustomInfoWindowAdapter(){
			mInfoWindow = getLayoutInflater().inflate(R.layout.googlemap_marker_window, null);
			mInfoContents = getLayoutInflater().inflate(R.layout.googlemap_marker_content, null);
		}

		@Override
		public View getInfoContents(Marker marker) {
			TextView tvTitle = (TextView) mInfoContents.findViewById(R.id.tv_marker_title);
			int position = getPositionByMarker(marker);
						
			int selectPosition = -1;
			if (mAdapter != null) {
				selectPosition = mAdapter.getSelectedPosition();
			}
			if (selectPosition == -1) {
				return null;
			}
			
			if(position!=-1 && stores!=null && position<stores.size()){
				StoreInfo store = stores.get(position);
				String title = store.name + "\n";
				title += store.address.address1 + " " + (store.phone==null?"":store.phone);
				tvTitle.setText(title);
			}
			return mInfoContents;
		}

		@Override
		public View getInfoWindow(Marker marker) {
			return null;
		}
		
	}
	
	private void saveStore(Context context, StoreInfo store){
		store.saveAsSelectedAtLocal(context);
	}

	class SearchStore implements Runnable {
		private WebService webService;
		private String zip = "";
		private String latitude = "", longtitude = "";
		
		public SearchStore(Context context){
			this(context, "", "", "");
		}
		
		public SearchStore(Context context, String zip){
			this(context, zip, "", "");
		}
		
		public SearchStore(Context context, String latitude, String longtitude){
			this(context, "", latitude, longtitude);
		}
		
		private SearchStore(Context context, String zip, String latitude, String longtitude){
			this.webService = new WebService(context);
			this.zip = zip;
			this.latitude = latitude;
			this.longtitude = longtitude;
		}

		@Override
		public void run() {
			List<Address> addresses = null;
			ArrayList<StoreInfo> findStores = null;
			//change by bing for optimize code on 2015-2-15
//			if(stores == null){
//				stores = new ArrayList<StoreInfo>();
//			}
//			synchronized (stores) {
//				stores.clear();
				mHandler.obtainMessage(SEARCH_STORE_START).sendToTarget();
				boolean isGeocoderAvailable = false;
				
				// This section is checking whether the Geocoder is available
				final String defaultCity = "Rochester";
				try {
					addresses = mGeocoder.getFromLocationName(defaultCity, 5);
					if(addresses!=null && !addresses.isEmpty()){
						isGeocoderAvailable = true;
					}
				} catch (IOException e) {
					isGeocoderAvailable = false;
					Log.e(TAG, "Geocoder is not available on this device....");
				}
				
				if(!"".equals(zip)){
					addresses = null;
					// if Geocoder is available, but the zip is invalid then do not get stores, just show error message
					if(isGeocoderAvailable){
						try {
							addresses = mGeocoder.getFromLocationName(zip.trim(), 5);
						} catch (IOException e) {
							// do nothing
						}
						if(addresses==null || addresses.isEmpty()){
							mHandler.obtainMessage(SEARCH_STORE_ERROR).sendToTarget();
							return;
						}
					}
				}
				try {
					findStores = (ArrayList<StoreInfo>) webService.getStoresTask(zip, latitude, longtitude, productDescIds, true, isWifiLocator);
				} catch (RssWebServiceException e) {
					e.printStackTrace();
				}
				
				if(findStores == null){
					mHandler.obtainMessage(SEARCH_STORE_ERROR).sendToTarget();
				} else if (findStores.size() == 0){		
					mHandler.obtainMessage(SEARCH_STORE_FAILED).sendToTarget();
				} else {
					Message message = new Message();
					message.what = SEARCH_STORE_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putSerializable("findStores", findStores);
					message.setData(bundle);
					mHandler.sendMessage(message);
//					mHandler.obtainMessage(SEARCH_STORE_SUCCESS).sendToTarget();
				}
//			}
		}
		
	}

}
