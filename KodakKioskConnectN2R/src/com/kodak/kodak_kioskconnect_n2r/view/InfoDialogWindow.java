package com.kodak.kodak_kioskconnect_n2r.view;

import com.kodak.kodak_kioskconnect_n2r.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A common dialog with builder.
 * It is similar to AlertDialog
 * @author Robin.Qian
 */
public class InfoDialogWindow extends Dialog{
	protected int width = WindowManager.LayoutParams.WRAP_CONTENT;
	protected int height = WindowManager.LayoutParams.WRAP_CONTENT;
    private TextView tvMessage ;
	protected InfoDialogWindow(Context context) {
		super(context);
	}
	

	protected InfoDialogWindow(Context context, int theme) {
		super(context, theme);
	}


	protected InfoDialogWindow(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}
	
	public void setMessage(int resid){
		tvMessage.setText(resid) ;
	}
	
	public void setMessage(CharSequence text){
		tvMessage.setText(text) ;
	}
	
	public void setMessageTextView(TextView textView){
		tvMessage = textView ;
	}
	
	@Override
	public void show() {
		super.show();
		
		if(width != WindowManager.LayoutParams.WRAP_CONTENT || height != WindowManager.LayoutParams.WRAP_CONTENT){
			getWindow().setLayout(width, height);
		}
	}
	
	/**
	 * The builder for InfoDialog.
	 * default cancelbale: false.  canceledOnTouchOutside:false
	 * The method is similar to AlertDialog.Builder.
	 * the component is hidden unless you do setXXX(...)
	 *  
	 * @author Robin.Qian
	 *
	 */
	public static class Builder{
		private Context context;
		private CharSequence message;
		private CharSequence title;
		private CharSequence positiveButtonText;
		private CharSequence negativeButtonText;
		private CharSequence neturalButtonText;
		private int width = WindowManager.LayoutParams.WRAP_CONTENT;
		private int height = WindowManager.LayoutParams.WRAP_CONTENT;
		private Bitmap bitmap;
		private boolean showProgressBar;
		private DialogInterface.OnClickListener positveButtonListener,negativeButtonListener,neturalButtonListener;
		private boolean cancelable = false;
		private boolean canceledOnTouchOutside = false;
		
		private int maxHeight;
		private int maxWidth;
		
		public Builder(Context context){
			this.context = context;
			
			//get the screen width and height
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			maxHeight = display.getHeight();
			maxWidth = display.getWidth();
		}
		
		public Builder setTitle(CharSequence title){
			this.title = title;
			return this;
		}
		
		public Builder setTitle(int titleId){
			this.title = context.getText(titleId);
			return this;
		}
		
		public Builder setMessage(CharSequence message){
			this.message = message;
			return this;
		}
		
		public Builder setMessage(int messageId){
			this.message = context.getText(messageId);
			return this;
		}
		
		/**
		 * 
		 * @param text 
		 * @param onClickListener 
		 * @return
		 */
		public Builder setPositiveButton(CharSequence text,OnClickListener onClickListener){
			this.positiveButtonText = text;
			this.positveButtonListener = onClickListener;
			return this;
		}
		
		/**
		 * @param textId
		 * @param onClickListener 
		 * @return
		 */
		public Builder setPositiveButton(int textId,OnClickListener onClickListener){
			return setPositiveButton(context.getText(textId), onClickListener);
		}
		
		/**
		 * @param text
		 * @param onClickListener 
		 * @return
		 */
		public Builder setNegativeButton(CharSequence text,OnClickListener onClickListener){
			this.negativeButtonText = text;
			this.negativeButtonListener = onClickListener;
			return this;
		}
		
		/**
		 * @param textId
		 * @param onClickListener 
		 * @return
		 */
		public Builder setNegativeButton(int textId,OnClickListener onClickListener){
			return setNegativeButton(context.getText(textId), onClickListener);
		}
		
		/**
		 * @param text
		 * @param onClickListener 
		 * @return
		 */
		public Builder setNeturalButton(CharSequence text,OnClickListener onClickListener){
			this.neturalButtonText = text;
			this.neturalButtonListener = onClickListener;
			return this;
		}
		
		/**
		 * @param textId
		 * @param onClickListener 
		 * @return
		 */
		public Builder setNeturalButton(int textId,OnClickListener onClickListener){
			return setNeturalButton(context.getText(textId), onClickListener);
		}
		
		public Builder setCancelable(boolean cancelable) {
			this.cancelable = cancelable;
			return this;
		}
		
