package com.jab.det;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.jab.det.DTObjects.DTDebt;
import com.jab.det.DTObjects.DTFriend;
import com.jab.det.DTObjects.DTUtils;
import com.parse.ParseException;

public class FriendBreakdownActivity extends Activity {

    public static final String FRIEND_FB_ID = "friend_fb_id";
    public static DTFriend friend;

    private TextView homeTextView;
    private TextView personNameTextView;
    private ProfilePictureView profilePictureView;
    private TextView friendDebtDetailTextView;
    private Button resolveAllButton;

    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_breakdown);

        Intent intent = getIntent();
        String friendFbId = intent.getStringExtra(FRIEND_FB_ID);
        for (DTFriend _friend : DetApplication.friends) {
            if (_friend.getFriend().getFacebookId().equals(friendFbId)) {
                friend = _friend;
                break;
            }
        }

        if (friend == null) {
            Log.wtf(DetApplication.TAG, "Friend not found");
        }

        initPage();
    }

    private void initPage() {
        // Set up back to home button
        homeTextView = (TextView) findViewById(R.id.cancelBreakdownButton);
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set friend name
        this.personNameTextView = (TextView) findViewById(R.id.friend_breakdown_name);
        // this.personNameTextView.setText(this.friend.getFriend().getName());
        String friendName = null;
        if (friend.getAmountOwedToCurrentUser() > 0) {
            String amount = String.format("<font color='%s'>%s</font>", "#66CC99", DTUtils.getDisplayableDollarAmount((Double) friend.getAmountOwedToCurrentUser()));
            friendName = friend.getFriend().getName().split(" ")[0] + " owes you " + amount;
        } else if (friend.getAmountOwedToCurrentUser() < 0) {
            String amount = String.format("<font color='%s'>%s</font>", "#EE6267", DTUtils.getDisplayableDollarAmount((Double) friend.getAmountOwedToCurrentUser() * -1));
            friendName = "You owe " + friend.getFriend().getName().split(" ")[0] + " " + amount;
        } else {
            friendName = "You guys break even!";
        }
        this.personNameTextView.setText(Html.fromHtml(friendName));

        // Set friend picture
        this.profilePictureView = (ProfilePictureView) findViewById(R.id.friend_breakdown_profile_pic);
        this.profilePictureView.setProfileId(friend.getFriend().getFacebookId());

        // Set friend debt details
        this.friendDebtDetailTextView = (TextView) findViewById(R.id.friend_breakdown_detail);
        this.friendDebtDetailTextView.setMovementMethod(new ScrollingMovementMethod());
        StringBuilder sb = new StringBuilder();
        for (DTDebt debt : friend.getDebts()) {
            String amount = String.format("<font color='%s'>$%s</font>", debt.getCreditor().equals(UserHomeActivity.getCurrentUser()) ? "#66CC99" : "#EE6267", debt.getAmount());
            sb.append(amount + " for " + debt.getTransaction().getDescription() + " on " + DetApplication.DATE_FORMAT.format(debt.getUpdatedAt()) + "<br>");
        }
        this.friendDebtDetailTextView.setText(Html.fromHtml(sb.toString()));

        // Set up confirmation alert dialog
        setupAlert();

        // Set up resolve button
        this.resolveAllButton = (Button) findViewById(R.id.friend_breakdown_resolve_all);
        this.resolveAllButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });
    }

    private void setupAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirm");
        builder.setMessage("This will delete the debts and can't be undone. Are you sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                ProgressDialog progressDialog = ProgressDialog.show(FriendBreakdownActivity.this, "", "Saving...", true);
                try {
                    for (DTDebt debt : friend.getDebts()) {
                        debt.getParseObject().delete();
                        debt.getTransaction().removeDebt(debt);
                        if (debt.getTransaction().getDebts().isEmpty()) {
                            debt.getTransaction().getParseObject().delete();
                        }
                    }
                } catch (ParseException e) {
                    DetApplication.showToast(getBaseContext(), "There was a problem. Please try again later.");
                }

                progressDialog.dismiss();
                DetApplication.startActivity(UserHomeActivity.class, getBaseContext(), FriendBreakdownActivity.this, true);

                dialog.dismiss();
            }

        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        alert = builder.create();
    }
}
