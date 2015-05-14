package com.kodak.rss.tablet.view.collage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.CollageEditListAdapter;
import com.kodak.rss.tablet.adapter.CollageEditListAdapter.OnSelectListener;
import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.util.CollageUtil;

public class CollageEditItemView extends LinearLayout{  
	private Button editBtn;
	private TextView editTxt;	
	public String category;
	public double price;
	public int num;	
	float scale = 1.0f;
	BitmapDrawable bd;
	CollageEditListAdapter editListAdapter;
	int position;	
	OnSelectListener onSelectListener;
	
	public CollageEditItemView(Context context,CollageEditListAdapter editListAdapter,OnSelectListener onSelectListener) {
		super(context);		
		inflate(context,R.layout.edit_item, this);	
		this.editListAdapter = editListAdapter;	
		this.onSelectListener = onSelectListener;	
		
		editBtn = (Button) findViewById(R.id.btn_edit);
		editTxt = (TextView) findViewById(R.id.txt_edit);
	}		

	public void initViewAndAction(int pos){
		this.position = pos;	
		PrintEditInfo editInfo = editListAdapter.editList.get(pos);
		if (editInfo == null) return;
		editInfo.setEnabled(true);
		String editName = editInfo.getName();
		if (editName != null && editName.equals("shuffle")) {
			Collage currentCollage = CollageUtil.getCurrentCollage();
			if (currentCollage != null && currentCollage.chosenpics != null && currentCollage.chosenpics.size() < 1) {
				editInfo.setEnabled(false);
			}
		}
		
		editBtn.setBackgroundResource(editInfo.getBtn_id());		
		editBtn.setEnabled(editInfo.isEnabled());			
		editBtn.setOnClickListener(editClick);
		editTxt.setText(editInfo.getTxt_id());		
		editTxt.setEnabled(editInfo.isEnabled());
		if (editInfo.isEnabled()) {			
			editTxt.setTextColor(Color.WHITE);
		}else {
			editTxt.setTextColor(Color.GRAY);
		}			
		editTxt.setOnClickListener(editClick);		
	}
		
	private OnClickListener editClick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if (onSelectListener != null) {
				PrintEditInfo editInfo = editListAdapter.editList.get(position);
				onSelectListener.onSelect(editInfo);
			}			
		}
	};

}
