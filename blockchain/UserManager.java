package blockchain;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class UserManager {

    /** The user of the system. This is not a user of any person. */
    public static final User SystemUser;

    /** The starting user ID */
    public static final int minUserId = 1;

    /** The lock that handles the access of user data */
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** Contains all the users, keyed by their ID */
    private static final Map<Long, User> usersById = new HashMap<>();

    /** Tracks the ID of the next user */
    private static volatile long nextUserId = minUserId;

    static {
        // Instantiate system user
        long id = -1;
        String name = "System";
        KeyPair keyPair = Encryption.generateKeys();
        SystemUser = new User(id, name, keyPair);
        usersById.put(id, SystemUser);
    }

    /** Generates a new user using random data */
    public static User generateNewUser() {
        try (var ignored = LockHandler.WriteMode(lock)) {
            long id = nextUserId;
            String name = String.format("user%s", id);
            KeyPair keyPair = Encryption.generateKeys();

            User user = new User(id, name, keyPair);
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

    /** Returns the number of users */
    public static int getUserCountExcludingSystem() {
        try (var ignored = LockHandler.ReadMode(lock)) {
            return usersById.size() - 1;
        }
    }
}
