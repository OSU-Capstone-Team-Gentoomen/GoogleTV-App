package edu.gentoomen.conduit.networking;

import java.net.MalformedURLException;
import java.util.LinkedList;

import android.util.Log;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class DeviceNavigator {
	
	/*
	 * create a new DeviceNavigator for each device you browse
	 * can't change device's ip address once instantiated
	 */
	
	private static final String TAG = "DevNav";
	
	private static String device = null;
	private static String path = "";
	
	public DeviceNavigator(String dev) {
		this.device = dev;		
	}
	
	public static LinkedList<SmbFile> deviceLS() {
		
		Log.d(TAG, "Listing " + path);
		LinkedList<SmbFile> files = new LinkedList<SmbFile>();
		try {
			for(SmbFile f : new SmbFile("smb://" + path).listFiles())
				files.add(f);
		} catch (SmbException e) {
			Log.d(TAG, "SmbException " + e.getMessage());
			return null;
		} catch (MalformedURLException e) {
			Log.d(TAG, "BadUrlException " + e.getMessage());
			return null;
		}
		
		return files;
		
	}
	
	public static String getPath() { return path; }
	
	/*
	 * note: UI restricts the user to only going up one directory at a time
	 * so no new paths of ../../../some_folder
	 */
	public static LinkedList<SmbFile> deviceCD(String folder) {
		
		if(folder.equalsIgnoreCase("..")) {
			/*
			 * remove last /, get index of previous /, lob that off, add new slash
			 */
			path = path.substring(0,
						path.substring(0, path.length() - 2).lastIndexOf("/")) + "/";			
		}
		else {
			Log.d(TAG, "path before append" + path);
			path = path + folder + "/";
			path.trim();
			Log.d(TAG, "path after append" + path);
		}
		
		return deviceLS();
		
	}
	
	public static LinkedList<SmbFile> jumpToPath(String path) {
		
		DeviceNavigator.path = path;
		return deviceLS();
		
	}
}
