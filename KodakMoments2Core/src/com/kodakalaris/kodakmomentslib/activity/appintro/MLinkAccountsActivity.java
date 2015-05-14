package com.kodakalaris.kodakmomentslib.activity.appintro;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kodakalaris.kodakmomentslib.R;
import com.kodakalaris.kodakmomentslib.widget.mobile.MActionBar;


public class MLinkAccountsActivity  extends BaseLinkAccountsActivity{
	private RelativeLayout vRelaLyFacebook;
	private RelativeLayout vRelaLyInstagram;
	private RelativeLayout vRelaLyFlickr;
	private RelativeLayout vRelaLydropboxbtn;
	private ImageView vImgFacebook;
	private ImageView vImgInstagram;
	private ImageView vImgFlickr;
	private ImageView vImgDropbox;
	private TextView vTxtFacebook;
	private TextView vTxtInstagram;
	private TextView vTxtFlickr;
	private TextView vTxtDropbox;
	
	private MActionBar vActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_m_link_accounts);
		
		initViews();
		initData();
		initEvents();
	}
	
	
	private void initViews() {
		vRelaLyFacebook=(RelativeLayout)findViewById(R.id.relaLy_facebook);
		vImgFacebook=(ImageView)findViewById(R.id.img_link_facebook);
		vTxtFacebook=(TextView)findViewById(R.id.txt_link_facebook);
		
		vRelaLyInstagram=(RelativeLayout)findViewById(R.id.relaLy_instagram);
		vImgInstagram=(ImageView)findViewById(R.id.img_link_instagram);
		vTxtInstagram=(TextView)findViewById(R.id.txt_link_instagram);
		
		vRelaLyFlickr=(RelativeLayout)findViewById(R.id.relaLy_flickr);
		vImgFlickr=(ImageView)findViewById(R.id.img_link_flickr);
		vTxtFlickr=(TextView)findViewById(R.id.txt_link_flickr);
		
		vRelaLydropboxbtn=(RelativeLayout)findViewById(R.id.relaLy_dropbox);
		vImgDropbox=(ImageView)findViewById(R.id.img_link_dropbox);
		vTxtDropbox=(TextView)findViewById(R.id.txt_link_dropbox);
		
		vActionBar = (MActionBar) findViewById(R.id.actionbar);
	}
	
	private void initData() {
	}
	
	private void initEvents(){
		vRelaLyFacebook.setOnTouchListener(ontouchlistener);
		vRelaLyInstagram.setOnTouchListener(ontouchlistener);
		vRelaLyFlickr.setOnTouchListener(ontouchlistener);
		vRelaLydropboxbtn.setOnTouchListener(ontouchlistener);
		
		
		vActionBar.setOnLeftButtonClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}
	
	private OnTouchListener ontouchlistener=new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (id == vRelaLyFacebook.getId()) {
					vImgFacebook
							.setImageResource(R.drawable.icon_facebook_grey_sel);
					vTxtFacebook.setTextColor(getResources().getColor(
							R.color.kodak_red));
				} else if (id == vRelaLyInstagram.getId()) {
					vImgInstagram
							.setImageResource(R.drawable.icon_instagram_grey_sel);
					vTxtInstagram.setTextColor(getResources().getColor(
							R.color.kodak_red));
				} else if (id == vRelaLyFlickr.getId()) {
					vImgFlickr
							.setImageResource(R.drawable.icon_flickr_grey_sel);
					vTxtFlickr.setTextColor(getResources().getColor(
							R.color.kodak_red));
				} else if (id == vRelaLydropboxbtn.getId()) {
					vImgDropbox
							.setImageResource(R.drawable.icon_dropbox_grey_sel);
					vTxtDropbox.setTextColor(getResources().getColor(
							R.color.kodak_red));
				}
				break;
            case MotionEvent.ACTION_UP:
            	if (id == vRelaLyFacebook.getId()) {
					vImgFacebook
							.setImageResource(R.drawable.icon_facebook_grey);
					vTxtFacebook.setTextColor(getResources().getColor(
							R.color.grey));
				} else if (id == vRelaLyInstagram.getId()) {
					vImgInstagram
							.setImageResource(R.drawable.icon_instagram_grey);
					vTxtInstagram.setTextColor(getResources().getColor(
							R.color.grey));
				} else if (id == vRelaLyFlickr.getId()) {
					vImgFlickr
							.setImageResource(R.drawable.icon_flickr_grey);
					vTxtFlickr.setTextColor(getResources().getColor(
							R.color.grey));
				} else if (id == vRelaLydropboxbtn.getId()) {
					vImgDropbox
							.setImageResource(R.drawable.icon_dropbox_grey);
					vTxtDropbox.setTextColor(getResources().getColor(
							R.color.grey));
				}
				break;
			default:
				break;
			}
			return true;
		}
		
	};
	

}
