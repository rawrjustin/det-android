
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.define("createTransaction", function(request, response) {
	var confirmedUsers = new Array();
	var facebookIds = request.params.fbIdentifiers;
	var numUsers = facebookIds.length;

  	var ParseUsers = Parse.Object.extend("Users");
	var query = new Parse.Query(ParseUsers);
	query.containedIn("fbID", facebookIds);
	query.find({
	  	success: function(results) {
	  		// found users that exist in DB
		    if (results.length > 0) {
		    	// for each existing user, add to confirmedUsers, and remove their ID from facebookIDs
		    	for (var i = 0; i < results.length; i++) {
		    		var existingUser = results[i];
		    		confirmedUsers.push(existingUser);
		    		var index = facebookIds.indexOf(existingUser);
		    		if (index > -1) {
    					facebookIds.splice(index, 1);
					}
		    	}
		    }
		    // we now have the number of users left to signup in faecbookIds. 
		    // for each user to create, signup user. 
	    	var numToCreate = facebookIds.length;
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
				  	confirmedUsers.push(user)
				  	numToCreate--;
				  	if (numToCreate == 0) {
				  		var Transaction = Parse.Object.extend("Transaction");
						var transaction = new Transaction();
						var debts = new Array();
						var Debt = Parse.Object.extend("Debt");

						 
						transaction.set("description", request.params["description"]);
				  		for (var i = 0; i < numUsers; i++) {
							var debt = new Debt();

							debt.set("debtor", confirmedUsers[i]);
							debt.set("creditor", request.params["creditor"]);
							debt.set("amount", request.params[confirmedUsers[i].get("fbID")]["amount"]);
							debts.push(debt);
				  		}
				  		transaction.save(null, {
						  success: function(transaction) {
						    // Execute any logic that should take place after the object is saved.
						    response.success();
						  },
						  error: function(transaction, error) {
						    // Execute any logic that should take place if the save fails.
						    // error is a Parse.Error with an error code and description.
						    response.error('failed to create trasnaction: ' + error.description);
						  }
						});
				  	}
				  },
				  error: function(user, error) {
				    // Show the error message somewhere and let the user try again.
				    response.error('creating a user failed' + error.message);
				  }
				});
	    	}
		},
		error: function(error) {
			response.error("Error: " + error.code + " " + error.message);
		}
	});
});