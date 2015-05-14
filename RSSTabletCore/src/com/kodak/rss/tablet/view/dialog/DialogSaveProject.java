package com.kodak.rss.tablet.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.kodak.rss.core.util.TextUtil;
import com.kodak.rss.tablet.R;
import com.kodak.rss.tablet.thread.SaveProjectTask;

public class DialogSaveProject extends Dialog implements OnClickListener{
		
	private Context mContext;
	private EditText projectNameText;
	private View dialogView;
	private String defaultName;
	private String saveId;
	
	public DialogSaveProject(Context context,String defaultName,String saveId) {
		super(context);
		this.mContext = context;
		this.defaultName = defaultName;
		this.saveId = saveId;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		dialogView = inflater.inflate(R.layout.dialog_save_project, null);			
		dialogView.findViewById(R.id.save_button).setOnClickListener(this);
		dialogView.findViewById(R.id.cancel_button).setOnClickListener(this);
		projectNameText = (EditText) dialogView.findViewById(R.id.project_name);
		TextUtil.addEmojiFilter(projectNameText);
		
		if(defaultName != null && !"".equals(defaultName)){
			projectNameText.setText(defaultName);
			projectNameText.setSelection(defaultName.length());
		}
	
		setContentView(dialogView);	
		ViewGroup.LayoutParams dialogLp = dialogView.getLayoutParams();
		DisplayMetrics dm = mContext.getResources().getDisplayMetrics();	
		dialogLp.height = dm.heightPixels/3;		
		dialogLp.width = dm.widthPixels/3;			
		dialogView.setLayoutParams(dialogLp);					
		setCanceledOnTouchOutside(false);
	}


	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.save_button){				
			String projectName = projectNameText.getText().toString().trim();						
			SaveProjectTask saveProjectTask = new SaveProjectTask(mContext,projectName,saveId);
			dismiss();		
			saveProjectTask.execute();				
		}else if(v.getId()==R.id.cancel_button){
			dismiss();	
		}		
	}		
}
