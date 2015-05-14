package com.kodakalaris.kodakmomentslib.activity.appintro;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.DataKey;
import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.eulaprivacy.MEulaPrivacyActivity;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.shoppingcart.MShoppingCartActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.thread.InitialDataTaskGroup;
import com.kodakalaris.kodakmomentslib.util.ConnectionUtil;
import com.kodakalaris.kodakmomentslib.util.CumulusDataUtil;
import com.kodakalaris.kodakmomentslib.util.SharedPreferrenceUtil;
import com.kodakalaris.kodakmomentslib.widget.Carousel;
import com.kodakalaris.kodakmomentslib.widget.WaitingDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.MCountrySelectionDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.MCountrySelectionDialog.OnCountrySelectedListener;

public class MAppIntroActivity extends BaseAppIntroActivity {
	private Carousel vCarousel;
	private ImageView vImgSplash;
	private TextView vTxtWelcome;
	private TextView vTxtInfo;
	private Button vBtnStart;
	private TextView vTxtLink;
	private MCountrySelectionDialog countrySelectionDialog;
	private WaitingDialog waitingDialog;
	private RelativeLayout vRelaLySplash;
	
	private boolean mNeedShowCountrySelection = true;
	private boolean mNeedGoNextAction = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_app_intro);
		initView();
		initData();
		setEvents();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (vCarousel != null) {
			vCarousel.startAutoFlip();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (vCarousel != null) {
			vCarousel.stopAutoFlip();
		}
	}

	private void initData() {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void setupCarousel() {
		List<KMConfig> list = KMConfigManager.getInstance().getConfigs(KMConfig.Property.WELCOME_CAROUSEL);
		SimpleCarouselAdapter adapter;
		if (list != null && list.size() > 0) {
			adapter = new SimpleCarouselAdapter(this, KMConfigManager.getInstance().getConfigs(KMConfig.Property.WELCOME_CAROUSEL).get(0));
			adapter.setImageScaleType(ScaleType.CENTER_CROP);
		} else {
			adapter = new SimpleCarouselAdapter(this, mDefaultImgs);
		}
		
		adapter.setLoadingProgressEnabled(false);
		
		vCarousel.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				List<KMConfig> list = KMConfigManager.getInstance().getConfigs(KMConfig.Property.WELCOME_CAROUSEL);
				if (list != null) {
					KMConfigEntry info = list.get(0).configData.entries.get(position);
					vTxtWelcome.setText(info.title);
					vTxtInfo.setText(info.subtitle);
					vBtnStart.setText(info.actionText);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
		
		vCarousel.setAdapter(adapter);
		//Because when carousel setAdapter(), it will setCurrentItem(), and setCurrentItem() may cause main thread block
		//I don't know the exact reason, so I set carousel Visibile GONE be setAdapter.
		//Note: If do setAdapter() before onresume ,there is no problem, strange.
		vCarousel.setVisibility(View.VISIBLE);
		
	}
	
	private void setEvents() {
		// vImgSplash
		vImgSplash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		// vBtnStart
		vBtnStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mNeedGoNextAction = true;
				
				if(mInitialDataTaskGroup!= null && mInitialDataTaskGroup.isTaskRunning()){
					showWaitingDialog();
					return;
				}
				
				if(mNeedShowCountrySelection && !CumulusDataUtil.isCountryCodeValid(KM2Application.getInstance().getCountryCodeUsed())
						&& ConnectionUtil.isConnected(MAppIntroActivity.this)){//TODO: temp code , when offline, dont'show it. Otherwise there will be some bugs
					showCountrySelectionDialog();
					return;
				}
				
				getDataCompleted();
			}
		});

		// vTxtLink
		vTxtLink.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO switch to Link Photos screen
				KM2Application.getInstance().devRestoreGlobalVariables();
//				startActivity(new Intent(MAppIntroActivity.this, MShoppingCartActivity.class));
				startActivity(new Intent(MAppIntroActivity.this, MLinkAccountsActivity.class));
			}
		});

		
	}

	private void initView() {
		vCarousel = (Carousel) findViewById(R.id.carousel_intro_container);
		vImgSplash = (ImageView) findViewById(R.id.img_intro_splash);
		vTxtWelcome = (TextView) findViewById(R.id.txt_intro_welcome);
		vTxtInfo = (TextView) findViewById(R.id.txt_intro_info);
		vBtnStart = (Button) findViewById(R.id.btn_intro_start);
		vTxtLink = (TextView) findViewById(R.id.txt_intro_link);
		vRelaLySplash = (RelativeLayout) findViewById(R.id.relaLy_splash);
		
		vCarousel.setVisibility(View.GONE);
	}
	
	private void showWaitingDialog() {
		waitingDialog = new WaitingDialog(MAppIntroActivity.this, false);
		waitingDialog.initDialog(R.string.TitlePage_Error_NoCatalog);
		waitingDialog.show(getSupportFragmentManager(), "mWaitingDialog");
	}
	
	private void dismissWaitingDialog(){
		if(waitingDialog != null){
			waitingDialog.dismiss();
		}
	}

	@Override
	protected void showCountrySelectionDialog() {
		if(!mNeedShowCountrySelection && (waitingDialog == null || !waitingDialog.isShowing())){
			mNeedShowCountrySelection = true;
			return;
		}
		mNeedShowCountrySelection = false;
		dismissWaitingDialog();
		countrySelectionDialog = new MCountrySelectionDialog(this, false, true);
		countrySelectionDialog.initDialog(this, new OnCountrySelectedListener() {
			
			@Override
			public void onCountrySelected() {
				mInitialDataTaskGroup = new InitialDataTaskGroup(MAppIntroActivity.this, MAppIntroActivity.this);
				mInitialDataTaskGroup.execute();
			}
		});
		countrySelectionDialog.show(getSupportFragmentManager(), "mCountrySelection");
	}

	@Override
	protected void showEulaDialog() {
		// TODO show Eula dialog
		dismissWaitingDialog();
	}

	@Override
	protected void showErrorDialog(WebAPIException e) {
		dismissWaitingDialog();
		e.handleException(this);
	}

	@Override
	protected void getDataCompleted() {
		dismissWaitingDialog();
		
		if (mNeedGoNextAction) {
			if(!mEulaAccepted){
				Intent intent = new Intent(MAppIntroActivity.this, MEulaPrivacyActivity.class);
				intent.putExtra(MEulaPrivacyActivity.TYPE, MEulaPrivacyActivity.TYPE_WELCOME);
				startActivity(intent);
			} else {
				Intent intent = new Intent(MAppIntroActivity.this, MHomeActivity.class);
				// need to modify
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		}
	}
	
	@Override
	protected void dismissSplashView() {
		setupCarousel();
		vRelaLySplash.setVisibility(View.GONE);
	}


}
