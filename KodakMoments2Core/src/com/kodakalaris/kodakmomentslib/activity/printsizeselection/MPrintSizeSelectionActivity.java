package com.kodakalaris.kodakmomentslib.activity.printsizeselection;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.imageselection.MImageSelectionMainActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.PrintSizeAndPriceAdapter;
import com.kodakalaris.kodakmomentslib.adapter.mobile.PrintSizeAndPriceAdapter.PrintSizeClickedListener;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfig;
import com.kodakalaris.kodakmomentslib.culumus.bean.config.KMConfigEntry;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.CountryInfo;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.RssEntry;
import com.kodakalaris.kodakmomentslib.manager.KMConfigManager;
import com.kodakalaris.kodakmomentslib.manager.PrintManager;
import com.kodakalaris.kodakmomentslib.widget.Carousel;
import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;

public class MPrintSizeSelectionActivity extends BasePrintSizeSelectionActivity {
	private Carousel vCarousel;
	private int[] mImgs;
	private SimpleCarouselAdapter vCarouselAdapter;
	private LinearListLayout vListSizeAndPrice;
	private MActionBar vActionBar;
	private PrintManager mPrintManager;
	private TextView vTxtTitle;
	private TextView vTxtSubtitle;
	private CountryInfo mCountryInfo;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_m_print_size_selection);
		initView();
		initData();
		initEvent();
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

	private void initEvent() {
		vCarousel.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				List<KMConfig> list = KMConfigManager.getInstance().getConfigs(KMConfig.Property.PRINTS_WORKFLOW_CAROUSEL);
				if (list != null & list.size() > 0) {
					KMConfigEntry info = list.get(0).configData.entries.get(position);
					vTxtTitle.setText(info.title);
					vTxtSubtitle.setText(info.subtitle);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}
			@Override
			public void onPageScrollStateChanged(int status) {

			}
		});
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}



	private void initData() {
		mPrintManager = PrintManager.getInstance(this);

		List<KMConfig> list = KMConfigManager.getInstance().getConfigs(KMConfig.Property.PRINTS_WORKFLOW_CAROUSEL);
		if (list != null & list.size() > 0) {
			vCarouselAdapter = new SimpleCarouselAdapter(this, list.get(0));
		} else {
			mImgs = new int[]{R.drawable.image_marketing_prints_1,R.drawable.image_marketing_prints_1,R.drawable.image_marketing_prints_1};
			vCarouselAdapter = new SimpleCarouselAdapter(this, mImgs);
		}
		vCarouselAdapter.setContainerSize(KM2Application.getInstance().getScreenW(), KM2Application.getInstance().getScreenH() / 2);
		vCarousel.setAdapter(vCarouselAdapter);

		
		mCountryInfo = KM2Application.getInstance().getCountryInfo();
		
		// RSSMOBILEPDC-2223 (Kane) - add the PrintSizeClickedListener
		vListSizeAndPrice.setAdapter(new PrintSizeAndPriceAdapter(this, mCountryInfo, new PrintSizeClickedListener() {
			
			@Override
			public void onPrintSizeClicked(RssEntry entry) {
				Intent intent = new Intent(MPrintSizeSelectionActivity.this, MImageSelectionMainActivity.class);
				// need to modify
				mPrintManager.setDefaultPrintSize(entry);
				startActivity(intent);
				finish();			
			}
		}));
	}

	private void initView() {
		vCarousel  = (Carousel) findViewById(R.id.carousel_print_size_selection);
		vListSizeAndPrice = (LinearListLayout) findViewById(R.id.list_size_price);
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
		vTxtTitle = (TextView) findViewById(R.id.txt_marketing_title);
		vTxtSubtitle = (TextView) findViewById(R.id.txt_marketing_subtitle);
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(MPrintSizeSelectionActivity.this, MHomeActivity.class);
		startActivity(intent);
		finish();
	}

}
