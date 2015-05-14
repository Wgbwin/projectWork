/**
 * GreetingCardActionFragment.java
 * com.kodak.kodak_kioskconnect_n2r
 * Created by Sunny on Nov 13, 2013
 * Copyright (c) 2013 Kodak(China) All Rights Reserved
 */
package com.kodak.kodak_kioskconnect_n2r;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.bean.DeleveryPrompt;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCard;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.EfficientAdapter;

/**
 * @author Sunny
 *
 */
public class GreetingCardActionFragment extends Fragment{
	
	private Button vCancelBtn ;
	
	private Button vMakingThisBtn ;
	
	private Button vDeliverySelectBtn ;
	
	/**
	 * hold the listview
	 */
	private LinearLayout vLayoutDeliveryContainer ;
	
	private ListView vDeliverList ;
	
	private WebView vDetailInfoWebview ;
	
	private GreetingCard mGreetingCard ;
	
	private boolean hasSelectedDelivery = false ;
	
	private String selectedIdentifier ;
    
	private List<DeleveryPrompt> deleveryPromptList ;
	
	private GreetingCardManager mManager ;
	private String ENCODING = "utf-8";
	
	private DeliveryAdapter mAdapter ;
	
	
	IActionListener mCallBack ;
	public interface IActionListener{
		void doCancel() ;
		
		void doMakeThis(GreetingCard greetingCard ,String productIdentifier);
	}
	
	
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try {
			mCallBack =(IActionListener) activity ;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			 throw new ClassCastException(activity.toString()
	                    + " must implement IActionListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if(bundle!=null){
			mGreetingCard = (GreetingCard) bundle.getSerializable("greetingcard");
			
		}
		mManager = GreetingCardManager.getGreetingCardManager(getActivity()) ;
		
		
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v  = inflater.inflate(R.layout.fragment_greetingcard_action, container, false) ;
		vCancelBtn = (Button) v.findViewById(R.id.cancel_btn) ;
		vMakingThisBtn = (Button) v.findViewById(R.id.make_this_btn) ;
		vDeliverySelectBtn = (Button) v.findViewById(R.id.delivery_button) ;
		vLayoutDeliveryContainer = (LinearLayout) v.findViewById(R.id.delevery_container) ;
		vDeliverList = (ListView) v.findViewById(R.id.delevery_list) ;
		vDetailInfoWebview = (WebView) v.findViewById(R.id.detailinfo_webview) ;
		
		vCancelBtn.setTypeface(PrintHelper.tf);
		vMakingThisBtn.setTypeface(PrintHelper.tf) ;
		vDeliverySelectBtn.setTypeface(PrintHelper.tf) ;
		init() ;
		setListeners() ;
		
		
		return v ;
	}
	
	
	
	
	
	private void init() {
		selectedIdentifier ="" ;
		if(whetherNeedToSelectDelivery(mGreetingCard)){
			
			//show the delivery button
			vDeliverySelectBtn.setVisibility(View.VISIBLE) ;
			if(mAdapter==null){
				mAdapter = new DeliveryAdapter(getActivity(), deleveryPromptList);
				vDeliverList.setAdapter(mAdapter) ;
			}else{
				mAdapter.setDataSource(deleveryPromptList);
			}
			
			
			/*if the delivery way just only has one,
			 * that means, deleveryPromptList.size()==1,
			 * we should select the delivery auto
			 * for RSS-MOBILE ISSUE-943
			 */
			if(deleveryPromptList.size()==1){
				DeleveryPrompt deleveryPrompt = deleveryPromptList.get(0);
				if(deleveryPrompt!=null){
					deliverySelect(deleveryPrompt) ;
				}
				vDeliverySelectBtn.setVisibility(View.INVISIBLE) ;
			}else{
				vDeliverySelectBtn.setText(R.string.select_delivery);
				vDeliverySelectBtn.setVisibility(View.VISIBLE) ;
				vDetailInfoWebview.setVisibility(View.GONE) ;
				vDetailInfoWebview.clearCache(true) ;
				vLayoutDeliveryContainer.setVisibility(View.GONE) ;
				hasSelectedDelivery = false ;
			}
			
		}else {
			vDeliverySelectBtn.setVisibility(View.INVISIBLE) ;
			vDeliverySelectBtn.setText(R.string.select_delivery);
			vDetailInfoWebview.setVisibility(View.VISIBLE) ;
			clearWebViewBackground(vDetailInfoWebview);
			vDetailInfoWebview.clearCache(true) ;
			vLayoutDeliveryContainer.setVisibility(View.GONE) ;
			hasSelectedDelivery = true ;
			if(mGreetingCard.productIdentifiers!=null && mGreetingCard.productIdentifiers.length>0){
				selectedIdentifier = mGreetingCard.productIdentifiers[0] ;
			}else {
				selectedIdentifier ="" ;
			}
		
			String detailInfo = mManager.getGreetingCardMaketing(selectedIdentifier,mGreetingCard);
			vDetailInfoWebview.loadDataWithBaseURL(null, detailInfo, "text/html", ENCODING, null) ;
			
		}
		
	}
	
