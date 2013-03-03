package edu.gentoomen.conduit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class AuthenticationContentProvider extends ContentProvider {

	//Our database
		private static SQLiteDatabase mDB;
		
		//Name of our table
		public static final String  TABLE_DEVICES = "credentials";
		
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
	
		
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
