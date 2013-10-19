package com.jab.det;

import com.parse.ParseObject;

public class DTDebt {
	
	public String objectId;
	public Number amount;
	public DTUser creditor;
	public DTUser debtor;
	public DTTransaction transaction;
	
	public DTDebt(ParseObject parseObject) {
		this.objectId = parseObject.getObjectId();
		this.amount = parseObject.getNumber("amount");
		this.creditor = new DTUser(parseObject.getString("creditor"));
		this.debtor = new DTUser(parseObject.getString("debtor"));
		this.transaction = new DTTransaction(parseObject.getString("transaction"));
	}
	
	public String toString() {
		return this.debtor.toString() + " owes " + this.creditor.toString() + " $" + this.amount;
	}
}
