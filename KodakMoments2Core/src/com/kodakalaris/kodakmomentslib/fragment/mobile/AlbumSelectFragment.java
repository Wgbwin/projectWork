package com.kodakalaris.kodakmomentslib.fragment.mobile;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.mobile.AlbumSelectionKodakAdapter;
import com.kodakalaris.kodakmomentslib.bean.AlbumHolder;
import com.kodakalaris.kodakmomentslib.bean.AlbumInfo;
import com.kodakalaris.kodakmomentslib.interfaces.ICommunicating;

public class AlbumSelectFragment extends Fragment{
	private static String TAG = AlbumSelectFragment.class.getSimpleName();
	private ListView vList;
	private List<AlbumInfo> mAlbums;
    private AlbumSelectionKodakAdapter mAdapter;
	private PhotoSource photoSource;
	
	private String productId = "";
	private ProgressDialog vProgressDialog;
	private ICommunicating mListener;
	private Bundle bundle ;
	
	public AlbumSelectFragment() {

	}

	public static AlbumSelectFragment newInstance(Bundle b) {
		AlbumSelectFragment f = new AlbumSelectFragment();

		f.setArguments(b);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ICommunicating) activity;
		} catch (ClassCastException e) {
	
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_selection_list, container, false);
		vList = (ListView) v.findViewById(R.id.list_imageselection);
		View footView = new View(getActivity());
		int footHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.imagetray_height);
		footView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,footHeight));
		
		vList.addFooterView(footView);
		vList.setAdapter(mAdapter);
		setEvents();
		return v;
	}
	
	private void initData() {
		if(bundle==null){
			bundle = getArguments();
		}
		if (bundle != null) {
			AlbumHolder albumHolder = (AlbumHolder) bundle.getSerializable(AppConstants.KEY_ALBUMS_HOLDER);
			if (albumHolder != null) {
				mAlbums = albumHolder.getAlbums();
			}
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
			productId = bundle.getString(AppConstants.KEY_PRODUCT_ID);

		}
		if(mAdapter==null){
			mAdapter = new AlbumSelectionKodakAdapter(getActivity(), mAlbums);
		}
	}
	
	private void setEvents() {
		vList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				AlbumInfo album = mAlbums.get(position);
				Bundle bundle = new Bundle();
				bundle.putSerializable(AppConstants.KEY_ALBUM, album);
				bundle.putSerializable(AppConstants.KEY_PHOTO_SOURCE, photoSource);
				bundle.putBoolean(AppConstants.KEY_ALBUM_SHOW_HEAD, true);
				mListener.showPhotosInAlbum(bundle);
				
			}
			
		});
		
	}
	
	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
		if (bundle != null) {
			AlbumHolder albumHolder = (AlbumHolder) bundle.getSerializable(AppConstants.KEY_ALBUMS_HOLDER);
			if (albumHolder != null) {
				mAlbums = albumHolder.getAlbums();
				if(mAdapter!=null){
					mAdapter.setDataSource(mAlbums);
				}
			}
			photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
			productId = bundle.getString(AppConstants.KEY_PRODUCT_ID);

		}
	}
}
