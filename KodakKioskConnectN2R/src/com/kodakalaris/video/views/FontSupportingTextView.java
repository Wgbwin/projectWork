package com.kodakalaris.video.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodakalaris.video.activities.BaseActivity;
/*
 * This textview supports setting a font by using an
 * xml attribute
 */
public class FontSupportingTextView extends TextView {

	private static final int OSWALD_REGULAR = 0;
	private static final int OSWALD_LIGHT = 1;
	private static final int OSWALD_BOLD = 2;
	private static final int OSWALD_STENCIL = 3;

	public FontSupportingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (BaseActivity.USE_CUSTOM_FONTS) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SquareImageView);
			int fontNumber = a.getInt(R.styleable.FontSupportingTextView_font, OSWALD_REGULAR);
			a.recycle();
			String font;
			switch (fontNumber) {
				case OSWALD_LIGHT :
					font = "Oswald-Light.otf";
				case OSWALD_BOLD :
					font = "Oswald-Bold.otf";
				case OSWALD_STENCIL :
					font = "Oswald-Stencil.otf";
				case OSWALD_REGULAR :
				default :
					font = "Oswald-Regular.otf";
			}
			this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/" + font));
		}
	}
}
