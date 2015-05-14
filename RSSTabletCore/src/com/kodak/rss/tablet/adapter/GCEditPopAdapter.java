package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.ProductEditPopItem;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.view.GCEditPopView;

public class GCEditPopAdapter extends ProductEditPopAdapter{
	
	public GCEditPopAdapter(Context context,int type){
		super(context, type);
	}
	
	public GCEditPopAdapter(Context context) {
		super(context);
	}

	@Override
	protected List<ProductEditPopItem> initFullList() {
		List<ProductEditPopItem> list = new ArrayList<ProductEditPopItem>();
		addLayerItemToList(list);
		return list;
	}
	
	private void addLayerItemToList(List<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_ENHANCE, GCEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Enhance, R.drawable.photo_enhance_popup_up));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_UNDO_ENHANCE, GCEditPopView.TYPE_LAYER, R.string.ComposePhotobook_UndoEnhance, R.drawable.photo_enhance_popup_up,false));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_RED_EYE,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_RedEye, R.drawable.red_eye_popup_up));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_UNDO_RED_EYE,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_UndoRedEye, R.drawable.red_eye_popup_up,false));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_COLOR_EFFECTS,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Effects, R.drawable.color_up,true,true));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_REMOVE_IMAGE,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Remove, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_ROTATE,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Rotate, R.drawable.rotate_up));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_FLIP_HORIZONTAL,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipHorizontal, R.drawable.fliphorizontalup));
		list.add(new ProductEditPopItem(GCEditPopView.LAYER_FLIP_VERTICAL,GCEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipVertical, R.drawable.flipverticalup));
	}
	
	@Override
	protected void refreshList(){
		//add item to current list
		mCurrentList.clear();
		for(ProductEditPopItem item : mFullList){
			//layer pop
			if(mType == GCEditPopView.TYPE_LAYER && mLayer != null){
				ProductLayerLocalInfo layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(mLayer.contentId);
				if(layerLocalInfo == null){
					//put in map if the layer local info not contained in it
					// TODO: replace isFromMyProject
					//RssTabletApp.getInstance().getPhotobookLayerLocalInfos().put(layer, activity.isFromMyProject);
					RssTabletApp.getInstance().getProductLayerLocalInfos().put(mLayer, false);
					layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(mLayer.contentId);
				}
				
				//show/hide enhanced/unenhanced , red eye/undo redeype
				if(item.id == GCEditPopView.LAYER_ENHANCE){
					item.visible = !layerLocalInfo.isEnhanced;
				}else if(item.id == GCEditPopView.LAYER_UNDO_ENHANCE){
					item.visible = layerLocalInfo.isEnhanced;
				}
				
				if(item.id == GCEditPopView.LAYER_RED_EYE){
					item.visible = !layerLocalInfo.isRedEyed;
				}else if(item.id == GCEditPopView.LAYER_UNDO_RED_EYE){
					item.visible = layerLocalInfo.isRedEyed;
				}
				
				//show/hide rotate
				if(item.id == GCEditPopView.LAYER_ROTATE){
					item.visible = true; //PhotobookPage.TYPE_LAYOUT_AUTO.equals(page.layoutType);
				}
			}
			
			if(item.visible && item.type == mType){
				mCurrentList.add(item);
			}
		}
		
	}
	
}
