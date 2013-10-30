package com.jab.det;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class LoadDebtsData extends AsyncTask<Void, Void, DTDebt[]>{

	private ListView debtListView;
	private TextView loadingDebtsTextView;
	private View rootView;
	private Context context;
	private DisplayDebtsAdapter debtListAdapter;
	private Button refreshButton;
	
	public LoadDebtsData(Context context, View rootView) {
		this.context = context;
		this.rootView = rootView;
		this.loadingDebtsTextView = (TextView) this.rootView.findViewById(R.id.loading_debts);
		this.refreshButton = (Button) this.rootView.findViewById(R.id.refreshDebtsButton);
		this.debtListView = (ListView) this.rootView.findViewById(R.id.debt_list);
	}
	
	@Override
	protected void onPreExecute() {
		this.refreshButton.setEnabled(false);
		this.loadingDebtsTextView.setVisibility(View.VISIBLE);
		this.loadingDebtsTextView.setText("Loading debts...");
		this.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new DTDebt[0]);
		this.debtListView.setAdapter(debtListAdapter);
	}
	
	protected void onPostExecute(DTDebt[] debts) {
		if (debts.length == 0) {
			loadingDebtsTextView.setText("You are not involved in any debts");
		} else {
			// Writes debts to view
			this.loadingDebtsTextView.setVisibility(View.GONE);
			this.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, debts);
			debtListView.setAdapter(this.debtListAdapter);
		}
		
		this.refreshButton.setEnabled(true);
	}

	@Override
	protected DTDebt[] doInBackground(Void... params) {
		return UserHomeActivity.getCurrentUser().getDebts();
	}
}
