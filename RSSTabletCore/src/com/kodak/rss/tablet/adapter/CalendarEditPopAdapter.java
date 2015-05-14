package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.ProductEditPopItem;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.util.ProductUtil;
import com.kodak.rss.tablet.view.PhotoBookEditPopView;
import com.kodak.rss.tablet.view.ProductEditPopView;

public class CalendarEditPopAdapter extends ProductEditPopAdapter{

	public CalendarEditPopAdapter(Context context, int type) {
		super(context, type);
	}
	
	public CalendarEditPopAdapter(Context context) {
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
			//layer pop
			if(mType == ProductEditPopView.TYPE_LAYER && mLayer != null){
				ProductLayerLocalInfo layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(mLayer.contentId);
				if(layerLocalInfo == null){
					//put in map if the layer local info not contained in it
					RssTabletApp.getInstance().getProductLayerLocalInfos().put(mLayer, true);
				}
				
				//show/hide add/edit/delete caption
				if(item.id == ProductEditPopView.LAYER_ENTER_CAPTION){
					item.visible = mLayer.isCaptionable() && !ProductUtil.isLayerCaptionAdded(mLayer);
				}
				if(item.id == PhotoBookEditPopView.LAYER_EDIT_CAPTION || item.id == PhotoBookEditPopView.LAYER_DELETE_CAPTION){
					item.visible = mLayer.isCaptionable() && ProductUtil.isLayerCaptionAdded(mLayer);
				}
				
				//show/hide enhanced/unenhanced , red eye/undo redeype
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
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Enhance, R.drawable.photo_enhance_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_UndoEnhance, R.drawable.photo_enhance_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_RedEye, R.drawable.red_eye_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_UndoRedEye, R.drawable.red_eye_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_COLOR_EFFECTS,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Effects, R.drawable.color_up,true,true));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_REMOVE_IMAGE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Remove, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENTER_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption, R.drawable.add_caption_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_EDIT_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption_Edit, R.drawable.add_caption_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_DELETE_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption_Delete, R.drawable.hide_caption_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_HORIZONTAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipHorizontal, R.drawable.fliphorizontalup));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_VERTICAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipVertical, R.drawable.flipverticalup));
	}
}
