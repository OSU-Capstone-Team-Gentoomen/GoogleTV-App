package edu.gentoomen.conduit.contentproviders;

import edu.gentoomen.utilities.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/*
 * This class and AuthenticationContentProvider should be inheriting
 * from a parent class to reduce code
 */

public class DeviceContentProvider extends TemplateContentProvider {

	private static SQLiteDatabase _DB;
	
	// Name of our table
	public static final String TABLE_DEVICES = "devices";

	// private constants
	private static final int DB_VERSION   = 1;
	private static final String DB_NAME   = "Devices.db";
	private static final String AUTHORITY = "edu.gentoomen.conduit.devices";
	private static final String BASE_PATH = "devices";
	private static final String TAG 	  = "NetworkContentProvider";

	// Publish our column names
	public static final String ID 			  = "_id";
	public static final String COL_IP_ADDRESS = "ipAddress";
	public static final String COL_SAMBA 	  = "hasSamba";
	public static final String COL_ONLINE 	  = "isOnline";
	public static final String COL_NBTADR 	  = "nbtAdr";
	public static final String COL_MAC 		  = "macAddress";

	// Column Indexes
	public static final int IPADDR_COLUMN = 1;
	public static final int ONLINE_COLUMN = 2;
	public static final int SAMBA_COLUMN  = 3;

	// URI for this provider
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	private final String[] avail = { ID, COL_IP_ADDRESS, COL_MAC, COL_ONLINE,
			COL_SAMBA, COL_NBTADR };

	private static final String CREATE_TABLE_DEVICES = "create table "
			+ TABLE_DEVICES + " (" + ID + " integer, " + COL_IP_ADDRESS
			+ " text not null," + COL_MAC + " text primary key," + COL_ONLINE
			+ " integer not null, " + COL_SAMBA + " integer not null, "
			+ COL_NBTADR + " text);";
	
	public static void clearDatabase() {
		_DB.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
		_DB.execSQL(CREATE_TABLE_DEVICES);
	}

	protected String assignLogTag() {
		return TAG;
	}

	protected String assignCreateDBString() {
		return CREATE_TABLE_DEVICES;
	}

	protected String assignTableName() {
		return TABLE_DEVICES;
	}

	protected Uri assignUri() {
		return CONTENT_URI;
	}

	protected String[] assignColumns() {
		return avail;		
	}

	protected void assignUriMatching() {		
		mUriMatcher.addURI(AUTHORITY, BASE_PATH, DEVICE);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", DEVICE_ID);
	}

	@Override
	protected SQLiteDatabase createSqlDB() {
		DatabaseHelper helper = new DatabaseHelper(getContext(), DB_NAME, null,
				DB_VERSION, CREATE_TABLE_DEVICES, table_name);
		_DB = helper.getWritableDatabase();
		return _DB;
	}
	
}
