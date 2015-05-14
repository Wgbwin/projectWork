package com.kodak.rss.tablet.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.thread.UploadImagesSaveProjectTask;
import com.kodak.rss.tablet.util.ShoppingCartUtil;

public class UploadImageProgressForSaveProject {

	private Context mContext;
	private View dialogView;
	public Dialog dialog;
	private TextView promptTxt;
	private TextView  uploadNumTxt;
	private ImageView uploadImageView;
	private Button cancelButton;	
	
	private int dialogLpWidth;		
	private int fileTotalSize;
	private int downLoadFileSize;
	private ImageInfo pBImageinfo;
	private Bitmap bitmap  = null;
	private UploadImagesSaveProjectTask uploadImageSaveProjectTask;
	private String uploadNumStr ;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {			
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {				
				case 0:					
					if (pBImageinfo !=null) {
						if (pBImageinfo.isfromNative) {
							bitmap  = ImageUtil.getThumbnail(mContext.getContentResolver(), Integer.valueOf(pBImageinfo.id));
						}else if (pBImageinfo.thumbnailUrl != null) {							
							bitmap = BitmapFactory.decodeFile(pBImageinfo.thumbnailUrl);
						}
					}					
					uploadImageView.setImageBitmap(bitmap);		
				    String strState="";
					strState = mContext.getString(R.string.N2RUpload_SendingProgress);
					strState = strState.replaceFirst("%%", "" + (downLoadFileSize+1));
					strState = strState.replaceFirst("%%", "" + fileTotalSize);
					
//					uploadNumStr = TextUtil.formatHighlightText(mContext.getString(R.string.N2RUpload_SendingProgress),
//							mContext.getResources().getColor(R.color.white), String.valueOf(downLoadFileSize),String.valueOf(fileTotalSize)).toString();					
					uploadNumTxt.setText(strState);			
					break;
				case 1:
					if (dialog != null) {
						dialog.dismiss();
					}
					break;
				case 2:
					if (dialog != null) {
						dialog.dismiss();
					}
					if (uploadImageSaveProjectTask != null) {
						uploadImageSaveProjectTask.isCancel = true;
						uploadImageSaveProjectTask.cancel(true);
					}					
					break;	
					
				case -1:
					String error = msg.getData().getString("error");					
					break;
				}
			}
			super.handleMessage(msg);
		}
	};
	
	public UploadImageProgressForSaveProject(Context context, String promptTitle, int fileTotalSize,UploadImagesSaveProjectTask uploadImageSaveProjectTask){
		this.mContext = context;
		this.uploadImageSaveProjectTask = uploadImageSaveProjectTask;
		ShoppingCartUtil.judgeImageDownload(context,true,true);
		initProgress(promptTitle, fileTotalSize);
	}
	
	public void initProgress(String promptTitle, int fileTotalSize){			
		LayoutInflater inflater = LayoutInflater.from(mContext);
		dialogView = inflater.inflate(R.layout.dialog_upload_image_save_project, null);	
		
		promptTxt = (TextView) dialogView.findViewById(R.id.prompt);
		uploadImageView = (ImageView) dialogView.findViewById(R.id.upload_image);
		uploadNumTxt  = (TextView) dialogView.findViewById(R.id.uploadNum);		
		cancelButton = (Button) dialogView.findViewById(R.id.cancel_button);
		
		cancelButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				msg.what = 2;
				handler.sendMessage(msg);				
			}
		});		

		promptTxt.setText(promptTitle);
		this.fileTotalSize = fileTotalSize;		
		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(mContext);
		dialog = dialogbuilder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		dialog.getWindow().setContentView(dialogView);
		ViewGroup.LayoutParams dialogLp = dialogView.getLayoutParams();
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();			
		if (dm.heightPixels < 600) {
			dialogLp.height = dm.heightPixels*3/4;
			dialogLpWidth = dm.heightPixels;			
		}else {
			dialogLp.height = dm.heightPixels/2;
			dialogLpWidth = dm.widthPixels/2;			
		}
		dialogLp.width = dialogLpWidth;
		dialogView.setLayoutParams(dialogLp);	

	}
	
	public void refresh(int downloadSize, ImageInfo imageinfo ,int flag ){
		if (flag == 0) {
			pBImageinfo = imageinfo;
			downLoadFileSize = downloadSize;			
		} 	
		Message msg = new Message();
		msg.what = flag;
		handler.sendMessage(msg);
		
	}

}
