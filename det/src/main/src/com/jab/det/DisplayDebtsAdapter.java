package com.jab.det;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    public DisplayDebtsAdapter(Context context, int resource, ArrayList<DTFriend> friends) {
        super(context, resource, friends);
        this.context = context;
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
        holder.resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFriendPictureClicked(currentFriend);
            }
        });

        return convertView;
    }

    private void onFriendPictureClicked(DTFriend friend) {
        Intent intent = new Intent(this.context, FriendBreakdownActivity.class);
        intent.putExtra(FriendBreakdownActivity.FRIEND_FB_ID, friend.getFriend().getFacebookId());
        this.context.startActivity(intent);
    }
}
