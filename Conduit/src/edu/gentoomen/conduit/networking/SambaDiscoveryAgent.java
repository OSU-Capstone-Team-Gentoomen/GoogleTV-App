package edu.gentoomen.conduit.networking;

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

import jcifs.smb.SmbFile;
import android.util.Log;

import edu.gentoomen.conduit.NetworkContentProvider;
import edu.gentoomen.conduit.networking.Pingable;


/*
 * This class will focus on samba discovery
 * for the network backend
 */
public class SambaDiscoveryAgent {
	
	/*Table containing all online hosts*/
	private HashSet<Pingable> hosts;
	
	/*Set our ExecutorService to use only one thread for a background scan*/
	private ExecutorService backgroundScanThread = Executors.newSingleThreadExecutor();	
	
	protected SambaDiscoveryAgent(HashSet<Pingable> hosts) {
		
		this.hosts = hosts;
		this.initalSambaScan();		
		this.backgroundSambaScan();
		
	}
	
	/*First scan across all the available hosts*/
	private void initalSambaScan(){
		
		Log.d("Will-Debug","Starting initial scan");
		ExecutorService executor = Executors.newFixedThreadPool(4);
		
		for (Pingable p : hosts) {
		
			Future<Pingable> result = executor.submit(new InitialScan(p.addr.getHostAddress()));
			Log.d("Will-Debug", "Intial Scanning - Pinging " + p.addr.getHostAddress());
			try {
				/*
				 * Attempt to get the results of our connection after
				 * 50ms and update the ContentProvider
				 */
				if (result.get(50, TimeUnit.MILLISECONDS) != null) {
					p.hasSambaShare = true;
					DiscoveryAgent.changeFlag(NetworkContentProvider.COL_SAMBA, 
											  1, p.addr.getHostAddress());
				}
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			} catch (TimeoutException e) {
				Log.d("Will-debug", "Intial scan for Samba on IP "
					  + p.addr.getHostAddress() + " has timed out");
			}
			
		}
		
	}
	
	public LinkedList<String> getFileListing(String path) throws Exception {
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		SmbFile share = new SmbFile(path);			
		SmbFile[] fileArray = share.listFiles();		

		for (SmbFile file : fileArray) {
			fileList.add(file.getName());
			System.out.println(file.getName());
		}

		return fileList;
		
	}
	
	protected void shutdownBackgroundScan(){
		backgroundScanThread.shutdown();
	}
		
	private void backgroundSambaScan() {			
		backgroundScanThread.submit(new BackgroundScan());		
	}
	
	/*
	 * Will attempt to do a samba connection to a single IP
	 * with a 50ms timeout
	 */
	private class InitialScan implements Callable<Pingable> {
		
		private String ipAddress;		
		
		public InitialScan(String ipAddress){
			this.ipAddress = ipAddress;	
		}

		public Pingable call() {
			
			Log.d("Will-Debug", "pinging for samba at " + ipAddress);
			
			/*Attempt the samba connection*/
			try {
					new SmbFile("smb://" + ipAddress).connect();
					Log.d("Will-Debug", "Found samba share at " + ipAddress);					
					return new Pingable(InetAddress.getByName(ipAddress));				
			} catch (Exception e) { }
			
			Log.d("Will-Debug", "Found no samba share at " + ipAddress);
			
			return null;
			
		}
	}
	
	/*Attempt to connect to a samba share with max timeout*/
	private class BackgroundScan implements Runnable {
		
		public BackgroundScan(){
		}
		
		public void run() {
			Log.d("Will-Debug", "Starting samba ping on background scan");
			for (Pingable p : hosts) {
				try { 
					new SmbFile("smb://" + p.addr.getHostAddress());
					DiscoveryAgent.changeFlag(NetworkContentProvider.COL_SAMBA, 
											  1, p.addr.getHostAddress());
				} catch (Exception e) {
					Log.d("Will-debug", "Background scan found no samba at " + 
						  p.addr.getHostAddress());
				}
			}
		}
	}
}
