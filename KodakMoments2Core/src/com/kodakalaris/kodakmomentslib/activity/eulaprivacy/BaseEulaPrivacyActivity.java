package com.kodakalaris.kodakmomentslib.activity.eulaprivacy;

import com.kodakalaris.kodakmomentslib.KM2Application;
import com.kodakalaris.kodakmomentslib.activity.BaseNetActivity;

public class BaseEulaPrivacyActivity extends BaseNetActivity {
	protected String getEulaUrl() {
		//TODO hard code
		return "https://mykodakmoments.kodak.com/mob/eula.aspx?language=" + KM2Application.getInstance().getCountryCodeUsed();
	}

	protected String getPrivacyUrl() {
		//TODO hard code 
		return "https://mykodakmoments.kodak.com/mob/privacy.aspx?language=" + KM2Application.getInstance().getCountryCodeUsed();
	}
}
