package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;

import com.facebook.model.GraphUser;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class AddTransactionActivity extends Activity {

	private static Collection<GraphUser> selectedFriends;
	private Dialog progressDialog;
	private TextView addFriendsResultTextView;
	private Button selectFriendsButton;
	private Button submitTransactionButton;
	private EditText transactionAmountEditText;
	private EditText transactionDescriptionEditText;
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
        
        this.addFriendsResultTextView = (TextView) findViewById(R.id.select_friends_result);
        this.selectFriendsButton = (Button) findViewById(R.id.select_friends);
        this.selectFriendsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickSelectFriends();
            }
        });
        
        submitTransactionButton = (Button) findViewById(R.id.add_transaction_submit);
        submitTransactionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	onClickSubmitTransaction();
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
    	// TODO
    	
    	// Check fields
    	this.transactionAmountEditText = (EditText) findViewById(R.id.edit_transaction_amount);
    	this.transactionDescriptionEditText = (EditText) findViewById(R.id.edit_transaction_description);
    	this.transactionAmount = transactionAmountEditText.getText().toString();
    	this.transactionDescription = transactionDescriptionEditText.getText().toString();
    	
    	// Amount must be valid
    	if (transactionAmount.trim().isEmpty() || !isValidAmount(transactionAmount)) {
    		showToast("Invalid amount");
    		return;
    	}
    	
    	// Description must be nonempty
    	if (transactionDescription.trim().isEmpty()) {
    		showToast("Description must be nonempty");
    		return;
    	}
    	
    	// Selected friends must be nonempty
    	if (selectedFriends == null || selectedFriends.size() == 0) {
    		showToast("No friends selected");
    		return;
    	}
    	
		progressDialog = ProgressDialog.show(AddTransactionActivity.this, "", "Submitting transaction...", true);

		Thread thread = new Thread() {
			public void run() {
		    	// Get all selected friends as DTUser objects
		    	ArrayList<DTUser> otherUsers = new ArrayList<DTUser>();
		    	for (GraphUser user : selectedFriends) {
		    		otherUsers.add(DTUser.getOrCreateUser(user.getId(), user.getName()));
		    	}

		    	// Initialize and save transaction (note: this also saves all corresponding debts)
		    	DTTransaction transaction = new DTTransaction(UserHomeActivity.getCurrentUser(), otherUsers, Double.valueOf(transactionAmount), transactionDescription);
		    	transaction.save();
				progressDialog.dismiss();
			}
		};
		
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
		}
		startUserHomeActivity();
    }
    
    // Display selected friends
    private void displaySelectedFriends() {
        String results = "";
        if (selectedFriends != null && selectedFriends.size() > 0) {
            ArrayList<String> names = new ArrayList<String>();
            for (GraphUser user : selectedFriends) {
                names.add(user.getName());
            }
            results = TextUtils.join(", ", names);
        } else {
            results = "<No friends selected>";
        }

        addFriendsResultTextView.setText(results);
    }

    // Runs when select friends is clicked
    private void onClickSelectFriends() {
        startPickFriendsActivity();
    }

    private void startPickFriendsActivity() {
        if (DTUser.getCurrentUser() != null) {
            selectedFriends = null;

            Intent intent = new Intent(this, SelectFriendsActivity.class);
            // Note: The following line is optional, as multi-select behavior is the default for
            // FriendPickerFragment. It is here to demonstrate how parameters could be passed to the
            // friend picker if single-select functionality was desired, or if a different user ID was
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
	
	// Starts UserHomeActivity
		private void startUserHomeActivity() {
			Intent intent = new Intent(this, UserHomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
    
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.cancel();
        }
    }

    // Show toast message
    private void showToast(String message) {
    	Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
    	toast.show();
    }
    
    // Returns whether user inputted amount is valid. 
    // Valid amount is no more than nine digits before the decimal and to no more than two decimal points
    private Boolean isValidAmount(String amount) {
    	return amount.contains(".") ? amount.length() - 1 - amount.indexOf(".") <= 2 && amount.indexOf(".") <= 9 : amount.indexOf(".") <= 9;
    }
}
