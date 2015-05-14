package com.kodak.kodak_kioskconnect_n2r.collage;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.os.AsyncTask;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.text.Font;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;

public class CollageFontCreateTask extends AsyncTask<Void, Void, Object> {
	private Context mContext;

	public CollageFontCreateTask(Context context) {
		mContext = context;
	}

	@Override
	protected Object doInBackground(Void... params) {
		CollageWebServices service = new CollageWebServices(mContext, "");
		List<Font> fonts;
		String language = Locale.getDefault().toString();
		fonts = service.getAvailableFontsTask(language);
		AppContext.getApplication().setFonts(fonts);
		return null;
	}

}
