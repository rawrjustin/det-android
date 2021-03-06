package com.jab.det.DTObjects;

import java.util.Date;

import com.jab.det.UserHomeActivity;
import com.parse.ParseObject;

public class DTDebt {

    private Number amount;
    private DTUser creditor;
    private DTUser debtor;
    private ParseObject parseObject;
    private DTTransaction transaction;
    private Date updatedAt;

    public DTDebt(DTUser creditor, DTUser debtor, Number amount, DTTransaction transaction) {
        this.creditor = creditor;
        this.debtor = debtor;
        this.amount = amount;
        this.transaction = transaction;
    }

    // Added from DTUser.getDebts
    public DTDebt(ParseObject parseObject) {
        this.amount = parseObject.getNumber("amount");
        this.creditor = new DTUser(parseObject.getParseUser("creditor").getString("fbID"), parseObject.getParseUser("creditor").getString("name"));
        this.debtor = new DTUser(parseObject.getParseUser("debtor").getString("fbID"), parseObject.getParseUser("debtor").getString("name"));
        this.updatedAt = parseObject.getUpdatedAt();
        this.parseObject = parseObject;
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof DTDebt) || o.equals(null) ? false : this.parseObject.getObjectId().equals(((DTDebt) o).parseObject.getObjectId());
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

    public ParseObject getParseObject() {
        return parseObject;
    }

    public DTTransaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(DTTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public int hashCode() {
        return this.parseObject.getObjectId().hashCode();
    }

    public DTUser getOtherUser() {
        return this.creditor.equals(UserHomeActivity.getCurrentUser()) ? this.debtor : this.creditor;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public String toString() {
        return String.format("%s: %s owes %s $%s for %s", this.parseObject.getObjectId().hashCode(), this.debtor, this.creditor, this.amount, this.transaction.getDescription());
    }
}
