package blockchain;

import dataentities.block.BlockData;
import dataentities.block.HashedBlock;
import dataentities.block.MinerBlock;
import dataentities.block.ValidatedBlock;
import dataentities.block.record.Record;
import dataentities.block.record.RecordValue;
import dataentities.concurrency.LockHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public abstract class Blockchain<T extends RecordValue> {

    /** The ID of the very first block */
    private final long FIRST_BLOCK_ID = 1;

    /**
     * For the first block, there is no previous block.
     * We use this value instead as the previous block's hash.
     */
    private final String FIRST_BLOCK_PREV_HASH = "0";

    /**
     * The number of seconds each block should be calculated.
     * We change this to change the hashing difficulty
     */
    private final int blockCalculationSpeedMs;

    /**
     * This blockchain is made to have a particular block calculation speed, plus-minus this value.
     */
    private final double blockCalculationSpeedUncertainty;

    /** The lock to be used when accessing message or block data */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The block data of the next block */
    protected volatile BlockData<T> nextBlockData = new BlockData<>(
            FIRST_BLOCK_ID, FIRST_BLOCK_PREV_HASH,
            0, List.of()
    );

    /** The ID of the next message */
    private volatile long nextRecordId = 1;

    /**
     * Stores the time when the previous block was created.
     * If this chain contains no blocks, then this is when the chain was created.
     */
    private Instant prevBlockCreatedWhen = Instant.now();

    /** All the blocks in this blockchain */
    protected final List<ValidatedBlock<T>> blocks = new ArrayList<>();

    protected Blockchain(int blockCalculationSpeedMs) {
        this.blockCalculationSpeedMs = blockCalculationSpeedMs;
        this.blockCalculationSpeedUncertainty = blockCalculationSpeedMs * 0.1;
    }

    /** Generates a block hash using the given values */
    public static <T extends RecordValue> String generateBlockHash(String prevBlockHash, List<Record<T>> messages, long nonce) {
        return applySha256(String.format(
            "%s%s%s",
            prevBlockHash,
            messages.stream().map(m -> m.value.toString()).reduce("", (result, message) -> result + message),
            nonce
        ));
    }

    /** Check whether the hash prefix matches the required zero count. */
    public static boolean blockHashMatchesPrefixZeroCount(HashedBlock<?> block) {
        String prefix = block.hash.substring(0, block.hashPrefixZeroCount);
        for (char c : prefix.toCharArray()) {
            if (c != '0') {
                return false;
            }
        }
        return true;
    }

    /* Applies Sha256 to a string and returns a hash. */
    private static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem: hash) {
                String hex = Integer.toHexString(0xff & elem);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Adds a new record to the next block */
    public boolean tryAddRecord(Record<T> record) {
        try (var ignored = LockHandler.ReadMode(lock)) {
            if (record.id != nextRecordId || !canAddRecordValue(record.value)) return false;
        }

        try (var ignored = LockHandler.WriteMode(lock)) {
            if (record.id != nextRecordId || !canAddRecordValue(record.value)) return false;

            nextBlockData = nextBlockData.WithNewRecord(record);
            nextRecordId++;
            return true;
        }
    }

    /** A check stating whether this record can be added. This check is performed within locks. */
    protected abstract boolean canAddRecordValue(T recordValue);

    /** Retrieves the ID of the next message */
    public long getNextRecordId() {
        try (var ignored = LockHandler.ReadMode(lock)) {
            return nextRecordId;
        }
    }

    /** Get the block data of the next block. */
    public MinerBlock<T> getNextBlockData(long minerUserId) {
        try (var ignored = LockHandler.ReadMode(lock)) {
            return MinerBlock.fromBlockData(nextBlockData, minerUserId, getMinerReward(minerUserId));
        }
    }

    /** Gets a reward for the miner if they manage to mine a block. This is called within a read lock. */
    protected abstract T getMinerReward(long minerUserId);

    /**
     * Attempts to add the hash to the block chain.
     * Returns a boolean stating whether the addition was successful or not.
     */
    public boolean tryAddBlock(HashedBlock<T> block) {
        Predicate<HashedBlock<T>> checkBlockValidity = innerBlock -> {
            // Check if this blockchain can add any more blocks
            if (!canAddNewBlock()) {
                return false;
            }

            // Check that the block info are the same as the one we need
            if (!innerBlock.equalBlockData(nextBlockData)) {
                return false;
            }

            // Check block properties are valid
            if (!blockIsValid(innerBlock)) {
                return false;
            }

            return true;
        };

        try (var ignored = LockHandler.ReadMode(lock)) {
            if(!checkBlockValidity.test(block)) return false;
        }

        try (var ignored = LockHandler.WriteMode(lock)) {
            // Check the block's validity again just in case the blocks
            // have changed since then
            if(!checkBlockValidity.test(block)) return false;

            // All the checks are done. Block is good.
            // Calculate the time it took to calculate the hash.
            Instant now = Instant.now();
            long timestamp = now.toEpochMilli();
            long calculationTimeMs = timestamp - prevBlockCreatedWhen.toEpochMilli();

            // Add the block
            blocks.add(ValidatedBlock.fromMinerBlock(block, timestamp, calculationTimeMs));
            prevBlockCreatedWhen = now;

            // Set up the block data for the next block
            int nextHashPrefixZeroCount;
            if (calculationTimeMs < blockCalculationSpeedMs - blockCalculationSpeedUncertainty) {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount + 1;
            } else if (calculationTimeMs > blockCalculationSpeedMs + blockCalculationSpeedUncertainty
                    && block.hashPrefixZeroCount > 0
            ) {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount - 1;
            } else {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount;
            }
            nextBlockData = new BlockData<T>(block.id + 1, block.hash, nextHashPrefixZeroCount, List.of());

            return true;
        }
    }

    /** Ensures the block is valid by ensuring all of its data and calculations match the blockchain. */
    private boolean blockIsValid(HashedBlock<T> block) {
        // Check every message in the block has valid signatures
        if (!block.records.stream().allMatch(Record::hasValidSignature)) {
            return false;
        }

        // Ensure the hash prefix starts with the required zeros
        if (!blockHashMatchesPrefixZeroCount(block)) {
            return false;
        }

        // Get the hash of the previous block of that block
        // No hash will be found if this block is the first block
        String prevHash = null;
        for (ValidatedBlock<T> prevBlock : blocks) {
            if (prevBlock.id == block.id - 1) {
                prevHash = prevBlock.hash;
            }
        }

        // Ensure the block's previous hash matches the previous block's hash
        if ((prevHash == null && !block.prevBlockHash.equals(FIRST_BLOCK_PREV_HASH))
                || (prevHash != null && !prevHash.equals(block.prevBlockHash))
        ) {
            return false;
        }

        // Ensure the hash calculation was correct
        if (!block.hash.equals(generateBlockHash(block.prevBlockHash, block.records, block.nonce))) {
            return false;
        }

        return true;
    }

    /** States whether this blockchain will allow the addition of a new block */
    public boolean canAddNewBlock() {
        // Note that a lock is not needed here because once this is true,
        // it is always true because the block size does not decrease
        return blocks.size() < 15;
    }

    /** Checks whether all the blocks are valid */
    public boolean blocksAreValid() {
        try (var ignored = LockHandler.ReadMode(lock)) {
            return blocks.stream().allMatch(this::blockIsValid);
        }
    }

    @Override
    public String toString() {
        try (var ignored = LockHandler.ReadMode(lock)) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < blocks.size(); i++) {
                // Get the block and the next block's hash prefix zero count
                ValidatedBlock<T> block = blocks.get(i);

                int blockNextHashPrefixZeroCount = i == blocks.size() - 1
                        ? nextBlockData.hashPrefixZeroCount
                        : blocks.get(i + 1).hashPrefixZeroCount;

                builder.append(block.toString());
                builder.append(
                        block.hashPrefixZeroCount == blockNextHashPrefixZeroCount
                                ? "N stays the same"
                                : blockNextHashPrefixZeroCount > block.hashPrefixZeroCount
                                ? String.format("N was increased to %s", blockNextHashPrefixZeroCount)
                                : String.format("N was decreased by %s", block.hashPrefixZeroCount - blockNextHashPrefixZeroCount)
                );
                builder.append("\n\n");
            }
            return builder.toString();
        }
    }
}
