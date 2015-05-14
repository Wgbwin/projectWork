package com.kodakalaris.kodakmomentslib.fragment.mobile;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppConstants.PhotoSource;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.imageselection.MImageSelectionMainActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.ImageSelectionKodakAdapter;
import com.kodakalaris.kodakmomentslib.bean.AlbumInfo;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.interfaces.IPhotoOperationInterface;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMAlbumCoverView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

public class PhotoSelectFragment extends Fragment {
	private static String TAG = PhotoSelectFragment.class.getSimpleName();
	private AlbumInfo mAlbum;
	private ListView vList ;
	private ImageSelectionKodakAdapter mPhotosAdapter;
	private PhotoSource photoSource;
	private List<PhotoInfo> mPhotosInAlbum;
	private IPhotoOperationInterface onPhotoOperationListener;
	private Bundle bundle ;
	private Context context;
	private boolean isShowHeadView;
	
	public static PhotoSelectFragment newInstance(Bundle b) {
		PhotoSelectFragment f = new PhotoSelectFragment();

		f.setArguments(b);
		return f;
	}
   
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onPhotoOperationListener = (IPhotoOperationInterface) activity;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_selection_list, container, false);
		vList = (ListView) v.findViewById(R.id.list_imageselection);
		
		if(isShowHeadView && mAlbum!=null && mAlbum.getmPhotosInAlbum()!=null &&
				mAlbum.getmPhotosInAlbum().size()>0){
			
			View headView  = inflater.inflate(R.layout.item_image_selection_album_list, container,false);
			headView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			KMAlbumCoverView vImageAlbumCover = (KMAlbumCoverView) headView.findViewById(R.id.image_album_cover) ;
			TextView vTxtAlbumName = (TextView) headView.findViewById(R.id.text_album_name);
			TextView vTxtAlbumCount = (TextView) headView.findViewById(R.id.text_album_count);
			
			vImageAlbumCover.setPhotos(mAlbum.getmPhotosInAlbum());
			vImageAlbumCover.startShowCover();
			vTxtAlbumName.setText(mAlbum.getmAlbumName());
			vTxtAlbumCount.setText(getActivity().getString(R.string.ImageSelection_album_photos_count,mAlbum.getmPhotosInAlbum().size() ));
			vList.addHeaderView(headView);
		}
		
		
		View footView = new View(getActivity());
		int footHeight = getActivity().getResources().getDimensionPixelSize(R.dimen.imagetray_height);
		footView.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,footHeight));
		vList.addFooterView(footView);
		vList.setAdapter(mPhotosAdapter);
		setEvents();
		return v;
	}
	
	

	private void initData() {
		if(bundle==null){
			bundle = getArguments();
		}
		mAlbum = (AlbumInfo) bundle.getSerializable(AppConstants.KEY_ALBUM);
		photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
		isShowHeadView = bundle.getBoolean(AppConstants.KEY_ALBUM_SHOW_HEAD, false);
		mPhotosInAlbum = mAlbum.getmPhotosInAlbum();
		List<PhotoInfo> tempSelectedPhotos = ((MImageSelectionMainActivity)getActivity()).getmImageSelector().getmTempSelectedPhotos();
		
		if (mPhotosInAlbum != null && mPhotosInAlbum.size() > 0 
				&& tempSelectedPhotos!=null && tempSelectedPhotos.size()>0){
			for (PhotoInfo photo : mPhotosInAlbum) {
				for (PhotoInfo selectedPhoto : tempSelectedPhotos) {
					if (photo.equalsNotConsiderDesId(selectedPhoto)) {
						photo.setSelected(selectedPhoto.isSelected());
					}
				}
			}
			
		}
		
		mPhotosAdapter = new ImageSelectionKodakAdapter(getActivity(), mPhotosInAlbum,onPhotoOperationListener,((MImageSelectionMainActivity)getActivity()).getmImageSelector());
		
	}
	
	private void setEvents() {
		vList.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mPhotosAdapter = null;
		vList = null;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
		mAlbum = (AlbumInfo) bundle.getSerializable(AppConstants.KEY_ALBUM);
		photoSource = (PhotoSource) bundle.getSerializable(AppConstants.KEY_PHOTO_SOURCE);
		isShowHeadView = bundle.getBoolean(AppConstants.KEY_ALBUM_SHOW_HEAD, false);
		mPhotosInAlbum = mAlbum.getmPhotosInAlbum();
		List<PhotoInfo> tempSelectedPhotos = ((MImageSelectionMainActivity)context).getmImageSelector().getmTempSelectedPhotos();
		
		if (mPhotosInAlbum != null && mPhotosInAlbum.size() > 0 
				&& tempSelectedPhotos!=null && tempSelectedPhotos.size()>0){
			for (PhotoInfo photo : mPhotosInAlbum) {
				for (PhotoInfo selectedPhoto : tempSelectedPhotos) {
					if (photo.equalsNotConsiderDesId(selectedPhoto)) {
						photo.setSelected(selectedPhoto.isSelected());
					}
				}
			}
		}
		if(mPhotosAdapter!=null){
			mPhotosAdapter.setDataSource(mPhotosInAlbum);
		}
		
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	

}