	private void setListeners() {
		vCancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCallBack.doCancel() ;
				
			}
		}) ;
		
		vMakingThisBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!hasSelectedDelivery ){
					showNotSelectDeliveryDialog() ;
				}else {
					//notify the activity to do make...
					mCallBack.doMakeThis(mGreetingCard, selectedIdentifier) ;
				}
				
			}
		}) ;
		
		vDeliverySelectBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			  if(vLayoutDeliveryContainer.getVisibility()==View.VISIBLE){
				  vLayoutDeliveryContainer.setVisibility(View.GONE);
			  }else if(vLayoutDeliveryContainer.getVisibility()==View.GONE){
				  vLayoutDeliveryContainer.setVisibility(View.VISIBLE);
			
			  }
			   
				
			}
		});
		
		vDeliverList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				
				DeleveryPrompt deleveryPrompt = deleveryPromptList.get(position);
				if(deleveryPrompt!=null){
					deliverySelect(deleveryPrompt) ;
				}
				
				
			}
			
		});
		
		
		
	}
	
	/**
	 * select the delivery way ,the item click event
	 */
	private void deliverySelect(DeleveryPrompt deleveryPrompt){
		String htmlDetail = "" ;
		selectedIdentifier = deleveryPrompt.getProductIdentifier() ;
		htmlDetail = mManager.getGreetingCardMaketing(selectedIdentifier, mGreetingCard) ;
		vDeliverySelectBtn.setText(deleveryPrompt.getDeleveryPrompt());
		hasSelectedDelivery = true ;
		vLayoutDeliveryContainer.setVisibility(View.GONE);
		if(!TextUtils.isEmpty(htmlDetail)){
			clearWebViewBackground(vDetailInfoWebview);
			vDetailInfoWebview.setVisibility(View.VISIBLE) ;
			vDetailInfoWebview.clearCache(true) ;
			vDetailInfoWebview.loadDataWithBaseURL(null, htmlDetail, "text/html", ENCODING, null) ;
			
		}
	
	}

	
	/**
	 * judge that whether we need to show the select delivery button
	 * @return 
	 */
	private boolean whetherNeedToSelectDelivery(GreetingCard card){
		boolean flag = false ;
		
		if(card!=null){
			String[] ids = mGreetingCard.productIdentifiers ;
			if(ids!=null && ids.length>0){
				 deleveryPromptList  = mManager.getGreetingCardProductDeliveryPromptList(card);
				 
				if(deleveryPromptList!=null &&deleveryPromptList.size()>0){
					flag = true ;
				}
			}
		}
		
		return flag ;
	}
	
	
	
	public void updateViews(GreetingCard card){
		mGreetingCard = card ;
		init();
	}
	
	
	@SuppressLint("NewApi")
	private void clearWebViewBackground(WebView wv){
		wv.setBackgroundColor(0);
		int  version = Build.VERSION.SDK_INT;
		if(version >= 11){
			@SuppressWarnings("rawtypes")
			Class webView = wv.getClass();
			try {
				@SuppressWarnings("unchecked")
				Method closeLayerType = webView.getMethod("setLayerType", new Class[]{int.class, android.graphics.Paint.class});
				try {
					closeLayerType.invoke(wv,new Object[]{0x1, null});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	private void showNotSelectDeliveryDialog(){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(getActivity());
		builder.setTitle("").setMessage(R.string.not_select_a_delivery_option);
		
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		vDetailInfoWebview.clearCache(true) ;
	}
	
	
	class DeliveryAdapter extends EfficientAdapter<DeleveryPrompt>{

		public DeliveryAdapter(Context context, List<DeleveryPrompt> dataList) {
			super(context, dataList);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected int getItemLayout() {
			// TODO Auto-generated method stub
			return R.layout.simple_list_item_for_text;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder() ;
			holder.vText = (TextView) v.findViewById(R.id.textview01) ;
			holder.vText.setTypeface(PrintHelper.tf);
		    v.setTag(holder) ; 
			
		}

		@Override
		protected void bindView(View v, DeleveryPrompt data, int position) {
			if(data == null){
				return ;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			holder.vText.setText(data.getDeleveryPrompt()) ;
			
		}
		
		
		class ViewHolder{
			TextView vText ;
		}
		
	}
	
	

}
