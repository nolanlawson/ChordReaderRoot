package com.nolanlawson.chordreader.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nolanlawson.chordreader.util.UtilLogger;

public class ChordReaderDBHelper extends SQLiteOpenHelper {


	//logger
	private static UtilLogger log = new UtilLogger(ChordReaderDBHelper.class);
	
	// schema constants
	
	private static final String DB_NAME = "chord_reader.db";
	private static final int DB_VERSION = 1;
	
	// table constants
	private static final String TABLE = "Queries";
	private static final String COLUMN_ID = "_id";
	private static final String COLUMN_QUERY＿TEXT = "query";
	private static final String COLUMN_QUERY_TIMESTAMP = "timestamp";
	
	// private variables
	private SQLiteDatabase db;
	
	public ChordReaderDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		db = getWritableDatabase();
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String createSql = "create table %s " +
			"(" +
			"%s integer not null primary key autoincrement, " +
			"%s text not null, " +
			"%s int not null " +
			");";
		
		createSql = String.format(createSql, TABLE, COLUMN_ID, COLUMN_QUERY＿TEXT, COLUMN_QUERY_TIMESTAMP);
		
		db.execSQL(createSql);
		db.execSQL("create unique index index_query on " + TABLE + " ( " + COLUMN_QUERY＿TEXT + ")");
	}

	@Override
	public void close() {
		super.close();
		if (db != null && db.isOpen()) { // just to be safe
			db.close();
		}
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public List<String> findAllQueriesAfter(long timestamp) {
		Cursor cursor = null;
		try {
			cursor = db.query(
					TABLE, 
					new String[]{COLUMN_QUERY＿TEXT}, 
					COLUMN_QUERY_TIMESTAMP + ">" + timestamp, 
					null, null, null, 
					COLUMN_QUERY_TIMESTAMP + " desc");
			
			List<String> result = new ArrayList<String>();
			while (cursor.moveToNext()) {
				result.add(cursor.getString(0));
			}
			return result;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	public void saveQuery(String queryText) {
		
		String insertSql = "insert into " + TABLE + " (" + COLUMN_QUERY＿TEXT + ", " + COLUMN_QUERY_TIMESTAMP
			+ ") values (?," + System.currentTimeMillis() + ")";
		try {
			db.execSQL(insertSql, new String[]{queryText});
		} catch (SQLException ignore) {
			// unique index exception; we don't care
		}
		
		String updateSql = "update " + TABLE + " set " + COLUMN_QUERY_TIMESTAMP + " = " + System.currentTimeMillis()
			+ " where " + COLUMN_QUERY＿TEXT + " = ?";
		
		db.execSQL(updateSql,new String[]{queryText});
	}
}
