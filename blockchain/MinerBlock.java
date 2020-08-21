package blockchain;

import java.util.List;

/** A block for the miner to mine */
class MinerBlock<T extends RecordValue> extends BlockData<T> {

    /** The ID of the miner which created this block */
    public final long minerUserId;

    /** The reward to the miner for mining this block */
    public final T minerReward;

    public MinerBlock(long id, String prevBlockHash, int hashPrefixZeroCount, List<Record<T>> records,
                      long minerUserId, T minerReward
    ) {
        super(id, prevBlockHash, hashPrefixZeroCount, records);
        this.minerUserId = minerUserId;
        this.minerReward = minerReward;
    }

    public static <T extends RecordValue> MinerBlock<T> fromBlockData(
            BlockData<T> blockData, long minerUserId, T minerReward
    ) {
        return new MinerBlock<>(
                blockData.id, blockData.prevBlockHash, blockData.hashPrefixZeroCount, blockData.records,
                minerUserId, minerReward
        );
    }
}
