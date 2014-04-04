package com.jab.det;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.jab.det.DTObjects.DTDebt;
import com.jab.det.DTObjects.DTFriend;
import com.jab.det.DTObjects.DTUser;
import com.jab.det.DTObjects.DTUtils;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class DetApplication extends Application {

    // The current user in the context of a user instance of the application
    public static DTUser currentUser;

    // Collection of friends, each object representing a debt relationship between user and friend
    public static List<DTFriend> friends;

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
     * Populates the friends to debts map using a debt list
     * 
     * @param debts
     */
    public static void populateFriendsCollection(List<DTDebt> debts) {
        Map<DTUser, List<DTDebt>> friendToDebtsMap = new HashMap<DTUser, List<DTDebt>>();

        for (DTDebt debt : debts) {
            DTUser friend = DTUtils.getFriend(debt);

            if (!friendToDebtsMap.containsKey(friend)) {
                friendToDebtsMap.put(friend, new ArrayList<DTDebt>(Arrays.asList(debt)));
            } else {
                friendToDebtsMap.get(friend).add(debt);
            }
        }

        friends = new ArrayList<DTFriend>();
        for (Entry<DTUser, List<DTDebt>> keyValuePair : friendToDebtsMap.entrySet()) {
            friends.add(new DTFriend(keyValuePair.getKey(), keyValuePair.getValue()));
        }
    }
}
