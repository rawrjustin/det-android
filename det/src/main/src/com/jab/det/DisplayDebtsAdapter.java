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
import com.jab.det.DTObjects.DTFriend;
import com.jab.det.DTObjects.DTUtils;

public class DisplayDebtsAdapter extends ArrayAdapter<DTFriend> {

    private Context context;
    private ArrayList<DTFriend> friends;

    public DisplayDebtsAdapter(Context context, int resource, ArrayList<DTFriend> friends) {
        super(context, resource, friends);
        this.context = context;
        this.friends = friends;
    }

    private class ViewHolder {
        ProfilePictureView profilePictureView;
        TextView friendNameText;
        TextView debtAmount;
        RelativeLayout resolveButton;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        final DTFriend currentFriend = getItem(position);
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

        holder.profilePictureView.setProfileId(currentFriend.getFriend().getFacebookId());
        holder.friendNameText.setText(currentFriend.getFriend().getName());
        holder.debtAmount.setText(DTUtils.getDisplayableDollarAmount((Number) (currentFriend.getAmountOwedToCurrentUser())));
        // holder.resolveButton.setOnLongClickListener(new View.OnLongClickListener() {
        // @Override
        // public boolean onLongClick(View v) {
        // // Update aggregate totals on home activity
        // if (currentDebt.getCreditor().equals(UserHomeActivity.getCurrentUser())) {
        // UserHomeActivity.amountOwedToOthers -= currentDebt.getAmount().doubleValue();
        // } else {
        // UserHomeActivity.amountOwedToYou -= currentDebt.getAmount().doubleValue();
        // }
        //
        // UserHomeActivity.resetAggregateTotalsDisplay();
        //
        // // Delete parse object from parse
        // currentDebt.getParseObject().deleteInBackground(new DeleteCallback() {
        // @Override
        // public void done(ParseException e) {
        // if (e != null) {
        // Log.e(DetApplication.TAG, "DETAPP Parse error: " + e.toString());
        // DetApplication.showToast(parent.getContext(), "Error deleting debt");
        // return;
        // }
        //
        // DetApplication.showToast(parent.getContext(), "Debt deleted from parse");
        //
        // // Remove debt from friend map
        // DTUser friend = DTUtils.getFriend(currentDebt);
        // DetApplication.friendToDebtsMap.get(friend).remove(currentDebt);
        // if (DetApplication.friendToDebtsMap.get(friend).isEmpty()) {
        // DetApplication.friendToDebtsMap.remove(friend);
        // }
        //
        // // Remove debt from transactions map and delete transaction if necessary
        // currentDebt.getTransaction().getDebts().remove(currentDebt);
        // if (currentDebt.getTransaction().getDebts().isEmpty()) {
        // currentDebt.getTransaction().getParseObject().deleteInBackground(new DeleteCallback() {
        // @Override
        // public void done(ParseException e) {
        // // TODO Auto-generated method stub
        // DetApplication.showToast(parent.getContext(), "Transaction deleted from parse");
        // }
        // });
        // }
        // }
        // });
        //
        // // Remove row
        // DisplayDebtsAdapter.this.debts.remove(position);
        // DisplayDebtsAdapter.this.notifyDataSetChanged();
        //
        // // If debt list is empty, show no debts text
        // if (DisplayDebtsAdapter.this.debts.isEmpty()) {
        // TextView noDebtTextView = (TextView) v.getRootView().findViewById(R.id.loading_debts);
        // noDebtTextView.setVisibility(View.VISIBLE);
        // noDebtTextView.setText(v.getResources().getString(R.string.no_debts));
        // }
        //
        // return true;
        // }
        // });

        return convertView;
    }
}
