package edu.gentoomen.contentproviders;

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

/*
 * This class and AuthenticationContentProvider should be inheriting
 * from a parent class to reduce code
 */

public class DeviceContentProvider extends ContentProvider {
	
	//Our database
	private static SQLiteDatabase mDB;
	
	//Name of our table
	public static final String  TABLE_DEVICES = "devices";
	
	//Constants to differentiate between different URIs
	private static final int    DEVICE = 7331;
	private static final int    DEVICE_ID = 1337;
	
	//private constants
	private static final int    DB_VERSION = 1;
	private static final String DB_NAME = "Devices.db";
	private static final String AUTHORITY = "edu.gentoomen.conduit.networking";
	private static final String BASE_PATH = "devices";
	private static final String TAG = "NetworkContentProvider";	
	
	//Publish our column names	
	public static final String  ID = "_id";
	public static final String  COL_IP_ADDRESS = "ipAddress";
	public static final String  COL_SAMBA = "hasSamba";
	public static final String  COL_ONLINE = "isOnline";
	public static final String  COL_NBTADR = "nbtAdr";
	public static final String	COL_MAC = "macAddress";
	
	//Column Indexes
	public static final int     IPADDR_COLUMN = 1;
	public static final int     ONLINE_COLUMN = 2;
	public static final int     SAMBA_COLUMN = 3;
	
	
	
	//URI for this provider
	public static final Uri CONTENT_URI = 
			Uri.parse("content://" + AUTHORITY + 
					"/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = 
			ContentResolver.CURSOR_DIR_BASE_TYPE  + "/devices";
	
	public static final String CONTENT_ITEM_TYPE = 
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/device";
	
	private static final UriMatcher mUriMatcher = 
			new UriMatcher(UriMatcher.NO_MATCH);
	
	static{
		
		mUriMatcher.addURI(AUTHORITY, BASE_PATH, DEVICE);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", DEVICE_ID);
		
	}
	
	private final String[] avail = {
			ID,
			COL_IP_ADDRESS,
			COL_MAC,
			COL_ONLINE,
			COL_SAMBA,
			COL_NBTADR
	};

	private static final String CREATE_TABLE_DEVICES = 
			"create table "  + TABLE_DEVICES + " ("
			+ ID             + " integer, "
			+ COL_IP_ADDRESS + " text not null,"
			+ COL_MAC		 + " text primary key,"
			+ COL_ONLINE     + " integer not null, " 
			+ COL_SAMBA      + " integer not null, "
			+ COL_NBTADR 	 + " text);"; 
		
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		//no deletion, at least not yet
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		
		switch(mUriMatcher.match(arg0)){
		
		case DEVICE:
			return "vnd.android.cursor.dir/vnd.gentoomen.device";
		case DEVICE_ID:
			return "vnd.android.cursor.item/vnd.gentoomen.device";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + arg0);		
		}
		
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {
		
		long rowID = mDB.insert(TABLE_DEVICES, null, values);
		if(rowID > 0){
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
		context.deleteDatabase(DB_NAME);
		DatabaseHelper helper 
			= new DatabaseHelper(context, DB_NAME, null, DB_VERSION, CREATE_TABLE_DEVICES, TABLE_DEVICES);
		mDB = helper.getWritableDatabase();
		
		if(mDB == null)
			return true;
		else
			return false;
		
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		//Use the sqlite query builder object to build our query
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_DEVICES);
		
		//Check if the caller has requested a nonexistant column
		DatabaseUtility.checkColumns(projection, avail);
				
		switch(mUriMatcher.match(uri)) {			
			case DEVICE_ID: 
				queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
			case DEVICE:
				break;				
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
				
		Cursor curse = queryBuilder.query(mDB, 
										  projection, 
										  selection, 
										  selectionArgs, 
										  null, null, 
										  sortOrder);
		
		curse.setNotificationUri(getContext().getContentResolver(), uri);
		Log.d(TAG, "Count of database is: " + curse.getCount());
		return curse;
	}

	@Override
	public int update(Uri uri, ContentValues values, 
					  String selection, String[] selectionArgs) {
		
		int rowsUpdated = 0;
		
		switch(mUriMatcher.match(uri)) {
			case DEVICE_ID:
				rowsUpdated = mDB.update(TABLE_DEVICES, 
										 values, 
										 selection, 
										 selectionArgs);
				break;
			case DEVICE:
				break;			
			default: 
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
					
		return rowsUpdated;
	}
	
	public static void clearDatabase() {
		mDB.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
		mDB.execSQL(CREATE_TABLE_DEVICES);
	}
	
}
