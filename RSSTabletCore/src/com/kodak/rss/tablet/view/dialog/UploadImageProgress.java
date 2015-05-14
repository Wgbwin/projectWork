package com.kodak.rss.tablet.view.dialog;

import java.text.DecimalFormat;

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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;

public class UploadImageProgress {

	private Context mContext;
	private View dialogView;
	public Dialog dialog;
	private TextView promptTxt;
	private ImageView uploadImageView;
	private TextView messageTxt;
	private ProgressBar progressBar;
	
	private int dialogLpWidth;	
	private DecimalFormat dFormat;
	private int fileTotalSize;
	private int downLoadFileSize;
	private ImageInfo pBImageinfo;
	private Bitmap bitmap  = null;

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
					progressBar.setMax(fileTotalSize);
				case 1:					
					if (pBImageinfo !=null) {
						if (pBImageinfo.isfromNative) {
							bitmap  = ImageUtil.getThumbnail(mContext.getContentResolver(), Integer.valueOf(pBImageinfo.id));
						}else if (pBImageinfo.thumbnailUrl != null) {							
							bitmap = BitmapFactory.decodeFile(pBImageinfo.thumbnailUrl);
						}
					}					
					uploadImageView.setImageBitmap(bitmap);
					
					progressBar.setProgress(downLoadFileSize);					
					break;
				case 2:
					if (dialog != null) {
						dialog.dismiss();
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
	
	public UploadImageProgress(Context context, String promptTitle, String message){
		this.mContext = context;
		initProgress(promptTitle, message);
	}

	
	public void initProgress(String promptTitle, String message){			
		LayoutInflater inflater = LayoutInflater.from(mContext);
		dialogView = inflater.inflate(R.layout.dialog_upload_image, null);	
		
		promptTxt = (TextView) dialogView.findViewById(R.id.prompt);
		uploadImageView = (ImageView) dialogView.findViewById(R.id.upload_image);
		messageTxt = (TextView) dialogView.findViewById(R.id.message);
		progressBar = (ProgressBar) dialogView.findViewById(R.id.progressbar);
		
		promptTxt.setText(promptTitle);
		messageTxt.setText(message);
		
		dFormat = (DecimalFormat) DecimalFormat.getInstance();
		dFormat.applyPattern("#0.0");
				
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
		if (flag == 1) {
			pBImageinfo = imageinfo;
			downLoadFileSize = downloadSize;			
		} else if (flag == 0) {
			fileTotalSize = downloadSize;			
		}		
		Message msg = new Message();
		msg.what = flag;
		handler.sendMessage(msg);
		
	}
	
	public void setFinish() {
		progressBar.setProgress(fileTotalSize);
	}
	
}
