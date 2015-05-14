package com.kodak.kodak_kioskconnect_n2r.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.android.displayingbitmaps.util.AsyncTask;
import com.kodak.kodak_kioskconnect_n2r.R;
import com.kodak.kodak_kioskconnect_n2r.adapter.ProductEditPopAdapter;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.Collage;
import com.kodak.kodak_kioskconnect_n2r.bean.collage.CollagePage;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Layer;
import com.kodak.kodak_kioskconnect_n2r.bean.print.Page;
import com.kodak.kodak_kioskconnect_n2r.webservices.CollageWebServices;

public abstract class ProductEditPopView<P extends Page, L extends Layer> extends FrameLayout{
	//item id for click listener
	public static final int PAGE_ADD_PAGE=1;
	public static final int PAGE_DELETE_PAGE=2;
	public static final int PAGE_ADD_PAGE_TEXT=3;
	public static final int PAGE_EDIT_PAGE_TEXT=4;
	public static final int PAGE_DELETE_PAGE_TEXT=12;
	public static final int PAGE_BACKGROUND_OPTIONS = 6;
	public static final int PAGE_EDIT_TITLE = 7;
	public static final int LAYER_CROP=101;
	public static final int LAYER_ENHANCE=102;
	public static final int LAYER_RED_EYE=103;
	public static final int LAYER_COLOR_EFFECTS=104;
	public static final int LAYER_ROTATE=105;
	public static final int LAYER_REMOVE_IMAGE=106;
	public static final int LAYER_SELECT_PAGE=107;
	public static final int LAYER_ENTER_CAPTION=108;
	public static final int LAYER_SET_AS_BACKGROUND=109;
	public static final int LAYER_FLIP_HORIZONTAL=110;
	public static final int LAYER_FLIP_VERTICAL=111;
	public static final int LAYER_UNDO_RED_EYE=112;
	public static final int LAYER_UNDO_ENHANCE=113;
	public static final int LAYER_DELETE_CAPTION = 114;
	public static final int LAYER_EDIT_CAPTION = 115;
	
	private Context context;

	private ListView listView;
	
	private ViewGroup content;
	private boolean touchable = true;
	private ProductEditPopAdapter adapter;
	private OnEditItemClickListener onEditItemClickListener;
	
	private int type = TYPE_LAYER;
	private int previousType = type;
	public static final int TYPE_PAGE = 1;
	public static final int TYPE_LAYER = 2;
	public static final int TYPE_PAGE_TEXT = 3;
	public static final int TYPE_COLOR_EFFECT = 4;
	
	private P page;
	private L layer;//layer can be null if it is not a layer pop
	private int contentMarginToPoint;
	private boolean showInLeft = true;
	
	private static final String TAG = "ProductEditPopView";
	
	public ProductEditPopView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ProductEditPopView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ProductEditPopView(Context context) {
		super(context);
		init(context);
		
	}
	
	private void init(Context context){
		this.context = context;
		inflate(context, R.layout.product_edit_popview, this);
		listView = (ListView) findViewById(R.id.list_view);
		content = (ViewGroup) findViewById(R.id.content);
		contentMarginToPoint = getContentLayoutParams().leftMargin;
		adapter = initAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(type != TYPE_COLOR_EFFECT && id==LAYER_COLOR_EFFECTS){
					previousType = type;
					type = TYPE_COLOR_EFFECT;
					//btnBack.setVisibility(View.VISIBLE);
					adapter.setInfo(TYPE_COLOR_EFFECT, page, layer);
					adapter.notifyDataSetChanged();
				}else if(onEditItemClickListener != null){
					onEditItemClickListener.onEditItemClick(ProductEditPopView.this,page, layer, (int)id);
				}

				
				
			}
		});
		

		
	};
	
	protected abstract ProductEditPopAdapter initAdapter();
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(touchable){
			return super.onInterceptTouchEvent(ev);
		}else{
			return true;
		}
	}
	
	private RelativeLayout.LayoutParams getContentLayoutParams(){
		return (RelativeLayout.LayoutParams)content.getLayoutParams();
	}
	
	public void setInfo(P page, L layer){
		this.page = page;
		this.layer = layer;
		
		if(layer != null){
			if(Layer.TYPE_IMAGE.equals(layer.type)){
				type = TYPE_LAYER;
			}else if(Layer.TYPE_TEXT_BLOCK.equals(layer.type)){
				type = TYPE_PAGE_TEXT;
			}
		}else{
			type = TYPE_PAGE;
		}
		
		if(type != TYPE_COLOR_EFFECT){
			//btnBack.setVisibility(View.INVISIBLE);
		}
		adapter.setInfo(type,page,layer);
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		
	}
	
	public void setShowInLeft(boolean showInLeft){
		this.showInLeft = showInLeft;
		if(showInLeft){
		
			getContentLayoutParams().leftMargin = 0;
			getContentLayoutParams().rightMargin = contentMarginToPoint;
		}else{
			
			getContentLayoutParams().leftMargin = contentMarginToPoint;
			getContentLayoutParams().rightMargin = 0;
		}
	}
	
	public int getType(){
		return type;
	}
	
//	public void setPointY(int y){
//		ImageView point = showInLeft ? ivPointerRight : ivPointerLeft;
//		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)point.getLayoutParams();
//		params.topMargin = y - point.getHeight()/2;
//	}
	
	public void setPopTouchable(boolean touchable){
		this.touchable = touchable;
	}
	
//	public void setOnDoneClickListener(OnClickListener onClickListener){
//		btnDone.setOnClickListener(onClickListener);
//	}
	
	public void setOnEditItemClickListener(OnEditItemClickListener onEditItemClickListener){
		this.onEditItemClickListener = onEditItemClickListener;
	}
	
	public static interface OnEditItemClickListener<V extends ProductEditPopView,P extends Page, L extends Layer>{
		void onEditItemClick(V view,P page,L layer,int itemId);
	}
	
	public boolean isShowLeft(){
		return showInLeft;
	}
	public class EditCollageAsyncTask extends AsyncTask<String, Void, CollagePage>{

		@Override
		protected void onPreExecute() {

			super.onPreExecute();
		}
		@Override
		protected CollagePage doInBackground(String... params) {
			CollageWebServices services=new CollageWebServices(context, "");
			CollagePage collagePage =null;
			//collagePage=services.rotateCollageContentTask(page, "", 90);
			
			return collagePage;
		}
		@Override
		protected void onPostExecute(CollagePage result) {
			
			super.onPostExecute(result);
		}
	}
}
