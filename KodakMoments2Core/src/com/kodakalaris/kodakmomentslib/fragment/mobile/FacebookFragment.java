package com.kodakalaris.kodakmomentslib.fragment.mobile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;

public class FacebookFragment extends Fragment{
	private TextView textView;
	private String name;
	
	public static FacebookFragment newInstance(Bundle b) {
		FacebookFragment f = new FacebookFragment();

		f.setArguments(b);
		return f;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}

	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_image_selection_facebook, container, false);
	
		textView = (TextView) v.findViewById(R.id.text_toast);
		textView.setText(name+"\n"+"To Be Continue");
		return v;
	}
	
	private void initData() {
		Bundle bundle = getArguments();
	     name= bundle.getString("string");
		
		
	}

}
