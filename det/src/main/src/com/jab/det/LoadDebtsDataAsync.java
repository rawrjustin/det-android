package com.jab.det;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class LoadDebtsDataAsync extends AsyncTask<Void, Void, DTDebt[]>{

	private GridView debtGridView;
	private TextView loadingDebtsTextView;
	private View rootView;
	private Context context;
	public static DisplayDebtsAdapter debtListAdapter;
	private Button refreshButton;
	
	public LoadDebtsDataAsync(Context context, View rootView) {
		this.context = context;
		this.rootView = rootView;
		this.loadingDebtsTextView = (TextView) this.rootView.findViewById(R.id.loading_debts);
		this.refreshButton = (Button) this.rootView.findViewById(R.id.refreshDebtsButton);
		this.debtGridView = (GridView) this.rootView.findViewById(R.id.debt_grid);
	}
	
	@Override
	protected void onPreExecute() {
		this.refreshButton.setEnabled(false);
		this.loadingDebtsTextView.setVisibility(View.VISIBLE);
		this.loadingDebtsTextView.setText("Loading debts...");
		this.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new ArrayList<DTDebt>());
		this.debtGridView.setAdapter(debtListAdapter);
	}
	
	protected void onPostExecute(DTDebt[] debts) {
		if (debts.length == 0) {
			loadingDebtsTextView.setText(this.rootView.getResources().getString(R.string.no_debts));
		} else {
			// Writes debts to view
			this.loadingDebtsTextView.setVisibility(View.GONE);
			this.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new ArrayList<DTDebt>(Arrays.asList(debts)));
			debtGridView.setAdapter(this.debtListAdapter);
		}
		
		this.refreshButton.setEnabled(true);
	}

	@Override
	protected DTDebt[] doInBackground(Void... params) {
		return UserHomeActivity.getCurrentUser().getDebts();
	}
}
