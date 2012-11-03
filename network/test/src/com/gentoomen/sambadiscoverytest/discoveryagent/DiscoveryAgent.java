package com.gentoomen.sambadiscoverytest.discoveryagent;

import java.io.IOException;
import jcifs.UniAddress;
import jcifs.smb.*;
import android.content.Context;
import android.net.*;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * This class is used to discover file servers that can then be browsed
 */
public class DiscoveryAgent extends AsyncTask<String, Void, String> {
	
	private UniAddress domain;
	private NtlmPasswordAuthentication auth;
	private DhcpInfo info;
	private WifiManager wifiInfo;
	
	//Set the thread policy to run off of the main thread. This will help keep the UI smooth
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();	
	
	public DiscoveryAgent(Context context){
		StrictMode.setThreadPolicy(policy);
		wifiInfo = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);		
		info = wifiInfo.getDhcpInfo();
	}
	
	/* 
	 * Function will perform network scan based off of the client's
	 * default gateway and scan for samba shares
	 */
	public String doInBackground(String... params){		
		
		switch(Integer.parseInt(params[0])){
		
		case 0:
			return this.determineDefaultGateway();
		case 1:
			return this.determineIPAddress();			
		case 2:
			return this.determineSubnetMask();
		case 3:
			return this.pingRouter();
		default:
			break;
		}
		return "Not Implemented";				
	}
		
	private String determineDefaultGateway(){	
		return intToIp(info.gateway);
	}
	
	private String determineIPAddress(){
		return intToIp(info.ipAddress);
	}
	
	private String determineSubnetMask(){
		return intToIp(info.netmask);
	}
	
	private String pingRouter(){
		try {
			InetAddress[] addresses = InetAddress.getAllByName(intToIp(info.gateway));
			for(InetAddress address : addresses){
				try {
					if(address.isReachable(1000)){
						return "Successful ping";
					}
				} catch (IOException e) {					
				}
			}
			
		} catch (UnknownHostException e) {
		}
		
		return "Could not find host";
	}
	
	private String intToIp(int num){
		return (num & 0xFF) + "." +  ((num >> 8 ) & 0xFF) + "."
				+ ((num >> 16 ) & 0xFF) + "." + ((num >> 24 ) & 0xFF );
	}
			
}
