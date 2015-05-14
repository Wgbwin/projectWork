package com.kodak.rss.tablet.view;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.util.ImageResources;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.CalendarEditActivity;
import com.kodak.rss.tablet.activities.CollageEditActivity;
import com.kodak.rss.tablet.activities.GCEditActivity;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;

public class ProductEditPopColorEffectItemView extends FrameLayout{
	private ImageView ivIcon;
	private TextView tvText;
	private ImageResources imgRes;

	public ProductEditPopColorEffectItemView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context){
		inflate(context, R.layout.product_edit_pop_color_effects_item, this);
		ivIcon = (ImageView) findViewById(R.id.iv_icon);
		tvText = (TextView) findViewById(R.id.tv_text);
		
		if(context instanceof PhotoBooksProductActivity){
			imgRes = ((PhotoBooksProductActivity)context).imageResources;
		} else if(context instanceof GCEditActivity) {
			imgRes = ((GCEditActivity)context).colorEffectResources;
		} else if (context instanceof CalendarEditActivity) {
			imgRes = ((CalendarEditActivity)context).colorEffectResources;
		} else if (context instanceof CollageEditActivity) {
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
