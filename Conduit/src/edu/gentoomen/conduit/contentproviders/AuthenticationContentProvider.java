package edu.gentoomen.conduit.contentproviders;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import edu.gentoomen.utilities.DatabaseHelper;

/*
 * This class and NetworkContentProvider should be inheriting
 * from a parent class to reduce code
 */
public class AuthenticationContentProvider extends TemplateContentProvider {

	// Our database
	private static SQLiteDatabase _DB;

	// Name of our table
	public static final String TABLE_CREDENTIALS = "credentials";

	// private constants
	private static final int DB_VERSION   = 1;
	private static final String DB_NAME   = "Credential.db";
	private static final String AUTHORITY = "edu.gentoomen.conduit.credentials";
	private static final String BASE_PATH = "credentials";
	private static final String TAG       = "AuthenticationNetworkProvider";

	// Publish our column names
	public static final String ID 			= "_id";
	public static final String COL_MAC      = "mac_address";
	public static final String COL_SERVICE  = "sharing_service";
	public static final String COL_USER     = "username";
	public static final String COL_PASSWORD = "password";

	// Column Indexes
	public static final int MAC_COLUMN 	    = 1;
	public static final int SERVICE_COLUMN  = 2;
	public static final int USER_COLUMN 	= 3;
	public static final int PASSWORD_COLUMN = 4;

	// URI for this provider
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	private static final String CREATE_TABLE_CREDENTIALS = "create table "
			+ TABLE_CREDENTIALS + " (" + ID + " integer, " + COL_MAC
			+ " text primary key," + COL_SERVICE + " text not null," + COL_USER
			+ " text not null," + COL_PASSWORD + " text not null)";

	private final String[] avail = {
			ID,
			COL_MAC,
			COL_SERVICE,
			COL_USER,
			COL_PASSWORD
	};

	@Override
	protected String assignLogTag() {
		return TAG;
	}

	@Override
	protected String assignTableName() {
		return TABLE_CREDENTIALS;
	}

	@Override
	protected Uri assignUri() {
		return CONTENT_URI;
	}

	@Override
	protected String[] assignColumns() {
		return avail ;
	}

	@Override
	protected void assignUriMatching() {
		mUriMatcher.addURI(AUTHORITY, BASE_PATH, CREDENTIAL);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CREDENTIAL_ID);		
	}

	@Override
	protected SQLiteDatabase createSqlDB() {
		DatabaseHelper helper = new DatabaseHelper(getContext(), DB_NAME, null,
				DB_VERSION, CREATE_TABLE_CREDENTIALS, table_name);
		_DB = helper.getWritableDatabase();
		return _DB;
	}



}
