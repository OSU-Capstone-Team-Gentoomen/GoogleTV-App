package edu.gentoomen.conduit.contentproviders;

import edu.gentoomen.utilities.DatabaseHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ResumeContentProvider extends TemplateContentProvider {

	private static SQLiteDatabase _DB;
	
	// Name of our table
	public static final String TABLE_RESUME = "resume";

	// private constants
	private static final int DB_VERSION   = 1;
	private static final String DB_NAME   = "Resume.db";
	private static final String AUTHORITY = "edu.gentoomen.conduit.resume";
	private static final String BASE_PATH = "resume";
	private static final String TAG       = "ResumeContentProvider";

	// Publish our column names
	public static final String ID 		= "_id";
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

	private final String[] avail = { ID, COL_TIME, COL_NAME, COL_PATH, COL_HASH };

	private static final String CREATE_TABLE_RESUME = "create table "
			+ TABLE_RESUME + " (" + ID + " integer, " + COL_HASH
			+ " text primary key, " + COL_PATH + " text not null, " + COL_NAME
			+ " text not null, " + COL_TIME + " text not null);";
	
	protected String assignLogTag() {
		return TAG;
	}

	protected String assignCreateDBString() {
		return CREATE_TABLE_RESUME;
	}

	protected String assignTableName() {
		return TABLE_RESUME;
	}

	protected Uri assignUri() {
		return CONTENT_URI;
	}

	protected String[] assignColumns() {
		return avail;
	}

	protected void assignUriMatching() {
		
		mUriMatcher.addURI(AUTHORITY, BASE_PATH, RESUME);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", RESUME_ID);
		
	}

	@Override
	protected SQLiteDatabase createSqlDB() {
		DatabaseHelper helper = new DatabaseHelper(getContext(), DB_NAME, null,
				DB_VERSION, CREATE_TABLE_RESUME, table_name);
		_DB = helper.getWritableDatabase();
		return _DB;
	}

}
