package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.PhotoBooksProductActivity;
import com.kodak.rss.tablet.thread.AddCopyPhotoToPageTask;
import com.kodak.rss.tablet.thread.MovePhotoTask;
import com.kodak.rss.tablet.thread.ReMovePhotoFromPhotoBookTask;
import com.kodak.rss.tablet.thread.WaitUploadAddImagesTask;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;

/**
 * Purpose: pop photo edit window
 * Author: Bing Wang 
 */
public class PhotoEditView extends FrameLayout implements OnClickListener{
	
	protected Context mContext;	
	private String[] addMoreImages;
	private LinearLayout ll;
	public PhotoBooksProductActivity activity;
	private ImageInfo mInfo;		
	private PhotobookPage currentPage;
	private boolean isDisplayMove = false;	
	private Layer addNeweLayer;
	private Photobook currentPhotoBook;
		
	public PhotoEditView(Context context) {
		super(context);
		init(context);
	}
	
	public PhotoEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public PhotoEditView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	protected void init(Context context){
		mContext = context;	
		this.activity = (PhotoBooksProductActivity) context;	
		inflate(mContext, R.layout.dialog_edit_photo, this);				
		initViews();
	};
	
	public void initViews(){
		ll =  (LinearLayout) findViewById(R.id.editPhotoDialog);		
		findViewById(R.id.frist_button).setOnClickListener(this);
		findViewById(R.id.sec_button).setOnClickListener(this);
		findViewById(R.id.thr_button).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);	
	}
	
	public PhotoEditView setMoreImages(int currentPos,ImageInfo info, String[] moreImages,Layer layer){		
		this.mInfo = info;	
		this.addMoreImages = moreImages;
		this.addNeweLayer = layer;		
		currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
		currentPage = PhotoBookProductUtil.getCurrentPage(currentPos);
		if (mInfo != null || addNeweLayer != null) {
			isDisplayMove = false;
			PhotobookPage[] pageItems = PhotoBookProductUtil.getPageItems(currentPhotoBook).get(currentPos);
			if (PhotoBookProductUtil.isDiaplayMove(pageItems)) {
				isDisplayMove = true;
			}								
			if (isDisplayMove) {
				((TextView) findViewById(R.id.frist_prompt)).setText(R.string.movePrompt);
				((Button) findViewById(R.id.frist_button)).setText(R.string.moveDispaly);				
			}else {
				((TextView) findViewById(R.id.frist_prompt)).setText(R.string.addCopyPrompt);
				((Button) findViewById(R.id.frist_button)).setText(R.string.addCopyDispaly);
			}						
			((TextView) findViewById(R.id.prompt)).setText(R.string.oldPhotoPrompt);			
			((TextView) findViewById(R.id.sec_prompt)).setText(R.string.removePrompt);
			((TextView) findViewById(R.id.thr_prompt)).setText(R.string.editPrompt);				
			((Button) findViewById(R.id.sec_button)).setText(R.string.reMoveDispaly);
			((Button) findViewById(R.id.thr_button)).setText(R.string.editDispaly);			
		}else {			
			((TextView) findViewById(R.id.prompt)).setText(R.string.newPhotoPrompt);
			((TextView) findViewById(R.id.frist_prompt)).setText(R.string.addToEndPrompt);
			((TextView) findViewById(R.id.sec_prompt)).setText(R.string.relayoutPrompt);
			((TextView) findViewById(R.id.thr_prompt)).setText(R.string.addToTrayPrompt);		
			((Button) findViewById(R.id.frist_button)).setText(R.string.addToEndDispaly);
			((Button) findViewById(R.id.sec_button)).setText(R.string.relayoutDispaly);
			((Button) findViewById(R.id.thr_button)).setText(R.string.addToTrayDispaly);
		}
		return this;
	}	

	public void showAt(){
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();	
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);			
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();	
		params.height = dm.heightPixels *3 / 4;
		if (dm.heightPixels < 750) {
			params.width  = dm.heightPixels;			
		}else {
			params.width  = dm.heightPixels *4 / 5;			
		}		
		setLayoutParams(params);									
		((View)getParent()).setVisibility(View.VISIBLE);	
		
		LayoutParams lparams = (LayoutParams) ll.getLayoutParams();
		lparams.height = dm.heightPixels *3 / 4;		
		if (dm.heightPixels < 750) {
			lparams.width  = dm.heightPixels;			
		}else {
			lparams.width  = dm.heightPixels *4 / 5;			
		}		
		ll.setLayoutParams(lparams);			
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.frist_button){						
			if (mInfo != null || addNeweLayer != null) {												
				if (isDisplayMove) {					
					MovePhotoTask movePhotoTask = new MovePhotoTask(mContext, currentPage, mInfo, addNeweLayer);
					movePhotoTask.execute();														
				}else {
					if (PhotoBookProductUtil.getPhotobookPageEditable(currentPage)) {						
						AddCopyPhotoToPageTask  task = new AddCopyPhotoToPageTask(mContext, currentPage, mInfo,addNeweLayer);	
						task.execute();											
					}					
				}			
			}else {				
				WaitUploadAddImagesTask wTask = new WaitUploadAddImagesTask(mContext,WaitUploadAddImagesTask.Add_End_Flg,addMoreImages);
				wTask.execute();									
			}																	
		}else if(v.getId()==R.id.sec_button ){								
			if (mInfo != null || addNeweLayer != null) {
				if (mInfo != null) {
					ReMovePhotoFromPhotoBookTask task = new ReMovePhotoFromPhotoBookTask(mContext, mInfo.imageThumbnailResource,null);
					task.execute();
				}else {
					ReMovePhotoFromPhotoBookTask task = new ReMovePhotoFromPhotoBookTask(mContext, null,addNeweLayer);
					task.execute();
				}
			}else {				
				WaitUploadAddImagesTask wTask = new WaitUploadAddImagesTask(mContext,WaitUploadAddImagesTask.Relayout_Flg,addMoreImages);
				wTask.execute();	
			}								
		}else if(v.getId()==R.id.thr_button){
			if (mInfo != null || addNeweLayer != null) {												
				PhotobookPage skipPage = null;
				Layer mLayer = null;				
				for (int i = 0; i < currentPhotoBook.pages.size(); i++) {
					if (skipPage != null) {
						break;
					}		
					PhotobookPage page = currentPhotoBook.pages.get(i);
					if (page!=null && page.layers != null) {
						for (Layer layer : page.layers) {
							if (layer != null) {
								if (mInfo != null && mInfo.imageThumbnailResource.id.equals(layer.contentId)) {
									skipPage = page;
									mLayer = layer;
									break;
								}else if (addNeweLayer != null && addNeweLayer.contentId.equals(layer.contentId)){
									skipPage = page;
									mLayer = layer;
									break;
								}
							}
						}
					}
				}
				
				if(skipPage != null && mLayer != null){
					int pos = PhotoBookProductUtil.getPageInListPosition(skipPage);
					activity.pbLayout.pageTo(pos);
					PhotoBookMainItemView iv = activity.pbLayout.getViewByPosition(pos);
					if(iv!=null){
						int[] location = new int[2];
						activity.picturesButton.getLocationOnScreen(location);	
						
						int[] layLocation = new int[2];
						activity.pbLayout.getLocationOnScreen(layLocation);	
						int height = activity.pbLayout.getHeight();
						
						if (layLocation[1]+height-8 > location[1] ) {
							activity.panel.setMaxOpenContentHeight(activity.defaultContentHeight);
						}
						if(skipPage.equals(iv.ivLeft.getPage())){
							activity.layerEdit.showEditImageAndPop(iv.ivLeft, mLayer);				
						}else if(skipPage.equals(iv.ivRight.getPage())){
							activity.layerEdit.showEditImageAndPop(iv.ivRight, mLayer);				
						}
					}
				}
			}				
		}else if(v.getId()==R.id.cancel){			
					
		}
		((View)getParent()).setVisibility(View.GONE);		
	}	

}
