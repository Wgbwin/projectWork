package com.kodakalaris.kodakmomentslib.activity.shoppingcart;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.AppManager;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.contactdetails.MContactDetailsActivity;
import com.kodakalaris.kodakmomentslib.activity.findstore.MDestinationStoreSelectionActivity;
import com.kodakalaris.kodakmomentslib.activity.home.MHomeActivity;
import com.kodakalaris.kodakmomentslib.activity.printsreview.MPrintsReviewActivity;
import com.kodakalaris.kodakmomentslib.activity.sendingorder.MSendingOrderActivity;
import com.kodakalaris.kodakmomentslib.adapter.mobile.ShoppingCartItemAdapter;
import com.kodakalaris.kodakmomentslib.adapter.mobile.ShoppingCartItemAdapter.OnEditBtnsClickListener;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Retailer;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Discount;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Pricing;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Pricing.LineItem;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.thread.CheckCouponCodeTask;
import com.kodakalaris.kodakmomentslib.thread.ContinueShoppingTask;
import com.kodakalaris.kodakmomentslib.thread.SetCustomerInfoTask;
import com.kodakalaris.kodakmomentslib.thread.SyncUpCartTask;
import com.kodakalaris.kodakmomentslib.util.StringUtils;
import com.kodakalaris.kodakmomentslib.util.TextViewUtil;
import com.kodakalaris.kodakmomentslib.widget.BaseGeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.LinearListLayout;
import com.kodakalaris.kodakmomentslib.widget.WaitingDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.ConfirmOrderDialog;
import com.kodakalaris.kodakmomentslib.widget.mobile.GeneralAlertDialogFragment;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;
import com.kodakalaris.kodakmomentslib.widget.mobile.TipBar;

public class MShoppingCartActivity extends BaseShoppingCartActivity {
	private static final String TAG = "MShoppingCartActivity";
	
	private LinearListLayout vLisvItems;
	private ShoppingCartItemAdapter mAdapterItems;
	private MActionBar vActionbar;
	private TextView vTxtApplyPromoCode;
	private EditText vEtxtPromoCode;
	private TextView vTxtDeliveryMethod;
	private TextView vTxtDeliveryMethodInAddress;
	private TextView vTxtDeliveryAddress;
	private TextView vTxtPersonalDetails;
	private TextView vTxtShippingHandling;
	private Button vBtnCancelOrder;
	private Button vBtnConfirmOrder;
	private TextView vTxtPriceOrderSubtotal;
	private TextView vTxtPriceShippingHandling;
	private TextView vTxtPricePromoSaving;
	private ViewGroup vVgroupPriceOrderSubtotal;
	private ViewGroup vVgroupPricePromoSaving;
	private ViewGroup vVgroupPriceShippingHandling;
	private LinearLayout vLinelyPromoCodeNotValidPrompts;
	private LinearLayout vLinelyPromoCodeValidPrompts;
	private LinearLayout vLinelyShipHandling;
	private LinearLayout vLinelyDeliveryAddressInfoNeeded;
	private LinearLayout vLinelyPersonalInfoNeeded;
	private RelativeLayout vRelalyDeliveryAddressArea;
	private RelativeLayout vRelalyPersonalInfoArea;
	private TipBar vTipBar;
	
