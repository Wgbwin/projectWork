package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.bean.ProductEditPopItem;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.ProductEditPopView;

public class PhotoBookEditPopAdapter extends ProductEditPopAdapter{
	private PhotoBooksProductActivity mActivity;

	public PhotoBookEditPopAdapter(Context context,int type){
		super(context,type);
		mActivity = (PhotoBooksProductActivity) context;
	}
	
	
	public PhotoBookEditPopAdapter(Context context) {
		super(context);
		mActivity = (PhotoBooksProductActivity) context;
	}


	@Override
	protected List<ProductEditPopItem> initFullList() {
		List<ProductEditPopItem> list = new ArrayList<ProductEditPopItem>();
		addLayerItemToList(list);
		addPageItemToList(list);
		addPageTextItemToList(list);
		return list;
	}
	
	private void addPageTextItemToList(List<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_DELETE_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.ComposePhotobook_DeleteJournalText, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_EDIT_PAGE_TEXT,ProductEditPopView.TYPE_PAGE_TEXT,R.string.ComposePhotobook_EditJournalText, R.drawable.add_caption_up));
	}
	
	private void addPageItemToList(List<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_ADD_PAGE,ProductEditPopView.TYPE_PAGE,R.string.ComposePhotobook_AddPage, R.drawable.add_page_up));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_DELETE_PAGE,ProductEditPopView.TYPE_PAGE,R.string.ComposePhotobook_DeletePage, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_BACKGROUND_OPTIONS,ProductEditPopView.TYPE_PAGE,R.string.ComposePhotobook_BackgroundOptions, R.drawable.set_as_page_background_up,false,true));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_ADD_PAGE_TEXT,ProductEditPopView.TYPE_PAGE,R.string.ComposePhotobook_AddJournalText, R.drawable.add_caption_up));
		list.add(new ProductEditPopItem(ProductEditPopView.PAGE_EDIT_TITLE,ProductEditPopView.TYPE_PAGE,R.string.ComposePhotobook_EditTitle, R.drawable.add_caption_up,false));
	}
	
	private void addLayerItemToList(List<ProductEditPopItem> list){
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_CROP, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Crop, R.drawable.crop_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_Enhance, R.drawable.photo_enhance_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_ENHANCE, ProductEditPopView.TYPE_LAYER, R.string.ComposePhotobook_UndoEnhance, R.drawable.photo_enhance_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_RedEye, R.drawable.red_eye_popup_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_UNDO_RED_EYE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_UndoRedEye, R.drawable.red_eye_popup_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_COLOR_EFFECTS,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Effects, R.drawable.color_up,true,true));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ROTATE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Rotate, R.drawable.rotate_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_REMOVE_IMAGE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Remove, R.drawable.remove_picture_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_SELECT_PAGE,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_SelectPage, R.drawable.add_page_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_ENTER_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption, R.drawable.add_caption_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_EDIT_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption_Edit, R.drawable.add_caption_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_DELETE_CAPTION,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_Caption_Delete, R.drawable.hide_caption_up,false));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_SET_AS_BACKGROUND,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_SetAsBackground, R.drawable.set_as_page_background_up));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_HORIZONTAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipHorizontal, R.drawable.fliphorizontalup));
		list.add(new ProductEditPopItem(ProductEditPopView.LAYER_FLIP_VERTICAL,ProductEditPopView.TYPE_LAYER,R.string.ComposePhotobook_FlipVertical, R.drawable.flipverticalup));
	}
	
	protected void refreshList(){
		//add item to current list
		mCurrentList.clear();
		PhotobookPage page = (PhotobookPage) mPage;
		for(ProductEditPopItem item : mFullList){
			//layer pop
			if(mType == ProductEditPopView.TYPE_LAYER && mLayer != null){
				ProductLayerLocalInfo layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(mLayer.contentId);
				if(layerLocalInfo == null){
					//put in map if the layer local info not contained in it
					RssTabletApp.getInstance().getProductLayerLocalInfos().put(mLayer, mActivity.isFromMyProject);
				}
				
				//show/hide add/edit/delete caption
				if(item.id == ProductEditPopView.LAYER_ENTER_CAPTION){
					item.visible = !PhotoBookProductUtil.isTitlePage(page) && !PhotoBookProductUtil.isLayerCaptionAdded(mLayer);
				}
				if(item.id == ProductEditPopView.LAYER_EDIT_CAPTION || item.id == ProductEditPopView.LAYER_DELETE_CAPTION){
					item.visible = !PhotoBookProductUtil.isTitlePage(page) && PhotoBookProductUtil.isLayerCaptionAdded(mLayer);
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
				
				//show/hide rotate
				if(item.id == ProductEditPopView.LAYER_ROTATE){
					item.visible = PhotobookPage.TYPE_LAYOUT_AUTO.equals(page.layoutType);
				}
				
			}
			
			//page pop
			if(mType == ProductEditPopView.TYPE_PAGE && page != null){
				if(item.id == ProductEditPopView.PAGE_BACKGROUND_OPTIONS){
					//page background is a image you setted(Layer->set as page background)
					item.visible = page.backgroundImageId != null && !"".equals(page.backgroundImageId);
				}
				
				if(item.id == ProductEditPopView.PAGE_EDIT_TITLE){
					item.visible = PhotoBookProductUtil.isTitlePage(page);
				}
				
				if(item.id == ProductEditPopView.PAGE_ADD_PAGE_TEXT){
					item.visible = !PhotoBookProductUtil.isTitlePage(page);
				}
				
			}
			
			if(item.visible && item.type == mType){
				mCurrentList.add(item);
			}
		}
		
	}

}
