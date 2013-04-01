package edu.gentoomen.conduit.networking;

import contentproviders.MediaContentProvider;
import jcifs.smb.SmbFile;
import android.content.ContentValues;
import android.os.AsyncTask;

/*
 * This class will take an Object
 * and determine the protocol used
 * and index the directory recursively
 */

public class FileIndexer extends AsyncTask<Object, Void, String> {

	@Override
	protected String doInBackground(Object... params) {	
		
		/*Determine the type of protocol the device is using*/
		if(params[0] instanceof SmbFile)
			indexSambaDrive((SmbFile)params[0]);
					
		return null;
		
	}
	
	private void indexSambaDrive(SmbFile file) {
		
		
		
	}
	
	@SuppressWarnings("unused")
	private void insertIntoProvider(SmbFile file) {
		
		ContentValues values = new ContentValues();
		String extension = "unknown";
		
		int index = file.getName().lastIndexOf(".");
		if (index > 0)
			extension = file.getName().substring(index + 1);
		
		values.put(MediaContentProvider.COL_NAME, file.getName());
		values.put(MediaContentProvider.COL_PATH, file.getPath());
		values.put(MediaContentProvider.COL_TYPE, extension);
		
	}
}
