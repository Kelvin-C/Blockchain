package dataentities.block;

import dataentities.block.record.Record;
import dataentities.block.record.RecordValue;

import java.util.List;

/** The blockchain block with a calculated hash value */
public class HashedBlock<T extends RecordValue> extends MinerBlock<T> {

    /** The magic number used only for generating a particular hash */
    public final long nonce;

    /** Hash value of this block */
    public final String hash;

    HashedBlock(long id, String prevBlockHash, int hashPrefixZeroCount, List<Record<T>> records,
                long minerUserId, T minerReward, long nonce, String hash
    ) {
        super(id, prevBlockHash, hashPrefixZeroCount, records, minerUserId, minerReward);
        this.nonce = nonce;
        this.hash = hash;
    }

    public static <T extends RecordValue> HashedBlock<T> fromMinerBlock(
            MinerBlock<T> minerBlock, long nonce, String hash
    ) {
        return new HashedBlock<>(
                minerBlock.id, minerBlock.prevBlockHash, minerBlock.hashPrefixZeroCount, minerBlock.records,
                minerBlock.minerUserId, minerBlock.minerReward, nonce, hash
        );
    }
}
