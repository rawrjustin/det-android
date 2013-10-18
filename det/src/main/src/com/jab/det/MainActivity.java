package com.jab.det;

import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Track parse statistics
        ParseAnalytics.trackAppOpened(getIntent());

		TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText("Test message");
        setContentView(textView);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
