package com.jab.det;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;

public class DTUser {

    public String objectId;
    public String email;
    public String facebookID;
    public String username;
    public String password;
    public String name;
    
    // Constructs DTUser object with ParseUser object
    public DTUser(ParseUser parseUser) {
		if (parseUser == null) {
			return;
		}
		
		this.objectId = parseUser.getObjectId();
		this.email = parseUser.getString("email");
		this.facebookID = parseUser.getString("fbID");
		this.username = parseUser.getUsername();
		this.password = "";
		this.name = parseUser.getString("name");
	}

    // Constructs DTUser object with objectId
    public DTUser(String objectId) {
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
		ParseObject parseObject;
		try {
			parseObject = query.get(objectId);
			this.objectId = parseObject.getObjectId();
			this.email = parseObject.getString("email");
			this.facebookID = parseObject.getString("fbID");
			this.username = parseObject.getString(username);
			this.password = "";
			this.name = parseObject.getString("name");

		} catch (ParseException e) {
			e.printStackTrace();
		}
    }
    
    // Returns a DTUser representing the current user.
    public static DTUser getCurrentUser() {
        return new DTUser(ParseUser.getCurrentUser());
    }

    // return consolidated debts as a list of user integer pairs, representing users and how much they owe currentUser
    public List<AbstractMap.SimpleEntry<DTUser, Integer>> getConsolidatedDebts() {
		return null;
    }
    
    // Returns a DTDebt array of all debts user is a part of
    public DTDebt[] getDebts() {
    	List<DTDebt> debts = new ArrayList<DTDebt>();
    	
    	// Query debts where user is creditor
    	try {
	    	ParseQuery<ParseObject> creditorsQuery = ParseQuery.getQuery("Debt");
	    	creditorsQuery.whereEqualTo("creditor", objectId);
	    	for (ParseObject queryResult : creditorsQuery.find()) {
	    		debts.add(new DTDebt(queryResult));
	    	}
	
	    	// Query debts where user is creditor
	    	ParseQuery<ParseObject> debtorsQuery = ParseQuery.getQuery("Debt");
	    	debtorsQuery.whereEqualTo("creditor", objectId);
	    	for (ParseObject queryResult : debtorsQuery.find()) {
	    		debts.add(new DTDebt(queryResult));
	    	}
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}

    	return debts.toArray(new DTDebt[debts.size()]);
    }
    
    // Returns the user with given facebook id. If the user did not already exist, a default DTUser with the facebook id will be created.
    public static DTUser getOrCreateUser(String fbID, String name) {
        return null;
    }

    // Creates new user in the Parse database
    public void registerUser() {
    }
    
    // Implements toString() behavior
    public String toString() {
    	return name;
    }
}
