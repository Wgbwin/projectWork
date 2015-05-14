package com.kodak.rss.tablet.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ROI;
import com.kodak.rss.core.n2r.bean.photobook.Photobook;
import com.kodak.rss.core.n2r.bean.photobook.PhotobookPage;
import com.kodak.rss.core.n2r.bean.collage.Collage;
import com.kodak.rss.core.n2r.bean.collage.CollagePage;

import com.kodak.rss.core.n2r.bean.prints.Data;
import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.bean.ProductLayerLocalInfo;
import com.kodak.rss.tablet.thread.SetCropTask;
import com.kodak.rss.tablet.util.CollageUtil;
import com.kodak.rss.tablet.util.PhotoBookProductUtil;
import com.kodak.rss.tablet.view.CropImageView;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class PhotoBookCropImageActivity extends BaseNetActivity implements OnClickListener{

	private CropImageView cropImage;
	private Button doneButton;
	private Button backButton;
	private Layer layer;
	private Page page;
	private ROI oRoi;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop_image_photobook);
		initView();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
		
	private void initView(){
		doneButton = (Button) findViewById(R.id.done_button);
		backButton = (Button) findViewById(R.id.back_button);
		doneButton.setOnClickListener(this);
		backButton.setOnClickListener(this);
		cropImage = (CropImageView) findViewById(R.id.cropImage);
		
		layer = (Layer) getIntent().getSerializableExtra("Layer");	
		page = (Page) getIntent().getSerializableExtra("page");	
		
		ImageInfo nativeInfo = null;
		ProductLayerLocalInfo layerLocalInfo = null;

		if (page instanceof PhotobookPage) {
			Photobook currentPhotoBook = PhotoBookProductUtil.getCurrentPhotoBook();
			if (layer != null) {
				layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(layer.contentId);
				for (ImageInfo info : currentPhotoBook.chosenpics) {
					if (info != null && info.imageThumbnailResource != null && layer.contentId.equals(info.imageThumbnailResource.id)) {
						nativeInfo = info;
						break;
					}
				}
			}
		} else if (page instanceof CollagePage){
			Collage currentCollage = CollageUtil.getCurrentCollage();
			if (layer != null) {
				layerLocalInfo = RssTabletApp.getInstance().getProductLayerLocalInfos().get(layer.contentId);
				for (ImageInfo info : currentCollage.chosenpics) {
					if (info != null && info.imageOriginalResource != null && layer.contentId.equals(info.imageOriginalResource.id)) {
						nativeInfo = info;
						break;
					}
				}
			}

		}		
		
		ROI roi = null;
		if (layer.data != null) {
			for (int i = 0; i < layer.data.length; i++) {
				Data data = layer.data[i];
				if (data == null) continue;
				if (data.valueType == null) continue;				
				if (Data.FLAG_ROI_VAL.equals(data.valueType)) {
					ROI oldRoi  = (ROI) data.value;
					if (oldRoi != null && oldRoi.ContainerW > 0 && oldRoi.ContainerH>0) {
						double x = oldRoi.x/oldRoi.ContainerW;
						double y = oldRoi.y/oldRoi.ContainerH;
						double w = oldRoi.w/oldRoi.ContainerW;
						double h = oldRoi.h/oldRoi.ContainerH;
						
						oRoi = new ROI();					
						oRoi.x = x;					
						oRoi.y = y;						
						oRoi.w = w;
						oRoi.h = h;	
						
						roi = new ROI();
						roi.x = x;
						roi.y = y;
						roi.w = w;
						roi.h = h;
					}					
					break;
				}
			}
		}		
		cropImage.setInfo(layer, roi, layerLocalInfo, nativeInfo, doneButton);
	}
	
	@Override
	public void judgeHaveItems(){		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		if(v.getId()==R.id.done_button){
			if (cropImage.roi != null) {
				if (isEquals(oRoi, cropImage.roi)) {
					Intent intent = new Intent();	
					intent.putExtra("fromCrop", true);
					PhotoBookCropImageActivity.this.setResult(Activity.RESULT_OK,intent);	
					PhotoBookCropImageActivity.this.finish();			
				}else {
					SetCropTask setCropTask = new SetCropTask(PhotoBookCropImageActivity.this, layer.contentId, page,cropImage.roi);
					setCropTask.execute();
				}
			}else {
				android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {						
						Intent intent = new Intent();	
						intent.putExtra("fromCrop", true);
						PhotoBookCropImageActivity.this.setResult(Activity.RESULT_OK,intent);	
						PhotoBookCropImageActivity.this.finish();			
					}		
				};		
				new InfoDialog.Builder(this).setMessage(R.string.lose_crop)
				.setPositiveButton(getText(R.string.d_no), null)
				.setNegativeButton(R.string.d_yes, yesOnClickListener)
				.create()
				.show();	
			}	
		}else if(v.getId() == R.id.back_button){
			onBackPressed();
		}
	}	

	private boolean isEquals(ROI roi,ROI oldRoi){
		if (oldRoi== null || roi == null) return false;
		if (roi.equals(oldRoi)) return true;
		if (Math.abs(roi.x - oldRoi.x) > 0.0000001) return false;
		if (Math.abs(roi.y - oldRoi.y) > 0.0000001) return false;
		if (Math.abs(roi.w - oldRoi.w) > 0.0000001) return false;
		if (Math.abs(roi.h - oldRoi.h) > 0.0000001) return false;		
		return true;
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();	
		intent.putExtra("fromCrop", true);
		setResult(RESULT_CANCELED,intent);
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (cropImage != null) cropImage.recycleInFinish();		
	}
	
}
