package com.jab.det;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseException;
import com.parse.codec.binary.StringUtils;

public class DTUser implements Serializable {

	private String objectId;
    private String email;
    private String facebookID;
    private String name;
    public static transient ParseUser currentParseUser;
    private static String defaultPassword = "password";
    
    // Constructs DTUser with fields
    public DTUser(String objectId, String email, String facebookID, String username, String password, String name) {
    	this.objectId = objectId;
    	this.email = email;
    	this.facebookID = facebookID;
    	this.name = name;
    }
    
    public DTUser(String facebookID, String name) {
    	this.facebookID = facebookID;
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
        return getUserFromParseUser(currentParseUser == null ? ParseUser.getCurrentUser() : currentParseUser);
    }

    // Returns a DTUser given a ParseUser object
    public static DTUser getUserFromParseUser(ParseUser parseUser) {
    	if (parseUser == null) {
			return null;
		}
		
    	try {
			parseUser.fetchIfNeeded();
		} catch (ParseException e) {
			Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
			e.printStackTrace();
		}
    	
		String objectId = null, facebookID = null, name = null, email = null, username = null, password = generatePassword(false);
		objectId = parseUser.getObjectId();
		name = parseUser.getString("name");
		facebookID = parseUser.getString("fbID");
		username = parseUser.getUsername();
		email = parseUser.isAuthenticated() ? parseUser.getString("email") : "";
		
		
		return new DTUser(objectId, email, facebookID, username, password, name);
	}
    
    // Returns consolidated debts as a list of user integer pairs, representing users and how much they owe currentUser
    public List<AbstractMap.SimpleEntry<DTUser, Integer>> getConsolidatedDebts() {
		return null;
    }
    
    // Returns a DTDebt array of all debts user is a part of
    public DTDebt[] getDebts() {
    	UserHomeActivity.transactionsMap = new HashMap<DTTransaction, HashSet<DTDebt>>();
    	UserHomeActivity.transactionsObjectIdToDTTransaction = new HashMap<String, DTTransaction>();	
    	UserHomeActivity.usersMap = new HashMap<DTUser, HashSet<DTDebt>>();
    	
    	ArrayList<DTDebt> debts = new ArrayList<DTDebt>();
    	    	
    	// Query debts where user is creditor
    	try {
    		// Query debts where user is debtor
	    	ParseQuery<ParseObject> creditorsQuery = ParseQuery.getQuery("Debt");
	    	creditorsQuery.whereEqualTo("creditor", DTUser.currentParseUser);
	    		    		
	    	// Query debts where user is creditor
	    	ParseQuery<ParseObject> debtorsQuery = ParseQuery.getQuery("Debt");
	    	debtorsQuery.whereEqualTo("debtor", DTUser.currentParseUser);
	    	
	    	// Main query to query for debts where the user is the creditor or debtor
	    	List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
	    	queries.add(creditorsQuery);
	    	queries.add(debtorsQuery);
	    	ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
	    	
	    	for (ParseObject queryResult : mainQuery.find()) {
	    		debts.add(new DTDebt(queryResult));
	    	}
    	} catch (ParseException e) {
    		Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
    		e.printStackTrace();
    	}
    	    	
    	// Setup maps and add to aggregate totals
    	for (DTDebt debt : debts) {
    		// Find out whether current user is debtor or creditor
    		DTUser userThatIsNotCurrentUser = null;
    		
    		// Increment aggregate totals
    		if (debt.getCreditor().equals(UserHomeActivity.getCurrentUser())) {
    			userThatIsNotCurrentUser = debt.getDebtor();
        		Log.d(DetApplication.TAG, "DETAPP: Inside DTUser:138");

    			Log.d(DetApplication.TAG, String.format("Adding %s to amountOwedToYou", debt.getAmount().toString()));
    			UserHomeActivity.amountOwedToYou += debt.getAmount().doubleValue();
    		} else {
    			userThatIsNotCurrentUser = debt.getCreditor();
    			Log.d(DetApplication.TAG, String.format("Adding %s to amountOwedToOthers", debt.getAmount().doubleValue()));
    			UserHomeActivity.amountOwedToOthers += debt.getAmount().doubleValue();
    		}
    		
    		// Add to users map
    		if (!UserHomeActivity.usersMap.containsKey(userThatIsNotCurrentUser)) {
    			UserHomeActivity.usersMap.put(userThatIsNotCurrentUser, new HashSet<DTDebt>());
    		}

    		UserHomeActivity.usersMap.get(userThatIsNotCurrentUser).add(debt);
    	}
    	
    	// Associate DTTransaction objects with their respective debts
    	for (Entry<DTTransaction, HashSet<DTDebt>> keyValuePair : UserHomeActivity.transactionsMap.entrySet()) {
    		keyValuePair.getKey().setDebts(new ArrayList<DTDebt>(keyValuePair.getValue()));
    		Log.d(DetApplication.TAG, "DETAPP " + keyValuePair.getKey().toString());
    	}
    	
    	return debts.toArray(new DTDebt[debts.size()]);
    }
    
    // Returns the user with given facebook id. If the user did not already exist, a default DTUser with the facebook id will be created.
    @Deprecated // Functionality replaced by cloud code
    public static DTUser getOrCreateUser(String fbID, String name) {
    	// Query ParseUser table for entry for fbID.
    	ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("fbID", fbID);
		List<ParseUser> queryResult = null;
		try {
			queryResult = query.find();
		} catch (ParseException e) {
			Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
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
			Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
			e.printStackTrace();
		}
        return getUserFromParseUser(parseUser);
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
			Log.e(DetApplication.TAG, "DETAPP ERROR: " + e.toString());
			e.printStackTrace();
		}
		
		return parseUser;
	}
	
	public static String generatePassword(Boolean random) {
		return random ? new BigInteger(130, new SecureRandom()).toString(32) : defaultPassword;
	}

	public String getEmail() {
		return this.email;
	}

	@Override
	public boolean equals(Object o) {
		return !(o instanceof DTUser) || o.equals(null) ? false : this.objectId.equals(((DTUser) o).getObjectId());
	}
	
	@Override
	public int hashCode() {
		return this.facebookID.hashCode();
	}
}
