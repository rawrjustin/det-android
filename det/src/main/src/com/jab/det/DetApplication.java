package com.jab.det;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import android.app.Application;

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

}
