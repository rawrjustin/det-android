package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;

public class DTTransaction {

	private String description;
	private Collection<DTDebt> debts;
	private DTUser currentUser;
	private ParseObject parseObject;
	
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
			inputStr = inputStr.substring(0, inputStr.indexOf(".") + 2);
		}
		
		return Double.valueOf(inputStr);
	}
	
	// Gets description
	public String getDescription() {
		return this.description;
	}
}
