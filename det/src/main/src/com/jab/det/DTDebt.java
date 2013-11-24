package com.jab.det;

import java.io.Serializable;
import java.util.HashSet;

import com.parse.ParseException;
import com.parse.ParseObject;

@SuppressWarnings("serial") // Serialized object will be deserialized in the same context
public class DTDebt implements Serializable {
	
	private String objectId;
	private Number amount;
	private DTUser creditor;
	private DTUser debtor;
	private DTTransaction transaction;
	private transient ParseObject parseObject;
	
	// Added from DTUser.getDebts
	public DTDebt(ParseObject parseObject) {
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
	
	public DTUser getDebtor() {
		return debtor;
	}
	
	public DTDebt(DTUser creditor, DTUser debtor, Number amount, DTTransaction transaction) {
		this.creditor = creditor;
		this.debtor = debtor;
		this.amount = amount;
		this.transaction = transaction;
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
	
	public DTUser getCreditor() {
		return this.creditor;
	}
		
	public void setParseObject(ParseObject parseObject) {
		this.parseObject = parseObject;
	}
	
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	// Saves the debt to Parse, including the relationship to the transaction
	@Deprecated // Functionality replaced with cloud code
	public void save(ParseObject transaction) {
		this.parseObject.put("transaction", transaction);
		try {
			this.parseObject.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return !(o instanceof DTDebt) || o.equals(null) ? false : this.objectId.equals(((DTDebt) o).getObjectId());
	}
	
	@Override
	public int hashCode() {
		return this.objectId.hashCode();
	}
}
