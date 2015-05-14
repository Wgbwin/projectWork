package com.kodakalaris.kodakmomentslib.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.Color;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewUtil {

	private static final ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
	public static final String KEY = "*";
	
	private static final InputFilter emojiFilter = new InputFilter() {
		Pattern emoji = Pattern
				.compile(
						"[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
						Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {
			Matcher emojiMatcher = emoji.matcher(source);

			if (emojiMatcher.find()) {
				return "";
			}

			return null;
		}

	};
	
	public static void addFilters(TextView textView, InputFilter[] filters) {
		InputFilter[] inputFilter = textView.getFilters();
		InputFilter[] newInputFilter = null;
		if (inputFilter != null) {
			newInputFilter = Arrays.copyOf(inputFilter, inputFilter.length + filters.length);
			
			for (int i = 0; i < filters.length; i++) {
				newInputFilter[newInputFilter.length - filters.length + i] = filters[i];
			}

		} else {
			newInputFilter = filters;
		}
		textView.setFilters(newInputFilter);
	}

	public static void addEmojiFilter(TextView textView) {

		InputFilter[] inputFilter = textView.getFilters();
		InputFilter[] newInputFilter = null;
		if (inputFilter != null) {
			newInputFilter = Arrays.copyOf(inputFilter, inputFilter.length + 1);

			newInputFilter[newInputFilter.length - 1] = emojiFilter;

		} else {
			newInputFilter = new InputFilter[] { emojiFilter };
		}
		textView.setFilters(newInputFilter);

	}

	public static void highlightText(ViewGroup v) {
		for (int i = 0, j = v.getChildCount(); i < j; i++) {
			View c = v.getChildAt(i);
			if (c instanceof TextView) {
				TextView txt = (TextView) c;
				final String source = txt.getText().toString().trim();
				int index = source.indexOf(KEY);
				if (index > -1) {
					SpannableStringBuilder nameStyle = new SpannableStringBuilder(source);
					nameStyle.setSpan(redSpan, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					txt.setText(nameStyle);
				}
			} else if (c instanceof ViewGroup) {
				highlightText((ViewGroup) c);
			}
		}
	}


	public static CharSequence formatHighlightText(String text, int defaultColor, final String... params) {		
		int[][] flag = new int[params.length][2];
		for (int i = 0, j = params.length; i < j; i++) {
			String key = "%" + (i + 1) + "$s";
			String value = (params[i] == null ? "" : params[i]);
			int index = text.indexOf(key);
			flag[i][0] = index;
			flag[i][1] = value.length();
			if (index > -1) {
				text = text.replace(key, value);
			}
		}
		ForegroundColorSpan redSpan;

		SpannableString nameStyle = new SpannableString(text);
		for (int i = 0, j = flag.length; i < j; i++) {
			if (flag[i][0] > -1) {
				int start = flag[i][0];
				int end = flag[i][1] + start;
				redSpan = new ForegroundColorSpan(defaultColor);				
				nameStyle.setSpan(redSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		}

		return nameStyle;
	}

	public static CharSequence highlightText(String source, String startWith) {
		CharSequence result;
		final String key = startWith;
		int index = TextUtils.isEmpty(key) ? -1 : source.toLowerCase().indexOf(key.toLowerCase());
		if (index > -1) {
			SpannableStringBuilder nameStyle = new SpannableStringBuilder(source);
			nameStyle.setSpan(redSpan, index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			result = nameStyle;
		} else {
			result = source;
		}

		return result;
	}

	
	public static CharSequence highlightText(String str) {
		SpannableStringBuilder nameStyle = new SpannableStringBuilder(str);
		nameStyle.setSpan(redSpan, 0, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return nameStyle;
	}

}
