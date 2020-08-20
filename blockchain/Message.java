package blockchain;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;

/** Wraps information regarding a message in a blockchain */
class Message {

    /** The identifier of this message */
    public final long id;

    /** The creator of this message */
    public final long userId;

    /** The actual text of this message */
    public final String value;

    /** The signature created by the creator of this message */
    public final byte[] signature;

    /** The public key from the creator of this message used to verify the signature */
    public final PublicKey publicKey;

    public Message(long id, long userId, String message, byte[] signature, PublicKey publicKey) {
        this.id = id;
        this.userId = userId;
        this.value = message;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    /** Checks whether this message has a valid signature */
    public boolean hasValidSignature() {
        return Encryption.signatureIsValid(getSignatureData(value, id), signature, publicKey);
    }

    /** Retrieves the string that should be used for signatures */
    public static String getSignatureData(String messageValue, long messageId) {
        return String.format("%s%s", messageValue, messageId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id &&
                userId == message.userId &&
                Objects.equals(value, message.value) &&
                Arrays.equals(signature, message.signature) &&
                Objects.equals(publicKey, message.publicKey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, userId, value, publicKey);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}
