package edu.gentoomen.conduit.networking;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jcifs.netbios.NbtAddress;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;
import edu.gentoomen.utilities.Services;


/*
 * This class will focus on samba discovery
 * for the network backend
 */

public class SambaDiscoveryAgent {

	private static final String TAG = "Samba Disc";
	private static final int	DEFAULT_TIMEOUT_MSEC = 1500;
	private Map<String, String> nbtHosts;
	
	/*Set our ExecutorService to use only one thread for a background scan*/
	private ExecutorService executorService = 
			Executors.newFixedThreadPool(4);

	protected SambaDiscoveryAgent() {
		
		nbtHosts = new HashMap<String, String>();
		this.findNbtHosts();
		this.initalSambaScan();

	}

	private void initalSambaScan() {

		Map<String, Pingable> hosts = DiscoveryAgent.getHostSet();

		Log.d("Will-Debug","Starting initial scan");		
		Log.d(TAG, "set size " + hosts.size());

		for (Map.Entry<String, Pingable> entry : hosts.entrySet()) {
			Pingable p = entry.getValue();
			
			if (nbtHosts.containsKey(p.mac)) {
				Log.d(TAG, "Found NBT name for share for mac: " + p.mac + " name: " + nbtHosts.get(p.mac));
				p.nbtName = nbtHosts.get(p.mac);
				DiscoveryAgent.addNewHost(p, Services.Samba);
				continue;
			}
			
			Log.d(TAG, "checking " + p.addr.getHostAddress());								
			Future<Boolean> result = executorService.submit(new CheckSamba(p.addr.getHostAddress()));
			
			try {
				//if(NbtAddress.getByName(p.addr.getHostAddress()).isActive()) {
				if (result.get(DEFAULT_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) {
					Log.d(TAG, "Found Samba Share at " + p.addr.getHostAddress() + " with mac " + p.mac);					
					DiscoveryAgent.addNewHost(p, Services.Samba);					
				} else {
					Log.d(TAG, "No share at " + p.addr.getHostAddress());
				}			
			} catch (InterruptedException e) {			
				e.printStackTrace();
			} catch (ExecutionException e) {				
				e.printStackTrace();
			} catch (TimeoutException e) {
				Log.d(TAG, "Samba check timed out!");
				//e.printStackTrace();
			}
		}

	}

	private class CheckSamba implements Callable<Boolean> {

		private String ipAddress;

		public CheckSamba(String address) {
			ipAddress = address;
		}

		@Override
		public Boolean call() throws Exception {

			if (NbtAddress.getByName(ipAddress).isActive()) 
				return true;			

			return false;

		}

	}
	
	private class GetNbtHostMac implements Callable<byte[]> {
		
		private SmbFile nbtHost;
		
		public GetNbtHostMac(SmbFile host) {
			nbtHost = host;
		}
		
		@Override
		public byte[] call() throws Exception {
			return NbtAddress.getByName(nbtHost.getName().substring(0, nbtHost.getName().length() - 1)).getMacAddress();			
		}
		
	}

	private void findNbtHosts() {

		if (nbtHosts == null)
			return;
		
		try {
			for (SmbFile s : new SmbFile("smb://Workgroup").listFiles()) {
				Log.d(TAG, "found " + s.getName());
				Future<byte[]> result = executorService.submit(new GetNbtHostMac(s));
								
				try {
					byte[] mac = result.get(DEFAULT_TIMEOUT_MSEC, TimeUnit.MILLISECONDS);
					nbtHosts.put(bytesToHexString(mac), s.getName().substring(0, s.getName().length() - 1));
					Log.d(TAG, "Mac found in workgroup: " + bytesToHexString(mac));
				} catch (InterruptedException e) {					
					e.printStackTrace();
					continue;
				} catch (ExecutionException e) {					
					e.printStackTrace();
				} catch (TimeoutException e) {					
					e.printStackTrace();
				}				
								
			}
		} catch (SmbException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}
	private static String bytesToHexString(byte[] bytes) {

		StringBuilder retVal = new StringBuilder(); 
		StringBuilder tmpString;

		for (int i = 0; i < bytes.length; i++) {

			tmpString = new StringBuilder();
			tmpString.append(Integer.toHexString(0xFF & bytes[i]));

			if (tmpString.length() == 1)
				tmpString.insert(0, '0');

			retVal.append(tmpString);

			if (i != bytes.length - 1)
				retVal.append(":");
		}

		return retVal.toString();
	}
}
