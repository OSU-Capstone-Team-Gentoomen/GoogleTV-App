package edu.gentoomen.conduit.networking;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedList;

import edu.gentoomen.utilities.Utils;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;

public class DeviceNavigator {

	/*
	 * create a new DeviceNavigator for each device you browse
	 * can't change device's ip address once instantiated
	 */

	private static final String TAG = "DevNav";

	public static String path = "";

	public DeviceNavigator(String dev) {}

	public static LinkedList<SmbFile> deviceLS() {

		Log.d(TAG, "Listing " + path);
		LinkedList<SmbFile> files = new LinkedList<SmbFile>();
		try {
			for (SmbFile f : new SmbFile("smb://" + path).listFiles())
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

	public static InputStream smbToInputStream(SmbFile file) {
		try {			
			return file.getInputStream();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "can't get input stream: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public static String getPath() { return path; }

	//TODO: add bounds checking so this stops trying to go up 
	//a directory when it's at the root directory of the share
	//TODO: ".." is a valid Windows file name
	public static String getParentPath(String path) {

		/*
		 * remove last /, get index of previous /, lob that off, add new slash
		 */
		if(path == null || path.lastIndexOf('/') < 0) { return path; }

		return path.substring(0,
				path.substring(0, path.length() - 2).lastIndexOf("/")) + "/";

	}

	/*
	 * note: UI restricts the user to only going up one directory at a time
	 * so no new paths of ../../../some_folder
	 */
	public static LinkedList<SmbFile> deviceCD(String folder) {

		String prevPath = path;
		if(folder.equalsIgnoreCase("..")) {
			path = getParentPath(path);			
		}
		else {
			Log.d(TAG, "path before append " + path);			
			path = path + folder + "/";
			path.trim();
			Log.d(TAG, "path after append " + path);
		}

		LinkedList<SmbFile> ls = deviceLS();
		if (ls == null && !Utils.isRoot(path)) {
			path = prevPath;
			return deviceLS();
		}
		else {
			return ls;
		}

	}

	public static LinkedList<SmbFile> jumpToPath(String path) {

		DeviceNavigator.path = path;
		return deviceLS();

	}

}