package com.jab.det.DTObjects;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.jab.det.DetApplication;
import com.jab.det.UserHomeActivity;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DTUser {

    private String objectId;
    private String email;
    private String facebookID;
    private String name;
    public static ParseUser currentParseUser;
    private static String defaultPassword = "password";

    // Constructs DTUser with fields
    public DTUser(String objectId, String email, String facebookID, String username, String password, String name) {
        this.objectId = objectId;
        this.email = email;
        this.facebookID = facebookID;
        this.name = name;
    }

    public DTUser(String facebookID, String name) {
        this.facebookID = facebookID;
        this.name = name;
    }

    // Constructs DTUser object with objectId
    public static DTUser getUserFromObjectId(String objectIdParam) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
        ParseObject parseObject;
        String objectId = null, email = null, facebookID = null, username = null, password = null, name = null;
        try {
            parseObject = query.get(objectIdParam);
            objectId = parseObject.getObjectId();
            email = parseObject.getString("email");
            facebookID = parseObject.getString("fbID");
            username = parseObject.getString("username");
            password = "";
            name = parseObject.getString("name");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new DTUser(objectId, email, facebookID, username, password, name);
    }

    // Returns a DTUser representing the current user.
    public static DTUser getCurrentUser() {
        if (currentParseUser == null) {
            currentParseUser = ParseUser.getCurrentUser();
        }
        return getUserFromParseUser(currentParseUser);
    }

    // Returns a DTUser given a ParseUser object
    public static DTUser getUserFromParseUser(ParseUser parseUser) {
        if (parseUser == null) {
            return null;
        }

        try {
            parseUser = parseUser.fetchIfNeeded();
        } catch (ParseException e) {
            Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
            e.printStackTrace();
        }

        String objectId = null, facebookID = null, name = null, email = null, username = null, password = generatePassword(false);
        objectId = parseUser.getObjectId();
        name = parseUser.getString("name");
        facebookID = parseUser.getString("fbID");
        username = parseUser.getUsername();
        email = parseUser.isAuthenticated() ? parseUser.getString("email") : "";

        return new DTUser(objectId, email, facebookID, username, password, name);
    }

    // Returns consolidated debts as a list of user integer pairs, representing
    // users and how much they owe currentUser
    public List<AbstractMap.SimpleEntry<DTUser, Integer>> getConsolidatedDebts() {
        return null;
    }

    private List<ParseObject> queryParseForDebts() throws ParseException {
        // Query debts where user is debtor
        ParseQuery<ParseObject> creditorsQuery = ParseQuery.getQuery(DetApplication.DEBT_TABLE_NAME);
        creditorsQuery.whereEqualTo(DetApplication.DEBT_TABLE_CREDITOR_COLUMN_NAME, DTUser.currentParseUser);

        // Query debts where user is creditor
        ParseQuery<ParseObject> debtorsQuery = ParseQuery.getQuery(DetApplication.DEBT_TABLE_NAME);
        debtorsQuery.whereEqualTo(DetApplication.DEBT_TABLE_DEBTOR_COLUMN_NAME, DTUser.currentParseUser);

        // Main query to query for debts where the user is the creditor or debtor
        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(creditorsQuery);
        queries.add(debtorsQuery);
        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.include(DetApplication.DEBT_TABLE_TRANSACTION_COLUMN_NAME);
        mainQuery.include(DetApplication.DEBT_TABLE_CREDITOR_COLUMN_NAME);
        mainQuery.include(DetApplication.DEBT_TABLE_DEBTOR_COLUMN_NAME);

        return mainQuery.find();
    }

    // Returns a DTDebt array of all debts user is a part of
    public DTDebt[] getDebts() throws ParseException {
        // Reset aggregate totals
        UserHomeActivity.resetAggregateTotalsValues();

        // Query parse for debts
        ArrayList<DTDebt> debts = new ArrayList<DTDebt>();
        for (ParseObject queryResult : this.queryParseForDebts()) {
            debts.add(new DTDebt(queryResult));
        }

        // Populate friends collection
        DetApplication.populateFriendsCollection(debts);

        // Add to aggregate totals
        for (DTDebt debt : debts) {
            // Get the friend in the debt (because the current use could be the debtor or creditor)
            DTUser friend = DTUtils.getFriend(debt);

            // Increment aggregate totals
            if (friend.equals(debt.getCreditor())) {
                UserHomeActivity.amountOwedToYou += debt.getAmount().doubleValue();
            } else {
                UserHomeActivity.amountOwedToOthers += debt.getAmount().doubleValue();
            }

        }

        // Associate debts to transactions
        DTUtils.associateDebtsTransactions(debts);

        return debts.toArray(new DTDebt[debts.size()]);
    }

    // Implements toString() behavior
    public String toString() {
        return getName();
    }

    // Gets DTUser name
    public String getName() {
        return this.name;
    }

    public String getFacebookId() {
        return this.facebookID;
    }

    // Gets ObjectId
    public String getObjectId() {
        return this.objectId;
    }

    public static String generatePassword(Boolean random) {
        return random ? new BigInteger(130, new SecureRandom()).toString(32) : defaultPassword;
    }

    public String getEmail() {
        return this.email;
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof DTUser) || o.equals(null) ? false : this.facebookID.equals(((DTUser) o).getFacebookId());
    }

    @Override
    public int hashCode() {
        return this.facebookID.hashCode();
    }
}
