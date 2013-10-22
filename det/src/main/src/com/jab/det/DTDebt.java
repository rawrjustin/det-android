package com.jab.det;

import com.parse.ParseException;
import com.parse.ParseObject;

public class DTDebt {
	
	private String objectId;
	private Number amount;
	private DTUser creditor;
	private DTUser debtor;
	private DTTransaction transaction;
	private ParseObject parseObject;
	
	public DTDebt(ParseObject parseObject) {
		this.objectId = parseObject.getObjectId();
		this.amount = parseObject.getNumber("amount");
		this.creditor = DTUser.getUserFromObjectId(parseObject.getString("creditor"));
		this.debtor = DTUser.getUserFromObjectId(parseObject.getString("debtor"));
		this.transaction = new DTTransaction(parseObject.getString("transaction"));
		this.parseObject = parseObject;
	}
	
	public DTDebt(DTUser creditor, DTUser debtor, Number amount, DTTransaction transaction) {
		this.creditor = creditor;
		this.debtor = debtor;
		this.amount = amount;
		this.transaction = transaction;
		
		this.parseObject = new ParseObject("Debt");
		this.parseObject.put("creditor", creditor.getParseUser());
		this.parseObject.put("debtor", debtor.getParseUser());
		this.parseObject.put("amount", amount);
		this.parseObject.put("transaction", transaction);
		
		// When to save DTDebt object?
	}
	
	public ParseObject getParseObject() {
		return parseObject;
	}

	public String toString() {
		return this.debtor.toString() + " owes " + this.creditor.toString() + " $" + this.amount;
	}
	
	// Saves the debt to Parse, including the relationship to the transaction
	public void save(ParseObject transaction) {
		this.parseObject.put("transaction", transaction);
		try {
			this.parseObject.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
