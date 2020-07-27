package blockchain;

/** The blockchain block that is generated by the miner */
class MinerBlock extends BlockData {

    /** The ID of the miner which created this block */
    public final long minerId;

    /** The magic number used only for generating a particular hash */
    public final long nonce;

    /** Hash value of this block */
    public final String hash;

    MinerBlock(long id, String prevBlockHash, int hashPrefixZeroCount, long minerId, long nonce, String hash) {
        super(id, prevBlockHash, hashPrefixZeroCount);
        this.minerId = minerId;
        this.nonce = nonce;
        this.hash = hash;
    }

    public static MinerBlock FromBlockData(BlockData data, long minerId, long nonce, String hash) {
        return new MinerBlock(data.id, data.prevBlockHash, data.hashPrefixZeroCount, minerId, nonce, hash);
    }
}
