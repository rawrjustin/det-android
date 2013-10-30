package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;

public class DTTransaction {

	private String description;
	private Collection<DTDebt> debts;
	private DTUser currentUser;
	private ParseObject parseObject;
	private String objectId;
	
	public DTTransaction(String objectId) {
		// TODO Auto-generated constructor stub
	}
	
	// Create a transaction split evenly
	public DTTransaction(DTUser currentUser, Collection<DTUser> otherUsers, Number amount, String description) {
		// Note: Implementation assumes current user is the creditor
		this.description = description;
		this.debts = new ArrayList<DTDebt>();
		this.parseObject = new ParseObject("Transaction");
		this.parseObject.put("description", this.description);
		for (DTUser user : otherUsers) {
			this.debts.add(new DTDebt(currentUser, user, trimDecimals(amount.doubleValue()/(otherUsers.size()+1))));
		}
	}
	
	public DTTransaction(ParseObject parseObject) {
		try {
			parseObject.fetchIfNeeded();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.parseObject = parseObject;
		this.objectId = parseObject.getObjectId();
		this.description = parseObject.getString("description");
		// TODO: Get debts
	}

	// Saves transaction (and corresponding debts) to Parse
	public void save() {
		for (DTDebt debt : this.debts) {
			debt.save(this.parseObject);
		}
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
	
	public Collection<DTDebt> getDebts() {
		return this.debts;
	}
	
	public String toString() {
		StringBuilder debts = new StringBuilder();
		for (DTDebt debt : this.debts) {
			debts.append(debt.getObjectId() + " ");
		}
		
		return String.format("Transaction %s has %s debts: %s", this.objectId, this.debts.size(), debts.toString());
	}
	
	public String getObjectId() {
		return this.objectId;
	}
	
	@Override
	public boolean equals(Object o) {
		return !(o instanceof DTTransaction) || o.equals(null) ? false : this.objectId.equals(((DTTransaction) o).getObjectId());
	}
	
	@Override
	public int hashCode() {
		return this.objectId.hashCode();
	}
}
