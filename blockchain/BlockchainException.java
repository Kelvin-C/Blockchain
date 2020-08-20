package blockchain;

/** A general exception to be used across this application */
class BlockchainException extends RuntimeException {
    public BlockchainException(String message) {
        super(message);
    }
}
