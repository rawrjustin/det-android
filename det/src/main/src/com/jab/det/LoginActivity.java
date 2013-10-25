package com.jab.det;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseFacebookUtils.Permissions;
import com.parse.SaveCallback;

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
	private String fbID, email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setupLoginButton();
		
		// Check if user is logged in and linked to facebook
		ParseUser currentUser = ParseUser.getCurrentUser();
		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			startUserHomeActivity();
		}
	}

	// Handles About button clicked
	public void aboutButtonClicked(View view) {
		Intent intent = new Intent(this, AboutPage.class);
		startActivity(intent);
	}
	
    // Adds onClick listener for login button
    private void setupLoginButton() {
    	loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});
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

	// Gets run when user clicks log in button
	private void onLoginButtonClicked() {
		LoginActivity.this.progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);
		List<String> permissions = Arrays.asList(Permissions.User.ABOUT_ME, Permissions.Friends.ABOUT_ME);
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(final ParseUser newlyCreatedUser, ParseException err) {
				if (newlyCreatedUser == null) {
					Log.d(DetApplication.TAG, "Uh oh. The user cancelled the Facebook login.");
				} else if (newlyCreatedUser.isNew()) {
					Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
						@Override
				        public void onCompleted(GraphUser graphUser, Response response) {
				            if (graphUser != null) {
				                // Get the parse user info
				            	LoginActivity.this.fbID = graphUser.getId();
				            	LoginActivity.this.email = (String) graphUser.getProperty("email");
				            	// Check if there is existing user row and authenticate it
				        		ParseQuery<ParseUser> query = ParseUser.getQuery();
				        		query.whereEqualTo("fbID", LoginActivity.this.fbID);       		
				        		try {
				        			List<ParseUser> queryResult = query.find();
				        			// If user row already exists
				        			if (queryResult.size() > 0) {
				        				// Delete newly created user
						        		ParseUser.deleteAllInBackground(new ArrayList<ParseObject>(Arrays.asList(newlyCreatedUser)), null);
						        		// Authenticate existing user row
					        			ParseUser userToAuthenticate = queryResult.get(0);
						        		ParseUser.logIn(userToAuthenticate.getUsername(), DTUser.defaultPassword);
						        		// Set random password
						        		userToAuthenticate.setPassword(DTUser.generatePassword(true));
						        		ParseFacebookUtils.link(userToAuthenticate, LoginActivity.this);
						        		userToAuthenticate.put("email", LoginActivity.this.email);
						        		userToAuthenticate.put("fbID", LoginActivity.this.fbID);
						        		userToAuthenticate.saveInBackground();
				        			}
				        		} catch (ParseException e) {
				        			// TODO Auto-generated catch block
				        			e.printStackTrace();
				        		}

				            	LoginActivity.this.progressDialog.dismiss();
				            	startUserHomeActivity();
				            } else {
				            	// TODO: Handle error
				            }
				        }
				    });
					if (!ParseFacebookUtils.isLinked(newlyCreatedUser)) {
						ParseFacebookUtils.link(newlyCreatedUser, LoginActivity.this, new SaveCallback() {
							@Override
							public void done(ParseException ex) {
								if (ParseFacebookUtils.isLinked(newlyCreatedUser)) {
									Log.d("MyApp", "Woohoo, user logged in with Facebook!");
								}
						    }
						});
					}
					Log.d(DetApplication.TAG, "User signed up and logged in through Facebook!");
				} else {
					Log.d(DetApplication.TAG, "User logged in through Facebook!");
					startUserHomeActivity();
				}
			}
		});
	}
	
	// Starts UserHomeActivity
	private void startUserHomeActivity() {
		Intent intent = new Intent(this, UserHomeActivity.class);
		startActivity(intent);
	}
}
