package com.kodak.rss.tablet.bean;

import java.util.Locale;

import android.content.Context;

import com.kodak.rss.core.n2r.bean.retailer.ProductDescription;
import com.kodak.rss.tablet.R;

public class MainMenuItem {
	//The int value will ensure the order 
	public static final int TYPE_MY_PROJECTS = 1;
	public static final int TYPE_PRINT = 2;
	public static final int TYPE_PHOTOBOOK = 3;
	public static final int TYPE_GREETING_CARDS = 4;
	public static final int TYPE_POSTERS = 5;
	public static final int TYPE_CALENDARS = 6;
	public static final int TYPE_COLLAGE = 7;
	public static final int TYPE_KIOSK = 8;
		
	private String mText="";
	private int mImageId;
	private ProductDescription mProductDescription;
	private int mProductType;
	
	public MainMenuItem(Context context, ProductDescription productDescription) {
		mProductDescription = productDescription;
		
		String type = productDescription.type.toLowerCase();
		if (type.contains("print")) {
			init(context, TYPE_PRINT);
		} else if (type.contains("quickBook") || type.contains("photobook")) {
			init(context, TYPE_PHOTOBOOK);
		} else if (type.contains("greeting")) {//mygreeting change to greeting
			init(context, TYPE_GREETING_CARDS);
		} else if (type.contains("collages")) {
			init(context, TYPE_COLLAGE);
		} else if (type.contains("calendar")) {
			init(context, TYPE_CALENDARS);
		}
	}
	
	public MainMenuItem(Context context, int productType) {
		init(context, productType);
	}
	
	private void init(Context context, int productType) {
		mProductType = productType;
		
		switch (productType) {
		case TYPE_MY_PROJECTS:
			mText = context.getString(R.string.l_project);
			mImageId = R.drawable.button_myprojects;
			break;
		case TYPE_PRINT:
			mText = context.getString(R.string.l_print);
			mImageId = R.drawable.button_prints;
			break;
		case TYPE_PHOTOBOOK:
			mText = context.getString(R.string.l_book);
			mImageId = R.drawable.button_photobook;
			break;
		case TYPE_GREETING_CARDS:
			mText = context.getString(R.string.l_card);
			mImageId = R.drawable.button_cards;
			break;
		case TYPE_POSTERS:
			mText = context.getString(R.string.l_poster);
			mImageId = R.drawable.button_poster;
			break;
		case TYPE_CALENDARS:
			mText = context.getString(R.string.l_calendar);
			mImageId = R.drawable.button_calendars;
			break;
		case TYPE_COLLAGE:
			mText = context.getString(R.string.l_collage);
			mImageId = R.drawable.button_collages;
			break;
		case TYPE_KIOSK:
			mText = context.getString(R.string.l_kiosk_connect);
			mImageId = R.drawable.button_kioskconnect;
			break;
		}
		
		mText = mText.toUpperCase(Locale.getDefault());
	}
	
	public int getProductType() {
		return mProductType;
	}
	
	public String getText() {
		return mText;
	}
	
	public ProductDescription getProductDescription() {
		return mProductDescription;
	}
	
	public int getImageId() {
		return mImageId;
	}
	
}
