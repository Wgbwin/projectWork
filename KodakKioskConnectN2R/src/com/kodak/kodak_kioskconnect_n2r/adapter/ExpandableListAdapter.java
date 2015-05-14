package com.kodak.kodak_kioskconnect_n2r.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.AppConstants;
import com.AppConstants.FlowType;
import com.AppContext;
import com.AppManager;
import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageResizer;
import com.example.android.bitmapfun.util.ImageWorker.ImageWorkerAdapter;
import com.kodak.flip.PhotoBookPage;
import com.kodak.kodak_kioskconnect_n2r.CartItem;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardProductActivity;
import com.kodak.kodak_kioskconnect_n2r.HelpActivity;
import com.kodak.kodak_kioskconnect_n2r.ImageSelectionDatabase;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.NewSettingActivity;
import com.kodak.kodak_kioskconnect_n2r.OrderSummaryWidget;
import com.kodak.kodak_kioskconnect_n2r.Pricing;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintMakerWebService;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.QuickBookFlipperActivity;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.ROI;
import com.kodak.kodak_kioskconnect_n2r.ShoppingCartHeaderItem;
import com.kodak.kodak_kioskconnect_n2r.ShoppingCartItem;
import com.kodak.kodak_kioskconnect_n2r.SingleImageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.StoreFinder;
import com.kodak.kodak_kioskconnect_n2r.TopOrderHeadSummaryWidget;
import com.kodak.kodak_kioskconnect_n2r.activity.CollageEditActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.MainMenu;
import com.kodak.kodak_kioskconnect_n2r.activity.ShoppingCartActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.Discount;
import com.kodak.kodak_kioskconnect_n2r.bean.PrintInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.ProductInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.bean.retailer.CountryInfo;
import com.kodak.kodak_kioskconnect_n2r.bean.shoppingcart.Cart;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardProduct;
import com.kodak.utils.ImageUtil;
import com.kodak.utils.PhotobookUtil;

