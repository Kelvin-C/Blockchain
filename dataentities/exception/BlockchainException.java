package dataentities.exception;

/** A general exception to be used across this application */
public class BlockchainException extends RuntimeException {
    public BlockchainException(String message) {
        super(message);
    }
}
