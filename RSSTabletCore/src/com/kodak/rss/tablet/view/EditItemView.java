package com.kodak.rss.tablet.view;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.kodak.rss.core.bean.ImageInfo;
import com.kodak.rss.core.n2r.bean.imageedit.ColorEffect;
import com.kodak.rss.core.n2r.webservice.WebService;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.activities.PrintsActivity;
import com.kodak.rss.tablet.adapter.PopEditAdapter;
import com.kodak.rss.tablet.adapter.PrintEditListAdapter;
import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.thread.ToolColorEffectEditTask;
import com.kodak.rss.tablet.thread.ToolEnhanceEditTask;
import com.kodak.rss.tablet.thread.ToolRedEyeEditTask;

/**
 * Purpose: 
 * Author: Bing Wang
 */
public class EditItemView extends LinearLayout{  
	private Button editBtn;
	private TextView editTxt;	
	public String category;
	public double price;
	public int num;	
	float scale = 1.0f;
	BitmapDrawable bd;
	PrintEditListAdapter editListAdapter;
	int position;	
	private Context context;
	private PrintsActivity activity;
	private Handler editHandler;
	private WebService service = null;	
	private PopupWindow mPopupWindow;
	private ImageEditView editImage;
	
	public EditItemView(Context context,PrintEditListAdapter editListAdapter, Handler handler,ImageEditView editImage) {
		super(context);
		this.context = context;
		activity = (PrintsActivity) context;
		inflate(context,R.layout.edit_item, this);	
		this.editListAdapter = editListAdapter;		
		this.editHandler = handler;	
		this.editImage = editImage;	
		
		editBtn = (Button) findViewById(R.id.btn_edit);
		editTxt = (TextView) findViewById(R.id.txt_edit);
	}		

	public void initViewAndAction(int pos){
		this.position = pos;	
		PrintEditInfo editInfo = editListAdapter.editList.get(pos);
		if (editInfo == null) return;
		editBtn.setBackgroundResource(editInfo.getBtn_id());		
		editBtn.setEnabled(editInfo.isEnabled());			
		editBtn.setOnClickListener(editClick);
		editTxt.setText(editInfo.getTxt_id());
		editTxt.setEnabled(editInfo.isEnabled());			
		editTxt.setOnClickListener(editClick);		
	}
		
	private OnClickListener editClick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			PrintEditInfo editInfo = editListAdapter.editList.get(position);
			RssTabletApp app = RssTabletApp.getInstance();
			if (editInfo.getName().equals("color")) {
				if (mPopupWindow == null || !mPopupWindow.isShowing()) {					
					popEditWindow((int)(210*activity.dm.density),(int)(activity.screenHeight*0.6), app.getColorEffectList());			
				} else {				
					mPopupWindow.dismiss();
				}	
			}else if (editInfo.getName().equals("redeye")){
				if (service == null) {
					service = new WebService(context);
				}		
				new Thread(new ToolRedEyeEditTask(app.chosenList, editHandler, service)).start();
			}else if (editInfo.getName().equals("enhance")){
				if (service == null) {
					service = new WebService(context);
				}		
				new Thread(new ToolEnhanceEditTask(app.chosenList, editHandler, service)).start();	
			}else if (editInfo.getName().equals("rotate")){
				editImage.rotate();
			}
		}		
	};
	
	private void popEditWindow(int width, int height, final List<ColorEffect> colorEffectList ) {
		if (mPopupWindow != null) {
			mPopupWindow = null;
		}
		
		if (!colorEffectList.isEmpty()) {
			final PopEditAdapter popEditAdapter = new PopEditAdapter(context,colorEffectList,activity.colorEffectResources);			
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.pop_coloreffect, null);			
			final ListView mPopListView  = (ListView)view.findViewById(R.id.list_coloreffect);

			mPopListView.setDivider(null);
			mPopListView.setVerticalScrollBarEnabled(true);			
			mPopListView.setAdapter(popEditAdapter);						
			mPopListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final int colorIndex = position;
					ImageInfo imageInfo = null;
					RssTabletApp app = RssTabletApp.getInstance();
					for(int i = 0; i < app.chosenList.size();i++){					
						if (app.chosenList.get(i).isCurrentChosen) {	
							imageInfo = app.chosenList.get(i);
						}
					}					
					for (int i = 0; i < colorEffectList.size(); i++) { 					
						if (i == position) {							
							colorEffectList.get(position).isChecked =true;
						}else {
							colorEffectList.get(i).isChecked =false;
						}					
					}	
					if (service == null) {
						service = new WebService(context);
					}	
					new Thread(new ToolColorEffectEditTask(imageInfo, editHandler, service, colorEffectList,colorIndex)).start();					
					popEditAdapter.notifyDataSetChanged();
					activity.progressBar.setVisibility(View.VISIBLE);
				}				
			});
			mPopupWindow = new PopupWindow(view,width,height);				
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));					
			mPopupWindow.setOnDismissListener(new OnDismissListener() {			
				@Override
				public void onDismiss() {
					mPopListView.setAdapter(null);	
					activity.progressBar.setVisibility(View.GONE);
					editImage.cropDismiss();					
				}
			});
			editImage.cropDismiss();
			mPopupWindow.showAtLocation(activity.panel, Gravity.LEFT|Gravity.TOP, 
					(int)(activity.dm.density * 250+editImage.LT - width), (int)((editImage.picCanvasHeight-height)/2+editImage.TP+activity.dm.density * 100));			
		}
	}	
	
}