@TargetApi(Build.VERSION_CODES.ECLAIR)
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	private String retailerID = "";
	private String cartId = "";
	public static String productsStrCheckStore = "";
	private static final String IMAGE_CACHE_DIR = "shoppingcartimages";
	private final String SCREEN_NAME = "Shopping Cart";
	// ship address information
	private String firstNameShip = "";
	private String lastNameShip = "";
	private String phoneShip = "";
	private String emailShip = "";
	private String addressOneShip = "";
	private String addressTwoShip = "";
	private String cityShip = "";
	private String stateShip = "";
	private String zipcodeShip = "";
	private String storenameValue = "";
	private String firstName = "";
	private String personName = "";
	private String personPhone = "";
	private String personEmail = "";
	private String fullName = "";
	private String storename = "";
	private String address = "";
	private String phonenumber = "";
	private String cityAndZip = "";
	private String hours = "";
	private String mDesIDs = "";
	
	private final String PRIVIEW = "priview";
	private final String LOWRESWARNING = "lowResWarning";
	private final String QUANTITYPLUS = "quantityPlus";
	private final String QUANTITYMINUS = "quantityMinus";
	private final String CHANGE = "change";
	private final String DELETE = "delete";
	private final String YES = "yes";
	private final String KEY_USER_INFO_CHANGED = "User Info Changed";
	private final String KEY_SHIPPING_ADDRESS_CHANGED = "Shipping Address Changed";
	private final String KEY_CART_EDIT = "Cart Edits";
	private final String KEY_CART_REMOVALS = "Cart Removals";
	private final String KEY_COUPON_APPLIED = "Coupon Applied";


	private int additionalPageCount;
	private int minQuantity = 1;
	private boolean okToCheckPrices = true; // not;
	private boolean isGermany = false; // the language is germany or not.
	private boolean isCustomerInfoValid = false;
	private boolean isStoreSelected = false;
	private boolean isShippingAddressValid = false;
	private boolean isScrolling = false;
	private boolean isShowsMSRPPricing = false; //if the price will display
	private boolean isGetPrice = true; //if get the price form server

	public ImageResizer mImageWorker;
	private SharedPreferences prefs;
	private Drawable estimatedBitmap;
	private GreetingCardManager manager;
	private final String TAG = this.getClass().getSimpleName();
	private List<String> pricesToShow;
	private List<ProductInfo> productInfoList;
	private List<String> groupItemList = new ArrayList<String>();
	private List<List<ProductInfo>> childItemList = new ArrayList<List<ProductInfo>>();
	private List<GreetingCardManager> mGreetingCardManagers ;
	private ImageSelectionDatabase mImageSelectionDatabase = null;
	private ProgressBar subtotalProgress;
	private TopOrderHeadSummaryWidget topOrderSummaryHeadWidget = null;
	private OrderSummaryWidget orderSummaryFootWidget = null;
	private Context context;
	private AppContext appContex;

	private TextView summary = null;
	private TextView storeName = null;
	private TextView storeAddress = null;
	private TextView storeNumber = null;
	private TextView storeHours = null;
	private TextView mTxtCityAndZip = null;
	private TextView name = null;
	private TextView email = null;
	private TextView phone = null;
	private TextView contactInfoTV = null;
	private TextView storeSelectedTV;
	private TextView shippingHandlingLabelTV;
	private TextView orderSubtotalLabelTV;
	private TextView shippingHandlingTV;
	private TextView orderSubtotalTV;
	private TextView noticeTV;
	private TextView shopCart_shipping_name;
	private TextView shopCart_shipping_email;
	private TextView shopCart_shipping_phone;
	private TextView shopCart_shipping_addressOne;
	private TextView shopCart_shipping_addressTwo;
	private TextView shopCart_shipping_cityStateZip;

	private Button shippingAddressChangeButton = null;
	private Button storeChangeBtn;
	private Button changeInforButton;
	private Button next;
	private LinearLayout ordersummary_storeInfo, personalInformation;
	private LinearLayout ordersummary_shippingAddressInfo;
	private View couponItemsGroup;
	private TextView couponStatus;
	private TextView couponPrice;
	private Button couponBtn;
	private Button couponTermsBtn;
	private Button deleteCouponBtn;
	private ExpandableListListener mListener;
	public double subTotal = 0.0;
	private Cart cart;
	private Dialog storeAvailableDialog = null;
	private Dialog canNotGetPriceDialog = null;
	private String productsStr;
	private Collage currentCollage = null;

	public interface ExpandableListListener

	{
		public void refreshExpandableList();

		public void addHeader(TopOrderHeadSummaryWidget topOrderSummaryHeadWidget);

		public void addFooter(OrderSummaryWidget orderSummaryFootWidget);
		
		/**
		 * Localytics for <B>Order Activity Summary</B>
		 * @param key
		 * @param value
		 */
		public void recordLocalyticsOASEvents(String key, String value);
	}

	public ExpandableListAdapter() {

	}

	public ExpandableListAdapter(Context context, AppContext appContex) {
		this.context = context;
		this.appContex = appContex;
		mListener = (ExpandableListListener) context;
		getViews();
		initData();
		initViews();
		setEvents();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Object obj = childItemList.get(groupPosition).get(childPosition);
		return obj;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ShoppingCartItem item = null;
		ProductInfo proInfo = childItemList.get(groupPosition).get(childPosition);
		int minQuantity = proInfo.minQuantity;
		if (convertView == null) {
			item = new ShoppingCartItem(context);
		} else {
			item = (ShoppingCartItem) convertView;
			item.normalProductLy.setVisibility(View.GONE);			
			item.normalOriginalQuantity_tex.setText("");
			item.normalOriginalPrice_tex.setText("");
			item.normalOriginalSize_tex.setText("null");			
			item.normalCouponsQuantity_tex.setText("");
			item.normalCouponsPrice_tex.setText("");
			item.normalCouponsSize_tex.setText("null");
			
			item.photoBookAdditionLy.setVisibility(View.GONE);
			item.additionalQuantity_tex.setText("");
			item.additionalSize_tex.setText("");
			item.additionalPrice_tex.setText("null");
			item.additionalCouponsQuantity_tex.setText("");
			item.additionalCouponsPrice_tex.setText("");
			item.additionalCouponsSize_tex.setText("null");			
			/*if (isGetPrice){
				item.additionalPrice_tex.setVisibility(View.VISIBLE);
				item.photoBookPrice_tex.setVisibility(View.VISIBLE);
			}else {
				item.additionalPrice_tex.setVisibility(View.INVISIBLE);
				item.photoBookPrice_tex.setVisibility(View.INVISIBLE);
			}*/
		}
		if (PrintHelper.price!=null){
			List<ProductInfo> includeProInfos = PrintHelper.price.getIncludePros(proInfo.descriptionId,childPosition);
			ProductInfo normalPro = PrintHelper.price.getNormalPro(proInfo.descriptionId,childPosition);
			if (includeProInfos !=null){
				if (includeProInfos.size()==1){
					item.normalProductLy.setVisibility(View.VISIBLE);	
					item.normalOriginalQuantity_tex.setText(normalPro.quantity+"");
					item.normalOriginalPrice_tex.setText(normalPro.priceStr);
					item.normalOriginalSize_tex.setText(normalPro.name);
					
					item.normalCouponsQuantity_tex.setText(includeProInfos.get(0).quantity+"");
					item.normalCouponsPrice_tex.setText(includeProInfos.get(0).priceStr);
					item.normalCouponsSize_tex.setText(includeProInfos.get(0).name);
					
				}else if (includeProInfos.size()==3){
					item.normalProductLy.setVisibility(View.VISIBLE);		
					item.photoBookAdditionLy.setVisibility(View.VISIBLE);
					
					item.normalOriginalQuantity_tex.setText(normalPro.quantity+"");
					item.normalOriginalPrice_tex.setText(normalPro.priceStr);
					item.normalOriginalSize_tex.setText(normalPro.name);
					
					item.normalCouponsQuantity_tex.setText(includeProInfos.get(0).quantity+"");
					item.normalCouponsPrice_tex.setText(includeProInfos.get(0).priceStr);
					item.normalCouponsSize_tex.setText(includeProInfos.get(0).name);
					
					item.additionalQuantity_tex.setText(includeProInfos.get(1).quantity+"");
					item.additionalSize_tex.setText(includeProInfos.get(1).name);
					item.additionalPrice_tex.setText(includeProInfos.get(1).priceStr);
					
					item.additionalCouponsQuantity_tex.setText(includeProInfos.get(2).quantity+"");
					item.additionalCouponsSize_tex.setText(includeProInfos.get(2).name);
					item.additionalCouponsPrice_tex.setText(includeProInfos.get(2).priceStr);
				}
			}
		}
		if (proInfo.productType.equals(AppConstants.BOOK_TYPE) && !proInfo.cartItemID.contains(PrintHelper.AdditionalPage)) {
			item.change.setVisibility(View.VISIBLE);
			Photobook photobook = PhotobookUtil.getPhotobookFromList(proInfo.ProductId);
			if (photobook.isLowResWarningShow) {
				item.lowResWarning.setVisibility(View.VISIBLE);
			} else {
				item.lowResWarning.setVisibility(View.INVISIBLE);
			}
		} else if (proInfo.productType.equals(AppConstants.CARD_TYPE) || proInfo.productType.equals(AppConstants.COLLAGE_TYPE)) {
			item.change.setVisibility(View.VISIBLE);
			boolean isLowRes = false;
			if (isLowRes) {
				item.lowResWarning.setVisibility(View.VISIBLE);
			} else {
				item.lowResWarning.setVisibility(View.INVISIBLE);
			}
		} else {
			if (PrintHelper.isLowResWarning(childItemList.get(groupPosition).get(childPosition))) {
				item.lowResWarning.setVisibility(View.VISIBLE);
			} else {
				item.lowResWarning.setVisibility(View.INVISIBLE);
			}
		}
		if (item.quantity.getText().equals(minQuantity + "") || !okToCheckPrices) {
			item.quantityMinusButton.setEnabled(false);
		} else {
			item.quantityMinusButton.setEnabled(true);
		}
		if (!okToCheckPrices) {
			item.delete.setEnabled(false);
			item.change.setEnabled(false);
			item.preview.setEnabled(false);
		} else {
			item.delete.setEnabled(true);
			item.change.setEnabled(true);
			item.preview.setEnabled(true);
		}
		if (item.quantity.getText().equals(999 / minQuantity * minQuantity + "") || !okToCheckPrices) {
			item.quantityPlusButton.setEnabled(false);
		} else {
			item.quantityPlusButton.setEnabled(true);
		}
		item.preview.setOnClickListener(getListener(PRIVIEW,groupPosition,childPosition));
		item.lowResWarning.setOnClickListener(getListener(LOWRESWARNING,groupPosition,childPosition));
		item.quantityPlusButton.setOnClickListener(getListener(QUANTITYPLUS,groupPosition,childPosition));
		item.quantityMinusButton.setOnClickListener(getListener(QUANTITYMINUS,groupPosition,childPosition));

		item.change.setTag(childPosition);
		item.change.setOnClickListener(getListener(CHANGE,groupPosition,childPosition));

		item.delete.setOnClickListener(getListener(DELETE,groupPosition,childPosition));
		if (proInfo.productType.equals(AppConstants.CARD_TYPE)) {
			GreetingCardManager manager = getGreetingCarManager(proInfo.ProductId);
			GreetingCardProduct product = manager.getGreetingCardProduct();
			String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER+ "/";
			String priviewFilePath = tempFolder +product.getPageBySequenceNumber(1).id + PrintHelper.PREVIEWFLAG + ".temp";
			Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(priviewFilePath,
					AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
			if (bitmap != null) {
				item.preview.setImageBitmap(bitmap);
			} else {
				Log.e(TAG, "bitmap is null.");
			}
		}else if (proInfo.productType.equals(AppConstants.COLLAGE_TYPE)) {
			Bitmap bitmap = getThumbnailById(proInfo.ProductId);
			if (bitmap != null) {
				item.preview.setImageBitmap(bitmap);
			} else {
				Log.e(TAG, "bitmap is null.");
			}
		}else {
			if (!isScrolling) {
				mImageWorker.loadImage(groupPosition, childPosition, item.preview);
			} else {
				Bitmap draw = PrintHelper.readBitMap(context, R.drawable.imagewait60x60);
				item.preview.setImageBitmap(draw);
			}
		}
		item.quantity.setText("" + childItemList.get(groupPosition).get(childPosition).quantity);
		item.quantity.setTypeface(PrintHelper.tfb);
		//item.price.setText(PrintHelper.price == null ? "" : PrintHelper.price.subUnitPrice(proInfo.name));
		item.setPadding(64, 0, 0, 0);

		convertView = item;
		return item;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
			if (childItemList.get(groupPosition).size() ==0 || groupItemList.get(groupPosition).contains(PrintHelper.AdditionalPageName)){
				return 0;
			}
			int size = childItemList.get(groupPosition).size();
			return size;
		
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupItemList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// return PrintHelper.groupsToShow.size();
		if (null == groupItemList) {
			return 0;
		}
		return groupItemList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		ShoppingCartHeaderItem item = null;
		if (convertView == null) {
			item = new ShoppingCartHeaderItem(context);
		} else {
			item = (ShoppingCartHeaderItem) convertView;
		}
		int quantity = 0;
		if (groupPosition < childItemList.size()) {
			for (int i = 0; i < childItemList.get(groupPosition).size(); i++) {
				quantity += childItemList.get(groupPosition).get(i).quantity;
			}
		}
		item.quantityTV.setTextColor(Color.WHITE);
		item.quantityTV.setBackgroundColor(Color.BLACK);
		item.quantityTV.setText("" + quantity);
		item.quantityTV.setTypeface(PrintHelper.tfb);
		for (PrintProduct prod : PrintHelper.products) {
			if (prod.getName().equals(groupItemList.get(groupPosition).toString()) || prod.getId().equals(groupItemList.get(groupPosition))) {
				groupItemList.set(groupPosition, prod.getName());
				try {
					if (PrintHelper.price != null) {
						ProductInfo proInfo = childItemList.get(groupPosition).get(0);
						String strPrice = PrintHelper.price.groupItemsPrice(proInfo.descriptionId);
						item.priceTV.setText(strPrice);
					} else {
						item.priceTV.setText("");
					}

				} catch (Exception ex) {
					item.priceTV.setText("");
					ex.printStackTrace();
				}
			}
		}
		item.pricingEstimated.setVisibility(View.GONE);
		item.pricingEstimated.setTypeface(PrintHelper.tf);
		item.productSizeTV.setText(groupItemList.get(groupPosition).toString());
		if (PrintHelper.orderType == 1 || PrintHelper.orderType == 0) {
			item.pricingEstimated.setText("" + context.getString(R.string.orderConfirmationEstimated));
		} else {
			item.pricingEstimated.setText("" + context.getString(R.string.OrderSummary_TaxesS2H));
		}
		if (groupItemList.get(groupPosition).equals(PrintHelper.AdditionalPageName)){
			item.productSizeTV.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		}
		if (groupItemList.get(groupPosition).toString().equals(context.getResources().getString(R.string.orderConfirmationEstimated))) {
			item.priceTV.setVisibility(View.INVISIBLE);
			item.quantityTV.setVisibility(View.INVISIBLE);
			item.productSizeTV.setVisibility(View.INVISIBLE);
			item.pricingEstimated.setVisibility(View.INVISIBLE);
			boolean isTaxWillBeCalculatedByRetailer = prefs.getBoolean("TaxWillBeCalculatedByRetailer", false);
			if (isTaxWillBeCalculatedByRetailer) {
				item.pricingEstimated.setVisibility(View.VISIBLE);
			}
			item.dividerDash.setVisibility(View.GONE);
			item.groupIndicator.setVisibility(View.INVISIBLE);
			item.viewAutomaticUpload2Albums.setVisibility(View.GONE);
			if (context.getApplicationContext().getPackageName().contains("wmc")) {
				item.viewAutomaticUpload2Albums.setVisibility(View.VISIBLE);

				item.viewShoppingCartGroupItem.setBackgroundDrawable(estimatedBitmap);

				CheckBox checkBoxAutoUpload = (CheckBox) item.findViewById(R.id.automaticUploadCB);
				checkBoxAutoUpload.setChecked(prefs.getBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, false));

				checkBoxAutoUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						Editor editor = prefs.edit();
						editor.putBoolean(PrintHelper.IS_AUTO_UPLOAD2ALBUMS, isChecked);
						editor.commit();
					}
				});
			} else if (context.getApplicationContext().getPackageName().contains("com.kodak.dm.rsscombinedapp")) {
				// item.pricingEstimated.setVisibility(View.INVISIBLE);
				item.setBackgroundDrawable(estimatedBitmap);
			} else {
				item.setBackgroundDrawable(estimatedBitmap);
			}
		} else {
			item.viewAutomaticUpload2Albums.setVisibility(View.GONE);
			item.priceTV.setVisibility(View.VISIBLE);
			item.quantityTV.setVisibility(View.VISIBLE);
			item.productSizeTV.setVisibility(View.VISIBLE);
			item.pricingEstimated.setVisibility(View.INVISIBLE);
			item.dividerDash.setVisibility(View.VISIBLE);
			item.setBackgroundDrawable(null);
			item.setBackgroundColor(Color.BLACK);
			item.groupIndicator.setVisibility(View.VISIBLE);
			item.groupIndicator.setImageResource(isExpanded ? R.drawable.expanded_button : R.drawable.collapsed_button);
		}
		if (groupItemList.get(groupPosition).equals(PrintHelper.AdditionalPageName)){
			item.groupIndicator.setVisibility(View.INVISIBLE);
			/*item.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});*/
		}
		if (isGetPrice){
			if (item.priceTV.getText().equals("") && item.priceTV.getVisibility() == View.VISIBLE && !AppContext.getApplication().isInStoreCloud()) {
				item.bar.setVisibility(View.VISIBLE);
			} else {
				item.bar.setVisibility(View.INVISIBLE);
			}
		}else {
			item.priceTV.setVisibility(View.INVISIBLE);
			item.bar.setVisibility(View.INVISIBLE);
		}
		convertView = item;
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private int getAdditionalPageCount() {
		if (!AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
			return 0;
		} else {
			/*int additionalPageCount = PrintHelper.photoBookPages.size()
					- prefs.getInt(PrintHelper.sNumberOfPagesPerBaseBook, PrintHelper.photoBookPages.size());*/
			Photobook photobook = AppContext.getApplication().getPhotobook();
			int additionalPageCount = photobook.photoBookPages.size() - photobook.numberOfPagesPerBaseBook;
			for (int i = 0; i < photobook.photoBookPages.size(); i++) {
				if (photobook.photoBookPages.get(i).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)) {
					additionalPageCount--;
				}
			}
			return additionalPageCount;
		}
	}

	public void initProductInfoData() {
		mGreetingCardManagers = AppContext.getApplication().getmGreetingCardManagers();
		ProductInfo proInfo = null;
		if (null != appContex.getProductInfos()) {
			productInfoList = appContex.getProductInfos();
		} else {
			productInfoList = new ArrayList<ProductInfo>();
		}
		if (AppContext.getApplication().getFlowType().isPhotoBookWorkFlow()) {
			List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
			for(Photobook photobook : photobooks){
				if (PrintHelper.cartChildren == null || PrintHelper.cartChildren.size() ==0 || PrintHelper.cartChildren.get(0).size() == 0){
					continue;
				}
				for (PrintProduct product : PrintHelper.products) {
					if (product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage)
							&& product.getId().equals(photobook.proDescId)) {
						PrintHelper.cartChildren.get(0).get(0).quantityIncrement = product.getQuantityIncrement() == 0 ? 1 : product
								.getQuantityIncrement();
						minQuantity = PrintHelper.cartChildren.get(0).get(0).quantity = product.getQuantityIncrement() == 0 ? 1 : product
								.getQuantityIncrement();
						if (additionalPageCount > 0) {
							PrintHelper.cartChildren.get(1).get(0).quantity = additionalPageCount * minQuantity;

						}
					}
				}
			}
		}
		
		// TODO: GrettingCard part
		if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
			GreetingCardProduct product = manager.getGreetingCardProduct();
			for (PrintProduct temp : PrintHelper.products) {
				if (temp.getId().equalsIgnoreCase(product.productDescriptionId)) {
					minQuantity = temp.getQuantityIncrement() == 0 ? 1 : temp.getQuantityIncrement();
					break;
				}
			}
			CartItem item = new CartItem(context);
			ArrayList<CartItem> items = new ArrayList<CartItem>();
			item.productDescriptionId = product.productDescriptionId;
			item.quantity = item.quantityIncrement = minQuantity;
			item.productType = AppConstants.CARD_TYPE;
			item.name = manager.getGreetingCardProductName(product.productDescriptionId);
			item.ProductId = manager.getGreetingCardProduct().id;
			items.add(item);
			PrintHelper.cartChildren.add(items);
		}
		
		// TODO: Collage part
			if (AppContext.getApplication().getFlowType().isCollageWorkFlow()) {
				for (PrintProduct temp : PrintHelper.products) {
					if (temp.getId().equalsIgnoreCase(currentCollage.proDescId)) {
						minQuantity = temp.getQuantityIncrement() == 0 ? 1 : temp.getQuantityIncrement();
						break;
					}
				}
				CartItem item = new CartItem(context);
				ArrayList<CartItem> items = new ArrayList<CartItem>();
				item.productDescriptionId = currentCollage.proDescId;
				item.quantity = item.quantityIncrement = minQuantity;
				item.productType = AppConstants.COLLAGE_TYPE;
				item.name = currentCollage.proDescId;
				item.ProductId = currentCollage.id;
				items.add(item);
				PrintHelper.cartChildren.add(items);
			}
		

		// get the product information list
		for (ArrayList<CartItem> cartItmeList : PrintHelper.cartChildren) {
			for (CartItem cartItem : cartItmeList) {
				proInfo = new ProductInfo();
				proInfo.displayImageUrl = cartItem.photoInfo.getLocalUri();
				proInfo.name = cartItem.name;
				proInfo.height = cartItem.height;
				proInfo.width = cartItem.width;
				proInfo.minQuantity = cartItem.quantityIncrement;
				proInfo.quantity = cartItem.quantity;
				proInfo.roi = cartItem.roi;
//				proInfo.photoInfo.setLocalUri(cartItem.photoInfo.getLocalUri()) ;
//				proInfo.photoInfo.setPhotoPath(cartItem.photoInfo.getPhotoPath());
				proInfo.photoInfo = cartItem.photoInfo ;
				proInfo.productType = cartItem.productType;
				proInfo.shortName = cartItem.shortName;
				proInfo.imgWidth = cartItem.imgWidth;
				proInfo.imgHeight = cartItem.imgHeight;
				proInfo.newWidth = cartItem.newWidth;
				proInfo.newHeight = cartItem.newHeight;
				proInfo.scaleFactor = cartItem.scaleFactor;
				proInfo.lastScaleFactor = cartItem.lastScaleFactor;
				proInfo.defaultScaleFactor = cartItem.defaultScaleFactor;
				proInfo.additionalPageCount = additionalPageCount;
				proInfo.descriptionId = cartItem.productDescriptionId;
				if (proInfo.productType.equals(AppConstants.PRINT_TYPE)){
					proInfo.ProductId = cartItem.serverID;
				}else{
					proInfo.ProductId = cartItem.ProductId;
				}	
				proInfo.imageId = cartItem.imageId;
				proInfo.cartItemID = cartItem.cartItemID;
				int i = 0 ;
				boolean isAdd = true;
				if (proInfo.productType.equals(AppConstants.PRINT_TYPE)){
					for (ProductInfo productInforTemp : productInfoList){
						if (productInforTemp.productType.equals(AppConstants.PRINT_TYPE)){
							if (productInforTemp.equals(proInfo)){
								isAdd = false;
								break ;
							}
						}
						
					}
				}else if (proInfo.productType.equals(AppConstants.BOOK_TYPE)){
					for (ProductInfo productInforTemp : productInfoList){
						if (productInforTemp.productType.equals(AppConstants.BOOK_TYPE)){
							if (productInforTemp.ProductId.equals(proInfo.ProductId)){
								productInfoList.remove(i);
								isAdd = false;
								productInfoList.add(i, proInfo);
								break;
							}
						}	
						i ++ ;
					}
					
				}else if (proInfo.productType.equals(AppConstants.CARD_TYPE)){
					for (ProductInfo productInforTemp : productInfoList){
						if (productInforTemp.productType.equals(AppConstants.CARD_TYPE)){
							if (productInforTemp.ProductId.equals(proInfo.ProductId)){
								GreetingCardManager manager = getGreetingCarManager(proInfo.ProductId);
								proInfo.quantity = manager.getGreetingCardProduct().count; //update the quantity of the greetingCar
								productInfoList.remove(i);
								isAdd = false;
								productInfoList.add(i, proInfo);
								break;
							}
						}	
						i ++ ;
					}
				
				}else if (proInfo.productType.equals(AppConstants.COLLAGE_TYPE)){
					for (ProductInfo productInforTemp : productInfoList){
						if (productInforTemp.productType.equals(AppConstants.COLLAGE_TYPE)){
							if (productInforTemp.ProductId.equals(proInfo.ProductId)){
								productInfoList.remove(i);
								isAdd = false;
								productInfoList.add(i, proInfo);
								break;
							}
						}	
						i ++ ;
					}
				
				}
				if (isAdd){
					if (proInfo.productType.equals(AppConstants.CARD_TYPE)){
						// if the card is new one set default value for count
						manager.getGreetingCardProduct().count = minQuantity;
					}
					productInfoList.add(proInfo);
				}
			}
		}
		sortProductInfo(productInfoList);
	}
	
	public void sortProductInfo(List<ProductInfo> mProductInfoList){
		List<ProductInfo> productInfoTempList = new ArrayList<ProductInfo>();
			for (PrintProduct pro : PrintHelper.products){
				for (ProductInfo proTemp : mProductInfoList){
					if (proTemp.descriptionId.equalsIgnoreCase(pro.getId())){
						productInfoTempList.add(proTemp);
					}
				}
			}
		// get the group item list
				groupItemList.clear();
				for (ProductInfo proInfoObj : productInfoTempList) {
					if (!groupItemList.contains(proInfoObj.name)) {
						if (!proInfoObj.name.equals(PrintHelper.AdditionalPageName)){
							groupItemList.add(proInfoObj.name);
						}
					}
				}
				groupItemList.add(context.getResources().getString(R.string.orderConfirmationEstimated));

				// get the child item list
				List<ProductInfo> tempList = new ArrayList<ProductInfo>();
				childItemList.clear();
				for (String productName : groupItemList) {
					if (productName.equals(context.getResources().getString(R.string.orderConfirmationEstimated))) {
						continue;
					}
					tempList = new ArrayList<ProductInfo>();
					for (ProductInfo productObj : productInfoTempList) {
						if (productName.equals(productObj.name)) {
							tempList.add(productObj);	
						}
					}
					if (null != tempList) {
						if (!productName.contains(PrintHelper.AdditionalPageName)){
							childItemList.add(tempList);
						}
						
					} 
				}
				childItemList.add(new ArrayList<ProductInfo>());
				setExpandableListData();
				saveProductInfoListData();
	}
	
	private void getPricesToShow(){
		int totalQuantity = 0;
		int itemQuantity = 0;
		String products = "";
		String productsBook = "";
		List<Photobook> photobooks = AppContext.getApplication().getPhotobooks();
		Photobook photobook = null;
		try {
			Log.i(TAG, "stotal groupItemList.size() = " + groupItemList.size() + " , childItemList.size() = " + childItemList.size());
			for (int i = 0; i < groupItemList.size(); i++) {
				totalQuantity = 0;
				if (i < childItemList.size()) {
					if ( childItemList.get(i).size() == 0){
						continue;
					}
					for (int j = 0; j < childItemList.get(i).size(); j++) {
						itemQuantity = childItemList.get(i).get(j).quantity;
						if (!childItemList.get(i).get(j).productType.equals(AppConstants.BOOK_TYPE)){
							Log.i(TAG, "childItemList.get(" + i + ").get(" + j + ").quantity = " + childItemList.get(i).get(j).quantity);
							subTotal += (itemQuantity * childItemList.get(i).get(j).price);
							totalQuantity += itemQuantity;
						}else {
							for (Photobook photobookTemp : photobooks){
								if (photobookTemp.id.equals(childItemList.get(i).get(j).ProductId)){
									photobook = photobookTemp;
								}
							}
							
							int realPageNumber = photobook.photoBookPages.size();
							for (int index = 0; index < photobook.photoBookPages.size(); index++) {
								if (photobook.photoBookPages.get(index).sPhotoBookPageName.equals(PhotoBookPage.DUPLEX_FILLER)) {
									realPageNumber--;
								}
							}
							productsBook = childItemList.get(i).get(j).descriptionId + "(" + realPageNumber + ")"+ "=" + itemQuantity;
							pricesToShow.add(productsBook);
						}
						
					}
					products = childItemList.get(i).get(0).descriptionId + "=" + totalQuantity;
					String temp = groupItemList.get(i).toString();
					if (totalQuantity !=0 && !temp.equals(context.getResources().getString(R.string.orderConfirmationEstimated)) && !temp.equals("")) {
						if (products !=null && products.length() !=0){
							pricesToShow.add(products);
						}
					}
				}
				
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void checkIsGetPrice (){
		isGetPrice = true;
		switch (PrintHelper.orderType){
		case 0:
			if (isShowsMSRPPricing()){
				isGetPrice = true;
			}else {
				isGetPrice = false;
			}	
			break;
		case 1:
			storenameValue = prefs.getString("selectedStoreName", "");
			isStoreSelected = !storenameValue.equals("");
			if (isStoreSelected){
				if (!checkStoreAvailable()){
					isGetPrice = false;
				}else {
					isGetPrice = true;
				}
			}else {
				if (isShowsMSRPPricing()){
					isGetPrice = true;
				}else {
					isGetPrice = false;
				}	
			}
			
			break;
		case 2:
			isGetPrice = true;
			break;
		}
	}
	
	private void getPrice() {
		pricesToShow.clear();
		PrintHelper.price = new Pricing();
		getPricesToShow();
		subtotalProgress.setVisibility(View.VISIBLE);
		couponBtn.setVisibility(View.INVISIBLE);
		if (orderSubtotalTV != null) {
			orderSubtotalTV.setText("");
		}
		Thread subTotalPricing = new Thread() {
			public void run() {
				getPriceRun ();
			}
		};
		okToCheckPrices = false;
		subTotalPricing.start();
	}
	
	private synchronized void getPriceRun (){

		checkIsGetPrice ();
		if (!isGetPrice){
			pricingHandler.sendEmptyMessage(1);
			return;
		}
		String storeId = prefs.getString("selectedStoreId", "");
		if(AppContext.getApplication().isInStoreCloud()){
			retailerID = AppContext.getApplication().getInStoreCloudRetailerID();
			storeId = AppConstants.IN_STORE_ID;
		} else if (PrintHelper.orderType == 1) {
			retailerID = prefs.getString("selectedRetailerId", "");
		} else if (PrintHelper.orderType == 2) {
			retailerID = prefs.getString("retailerIdPayOnline", "");
		}
		PrintMakerWebService service = new PrintMakerWebService(context, "");
		int count = 0;
		productsStr = "";
		productsStrCheckStore = "";
		for (int i = 0; i < pricesToShow.size(); i++) {
			if (i == pricesToShow.size() - 1) {
				productsStr += pricesToShow.get(i).toString();
				productsStrCheckStore += pricesToShow.get(i).toString().substring(0, pricesToShow.get(i).toString().indexOf("="));
			} else {
				productsStr += pricesToShow.get(i).toString() + ",";
				productsStrCheckStore += pricesToShow.get(i).toString().substring(0, pricesToShow.get(i).toString().indexOf("=")) + ",";
			}
		}
		Log.e(TAG, "productsStrCheckStore: " + productsStrCheckStore);
		Log.e(TAG, "productsStr: " + productsStr);
		Pricing price = null;
		count = 0;
		if (cartId.equals("") && count < 5) {
			cartId = service.CreateCart(context);
			count++;
		}
		// Set the retailer and store on the cart, added by Alan Swire
		// on 3-February-2014, per pricing crisis
		
		if (storeId != "") {
			Log.e(TAG, "store id =" + storeId + " retailer id = " + retailerID);
			String result = "";
			count = 0;
			while (result == "" && count < 5) {
				result = service.SetStoreID(context, storeId);
				count++;
			}
		}

		count = 0;
		while (price == null && count < 5) {
			PrintHelper.lineItems.clear();
			cart = service.PriceProducts2(context, productsStr, retailerID);
			if (cart !=null){
				price = cart.pricing;
			}
			count++;
		}
		PrintHelper.price = price;
		if (null != PrintHelper.price) {
			pricingHandler.sendEmptyMessage(0);
			pricingHandler.sendEmptyMessage(1);
		}else {
			if (ShoppingCartActivity.isShoppingCardLive){
				showCanNotGetPriceDialog();
			}
		}
	}
	
	public void refreshSubTotal() {
		getPrice();
	}

	// check the locale is germany or not;
	private void checkCountry() {
		Resources resources = context.getResources();
		Configuration config = resources.getConfiguration();
		DisplayMetrics dm = resources.getDisplayMetrics();
		if (config.locale.getLanguage().equals(Locale.GERMANY.getLanguage())) {
			isGermany = true;
		} else {
			isGermany = false;
		}

		resources.updateConfiguration(config, dm);
	}

	public Handler pricingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			okToCheckPrices = true;
			changeSubtotalTest();
			couponBtn.setVisibility(View.VISIBLE);
			switch (msg.what) {
			case 0: 
				try {
					subtotalProgress.setVisibility(View.INVISIBLE);
					PrintHelper.totalCost = PrintHelper.price == null ? "" : PrintHelper.price.totalPrice();
					if (orderSubtotalTV != null) {
						orderSubtotalTV.setText(PrintHelper.price == null ? "" : PrintHelper.price.totalPrice());
					}
					if (shippingHandlingTV != null) {
						shippingHandlingTV.setText(PrintHelper.price == null ? "" : PrintHelper.price.shippingAndHandlingPrice());
					}
					if (null != PrintHelper.price.shipAndHandling) {
						shippingHandlingTV.setVisibility(View.VISIBLE);
						shippingHandlingLabelTV.setVisibility(View.VISIBLE);
					} else {
						shippingHandlingTV.setVisibility(View.INVISIBLE);
						shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
					}
					if(cart!=null && cart.discounts!=null && cart.discounts.length>0){
						couponItemsGroup.setVisibility(View.VISIBLE);
						Discount discount = cart.discounts[0];
						couponStatus.setText(discount.localizedName + " - " + discount.localizedStatusDescription);
						Pricing price = cart.pricing;
						if(price.totalSavings!=null && !price.totalSavings.priceStr.equals("")){
							couponPrice.setText(price.totalSavings.priceStr);
						} else {
							couponPrice.setText("");
						}
						if(discount.termsAndConditionsURL!=null && !discount.termsAndConditionsURL.equals("")){
							couponTermsBtn.setVisibility(View.VISIBLE);
						} else {
							couponTermsBtn.setVisibility(View.GONE);
						}
						if(cart.discounts[0].status == 0){
							mListener.recordLocalyticsOASEvents(KEY_COUPON_APPLIED, YES);
						} else {
							mListener.recordLocalyticsOASEvents(KEY_COUPON_APPLIED, "no");
						}
					} else {
						couponItemsGroup.setVisibility(View.GONE);
					}
					/*
					 * expandableListAdapter = ((ExpandableListAdapter)
					 * expandableList.getExpandableListAdapter());
					 * expandableListAdapter.notifyDataSetChanged();
					 */
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			case 1:
				subtotalProgress.setVisibility(View.INVISIBLE);
				noticeTV.setVisibility(View.VISIBLE);
				orderSubtotalLabelTV.setVisibility(View.INVISIBLE);
				orderSubtotalTV.setVisibility(View.INVISIBLE);
				if (!isGetPrice){
					switch (PrintHelper.orderType){
					case 0:
						noticeTV.setText(context.getString(R.string.NoDeliver_Price));
						break;
					case 1:
						noticeTV.setText(context.getString(R.string.NoStore_Price));
						break;
					case 2:
						noticeTV.setText(context.getString(R.string.NoDeliver_Price));
						break;
					}
				}else {
					noticeTV.setVisibility(View.GONE);
					orderSubtotalLabelTV.setVisibility(View.VISIBLE);
					orderSubtotalTV.setVisibility(View.VISIBLE);
				}
			}		
			mListener.refreshExpandableList();
			
		}
	};

	private void getViews() {
		next = (Button) ((Activity) context).findViewById(R.id.next_btn);

		topOrderSummaryHeadWidget = new TopOrderHeadSummaryWidget(this.context);
		orderSummaryFootWidget = new OrderSummaryWidget(this.context);
		// order summary head
		shippingHandlingLabelTV = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.shippingHandlingLabelTV);
		shippingHandlingTV = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.shippingHandlingTV);
		orderSubtotalLabelTV = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.subtotalLabelTV);
		orderSubtotalTV = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.subtotalTV);
		noticeTV = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.noticeTV);
		subtotalProgress = (ProgressBar) topOrderSummaryHeadWidget.findViewById(R.id.shoppingCartProgressBar1);
		couponItemsGroup = topOrderSummaryHeadWidget.findViewById(R.id.coupon_items);
		couponStatus = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.tv_coupon_status);
		couponPrice = (TextView) topOrderSummaryHeadWidget.findViewById(R.id.tv_coupon_price);
		couponBtn = (Button) topOrderSummaryHeadWidget.findViewById(R.id.coupons_btn);
		deleteCouponBtn = (Button) topOrderSummaryHeadWidget.findViewById(R.id.bt_coupon_delete);
		couponTermsBtn = (Button) topOrderSummaryHeadWidget.findViewById(R.id.btReadTerms);
		mListener.addHeader(topOrderSummaryHeadWidget);
		mListener.addFooter(orderSummaryFootWidget);

		// order summary foot
		ordersummary_storeInfo = (LinearLayout) orderSummaryFootWidget.findViewById(R.id.ordersummary_storeInfo);
		personalInformation = (LinearLayout) orderSummaryFootWidget.findViewById(R.id.personalInformation);
		ordersummary_shippingAddressInfo = (LinearLayout) orderSummaryFootWidget.findViewById(R.id.ordersummary_shippingAddressInfo);
		shopCart_shipping_name = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_name);
		shopCart_shipping_email = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_email);
		shopCart_shipping_phone = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_phone);
		shopCart_shipping_addressOne = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_addressOne);
		shopCart_shipping_addressTwo = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_addressTwo);
		shopCart_shipping_cityStateZip = (TextView) orderSummaryFootWidget.findViewById(R.id.shopCart_shipping_cityStateZip);
		shippingAddressChangeButton = (Button) orderSummaryFootWidget.findViewById(R.id.shippingAddressChangeButton);
		summary = (TextView) orderSummaryFootWidget.findViewById(R.id.cartItemSummary_tex);
		storeName = (TextView) orderSummaryFootWidget.findViewById(R.id.storeName_tex);
		storeAddress = (TextView) orderSummaryFootWidget.findViewById(R.id.storeAddress_tex);
		mTxtCityAndZip = (TextView) orderSummaryFootWidget.findViewById(R.id.cityZip_tex);
		storeNumber = (TextView) orderSummaryFootWidget.findViewById(R.id.storePhone_tex);
		storeHours = (TextView) orderSummaryFootWidget.findViewById(R.id.storeHours_tex);
		name = (TextView) orderSummaryFootWidget.findViewById(R.id.name_tex);
		email = (TextView) orderSummaryFootWidget.findViewById(R.id.email_tex);
		phone = (TextView) orderSummaryFootWidget.findViewById(R.id.phone_tex);
		contactInfoTV = (TextView) orderSummaryFootWidget.findViewById(R.id.contactInfo_tex);
		storeSelectedTV = (TextView) orderSummaryFootWidget.findViewById(R.id.storeInf_tex);
		storeChangeBtn = (Button) orderSummaryFootWidget.findViewById(R.id.storeChange_btn);
		changeInforButton = (Button) orderSummaryFootWidget.findViewById(R.id.changeInfor_btn);
		changeSubtotalTest();
	}

	private void initData() {
		currentCollage = CollageManager.getInstance().getCurrentCollage() ;
		if (AppContext.getApplication().getFlowType().isGreetingCardWorkFlow()) {
			manager = GreetingCardManager.getGreetingCardManager(this.context);
		}
		isShowsMSRPPricing = isShowsMSRPPricing();
		appContex.setEditGreetingCart(false);
		ImageCacheParams cacheParams = new ImageCacheParams(IMAGE_CACHE_DIR);
		mImageWorker = new ImageFetcher(this.context, this.context.getResources().getDimensionPixelSize(R.dimen.image_cart_size));
		mImageWorker.setAdapter(imageThumbWorkerUrlsAdapter);
		mImageWorker.setLoadingImage(R.drawable.imagewait60x60);
		mImageWorker.setImageCache(ImageCache.findOrCreateCache((FragmentActivity) this.context, cacheParams));
		mImageSelectionDatabase = new ImageSelectionDatabase(this.context);
		prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		additionalPageCount = getAdditionalPageCount();
		pricesToShow = new ArrayList<String>();
		estimatedBitmap = (Drawable) this.context.getResources().getDrawable(R.drawable.cartbottom);
		// initQuantityIncrement();
		checkCountry();
		initProductInfoData();
		mDesIDs= new ShoppingCartActivity().getDesIDs(AppContext.getApplication().getProductInfos());
	}

	// for different delivery Model , the screen show will different
	public void changeScreenShow() {
		getUserInfos();
		ordersummary_storeInfo.setVisibility(View.GONE);
		personalInformation.setVisibility(View.GONE);
		ordersummary_shippingAddressInfo.setVisibility(View.GONE);
		next.setText(this.context.getString(R.string.next));
		isStoreSelected = !storenameValue.equals("");
		isShippingAddressValid = isShippingAddressValid(this.context);
		shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
		shippingHandlingTV.setVisibility(View.INVISIBLE);
		isCustomerInfoValid = isCustomerInfoValid(this.context, false);
		initExpandableListFootView();
		if(AppContext.getApplication().isInStoreCloud()){
			shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
			shippingHandlingTV.setVisibility(View.INVISIBLE);
			personalInformation.setVisibility(View.VISIBLE);
			if(isCustomerInfoValid){
				next.setText(R.string.buy);
			} else {
				next.setText(R.string.next);
			}
			return;
		}
		switch (PrintHelper.orderType) {
		case 0:
			next.setText(this.context.getString(R.string.next));
			shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
			shippingHandlingTV.setVisibility(View.INVISIBLE);
			break;
		case 1:
			shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
			shippingHandlingTV.setVisibility(View.INVISIBLE);
			final String infoDialog = "infoDialog";
			if (storename.equals("") && prefs.getBoolean(infoDialog, true)) {
				Editor editor = prefs.edit();
				editor.putBoolean(infoDialog, false);
				editor.commit();
				Log.e(TAG, "on resume: infoDialog start" + ":" + prefs.getBoolean(infoDialog, true));
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(context);
				builder.setTitle("");
				builder.setMessage(this.context.getString(R.string.selectstore));
				builder.setPositiveButton(this.context.getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context, StoreFinder.class);
						intent.putExtra("productStringCheckStore", ((ShoppingCartActivity)context).getDesIDs(AppContext.getApplication().getProductInfos()));
						intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
						Editor editor = prefs.edit();
						editor.putBoolean(infoDialog, true);
						editor.commit();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(this.context.getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor editor = prefs.edit();
						editor.putBoolean(infoDialog, true);
						editor.commit();
						dialog.dismiss();
					}
				});
				builder.create().show();
			} else if (isCustomerInfoValid) {
				personalInformation.setVisibility(View.VISIBLE);
				if (isStoreSelected) {
					next.setText(this.context.getString(R.string.buy));
					ordersummary_storeInfo.setVisibility(View.VISIBLE);
				}
			}
			break;
		case 2:
			shippingHandlingLabelTV.setVisibility(View.VISIBLE);
			shippingHandlingTV.setVisibility(View.VISIBLE);
			if (isCustomerInfoValid) {
				orderSubtotalLabelTV.setVisibility(View.VISIBLE);
				orderSubtotalTV.setVisibility(View.VISIBLE);
				noticeTV.setVisibility(View.GONE);
				personalInformation.setVisibility(View.VISIBLE);
				ordersummary_shippingAddressInfo.setVisibility(View.VISIBLE);
				getShippingAddressInfo();
				if (isShippingAddressValid) {
					next.setText(this.context.getString(R.string.buy));
				}
			}

			break;
		}
	}

	private void getShippingAddressInfo() {
		if (TextUtils.isEmpty(firstNameShip) && TextUtils.isEmpty(lastNameShip)) {
			shopCart_shipping_name.setVisibility(View.GONE);
		} else {
			shopCart_shipping_name.setVisibility(View.VISIBLE);
			shopCart_shipping_name.setText(firstNameShip + " " + lastNameShip);
		}

		if (TextUtils.isEmpty(emailShip)) {
			shopCart_shipping_email.setVisibility(View.GONE);
		} else {
			shopCart_shipping_email.setVisibility(View.VISIBLE);
			shopCart_shipping_email.setText(emailShip);
		}

		if (TextUtils.isEmpty(phoneShip)) {
			shopCart_shipping_phone.setVisibility(View.GONE);
		} else {
			shopCart_shipping_phone.setVisibility(View.VISIBLE);
			shopCart_shipping_phone.setText(phoneShip);
		}

		if (TextUtils.isEmpty(addressOneShip)) {
			shopCart_shipping_addressOne.setVisibility(View.GONE);
		} else {
			shopCart_shipping_addressOne.setVisibility(View.VISIBLE);
			shopCart_shipping_addressOne.setText(addressOneShip);
		}

		if (TextUtils.isEmpty(addressTwoShip)) {
			shopCart_shipping_addressTwo.setVisibility(View.GONE);
		} else {
			shopCart_shipping_addressTwo.setVisibility(View.VISIBLE);
			shopCart_shipping_addressTwo.setText(addressTwoShip);
		}

		if (TextUtils.isEmpty(cityShip) && TextUtils.isEmpty(stateShip) && TextUtils.isEmpty(zipcodeShip)) {
			shopCart_shipping_cityStateZip.setVisibility(View.GONE);
		} else {
			shopCart_shipping_cityStateZip.setVisibility(View.VISIBLE);
			String cityShipStr = "";
			if (!TextUtils.isEmpty(cityShip)) {

				cityShipStr = cityShip + ",";
			}

			shopCart_shipping_cityStateZip.setText(cityShipStr + " " + stateShip + " " + zipcodeShip);
		}
	}

	public boolean isCustomerInfoValid(Context context, boolean shippingToHome) {
		ArrayList<Integer> requiredContactInfo = new ArrayList<Integer>();
		if (PrintHelper.orderType == 2) { // ship to home
			String retailerIdPayOnline = prefs.getString("retailerIdPayOnline", "");
			requiredContactInfo = PrintHelper.requiredContactInfos.get(retailerIdPayOnline);
		} else {
			if (PrintHelper.requiredContactInfos == null)
				PrintHelper.requiredContactInfos = new HashMap<String, ArrayList<Integer>>();
			if (PrintHelper.requiredContactInfos.size() > 1) {
				String retailerId = "";
				if(AppContext.getApplication().isInStoreCloud()){
					retailerId = AppContext.getApplication().getInStoreCloudRetailerID();
				} else {
					retailerId = prefs.getString("selectedRetailerId", "");
				}
				ArrayList<Integer> a = PrintHelper.requiredContactInfos.get(retailerId);
				if (a != null)
					requiredContactInfo = a;
			} else if (PrintHelper.requiredContactInfos.size() == 1) {
				for (ArrayList<Integer> a : PrintHelper.requiredContactInfos.values()) {
					requiredContactInfo = a;
				}
			}
		}

		if (requiredContactInfo==null || requiredContactInfo.size() == 0) {
			return true;
		}
		for (int i = 0; i < requiredContactInfo.size(); i++) {
			switch (requiredContactInfo.get(i)) {
			case 0:
				if (firstName.equals("")) {
					return false;
				}
				break;
			case 1:
				if (personName.equals("")) {
					return false;
				}
				break;
			case 2:
				if (personPhone.equals("")) {
					return false;
				}
				break;
			case 6:
				if (personEmail.equals("")) {
					return false;
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	public boolean isShippingAddressValid(Context context) {
		/*
		 * CountryInfo countryInfo = null; countryInfo =
		 * RssTabletApp.getInstance().countryInfo;
		 */
		boolean valid = true;
		List<Integer> requiredContactInfo = null;
		String retailerIdPayOnline = prefs.getString("retailerIdPayOnline", "");
		requiredContactInfo = PrintHelper.requiredContactInfos.get(retailerIdPayOnline);
		if (requiredContactInfo != null) {
			for (Integer info : requiredContactInfo) {
				switch (info) {
				case 0:
					if (TextUtils.isEmpty(firstNameShip)) {
						valid = false;
					}
					break;

				case 1:
					if (TextUtils.isEmpty(lastNameShip)) {
						valid = false;
					}
					break;
//				case 2:
//					if (TextUtils.isEmpty(phoneShip)) {
//						valid = false;
//					}
//					break;
//				case 6:
//					if (TextUtils.isEmpty(emailShip)) {
//						valid = false;
//					}
//					break;

				default:
					break;
				}
			}
		}

		if (valid) {
			if (PrintHelper.selectedCountryInfo != null) {
				CountryInfo countryInfo = PrintHelper.selectedCountryInfo;
				if (countryInfo != null) {
					if (TextUtils.isEmpty(addressOneShip)) {
						valid = false;
						return false;
					}
					String addressStyle = countryInfo.addressStyle;
					if (addressStyle.contains(CountryInfo.CITY)) {
						if (TextUtils.isEmpty(cityShip)) {
							valid = false;
							return false;
						}
					}
					if (addressStyle.contains(CountryInfo.STATE)) {
						if (TextUtils.isEmpty(stateShip)) {
							valid = false;
							return false;
						}
					}
					if (addressStyle.contains(CountryInfo.ZIP)) {
						if (TextUtils.isEmpty(zipcodeShip)) {
							valid = false;
							return false;
						}
					}
				}
			}
		}

		return valid;
	}

	private void initExpandableListFootView() {
		shippingHandlingLabelTV.setText(this.context.getString(R.string.OrderSummary_ShippingAndHandling));
		shippingHandlingLabelTV.setVisibility(View.INVISIBLE);
		shippingHandlingTV.setVisibility(View.INVISIBLE);
		storeSelectedTV.setText(this.context.getString(R.string.order_summary_selected_store));
		summary.setText("");
		summary.setVisibility(View.GONE);

		if (storename.equals("")) {
			storeName.setVisibility(View.INVISIBLE);
		} else {
			storeName.setText(storename);
			storeName.setVisibility(View.VISIBLE);
		}
		if (address.equals("")) {
			storeAddress.setVisibility(View.INVISIBLE);
		} else {
			storeAddress.setText(address);
			storeAddress.setVisibility(View.VISIBLE);
		}
		if (cityAndZip.equals("") || cityAndZip.trim().length() == 0) {
			mTxtCityAndZip.setVisibility(View.INVISIBLE);
		} else {
			mTxtCityAndZip.setText(cityAndZip);
			mTxtCityAndZip.setVisibility(View.VISIBLE);
		}
		if (phonenumber.equals("")) {
			storeNumber.setVisibility(View.INVISIBLE);
		} else {
			storeNumber.setText(phonenumber);
			storeNumber.setVisibility(View.VISIBLE);
		}
		if (hours.equals("")) {
			storeHours.setVisibility(View.GONE);
		} else {
			storeHours.setText(hours);
			storeHours.setVisibility(View.VISIBLE);
		}
		if (fullName != null && !fullName.equals(" ")) {
			name.setText(fullName);
			name.setVisibility(View.VISIBLE);
		} else {
			name.setVisibility(View.INVISIBLE);
		}
		if (personPhone != null && !personPhone.equals("")) {
			phone.setText(personPhone);
			phone.setVisibility(View.VISIBLE);
		} else {
			phone.setVisibility(View.INVISIBLE);
		}
		if (personEmail != null && !personEmail.equals("")) {
			email.setText(personEmail);
			email.setVisibility(View.VISIBLE);
		} else {
			email.setVisibility(View.INVISIBLE);
		}
		if (name.getVisibility() == View.INVISIBLE && phone.getVisibility() == View.INVISIBLE && email.getVisibility() == View.INVISIBLE) {
			contactInfoTV.setVisibility(View.INVISIBLE);
		} else {
			contactInfoTV.setVisibility(View.VISIBLE);
		}
	}

	private void initViews() {
		shippingHandlingLabelTV.setTypeface(PrintHelper.tf);
		shippingHandlingTV.setTypeface(PrintHelper.tf);
		orderSubtotalLabelTV.setTypeface(PrintHelper.tf);
		orderSubtotalTV.setTypeface(PrintHelper.tf);
		summary.setTypeface(PrintHelper.tf);
		storeName.setTypeface(PrintHelper.tfb);
		storeAddress.setTypeface(PrintHelper.tf);
		mTxtCityAndZip.setTypeface(PrintHelper.tf);
		storeNumber.setTypeface(PrintHelper.tf);
		storeHours.setTypeface(PrintHelper.tf);
		name.setTypeface(PrintHelper.tf);
		email.setTypeface(PrintHelper.tf);
		phone.setTypeface(PrintHelper.tf);
		contactInfoTV.setTypeface(PrintHelper.tfb);
	}

	private void getUserInfos() {
		storename = prefs.getString("selectedStoreName", "");
		address = prefs.getString("selectedStoreAddress", "");
		phonenumber = prefs.getString("selectedStorePhone", "");
		cityAndZip = prefs.getString("selectedCity", "") + " " + prefs.getString("selectedPostalCode", "");
		hours = prefs.getString("selectedStoreHours", "");

		storenameValue = prefs.getString("selectedStoreName", "");
		firstName = prefs.getString("firstName", "");
		personName = prefs.getString("lastName", "");
		fullName = firstName + " " + personName;
		personPhone = prefs.getString("phone", "");
		personEmail = prefs.getString("email", "");

		// get shipping address informations.
		firstNameShip = prefs.getString("firstNameShip", "");
		lastNameShip = prefs.getString("lastNameShip", "");
		phoneShip = prefs.getString("phoneShip", "");
		emailShip = prefs.getString("emailShip", "");

		addressOneShip = prefs.getString("addressOneShip", "");
		addressTwoShip = prefs.getString("addressTwoShip", "");
		cityShip = prefs.getString("cityShip", "");
		stateShip = prefs.getString("stateShip", "");
		zipcodeShip = prefs.getString("zipcodeShip", "");
	}

	private void setEvents() {
		shippingAddressChangeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mListener.recordLocalyticsOASEvents(KEY_SHIPPING_ADDRESS_CHANGED, YES);
				Intent intent = null;
				intent = new Intent(context, NewSettingActivity.class);
				intent.putExtra("requireInfoEntry", true);
				intent.putExtra("currentItem", R.id.radio_address);
				Bundle b = new Bundle();
				b.putString(NewSettingActivity.SETTINGS_LOCATION, SCREEN_NAME);
				intent.putExtras(b);
				context.startActivity(intent);

			}
		});

		storeChangeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(context, StoreFinder.class);
				myIntent.putExtra("productStringCheckStore", ((ShoppingCartActivity)context).getDesIDs(AppContext.getApplication().getProductInfos()));
				myIntent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
				context.startActivity(myIntent);
			}
		});
		changeInforButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.recordLocalyticsOASEvents(KEY_USER_INFO_CHANGED, YES);
				Intent myIntent = new Intent(context, NewSettingActivity.class);
				myIntent.putExtra("requireInfoEntry", true);
				Bundle b = new Bundle();
				b.putString(NewSettingActivity.SETTINGS_LOCATION, SCREEN_NAME);
				myIntent.putExtras(b);
				context.startActivity(myIntent);
			}
		});
		
		couponBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(PrintHelper.orderType == 0){
					((ShoppingCartActivity)context).showWarningDialog(R.string.not_select_a_delivery_option);
				}
				else if(PrintHelper.orderType == 1 && storename.equals("")){
					((ShoppingCartActivity)context).showWarningDialog(R.string.N2RShoppingCart_SelectStore);
				}
				else {
					((ShoppingCartActivity)context).showEnterCouponDialog();
				}
			}
		});
		
		couponTermsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(context, HelpActivity.class);
				Bundle bundle = new Bundle();
				bundle.putBoolean("coupon", true);
				bundle.putString("couponTerms", cart.discounts[0].termsAndConditionsURL);
				mIntent.putExtras(bundle);
				context.startActivity(mIntent);
			}
		});
		
		deleteCouponBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PrintHelper.couponCode = "";
				couponItemsGroup.setVisibility(View.GONE);
				refreshSubTotal();
			}
		});
	}

	private OnClickListener getListener(String type,final int groupPosition,final int childPosition) {
		OnClickListener listener = null;
		if (type.equals(PRIVIEW)) {
			listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final int group = groupPosition;
					final int child = childPosition;
					ProductInfo productInfo = childItemList.get(group).get(child);
					if (!productInfo.productType.equals(AppConstants.PRINT_TYPE)) {
						return;
					}
					PrintHelper.selectedImage = productInfo;
					PrintHelper.lastSelectedImage = PrintHelper.selectedImage;
					PrintHelper.lastGroupPosition = group;
					PrintHelper.lastChildPosition = child;
					PrintHelper.selectedImageGroup = group;
					PrintHelper.selectedImageChild = child;
					Intent intent = new Intent(context, SingleImageEditActivity.class);
					intent.putExtra("EditMode", false);
					context.startActivity(intent);
					((Activity) context).finish();
				}
			};
		} else if (type.equals(LOWRESWARNING)) {
			listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(context);
					builder.setTitle(context.getString(R.string.lowResWarning));
					builder.setMessage("");
					builder.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setNegativeButton("", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();

				}

			};

		} else if (type.equals(QUANTITYPLUS)) {
			listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					PrintHelper.quantityChanges++;
					final int group = groupPosition;
					final int child = childPosition;
					ProductInfo productInfo = childItemList.get(group).get(child);
					productInfo.quantity += productInfo.minQuantity;
					if (productInfo.productType.equals(AppConstants.CARD_TYPE)) {
						GreetingCardManager manager = getGreetingCarManager(productInfo.ProductId);
						manager.getGreetingCardProduct().count = productInfo.quantity;
					}
					refreshSubTotal();
					/*
					 * expandableListAdapter = ((ExpandableListAdapter)
					 * expandableList.getExpandableListAdapter());
					 * expandableListAdapter.notifyDataSetChanged();
					 */
					mListener.refreshExpandableList();

				}

			};

		} else if (type.equals(QUANTITYMINUS)) {
			listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					PrintHelper.quantityChanges++;
					final int group = groupPosition;
					final int child = childPosition;
					ProductInfo productInfo = childItemList.get(group).get(child);
					if (productInfo.quantity > 1) {
						productInfo.quantity -= productInfo.minQuantity;
						if (productInfo.productType.equals(AppConstants.CARD_TYPE)) {
							GreetingCardManager manager = getGreetingCarManager(productInfo.ProductId);
							manager.getGreetingCardProduct().count = productInfo.quantity;
						}
						refreshSubTotal();
						/*
						 * expandableListAdapter = ((ExpandableListAdapter)
						 * expandableList.getExpandableListAdapter());
						 * expandableListAdapter.notifyDataSetChanged();
						 */
						mListener.refreshExpandableList();
					}

				}

			};

		} else if (type.equals(CHANGE)) {
			listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.recordLocalyticsOASEvents(KEY_CART_EDIT, YES);
					final int group = groupPosition;
					final int child = childPosition;
					ProductInfo productInfo = childItemList.get(group).get(child);
					if (productInfo.productType.equals(AppConstants.BOOK_TYPE)) {
						Photobook photobook = PhotobookUtil.getPhotobookFromList(productInfo.ProductId);
						AppContext.getApplication().setPhotobook(photobook);
						Intent intent = new Intent(context, QuickBookFlipperActivity.class);
						AppContext.getApplication().setFlowType(FlowType.BOOK);
						intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
						context.startActivity(intent);
					} else if (productInfo.productType.equals(AppConstants.CARD_TYPE)) {
						AppContext.getApplication().setFlowType(FlowType.CARD);
						Intent intent = new Intent(context, GreetingCardProductActivity.class);
						PrintHelper.GreetingCardProductID =  productInfo.ProductId;
						PrintHelper.lastStepTo = 1; //reset the greetingcard's step default value
						appContex.setEditGreetingCart(true);
						context.startActivity(intent);
						((Activity) context).finish();
					} else if (productInfo.productType.equals(AppConstants.COLLAGE_TYPE)) {
						AppContext.getApplication().setFlowType(FlowType.COLLAGE);
						CollageManager.getInstance().setCurrentCollageById(productInfo.ProductId);
						Intent intent = new Intent(context, CollageEditActivity.class);
						intent.putExtra(AppConstants.FROM_SHOPPINGCART, true);
						context.startActivity(intent);
						((Activity) context).finish();
					}else {
						AppContext.getApplication().setFlowType(FlowType.PRINT);
						PrintHelper.selectedImage = productInfo;
						PrintHelper.lastSelectedImage = PrintHelper.selectedImage;
						PrintHelper.lastGroupPosition = group;
						PrintHelper.lastChildPosition = child;
						PrintHelper.selectedImageGroup = group;
						PrintHelper.selectedImageChild = child;
						Intent intent = new Intent(context, SingleImageEditActivity.class);
						intent.putExtra("EditMode", true);
						context.startActivity(intent);
						((Activity) context).finish();
					}

				}

			};

		} else if (type.equals(DELETE)) {
			listener = new View.OnClickListener() {

				@SuppressLint("NewApi")
				@Override
				public void onClick(View v) {
					final int group = groupPosition;
					final int child = childPosition;
					final ProductInfo productInfo = childItemList.get(group).get(child);
					Bitmap rotated = null;
					Bitmap img = null;
					Bitmap bit = null;
					String prodcutName = "";
					ROI roi = new ROI();
					BitmapFactory.Options options = new Options();
					int width = 0;
					int height = 0;
					try {
						if (productInfo.productType.equals(AppConstants.CARD_TYPE)) {
							GreetingCardManager manager = getGreetingCarManager(productInfo.ProductId);
							GreetingCardProduct gcProduct = manager.getGreetingCardProduct();
//							bit = gcProduct.getPagePreview(gcProduct.getPageBySequenceNumber(1).id, PrintHelper.PREVIEWFLAG);
							bit = gcProduct.getPagePreviewByPath(gcProduct.getPageBySequenceNumber(1).id, PrintHelper.PREVIEWFLAG);
							prodcutName = manager.getGreetingCardProductName(gcProduct.productDescriptionId);
						}  else if (productInfo.productType.equals(AppConstants.COLLAGE_TYPE)){
							bit = getThumbnailById(productInfo.ProductId);
						}else if (productInfo.productType.equals(AppConstants.PRINT_TYPE)){
							String uri = productInfo.photoInfo.getLocalUri();
							String filename = productInfo.photoInfo.getPhotoPath();
							prodcutName = productInfo.shortName;
							img = PrintHelper.loadThumbnailImage(uri, MediaStore.Images.Thumbnails.MINI_KIND, options, context);
							roi = productInfo.roi;
							ExifInterface exif = new ExifInterface(filename);
							if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_90) {
								Matrix matrix = new Matrix();
								matrix.postRotate(90);
								rotated = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
							} else if (exif.getAttributeInt("Orientation", 0) == ExifInterface.ORIENTATION_ROTATE_270) {
								Matrix matrix = new Matrix();
								matrix.postRotate(270);
								rotated = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
							}
							if (rotated != null) {
								img = null;
								img = rotated;
							}
							if (roi == null) {
								Log.e(TAG, "Error, roi is null!");
							} else {
								width = (int) (img.getWidth() * roi.w);
								height = (int) (img.getHeight() * roi.h);
								Log.i(TAG, "roi coming out: " + roi.x + " " + roi.y + " " + roi.w + " " + roi.h + " ");
							}
							if (width == 0 || height == 0) {
								double productWidth = Double.parseDouble(productInfo.width);
								double productHeight = Double.parseDouble(productInfo.height);
								double ratio = 1.0;
								if (productWidth > productHeight) {
									ratio = productWidth / productHeight;
								} else {
									ratio = productHeight / productWidth;
								}
								ROI tempRoi = PrintHelper.CalculateDefaultRoi(img.getWidth(), img.getHeight(), ratio);
								tempRoi.x = tempRoi.x / img.getWidth();
								tempRoi.y = tempRoi.y / img.getHeight();
								tempRoi.w = tempRoi.w / img.getWidth();
								tempRoi.h = tempRoi.h / img.getHeight();
								roi = tempRoi;
								productInfo.roi = roi;
								width = (int) (img.getWidth() * roi.w);
								height = (int) (img.getHeight() * roi.h);
								Log.d(TAG,
										"roi.x: " + roi.x + " roi.y: " + roi.y + " roi.h: " + roi.h + "roi.w: " + roi.w + " image Height: "
												+ img.getHeight() + " image Width: " + img.getWidth());
							}
							bit = Bitmap.createBitmap(img, (int) ((roi.x) * img.getWidth()), (int) ((roi.y) * img.getHeight()),
									(int) ((roi.w) * img.getWidth()), (int) ((roi.h) * img.getHeight()));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(context);
					if (isGermany) {
						builder.setTitle(context.getString(R.string.remove));
					} else {
						builder.setTitle(context.getString(R.string.remove) + " " + prodcutName + " " + context.getString(R.string.print) + "?");
					}
					if (productInfo.productType.equals(AppConstants.BOOK_TYPE)) {
						PrintProduct photoBookProduct = null;
						for (PrintProduct product : PrintHelper.products) {
							if (product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage)
									&& product.getId().equals(productInfo.descriptionId)) {
								photoBookProduct = product;
								break;
							}
						}
						if (isGermany) {
							builder.setTitle(context.getString(R.string.remove));
						} else {
							builder.setTitle(context.getString(R.string.remove) + " " + photoBookProduct.getName() + "?");
						}
					}
					builder.setMessage("");
					if (bit != null)
						builder.setPreviewImage(bit);
						builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String uri = "";
							try {
								PrintHelper.cartRemovals ++;
								mListener.recordLocalyticsOASEvents(KEY_CART_REMOVALS, PrintHelper.cartRemovals < 6 ? PrintHelper.cartRemovals+ "" : "6 +");
								uri = productInfo.photoInfo.getLocalUri();
								mImageSelectionDatabase.open();
								mImageSelectionDatabase.handleDeleteN2RUri(uri);
								mImageSelectionDatabase.close();
								PrintHelper.selectedHash.put(uri, "0");
								PrintHelper.uploadQueue.remove(uri);
								//EncryptUtil.removeLast(PrintHelper.uploadQueue, uri);
								if (PrintHelper.uploadShare2WmcQueue != null)
									PrintHelper.uploadShare2WmcQueue.remove(uri);
									ProductInfo tempPro = childItemList.get(group).get(child);
									childItemList.get(group).remove(child);
								if (childItemList.get(group).size() == 0) {
									groupItemList.remove(group);
									childItemList.remove(group);
								}
								
								if(AppConstants.PRINT_TYPE.equals(productInfo.productType)){
									int count = 0;
									for ( ProductInfo tempProductInfo : productInfoList){
										if (tempProductInfo.photoInfo.getPhotoPath().equals(productInfo.photoInfo.getPhotoPath())){
											count ++;
										}
									}
									
									/** update by song
									 * fixed for RSSMOBILEPDC-1576
									 * Change print size with keep previous size, remove the new print, then sending order failed with 500
									 */
									if (count < 2){
										PrintInfo printInfo = new PrintInfo(productInfo.photoInfo) ;
										 AppContext.getApplication().removePrintFromPrintList(printInfo) ;
										 AppContext.getApplication().getmUploadPhotoList().remove(productInfo.photoInfo) ;
									}									  
//									AppContext.getApplication().
								
								}

								if (productInfo.productType.equals(AppConstants.BOOK_TYPE)) {
									Photobook photobook = PhotobookUtil.getPhotobookFromList(productInfo.ProductId);
									AppContext.getApplication().deletePhotobook(photobook);
								}
								
								if (productInfo.productType.equals(AppConstants.CARD_TYPE)) {
									for (GreetingCardManager managersTemp : mGreetingCardManagers){
										if (null == managersTemp.getGreetingCardProductCardProduct() || null == managersTemp.getGreetingCardProductCardProduct().id){
											continue;
										}
										if (managersTemp.getGreetingCardProductCardProduct().id.equals(tempPro.ProductId)){
											manager = managersTemp;
										}	
									}
									mGreetingCardManagers.remove(manager);
								}
								
								if (productInfo.productType.equals(AppConstants.COLLAGE_TYPE)) {
									CollageManager.getInstance().removeCollageById(productInfo.ProductId) ;
									
								}

								int children = 0;
								for (int i = 0; i < childItemList.size(); i++) {
									if (childItemList.get(i) != null)
										children += childItemList.get(i).size();
								}
								if (children == 0) {
									Intent myIntent = new Intent(context, MainMenu.class);
//									myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									AppManager.getAppManager().finishAllActivity();
									
									context.startActivity(myIntent);
									PrintHelper.clearDataForDoMore();
									((Activity) context).finish() ;
									return;
								}
								/*
								 * expandableListAdapter =
								 * ((ExpandableListAdapter)
								 * expandableList.getExpandableListAdapter());
								 * expandableListAdapter.notifyDataSetChanged();
								 */
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							dialog.dismiss();
							saveProductInfoListData();
							mListener.refreshExpandableList();
							refreshSubTotal();
						}
					});
					builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();

				}

			};

		}

		return listener;
	}

	public void setExpandableListData() {
		appContex.setChildItemList(childItemList);
		appContex.setGroupItemList(groupItemList);
	}

	public void saveProductInfoListData() {
		if (!PrintHelper.isClearDataForDoMore){
			List<ProductInfo> productInfoList = new ArrayList<ProductInfo>();
			for (List<ProductInfo> temp : childItemList) {
				for (ProductInfo pro : temp) {
					productInfoList.add(pro);
				}
			}
			appContex.setProductInfos(productInfoList);
			appContex.setChildItemList(childItemList);
			appContex.setGroupItemList(groupItemList);
			mDesIDs= new ShoppingCartActivity().getDesIDs(AppContext.getApplication().getProductInfos());
		}
		PrintHelper.isClearDataForDoMore = false; //reset the value
	}

	/**
	 * Simple static adapter to use for image thumbnails.
	 */
	public final ImageWorkerAdapter imageThumbWorkerUrlsAdapter = new ImageWorkerAdapter() {
		int groupPosition = 0;

		@Override
		public Object getItem(int groupPosition, int childPosition) {
			this.groupPosition = groupPosition;
			return childItemList.get(groupPosition).get(childPosition);
		}

		@Override
		public int getSize() {
			return childItemList.get(groupPosition).size();
		}
	};
	
	private GreetingCardManager getGreetingCarManager (String productId){
		GreetingCardManager manager = null;
		for (GreetingCardManager managersTemp : mGreetingCardManagers){
			if (null == managersTemp.getGreetingCardProductCardProduct() || null == managersTemp.getGreetingCardProductCardProduct().id){
				continue;
			}
			if (managersTemp.getGreetingCardProductCardProduct().id.equals(productId)){
				manager = managersTemp;
			}	
		}
		return manager;
	}
	
	private boolean isShowsMSRPPricing(){
		boolean isDMC = context.getPackageName().contains(MainMenu.DM_COMBINED_PACKAGE_NAME);
		if (isDMC){
			return true;
		}
		String temValue = "";
		boolean isShowsMSRPPricing = false;
		if (PrintHelper.selectedCountryInfo != null) {
			CountryInfo countryInfo = PrintHelper.selectedCountryInfo;
			if (countryInfo != null) {
				temValue = countryInfo.countryAttributes.get("ShowsMSRPPricing");
				if (temValue.equals("true")){
					isShowsMSRPPricing = true;
				}
			}
		}
		return isShowsMSRPPricing;
	}
	
	/**
	 * remove the "*" from the text.
	 * add by song
	 */
	private void changeSubtotalTest(){
		String subTotalText = this.context.getString(R.string.orderSubtotal);
		prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
		boolean TaxWillBeCalculatedByRetailer = prefs.getBoolean("TaxWillBeCalculatedByRetailer", false);
		if(!TaxWillBeCalculatedByRetailer){
			subTotalText = subTotalText.replace("*", "");
		}
		orderSubtotalLabelTV.setText(subTotalText);
	}
	
	/**
	 * 
	 * add by song
	 */
	private boolean  checkStoreAvailable() {
		boolean isAvailable = true;
		String StorId = prefs.getString("selectedStoreId", "");
		PrintMakerWebService service = new PrintMakerWebService(context, "");
		isAvailable = service.checkStores("", StorId, mDesIDs);	
		if (!isAvailable && ShoppingCartActivity.isShoppingCardLive){
			showStoreAvailableDialog();
		}		
		return isAvailable;
	}
	
	private void showStoreAvailableDialog() {
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (storeAvailableDialog != null && storeAvailableDialog.isShowing()){
					return;
				}
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(context);
				builder.setTitle("");
				builder.setMessage(context.getString(R.string.product_not_available_at_store));
				builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PrintHelper.stores.clear();
						Intent intent = new Intent(context, StoreFinder.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("productStringCheckStore", new ShoppingCartActivity().getDesIDs(AppContext.getApplication().getProductInfos()));
						intent.putExtra(AppConstants.IS_FORM_SHOPPINGCART, true);
						context.startActivity(intent);
						dialog.dismiss();
					}
				});
				builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						okToCheckPrices = true;
						dialog.dismiss();
					}
				});
				storeAvailableDialog = builder.create();
				storeAvailableDialog.show();					
				subtotalProgress.setVisibility(View.INVISIBLE);
			}
		});		
	}
	
	private void showCanNotGetPriceDialog() {
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (canNotGetPriceDialog != null && canNotGetPriceDialog.isShowing()){
					return;
				}
				InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(context);
				builder.setTitle("");
				builder.setMessage(context.getString(R.string.N2RShoppingCart_ErrorGettingPrice));
				builder.setPositiveButton(context.getString(R.string.Back), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				canNotGetPriceDialog = builder.create();
				canNotGetPriceDialog.show();					
				subtotalProgress.setVisibility(View.INVISIBLE);
			}
		});		
	}
	
	public Cart getCart() {
		return cart;
	}
	
	public String getCartId(){
		return cartId;
	}
	
	public String getProductIDs(){
		return productsStr;
	}
	
	private Bitmap getThumbnailById(String ProductId){
		String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER+ "/";
		String priviewFilePath = tempFolder +ProductId + "Priview" + ".temp";
		Bitmap bitmap = ImageUtil.decodeSampledBitmapFromFile(priviewFilePath,
				AppConstants.THUMNAIL_STANDARD_WIDTH, AppConstants.THUMNAIL_STANDARD_HEIGHT) ;
		return bitmap;
	}
}
