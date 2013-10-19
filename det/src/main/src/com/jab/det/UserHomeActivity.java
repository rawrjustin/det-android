package com.jab.det;

import java.util.ArrayList;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
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
	private ArrayAdapter<String> debtListAdapter;
	private DTUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        getCurrentUser();
		setupLogoutButton();
		displayDebts();
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
    private void getCurrentUser() {
    	currentUser = DTUser.getCurrentUser();
		if (currentUser != null) {
			// Current user successfully retrieved
			Log.d(DetApplication.TAG, "Successfully retrieved current user");
		} else {
			// Failed to retrieve current user
			Log.d(DetApplication.TAG, "Failed to retrieve current user");
			startLoginActivity();
		}

    }
    
    // Gets all of current user's associated debts and writes them to the ListView
    private void displayDebts() {
    	// Dispay intro message
        userIntroView = (TextView) findViewById(R.id.user_home_intro);
		userIntroView.setText("Hi " + currentUser.name + ", add a transaction or view your debts below");
		
		// Populate debtsList ListView with debts
		debtListView = (ListView) findViewById(R.id.debt_list);
		ArrayList<String> debtList = new ArrayList<String>();  
		
		for (DTDebt debt : currentUser.getDebts()) {
			debtList.add(debt.toString());
		}
		
		if (debtList.size() == 0) {
			debtList.add("You are not involved in any debts");
		}
		
		// Writes debts to view
		debtListAdapter = new ArrayAdapter<String>(this, R.layout.debt_row, debtList);
		debtListView.setAdapter(debtListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_home, menu);
        return true;
    }
    
    // Called when user clicks add transaction button
	public void addTransaction(View view) {
    	Log.d(DetApplication.TAG, "Add transaction button clicked");
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
