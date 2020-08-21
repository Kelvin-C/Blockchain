package dataentities.block.record;

import blockchain.UserManager;
import dataentities.user.User;

/** The reward to the miner for mining a currency transfer block */
public class CurrencyTransferReward extends CurrencyTransfer {

    public CurrencyTransferReward(long fromUserId, int amount, long toUserId) {
        super(fromUserId, amount, toUserId);
    }

    @Override
    public String toString() {
        User toUser = UserManager.getUser(toUserId);
        return String.format("%s gets %s VC", toUser.name, amount);
    }
}