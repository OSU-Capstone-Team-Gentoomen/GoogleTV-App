package edu.gentoomen.conduit.networking;

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
import edu.gentoomen.utilities.Services;


/*
 * This class will focus on samba discovery
 * for the network backend
 */

public class SambaDiscoveryAgent {
	
	private static final String TAG = "Samba Disc";
	
	private static final String[] SYS_FOLDERS = { "C$", "H$", "IPC$", "ADMIN$" };
	
	@SuppressWarnings("unused")
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
		
	}

	private void initalSambaScan(){
		
		Log.d("Will-Debug","Starting initial scan");		
		Log.d(TAG, "set size " + hosts.size());		
		
		for (Pingable p : hosts) {
			Log.d(TAG, "checking " + p.addr.getHostAddress());
			Future<Boolean> result = backgroundScanThread.submit(new InitialScan(p.addr.getHostAddress()));			
			try {
				//if(NbtAddress.getByName(p.addr.getHostAddress()).isActive()) {
				if (result.get(1000, TimeUnit.MILLISECONDS)) {
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
}
