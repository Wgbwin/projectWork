package com.kodak.rss.tablet.view;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.ProjectsAdapter;
import com.kodak.rss.tablet.util.MemoryCacheUtil;

/**
 * Purpose: 
 * Author: Bing Wang 
 */
public class ProjectItemView extends LinearLayout implements OnClickListener{   
    private Context mContext;
    private ImageView projectImage;
    private LinearLayout describeView;	
    private TextView projectName;
    private TextView productName;
    private TextView saveTime;
    private TextView expiresTime;
    private ImageView deleteImage;		
    
    private DisplayMetrics dm;
    private LinearLayout.LayoutParams mILayoutParams;
	private LinearLayout.LayoutParams mLLayoutParams;
	private int imageHeight,describeHeight;	
	private Project project;
	private ProjectsAdapter adapter;	
    
	public ProjectItemView(Context context) {
		super(context);				
		initView(context);	
	}	

	public ProjectItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	private void initView(Context context){
		inflate(context,R.layout.project_item, this);	
		this.mContext = context;
		this.dm = context.getResources().getDisplayMetrics();		
		
		projectImage = (ImageView) findViewById(R.id.project_image);
		describeView = (LinearLayout) findViewById(R.id.project_describe);
		projectName = (TextView) findViewById(R.id.project_name);
		productName = (TextView) findViewById(R.id.product_name);
		saveTime = (TextView) findViewById(R.id.save_time);
		expiresTime = (TextView) findViewById(R.id.expires_time);
		deleteImage = (ImageView) findViewById(R.id.delete_image);

		projectImage.setOnClickListener(this);
		describeView.setOnClickListener(this);
		deleteImage.setOnClickListener(this);
		
		imageHeight = (int) ((dm.heightPixels - dm.density*(195+RssTabletApp.getInstance().statusBarHeight))/3);		
		describeHeight = (int) ((dm.heightPixels - dm.density*(195+RssTabletApp.getInstance().statusBarHeight))/6);	
		
		mILayoutParams = (LayoutParams) projectImage.getLayoutParams();
		mILayoutParams.height = imageHeight;
		projectImage.setLayoutParams(mILayoutParams);
		
		mLLayoutParams = (LayoutParams) describeView.getLayoutParams();
		mLLayoutParams.height = describeHeight;
		describeView.setLayoutParams(mLLayoutParams);
	}

	public void setData(Project project,ProjectsAdapter adapter,int position){		
		this.adapter = adapter;	
		this.project = project;
		
		String Saved = "Saved: ";
		String Expires = "Expires: ";
		
		Saved = mContext.getString(R.string.Saved)+": ";
		Expires = mContext.getString(R.string.Expires)+": ";
		
		projectName.setText(project.projectName);
		productName.setText(project.productDescriptionIdLocalized);		
		saveTime.setText(Saved+getDate(project.creationDate));
		expiresTime.setText(Expires+getDate(project.expirationDate));
		
		URI pictureURI = getUrl(project);	
		if (pictureURI != null) {
			String projectId = project.id;		
			Bitmap mBitmap = MemoryCacheUtil.getBitmap(adapter.mMemoryCache, projectId); 			
			if (mBitmap == null) {				  	    	        	 					
				projectImage.setTag(projectId);
				adapter.imageDownloader.downloadProfilePicture(projectId, pictureURI, projectImage,position,true);					  	  	 							        	
			}
			projectImage.setImageBitmap(mBitmap);
		}else {
			projectImage.setImageBitmap(null);
		}	
	}
			
	@Override
	public void onClick(View v) {
		if (v.getId()== R.id.project_image || v.getId()== R.id.project_describe ) {
			if (adapter != null && adapter.onSelectProjectListener != null) {
				adapter.onSelectProjectListener.onSelectProject(project);
			}		
		}else if (v.getId()== R.id.delete_image) {
			if (adapter != null && adapter.onDeleteProjectListener != null) {
				adapter.onDeleteProjectListener.onDeleteProject(project);
			}			
		}		
	}
	
	private String getDate(String dateStr){		
		String sDateTime ="";
		int subStart = dateStr.indexOf("(");
		int subEnd =  dateStr.lastIndexOf(")");
		String saveName = dateStr.substring(subStart+1, subEnd-3);
		long time = Long.valueOf(saveName);
		SimpleDateFormat sdf= new SimpleDateFormat("yy/MM/dd");		
		java.util.Date dt = new Date(time* 1000);  
		sDateTime = sdf.format(dt);  
		return sDateTime;
	}
	
	public static URI getUrl(Project project){
		URI pictureURI = null;
		try {
			if (project != null) {			
				String url = project.glyphURL;
				url= url.replaceAll(" ", "%20");
				pictureURI = new URI(url);			
			}
		} catch (URISyntaxException e) {
			pictureURI = null;
		}
		return pictureURI;
	}
	
}
