package dataentities.block.record;

import functionality.Encryption;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;

/** Wraps information regarding a record in a blockchain */
public class Record<T extends RecordValue>  {

    /** The unique identifier */
    public final long id;

    /** The creator's ID */
    public final long creatorUserId;

    /** The actual value of this record */
    public final T value;

    /** The signature created by the creator */
    public final byte[] signature;

    /** The public key from the creator used to verify the signature */
    public final PublicKey publicKey;

    public Record(long id, long creatorUserId, T value, byte[] signature, PublicKey publicKey) {
        this.id = id;
        this.creatorUserId = creatorUserId;
        this.value = value;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    /** Checks whether this has a valid signature */
    public boolean hasValidSignature() {
        return Encryption.signatureIsValid(getSignatureData(value, id), signature, publicKey);
    }

    /** Retrieves the string that should be used for signatures */
    public static String getSignatureData(RecordValue recordValue, long messageId) {
        return String.format("%s%s", recordValue, messageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record<?> record = (Record<?>) o;
        return id == record.id &&
                creatorUserId == record.creatorUserId &&
                Objects.equals(value, record.value) &&
                Arrays.equals(signature, record.signature) &&
                Objects.equals(publicKey, record.publicKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, creatorUserId, value, publicKey);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}
