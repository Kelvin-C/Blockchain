package blockchain;

import java.util.stream.Stream;

class CurrencyBlockchain extends Blockchain<CurrencyTransfer> {

    /** The starting balance for each user */
    static final int STARTING_USER_BALANCE = 100;

    public CurrencyBlockchain(int blockCalculationSpeedMs) {
        super(blockCalculationSpeedMs);
    }

    @Override
    protected boolean canAddRecordValue(CurrencyTransfer transfer) {
        // The record value can be added if none of the users will have negative value afterwards
        // Therefore, we only need to check the user losing money.
        // Note that if the money comes from the system, then we can accept because the system has infinite money.
        if(transfer.fromUserId == UserManager.SystemUser.id) return true;
        long userBalance = getUserBalance(transfer.fromUserId);
        return userBalance - transfer.amount >= 0;
    }

    @Override
    protected CurrencyTransfer getMinerReward(long minerUserId) {
        return new CurrencyTransferReward(UserManager.SystemUser.id, 100, minerUserId);
    }

    /** Gets the currency balance of the user. Must be called within locks. */
    private long getUserBalance(long userId) {
        long totalAmountToUser =
                Stream.concat(
                    blocks.stream()
                        .filter(block -> block.minerUserId == userId)
                        .map(block -> block.minerReward),
                    blocks.stream()
                        .flatMap(block -> block.records.stream().map(record -> record.value))
                        .filter(record -> record.toUserId == userId)
                ).mapToInt(transfer -> transfer.amount).sum();

        long totalAmountFromUser =
                blocks.stream()
                    .flatMap(block -> block.records.stream().map(record -> record.value))
                    .filter(record -> record.fromUserId == userId)
                    .mapToInt(record -> record.amount)
                    .sum();

        return STARTING_USER_BALANCE + totalAmountToUser - totalAmountFromUser;
    }
}
