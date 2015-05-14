package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.utils.ImageResources;

public class PhotoBookEditPopColorEffectItemView extends FrameLayout{
	private ImageView ivIcon;
	private TextView tvText;
	private ImageResources imgRes;

	public PhotoBookEditPopColorEffectItemView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		inflate(context, R.layout.photobook_edit_pop_color_effects_item, this);
		ivIcon = (ImageView) findViewById(R.id.iv_icon);
		tvText = (TextView) findViewById(R.id.tv_text);
		
		 if(context instanceof CollageEditActivity) {
			imgRes = ((CollageEditActivity)context).colorEffectResources;
		}
	}
	
	public void setInfo(ColorEffect colorEffect){
		tvText.setText(colorEffect.name);
		
		if(imgRes!=null){
			ivIcon.setImageBitmap(imgRes.getCacheBitmap(colorEffect.name));
		}
	}
	
}
