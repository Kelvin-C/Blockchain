package dataentities.block.record;

import blockchain.UserManager;
import dataentities.user.User;

/** Wraps information on the transfer of currency */
public class CurrencyTransfer extends RecordValue {

    public final long fromUserId;
    public final int amount;
    public final long toUserId;

    public CurrencyTransfer(long fromUserId, int amount, long toUserId) {
        this.fromUserId = fromUserId;
        this.amount = amount;
        this.toUserId = toUserId;
    }

    @Override
    public String toString() {
        User fromUser = UserManager.getUser(fromUserId);
        User toUser = UserManager.getUser(toUserId);
        return String.format("%s sent %s VC to %s", fromUser.name, amount, toUser.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyTransfer that = (CurrencyTransfer) o;
        return fromUserId == that.fromUserId &&
                amount == that.amount &&
                toUserId == that.toUserId;
    }
}


