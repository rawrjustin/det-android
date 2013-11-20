package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.time.StopWatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

public class AddTransactionActivity extends Activity {

	public final static String EXTRA_DEBTS = "com.jab.det.addTransaction";
	private static Collection<GraphUser> selectedFriends;
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
    	
    	// Initialize and save transaction (note: this also saves all corresponding debts)
    	final DTTransaction transaction = new DTTransaction(UserHomeActivity.getCurrentUser(), otherUsers, Double.valueOf(transactionAmount), transactionDescription);
    	
    	// Duplicates! >:(
    	Log.d(DetApplication.TAG, "Duplicates test: " + transaction.toString());
    	
    	stopWatch.stop();
    	Log.d(DetApplication.TAG, "DETAPP: Time elapsed for creating transaction: " + stopWatch.getTime());
    	
    	// Call cloud code function that creates new users if necessary and creates debt(s) and transaction rows
    	ParseCloud.callFunctionInBackground("createTransaction", transaction.getCloudCodeRequestObject(), new FunctionCallback<HashMap<String, Object>>() {
    		public void done(HashMap<String, Object> savedDebts, ParseException e) {
    			if (e != null) {
    				Log.e(DetApplication.TAG, "DETAPP Error calling cloud function" + e.toString());
    		    }
    			
    			DetApplication.showToast(getApplicationContext(), "Transaction and debts added to Parse");
    			
    			// Save debt parse objects to their respective debts
    			for (DTDebt debt : transaction.getDebts()) {
    				ParseObject savedDebtParseObject= (ParseObject) savedDebts.get(debt.getDebtor().getFacebookId());
    				debt.setObjectId(savedDebtParseObject.getObjectId());
    				debt.setParseObject(savedDebtParseObject);
    				
    				// Save debts users map
    				UserHomeActivity.usersMap.get(debt.getDebtor()).add(debt);
    			}
    			
    			// Save transaction parse object to DTTransaction object
    			ParseObject savedTransactionParseObject = (ParseObject) savedDebts.get("transaction");
    			transaction.setObjectId(savedTransactionParseObject.getObjectId());
    			transaction.setParseObject(savedTransactionParseObject);
    			
    			// Update transaction map with transaction
    			UserHomeActivity.transactionsMap.put(transaction, new HashSet<DTDebt>(transaction.getDebts()));
    		}
    	});
    	
    	// Update aggregate totals
    	// Note: Assumes user is the creditor
    	for (DTDebt debt : transaction.getDebts()) {
    		UserHomeActivity.amountOwedToYou += debt.getAmount().doubleValue();
    	}
    	UserHomeActivity.resetAggregateTotals();
    	
    	// Update users map
    	// Note: Assumes user is the creditor
    	for (DTDebt debt : transaction.getDebts()) {
    		if (!UserHomeActivity.usersMap.containsKey(debt.getDebtor())) {
    			UserHomeActivity.usersMap.put(debt.getDebtor(), new HashSet<DTDebt>());
    		}
    		// Debt is added to users map when cloud code is done
    	}
    	
    	Intent intent = new Intent(this, UserHomeActivity.class);
    	Bundle extras = new Bundle();
    	extras.putSerializable(EXTRA_DEBTS, transaction);
    	intent.putExtras(extras);
    	setResult(RESULT_OK, intent);
		finish();
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
	
	// Returns whether user inputted amount is valid. 
    // Valid amount is no more than nine digits before the decimal and to no more than two decimal points
    private Boolean isValidAmount(String amount) {
    	return amount.contains(".") ? amount.length() - 1 - amount.indexOf(".") <= 2 && amount.indexOf(".") <= 9 : amount.indexOf(".") <= 9;
    }
}
