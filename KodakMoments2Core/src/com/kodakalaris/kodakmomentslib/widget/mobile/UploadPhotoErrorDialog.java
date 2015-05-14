package com.kodakalaris.kodakmomentslib.widget.mobile;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.adapter.EfficientAdapter;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

public class UploadPhotoErrorDialog extends BaseGeneralAlertDialogFragment{
	private LayoutInflater mInflater;
	private TextView vTextDetails;
	private GridView vGridPhotos;
	private List<PhotoInfo> uploadFailedPhotos;
	private PhotoAdapter mPhotoAdapter;
	public UploadPhotoErrorDialog(Context context,List<PhotoInfo> uploadFailedPhotos) {
		super(context, false);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.uploadFailedPhotos = uploadFailedPhotos;
		mPhotoAdapter = new PhotoAdapter(context,uploadFailedPhotos);
		initDialog(context);
	}
	
	private void initDialog(Context context){
		setTitle("Upload Error");
		setPositiveButton(context.getString(R.string.Common_OK), new OnClickListener() {
			
			@Override
			public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		setNegativeButton("", null);
		setContentAreaSize(0.78f, 0.69f);
	}

	@Override
	protected View initMessageContent() {
		
		View v = mInflater.inflate(R.layout.upload_error_dialog, null, false);
		vGridPhotos = (GridView) v.findViewById(R.id.error_photos_gridView);
		vTextDetails= (TextView) v.findViewById(R.id.text_error_detail);
		int photoListSize = uploadFailedPhotos.size();
		int width = photoListSize*mContainerAreaWidth/2 +(photoListSize-1)*mContext.getResources().getDimensionPixelSize(
				R.dimen.list_item_space);
		LayoutParams params = new LayoutParams(width,mContainerAreaWidth/2+10);
		
		vGridPhotos.setAdapter(mPhotoAdapter);
		vGridPhotos.setLayoutParams(params);
		vGridPhotos.setNumColumns(uploadFailedPhotos.size());
		vGridPhotos.setColumnWidth(mContainerAreaWidth/2);
		vGridPhotos.setStretchMode(GridView.NO_STRETCH);
		return v;
	}
	
	private class PhotoAdapter extends EfficientAdapter<PhotoInfo>{


		public PhotoAdapter(Context context, List<PhotoInfo> dataList) {
			super(context, dataList);
		}

		@Override
		protected int getItemLayout() {
		
			return R.layout.item_error_photos;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder();
			holder.vImageUploadFailed = (ImageView) v.findViewById(R.id.image);
			LinearLayout.LayoutParams params = new LayoutParams(mContainerAreaWidth/2 ,mContainerAreaWidth/2);
			holder.vImageUploadFailed.setLayoutParams(params);
			v.setTag(holder);
		}

		@Override
		protected void bindView(View v, PhotoInfo data, int position) {
			if (data == null) {
				return;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			if(data.getPhotoSource().isFromPhone()){
				ImageLoader.getInstance().displayImage("file://"+data.getPhotoPath(), holder.vImageUploadFailed);
			}
			
			
			
		}
		
		class ViewHolder{
			private ImageView vImageUploadFailed;
		}
		
	}
	
	

}
