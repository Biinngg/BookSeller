package com.unixoss.bookseller.discuz;

import java.util.HashMap;
import java.util.Map;

import com.unixoss.bookseller.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LoginHelper {
	Context context;
	String seed = "su4dezax5g";
	
	public LoginHelper(Context context) {
		this.context = context;
	}
	
	public Map<String,String> getLoginData() {
		String[] columns = new String[] {"username", "passwd"};
		Map<String,String> result = new HashMap<String,String>();
		Database db = new Database(context);
		SQLiteDatabase database = db.getReadableDatabase();
		Cursor cursor = database.query("login",
				columns, null, null, null, null, "_id DESC", "1");
		if(!cursor.moveToNext()) {
			db.close();
			return null;
		}
		result.put("username", cursor.getString(0));
		try {
			result.put("passwd", AESEncryptor.decrypt(seed, cursor.getString(1)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.close();
		return result;
	}
	
	public void saveLoginData(String username, String passwd) {
		ContentValues cv = new ContentValues();
        try{
            passwd = AESEncryptor.encrypt(seed, passwd);
        }catch(Exception ex){
            ex.printStackTrace();
        }
		cv.put("username", username);
		cv.put("passwd", passwd);
		Database db = new Database(context);
		SQLiteDatabase database = db.getWritableDatabase();
		database.delete("login", null, null);
		database.insert("login", null, cv);
		db.close();
	}
}
