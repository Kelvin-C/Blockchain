package blockchain;

import java.util.ArrayList;
import java.util.List;

/** Stores information a blockchain block's inner data */
class BlockData {
    /** The ID of this block */
    public final long id;

    /** The non-null hash of the previous block */
    public final String prevBlockHash;

    /** The number of zeros that the new hash must have as a prefix */
    public final int hashPrefixZeroCount;

    /** The messages that should be stored in this block */
    public final List<Message> messages;

    public BlockData(long id, String prevBlockHash, int hashPrefixZeroCount, List<Message> messages) {
        this.id = id;
        this.prevBlockHash = prevBlockHash;
        this.hashPrefixZeroCount = hashPrefixZeroCount;
        this.messages = messages;
    }

    /** Returns a new Block Data object with the message added to the message list */
    public BlockData WithNewMessage(Message message) {
        List<Message> newMessages = new ArrayList<>(messages);
        newMessages.add(message);
        return new BlockData(id, prevBlockHash, hashPrefixZeroCount, newMessages);
    }

    /** Check whether this block data is equivalent to the other block's data */
    public boolean equalBlockData(BlockData otherData) {
        return id == otherData.id &&
                hashPrefixZeroCount == otherData.hashPrefixZeroCount &&
                prevBlockHash.equals(otherData.prevBlockHash) &&
                messages.equals(otherData.messages);
    }
}
