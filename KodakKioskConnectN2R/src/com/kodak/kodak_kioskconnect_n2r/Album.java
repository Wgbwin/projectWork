package com.kodak.kodak_kioskconnect_n2r;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Album extends LinearLayout
{
	TextView albumName = null;
	TextView sizeTV = null;
	TextView targetTex = null;
	String albumNameStr = "";
	String uri = "";
	String size = "0";
	String selected = "";
	public Album(Context context)
	{
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.album, this);
		albumName = (TextView) findViewById(R.id.textView1);
		sizeTV = (TextView)findViewById(R.id.textView2);
		targetTex = (TextView)findViewById(R.id.textView3);
		albumName.setTypeface(PrintHelper.tf);
		sizeTV.setTypeface(PrintHelper.tf);
		
	}
	
	int albumID;
	public int getAlbumID() {
		return albumID;
	}
	public void setAlbumID(int albumID) {
		this.albumID = albumID;
	}
}
