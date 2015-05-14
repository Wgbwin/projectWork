package com.kodak.kodak_kioskconnect_n2r;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.activity.BaseActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.PhotoSelectMainFragmentActivity;
import com.kodak.kodak_kioskconnect_n2r.activity.ProductSelectActivity;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.kodak_kioskconnect_n2r.collage.LoadThumbTask;
import com.kodak.utils.RSSLocalytics;

public class QuickBookSelectionActivity extends BaseActivity {
	private static final String TAG = QuickBookSelectionActivity.class.getSimpleName();
	
	private SharedPreferences prefs;
	private Button back;
	private Button next;
	//private Button makeThis;
	private Button cancel;
	
	private TextView tvTitle;
	
	private List<PrintProduct> quickbookProducts;
	private GridView gvProducts;
	private QuickBookAdapter adapter;
	private int[] selectedSize;
	private ProgressDialog dialog;
	
	private Display display;
	private HorizontalScrollView horScrolView;
	private RelativeLayout bottomBar;
	private boolean lockScreen = false;
	
	private String ENCODING = "utf-8";
	private String result="";
	private final String SCREEN_NAME = "Photobook Type";
	private final String PHOTOBOOK_TYPE = "Photobook Type";
	private final String EVENT = "Photobook Type Selected";
	
	private HashMap<String, String> attr;
	
	private String selectedPhotobookTypeID = "";
	private String selectedPhotobookAddTypeID = "";
	private int position;

