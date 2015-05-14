package com.kodak.kodak_kioskconnect_n2r.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductEditPopItem;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;
import com.kodak.kodak_kioskconnect_n2r.view.PhotoBookEditPopColorEffectItemView;
import com.kodak.kodak_kioskconnect_n2r.view.ProductEditPopItemView;
import com.kodak.kodak_kioskconnect_n2r.view.ProductEditPopView;

public abstract class ProductEditPopAdapter extends BaseAdapter{
	protected Context mContext;
	protected List<ProductEditPopItem> mFullList;
	protected List<ProductEditPopItem> mCurrentList;
	private WeakReference<List<ColorEffect>> mColorEffectListRef;
	protected Page mPage;
	protected Layer mLayer;

	protected int mType;
	
	private static final int ITEM_TYPE_NORMAL = 0;
	private static final int ITEM_TYPE_COLOR_EFFECT = 1;
	
	public ProductEditPopAdapter(Context context,int type){
		this.mContext = context;
		this.mType = type;
		initList();
	}
	
	public ProductEditPopAdapter(Context context){
		this.mContext = context;
		this.mType = ProductEditPopView.TYPE_LAYER;
		initList();
	}
	
	private void initList(){
		mFullList = initFullList();
		mCurrentList = new ArrayList<ProductEditPopItem>();
		mColorEffectListRef = new WeakReference<List<ColorEffect>>(AppContext.getApplication().getColorEffects());
		Log.d("ProductEditPopAdapter", "colorEffects="+AppContext.getApplication().getColorEffects()+"");
		refreshList();
	}
	
	
	protected abstract List<ProductEditPopItem> initFullList();
	
	protected abstract void refreshList();
	
	@Override
	public int getItemViewType(int position) {
		return mType == ProductEditPopView.TYPE_COLOR_EFFECT ? ITEM_TYPE_COLOR_EFFECT : ITEM_TYPE_NORMAL;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getCount() {
		List<ColorEffect> list = mColorEffectListRef.get();
		int cCount = list == null ? 0 : list.size();
		return mType==ProductEditPopView.TYPE_COLOR_EFFECT ? cCount : mCurrentList.size();
	}

	@Override
	public Object getItem(int position) {
		if(getItemViewType(position) == ITEM_TYPE_COLOR_EFFECT){
			List<ColorEffect> list = mColorEffectListRef.get();
			return list == null ? null : list.get(position); 
		}else{
			return mCurrentList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		Object item = getItem(position);
		if(item == null){
			return 0;
		}else if(getItemViewType(position) == ITEM_TYPE_COLOR_EFFECT){
			return ((ColorEffect) item).id;
		}else{
			return ((ProductEditPopItem)item).id;
		}
	}

	@Override
	public View getView(int position, View cv, ViewGroup parent) {
		int viewType = getItemViewType(position);
		if(viewType == ITEM_TYPE_NORMAL){
			ProductEditPopItemView itemView;
			if(cv == null){
				itemView = new ProductEditPopItemView(mContext);
				cv = itemView;
				cv.setTag(itemView);
			}else{
				itemView = (ProductEditPopItemView) cv.getTag();
			}
			
			itemView.setInfo(mCurrentList.get(position));
		}else if(viewType == ITEM_TYPE_COLOR_EFFECT){
			PhotoBookEditPopColorEffectItemView itemView;
			if(cv == null){
				itemView = new PhotoBookEditPopColorEffectItemView(mContext);
				cv = itemView;
				cv.setTag(itemView);
			}else{
				itemView = (PhotoBookEditPopColorEffectItemView) cv.getTag();
			}
			
			itemView.setInfo((ColorEffect) getItem(position));
		}
		return cv;
	}
	
	public void setInfo(int type, Page page, Layer layer){
		this.mType = type;
		this.mPage = page;
		this.mLayer = layer;
	}
	
	private ProductEditPopItem removeItemFromList(List<ProductEditPopItem> list,int id){
		for(int i=0,len=list.size();i<len;i++){
			ProductEditPopItem item = list.get(i);
			if(item.id == id){
				return list.remove(i);
			}
		}
		return null;
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		refreshList();
		super.notifyDataSetChanged();
	}

}
