package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.kodak.kodak_kioskconnect_n2r.R;
/**
 * left fragment in setting
 * @author sunny
 *
 */
public class WifiSettingLeftFragment extends Fragment {
	private RadioGroup vRadioGroup ;
	private RadioButton vRadioButtonLegal ;
	private RadioButton vRadioButtonAbout ;
	
	IOnSettingRadioGroupSelected mListener ;
	
	private int mEachItemHeight ;
	public interface IOnSettingRadioGroupSelected {
		
       public void onSettingRadioGroupSelected(int checkedId);
       
    }
	
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		 try {
	          mListener = (IOnSettingRadioGroupSelected) activity;
	        } catch (ClassCastException e) {
	          throw new ClassCastException(activity.toString() + " must implement IOnSettingRadioGroupSelected");
	        }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.fragment_wifi_setting_left,
				container, false);
		vRadioGroup = (RadioGroup) v.findViewById(R.id.wifi_setting_radio_group) ;
		vRadioButtonLegal = (RadioButton) v.findViewById(R.id.wifi_setting_legal_radion_button) ;
		vRadioButtonAbout = (RadioButton) v.findViewById(R.id.wifi_setting_about_radion_button) ;
		vRadioGroup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){

			@TargetApi(16)
			@Override
			public void onGlobalLayout() {
				int groupHeight = vRadioGroup.getHeight() ;
				mEachItemHeight = groupHeight/7 ;
				LinearLayout.LayoutParams mLayoutParamsItem = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						mEachItemHeight);
				
				
				vRadioButtonLegal.setLayoutParams(mLayoutParamsItem) ;
				vRadioButtonAbout.setLayoutParams(mLayoutParamsItem) ;
				
				Class<?> c =vRadioGroup.getViewTreeObserver().getClass() ;
				try {
					Method method = c.getDeclaredMethod("removeOnGlobalLayoutListener", OnGlobalLayoutListener.class) ;
					method.invoke(vRadioGroup.getViewTreeObserver(), this);
					
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
			
			
		}) ;
		
		
		vRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mListener.onSettingRadioGroupSelected(checkedId);
				
			}
		});
		
		return  v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		mListener.onSettingRadioGroupSelected(R.id.wifi_setting_legal_radion_button);
		
		
		
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
