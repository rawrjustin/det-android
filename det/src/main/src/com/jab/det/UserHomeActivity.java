package com.jab.det;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jab.det.DTObjects.DTUser;
import com.jab.det.DTObjects.DTUtils;
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
        // setupLogoutButton();
        displayDebts();
    }

    public static void resetAggregateTotalsDisplay(double amountOwedToYou, double amountOwedToOthers) {
        double balance = amountOwedToYou - amountOwedToOthers;

        UserHomeActivity.amountOwedToYouTextView.setText(DTUtils.getDisplayableDollarAmount(amountOwedToYou));
        UserHomeActivity.amountYouOweTextView.setText(DTUtils.getDisplayableDollarAmount(amountOwedToOthers));
        UserHomeActivity.amountBalanceTextView.setText(DTUtils.getDisplayableDollarAmount(balance));

        if (balance < 0) {
            UserHomeActivity.amountBalanceTextView.setTextColor(DetApplication.DET_RED);
        } else {
            UserHomeActivity.amountBalanceTextView.setTextColor(DetApplication.DET_GREEN);
        }

        float owedToYouRatio1 = (float) (amountOwedToOthers / (amountOwedToYou + amountOwedToOthers));
        float owedToYouRatio2 = (float) (amountOwedToYou / (amountOwedToYou + amountOwedToOthers));
        float youOweRatio1 = (float) (amountOwedToYou / (amountOwedToYou + amountOwedToOthers));
        float youOweRatio2 = (float) (amountOwedToOthers / (amountOwedToYou + amountOwedToOthers));

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
                UserHomeActivity.this.loadDebtsData = new LoadDebtsDataAsync(UserHomeActivity.this, getWindow().getDecorView().getRootView());
                UserHomeActivity.this.loadDebtsData.execute();
            }
        });
    }

    // Adds onClick listener for logout button
    private void setupLogoutButton() {
        this.logoutButton = (TextView) findViewById(R.id.user_home_intro);
        this.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear token information
                ParseFacebookUtils.getSession().closeAndClearTokenInformation();

                // Log the user out
                ParseUser.logOut();

                // Go to the login view
                DetApplication.startActivity(LoginActivity.class, getBaseContext(), UserHomeActivity.this, true);
            }
        });
    }

    // Gets current user and checks success
    private void setCurrentUser() {
        DetApplication.currentUser = DTUser.getCurrentUser();
        if (DetApplication.currentUser == null) {
            DetApplication.startActivity(LoginActivity.class, getBaseContext(), this, true);
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

    private void setupAddTransactionButton() {
        this.addTransactionButton = (TextView) findViewById(R.id.addTransactionButton);
        this.addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTransactionActivity.setSelectedUsers(null);
                Intent intent = new Intent(UserHomeActivity.this, AddTransactionActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Back button on the home activity will back out of the app instead of to login activity
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
