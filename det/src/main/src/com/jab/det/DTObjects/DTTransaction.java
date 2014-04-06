package com.jab.det.DTObjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;

import com.jab.det.UserHomeActivity;
import com.parse.ParseObject;

public class DTTransaction {

    private String description;
    private List<DTDebt> debts;
    private ParseObject parseObject;

    // Create a transaction split evenly
    public DTTransaction(DTUser currentUser, Collection<DTUser> otherUsers, Number amount, String description) {
        // Note: Implementation assumes current user is the creditor
        this.description = description;
        this.debts = new ArrayList<DTDebt>();
        for (DTUser user : otherUsers) {
            this.debts.add(new DTDebt(currentUser, user, trimDecimals(amount.doubleValue() / (otherUsers.size() + 1)), this));
        }
    }

    // Called from DTDebt(ParseObject), which is called by DTUser.getDebts()
    public DTTransaction(ParseObject parseObject, List<DTDebt> debts) {
        this.parseObject = parseObject;
        this.description = parseObject.getString("description");
        this.debts = debts;
    }

    // Trims decimal to at most two places
    private static Double trimDecimals(Double input) {
        String inputStr = input.toString();
        if (inputStr.contains(".") && inputStr.length() - 1 - inputStr.indexOf(".") > 2) {
            inputStr = inputStr.substring(0, inputStr.indexOf(".") + 3);
        }

        return Double.valueOf(inputStr);
    }

    // Gets description
    public String getDescription() {
        return this.description;
    }

    public void setDebts(ArrayList<DTDebt> debts) {
        this.debts = debts;
    }

    public ParseObject getParseObject() {
        return this.parseObject;
    }

    public List<DTDebt> getDebts() {
        return this.debts;
    }

    public String toString() {
        StringBuilder debts = new StringBuilder();
        for (DTDebt debt : this.debts) {
            debts.append("    " + debt.getParseObject().getObjectId() + "|D:" + debt.getDebtor().getName() + "|C:" + debt.getCreditor().getName() + "|A:" + debt.getAmount() + "\n");
        }

        return String.format("Transaction %s has %s debts:\n%s", this.parseObject.getObjectId(), this.debts.size(), debts.toString());
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof DTTransaction) || o.equals(null) ? false : this.parseObject.getObjectId().equals(((DTTransaction) o).getParseObject().getObjectId());
    }

    @Override
    public int hashCode() {
        return this.parseObject.getObjectId().hashCode();
    }

    public HashMap<String, Object> getCloudCodeRequestObject() {
        HashMap<String, Object> requestObject = new HashMap<String, Object>();
        ArrayList<String> fbIds = new ArrayList<String>();
        requestObject.put("user", UserHomeActivity.getCurrentUser().getObjectId());
        requestObject.put("description", this.description);
        for (DTDebt debt : this.debts) {
            HashMap<String, Object> debtorMap = new HashMap<String, Object>();
            debtorMap.put("name", debt.getDebtor().getName());
            debtorMap.put("amount", debt.getAmount());
            requestObject.put(debt.getDebtor().getFacebookId(), debtorMap);
            fbIds.add(debt.getDebtor().getFacebookId());
        }

        requestObject.put("fbIdentifiers", new JSONArray(fbIds));
        return requestObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public void removeDebt(DTDebt debt) {
        this.debts.remove(debt);
    }
}
