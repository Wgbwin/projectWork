package com.kodak.kodak_kioskconnect_n2r.fragments;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.fragments.CollageThemeFragment.IOnCollageFragmentListener;
import com.kodak.kodak_kioskconnect_n2r.view.AlternateLayoutImageView;

public class AlternateLayoutFragment extends Fragment{
	private TextView vTextViewDone ;
	private GridView vGridViewAlternateLayout ;
	private IOnCollageFragmentListener mListener ;
	private AlternateLayoutAdapter mAdapter ;
	private List<AlternateLayout> mAlternateLayouts ;
	private int mAlternateLayoutWidth ;
	private RelativeLayout.LayoutParams mImageViewLayoutParams;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mListener = (IOnCollageFragmentListener) activity;
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData() ;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		final View v = inflater.inflate(R.layout.fragment_collage_theme, container, false);
		vTextViewDone = (TextView) v.findViewById(R.id.text_done) ;
		vGridViewAlternateLayout = (GridView) v.findViewById(R.id.gridview_theme);
		vGridViewAlternateLayout.setAdapter(mAdapter) ;
		setEvents();
		return v ;
	}
	
	


	private void initData() {
		mAlternateLayouts = CollageManager.getInstance().getCurrentCollage().page.alternateLayouts ;
		if(mAlternateLayouts!=null && mAlternateLayouts.size() >0 ){
			boolean hasSelectedAlternateLayout = false ;
			for (AlternateLayout alternateLayout : mAlternateLayouts) {
				if(alternateLayout.isSelected()){
					hasSelectedAlternateLayout = true ;
					break ;
				}
			}
			
			if(!hasSelectedAlternateLayout){
				mAlternateLayouts.get(0).setSelected(true) ;
			}
			
			
		}
		
		mAlternateLayoutWidth = getResources().getDimensionPixelSize(R.dimen.collage_theme_image_width);
		int mAlternateLayoutHeight = (int) (mAlternateLayoutWidth*CollageManager.getInstance().getCurrentCollage().page.height 
				/ CollageManager.getInstance().getCurrentCollage().page.width) ;
		mImageViewLayoutParams = new RelativeLayout.LayoutParams(mAlternateLayoutWidth,mAlternateLayoutHeight) ;
		mAdapter = new AlternateLayoutAdapter(getActivity(),mImageViewLayoutParams, mAlternateLayouts) ;
	}
	
	private void setEvents() {
		vTextViewDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.doneOnClick() ;
				
			}
		}) ;
		
		
		vGridViewAlternateLayout.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position < mAdapter.getNumColumns()) {
					return;
				}
				
				AlternateLayout alternateLayout = mAlternateLayouts.get(position - mAdapter.getNumColumns()) ;
				if(alternateLayout.isSelected()){
					return  ;
				}
				
				
				for (AlternateLayout alternateLayoutInCollage : mAlternateLayouts) {
					if(alternateLayoutInCollage.layoutId.equals(alternateLayout.layoutId)){
						alternateLayoutInCollage.setSelected(true) ;
					}else {
						alternateLayoutInCollage.setSelected(false) ;
					}
				}
				mAdapter.notifyDataSetChanged() ;
				mListener.selectPageLayout(alternateLayout) ;
				
			}
			
			
			
		}) ;
		
		
	}
	
	class AlternateLayoutAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		private List<AlternateLayout> mDataList;
		private final Context mContext;
		private int mNumColumns = 2;
		private int mTopViewHeight = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		public AlternateLayoutAdapter(Context context, RelativeLayout.LayoutParams mImageViewLayoutParams ,List<AlternateLayout> dataList) {
			mDataList = dataList;
			mContext = context;
			this.mImageViewLayoutParams = mImageViewLayoutParams ;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTopViewHeight = getResources().getDimensionPixelSize(R.dimen.top_view_height);
		}
		
		public int getNumColumns() {
			// TODO Auto-generated method stub
			return mNumColumns;
		}
		@Override
		public int getCount() {
			if (mNumColumns == 0) {
				return 0;
			}
            if(mDataList!=null){
            	return mDataList.size() + mNumColumns;
            }else {
            	return 0 ;
            }
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return  position < mNumColumns ? null : mDataList.get(position - mNumColumns);
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position < mNumColumns ? 0 : position - mNumColumns;
		}
		
		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return (position < mNumColumns) ? 1 : 0;
		}
		
		@Override
		public boolean hasStableIds() {
			return true;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position < mNumColumns) {
				if (convertView == null) {
					convertView = new View(mContext);
				}
				// Set empty view with height
				convertView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, mTopViewHeight));
				return convertView;
			}
			
			AlternateLayout data = (AlternateLayout) getItem(position);
			final ViewHolder holder;
			if (convertView == null) { 
				convertView = mInflater.inflate(R.layout.collage_alternate_layout_grid_item, parent, false);
				holder = new ViewHolder();
				holder.vImageViewAlternateLayout = (AlternateLayoutImageView) convertView.findViewById(R.id.image_alternate_layout) ;
				holder.vImageViewAlternateLayout.setLayoutParams(mImageViewLayoutParams) ;
				holder.vImageViewAlternateLayout.setScaleType(ImageView.ScaleType.FIT_XY);
				
				convertView.setTag(holder);
				
			}else {
				holder = (ViewHolder) convertView.getTag();
				holder.vImageViewAlternateLayout = (AlternateLayoutImageView) convertView ;
			}
			
			holder.vImageViewAlternateLayout.setmAlternateLayout(data,mImageViewLayoutParams.width ,mImageViewLayoutParams.height) ;
			holder.vImageViewAlternateLayout.setChecked(data.isSelected()) ;
			return convertView ;
		}
		
		
		private class ViewHolder {
			private AlternateLayoutImageView vImageViewAlternateLayout;
			
		}
		
	}
	
	
	
}
