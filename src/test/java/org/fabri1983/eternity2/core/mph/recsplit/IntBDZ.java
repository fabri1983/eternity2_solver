package org.fabri1983.eternity2.core.mph.recsplit;

/**
 * A simple implementation of the BDZ algorithm as documented in
 * "Simple and Space-Efficient Minimal Perfect Hash Functions" (F. C. Botelho,
 * R. Pagh, N. Ziviani).
 *
 * This implementation around 3.66 bits/key, which is much more than really
 * needed, mainly because no compression is used.
 *
 * @param <T> the type
 */
public class IntBDZ {

    // needs 3.66 bits/key
    private static final int HASHES = 3;
    private static final int FACTOR_TIMES_100 = 123;
    private static final int BITS_PER_ENTRY = 2;

    // needs 3.78 bits/key
    // private static final int HASHES = 4;
    // private static final int FACTOR_TIMES_100 = 132;
    // private static final int BITS_PER_ENTRY = 2;

    // needs 4.25 bits/key
    // private static final int HASHES = 2;
    // private static final int FACTOR_TIMES_100 = 240;
    // private static final int BITS_PER_ENTRY = 1;

    private final BitBuffer data;
    private final int hashIndex;
    private final int arrayLength;
    private final int size;
    private final int startPos;
    private final VerySimpleRank rank;

    private IntBDZ(BitBuffer data) {
        this.data = data;
        this.size = (int) data.readEliasDelta() - 1;
        this.arrayLength = getArrayLength(size);
        this.hashIndex = (int) data.readEliasDelta() - 1;
        this.rank = VerySimpleRank.load(data);
        this.startPos = data.position();
        data.seek(startPos + size * BITS_PER_ENTRY);
    }

    public int evaluate(int x) {
        int sum = 0;
        for (int hi = 0; hi < HASHES; hi++) {
            int h = getHash(x, hashIndex, hi, arrayLength);
            if (rank.get(h)) {
                int pos = (int) rank.rank(h);
                sum += data.readNumber(startPos + pos * BITS_PER_ENTRY, BITS_PER_ENTRY);
            }
        }
        int h = getHash(x, hashIndex, sum % HASHES, arrayLength);
        int pos = (int) rank.rank(h);
        return pos;
    }

    public static IntBDZ load(BitBuffer data) {
        return new IntBDZ(data);
    }

    public int getSize() {
        return size;
    }

    private static int getArrayLength(int size) {
        return HASHES + FACTOR_TIMES_100 * size / 100;
    }

    private static int getHash(int x, int hashIndex, 
    		int index, int arrayLength) {
        int r = IntHash.universalHash(x, hashIndex + index);
        r = Settings.reduce(r, arrayLength / HASHES);
        r = r + index * arrayLength / HASHES;
        return (int) r;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " size " + size + " hashIndex " + hashIndex;
    }

}
