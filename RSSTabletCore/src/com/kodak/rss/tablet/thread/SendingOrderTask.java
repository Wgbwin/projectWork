package com.kodak.rss.tablet.thread;

import java.util.List;

import android.content.Context;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.LocalCustomerInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.exception.RssWebServiceException;
import com.kodak.rss.core.n2r.bean.prints.StandardPrint;
import com.kodak.rss.core.n2r.bean.shoppingcart.Cart;
import com.kodak.rss.core.n2r.bean.shoppingcart.CartItem;
import com.kodak.rss.core.n2r.bean.shoppingcart.NewOrder;
import com.kodak.rss.core.n2r.bean.storelocator.StoreInfo;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.core.util.EncryptUtil;
import com.kodak.rss.core.util.Log;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.UploadProgressUtil;

public class SendingOrderTask implements Runnable{
	private final String TAG = SendingOrderTask.class.getSimpleName();
	
	private int orderType = 0;
	public static final int ORDER_TYPE_DEFAULT = 0;
	public static final int ORDER_TYPE_STORE = 1;
	public static final int ORDER_TYPE_HOME = 2;
	
	private Context mContext;
	private SendingOrderTaskListener listener;
	private List<ImageInfo> images;
	private WebService webService;
	
	private List<StandardPrint> prints;
	private Cart cart;
	private CartItem[] cartItems;
	private LocalCustomerInfo customerInfo;
	private StoreInfo store;
	private RssTabletApp app;
	private boolean canceled = false;
	
	public SendingOrderTask(Context context, int orderType, SendingOrderTaskListener listener){
		this.orderType = orderType;
		this.listener = listener;
		mContext = context;
		app = RssTabletApp.getInstance();
		images = UploadProgressUtil.allImages();		
		customerInfo = new LocalCustomerInfo(context);
		store = StoreInfo.loadSelectedStore(context);
		webService = new WebService(context);
	}

