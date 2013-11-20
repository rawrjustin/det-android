package com.jab.det;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
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
            holder.textView = (TextView) convertView.findViewById(R.id.debt_text);
            holder.resolveButton = (RelativeLayout) convertView.findViewById(R.id.grid_element);
            holder.resolveButton.setOnClickListener(new View.OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				// The current debt's object id is null only between when it was added optimistically via 
    				// serialization of the submitted transaction and when it is successfully saved
    				if (currentDebt.getObjectId() == null) {
    					return;
    				}
    				
    				// Remove row
    				DisplayDebtsAdapter.this.debts.remove(position);
    	            DisplayDebtsAdapter.this.notifyDataSetChanged();
    	            
    	            // Update aggregate total
    	            if (currentDebt.getCreditor().equals(UserHomeActivity.getCurrentUser())) {
    	            	UserHomeActivity.amountOwedToYou -= currentDebt.getAmount().doubleValue();
    	            } else {
    	            	UserHomeActivity.amountOwedToOthers -= currentDebt.getAmount().doubleValue();
    	            }
    	            UserHomeActivity.resetAggregateTotals();
    	            
    	            // If debt list is empty, show no debts text
    	            if (DisplayDebtsAdapter.this.debts.isEmpty()) {
    	            	TextView noDebtTextView = (TextView) v.getRootView().findViewById(R.id.loading_debts);
    	            	noDebtTextView.setVisibility(View.VISIBLE);
    	            	noDebtTextView.setText(v.getResources().getString(R.string.no_debts));
    	            }
    	            
    	            // Delete parse object from parse
    				currentDebt.getParseObject().deleteInBackground(new DeleteCallback() {
						@Override
						public void done(ParseException e) {
							DetApplication.showToast(parent.getContext(), "Debt deleted from parse");
							
							// Remove debt from users map
							DTUser userThatIsNotCurrentUser = currentDebt.getCreditor().equals(UserHomeActivity.getCurrentUser()) ?
									currentDebt.getDebtor() : currentDebt.getCreditor();
							UserHomeActivity.usersMap.get(userThatIsNotCurrentUser).remove(currentDebt);
							if (UserHomeActivity.usersMap.get(userThatIsNotCurrentUser).isEmpty()) {
								UserHomeActivity.usersMap.remove(userThatIsNotCurrentUser);
							}
							
							// Remove debt from transactions map and delete transaction if necessary
							UserHomeActivity.transactionsMap.get(currentDebt.getTransaction()).remove(currentDebt);
							if (UserHomeActivity.transactionsMap.get(currentDebt.getTransaction()).isEmpty()) {
								currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
									@Override
									public void done(ParseException e) {
										DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
										UserHomeActivity.transactionsMap.remove(currentDebt.getTransaction());
									}
								});
							}
							
//							ParseQuery<ParseObject> query = ParseQuery.getQuery("Debt");
//							ArrayList<String> objectIds = new ArrayList<String>();
//							for (DTDebt debt : currentDebt.getTransaction().getDebts()) {
//								objectIds.add(debt.getObjectId());
//							}
//							
//							query.whereContainedIn("objectId", objectIds);
//							try {
//								if (query.find().isEmpty()) {
//									currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
//										@Override
//										public void done(ParseException e) {
//											DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
//										}
//									});
//								}
//							} catch (ParseException e1) {
//								Log.e(DetApplication.TAG, "DETAPP ERROR: " + e1.toString());
//								e1.printStackTrace();
//							}
						}
    				});
    			}
    		});

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.profilePictureView.setProfileId(currentDebt.getDebtor().getFacebookId());
        holder.textView.setText(currentDebt.toString());
        
	    return convertView;
	 }
}
