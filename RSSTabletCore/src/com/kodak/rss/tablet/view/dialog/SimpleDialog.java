package com.kodak.rss.tablet.view.dialog;

import com.kodak.rss.tablet.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

public abstract class SimpleDialog {
	
	private int maxHeight;
	private int maxWidth;
	
	private int width = WindowManager.LayoutParams.WRAP_CONTENT;
	private int height = WindowManager.LayoutParams.WRAP_CONTENT;
	
	private Dialog dialog;
	protected View contentView;
	
	public SimpleDialog(Context context, int resource){
		contentView = LayoutInflater.from(context).inflate(resource, null);
		dialog = new Dialog(context, R.style.SimpleDialogTheme);
		dialog.setContentView(contentView);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		maxHeight = display.getHeight();
		maxWidth = display.getWidth();
	}
	
	public void setWidth(float percent){
		if(percent >= 1){
			width = LayoutParams.MATCH_PARENT;
		}else if(percent<=0){
			width = LayoutParams.WRAP_CONTENT;
		}else{
			width = (int) (maxWidth * percent);
		}
	}
	
	/**
	 * set the dialog height
	 * if you don't set this value, the default is wrap_content(detail: see info_dialog.xml)
	 * @param percent 0-1, 0 means wrap_content, 1 means match_prent, 0.XX means XX % percent in the activity
	 * @return
	 */
	public void setHeight(float percent){
		if(percent >= 1){
			height = LayoutParams.MATCH_PARENT;
		}else if(percent<=0){
			height = LayoutParams.WRAP_CONTENT;
		}else{
			height = (int) (maxHeight * percent);
		} 
	}
	
	private void initDialog(){
		initDialogSize();
		dialog.setCancelable(isDialogCancelable());
		dialog.setCanceledOnTouchOutside(isDialogCanceledOnTouchOutside());
	}
	
	private void initDialogSize(){
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams params = dialogWindow.getAttributes();
		if(width != WindowManager.LayoutParams.WRAP_CONTENT){
			params.width = width;
		}
		if(height != WindowManager.LayoutParams.WRAP_CONTENT){
			params.height = height;
		}
		dialogWindow.setAttributes(params);
	}
	
	public abstract boolean isDialogCancelable();
	public abstract boolean isDialogCanceledOnTouchOutside();
	
	public void show(){
		if(dialog!=null && dialog.isShowing()){
			dismiss();
		}
		
		initDialog();
		
		dialog.show();
	}
	
	public void dismiss(){
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss();
		}
	}
}
