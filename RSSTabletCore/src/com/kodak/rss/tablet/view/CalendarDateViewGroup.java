package com.kodak.rss.tablet.view;

import java.util.List;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.thread.calendar.CalendarSetDateTaskGroup;
import com.kodak.rss.tablet.view.CalendarDateTextView.DateFont;

public class CalendarDateViewGroup extends LinearLayout {
	
	private LayoutInflater mLayoutInflater;
	private String lastTag = "";
	private List<DateFont> dateFonts;
	
	private CalendarDateTextView mContainer;
	private CalendarSetDateTaskGroup dateTaskGroup;

	public CalendarDateViewGroup(Context context) {
		super(context);
		init(context);
	}
	
	public CalendarDateViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CalendarDateViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context){
		mLayoutInflater = LayoutInflater.from(context);
		setOrientation(LinearLayout.VERTICAL);
	}
	
	public void refreshDates(CalendarDateTextView container, CalendarSetDateTaskGroup dateTaskGroup, List<DateFont> dateFonts){
		removeAllViews();
		this.mContainer = container;
		this.dateFonts = dateFonts;
		this.dateTaskGroup = dateTaskGroup;
		for(int i=0; i<dateFonts.size(); i++){
			DateFont dateFont = dateFonts.get(i);
			View v = mLayoutInflater.inflate(R.layout.calendar_date_text_item, null);
			TextView tvDay = (TextView) v.findViewById(R.id.tv_day);
			EditText etDay = (EditText) v.findViewById(R.id.et_day);
			TextUtil.addEmojiFilter(etDay);
			etDay.setTag(dateFont.getMonthAndDayString());
			
			tvDay.setText(dateFont.date.day + "");
			etDay.setText(dateFont.textBlock.text);
			
			String tag = dateFont.getMonthAndDayString();
			etDay.setOnFocusChangeListener(new OnDateFocusChangeListener(tag));
			etDay.addTextChangedListener(new DateTextWatcher(tag));
			if(i == 0){
				etDay.requestFocus();
			}
			addView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}
	
	private class OnDateFocusChangeListener implements OnFocusChangeListener {
		
		private String tag;
		
		public OnDateFocusChangeListener(String tag) {
			this.tag = tag;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if(hasFocus){
				DateFont dateFont = getDateFontByTag((String) v.getTag());
				if(tag.equals(lastTag)){
					return;
				} else {
					lastTag = dateFont.getMonthAndDayString();
					mContainer.setCurrentDateFont(dateFont);
					mContainer.updateFontValues(dateFont.textBlock);
				}
			}
		}
	}
	
	private DateFont getDateFontByTag(String tag){
		for(DateFont dateFont : dateFonts){
			if(tag.equals(dateFont.getMonthAndDayString())){
				return dateFont;
			}
		}
		return null;
	}
	
	private class DateTextWatcher implements TextWatcher {
		
		private String tag;
		
		public DateTextWatcher(String tag){
			this.tag = tag;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String currentText = s==null ? "" : s.toString().trim();
			DateFont dateFont = getDateFontByTag(tag);
			dateFont.textBlock.text = currentText;
			dateFont.textBlock.smallTextSize = true;
			dateTaskGroup.addTask(dateFont);
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}
		
	}

}
