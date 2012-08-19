package com.unixoss.bookseller.discuz;

import java.util.Map;

import com.unixoss.bookseller.R;
import com.unixoss.bookseller.SubmitStatus;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.content.Intent;

public class Login extends Activity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        final Bundle bl = getIntent().getExtras();
        final EditText editUser = (EditText)findViewById(R.id.login_editText1);
        final EditText editPass = (EditText)findViewById(R.id.login_editText2);
        final LoginHelper lh = new LoginHelper(this);
        Map<String,String> loginData = lh.getLoginData();
        if(loginData != null) {
        	editUser.setText(loginData.get("username"));
        	editPass.setText(loginData.get("passwd"));
        }
        Button button = (Button)findViewById(R.id.login_button1);
        button.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		String username = editUser.getText().toString();
        		String passwd = editPass.getText().toString();
        		if(username!=null && passwd!=null) {
        			lh.saveLoginData(username, passwd);
        			Intent intent = new Intent();
        			intent.setClass(Login.this, SubmitStatus.class);
        			intent.putExtras(bl);
        			startActivity(intent);
        			finish();
        		}
        	}
        });
    }
}
