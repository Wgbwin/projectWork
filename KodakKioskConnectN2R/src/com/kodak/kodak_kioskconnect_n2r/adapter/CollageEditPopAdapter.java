package com.kodak.kodak_kioskconnect_n2r.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductEditPopItem;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductLayerLocalInfo;
import com.kodak.kodak_kioskconnect_n2r.view.PhotoBookEditPopView;
import com.kodak.kodak_kioskconnect_n2r.view.ProductEditPopView;
import com.kodak.utils.ProductUtil;

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
			//layer pop
			if(mType == ProductEditPopView.TYPE_LAYER && mLayer != null){
				ProductLayerLocalInfo layerLocalInfo = AppContext.getApplication().getProductLayerLocalInfos().get(mLayer.contentId);
				if(layerLocalInfo == null){
					//put in map if the layer local info not contained in it
					AppContext.getApplication().getProductLayerLocalInfos().put(mLayer, true);
				}
				
				//show/hide add/edit/delete caption
				if(item.id == ProductEditPopView.LAYER_ENTER_CAPTION){
					item.visible = !ProductUtil.isLayerCaptionAdded(mLayer);
				}
				if(item.id == PhotoBookEditPopView.LAYER_EDIT_CAPTION || item.id == PhotoBookEditPopView.LAYER_DELETE_CAPTION){
					item.visible = ProductUtil.isLayerCaptionAdded(mLayer);
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
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_DELETE_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.DeleteJournalText, R.drawable.toolsdeletesmallup));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_EDIT_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.EditJournalText, R.drawable.add_caption_up));
	}
	
	private void addLayerItemToList(ArrayList<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ROTATE, ProductEditPopView.TYPE_LAYER, R.string.rotate, R.drawable.rotate));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_COLOR_EFFECTS,ProductEditPopView.TYPE_LAYER,R.string.Color_Effects, R.drawable.color_up,true,true));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_REMOVE_IMAGE,ProductEditPopView.TYPE_LAYER,R.string.delete, R.drawable.toolsdeletesmallup,true));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.Enhance, R.drawable.photo_enhance_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.UndoEnhance, R.drawable.photo_enhance_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.RedEye, R.drawable.red_eye_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.UndoRedEye, R.drawable.red_eye_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_HORIZONTAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipHorizontal, R.drawable.fliphorizontalup));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_VERTICAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipVertical, R.drawable.flipverticalup));
	}
}
