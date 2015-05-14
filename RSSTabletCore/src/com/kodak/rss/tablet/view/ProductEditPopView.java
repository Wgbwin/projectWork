package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.kodak.rss.core.n2r.bean.prints.Layer;
import com.kodak.rss.core.n2r.bean.prints.Page;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.ProductEditPopAdapter;

public abstract class ProductEditPopView<P extends Page, L extends Layer> extends FrameLayout{
	//item id for click listener
	public static final int PAGE_ADD_PAGE=1;
	public static final int PAGE_DELETE_PAGE=2;
	public static final int PAGE_ADD_PAGE_TEXT=3;
	public static final int PAGE_EDIT_PAGE_TEXT=4;
	public static final int PAGE_DELETE_PAGE_TEXT=5;
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
	private Button btnDone;
	private Button btnBack;
	private ListView listView;
	private ImageView ivPointerLeft;
	private ImageView ivPointerRight;
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
		btnDone = (Button) findViewById(R.id.btn_done);
		btnBack = (Button) findViewById(R.id.btn_back);
		listView = (ListView) findViewById(R.id.list_view);
		ivPointerLeft = (ImageView) findViewById(R.id.pointer_left);
		ivPointerRight = (ImageView) findViewById(R.id.pointer_right);
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
					btnBack.setVisibility(View.VISIBLE);
					adapter.setInfo(TYPE_COLOR_EFFECT, page, layer);
					adapter.notifyDataSetChanged();
				}else if(onEditItemClickListener != null){
					onEditItemClickListener.onEditItemClick(ProductEditPopView.this,page, layer, (int)id);
				}
			}
		});
		
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnBack.setVisibility(View.INVISIBLE);
				type = previousType;
				adapter.setInfo(type, page, layer);
				adapter.notifyDataSetChanged();
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
			btnBack.setVisibility(View.INVISIBLE);
		}
		adapter.setInfo(type,page,layer);
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		
	}
	
	public void setShowInLeft(boolean showInLeft){
		this.showInLeft = showInLeft;
		if(showInLeft){
			ivPointerLeft.setVisibility(View.GONE);
			ivPointerRight.setVisibility(View.VISIBLE);
			getContentLayoutParams().leftMargin = 0;
			getContentLayoutParams().rightMargin = contentMarginToPoint;
		}else{
			ivPointerLeft.setVisibility(View.VISIBLE);
			ivPointerRight.setVisibility(View.GONE);
			getContentLayoutParams().leftMargin = contentMarginToPoint;
			getContentLayoutParams().rightMargin = 0;
		}
	}
	
	public int getType(){
		return type;
	}
	
	public void setPointY(int y){
		ImageView point = showInLeft ? ivPointerRight : ivPointerLeft;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)point.getLayoutParams();
		params.topMargin = y - point.getHeight()/2;
	}
	
	public void setPopTouchable(boolean touchable){
		this.touchable = touchable;
	}
	
	public void setOnDoneClickListener(OnClickListener onClickListener){
		btnDone.setOnClickListener(onClickListener);
	}
	
	public void setOnEditItemClickListener(OnEditItemClickListener onEditItemClickListener){
		this.onEditItemClickListener = onEditItemClickListener;
	}
	
	public static interface OnEditItemClickListener<V extends ProductEditPopView,P extends Page, L extends Layer>{
		void onEditItemClick(V view,P page,L layer,int itemId);
	}
	
	public boolean isShowLeft(){
		return showInLeft;
	}
}
