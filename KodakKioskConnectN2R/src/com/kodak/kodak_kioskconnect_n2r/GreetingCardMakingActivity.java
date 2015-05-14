/**
 * GreetingCardMakingActivity.java
 * com.kodak.kodak_kioskconnect_n2r
 * Created by Sunny on Nov 12, 2013
 * Copyright (c) 2013 Kodak(China) All Rights Reserved
 */
package com.kodak.kodak_kioskconnect_n2r;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.kodak.kodak_kioskconnect_n2r.GreetingCardActionFragment.IActionListener;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardListFragment.IonCardItemSelectedListener;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCard;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.RSSLocalytics;

/**
 * Select the template and you can make it
 * @author Sunny
 *
 */
public class GreetingCardMakingActivity extends FragmentActivity implements IonCardItemSelectedListener,IActionListener{
	
	private GreetingCard mSelectedCard ;
	
	private ProgressBar vProgressBar ;
	
	private GreetingCardManager manager;
	
	private LinearLayout vLayoutList ;
	
	private LinearLayout vLayoutAction ;
	
	private List<GreetingCard> mGreetingCardTemplateList ;
	
	private GetGreetingCardTemplateInfoTask mGetGreetingCardTemplateInfoTask ;
	
	private static final int RESULT_ERROR =2;
	
	private final String SCREEN_NAME = "GC Type";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this) ;
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_greetingcard_making) ;
		
		findViews() ;
		init() ;
		
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RSSLocalytics.onActivityResume(this) ;
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		RSSLocalytics.onActivityPause(this) ;
	}
	
	
	private void findViews(){
		
		vProgressBar = (ProgressBar) findViewById(R.id.progressbar) ;
		vLayoutList = (LinearLayout) findViewById(R.id.layout_list) ;
		vLayoutAction = (LinearLayout) findViewById(R.id.layout_action) ;
		
	}
	
	private void init(){
		manager = GreetingCardManager.getGreetingCardManager(getApplicationContext());
		
		Intent intent = getIntent() ;
		if(intent!=null) {
			mSelectedCard = (GreetingCard) intent.getSerializableExtra("selectedcard") ;
		}
		if(mSelectedCard!=null && Connection.isNetWorkAvailable(getApplicationContext())){
			mGetGreetingCardTemplateInfoTask = new GetGreetingCardTemplateInfoTask() ;
			mGetGreetingCardTemplateInfoTask.execute();
			
			
		}else {
			//network is not available we should back
			doBack() ;
			
		}
		
		
	}
	
	/**
	 * do something when press back or cancel
	 */
	private void doBack(){
		setResult(RESULT_ERROR) ;
		finish() ;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
//		super.onBackPressed();
		//removeFragmentsWithAnimition();
	}
	
	@SuppressLint("NewApi")
	private class GetGreetingCardTemplateInfoTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			vProgressBar.setVisibility(View.VISIBLE) ;
			
			
			
		}

		@Override
		protected Void doInBackground(Void... params) {
			String descIds = manager.getDesIds() ;
			mGreetingCardTemplateList = manager.getContentForDesigns(mSelectedCard.usage, mSelectedCard.id, descIds) ;
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if(mGreetingCardTemplateList!=null && mGreetingCardTemplateList.size()>0){
				//show the details
				FragmentManager fragmentManager = getSupportFragmentManager();
			    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				GreetingCardListFragment greetingCardListFragment = new GreetingCardListFragment() ;
				GreetingCardActionFragment greetingCardActionFragment = new GreetingCardActionFragment() ;
				
				Bundle bundle = new Bundle() ;
				bundle.putSerializable("greetingcardlist", (Serializable) mGreetingCardTemplateList);
				greetingCardListFragment.setArguments(bundle) ;
				
				bundle.putSerializable("greetingcard", (Serializable) mGreetingCardTemplateList.get(0));
				greetingCardActionFragment.setArguments(bundle) ;
				fragmentTransaction.add(R.id.layout_list, greetingCardListFragment, GreetingCardListFragment.class.getSimpleName());
				fragmentTransaction.add(R.id.layout_action, greetingCardActionFragment, GreetingCardActionFragment.class.getSimpleName());
				
				fragmentTransaction.commitAllowingStateLoss() ;
				 
				Animation animLeftIn = AnimationUtils.loadAnimation(GreetingCardMakingActivity.this, R.anim.left_in );
				vLayoutList.setAnimation(animLeftIn);
				animLeftIn.start();
				Animation animRightIn = AnimationUtils.loadAnimation(GreetingCardMakingActivity.this, R.anim.right_in );
				vLayoutAction.setAnimation(animRightIn);
				animRightIn.start();
				
				vProgressBar.setVisibility(View.GONE) ;
				
				
			}else {
				//ERROR or can not find template we should back 
				doBack();
				
			}
			
			
			
			
		}
		
	}




	@Override
	public void onCardItemSelected(int position) {
		// update the action fragment
		GreetingCardActionFragment actionFragment = (GreetingCardActionFragment) getSupportFragmentManager().findFragmentById(R.id.layout_action);
		
		if(actionFragment!=null){
			GreetingCard selectedCard = mGreetingCardTemplateList.get(position);
			actionFragment.updateViews(selectedCard);
			
			
		}else {
			FragmentManager fragmentManager = getSupportFragmentManager();
		    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			actionFragment = new GreetingCardActionFragment();
			Bundle bundle = new Bundle() ;
			bundle.putSerializable("greetingcard", (Serializable) mGreetingCardTemplateList.get(position));
			
			fragmentTransaction.add(R.id.layout_action, actionFragment, GreetingCardActionFragment.class.getSimpleName())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit() ;
            
            
            
            
			
		}
		
	}


	@Override
	public void doCancel() {
		// TODO Auto-generated method stub
		//maybe remove the fragment...
		
		removeFragmentsWithAnimition() ;

	}


	@Override
	public void doMakeThis(GreetingCard greetingCard, String productIdentifier) {
		// TODO Auto-generated method stub
		//remove the fragment and then finish this activity with  setResult
		if(PrintHelper.productWithId==null){
			PrintHelper.productWithId = new HashMap<String, String>() ;
		}
		/*PrintHelper.productWithId .clear() ;*/
		String name = "";
		for(PrintProduct pro : PrintHelper.products){
			if(productIdentifier.equals(pro.getId())){
				name = pro.getName();
				break;
			}
		}
		PrintHelper.productWithId.put(name, productIdentifier) ;
		
		Intent intent = new Intent() ;
		intent.putExtra("selectedGreetingcard", greetingCard);
		intent.putExtra("productIdentifier", productIdentifier);
		setResult(RESULT_OK, intent);
		removeFragmentsWithAnimition() ;
		
		
		
		
	}
	
	private void removeFragmentsWithAnimition(){
		Animation animLeftOut = AnimationUtils.loadAnimation(GreetingCardMakingActivity.this, R.anim.left_out) ;
		vLayoutList.startAnimation(animLeftOut) ;
		animLeftOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				finish() ;
			}
		}) ;
		
		
		Animation animRightOut = AnimationUtils.loadAnimation(GreetingCardMakingActivity.this, R.anim.right_out) ;
		vLayoutAction.startAnimation(animRightOut) ;
		
		
	}

}
