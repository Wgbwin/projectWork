package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.widget.Spinner;

public class ProductSizeDropDown extends Spinner
{
	String[] sizes;
	public Context mContext = null;

	public ProductSizeDropDown(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	public boolean performClick()
	{
		boolean handled = false;
		if (!handled)
		{
			handled = true;
			/*
			 * CustomSpinnerDialog dialog = new CustomSpinnerDialog(mContext,
			 * (ListAdapter) getAdapter(), this, R.style.FullHeightDialog);
			 * dialog
			 * .setDialogTitle(mContext.getResources().getString((R.string.
			 * my_dialog_text))); dialog.show();
			 */
		}
		return handled;
	}
}
