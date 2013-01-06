package edu.gentoomen.conduit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class DummyProvider extends ContentProvider {

	private static final String LOG_TAG = "DummyProvider";

	private static final int DEVICES = 1;
	private static final int DEVICE_ONE = 2;
	private static final int DEVICE_TWO = 3;
	private static final int DEVICE_ONE_A = 4;
	private static final int DEVICE_ONE_B = 5;
	private static final int DEVICE_DELAYED = 6;
	private static final int DEVICE_DELAYED_DIR = 7;

	public static final int FILE_TYPE = 1;
	public static final int DIR_TYPE = 2;

	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		String authority = "edu.gentoomen.conduit.dummyprovider";
		sUriMatcher.addURI(authority, "devices", DEVICES);
		// TODO prefix these with "devices/" to avoid name collisions
		sUriMatcher.addURI(authority, "one", DEVICE_ONE);
		sUriMatcher.addURI(authority, "two", DEVICE_TWO);
		sUriMatcher.addURI(authority, "delayed", DEVICE_DELAYED);
		sUriMatcher.addURI(authority, "delayed/dir", DEVICE_DELAYED_DIR);

		//sUriMatcher.addURI(authority, "local", DEVICE_ONE);
		sUriMatcher.addURI(authority, "one/dir one", DEVICE_ONE_A);
		sUriMatcher.addURI(authority, "one/dir two", DEVICE_ONE_B);
	}

	@Override
	public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
			String arg4) {

		String[] cols = new String[] {
			"_ID",
			"name",
			"path",
			"type",
		};

		MatrixCursor c = new MatrixCursor(cols);

		Log.d(LOG_TAG, uri.toString());
		Log.d(LOG_TAG, Integer.toString(sUriMatcher.match(uri)));

		
		
		switch (sUriMatcher.match(uri)) {
		
			case DEVICE_ONE:
				c.newRow().add(1).add("dir one").add("one/dir one").add(DIR_TYPE);
				c.newRow().add(2).add("dir two").add("one/dir two").add(DIR_TYPE);
				break;
			case DEVICE_TWO:
				c.newRow().add(1).add("file three").add("two/file three").add(FILE_TYPE);
				c.newRow().add(2).add("file four").add("two/file four").add(FILE_TYPE);
				break;
			case DEVICE_ONE_A:
				for (int i = 1; i < 20; i++) {
					c.newRow().add(i).add("file " + Integer.toString(i)).add("one/dir one/file").add(FILE_TYPE);	
				}
				break;
			case DEVICE_ONE_B:
				c.newRow().add(1).add("file seven").add("one/dir two/file seven").add(FILE_TYPE);
				c.newRow().add(2).add("file eight").add("one/dir two/file eight").add(FILE_TYPE);
				break;
			case DEVICE_DELAYED:
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				    Thread.currentThread().interrupt();
				}
	        	c.newRow().add(1).add("delayed dir").add("delayed/dir").add(DIR_TYPE);
	        	c.newRow().add(2).add("file").add("delayed/file").add(FILE_TYPE); 
			    break;
			case DEVICE_DELAYED_DIR:
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				    Thread.currentThread().interrupt();
				}
				c.newRow().add(1).add("file").add("delayed/dir/file").add(FILE_TYPE);
				break;
			default:
		}

		return c;
	}

	// TODO throwing exceptions for unimplemented methods would be nice

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		return true;
	}


	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}