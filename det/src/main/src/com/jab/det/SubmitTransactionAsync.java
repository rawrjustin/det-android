package com.jab.det;

import android.content.Context;
import android.os.AsyncTask;

public class SubmitTransactionAsync extends AsyncTask<DTTransaction, Void, Boolean>{

	private Context context;
	
	public SubmitTransactionAsync(Context context) {
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		
	}
	
	protected void onPostExecute() {

	}

	@Override
	protected Boolean doInBackground(DTTransaction... params) {
		params[0].save();
		return true;
	}

}
