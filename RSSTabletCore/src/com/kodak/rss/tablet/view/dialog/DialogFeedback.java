package com.kodak.rss.tablet.view.dialog;

import com.kodak.rss.tablet.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class DialogFeedback implements OnClickListener{
	
	private Dialog dialog;
	private View contentView;
	private DialogDismissListener listener;
	
	public void createDialog(Context context, int parentHeight, DialogDismissListener listener){
		LayoutInflater mInflater = LayoutInflater.from(context);
		contentView = mInflater.inflate(R.layout.dialog_rate, null);
		contentView.findViewById(R.id.bt_later).setOnClickListener(this);
		contentView.findViewById(R.id.bt_send).setOnClickListener(this);
		this.listener = listener;
		
		dialog = new Dialog(context, R.style.FeedbackDialogTheme);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		resizeDialog(parentHeight);
		
		dialog.setContentView(contentView);
	}
	
	private void resizeDialog(int size){
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams dialogLp = dialogWindow.getAttributes();
		dialogLp.width = (int) (size * 1.1);
		dialogLp.height = size;
		dialogWindow.setAttributes(dialogLp);
	}
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.bt_later){
			dialog.dismiss();
			listener.onDismiss();
		} else if(v.getId() == R.id.bt_send){
			// TODO: send the rating info
			dialog.dismiss();
			listener.onDismiss();
		}
	}
	
	public interface DialogDismissListener{
		void onDismiss();
	}

}
