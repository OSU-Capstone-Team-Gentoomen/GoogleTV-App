package edu.gentoomen.conduit.contentproviders;

import edu.gentoomen.utilities.DatabaseHelper;
import edu.gentoomen.utilities.DatabaseUtility;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class ResumeContentProvider extends ContentProvider {

	// Our database
	private static SQLiteDatabase mDB;

	// Name of our table
	public static final String TABLE_RESUME = "resume";

	// Constants to differentiate between different URIs
	private static final int RESUME = 888;
	private static final int RESUME_ID = 889;

	// private constants
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "Resume.db";
	private static final String AUTHORITY = "edu.gentoomen.conduit.resume";
	private static final String BASE_PATH = "resume";
	private static final String TAG = "ResumeContentProvider";

	// Publish our column names
	public static final String ID = "_id";
	public static final String COL_HASH = "md5Hash";
	public static final String COL_PATH = "filePath";
	public static final String COL_NAME = "name";
	public static final String COL_TIME = "playbackTime";

	// Column Indexes
	public static final int HASH_COLUMN = 1;
	public static final int PATH_COLUMN = 2;
	public static final int NAME_COLUMN = 3;
	public static final int TIME_COLUMN = 4;

	// URI for this provider
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/resume";

	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/resume_item";

	private static final UriMatcher mUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {

		mUriMatcher.addURI(AUTHORITY, BASE_PATH, RESUME);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RESUME_ID);

	}

	private final String[] avail = { ID, COL_TIME, COL_NAME, COL_PATH, COL_HASH };

	private static final String CREATE_TABLE_RESUME = "create table "
			+ TABLE_RESUME + " (" + ID + " integer, " + COL_HASH
			+ " text primary key, " + COL_PATH + " text not null, " + COL_NAME
			+ " text not null, " + COL_TIME + " text not null);";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int rowsDeleted = 0;
		switch (mUriMatcher.match(uri)) {
		case RESUME:
			Log.d(TAG, "Deleting row");
			rowsDeleted = mDB.delete(TABLE_RESUME, selection, null);
			break;
		case RESUME_ID:
			break;
		default:
			throw new IllegalStateException("Passed in an invalid URI: " + uri);
		}

		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {

		switch (mUriMatcher.match(uri)) {

		case RESUME:
			return "vnd.android.cursor.dir/vnd.gentoomen.resume";
		case RESUME_ID:
			return "vnd.android.cursor.item/vnd.gentoomen.resume";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {

		long rowID = mDB.insert(TABLE_RESUME, null, values);
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}

		Log.d(TAG, "Failed to insert row into " + inuri);
		return null;

	}

	@Override
	public boolean onCreate() {

		Context context = getContext();
		DatabaseHelper helper = new DatabaseHelper(context, DB_NAME, null,
				DB_VERSION, CREATE_TABLE_RESUME, TABLE_RESUME);
		mDB = helper.getWritableDatabase();

		if (mDB == null)
			return true;
		else
			return false;

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Use the sqlite query builder object to build our query
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_RESUME);

		// Check if the caller has requested a nonexistant column
		DatabaseUtility.checkColumns(projection, avail);

		switch (mUriMatcher.match(uri)) {
		case RESUME_ID:
			queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
		case RESUME:
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		Cursor curse = queryBuilder.query(mDB, projection, selection,
				selectionArgs, null, null, sortOrder);

		curse.setNotificationUri(getContext().getContentResolver(), uri);
		Log.d("Will-debug", "Count of database is: " + curse.getCount());
		return curse;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int rowsUpdated = 0;

		switch (mUriMatcher.match(uri)) {
		case RESUME_ID:
			break;
		case RESUME:
			Log.d(TAG, "Updating entry in resume.db");
			rowsUpdated = mDB.update(TABLE_RESUME, values, selection,
					selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		return rowsUpdated;

	}

}
