package edu.gentoomen.conduit;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import edu.gentoomen.conduit.networking.DeviceNavigator;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class MediaContentProvider extends ContentProvider {
	
	/*Constants used to differentiate between different URIs*/
	public static final int    MEDIA = 666;	
	public static final int	   FOLDER = 667;
	
	/*private constants*/	
	private static final String AUTHORITY = "edu.gentoomen.conduit.media";
	private static final String BASE_PATH = "media";
	private static final String TAG = "MediaContentProvider";
	
	/*Column names*/
	public static final String ID = "_id";
	public static final String COL_PATH = "path";
	public static final String COL_NAME = "name";
	public static final String COL_TYPE = "type";	
	
	/*Column indexes*/
	public static final int PATH_COLUMN = 1;
	public static final int NAME_COLUMN = 2;
	public static final int TYPE_COLUMN = 3;
	
	/*URI for this provider*/
	public static final Uri MEDIA_URI =
			Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	
	private static final UriMatcher mUriMatcher = 
				new UriMatcher(UriMatcher.NO_MATCH);
	
	/*
	 * All our columns in array form, useful for checking
	 * if the projection passed in contains a valid column
	 */
	String[] availableColumns = {
			ID,
			COL_PATH,
			COL_NAME,
			COL_TYPE
	};
	
	static {
		
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/", FOLDER);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/file", MEDIA);
		
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {			
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String path,
			String[] selectionArgs, String sortOrder) {
		
		MatrixCursor curse = new MatrixCursor(availableColumns);
		Log.d(TAG, "Querying for files, uri given: " + uri);
		Log.d(TAG, "Selected path " + path);
		
		switch (mUriMatcher.match(uri)) {
		case MEDIA:
			Log.d(TAG, "Media file type selected");
			break;
		case FOLDER:
			Log.d(TAG, "folder type selected, doing LS of" + path);
			int counter = 1;
			for (SmbFile f : DeviceNavigator.deviceCD(path)) {
				try {
					if (f.isDirectory()) 
						curse.newRow().add(counter).add(f.getPath()).add(f.getName().substring(0, f.getName().length() - 1)).add(FOLDER);
					else 
						curse.newRow().add(counter).add(f.getPath()).add(f.getName()).add(MEDIA);
					counter++;
				} catch (SmbException e) {
					Log.d(TAG, "SmbException: " + e.getMessage());
				}
			}
			break;
		default:
			Log.d(TAG, "Unknown URI " + uri);
			return null;
		}
		
		return curse;
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
