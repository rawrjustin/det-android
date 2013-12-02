package com.jab.det;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.parse.DeleteCallback;
import com.parse.ParseException;

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
		TextView friendNameText;
		TextView debtAmount;
		RelativeLayout resolveButton;
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
//            holder.textView = (TextView) convertView.findViewById(R.id.debt_text);
            holder.friendNameText = (TextView) convertView.findViewById(R.id.friend_name);
            holder.debtAmount = (TextView) convertView.findViewById(R.id.amount);
            holder.resolveButton = (RelativeLayout) convertView.findViewById(R.id.grid_element);
           
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.profilePictureView.setProfileId(currentDebt.getDebtor().getFacebookId());
//        holder.textView.setText(currentDebt.toString());
        holder.friendNameText.setText(currentDebt.getDebtor().getName());
        holder.debtAmount.setText(currentDebt.getAmountToString());
        holder.resolveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// The current debt's object id is null only between when it was added optimistically via 
				// serialization of the submitted transaction and when it is successfully saved
				
				Log.d(DetApplication.TAG, "DEBUG, debt to delete: " + currentDebt.toString());
				
				// Debt has not been saved to parse yet, so return without doing anything
				if (currentDebt.getObjectId() == null) {
					return;
				}
				
				
	            // Update aggregate totals on home activity
	            if (UserHomeActivity.getCurrentUser().equals(currentDebt.getOtherUser())) {
	            	UserHomeActivity.amountOwedToOthers -= currentDebt.getAmount().doubleValue();
	            } else {
	            	UserHomeActivity.amountOwedToYou -= currentDebt.getAmount().doubleValue();
	            }
	            UserHomeActivity.resetAggregateTotalsDisplay();
	            	            
	            // Delete parse object from parse
				currentDebt.getParseObject().deleteInBackground(new DeleteCallback() {
					@Override
					public void done(ParseException e) {
						if (e != null) {
							Log.e(DetApplication.TAG, "DETAPP Parse error: " + e.toString());
							DetApplication.showToast(parent.getContext(), "Error deleting debt");
							return;
						}
						
						DetApplication.showToast(parent.getContext(), "Debt deleted from parse");
						
						// Remove debt from users map
						Log.d(DetApplication.TAG, "DEBUG: usersMap before removing debt: " + UserHomeActivity.usersMap);
						UserHomeActivity.usersMap.get(currentDebt.getOtherUser()).remove(currentDebt);
						if (UserHomeActivity.usersMap.get(currentDebt.getOtherUser()).isEmpty()) {
							UserHomeActivity.usersMap.remove(currentDebt.getOtherUser());
						}
						
						Log.d(DetApplication.TAG, "DEBUG: usersMap after removing debt: " + UserHomeActivity.usersMap);

						Log.d(DetApplication.TAG, "DEBUG: transactionsMap before removing debt: " + UserHomeActivity.transactionsMap);

						// Remove debt from transactions map and delete transaction if necessary
						UserHomeActivity.transactionsMap.get(currentDebt.getTransaction()).remove(currentDebt);
						if (UserHomeActivity.transactionsMap.get(currentDebt.getTransaction()).isEmpty()) {
							UserHomeActivity.transactionsMap.remove(currentDebt.getTransaction());
							currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
								@Override
								public void done(ParseException e) {
									if (e != null) {
										Log.e(DetApplication.TAG, "DETAPP Parse Error: " + e.toString());
										return;
									}
									
									DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
									UserHomeActivity.transactionsMap.remove(currentDebt.getTransaction());
								}
							});
						}
						
						currentDebt.getTransaction().removeDebt(currentDebt);
						Log.d(DetApplication.TAG, "DEBUG: transactionsMap after removing debt: " + UserHomeActivity.transactionsMap);
					}
				});
				
				// Remove row
				DisplayDebtsAdapter.this.debts.remove(position);
	            DisplayDebtsAdapter.this.notifyDataSetChanged();
	            
	            // If debt list is empty, show no debts text
	            if (DisplayDebtsAdapter.this.debts.isEmpty()) {
	            	TextView noDebtTextView = (TextView) v.getRootView().findViewById(R.id.loading_debts);
	            	noDebtTextView.setVisibility(View.VISIBLE);
	            	noDebtTextView.setText(v.getResources().getString(R.string.no_debts));
	            }
			}
		});
        
	    return convertView;
	 }
	
	public ArrayList<DTDebt> getDebts() {
		return this.debts;
	}
}
