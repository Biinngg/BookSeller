package com.unixoss.bookseller;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Edit extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
    	final Bundle bl = getIntent().getExtras();
    	String title = bl.getString("title");
    	String number = bl.getString("number");
    	String price = bl.getString("price");
    	String author = bl.getString("author");
    	String authority = bl.getString("authority");
    	Log.d("bundle", title + " " + number + " " + price + " " + author + " " + authority);
    	final String historyID = bl.getString("historyID");
    	final String _id = bl.getString("_id");
    	final EditText editTitle = (EditText)findViewById(R.id.edit_editText1);
    	editTitle.setText(title);
    	final EditText editAuthor = (EditText)findViewById(R.id.edit_editText2);
    	editAuthor.setText(author);
    	final EditText editPress = (EditText)findViewById(R.id.edit_editText3);
    	editPress.setText(authority);
    	final EditText editNumber = (EditText)findViewById(R.id.edit_editText4);
    	editNumber.setText(number);
    	final EditText editPrice = (EditText)findViewById(R.id.edit_editText5);
    	editPrice.setText(price);
    	Button save = (Button)findViewById(R.id.edit_button2);
    	save.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			DatabaseHelper helper = new DatabaseHelper(Edit.this);
    			Map<String,String> cv = new HashMap<String,String>();
    			cv.put("bookName", editTitle.getText().toString());
    			cv.put("author", editAuthor.getText().toString());
    			cv.put("authority", editPress.getText().toString());
    			cv.put("number", editNumber.getText().toString());
    			cv.put("price", editPrice.getText().toString());
    			cv.put("historyid", historyID);
    			helper.updateContentValues(cv, "list", _id);
    			finish();
    		}
    	});
    	Button cancel = (Button)findViewById(R.id.edit_button3);
    	cancel.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View v) {
    			finish();
    		}
    	});
    }
    
}
