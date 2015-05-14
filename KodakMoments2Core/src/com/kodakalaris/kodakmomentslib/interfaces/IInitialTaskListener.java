package com.kodakalaris.kodakmomentslib.interfaces;

import com.kodakalaris.kodakmomentslib.exception.WebAPIException;

public interface IInitialTaskListener {

	public void onCountryCodeInvalided();
	public void onEulaOutdate();
	public void onInitialDataSucceed();
	public void onInitialDataFailed(WebAPIException e);
	public void onWelcomeConfigObtained();
}
