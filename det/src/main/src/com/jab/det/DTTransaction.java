package com.jab.det;

import java.util.ArrayList;
import java.util.Collection;

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
			this.debts.add(new DTDebt(currentUser, user, amount.doubleValue()/(otherUsers.size()+1), this));
		}
	}
	
	// Create a transaction using debts
	public DTTransaction(DTUser currentUser, Collection<DTDebt> debts, String description) {
		this.currentUser = currentUser;
		this.debts = debts;
		this.description = description;
	}
	
	// Saves transaction (and corresponding debts) to Parse
	public void save() {
		for (DTDebt debt : this.debts) {
			debt.save(this.parseObject);
		}
	}
}
