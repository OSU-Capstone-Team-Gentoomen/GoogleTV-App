package edu.gentoomen.conduit;

import edu.gentoomen.utilities.DatabaseUtility;
import jcifs.util.MD4;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MediaContentProvider extends ContentProvider {
	
	/*Our Database*/
	private SQLiteDatabase mDB;
	
	/*Name of our table*/
	private static final String TABLE_MEDIA = "media"; 
	
	/*Constants used to differentiate between different URIs*/
	private static final int    MEDIA = 666;
	private static final int    MEDIA_ID = 667;
	
	/*private constants*/
	private static final int    DB_VERSION = 1;
	private static final String DB_NAME = "Media.db";
	private static final String AUTHORITY = "edu.gentoomen.condiut.networking";
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
		
		mUriMatcher.addURI(AUTHORITY, BASE_PATH, MEDIA);
		mUriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MEDIA);
		
	}
	
	/*Our DB Helper Class*/
	private static class MediaDatabaseHelpder extends SQLiteOpenHelper {

		private static final String CREATE_TABLE_MEDIA =
				"create table " + TABLE_MEDIA + " ("
				+ ID            + " integer autoincrement primary key, "
				+ COL_NAME      + " text not null, "
				+ COL_PATH      + " text not null, "
				+ COL_TYPE 		+ " text not null)";
		
		public MediaDatabaseHelpder(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TABLE_MEDIA);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
			onCreate(db);
			
		}
		
	}	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int uriType = mUriMatcher.match(uri);
		int rowsRemoved = 0;
		
		switch (uriType) {
		case MEDIA:
			rowsRemoved = mDB.delete(TABLE_MEDIA, selection, selectionArgs);
			break;
		case MEDIA_ID:
			String id = uri.getLastPathSegment();
			
			if (selection.isEmpty())
				rowsRemoved = mDB.delete(TABLE_MEDIA, ID
										 + "=" + id 
										 + selection, 
										 selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsRemoved;
		
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri inuri, ContentValues values) {
		
		long rowID = mDB.insert(TABLE_MEDIA, null, values);
		if (rowID > 0) {
			Uri uri = ContentUris.withAppendedId(MEDIA_URI, rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		return null;
	}

	@Override
	public boolean onCreate() {
		
		Context context = getContext();
		context.deleteDatabase(DB_NAME);
		MediaDatabaseHelpder helper = 
				new MediaDatabaseHelpder(context, DB_NAME, null, DB_VERSION);
		
		mDB = helper.getWritableDatabase();
		
		if (mDB == null)
			return true;
		else
			return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		/*Use the query builder to create our query*/
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_MEDIA);
		
		/*Check if the query has requested a nonexistent column*/
		DatabaseUtility.checkColumns(projection, availableColumns);
		
		switch (mUriMatcher.match(uri)) {
		case MEDIA_ID:
			queryBuilder.appendWhere(ID + "=" + uri.getLastPathSegment());			
		case MEDIA:
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
		return curse;
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rowsUpdated = 0;
		
		switch (mUriMatcher.match(uri))	{
		case MEDIA_ID:
			rowsUpdated = mDB.update(TABLE_MEDIA, 
									 values, 
									 selection, 
									 selectionArgs);
		case MEDIA:
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);			
		}
		
		return rowsUpdated;
		
	}

}
