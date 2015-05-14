package com.kodak.rss.tablet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.PopEditAdapter;

public class ColorEffectItemView extends LinearLayout{
	private ImageView ColorEditView;
	private TextView colorNameText;
	private View colorEffectLayout;
	
	PopEditAdapter popEditAdapter;
	
	public ColorEffectItemView(Context context,PopEditAdapter popEditAdapter,int position ) {
		super(context);	
		this.popEditAdapter = popEditAdapter;		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.color_effects_item, this);
		initViewAndAction();				
	}		

	private  void initViewAndAction(){
		colorEffectLayout = findViewById(R.id.colorEffectLayout);
		ColorEditView = (ImageView) findViewById(R.id.color_edit);
		colorNameText = (TextView) findViewById(R.id.color_name);	
	}
	
	public void setControlValue(ColorEffect ce){		
		if(ce.isChecked){
			setBackground();				
		}else {
			replyBackgroud();				
		}		
		colorNameText.setText(ce.name);			
		Bitmap  bitmap =  popEditAdapter.mRes.getCacheBitmap(ce.name);
		if (bitmap != null) {
			ColorEditView.setImageBitmap(bitmap);
		}		
	}		
	
	private void setBackground(){
		colorEffectLayout.setBackgroundResource(R.drawable.yellow_frame);
	}
	
	private void replyBackgroud(){
		colorEffectLayout.setBackgroundDrawable(null);
	}
	
}
