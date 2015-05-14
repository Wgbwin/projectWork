package com.kodakalaris.kodakmomentslib.activity.sendingorder;

import java.lang.ref.WeakReference;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kodakalaris.kodakmomentslib.AppConstants;
import com.kodakalaris.kodakmomentslib.AppManager;
import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.activity.orderconfirmation.MOrderConfirmationActivity;
import com.kodakalaris.kodakmomentslib.bean.LocalCustomerInfo;
import com.kodakalaris.kodakmomentslib.bean.OrderDetail;
import com.kodakalaris.kodakmomentslib.bean.PhotoInfo;
import com.kodakalaris.kodakmomentslib.bean.items.PrintItem;
import com.kodakalaris.kodakmomentslib.bean.items.ShoppingCartItem;
import com.kodakalaris.kodakmomentslib.culumus.api.GeneralAPI;
import com.kodakalaris.kodakmomentslib.culumus.bean.retailer.Retailer;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.Cart;
import com.kodakalaris.kodakmomentslib.culumus.bean.shoppingcart.NewOrder;
import com.kodakalaris.kodakmomentslib.culumus.bean.storelocator.StoreInfo;
import com.kodakalaris.kodakmomentslib.exception.WebAPIException;
import com.kodakalaris.kodakmomentslib.manager.ShoppingCartManager;
import com.kodakalaris.kodakmomentslib.util.UploadProgressUtil;
import com.kodakalaris.kodakmomentslib.widget.mobile.UploadPhotoErrorDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MSendingOrderActivity extends BaseSendingOrderActivity {
	private static final String TAG = "MSendingOrderActivity";
	private UploadingPhotoReceiver mUploadingPhotoReceiver;
	private ImageView vImgSendingImage;
	private ProgressBar vProgressBar;
	private TextView vTxtSendingProgress;
	private TextView vTxtSendingPrompts;
	
	private UpdateUploadPhotoHander handler ;
	private GeneralAPI mGeneralAPI;
	private static final int HANDLER_UPLOADING = 0;
	private static final int HANDLER_UPLOAD_SUCCESS = 1 ;
	private static final int HANDLER_UPLOAD_FAILED = 2;
	
//	private int uploadErrorCounter;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
	    filter.addAction(AppConstants.UPLOAD_PHOTO_ACTION);
		registerReceiver(mUploadingPhotoReceiver, filter);
		setInitState();
	}
	@Override
	protected void setKMContentView() {
		setContentView(R.layout.activity_m_sending_order);
	}
	
	@Override
	protected void initViews() {
		vImgSendingImage = (ImageView) findViewById(R.id.img_sending_image);
		vProgressBar = (ProgressBar) findViewById(R.id.progressbar_sending);
		vTxtSendingProgress = (TextView) findViewById(R.id.txt_sending_progress);
		vTxtSendingPrompts = (TextView) findViewById(R.id.txt_sending_prompts);
	}
	
	@Override
	protected void setEvents() {
		
		
	}
	@Override
	protected void initData() {
		
		mUploadingPhotoReceiver = new UploadingPhotoReceiver();
		handler =new UpdateUploadPhotoHander(this);
		mGeneralAPI=new GeneralAPI(this);
	}
	
	
	private void setInitState(){
		updateUploadingImageView();
		updateUploadingProgress();
	}
	
	
	
	private void updateUploadingImageView(){
		PhotoInfo currentUploadingPhoto = UploadProgressUtil.getCurrentUploadingPhoto(MSendingOrderActivity.this);
		if(currentUploadingPhoto!=null){
			vImgSendingImage.setVisibility(View.VISIBLE);
			ImageLoader.getInstance().displayImage("file://"+currentUploadingPhoto.getPhotoPath(), vImgSendingImage);
		}
	}
	
	private void updateUploadingProgress(){
		List<PhotoInfo> allPhotos =  UploadProgressUtil.getAllUploadImages(MSendingOrderActivity.this); 
		int successNum  = UploadProgressUtil.getUploadPicSuccessNum(allPhotos);
		int total  = allPhotos.size();
		int uploadErrorCounter =  UploadProgressUtil.getUploadFailedPhotos(MSendingOrderActivity.this).size();
		
		if(successNum == total){
			// all photos have uploaded, go to sending order
			vImgSendingImage.setVisibility(View.INVISIBLE);
			vTxtSendingProgress.setVisibility(View.INVISIBLE);
			vProgressBar.setProgress((int)(successNum*1.00/total*100*0.8));
			vTxtSendingPrompts.setText(R.string.SendingOrder_preparingOrder);
		
			try {
				unregisterReceiver(mUploadingPhotoReceiver);
			} catch (Exception e) {
				
			}
			//do convert order
			ConvertToOrderTask convertToOrderTask = new ConvertToOrderTask();
			convertToOrderTask.execute();
			
			
		}else{
			//TODO Need handler upload failed
			
			if(uploadErrorCounter+successNum == total){
				try {
					unregisterReceiver(mUploadingPhotoReceiver);
				} catch (Exception e) {
					
				}
				//show error dialog
			    List<PhotoInfo> uploadFailedPhotos =UploadProgressUtil.getUploadFailedPhotos(MSendingOrderActivity.this) ;
				
				UploadPhotoErrorDialog errorDialog = new UploadPhotoErrorDialog(MSendingOrderActivity.this,uploadFailedPhotos);
				errorDialog.show(getSupportFragmentManager(), "upload error");
					
				
				
			}
				vTxtSendingProgress.setVisibility(View.VISIBLE);
				vTxtSendingProgress.setText(getString(R.string.SendingOrder_sendingProgress,successNum, total));
				vTxtSendingPrompts.setText(R.string.SendingOrder_sendingPhotos);
				vProgressBar.setProgress((int)(successNum*1.00/total*100*0.8));
			
			
		}
	}
	
    @Override
    public void onBackPressed() {
    	return;
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(mUploadingPhotoReceiver);
		} catch (Exception e) {
			
		}
	}
	
	class UploadingPhotoReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(AppConstants.UPLOAD_PHOTO_ACTION.equals(action)){
				PhotoInfo photo = (PhotoInfo) intent.getSerializableExtra(AppConstants.UPLOAD_PHOTO_FLAG);
				Message msg = handler.obtainMessage();
				if(photo.getPhotoUploadingState().isUploading()){
					msg.what = HANDLER_UPLOADING;
					msg.obj = photo;
					
				}else if(photo.getPhotoUploadingState().isUploadedSuccess()){
					msg.what = HANDLER_UPLOAD_SUCCESS;
					msg.obj = photo;
					
				}else if (photo.getPhotoUploadingState().isUploadedFailed()){
					msg.what = HANDLER_UPLOAD_FAILED;
					msg.obj = photo;
					
				}
				
				msg.sendToTarget();
			
			}
			
		}
		
	}
	
	static class UpdateUploadPhotoHander extends Handler{
		WeakReference<MSendingOrderActivity> mActivity;  
		public UpdateUploadPhotoHander(MSendingOrderActivity activity) {
			mActivity = new WeakReference<MSendingOrderActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			MSendingOrderActivity theActivity = mActivity.get(); 
			
			switch (msg.what) {
			case HANDLER_UPLOADING :
				theActivity.updateUploadingImageView();
				break;
			case HANDLER_UPLOAD_SUCCESS:
				theActivity.updateUploadingProgress();
				break;
			case HANDLER_UPLOAD_FAILED:
//				PhotoInfo photoError = (PhotoInfo) msg.obj;
//				if(UploadProgressUtil.getAllUploadImages(theActivity).contains(photoError)){
//					theActivity.errorCounterPlusPlus();
//				}
				theActivity.updateUploadingProgress();
				
				break;
			default:
				break;
			}
		}
	}
	
	class ConvertToOrderTask extends AsyncTask<Void, Void, OrderDetail>{

		@Override
		protected OrderDetail doInBackground(Void... params) {
			OrderDetail orderDetail = null;
			Cart cart = ShoppingCartManager.getInstance().getCart();
			String cartId = cart.cartId;
			JSONArray productArray = new JSONArray();
			List<ShoppingCartItem> items =  ShoppingCartManager.getInstance().getShoppingCartItems();
			if(items!=null && items.size()>0){
				for (ShoppingCartItem shoppingCartItem : items) {
					if(shoppingCartItem instanceof PrintItem){
						PhotoInfo photoInfo = ((PrintItem) shoppingCartItem).getImage();
						
						JSONObject printObj = new JSONObject();
						try {
							printObj.put("ProductId", shoppingCartItem.getServerId());
							printObj.put("ImageId", photoInfo.getImageResource().id);
							JSONObject ROIObj = new JSONObject();
							ROIObj.put("X", ((PrintItem) shoppingCartItem).getRoi().x);
							ROIObj.put("Y", ((PrintItem) shoppingCartItem).getRoi().y);
							ROIObj.put("W", ((PrintItem) shoppingCartItem).getRoi().w);
							ROIObj.put("H", ((PrintItem) shoppingCartItem).getRoi().h);
							ROIObj.put("ContainerW", 0);
							ROIObj.put("ContainerH", 0);
							printObj.put("ImageROI", ROIObj);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						productArray.put(printObj);
					}
					
				}
			}
			
			String postDataJsonString = productArray.toString();
			Log.v(TAG, "sunny: postDataJsonString: "+ postDataJsonString);
			NewOrder result = null;
			try {
				result = mGeneralAPI.convertToOrderTask3(cartId, postDataJsonString);
			} catch (WebAPIException e) {
				e.printStackTrace();
				result = null;
			}
			
			if(result != null){
				orderDetail = new OrderDetail(MSendingOrderActivity.this);
				orderDetail.setOrderId(result.orderId);
				orderDetail.setCustomerInfo(new LocalCustomerInfo(MSendingOrderActivity.this));
				orderDetail.setStoreInfo(StoreInfo.loadSelectedStore(MSendingOrderActivity.this));
				try {
					String strCart = mGeneralAPI.getCartStrTask(cartId);
					if(strCart != null){
						orderDetail.setCart(strCart);
					}
				} catch (WebAPIException e) {
					e.printStackTrace();
				}
			}
			
			return orderDetail;
		}
		
		@Override
		protected void onPostExecute(OrderDetail result) {
			if(result!=null){
				Retailer currentRetailer = ShoppingCartManager.getInstance().getCurrentRetailer();
				if(currentRetailer!=null){
					if(currentRetailer.payOnline){
						//TODO go to pay online
					}else{
						result.save(MSendingOrderActivity.this);
						// go to order confirmation
						Intent intent  = new Intent(MSendingOrderActivity.this, MOrderConfirmationActivity.class);
						intent.putExtra(AppConstants.KEY_ORDER, result.getOrderId());
						startActivity(intent);
						finish();
					}
				}
				
			}else {
				Toast.makeText(MSendingOrderActivity.this, "convert order failed", Toast.LENGTH_LONG).show();
			}
		}
	}
	
}
