package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aphidmobile.utils.TextureUtils;
import com.google.android.maps.OverlayItem;
import com.kodak.kodak_kioskconnect_n2r.R;

public class BubbleView<Item extends OverlayItem> extends FrameLayout {
	private LinearLayout layout;
	private TextView vTitleText;
	private TextView vSnippetText;
	
	
	
	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BubbleView(Context context, int bubbleBottomOffset) {

		super(context);

		setPadding(10, 0, 10, bubbleBottomOffset);
		
		layout = new LimitLinearLayout(context);
		layout.setVisibility(VISIBLE);

		setupView(context, layout);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}
	
	/**
	 * Inflate and initialize the BubbleView UI. Override this method
	 * to provide a custom view/layout for the balloon. 
	 * 
	 * @param context - The activity context.
	 * @param parent - The root layout into which you must inflate your view.
	 */
	protected void setupView(Context context, final ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.bubble_content_layout, parent);
		vTitleText = (TextView) v.findViewById(R.id.text_title);
		vSnippetText = (TextView) v.findViewById(R.id.text_snippet) ;
	}
	
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data. 
	 */
	public void setData(Item item) {
		layout.setVisibility(VISIBLE);
		setBubbleData(item, layout);
	}
	
	
	/**
	 * Sets the view data from a given overlay item. Override this method to create
	 * your own data/view mappings.
	 * 
	 * @param item - The overlay item containing the relevant view data.
	 * @param parent - The parent layout for this BalloonOverlayView.
	 */
	protected void setBubbleData(Item item, ViewGroup parent) {
		String title  = item.getTitle() ;
		String snippet = item.getSnippet() ;
		if(!TextUtils.isEmpty(title)){
			vTitleText.setVisibility(VISIBLE);
			vTitleText.setText(title);
		}else {
			vTitleText.setVisibility(View.GONE) ;
			vTitleText.setText("") ;
		}
		
		if(!TextUtils.isEmpty(snippet)){
			vSnippetText.setVisibility(VISIBLE);
			vSnippetText.setText(snippet);
		}else {
			vSnippetText.setVisibility(View.GONE);
			vSnippetText.setText("");
		}
		
	}
	
	
	private class LimitLinearLayout extends LinearLayout {

	    private static final int MAX_WIDTH_DP = 280;
	    
	    final float SCALE = getContext().getResources().getDisplayMetrics().density;

	    public LimitLinearLayout(Context context) {
	        super(context);
	    }

	    public LimitLinearLayout(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	        int mode = MeasureSpec.getMode(widthMeasureSpec);
	        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
	        int adjustedMaxWidth = (int)(MAX_WIDTH_DP * SCALE + 0.5f);
	        int adjustedWidth = Math.min(measuredWidth, adjustedMaxWidth);
	        int adjustedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(adjustedWidth, mode);
	        super.onMeasure(adjustedWidthMeasureSpec, heightMeasureSpec);
	    }
	}
}
