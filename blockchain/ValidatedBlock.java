package blockchain;

import java.util.List;

/** A valid blockchain block that has been validated based on the block info */
class ValidatedBlock extends MinerBlock {

    /** Defines when this block was created */
    public final long timestamp;

    /** The time it took to calculate the hash */
    public final long calculationTimeSeconds;

    ValidatedBlock(
            long id, String prevBlockHash, int hashPrefixZeroCount, List<Message> messages, long minerId,
            long nonce, String hash, long timestamp, long calculationTimeSeconds
    ) {
        super(id, prevBlockHash, hashPrefixZeroCount, messages, minerId, nonce, hash);
        this.timestamp = timestamp;
        this.calculationTimeSeconds = calculationTimeSeconds;
    }

    /** Creates a validated block with a miner block */
    public static ValidatedBlock fromMinerBlock(MinerBlock block, long timestamp, long calculationTimeSeconds) {
        return new ValidatedBlock(
                block.id, block.prevBlockHash, block.hashPrefixZeroCount, block.messages, block.minerId,
                block.nonce, block.hash, timestamp, calculationTimeSeconds
        );
    }

    /**
     * Returns a string stating all information regarding this block.
     */
    @Override
    public String toString() {
        return formattedLine("Block:") +
            formattedLine("Created by miner # %s", minerId) +
            formattedLine("Id: %s", id) +
            formattedLine("Timestamp: %s", timestamp) +
            formattedLine("Magic number: %s", nonce) +
            formattedLine("Hash of the previous block:") +
            formattedLine(prevBlockHash) +
            formattedLine("Hash of the block:") +
            formattedLine(hash) +
            formattedLine("Block data:%s", messages.isEmpty() ? " no messages" : "") +
            (messages.isEmpty()
                    ? ""
                    : messages.stream()
                        .map(m -> formattedLine("%s: %s", UserManager.getUser(m.userId).name, m.value))
                        .reduce("", (result, message) -> result + message)
            ) +
            formattedLine("Block was generating for %s seconds", calculationTimeSeconds);
    }

    /** Returns a line string using the format and data */
    private String formattedLine(String format, Object ... data) {
        return String.format(format, data) + "\n";
    }
}
