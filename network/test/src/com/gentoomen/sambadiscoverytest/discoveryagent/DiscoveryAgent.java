package com.gentoomen.sambadiscoverytest.discoveryagent;

import android.content.Context;
import android.net.*;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jcifs.smb.SmbFile;
import com.gentoomen.entities.Pingable;

/*
 * This class is used to discover file servers that can then be browsed
 */
public class DiscoveryAgent extends AsyncTask<String, Void, String> {
	
	protected DhcpInfo info;
	private WifiManager wifiInfo;
	protected int lowest;
	protected int highest;
	private SambaDiscoveryAgent sAgent;
	private HashSet<Pingable> hosts = new HashSet<Pingable>();	
	
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
			return String.valueOf(hosts.size());			
		case 1:
			sAgent = new SambaDiscoveryAgent(hosts, getOctets(lowest), getOctets(highest), getOctets(info.gateway));
			return String.valueOf(sAgent.findNumOfSambaShares());			
		case 2:
			
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
			 break;
		case 4:
			return intToIp(lowest) + " - " + intToIp(highest);
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
	private String findNumberOfShares(){
		
		String fileListing = "Files/Folders found: ";		
		return "Error";
	}
	
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
		return false;
	}
	
	private void findAvailHosts() {
		ExecutorService exec = Executors.newFixedThreadPool(4);
		int start = getOctets(lowest)[3];
		int end = getOctets(highest)[3];
		int[] gateway = getOctets(info.gateway);
		String ipPrefix = gateway[0] + "." + gateway[1] + "." + gateway[2] + ".";
		for(int i = start + 1; i < end - 1; i++) {
			exec.submit(new ThreadedInitialScan(ipPrefix + i));
		}		
		exec.shutdown(); /*ExecutorService will no longer accept new jobs*/
		while(!exec.isTerminated());
	}
	
	private String hostIter() {
		
		if(hosts.size() == 0 || hosts == null){
			return "Error scanning"; 												
		}
		
		String ans = "";
		for(Pingable p : hosts) {
			ans += p.addr.getHostAddress() + ", ";
		}
		
		return ans.substring(0, ans.length() - 2);
	}
		
	private String intToIp(int num){
		String returnStr = "";
		int[] octets = this.getOctets(num);
		for(int i = 0; i < octets.length; i++){
			returnStr += octets[i] + ".";
		}
		
		return returnStr.substring(0, returnStr.length() - 1);
	}
	
	private int[] getOctets(int num){
		int octets[] = new int[4];
		
		octets[0] = num & 0xFF;
		octets[1] = (num >> 8) & 0xFF;
		octets[2] = (num >> 16) & 0xFF;
		octets[3] = (num >> 24) & 0xFF;
		
		return octets;
	}

	private class ThreadedInitialScan implements Runnable {
		private String ipAddr;

		public ThreadedInitialScan(String ip) {
			this.ipAddr = ip;
		}
		
		public void run() {					
			try {
				InetAddress _addr = InetAddress.getByName(ipAddr);
				_addr.isReachable(25);
				if(InetAddress.getByName(ipAddr).isReachable(50)) {
					Log.d("Creole-Debug", ipAddr + " is reachable");
					hosts.add(new Pingable(InetAddress.getByName(ipAddr)));
				}
				else {
					Log.d("Creole-Debug", ipAddr + " is not reachable");
				}
			} catch(UnknownHostException e) {
				Log.d("Creole-Debug", "UnknownHost exception thrown");
			}
			catch(IOException e) {
				Log.d("Creole-Debug", "IOException thrown");
			}
		}
	}
}
