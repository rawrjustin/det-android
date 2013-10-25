package com.jab.det;

import java.util.ArrayList;
import java.util.Arrays;

import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UserHomeActivity extends Activity {

	private Button logoutButton;
	private TextView userIntroView;
	private ListView debtListView;
	private DisplayDebtsAdapter debtListAdapter;
	private static DTUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        setCurrentUser();
		setupLogoutButton();
		displayDebts();
		// Set loading message
		//debtListView = (ListView) findViewById(R.id.debt_list);
		//debtListAdapter = new ArrayAdapter<String>(this, R.layout.debt_row, new ArrayList<String>(Arrays.asList("Loading debts...")));
		//debtListView.setAdapter(debtListAdapter);
		/*Thread thread = new Thread() {
			public void run() {
				displayDebts();
			}
		};
		
		thread.start();*/
    }

    // Adds onClick listener for logout button
    private void setupLogoutButton() {
    	logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLogoutButtonClicked();
			}
		});
    }
    
    // Gets current user and checks success
    private void setCurrentUser() {
    	currentUser = currentUser == null ? DTUser.getCurrentUser() : currentUser;
    	
    	// Dispay intro message
        userIntroView = (TextView) findViewById(R.id.user_home_intro);
		userIntroView.setText("Hi " + currentUser.getName() + ", add a transaction or view your debts below");
//    	if (currentUser == null) {
//    		startLoginActivity();
//    	}
    }
    
    public static DTUser getCurrentUser() {
    	return currentUser;
    }
    
    // Gets all of current user's associated debts and writes them to the ListView
    private void displayDebts() {
		// Populate debtsList ListView with debts
		debtListView = (ListView) findViewById(R.id.debt_list);
		
		DTDebt[] currentUserDebts = currentUser.getDebts();

		if (currentUserDebts.length == 0) {
			debtListView.setAdapter(new ArrayAdapter<String>(this, R.layout.simple_debt_row, 
					new ArrayList<String>(Arrays.asList("You are not a part of any debts"))));
		} else {
			// Writes debts to view
			debtListAdapter = new DisplayDebtsAdapter(this, R.layout.debt_row, currentUserDebts);
			debtListView.setAdapter(debtListAdapter);
		}
    }

    // Called when user clicks add transaction button
	public void addTransaction(View view) {
    	Log.d(DetApplication.TAG, "Add transaction button clicked");
    	AddTransactionActivity.setSelectedUsers(null);
    	startAddTransactionActivity();
    }
    
	// Starts AddTransactionActivity
	private void startAddTransactionActivity() {
		Intent intent = new Intent(this, AddTransactionActivity.class);
		startActivity(intent);
	}
	
	// Logs the user out and starts LoginActivity
	private void onLogoutButtonClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	}

	// Starts LoginActivity
	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
