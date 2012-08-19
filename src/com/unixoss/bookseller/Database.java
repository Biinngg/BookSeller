package com.unixoss.bookseller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "seller.db";
	
	public Database(Context ctx) {
		super(ctx, DATABASE_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE login (_id INTEGER PRIMARY KEY "+
		        "AUTOINCREMENT, username TEXT, passwd TEXT);");
		db.execSQL("CREATE TABLE list (_id INTEGER PRIMARY KEY "+
				"AUTOINCREMENT, bookName TEXT, isbn TEXT," +
				" number INTEGER, price INTEGER, marc_no TEXT," +
				" author TEXT, authority TEXT, historyid INTEGER);"); 
		db.execSQL("CREATE TABLE history (_id INTEGER PRIMARY KEY "+
				"AUTOINCREMENT, title TEXT);"); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db,
			int oldVersion, int newVersion) {
	}
}