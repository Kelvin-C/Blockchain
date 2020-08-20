package blockchain;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

class User {

    /** The unique identifier of this user */
    public final long id;

    /** The name of this user */
    public final String name;

    /** The encryption public key of this user */
    public final PublicKey publicKey;

    /** An encryption private key of this user */
    public final PrivateKey privateKey;

    public User(long id, String name, KeyPair encryptionKeyPair) {
        this.id = id;
        this.name = name;
        this.publicKey = encryptionKeyPair.getPublic();
        this.privateKey = encryptionKeyPair.getPrivate();
    }

    /** Generates a signature for the given data */
    public byte[] getSignature(String messageValue, long messageId) {
        return Encryption.sign(Message.getSignatureData(messageValue, messageId), privateKey);
    }
}
