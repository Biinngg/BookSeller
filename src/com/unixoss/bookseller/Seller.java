package com.unixoss.bookseller;

import com.google.zxing.client.android.CaptureActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Seller extends Activity {
	ListAdapter adapter;
	int historyID;
	ListView listView;
	EditText editTitle;
	DatabaseHelper database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller);

		database = new DatabaseHelper(this); 
		String[] history = database.getHistory();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, history);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Spinner spinner = (Spinner) findViewById(R.id.seller_spinner1);
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(history.length - 1);
        spinner.setOnItemSelectedListener(new MyOnSpinnerSelectedListener());
        
        Button buttonAdd = (Button)findViewById(R.id.seller_button1);
        Button buttonSub = (Button)findViewById(R.id.seller_button2);
        Button buttonSca = (Button)findViewById(R.id.seller_button3);
        buttonAdd.setOnClickListener(new OnClickListener(){
    		@Override
    		public void onClick(View v) {
    			Bundle bl = new Bundle();
    			bl.putString("title", "");
    			bl.putString("number", "");
    			bl.putString("price", "");
    			bl.putString("historyID", "" + historyID);
    			bl.putString("_id", "0");
    			Intent intent = new Intent();
    			intent.setClass(Seller.this, Edit.class);
    			intent.putExtras(bl);
    			startActivity(intent);
    		}
        });
        editTitle = (EditText)findViewById(R.id.seller_editText1);
        buttonSub.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String title = editTitle.getText().toString();
				if(title.length() != 0) {
					database.saveHistory(historyID, title);
					submit();
				}
			}
        });
        
        buttonSca.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(Seller.this, CaptureActivity.class);
				intent.putExtra("_id", "0");
				intent.putExtra("historyID", historyID);
				startActivity(intent);
			}
        });

        historyID = getHistoryID();
        adapter = new ListAdapter(this, historyID);
        listView = (ListView)findViewById(R.id.seller_listView1);
        listView.setAdapter(adapter);
    }
    
    public class MyOnSpinnerSelectedListener implements OnItemSelectedListener {

        @Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        	historyID = pos + 1;
            adapter = new ListAdapter(Seller.this, historyID);
            listView.setAdapter(adapter);
            editTitle.setText(database.getHistory(historyID));
        }

        @Override
		public void onNothingSelected(AdapterView parent) {
          // Do nothing.
        }
    }
    
    private int getHistoryID() {
    	Bundle bl = this.getIntent().getExtras();
    	int historyID = 0;
    	try {
    		historyID = bl.getInt("historyID");
    	} catch(NullPointerException e) {
    		DatabaseHelper dh = new DatabaseHelper(this);
    		historyID = dh.getCurrentID();
    	}
    	return historyID;
    }
    
    public void submit() {
		Intent i = new Intent();
		i.putExtra("historyID", historyID);
		i.setClass(this, SubmitStatus.class);
		startActivity(i);
    }
    
    @Override
	public void onResume() {
    	super.onResume();
    	adapter.notifyDataSetChanged();
    }
    
    @Override
	public void onRestart() {
    	super.onRestart();
    	adapter.notifyDataSetChanged();
    }

/***********************The menu inflater****************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.activity_main, menu);
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_seller_about:
			Intent intent = new Intent();
			intent.setClass(Seller.this, About.class);
			startActivity(intent);
    		break;
    	case R.id.menu_seller_quit:
    		finish();
    		break;
    	}
		return true;
    }
}
