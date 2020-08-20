package blockchain;

class Miner implements Runnable {
    /** The ID of this block */
    private final long id;

    /** The blockchain that this miner mines for */
    private final Blockchain blockchain;

    /** The amount the nonce value should increase after each attempt */
    private final int nonceIncrementValue;

    public Miner(long id, Blockchain blockchain, int nonceIncrementValue) {
        this.id = id;
        this.blockchain = blockchain;
        this.nonceIncrementValue = nonceIncrementValue;
    }

    @Override
    public void run() {
        BlockData blockData = null;
        long nonce = 0;
        do {
            // Get block data. If the block data has changed, then reset the nonce value
            BlockData newBlockData = blockchain.getNextBlockData();
            if (blockData == null || !newBlockData.equalBlockData(blockData)) {
                blockData = newBlockData;
                nonce = 0;
            }

            // Only mine if there are messages since that is the whole point of a blockchain.
            // Usually, the miner would not get any incentive to mine a block with nothing in it
            // because the whole point of mining is the making the inner data (i.e. messages) more secure
            if (blockData.messages.isEmpty()) {
                continue;
            }

            // Calculate the hash and the miner block
            String hash = Blockchain.generateBlockHash(blockData.prevBlockHash, blockData.messages, nonce);
            MinerBlock block = MinerBlock.FromBlockData(blockData, id, nonce, hash);

            // Add the block if the hash matches the zero count requirement
            if (Blockchain.blockHashMatchesPrefixZeroCount(block)) {
                // Try to add the block
                blockchain.tryAddBlock(block);
            }

            nonce += nonceIncrementValue;
        }
        while (blockchain.canAddNewBlock());
    }
}
