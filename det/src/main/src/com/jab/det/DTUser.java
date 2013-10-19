package com.jab.det;

import com.parse.ParseUser;

/**
 * Created by justinhuang on 10/14/13.
 */
public class DTUser {

    public String objectID;
    public String email;
    public String facebookID;
    public String username;
    public String password;
    public String name;

    public DTUser(String fbID, String name, String email)
    {
    	this.facebookID = fbID;
    	this.name = name;
    	this.email = email;
    }
    
    public DTUser()
    {
    	
    }
    
    public DTUser(ParseUser currentUser)
    {
		if (currentUser == null)
		{
			return;
		}
		
		this.objectID = currentUser.getObjectId();
		this.email = currentUser.getString("email");
		this.facebookID = currentUser.getString("fbID");
		this.username = currentUser.getUsername();
		this.password = "";
		this.name = currentUser.getString("name");
	}

	/*
    Returns a DTUser representing the current user.
     */
    public static DTUser getCurrentUser()
    {
        return new DTUser(ParseUser.getCurrentUser());
    }

    /*
    Returns the user with given facebook id. If the user did not already exist, a default DTUser with the facebook id will be created.
     
    public static DTUser getOrCreateUser(String fbID, String name) {
        return new DTUser();
    }
     */
    /*
    Either creates a new user, or links with existing PFuser in database
     */
    public static void createOrLinkUser(String fbID, String name, String email) {

    }

    /*
    Updates the PFUser associated with this user with the user's property values.
     */
    public void saveUser() {

    }

    /*
    Creates new user in the Parse database.
     */
    public void registerUser() {

    }

    /*
    Given a facebook id, creates a place holder DTUser and registers it as a PFUser in the parse database.
     */
    public DTUser initDefaultUser(String fbID, String name, String email) {
        return new DTUser(fbID, name, email);
    }
}
