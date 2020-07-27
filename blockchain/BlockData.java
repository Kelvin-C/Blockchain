package blockchain;

import java.util.Objects;

/** Stores information a blockchain block's inner data */
class BlockData {
    /** The ID of this block */
    public final long id;

    /** The non-null hash of the previous block */
    public final String prevBlockHash;

    /** The number of zeros that the new hash must have as a prefix */
    public final int hashPrefixZeroCount;

    public BlockData(long id, String prevBlockHash, int hashPrefixZeroCount) {
        this.id = id;
        this.prevBlockHash = prevBlockHash;
        this.hashPrefixZeroCount = hashPrefixZeroCount;
    }

    /** Check whether this block data is equivalen to the other block's data */
    public boolean equalBlockData(BlockData otherData) {
        return id == otherData.id &&
                hashPrefixZeroCount == otherData.hashPrefixZeroCount &&
                prevBlockHash.equals(otherData.prevBlockHash);
    }
}
