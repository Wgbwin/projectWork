package com.kodak.rss.tablet.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.greetingcard.GCCategory;
import com.kodak.rss.core.n2r.bean.retailer.Catalog;
import com.kodak.rss.core.n2r.bean.retailer.RssEntry;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.GCCategorySelectActivity;
import com.kodak.rss.tablet.adapter.GCDesignCategorySelectionAdapter;
import com.kodak.rss.tablet.thread.GCCreateCardTask;
import com.kodak.rss.tablet.thread.SingleThreadPool;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

/**
 * Purpose: create greeting card
 * Author: Bing Wang 
 */
public class GCCreateView extends FrameLayout implements OnClickListener, onProcessImageResponseListener{
	
	protected Context mContext;		
	private LinearLayout ll;	
	private ListView layoutListView;
	private ImageView layoutIconImageView;
	private TextView tvCategoryName;
	private TextView tvLayoutsNum;
	private TextView detailPromptTxt;
	private WebView layoutDetailWebView;	
	private int dialogWidth;
	private int dialogHeight;
	private GCCategorySelectActivity activity;
	private GCDesignCategorySelectionAdapter dAdapter;
	private DisplayMetrics dm;
	private ImageUseURIDownloader imageDownloader;
	List<GCCategory> designGCCategorys;
	private final Map<String, Request> pendingRequests = new HashMap<String, Request>();	 
	private GCCategory selectedGcc;
		
	private List<Delivery> deliveries;
	private Button SDButton;
	private TextView deliveryText;
	private Delivery selectedDelivery;
	
	private final String ENCODING = "utf-8";
	private SingleThreadPool mThreads;
	
	public GCCreateView(Context context) {
		super(context);
		init(context);
	}
	
	public GCCreateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public GCCreateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	protected void init(Context context){
		this.mContext = context;
		activity = (GCCategorySelectActivity) context;	
		if(mThreads == null){
			mThreads = new SingleThreadPool();
		}
		inflate(mContext, R.layout.gc_create_dialog, this);				
		initViews();
	};
	
	public void initViews(){
		dm = mContext.getResources().getDisplayMetrics();
		dialogWidth = (int) (dm.widthPixels  - dm.density*250);	
		dialogHeight = (int) (dm.heightPixels - dm.density*150);		
		
		ll =  (LinearLayout) findViewById(R.id.createGCDialog);		
		layoutListView = (ListView) findViewById(R.id.layout_list);		
		layoutIconImageView = (ImageView) findViewById(R.id.layout_icon);	
		tvCategoryName = (TextView) findViewById(R.id.tv_categoryName);
		tvLayoutsNum = (TextView) findViewById(R.id.layout_size);
		findViewById(R.id.cancel_button).setOnClickListener(this);
		findViewById(R.id.create_button).setOnClickListener(this);		
		detailPromptTxt = (TextView) findViewById(R.id.detail_prompt);		
		layoutDetailWebView = (WebView) findViewById(R.id.layout_detail);		
		layoutDetailWebView.setBackgroundColor(0);
		layoutDetailWebView.getBackground().setAlpha(2);
	
		SDButton  = (Button) findViewById(R.id.select_delivery_button);	
		deliveryText = (TextView) findViewById(R.id.select_delivery_text);
		SDButton.setOnClickListener(this);		
	}
	
	public GCCreateView setValue(List<GCCategory> designGCCList){	
		int itemWidth = (int) (dialogWidth/4 - dm.density*10);
		int itemHeight = dialogHeight/3;
		designGCCategorys = designGCCList;
		if (designGCCategorys == null ) return this;
		if (designGCCategorys.size() == 0 ) return this;	
		tvLayoutsNum.setText(mContext.getString(R.string.ComposePhotobook_Layouts) + " ("  + designGCCategorys.size() + ")");
		dAdapter = new GCDesignCategorySelectionAdapter(mContext, itemWidth, itemHeight, designGCCategorys, activity.mMemoryCache); 
		layoutListView.setAdapter(dAdapter);
		layoutListView.setBackgroundDrawable(null);
		layoutListView.setDivider(null);
		layoutListView.setOnItemClickListener(onItemClickListener);
		selectedGcc = designGCCategorys.get(0);
		tvCategoryName.setText(selectedGcc.localizedName);
		setIcon(selectedGcc,0);
		initViewAndData();
		return this;
	}
	
	private void initViewAndData(){
		deliveryText.setText(R.string.N2RShoppingCart_SelectDestination);
		List<Catalog> catalogs = RssTabletApp.getInstance().getCatalogList();
		detailPromptTxt.setVisibility(View.INVISIBLE);
		layoutDetailWebView.clearView();
		selectedDelivery = null;
		if(updateDeliveries(catalogs, selectedGcc)){
			if(deliveries.size() == 1){
				selectedDelivery = deliveries.get(0);
				SDButton.setVisibility(View.INVISIBLE);
				deliveryText.setVisibility(View.INVISIBLE);
			} else {
				selectedDelivery = null;
				SDButton.setVisibility(View.VISIBLE);
				deliveryText.setVisibility(View.VISIBLE);
			}
		}
		if(window!=null && window.isShowing()){
			window.dismiss();
		}
		refreshDetails(catalogs, selectedDelivery);
	}
	
