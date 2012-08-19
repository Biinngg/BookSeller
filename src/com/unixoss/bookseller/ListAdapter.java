package com.unixoss.bookseller;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater mInflater;
	private DatabaseHelper helper;
	private String table = "list";
	private int historyID;

	public ListAdapter(Context context, int historyID) {
		this(context);
		this.historyID = historyID;
		helper.updateCursor(table, historyID);
	}
	
	public ListAdapter(Context context) {
		this.context = context;
		helper = new DatabaseHelper(context);
		this.historyID = helper.getCurrentID();
		mInflater = LayoutInflater.from(context);
		helper.updateCursor(table, historyID);
	}
	
	@Override
	public void notifyDataSetChanged() {
		helper.updateCursor(table, historyID);
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return helper.getCount(table, historyID);
	}

	@Override
	public String[] getItem(int arg0) {
		return helper.getData(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addItem() {
		ContentValues cv = new ContentValues();
		cv.put("historyid", historyID);
		helper.saveContentValues(cv, table, "0");
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View v = convertView;
		if((v == null) || (v.getTag() == null)) {
			v = mInflater.inflate(R.layout.row, null);
			holder = new ViewHolder();
			holder.id = (TextView)v.findViewById(R.id.row_textView1);
			holder.title = (TextView)v.findViewById(R.id.row_textView2);
			holder.number = (TextView)v.findViewById(R.id.row_textView4);
			holder.price = (TextView)v.findViewById(R.id.row_textView6);
			holder.button = (Button)v.findViewById(R.id.row_button1);
			holder.exit = (ImageButton)v.findViewById(R.id.row_imageButton1);
			v.setTag(holder);
		} else {
			holder = (ViewHolder)v.getTag();
		}
		holder.listItem = getItem(position);
		holder.button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
			    Intent intent = new Intent();
			    intent.putExtra("title", holder.listItem[1]);
			    intent.putExtra("number", holder.listItem[3]);
			    intent.putExtra("price", holder.listItem[4]);
			    intent.putExtra("author", holder.listItem[6]);
			    intent.putExtra("authority", holder.listItem[7]);
			    intent.putExtra("historyID", historyID);
			    intent.putExtra("_id", holder.listItem[0]);
			    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			    intent.setClass(context,Edit.class);
			    context.startActivity(intent);
			}
		});
		holder.exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatabaseHelper helper = new DatabaseHelper(context);
				helper.delete(holder.listItem[0]);
				notifyDataSetChanged();
			}
		});
		holder.id.setText((position+1) + "");
		holder.title.setText(holder.listItem[1]);
		holder.number.setText(holder.listItem[3]);
		holder.price.setText(holder.listItem[4]);
		v.setTag(holder);
		return v;
	}
	
	class ViewHolder {
		String[] listItem;
		TextView id;
		TextView title;
		TextView number;
		TextView price;
		Button button;
		ImageButton exit;
	}
}