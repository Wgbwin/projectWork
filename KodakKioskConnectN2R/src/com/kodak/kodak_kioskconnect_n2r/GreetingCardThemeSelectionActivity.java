package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.ProductSelectActivity;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardProvider;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardTheme;
import com.kodak.utils.RSSLocalytics;

public class GreetingCardThemeSelectionActivity extends BaseActivity {
	private static String TAG = GreetingCardThemeSelectionActivity.class.getSimpleName();

	private GridView gvThemes;
	private TextView tvVersion;
	private Button btBack;
	private Button btNext;
	private ThemesAdapter themesAdapter;
	private HashMap<String,Bitmap> themeThumbs;
	private List<GreetingCardTheme> themes;
	private GreetingCardManager manager;
	
	//added by Robin.Qian
	private Vector<String> downloadThemes;//save the themes which are downloading or downloaded.Use vector to make sure that it is thread safe
	private boolean canDownload=true;//if false, activity will stop download themes
	private final int MAX_DOWNLOAD_SIZE = 3; // the max amount of thread  for download themes
	
	private WaitingDialog waitingDialog;
	private final String SCREEN_NAME = "GC Design";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		//setContentView(R.layout.product_greetingtheme_choicetheme_choice);
		setContentLayout(R.layout.productgreetingfield);	
		getViews();
		initData();
		setEvents();
		initProductsGridView();		
		themesAdapter = new ThemesAdapter(this);
		gvThemes.setAdapter(themesAdapter);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent myIntent = new Intent(GreetingCardThemeSelectionActivity.this, ProductSelectActivity.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
		
	class ItemOnClickListener implements OnClickListener{
		private int position;
		
		public ItemOnClickListener(int position){
			this.position = position;
		}

		@Override
		public void onClick(final View v) {
			if (!Connection.isConnected(GreetingCardThemeSelectionActivity.this))	{
				showNoConnectionDialog(v);
				return;
			}
			waitingDialog = new WaitingDialog(GreetingCardThemeSelectionActivity.this, R.string.animation_quickbook_wait);
			waitingDialog.setCancelable(false);
			waitingDialog.setCanceledOnTouchOutside(false);
			waitingDialog.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(manager.hasValidGreetingCardCategory()){
						Intent intent = new Intent(GreetingCardThemeSelectionActivity.this, GreetingCardSelectionActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						showNorespondDialog(v);
					}
					
				}
			});
			waitingDialog.setOnShowListener(new OnShowListener() {
				
				@Override
				public void onShow(DialogInterface dialog) {
					shutDownDownloader();
				}
			});
			Log.e(TAG, "name: " + themes.get(position).name);
			new Thread(new GetGreetingCardCategory(position)).start();
		}
		
	}
	
	class GetGreetingCardCategory implements Runnable{
		private int position;
		
		public GetGreetingCardCategory(int position){
			this.position = position;
		}

		@Override
		public void run() {
			handler.obtainMessage(START_GETTING_CARDS).sendToTarget();
			manager.createGreetingCardCategory(themes.get(position).language, themes.get(position).filters);
			handler.obtainMessage(FINISH_GETTING_CARDS).sendToTarget();
		}
		
	}
	
	private final int REFRESH_THUMBS = 0;
	private final int START_GETTING_CARDS = 1;
	private final int FINISH_GETTING_CARDS = 2;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case REFRESH_THUMBS:
				themesAdapter.notifyDataSetChanged();
				break;
			case START_GETTING_CARDS:
				waitingDialog.show();
				break;
			case FINISH_GETTING_CARDS:
				GreetingCardProvider.getGreetingCardProvoider(GreetingCardThemeSelectionActivity.this).initPreviewDatabase();
				// TODO: here need to do something to handle lock/unlock screen.
				waitingDialog.dismiss();
				break;
			}
		}
		
	};
	
	private void initProductsGridView(){
		Display display = getWindowManager().getDefaultDisplay();
		int columnNumber = themes.size();
		int itemWidth = 0;
		if(columnNumber>3){
			itemWidth = (int) (display.getWidth() / 3.3);
		} else {
			itemWidth = display.getWidth() / columnNumber;
		}
		LayoutParams params = new LayoutParams(itemWidth * columnNumber,LayoutParams.FILL_PARENT);
		gvThemes.setLayoutParams(params);
		gvThemes.setNumColumns(columnNumber);
		gvThemes.setColumnWidth(itemWidth);
		gvThemes.setStretchMode(GridView.NO_STRETCH);
	}
	
	@Override
	protected void onStart() {
		themesAdapter.notifyDataSetChanged();
		canDownload = true;
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		canDownload = false;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(themeThumbs!=null && themeThumbs.size()>0){
			for(Bitmap bitmap : themeThumbs.values()){
				if(bitmap != null){
					bitmap.recycle();
				}
			}
			themeThumbs.clear();
		}
		if (PrintHelper.mActivities.contains(this)) {
			PrintHelper.mActivities.remove(this);
		}
		themesAdapter = null;
		gvThemes.setAdapter(themesAdapter);
		gvThemes = null;
	}
	
	class ThemesAdapter extends BaseAdapter{
		private LayoutInflater layoutInflater;
		
		public ThemesAdapter(Context context){
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			if(themes != null){
				return themes.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = layoutInflater.inflate(R.layout.product_item, null);
			ImageView ivProduct = (ImageView) convertView.findViewById(R.id.proIV);
			WebView wvProduct = (WebView) convertView.findViewById(R.id.proWV);
			TextView tvProduct = (TextView) convertView.findViewById(R.id.proTV);
			wvProduct.setVisibility(View.GONE);
			
			GreetingCardTheme theme = themes.get(position);
			if(!themeThumbs.containsKey(theme.glyphURL) || themeThumbs.get(theme.glyphURL)==null){
				ivProduct.setImageResource(R.drawable.imagewait96x96);
				if(!checkDownload(theme.glyphURL) && downloadThemes.size()<MAX_DOWNLOAD_SIZE){
					try{
						new Thread(new ThumbDownloader(theme)).start();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			} else {
				ivProduct.setImageBitmap(themeThumbs.get(theme.glyphURL));
			}
			tvProduct.setText(theme.name);
			tvProduct.setTypeface(PrintHelper.tf);
			
			convertView.setOnClickListener(new ItemOnClickListener(position));
			
			return convertView;
		}
		
	}
	
	
	/**
	 * check if the url is been downloading
	 * @param url
	 * @return
	 */
	private boolean checkDownload(String url){
		return downloadThemes.contains(url);
	}
	
	private void shutDownDownloader(){
		canDownload = false;
	}
	
	private void showNorespondDialog(final View v){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				v.performClick();
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
	

	class ThumbDownloader implements Runnable {
		private GreetingCardTheme theme;
		
		public ThumbDownloader(GreetingCardTheme theme){
			this.theme = theme;
		}
		
		private boolean needShutDown(){
			boolean result = !canDownload;
			if(result && downloadThemes!=null){
				downloadThemes.remove(theme.glyphURL);
			}
			return result;
		}

		@Override
		public void run() {
			if( (downloadThemes!= null && downloadThemes.contains(theme.glyphURL))
					|| (themeThumbs != null && themeThumbs.get(theme.glyphURL) != null)){
				//avoid re-download themes
				return;
			}
			Log.i(TAG, "start download url[" + theme.glyphURL + "]");
			if(downloadThemes!=null){
				downloadThemes.add(theme.glyphURL);
			}
			if(needShutDown()){
				return;
			}
			int count = 0;
			byte[] imgData = null;
			while(imgData==null && count<5 && !needShutDown()){
				InputStream is = null;
				HttpURLConnection conn = null;
				count++;
				try{
					byte[] data = null;
					URL url = new URL(PrintHelper.escapeURL(theme.glyphURL));
					conn = (HttpURLConnection) url.openConnection();
					if(needShutDown()){
						return;
					}
					conn.setConnectTimeout(5 * 1000);
					conn.setReadTimeout(10 * 1000);
					is = conn.getInputStream();
					if(needShutDown()){
						return;
					}
					int length = (int) conn.getContentLength();
				
					if (length > 0) {
						data = new byte[length];
						byte[] buffer = new byte[4098];
						int readLen = 0;
						int destPos = 0;
						while ((readLen = is.read(buffer)) >= 0) {
							if(needShutDown()){
								return;
							}
							if (readLen > 0) {
								System.arraycopy(buffer, 0, data, destPos, readLen);
								destPos += readLen;
							} else {
								Log.w(TAG, "");
							}
						}
						imgData = data;
					}
				} catch(Exception e){
					e.printStackTrace();
				} finally {
					if(is!=null){
						try {
							is.close();
							is = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (conn != null) {
							conn.disconnect();
						}
					}
				}
			}
			
			if(needShutDown()){
				return;
			}
			
			try{
				if(imgData != null){
					if(themes != null && handler != null){
						Bitmap thumb = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
						themeThumbs.put(theme.glyphURL, thumb);
						handler.obtainMessage(REFRESH_THUMBS).sendToTarget();
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(downloadThemes != null){
					downloadThemes.remove(theme.glyphURL);
				}
			}
		}
	
	}


	@Override
	public void getViews() {
		gvThemes = (GridView) findViewById(R.id.products_gridView);
		tvVersion = (TextView) findViewById(R.id.versionCopyright_tex);
		btBack = (Button) findViewById(R.id.back_btn);
		btNext = (Button) findViewById(R.id.next_btn);
		
	}

	@Override
	public void initData() {
		btNext.setVisibility(View.INVISIBLE);
		tvVersion.setVisibility(View.INVISIBLE);
		btBack.setVisibility(View.VISIBLE);
		
		manager = GreetingCardManager.getGreetingCardManager(getApplicationContext());
		themes = manager.getGreetingCardThemes();
		downloadThemes = new Vector<String>();
		themeThumbs = new HashMap<String, Bitmap>();
		
	}

	@Override
	public void setEvents() {

		btBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(GreetingCardThemeSelectionActivity.this, ProductSelectActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
			}
		});
		
		
		
	
		
	}

}
