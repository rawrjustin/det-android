package com.jab.det;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import com.parse.ParseException;
import com.parse.ParseObject;

@SuppressWarnings("serial")
// Serialized object will be deserialized in the same context
public class DTDebt implements Serializable {

    private Number amount;
    private DTUser creditor;
    private final String dateCreated;
    private DTUser debtor;
    private String objectId;
    private transient ParseObject parseObject;
    private DTTransaction transaction;

    public DTDebt(DTUser creditor, DTUser debtor, Number amount, DTTransaction transaction) {
        this.dateCreated = (new Date()).toString();
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
        this.transaction = transaction;
        this.objectId = (new java.util.Date()).toString();
    }

    // Added from DTUser.getDebts
    public DTDebt(ParseObject parseObject) {
        this.dateCreated = (new Date()).toString();
        this.objectId = parseObject.getObjectId();
        this.amount = parseObject.getNumber("amount");
        this.creditor = new DTUser(parseObject.getParseUser("creditor").getString("fbID"), parseObject.getParseUser("creditor").getString("name"));
        this.debtor = new DTUser(parseObject.getParseUser("debtor").getString("fbID"), parseObject.getParseUser("debtor").getString("name"));
        this.parseObject = parseObject;

        // Transaction setup
        ParseObject parseTransaction = parseObject.getParseObject("transaction");
        String parseTransactionObjectId = parseTransaction.getObjectId();

        // Save to transaction maps
        if (UserHomeActivity.transactionsObjectIdToDTTransaction.containsKey(parseTransactionObjectId)) {
            this.transaction = UserHomeActivity.transactionsObjectIdToDTTransaction.get(parseTransactionObjectId);
            UserHomeActivity.transactionsMap.get(this.transaction).add(this);
        } else {
            this.transaction = new DTTransaction(parseTransaction);
            UserHomeActivity.transactionsObjectIdToDTTransaction.put(parseTransactionObjectId, this.transaction);
            HashSet<DTDebt> associatedDebts = new HashSet<DTDebt>();
            associatedDebts.add(this);
            UserHomeActivity.transactionsMap.put(this.transaction, associatedDebts);
        }
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof DTDebt) || o.equals(null) ? false : this.objectId.equals(((DTDebt) o).getObjectId());
    }

    public Number getAmount() {
        return this.amount;
    }

    public DTUser getCreditor() {
        return this.creditor;
    }

    public DTUser getDebtor() {
        return debtor;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    // Prepends $ to amount and returns string
    public String getAmountToString() {
        // Making the string look pretty. Amounts will have no decimals if it is
        // an integer, two decimal places otherwise
        StringBuilder amountString = new StringBuilder(this.amount.toString());
        boolean isInt = false;
        if (amountString.indexOf(".") >= 0) {
            isInt = true;
            for (int i = 0; i < amountString.length() - 1 - amountString.indexOf("."); i++) {
                if (amountString.charAt(amountString.length() - 1 - i) != '0') {
                    isInt = false;
                }
            }

            if (!isInt && amountString.length() - 1 - amountString.indexOf(".") == 1) {
                amountString.append("0");
            }
        }

        if (isInt) {
            amountString.delete(amountString.indexOf("."), amountString.length() - 1);
        }

        return "$" + amountString.toString();
    }

    public DTTransaction getTransaction() {
        return this.transaction;
    }

    @Override
    public int hashCode() {
        // return this.objectId.hashCode();
        return this.dateCreated.hashCode();
    }

    public DTUser getOtherUser() {
        return this.creditor.equals(UserHomeActivity.getCurrentUser()) ? this.debtor : this.creditor;
    }

    // Saves the debt to Parse, including the relationship to the transaction
    @Deprecated
    // Functionality replaced with cloud code
    public void save(ParseObject transaction) {
        this.parseObject.put("transaction", transaction);
        try {
            this.parseObject.save();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    @Override
    public String toString() {
        return String.format("%s: %s owes %s $%s for %s", this.dateCreated.hashCode(), this.debtor, this.creditor, this.amount, this.transaction.getDescription());
    }
}
