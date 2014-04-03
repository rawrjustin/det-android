package com.jab.det;

import java.util.ArrayList;

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
import com.jab.det.DTObjects.DTDebt;
import com.jab.det.DTObjects.DTUser;
import com.jab.det.DTObjects.DTUtils;
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
        TextView friendNameText;
        TextView debtAmount;
        RelativeLayout resolveButton;
    }

    public void addToView(ArrayList<DTDebt> debts) {
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
            holder.friendNameText = (TextView) convertView.findViewById(R.id.friend_name);
            holder.debtAmount = (TextView) convertView.findViewById(R.id.amount);
            holder.resolveButton = (RelativeLayout) convertView.findViewById(R.id.grid_element);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.profilePictureView.setProfileId(currentDebt.getDebtor().getFacebookId());
        holder.friendNameText.setText(currentDebt.getDebtor().getName());
        holder.debtAmount.setText(currentDebt.getAmountToString());
        holder.resolveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Update aggregate totals on home activity
                if (currentDebt.getCreditor().equals(UserHomeActivity.getCurrentUser())) {
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

                        // Remove debt from friend map
                        DTUser friend = DTUtils.getFriend(currentDebt);
                        DetApplication.friendToDebtsMap.get(friend).remove(currentDebt);
                        if (DetApplication.friendToDebtsMap.get(friend).isEmpty()) {
                            DetApplication.friendToDebtsMap.remove(friend);
                        }

                        // Remove debt from transactions map and delete transaction if necessary
                        currentDebt.getTransaction().getDebts().remove(currentDebt);
                        if (currentDebt.getTransaction().getDebts().isEmpty()) {
                            currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    // TODO Auto-generated method stub
                                    DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
                                }
                            });
                        }
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

                return true;
            }
        });

        return convertView;
    }

    public ArrayList<DTDebt> getDebts() {
        return this.debts;
    }
}
