package com.jab.det;

import com.facebook.widget.ProfilePictureView;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DisplayDebtsAdapter extends ArrayAdapter<DTDebt> {
	private Context context;
	public DisplayDebtsAdapter(Context context, int resource, DTDebt[] objects) {
		super(context, resource, objects);
		this.context = context;
	}
	
	private class ViewHolder {
		ProfilePictureView profilePictureView;
		TextView textView;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		DTDebt currentDebt = getItem(position); 
	    LayoutInflater mInflater = (LayoutInflater) context
	        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.debt_row, null);
            holder = new ViewHolder();
            holder.profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.profile_pic);
            holder.textView = (TextView) convertView.findViewById(R.id.debt_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.profilePictureView.setProfileId(currentDebt.getDebtor().getFacebookId());
        holder.textView.setText(currentDebt.toString());
	    return convertView;
	 }
}
