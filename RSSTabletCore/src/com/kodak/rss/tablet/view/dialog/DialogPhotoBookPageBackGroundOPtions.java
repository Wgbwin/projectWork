package com.kodak.rss.tablet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kodak.rss.tablet.R;

public class DialogPhotoBookPageBackGroundOPtions extends Dialog{
	public static final int PHOTOBOOK_COPY = 1;
	public static final int PHOTOBOOK_EXTEND = 2;
	public static final int PHOTOBOOK_REMOVE = 3;
	public static final int PHOTOBOOK_CANCEL = 4;
	
	private Button btnCopy;
	private Button btnExtend;
	private Button btnRemove;
	private Button btnCancel;
	
	private ViewGroup vgCopy;
	private ViewGroup vgExtend;
	
	private OnClickListener onClickListener;

	public DialogPhotoBookPageBackGroundOPtions(Context context,boolean isDuplex) {
		super(context,R.style.SimpleDialogTheme);
		init(context,isDuplex);
	}
	
	private void init(Context context,boolean isDuplex){
		setContentView(R.layout.dialog_photobook_page_background_options);
		
		btnCopy = (Button) findViewById(R.id.btn_copy);
		btnExtend = (Button) findViewById(R.id.btn_extend);
		btnRemove = (Button) findViewById(R.id.btn_remove);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		vgCopy = (ViewGroup) findViewById(R.id.layout_copy);
		vgExtend = (ViewGroup) findViewById(R.id.layout_extend);
		
		btnCopy.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onClickListener != null){
					onClickListener.onClick(DialogPhotoBookPageBackGroundOPtions.this, PHOTOBOOK_COPY);
				}
			}
		});
		
		btnExtend.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onClickListener != null){
					onClickListener.onClick(DialogPhotoBookPageBackGroundOPtions.this, PHOTOBOOK_EXTEND);
				}
			}
		});
		
		btnRemove.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onClickListener != null){
					onClickListener.onClick(DialogPhotoBookPageBackGroundOPtions.this, PHOTOBOOK_REMOVE);
				}
			}
		});
		
		btnCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(onClickListener != null){
					onClickListener.onClick(DialogPhotoBookPageBackGroundOPtions.this, PHOTOBOOK_CANCEL);
				}
			}
		});
		
		if(!isDuplex){
			vgCopy.setVisibility(View.GONE);
			vgExtend.setVisibility(View.GONE);
		}
	}
	
	public void setOnBtnClickListener(OnClickListener onClickListener){
		this.onClickListener = onClickListener;
	}
	
}
