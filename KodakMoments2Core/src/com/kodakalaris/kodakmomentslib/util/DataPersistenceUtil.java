package com.kodakalaris.kodakmomentslib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;

/**
 * Util for data persistence
 * @author Robin QIAN
 *
 */
public class DataPersistenceUtil {
	private static final String TAG = "PersistenceUtil";
	
	public static boolean saveObject(Context context, Serializable ser, String filename){
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "save object("+ ser.toString()+") fail", e);
			return false;
		} finally {
			if(oos != null){
				try{
					oos.close();
				}catch(Exception e){
					Log.e(TAG,e);
				}
			}
			if(fos != null){
				try{
					fos.close();
				}catch(Exception e){
					Log.e(TAG,e);
				}
			}
		}
	}
	
	public static Serializable readObject(Context context, String filename){
		if(!context.getFileStreamPath(filename).exists()){
			return null;
		}
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = context.openFileInput(filename);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (Exception e) {
			Log.e(TAG, "read object fail" ,e);
			//delete cache file if fail
			if(e instanceof InvalidClassException){
				File data = context.getFileStreamPath(filename);
				data.delete();
			}
			return null;
		} finally {
			if(ois != null){
				try {
					ois.close();
				} catch (Exception e2) {
					Log.e(TAG, e2);
				}
			}
			if(fis != null){
				try {
					fis.close();
				} catch (Exception e2) {
					Log.e(TAG, e2);
				}
			}
		}
	}
}
