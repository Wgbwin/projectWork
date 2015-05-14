package com.kodak.rss.tablet.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.R;

public class DialogEulaAndPrivate implements OnClickListener,OnKeyListener{
	private View dialogview;
	private Dialog dialog;
	private Context context;
	
	private onDialogEulaListener listener;
	// define the onClick listener for two buttons 
	public interface onDialogEulaListener{
		void onYes();
		void onNo();
	}

	public void craeteDialog(Context context,String url,String title, String label, String yes, String no,onDialogEulaListener listener) {
		this.listener = listener;
		this.context = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		dialogview = inflater.inflate(R.layout.dialog_eula, null);
		
		dialogview.findViewById(R.id.yes).setOnClickListener(this);
		dialogview.findViewById(R.id.no).setOnClickListener(this);
		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setOnKeyListener(this);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		dialog.getWindow().setContentView(dialogview);
		((TextView)dialogview.findViewById(R.id.title)).setText(title);
		if(label!=null){
			((TextView)dialogview.findViewById(R.id.label)).setText(label);
		}else{
			dialogview.findViewById(R.id.label).setVisibility(View.INVISIBLE);
		}
		if(yes!=null){
			((TextView)dialogview.findViewById(R.id.yes)).setText(yes);
		}else{
			dialogview.findViewById(R.id.yes).setVisibility(View.INVISIBLE);
		}
		if(no!=null){
			((TextView)dialogview.findViewById(R.id.no)).setText(no);
		}else{
			dialogview.findViewById(R.id.no).setVisibility(View.INVISIBLE);
		}
		
		WebView content = (WebView)dialogview.findViewById(R.id.license_content);
		content.setBackgroundColor(0);
		WebSettings webSettings = content.getSettings();
		webSettings.setJavaScriptEnabled(true);
		content.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
		});
		content.loadUrl(url);
		
		ViewGroup.LayoutParams dialogLp = dialogview.getLayoutParams();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		dialogLp.height = dm.heightPixels*5/6;
		dialogLp.width = dm.widthPixels*2/3;
		dialogview.setLayoutParams(dialogLp);
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.yes){
			if (dialog != null) {
				dialog.dismiss();
				if(listener!=null){
					listener.onYes();
				}
			}
		}else if(v.getId()==R.id.no){
			if (dialog != null) {
				dialog.dismiss();			
			}
			if(!((Activity)context).isFinishing()){
				((Activity)context).finish();
			}
		}
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !SharedPreferrenceUtil.getBoolean(context, SharedPreferrenceUtil.SBELUA_ACCEPTED)) {
			if (dialog != null) {
				dialog.dismiss();
			}	
			if(!((Activity)context).isFinishing()){
				((Activity)context).finish();
			}
			return true;
		}		
		return false;
	}
	
	public boolean isShowing() {
		return dialog != null && dialog.isShowing();
	}
}