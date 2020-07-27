package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

class Blockchain {

    /** The ID of the very first block */
    private final long FIRST_BLOCK_ID = 1;

    /**
     * For the first block, there is no previous block.
     * We use this value instead as the previous block's hash.
     */
    private final String FIRST_BLOCK_PREV_HASH = "0";

    /**
     * The number of seconds each block should be calculated.
     * We change the hash calculation difficulty to match this.
     */
    private final int BLOCK_CALCULATION_SPEED_SECONDS = 2;

    /** The timezone offset of this machine. */
    private final ZoneOffset timezone = ZoneOffset.UTC;

    /** The block data of the next block */
    private volatile BlockData nextBlockData = new BlockData(FIRST_BLOCK_ID, FIRST_BLOCK_PREV_HASH, 0);

    /**
     * Stores the time when the previous block was created.
     * If this chain contains no blocks, then this is when the chain was created.
     */
    private LocalDateTime prevBlockCreatedWhen = LocalDateTime.now();

    /** All the blocks in this blockchain */
    private final List<ValidatedBlock> blocks = new ArrayList<>();

    /** Generates a block hash using the given values */
    public static String generateBlockHash(String prevBlockHash, long nonce) {
        return applySha256(String.format("%s%s", prevBlockHash, nonce));
    }

    /** Check whether the hash prefix matches the required zero count. */
    public static boolean blockHashMatchesPrefixZeroCount(MinerBlock block) {
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

    /** Get the block data of the next block. */
    public BlockData getNextBlockData() {
        return nextBlockData;
    }

    /** Ensures the block is valid by ensuring all of its data and calculations match the blockchain. */
    private boolean blockIsValid(MinerBlock block) {
        // Ensure the hash prefix starts with the required zeros
        if (!blockHashMatchesPrefixZeroCount(block)) {
            return false;
        }

        // Get the hash of the previous block of that block
        String prevHash = null;
        for (ValidatedBlock prevBlock : blocks) {
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
        if (!block.hash.equals(generateBlockHash(block.prevBlockHash, block.nonce))) {
            return false;
        }

        return true;
    }

    /**
     * Attempts to add the hash to the block chain.
     * Returns a boolean stating whether the addition was successful or not.
     */
    public boolean tryAddBlock(MinerBlock block) {
        synchronized (this) {
            if(!canAddNewBlock()) {
                return false;
            }

            // Check that the block info are the same as the one we need
            if (!block.equalBlockData(nextBlockData)) {
                return false;
            }

            // Check block properties are valid
            if (!blockIsValid(block)) {
                return false;
            }

            // Calculate the time it took to calculate the hash.
            LocalDateTime now = LocalDateTime.now();
            long timestamp = now.toEpochSecond(timezone);
            long calculationTime = timestamp - prevBlockCreatedWhen.toEpochSecond(timezone);

            // All the checks are done. Block is good, so we add it
            blocks.add(ValidatedBlock.fromMinerBlock(block, timestamp, calculationTime));
            prevBlockCreatedWhen = now;

            // Set up the block data for the next block
            int nextHashPrefixZeroCount;
            if (calculationTime < BLOCK_CALCULATION_SPEED_SECONDS - 1) {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount + 1;
            } else if (calculationTime > BLOCK_CALCULATION_SPEED_SECONDS + 1 && block.hashPrefixZeroCount  > 0) {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount - 1;
            } else {
                nextHashPrefixZeroCount = block.hashPrefixZeroCount;
            }
            nextBlockData = new BlockData(block.id + 1, block.hash, nextHashPrefixZeroCount);

            return true;
        }
    }

    /** States whether this blockchain will allow the addition of a new block */
    public boolean canAddNewBlock() {
        return blocks.size() < 5;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            // Get the block and the next block's hash prefix zero count
            ValidatedBlock block = blocks.get(i);
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
