package edu.gentoomen.conduit.contentproviders;

import java.net.MalformedURLException;
import java.util.LinkedList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import edu.gentoomen.conduit.BrowserActivity;
import edu.gentoomen.conduit.FileListFragment;
import edu.gentoomen.conduit.networking.DeviceNavigator;

public class MediaContentProvider extends ContentProvider {

	/* Constants used to differentiate between different URIs */
	public static final int MEDIA  = 666;
	public static final int FOLDER = 667;
	public static final int FAVS   = 668;
	public static final int UNFIN  = 669;
	
	/* Used to multiplex between different selection states */
	public static final int SELECT_FROM_FAVORITES  = 0;
	public static final int SELECT_FROM_UNFINISHED = 1;
	public static final int SELECT_FROM_DEVICE 	   = 2;
	
	/* private constants */
	private static final String AUTHORITY = "edu.gentoomen.conduit.media";
	private static final String BASE_PATH = "media";
	private static final String TAG = "MediaContentProvider";

	/* Column names */
	public static final String ID = "_id";
	public static final String COL_PATH = "path";
	public static final String COL_NAME = "name";
	public static final String COL_TYPE = "type";

	/* Column indexes */
	public static final int PATH_COLUMN = 1;
	public static final int NAME_COLUMN = 2;
	public static final int TYPE_COLUMN = 3;

	/* URI for this provider */
	public static final Uri MEDIA_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	private static final UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);	

	/*
	 * All our columns in array form, useful for checking if the projection
	 * passed in contains a valid column
	 */
	String[] availableColumns = { ID, COL_PATH, COL_NAME, COL_TYPE };

	static {

		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/", FOLDER);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/file", MEDIA);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/favorites", FAVS);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/unfinished", UNFIN);
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

	/*
	 * Uses the current path in the device navigator to determine if we are in a
	 * root directory
	 */
	public static boolean isRoot() {

		try {
			return (new SmbFile("smb://" + DeviceNavigator.getPath(),
					BrowserActivity.getCredentials().getNtlmAuth(
							FileListFragment.selectedServer.mac)).getParent()
					.toString().equalsIgnoreCase("smb://"));
		} catch (MalformedURLException e) {
			Log.d(TAG,
					"MalformedURLException caught on checking for root on path "
							+ DeviceNavigator.getPath());
		}

		return false;

	}

	/*
	 * Pass in a path to determine if the path is the root or not
	 */
	public static boolean isRoot(String filePath) {

		try {
			return (new SmbFile("smb://" + filePath, BrowserActivity
					.getCredentials().getNtlmAuth(
							FileListFragment.selectedServer.mac)).getParent()
					.toString().equalsIgnoreCase("smb://"));
		} catch (MalformedURLException e) {
			Log.d(TAG,
					"MalformedURLException caught on checking for root on path "
							+ DeviceNavigator.getPath());
		}

		return false;

	}

	@Override
	/*
	 * fileName can also be a folder, if so it's only a folder name and not an
	 * absolute or relative path
	 */
	public Cursor query(Uri uri, String[] projection, String fileName,
			String[] selectionArgs, String sortOrder) {

		MatrixCursor curse = new MatrixCursor(availableColumns);
		Log.d(TAG, "Querying for files, uri given: " + uri);
		Log.d(TAG, "Selected file " + fileName);

		switch (mUriMatcher.match(uri)) {
		case MEDIA:
			Log.d(TAG, "Media file type selected");
			break;

		case FAVS:			
			break;
			
		case UNFIN:
			String mProjection[] = {
				ResumeContentProvider.COL_PATH,
				ResumeContentProvider.COL_NAME,						
			};
			
			ContentResolver resolver = getContext().getContentResolver();
			Cursor mCurse = resolver.query(ResumeContentProvider.CONTENT_URI, mProjection, null, null, "");
						
			for (int i = 0; mCurse.moveToNext(); i++) {
				
			curse.newRow().add(i)
						  .add(mCurse.getString(0) + mCurse.getString(1))
					      .add(mCurse.getString(1))
						  .add(MEDIA);
			
			}
			
			return curse;
			
		case FOLDER:
			Log.d(TAG, "folder type selected, doing LS of " + fileName);
			int counter = 1;

			LinkedList<SmbFile> listOfFiles = DeviceNavigator.deviceCD(fileName);

			if (!isRoot()) {
				/*
				 * add a .. folder to go back, will be moved to the front end
				 * later
				 */
				curse.newRow().add(counter)
						.add(DeviceNavigator.getParentPath(fileName)).add("..")
						.add(FOLDER);
				counter++;
			}

			if (listOfFiles == null)
				return null;

			for (SmbFile f : listOfFiles) {
				try {
					String name = f.getName();
					int type;

					// if this is a directory, string the trailing slash
					if (f.isDirectory()) {
						name = name.substring(0, name.length() - 1);
						type = FOLDER;
					} else {
						type = MEDIA;
					}

					curse.newRow().add(counter).add(f.getPath().substring(6)).add(name)
							.add(type);

					counter++;
				} catch (SmbException e) {

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
