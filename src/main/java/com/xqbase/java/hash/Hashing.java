package com.xqbase.java.hash;

/**
 * Jump Consistent Hash implementation.
 */
public final class Hashing {

    /**
     * Assigns to {@code input} a "bucket" in the range {@code [0, buckets)}, in a uniform
     * manner that minimizes the need for remapping as {@code buckets} grows. That is,
     * {@code consistentHash(h, n)} equals:
     *
     * <ul>
     * <li>{@code n - 1}, with approximate probability {@code 1/n}
     * <li>{@code consistentHash(h, n - 1)}, otherwise (probability {@code 1 - 1/n})
     * </ul>
     *
     * <p>See the <a href="http://en.wikipedia.org/wiki/Consistent_hashing">wikipedia
     * article on consistent hashing</a> for more information.
     */
    public static int consistentHash(long input, int buckets) {
        if (buckets <= 0) {
            throw new IllegalArgumentException("buckets must be positive");
        }
        LinearCongruentialGenerator generator = new LinearCongruentialGenerator(input);
        int candidate = 0;
        int next;

        while (true) {
            next = (int) ((candidate + 1) / generator.nextDouble());
            if (next >=0 && next < buckets) {
                candidate = next;
            } else {
                return candidate;
            }
        }
    }

    /**
     * Linear CongruentialGenerator to use for consistent hashing.
     * See http://en.wikipedia.org/wiki/Linear_congruential_generator
     */
    private static final class LinearCongruentialGenerator {
        private long state;

        public LinearCongruentialGenerator(long seed) {
            this.state = seed;
        }

        public double nextDouble() {
            state = 2862933555777941757L * state + 1;
            return ((double) ((int) (state >>> 33) + 1)) / (0x1.0p31);
        }
    }
}