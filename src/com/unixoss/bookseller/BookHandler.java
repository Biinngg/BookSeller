package com.unixoss.bookseller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;

public class BookHandler {
	private Context context;
	private int historyID;
	private DatabaseHelper helper;
	private String spaFilter = "\\S";
	private String retFilter = "[^\\r]";
	private String isbn;
	
	public BookHandler(Context context, int historyID) {
		this.context = context;
		this.historyID = historyID;
	}
    
    private String toHexString(String s) {
    	String str= "";
    	for(byte b: s.getBytes()) {
    		str += "%" + Integer.toHexString(b & 0XFF);
    	}
    	return str;
    }
    
	public String fetchData(String isbn) throws ClientProtocolException, IOException {
		this.isbn = isbn;
		String url = "http://api.ibeike.com/lib.php?ISBN=" + isbn + "&page=1";
		DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        // Create a response handler
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String tmp = null;
        String htmlRet="";
        boolean mark = false;
        while((tmp=reader.readLine())!=null) {
        	if(mark)
        		htmlRet += tmp;
        	if(tmp.contains("<body>")) {
        		mark = true;
        	} else if(tmp.contains("</body>")) {
        		mark = false;
        	} else if(tmp.contains("{")) {
        		htmlRet += tmp;
        		mark = true;
        	}
        }
        htmlRet.replace("</body>", "");
        String htmlBody = new String(htmlRet.getBytes("UTF-8"), "UTF-8");
        reader.close();
        return htmlBody;
	}

	public ContentValues parseAndSave(String isbn, String _id) throws
	JSONException, ClientProtocolException, IOException {
		String string = fetchData(isbn);
		ContentValues cv;
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject = new JSONObject();
		JSONTokener jsonTokener = new JSONTokener(decodeUnicode(string).toString());
		try{
			jsonArray = (JSONArray)jsonTokener.nextValue();
		} catch(ClassCastException e) {
			e.printStackTrace();
			return null;
		}
		helper = new DatabaseHelper(context);
		jsonObject = jsonArray.getJSONObject(0);
		cv = converter(jsonObject);
		cv = parseList(cv);
		helper.saveContentValues(cv, "list", _id);
		return cv;
	}
	
	public ContentValues converter(JSONObject obj) throws JSONException {
		Iterator iterator = obj.keys();
		ContentValues cv = new ContentValues();
		while(iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = obj.getString(key);
			cv.put(key, value);
		}
		return cv;
	}

    
	public static StringBuffer decodeUnicode(String dataStr) {
		dataStr=dataStr.replace("&#x", "\\u");
		final StringBuffer buffer = new StringBuffer();
		String tempStr = "";
		String operStr = dataStr;
		if (operStr != null && operStr.indexOf("\\u") == -1)
			return buffer.append(operStr);
		if (operStr != null && !operStr.equals("")
				&& !operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, operStr.indexOf("\\u"));
			operStr = operStr.substring(operStr.indexOf("\\u"), operStr
					.length());
		}
		buffer.append(tempStr);
		while (operStr != null && !operStr.equals("")
				&& operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, 6);
			operStr = operStr.substring(7, operStr.length());
			String charStr = "";
			charStr = tempStr.substring(2, tempStr.length());
			char letter = (char) Integer.parseInt(charStr, 16);
			buffer.append(new Character(letter).toString());
			if (operStr.indexOf("\\u") == -1) {
				buffer.append(operStr);
			} else { 
				tempStr = operStr.substring(0, operStr.indexOf("\\u"));
				operStr = operStr.substring(operStr.indexOf("\\u"), operStr
						.length());
				buffer.append(tempStr);
			}
		}
		return buffer;
	}
	
	private ContentValues parseList(ContentValues cv) {
		ContentValues result = new ContentValues();
		String[] keys = new String[] { "isbn", "bookName",
				"marc_no", "author", "authority" };
		result.put(keys[0], isbn);
		result.put("historyid", historyID);
		for(int i=1; i<keys.length; i++) {
			String value = cv.getAsString(keys[i]);
			value = filter(value, spaFilter);
			value = filter(value, retFilter);
			value = value.replace("&nbsp;", " ");
			result.put(keys[i], value);
		}
		return result;
	}
	
	public String filter(String src, String filter) {
		String result = "";
        Pattern pattern = Pattern.compile(filter);
        Matcher matcher  = pattern.matcher(src);
        while (matcher.find()) {
        	result += matcher.group();
		}
        return result;
	}
}
