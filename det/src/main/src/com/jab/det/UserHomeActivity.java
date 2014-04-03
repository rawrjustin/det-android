package com.jab.det;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jab.det.DTObjects.DTUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class UserHomeActivity extends Activity {

    private TextView logoutButton;
    private TextView refreshButton;
    private TextView addTransactionButton;
    private static TextView amountOwedToYouTextView;
    private static TextView amountYouOweTextView;
    private static TextView amountBalanceTextView;
    private static View aggregate_graph_owed_to_you_ratio1;
    private static View aggregate_graph_owed_to_you_ratio2;
    private static View aggregate_graph_you_owe_ratio1;
    private static View aggregate_graph_you_owe_ratio2;
    private LoadDebtsDataAsync loadDebtsData;
    public static double amountOwedToOthers;
    public static double amountOwedToYou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        UserHomeActivity.amountOwedToYouTextView = (TextView) findViewById(R.id.aggregate_graph_owed_to_you_amount);
        UserHomeActivity.amountYouOweTextView = (TextView) findViewById(R.id.aggregate_graph_you_owe_amount);
        UserHomeActivity.amountBalanceTextView = (TextView) findViewById(R.id.aggregate_graph_total_balance_amount);
        UserHomeActivity.aggregate_graph_owed_to_you_ratio1 = (View) findViewById(R.id.aggregate_graph_owed_to_you_ratio1);
        UserHomeActivity.aggregate_graph_owed_to_you_ratio2 = (View) findViewById(R.id.aggregate_graph_owed_to_you_ratio2);
        UserHomeActivity.aggregate_graph_you_owe_ratio1 = (View) findViewById(R.id.aggregate_graph_you_owe_ratio1);
        UserHomeActivity.aggregate_graph_you_owe_ratio2 = (View) findViewById(R.id.aggregate_graph_you_owe_ratio2);
        setCurrentUser();
        setupAddTransactionButton();
        setupRefreshButton();
        setupLogoutButton();
        displayDebts();
    }

    public static void resetAggregateTotalsDisplay() {
        amountOwedToOthers = Math.round(amountOwedToOthers * 100.0) / 100.0;
        amountOwedToYou = Math.round(amountOwedToYou * 100.0) / 100.0;
        double balance = Math.round((amountOwedToYou - amountOwedToOthers) * 100.0) / 100.0;

        UserHomeActivity.amountOwedToYouTextView.setText(DetApplication.formatAsDollarAmount(amountOwedToYou));
        UserHomeActivity.amountYouOweTextView.setText(DetApplication.formatAsDollarAmount(amountOwedToOthers));
        UserHomeActivity.amountBalanceTextView.setText(DetApplication.formatAsDollarAmount(balance));

        if (balance < 0) {
            UserHomeActivity.amountBalanceTextView.setTextColor(Color.rgb(238, 98, 103));
        } else {
            UserHomeActivity.amountBalanceTextView.setTextColor(Color.rgb(102, 204, 153));
        }

        float owedToYouRatio1 = (float) ((float) amountOwedToOthers / (amountOwedToYou + amountOwedToOthers));
        float owedToYouRatio2 = (float) ((float) amountOwedToYou / (amountOwedToYou + amountOwedToOthers));
        float youOweRatio1 = (float) ((float) amountOwedToYou / (amountOwedToYou + amountOwedToOthers));
        float youOweRatio2 = (float) ((float) amountOwedToOthers / (amountOwedToYou + amountOwedToOthers));

        if (amountOwedToYou == 0 && amountOwedToOthers == 0) {
            owedToYouRatio1 = 1;
            owedToYouRatio2 = 0;
            youOweRatio1 = 1;
            youOweRatio2 = 0;
        }
        // Set owed to you graph
        UserHomeActivity.aggregate_graph_owed_to_you_ratio1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, owedToYouRatio1));
        UserHomeActivity.aggregate_graph_owed_to_you_ratio2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, owedToYouRatio2));

        // Set you owe graph
        UserHomeActivity.aggregate_graph_you_owe_ratio1.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, youOweRatio1));
        UserHomeActivity.aggregate_graph_you_owe_ratio2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, youOweRatio2));
    }

    private void setupRefreshButton() {
        this.refreshButton = (TextView) findViewById(R.id.refreshDebtsButton);
        this.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefreshButtonClicked();
            }
        });
    }

    private void onRefreshButtonClicked() {
        this.loadDebtsData = new LoadDebtsDataAsync(this, getWindow().getDecorView().getRootView());
        this.loadDebtsData.execute();
    }

    // Adds onClick listener for logout button
    private void setupLogoutButton() {
        this.logoutButton = (TextView) findViewById(R.id.user_home_intro);
        this.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log out and start LoginActivity
                onLogoutButtonClicked();
            }
        });
    }

    // Gets current user and checks success
    private void setCurrentUser() {
        DetApplication.currentUser = DTUser.getCurrentUser();
        if (DetApplication.currentUser == null) {
            UserHomeActivity.this.startLoginActivity();
        }
    }

    public static DTUser getCurrentUser() {
        return DetApplication.currentUser;
    }

    // Gets all of current user's associated debts and writes them to the ListView
    private void displayDebts() {
        // Populate debtsList ListView with debts
        this.loadDebtsData = new LoadDebtsDataAsync(this, getWindow().getDecorView().getRootView());
        loadDebtsData.execute();
    }

    // Called when user clicks add transaction button
    private void onAddTransactionButtonClicked() {
        AddTransactionActivity.setSelectedUsers(null);
        startAddTransactionActivity();
    }

    // Starts AddTransactionActivity
    private void startAddTransactionActivity() {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        startActivityForResult(intent, 1);
    }

    private void setupAddTransactionButton() {
        this.addTransactionButton = (TextView) findViewById(R.id.addTransactionButton);
        this.addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddTransactionButtonClicked();
            }
        });
    }

    // Logs the user out and starts LoginActivity
    private void onLogoutButtonClicked() {
        // Clear token information
        ParseFacebookUtils.getSession().closeAndClearTokenInformation();

        // Log the user out
        ParseUser.logOut();

        // Go to the login view
        startLoginActivity();
    }

    // Starts LoginActivity
    public void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static void resetAggregateTotalsValues() {
        UserHomeActivity.amountOwedToOthers = 0;
        UserHomeActivity.amountOwedToYou = 0;
    }
}
