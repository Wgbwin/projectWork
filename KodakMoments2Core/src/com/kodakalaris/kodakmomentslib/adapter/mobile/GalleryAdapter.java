package com.kodakalaris.kodakmomentslib.adapter.mobile;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.culumus.bean.imageedit.ColorEffect;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class GalleryAdapter extends BaseAdapter {
	private static final String TEMP_FOLDER = "/temp/.colorEffect";
	// TODO should be clean after placing the order by kaly
	public final static String tempFolder = Environment
			.getExternalStorageDirectory().getAbsolutePath() + TEMP_FOLDER;
	private Context context;
	private DisplayImageOptions options;;
	// TODO get the data List and create the Constructor
	private List<ColorEffect> mList;
	// private String bitmapPath;
	private LayoutParams params;
	private int imgWidth = 58;
	private int imgHeight = 62;
	private int[] imageSize = { 0, 0 };
	private int cornerRadiusPixels = 8;
	private int marginPixels = 3;
	private float density = 1.0f;

	public GalleryAdapter(Context context, List<ColorEffect> list) {
		this.context = context;
		density = context.getResources().getDisplayMetrics().density;
		imgWidth = (int) (imgWidth * density);
		imgHeight = (int) (imgHeight * density);
		mList = list;
		// this.bitmapPath = bitmapPath;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.imagewait96x96)
				.showImageForEmptyUri(R.drawable.imageerror)
				.showImageOnFail(R.drawable.imageerror)
				.cacheInMemory(true)
				.cacheOnDisk(false)
				.considerExifParams(true)
				.displayer(
						new RoundedBitmapDisplayer(cornerRadiusPixels,
								marginPixels)).build();

	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder = null;
		String filePath = tempFolder + File.separator
				+ mList.get(position).name;
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_photo_edit_gallery, null);
			mHolder.mImgIcon = (ImageView) convertView
					.findViewById(R.id.img_item_photo_filter_picture);
			if (imageSize[0] == 0) {
				int[] size = getImageSize(filePath);
				if (size != null) {
					imageSize = size;
				}
				if (imageSize[0] != 0 && imageSize[1] != 0) {
					imgHeight = (int) (imageSize[1] * imgWidth / imageSize[0]);
				}
			}
			if (params == null) {
				params = mHolder.mImgIcon.getLayoutParams();
				params.width = imgWidth;
				params.height = imgHeight;
			}
			mHolder.mImgIcon.setLayoutParams(params);
			// mHolder.mTxtUnderLine = (TextView) convertView
			// .findViewById(R.id.txt_item_photo_filter_selecter);
			// mHolder.mTxtUnderLine.setVisibility(View.GONE);
			// mHolder.mTxtSubject = (TextView) convertView
			// .findViewById(R.id.txt_item_photo_filter_picturename);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		if (ifFileExist(filePath)) {
			ImageLoader.getInstance().displayImage(filePath, mHolder.mImgIcon,
					options);
		} else {
			ImageLoader.getInstance().displayImage(
					mList.get(position).glyphPathUrl.replaceAll(" ", "%20"),
					mHolder.mImgIcon, options);
		}

		// mHolder.mTxtSubject.setText(mList.get(position).name);
		return convertView;
	}

	public class ViewHolder {
		ImageView mImgIcon = null;
		// TextView mTxtUnderLine = null;
		// TextView mTxtSubject = null;
	}

	private int[] getImageSize(String filePath) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		return new int[] { opts.outWidth, opts.outHeight };
	}

	private boolean ifFileExist(String filePath) {
		File file = new File(filePath);
		return file.exists();
	}
}
