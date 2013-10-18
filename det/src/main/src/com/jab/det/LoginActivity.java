package com.jab.det;

import java.util.Arrays;
import java.util.List;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {

	private Button loginButton;
	private Dialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});

		// Check if there is a currently logged in user
		// and they are linked to a Facebook account.
		ParseUser currentUser = ParseUser.getCurrentUser();
		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			showUserHomeActivity();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	/* Gets run when user clicks log in with facebook button */
	private void onLoginButtonClicked() {
		// TODO: Link new user with existing parse row if exists
		LoginActivity.this.progressDialog = ProgressDialog.show(
				LoginActivity.this, "", "Logging in...", true);
		List<String> permissions = Arrays.asList("email", "basic_info", "user_about_me",
				"user_relationships", "user_birthday", "user_location");
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(final ParseUser parseUser, ParseException err) {
				if (parseUser == null) {
					Log.d(DetApplication.TAG,
							"Uh oh. The user cancelled the Facebook login.");
				} else if (parseUser.isNew()) {
					Log.d(DetApplication.TAG,
							"User signed up and logged in through Facebook!");

					// TODO: Populate user properties
					// Request me request
				    Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {

				        @Override
				        public void onCompleted(GraphUser graphUser, Response response) {
				            if (graphUser != null) {
				                // Display the parsed user info
				            	parseUser.put("name", graphUser.getName());
				            	parseUser.put("fbID", graphUser.getId());
				            	parseUser.put("email", graphUser.getProperty("email"));
				            	try {
					            	parseUser.save();
				            	} catch (Exception e)
				            	{
				            		// TODO: Handle save exception
				            	}
				            	
				            	Log.d(DetApplication.TAG, "User properties saved");
				            	LoginActivity.this.progressDialog.dismiss();
								showUserHomeActivity();
				            } else {
				            	// TODO: Handle error
				            }
				        }
				    });
				} else {
					Log.d(DetApplication.TAG,
							"User logged in through Facebook!");
					showUserHomeActivity();
				}
			}
		});
	}
	
	private void showUserHomeActivity() {
		Intent intent = new Intent(this, UserHomeActivity.class);
		startActivity(intent);
	}
}
