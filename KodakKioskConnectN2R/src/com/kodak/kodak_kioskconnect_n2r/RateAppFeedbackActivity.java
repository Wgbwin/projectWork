package com.kodak.kodak_kioskconnect_n2r;

import com.AppContext;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
/**
 * 
 * rate the app
 * @author Sunny
 *
 */
public class RateAppFeedbackActivity extends Activity{
	private RatingBar vRatingBar ;
	
	private EditText vInputEditText ;
	
	private CheckBox vCheckEmail ;
	
	private Button vLaterBtn ;
	
	private Button vSendBtn ;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_rate_app_feedback);
		
		findViews() ;
		init() ;
		setListeners() ;
		
	}
	
	private void findViews(){
		vRatingBar = (RatingBar) findViewById(R.id.rating_bar) ;
		vInputEditText = (EditText) findViewById(R.id.input_edit) ;
		vCheckEmail = (CheckBox) findViewById(R.id.check_email) ;
		vLaterBtn = (Button) findViewById(R.id.later_button) ;
		vSendBtn = (Button) findViewById(R.id.send_button) ;
		AppContext.getApplication().setEmojiFilter(vInputEditText) ;
	}
	
	//TODO
	private void init(){
		
	}
	
	private void setListeners() {
		
		
		
		
	}

}