	private boolean showingProdDesc = false;
	private View prodDescPart;
	private TextView webDetailTV;
	private WebView detailWebView;
	private ImageView ivThumb;
	private String imageURL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.photobook_selection_fields);
		initData();
		getViews();
		setEvents();		
	}
	
	private void showProductDescription(QuickBookProduct quickbookProduct){
		showingProdDesc = true;
		prodDescPart.setVisibility(View.VISIBLE);
		gvProducts.setVisibility(View.INVISIBLE);
		clearWebViewBackground(detailWebView);
		String detailInfo = getDetailData();
		if (detailInfo == null || detailInfo.equals("")) {
			detailInfo = getString(R.string.no_description);
			webDetailTV.setText(detailInfo);
			webDetailTV.setTypeface(PrintHelper.tf);
			webDetailTV.setVisibility(View.VISIBLE);
			detailWebView.setVisibility(View.GONE);
		} else {
			detailWebView.setVerticalScrollBarEnabled(false);
			detailWebView.setHorizontalScrollBarEnabled(false);
			detailWebView.loadDataWithBaseURL(null,detailInfo, "text/html", ENCODING, null);
		}
		imageURL = quickbookProduct.getLgGlyphURL();
		if(quickbookProduct.getBitmap() != null){
			ivThumb.setImageBitmap(quickbookProduct.getBitmap());
		} else {
			LoadThumbTask loadThumbTask = new LoadThumbTask(this, ivThumb, quickbookProduct.getLgGlyphURL());
			loadThumbTask.execute();
		}
		next.setVisibility(View.VISIBLE);
		
	}
	
	private void hideProductDescription(){
		showingProdDesc = false;
		prodDescPart.setVisibility(View.GONE);
		next.setVisibility(View.INVISIBLE);
		gvProducts.setVisibility(View.VISIBLE);
		ivThumb.setImageBitmap(null);
	}
	
	private String getDetailData(){
		String data = "";
		String minPriceStr = "";
		
		if(selectedSize!=null && selectedSize.length == 2){
			for(PrintProduct pro : quickbookProducts){
				if(pro.getId().equals(quickbookProducts.get(position).getId())){
					data = pro.getHtmlMarketing();
					minPriceStr = pro.getMinPriceStr();
					if(PrintHelper.productWithId == null){
						PrintHelper.productWithId = new HashMap<String, String>();
					}/*else {
						PrintHelper.productWithId.clear();
					}*/
					PrintHelper.productWithId.put(pro.getName(), pro.getId());
					for(PrintProduct p : PrintHelper.products){
						if(p.getId().startsWith(pro.getId()) && !p.getId().equals(pro.getId())){
							PrintHelper.productWithId.put(p.getName(), p.getId());
						}
					}
					break;
				}
			}
		}
		if(data!=null && !data.equals("")){
			data = data.replace("%@", minPriceStr);
			Log.w(TAG, "price: " + minPriceStr);
		}
		return data;
	}
	
	class ItemOnClickListener implements OnClickListener{
		private int width, height;
		private int pos;
		
		public ItemOnClickListener(int w, int h, int position){
			this.width = w;
			this.height = h;
			this.pos = position;
		}
		
		@Override
		public void onClick(View v) {
			if(showingProdDesc){
				return;
			}
			selectedSize = new int[]{width,height};
			position = pos;
			
			PrintProduct photoBookProduct = null, photoBookAdditionalPageProduct = null;
			for(PrintProduct product : PrintHelper.products){
				if(product.getType().contains(PrintProduct.TYPE_QUICKBOOK) && selectedSize[0] == product.getWidth() && selectedSize[1] == product.getHeight()){
					if(product.getId().contains(PrintHelper.PhotoBook) && !product.getId().contains(PrintHelper.AdditionalPage) && product.getId().equals(quickbookProducts.get(pos).getId())){
						photoBookProduct = product;
					}
					if(product.getId().contains(PrintHelper.PhotoBook) && product.getId().contains(PrintHelper.AdditionalPage)&& product.getId().contains(quickbookProducts.get(pos).getId())){
						photoBookAdditionalPageProduct = product;
					}
				}
			}
			SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(QuickBookSelectionActivity.this);
			Editor mEditor = mSharedPreferences.edit();
			if(photoBookProduct != null){
				String productId = photoBookProduct.getId();
				selectedPhotobookTypeID = productId;
				attr = new HashMap<String, String>();
				attr.put(PHOTOBOOK_TYPE, productId);
				RSSLocalytics.recordLocalyticsEvents(QuickBookSelectionActivity.this, EVENT,attr);
			}
			selectedPhotobookAddTypeID = photoBookAdditionalPageProduct==null?"":photoBookAdditionalPageProduct.getId();
			mEditor.commit();
			
			showProductDescription((QuickBookProduct) quickbookProducts.get(pos));
		}
		
	}
	
	class CreateQuickBookDialog extends ProgressDialog{
		
		public CreateQuickBookDialog(Context context) {
			super(context);
		}

		public CreateQuickBookDialog(Context context, int theme) {
			super(context, theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.getting_products_dialog);
			RelativeLayout dialog_LinearLayout = (RelativeLayout) findViewById(R.id.dialog_LinearLayout);
			TextView dialog_title = (TextView) findViewById(R.id.dialog_textView);
			dialog_title.setText(getString(R.string.dialogTile_creating_photobook));
			ViewGroup.LayoutParams dialogLp = dialog_LinearLayout.getLayoutParams();
			Display display = getWindowManager().getDefaultDisplay();
			dialogLp.height = display.getHeight() * 3 / 4;
			dialogLp.width = display.getWidth() * 2 / 3;
			dialog_LinearLayout.setLayoutParams(dialogLp);
		}

		@Override
		public void show() {
			super.show();
		}
		
	}
	
	class CreatePhotobookRunnable implements Runnable{

		@Override
		public void run() {
			PrintMakerWebService service = new PrintMakerWebService(QuickBookSelectionActivity.this, "");
		    int count = 0;
		    result = "";
		    while (count < 5 && result.equals("")){
		    	result = service.pbCreatePhotoBook(QuickBookSelectionActivity.this, selectedPhotobookTypeID);
		    	count++;
		    }
		    if(dialog!=null && dialog.isShowing()){
		    	dialog.dismiss();
		    }
		    
		}
		
	}
	
	class ItemHolder {
		ImageView imageView;
		TextView textView;
		WebView webView;
	}
	
	class QuickBookAdapter extends BaseAdapter {
		private LayoutInflater layoutInflater;
		private Bitmap wait_image;
		int width, height;
		
		public QuickBookAdapter(Context context){
			layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			wait_image = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait_4x6);
			width = wait_image.getWidth();
			height = wait_image.getHeight();
		}

		@Override
		public int getCount() {
			return quickbookProducts.size();
		}

		@Override
		public Object getItem(int position) {
			return quickbookProducts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemHolder holder;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.product_item, null);
				holder = new ItemHolder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.proIV);
				holder.textView = (TextView) convertView.findViewById(R.id.proTV);
				holder.webView = (WebView) convertView.findViewById(R.id.proWV);
				
				convertView.setTag(holder);
			} else {
				holder = (ItemHolder) convertView.getTag();
			}
			holder.textView.setVisibility(View.GONE);
			LinearLayout.LayoutParams vLp = (LayoutParams) holder.imageView.getLayoutParams();
			int newSize;
			if(height<width){
				newSize = height;
			} else {
				newSize = width;
			}
			vLp.width = newSize;
			vLp.height = newSize;
			vLp.gravity = Gravity.CENTER;
			holder.imageView.setLayoutParams(vLp);
			clearWebViewBackground(holder.webView);
			
			QuickBookProduct product = (QuickBookProduct) getItem(position);
			if(product.getBitmap()==null){
				holder.imageView.setImageBitmap(wait_image);
				if(!product.downloading){
					product.downloading = true;
					new Thread(new ThumbDownloader(product)).start();
				}
			} else {
				holder.imageView.setImageBitmap(product.getBitmap());
			}
			String data = product.getHtmlShortMarketing();
			if(data==null){
				holder.textView.setVisibility(View.VISIBLE);
				holder.webView.setVisibility(View.GONE);
				holder.textView.setText(product.getName());
				holder.textView.setTypeface(PrintHelper.tf);
			} else {
				if(data.contains("%@")){
					data = data.replace("%@", product.getMinPriceStr());
				}
				holder.webView.loadDataWithBaseURL(null, data, "text/html", ENCODING, null);
				
			}
						
			ItemOnClickListener listener = new ItemOnClickListener(product.getWidth(), product.getHeight(), position);
			convertView.setOnClickListener(listener);
			return convertView;
		}
		
	}
	
	class QuickBookProduct extends PrintProduct {
		private Bitmap bitmap;
		public boolean downloading = false;

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}
		
	}
	
	class ThumbDownloader implements Runnable {
		private QuickBookProduct product;
		
		public ThumbDownloader(QuickBookProduct product){
			this.product = product;
		}

		@Override
		public void run() {
			Log.i(TAG, "start download url[" + product.getLgGlyphURL() + "]");
			byte[] imgData = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			int count = 0;
			try{
				byte[] data = null;
				while(imgData==null && count<5){
					URL url = new URL(product.getLgGlyphURL());
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(5 * 1000);
					conn.setReadTimeout(10 * 1000);
					is = conn.getInputStream();
					int length = (int) conn.getContentLength();
				
					if (length > 0) {
						data = new byte[length];
						byte[] buffer = new byte[4098];
						int readLen = 0;
						int destPos = 0;
						while ((readLen = is.read(buffer)) >= 0) {
							if (readLen > 0) {
								System.arraycopy(buffer, 0, data, destPos, readLen);
								destPos += readLen;
							} else {
								Log.w(TAG, "");
							}
						}
						imgData = data;
					}
					count++;
				}
				if(imgData != null){
					product.downloading = false;
					product.setBitmap(BitmapFactory.decodeByteArray(imgData, 0, imgData.length));
					thumbHandler.obtainMessage().sendToTarget();
				}
			} catch(Exception e){
				e.printStackTrace();
			} finally {
				if(is!=null){
					try {
						is.close();
						is = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (conn != null) {
						conn.disconnect();
					}
				}
			}
		}
		
	}
	
	Handler thumbHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			adapter.notifyDataSetChanged();
		}
		
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		RSSLocalytics.onActivityPause(this);
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		RSSLocalytics.onActivityResume(this);
		super.onResume();
		lockScreen = false;
	}
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(dialog!=null && dialog.isShowing()){
			lockScreen = true;
			dialog.dismiss();
		}
	}

	private void initQuickBookProducts(){
		if(quickbookProducts == null){
			quickbookProducts = new ArrayList<PrintProduct>();
		} else {
			quickbookProducts.clear();
		}
		for(PrintProduct product : PrintHelper.products){
			if(product.getType().equals(PrintProduct.TYPE_QUICKBOOK)) {
				QuickBookProduct qbProduct = createQuickBookProduct(product);
				quickbookProducts.add(qbProduct);
			}
		}
	}
	
	private void initProductsGridView(){
		int columnNumber = quickbookProducts.size();
		int itemWidth = display.getWidth() / columnNumber;
		LayoutParams params = new LayoutParams(itemWidth * columnNumber,LayoutParams.FILL_PARENT);
		gvProducts.setLayoutParams(params);
		gvProducts.setNumColumns(columnNumber);
		gvProducts.setColumnWidth(itemWidth);
		gvProducts.setStretchMode(GridView.NO_STRETCH);
	}
	
	private void showNorespondDialog(final View v){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,	int which) {
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(getString(R.string.share_upload_retry), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				v.performClick();
			}
		});
		builder.setCancelable(false);
		builder.create().show();
	}
	

	
	private void clearWebViewBackground(WebView wv){
		wv.setBackgroundColor(0);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if(version >= 11){
			@SuppressWarnings("rawtypes")
			Class webView = wv.getClass();
			try {
				@SuppressWarnings("unchecked")
				Method closeLayerType = webView.getMethod("setLayerType", new Class[]{int.class, android.graphics.Paint.class});
				try {
					closeLayerType.invoke(wv,new Object[]{0x1, null});
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private QuickBookProduct createQuickBookProduct(PrintProduct product){
		QuickBookProduct quickBookProduct = new QuickBookProduct();
		
		quickBookProduct.setHeight(product.getHeight());
		quickBookProduct.setHtmlMarketing(product.getHtmlMarketing());
		quickBookProduct.setHtmlShortMarketing(product.getHtmlShortMarketing());
		quickBookProduct.setId(product.getId());
		quickBookProduct.setLgGlyphURL(product.getLgGlyphURL());
		quickBookProduct.setMaxPrice(product.getMaxPrice());
		quickBookProduct.setMinPrice(product.getMinPrice());
		quickBookProduct.setName(product.getName());
		quickBookProduct.setPrice(product.getPrice());
		quickBookProduct.setShortName(product.getShortName());
		quickBookProduct.setSmGlyphURL(product.getSmGlyphURL());
		quickBookProduct.setType(product.getType());
		quickBookProduct.setWidth(product.getWidth());
		quickBookProduct.setMaxPriceStr(product.getMaxPriceStr());
		quickBookProduct.setMinPriceStr(product.getMinPriceStr());
		
		return quickBookProduct;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			if(showingProdDesc){
				hideProductDescription();
				selectedSize = null;
				return false;
			}
			Intent myIntent = new Intent(QuickBookSelectionActivity.this, ProductSelectActivity.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
		}
		return false;
	}

	@Override
	public void getViews() {
		back = (Button) findViewById(R.id.back_btn);
		next = (Button) findViewById(R.id.next_btn);
		gvProducts = (GridView) findViewById(R.id.products_gridView);
		tvTitle = (TextView) findViewById(R.id.headerBar_tex);
		horScrolView = (HorizontalScrollView) findViewById(R.id.product_choice_horScrolView);
		bottomBar = (RelativeLayout) findViewById(R.id.main_bottombar);
		horScrolView.setVerticalScrollBarEnabled(false);

		prodDescPart = findViewById(R.id.prod_desc);
		detailWebView = (WebView) findViewById(R.id.detail_webView);
		webDetailTV = (TextView) findViewById(R.id.detail_textView);
		ivThumb = (ImageView) findViewById(R.id.iv_thumb);
		cancel = (Button) findViewById(R.id.detail_cancel);
		
		tvTitle.setText(getString(R.string.product_quickbook));
		next.setText(getString(R.string.button_make_this));
		back.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		tvTitle.setTypeface(PrintHelper.tf);
		
		back.setVisibility(View.VISIBLE);
		next.setVisibility(View.INVISIBLE);
		
		display = getWindowManager().getDefaultDisplay();
		
		initProductsGridView();
		gvProducts.setAdapter(adapter);
	}

	@Override
	public void initData() {
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		prefs = PreferenceManager.getDefaultSharedPreferences(QuickBookSelectionActivity.this);
		initQuickBookProducts();
		adapter = new QuickBookAdapter(this);
		
	}

	@Override
	public void setEvents() {
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				hideProductDescription();
				if (!Connection.isConnected(QuickBookSelectionActivity.this))	{
					showNoConnectionDialog(v);
					return;
				} 
				dialog = new CreateQuickBookDialog(QuickBookSelectionActivity.this);
				dialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						if(lockScreen){
							return;
						}
						if(!result.equals("")){
							Photobook photobook = AppContext.getApplication().getPhotobook();
							photobook.isFirstToCreatePhotoBook = true;
							photobook.title = "";
							photobook.author = "";
							photobook.subTitle = "";
							photobook.additionalPageId = selectedPhotobookAddTypeID;
							photobook.proDescId = selectedPhotobookTypeID;							
							
//							Intent intent = new Intent(QuickBookSelectionActivity.this, PhotoSourceSelectMainActivity.class);
							Intent intent = new Intent(QuickBookSelectionActivity.this, PhotoSelectMainFragmentActivity.class);
							intent.putExtra("isFromMainMenu", true);
							intent.putExtra(AppConstants.KEY_PRODUCT_ID, photobook.id);
							intent.putExtra(AppConstants.KEY_PRODUCT_DECID, photobook.proDescId);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						} else {
							showNorespondDialog(v);
						}
					}
				});
				dialog.setOnShowListener(new OnShowListener() {
					
					@Override
					public void onShow(DialogInterface dialog) {
						new Thread(new CreatePhotobookRunnable()).start();
					}
				});
				dialog.setCancelable(false);
				dialog.show();
				return;
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(showingProdDesc){
					hideProductDescription();
					selectedSize = null;
					return;
				}
				Intent myIntent = new Intent(QuickBookSelectionActivity.this, ProductSelectActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideProductDescription();
			}
		});
		
	}
	
	public String getCurrentLoadingImageURL(){
		return imageURL;
	}

}
