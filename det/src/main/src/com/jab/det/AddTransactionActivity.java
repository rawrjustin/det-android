package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;

import com.facebook.model.GraphUser;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class AddTransactionActivity extends Activity {

	private static Collection<GraphUser> selectedFriends;
	private TextView addFriendsResultTextView;
	private Button selectFriendsButton;
	
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
        
        addFriendsResultTextView = (TextView) findViewById(R.id.select_friends_result);
        selectFriendsButton = (Button) findViewById(R.id.select_friends);
        selectFriendsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickSelectFriends();
            }
        });
        // Show the Up button in the action bar.
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Update the display every time we are started.
        displaySelectedFriends();
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

    // Runs when 
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
    
    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_transaction, menu);
        return true;
    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
