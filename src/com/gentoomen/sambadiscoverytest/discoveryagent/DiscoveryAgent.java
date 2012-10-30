package com.gentoomen.sambadiscoverytest.discoveryagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.StrictMode;

public class DiscoveryAgent extends AsyncTask<String, Void, String> {
	
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	
	ArrayList<String> stringList = new ArrayList<String>();
	
	public DiscoveryAgent(){
		StrictMode.setThreadPolicy(policy);
	}
	
	public String doInBackground(String... params){
		try{
			URL url = new URL("http://google.com");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			
			connection.connect();
			String response = convertStreamToString(connection.getInputStream());
			
			if(response != null){
				return response;			
			}
		}
		catch(IOException e){
			
		}		
		return "No response\n";
	}
	
	public String convertStreamToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				inputStream.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	
}
