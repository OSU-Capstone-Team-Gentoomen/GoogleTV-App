package edu.gentoomen.conduit.networking;

import jcifs.smb.SmbFile;
import android.os.AsyncTask;

/*
 * This class will take an SMBFile
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
		//TODO Algorithm for indexing a samba shared drive and writing it to the content provider
	}

}
