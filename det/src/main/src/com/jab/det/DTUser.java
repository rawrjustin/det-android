package com.jab.det;

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

    /*
    Returns a DTUser representing the current user.
     */
    public static DTUser getCurrentUser() {
        return new DTUser();
    }

    /*
    Returns the user with given facebook id. If the user did not already exist, a default DTUser with the facebook id will be created.
     */
    public static DTUser getOrCreateUser(String fbID, String name) {
        return new DTUser();
    }

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
        return new DTUser();
    }

    /*
    // Given a PFUser, returns a DTUser with property values copied from the PFUser
     */
    //TODO IMPORT PFUSER CLASSES
    public DTUser initWithPFUser(String PFUser) {
        return new DTUser();
    }


}
