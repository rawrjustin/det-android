package com.jab.det.DTObjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jab.det.DetApplication;
import com.jab.det.UserHomeActivity;
import com.parse.ParseObject;

/**
 * This class contains static methods that act on DT objects in a static fashion
 */
public class DTUtils {
    /**
     * Method that returns the friend in a debt
     * 
     * @param debt
     * @return friend in the debt
     */
    public static DTUser getFriend(DTDebt debt) {
        if (debt.getCreditor().equals(UserHomeActivity.getCurrentUser())) {
            return debt.getDebtor();
        } else {
            return debt.getCreditor();
        }
    }

    /**
     * For all debt objects, associate them to unique transaction objects, and vice versa
     * 
     * @param debts
     */
    public static void associateDebtsTransactions(List<DTDebt> debts) {
        /**
         * Between multiple debt objects in a transaction, the parse objects are not unique. However,
         * their object IDs are. We can store in a hash map transaction object IDs to the debts that
         * they are associated with, and a reverse index of transaction ID to transaction parse object.
         * This way, we can iterate through the key value pairs and construct transaction objects that
         * are tied to their debts, which are in turn tied back to their transaction object.
         */
        Map<String, List<DTDebt>> transactionObjectIdToDebtsMap = new HashMap<String, List<DTDebt>>();
        Map<String, ParseObject> objectIdToTransactionParseObjectMap = new HashMap<String, ParseObject>();

        for (DTDebt debt : debts) {
            ParseObject transactionParseObject = debt.getParseObject().getParseObject(DetApplication.DEBT_TABLE_TRANSACTION_COLUMN_NAME);
            String transactionObjectId = transactionParseObject.getObjectId();

            objectIdToTransactionParseObjectMap.put(transactionObjectId, transactionParseObject);

            if (!transactionObjectIdToDebtsMap.containsKey(transactionObjectId)) {
                transactionObjectIdToDebtsMap.put(transactionObjectId, new ArrayList<DTDebt>(Arrays.asList(debt)));
            } else {
                transactionObjectIdToDebtsMap.get(transactionObjectId).add(debt);
            }
        }

        for (Map.Entry<String, List<DTDebt>> keyValuePair : transactionObjectIdToDebtsMap.entrySet()) {
            DTTransaction transaction = new DTTransaction(objectIdToTransactionParseObjectMap.get(keyValuePair.getKey()), keyValuePair.getValue());
            for (DTDebt debt : keyValuePair.getValue()) {
                debt.setTransaction(transaction);
            }
        }
    }

    // Prepends $ to amount and returns string
    public static String getDisplayableDollarAmount(Number amount) {
        // Making the string look pretty. Amounts will have no decimals if it is
        // an integer, two decimal places otherwise
        StringBuilder sb = new StringBuilder("$" + amount.toString());

        if (sb.toString().contains(".")) {
            String[] split = sb.toString().split("\\.");
            sb = new StringBuilder(split[0]);
            if (split[1].length() == 0) {

            } else if (split[1].length() == 1) {
                sb.append("." + split[1] + "0");
            } else if (split[1].length() == 2) {
                sb.append("." + split[1]);
            } else {
                sb.append("." + split[1].substring(0, 2));
            }
        }

        return sb.toString();
    }
}
