package com.kodak.rss.tablet.view.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kodak.rss.tablet.R;

public class DialogCountrySelector implements OnClickListener{

	private static final String DEFAULT_COUNTRY_CODE = "US";
	
	private View dialogview;
	private Dialog dialog;
	private Context context;
	private List<String> countries;
	private Map<String, String> countryMap;
	private TextView countryLabel;
	private int selectIndex = 0;
	private PopupWindow window;	
	private int dialogLpWidth;
	private Button yesButton;
	
	private String oriCountryName = "";
	private String oriCountryCode = "";
	
	private onDialogErrorListener listener;
	public interface onDialogErrorListener{
		void onYes(String countryName, String countryCode, String oriCountryName, String oriCountryCode);
		void onDismiss();		
	}
	
	public void initCountrySelectorMessage(Context context, String message,String buttonName, Map<String,String> countryMap, String defaultCountryCode, onDialogErrorListener listener){
		this.listener = listener;
		this.context = context;
		this.countryMap = countryMap;
		this.countries = new ArrayList<String>(countryMap.values());		
		LayoutInflater inflater = LayoutInflater.from(context);
		dialogview = inflater.inflate(R.layout.dialog_country_selector, null);				
		
		dialogview.findViewById(R.id.yes).setOnClickListener(this);
		((TextView)dialogview.findViewById(R.id.message)).setText(message);
		countryLabel = (TextView)dialogview.findViewById(R.id.country);
		selectIndex = -1; // 0
		
		if(defaultCountryCode == null){
			defaultCountryCode = "";
		}
		
		String currentCountryName = countryMap.get(defaultCountryCode);
		oriCountryName = currentCountryName;
		oriCountryCode = defaultCountryCode;
		
//		if(currentCountryName == null){
//			for(String key:countryMap.keySet()){
//				if(key.equalsIgnoreCase(DEFAULT_COUNTRY_CODE)){
//					currentCountryName = countryMap.get(key);
//					break;
//				}
//			}	
//		}
		
		if(currentCountryName != null){
			for (int i = 0; i < countries.size(); i++) {
				if (countries.get(i).equals(currentCountryName)) {
					selectIndex = i;
					break;
				}
			}
		}
		if (selectIndex != -1) {
			oriCountryName = countries.get(selectIndex);
		}
//		oriCountryName = countries.get(selectIndex);
		countryLabel.setText(oriCountryName);
		countryLabel.setOnClickListener(this);
		
		yesButton = ((Button) dialogview.findViewById(R.id.yes));
		yesButton.setText(buttonName);
		if (selectIndex == -1) {
			yesButton.setEnabled(false);
		}else {
			yesButton.setEnabled(true);
		}	
		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		dialog.getWindow().setContentView(dialogview);
		
		ViewGroup.LayoutParams dialogLp = dialogview.getLayoutParams();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		dialogLp.height = dm.heightPixels *3 / 4;
		if (dm.heightPixels < 750) {
			dialogLpWidth = dm.heightPixels;			
		}else {
			dialogLpWidth = dm.heightPixels *3 / 4;			
		}
		dialogLp.width = dialogLpWidth;
		dialogview.setLayoutParams(dialogLp);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.yes){
			if (selectIndex == -1) return;
			if (dialog != null) {
				dialog.dismiss();
				if(listener!=null){
					String selectedCountryName = countries.get(selectIndex);
					String selectedCountryCode = "";
					for(String key : countryMap.keySet()){
						if(countryMap.get(key).equals(selectedCountryName)){
							selectedCountryCode = key;
						}
					}
					
					listener.onYes(selectedCountryName, selectedCountryCode, oriCountryName, oriCountryCode);
					listener.onDismiss();
				}
			}
		}else if(v.getId()==R.id.no){
			if (dialog != null) {
				dialog.dismiss();
				if(listener!=null){					
					listener.onDismiss();
				}
			}
		}else if(v.getId()==R.id.country){
			if(window!=null&&window.isShowing()){
				window.dismiss();
				return;
			}
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.pop_country, null);
			DisplayMetrics dm = context.getResources().getDisplayMetrics();	
			int height = (int) (dm.heightPixels *3/8 - 35*dm.density);
			int width = dialogLpWidth /2;
			window = new PopupWindow(view,width,height,true);			
			ListView listView  = (ListView)view.findViewById(R.id.list_countries);							
			ArrayAdapter<String> adapter = new PopCountriesArray(context,R.layout.pop_countries_item_1, countries,selectIndex);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					selectIndex = arg2;	
					yesButton.setEnabled(true);
					countryLabel.setText(countries.get(selectIndex));
					window.dismiss();
				}
			});
			listView.setAdapter(adapter);		
			window.showAtLocation(v, Gravity.NO_GRAVITY, (dialogLpWidth-width)/2, (int)v.getY()+v.getHeight());
		}
	}
}

class PopCountriesArray extends ArrayAdapter<String>{
     private int selectIndex;
	
	public PopCountriesArray(Context context, int textViewResourceId,List<String> objects,int index) {
		super(context, textViewResourceId, objects);
		this.selectIndex = index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		View v = super.getView(position, convertView, parent);
		TextView tv = (TextView) v.findViewById(R.id.text1);
		if(tv != null && position == selectIndex){
			tv.setTextColor(Color.YELLOW);
		}else{
			tv.setTextColor(Color.WHITE);
		}
		return v;
	}
			
}