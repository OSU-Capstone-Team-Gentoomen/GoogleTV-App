package edu.gentoomen.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private String mCreateTable = null;
	private String mTableName = null;

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version, String createTable, String tableName) {
		super(context, name, factory, version);

		mCreateTable = createTable;
		mTableName = tableName;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		if (mCreateTable != null)
			db.execSQL(mCreateTable);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (mTableName != null) {
			db.execSQL("DROP TABLE IF EXISTS " + mTableName);
			onCreate(db);
		}

	}

}
