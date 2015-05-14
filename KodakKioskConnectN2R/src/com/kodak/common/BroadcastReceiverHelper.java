package com.kodak.common;


import java.io.File;

import com.kodak.kodak_kioskconnect_n2r.PrintHelper;
import com.kodak.utils.ImageUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

public class BroadcastReceiverHelper extends BroadcastReceiver {

    Context ct=null;
    BroadcastReceiverHelper receiver;
    Thread thread;
    
    public BroadcastReceiverHelper(Context c){
        ct=c;
        receiver=this;
        thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				convertSelected();				
			}
		});
    }
    public void registerAction(String action){
        IntentFilter filter=new IntentFilter();
        filter.addAction(action);
        ct.registerReceiver(receiver, filter);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("CONVERT")){
            	if (!thread.isAlive()){
            		thread.run();
            	}
            }
    }
    
    public void convertSelected(){

		for (int i = 1 ;i<=PrintHelper.uploadQueue.size();i++){
			String uri = PrintHelper.uploadQueue.get(i-1);
			boolean isSecondUpload = uri.startsWith("FIRST_UPLOAD_THUMBNAILS");
			uri = (isSecondUpload ? uri.substring("FIRST_UPLOAD_THUMBNAILS".length()) : uri);
			String filename = PrintHelper.selectedFileNames.get(uri);
			//add by song . if the file is .png will compress to .jpg
			if (filename.toUpperCase().endsWith(".PNG")){
				String tempFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + PrintHelper.TEMP_FOLDER;
				File folder = new File(tempFolder);
				if(!folder.exists()){
					folder.mkdirs();
				}
				String newLFilePath = tempFolder + uri.substring(uri.lastIndexOf("/"), uri.length())+ "newL" + ".jpg";
				File newLFile = new File(newLFilePath);
				if(!newLFile.exists()){
					try {
						ImageUtil.pngToJpg(filename,newLFilePath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}		
	
    
    }
}
