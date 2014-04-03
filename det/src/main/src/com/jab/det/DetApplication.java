package com.jab.det;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.jab.det.DTObjects.DTDebt;
import com.jab.det.DTObjects.DTTransaction;
import com.jab.det.DTObjects.DTUser;
import com.jab.det.DTObjects.DTUtils;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class DetApplication extends Application {

    // The current user in the context of a user instance of the application
    public static DTUser currentUser;

    // Maps friends to their respective debts
    public static Map<DTUser, List<DTDebt>> friendToDebtsMap;

    // Maps transactions to their respective debts.
    // This is used so that we know when need to remove a transaction from parse.
    public static Map<DTTransaction, List<DTDebt>> transactionToDebtsMap;

    // The tag used for filtering debug logs
    public static final String TAG = "DetApp";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("$##.00");

    public static final String DEBT_TABLE_NAME = "Debt";
    public static final String DEBT_TABLE_DEBTOR_COLUMN_NAME = "debtor";
    public static final String DEBT_TABLE_CREDITOR_COLUMN_NAME = "creditor";
    public static final String DEBT_TABLE_TRANSACTION_COLUMN_NAME = "transaction";

    @Override
    public void onCreate() {
        // Initialize Parse
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.app_id));

        // Call superclass's onCreate method
        super.onCreate();
    }

    /**
     * Show a toast to the user with the given message
     * 
     * @param context
     * @param message
     *            to show
     */
    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    // Format double as dollar amount
    /**
     * Formats a double valued number as a dollar amount
     * 
     * @param the
     *            double valued number
     * @return a string representing the dollar amount
     */
    public static String formatAsDollarAmount(double d) {
        return d == 0 ? "$0" : DetApplication.DECIMAL_FORMAT.format(d);
    }

    /**
     * Reinitialize all application scoped hash maps
     */
    public static void resetMaps() {
        DetApplication.friendToDebtsMap = new HashMap<DTUser, List<DTDebt>>();
        DetApplication.transactionToDebtsMap = new HashMap<DTTransaction, List<DTDebt>>();
    }

    /**
     * Populates the friends to debts map using a debt list
     * 
     * @param debts
     */
    public static void populateFriendToDebtsMap(List<DTDebt> debts) {
        for (DTDebt debt : debts) {
            DTUser friend = DTUtils.getFriend(debt);

            if (!DetApplication.friendToDebtsMap.containsKey(friend)) {
                DetApplication.friendToDebtsMap.put(friend, new ArrayList<DTDebt>(Arrays.asList(debt)));
            } else {
                DetApplication.friendToDebtsMap.get(friend).add(debt);
            }
        }
    }

}
