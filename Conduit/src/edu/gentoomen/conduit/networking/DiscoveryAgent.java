package edu.gentoomen.conduit.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import edu.gentoomen.conduit.NetworkContentProvider;
import edu.gentoomen.conduit.networking.Pingable;
import edu.gentoomen.utilities.Services;

/*
 * This class is used to discover file servers that can then be browsed
 */
public class DiscoveryAgent extends AsyncTask<String, Void, String> {
		
	/*Emulator settings*/
	private final String                EMU_IP_PREFIX = "192.168.1.";
	private final int                   EMU_IP_LOW = 1;
	private final int                   EMU_IP_HIGH = 254;	
	private final boolean               EMU_MODE = false;
	
	/*Static fields for this class*/
	private static boolean              initialScanCompleted = false;
	private static ContentResolver      resolver = null;
	private static WifiManager          wifiInfo = null;
	private static SambaDiscoveryAgent  sAgent = null;
	
	/*Used to keep track of all online hosts*/
	protected static HashSet<Pingable>  hosts = new HashSet<Pingable>();
	protected static DhcpInfo 			info = null;
	
	/*The highest and lowest ip addresses in our subnet*/	
	protected static int                lowest = 0;
	protected static int                highest = 0;
	
	/*Async scanning functions*/
	AsyncTask<String, Void, String>     reachable;
	AsyncTask<Void, Void, String>       arp;
			
	/*
	 * Set the thread policy to run off of the main thread. 
	 * This will help keep the UI smooth
	 */
	static StrictMode.ThreadPolicy policy = 
			new StrictMode.ThreadPolicy.Builder().permitAll().build();
	
	static {
		
		/*Set our thread policy*/
		StrictMode.setThreadPolicy(policy);		
	}
	
	public DiscoveryAgent(Context context) {		
		
		if(resolver == null)
			resolver = context.getContentResolver();

		
		/*Get our wifi service for our device networking information*/
		if(wifiInfo == null) {
			wifiInfo = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			info = wifiInfo.getDhcpInfo();
		}
		
	}
		
	/* 
	 * Function will perform network scan based off of the client's
	 * default gateway and scan for samba shares
	 */
	public String doInBackground(String... params){
		
		/*Do our scan for online hosts if it hasn't already been done*/
		if (!initialScanCompleted) {			
			getIpRange();
			findAvailHosts();
			sAgent = new SambaDiscoveryAgent(hosts);				
		}			
		
		return "";
	}

	
	/*how many scanned IPs are stored in the database*/
	public int getScannedCount() {
		
		Cursor c = resolver.query(NetworkContentProvider.CONTENT_URI, 
								  null, null, null, null);		
		Log.d("Will-Debug", "Found hosts: " + c.getCount());
		return c.getCount();
		
	}
	
	/*how many IPs are online in the database*/
	public int getOnlineCount() {
		
		String[] projection = { NetworkContentProvider.COL_ONLINE };
		
		Cursor c = resolver.query(NetworkContentProvider.CONTENT_URI, projection,
								  NetworkContentProvider.COL_ONLINE + " = 1",
								  null, null);
		
		return c.getCount();
		
	}
		
	/*how many online IPs have a samba share*/
	public int getSambaCount() {
		
		String[] projection = { 
				NetworkContentProvider.COL_ONLINE, 
				NetworkContentProvider.COL_SAMBA 
		};
		
		Cursor c = resolver.query(NetworkContentProvider.CONTENT_URI, 
								  projection, 
								  NetworkContentProvider.COL_ONLINE + " = 1 AND "
								  + NetworkContentProvider.COL_SAMBA + " = 1", 
								  null, null);
		return c.getCount();
		
	}
	
	/*no such thing as an unsigned byte in Java...*/
	private void getIpRange() {
		
		int gateway = info.gateway;
		int netmask = info.netmask;
		
		/*get the highest and lowest possible addresses*/
		if (lowest == 0 || highest == 0) {
			lowest = gateway & netmask;
			highest = lowest | (~netmask);
		}
		
	}
	
	/*Scan the entire subnet to find all hosts without a firewall*/
	private void findAvailHosts() {
		
		int start;
		int end;
		int[] gateway;
		String ipPrefix;
		
		if (EMU_MODE) {			
			start = EMU_IP_LOW;
			end = EMU_IP_HIGH;
			ipPrefix = EMU_IP_PREFIX;			
		} else {		
			start = getOctets(lowest)[3];
			end = getOctets(highest)[3];
			gateway = getOctets(info.gateway);
			
			/*Construct part of our ip address*/
			ipPrefix = gateway[0] + "." + gateway[1] + "." + gateway[2] + ".";			
		}
				
		/*Iterate over the entire network and ping each ip address*/
		for (int i = start + 1; i < end - 1; i++) {
			reachable = new CheckReachable();
			reachable.execute(ipPrefix + i, "15");
			sleep(15);
			if (isCancelled()) {
				reachable.cancel(true);
				return;
			}
		}
		
		/*Check our ARP table for online hosts*/
		arp = new ArpScan();
		arp.execute();
		if (isCancelled()) {
			arp.cancel(true);
			return;
		}
		
		try {
			arp.get();
		} catch (InterruptedException e) {		
		} catch (ExecutionException e) {			
		}
		
		initialScanCompleted = true;
	
	}
	
	/*
	 * Take our thread off the run queue
	 * since the thread pool is limited
	 */
	private void sleep(int time) {		
		
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) { }
		
	}

	/*
	 * Get the four parts of an ip address from the binary
	 * representation passed in	
	 */
	private int[] getOctets(int num){
		
		int octets[] = new int[4];
		
		octets[0] = num & 0xFF;
		octets[1] = (num >> 8) & 0xFF;
		octets[2] = (num >> 16) & 0xFF;
		octets[3] = (num >> 24) & 0xFF;
		
		return octets;
		
	}
	
	protected static void addNewHostToSet(String ip) {
		try {
			hosts.add(new Pingable(InetAddress.getByName(ip)));
		} catch (UnknownHostException e) { 		
		}
	}
	
	/*
	 * Called when a new host is found from the
	 * arp table scan. Will attempt to add the new host
	 * to the ContentProvider
	 */
	protected static void addNewHost(Pingable p, Services s) {
		
		ContentValues values = new ContentValues();
		String ip = p.addr.getHostAddress();
						
		values.put(NetworkContentProvider.ID, ip.hashCode());
		values.put(NetworkContentProvider.COL_IP_ADDRESS, ip);
		values.put(NetworkContentProvider.COL_ONLINE, 1);
		
		if(s == Services.Samba)
			values.put(NetworkContentProvider.COL_SAMBA, 1);
		else
			values.put(NetworkContentProvider.COL_SAMBA, 0);
		
		resolver.insert(NetworkContentProvider.CONTENT_URI, values);

		
	}
	
	/*
	 * Called when a file sharing protocol is found to 
	 * be active and updates the ContentProvider to reflect
	 * the service found 
	 */
	protected static void changeFlag(String column, int flag, String ip) {
		
		ContentValues values = new ContentValues();		
		
		values.put(NetworkContentProvider.COL_SAMBA, flag);
		values.put(NetworkContentProvider.COL_ONLINE, 1);
		resolver.update(NetworkContentProvider.CONTENT_URI, values, 
						NetworkContentProvider.ID + "=" + ip.hashCode(), null);
		
	}
}
