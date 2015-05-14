package com.kodak.rss.tablet.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;

import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.util.ConnectionUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * The activity which extends from this class will detect the network.
 * When there is no network available, it will show a dialog.
 * @author Robin.Qian
 *
 */
public class BaseNetActivity extends BaseActivity{
	protected BroadcastReceiver networkChangeReceiver;
	protected boolean isNetworkChangeReceiverRegistered = false;
	protected InfoDialog noNetWorkDialog;
	protected InfoDialog networkWeakDialog;
	protected InfoDialog serverErrorDialog;
	protected InfoDialog appObsoleteDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		noNetWorkDialog = new InfoDialog.Builder(this)
							.setMessage(R.string.Task_No_Internet)
							.setPositiveButton(R.string.d_ok, null)
							.create();
		
		networkChangeReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				detectNetWork();
			}
		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try {
			unregisterReceiver(networkChangeReceiver);
		} catch (Exception e) {
			Log.e(BaseNetActivity.class.getSimpleName(),"unregister networkReceiver error",e);
		}
	}
	
	
	protected boolean detectNetWork(){
		if(!ConnectionUtil.isConnected(this)){
			if(!noNetWorkDialog.isShowing()){
				noNetWorkDialog.show();
			}
			return false;
		}else {
			new DetectInternetReachableTask().execute();
		}
		
		return true;
	}
	
	private class DetectInternetReachableTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return ConnectionUtil.isConnected(BaseNetActivity.this);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(!isFinishing()){
				if(result){
					if(noNetWorkDialog.isShowing()){
						noNetWorkDialog.dismiss();
					}
				}else{
					if(!noNetWorkDialog.isShowing()){
						noNetWorkDialog.show();
					}
				}
			}
		}
		
	}
	
	public void showNetworkWeakWarning(){
		if(networkWeakDialog == null){
			networkWeakDialog = new InfoDialog.Builder(this)
									.setMessage(R.string.error_cannot_connect_to_internet)
									.setPositiveButton(R.string.d_ok, null)
									.create();
		}
		
		if(!networkWeakDialog.isShowing() && !isFinishing()){
			networkWeakDialog.show();
		}
	}
	
	public void showServerErrorWarning(){
		if(serverErrorDialog == null){
			serverErrorDialog = new InfoDialog.Builder(this)
									.setMessage(R.string.error_server)
									.setPositiveButton(R.string.d_ok, null)
									.create();
		}
		
		if(!serverErrorDialog.isShowing() && !isFinishing()){
			serverErrorDialog.show();
		}
	}
	
	public void showAppObsoleteWarning(final RssWebServiceException e) {
		if(appObsoleteDialog == null){
			appObsoleteDialog = new InfoDialog.Builder(this)
									.setMessage(R.string.TitlePage_Error_App_Obsolete)
									.setPositiveButton(R.string.d_ok, new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											doOnClickOkForAppObsoleteDialog(e);
										}
									})
									.create();
		}
		
		if(!appObsoleteDialog.isShowing() && !isFinishing()){
			appObsoleteDialog.show();
		}
	}
	
	protected void doOnClickOkForAppObsoleteDialog(RssWebServiceException e) {
		if (e != null && e.getMessage() != null) {
			RssTabletApp.getInstance().appForbidden = true;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(e.getMessage()));
			startActivity(intent);
		}
	}
	
	/**
	 * Run this method in main-thread, it will show dialog
	 * @param e
	 */
	public void showErrorWarning(RssWebServiceException e){
		if ( e!= null) {
			if (e.isNetworkWeak()) {
				showNetworkWeakWarning();
			} else if (e.isServerError()) {
				showServerErrorWarning();
			} else if (e.isAppObsolete()) {
				showAppObsoleteWarning(e);
			}
		}
	}
	
}
