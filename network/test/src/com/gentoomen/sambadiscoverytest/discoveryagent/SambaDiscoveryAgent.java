package com.gentoomen.sambadiscoverytest.discoveryagent;

import jcifs.smb.*;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.gentoomen.entities.Pingable;
import android.util.Log;


/*
 * This class will focus on samba discovery
 * for the network backend
 */
public class SambaDiscoveryAgent {
		
	private HashSet<Pingable> hosts;	
	private ExecutorService backgroundScanThread = Executors.newSingleThreadExecutor();
	
	protected SambaDiscoveryAgent(HashSet<Pingable> hosts, int[] lowest, int[] highest, int[] defaultGateway) {
		
		this.hosts = hosts;
		this.initalSambaScan();
		this.backgroundSambaScan(lowest, highest, defaultGateway);		
	}
	
	public int findNumOfSambaShares() {		
		int counter = 0;
		for(Pingable p : hosts){
			if(p.hasSambaShare){
				counter++;
			}
		}
		
		return counter;
	}
	
	public LinkedList<String> getFileListing(String path) throws Exception{
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		SmbFile share = new SmbFile(path);			
		SmbFile[] fileArray = share.listFiles();		

		for(SmbFile file : fileArray){
			fileList.add(file.getName());
			System.out.println(file.getName());
		}

		return fileList;
		
	}
	
	public void shutdownBackgroundScan(){
		backgroundScanThread.shutdown();
	}
	
	private void initalSambaScan(){
		Log.d("Will-Debug","Starting initial scan");
		ExecutorService executor = Executors.newFixedThreadPool(4);
		for(Pingable p : hosts){
			Future<Pingable> result = executor.submit(new InitialScan(p.addr.getHostAddress()));
			Log.d("Will-Debug", "Intial Scanning - Pinging " + p.addr.getHostAddress());
			try {
				if(result.get(50, TimeUnit.MILLISECONDS) != null){
					p.hasSambaShare = true;
				}
			} catch (InterruptedException e) {				
			} catch (ExecutionException e) {				
			} catch (TimeoutException e){
				Log.d("Will-debug", "Intial scan for Samba on IP " + p.addr.getHostAddress() +
						" has timed out");
			}
			
		}
	}
	
	private void backgroundSambaScan(int[] lowest, int[] highest,
			int[] defaultGateway) {
		
		String ipPrefix = defaultGateway[0] + "." + defaultGateway[1] + "." + defaultGateway[2] + ".";
		backgroundScanThread.submit(new BackgroundScan(lowest[3], highest[3], ipPrefix));		
	}
	
	private class InitialScan implements Callable<Pingable> {
		private String ipAddress;		
		public InitialScan(String ipAddress){
			this.ipAddress = ipAddress;	
		}

		public Pingable call() throws Exception {
			Log.d("Will-Debug", "pinging for samba at " + ipAddress);
			try {
					new SmbFile("smb://" + ipAddress).connect();
					Log.d("Will-Debug", "Found samba share at " + ipAddress);					
					return new Pingable(InetAddress.getByName(ipAddress));					
				
			} catch (Exception e) { }			  
			Log.d("Will-Debug", "Found no samba share at " + ipAddress);
			return null;
		}
	}
	
	private class BackgroundScan implements Callable<Pingable> {
		
		private int lowest;
		private int highest;		
		private String ipPrefix;
		
		public BackgroundScan(int lowest, int highest, String ipPrefix){
			this.highest = highest;
			this.lowest = lowest;
			this.ipPrefix = ipPrefix;
		}
		
		public Pingable call() throws Exception {
			Log.d("Will-Debug", "Starting samba ping on background scan");
			int current = lowest;			
			while(true){		
				try{
					new SmbFile("smb://" + ipPrefix + current).connect();					
					Pingable mPing = new Pingable(InetAddress.getByName(ipPrefix + current));					
					if(hosts.contains(mPing)){
						for(Pingable p : hosts){
							if(p.ip == mPing.ip){
								p.hasSambaShare = true;
								break;
							}
						}
					}else{
						mPing.hasSambaShare = true;					
						hosts.add(mPing);
					}
					Log.d("Will-Debug", "BackgroundScan found samba share at " + ipPrefix + current);
				}catch (Exception e){
					Log.d("Will-Debug", "BackgroundScan found no samba share at " + ipPrefix + current);
				}
				if(current == highest)
					current = lowest;					
				current++;
			}
		}


	}
	
}