	private ShoppingCartManager mCartManager;
	private LocalCustomerInfo mLocalCustomerInfo;
	private boolean mInitTaskSucceed = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_shopping_cart);
		
		initViews();
		initData();
		
		syncUpCart();
	}
	
	private void syncUpCart() {
		final WaitingDialog waitingDialog = new WaitingDialog(this, false);
		waitingDialog.initDialog(R.string.Common_please_wait);
		new SyncUpCartTask(this) {
			
			@Override
			protected void onPreExecute() {
				waitingDialog.show(getSupportFragmentManager(), "SyncUpCart");
				updateConfirmBtn();
			};
			
			@Override
			protected void onFinished(boolean success, WebAPIException e) {
				super.onFinished(success, e);
				waitingDialog.dismiss();
				vTipBar.closeDelay();
				mInitTaskSucceed = success;
				if (!success) {
					e.handleException(MShoppingCartActivity.this);
				} else {
					setDeliveryData();
					setPriceData();
					setPersonalDetailsData();
					
					initEvents();
				}
				updateConfirmBtn();
			}
		}.execute();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		updateData();
	}
	
	private void initViews() {
		vLisvItems = (LinearListLayout) findViewById(R.id.lisv_products);
		vActionbar = (MActionBar) findViewById(R.id.actionbar);
		vTxtApplyPromoCode = (TextView) findViewById(R.id.txt_apply_promo_code);
		vEtxtPromoCode = (EditText) findViewById(R.id.etxt_promo_code);
		vTxtDeliveryMethod = (TextView) findViewById(R.id.txt_delivery_method);
		vTxtDeliveryMethodInAddress = (TextView) findViewById(R.id.txt_delivery_method_in_address);
		vTxtDeliveryAddress = (TextView) findViewById(R.id.txt_delivery_address);
		vTxtPersonalDetails = (TextView) findViewById(R.id.txt_personal_details);
		vTxtShippingHandling = (TextView) findViewById(R.id.txt_shipping_handling);
		vBtnCancelOrder = (Button) findViewById(R.id.btn_cancel_order);
		vBtnConfirmOrder = (Button) findViewById(R.id.btn_confirm_order);
		vTxtPriceOrderSubtotal = (TextView) findViewById(R.id.txt_price_order_subtotal);
		vTxtPriceShippingHandling = (TextView) findViewById(R.id.txt_price_shipping_handling);
		vTxtPricePromoSaving = (TextView) findViewById(R.id.txt_price_promo_saving);
		vVgroupPriceOrderSubtotal = (ViewGroup) findViewById(R.id.vgroup_order_subtotal);
		vVgroupPricePromoSaving = (ViewGroup) findViewById(R.id.vgroup_promo_saving);
		vVgroupPriceShippingHandling = (ViewGroup) findViewById(R.id.vgroup_shipping_handling);
		vLinelyPromoCodeNotValidPrompts = (LinearLayout) findViewById(R.id.linely_pormo_not_valid_prompts);
		vLinelyPromoCodeValidPrompts = (LinearLayout) findViewById(R.id.linely_pormo_valid_prompts);
		vLinelyShipHandling = (LinearLayout) findViewById(R.id.linely_ShippingAndHandling_container);
		vLinelyDeliveryAddressInfoNeeded = (LinearLayout) findViewById(R.id.linely_delivery_address_need_information);
		vLinelyPersonalInfoNeeded = (LinearLayout) findViewById(R.id.linely_personal_details_need_information);
		vRelalyDeliveryAddressArea = (RelativeLayout) findViewById(R.id.relaly_delivery_address_info_area);
		vRelalyPersonalInfoArea = (RelativeLayout) findViewById(R.id.relaly_personal_details_info_area);
		vTipBar = (TipBar) findViewById(R.id.tipbar);
		
		TextViewUtil.addFilters(vEtxtPromoCode, new InputFilter[] {new InputFilter.AllCaps()});
	}
	
	private void initEvents() {
		vActionbar.setOnLeftButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		//continue shopping
		vActionbar.setOnRightButtonClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				GeneralAlertDialogFragment dialog = new GeneralAlertDialogFragment(MShoppingCartActivity.this)
														.setTitle(R.string.ShoppingCart_MakeMore)
														.setMessage(R.string.ShoppingCart_MakeMorePrompts)
														.setNegativeButton(R.string.Common_Cancel, null)
														.setPositiveButton(R.string.Common_Yes, new BaseGeneralAlertDialogFragment.OnClickListener() {
															
															@Override
															public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
																continueShopping();
															}
														});
				dialog.show(getSupportFragmentManager(), "continue shopping");
				
			}
		});
		
		vTxtApplyPromoCode.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String code = vEtxtPromoCode.getText().toString();
				if (StringUtils.isEmpty(code)) {
					return;
				}
				
				final WaitingDialog dialog = new WaitingDialog(MShoppingCartActivity.this, false);
				dialog.initDialog(R.string.Common_please_wait);
				dialog.show(getSupportFragmentManager(), "promo code");
				new CheckCouponCodeTask(MShoppingCartActivity.this, code){

					@Override
					protected void onFinish(boolean success, WebAPIException e) {
						dialog.dismiss();
						if (success) {
							boolean valid = false;
							
							if (mCartManager.getCart().discounts != null) {
								for (int i = 0; i< mCartManager.getCart().discounts.length; i++) {
									if (mCartManager.getCart().discounts[i].status == Discount.Applied) {
										valid = true;
										break;
									}
								}
							}
							
							if (valid) {
								vLinelyPromoCodeNotValidPrompts.setVisibility(View.GONE);
								vLinelyPromoCodeValidPrompts.setVisibility(View.VISIBLE);
							} else {
								vLinelyPromoCodeNotValidPrompts.setVisibility(View.VISIBLE);
								vLinelyPromoCodeValidPrompts.setVisibility(View.GONE);
							}
						} else {
							e.handleException(MShoppingCartActivity.this);
						}
					}
					
				}.execute();
			}
		});
		
		vTxtShippingHandling.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO go delivery screen
				
			}
		});
		
		vRelalyPersonalInfoArea.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MShoppingCartActivity.this, MContactDetailsActivity.class);
				startActivity(intent);
			}
		});
		
		vRelalyDeliveryAddressArea.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MShoppingCartActivity.this, MDestinationStoreSelectionActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		vBtnCancelOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO code for cancel order
				new GeneralAlertDialogFragment(MShoppingCartActivity.this)
					.setTitle(R.string.ShoppingCart_start_over)
					.setMessage(R.string.ShoppingCart_start_over_prompts)
					.setNegativeButton(R.string.Common_Cancel, null)
					.setPositiveButton(R.string.Common_OK, new BaseGeneralAlertDialogFragment.OnClickListener() {
						
						@Override
						public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
							AppManager.getInstance().startOver();
						}
					})
					.show(getSupportFragmentManager(), "");
			}
		});
		
		vBtnConfirmOrder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!checkOrderLimit()) {
					return;
				}
				
				// TODO more code
				new ConfirmOrderDialog(MShoppingCartActivity.this, false)
						.setAddress(getOrderTargetAddress())
						.setPositiveButton(R.string.Common_Yes, new BaseGeneralAlertDialogFragment.OnClickListener() {
							
							@Override
							public void onClick(BaseGeneralAlertDialogFragment dialogFragment, View v) {
								if (((ConfirmOrderDialog) dialogFragment).isCheckedSendEmail()) {
									//TODO 
								}
								
								final WaitingDialog waitingDialog = new WaitingDialog(MShoppingCartActivity.this, false)
																	.initDialog(R.string.Common_please_wait);
								waitingDialog.show(getSupportFragmentManager(), "wait");
								new SetCustomerInfoTask(MShoppingCartActivity.this, mLocalCustomerInfo) {

									@Override
									protected void OnFinished(boolean success,
											WebAPIException exception) {
										waitingDialog.dismiss();
										if (success) {
											Intent intent = new Intent(MShoppingCartActivity.this, MSendingOrderActivity.class);
											startActivity(intent);
											finish();
										} else {
											exception.handleException(MShoppingCartActivity.this);
										}
									}
									
								}.execute();
								
							}
						})
						.setNegativeButton(R.string.Common_Cancel, null)
						.show(getSupportFragmentManager(), "");
				
			}
		});
		
		mAdapterItems.setOnEditBtnsClickListener(new OnEditBtnsClickListener() {
			
			@Override
			public void onEditBtnClick(View v, int Position, LineItem lineItem) {
				Intent intent = new Intent(MShoppingCartActivity.this, MPrintsReviewActivity.class);
				startActivity(intent);
				finish();
			}
			
			@Override
			public void onDeleteBtnClick(View v, int Position, LineItem lineItem) {
				//TODO 
				//Now only print product is available, so there is no delete button
			}
		});
	}
	
	private void initData() {
		mLocalCustomerInfo = new LocalCustomerInfo(this);
		mCartManager = ShoppingCartManager.getInstance();
		
	}
	
	private void updateData() {
		updatePersonalDetailsData();
	}
	
	private void setPriceData() {
		Pricing pricing = mCartManager.getCart().pricing;
		
		mAdapterItems = new ShoppingCartItemAdapter(this, pricing.lineItems);
		vLisvItems.setAdapter(mAdapterItems);
		
		vTxtPriceOrderSubtotal.setText(pricing.grandTotal.priceStr);
		
		if (mCartManager.isShipToHome) {
			String shipPrice = pricing.shipAndHandling.priceStr;
			vVgroupPriceShippingHandling.setVisibility(View.VISIBLE);
			vTxtPriceShippingHandling.setText(shipPrice);
		}
	}
	
	private void setDeliveryData() {
		if (mCartManager.isShipToHome) {
			vTxtDeliveryMethod.setText(R.string.ShoppingCart_DeliveryMethod_ShipToHome);
			vTxtDeliveryMethodInAddress.setText(R.string.ShoppingCart_DeliveryMethod_ShipToHome);
			
			//TODO see checkout work flow document, IOS is TODO too
			vLinelyShipHandling.setVisibility(View.GONE);
			
			if (!mCartManager.isShippingAddressValid(this)) {
				vTxtDeliveryAddress.setVisibility(View.GONE);
				vLinelyDeliveryAddressInfoNeeded.setVisibility(View.VISIBLE);
			} else {
				vTxtDeliveryAddress.setVisibility(View.VISIBLE);
				vLinelyDeliveryAddressInfoNeeded.setVisibility(View.GONE);
				
			}
		} else {
			vTxtDeliveryMethod.setText(R.string.ShoppingCart_DeliveryMethod_PickUpInStore);
			vTxtDeliveryMethodInAddress.setText(R.string.ShoppingCart_DeliveryMethod_PickUpInStore);
			
			vLinelyShipHandling.setVisibility(View.GONE);
		}
		
		vTxtDeliveryAddress.setText(getOrderTargetAddress());
		
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(MShoppingCartActivity.this, MDestinationStoreSelectionActivity.class);
		startActivity(intent);
		finish();
	}
	
	private String getOrderTargetAddress() {
		StringBuilder address = new StringBuilder();
		if (mCartManager.isShipToHome) {
			address.append(mLocalCustomerInfo.getShipFirstName() + " " + mLocalCustomerInfo.getShipLastName());
			address.append("\n");
			address.append(mLocalCustomerInfo.getShipAddress1());
			if (!StringUtils.isEmpty(mLocalCustomerInfo.getShipAddress2())) {
				address.append("\n");
				address.append(mLocalCustomerInfo.getShipAddress2());
			}
			address.append("\n");
			address.append(mLocalCustomerInfo.getShipCity() + "," + mLocalCustomerInfo.getShipState() + " " + mLocalCustomerInfo.getShipZip());
		} else {
			StoreInfo storeInfo = StoreInfo.loadSelectedStore(this);
			if (storeInfo != null && storeInfo.address != null) {
				if (StringUtils.isNotEmpty(storeInfo.name)) {
					address.append(storeInfo.name);
					address.append("\n");
				}
				address.append(storeInfo.address.address1);
				if (StringUtils.isNotEmpty(storeInfo.address.address2)) {
					address.append("\n");
					address.append(storeInfo.address.address2);
				}
				if (StringUtils.isNotEmpty(storeInfo.address.address3)) {
					address.append("\n");
					address.append(storeInfo.address.address3);
				}
				
				address.append("\n");
				address.append(storeInfo.address.city + "," + storeInfo.address.stateProvince + " " + storeInfo.address.postalCode);
			}
		}
		
		return address.toString();
	}
	
	private void setPersonalDetailsData() {
		if (!mCartManager.isCustomerInfoValid(this, mCartManager.getCurrentRetailer())) {
			vLinelyPersonalInfoNeeded.setVisibility(View.VISIBLE);
			vTxtPersonalDetails.setVisibility(View.GONE);
			
		} else {
			vLinelyPersonalInfoNeeded.setVisibility(View.GONE);
			vTxtPersonalDetails.setVisibility(View.VISIBLE);
			
			StringBuilder sb = new StringBuilder();
			sb.append(mLocalCustomerInfo.getCusFirstName() + " " + mLocalCustomerInfo.getCusLastName());
			if (!StringUtils.isEmpty(mLocalCustomerInfo.getCusEmail())) {
				sb.append("\n");
				sb.append(mLocalCustomerInfo.getCusEmail());
			}
			
			if (!StringUtils.isEmpty(mLocalCustomerInfo.getCusPhone())) {
				sb.append("\n");
				sb.append(mLocalCustomerInfo.getCusPhone());
			}
			vTxtPersonalDetails.setText(sb.toString());
		}
		
	}
	
	private void updatePersonalDetailsData() {
		mLocalCustomerInfo = new LocalCustomerInfo(this);
		
		if (mCartManager.getCurrentRetailer() != null) {
			setPersonalDetailsData();
			updateConfirmBtn();
		}
	}
	
	private boolean checkOrderLimit() {
		double orderTotelPrice = mCartManager.getCart().pricing.grandTotal.price;
		if (orderTotelPrice <= 0) {
			return true;
		}
		Retailer retailer = mCartManager.getCurrentRetailer();
		if (retailer == null || StringUtils.isEmpty(retailer.id)) {
			return true;
		}
		
		//check order maximum cost
		if (retailer.cartLimit != null) {
			float priceLimit = 	retailer.cartLimit.price;
			if (priceLimit != 0 && orderTotelPrice > priceLimit) {
				String prompt = getResources().getString(R.string.ShoppingCart_TooExpensive, retailer.cartLimit.PriceStr);
				
				new GeneralAlertDialogFragment(this)
					.setMessage(prompt)
					.setPositiveButton(R.string.Common_OK, null)
					.show(getSupportFragmentManager(), "cartLimit");
				return false;
			}
			
		}
		
		//check order minimum cost
		if (retailer.cartMinimumLimit != null) {
			float priceMinimumLimit = 	retailer.cartMinimumLimit.price;
			if (priceMinimumLimit != 0 && orderTotelPrice < priceMinimumLimit) {					
				String prompt = getResources().getString(R.string.ShoppingCart_NotEnoughOrdered, retailer.cartMinimumLimit.PriceStr);
				new GeneralAlertDialogFragment(this)
				.setMessage(prompt)
				.setPositiveButton(R.string.Common_OK, null)
				.show(getSupportFragmentManager(), "cartMinimumLimit");
				return false;
			}
		}	
			
		return true;
	}
	
	private boolean isAllInfoValid() {
		if (!mInitTaskSucceed) {
			return false; 
		}
		
		if (mCartManager.isShipToHome && !mCartManager.isShippingAddressValid(this)) {
			return false;
		}
		
		if (!mCartManager.isCustomerInfoValid(this, mCartManager.getCurrentRetailer())) {
			return false;
		}
		
		return true;
	}
	
	private void updateConfirmBtn() {
		vBtnConfirmOrder.setEnabled(isAllInfoValid());
	}
	
	private void continueShopping() {
		final WaitingDialog waitingDialog = new WaitingDialog(this, false, R.string.Common_please_wait);
		waitingDialog.show(getSupportFragmentManager(), "get catalog for continue shopping");
		
		new ContinueShoppingTask(this) {
			
			@Override
			protected void onFinished(boolean success, WebAPIException e) {
				if (isFinishing()) return;
				
				waitingDialog.dismiss();
				
				if (success) {
					ShoppingCartManager.getInstance().setInDoMoreMode(true);
					Intent intent = new Intent(MShoppingCartActivity.this, MHomeActivity.class);
					startActivity(intent);
					finish();
				} else {
					e.handleException(MShoppingCartActivity.this);
				}
			};
		}.execute();
	}
	
}
