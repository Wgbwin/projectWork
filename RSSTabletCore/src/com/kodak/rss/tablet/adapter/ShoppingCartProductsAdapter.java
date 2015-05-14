package com.kodak.rss.tablet.adapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.bean.ProductInfo;
import com.kodak.rss.core.n2r.bean.shoppingcart.Discount;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing;
import com.kodak.rss.core.n2r.bean.shoppingcart.Pricing.LineItem;
import com.kodak.rss.core.util.DimensionUtil;
import com.kodak.rss.core.util.ImageUtil;
import com.kodak.rss.tablet.AppConstants;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.ShoppingCartActivity;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.ShoppingCartUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.OnProcessResponseEndListener;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;

public class ShoppingCartProductsAdapter extends BaseExpandableListAdapter {
	private static final String TAG = ShoppingCartProductsAdapter.class.getSimpleName();
	
	private Context context;
	private LayoutInflater mLayoutInflater;
	private ArrayList<ProductInfo> proInfos;
	private ArrayList<String> products = new ArrayList<String>();
	private ArrayList<List<ProductInfo>> groupProducts = new ArrayList<List<ProductInfo>>();
	
	private ShoppingCartListener listener;
	private boolean pricing = false;
	private Pricing productPrice = null;
	private ScrollView sv;
	
	private Bitmap waitBitmap;
	public ImageUseURIDownloader imageDownloader;	
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	private boolean showShippingAndHandling = false;
	//private boolean isShowsMSRPPricing = false; //if the price will display
	private boolean isGetPrice = true; //if get the price form server
	
	private LruCache<String, Bitmap> cache;  	
	private Discount discount = null;

	public ShoppingCartProductsAdapter(Context context,LruCache<String, Bitmap> mMemoryCache, List<ProductInfo> proInfos, ShoppingCartListener listener, ExpandableListView elv, ScrollView sv){
		this.context = context;
		this.cache = mMemoryCache;		
		mLayoutInflater = LayoutInflater.from(context);
		this.proInfos = (ArrayList<ProductInfo>) proInfos;
		this.listener = listener;
		this.sv = sv;		
		this.waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
		initData(proInfos);
	}
	
	public void initData(List<ProductInfo> proInfos){
		products = (ArrayList<String>) ShoppingCartUtil.getProductDescriptionIDList(proInfos);
		initGroupProductsList();
	}
	
	private void initGroupProductsList(){
		groupProducts.clear();
		for(String type : products){
			groupProducts.add(ShoppingCartUtil.getGroupProductInfoList(proInfos, type));
		}
	}
	
	public int getProductGroupCount(){
		return products==null ? 0 : products.size();
	}
	
