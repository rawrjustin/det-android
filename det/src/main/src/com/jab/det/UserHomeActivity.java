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
	private Button refreshButton;
	private Button addTransactionButton;
	private TextView userIntroView;
	private ListView debtListView;
	private static DTUser currentUser;
	private LoadDebtsData loadDebtsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        this.debtListView = (ListView) findViewById(R.id.debt_list);
        setCurrentUser();
        setupAddTransactionButton();
        setupRefreshButton();
		setupLogoutButton();
		displayDebts();
    }
   
    private void setupRefreshButton() {
    	this.refreshButton = (Button) findViewById(R.id.refreshDebtsButton);
		this.refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onRefreshButtonClicked();
			}
		});
    }
    
	private void onRefreshButtonClicked() {
		this.loadDebtsData = new LoadDebtsData(this, getWindow().getDecorView().getRootView());
		this.loadDebtsData.execute();
	}
    
    // Adds onClick listener for logout button
    private void setupLogoutButton() {
    	this.logoutButton = (Button) findViewById(R.id.logoutButton);
		this.logoutButton.setOnClickListener(new View.OnClickListener() {
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
		this.loadDebtsData = new LoadDebtsData(this, getWindow().getDecorView().getRootView());
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
		startActivity(intent);
	}

    private void setupAddTransactionButton() {
    	this.addTransactionButton = (Button) findViewById(R.id.addTransactionButton);
    	this.addTransactionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddTransactionButtonClicked();
			}
		});
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
