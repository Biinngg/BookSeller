package com.unixoss.bookseller;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Status extends Activity implements Runnable {
	private String isbn;
	private String _id;
	private int historyID;
	private BlockingQueue<String> queue;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        
        getBundle();
        
        queue = new LinkedBlockingQueue<String>();
        ProgressBar pb = (ProgressBar)findViewById(R.id.status_progressBar1);
        TextView tv = (TextView)findViewById(R.id.status_textView1);
        Thread thread = new Thread(this);
        thread.start();
        String msg = fetch();
        pb.setVisibility(View.GONE);
        tv.setText(msg);
        finish();
    }
    
    private void getBundle() {
    	Bundle bl = this.getIntent().getExtras();
    	_id = bl.getString("_id");
    	isbn = bl.getString("isbn");
    	historyID = bl.getInt("historyID");
    }
    
    private String fetch() {
        String msg = "";
        try {
			msg = queue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        if(msg.equals(".")) {
			Toast toast = Toast.makeText(this, R.string.status_null, Toast.LENGTH_SHORT);
			toast.show();
        	return getResources().getString(R.string.status_null);
        }
        else
        	return msg;
    }

	@Override
	public void run() {
		BookHandler bh = new BookHandler(this, historyID);
		ContentValues cv = new ContentValues();
		try {
			cv = bh.parseAndSave(isbn, _id);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(cv == null)
				queue.put(".");
			else {
				String name = cv.getAsString("bookName");
				if(name != null)
					queue.put(name);
				else {
					queue.put(".");
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
