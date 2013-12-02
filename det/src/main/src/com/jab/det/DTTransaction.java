package com.jab.det;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;

import com.parse.ParseObject;

@SuppressWarnings("serial") // Serialized object will be deserialized in the same context
public class DTTransaction implements Serializable {

	private String description;
	private ArrayList<DTDebt> debts;
	private transient ParseObject parseObject;
	private String objectId;
	private final String dateCreated;
	
	// Create a transaction split evenly
	public DTTransaction(DTUser currentUser, Collection<DTUser> otherUsers, Number amount, String description) {
		this.dateCreated = (new Date()).toString();
		// Note: Implementation assumes current user is the creditor
		this.description = description;
		this.debts = new ArrayList<DTDebt>();
		for (DTUser user : otherUsers) {
			this.debts.add(new DTDebt(currentUser, user, trimDecimals(amount.doubleValue()/(otherUsers.size()+1)), this));
		}
	}
	
	// Called from DTDebt(ParseObject), which is called by DTUser.getDebts()
	public DTTransaction(ParseObject parseObject) {
		this.dateCreated = (new Date()).toString();
		this.parseObject = parseObject;
		this.objectId = parseObject.getObjectId();
		this.description = parseObject.getString("description");
		// Debts added later in getDebts()
	}

	// Saves transaction (and corresponding debts) to Parse
	@Deprecated // Functionality implemented in cloud code
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
	
	public ArrayList<DTDebt> getDebts() {
		return this.debts;
	}
	
	public String toString() {
		StringBuilder debts = new StringBuilder();
		for (DTDebt debt : this.debts) {
			debts.append("    " + debt.getObjectId() + "|D:" + debt.getDebtor().getName() + "|C:" + debt.getCreditor().getName() + "|A:" + debt.getAmount() + "\n");
		}
		
		return String.format("Transaction %s has %s debts:\n%s", this.objectId, this.debts.size(), debts.toString());
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
		//return this.objectId.hashCode();
		return this.dateCreated.hashCode();
	}

	public HashMap<String, Object> getCloudCodeRequestObject() {
		HashMap<String, Object> requestObject = new HashMap<String, Object>();
		ArrayList<String> fbIds = new ArrayList<String>(); 
		requestObject.put("creditor", UserHomeActivity.getCurrentUser().getObjectId());
		requestObject.put("description", this.description);
		for (DTDebt debt : this.debts) {
			HashMap<String, Object> debtorMap = new HashMap<String, Object>();
			debtorMap.put("name", debt.getDebtor().getName());
			//debtorMap.put("email", debt.getDebtor().getEmail());
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
	
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public void removeDebt(DTDebt debt) {
		this.debts.remove(debt);
	}
}
