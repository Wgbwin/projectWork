package com.kodak.rss.tablet.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.load.FilePathConstant;
import com.kodak.rss.tablet.util.load.ImageUseURIDownloader;
import com.kodak.rss.tablet.util.load.Request;
import com.kodak.rss.tablet.util.load.Response;
import com.kodak.rss.tablet.util.load.onProcessImageResponseListener;
import com.kodak.rss.tablet.view.ProjectDuplexItemsView;

public class ProjectsAdapter extends BaseAdapter implements onProcessImageResponseListener {		
	private Context context;	
	private List<Project[]> projectsList;	
	public OnSelectProjectListener onSelectProjectListener;
	public OnDeleteProjectListener onDeleteProjectListener;	
	
	public ImageUseURIDownloader imageDownloader;
	public final Map<String, Request> pendingRequests = new HashMap<String, Request>();
	public LruCache<String, Bitmap> mMemoryCache; 
	
	public ProjectsAdapter(Context context,List<Project> projectList,LruCache<String, Bitmap> mMemoryCache) {
		this.context = context;
		this.mMemoryCache = mMemoryCache;
		this.imageDownloader = new ImageUseURIDownloader(context,pendingRequests,this);	
		this.imageDownloader.setSaveType(FilePathConstant.projectType);
		this.imageDownloader.setIsThumbnail(true);				
		if (projectList != null) {
			int size = projectList.size();
			int duplexItemsSize = (int) Math.ceil(size*1f/2); 
			this.projectsList = new ArrayList<Project[]>(duplexItemsSize);			
			for (int i = 0; i < duplexItemsSize; i++) {
				Project[] itemsValue = new Project[2];
				if (2*i < size) {
					itemsValue[0] = projectList.get(2*i);
				}
				if (2*i+1 < size) {
					itemsValue[1] = projectList.get(2*i+1);
				}	
				projectsList.add(itemsValue);			
			}			
		}		
	}
	
	public interface OnSelectProjectListener{
		public void onSelectProject(Project project);
	}
	
	public interface OnDeleteProjectListener{
		public void onDeleteProject(Project project);
	}
	
	public void setOnSelectProjectListener(OnSelectProjectListener listener){
		this.onSelectProjectListener = listener;
	}
	
	public void setOnDeleteProjectListener(OnDeleteProjectListener listener){
		this.onDeleteProjectListener = listener;
	}

	@Override
	public int getCount() {
		int count = 0;
		if (projectsList != null) {
			count = projectsList.size();
		}
		return count;
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
		ProjectDuplexItemsView projectDuplexItemsView = null;
		if (convertView == null) {			
			projectDuplexItemsView = new ProjectDuplexItemsView(context,ProjectsAdapter.this);		
			convertView = projectDuplexItemsView;
			convertView.setTag(projectDuplexItemsView);
		} else {
			projectDuplexItemsView = (ProjectDuplexItemsView) convertView.getTag();
		}
		Project[] itemsValue = projectsList.get(position);		
		projectDuplexItemsView.setData(itemsValue,position);
		
		return convertView;
      }

	@Override
	public void onProcess(Response response, String profileId, View view,int position,String flowTpye,String productId) {			
		if (response == null||imageDownloader==null) return;									
		if (response.getError() != null) {
			
		}else{			
			Bitmap bitmap = response.getBitmap();
			MemoryCacheUtil.putBitmap(mMemoryCache, profileId, bitmap);			
			if (bitmap != null && view != null && view instanceof ImageView) {	
				if (view.getTag().toString().equals(profileId)) {
					((ImageView) view).setImageBitmap(bitmap);
				}
			}															
		}
	}

}
	