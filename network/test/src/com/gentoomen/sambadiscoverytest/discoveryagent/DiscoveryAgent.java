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
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import com.gentoomen.entities.Pingable;

/*
 * This class is used to discover file servers that can then be browsed
 */
public class DiscoveryAgent extends AsyncTask<String, Void, String> {	
	
	//private UniAddress domain;
	//private NtlmPasswordAuthentication auth;
	protected DhcpInfo info;
	
	private WifiManager wifiInfo;
	protected int lowest;
	protected int highest;
	private LinkedList<Pingable> hosts;
	
	//Set the thread policy to run off of the main thread. This will help keep the UI smooth
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();	
	
	public DiscoveryAgent(Context context){
		StrictMode.setThreadPolicy(policy);
		wifiInfo = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		info = wifiInfo.getDhcpInfo();
		getIpRange();
		findAvailHosts();
	}
	
	/* 
	 * Function will perform network scan based off of the client's
	 * default gateway and scan for samba shares
	 */
	public String doInBackground(String... params){		
		
		switch(Integer.parseInt(params[0])){
		case 0:
			return this.diagFunctions(params[1]);			
		case 1:
			return this.scanningFunctions(params[1]);		
		case 2:
			return "Browsing not implemented";			
		}			
		
		return "Error";
	}
	
	private String scanningFunctions(String args) {
		switch(Integer.parseInt(args)) {
		case 0:
			return hostIter();	
		case 1:
			break; //return this.findNumberOfShares();
		default:
			break;
		}
		
		return "Error";
	}
	
	private String diagFunctions(String args){
		
		switch(Integer.parseInt(args)){
		
		case 0:
			return this.determineDefaultGateway();
		case 1:
			return this.determineIPAddress();			
		case 2:
			return this.determineSubnetMask();
		case 3:
			return this.pingRouter();
		case 4:
			return intToIp(highest) + " - " + intToIp(lowest);
		default:
			break;
		}
		return "Not Implemented";	
				
	}
	
	/*
	 * Probably need to determine
	 * domain and auth credentials before
	 * calling listfiles from server
	 */
	/*private String findNumberOfShares(){
		
		String fileListing = "Files/Folders found: ";
		
		try {
			LinkedList<String> list = new SambaDiscoveryAgent("192.168.1.11").getFileListing("smb://192.168.1.11");
			for(String str : list){										
				fileListing = fileListing + ", " + str;				
			}
			
			return String.valueOf(fileListing);
		} catch (Exception e) { 
			e.printStackTrace();
		}
		
		return "Error";
	}*/
	
	//no such thing as an unsigned byte in Java...
	private void getIpRange() {
		int gateway = info.gateway;
		int netmask = info.netmask;
		
		//get the lowest possible address
		lowest = gateway & netmask;
		
		highest = lowest | (~netmask);
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
	
	//ping a single address
	private boolean ping(InetAddress addr) {
		try {
			if(addr.isReachable(10)) {
				return true;
			}
		} catch(IOException e) {}
		
		return false;
	}
	
	private void findAvailHosts() {
		hosts = new LinkedList<Pingable>();
		InetAddress curAddr;
		
		for(int i = lowest; i < highest; i++) {
			try {
				curAddr = InetAddress.getByName(intToIp(i));
			} catch(UnknownHostException e) { continue; }
			
			if(ping(curAddr)) {
				hosts.add(new Pingable(curAddr));
			}
		}
	}
	
	private String hostIter() {
		String ans = "";
		
		for(Pingable p : hosts) {
			ans += p.addr.getHostAddress() + ", ";
		}
		
		return ans.substring(0, ans.length() - 2);
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
