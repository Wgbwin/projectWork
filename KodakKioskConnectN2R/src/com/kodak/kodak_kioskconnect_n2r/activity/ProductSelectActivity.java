package com.kodak.kodak_kioskconnect_n2r.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.AppConstants.FlowType;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.GreetingCardThemeSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PictureUploadService2;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.QuickBookSelectionActivity;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.WaitingDialog;
import com.kodak.kodak_kioskconnect_n2r.greetingcard.GreetingCardManager;
import com.kodak.utils.EfficientAdapter;
import com.kodak.utils.RSSLocalytics;

public class ProductSelectActivity extends BaseActivity{
	private static String TAG = ProductSelectActivity.class.getSimpleName() ;
	private Button vButtonBack ;
	private TextView vTextViewTitle ;
	private GridView vGridVeiwProduct ;
	private ProductAdapter mProductAdapter ;
	private String packageName ;
	private List<ProductItem> productList;
	
	private GreetingCardManager manager;
	private static WaitingDialog waitingDialog;
	/**
	 * Workflow Selected
	 */
	private final String WORKFLOW_SELECTED = "Workflow Selected";
	private final String SCREEN_NAME = "Workflow Choice"; //Product Choice  --- > Workflow Choice
	private final String WORKFLOW_TYPE = "Workflow Type";
	public static final String TYPE_PRINT = "Prints";
	private final String TYPE_PHOTOBOOK = "Photobooks";
	private final String TYPE_GC = "Greeting Cards";
	private final String TYPE_COLLAGES = "Collages" ;
	private HashMap<String, String> attr = new HashMap<String, String>();
	
	private final static int START_GETTING_THEME = 0;
	private final static int FINISH_GETTING_THEME = 1;
	
