package com.kodak.rss.tablet.view.dialog;

import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.R.color;
import com.kodak.rss.tablet.RssTabletApp;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

public class DialogConfirmDestination extends SimpleDialog {
	
	private Button btYes;
	private Button btNo;
	
	private TextView tvDetailTop;
	private TextView tvDetailBottom;
	
	private String detailTop = "";
	private String detailBottom = "";

	private DialogConfirmDestination(Context context, int resource) {
		super(context, resource);
		btYes = (Button) contentView.findViewById(R.id.bt_yes);
		btNo = (Button) contentView.findViewById(R.id.bt_no);
		tvDetailTop = (TextView) contentView.findViewById(R.id.tv_detail_top);
		tvDetailBottom = (TextView) contentView.findViewById(R.id.tv_detail_bottom);
		setHeight(0.6f);
		setWidth(0.4f);
	}
	
	public DialogConfirmDestination(Context context, int resource, StoreInfo store){
		this(context, resource);
		detailTop = store.name;
		detailBottom = store.address.address1 + "\n";
		if(store.address.address2!=null && !store.address.address2.equals("")){
			detailBottom += store.address.address2 + "\n";
		}
		String countryCode = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
		if("DE".equalsIgnoreCase(countryCode)){
			detailBottom += store.address.postalCode + " " + store.address.city + "\n";
		} else {
			detailBottom += store.address.city + ", " + store.address.stateProvince + "\n";
		}
		detailBottom += store.address.postalCode;
		tvDetailTop.setTextColor(context.getResources().getColor(R.color.yellow));
		setDetails();
	}
	
	public DialogConfirmDestination(Context context, int resource, LocalCustomerInfo customer){
		this(context, resource);
		detailTop = customer.getShipFirstName() + " " + customer.getShipLastName();
		detailBottom += customer.getShipAddress1() + "\n";
		if(!customer.getShipAddress2().equals("")){
			detailBottom += customer.getShipAddress2() + "\n";
		}
		String countryCode = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
		if("DE".equalsIgnoreCase(countryCode)){
			detailBottom += customer.getShipZip() + " " + customer.getShipCity() + "\n";
		} else {
			detailBottom += customer.getShipCity() + ", " + customer.getShipState() + "\n";
		}
		detailBottom += customer.getShipZip() + "\n";
		setDetails();
	}
	
	private void setDetails(){
		tvDetailTop.setText(detailTop);
		tvDetailBottom.setText(detailBottom);
	}

	@Override
	public boolean isDialogCancelable() {
		return false;
	}

	@Override
	public boolean isDialogCanceledOnTouchOutside() {
		return false;
	}

	public Button getBtYes() {
		return btYes;
	}

	public Button getBtNo() {
		return btNo;
	}
	
	
	
}
