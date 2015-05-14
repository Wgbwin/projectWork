package com.kodakalaris.kodakmomentslib.activity.appintro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.home.THomeActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.SimpleCarouselAdapter;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.widget.Carousel;

public class TAppIntroActivity extends BaseAppIntroActivity {
	private Carousel vCarousel;
	private ImageView vImgSplash;
	private TextView vTxtWelcome;
	private TextView vTxtInfo;
	private LinearLayout vLineLyDotsSet;
	private Button vBtnStart;
	private TextView vTxtLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_t_app_intro);
		initView();
		initData();
		initLineLyDotsSet();
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

	private void initView() {
		vCarousel = (Carousel) findViewById(R.id.carousel_intro_t_container);
		vImgSplash = (ImageView) findViewById(R.id.img_intro_t_splash);
		vTxtWelcome = (TextView) findViewById(R.id.txt_intro_t_welcome);
		vTxtInfo = (TextView) findViewById(R.id.txt_intro_t_info);
		vLineLyDotsSet = (LinearLayout) findViewById(R.id.lineLy_intro_t_dotsSet);
		vBtnStart = (Button) findViewById(R.id.btn_intro_t_start);
		vTxtLink = (TextView) findViewById(R.id.txt_intro_t_link);
	}

	private void initData() {
		SimpleCarouselAdapter adapter = new SimpleCarouselAdapter(this, mDefaultImgs);
		vCarousel.setAdapter(adapter);
	}

	private void initLineLyDotsSet() {
		vLineLyDotsSet.removeAllViews();
		for (int i = 0; i < mDefaultImgs.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setImageResource(R.drawable.intro_dots);
			imageView.setTag(i);
			imageView.setEnabled(true);
			vLineLyDotsSet.addView(imageView);
		}
		vLineLyDotsSet.getChildAt(0).setEnabled(false);
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
				Intent intent = new Intent(TAppIntroActivity.this,
						THomeActivity.class);
				// need to modify
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});

		// vTxtLink
		vTxtLink.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TAppIntroActivity.this,
						MAppIntroActivity.class);
				// need to modify
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);

			}
		});

		// vCarousel
		vCarousel.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < mDefaultImgs.length; i++) {
					vLineLyDotsSet.getChildAt(i).setEnabled(true);
				}
				vLineLyDotsSet.getChildAt(position)
						.setEnabled(false);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	@Override
	protected void showCountrySelectionDialog() {
		
	}

	@Override
	protected void showEulaDialog() {
		
	}

	@Override
	protected void showErrorDialog(WebAPIException e) {
		
	}

	@Override
	protected void getDataCompleted() {
		
	}

	@Override
	protected void dismissSplashView() {
		
	}

}