	@Override
	public void run() {		
		listener.onSendingOrderStart();
		if(orderType == ORDER_TYPE_STORE){
			Log.i(TAG, "Store Order");
			try {
				boolean validOrder = webService.checkStoreTask(store.retailerID, store.id, ShoppingCartUtil.getProductDescriptionIDs(app.products));
				if(!validOrder){
					listener.onStoreNotSupportAllProducts();
					return;
				}
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			uploadOrder(ORDER_TYPE_STORE);
		} 
		else if(orderType == ORDER_TYPE_HOME){
			Log.i(TAG, "Home Order");
			uploadOrder(ORDER_TYPE_HOME);
		} 
		else {
			Log.e("TAG", "Order Error... unrecognized order type.");
		}
	}
	
	private void uploadOrder(int orderType){
		final int totalStep = app.getCouponCode().equals("") ? 6 : 7;
		int step = 0;
		listener.progress(step, totalStep);
		while(UploadProgressUtil.isImageUploading(images,false)){
			if(canceled){
				return;
			}
			listener.progress(step, totalStep);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(canceled){
			return;
		}
		step ++;
		
		if(!"".equals(app.getLastFailedCartID())){
			try {
				cart = webService.removeAllProductsTask(app.getLastFailedCartID());
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			
			if(cart == null){
				listener.onTaskFailed(CREATE_CART);
				return;
			}
			cart = null;
		}
		
		// 1st: Create Prints Task
		boolean containPrints = UploadProgressUtil.isContainsPrints(app.products);
		if(containPrints){
			try {
				prints = webService.createStandardPrintsTask(app.products);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			if(prints == null){
				listener.onTaskFailed(CREATE_PRINT);
				return;
			}
			listener.progress(step, totalStep);
		}
		
		// 2nd: Create Cart Task
		if(cart == null){
			try {
				cart = webService.createCartTask();
				app.setLastFailedCartID(cart.cartId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
		}
		if(cart == null){
			listener.onTaskFailed(CREATE_CART);
			return;
		}
		listener.progress(step, totalStep);
		step ++;
		
		Cart tempCart = null;
		// 3rd: Set Retailer ID Task
		if(orderType == ORDER_TYPE_HOME){
			String homeRetailerId = app.getHomeDeliveryRetailerId(app.getRetailers());
			try {
				tempCart = webService.setRetailerIdTask(cart.cartId, homeRetailerId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			if(tempCart == null){
				listener.onTaskFailed(SET_RETAILER);
				return;
			} else {
				cart = tempCart;
			}
		} else {
			try {
				tempCart = webService.setStoreTask(cart.cartId, store.retailerID, store.id);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
			if(tempCart == null){
				listener.onTaskFailed(SET_STORE);
				return;
			} else {
				cart = tempCart;
			}
		}
		listener.progress(step, totalStep);
		step ++;
		
		// 4th: Set Customer Info Task
		boolean isShipToHome = orderType==ORDER_TYPE_HOME ? true : false;
		try {
			tempCart = webService.setCustomerTask(cart.cartId, isShipToHome, customerInfo);
		} catch (RssWebServiceException e1) {
			e1.printStackTrace();
		}
		if(tempCart == null){
			listener.onTaskFailed(SET_CUS_INFO);
			return;
		} else {
			cart = tempCart;
		}
		listener.progress(step, totalStep);
		step ++;
		
		// add coupon to cart
		if(!app.getCouponCode().equals("")){
			try {
				tempCart = webService.checkCouponCodeTask(cart.cartId, app.getCouponCode());
			} catch (RssWebServiceException e2) {
				e2.printStackTrace();
			}
			if(tempCart == null){
				listener.onTaskFailed(SET_COUPON);
				return;
			} else {
				cart = tempCart;
			}
			listener.progress(step, totalStep);
			step ++;
		}
		
		// 5th: Add Products Task
		try {
			cartItems = webService.addPrintProductsTask(cart.cartId, app.products, prints);
		} catch (RssWebServiceException e1) {
			e1.printStackTrace();
		}
		if(cartItems == null){
			removeProducts();
			listener.onTaskFailed(ADD_PRO);
			return;
		}
		listener.progress(step, totalStep);
		step ++;
		
		// 6th: Convert To Order Task
		NewOrder order = null;
		if(cartItems != null){
			try {
				order = webService.convertToOrderTask(cart.cartId);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
		}
		
		if(order != null){
			try {
				cart = webService.getCartTask(cart.cartId);
			} catch (RssWebServiceException e1) {
				e1.printStackTrace();
			}
			if(orderType == ORDER_TYPE_HOME){
				try {
					String url = getPayOnlineURL(mContext, order);
					listener.onHomeOrderTaskSucceed(order, url, cart);
				} catch (Exception e) {
					e.printStackTrace();
					removeProducts();
					listener.onTaskFailed(CONVERT_ORDER);
				}
			} else {
				String url = "";
				try {
					url = getPayOnlineURL(mContext, order);
				} catch (Exception e) {
					e.printStackTrace();
				}
				listener.onStoreOrderTaskSucceed(order, url, cart);
			}
		} else {
			removeProducts();
			listener.onTaskFailed(CONVERT_ORDER);
		}
	}
	
	private String getPayOnlineURL(Context context, NewOrder order) throws Exception{
		EncryptUtil encrypt = new EncryptUtil();
		String appname = mContext.getString(R.string.cumulus_appid);
		String needToEncrypt = "orderId=" + order.orderId + "&locale=en_US&brand=Kodak&appname=" + appname;
		String key = mContext.getString(R.string.encrypt_key).replace("-", "");
		String iv = mContext.getString(R.string.encrypt_iv).replace("-", "");
		byte[] bytes = encrypt.encrypt(needToEncrypt.getBytes(), encrypt.generateKey(key, 32), encrypt.generateKey(iv, 16));
		String result = encrypt.encodeResult(bytes);
		String url = mContext.getString(R.string.cumulus_paymenturl) + "PaymentBegin.aspx?info=" + result;
	
		String currentServer = mContext.getString(R.string.cumulus_check_internet);
		String firstName = SharedPreferrenceUtil.getString(mContext, SharedPreferrenceUtil.BACK_DOOR_NAME);
		if(firstName.equals("RSS_Staging")){
			url = url.replace(currentServer, "mykodakmomentsstage.kodak.com");
		}else if(firstName.equals("RSS_Production")){
			url = url.replace(currentServer, "mykodakmoments.kodak.com");
		}else if(firstName.equals("RSS_Development")){
			url = url.replace(currentServer, "rssdev.kodak.com");
		}else if("RSS_Env1".equalsIgnoreCase(firstName)){
			url = url.replace(currentServer, "rssdev1.kodak.com");
		}else if("RSS_Env2".equalsIgnoreCase(firstName)){
			url = url.replace(currentServer, "rssdev2.kodak.com");
		}
		return url;
	}
	
	public void cancelTask(){
		this.canceled = true;
	}
	
	private void removeProducts(){
		if(app.products != null){
			try {
				webService.removeProductsTask(cart.cartId, app.products, prints);
			} catch (RssWebServiceException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create Cart Task Error
	 */
	public static int CREATE_CART = 1;
	/**
	 * Set Store ID Task Error
	 */
	public static int SET_STORE = 2;
	/**
	 * Set Customer Info Task Error
	 */
	public static int SET_CUS_INFO = 3;
	/**
	 * Create Prints Task Error
	 */
	public static int CREATE_PRINT = 4;
	/**
	 * Add Products Task Error
	 */
	public static int ADD_PRO = 5;
	/**
	 * Convert Order Task Error
	 */
	public static int CONVERT_ORDER = 6;
	/**
	 * Set Retailer ID Task Error
	 */
	public static int SET_RETAILER = 7;
	/**
	 * Clone Image ID Task Error
	 */
	public static int CLONE_IMAGE = 8;
	
	public static final int SET_COUPON = 9;
	
	public interface SendingOrderTaskListener{
		void progress(int step, int totalStep);
		void onSendingOrderStart();
		void onStoreOrderTaskSucceed(NewOrder order, String url, Cart cart);
		void onStoreNotSupportAllProducts();
		void onHomeOrderTaskSucceed(NewOrder order, String url, Cart cart);
		/**
		 * <B><I>before you get this error, the products will be removed from server
		 * @param errorCode
		 */
		void onTaskFailed(int errorCode);
		void onTaskCanceled();
	}
	
	private static final int homeOrderSteps = 6;
	private static final int storeOrderSteps = 6;

	public static int getHomeOrderSteps(List<ProductInfo> products) {
		if(!UploadProgressUtil.isContainsPrints(products)){
			return homeOrderSteps -1;
		}
		return homeOrderSteps;
	}

	public static int getStoreOrderSteps(List<ProductInfo> products) {
		if(!UploadProgressUtil.isContainsPrints(products)){
			return storeOrderSteps -1;
		}
		return storeOrderSteps;
	}
	
}
