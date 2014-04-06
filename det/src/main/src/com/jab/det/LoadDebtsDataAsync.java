package com.jab.det;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.jab.det.DTObjects.DTDebt;
import com.jab.det.DTObjects.DTFriend;
import com.parse.ParseException;

public class LoadDebtsDataAsync extends AsyncTask<Void, Void, DTDebt[]> {

    private GridView debtGridView;
    private TextView loadingDebtsTextView;
    private View rootView;
    private Context context;
    public static DisplayDebtsAdapter debtListAdapter;
    private TextView refreshButton;

    public LoadDebtsDataAsync(Context context, View rootView) {
        this.context = context;
        this.rootView = rootView;
        this.loadingDebtsTextView = (TextView) this.rootView.findViewById(R.id.loading_debts);
        this.refreshButton = (TextView) this.rootView.findViewById(R.id.refreshDebtsButton);
        this.debtGridView = (GridView) this.rootView.findViewById(R.id.debt_grid);
    }

    @Override
    protected void onPreExecute() {
        this.refreshButton.setEnabled(false);
        this.loadingDebtsTextView.setVisibility(View.VISIBLE);
        this.loadingDebtsTextView.setText("Loading debts...");
        LoadDebtsDataAsync.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, new ArrayList<DTFriend>());
        this.debtGridView.setAdapter(debtListAdapter);
    }

    protected void onPostExecute(DTDebt[] debts) {
        // Set text for aggregates
        double amountOwedToYou = 0;
        double amountOwedToOthers = 0;

        for (DTDebt debt : debts) {
            if (debt.getCreditor().equals(DetApplication.currentUser)) {
                amountOwedToYou += Double.valueOf(debt.getAmount().toString());
            } else {
                amountOwedToYou -= Double.valueOf(debt.getAmount().toString());
            }
        }

        UserHomeActivity.resetAggregateTotalsDisplay(amountOwedToYou, amountOwedToOthers);

        // Set loading text
        if (debts.length == 0) {
            loadingDebtsTextView.setText(this.rootView.getResources().getString(R.string.no_debts));
        } else {
            // Writes debts to view
            this.loadingDebtsTextView.setVisibility(View.GONE);
            LoadDebtsDataAsync.debtListAdapter = new DisplayDebtsAdapter(this.context, R.layout.debt_row, (ArrayList<DTFriend>) DetApplication.friends);
            debtGridView.setAdapter(LoadDebtsDataAsync.debtListAdapter);
        }

        // Set refresh button
        this.refreshButton.setEnabled(true);
    }

    @Override
    protected DTDebt[] doInBackground(Void... params) {
        DTDebt[] currentUserDebts = null;
        try {
            currentUserDebts = UserHomeActivity.getCurrentUser().getDebts();
        } catch (ParseException e) {
            Log.e(DetApplication.TAG, "Parse exception thrown: " + e.toString());
            e.printStackTrace();
        }

        return currentUserDebts;
    }
}
