package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.time.StopWatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.jab.det.DTObjects.DTTransaction;
import com.jab.det.DTObjects.DTUser;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

public class AddTransactionActivity extends Activity {

    private static Collection<GraphUser> selectedFriends;
    private TextView homeTextView;
    private TextView selectedFriendsTextView;
    private Button selectFriendsButton;
    private TextView submitTransactionButton;
    private EditText transactionAmountEditText;
    private EditText transactionDescriptionEditText;
    private TextView paidByText;
    private TextView howSplitText;
    private String transactionAmount;
    private String transactionDescription;

    public static Collection<GraphUser> getSelectedUsers() {
        return selectedFriends;
    }

    public static void setSelectedUsers(Collection<GraphUser> selectedUsers) {
        selectedFriends = selectedUsers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        homeTextView = (TextView) findViewById(R.id.cancelDebtButton);
        homeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        selectedFriendsTextView = (TextView) findViewById(R.id.selected_friends);
        selectFriendsButton = (Button) findViewById(R.id.select_friends);
        selectFriendsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickSelectFriends();
            }
        });

        submitTransactionButton = (TextView) findViewById(R.id.saveTransactionButton);
        submitTransactionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickSubmitTransaction();
            }
        });

        howSplitText = (TextView) findViewById(R.id.add_transaction_how_split_text);
        howSplitText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DetApplication.showToast(getApplicationContext(), "Only split evenly supported");
            }
        });

        paidByText = (TextView) findViewById(R.id.add_transaction_whom_text);
        paidByText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DetApplication.showToast(getApplicationContext(), "Only user paid supported");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Update the display every time we are started.
        displaySelectedFriends();
    }

    // Runs when submit transaction is clicked
    private void onClickSubmitTransaction() {
        // Check fields
        this.transactionAmountEditText = (EditText) findViewById(R.id.edit_transaction_amount);
        this.transactionDescriptionEditText = (EditText) findViewById(R.id.edit_transaction_description);
        this.transactionAmount = transactionAmountEditText.getText().toString();
        this.transactionDescription = transactionDescriptionEditText.getText().toString();

        // Amount must be valid
        if (transactionAmount.trim().isEmpty() || !isValidAmount(transactionAmount)) {
            DetApplication.showToast(this.getApplicationContext(), "Invalid amount");
            return;
        }

        // Description must be nonempty
        if (transactionDescription.trim().isEmpty()) {
            DetApplication.showToast(this.getApplicationContext(), "Description must be nonempty");
            return;
        }

        // Selected friends must be nonempty
        if (selectedFriends == null || selectedFriends.size() == 0) {
            DetApplication.showToast(this.getApplicationContext(), "No friends selected");
            return;
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.reset();
        stopWatch.start();

        // Get all selected friends as DTUser objects
        ArrayList<DTUser> otherUsers = new ArrayList<DTUser>();
        for (GraphUser user : selectedFriends) {
            otherUsers.add(new DTUser(user.getId(), user.getName()));
        }

        stopWatch.stop();
        Log.d(DetApplication.TAG, "DETAPP: Time elapsed for creating DTUsers: " + stopWatch.getTime());
        stopWatch.reset();
        stopWatch.start();

        // Initialize and save transaction (note: this also saves all
        // corresponding debts)
        final DTTransaction transaction = new DTTransaction(UserHomeActivity.getCurrentUser(), otherUsers, Double.valueOf(transactionAmount), transactionDescription);

        stopWatch.stop();
        Log.d(DetApplication.TAG, "DETAPP: Time elapsed for creating transaction: " + stopWatch.getTime());

        final ProgressDialog progressDialog = ProgressDialog.show(AddTransactionActivity.this, "", "Saving...", true);

        // Call cloud code function that creates new users if necessary and
        // creates debt(s) and transaction rows
        ParseCloud.callFunctionInBackground("createTransaction", transaction.getCloudCodeRequestObject(), new FunctionCallback<HashMap<String, Object>>() {
            public void done(HashMap<String, Object> savedDebts, ParseException e) {
                if (e != null) {
                    Log.e(DetApplication.TAG, "DETAPP Error calling cloud function" + e.toString());
                    DetApplication.showToast(getApplicationContext(), "Error, please try again later");
                    progressDialog.dismiss();
                    return;
                }

                // Show toast
                DetApplication.showToast(getApplicationContext(), "Transaction added to Parse");

                Intent intent = new Intent(AddTransactionActivity.this, UserHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    // Display selected friends
    private void displaySelectedFriends() {
        String results = "";
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectFriendsButton.getLayoutParams();
        if (selectedFriends != null && selectedFriends.size() > 0) {
            ArrayList<String> names = new ArrayList<String>();
            for (GraphUser user : selectedFriends) {
                names.add(user.getName());
            }
            results = TextUtils.join("\n", names);
            params.addRule(RelativeLayout.BELOW, R.id.selected_friends);
        } else {
            params.addRule(RelativeLayout.BELOW, R.id.add_transaction_with_you_and);
        }

        selectFriendsButton.setLayoutParams(params);
        selectedFriendsTextView.setText(results);
    }

    // Runs when select friends is clicked
    private void onClickSelectFriends() {
        startPickFriendsActivity();
    }

    private void startPickFriendsActivity() {
        if (DTUser.getCurrentUser() != null) {
            selectedFriends = null;

            Intent intent = new Intent(this, SelectFriendsActivity.class);
            // Note: The following line is optional, as multi-select behavior is
            // the default for
            // FriendPickerFragment. It is here to demonstrate how parameters
            // could be passed to the
            // friend picker if single-select functionality was desired, or if a
            // different user ID was
            // desired (for instance, to see friends of a friend).
            SelectFriendsActivity.populateParameters(intent, null, true, true);
            startActivityForResult(intent, 1);
        } else {
            startLoginActivity();
        }
    }

    // Starts LoginActivity
    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Returns whether user inputted amount is valid.
    // Valid amount is no more than nine digits before the decimal and to no
    // more than two decimal points
    private Boolean isValidAmount(String amount) {
        return amount.contains(".") ? amount.length() - 1 - amount.indexOf(".") <= 2 && amount.indexOf(".") <= 9 : amount.indexOf(".") <= 9;
    }
}
