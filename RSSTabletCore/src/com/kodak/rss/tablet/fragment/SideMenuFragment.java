package com.kodak.rss.tablet.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.adapter.SideMenuAdapter;
import com.kodak.rss.tablet.bean.SideMenuItem;

public class SideMenuFragment extends Fragment{
	private static final String TAG = "SideMenuFragment";
	
	private ListView lvMenu;
	private ImageButton imgBtnHandler;
	private TextView tvVersion;
	
	private SideMenuAdapter adapter;
	private String strLogin, strLogout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "side menu onCreateView");
		View v = inflater.inflate(R.layout.side_menu, null);
		lvMenu = (ListView) v.findViewById(R.id.lv_menu);
		imgBtnHandler = (ImageButton) v.findViewById(R.id.imgBtn_handler);
		tvVersion = (TextView) v.findViewById(R.id.tv_version);
		
		strLogin = getString(R.string.side_menu_login);
		strLogout = getString(R.string.side_menu_logout);
		
		initViews();
		return v;
	}
	
	private void initViews(){
		initListView();
		try {
			PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			//tvVersion.setText(getString(R.string.Version_String) + packageInfo.versionName + " " + getString(R.string.Copyright_String));
			tvVersion.setText(getString(R.string.Version_String) + packageInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		imgBtnHandler.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Activity activity = getActivity();
				if(activity instanceof BaseActivity){
					((BaseActivity)activity).closeSideMenu();
				}
			}
		});
	}
	
	private void initListView(){
		List<SideMenuItem> list = new ArrayList<SideMenuItem>();
		list.add(new SideMenuItem(SideMenuItem.ITEM_HOME, R.drawable.menu_home, getString(R.string.side_menu_home)));
		list.add(new SideMenuItem(SideMenuItem.ITEM_SETTINGS, R.drawable.menu_setting, getString(R.string.side_menu_settings)));
		list.add(new SideMenuItem(SideMenuItem.ITEM_INFO, R.drawable.menu_info, getString(R.string.side_menu_info)));
		//TBD tips
//		list.add(new SideMenuItem(SideMenuItem.ITEM_TIPS, R.drawable.menu_tips, getString(R.string.side_menu_tips)));
		list.add(new SideMenuItem(SideMenuItem.ITEM_FACEBOOK, R.drawable.facebook, isFacebookLogin()? strLogout : strLogin));
		
		adapter = new SideMenuAdapter(getActivity(), list);
		lvMenu.setAdapter(adapter);
	}
	
	public boolean isFacebookLogin(){
		return RssTabletApp.getInstance().isFacebookLogin();
	}
	
	public void setOnItemClickListner(OnItemClickListener onItemClickListener){
		lvMenu.setOnItemClickListener(onItemClickListener);
	}
	
	public SideMenuAdapter getSideMenuAdapter(){
		return adapter;
	}
	
	public void removeItem(int itemId){
		adapter.removeItem(itemId);
	}
	
	public void notifyLoginStatusChanged(){
		for(SideMenuItem item : adapter.getList()){
			switch(item.getId()){
			case SideMenuItem.ITEM_FACEBOOK:
				item.setText(isFacebookLogin()? strLogout : strLogin);
				break;
			}
		}
		adapter.notifyDataSetChanged();
	}
}
