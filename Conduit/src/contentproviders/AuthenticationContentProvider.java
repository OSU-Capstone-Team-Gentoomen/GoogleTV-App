package contentproviders;

import java.util.Arrays;
import java.util.HashSet;

import edu.gentoomen.utilities.DatabaseHelper;

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
 * This class and NetworkContentProvider should be inheriting
 * from a parent class to reduce code
 */
public class AuthenticationContentProvider extends ContentProvider {
		
		//Our database
		private static SQLiteDatabase mDB;
		
		//Name of our table
		public static final String  TABLE_CREDENTIALS = "credentials";
		
		//Constants to differentiate between different URIs
		private static final int    CREDENTIAL = 666;
		private static final int    CREDENTIAL_ID = 777;
		
		//private constants
		private static final int    DB_VERSION = 1;
		private static final String DB_NAME = "Credential.db";
		private static final String AUTHORITY = "edu.gentoomen.conduit.networking";
		private static final String BASE_PATH = "credentials";
		private static final String TAG = "AuthenticationNetworkProvider";	
		
		//Publish our column names	
		public static final String  ID = "_id";
		public static final String  COL_MAC = "mac_address";
		public static final String  COL_SERVICE = "sharing_service";
		public static final String 	COL_USER = "username";
		public static final String 	COL_PASSWORD = "password";
		
		//Column Indexes
		public static final int     MAC_COLUMN = 1;
		public static final int     SERVICE_COLUMN = 2;
		public static final int     USER_COLUMN = 3;
		public static final int		PASSWORD_COLUMN = 4;
		
		//URI for this provider
		public static final Uri CONTENT_URI = 
				Uri.parse("content://" + AUTHORITY + 
						"/" + BASE_PATH);
		
		public static final String CONTENT_TYPE = 
				ContentResolver.CURSOR_DIR_BASE_TYPE  + "/credentials";
		
		public static final String CONTENT_ITEM_TYPE = 
				ContentResolver.CURSOR_ITEM_BASE_TYPE + "/credential";
		
		private static final UriMatcher mUriMatcher = 
				new UriMatcher(UriMatcher.NO_MATCH);
		
		static{
			
			mUriMatcher.addURI(AUTHORITY, BASE_PATH, CREDENTIAL);
			mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CREDENTIAL_ID);
			
		}

		private static final String CREATE_TABLE_CREDENTIALS = 
				"create table "  + TABLE_CREDENTIALS + " ("
				+ ID             + " integer, "				
				+ COL_MAC		 + " text primary key,"
				+ COL_SERVICE 	 + " text not null,"
				+ COL_USER		 + " text not null,"
				+ COL_PASSWORD	 + " text not null)";
	
//		// Our DB helper class
//		private static class CredentialsDatabaseHelper extends SQLiteOpenHelper {
//
//			public CredentialsDatabaseHelper(Context context, String name, 
//					CursorFactory factory, int version) {
//				super(context, name, factory, version);		
//			}
//
//			@Override
//			public void onCreate(SQLiteDatabase db) {
//				db.execSQL(CREATE_TABLE_DEVICES);		
//			}
//
//			@Override
//			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//				Log.d(TAG, "Upgrading DB, [" + oldVersion + "] -> [" 
//						+ newVersion + "]");
//
//				db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
//				onCreate(db);	
//
//			}
//
//		}
		
		
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		//Not implemented
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		
		switch(mUriMatcher.match(uri)){		
		case CREDENTIAL:
			return "vnd.android.cursor.dir/vnd.gentoomen.credential";
		case CREDENTIAL_ID:
			return "vnd.android.cursor.item/vnd.gentoomen.credential";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);		
		}
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {
		
		long rowID = mDB.insert(TABLE_CREDENTIALS, null, values);
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
		DatabaseHelper helper 
			= new DatabaseHelper(context, DB_NAME, null, DB_VERSION, CREATE_TABLE_CREDENTIALS, TABLE_CREDENTIALS);
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
		queryBuilder.setTables(TABLE_CREDENTIALS);

		//Check if the caller has requested a nonexistant column
		checkColumns(projection);

		switch(mUriMatcher.match(uri)) {			
		case CREDENTIAL_ID: 
			queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());
		case CREDENTIAL:
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
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rowsUpdated = 0;
		
		switch(mUriMatcher.match(uri)) {
			case CREDENTIAL_ID:
				rowsUpdated = mDB.update(TABLE_CREDENTIALS, 
										 values, 
										 selection, 
										 selectionArgs);
				break;
			case CREDENTIAL:
				break;			
			default: 
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
					
		return rowsUpdated;
			
	}
	
	private void checkColumns(String[] projection) {
		
		String[] avail = {
				ID,				
				COL_MAC,
				COL_USER,
				COL_PASSWORD,
				COL_SERVICE
		};
		
		if(projection != null){
			HashSet<String> availColumns = 
					new HashSet<String>(Arrays.asList(avail));
			
			HashSet<String> reqColumns = 
					new HashSet<String>(Arrays.asList(projection));
			
			//Check if all columns that were requested are available
			if(!availColumns.containsAll(reqColumns))
				throw new IllegalArgumentException("Unknown Column in projection");
		}
		
	}

}
