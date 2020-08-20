package blockchain;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UserManager {

    /** The lock that handles the access of user data */
    static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** Contains all the users, keyed by their ID */
    static final Map<Long, User> usersById = new HashMap<>();

    /** A random generator used to generate user names */
    static final RandomGenerator random = new RandomGenerator();

    /** Tracks the ID of the next user */
    static volatile long nextUserId = 1;

    /** Generates a new user using random data */
    public static User generateNewUser() {
        try (var ignored = LockHandler.WriteMode(lock)) {
            String name = random.generate(1, 10);
            KeyPair keyPair = Encryption.generateKeys();

            User user = new User(nextUserId, name, keyPair);
            usersById.put(user.id, user);

            nextUserId++;
            return user;
        }
    }

    /** Gets the user with the given ID */
    public static User getUser(long userId) {
        try (var ignored = LockHandler.ReadMode(lock)) {
            return usersById.get(userId);
        }
    }
}
