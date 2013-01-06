package edu.gentoomen.conduit.networking;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;
import edu.gentoomen.conduit.NetworkContentProvider;
import edu.gentoomen.utilities.Services;


/*
 * This class will focus on samba discovery
 * for the network backend
 */
public class SambaDiscoveryAgent {
	
	private static final String TAG = "Samba Disc";	
	
	private static NtlmPasswordAuthentication auth = 
								NtlmPasswordAuthentication.ANONYMOUS;
		
	/*Table containing all online hosts*/
	private HashSet<Pingable>   hosts;
	
	/*Set our ExecutorService to use only one thread for a background scan*/
	private ExecutorService backgroundScanThread = 
						Executors.newSingleThreadExecutor();
	
	protected SambaDiscoveryAgent(HashSet<Pingable> hosts) {
		
		this.hosts = hosts;
		this.initalSambaScan();
		//this.backgroundSambaScan();
		
	}

	private void initalSambaScan(){
		
		Log.d("Will-Debug","Starting initial scan");		
		Log.d(TAG, "set size " + hosts.size());		
		for (Pingable p : hosts) {
			Log.d(TAG, "checking " + p.addr.getHostAddress());
			try {
				if(NbtAddress.getByName(p.addr.getHostAddress()).isActive()) {
					Log.d(TAG, "Found Samba Share at " + p.addr.getHostAddress());					
					DiscoveryAgent.addNewHost(p, Services.Samba);					
				} else {
					Log.d(TAG, "No share at " + p.addr.getHostAddress());
				}
			} catch (UnknownHostException e) {
				Log.d(TAG, "Could not find host: " + e.getMessage());
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
