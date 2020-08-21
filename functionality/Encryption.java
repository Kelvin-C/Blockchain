package functionality;

import dataentities.exception.BlockchainException;

import java.security.*;

public class Encryption {

    final static int keyLength = 1024;
    final static String keyGeneratorAlgorithm = "RSA";
    final static String signatureAlgorithm = "SHA512withRSA";

    /** Generates a random set of keys */
    public static KeyPair generateKeys() {
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance(keyGeneratorAlgorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Invalid encryption algorithm was found");
        }

        keyGen.initialize(keyLength);
        return keyGen.generateKeyPair();
    }

    /** Writes a signature using the provided data and private key */
    public static byte[] sign(String data, PrivateKey key) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(key);
            signature.update(data.getBytes());
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Invalid signature algorithm was found");
        } catch (InvalidKeyException e) {
            throw new BlockchainException("Invalid private key was provided");
        } catch (SignatureException e) {
            throw new BlockchainException("Signature was not initialised correctly");
        }
    }

    /** Checks whether the provided signature is valid using the given data */
    public static boolean signatureIsValid(String data, byte[] signature, PublicKey key) {
        try {
            Signature verifier = Signature.getInstance(signatureAlgorithm);
            verifier.initVerify(key);
            verifier.update(data.getBytes());
            return verifier.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new BlockchainException("Invalid signature algorithm was found");
        } catch (InvalidKeyException e) {
            throw new BlockchainException("Invalid public key was provided");
        } catch (SignatureException e) {
            throw new BlockchainException("Verifier was not initialised correctly");
        }
    }
}
