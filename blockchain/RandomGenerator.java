package blockchain;

import java.util.Random;

class RandomGenerator {

    /** The random generator to be use */
    final Random random = new Random();

    /** The available characters */
    final static char[] letters = " abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /** Generate a random string with the given max length */
    public String generate(int minLength, int maxLength) {
        return random.ints(minLength + Math.floorMod(random.nextInt(), maxLength - minLength))
                .map(value -> letters[Math.floorMod(value, letters.length)])
                .mapToObj(Character::toString)
                .reduce("", (result, c) -> result + c);
    }
}
