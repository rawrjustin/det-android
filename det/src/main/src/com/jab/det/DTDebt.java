package com.jab.det;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.parse.ParseException;
import com.parse.ParseObject;

public class DTDebt implements Serializable {
	
	private String objectId;
	private Number amount;
	private DTUser creditor;
	private DTUser debtor;
	private DTTransaction transaction;
	private transient ParseObject parseObject;
	private String toString;
	
	public DTDebt(ParseObject parseObject) {
		try {
			parseObject.fetchIfNeeded();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.objectId = parseObject.getObjectId();
		this.amount = parseObject.getNumber("amount");
		this.creditor = DTUser.getUserFromParseUser(parseObject.getParseUser("creditor"));
		this.debtor = DTUser.getUserFromParseUser(parseObject.getParseUser("debtor"));
		ParseObject parseTransaction = parseObject.getParseObject("transaction");
		this.transaction = new DTTransaction(parseTransaction);
		this.parseObject = parseObject;
		this.toString = String.format("%s owes %s $%s for %s", this.debtor.toString(), this.creditor.toString(), this.amount.toString(), this.transaction.getDescription());
	}
	
	public DTUser getDebtor() {
		return debtor;
	}
	
	public DTDebt(DTUser creditor, DTUser debtor, Number amount) {
		this.creditor = creditor;
		this.debtor = debtor;
		this.amount = amount;
		this.parseObject = new ParseObject("Debt");
		this.parseObject.put("creditor", creditor.getParseUser());
		this.parseObject.put("debtor", debtor.getParseUser());
		this.parseObject.put("amount", amount);
	}
	
	public ParseObject getParseObject() {
		return parseObject;
	}

	public String toString() {
		return String.format("%s owes %s $%s for %s", this.debtor.toString(), this.creditor.toString(), this.amount.toString(), this.transaction.getDescription());
	}
	
	public DTTransaction getTransaction() {
		return this.transaction;
	}
	
	public String getObjectId() {
		return this.objectId;
	}
	
	public Number getAmount() {
		return this.amount;
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

	public static ArrayList<DTDebt> fromDebtData(ArrayList<HashMap<String, String>> debtsFromIntent) {
		ArrayList<DTDebt> debts = new ArrayList<DTDebt>();
		for (HashMap<String, String> debt : debtsFromIntent) {
			
		}
		return null;
	}
}
