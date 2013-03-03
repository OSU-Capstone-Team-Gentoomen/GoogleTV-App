package edu.gentoomen.conduit.networking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

public class ArpScan extends AsyncTask<Void, Void, String> {

	String TAG = "ARP";
	String ipAddress;
	
	@Override
	protected String doInBackground(Void... params) {
		try {			
			BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"));
			String line;
			
			try {				
				while((line = reader.readLine()) != null) {
					String split[] = line.split(" +");
					if(split != null && split.length >= 4) {
						String macAddress = split[3];
						String ip = split[0];
						if(macAddress.matches("..:..:..:..:..:..") && !macAddress.equals("00:00:00:00:00:00")) {
							Log.d(TAG, "Found host " + ip + " with mac " + macAddress);
							addNewHost(ip, macAddress);							
						}						
					}
				}				
				reader.close();
			} catch (IOException e) { }
						
		} catch (FileNotFoundException e) { }
		return null;
	}
	
	private void addNewHost(String ip, String mac) {		
		DiscoveryAgent.addNewHostToSet(ip, mac);		
	}

}
