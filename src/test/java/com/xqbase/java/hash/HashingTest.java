package com.xqbase.java.hash;

import com.google.common.util.concurrent.AtomicLongMap;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Consistent Hash Test Case.
 */
public class HashingTest extends TestCase {

    private static final int ITERS = 10000;
    private static final int MAX_SHARDS = 500;

    public void testConsistentHash_correctness() {
        long[] interestingValues = { -1, 0, 1, 2, Long.MAX_VALUE, Long.MIN_VALUE };
        for (long h : interestingValues) {
            checkConsistentHashCorrectness(h);
        }
        Random r = new Random(7);
        for (int i = 0; i < 20; i++) {
            checkConsistentHashCorrectness(r.nextLong());
        }
    }

    private void checkConsistentHashCorrectness(long hashCode) {
        int last = 0;
        for (int shards = 1; shards <= 100000; shards++) {
            int b = Hashing.consistentHash(hashCode, shards);
            if (b != last) {
                assertEquals(shards - 1, b);
                last = b;
            }
        }
    }

    public void testConsistentHash_probabilities() {
        AtomicLongMap<Integer> map = AtomicLongMap.create();
        Random r = new Random(9);
        for (int i = 0; i < ITERS; i++) {
            countRemaps(r.nextLong(), map);
        }
        for (int shard = 2; shard <= MAX_SHARDS; shard++) {
            // Rough: don't exceed 1.2x the expected number of remaps by more than 20
            assertTrue(map.get(shard) <= 1.2 * ITERS / shard + 20);
        }
    }

    private void countRemaps(long h, AtomicLongMap<Integer> map) {
        int last = 0;
        for (int shards = 2; shards <= MAX_SHARDS; shards++) {
            int chosen = Hashing.consistentHash(h, shards);
            if (chosen != last) {
                map.incrementAndGet(shards);
                last = chosen;
            }
        }
    }

    /**
     * Check a few "golden" values to see that implementations across languages
     * are equivalent.
     */
    public void testConsistentHash_linearCongruentialGeneratorCompatibility() {
        int[] golden100 =
                { 0, 55, 62, 8, 45, 59, 86, 97, 82, 59,
                        73, 37, 17, 56, 86, 21, 90, 37, 38, 83 };
        for (int i = 0; i < golden100.length; i++) {
            assertEquals(golden100[i], Hashing.consistentHash(i, 100));
        }
        assertEquals(6, Hashing.consistentHash(10863919174838991L, 11));
        assertEquals(3, Hashing.consistentHash(2016238256797177309L, 11));
        assertEquals(5, Hashing.consistentHash(1673758223894951030L, 11));
        assertEquals(80343, Hashing.consistentHash(2, 100001));
        assertEquals(22152, Hashing.consistentHash(2201, 100001));
        assertEquals(15018, Hashing.consistentHash(2202, 100001));
    }
}
