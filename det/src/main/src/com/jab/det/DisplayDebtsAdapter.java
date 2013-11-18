package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.facebook.widget.ProfilePictureView;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DisplayDebtsAdapter extends ArrayAdapter<DTDebt> {
	
	private Context context;
	private ArrayList<DTDebt> debts;
	public DisplayDebtsAdapter(Context context, int resource, ArrayList<DTDebt> debts) {
		super(context, resource, debts);
		this.context = context;
		this.debts = debts;
	}
	
	private class ViewHolder {
		ProfilePictureView profilePictureView;
		TextView textView;
		Button resolveButton;
	}
	
	public void addToView(ArrayList<DTDebt> debts){
		this.debts.addAll(debts);
		this.notifyDataSetChanged();
	}
	
	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder = null;
		final DTDebt currentDebt = getItem(position); 
	    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.debt_row, null);
            holder = new ViewHolder();
            holder.profilePictureView = (ProfilePictureView) convertView.findViewById(R.id.profile_pic);
            holder.textView = (TextView) convertView.findViewById(R.id.debt_text);
            //holder.resolveButton = (Button) convertView.findViewById(R.id.resolve_debt_button);
            //holder.resolveButton.setOnClickListener(new View.OnClickListener() {
    		//	@Override
    		//	public void onClick(View v) {
    		//		// Remove row
    		//		DisplayDebtsAdapter.this.debts.remove(position);
    	    //        DisplayDebtsAdapter.this.notifyDataSetChanged();
    	    //        
    	    //        // If debt list is empty, show no debts text
    	    //        if (DisplayDebtsAdapter.this.debts.isEmpty()) {
    	    //        	TextView noDebtTextView = (TextView) v.getRootView().findViewById(R.id.loading_debts);
    	    //        	noDebtTextView.setVisibility(View.VISIBLE);
    	    //        	noDebtTextView.setText(v.getResources().getString(R.string.no_debts));
    	    //        }
    	    //        
    	    //        // Delete parse object from parse
    		//		currentDebt.getParseObject().deleteInBackground(new DeleteCallback() {
			//			@Override
			//			public void done(ParseException e) {
			//				ParseQuery<ParseObject> query = ParseQuery.getQuery("Debt");
			//				ArrayList<String> objectIds = new ArrayList<String>();
			//				for (DTDebt debt : currentDebt.getTransaction().getDebts()) {
			//					objectIds.add(debt.getObjectId());
			//				}
			//				
			//				query.whereContainedIn("objectId", objectIds);
			//				try {
			//					if (query.find().isEmpty()) {
			//						currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
			//							@Override
			//							public void done(ParseException e) {
			//								DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
			//								//Toast.makeText(parent.getContext(), , Toast.LENGTH_SHORT).show();
			//							}
			//						});
			//					}
			//				} catch (ParseException e1) {
			//					// TODO Auto-generated catch block
			//					e1.printStackTrace();
			//				}
			//				
			//				DetApplication.showToast(parent.getContext(), "Debt deleted from parse");
			//				//Toast.makeText(parent.getContext(), "Debt deleted from parse", Toast.LENGTH_SHORT).show();
			//			}
    		//		});
    		//	}
    		//});

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.profilePictureView.setProfileId(currentDebt.getDebtor().getFacebookId());
        holder.textView.setText(currentDebt.toString());
        
	    return convertView;
	 }
}
