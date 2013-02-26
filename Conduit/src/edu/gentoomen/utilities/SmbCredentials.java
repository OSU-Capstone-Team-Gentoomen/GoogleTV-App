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
	
	public boolean ipHasAuth(String ip) {
		
		if (creds.size() == 0 || !creds.containsKey(ip))
			return false;

		return true;
		
	}
	
	//should be keying on mac when this is stored permanently
	public void addCredentials(String ip, String user, String password) {		
		creds.put(ip, new UserPass(user, password));
	}
	
	public NtlmPasswordAuthentication getNtlmAuth(String ip) {
		
		if (creds.size() != 0 && creds.containsKey(ip)) {
			UserPass user = creds.get(ip);
			return new NtlmPasswordAuthentication("", user.user, user.pass);
		}
		else {
			Log.d(TAG, "No authentication present, loggin in as anonymous");
			return new NtlmPasswordAuthentication("", "guest", "");
		}
		
	}
}
