package com.kodak.rss.tablet.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kodak.rss.core.n2r.bean.project.Project;
import com.kodak.rss.core.util.RSSLocalytics;
import com.kodak.rss.core.util.SharedPreferrenceUtil;
import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.RssTabletApp;
import com.kodak.rss.tablet.adapter.ProjectsAdapter;
import com.kodak.rss.tablet.adapter.ProjectsAdapter.OnDeleteProjectListener;
import com.kodak.rss.tablet.adapter.ProjectsAdapter.OnSelectProjectListener;
import com.kodak.rss.tablet.thread.DeleteProjectTask;
import com.kodak.rss.tablet.thread.LoadProjectTask;
import com.kodak.rss.tablet.util.MemoryCacheUtil;
import com.kodak.rss.tablet.util.RSSTabletLocalytics;
import com.kodak.rss.tablet.view.dialog.InfoDialog;

public class MyProjectsActivity extends BaseNetActivity implements OnClickListener {
	
	public ViewPager viewpager;
	private RadioGroup radiogroup;
	private ImageView arrowLeft, arrowRight;
	private LayoutInflater mInflater;
	private ViewPagerAdapter adapter;
	private List<View> mList;
	private TextView promptTxt;
	private int index = 0;
	private List <Project> projects;
	public LruCache<String, Bitmap> mMemoryCache; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_projects);
		mMemoryCache = MemoryCacheUtil.generMemoryCache(16);
		initView();
		initActionAndData();
	}

	private void initView() {
		mInflater = getLayoutInflater();
		viewpager = (ViewPager) findViewById(R.id.viewPager);
		radiogroup = (RadioGroup) findViewById(R.id.circle_tab);
		arrowLeft = (ImageView) findViewById(R.id.arrow_left);
		arrowRight = (ImageView) findViewById(R.id.arrow_right);
		
		promptTxt = (TextView) findViewById(R.id.prompt);	
		
		String promptStr = getString(R.string.l_project);
		String facebookUserId = SharedPreferrenceUtil.getFacebookUserId(MyProjectsActivity.this);          
		if (facebookUserId !=null && !"".equals(facebookUserId)) {
			String firstName = SharedPreferrenceUtil.getFacebookFristName(MyProjectsActivity.this);
			String lastName = SharedPreferrenceUtil.getFacebookLastName(MyProjectsActivity.this);
			promptStr = getString(R.string.use_facebook_login_prompt)+" " + firstName +" "+lastName;
		}
		promptTxt.setText(promptStr);

		findViewById(R.id.previous_button).setOnClickListener(this);
		arrowRight.setOnClickListener(this);
		arrowLeft.setOnClickListener(this);
	}

	public void initActionAndData() {
		index = 0;
		mList = null;
		adapter = null;		
		radiogroup.removeAllViews();
		projects = RssTabletApp.getInstance().projects;
		if (projects == null || (projects != null && projects.size() == 0)) {	
			viewpager.setAdapter(null);
			udpateArrowView(0,index);	
			radiogroup.setVisibility(View.INVISIBLE);
			
			new InfoDialog.Builder(this).setMessage(R.string.no_project_find)
			.setPositiveButton(getText(R.string.d_ok), null).create()
			.show();				
			return;
		}		
		int size = projects.size();
		final int viewSize = (int) Math.ceil(size*1f/8); 		
		if (viewSize == 0)  {	
			viewpager.setAdapter(null);
			udpateArrowView(0,index);
			radiogroup.setVisibility(View.INVISIBLE);
			return;
		}
		mList = new ArrayList<View>(viewSize);
		for (int i = 0; i < viewSize; i++) {
			View view = mInflater.inflate(R.layout.projects_view_item, null);
			GridView projectsGView = (GridView) view.findViewById(R.id.gv_projects);			
			List<Project> items = new ArrayList<Project>(8);			
			for (int j = 0,pos = 8*i; j < 8; j++) {
				Project item = null;
				int position = pos+j;
				if (position < size) {
					item = projects.get(position);
					items.add(item);	
				}else {
					break;
				}				
			}
			ProjectsAdapter projectsAdapter = new ProjectsAdapter(MyProjectsActivity.this, items,mMemoryCache);
			projectsAdapter.setOnSelectProjectListener(new OnSelectProjectListener() {				
				@Override
				public void onSelectProject(Project project) {
					if (project != null) {
						//Localytics
						HashMap<String,String> map = new HashMap<String, String>();
						map.put(RSSTabletLocalytics.LOCALYTICS_KEY_SELECT_PROJECT_TYPE, project.type);
						RSSLocalytics.recordLocalyticsEvents(MyProjectsActivity.this, RSSTabletLocalytics.LOCALYTICS_EVENT_PROJECT_LOAD, map);
						LoadProjectTask loadTask = new LoadProjectTask(MyProjectsActivity.this, project); 
						loadTask.execute();	
					}									
				}
			});
			projectsAdapter.setOnDeleteProjectListener(new OnDeleteProjectListener() {				
				@Override
				public void onDeleteProject(final Project project) {
					if (project == null) return;
					android.content.DialogInterface.OnClickListener yesOnClickListener  = new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							deleteProject(project);				
						}		
					};	
					String promptStr = TextUtil.formatHighlightText(getString(R.string.delete_project_prompt),getResources().getColor(R.color.white), project.projectName).toString();					
					new InfoDialog.Builder(MyProjectsActivity.this).setMessage(promptStr)
					.setPositiveButton(getText(R.string.d_no), null)
					.setNegativeButton(R.string.d_yes, yesOnClickListener)
					.create()
					.show();						
				}
			});				
			projectsGView.setAdapter(projectsAdapter);
			mList.add(view);
				
			RadioButton rButton = new RadioButton(MyProjectsActivity.this);				
			rButton.setBackgroundDrawable(null);
			rButton.setButtonDrawable(R.drawable.radiobutton);	
			radiogroup.addView(rButton);			
		}

		adapter = new ViewPagerAdapter(mList);
		viewpager.setAdapter(adapter);
		viewpager.setOnPageChangeListener(new PageChangeListener(viewSize));

		udpateArrowView(viewSize,index);
		if (viewSize < 2) {
			radiogroup.setVisibility(View.INVISIBLE);
		}else {
			((RadioButton) radiogroup.getChildAt(index)).setChecked(true);
		}
	}

	private void udpateArrowView(int viewNum ,int index) {
		if (viewNum < 2) {
			arrowLeft.setVisibility(View.INVISIBLE);
			arrowRight.setVisibility(View.INVISIBLE);
			return;
		}		
		if (index == 0) {
			arrowRight.setVisibility(View.VISIBLE);
			arrowLeft.setVisibility(View.INVISIBLE);
		} else if (index == viewNum-1) {
			arrowLeft.setVisibility(View.VISIBLE);
			arrowRight.setVisibility(View.INVISIBLE);
		} else {
			arrowLeft.setVisibility(View.VISIBLE);
			arrowRight.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void judgeHaveItems(){
		if (judgeSelectHavedProductInfo()) {			
			Intent mIntent = new Intent(this, ShoppingCartActivity.class);
			startActivity(mIntent);	
			this.finish();
		}else {
			popNoItemDialog();
		}		
	}	

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.arrow_left) {
			index--;
			viewpager.setCurrentItem(index);
		} else if (v.getId() == R.id.arrow_right) {
			index++;
			viewpager.setCurrentItem(index);
		} else if (v.getId() == R.id.previous_button) {
			viewpager.setAdapter(null);
			RssTabletApp.getInstance().projects = null;		
			previousDoMoreOver();
		}
	}
	
	private void deleteProject(Project project){
		DeleteProjectTask deleteProjectTask = new DeleteProjectTask(MyProjectsActivity.this, project);
		deleteProjectTask.execute();
	}
	
	private class ViewPagerAdapter extends PagerAdapter {
		private List<View> views;

		private ViewPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(views.get(position));
			return views.get(position);
		}
	};
	
	private class  PageChangeListener implements OnPageChangeListener {
		int size;
		
		private PageChangeListener(int viewSize) {
			this.size = viewSize;
		}
		
		@Override
		public void onPageSelected(int postion) {
			((RadioButton) radiogroup.getChildAt(postion)).setChecked(true);
			index = postion;
			udpateArrowView(size,index);
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {}
	};
	
	@Override
	protected void onPause() {			
		MemoryCacheUtil.evictAll(mMemoryCache);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();				
		mMemoryCache = null;
	}
	
}
