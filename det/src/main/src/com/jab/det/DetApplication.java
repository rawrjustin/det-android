package com.jab.det;

import com.parse.Parse;
import android.app.Application;

public class DetApplication extends Application {

	@Override
	public void onCreate() {
		// Initialize Parse
		Parse.initialize(this, "SnZIklEaMlecec0zVwMgEgXDgeDWgM458xCUXPqU", "TLNJj6cz7SquDWwPlNHRg79YGHWpftHbh0WoEyLx"); 

		super.onCreate();
	}

}
