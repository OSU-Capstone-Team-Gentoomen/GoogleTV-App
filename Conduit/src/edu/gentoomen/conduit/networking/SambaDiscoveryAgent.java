package edu.gentoomen.conduit.networking;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jcifs.netbios.NbtAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import android.util.Log;
import edu.gentoomen.conduit.NetworkContentProvider;
import edu.gentoomen.utilities.Services;


/*
 * This class will focus on samba discovery
 * for the network backend
 */

public class SambaDiscoveryAgent {
	
	private static final String TAG = "Samba Disc";
	
	private static final String[] SYS_FOLDERS = { "C$", "H$", "IPC$", "ADMIN$" };
	
	private static NtlmPasswordAuthentication auth = 
								NtlmPasswordAuthentication.ANONYMOUS;
		
	public class FileListFilter implements SmbFileFilter {
		
		private static final String TAG = "FileListFilter";

		@Override
		public boolean accept(SmbFile file) throws SmbException {
			/*
			 * make sure that we don't show any hidden or system files
			 * if any of the attributes for this file are anded with these values it will 
			 * make the end value greater than 0
			 */
			if ((file.getAttributes() & (SmbFile.ATTR_HIDDEN | SmbFile.ATTR_SYSTEM)) > 0) {
				Log.d(TAG, "skipping file " + file.getName() + " for being either a system file or a hidden file");
				return false;
			}
			
			for (String str : SambaDiscoveryAgent.SYS_FOLDERS) {
				Log.d(TAG, "file name is " + file.getName());
				if (file.getName().equalsIgnoreCase(str))
					return false;
			}
			
			return true;
		}
	}
	
	/*Table containing all online hosts*/
	private HashSet<Pingable>   hosts;
	
	/*Set our ExecutorService to use only one thread for a background scan*/
	private ExecutorService backgroundScanThread = 
						Executors.newFixedThreadPool(4);
	
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
			Future<Boolean> result = backgroundScanThread.submit(new InitialScan(p.addr.getHostAddress()));			
			try {
				//if(NbtAddress.getByName(p.addr.getHostAddress()).isActive()) {
				if (result.get(500, TimeUnit.MILLISECONDS)) {
					Log.d(TAG, "Found Samba Share at " + p.addr.getHostAddress());					
					DiscoveryAgent.addNewHost(p, Services.Samba);					
				} else {
					Log.d(TAG, "No share at " + p.addr.getHostAddress());
				}			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public LinkedList<String> getFileListing(String path) throws Exception {
		
		LinkedList<String> fileList = new LinkedList<String>();
		
		SmbFile share = new SmbFile(path);			
		SmbFile[] fileArray = share.listFiles(new FileListFilter());		

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
	
	private class InitialScan implements Callable<Boolean> {

		private String ipAddress;
		
		public InitialScan(String address) {
			ipAddress = address;
		}
		
		@Override
		public Boolean call() throws Exception {
			if (NbtAddress.getByName(ipAddress).isActive()) 
				return true;			
			
			return false;
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
