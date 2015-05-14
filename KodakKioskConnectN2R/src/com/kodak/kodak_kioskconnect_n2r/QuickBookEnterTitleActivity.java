package com.kodak.kodak_kioskconnect_n2r;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.AppContext;
import com.kodak.kodak_kioskconnect_n2r.bean.photobook.Photobook;
import com.kodak.utils.RSSLocalytics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class QuickBookEnterTitleActivity extends Activity{
	
	private Button vBackButton ;
	private Button vDoneButton ;
	private TextView vTextViewTitle ;
	
	private EditText vEditTextEnterTitle ;
	private ImageView vImageViewDivisionLine1 ;
	private EditText vEditTextEnterAuthor ;
	private ImageView vImageViewDivisionLine2 ;
	private EditText vEditTextEnterSubtitle ;
	
	private String mPhotoBookID ;
	private String mTitle ;
	private String mAuthor ;
	private String mSubtitle ;
	private final String SCREEN_NAME = "PB Project Data";
	
	private OnEditorActionListener mOnEditorActionListener ;
	private QBSetTitleTask mQBSetTitleTask ;
	private PrintMakerWebService mService ;
	
	private boolean isAutoEnterTitle ;
	private boolean isCanSetTitle ;
	private boolean isCanSetAuthor ;
	private boolean isCanSetSubTitle ;
	
	private Photobook photobook;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_photobook) ;
		RSSLocalytics.onActivityCreate(this);
		RSSLocalytics.recordLocalyticsPageView(this, SCREEN_NAME);
		findViews() ;
		setListeners() ;
		init() ;
		
	}

	private void findViews() {
		vBackButton = (Button) findViewById(R.id.backButton) ;
		vDoneButton = (Button) findViewById(R.id.nextButton) ;
		vTextViewTitle = (TextView) findViewById(R.id.headerBarText) ;
		
		vBackButton.setVisibility(View.INVISIBLE);
		vDoneButton.setVisibility(View.VISIBLE) ;
		vDoneButton.setText(R.string.done)  ;
		vTextViewTitle.setText(R.string.enter_title) ;
		
		vEditTextEnterTitle = (EditText) findViewById(R.id.edit_title) ;
		AppContext.getApplication().setEmojiFilter(vEditTextEnterTitle);
		vImageViewDivisionLine1 = (ImageView) findViewById(R.id.image_division_line1) ;
		
		vEditTextEnterAuthor = (EditText) findViewById(R.id.edit_author) ;
		AppContext.getApplication().setEmojiFilter(vEditTextEnterAuthor);
		vImageViewDivisionLine2 = (ImageView) findViewById(R.id.image_division_line2) ;
		
		vEditTextEnterSubtitle = (EditText) findViewById(R.id.edit_subtitle) ;
		AppContext.getApplication().setEmojiFilter(vEditTextEnterSubtitle);
	}

	private void setListeners() {
		mOnEditorActionListener = new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_DONE){
					doEditorActionDone() ;
				}
				return false;
			}
		};
		
		vDoneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				doEditorActionDone() ;
			}
		}) ;
		
