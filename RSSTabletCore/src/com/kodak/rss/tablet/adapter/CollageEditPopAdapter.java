package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.ProductEditPopItem;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.view.ProductEditPopView;

public class CollageEditPopAdapter extends ProductEditPopAdapter{

	public CollageEditPopAdapter(Context context, int type) {
		super(context, type);
	}
	
	public CollageEditPopAdapter(Context context) {
		super(context);
	}

	@Override
	protected List<ProductEditPopItem> initFullList() {
		ArrayList<ProductEditPopItem> list = new ArrayList<ProductEditPopItem>();
		addPageTextItemToList(list);
		addLayerItemToList(list);
		
		return list;
	}

	@Override
	protected void refreshList() {
		//add item to current list
		mCurrentList.clear();
		for(ProductEditPopItem item : mFullList){
			if(mType == ProductEditPopView.TYPE_LAYER && mLayer != null){
				ProductLayerLocalInfo layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(mLayer.contentId);
				if(layerLocalInfo == null){
					RssTabletApp.getInstance().getProductLayerLocalInfos().put(mLayer, true);
				}

				if(item.id == ProductEditPopView.LAYER_ENHANCE){
					item.visible = !layerLocalInfo.isEnhanced;
				}else if(item.id == ProductEditPopView.LAYER_UNDO_ENHANCE){
					item.visible = layerLocalInfo.isEnhanced;
				}
				
				if(item.id == ProductEditPopView.LAYER_RED_EYE){
					item.visible = !layerLocalInfo.isRedEyed;
				}else if(item.id == ProductEditPopView.LAYER_UNDO_RED_EYE){
					item.visible = layerLocalInfo.isRedEyed;
				}			
			}

			if(item.visible && item.type == mType){
				mCurrentList.add(item);
			}
		}
	}
	
	private void addPageTextItemToList(ArrayList<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_DELETE_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.ComposePhotobook_DeleteJournalText, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_EDIT_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.ComposePhotobook_EditJournalText, R.drawable.add_caption_up));
	}
	
	private void addLayerItemToList(ArrayList<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_CROP, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Crop, R.drawable.crop_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Enhance, R.drawable.photo_enhance_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_UndoEnhance, R.drawable.photo_enhance_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_RedEye, R.drawable.red_eye_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_UndoRedEye, R.drawable.red_eye_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_COLOR_EFFECTS,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Effects, R.drawable.color_up,true,true));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ROTATE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Rotate, R.drawable.rotate_up));		
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_REMOVE_IMAGE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Remove, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_SET_AS_BACKGROUND,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_SetAsBackground, R.drawable.set_as_page_background_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_HORIZONTAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipHorizontal, R.drawable.fliphorizontalup));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_VERTICAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipVertical, R.drawable.flipverticalup));
	}
}
