package com.unixoss.bookseller;

import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseHelper {
	private Context context;
	private Cursor comCursor;

	public DatabaseHelper(Context context) {
		this.context = context;
	}
    
    public Cursor updateCursor(String table, int historyID) {
    	String where = "historyid=" + historyID;
    	Database db = new Database(context);
    	SQLiteDatabase database = db.getReadableDatabase();
    	comCursor = database.query(table, null, where,
    			null, null, null, "_id DESC");
    	comCursor.getCount();
    	db.close();
    	return comCursor;
    }
    
    public String[] getData(int position) {
    	comCursor.moveToPosition(position);
    	int size = comCursor.getColumnCount();
    	String[] data = new String[size];
    	for(int i=0;i<size;i++) {
    		String value = comCursor.getString(i);
    		data[i] = value;
    	}
    	return data;
    }
    
    public void delete(String _id) {
    	Database db = new Database(context);
    	SQLiteDatabase database = db.getWritableDatabase();
    	database.delete("list", "_id=" + _id, null);
    	db.close();
    }
    
    public ContentValues getSubmitData(int historyID) throws NullPointerException {
    	ContentValues cv = new ContentValues();
    	Database db = new Database(context);
    	SQLiteDatabase database = db.getReadableDatabase();
    	String selection = "_id=" + historyID;
    	Cursor subCursor = database.query("history", null, selection, null, null, null, null);
    	if(subCursor.moveToNext()) {
    		cv.put("title", subCursor.getString(1));
    	}
    	selection = "historyid=" + historyID;
    	subCursor = database.query("list", null, selection, null, null, null, null);
    	String content = "";
    	while(subCursor.moveToNext()) {
    		content += contentBuilder(subCursor.getString(5), subCursor.getString(1), subCursor.getString(3),
    				subCursor.getString(4), subCursor.getString(6), subCursor.getString(7));
    	}
    	content += "-----------------------------\n" +
    			"Submitted by [url=http://unixoss.com/bs]BookSeller[/url].";
    	cv.put("content", content);
    	db.close();
    	return cv;
    }
    
    private String contentBuilder(String marc_no, String bookName, String number,
    		String price, String author, String press) {
    	String result = "";
    	if(bookName != null) {
    		String link = null;
    		if(marc_no != null && marc_no.length() != 0) {
    			link = "http://lib.ustb.edu.cn:8080/opac/item.php?marc_no=";
    			link += marc_no;
    		}
    		result += context.getText(R.string.edit_title).toString();
    		result += parser(bookName, link);
	    	if(number != null && number.length() != 0) {
	    		result += context.getText(R.string.row_number);
	    		result += number + "  ";
	    	}
	    	if(price != null && price.length() != 0) {
	    		result += context.getText(R.string.row_price);
	    		result += price + context.getText(R.string.content_price);
	    	}
	    	if(author != null && author.length() != 0) {
	    		result += "\n" + context.getText(R.string.edit_author);
	    		result += author;
	    	}
	    	if(press != null && press.length() != 0) {
	    		result += "\n" + context.getText(R.string.edit_press);
	    		result += press;
	    	}
	    	result += "\n\n";
    	}
    	return result;
    }
    
    public String parser(String src, String link) {
    	int length = src.length();
    	src = "[b]" + src + "[/b]";
    	if(link != null)
    		src = "[url=" + link + "]" + src + "[/url]";
    	int left = 65 - 10 - length;//每行65个字符，数量与价格占10个字符。
    	while(left-- > 0) {
    		src += " ";
    	}
    	return src;
    }
    
    public int getCount(String table, int historyID) {
    	String where = "historyid=" + historyID;
    	Database db = new Database(context);
    	SQLiteDatabase database = db.getReadableDatabase();
    	Cursor cursor = database.query(table, null,
    			where, null, null, null, null);
    	int num = cursor.getCount();
    	db.close();
    	return num;
    }
    
    public int getCurrentID() {
    	String[] columns = new String[]{"_id"};
    	Database db = new Database(context);
    	SQLiteDatabase database = db.getReadableDatabase();
    	Cursor cursor = database.query("history",
    			columns, null, null, null, null, "_id DESC", "1");
    	if(cursor.moveToNext()) {
    		int id = cursor.getInt(0) + 1;
    		db.close();
    		return id;
    	}
    	else {
    		db.close();
    		return 1;
    	}
    }
	
	public void saveContentValues(ContentValues cv, String table, String _id) {
		int id = Integer.parseInt(_id);
		Database db = new Database(context);
		SQLiteDatabase database = db.getWritableDatabase();
		if(id != 0) {
			String where = "_id=" + _id;
			database.delete(table, where, null);
		}
		String[] columns = new String[]{"number"};
		String where = "historyid=" + cv.getAsString("historyid")
				+ " and isbn=" + cv.getAsString("isbn");
		Cursor cursorout = database.query(table, columns, where, null, null, null, null);
		if(!cursorout.moveToNext())
		{
			if(!cv.containsKey("number"))
				cv.put("number", 1);
			database.insert(table, null, cv);
		}
		else
		{
			int num = cursorout.getInt(0);
			ContentValues values = new ContentValues();
			values.put("number", ++num);
			database.update(table, values, where, null);
		}
		db.close();
	}
	
	public void updateContentValues(Map<String,String> contentValues, String table, String _id) {
		ContentValues cv = new ContentValues();
		for(String key : contentValues.keySet()) {
			String value = contentValues.get(key);
			if(value != null) {
				cv.put(key, value);
			}
		}
		String where = "_id=" + _id;
		Database db = new Database(context);
		SQLiteDatabase database = db.getWritableDatabase();
		int row = database.update(table, cv, where, null);
		Log.d("update", where + " " + row);
		db.close();
		if(row == 0)
			saveContentValues(cv, table, _id);
	}
	
	public void saveHistory(int historyID, String title) {
		Database db = new Database(context);
		SQLiteDatabase database = db.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("title", title);
		int row = database.update("history", values, "_id="+historyID, null);
		if(row == 0)
			database.insert("history", null, values);
		db.close();
	}

	public String[] getHistory() {
		Database db = new Database(context);
		SQLiteDatabase database = db.getReadableDatabase();
		Cursor cursor = database.query("history", null, null, null, null, null, null);
		int length = cursor.getCount() + 1;
		String[] result = new String[length];
		for(int i=0; cursor.moveToNext(); i++)
			result[i] = cursor.getString(0) + ". " + cursor.getString(1);
		db.close();
		result[length - 1] = context.getString(R.string.seller_spinner_last);
		return result;
	}
	public String getHistory(int id) {
		String result = null;
		Database db = new Database(context);
		SQLiteDatabase database = db.getReadableDatabase();
		Cursor cursor = database.query("history", null, "_id="+id, null, null, null, null);
		if(cursor.moveToNext())
			result = cursor.getString(1);
		db.close();
		return result;
	}
}