//		vEditTextEnterTitle.setOnEditorActionListener(mOnEditorActionListener) ;
//		
//		vEditTextEnterAuthor.setOnEditorActionListener(mOnEditorActionListener) ;
		
		vEditTextEnterSubtitle.setOnEditorActionListener(mOnEditorActionListener) ;
	}
	
	@SuppressLint("InlinedApi")
	private void init() {
		Intent intent = getIntent() ;
		if(intent!=null){
			mPhotoBookID = intent.getStringExtra("photobookid") ;
			isAutoEnterTitle = intent.getBooleanExtra("isAutoEnterTitle", false) ;
		}
		/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuickBookEnterTitleActivity.this);
		isCanSetTitle = prefs.getBoolean(PrintHelper.sCanSetTitle, true) ;
		isCanSetAuthor = prefs.getBoolean(PrintHelper.sCanSetAuthor, true) ;
        isCanSetSubTitle = prefs.getBoolean(PrintHelper.sCanSetSubtitle, true) ;*/
		photobook = AppContext.getApplication().getPhotobook();
		isCanSetTitle = photobook.canSetTitle;
		isCanSetAuthor = photobook.canSetAuthor;
        isCanSetSubTitle = photobook.canSetSubtitle;
        
        vEditTextEnterTitle.setVisibility(isCanSetTitle?View.VISIBLE:View.GONE) ;
        vImageViewDivisionLine1.setVisibility(isCanSetTitle?View.VISIBLE:View.GONE) ;
        vEditTextEnterAuthor.setVisibility(isCanSetAuthor?View.VISIBLE:View.GONE) ;
        vImageViewDivisionLine2.setVisibility(isCanSetAuthor?View.VISIBLE:View.GONE) ;
        vEditTextEnterSubtitle.setVisibility(isCanSetSubTitle?View.VISIBLE:View.GONE) ;
        
		if(!isCanSetSubTitle){
			if(!isCanSetAuthor){
				vEditTextEnterTitle.setImeOptions(EditorInfo.IME_ACTION_DONE) ;
				vEditTextEnterTitle.setOnEditorActionListener(mOnEditorActionListener) ;
			}else {  //can set author
				vEditTextEnterAuthor.setImeOptions(EditorInfo.IME_ACTION_DONE) ;
				vEditTextEnterAuthor.setOnEditorActionListener(mOnEditorActionListener) ;
			}
		}
        
        
		/*String titleInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_title") ;
		String authorInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_author") ;
		String subTitleInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_subtitle") ;*/
		String titleInLocal = photobook.title;
		String authorInLocal = photobook.author;
		String subTitleInLocal = photobook.subTitle;
		if(isAutoEnterTitle&& isCanSetSubTitle &&TextUtils.isEmpty(titleInLocal)
				&& TextUtils.isEmpty(authorInLocal)
				&& TextUtils.isEmpty(subTitleInLocal)
				){
			//vEditTextEnterSubtitle set the default text (mounth and year)
			SimpleDateFormat f = new SimpleDateFormat("MMMM-yyyy");
			Date date = new Date() ;
			String dateStr  = f.format(date) ;
			vEditTextEnterSubtitle.setText(dateStr) ;
			
		}else {
			if(isCanSetTitle){
				vEditTextEnterTitle.setText(titleInLocal) ;
			}
		
			if(isCanSetAuthor){
				vEditTextEnterAuthor.setText(authorInLocal) ;
			}
			
			if(isCanSetSubTitle){
				vEditTextEnterSubtitle.setText(subTitleInLocal) ;
			}
			
			
			
		}
		
		
		mService = new PrintMakerWebService(QuickBookEnterTitleActivity.this, "") ;
		
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		
		if(keyCode==KeyEvent.KEYCODE_BACK){
//			finish() ;
//			this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_left) ;
			return false ;
		}
		
		
		return super.onKeyDown(keyCode, event);
		
		
	}
	
	
	/**
	 * when press down the "done" button on the soft keyboard or press "Done" button
	 */
	private void doEditorActionDone(){
		mTitle = vEditTextEnterTitle.getText().toString() ;
		mAuthor = vEditTextEnterAuthor.getText().toString() ;
		mSubtitle = vEditTextEnterSubtitle.getText().toString() ;
		
		//if title,author,subtitle has changed then we post the request
		// else  just finish this actiity.
		/*String titleInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_title") ;
		String authorInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_author") ;
		String subtitleInLocal = getValueFromSharedPreferenceByKey("QuickBookEnterTitleActivity_subtitle") ;*/
		String titleInLocal = photobook.title;
		String authorInLocal = photobook.author;
		String subtitleInLocal = photobook.subTitle;
		
		boolean isTitileChanged = !mTitle.equals(titleInLocal) ;
		boolean isAuthorChanged = !mAuthor.equals(authorInLocal) ;
		boolean isSubTitleChanged = !mSubtitle.equals(subtitleInLocal) ;
		
		if(isTitileChanged||isAuthorChanged||isSubTitleChanged){
			//post request for setTitle
			mQBSetTitleTask = new QBSetTitleTask() ;
			mQBSetTitleTask.execute(mPhotoBookID,mTitle,mAuthor,mSubtitle) ;
			
		}else {
			//back...
			finish() ;
			this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_left) ;
		}
	}
	
	private String getValueFromSharedPreferenceByKey(String key){
		String value = "" ;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuickBookEnterTitleActivity.this);
		value = prefs.getString(key, "") ;
        return value ;		
	}
	
	
	private void saveInfoToLocal(String title ,String author ,String subTitle){
		/*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(QuickBookEnterTitleActivity.this);
		Editor editor =   prefs.edit() ;
		editor.putString("QuickBookEnterTitleActivity_title", tilte) ;
		editor.putString("QuickBookEnterTitleActivity_author", author) ;
		editor.putString("QuickBookEnterTitleActivity_subtitle", subTitile) ;
		editor.commit() ;*/
		photobook.title = title;
		photobook.author = author;
		photobook.subTitle = subTitle;
	}
	
	
	private void showErrorDialog(){
		InfoDialog.InfoDialogBuilder builder = new InfoDialog.InfoDialogBuilder(this);
		builder.setTitle("");
		String message = getString(R.string.title_page_set_failed);
		builder.setMessage(message);
		builder.setPositiveButton(getString(R.string.OK), new  DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss() ;
//				Intent intent = new Intent(QuickBookEnterTitleActivity.this,QuickBookFlipperActivity.class) ;
//				setResult(RESULT_CANCELED,intent) ;
				finish() ;
				QuickBookEnterTitleActivity.this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_left) ;
			}
			
		}) ;
		
		builder.create().show() ;
		
		
		
	}
	
	
	
	class QBSetTitleTask extends AsyncTask<String, Void, String>{
		private WaitingDialog waitingDialog;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			waitingDialog = new WaitingDialog(QuickBookEnterTitleActivity.this, R.string.animation_quickbook_wait) ;
			waitingDialog.setCancelable(false) ;
			waitingDialog.show() ;
			
			
		}
        
		@Override
		protected String doInBackground(String... params) {
			String resultStr = mService.pbSetTitle(QuickBookEnterTitleActivity.this,
					params[0], params[1], params[2], params[3]) ;
			return resultStr;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(waitingDialog!=null && waitingDialog.isShowing()){
				waitingDialog.dismiss() ;
			}
			if(!TextUtils.isEmpty(result)){
				//save title,author,subtitle,back to pre-activity
				saveInfoToLocal(mTitle, mAuthor, mSubtitle) ;
				Intent intent = new Intent(QuickBookEnterTitleActivity.this,QuickBookFlipperActivity.class) ;
				setResult(RESULT_OK,intent) ;
				finish() ;
				QuickBookEnterTitleActivity.this.overridePendingTransition(R.anim.slide_out_left, R.anim.slide_out_left) ;
				
				
			}else {
				//show error dialog
				showErrorDialog() ;
				
			}
			
			
			
		}
		
	}
	
	@Override
	protected void onResume() {
		RSSLocalytics.onActivityResume(this);
		super.onResume();
	}
	
	@Override
 	protected void onPause() {
		RSSLocalytics.onActivityPause(this);
	 	super.onPause();
 	}

}