		public Builder setCanceledOnTouchOutside(boolean canceledOnTouchOutside){
			this.canceledOnTouchOutside = canceledOnTouchOutside;
			return this;
		}
		
		public Builder setProgressBar(boolean show){
			this.showProgressBar = show;
			return this;
		}
		
		public Builder setImage(Bitmap bitmap){
			this.bitmap = bitmap;
			return this;
		}
		
		public Builder setImage(int resid){
			this.bitmap = BitmapFactory.decodeResource(context.getResources(), resid);
			return this;
		}
		
		/**
		 * set the dialog width
		 * if you don't set this value, the default is wrap_content(detail: see info_dialog.xml)
		 * @param percent 0-1, 0 means wrap_content, 1 means match_prent, 0.XX means XX % in the activity
		 * @return
		 */
		public Builder setWidth(float percent){
			if(percent >= 1){
				width = LayoutParams.MATCH_PARENT;
			}else if(percent<=0){
				width = LayoutParams.WRAP_CONTENT;
			}else{
				width = (int) (maxWidth * percent);
			}
			return this;
		}
		
		/**
		 * set the dialog height
		 * if you don't set this value, the default is wrap_content(detail: see info_dialog.xml)
		 * @param percent 0-1, 0 means wrap_content, 1 means match_prent, 0.XX means XX % percent in the activity
		 * @return
		 */
		public Builder setHeight(float percent){
			if(percent >= 1){
				height = LayoutParams.MATCH_PARENT;
			}else if(percent<=0){
				height = LayoutParams.WRAP_CONTENT;
			}else{
				height = (int) (maxHeight * percent);
			} 
			return this;
		}
		
		public InfoDialogWindow create(){
			final InfoDialogWindow dialog = new InfoDialogWindow(context, R.style.SimpleDialogTheme);
			View v = LayoutInflater.from(context).inflate(R.layout.info_dialog_window,null);
			
			if(title != null){
				TextView tvTitle = (TextView) v.findViewById(R.id.info_dialog_title);
				tvTitle.setText(title);
				tvTitle.setVisibility(View.VISIBLE);
			}
			
			if(message != null){
				TextView tvMessage = (TextView) v.findViewById(R.id.info_dialog_message);
				dialog.setMessageTextView(tvMessage) ;
				tvMessage.setText(message);
				tvMessage.setVisibility(View.VISIBLE);
			}
			
			if(positiveButtonText != null){
				Button btnPositive = (Button) v.findViewById(R.id.info_dialog_positive_button);
				btnPositive.setText(positiveButtonText);
				
				btnPositive.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(positveButtonListener != null){
							positveButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
						dialog.dismiss();
					}
				});
					
				v.findViewById(R.id.info_dialog_positive_layout).setVisibility(View.VISIBLE);
			}
			
			if(showProgressBar){
				ProgressBar pb = (ProgressBar) v.findViewById(R.id.info_dialog_progressbar);
				pb.setVisibility(View.VISIBLE);
			}
			
			if(bitmap != null){
				ImageView iv = (ImageView) v.findViewById(R.id.info_dialog_image);
				iv.setImageBitmap(bitmap);
				iv.setVisibility(View.VISIBLE);
			}
			
			if(negativeButtonText != null){
				Button btnNegative = (Button) v.findViewById(R.id.info_dialog_negative_button);
				btnNegative.setText(negativeButtonText);
				
				btnNegative.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(negativeButtonListener != null){
							negativeButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
						dialog.dismiss();
					}
				});
					
				v.findViewById(R.id.info_dialog_negative_layout).setVisibility(View.VISIBLE);
			}
			
			if(neturalButtonText != null){
				Button btnNetural = (Button) v.findViewById(R.id.info_dialog_netural_button);
				btnNetural.setText(neturalButtonText);
				
				btnNetural.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(neturalButtonListener != null){
							neturalButtonListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
						dialog.dismiss();
					}
				});
				
				v.findViewById(R.id.info_dialog_netural_layout).setVisibility(View.VISIBLE);
			}
			
			
			//set dialog size must do dialog.getWindow().setLayout(width, height) after dialog.show()
			//so I set the size in override method dialog.show()
			if(width != WindowManager.LayoutParams.WRAP_CONTENT){
				dialog.width = width;
			}
			if(height != WindowManager.LayoutParams.WRAP_CONTENT){
				dialog.height = height;
			}
			
			dialog.setCancelable(cancelable);
			dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
			dialog.setContentView(v);
			
			return dialog;
		}
		
	}
	
}
