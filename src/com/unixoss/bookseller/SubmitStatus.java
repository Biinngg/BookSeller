package com.unixoss.bookseller;

import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.unixoss.bookseller.discuz.DiscuzLoginPost;
import com.unixoss.bookseller.discuz.Login;
import com.unixoss.bookseller.discuz.LoginHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SubmitStatus extends Activity implements Runnable {
	TextView textView;
	ProgressBar progressBar;
	ContentValues contentValues;
	int historyID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        Bundle bl = getIntent().getExtras();
        historyID = bl.getInt("historyID");
        textView = (TextView)findViewById(R.id.status_textView1);
        textView.setText(getText(R.string.status_submitting));
        progressBar = (ProgressBar)findViewById(R.id.status_progressBar1);
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public String login() throws ClientProtocolException, IOException {
    	LoginHelper lh = new LoginHelper(this);
    	Map<String,String> loginData = lh.getLoginData();
    	DatabaseHelper dh = new DatabaseHelper(this);
    	contentValues = dh.getSubmitData(historyID);
    	DiscuzLoginPost dlp = null;
    	if(loginData == null) {
    		startLogin();
    		finish();
    		return null;
    	} else {
    		dlp = new DiscuzLoginPost(this, loginData);
    	}
    	String result = dlp.loginPost(contentValues);
    	return result;
    }
    
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle b = msg.getData();
            if(b.containsKey("suc")) {
            	Toast t = Toast.makeText(SubmitStatus.this,
            			getString(R.string.login_successful), Toast.LENGTH_SHORT);
            	t.show();
            	textView.setText(b.getString("suc"));
            	progressBar.setVisibility(View.GONE);
            	Log.d("suc", b.getString("suc"));
            } else {
            	textView.setText(b.getString("fail"));
            	progressBar.setVisibility(View.GONE);
            	String fail = b.getString("fail");
            	if(fail == null) {
            		startLogin();
            		finish();
            		return;
            	}
            	Toast t = Toast.makeText(SubmitStatus.this,
            			getString(R.string.login_failed_network), Toast.LENGTH_SHORT);
            	if(fail.contains(getString(R.string.login_failed_user))) {
            		startLogin();
            		t = Toast.makeText(SubmitStatus.this,
                			getString(R.string.login_failed_user), Toast.LENGTH_SHORT);
            	}
            	t.show();
            }
            finish();
        }
    };
    
    private void startLogin() {
		Intent intent = new Intent();
		intent.setClass(SubmitStatus.this, Login.class);
		intent.putExtra("historyID", historyID);
		startActivity(intent);
    }

	@Override
	public void run() {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		try {
			String result = login();
			if(result != this.getText(R.string.login_successful)) {
				bundle.putString("fail", result);
			} else {
				bundle.putString("suc", result);
			}
		} catch (ClientProtocolException e) {
			bundle.putString("fail", e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			bundle.putString("fail", e.toString());
			e.printStackTrace();
		}
		msg.setData(bundle);
		myHandler.sendMessage(msg);
	}
}
