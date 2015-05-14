package com.kodak.rss.tablet.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kodak.rss.tablet.bean.PrintEditInfo;
import com.kodak.rss.tablet.view.EditItemView;
import com.kodak.rss.tablet.view.ImageEditView;

/**
 * Purpose: prints edit List
 * Author: Bing Wang
 */
public class PrintEditListAdapter  extends BaseAdapter {
	
	private Context context;	
	public List<PrintEditInfo> editList;	
	private Handler editHandler;
	private ImageEditView editImage;
	
	public PrintEditListAdapter(Context context,List<PrintEditInfo> editList,Handler handler,ImageEditView editImage) {
		this.context = context;	
		this.editList = editList;
		this.editHandler = handler;	
		this.editImage = editImage;			
	}

	@Override
	public int getCount() {		
		return editList.size();
	}

	@Override
	public Object getItem(int position) {		
		return position;
	}

	@Override
	public long getItemId(int position) {		
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		EditItemView itemView;
		if (convertView == null) {						
			itemView = new EditItemView(context, PrintEditListAdapter.this, editHandler, editImage);
			convertView = itemView;
			convertView.setTag(itemView);
		}else {
			itemView = (EditItemView) convertView.getTag();
		}		
		itemView.initViewAndAction(position);									
		return convertView;
	}

}
