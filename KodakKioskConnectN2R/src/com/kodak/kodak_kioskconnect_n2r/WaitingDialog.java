package com.kodak.kodak_kioskconnect_n2r;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WaitingDialog extends ProgressDialog {
	private Context context;
	private int titleId;
	
	private RelativeLayout dialog_LinearLayout;
	private TextView dialog_title;

	public WaitingDialog(Context context, int titleId) {
		super(context);
		this.context = context;
		this.titleId = titleId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getting_products_dialog);
		dialog_LinearLayout = (RelativeLayout) findViewById(R.id.dialog_LinearLayout);
		dialog_title = (TextView) findViewById(R.id.dialog_textView);
		dialog_title.setText(context.getString(titleId));
		ViewGroup.LayoutParams dialogLp = dialog_LinearLayout.getLayoutParams();
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		dialogLp.height = display.getHeight() * 3 / 4;
		dialogLp.width = display.getWidth() * 2 / 3;
		dialog_LinearLayout.setLayoutParams(dialogLp);
	}
	
	public void resetTitle(int titleId){
		this.titleId = titleId;
		handler.sendEmptyMessage(REFRESH_TITLE);
		
	}

	private final int REFRESH_TITLE = 0;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			switch (action) {
			case REFRESH_TITLE:
				dialog_title.setText(((Activity)context).getString(titleId));
				break;
			default:
				break;
			}
		}
		
	};
}
