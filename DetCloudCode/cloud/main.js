/*
{
    "123": {
        "name": "justin",
        "email": "justingotemail@gmail.com",
        "amount": 50
    },
    “456”: {
        ...
        
    }
    "fbIdentifiers": [
                      "123", “456”
                      ],
    "creditor": "9t6UZpDJnp",
    "description": "CLOUD CODE WORKS"
}
*/

// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("createTransaction", function(request, response) {
	var facebookIds = request.params.fbIdentifiers;
	var numUsers = facebookIds.length;
	
	// initialize an array to hold all created debts
	var savedDebts = new Object();
	
	// initialize a counter of debts to save
	var debtsLeftToSave = facebookIds.length;

	//User and Debt Objects
	var ParseUsers = Parse.Object.extend("User");
	var Debt = Parse.Object.extend("Debt");

	//create this initial transaction object
	var Transaction = Parse.Object.extend("Transaction");
	var transaction = new Transaction();
	transaction.set("description", request.params["description"]);

	transaction.save(null, {
	  success: function(transaction) {
	  	//transaction success

		//get the initial creditor user via query
		var creditorQuery = new Parse.Query(ParseUsers);
		creditorQuery.get(request.params["user"], {
		  success: function(myUser) {
		    // The user was retrieved successfully.
		    // Try to find users already in DB
			var existingQuery = new Parse.Query(ParseUsers);
			existingQuery.containedIn("fbID", facebookIds);
		    existingQuery.find({
		  	success: function(results) {

		  		// found users that exist in DB
			    if (results.length > 0) {
			    	// for each existing user, create debt, and remove their ID from facebookIDs
			    	for (var i = 0; i < results.length; i++) {
			    		var existingUser = results[i];

			    		//create debt object for this existing user
  						var debt = new Debt();
  						var userFbID = existingUser.get("fbID"); 
              var amount = request.params[userFbID]["amount"];
              if (amount > 0) {
                debt.set("debtor", existingUser);
                debt.set("creditor", myUser);
              } else {
                debt.set("debtor", myUser);
                debt.set("creditor", existingUser);
              }
  						
  						debt.set("amount", request.params[userFbID]["amount"]);
  						debt.set("transaction", transaction);
  						debt.save(null, {
  						  success: function(debt) {
  						    var pulledFBID = debt.get("debtor").get("fbID");
  						    savedDebts[pulledFBID] = debt;
  						  	debtsLeftToSave--;
  						  	if (debtsLeftToSave == 0) {
  						  		savedDebts["transaction"] = transaction;
  								  response.success(savedDebts);
  						  	}
  						  },
  						  error: function(debt, error) {
  						 	 response.error('failed to create debt: ' + error.description);
  						  }
  						});

			    		//remove user fbID from facebookIDs array
			    		var index = facebookIds.indexOf(existingUser.get("fbID"));
			    		if (index > -1) {
	    					facebookIds.splice(index, 1);
              }
			    	}
			    }
			    // we now have the number of users left to signup in faecbookIds. 
			    // for each user to create, signup user. 
		    	var numToCreate = facebookIds.length;
		    	
		    	//make an array for users after signup to debit
		    	var usersToCreateDebts = new Array();
		    	
		    	for (var i = 0; i < facebookIds.length; i++) {
		    		var fbID = facebookIds[i];
		    		var user = new Parse.User();
  					user.set("username", fbID.toString());
  					user.set("password", "password");
  					user.set("name", request.params[fbID]['name'])
  					user.set("email", request.params[fbID]['email']);
  					user.set("fbID", fbID.toString());
  					 
  					user.signUp(null, {
  					  success: function(user) {
  					    usersToCreateDebts.push(user);
  					  	numToCreate--;
  					  	if (numToCreate == 0) {
  					  		for (var i = 0; i < usersToCreateDebts.length; i++) {
    								var debt = new Debt();
    								var newUser = usersToCreateDebts[i];
    								var userFbID = newUser.get("fbID");
    								
    								var amount = request.params[userFbID]["amount"];
                    if (amount > 0) {
                      debt.set("debtor", newUser);
                      debt.set("creditor", myUser);
                    } else {
                      debt.set("debtor", myUser);
                      debt.set("creditor", newUser);
                    }
    								debt.set("amount", request.params[userFbID]["amount"]);
    								debt.set("transaction", transaction);
    								debt.save(null, {
    								  success: function(debt) {
    								    var pulledFBID = debt.get("debtor").get("fbID");
                        savedDebts[pulledFBID] = debt;
    								  	debtsLeftToSave--;
    								  	if (debtsLeftToSave == 0) {
    								  	  savedDebts["transaction"] = transaction;
    								  		response.success(savedDebts);
    								  	}
    								  },
    								  error: function(debt, error) {
    								 	response.error('failed to create debt: ' + error.description);
    								  }
    								});
  					  		}
  					  	}
  					  },
  					  error: function(user, error) {
  					    // Show the error message somewhere and let the user try again.
  					    response.error('creating a user failed ' + error.message + ' existing user: ' + results[0].get("fbID"));
  					  }
					});
		    	}
			},
			error: function(error) {
				response.error("Error: " + error.code + " " + error.message);
			}
		});
		  },
		  error: function(object, error) {
		    // The object was not retrieved successfully.
		    // error is a Parse.Error with an error code and description.
		    response.error("Cant find creditor: " + request.params["user"] + error.code + " " + error.message);
		  }
		});
	  },
	  error: function(transaction, error) {
	    // Execute any logic that should take place if the save fails.
	    // error is a Parse.Error with an error code and description.
	    response.error('failed to create trasnaction: ' + error.description);
	  }
	});
});