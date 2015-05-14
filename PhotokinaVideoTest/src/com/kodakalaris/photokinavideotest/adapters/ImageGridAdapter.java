package com.kodakalaris.photokinavideotest.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodakalaris.photokinavideotest.R;
import com.kodakalaris.photokinavideotest.ThumbnailRenderer;
import com.kodakalaris.photokinavideotest.activities.BaseActivity;
import com.kodakalaris.photokinavideotest.views.SquareImageView;

public class ImageGridAdapter extends BaseAdapter {

	private static final String TAG = ImageGridAdapter.class.getSimpleName();
	private int count;
	private LayoutInflater mInflater;
	private Cursor imagecursor;
	Context mContext;

	public ImageGridAdapter(Context context) {
		this.mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
		final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
		imagecursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
		this.count = imagecursor.getCount();

	}

	public int getCount() {
		return count;
	}

	public Object getItem(int position) {
		imagecursor.moveToPosition(position);
		// int id =
		// imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.Media._ID));
		int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
		String filePath = imagecursor.getString(dataColumnIndex);
		return filePath;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		SquareImageView holder;
		if (convertView == null) {
			holder = (SquareImageView) mInflater.inflate(R.layout.activity_select_photos_grid_element, null);
		} else {
			holder = (SquareImageView) convertView;
		}
		doSetView(position, holder, null);
		return holder;
	}

	public void doSetView(int position, SquareImageView holder, View progressView) {
		imagecursor.moveToPosition(position);
		int id = imagecursor.getInt(imagecursor.getColumnIndex(MediaStore.Images.Media._ID));
		int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
		String filePath = imagecursor.getString(dataColumnIndex);
		doSetViewWithFilePath(position, holder, progressView, id, filePath);
	}

	@SuppressWarnings("unchecked")
	static void doSetViewWithFilePath(int position, SquareImageView holder, View progressView, int id, String filePath) {
		if (holder.getContext() instanceof BaseActivity) {
			BaseActivity activity = (BaseActivity) holder.getContext();
			if (holder.getImageSize() == SquareImageView.RESOLUTION_HIGH) {
				Bitmap b = activity.getBitmapFromCache(filePath);
				if (b != null) {
					holder.setImageBitmap(b);
					holder.setFilePath(filePath);
					// Log.w(TAG,"Setting gone 1");
					if (progressView != null) {
						progressView.setVisibility(View.GONE);
					}
					return;
				}
			} else {
				holder.setImagePosition(position);
			}
			if (holder.getImageSize() == SquareImageView.RESOLUTION_THUMBNAIL) {
				Bitmap thumb = activity.getThumbFromCache(filePath);
				if (thumb != null) {
					holder.setImageBitmap(thumb);
					holder.setFilePath(filePath);
					return;
				}
			}
		}
		// show this while loading the image
		holder.setImageBitmapAndFilePath("");
		// however, set the file path, so the ThumbnailRenderer knows it has
		// the right path after the render is done
		holder.setFilePath(filePath);
		// Log.i(TAG, "FilePath:" + filePath);

		ThumbnailRenderer t = new ThumbnailRenderer(holder, progressView);
		//t.execute(new Pair<String, Integer>(filePath, id));
		t.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Pair<String, Integer>(filePath, id));

	}
}