	private PopupWindow window;
	Button btDestinationTop;
	Button btDestinationBottom;
	
	private void updateDeliverySelectionWindow(View button, int width, int height){
		if(window!=null && window.isShowing()){
			window.dismiss();
			return;
		}
		View v = LayoutInflater.from(mContext).inflate(R.layout.shoppingcart_delivery_selection, null);
		btDestinationTop = (Button) v.findViewById(R.id.bt_pickInStore);
		btDestinationBottom = (Button) v.findViewById(R.id.bt_pickInHome);
		btDestinationTop.setVisibility(View.INVISIBLE);
		btDestinationBottom.setVisibility(View.INVISIBLE);
		refreshDeliveryButtons(RssTabletApp.getInstance().getCatalogList(), selectedGcc);
		
		btDestinationTop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedDelivery = deliveries.get(0);
				refreshDetails(RssTabletApp.getInstance().getCatalogList(), selectedDelivery);
				deliveryText.setText(selectedDelivery.getDestination());
				window.dismiss();
			}
		});
		
		btDestinationBottom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectedDelivery = deliveries.get(1);
				refreshDetails(RssTabletApp.getInstance().getCatalogList(), selectedDelivery);
				deliveryText.setText(selectedDelivery.getDestination());
				window.dismiss();
			}
		});
		window = new PopupWindow(v, width, height*3);
		window.showAsDropDown(button);
	}
	
	private void refreshDeliveryButtons(List<Catalog> catalogs, GCCategory category){
		if(btDestinationTop==null || btDestinationBottom==null)
			return;
		if(updateDeliveries(catalogs, category)){
			if(deliveries.size()>0){
				btDestinationTop.setText(deliveries.get(0).getDestination());
				btDestinationTop.setVisibility(View.VISIBLE);
			}
			
			if(deliveries.size()>1){
				btDestinationBottom.setText(deliveries.get(1).getDestination());
				btDestinationBottom.setVisibility(View.VISIBLE);
			}
		} else {
			return ;
		}
	}
	
	private OnItemClickListener onItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (designGCCategorys == null)return;
			GCCategory tappedCategory = designGCCategorys.get(position);
			if(!selectedGcc.id.equals(tappedCategory.id)){
				dAdapter.setSelectedItem(position);
				selectedGcc = tappedCategory;
				setIcon(selectedGcc,position);
				initViewAndData();
				tvCategoryName.setText(selectedGcc.localizedName);
			}
			
		}
	};
	
	private boolean updateDeliveries(List<Catalog> catalogs, GCCategory category){
		if(deliveries==null){
			deliveries = new ArrayList<GCCreateView.Delivery>();
		} else {
			deliveries.clear();
		}
		if(catalogs!=null && !catalogs.isEmpty() && category!=null){
			for(Catalog catalog : catalogs){
				for(String identifier : category.productIdentifiers){
					String destination = catalog.getDestination(identifier);
					Delivery delivery = new Delivery(identifier, destination);
					deliveries.add(delivery);
				}
			}
		}
		return !deliveries.isEmpty();
	}
	
	private void setIcon(GCCategory gcc,int position){	
		selectedGcc = gcc;
		if (imageDownloader == null) {
			this.imageDownloader = new ImageUseURIDownloader(mContext, pendingRequests, this);	
			this.imageDownloader.setSaveType(FilePathConstant.cardType);
			this.imageDownloader.setIsThumbnail(false);	
		}
		String id = "org"+gcc.id;
		URI pictureURI = getUri(gcc.glyphURL);
		Bitmap mBitmap = getBitmap(id, pictureURI, layoutIconImageView, position);    						
		layoutIconImageView.setImageBitmap(mBitmap);			
	}
	
	private void refreshDetails(List<Catalog> catalogs, Delivery delivery){
		if(delivery==null && deliveries.size()>1){
			String server = mContext.getResources().getString(R.string.cumulus_check_internet);
			String appId = mContext.getResources().getString(R.string.cumulus_appid);
			String countryCode = RssTabletApp.getInstance().getCountrycodeCurrentUsed();
			String language = Locale.getDefault().getLanguage();
			final String url = "https://"+server+"/mobile/"+appId+"/cards/"+countryCode+"/"+deliveries.get(0).getIdentifier()+"_"+language+".HTM";
			detailPromptTxt.setVisibility(View.INVISIBLE);
			layoutDetailWebView.clearView();
			mThreads.addHighPriorityTask(new LoadUrlRunnable(url, ENCODING, mHandler));
		} else {
			for(Catalog catalog : catalogs){
				RssEntry entry = catalog.getProductEntry(delivery.getIdentifier());
				if(entry != null){
					String detailInfo = entry.getGCMarketing();
					detailInfo = detailInfo.replaceFirst("%%", selectedGcc.localizedName);
					detailPromptTxt.setVisibility(View.VISIBLE);
					layoutDetailWebView.loadDataWithBaseURL(null, detailInfo, "text/html", ENCODING, null);
					break;
				}
			}
		}
	}	
	
	class LoadUrlRunnable implements Runnable {
		
		private String url;
		private String encoding;
		private Handler handler;
		
		public LoadUrlRunnable(String url, String encoding, Handler handler){
			this.url = url;
			this.handler = handler;
			this.encoding = encoding;
		}

		@Override
		public void run() {
			URL mUrl = null;
			InputStream is = null;
			ByteArrayOutputStream bos = null;
			try {
				mUrl = new URL(url);
				try {
					is = mUrl.openStream();
					bos = new ByteArrayOutputStream();
					int count = 0;
					byte[] data = new byte[1024];
					while((count = is.read(data, 0, 1024)) != -1){
						bos.write(data, 0, count);
					}
					String detailInfo = new String(bos.toByteArray(), encoding);
					Message msg = new Message();
					msg.what = REFRESH_WEBVIEW;
					msg.obj = detailInfo;
					handler.sendMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} finally {
				if(bos != null){
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(is != null){
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				mUrl = null;
			}
		}
	}

	public void showAt(){
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();	
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);						
		params.height = dialogHeight;		
		params.width  = dialogWidth;						
		setLayoutParams(params);									
		((View)getParent()).setVisibility(View.VISIBLE);			
		LayoutParams lparams = (LayoutParams) ll.getLayoutParams();
		lparams.height = dialogHeight;		
		lparams.width  = dialogWidth;	
		ll.setLayoutParams(lparams);			
	}

	@Override
	public void onClick(View v) {		
		int viewId = v.getId();
		if(viewId == R.id.select_delivery_button){
			updateDeliverySelectionWindow(v, v.getWidth(), v.getHeight());
		}else if(viewId == R.id.create_button){
			if(selectedDelivery != null){
				((View)getParent()).setVisibility(View.INVISIBLE);
				new GCCreateCardTask(mContext, selectedGcc.id, selectedDelivery.getIdentifier()).execute();	
			} else {
				InfoDialog waringDialog = new InfoDialog.Builder(mContext).setMessage(R.string.N2RShoppingCart_SelectDestinationError)
						.setNeturalButton(R.string.d_ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create();
				waringDialog.show();
			}
		} else {
			if(window!=null && window.isShowing()){
				window.dismiss();				
			}			
			((View)getParent()).setVisibility(View.GONE);
		}
	}

	@Override
	public void onProcess(Response response, String profileId, View view,int position, String flowType, String productId) {
		if (response == null || imageDownloader == null) return;								
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			if (bitmap != null) {	
				MemoryCacheUtil.putBitmap(activity.mMemoryCache, profileId, bitmap);				
				if (view != null) {
					if (view instanceof ImageView) {
						if (view.getTag().toString().equals(profileId)) {
							if (view.getVisibility() == View.VISIBLE) {
								((ImageView)view).setImageBitmap(bitmap);
							}	
						}						 																													
					}
				}					
			}
		}						
	}	

	public Bitmap getBitmap(String Id,URI pictureURI,View view,int position){
		if (Id == null) return null;
    	Bitmap mBitmap = MemoryCacheUtil.getBitmap(activity.mMemoryCache, Id);   	        
        if (mBitmap == null) {
        	String dispalyUrl = FilePathConstant.getLoadFilePath(FilePathConstant.cardType, Id, false);
        	if (dispalyUrl == null) {
				if (imageDownloader != null) {
					view.setTag(Id);
					imageDownloader.downloadProfilePicture(Id, pictureURI, view,position,true);   	
				}				  	 						
        	}else {    	 						
        		mBitmap = BitmapFactory.decodeFile(dispalyUrl);
        		MemoryCacheUtil.putBitmap(activity.mMemoryCache, Id, mBitmap);	       		       					
			}    	        	   	    	        	
		}
        return mBitmap;
    }
	
	public URI getUri(String url){
		URI pictureURI = null;
		if (url == null) return pictureURI;
		if ("".equals(url)) return pictureURI;	
		url= url.replaceAll(" ", "%20");
		try {
			pictureURI = new URI(url);
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
	private static class Delivery {
		private String identifier;
		private String destination;
		
		public Delivery(String identifier, String destination){
			this.identifier = identifier;
			this.destination = destination;
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getDestination() {
			return destination;
		}
	}
	
	private final int REFRESH_WEBVIEW = 0;
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_WEBVIEW:
				String detailInfo = (String) msg.obj;
				detailInfo = detailInfo.replaceFirst("%@", selectedGcc.localizedName);
				detailPromptTxt.setVisibility(View.VISIBLE);
				layoutDetailWebView.loadDataWithBaseURL(null, detailInfo, "text/html", ENCODING, null) ;
				break;

			default:
				break;
			}
		}
		
	};

}
