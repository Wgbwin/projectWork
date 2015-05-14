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

import com.AppConstants.LoadImageType;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kodak.kodak_kioskconnect_n2r.ImageCheckBoxView;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.AlternateLayout;
import com.kodak.kodak_kioskconnect_n2r.bean.content.Theme;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;

public class CollageThemeFragment extends Fragment{
	private TextView vTextViewDone ;
	private GridView vGridViewTheme ;
	private ThemeAdapter mAdapter ;
	private List<Theme> mCollageThemes ;
	private ImageFetcher mImageFetcher;
	private IOnCollageFragmentListener mListener ;
	private int mCollageThemeWidth ;
	private RelativeLayout.LayoutParams mImageViewLayoutParams;
	 public interface IOnCollageFragmentListener {
		 
		 public void doneOnClick() ;
		 
		 public void selectTheme(Theme theme ) ;
		 
		 public void selectPageLayout(AlternateLayout alternateLayout);
	       
	 }
	
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
		
		final View v = inflater.inflate(R.layout.fragment_collage_theme, container, false);

		vTextViewDone = (TextView) v.findViewById(R.id.text_done) ;
		vGridViewTheme = (GridView) v.findViewById(R.id.gridview_theme);
		mAdapter = new ThemeAdapter(getActivity(), mCollageThemes) ;
		vGridViewTheme.setAdapter(mAdapter);
		
		setEvents();

		return v;
	}

	private void  initData(){
		mImageFetcher = ((CollageEditActivity)getActivity()).getImageFetcher() ;
		
		mCollageThemes = ((CollageEditActivity)getActivity()).getCollageTheme() ;
		mCollageThemeWidth  = getResources().getDimensionPixelSize(R.dimen.collage_theme_image_width);
		float pageWidth = CollageManager.getInstance().getCurrentCollage().page.width ;
		float pageHeight = CollageManager.getInstance().getCurrentCollage().page.height ;
		int mCollageThemeHeight = 0 ;
		if(pageWidth>pageHeight){
			mCollageThemeHeight = (int) (mCollageThemeWidth*pageHeight
					/ pageWidth) ;
		}else {
			mCollageThemeHeight = (int) (mCollageThemeWidth*pageWidth
					/ pageHeight) ;
		}
		
		mImageViewLayoutParams = new RelativeLayout.LayoutParams(mCollageThemeWidth,mCollageThemeHeight) ;
	}
	
	
	private void setEvents() {
		
		vTextViewDone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mListener.doneOnClick() ;
			}
		}) ;
		
		vGridViewTheme.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (position < mAdapter.getNumColumns()) {
					return;
				}
				
				Theme theme = mCollageThemes.get(position - mAdapter.getNumColumns());
				
				if(theme.isSelected()){
					return  ;
				}
//				ThemeAdapter.ViewHolder holder = (ThemeAdapter.ViewHolder) view.getTag();
//				ImageCheckBoxView imageView = holder.vImageViewTheme;
				for (Theme collageTheme  : mCollageThemes) {
					if(collageTheme.id.equals(theme.id)){
						collageTheme.setSelected(true) ;
					}else {
						collageTheme.setSelected(false) ;
					}
					
					
				}
			
				mAdapter.notifyDataSetChanged() ;
				mListener.selectTheme(theme) ;
				
			}
		}) ;
		
		
	}
	
	
	class ThemeAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Theme> mDataList;
		private final Context mContext;
		private int mNumColumns = 2;
		private int mTopViewHeight = 0;
		
		public ThemeAdapter(Context context, List<Theme> dataList) {
			mDataList = dataList;
			mContext = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTopViewHeight = getResources().getDimensionPixelSize(R.dimen.top_view_height);
		}
		
		public int getNumColumns() {
			// TODO Auto-generated method stub
			return mNumColumns;
		}

		public void setDataSource(List<Theme> mDataList){
			this.mDataList = mDataList ;
			if( mDataList != null && getCount() > 0 ){
				notifyDataSetChanged() ;
			}else {
				notifyDataSetInvalidated() ;
			}
			
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
			
			Theme data = (Theme) getItem(position);
			
			final ViewHolder holder;
			if (convertView == null) { 
				convertView = mInflater.inflate(R.layout.collage_theme_gird_item, parent, false);
				holder = new ViewHolder();
				holder.vImageViewTheme = (ImageCheckBoxView) convertView.findViewById(R.id.image_theme) ;
				holder.vImageViewTheme.setLayoutParams(mImageViewLayoutParams) ;
				holder.vImageViewTheme.setScaleType(ImageView.ScaleType.FIT_XY);
				
				convertView.setTag(holder);
				
			}else {
				holder = (ViewHolder) convertView.getTag();
				holder.vImageViewTheme = (ImageCheckBoxView) convertView ;
			}
			
			mImageFetcher.loadImage(data.glyph, data.glyph, holder.vImageViewTheme, LoadImageType.WEB_IMAGE) ;
			holder.vImageViewTheme.setChecked(data.isSelected());
			
			return convertView;
		}
		
		
		
		private class ViewHolder {
			private ImageCheckBoxView vImageViewTheme;
			
		}
		
	}
	
	

	
	
	
	

}
