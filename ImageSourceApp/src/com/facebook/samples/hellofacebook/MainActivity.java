package com.facebook.samples.hellofacebook;

import java.util.Arrays;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.samples.hellofacebook.R;

public class MainActivity extends FragmentActivity{
	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private ImageView vImageFacebook ;	
	public static final int QUERY_TOKEN = 34;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_image_source_main);
		 
		 findViews() ;
		 setListeners() ;
		 init(savedInstanceState) ;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		  Session session = Session.getActiveSession();
	        Session.saveSession(session, outState);
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	
	

	private void findViews() {
		vImageFacebook = (ImageView) findViewById(R.id.image_facebook_icon) ;
	}
	
	private void setListeners() {
		vImageFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Session session = Session.getActiveSession();
				String token = session.getAccessToken() ;
				if(session.isOpened()&& !TextUtils.isEmpty(token)){
					//we can access data about your facebook account
					Log.e("sunny", "sunny:token onclick "+token) ;
					
					
				}else {
					 onClickLogin() ;
				}
				
			}
		}) ;

	}
	
	private void init(Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
		}
	}

	/**
	 * facebook 
	 */
	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback)
					 .setPermissions(Arrays.asList("user_groups", "user_friends", "user_photos","friends_photos")));
		} else {
			Session.openActiveSession(this, true, statusCallback);
		}
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            //do something with the token 
        	Session sessionResult = Session.getActiveSession() ;
        	
        	String token = sessionResult.getAccessToken() ;
        	Log.e("sunny", "sunny token "+token) ;
        	if(sessionResult.isOpened()){
        		
    			
        	}
        }
    }
	

}
