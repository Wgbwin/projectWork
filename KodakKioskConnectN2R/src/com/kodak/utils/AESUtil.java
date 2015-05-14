package com.kodak.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

	public static String Encrypt(String sSrc, String sKey, String iniVector)
			throws Exception {
		if (sKey == null) {
			System.out.print("Key is null");
			return null;
		}
		if (sKey.length() != 16) {
			System.out.print("length of key is not 16 ");
			return null;
		}
		byte[] raw = sKey.getBytes();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// (algorithm/mode/padding)
		IvParameterSpec iv = new IvParameterSpec(iniVector.getBytes());//
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
		byte[] encrypted = cipher.doFinal(sSrc.getBytes());

		return Base64Util.encode(encrypted);//
	}

	public static String Decrypt(String sSrc, String sKey, String iniVector) throws Exception {
		try {
			if (sKey == null) {
				System.out.print("Key is null");
				return null;
			}
			if (sKey.length() != 16) {
				System.out.print("length of Key is not 16");
				return null;
			}
			byte[] raw = sKey.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec iv = new IvParameterSpec(
					iniVector.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] encrypted1 = Base64Util.decode(sSrc);// 
			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original);
				return originalString;
			} catch (Exception e) {
				System.out.println(e.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}
}
