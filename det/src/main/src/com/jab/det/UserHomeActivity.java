package com.jab.det;

import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserHomeActivity extends Activity {

	private Button logoutButton;
	private TextView userIntroView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        
        userIntroView = (TextView) findViewById(R.id.user_home_intro);
        
    	logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLogoutButtonClicked();
			}
		});
		
		// TODO: Fetch all debts for current user and display them on this activity
    }

	@Override
	public void onResume() {
		super.onResume();

		//ParseUser currentUser = ParseUser.getCurrentUser();
		DTUser currentUser = DTUser.getCurrentUser();
		if (currentUser != null) {
			userIntroView.setText("Hi " + currentUser.name);
			// Pull user's transactions and display them
			Log.d(DetApplication.TAG, "Failed to retrieve current user on UserHomeActivity resume");
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}
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
    
	private void startAddTransactionActivity() {
		Intent intent = new Intent(this, AddTransactionActivity.class);
		startActivity(intent);
	}
	
	private void onLogoutButtonClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
