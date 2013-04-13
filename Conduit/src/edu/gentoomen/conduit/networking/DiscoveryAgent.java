package edu.gentoomen.conduit.networking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import edu.gentoomen.conduit.contentproviders.DeviceContentProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.StrictMode;
import android.util.Log;
import edu.gentoomen.utilities.NetworkUtils;
import edu.gentoomen.utilities.Services;

/*
 * This class is used to discover file servers that can then be browsed
 */
public class DiscoveryAgent {

	private static String 			   TAG = "DiscoveryAgent";

	/* Will be used to query the db */
	private static ContentResolver 	   resolver = null;
	
	@SuppressWarnings("unused")
	private SambaDiscoveryAgent 	   sAgent = null;

	/* Used to keep track of all online hosts */
	private static Map<String, Device> hosts = new HashMap<String, Device>();
	private DhcpInfo 				   info = null;

	/* The highest and lowest ip addresses in our subnet */
	protected int 					   lowest = 0;
	protected int 					   highest = 0;

	/* Async scanning functions */
	AsyncTask<String, Void, String>    reachable;	

	/* the threaded scan task */
	private ScanTask 		   		   scanTask;

	/* Callbacks used to inform the front end of scan events */
	private ScanListener 			   callbacks;

	public interface ScanListener {
		public void onScanStarted();
		public void onScanFinished();
	}

	/*
	 * Set the thread policy to run off of the main thread. This will help keep
	 * the UI smooth
	 */
	static StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	static {
		/* Set our thread policy */
		StrictMode.setThreadPolicy(policy);
	}

	public DiscoveryAgent(ContentResolver resolv, DhcpInfo info,
			ScanListener callbacks) {

		resolver = resolv;
		this.info = info;
		this.callbacks = callbacks;

		if (hosts.size() > 0) {
			hosts.clear();
		}
	}

	/* how many scanned IPs are stored in the database */
	public int getScannedCount() {

		Cursor c = resolver.query(DeviceContentProvider.CONTENT_URI, null,
				null, null, null);
		log("Found hosts: " + c.getCount());
		return c.getCount();

	}

	/* how many IPs are online in the database */
	public int getOnlineCount() {

		String[] projection = { DeviceContentProvider.COL_ONLINE };

		Cursor c = resolver.query(DeviceContentProvider.CONTENT_URI,
				projection, DeviceContentProvider.COL_ONLINE + " = 1", null,
				null);

		return c.getCount();

	}

	/* how many online hosts have a samba share */
	public int getSambaCount() {

		String[] projection = { DeviceContentProvider.COL_ONLINE,
				DeviceContentProvider.COL_SAMBA };

		Cursor c = resolver.query(DeviceContentProvider.CONTENT_URI,
				projection, DeviceContentProvider.COL_ONLINE + " = 1 AND "
						+ DeviceContentProvider.COL_SAMBA + " = 1", null, null);
		return c.getCount();

	}

	/*
	 * Adds online hosts to a hashset which will be used by other discovery
	 * services to determine if the hosts contain a sharing service
	 * 
	 * This function does not add anything to the content provider, it just adds
	 * hosts to the hashset to be used by other discovery services
	 */
	public static void addNewHostToSet(String ip, String macAddress) {

		try {
			hosts.put(macAddress, new Device(InetAddress.getByName(ip),
					macAddress, null));
		} catch (UnknownHostException e) {
		}

	}

	/*
	 * Called when a host share is found from the share discovery agents. Will
	 * attempt to add the new host to the ContentProvider
	 */
	public static void addNewHost(Device p, Services s) {

		ContentValues values = new ContentValues();
		String ip = p.addr.getHostAddress();
		String macAddress = p.mac;

		values.put(DeviceContentProvider.ID, ip.hashCode());
		values.put(DeviceContentProvider.COL_IP_ADDRESS, ip);
		values.put(DeviceContentProvider.COL_MAC, macAddress);
		values.put(DeviceContentProvider.COL_ONLINE, 1);
		values.put(DeviceContentProvider.COL_NBTADR, p.nbtName);

		if (s == Services.Samba)
			values.put(DeviceContentProvider.COL_SAMBA, 1);
		else
			values.put(DeviceContentProvider.COL_SAMBA, 0);

		resolver.insert(DeviceContentProvider.CONTENT_URI, values);

	}

	/*
	 * Called when a file sharing protocol is found to be active and updates the
	 * ContentProvider to reflect the service found
	 */
	public static void changeSambaFlag(String column, int flag, String ip) {

		ContentValues values = new ContentValues();

		values.put(DeviceContentProvider.COL_SAMBA, flag);
		values.put(DeviceContentProvider.COL_ONLINE, 1);
		resolver.update(DeviceContentProvider.CONTENT_URI, values,
				DeviceContentProvider.ID + "=" + ip.hashCode(), null);

	}

	public static Map<String, Device> getHostSet() {
		return hosts;
	}

	public static Device macToPingable(String mac) throws IllegalArgumentException {

		if (hosts.containsKey(mac))
			return hosts.get(mac);

		/* Should be creating a new Device instead */
		throw new IllegalStateException("Passed in a nonexistant MAC");
	}

	public void scan() {

		DeviceContentProvider.clearDatabase();
		
		if (hosts.size() > 0)
			hosts.clear();

		if (scanTask != null
				&& (scanTask.getStatus() == Status.RUNNING || scanTask
						.getStatus() == Status.PENDING))
			scanTask.cancel(true);

		//callbacks.onScanStarted();		
		scanTask = new ScanTask();		
		scanTask.execute("");
	}

	/* Our class used to represent the threaded task to be executed */
	private class ScanTask extends AsyncTask<String, Void, String> {
		
		/*
		 * Function will perform network scan based off of the client's default
		 * gateway and scan for samba shares
		 */
		public String doInBackground(String... params) {
			
			
			/* Do our scan for online hosts */
			int[] range = NetworkUtils.getIpRange(info.gateway, info.netmask);
			lowest = range[0];
			highest = range[1];

			findAvailHosts();

			/* Look for samba shares from online hosts */
			sAgent = new SambaDiscoveryAgent();

			return "";

		}

		@Override
		public void onPostExecute(String results) {
			callbacks.onScanFinished();
		}
	}
	
	/* Scan the entire subnet to find all online hosts */
	private void findAvailHosts() {

		int start;
		int end;
		int[] gateway;
		String ipPrefix;

		start = NetworkUtils.getOctets(lowest)[3];
		end = NetworkUtils.getOctets(highest)[3];
		gateway = NetworkUtils.getOctets(info.gateway);

		/* Construct part of our ip address */
		ipPrefix = gateway[0] + "." + gateway[1] + "." + gateway[2] + ".";

		/* Iterate over the entire network and ping each ip address */
		for (int i = start + 2; i < end; i++) {
			reachable = new CheckReachable();
			reachable.execute(ipPrefix + i, "15");
			sleep(15);
			if (scanTask.isCancelled()) {
				reachable.cancel(true);
				return;
			}
		}

		/* Check our ARP table for online hosts */
		ArpScan.dumpArp();

	}

	/*
	 * Take our thread off the run queue since the thread pool is limited
	 */
	private void sleep(int time) {

		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}

	}
	
	private void log(String message) {
		Log.d(TAG, message);
	}
}
