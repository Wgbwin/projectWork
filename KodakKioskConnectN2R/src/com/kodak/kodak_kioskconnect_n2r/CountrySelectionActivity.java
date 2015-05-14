package com.kodak.kodak_kioskconnect_n2r;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.utils.RSSLocalytics;

public class CountrySelectionActivity extends BaseActivity {
	private String TAG = CountrySelectionActivity.class.getSimpleName();

	private Button btDone;
	private TextView tvTitle;
	private TextView tvCountry;
	private Button btCountry;
	private ListView lvCountry;
	private LinearLayout countryContainer;
	private CountryAdapter adapter;

	private List<String> countryCodes;
	private List<String> countryNames;

	private String selectedCountryName = "";
	private int selectedPosition = 0;

	private SharedPreferences prefs;
	private String FORM_SETTING = "isFromSetting";
	private boolean isFromSetting = false;
	public static final String COUNTRY_CHANGED = "isCountryChanged";
	private boolean isCountryChanged = false;
	public static HashMap<String, String> attr;
	public static String EVENT_CATALOG_COUNTRY = "Catalog Country";
	private String KEY_CATALOG_COUNTRY_CODE = "Catalog Country Code" ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.countryselection);
		setContentLayout(R.layout.countryselectionfield);
		getViews();
		initData();
		setEvents();
	}

	private void initLocalyticsData(String countryCode) {
		if(attr==null){
			attr = new HashMap<String, String>() ;
		}
		
		attr.put(KEY_CATALOG_COUNTRY_CODE, countryCode) ;
	}

	private void clearSelectedStoreInfo() {
		PrintHelper.selectedStore = null;
		if (prefs != null) {
			Editor editor = prefs.edit();
			editor.putString("selectedStoreName", "");
			editor.putString("selectedStoreAddress", "");
			editor.putString("selectedStoreHours", "");
			editor.putString("selectedCity", "");
			editor.putString("selectedPostalCode", "");
			editor.putString("selectedStoreEmail", "");
			editor.putString("selectedStorePhone", "");
			editor.putString("selectedStoreLatitude", "");
			editor.putString("selectedStoreLongitude", "");
			editor.putString("selectedStoreId", "");
			editor.putString("selectedRetailerId", "");
			editor.putString("selectedStoreCountry", "");
			editor.commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	class CountryHolder {
		TextView countryTV;
		ImageView imageView;
	}

	private class CountryAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public CountryAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if (countryNames != null) {
				return countryNames.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CountryHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_with_textview_imageview, parent ,false);

				holder = new CountryHolder();
				holder.countryTV = (TextView) convertView.findViewById(R.id.tvTest);
				holder.imageView = (ImageView) convertView.findViewById(R.id.ivTest);
				convertView.setTag(holder);
			} else {
				holder = (CountryHolder) convertView.getTag();
			}

			holder.imageView.setVisibility(View.GONE);
			holder.countryTV.setVisibility(View.VISIBLE);

			holder.countryTV.setText(countryNames.get(position));
			/*
			 * Log.w(TAG, "CountryName[" + position + ", " +
			 * countryNames.get(position) + "]");
			 */
			holder.countryTV.setTypeface(PrintHelper.tf);
			if (position == selectedPosition && selectedCountryName.equals(holder.countryTV.getText())) {
				holder.countryTV.setTextColor(Color.parseColor("#FBBA06"));
			} else {
				holder.countryTV.setTextColor(Color.parseColor("#FFFFFF"));
			}

			LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) holder.countryTV.getLayoutParams();
			params.topMargin = 8;
			params.bottomMargin = 8;
			holder.countryTV.setLayoutParams(params);
			return convertView;
		}

	}

	@Override
	public void getViews() {
		btDone = (Button) findViewById(R.id.next_btn);
		tvCountry = (TextView) findViewById(R.id.countrylabel);
		tvTitle = (TextView) findViewById(R.id.headerBar_tex);
		btCountry = (Button) findViewById(R.id.country_button);
		lvCountry = (ListView) findViewById(R.id.countryList);
		countryContainer = (LinearLayout) findViewById(R.id.country_container);

	}

	@Override
	public void initData() {
		btDone.setVisibility(View.VISIBLE);
		tvTitle.setVisibility(View.VISIBLE);
		btDone.setTypeface(PrintHelper.tf);
		tvTitle.setTypeface(PrintHelper.tf);
		tvCountry.setTypeface(PrintHelper.tf);
		btCountry.setTypeface(PrintHelper.tf);

		if (countryCodes == null) {
			countryCodes = new ArrayList<String>();
		} else {
			countryCodes.clear();
		}
		if (countryNames == null) {
			countryNames = new ArrayList<String>();
		} else {
			countryNames.clear();
		}
		if (PrintHelper.countries != null) {
			Iterator<Entry<String, String>> iter = PrintHelper.countries.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				countryCodes.add(entry.getKey());
				countryNames.add(entry.getValue());
			}
			/*
			 * Log.w(TAG, "countryCodes size:" + countryCodes.size());
			 * Log.w(TAG, "countryNames size:" + countryNames.size());
			 */
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String code = prefs.getString(MainMenu.SelectedCountryCode, "");
		if (code.equals("")) {
			if (countryNames.size() > 0) {
				selectedCountryName = countryNames.get(0);
				selectedPosition = 0;
			}
		} else if (countryNames.size() > 0 && countryCodes.size() > 0 && !countryCodes.contains(code)) {
			selectedCountryName = countryNames.get(0);
			selectedPosition = 0;
		} else {
			if (countryCodes.size() > 0 && countryNames.size() > 0 && countryCodes.size() == countryNames.size()) {
				for (int i = 0; i < countryCodes.size(); i++) {
					if (countryCodes.get(i).equals(code)) {
						selectedCountryName = countryNames.get(i);
						selectedPosition = i;
						break;
					}
				}
			}
		}
		initLocalyticsData(code) ;
		btCountry.setText(selectedCountryName);
		Bundle b = getIntent().getExtras();
		if (b != null) {
			isFromSetting = b.getBoolean(FORM_SETTING);
		}
		btDone.setText(getString(R.string.done));
		tvTitle.setText(getString(R.string.change_country));
		btCountry.setText(selectedCountryName);
		adapter = new CountryAdapter(this);
		lvCountry.setDivider(null);
		lvCountry.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void setEvents() {

		btDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String lastSelectedCountryCode = prefs.getString(MainMenu.SelectedCountryCode, "");
				String countryCode = "";
				for (int i = 0; i < countryNames.size(); i++) {
					if (countryNames.get(i).equals(selectedCountryName)) {
						countryCode= countryCodes.get(i) ;
						prefs.edit().putString(MainMenu.SelectedCountryCode, countryCode).commit();
						prefs.edit().putString(MainMenu.CurrentlyCountryCode, countryCode).commit();
						selectedPosition = i;
						break;
					}
				}
				
				initLocalyticsData(countryCode) ;
				if(countryCode!=null &&!"".equals(countryCode)){
					RSSLocalytics.recordLocalyticsEvents(CountrySelectionActivity.this, EVENT_CATALOG_COUNTRY, attr) ;
				}
				
				
				if (!lastSelectedCountryCode.toLowerCase().equals(prefs.getString(MainMenu.SelectedCountryCode, "").toLowerCase())) {
					isCountryChanged = true;
					clearSelectedStoreInfo();
					if (PrintHelper.stores != null) {
						PrintHelper.stores.clear();
					}
					//fixed for RSSMOBILEPDC-2126 by song
					/*if (PrintHelper.countryInfoMap != null) {
						PrintHelper.selectedCountryInfo = PrintHelper.countryInfoMap.get(prefs.getString(MainMenu.SelectedCountryCode, ""));
					}*/

					SharedPreferences prefs;
					Editor editor;
					prefs = PreferenceManager.getDefaultSharedPreferences(CountrySelectionActivity.this);
					editor = prefs.edit();
					editor.putString("selectedRetailerInfo", "");
					editor.putBoolean("ifFollowCLO", false); // reset the value
					editor.putString("defaultSize", ""); // result the printSize
															// when change the
															// country
					editor.commit();
					PrintHelper.products = null;
					PrintHelper.StartOver();
					PrintHelper.defaultPrintSizeIndex = 0;// result the index of
															// printSize when
															// change the
															// country
					try {
						Thread thrd = new Thread() {
							@Override
							public void run() {
								PrintMakerWebService service = new PrintMakerWebService(CountrySelectionActivity.this, "");
								// service.getPrintProducts(false,"");
								service.GetRequiredContactInformation(CountrySelectionActivity.this);
								if (isFromSetting) {
									Bundle b = new Bundle();
									b.putBoolean(CountrySelectionActivity.COUNTRY_CHANGED, isCountryChanged);
									Intent mIntent = new Intent(CountrySelectionActivity.this, NewSettingActivity.class);
									mIntent.putExtras(b);
									mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(mIntent);
								} else {
									finish();
								}
							}
						};
						thrd.start();
					} catch (Exception ex) {
						Log.e(TAG, "Error getting print prices");
					}

				} else {
					finish();
				}
			}
		});

		lvCountry.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selectedCountryName = countryNames.get(position);
				btCountry.setText(selectedCountryName);
				selectedPosition = position;
				/*
				 * Log.e(TAG, "[CountryCode:" + countryCodes.get(position) +
				 * ", CountryName:" + selectedCountryName + "]");
				 */
				countryContainer.setVisibility(View.GONE);
			}

		});

		btCountry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapter.notifyDataSetChanged();
				countryContainer.setVisibility(View.VISIBLE);
			}
		});

	}

}
