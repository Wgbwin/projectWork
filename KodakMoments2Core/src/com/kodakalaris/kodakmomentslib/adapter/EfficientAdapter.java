package com.kodakalaris.kodakmomentslib.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * To work efficiently the adapter implemented here uses two techniques: - It
 * reuses the convertView passed to getView() to avoid inflating View when it is
 * not necessary - It uses the ViewHolder pattern to avoid calling
 * findViewById() when it is not necessary
 * 
 * The ViewHolder pattern consists in storing a data structure in the tag of the
 * view returned by getView(). This data structures contains references to the
 * views we want to bind data to, thus avoiding calls to findViewById() every
 * time getView() is invoked.
 * 
 */
public abstract class EfficientAdapter<T> extends BaseAdapter {
	private LayoutInflater mInflater;
	private int mItemLayout;
	protected List<T> mDataList;
	protected DisplayImageOptions options;
	public EfficientAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mItemLayout = getItemLayout();
	}

	public EfficientAdapter(Context context, List<T> dataList) {
		this(context);
		mDataList = dataList;
	}

	public EfficientAdapter(Context context, T[] dataArray) {
		this(context);
		if (dataArray != null) {
			mDataList = new ArrayList<T>();
			for (T t : dataArray) {
				mDataList.add(t);
			}

		}
	}

	/**
	 * set the datasource for the adapter ,you should invoke this method always
	 * in the main thread
	 * 
	 * @param dataList
	 */
	public void setDataSource(List<T> dataList) {
		mDataList = dataList;
		if (mDataList != null && getCount() > 0) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}

	public void setDataSource(T[] dataArray) {
		if (dataArray != null) {
			if (mDataList == null) {
				mDataList = new ArrayList<T>();
			} else {
				mDataList.clear();
			}

			for (T t : dataArray) {
				mDataList.add(t);
			}

		}
		if (mDataList != null && getCount() > 0) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}

	@Override
	public int getCount() {
		if (mDataList == null) {
			return 0;
		} else {
			return mDataList.size();
		}

	}

	@Override
	public T getItem(int position) {
		if (position >= getCount()) {
			return null;
		}

		return mDataList.get(position);
	}

	/**
	 * use the array index as a unique id.
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newView(parent);
			initView(v);
		} else {
			v = convertView;
		}
		bindView(v, getItem(position), position);
		return v;
	}

	protected View newView(ViewGroup parent) {
		return mInflater.inflate(mItemLayout, parent, false);
	}

	/**
	 * get the layout resource id of the item
	 */
	protected abstract int getItemLayout();

	/**
	 * 
	 * initialize the view by findViewById()
	 */
	protected abstract void initView(View v);

	/**
	 * bind the data to the view
	 */
	protected abstract void bindView(View v, T data, int position);

}
