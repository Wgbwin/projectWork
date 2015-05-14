package com.kodak.kodak_kioskconnect_n2r;

/*
 * The content of this file is (c) 2003 - 2011 dmc digital media center GmbH. All rights reserved.
 * 
 * This software is the confidential and proprietary information of dmc digital media center GmbH.
 */
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.R;

/**
 * Custom info dialog.
 * 
 * @author horlaste
 */
public class CouponsEditTestInputDialog extends Dialog
{
	private static EditText editTextInput;
	/**
	 * Constructs a new info dialog.
	 * 
	 * @param context
	 *            The context.
	 * @param theme
	 *            The theme id.
	 */
	public CouponsEditTestInputDialog(Context context, int theme)
	{
		super(context, theme);
	}

	/**
	 * Constructs a new info dialog.
	 * 
	 * @param context
	 *            The context.
	 */
	public CouponsEditTestInputDialog(Context context)
	{
		super(context);
	}

	@Override
	public void setCancelable(boolean cancel)
	{
		super.setCancelable(cancel);
	}

	/**
	 * Helper class for creating a custom dialog.
	 */
	public static class EditTestInputDialogBuilder
	{
		private Context context;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private boolean cancelable = true;
		private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;
		boolean ifFistTimeClick = true;
		private int maxLines = 2;

		/**
		 * Constructs a new info dialog builder.
		 * 
		 * @param context
		 *            The context.
		 */
		public EditTestInputDialogBuilder(Context context,int maxLines)
		{
			this.context = context;
			this.maxLines = maxLines;
		}

		/**
		 * Set the Dialog message from String.
		 * 
		 * @param message
		 *            The message.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setMessage(String message)
		{
			this.message = message;
			return this;
		}

		/**
		 * Set the Dialog message from resource.
		 * 
		 * @param message
		 *            The message resource id.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setMessage(int message)
		{
			this.message = (String) context.getText(message);
			return this;
		}

		/**
		 * Set the Dialog title from resource.
		 * 
		 * @param title
		 *            The title resource id.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setTitle(int title)
		{
			this.title = (String) context.getText(title);
			return this;
		}

		/**
		 * Set the Dialog title from String.
		 * 
		 * @param title
		 *            The title.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setTitle(String title)
		{
			this.title = title;
			return this;
		}

		public EditTestInputDialogBuilder setCancelable(boolean bool)
		{
			cancelable = bool;
			return this;
		}

		/**
		 * Set the positive button resource and it's listener.
		 * 
		 * @param positiveButtonText
		 *            The positive button resource id text.
		 * @param listener
		 *            The on click listener.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = (String) context.getText(positiveButtonText);
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the positive button text and it's listener.
		 * 
		 * @param positiveButtonText
		 *            The positive button text.
		 * @param listener
		 *            The on click listener.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener)
		{
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = listener;
			return this;
		}

		/**
		 * Set the negative button resource and it's listener.
		 * 
		 * @param negativeButtonText
		 *            The negative button text resource id.
		 * @param listener
		 *            The on click listener.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = (String) context.getText(negativeButtonText);
			this.negativeButtonClickListener = listener;
			return this;
		}
		
		
		/**
		 * Set the negative button text and it's listener.
		 * 
		 * @param negativeButtonText
		 *            The negative button text.
		 * @param listener
		 *            The on click listener.
		 * 
		 * @return The info dialog builder.
		 */
		public EditTestInputDialogBuilder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener)
		{
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = listener;
			return this;
		}
		
		
		/**
		 * Create the custom dialog.
		 * 
		 * @return The info dialog.
		 */
		public CouponsEditTestInputDialog create()
		{	
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// instantiate the dialog with the custom Theme
			final CouponsEditTestInputDialog dialog = new CouponsEditTestInputDialog(context, R.style.Dialog);
			View layout = inflater.inflate(R.layout.coupons_edit_input, null);
			editTextInput = ((EditText) layout.findViewById(R.id.editTextInputCoupons));
			AppContext.getApplication().setEmojiFilter(editTextInput);
			showSoftInput();
			dialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			dialog.setCancelable(cancelable);			 
			RelativeLayout navigationbar = (RelativeLayout)layout.findViewById(R.id.navigationbar);
			TextView tvTitle = (TextView)layout.findViewById(R.id.headerBarText);
			navigationbar.setBackgroundColor(Color.WHITE);
			
			Button positiveButton = ((Button) layout.findViewById(R.id.nextButton));
			Button negativeButton = ((Button) layout.findViewById(R.id.backButton));
			editTextInput.setMaxLines(maxLines);
			tvTitle.setText(title);
			editTextInput.setText(message);
			editTextInput.setCursorVisible(true);
			editTextInput.setSelection(message.length());
			if (positiveButtonText != null)
			{
				positiveButton.setText(positiveButtonText);
				if (positiveButtonClickListener != null)
				{
					positiveButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							hiddenSoftInput();
							PrintHelper.editTextForGreetingCard = editTextInput.getText().toString();
							positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
						}
					});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				positiveButton.setVisibility(View.GONE);
			}
			// set the cancel button
			if (negativeButtonText != null && !negativeButtonText.equals(""))
			{
				negativeButton.setText(negativeButtonText);
				if (negativeButtonClickListener != null)
				{
					negativeButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							hiddenSoftInput();
							negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
						}
					});
				}
			}
			else
			{
				// if no confirm button just set the visibility to GONE
				negativeButton.setVisibility(View.GONE);
			}
			dialog.setContentView(layout);
			return dialog;
		}
		
		private void showSoftInput(){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.showSoftInput(editTextInput,
							InputMethodManager.SHOW_FORCED);
				}
			}).start();
		}
		
		private void hiddenSoftInput(){			
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editTextInput.getWindowToken(), 0);

		}
	}
}