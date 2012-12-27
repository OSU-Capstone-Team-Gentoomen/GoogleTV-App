package edu.gentoomen.conduit.networking;

import jcifs.smb.SmbFile;
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

}
