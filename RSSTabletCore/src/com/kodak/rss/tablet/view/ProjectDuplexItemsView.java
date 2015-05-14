package com.kodak.rss.tablet.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.adapter.ProjectsAdapter;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class ProjectDuplexItemsView extends LinearLayout{      
    private ProjectItemView upItemView;  
    private ProjectItemView downItemView;		    
    private ProjectsAdapter adapter;
    
	public ProjectDuplexItemsView(Context context,ProjectsAdapter adapter) {
		super(context);
		this.adapter = adapter;
		initView(context);	
	}	

	public ProjectDuplexItemsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context){
		inflate(context,R.layout.project_duplex_items, this);				
		upItemView = (ProjectItemView) findViewById(R.id.upItem);
		downItemView = (ProjectItemView) findViewById(R.id.downItem);				
	}

    public void setData(Project[] projects,int position){
    	if (projects[0] != null) {
    		upItemView.setData(projects[0],adapter,position);
		}else {
			upItemView.setVisibility(View.GONE);
		}
    	if (projects[1] != null) {
    		downItemView.setData(projects[1],adapter,position);  		
    	}else {
    		downItemView.setVisibility(View.GONE);
		}
    	
    	
    }	
}
