/*
 * Copyright (C) 2011 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.zxing.client.android.wifi;

import java.util.List;
import java.util.regex.Pattern;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.kodak.kodak_kioskconnect_n2r.Connection;
import com.kodak.kodak_kioskconnect_n2r.PrintHelper;

/**
 * @author Vikram Aggarwal
 * @author Sean Owen
 */
public final class WifiConfigManager
{
	private static final String TAG = WifiConfigManager.class.getSimpleName();
	private static final Pattern HEX_DIGITS = Pattern.compile("[0-9A-Fa-f]+");

	private WifiConfigManager()
	{
	}

	public static void configure(final WifiManager wifiManager, final String ssid, final String password, final String networkTypeString)
	{
		Runnable configureRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				// Start WiFi, otherwise nothing will work
				if (!wifiManager.isWifiEnabled())
				{
					PrintHelper.status = "Enabling wi-fi...";
					Log.d(TAG, "Enabling wi-fi...");
					if (wifiManager.setWifiEnabled(true))
					{
						PrintHelper.status = "Wi-fi enabled";
						Log.d(TAG, "Wi-fi enabled");
					}
					else
					{
						PrintHelper.status = "Wi-fi could not be enabled!";
						Log.d(TAG, "Wi-fi could not be enabled!");
						return;
					}
					// This happens very quickly, but need to wait for it to
					// enable. A little busy wait?
					int count = 0;
					while (!wifiManager.isWifiEnabled())
					{
						if (count >= 10)
						{
							PrintHelper.status = "Took too long to enable wi-fi, quitting";
							Log.d(TAG, "Took too long to enable wi-fi, quitting");
							return;
						}
						PrintHelper.status = "Still waiting for wi-fi to enable...";
						Log.d(TAG, "Still waiting for wi-fi to enable...");
						try
						{
							Thread.sleep(1000L);
						}
						catch (InterruptedException ie)
						{
							// continue
						}
						count++;
					}
				}
				NetworkType networkType = NetworkType.forIntentValue(networkTypeString);
				if (networkType == NetworkType.NO_PASSWORD)
				{
					changeNetworkUnEncrypted(wifiManager, ssid);
				}
				else
				{
					if (password == null || password.length() == 0)
					{
						throw new IllegalArgumentException();
					}
					if (networkType == NetworkType.WEP)
					{
						changeNetworkWEP(wifiManager, ssid, password);
					}
					else if (networkType == NetworkType.WPA)
					{
						changeNetworkWPA(wifiManager, ssid, password);
					}
				}
			}
		};
		new Thread(configureRunnable).start();
	}

	/**
	 * Update the network: either create a new network or modify an existing
	 * network
	 * 
	 * @param config
	 *            the new network configuration
	 * @return network ID of the connected network.
	 */
	private static void updateNetwork(WifiManager wifiManager, WifiConfiguration config)
	{
		/*long start;
		long end;
		long diff;
		start = System.currentTimeMillis();
		Log.d(TAG, "Start"+start);
		Integer foundNetworkID = findNetworkInExistingConfig(wifiManager, config.SSID);
		end = System.currentTimeMillis();
		diff = end-start;
		Log.d(TAG, "End"+diff);
		if (foundNetworkID != null)
		{
			Log.d(TAG, "Removing old configuration for network " + config.SSID);
			wifiManager.removeNetwork(foundNetworkID);
			wifiManager.saveConfiguration();
		}*/
		
		//Fix for RSSMOBILEPDC-2070
		if (Build.VERSION.SDK_INT >= 21) {
			config.SSID = config.SSID.replace("\"", "");
		}
		
		int networkId = wifiManager.addNetwork(config);
		if (networkId >= 0)
		{
			//add to list, so we can remove it when needed
			if(config.SSID.contains("kodak")){
				synchronized (PrintHelper.wifiSetTryConnected){
					PrintHelper.wifiSetTryConnected.add(networkId);
				}
			}
			// Try to disable the current network and start a new one.
			if (wifiManager.enableNetwork(networkId, true))
			{
				Log.d(TAG, "Associating to network " + config.SSID);
				wifiManager.saveConfiguration();
				
				//Fix for RSSMOBILEPDC-2070
				final int MAX_RETRY_TIMES = 10;
				int retryCount = 0;
				try {
					while (!isConnectKiosk(wifiManager) && retryCount < MAX_RETRY_TIMES) {
						wifiManager.enableNetwork(networkId, true);
						retryCount ++;
						Thread.sleep(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (isConnectKiosk(wifiManager)) {
					PrintHelper.status = "Connected";
				} else {
					Log.e(TAG, "Failed to enable network " + config.SSID);
				}
				
			}
			else
			{
				Log.e(TAG, "Failed to enable network " + config.SSID);
			}
		}
		else
		{
			Log.e(TAG, "Unable to add network " + config.SSID);
		}
	}
	
	private static boolean isConnectKiosk(WifiManager wifiManager) {
		return wifiManager.getConnectionInfo() != null && Connection.isKioskWifi(wifiManager.getConnectionInfo().getSSID());
	}

	private static WifiConfiguration changeNetworkCommon(String ssid)
	{
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		// Android API insists that an ascii SSID must be quoted to be correctly
		// handled.
		config.SSID = quoteNonHex(ssid);
		return config;
	}

	// Adding a WEP network
	private static void changeNetworkWEP(WifiManager wifiManager, String ssid, String password)
	{
		WifiConfiguration config = changeNetworkCommon(ssid);
		config.wepKeys[0] = quoteNonHex(password, 10, 26, 58);
		config.wepTxKeyIndex = 0;
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		updateNetwork(wifiManager, config);
	}

	// Adding a WPA or WPA2 network
	private static void changeNetworkWPA(WifiManager wifiManager, String ssid, String password)
	{
		WifiConfiguration config = changeNetworkCommon(ssid);
		// Hex passwords that are 64 bits long are not to be quoted.
		config.preSharedKey = quoteNonHex(password, 64);
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // For WPA
		config.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		updateNetwork(wifiManager, config);
	}

	// Adding an open, unsecured network
	private static void changeNetworkUnEncrypted(WifiManager wifiManager, String ssid)
	{
		WifiConfiguration config = changeNetworkCommon(ssid);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		updateNetwork(wifiManager, config);
	}

	private static Integer findNetworkInExistingConfig(WifiManager wifiManager, String ssid)
	{
		List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs)
		{
			if (existingConfig.SSID.equals(ssid))
			{
				return existingConfig.networkId;
			}
		}
		return null;
	}

	private static String quoteNonHex(String value, int... allowedLengths)
	{
		return isHexOfLength(value, allowedLengths) ? value : convertToQuotedString(value);
	}

	/**
	 * Encloses the incoming string inside double quotes, if it isn't already
	 * quoted.
	 * 
	 * @param string
	 *            the input string
	 * @return a quoted string, of the form "input". If the input string is
	 *         null, it returns null as well.
	 */
	private static String convertToQuotedString(String string)
	{
		if (string == null || string.length() == 0)
		{
			return null;
		}
		// If already quoted, return as-is
		if (string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"')
		{
			return string;
		}
		return '\"' + string + '\"';
	}

	/**
	 * @param value
	 *            input to check
	 * @param allowedLengths
	 *            allowed lengths, if any
	 * @return true if value is a non-null, non-empty string of hex digits, and
	 *         if allowed lengths are given, has an allowed length
	 */
	private static boolean isHexOfLength(CharSequence value, int... allowedLengths)
	{
		if (value == null || !HEX_DIGITS.matcher(value).matches())
		{
			return false;
		}
		if (allowedLengths.length == 0)
		{
			return true;
		}
		for (int length : allowedLengths)
		{
			if (value.length() == length)
			{
				return true;
			}
		}
		return false;
	}
}