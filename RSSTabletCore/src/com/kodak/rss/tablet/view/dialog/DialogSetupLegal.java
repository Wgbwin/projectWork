package com.kodak.rss.tablet.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.kodak.rss.tablet.R;

public class DialogSetupLegal implements OnClickListener{
	
	private Dialog dialog;
	private View contentView;
	
	public void createDialog(Context context, int titleId, int urlId, int parentHeight){
		LayoutInflater mInflater = LayoutInflater.from(context);
		contentView = mInflater.inflate(R.layout.dialog_setup_license_and_privacy, null);
		contentView.findViewById(R.id.bt_ok).setOnClickListener(this);
		((TextView)contentView.findViewById(R.id.tv_title)).setText(context.getString(titleId));
		initWebView(context, (WebView)contentView.findViewById(R.id.wv_content), context.getString(urlId));
		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		resizeDialog(parentHeight);
		dialog.setContentView(contentView);
	}
	
	public void createDialog(Context context, int titleId, String url, int parentHeight){
		LayoutInflater mInflater = LayoutInflater.from(context);
		contentView = mInflater.inflate(R.layout.dialog_setup_license_and_privacy, null);
		contentView.findViewById(R.id.bt_ok).setOnClickListener(this);
		((TextView)contentView.findViewById(R.id.tv_title)).setText(context.getString(titleId));
		initWebView(context, (WebView)contentView.findViewById(R.id.wv_content), url);
		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		resizeDialog(parentHeight);
		dialog.setContentView(contentView);
	}
	
	private void resizeDialog(int size){
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams dialogLp = dialogWindow.getAttributes();
		dialogLp.width = (int) (size * 1.1);
		dialogLp.height = (int) (size * 0.9);
		dialogWindow.setAttributes(dialogLp);
	}
	
	private void initWebView(Context context, WebView webView, String url){
		webView.setBackgroundColor(0);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.loadUrl(url);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.bt_ok){
			dialog.dismiss();
		}
		
	}

}
