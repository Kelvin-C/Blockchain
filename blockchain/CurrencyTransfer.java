package blockchain;

/** Wraps information on the transfer of currency */
class CurrencyTransfer extends RecordValue {

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

/** The reward to the miner for mining a currency transfer block */
class CurrencyTransferReward extends CurrencyTransfer {

    public CurrencyTransferReward(long fromUserId, int amount, long toUserId) {
        super(fromUserId, amount, toUserId);
    }

    @Override
    public String toString() {
        User toUser = UserManager.getUser(toUserId);
        return String.format("%s gets %s VC", toUser.name, amount);
    }
}
