package com.jab.det;

import java.text.DecimalFormat;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class DetApplication extends Application {

	public static final String TAG = "DetApp";
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("$##.00");
	
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
    
    // Format double as dollar amount
    public static String formatAsDollarAmount(double d) {
    	return d == 0 ? "$0" : DetApplication.DECIMAL_FORMAT.format(d);
    }
}
