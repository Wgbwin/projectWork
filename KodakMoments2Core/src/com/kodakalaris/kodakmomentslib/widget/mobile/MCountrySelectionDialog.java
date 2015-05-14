package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.simonvt.numberpicker.NumberPicker;
import net.simonvt.numberpicker.NumberPicker.OnScrollListener;
import net.simonvt.numberpicker.NumberPicker.OnValueChangeListener;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.util.CumulusDataUtil;
import com.kodakalaris.kodakmomentslib.util.DimensionUtil;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.R;

public class MCountrySelectionDialog extends BaseGeneralAlertDialogFragment {
	
	private LinkedHashMap<String, String> mCountries;
	private Context mContext;
	private CustomNumberPicker numberPicker = null;
	private PopupWindow popupWindow = null;
	private boolean mFromHome = false;
	private String mCountryName;
	private Button btSelectCountry;
	
	public MCountrySelectionDialog(Context context, boolean cancelable, boolean isFromHomeScreen) {
		super(context, cancelable);
		mCountries = KM2Application.getInstance().getCountries();
		mContext = context;
		mFromHome = isFromHomeScreen;
	}

	@Override
	protected View initMessageContent() {
		final View content = LayoutInflater.from(mContext).inflate(R.layout.dialog_country_selection, null);
		TextView tvMessage = (TextView) content.findViewById(R.id.tv_message);
		if(mFromHome){
			tvMessage.setText(R.string.KMTopScreen_Location_Not_Determined);
		} else {
			tvMessage.setText(R.string.KMMenu_Country_DialogMessage);
		}
		final List<String> countries = getCountryNames();
		String countryCode = KM2Application.getInstance().getCountryCodeUsed();
		if(mCountries!=null){
			mCountryName = mCountries.get(countryCode);
		}
		
		
		btSelectCountry = (Button) content.findViewById(R.id.bt_select_country);	
		if(("".equalsIgnoreCase(mCountryName) || mCountryName==null) && countries.size()>0){
			mCountryName = countries.get(0);
		}
		btSelectCountry.setText(mCountryName);
		btSelectCountry.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(countries == null || countries.size()==0){
					return;
				}
				View popContentView = LayoutInflater.from(mContext.getApplicationContext()).inflate(R.layout.number_picker, null);
				String[] arrCountries = new String[countries.size()];
				countries.toArray(arrCountries);
				int valueIndex = 0;
				if(countries.size() > 0 && countries.contains(mCountryName)){
					valueIndex = countries.indexOf(mCountryName);
				}
				numberPicker = (CustomNumberPicker) popContentView.findViewById(R.id.numberPicker);
				numberPicker.setMaxValue(countries.size()-1);
				numberPicker.setMinValue(0);
				numberPicker.setDisplayedValues(arrCountries);
				numberPicker.setValue(valueIndex);
				numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
					
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
						String countryName = getCountryNames().get(numberPicker.getValue());
						btSelectCountry.setText(countryName);
						
					}
				});
				
				
				int pending = DimensionUtil.dip2px(mContext, 16);
				if (null != popupWindow && popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					popupWindow = new PopupWindow(popContentView, content.getWidth() + 2 * pending, content.getHeight(), true);
					popupWindow.setFocusable(true);
					popupWindow.setOutsideTouchable(true);
					popupWindow.setBackgroundDrawable(new PaintDrawable(Color.WHITE));
					popupWindow.showAsDropDown(content, -pending, 0);
					
					popupWindow.setOnDismissListener(new OnDismissListener() {
						
						@Override
						public void onDismiss() {
							btSelectCountry.setTextColor(mContext.getResources().getColor(R.color.near_black));
						}
					});
					btSelectCountry.setTextColor(mContext.getResources().getColor(R.color.kodak_red));
				}
			}
		});
		return content;
	}

	public MCountrySelectionDialog initDialog(Context context, final OnCountrySelectedListener listener){
		setTitle(context.getString(R.string.SettingsScreen_SelectCountry));
		
		setPositiveButton(context.getString(R.string.Common_Done), new OnClickListener() {
			
			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				if(mCountries!=null && mCountries.size()>0){
					String countryCode = CumulusDataUtil.findCountryCodeByName(mCountries, btSelectCountry.getText().toString().trim());
					if(mFromHome){
						KM2Application.getInstance().setCountryCodeUsed(countryCode);
					} else {
						SharedPreferrenceUtil.setString(mContext, DataKey.LAST_SELECTED_COUNTRY, countryCode);
					}
				}
				listener.onCountrySelected();
				dismiss();
			}
		});
		//setContentAreaSize(0.665f, 0.516f);
		setContentAreaSize(0.665f, 0.35f);
		return this;
	}
	
	protected List<String> getCountryNames(){
		List<String> countryNames = new ArrayList<String>();
		if(mCountries != null){
			countryNames.addAll(mCountries.values());
		}
		return countryNames;
	}
	
	protected String getCountryNameByCode(String countryCode) {
		String countryName = "";
		if(mCountries != null){
			countryName = mCountries.get(countryCode);
		}
		return countryName;
	}
	
	public interface OnCountrySelectedListener {
		public void onCountrySelected();
	}
}
