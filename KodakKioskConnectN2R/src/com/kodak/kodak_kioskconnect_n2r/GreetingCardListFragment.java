/**
 * GreetingCardListFragment.java
 * com.kodak.kodak_kioskconnect_n2r
 * Created by Sunny on Nov 13, 2013
 * Copyright (c) 2013 Kodak(China) All Rights Reserved
 */
package com.kodak.kodak_kioskconnect_n2r;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCard;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.EfficientAdapter;
import com.kodak.utils.PictureDowloadHandler;

/**
 * @author Sunny
 * 
 */
public class GreetingCardListFragment extends Fragment implements OnItemClickListener{

	private ListView vList;

	private List<GreetingCard> templateList;

	private TemplateAdapter mAdapter;

	private GreetingCardManager mManager;

	private PictureDowloadHandler mPictureDownloadHandler;
	
	IonCardItemSelectedListener mCallBack ;
	
	
	private int selectedItemPosition ;
	
	public interface IonCardItemSelectedListener{
		void onCardItemSelected(int position) ;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		
		try {
			mCallBack =(IonCardItemSelectedListener) activity ;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			 throw new ClassCastException(activity.toString()
	                    + " must implement IonCardItemSelectedListener");
		}
		
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mManager = GreetingCardManager.getGreetingCardManager(getActivity());
		mPictureDownloadHandler = new PictureDowloadHandler(getActivity());
		mPictureDownloadHandler.setLoadingImage(R.drawable.image_wait_4x6);

		Bundle bundle = getArguments();
		if (bundle != null) {
			templateList = (List<GreetingCard>) bundle
					.getSerializable("greetingcardlist");
		}
		mAdapter = new TemplateAdapter(getActivity(), null);
		mAdapter.setDataSource(templateList);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_greetingcard_list,
				container, false);
//		Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fragment_left_in) ;
//		v.setAnimation(anim);
//		anim.start();
		
		vList = (ListView) v.findViewById(R.id.card_list);

		vList.setAdapter(mAdapter);
		
		vList.setOnItemClickListener(this);

		return v;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPictureDownloadHandler.setExitTasksEarly(false);
		
		
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mPictureDownloadHandler.setExitTasksEarly(true);
	}
	
	

	class TemplateAdapter extends EfficientAdapter<GreetingCard> {

		public TemplateAdapter(Context context, List<GreetingCard> dataList) {
			super(context, dataList);

		}

		@Override
		protected int getItemLayout() {
			// TODO Auto-generated method stub
			return R.layout.greetingcard_template_list_item;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder();
			holder.vImageTemplate = (ImageView) v
					.findViewById(R.id.template_image);
			holder.vTextTemplateDescribe = (TextView) v
					.findViewById(R.id.text_describe);
			holder.vTextTemplateDescribe.setTypeface(PrintHelper.tf);
			v.setTag(holder);

		}

		@Override
		protected void bindView(View v, GreetingCard data, int position) {
			if (position == selectedItemPosition) {
				
				v.setBackgroundResource(R.drawable.highlight_simplex);
			} else {
				v.setBackgroundDrawable(null);
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			String imageUrl = data.glyphURL;
			mPictureDownloadHandler.downloadImage(imageUrl,
					holder.vImageTemplate);
			String[] productIdentifiers = data.productIdentifiers;
			if (productIdentifiers != null && productIdentifiers.length > 0) {
				String id =productIdentifiers[0];
				
//				for (String str : productIdentifiers) {
//					if (!str.contains("S2H")) {
//						id = str;
//						break;
//					}
//				}
                if(!"".equals(id)){
                	String mDescribe = mManager.getGreetingCardProductShortName(id);
    				holder.vTextTemplateDescribe.setText(mDescribe);
                }
				
			}
		}

		class ViewHolder {
			ImageView vImageTemplate;
			TextView vTextTemplateDescribe;
		}

	}





	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedItemPosition = position ;
		mAdapter.notifyDataSetChanged();
		
		mCallBack.onCardItemSelected(position);
		
	}

}
