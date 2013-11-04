package com.jab.det;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class DetApplication extends Application {

	static final String TAG = "DetApp";
	@Override
	public void onCreate() {
		// Initialize Parse
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key)); 
		ParseFacebookUtils.initialize(getString(R.string.app_id));
		
		// Call superclass's onCreate method
		super.onCreate();
	}

    // Show toast message
    public static void showToast(Context context, String message) {
    	Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
    	toast.show();
    }
}
