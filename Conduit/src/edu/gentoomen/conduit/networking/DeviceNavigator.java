package edu.gentoomen.conduit.networking;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.gentoomen.conduit.BrowserActivity;
import edu.gentoomen.conduit.FileListFragment;
import edu.gentoomen.utilities.Utils;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;

public class DeviceNavigator {

	/*
	 * create a new DeviceNavigator for each device you browse can't change
	 * device's ip address once instantiated
	 */

	private static ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	private static final String TAG = "DevNav";

	private static String path = "";
		
	public interface Callbacks {
		public void onAuthFailed();
		public void onTimeout();
		public void onConnectError();
		public void onAccessDenied();
	}
	
	private static Callbacks errCallbacks;
	
	public DeviceNavigator() {
	}
	
	private static class CdFolder implements Callable<SmbFile[]> {
		
		public CdFolder() {			
		}
		
		@Override
		public SmbFile[] call() throws SmbException, MalformedURLException {
			return new SmbFile("smb://" + path, BrowserActivity.getCredentials().getNtlmAuth(FileListFragment.selectedServer.mac)).listFiles(new FileListFilter());
		}
	}

	public static LinkedList<SmbFile> deviceLS() {

		Log.d(TAG, "Listing " + path);
		LinkedList<SmbFile> files = new LinkedList<SmbFile>();
		Future<SmbFile[]> result = executorService.submit(new CdFolder());
		SmbFile[] tmp = null;
		try {
			tmp = result.get(2000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
		} catch (ExecutionException e1) {
			Throwable e = e1.getCause();
			
			if(e instanceof SmbException) {
				int status = ((SmbException)e).getNtStatus();
				
				switch(status) {
				case SmbException.NT_STATUS_ACCESS_DENIED:
					Log.d(TAG, "auth error caught");					
					errCallbacks.onAccessDenied();
					break;
				case SmbException.NT_STATUS_UNSUCCESSFUL:
					Log.d(TAG, "Could not connect");
					errCallbacks.onConnectError();
					break;
				case SmbException.NT_STATUS_LOGON_FAILURE:
					Log.d(TAG, "Could not logon");
					errCallbacks.onAuthFailed();
					break;
				default:
					Log.d(TAG, "Other error caught");
				}
			}						
			
		} catch (TimeoutException e) {
			errCallbacks.onTimeout();
		}
		
		if (tmp == null)
			return null;
		
		for (SmbFile f : tmp)
			files.add(f);
		
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

	// TODO: add bounds checking so this stops trying to go up
	// a directory when it's at the root directory of the share
	// TODO: ".." is a valid Windows file name
	public static String getParentPath(String path) {

		/*
		 * remove last /, get index of previous /, lob that off, add new slash
		 */
		if (path == null || path.lastIndexOf('/') < 0) {
			return path;
		}

		return path.substring(0, path.substring(0, path.length() - 2)
				.lastIndexOf("/"))
				+ "/";

	}

	/*
	 * note: UI restricts the user to only going up one directory at a time so
	 * no new paths of ../../../some_folder
	 */
	public static LinkedList<SmbFile> deviceCD(String folder) {

		String prevPath = path;
		if (folder.equalsIgnoreCase("..")) {
			path = getParentPath(path);
		} else {
			Log.d(TAG, "path before append " + path);
			path = path + folder + "/";
			path.trim();
			Log.d(TAG, "path after append " + path);
		}

		LinkedList<SmbFile> ls = deviceLS();
		if (ls == null && !Utils.isRoot(path)) {
			path = prevPath;
			return deviceLS();
		} else {
			return ls;
		}

	}

	public static LinkedList<SmbFile> jumpToPath(String path) {

		DeviceNavigator.path = path;
		return deviceLS();

	}

	public static String getPath() {
		return path;
	}

	public static void setPath(String newPath) {
		path = newPath;
	}

	public static void setErrCallbacks(Callbacks errorListener) {
		errCallbacks = errorListener;
	}
		

}