	@Override
	public int getGroupCount() {
		return products.size() + extraGroupNumber();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int extraGroup = extraGroupNumber();
		if(extraGroup>0){
			if(groupPosition<extraGroup){
				return 0;
			}
		}
		return groupProducts.get(groupPosition-extraGroup).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	private int extraGroupNumber(){
		int num = 0;
		if(showShippingAndHandling){
			num ++;
		}
		if(discount != null){
			num ++;
		}
		return num;
		
	}
	
	private void updateShippingAndHandlingGroup(GroupHolder holder){
		holder.tvTitle.setText(R.string.OrderSummary_ShippingAndHandling);
		if(!pricing && productPrice!=null){
			holder.tvPrice.setText(productPrice.shipAndHandling.priceStr);
		}
		holder.tvRoundabout.setVisibility(View.VISIBLE);
		holder.couponRemove.setVisibility(View.INVISIBLE);
		holder.tvCouponTerms.setVisibility(View.INVISIBLE);
		holder.tvRoundabout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onRoundaboutSelected();
			}
		});
	}
	
	private void updateDiscountGroup(GroupHolder holder){
		holder.tvTitle.setText(discount.code+" - "+discount.localizedStatusDescription);
		String showPrice = "";
		if (productPrice!=null && productPrice.totalSavings != null && !"".equals(productPrice.totalSavings.priceStr)) {
			showPrice = productPrice.totalSavings.priceStr;
		}						
		holder.tvPrice.setText(showPrice);
		holder.couponRemove.setVisibility(View.VISIBLE);				
		holder.tvRoundabout.setVisibility(View.INVISIBLE);				
		if (discount.termsAndConditionsURL != null && !"".equals(discount.termsAndConditionsURL)) {
			holder.tvCouponTerms.setVisibility(View.VISIBLE);
		}else {
			holder.tvCouponTerms.setVisibility(View.INVISIBLE);
		}				
		holder.couponRemove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				discount = null;
				listener.onRemoveCouponSelected();
				notifyDataSetChanged();
			}
		});
		holder.tvCouponTerms.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((ShoppingCartActivity)context).showCouponTerms(discount.termsAndConditionsURL);
			}
		});
	}
	
	private void updateExtraGroupStatus(GroupHolder holder){
		if(!pricing && productPrice!=null){	
			holder.tvTitle.setTextColor(holder.tvShowDetails.getTextColors());
			if(pricing || productPrice==null){
				holder.pbWaiting.setVisibility(View.VISIBLE);
				holder.tvPrice.setVisibility(View.INVISIBLE);
			} else {
				holder.pbWaiting.setVisibility(View.GONE);
				holder.tvPrice.setVisibility(View.VISIBLE);
			}
		}
		holder.btShowDetails.setVisibility(View.INVISIBLE);
		holder.tvShowDetails.setVisibility(View.INVISIBLE);
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder holder = null;
		isGetPrice = RssTabletApp.getInstance().needGetPriceFromServer;
		if(convertView == null){
			holder = new GroupHolder();
			convertView = mLayoutInflater.inflate(R.layout.shoppingcart_item, null);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_prodcut_TypeAndCount);
			holder.btShowDetails = (ImageView) convertView.findViewById(R.id.iv_show_detail);
			holder.tvShowDetails = (TextView) convertView.findViewById(R.id.tv_show_detail);
			holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
			holder.pbWaiting = (ProgressBar) convertView.findViewById(R.id.pb_waiting);
			holder.tvRoundabout = (TextView) convertView.findViewById(R.id.tv_roundabout);
			
			holder.couponRemove = (TextView) convertView.findViewById(R.id.coupon_remove);
			holder.tvCouponTerms = (TextView) convertView.findViewById(R.id.tv_coupon_terms);
						
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}
		convertView.setOnTouchListener(null);
		int productIndex = groupPosition - extraGroupNumber();
		// the 1st group may be Shipping and Handling or Discount
		if(extraGroupNumber()==1 && groupPosition==0){
			updateExtraGroupStatus(holder);
			if(showShippingAndHandling){
				updateShippingAndHandlingGroup(holder);
			} else {
				updateDiscountGroup(holder);
			}
		} 
		// when need show both shipping and handling and discount, 1st group should be discount
		else if(extraGroupNumber()==2 && groupPosition==0){
			updateExtraGroupStatus(holder);
			updateDiscountGroup(holder);
		} 
		// when need show both shipping and handling and discount, 2nd group should be shipping and handling
		else if(extraGroupNumber()==2 && groupPosition==1){
			updateExtraGroupStatus(holder);
			updateShippingAndHandlingGroup(holder);
		} 
		// show the product items
		else {
			holder.couponRemove.setVisibility(View.INVISIBLE);			
			holder.tvCouponTerms.setVisibility(View.INVISIBLE);
			// using R.color.blue_shppingcart the text always disappeared.
			holder.tvTitle.setTextColor(Color.parseColor("#FF2D95DB"));
			RssTabletApp app = RssTabletApp.getInstance();
			int count = ShoppingCartUtil.getProductsCount(proInfos, products.get(productIndex));
			String productName = ShoppingCartUtil.getProductName(app.getCatalogList(), products.get(productIndex));
			
			holder.tvTitle.setText(count + " - " + productName);
			if(isExpanded){
				holder.btShowDetails.setBackgroundResource(R.drawable.hideoptions_up);
				holder.tvShowDetails.setText(R.string.OrderSummary_HideDetails);
			} else {
				holder.btShowDetails.setBackgroundResource(R.drawable.moreoptions_up);
				holder.tvShowDetails.setText(R.string.OrderSummary_ShowDetails);
			}
			if (isGetPrice){
				if(productPrice != null){
					String strPrice = productPrice.groupItemsPrice(products.get(productIndex));
					holder.tvPrice.setText(strPrice);
				}
				
				if(pricing || productPrice==null){
					holder.pbWaiting.setVisibility(View.VISIBLE);
					holder.tvPrice.setVisibility(View.INVISIBLE);
				} else {
					holder.pbWaiting.setVisibility(View.GONE);
					holder.tvPrice.setVisibility(View.VISIBLE);
				}
			}else {
				holder.pbWaiting.setVisibility(View.GONE);
				holder.tvPrice.setVisibility(View.INVISIBLE);
			}
			holder.btShowDetails.setVisibility(View.VISIBLE);
			holder.tvShowDetails.setVisibility(View.VISIBLE);
			holder.tvRoundabout.setVisibility(View.INVISIBLE);
			convertView.setOnTouchListener(new OnGroupClickListener(productIndex, isExpanded));
		}
			
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder = null;
		if(convertView == null){
			holder = new ChildHolder();
			convertView = mLayoutInflater.inflate(R.layout.shoppingcart_product_item, null);
			holder.ivImage = (ImageView) convertView.findViewById(R.id.iv_product_preview);
			holder.tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
			holder.btMinus = (Button) convertView.findViewById(R.id.bt_minus);
			holder.btPlus = (Button) convertView.findViewById(R.id.bt_plus);
			holder.btEdit = (Button) convertView.findViewById(R.id.bt_edit);
			holder.btRemove = (Button) convertView.findViewById(R.id.bt_delete);
			holder.upProdDetail = convertView.findViewById(R.id.up_proParts);
			holder.downProdDetail = convertView.findViewById(R.id.down_proParts);
			holder.pbBottomWaiting = (ProgressBar) convertView.findViewById(R.id.pb_waiting);
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		
		final int productIndex = groupPosition - extraGroupNumber();
		
		final ProductInfo product = groupProducts.get(productIndex).get(childPosition);
		RssTabletApp app = RssTabletApp.getInstance();
		String productName = ShoppingCartUtil.getProductName(app.getCatalogList(), products.get(productIndex));
		
		if(!pricing && productPrice!=null){
			LineItem currentPhotobookItem = productPrice.getCurrentLineItem(productName, childPosition);
			updateProductDetail(holder, currentPhotobookItem, productIndex, childPosition);
		} else {
			((TextView)holder.upProdDetail.findViewById(R.id.tv_upProPrice)).setText("");
			((TextView)holder.upProdDetail.findViewById(R.id.tv_downProPrice)).setText("");
			
			((TextView)holder.downProdDetail.findViewById(R.id.tv_upProPrice)).setText("");
			((TextView)holder.downProdDetail.findViewById(R.id.tv_downProPrice)).setText("");			
		}
		
		if(product.num == product.quantityIncrement){
			holder.btMinus.setEnabled(false);
			holder.btMinus.setBackgroundResource(R.drawable.minus_dis);
		} else {
			holder.btMinus.setEnabled(true);
			holder.btMinus.setBackgroundResource(R.drawable.minus_up);
		}
		
		holder.btMinus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int quantityIncrement = product.quantityIncrement;
				product.num -= quantityIncrement;
				if (product.num < quantityIncrement) {
					product.num = quantityIncrement;
				}
				listener.onProductsUpdated(product, ACTION_UPDATE);
			}
		});
		
		holder.btPlus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int quantityIncrement = product.quantityIncrement;
				product.num += quantityIncrement;
				listener.onProductsUpdated(product, ACTION_UPDATE);
			}
		});
		
		holder.btRemove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onProductsUpdated(product, ACTION_DELETE);
			}
		});
		
		holder.btEdit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onEdit(groupProducts.get(productIndex).get(childPosition));
			}
		});		
		
		//for get the edit result changed by bing wang on 2-21-2014
		String cacheKey = product.displayImageUrl;
		if (AppConstants.printType.equals(product.productType)) {
			cacheKey = product.chosenImageList.get(0).id+product.category;
		}	
		Bitmap bitmap = MemoryCacheUtil.getBitmap(cache, cacheKey);		
		if(bitmap == null){
			generateBitmap(context, product, holder.ivImage, childPosition);			
		} else {
			holder.ivImage.setImageBitmap(bitmap);
		}
		holder.tvNumber.setText(product.num + "");
		
		return convertView;
	}
	
	private void updateProductDetail(ChildHolder childHolder, LineItem lineItem, int productIndex, int childIndex) {
		RssTabletApp app = RssTabletApp.getInstance();
		//int count = ShoppingCartUtil.getProductsCount(proInfos, products.get(productIndex));
		String productName = ShoppingCartUtil.getProductName(app.getCatalogList(), products.get(productIndex));
		if(lineItem!=null && lineItem.included!=null && lineItem.included.size()>0){
			childHolder.downProdDetail.setVisibility(View.VISIBLE);
			if(lineItem.included.size() == 1) {
				childHolder.upProdDetail.setVisibility(View.GONE);
				String upName = lineItem.quantity + " - " + productName;
				String upPrice = productPrice.getProductSubTotalPrice(productName, childIndex);
				String downName = lineItem.included.get(0).quantity + " - " + lineItem.included.get(0).name;
				String downPrice = lineItem.included.get(0).totalPrice.priceStr;
				
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_upProName)).setText(upName);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_upProPrice)).setText(upPrice);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_downProName)).setText(downName);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_downProPrice)).setText(downPrice);
			} else if(lineItem.included.size() == 3) {
				childHolder.upProdDetail.setVisibility(View.VISIBLE);
				// du means down part but up name. 
				String uuName = lineItem.quantity + " - " + productName;
				String uuPrice = productPrice.getProductSubTotalPrice(productName, childIndex);
				String udName = lineItem.included.get(0).quantity + " - " + lineItem.included.get(0).name;
				String udPrice = lineItem.included.get(0).totalPrice.priceStr;
				((TextView)childHolder.upProdDetail.findViewById(R.id.tv_upProName)).setText(uuName);
				((TextView)childHolder.upProdDetail.findViewById(R.id.tv_upProPrice)).setText(uuPrice);
				((TextView)childHolder.upProdDetail.findViewById(R.id.tv_downProName)).setText(udName);
				((TextView)childHolder.upProdDetail.findViewById(R.id.tv_downProPrice)).setText(udPrice);
				
				String duName = lineItem.included.get(1).quantity + " - " + lineItem.included.get(1).name;
				String duPrice = lineItem.included.get(1).totalPrice.priceStr;
				String ddName = lineItem.included.get(2).quantity + " - " + lineItem.included.get(2).name;
				String ddPrice = lineItem.included.get(2).totalPrice.priceStr;
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_upProName)).setText(duName);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_upProPrice)).setText(duPrice);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_downProName)).setText(ddName);
				((TextView)childHolder.downProdDetail.findViewById(R.id.tv_downProPrice)).setText(ddPrice);
			}
		} else {
			childHolder.upProdDetail.setVisibility(View.GONE);
			childHolder.downProdDetail.setVisibility(View.GONE);
		}
	}

	public void generateBitmap(Context context,final ProductInfo productInfo,final ImageView view,int position) {
		if (productInfo == null) return;
		if (waitBitmap ==null || waitBitmap.isRecycled()) {
        	waitBitmap= BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
 		}
		view.setImageBitmap(waitBitmap);
		if (AppConstants.printType.equals(productInfo.productType)){
			List<ImageInfo> chosenImageList = productInfo.chosenImageList;
			if (chosenImageList == null) return;
			ImageInfo imageInfo = chosenImageList.get(0);
			if (imageInfo == null) return;
			if (!imageInfo.isfromNative && productInfo.displayImageUrl == null) {
				String originalPath =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, false);
				if (originalPath != null) {
					imageInfo.editUrl = originalPath;	
					imageInfo.uploadOriginalUrl = originalPath;
					productInfo.displayImageUrl = originalPath;									
				}				
			}
			if (productInfo.getRoi() == null) {
				ImageUtil.calculateDefaultRoi(imageInfo, productInfo);
			}	
			if (productInfo.displayImageUrl != null && productInfo.getRoi() != null) {
				directUseUrlNativeAsync(productInfo.displayImageUrl, view, true, productInfo);
				return;
			}else {
				productInfo.displayImageUrl =  FilePathConstant.getLoadFilePath(FilePathConstant.externalType, imageInfo.id, false);
				if (productInfo.displayImageUrl != null) {
					if (productInfo.getRoi() == null) {
						ImageUtil.calculateDefaultRoi(imageInfo, productInfo);
					}
					if (productInfo.getRoi() != null) {
						directUseUrlNativeAsync(productInfo.displayImageUrl, view, true, productInfo);
						return;
					}						
				}else {
					if (productInfo.downloadDisplayImageUrl != null) {
						URI pictureURI = null ;			
						try {
							pictureURI = new URI(productInfo.downloadDisplayImageUrl);
			    		} catch (URISyntaxException e) {
			    			pictureURI = null;
			    		} 			    	
			    		RssTabletApp.getInstance().imageDownloader.downloadProfilePicture(imageInfo.id, pictureURI, null, position, true,false,FilePathConstant.printType,null);
			    		RssTabletApp.getInstance().setOnProcessResponseEndListener(new OnProcessResponseEndListener() {						
							@Override
							public void onProcessEnd(ImageInfo imageInfo,boolean isEdit) {
								if (isEdit) return;
								notifyDataSetChanged();						
							}
						});			
					}
				}
			}			
		}else if (AppConstants.bookType.equals(productInfo.productType) || AppConstants.cardType.equals(productInfo.productType) 
				|| AppConstants.calendarType.equals(productInfo.productType) || AppConstants.collageType.equals(productInfo.productType)) {
			String pathType = "";
			if(AppConstants.bookType.equals(productInfo.productType)){
				pathType = FilePathConstant.bookType;
			} else if(AppConstants.cardType.equals(productInfo.productType)){
				pathType = FilePathConstant.cardType;
			} else if(AppConstants.calendarType.equals(productInfo.productType)){
				pathType = FilePathConstant.calendarType;
			}else if(AppConstants.collageType.equals(productInfo.productType)){
				pathType = FilePathConstant.collageType;
			}
			String dispalyPath =  FilePathConstant.getLoadFilePath(pathType, productInfo.displayImageUrl, true);			
			if (dispalyPath != null) {
				directUseUrlNativeAsync(dispalyPath, view, false, productInfo);
				return;
			}else {
				URI pictureURI = null ;			
				try {
					pictureURI = new URI(productInfo.downloadDisplayImageUrl);
	    		} catch (URISyntaxException e) {
	    			pictureURI = null;
	    		}  
	    		if (pictureURI != null) {
	    			if (this.imageDownloader == null) {
	    				this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests); 
	    				this.imageDownloader.setViewParameters(80, 80);
	    				this.imageDownloader.setOnProcessImageResponseListener(new onProcessImageResponseListener() {					
							@Override
							public void onProcess(Response response, String profileId, View view,int position, String flowType,String productId) {
								if (response == null || pendingRequests == null) return;																					
								if (response.getError() != null) {								
								}else{			
									Bitmap bitmap = response.getBitmap();
									MemoryCacheUtil.putBitmap(cache, profileId, bitmap);									
									if (bitmap != null && view != null && view instanceof ImageView) {
										if (view.getTag().toString().equals(profileId)) {											
											((ImageView) view).setImageBitmap(bitmap);		
										}																												
									}															
								}		
							}
						});
					}	    					    				    				    			
	    			view.setTag(productInfo.displayImageUrl);
	    			boolean isThum = true;
	    			if (AppConstants.calendarType.equals(productInfo.productType) || AppConstants.collageType.equals(productInfo.productType)) {
	    				isThum = false;
					}	    			
	    			this.imageDownloader.downloadProfilePicture(productInfo.displayImageUrl, pictureURI, view,position,true,isThum,pathType,productInfo.cartItemId,pathType);
				}	    	       				
			}
		}	
		return;
	}
	
	private void directUseUrlNativeAsync(final String dispalyUrl, final ImageView view, final boolean isUseOriginal,final ProductInfo productInfo){
		view.setTag(productInfo.displayImageUrl);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final Bitmap bm = directUseUrlNative(dispalyUrl, isUseOriginal, productInfo);
				view.post(new Runnable() {
					
					@Override
					public void run() {
						if(productInfo.displayImageUrl.equals(view.getTag())){
							view.setImageBitmap(bm);
						}
					}
				});
			}
		}).start();	
	}
	
	private Bitmap directUseUrlNative(String dispalyPath,boolean isUseOriginal,ProductInfo productInfo){
		if (dispalyPath == null || productInfo == null){
			if (waitBitmap ==null || waitBitmap.isRecycled()) {
	        	waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
	 		}
			return waitBitmap;
		}

		int rotate = ImageUtil.getDegreesExifOrientation(dispalyPath); 
		BitmapFactory.Options opts = new BitmapFactory.Options();				
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(dispalyPath, opts);
		opts.inSampleSize = opts.outWidth > opts.outHeight ? opts.outHeight/DimensionUtil.dip2px(context, 60) : opts.outWidth/DimensionUtil.dip2px(context, 60);
		opts.inJustDecodeBounds = false;
		opts.inPreferredConfig = Bitmap.Config.RGB_565; 
		Bitmap bitmap = BitmapFactory.decodeFile(dispalyPath, opts);
		if(rotate > 0 && bitmap != null) { 					
			Bitmap rotateBitmap = ImageUtil.rotateBitmap(bitmap,rotate);		             
            if(rotateBitmap != null) {   
            	bitmap.recycle();   
            	bitmap = rotateBitmap;   
             }
		}
		if (bitmap == null){
			if (waitBitmap ==null || waitBitmap.isRecycled()) {
  	        	waitBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.imagewait60x60);
  	 		}
  			return waitBitmap;
         }
		 
		int outHeight = bitmap.getHeight();
        int outWidth = bitmap.getWidth();
		
		if (isUseOriginal && productInfo.getRoi() != null) {
			int width = (int) (outWidth * productInfo.getRoi().w);
			int height = (int) (outHeight * productInfo.getRoi().h);
			int x = (int) (outWidth * productInfo.getRoi().x);
			int y = (int) (outHeight * productInfo.getRoi().y);	
			if ((x + width <= outWidth) && (y + height <= outHeight) ) {				
				bitmap = Bitmap.createBitmap(bitmap, x, y, width > 1 ? width:1 , height> 1 ? height:1);
			}
		}
		String cacheKey = productInfo.displayImageUrl;
		if (AppConstants.printType.equals(productInfo.productType)) {
			cacheKey = productInfo.chosenImageList.get(0).id+productInfo.category;
		}
		MemoryCacheUtil.putBitmap(cache, cacheKey, bitmap);			
		return bitmap;
	}	

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	
	class GroupHolder{
		TextView tvTitle;
		ImageView btShowDetails;
		TextView tvShowDetails;
		TextView tvPrice;
		ProgressBar pbWaiting;
		TextView tvRoundabout;
					
		TextView couponRemove;
		TextView tvCouponTerms;
	}
	
	class ChildHolder{
		ImageView ivImage;
		TextView tvNumber;
		Button btMinus;
		Button btPlus;
		Button btEdit;
		Button btRemove;
		View upProdDetail;
		View downProdDetail;
		ProgressBar pbBottomWaiting;
	}

	public static final int ACTION_DELETE = 1;
	public static final int ACTION_UPDATE = 2;
	public interface ShoppingCartListener{
		void onSelected(int groupPosition, boolean isExpanded);
		void onProductsUpdated(ProductInfo proInfo, int action);
		void onEdit(ProductInfo proInfo);
		void onRoundaboutSelected();
		void onRemoveCouponSelected();
	}

	public boolean isPricing() {
		return pricing;
	}

	public void setPricing(boolean pricing) {
		this.pricing = pricing;
		notifyDataSetChanged();
	}

	public Pricing getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(Pricing productPrice) {
		this.productPrice = productPrice;
	}

	public Discount getProductDiscount() {
		return discount;
	}
	
	public void setProductDiscount(Discount discount) {
		this.discount = discount;
	}
	
	public boolean isShowShippingAndHandling() {
		return showShippingAndHandling;
	}

	public void setShowShippingAndHandling(boolean showShippingAndHandling) {
		this.showShippingAndHandling = showShippingAndHandling;
	}

	class OnGroupClickListener implements OnTouchListener{
		
		private int moveCount = 0;
		private int groupPosition;
		boolean isExpanded;
		
		public OnGroupClickListener(int groupPosition, boolean isExpaned){
			this.groupPosition = groupPosition;
			this.isExpanded = isExpaned;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			v.setBackgroundColor(Color.BLACK);
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				moveCount = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				moveCount ++;
				if(moveCount>1){
					sv.requestDisallowInterceptTouchEvent(true);
				}
				break;
			case MotionEvent.ACTION_UP:
				if(moveCount<2){
					listener.onSelected(groupPosition, isExpanded);
					notifyDataSetChanged();
				} else {		
					sv.requestDisallowInterceptTouchEvent(true);
				}
				break;
			}
			return false;
		}
		
	}
	
}
