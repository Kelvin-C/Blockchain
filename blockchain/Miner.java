package blockchain;

class Miner implements Runnable {
    /** The ID of this block. Starts from 1. */
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
        long nonce = 0;
        do {
            // Get block data and calculate its hash
            BlockData blockData = blockchain.getNextBlockData();
            String hash = Blockchain.generateBlockHash(blockData.prevBlockHash, nonce);
            MinerBlock block = MinerBlock.FromBlockData(blockData, id, nonce, hash);

            // Try to add the block
            boolean success = blockchain.tryAddBlock(block);
            nonce = success ? 0 : nonce + nonceIncrementValue;
        }
        while (blockchain.canAddNewBlock());
    }
}
