package blockchain;

import java.util.Random;

/** An object which can automatically generate new record values */
abstract class RandomGenerator<T> {
    /** The random generator to be use */
    final Random random = new Random();

    /** The available characters */
    final static char[] letters = " abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /** Generate a random string with the given max length */
    protected String generateText(int minLength, int maxLength) {
        return random.ints(minLength + Math.floorMod(random.nextInt(), maxLength - minLength))
                .map(value -> letters[Math.floorMod(value, letters.length)])
                .mapToObj(Character::toString)
                .reduce("", (result, c) -> result + c);
    }

    /** Generates a random integer within the given range (inclusive) */
    protected int generateInt(int min, int max) {
        return min + Math.floorMod(random.nextInt(), max - min);
    }

    /** Returns new record value */
    public abstract T generate();
}

/** Automatically generates currency transfers */
class CurrencyTransferGenerator extends RandomGenerator<CurrencyTransfer> {

    /** The upper limit to each transfer */
    static final int MAX_AMOUNT_PER_TRANSFER = 100;

    @Override
    public CurrencyTransfer generate() {
        int userCount = UserManager.getUserCountExcludingSystem();
        long fromUserId = generateInt(UserManager.minUserId, userCount + UserManager.minUserId);
        int amount = generateInt(1, MAX_AMOUNT_PER_TRANSFER);
        long toUserId = generateInt(UserManager.minUserId, userCount + UserManager.minUserId);

        return new CurrencyTransfer(fromUserId, amount, toUserId);
    }
}
