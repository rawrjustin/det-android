package com.jab.det;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;

public class DTUser {

    private String objectId;
    private String email;
    private String facebookID;
    private String username;
    private String password;
    private String name;
    
    // Constructs DTUser with fields
    public DTUser(String objectId, String email, String facebookID, String username, String password, String name) {
    	this.objectId = objectId;
    	this.email = email;
    	this.facebookID = facebookID;
    	this.username = username;
    	this.password = password;
    	this.name = name;
    }

    // Constructs DTUser object with objectId
    public static DTUser getUserFromObjectId(String objectIdParam) {
    	ParseQuery<ParseObject> query = ParseQuery.getQuery("user");
		ParseObject parseObject;
		String objectId = null, email = null, facebookID = null, username = null, password = null, name = null;
		try {
			parseObject = query.get(objectIdParam);
			objectId = parseObject.getObjectId();
			email = parseObject.getString("email");
			facebookID = parseObject.getString("fbID");
			username = parseObject.getString("username");
			password = "";
			name = parseObject.getString("name");

		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return new DTUser(objectId, email, facebookID, username, password, name);
    }
    
    // Returns a DTUser representing the current user.
    public static DTUser getCurrentUser() {
        return getUserFromParseUser(ParseUser.getCurrentUser());
    }

    // Returns a DTUser given a ParseUser object
    public static DTUser getUserFromParseUser(ParseUser parseUser) {
		if (parseUser == null) {
			return null;
		}
		
		String objectId = parseUser.getObjectId();
		String email = parseUser.getString("email");
		String facebookID = parseUser.getString("fbID");
		String username = parseUser.getUsername();
		String password = "";
		String name = parseUser.getString("name");
		
		return new DTUser(objectId, email, facebookID, username, password, name);
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
    	return getName();
    }

	// Gets DTUser name
	public String getName() {
		return this.name;
	}
	
	// Gets ParseUser
	public ParseUser getParseUser() {
		// TODO: Change this to a field and add logic to constructors
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("fbID", this.facebookID);
		ParseUser parseUser = null;
		try {
			List<ParseUser> queryResult = query.find();
			if (queryResult.size() != 1) {
				Log.e(DetApplication.TAG, "DTUser query for parse user using facebook ID did not return one element");
			} else {
				parseUser = queryResult.get(0);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parseUser;
	}
}
