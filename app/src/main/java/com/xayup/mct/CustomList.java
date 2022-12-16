package com.xayup.mct;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Map;

public class CustomList extends BaseAdapter {
	List<Map<String, Integer>> list;
	Context context;
	View view;
	public CustomList(Context context, List<Map<String, Integer>> list){
		this.list = list;
		this.context = context;
	}
	@Override
	public int getCount() {
	    return list.size();
	}

	@Override
	public Object getItem(int position) {
	    return list.get(position);
	}

	@Override
	public long getItemId(int position) {
	    return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    view = LayoutInflater.from(context).inflate(R.layout.list_color_table_item, null);
		((TextView)view.findViewById(R.id.color_id)).setText(list.get(position).get("id").toString());
		((TextView)view.findViewById(R.id.color_rgb)).setText("R= " + list.get(position).get("r") + ", " +
															  "G= " + list.get(position).get("g") + ", " +
															 "B= " + list.get(position).get("b") );
		((ImageView)view.findViewById(R.id.color_view)).setBackgroundColor(Color.rgb(list.get(position).get("r"), list.get(position).get("g"),list.get(position).get("b"))) ;
	
		return view;
	}
	
}