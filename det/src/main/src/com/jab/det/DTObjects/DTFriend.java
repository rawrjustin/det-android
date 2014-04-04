package com.jab.det.DTObjects;

import java.util.List;

/**
 * This class encapsulates a debt relationship between the current user
 * and one of the user's friends. In the home activity, each tile represents
 * a collection of debts between these two people.
 */
public class DTFriend {

    private DTUser friend;
    private List<DTDebt> debts;
    private double amountOwedToCurrentUser;

    public DTFriend(DTUser friend, List<DTDebt> debts) {
        this.friend = friend;
        this.debts = debts;

        for (DTDebt debt : debts) {
            if (debt.getCreditor().equals(friend)) {
                this.amountOwedToCurrentUser -= debt.getAmount().doubleValue();
            } else {
                this.amountOwedToCurrentUser += debt.getAmount().doubleValue();
            }
        }
    }

    public boolean removeDebt(DTDebt debt) {
        if (!debts.contains(debt)) {
            return false;
        }

        this.debts.remove(debt);
        if (debt.getCreditor().equals(friend)) {
            this.amountOwedToCurrentUser += debt.getAmount().doubleValue();
        } else {
            this.amountOwedToCurrentUser -= debt.getAmount().doubleValue();
        }

        return true;
    }

    public DTUser getFriend() {
        return this.friend;
    }

    public double getAmountOwedToCurrentUser() {
        return this.amountOwedToCurrentUser;
    }

    @Override
    public boolean equals(Object o) {
        return !(o instanceof DTFriend) || o.equals(null) ? false : this.friend.getFacebookId().equals(((DTFriend) o).getFriend().getFacebookId());
    }

    @Override
    public int hashCode() {
        return this.friend.getFacebookId().hashCode();
    }
}
