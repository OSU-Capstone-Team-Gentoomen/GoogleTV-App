package com.gentoomen.sambadiscoverytest.discoveryagent;

import android.content.Context;
import android.net.*;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
	
	private class ScanAddress implements Callable<Boolean> {
		private InetAddress addr;
		
		public ScanAddress(InetAddress addr) {
			this.addr = addr;
		}

		public Boolean call() throws Exception {
			try {
				new SmbFile("smb://" + addr.getHostName()).connect();
			} catch(Exception e) { return false; }
			return true;
		}
	}
	
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
		//findAvailHosts();
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
			findAvailHosts();
			//return findNumberOfShares();
			return hostIter();
		case 1:
			if(hosts == null)
				findAvailHosts();
			SambaDiscoveryAgent sAgent;
			try {
				sAgent = new SambaDiscoveryAgent(hosts);
				String str = "";
				LinkedList<String> fileList = sAgent.getFileListing("smb://192.168.1.37");
				for(String _str : fileList){
					str += _str + ", ";
				}				
				return str.substring(0, str.length() - 2);
			} catch (Exception e) { 
				e.printStackTrace();
				return "Error";
			}		
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
			 try {
				if(this.ping(InetAddress.getByName(intToIp(info.gateway))))
					 return "Ping successful";
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 return "Could not ping";
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
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Boolean> future = exec.submit(new ScanAddress(addr));
		
		try {
			future.get(100, TimeUnit.MILLISECONDS);
			exec.shutdownNow();
			return true;
		}
		catch(TimeoutException e) {} 
		catch (InterruptedException e) {}
		catch (ExecutionException e) {}
		
		exec.shutdownNow();
		return false;
	}
	
	private void findAvailHosts() {
		
		hosts = new LinkedList<Pingable>();
		InetAddress curAddr;
		int[] lowestOctets = getOctets(lowest);
		int[] highestOctets = getOctets(highest);
		int[] defaultOctets = getOctets(info.gateway);
		
		String ipPrefix = defaultOctets[0] + "." + defaultOctets[1] + "." + defaultOctets[2] + ".";
				
		for(int i = lowestOctets[3] + 1; i < highestOctets[3] - 1; i++) {
			try {
				curAddr = InetAddress.getByName(ipPrefix + i);
			} catch(UnknownHostException e) { continue; }
			
			if(ping(curAddr)) {
				hosts.add(new Pingable(curAddr));
			}
		}
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
	
}