	private MyHandler gotoNextStepHandler ;
	private DisplayMetrics dm;
	private int itemWidth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentLayout(R.layout.activity_product_select);
		getViews() ;
		initData() ;
		initProductsGridView() ;
		setEvents() ;
	}

	@Override
	public void getViews() {
		vButtonBack = (Button) findViewById(R.id.back_btn) ;
		vTextViewTitle = (TextView) findViewById(R.id.headerBar_tex) ;
		vGridVeiwProduct = (GridView) findViewById(R.id.products_gridView) ;
		vTextViewTitle.setText(R.string.products);
		vTextViewTitle.setTypeface(PrintHelper.tf);
		vButtonBack.setVisibility(View.VISIBLE);
	}

	@Override
	public void initData() {
		packageName  = getPackageName() ;
		initProducts() ;
		mProductAdapter = new ProductAdapter(this, productList) ;
		vGridVeiwProduct.setAdapter(mProductAdapter) ;
	}
	
	private void initProducts() {
		boolean addQuickBook = false ;
		boolean addPrints = false ; 
		boolean addCards = false;
		boolean addCollages  =false;
		
		productList = new ArrayList<ProductItem>() ;
		if(PrintHelper.products!=null){
			for (PrintProduct product : PrintHelper.products) {
				String productType = product.getType() ;
				if (PrintProduct.TYPE_QUICKBOOK.equals(productType) && !addQuickBook) {
					ProductItem item = new ProductItem();
					if (packageName.contains("dm")) {
						item.setImageId(R.drawable.photo_book_6x8_130x130);
					} else {
						item.setImageId(R.drawable.photo_book_product);
					}
					item.setNameId(R.string.product_quickbook);
					item.setType(productType);
					productList.add(item);
					addQuickBook = true;
					continue;
				}else if(PrintProduct.TYPE_PRINTS.equals(productType) && !addPrints){
					ProductItem item = new ProductItem();
					item.setImageId(R.drawable.prints_product);
					item.setNameId(R.string.product_prints);
					item.setType(productType);
					productList.add(item);
					addPrints = true;
					continue;
				}else if( (PrintProduct.TYPE_DUPLEX_MY_GREETING.equals(productType) ||PrintProduct.TYPE_GREETING_CARDS.equals(productType)) && !addCards){
					ProductItem item = new ProductItem();
					item.setImageId(R.drawable.cards_product);
					item.setNameId(R.string.product_cards);
					item.setType(product.getType());
					productList.add(item);
					addCards = true;
					continue;
				}else if(PrintProduct.TYPE_COLLAGES.equals(productType) && !addCollages){
					ProductItem item = new ProductItem();
					item.setImageId(R.drawable.collage_product);
					item.setNameId(R.string.product_collages);
					item.setType(productType);
					productList.add(item);
					addCollages = true;
					continue;
				}
				
				if (addPrints && addQuickBook && addCards && addCollages) {
					break;
				}
			}
		}
	}
	
	private void initProductsGridView(){
		dm = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(dm) ;
		int screenWidth = dm.widthPixels;
		int columnNumber = productList.size();
		itemWidth = 0;
		if (columnNumber==0) {
			showProductInfoDialog();
			return;
		}
		
		if(columnNumber>3){
			itemWidth = (int) (screenWidth / 3.3);
		} else {
			itemWidth = screenWidth / columnNumber;
		}
		LayoutParams params = new LayoutParams(itemWidth * columnNumber,LayoutParams.MATCH_PARENT);
		vGridVeiwProduct.setLayoutParams(params);
		vGridVeiwProduct.setNumColumns(columnNumber);
		vGridVeiwProduct.setColumnWidth(itemWidth);
		vGridVeiwProduct.setStretchMode(GridView.NO_STRETCH);
	}

	@Override
	public void setEvents() {
		vButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(ProductSelectActivity.this, MainMenu.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
				finish() ;
			}
		}) ;
		
		vGridVeiwProduct.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!Connection.isConnected(ProductSelectActivity.this)) {
					showNoConnectionDialog(view);
					return;
				}
				ProductItem product = productList.get(position) ;
				String productType = product.getType() ;
				gotoNextStep(view, productType);
			}
		});
	}
	
	
	private void gotoNextStep(final View view,String productType){
		Class<com.kodak.kodak_kioskconnect_n2r.PictureUploadService2> pictureUploadService2 = com.kodak.kodak_kioskconnect_n2r.PictureUploadService2.class;
		Intent serviceIntent = new Intent(ProductSelectActivity.this, pictureUploadService2);
		try {
			ComponentName serviceComponentName = startService(serviceIntent);
			if (serviceComponentName != null) {
				Log.i(TAG, "onCreate() startService called CompnentName=" + serviceComponentName.toString());
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		}
		
		PrintHelper.wififlow = false;
		Intent myIntent = null;
		if (PrintProduct.TYPE_QUICKBOOK.equals(productType)) {
			AppContext.getApplication().setFlowType(FlowType.BOOK);
			attr.put(WORKFLOW_TYPE, TYPE_PHOTOBOOK);
			PrintHelper.inQuickbook = true;
			myIntent = new Intent(ProductSelectActivity.this, QuickBookSelectionActivity.class);
		} else if (PrintProduct.TYPE_PRINTS.equals(productType)) {
			AppContext.getApplication().setFlowType(FlowType.PRINT);
			attr.put(WORKFLOW_TYPE, TYPE_PRINT);
			PrintHelper.inQuickbook = false;
			myIntent = new Intent(ProductSelectActivity.this, PhotoSelectMainFragmentActivity.class);
		} else if (PrintProduct.TYPE_DUPLEX_MY_GREETING.equals(productType) ||PrintProduct.TYPE_GREETING_CARDS.equals(productType)) {
			AppContext.getApplication().setFlowType(FlowType.CARD);
			attr.put(WORKFLOW_TYPE, TYPE_GC);
			PrintHelper.inQuickbook = false;
			manager = GreetingCardManager.getGreetingCardManager(getApplicationContext());
			waitingDialog = new WaitingDialog(this, R.string.animation_quickbook_wait);
			waitingDialog.setCancelable(false);
			waitingDialog.setCanceledOnTouchOutside(false);
			waitingDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (manager.hasValidGreetingCardThemes() && manager.hasValidGreetingCardCatalogData()) {
						Intent intent = new Intent(ProductSelectActivity.this, GreetingCardThemeSelectionActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						showNorespondDialog(view);
					}
				}
			});
			
			if(gotoNextStepHandler==null){
				gotoNextStepHandler = new MyHandler(this) ;
			}
			
			Thread thread = new Thread() {

				@Override
				public void run() {
					gotoNextStepHandler.obtainMessage(START_GETTING_THEME).sendToTarget();
					String desIds = "";
					if (PrintHelper.products != null) {
						for (int i = 0; i < PrintHelper.products.size(); i++) {
							PrintProduct pro = PrintHelper.products.get(i);
							if ("Greeting Cards".equals(pro.getType()) || "DuplexMyGreeting".equals(pro.getType())) {
								if (i != PrintHelper.products.size() - 1) {
									desIds += pro.getId() + ",";
								} else {
									desIds += pro.getId();
								}
							}
						}
					}
					manager.createGreetingCardThemes(desIds);
					manager.createGreetingCardCatalogData("");
					gotoNextStepHandler.obtainMessage(FINISH_GETTING_THEME).sendToTarget();
				}
			};
			thread.start();
			RSSLocalytics.recordLocalyticsEvents(this, WORKFLOW_SELECTED, attr);
			return;
			
		}else if(PrintProduct.TYPE_COLLAGES.equals(productType)){
			AppContext.getApplication().setFlowType(FlowType.COLLAGE);
			attr.put(WORKFLOW_TYPE, TYPE_COLLAGES);
			PrintHelper.inQuickbook = false;
			//TODO .....
			myIntent = new Intent(ProductSelectActivity.this, CollageSelectionActivity.class);
		}
		RSSLocalytics.recordLocalyticsEvents(this, WORKFLOW_SELECTED, attr);
		myIntent.putExtra("isFromMainMenu", true);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(myIntent);
	}
	
	private void showNorespondDialog(final View v) {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				v.performClick();
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
	
	private void showProductInfoDialog() {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
		String countryCode=sharedPreferences.getString(MainMenu.CurrentlyCountryCode, "");
		String countryName=PrintHelper.countries.get(countryCode);
		if (countryName.length()==0) {
			countryName="this country";
		}
		String productName=getString(R.string.TMS_mainmenu_photoproducts_text);
		String middleString=getString(R.string.TitlePage_Error_NoProductsRegion);
		String message = productName+" "+ middleString+" "+countryName+".";
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				finish();
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
		PictureUploadService2.isDoneSelectPics = true;
		PictureUploadService2.canUploadFullSize = true;
		PrintHelper.inQuickbook = false;
		PrintHelper.hasQuickbook = false;
		if (PrintHelper.isNull()) {
			new PrintHelper(this);
		} else {
			PrintHelper.StartOver();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Intent myIntent = new Intent(ProductSelectActivity.this, MainMenu.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
			finish() ;
		}
		return false;
	}
	
	class ItemOnClickListener implements OnClickListener {
		private ProductItem product;

		public ItemOnClickListener(ProductItem product) {
			this.product = product;
		}
		@Override
		public void onClick(View v) {
			if (!Connection.isConnected(ProductSelectActivity.this)) {
				showNoConnectionDialog(v);
				return;
			}
			String productType = product.getType() ;
			gotoNextStep(v, productType);
		}
	}
	
	static class MyHandler extends Handler{
		private WeakReference<ProductSelectActivity> mActivity; 
		
		public MyHandler(ProductSelectActivity activity) {  
            mActivity = new WeakReference<ProductSelectActivity>(activity);  
        } 
		
		@Override
		public void handleMessage(Message msg) {
			ProductSelectActivity activity  = mActivity.get() ;
			int action = msg.what;
			switch (action) {
			case START_GETTING_THEME:
				// TODO: here need to do something to handle lock/unlock screen.
				waitingDialog.show();
				break;
			case FINISH_GETTING_THEME:
				if (!activity.isFinishing()) {
					waitingDialog.dismiss();
				}
				break;
			}
		}
	}
	
	class ProductItem {
		private int nameId;
		private int imageId;
		private String type;

		public int getNameId() {
			return nameId;
		}

		public void setNameId(int nameId) {
			this.nameId = nameId;
		}

		public int getImageId() {
			return imageId;
		}

		public void setImageId(int imageId) {
			this.imageId = imageId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}
	
	/**
	 * the adapter of product list
	 * @author sunny
	 *
	 */
	class ProductAdapter extends EfficientAdapter<ProductItem>{

		public ProductAdapter(Context context, List<ProductItem> dataList) {
			super(context, dataList);
		}

		@Override
		protected int getItemLayout() {
			return R.layout.product_grid_item;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder();
			holder.vProductImage = (ImageView) v.findViewById(R.id.product_image);
			holder.vProductName = (TextView) v.findViewById(R.id.product_text);
			android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
			params.height = dm.heightPixels * 9 / 12;
			params.width = (int) (dm.widthPixels / 3.3);
			v.setLayoutParams(params);
			v.setTag(holder);
		}

		@Override
		protected void bindView(View v, ProductItem data, int position) {
			if (data == null) {
				return;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
			holder.vProductImage.setImageResource(data.getImageId()) ;
			holder.vProductName.setText(data.getNameId()) ;
			ItemOnClickListener listener = new ItemOnClickListener(data);
			v.setOnClickListener(listener) ;
		}
		
		private class ViewHolder {
			private ImageView vProductImage;
			private TextView vProductName;
		}
	}
	
}
