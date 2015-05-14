package com.kodak.kodak_kioskconnect_n2r.activity;

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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.AppConstants;
import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.InfoDialog;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.kodak_kioskconnect_n2r.PrintProduct;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.bean.ColorEffect;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.collage.CollageManager;
import com.kodak.kodak_kioskconnect_n2r.collage.LoadThumbTask;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;
import com.kodak.utils.EfficientAdapter;
import com.kodak.utils.RSSLocalytics;

public class CollageSelectionActivity extends BaseActivity {
	private static final String TAG = CollageSelectionActivity.class.getSimpleName();
	
	private TextView tvTitle;
	private Button cancelButton, back, next;
	private Display display;
	private DisplayMetrics dm;
	private CollageAdapter adapter;
	private String ENCODING = "utf-8";
	private RelativeLayout bottomBar;

	private List<PrintProduct> collageProducts;

	private GridView gvProducts;
	private ProgressDialog dialog;
	private static String proDesID = "";
	private CreateCollageAsyncTask collageAsyncTask;
	private boolean showingProdDesc = false;
	private View prodDescPart;
	private TextView webDetailTV;
	private WebView detailWebView;
	private ImageView ivThumb;
	private int itemWidth;
	public static final String COLLAGE_TYPE_="Collage Type";
	public static HashMap<String, String>attr = new HashMap<String, String>();
	public static final String Collage_Type_Selected="Collage Type Selected";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentLayout(R.layout.collage_selection_fields);
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, COLLAGE_TYPE_);
		initData();
		getViews();
		setEvents();
	}

	@Override
	public void getViews() {
		back = (Button) findViewById(R.id.back_btn);
		next = (Button) findViewById(R.id.next_btn);
		
		tvTitle = (TextView) findViewById(R.id.headerBar_tex);
		bottomBar = (RelativeLayout) findViewById(R.id.main_bottombar);
		gvProducts = (GridView) findViewById(R.id.products_gridView);
		tvTitle.setText(getString(R.string.product_collages));
		next.setText(getString(R.string.button_make_this));
		back.setTypeface(PrintHelper.tf);
		next.setTypeface(PrintHelper.tf);
		tvTitle.setTypeface(PrintHelper.tf);
		prodDescPart = findViewById(R.id.prod_desc);
		detailWebView = (WebView) findViewById(R.id.detail_webView);
		webDetailTV = (TextView) findViewById(R.id.detail_textView);
		ivThumb = (ImageView) findViewById(R.id.iv_thumb);
		cancelButton = (Button) findViewById(R.id.detail_cancel);

		back.setVisibility(View.VISIBLE);
		next.setVisibility(View.INVISIBLE);
		initProductsGridView();

		gvProducts.setAdapter(adapter);
	}

	@Override
	public void initData() {
		initCollageProducts();
		adapter = new CollageAdapter(this, collageProducts);
	}

	private void initCollageProducts() {
		if (collageProducts == null) {
			collageProducts = new ArrayList<PrintProduct>();
		} else {
			collageProducts.clear();
		}
		for (PrintProduct product : PrintHelper.products) {
			if (product.getType().equals(PrintProduct.TYPE_COLLAGES)) {
				CollageProduct qbProduct = createCollageProduct(product);
				collageProducts.add(qbProduct);
			}
		}
	}

	private void initProductsGridView() {
		display = getWindowManager().getDefaultDisplay();
		dm = new DisplayMetrics();
		display.getMetrics(dm);
		int columnNumber = collageProducts.size();
		int screenWidth = dm.widthPixels;
		itemWidth = 0;
		if (columnNumber > 3) {
			itemWidth = (int) (screenWidth / 3.3);
		} else {
			itemWidth = screenWidth / columnNumber;
		}
		LayoutParams params = new LayoutParams(itemWidth * columnNumber, LayoutParams.MATCH_PARENT);
		gvProducts.setLayoutParams(params);
		gvProducts.setNumColumns(columnNumber);
		gvProducts.setColumnWidth(itemWidth);
		gvProducts.setStretchMode(GridView.NO_STRETCH);
	}

	private CollageProduct createCollageProduct(PrintProduct product) {
		CollageProduct collageProduct = new CollageProduct();
		// collageProduct.setHeight(product.getHeight());
		collageProduct.setHtmlMarketing(product.getHtmlMarketing());
		collageProduct.setHtmlShortMarketing(product.getHtmlShortMarketing());
		collageProduct.setId(product.getId());
		collageProduct.setLgGlyphURL(product.getLgGlyphURL());
		collageProduct.setMaxPrice(product.getMaxPrice());
		collageProduct.setMinPrice(product.getMinPrice());
		collageProduct.setName(product.getName());
		collageProduct.setPrice(product.getPrice());
		collageProduct.setShortName(product.getShortName());
		collageProduct.setSmGlyphURL(product.getSmGlyphURL());
		collageProduct.setType(product.getType());
		// collageProduct.setWidth(product.getWidth());
		collageProduct.setMaxPriceStr(product.getMaxPriceStr());
		collageProduct.setMinPriceStr(product.getMinPriceStr());

		return collageProduct;
	}

	class CollageAdapter extends EfficientAdapter<PrintProduct> {
		int width, height;
		private Bitmap bitmap ;

		public CollageAdapter(Context context, List<PrintProduct> dataList) {
			super(context, dataList);
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image_wait_4x6);
			width = bitmap.getWidth();
			height = bitmap.getHeight();
		}

		@Override
		protected int getItemLayout() {
			return R.layout.product_grid_item;
		}

		@Override
		protected void initView(View v) {
			ViewHolder holder = new ViewHolder();
			holder.vCollageImage = (ImageView) v.findViewById(R.id.product_image);
			holder.vCollageName = (TextView) v.findViewById(R.id.product_text);
			v.setTag(holder);

		}

		@Override
		protected void bindView(View v, PrintProduct data, int position) {
			if (data == null) {
				return;
			}
			final ViewHolder holder = (ViewHolder) v.getTag();
//			mImageLoader.loadImage(holder.vCollageImage, data.getLgGlyphURL()) ;
			if(((CollageProduct)data).getBitmap()==null){
				holder.vCollageImage.setImageBitmap(bitmap);
				if(!((CollageProduct)data).downloading){
					((CollageProduct)data).downloading = true;
					new Thread(new ThumbDownloader(((CollageProduct)data))).start();
				}
			} else {
				holder.vCollageImage.setImageBitmap(((CollageProduct)data).getBitmap());
			}
			holder.vCollageName.setText(data.getName());
			ItemOnClickListener listener = new ItemOnClickListener(data,position);
			android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
			params.height = dm.heightPixels * 9 / 12;
			params.width = (int) (dm.widthPixels / 3.3);
			v.setLayoutParams(params);
			v.setOnClickListener(listener);
		}

		private class ViewHolder {
			private ImageView vCollageImage;
			private TextView vCollageName;
		}
	}

	class ItemHolder {
		ImageView imageView;
		TextView textView;
		WebView webView;
	}

	class CollageProduct extends PrintProduct {
		private Bitmap bitmap;
		public boolean downloading = false;

		public Bitmap getBitmap() {
			return bitmap;
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

	}

	class ItemOnClickListener implements OnClickListener {
		private PrintProduct printProductData;
		private int pos;

		public ItemOnClickListener(PrintProduct printProductData, int position) {
			this.printProductData = printProductData;
		}

		@Override
		public void onClick(View v) {
			if (showingProdDesc) {
				return;
			}
			proDesID = printProductData.getId();
			showProductDescription((CollageProduct) printProductData);
		}

	}

	@Override
	public void setEvents() {
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideProductDescription();
			}
		});
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (showingProdDesc) {
					hideProductDescription();
					return;
				}
				Intent myIntent = new Intent(CollageSelectionActivity.this, ProductSelectActivity.class);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(myIntent);
			}
		});

		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Log.d("CollageSelection", "make this on click");
				dialog = new CreateCollageDialog(CollageSelectionActivity.this);
				dialog.show();
				hideProductDescription();
				collageAsyncTask = new CreateCollageAsyncTask();
				collageAsyncTask.execute("");
			};
		});
	}
	
	private void showProductDescription(CollageProduct collageProduct){
		showingProdDesc = true;
		prodDescPart.setVisibility(View.VISIBLE);
		gvProducts.setVisibility(View.INVISIBLE);
		clearWebViewBackground(detailWebView);
		String detailInfo = getDetailData(collageProduct);
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
		imageURL = collageProduct.getLgGlyphURL();
		if(collageProduct.getBitmap() != null){
			ivThumb.setImageBitmap(collageProduct.getBitmap());
		} else {
			LoadThumbTask loadThumbTask = new LoadThumbTask(this, ivThumb, collageProduct.getLgGlyphURL());
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

	private void clearWebViewBackground(WebView wv) {
		wv.setBackgroundColor(0);
		int version = Integer.valueOf(android.os.Build.VERSION.SDK);
		if (version >= 11) {
			@SuppressWarnings("rawtypes")
			Class webView = wv.getClass();
			try {
				@SuppressWarnings("unchecked")
				Method closeLayerType = webView.getMethod("setLayerType", new Class[] { int.class, android.graphics.Paint.class });
				try {
					closeLayerType.invoke(wv, new Object[] { 0x1, null });
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

	private String getDetailData(PrintProduct collageProductData) {
		String data = "";
		String minPriceStr = "";
		data = collageProductData.getHtmlMarketing();
		minPriceStr = collageProductData.getMinPriceStr();
		if (PrintHelper.productWithId == null) {
			PrintHelper.productWithId = new HashMap<String, String>();
		}
		PrintHelper.productWithId.put(collageProductData.getName(), collageProductData.getId());
		for (PrintProduct p : PrintHelper.products) {
			if (p.getId().startsWith(collageProductData.getId()) && !p.getId().equals(collageProductData.getId())) {
				PrintHelper.productWithId.put(p.getName(), p.getId());
			}
		}
		if (!TextUtils.isEmpty(data)) {
			data = data.replace("%@", minPriceStr);
			Log.w(TAG, "price: " + minPriceStr);
		}
		return data;
	}

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (showingProdDesc) {
				//hideAnimation();
				hideProductDescription();
				return false;
			}
			Intent myIntent = new Intent(CollageSelectionActivity.this, ProductSelectActivity.class);
			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(myIntent);
		}
		return false;
	};

	class CreateCollageDialog extends ProgressDialog {

		public CreateCollageDialog(Context context) {
			super(context);
		}

		public CreateCollageDialog(Context context, int theme) {
			super(context, theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.getting_products_dialog);
			RelativeLayout dialog_LinearLayout = (RelativeLayout) findViewById(R.id.dialog_LinearLayout);
			TextView dialog_title = (TextView) findViewById(R.id.dialog_textView);
			dialog_title.setText(getString(R.string.dialogTile_creating_collage));
			ViewGroup.LayoutParams dialogLp = dialog_LinearLayout.getLayoutParams();
			Display display = getWindowManager().getDefaultDisplay();
			dm = new DisplayMetrics();
			display.getMetrics(dm);
			dialogLp.height = dm.heightPixels * 3 / 4;
			dialogLp.width = dm.widthPixels * 2 / 3;
			dialog_LinearLayout.setLayoutParams(dialogLp);
		}

		@Override
		public void show() {
			super.show();
		}

	}

	private void showNorespondDialog(final View v) {
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.share_upload_error_no_responding);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builder.setNegativeButton(getString(R.string.share_upload_retry),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					v.performClick();
				}
			});
		builder.setCancelable(false);
		builder.create().show();
	}

	private class CreateCollageAsyncTask extends AsyncTask<String, Void, Collage> {

		@Override
		protected void onPreExecute() {
			if (dialog == null) {
				dialog = new CreateCollageDialog(CollageSelectionActivity.this);
				dialog.show();
			}
		}

		@Override
		protected Collage doInBackground(String... params) {
			CollageWebServices service = new CollageWebServices(CollageSelectionActivity.this, "");
			Collage collage = service.createCollageTask(proDesID);
			return collage;
		}

		@Override
		protected void onPostExecute(Collage result) {
			if (result!=null) {
				CollageManager.getInstance().addCollage(result);
				LoadColorEffectsAsyncTask mLEA = new LoadColorEffectsAsyncTask();
				mLEA.execute();
			}else {
				dialog.dismiss();
				showNorespondDialog(next);
			}
		}

	}

	private class LoadColorEffectsAsyncTask extends AsyncTask<Void, Void, List<ColorEffect>> {

		@Override
		protected List<ColorEffect> doInBackground(Void... params) {
			CollageWebServices service = new CollageWebServices(CollageSelectionActivity.this, "");
			List<ColorEffect> mColorEffects = new ArrayList<ColorEffect>();
			mColorEffects = service.getAvailableColorEffect2Task();
			return mColorEffects;
		}

		@Override
		protected void onPostExecute(List<ColorEffect> result) {
			if (dialog != null) {
				dialog.dismiss();
			}
			if (result == null) {
				showNorespondDialog(next);
				super.onPostExecute(result);
			} else {
				Collage collage=CollageManager.getInstance().getCurrentCollage();
				AppContext.getApplication().setColorEffects(result);
				Intent myIntent = new Intent(CollageSelectionActivity.this, PhotoSelectMainFragmentActivity.class);
				myIntent.putExtra(AppConstants.KEY_PRODUCT_ID, collage.id);
				myIntent.putExtra(AppConstants.KEY_PRODUCT_DECID, CollageManager.getInstance().getCurrentCollage().proDescId);
				attr.put(COLLAGE_TYPE_, collage.proDescId);
				RSSLocalytics.recordLocalyticsEvents(CollageSelectionActivity.this, Collage_Type_Selected, attr);
				startActivity(myIntent);
			}
		}

	}

	
	
	class ThumbDownloader implements Runnable {
        private CollageProduct product ;
        public ThumbDownloader(CollageProduct product ) {
            this.product = product ;
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
	
	private String imageURL = "";
	public String getCurrentLoadingImageURL(){
		return imageURL;
	}
	@Override
	protected void onResume() {
		super.onResume();
		RSSLocalytics.onActivityResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		RSSLocalytics.onActivityPause(this);
	}
	
}
