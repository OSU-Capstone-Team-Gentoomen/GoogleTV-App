package edu.gentoomen.utilities;

import java.util.HashMap;

import android.util.Log;

import jcifs.smb.NtlmPasswordAuthentication;

public class SmbCredentials {
	
	private class UserPass {
		public String user;
		public String pass;
		
		public UserPass(String user, String pass) {
			this.user = user;
			this.pass = pass;
		}
	}

	private static final String TAG = "SmbCredentials";
	
	private HashMap<String, UserPass> creds;
	
	public SmbCredentials() {		
		creds = new HashMap<String, UserPass>();		
	}
	
	/*Can take either IP or NBT hostname, prefer NBT hostname since IPs can change*/
	public boolean hostHasAuth(String host) {
		
		if (creds.size() == 0 || !creds.containsKey(host))
			return false;

		return true;
		
	}
	
	//should be keying on mac when this is stored permanently
	public void addCredentials(String mac, String user, String password) {		
		creds.put(mac, new UserPass(user, password));
	}
	
	public NtlmPasswordAuthentication getNtlmAuth(String mac) {
		
		if (creds.size() != 0 && creds.containsKey(mac)) {
			UserPass user = creds.get(mac);
			return new NtlmPasswordAuthentication("", user.user, user.pass);
		}
		else {
			Log.d(TAG, "No authentication present, loggin in as anonymous");
			return new NtlmPasswordAuthentication("", "guest", "");
		}
		
	}
	
	public void clearCredentials() {
		creds.clear();
	}
	
	public void removeCredential(String ip) {
		creds.remove(ip);
	}
}
