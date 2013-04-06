package edu.gentoomen.conduit.networking;

import java.net.InetAddress;

import android.os.AsyncTask;
import android.util.Log;

public class CheckReachable extends AsyncTask<String, Void, String> {

	String ipAddress;
	int timeout;

	@Override
	protected String doInBackground(String... params) {

		ipAddress = params[0];
		timeout = Integer.parseInt(params[1]);

		if (pingAddress(ipAddress, timeout)) {
			return ipAddress;
		}

		return null;
	}

	private boolean pingAddress(String address, int timeout) {
		boolean isReachable = false;
		try {
			Log.d("Will-debug", "Pinging address " + address);
			InetAddress addr = InetAddress.getByName(address);
			isReachable = addr.isReachable(timeout);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isReachable;
	}

}
