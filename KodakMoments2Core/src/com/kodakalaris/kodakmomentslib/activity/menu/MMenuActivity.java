package com.kodakalaris.kodakmomentslib.activity.menu;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.countryselection.MCountrySelectionActivity;
import com.kodakalaris.kodakmomentslib.activity.eulaprivacy.MEulaPrivacyActivity;
import com.kodakalaris.kodakmomentslib.bean.OptionsDialogModel;
import com.kodakalaris.kodakmomentslib.bean.OptionsModel;
import com.kodakalaris.kodakmomentslib.fragment.mobile.menu.MMentRadioFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.menu.MMenuSettingFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.menu.MSettingsAboutFragment;
import com.kodakalaris.kodakmomentslib.fragment.mobile.menu.MSettingsLicenseFragment;
import com.kodakalaris.kodakmomentslib.interfaces.menu.OnHeadlineSelectedListener;


public class MMenuActivity extends BaseMenuActivity implements OnHeadlineSelectedListener {
	
	private TextView vTxtTitleBar;
	private ImageView vImgBack;
	private Fragment fragment = null;
	private OptionsDialogModel mOptionsDialogModel;
	private ArrayList<OptionsModel> optionModelArrayList;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_m_menu);
		initViews();
		initData();
		initEvent();
	}

	private void initEvent() {
		vImgBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackRunning();
			}
		});
	}

	private void initViews() {
		vTxtTitleBar = (TextView) findViewById(R.id.txt_title_bar);
		vImgBack = (ImageView) findViewById(R.id.img_title_bar_back);
	}
	
	@Override
	protected void initData() {
		super.initData();
		mOptionsDialogModel = new OptionsDialogModel();
		optionModelArrayList = new ArrayList<OptionsModel>();

		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction  transaction = mFragmentManager.beginTransaction();

		Intent intent = getIntent();
		String action = intent.getStringExtra(ACTION_KEY);
		
		if(ACTION_VALUE_CART.equals(action.trim())){
			
		} 
		else if(ACTION_VALUE_ORDER.equals(action.trim())){
			
		}
		else if(ACTION_VALUE_PROFILE.equals(action.trim())){
				
		}
		else if(ACTION_VALUE_SETTINGS.equals(action.trim())){
			fragment = MMenuSettingFragment.getSettingFragmentInstance();
			vTxtTitleBar.setText(R.string.Common_Settings);
		}
		else if(ACTION_VALUE_CLEARCART.equals(action.trim())){
			
		}
		else if(ACTION_VALUE_GALLERY.equals(action.trim())){
			
		}

		if (null != fragment) {
			transaction.replace(R.id.realLy_menu_framLy, fragment);
			transaction.commit();
		}
		//TODO To obtain mRodioData or mRodioData2 join mOptionsDialogModel
	}

	/**
	 * OnHeadlineSelectedListener 
	 */
	@Override
	public void onItemSelected(int position,String FragmentName) {
		if(AppConstants.MMENU_SETTING_FRAGMENT.equals(FragmentName)){
			menuSettingsFragSelect(position);
		}

	}

	private void menuSettingsFragSelect(int position) {
		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction  transaction = mFragmentManager.beginTransaction();
		Bundle bundle = new Bundle();
		
		if(AppConstants.SETTING_COUNTRY == position){
			/*vTxtTitleBar.setText(R.string.Settings_country);
			initRodioData(getCountryNames(), getCountryNameByCode(KM2Application.getInstance().getCountryCodeUsed()));
			mOptionsDialogModel.setTitle(getString(R.string.Settings_country));
			mOptionsDialogModel.setObjectsArraylist(optionModelArrayList );
			bundle.putInt(MMentRadioFragment.TYPE, MMentRadioFragment.TYPE_COUNTRY);
			bundle.putSerializable(MMentRadioFragment.OptionsDialogModel, mOptionsDialogModel);
			fragment = MMentRadioFragment.getDefaultSizeFragmentInstance(bundle);*/
			Intent intent = new Intent(this, MCountrySelectionActivity.class);
			startActivity(intent);
		}
		else if(AppConstants.SETTING_ABOUT == position){
			vTxtTitleBar.setText(R.string.Settings_about);
			mOptionsDialogModel.setTitle(getString(R.string.Settings_about));
			bundle.putSerializable(MMentRadioFragment.TYPE, mOptionsDialogModel);
			fragment = MSettingsAboutFragment.getAboutFragmentInstance();
		} 
		else if(AppConstants.SETTING_LICENSE == position){
			/*vTxtTitleBar.setText(R.string.EULAScreen_Title);
			mOptionsDialogModel.setTitle(getString(R.string.EULAScreen_Title));
			bundle.putSerializable(MMentRadioFragment.TYPE, mOptionsDialogModel);
			fragment = MSettingsLicenseFragment.getLicenseFragmentInstance();*/
			Intent intent = new Intent(this, MEulaPrivacyActivity.class);
			intent.putExtra(MEulaPrivacyActivity.TYPE, MEulaPrivacyActivity.TYPE_SETTING);
			startActivity(intent);
		}
		
		if (null != fragment) {
			transaction.replace(R.id.realLy_menu_framLy,fragment);
			transaction.addToBackStack(null);
			transaction.commit();
		}
	}

	private void initRodioData(List<String> rodioData, String name) {
		optionModelArrayList.clear();
		int selectedIndex = 0;
		for (int i = 0; i < rodioData.size(); i++) {
			OptionsModel optionsModel = new OptionsModel();
			if(rodioData.get(i).equals(name)){
				selectedIndex = i;
			}
			optionsModel.setTextValue(rodioData.get(i));
			optionModelArrayList.add(optionsModel);
		}
		optionModelArrayList.get(selectedIndex).setSelected(true);
		mOptionsDialogModel.setSelecterNum(selectedIndex);
	}

	public void onBackRunning() {
		FragmentManager mFragmentManager = getSupportFragmentManager();
		if (!mFragmentManager.popBackStackImmediate()) {
			finish();
		}else {
			if((fragment instanceof MMentRadioFragment || fragment instanceof MSettingsLicenseFragment 
					|| fragment instanceof MSettingsAboutFragment)){
				mFragmentManager.popBackStack();
				vTxtTitleBar.setText(R.string.Common_Settings);
			}
		}
	}

	@Override
	public void onBackPressed() {
		onBackRunning();
	}

}
