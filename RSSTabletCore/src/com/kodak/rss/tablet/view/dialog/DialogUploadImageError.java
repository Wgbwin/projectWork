package com.kodak.rss.tablet.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.activities.BaseActivity;
import com.kodak.rss.tablet.services.PictureUploadService;
import com.kodak.rss.tablet.util.ShoppingCartUtil;

public class DialogUploadImageError implements OnClickListener{
	private Context mContext;
	private BaseActivity baseActivity;
	private Dialog dialog;
	private View dialogView;
	private ImageView disPalyImageView;
		
	public void initDialogUploadImageError(Context context, ImageInfo imageInfo ) { //String flowType,		
		this.mContext = context;
		baseActivity = (BaseActivity) context;
		
		baseActivity.isHaveUploadErrorDialog = true;
		LayoutInflater inflater = LayoutInflater.from(context);
		dialogView = inflater.inflate(R.layout.dialog_upload_image_error, null);			
		dialogView.findViewById(R.id.start_button).setOnClickListener(this);
		dialogView.findViewById(R.id.retry_button).setOnClickListener(this);
		
		disPalyImageView = (ImageView) dialogView.findViewById(R.id.dispaly_image);
		RelativeLayout layout = (RelativeLayout) dialogView.findViewById(R.id.dispaly_layout);		
		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(context);
		dialog = dialogbuilder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		dialog.getWindow().setContentView(dialogView);
		
		ViewGroup.LayoutParams dialogLp = dialogView.getLayoutParams();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();	
		dialogLp.height = dm.heightPixels;		
		dialogLp.width = dm.widthPixels;
		dialogView.setLayoutParams(dialogLp);
		
		int height = (int) (dm.heightPixels - dm.density*95);
		int width = (int) (dm.widthPixels - dm.density*100);
				
		LinearLayout.LayoutParams ll =  (LayoutParams) layout.getLayoutParams();
		ll.height = height;
		layout.setLayoutParams(ll);
		stopUpload();
		loadBitmap(disPalyImageView, imageInfo, height, width);		
	}
	
	private void loadBitmap(ImageView disPalyImageView,ImageInfo imageInfo,double drawMaxHeight,double drawMaxWidth){
		if (imageInfo == null) return;
		Bitmap initBitmap = null;
		try {
			int downsample;
			String displayPath = imageInfo.originalUrl;			
			if (displayPath == null) {				
				if (imageInfo.isfromNative) {
					initBitmap = ImageUtil.getThumbnail(mContext.getContentResolver(), Integer.valueOf(imageInfo.id));
				}else if (imageInfo.thumbnailUrl != null) {						
					initBitmap = BitmapFactory.decodeFile(imageInfo.thumbnailUrl);
				}		
				disPalyImageView.setImageBitmap(initBitmap);	
				return ;
			}			
			Bitmap mBitmap = null;
			double drawHeight = drawMaxHeight;
			double drawWidth = drawMaxWidth;	
			try {
				BitmapFactory.Options options = new Options();
				options.inJustDecodeBounds = true;							
				BitmapFactory.decodeFile(displayPath, options);
				int origW = options.outWidth;
				int origH = options.outHeight;
											
				if (origW > origH) {
					drawHeight = drawWidth*origH/origW;
					if (drawHeight > drawMaxHeight) {
						drawHeight = drawMaxHeight;	
						drawWidth = drawMaxHeight*origW/origH;
					}
				}else {
					drawWidth = drawMaxHeight*origW/origH;
				}
				
				int sampleSizeH = (int) Math.ceil((origH * 1.0)/ (drawHeight * 1.0));								
				int sampleSizeW = (int) Math.ceil((origW * 1.0)/ (drawWidth * 1.0));					
				if (sampleSizeH > sampleSizeW) {
					downsample = sampleSizeH;
				} else {
					downsample = sampleSizeW;
				}
								
				options.inJustDecodeBounds = false;
				options.inSampleSize = downsample;
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				mBitmap = BitmapFactory.decodeFile(displayPath, options);
			}catch (OutOfMemoryError oom) {
    	    	Log.e("loadBitmap", oom);
    	    	mBitmap = null;
    	    	System.gc();
			}	
			if (mBitmap == null) {				
				if (imageInfo.isfromNative) {
					initBitmap = ImageUtil.getThumbnail(mContext.getContentResolver(), Integer.valueOf(imageInfo.id));
				}else if (imageInfo.thumbnailUrl != null) {						
					initBitmap = BitmapFactory.decodeFile(imageInfo.thumbnailUrl);
				}		
				disPalyImageView.setImageBitmap(initBitmap);	
				return;
			}			
			int rotate = ImageUtil.getDegreesExifOrientation(displayPath);  
			if(rotate > 0 && mBitmap != null) {   
				Bitmap rotateBitmap = ImageUtil.rotateBitmap(mBitmap,rotate);   
	            if(rotateBitmap != null) {   
	            	mBitmap.recycle();   
	            	mBitmap = rotateBitmap;   
	            }             
			}
			int height = mBitmap.getHeight();
            int width = mBitmap.getWidth();
					
			double scaleHeight = (drawHeight*1.0)/ (height*1.0);
			double scaleWidth = (drawWidth*1.0)/ (width*1.0);
			if (scaleHeight < scaleWidth) {
				initBitmap = Bitmap.createScaledBitmap(mBitmap,(int) (width*scaleHeight),(int) (height*scaleHeight), true);
			} else {
				initBitmap = Bitmap.createScaledBitmap(mBitmap,(int) (width*scaleWidth),(int) (height*scaleWidth), true);
			}
			mBitmap.recycle();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		disPalyImageView.setImageBitmap(initBitmap);		
	}
	
	private void stopUpload(){
		PictureUploadService.mTerminated = true;
	}

	@Override
	public void onClick(View v) {		
		baseActivity.isHaveUploadErrorDialog = false;
		if(v.getId()==R.id.start_button){		
			baseActivity.startOver();		
		}else if(v.getId()==R.id.retry_button){					
			ShoppingCartUtil.judgeImageUpload(mContext, false, false);	
			ShoppingCartUtil.judgeImageUpload(mContext, false, true);
		}
		dialog.dismiss();		
	}
		
}
