package dataentities.block;

import dataentities.block.record.Record;
import dataentities.block.record.RecordValue;
import blockchain.UserManager;

import java.util.List;

/** A valid blockchain block that has been successfully validated */
public class ValidatedBlock<T extends RecordValue> extends HashedBlock<T> {

    /** Defines when this block was created */
    public final long timestamp;

    /** The time it took to calculate the hash, in milliseconds */
    public final long calculationTimeMs;

    ValidatedBlock(
            long id, String prevBlockHash, int hashPrefixZeroCount, List<Record<T>> records,
            long minerId, T minerReward, long nonce, String hash, long timestamp, long calculationTimeMs
    ) {
        super(id, prevBlockHash, hashPrefixZeroCount, records, minerId, minerReward, nonce, hash);
        this.timestamp = timestamp;
        this.calculationTimeMs = calculationTimeMs;
    }

    /** Creates a validated block with a miner block */
    public static <T extends RecordValue> ValidatedBlock<T> fromMinerBlock(
            HashedBlock<T> block, long timestamp, long calculationTimeMs
    ) {
        return new ValidatedBlock<T>(
                block.id, block.prevBlockHash, block.hashPrefixZeroCount, block.records,
                block.minerUserId, block.minerReward, block.nonce, block.hash, timestamp, calculationTimeMs
        );
    }

    /**
     * Returns a string stating all information regarding this block.
     */
    @Override
    public String toString() {
        return formattedLine("Block:") +
                formattedLine("Created by miner: %s", UserManager.getUser(minerUserId).name) +
                formattedLine(minerReward.toString()) +
                formattedLine("Id: %s", id) +
                formattedLine("Timestamp: %s", timestamp) +
                formattedLine("Magic number: %s", nonce) +
                formattedLine("Hash of the previous block:") +
                formattedLine(prevBlockHash) +
                formattedLine("Hash of the block:") +
                formattedLine(hash) +
                formattedLine("Block data:%s", records.isEmpty() ? " no messages" : "") +
                (records.isEmpty()
                        ? ""
                        : records.stream()
                            .map(m -> formattedLine(m.value.toString()))
                            .reduce("", (result, recordLine) -> result + recordLine)
                ) +
                formattedLine("Block was generating for %s milliseconds", calculationTimeMs);
    }

    /** Returns a line string using the format and data */
    private String formattedLine(String format, Object ... data) {
        return String.format(format, data) + "\n";
    }
}
