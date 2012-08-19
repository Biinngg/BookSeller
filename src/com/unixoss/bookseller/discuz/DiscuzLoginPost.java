package com.unixoss.bookseller.discuz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.unixoss.bookseller.R;

import android.content.ContentValues;
import android.content.Context;

public class DiscuzLoginPost {
	private Context context;
	private static List<NameValuePair> nvps;
	private String htmlBody;
	
	
	public DiscuzLoginPost(Context context, Map<String,String> loginData) {
		this.context = context;
		nvps = new ArrayList<NameValuePair>();
		loginDataHandler(loginData);
	}
	
	private void loginDataHandler(Map<String,String> loginData) {
		String username = "";
		String passwd = "";
		MD5Encryptor md5 = new MD5Encryptor();
		if(loginData != null) {
			username = decodeUnicode(loginData.get("username"));
			passwd = md5.MD5(loginData.get("passwd"));
		}
		nvps.clear();
		nvps.add(new BasicNameValuePair("loginfield","username"));
		nvps.add(new BasicNameValuePair("username",username));
		nvps.add(new BasicNameValuePair("password",passwd));
		nvps.add(new BasicNameValuePair("questionid","0"));
		nvps.add(new BasicNameValuePair("loginsubmit","true"));
		nvps.add(new BasicNameValuePair("cookietime","2592000"));
	}
	
	private void postDataHandler(ContentValues postData, String formhash) {
		long time = System.currentTimeMillis();
		nvps.clear();
		nvps.add(new BasicNameValuePair("msgto%5B%5D",""));
		nvps.add(new BasicNameValuePair("formhash",formhash));
		nvps.add(new BasicNameValuePair("posttime",time + ""));
		nvps.add(new BasicNameValuePair("wysiwyg","1"));
		nvps.add(new BasicNameValuePair("iconid","21"));
		nvps.add(new BasicNameValuePair("subject",
				decodeUnicode(postData.getAsString("title"))));
		nvps.add(new BasicNameValuePair("typeid","101"));
		nvps.add(new BasicNameValuePair("message",
				decodeUnicode(postData.getAsString("content"))));
		nvps.add(new BasicNameValuePair("attention_add","1"));
		nvps.add(new BasicNameValuePair("usesig","1"));
	}
	
    public String loginPost(ContentValues postData) throws ClientProtocolException, IOException {
    	//String baseUrl = "http://city.ibeike.com/logging.php?action=login";
    	String loginUrl = "http://city.ibeike.com/logging.php?" +
    			"action=login&loginsubmit=yes&inajax=1";
    	String getUrl = "http://city.ibeike.com/post.php?" +
    			"action=newthread&fid=32";
    	String postUrl = "http://city.ibeike.com/post.php?" +
    			"action=newthread&fid=32&extra=&topicsubmit=yes";
    	int[] statusCode = new int[3];
        DefaultHttpClient httpclient = new DefaultHttpClient();
        
        HttpPost httpost = new HttpPost(loginUrl);
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        HttpResponse response = httpclient.execute(httpost);
        statusCode[0] = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
        
        HttpGet httpget = new HttpGet(getUrl);
        response = httpclient.execute(httpget);
        statusCode[1] = response.getStatusLine().getStatusCode();
        entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        String formhash;
        try {
        	formhash = getFormhash(inputStream);
        } catch(RuntimeException e) {
        	return e.toString();
        }

        postDataHandler(postData, formhash);
        httpost = new HttpPost(postUrl);
        httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        response = httpclient.execute(httpost);
        statusCode[2] = response.getStatusLine().getStatusCode();
        
        for(int i=0; i<3; i++) {
        	if(statusCode[i] != 200)
        		return context.getString(R.string.login_failed_network);
        }
        return context.getString(R.string.login_successful);
    }
    
    public String getFormhash(InputStream inputStream) throws RuntimeException {
    	String re = ".*name=\"formhash\".*value=\"(\\w*)\".*";
    	Pattern pattern = Pattern.compile(re);
    	BufferedReader reader = new BufferedReader(
    			new InputStreamReader(inputStream));
    	Matcher matcher = null;
    	do {
    		String line;
			try {
				line = reader.readLine();
	    		matcher = pattern.matcher(line);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			if(line!=null && line.contains(context.getString(R.string.login_nologin))) {
				throw new RuntimeException(context.getString(R.string.login_failed_user));
			}
    	} while(!matcher.find());
    	return matcher.group(1);
    }
    
	public String decodeUnicode(String dataStr) {
		dataStr=dataStr.replace("&#x", "\\u");
		final StringBuffer buffer = new StringBuffer();
		String tempStr = "";
		String operStr = dataStr;
		if (operStr != null && operStr.indexOf("\\u") == -1)
			return buffer.append(operStr).toString();
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
		return buffer.toString();
	}
}