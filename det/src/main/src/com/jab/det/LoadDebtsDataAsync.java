package com.jab.det;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.time.StopWatch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

public class LoadDebtsDataAsync extends AsyncTask<Void, Void, DTDebt[]>{

	private GridView debtGridView;
	private TextView loadingDebtsTextView;
	private View rootView;
	private Context context;
	public static DisplayDebtsAdapter debtListAdapter;
	private TextView refreshButton;
	private StopWatch stopWatch;
	
	public LoadDebtsDataAsync(Context context, View rootView) {
		this.context = context;
		this.rootView = rootView;
		this.loadingDebtsTextView = (TextView) this.rootView.findViewById(R.id.loading_debts);
		this.refreshButton = (TextView) this.rootView.findViewById(R.id.refreshDebtsButton);
		this.stopWatch = new StopWatch();
		this.debtGridView = (GridView) this.rootView.findViewById(R.id.debt_grid);
	}
	
	@Override
	protected void onPreExecute() {
		stopWatch.start();
		this.refreshButton.setEnabled(false);
		this.loadingDebtsTextView.setVisibility(View.VISIBLE);
		this.loadingDebtsTextView.setText("Loading debts...");
		LoadDebtsDataAsync.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new ArrayList<DTDebt>());
		stopWatch.stop();
		Log.d(DetApplication.TAG, "DETAPP: Time elapsed for preexecute: " + stopWatch.getTime());
		this.debtGridView.setAdapter(debtListAdapter);
	}
	
	protected void onPostExecute(DTDebt[] debts) {
		stopWatch.reset();
		stopWatch.start();
		
		// Set text for aggregates
		UserHomeActivity.resetAggregateTotals();
		
		if (debts.length == 0) {
			loadingDebtsTextView.setText(this.rootView.getResources().getString(R.string.no_debts));
		} else {
			// Writes debts to view
			this.loadingDebtsTextView.setVisibility(View.GONE);
			LoadDebtsDataAsync.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new ArrayList<DTDebt>(Arrays.asList(debts)));
			debtGridView.setAdapter(LoadDebtsDataAsync.debtListAdapter);
		}
		
		this.refreshButton.setEnabled(true);
		
		stopWatch.stop();
		Log.d(DetApplication.TAG, "DETAPP: Time elapsed for postExecute: " + stopWatch.getTime());
	}

	@Override
	protected DTDebt[] doInBackground(Void... params) {
		stopWatch.reset();
		stopWatch.start();
		//return UserHomeActivity.getCurrentUser().getDebts();
		DTDebt[] ret = UserHomeActivity.getCurrentUser().getDebts();
		stopWatch.stop();
		Log.d(DetApplication.TAG, "DETAPP: Time elapsed for doInBackground: " + stopWatch.getTime());
		return ret;
	}
}
