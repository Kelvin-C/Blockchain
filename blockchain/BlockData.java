package blockchain;

import java.util.ArrayList;
import java.util.List;

/** Stores information a blockchain block's inner data */
class BlockData<T extends RecordValue> {
    /** The ID of this block */
    public final long id;

    /** The non-null hash of the previous block */
    public final String prevBlockHash;

    /** The number of zeros that the new hash must have as a prefix */
    public final int hashPrefixZeroCount;

    /** The messages that should be stored in this block */
    public final List<Record<T>> records;

    public BlockData(long id, String prevBlockHash, int hashPrefixZeroCount, List<Record<T>> records) {
        this.id = id;
        this.prevBlockHash = prevBlockHash;
        this.hashPrefixZeroCount = hashPrefixZeroCount;
        this.records = records;
    }

    /** Returns a new Block Data object with the record added to the record list */
    public final BlockData<T> WithNewRecord(Record<T> record) {
        List<Record<T>> newMessages = new ArrayList<>(records);
        newMessages.add(record);
        return new BlockData<T>(id, prevBlockHash, hashPrefixZeroCount, newMessages);
    }

    /** Check whether this block data is equivalent to the other block's data */
    public boolean equalBlockData(BlockData<T> otherData) {
        return id == otherData.id &&
                hashPrefixZeroCount == otherData.hashPrefixZeroCount &&
                prevBlockHash.equals(otherData.prevBlockHash) &&
                records.equals(otherData.records);
    }
}
