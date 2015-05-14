package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.EfficientAdapter;
import com.kodakalaris.kodakmomentslib.bean.AlbumInfo;
import com.kodakalaris.kodakmomentslib.widget.mobile.KMAlbumCoverView;

public class AlbumSelectionKodakAdapter extends EfficientAdapter<AlbumInfo>{
    private Context mContext;
    
   
	public AlbumSelectionKodakAdapter(Context context,List<AlbumInfo> dataList) {
		super(context,dataList);
		mContext = context;
	}

	@Override
	protected int getItemLayout() {
		
		return R.layout.item_image_selection_album_list;
	}

	@Override
	protected void initView(View v) {
		ViewHolder holder = new ViewHolder() ;
		holder.vImageAlbumCover = (KMAlbumCoverView) v.findViewById(R.id.image_album_cover) ;
		holder.vTxtAlbumName = (TextView) v.findViewById(R.id.text_album_name);
		holder.vTxtAlbumCount = (TextView) v.findViewById(R.id.text_album_count);
	    v.setTag(holder) ; 
	}

	@Override
	protected void bindView(View v, final AlbumInfo data, int position) {
		if(data == null){
			return ;
		}
		final ViewHolder holder = (ViewHolder) v.getTag();
		holder.vImageAlbumCover.setPhotos(data.getmPhotosInAlbum());
		holder.vImageAlbumCover.startShowCover();
		
		holder.vTxtAlbumName.setText(data.getmAlbumName());
		holder.vTxtAlbumCount.setText(mContext.getString(R.string.ImageSelection_album_photos_count,data.getmPhotosInAlbum().size() ));
		
		
	}
	
	private class ViewHolder{
		private KMAlbumCoverView vImageAlbumCover;
		private TextView vTxtAlbumName;
		private TextView vTxtAlbumCount;
	}
	

}
