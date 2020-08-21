package blockchain;

class Miner<T extends RecordValue> implements Runnable {
    /** The ID of this block */
    private final User user;

    /** The blockchain that this miner mines for */
    private final Blockchain<T> blockchain;

    /** The nonce value to start with */
    private final int startingNonce;

    /** The amount the nonce value should increase after each attempt */
    private final int nonceIncrementValue;

    public Miner(User user, Blockchain<T> blockchain, int startingNonce, int nonceIncrementValue) {
        this.user = user;
        this.blockchain = blockchain;
        this.startingNonce = startingNonce;
        this.nonceIncrementValue = nonceIncrementValue;
    }

    @Override
    public void run() {
        MinerBlock<T> minerBlock = null;
        long nonce = startingNonce;
        do {
            // Get block data. If the block data has changed, then reset the nonce value
            MinerBlock<T> newBlockData = blockchain.getNextBlockData(user.id);
            if (minerBlock == null || !newBlockData.equalBlockData(minerBlock)) {
                minerBlock = newBlockData;
                nonce = startingNonce;
            }

            // Only mine if there are messages since that is the whole point of a blockchain.
            // Usually, the miner would not get any incentive to mine a block with nothing in it
            // because the whole point of mining is the making the inner data (i.e. messages) more secure
            if (minerBlock.records.isEmpty()) {
                continue;
            }

            // Calculate the hash and the miner block
            String hash = Blockchain.generateBlockHash(minerBlock.prevBlockHash, minerBlock.records, nonce);
            HashedBlock<T> block = HashedBlock.fromMinerBlock(minerBlock, nonce, hash);

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
