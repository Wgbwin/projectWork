package com.kodak.utils;

import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtil {

	public byte[] generateKey(String strKey, int b) {
		byte[] key = new byte[b];

		int index = 0;
		char cNow;
		int tempVal;
		for (int i = 0; i < b; i++) {
			cNow = strKey.charAt(index++);
			tempVal = (cNow >= '0' && cNow <= '9') ? (cNow - '0')
					: (cNow - 'A' + 10);
			cNow = strKey.charAt(index++);
			tempVal = (tempVal << 4)
					+ ((cNow >= '0' && cNow <= '9') ? (cNow - '0')
							: (cNow - 'A' + 10));

			key[i] = (byte) tempVal;
		}

		return key;
	}
	
	public String encodeResult(byte[] result) {
		int len = result.length;

		StringBuffer sb = new StringBuffer(64);

		int bHigh, bLow;
		char tmpVal;
		for (int i = 0; i < len; i++) {
			bHigh = (result[i] >> 4) & 0x0F;
			bLow = result[i] & 0x0F;
			if (i > 0) {
				sb.append('-');
			}
			if (bHigh > 9) {
				tmpVal = (char) ('A' + bHigh - 10);
				sb.append(tmpVal);
			} else {
				tmpVal = (char) ('0' + bHigh);
				sb.append(tmpVal);
			}
			if (bLow > 9) {
				tmpVal = (char) ('A' + bLow - 10);
				sb.append(tmpVal);
			} else {
				tmpVal = (char) ('0' + bLow);
				sb.append(tmpVal);
			}
		}
		return sb.toString();
	}
	
	public static byte[] encrypt(byte[] text, byte[] key, byte[] iv) throws Exception {
		byte[] bytes = null;
		
		final int BlockSizeAES128 = 16;
		if (text.length % BlockSizeAES128 != 0) {
			int oldLen = text.length;
			int newLen = (oldLen / BlockSizeAES128) * BlockSizeAES128
					+ BlockSizeAES128;
			byte[] expandedText = new byte[newLen];
			System.arraycopy(text, 0, expandedText, 0, oldLen);
			for (int i = oldLen; i < newLen; i++) {
				expandedText[i] = 0x20;
			}

			text = expandedText;
		}
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec spec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, spec, ivParamSpec);
		bytes = cipher.doFinal(text);
		
		return bytes;
	}
	
	/**
	 *Removes the last one element at the specified position in this list. 
	 * @param list
	 * @param str
	 * @return ArrayList
	 */
	public static ArrayList<String> removeLast (ArrayList<String> list,String str){
		int j=0;
		boolean needDelete = false;
		for(int i =0;i<list.size();i++){
			if (list.get(i).equals(str)){
				if (j<i){
					j=i;
					needDelete = true;
				}				
			}
		}
		if (needDelete){
			list.remove(j);
		}
		return list;
		
	}
}
