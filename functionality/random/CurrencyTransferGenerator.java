package functionality.random;

import blockchain.UserManager;
import dataentities.block.record.CurrencyTransfer;

/** Automatically generates currency transfers */
public class CurrencyTransferGenerator extends RandomGenerator<CurrencyTransfer> {

    /** The upper limit to each transfer */
    static final int MAX_AMOUNT_PER_TRANSFER = 100;

    @Override
    public CurrencyTransfer generate() {
        int userCount = UserManager.getUserCountExcludingSystem();
        long fromUserId = generateInt(UserManager.minUserId, userCount + UserManager.minUserId);
        int amount = generateInt(1, MAX_AMOUNT_PER_TRANSFER);
        long toUserId = generateInt(UserManager.minUserId, userCount + UserManager.minUserId);

        return new CurrencyTransfer(fromUserId, amount, toUserId);
    }
}
