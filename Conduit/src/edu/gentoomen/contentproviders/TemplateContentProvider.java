package edu.gentoomen.contentproviders;

import edu.gentoomen.utilities.DatabaseHelper;
import edu.gentoomen.utilities.DatabaseUtility;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public abstract class TemplateContentProvider extends ContentProvider {

	private static SQLiteDatabase mDB;
	
	private static final int    RESUME = 888;
	private static final int    RESUME_ID = 889;
	private static final int    DEVICE = 7331;
	private static final int    DEVICE_ID = 1337;
	private static final int    CREDENTIAL = 666;
	private static final int    CREDENTIAL_ID = 777;	
	
	public  static final String ID = "_id";
	private static final String CRED_STRING = "vnd.android.cursor.dir/vnd.gentoomen.credential";
	private static final String CRED_ID_STRING = "vnd.android.cursor.item/vnd.gentoomen.credential";
	private static final String RESUME_STRING = "vnd.android.cursor.dir/vnd.gentoomen.resume";
	private static final String RESUME_ID_STRING = "vnd.android.cursor.item/vnd.gentoomen.resume";
	private static final String DEVICE_STRING = "vnd.android.cursor.dir/vnd.gentoomen.device";
	private static final String DEVICE_ID_STRING = "vnd.android.cursor.item/vnd.gentoomen.device";	
		
	private static int    		db_version;
	private static String 		db_name;
	private static String 		authority;
	private static String 		base_path;
	private static String 		tag;
	private	static String		create_table;
	public  static String  		table_name;
	public 	static Uri			content_uri;
	
	private static 	UriMatcher	mUriMatcher =
			new UriMatcher(UriMatcher.NO_MATCH);
		
	private static String[] avail = null;
	
	private static  SQLiteQueryBuilder queryBuilder = 
			new SQLiteQueryBuilder();
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int rowsDeleted = 0;
		
		
		if (!matchUri(uri)) {
			Log.d(tag, "Deleting Row");
			rowsDeleted = mDB.delete(table_name, selection, null);
		}
		
//		switch (mUriMatcher.match(uri)) {
//		case RESUME:
//			Log.d(TAG, "Deleting row");
//			rowsDeleted = mDB.delete(TABLE_NAME, selection, null);
//			break;
//		case RESUME_ID:
//			break;
//		default:
//			throw new IllegalStateException("Passed in an invalid URI: " + uri);
//		}
		
		return rowsDeleted;
		
	}

	@Override
	public String getType(Uri uri) {
		
		switch (mUriMatcher.match(uri)) {
		case RESUME:
			return RESUME_STRING;
		case RESUME_ID:
			return RESUME_ID_STRING;
		case CREDENTIAL:
			return CRED_STRING;
		case CREDENTIAL_ID:
			return CRED_ID_STRING;
		case DEVICE:
			return DEVICE_STRING;
		case DEVICE_ID:
			return DEVICE_ID_STRING;
		}
		
		return null;
		
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {
		
		long rowID = mDB.insert(table_name, null, values);
		if(rowID > 0){
			Uri uri = ContentUris.withAppendedId(content_uri, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}	

		Log.d(tag, "Failed to insert row into " + inuri);
		return null;
		
	}

	@Override
	public boolean onCreate() {
		
		initFields();
		DatabaseHelper helper 
		= new DatabaseHelper(getContext(), db_name, null, db_version, create_table, table_name);
		mDB = helper.getWritableDatabase();

		if(mDB == null)
			return true;
		else
			return false;
				
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		/* Ensure valid columns are being selected */
		DatabaseUtility.checkColumns(projection, avail);
		
		if (matchUri(uri))
			queryBuilder.appendWhere(ID + '=' + uri.getLastPathSegment());
		else
			throw new IllegalArgumentException("Unknown URI: " + uri);
		
//		switch(mUriMatcher.match(uri)) {			
//			case DEVICE_ID: 
//				queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
//			case DEVICE:
//				break;		
//			default:
//				throw new IllegalArgumentException("Unknown URI: " + uri);
//		}			
		
		/* Build the SQL query and execute it */
		Cursor curse = queryBuilder.query(mDB, 
										  projection, 
										  selection, 
										  selectionArgs, 
										  null, null, 
										  sortOrder);
		
		curse.setNotificationUri(getContext().getContentResolver(), uri);
		Log.d(tag, "Count of database is: " + curse.getCount());
		return curse;
	}

	private boolean matchUri(Uri uri) {
		
		switch (mUriMatcher.match(uri)) {
		case DEVICE:
			return false;
		case DEVICE_ID:
			return true;
		case RESUME:
			return false;
		case RESUME_ID:
			return true;
		case CREDENTIAL:
			return false;
		case CREDENTIAL_ID:
			return true;
		default:
			throw new IllegalStateException("Unknown URI: " + uri);
		}
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rowsUpdated = 0;

		if (!matchUri(uri)) {
			Log.d(tag, "Updating entry in " + table_name);
			rowsUpdated = mDB.update(table_name, values, selection, selectionArgs);
		}
		
//		switch(mUriMatcher.match(uri)) {
//		case RESUME_ID:			
//			break;
//		case RESUME:
//			Log.d(tag, "Updating entry in resume.db");
//			rowsUpdated = mDB.update(table_name, 
//					values, 
//					selection, 
//					selectionArgs);
//			break;			
//		default: 
//			throw new IllegalArgumentException("Unknown URI: " + uri);
//		}

		return rowsUpdated;
		
	}

	/* Will be called by onCreate to init the string fields */
	protected abstract int initFields();
	
}
