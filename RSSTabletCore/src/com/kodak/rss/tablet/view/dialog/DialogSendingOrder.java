package com.kodak.rss.tablet.view.dialog;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.thread.SendingOrderTask.SendingOrderTaskListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 
 * @author Kane
 *
 */
public class DialogSendingOrder implements OnClickListener{
	
	private Context mContext;
	private Dialog dialog;
	private View contentView;
	private ImageView ivUploadingImage;
	private TextView tvProgress;
	private ProgressBar pbProgress;
	private Button btCancel;
	
	private int totalImageNumber;
	private Bitmap bitmap;
	
	private SendingOrderTaskListener mListener;
	
	/**
	 * state uploading images<P>
	 * when using it in Handler, should send uri as obj, number of image as arg1
	 */
	public static final int STATE_UPLOADING = 1;
	/**
	 * state sending order
	 * when using it in Handler, step as arg1
	 */
	public static final int STATE_SENDING_ORDER = 2;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			int action = msg.what;
			int step = 0;
			String strState = "";
			updateContentViewVisibility(action);
			switch (action) {
			case STATE_UPLOADING:
				ImageInfo image = (ImageInfo) msg.obj;
				int uploadedImageNumber = step = msg.arg1;
				
				if (image !=null) {
					if (image.isfromNative) {
						bitmap  = ImageUtil.getThumbnail(mContext.getContentResolver(), Integer.valueOf(image.id));
					}else if (image.thumbnailUrl != null) {						
						bitmap = BitmapFactory.decodeFile(image.thumbnailUrl);
					}						
					ivUploadingImage.setImageBitmap(bitmap);
				}
				strState = mContext.getString(R.string.N2RUpload_SendingProgress);
				strState = strState.replaceFirst("%%", "" + (uploadedImageNumber+1));
				strState = strState.replaceFirst("%%", "" + totalImageNumber);
				break;
			case STATE_SENDING_ORDER:
				if(bitmap!=null && !bitmap.isRecycled()){
					bitmap.recycle();
				}
				step = msg.arg1 + 1;
				strState = mContext.getString(R.string.N2RUpload_SendingOrder);
				break;
			}
			pbProgress.setProgress(step);
			tvProgress.setText(strState);
		}
		
	};
	
	private void updateContentViewVisibility(int state){
		switch (state) {
		case STATE_UPLOADING:
			ivUploadingImage.setVisibility(View.VISIBLE);
			btCancel.setVisibility(View.VISIBLE);
			break;
		case STATE_SENDING_ORDER:
			ivUploadingImage.setVisibility(View.INVISIBLE);
			btCancel.setVisibility(View.INVISIBLE);
			break;
		}
	}
	
	public DialogSendingOrder(Context context, int totalImageNumber, int totalStep, SendingOrderTaskListener listener){
		this.mContext = context;
		this.totalImageNumber = totalImageNumber;
		this.mListener = listener;
		initDialog(mContext, totalStep);
	}	
	
	private void initDialog(Context context, int totalStep){
		contentView = LayoutInflater.from(context).inflate(R.layout.dialog_sending_order, null);
		ivUploadingImage = (ImageView) contentView.findViewById(R.id.iv_uploadingImage);
		tvProgress = (TextView) contentView.findViewById(R.id.tv_progress);
		pbProgress = (ProgressBar) contentView.findViewById(R.id.pb_progress);
		btCancel = (Button) contentView.findViewById(R.id.bt_cancel);
		
		btCancel.setOnClickListener(this);
		pbProgress.setMax(totalStep);
		
		dialog = new Dialog(context, R.style.SimpleDialogTheme);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		resizeDialog((Activity) context);
		dialog.setContentView(contentView);
		dialog.setCancelable(false);
	}
	
	private void resizeDialog(Activity activity){
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams dialogLp = dialogWindow.getAttributes();
		int size = (int) (activity.getWindowManager().getDefaultDisplay().getHeight() * 0.6);
		dialogLp.width = size;
		dialogLp.height = size;
		dialogWindow.setAttributes(dialogLp);
	}
	
	public boolean show(){
		if(dialog != null){
			dialog.show();
			return true;
		}
		return false;
	}
	
	public boolean isShowing(){
		if(dialog!=null && dialog.isShowing()){
			return true;
		}
		return false;
	}
	
	public void dismiss(){
		if(dialog!=null && dialog.isShowing()){
			dialog.dismiss();
		}
	}
	
	/**
	 * This handler has two action ({@link #STATE_SENDING_ORDER} and {@link #STATE_UPLOADING})
	 * @return
	 * 		handler which could refresh this dialog
	 */
	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == btCancel.getId()){
			mListener.onTaskCanceled();
			dismiss();
		}
		
	}
}
