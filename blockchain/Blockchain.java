package blockchain;

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

    /** The number of zeros at the start of block hashes for the next new block hash */
    private volatile int nextHashPrefixZeroCount = 0;

    /**
     * The number of seconds each block should be calculated.
     * We change the hash calculation difficulty to match this.
     */
    private final int blockCalculationSpeedSeconds = 2;

    /**
     * Stores the time when the previous block was created.
     * If this chain contains no blocks, then this is when the chain was created.
     */
    private LocalDateTime prevBlockCreatedWhen = LocalDateTime.now();

    /** The timezone offset of this machine. */
    private final ZoneOffset timezone = ZoneOffset.UTC;

    /** All the blocks in this blockchain */
    private final List<ValidatedBlock> blocks = new ArrayList<>();

    /** Generates a block hash using the given values */
    public static String generateBlockHash(String prevBlockHash, long nonce) {
        return BlockchainUtil.applySha256(String.format("%s%s", prevBlockHash, nonce));
    }

    /** Get the block data of the next block. */
    public BlockData getNextBlockData() {
        synchronized (this) {
            ValidatedBlock latestBlock = blocks.size() == 0 ? null : blocks.get(blocks.size() - 1);
            long id = latestBlock == null ? FIRST_BLOCK_ID : latestBlock.id + 1;
            String prevHash = latestBlock == null ? FIRST_BLOCK_PREV_HASH : latestBlock.hash;
            return new BlockData(id, prevHash, nextHashPrefixZeroCount);
        }
    }

    /** Checks the hash to ensure its prefix starts with zeros. */
    private boolean blockIsValid(MinerBlock block) {
        // Get the prefix and check all prefix characters are zeros.
        String prefix = block.hash.substring(0, block.hashPrefixZeroCount);
        for (char c : prefix.toCharArray()) {
            if (c != '0') {
                return false;
            }
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
        if(!canAddNewBlock()) {
            return false;
        }

        synchronized (this) {
            // Get the block info of the new block that we should add
            BlockData nextBlockData = getNextBlockData();

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

            // Change the next block's zero prefix count based on calculation speed.
            if (calculationTime < blockCalculationSpeedSeconds - 1) {
                nextHashPrefixZeroCount++;
            } else if (calculationTime > blockCalculationSpeedSeconds + 1 && nextHashPrefixZeroCount > 0) {
                nextHashPrefixZeroCount--;
            }

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
                    ? nextHashPrefixZeroCount
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
