package com.jab.det;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.ParseException;

public class DTUser {

    private String objectId;
    private String email;
    private String facebookID;
    private String username;
    private String password;
    private String name;
    public static ParseUser currentParseUser;
    public static String defaultPassword = "password";
    
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
    	currentParseUser = currentParseUser == null ? ParseUser.getCurrentUser() : currentParseUser;
        return getUserFromParseUser(currentParseUser);
    }

    // Returns a DTUser given a ParseUser object
    public static DTUser getUserFromParseUser(ParseUser parseUser) {
    	if (parseUser == null) {
			return null;
		}
		
    	try {
			parseUser.fetch();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		String objectId = null, facebookID = null, name = null, email = null, username = null, password = generatePassword(false);
		objectId = parseUser.getObjectId();
		name = parseUser.getString("name");
		facebookID = parseUser.getString("fbID");
		username = parseUser.getUsername();
		
		if (parseUser.isAuthenticated()) {
			email = parseUser.getString("email");
		} else {
			email = "";
		}
		
		return new DTUser(objectId, email, facebookID, username, password, name);
	}
    
    // Returns consolidated debts as a list of user integer pairs, representing users and how much they owe currentUser
    public List<AbstractMap.SimpleEntry<DTUser, Integer>> getConsolidatedDebts() {
		return null;
    }
    
    // Returns a DTDebt array of all debts user is a part of
    public DTDebt[] getDebts() {
    	ArrayList<DTDebt> debts = new ArrayList<DTDebt>();
    	    	
    	// Query debts where user is creditor
    	try {
    		// Query debts where user is debtor
	    	ParseQuery<ParseObject> creditorsQuery = ParseQuery.getQuery("Debt");
	    	creditorsQuery.whereEqualTo("creditor", DTUser.currentParseUser);
//	    	for (ParseObject queryResult : creditorsQuery.find()) {
//	    		debts.add(new DTDebt(queryResult));
//	    	}
	    		    		
	    	// Query debts where user is creditor
	    	ParseQuery<ParseObject> debtorsQuery = ParseQuery.getQuery("Debt");
	    	debtorsQuery.whereEqualTo("debtor", DTUser.currentParseUser);
//	    	for (ParseObject queryResult : debtorsQuery.find()) {
//	    		debts.add(new DTDebt(queryResult));
//	    	}
	    	
	    	// Main query to query for debts where the user is the creditor or debtor
	    	List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
	    	queries.add(creditorsQuery);
	    	queries.add(debtorsQuery);
	    	ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
	    	
	    	for (ParseObject queryResult : mainQuery.find()) {
	    		debts.add(new DTDebt(queryResult));
	    	}
    	} catch (ParseException e) {
    		e.printStackTrace();
    	}
    	
    	// Map transactions to their debts
    	HashMap<DTTransaction, HashSet<DTDebt>> transactionsMap = new HashMap<DTTransaction, HashSet<DTDebt>>();
    	for (DTDebt debt : debts) {
    		if (!transactionsMap.containsKey(debt.getTransaction())) {
    			transactionsMap.put(debt.getTransaction(), new HashSet<DTDebt>(Arrays.asList(debt)));
    		} else {
    			transactionsMap.get(debt.getTransaction()).add(debt);
    		}
    	}
    	
    	// Associate DTTransaction objects with their debts
    	for (Entry<DTTransaction, HashSet<DTDebt>> keyValuePair : transactionsMap.entrySet()) {
    		for (DTDebt debt : keyValuePair.getValue()) {
    			debt.getTransaction().setDebts(new ArrayList<DTDebt>(keyValuePair.getValue()));
    		}
    		Log.d(DetApplication.TAG, "DETAPP " + keyValuePair.getKey().toString());
    	}
    	
    	return debts.toArray(new DTDebt[debts.size()]);
    }
    
    // Returns the user with given facebook id. If the user did not already exist, a default DTUser with the facebook id will be created.
    public static DTUser getOrCreateUser(String fbID, String name) {
    	// Query ParseUser table for entry for fbID.
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("fbID", fbID);
		List<ParseUser> queryResult = null;
		try {
			queryResult = query.find();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (queryResult.size() == 1) {
			return getUserFromParseUser(queryResult.get(0));
		}
		
		ParseUser parseUser = new ParseUser();
		parseUser.put("fbID", fbID);
		parseUser.put("name", name);
		parseUser.setUsername(fbID);
		parseUser.setPassword(generatePassword(false));
		
		try {
			parseUser.signUp();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return getUserFromParseUser(parseUser);
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
	
	public String getFacebookId() {
		return this.facebookID;
	}
	
	// Gets ObjectId
	public String getObjectId() {
		return this.objectId;
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
	
	public static String generatePassword(Boolean random) {
		return random ? new BigInteger(130, new SecureRandom()).toString(32) : defaultPassword;
	}
}
