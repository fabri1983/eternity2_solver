package org.fabri1983.eternity2.experimental.mph.recsplit;

import java.util.BitSet;

/**
 * A select data structure for a list of bits.
 * <p>
 * Select(x) gets the position of the xth 1 bit (positions starting at 0).
 */
public abstract class Select {

    public static final boolean SIMPLE_SELECT = true;

    /**
     * Generate a select object, and store it into the provided buffer.
     *
     * @param set the bit set
     * @param buffer the buffer
     * @return the generated object
     */
    public static Select generate(BitSet set, BitBuffer buffer) {
        if (SIMPLE_SELECT) {
            return SimpleSelect.generate(set, buffer);
        }
        return VerySimpleSelect.generate(set, buffer);
    }

    /**
     * Get the number of bits needed.
     *
     * @param set the bit set
     * @return the number of bits
     */
    public static int getSize(BitSet set) {
        if (SIMPLE_SELECT) {
            return SimpleSelect.getSize(set);
        }
        return VerySimpleSelect.getSize(set);
    }

    /**
     * Generate a rank/select object from the provided buffer.
     *
     * @param buffer the buffer
     * @return the loaded object
     */
    public static Select load(BitBuffer buffer) {
        if (SIMPLE_SELECT) {
            return SimpleSelect.load(buffer);
        }
        return VerySimpleSelect.load(buffer);
    }

    /**
     * Get the position of the xth 1 bit.
     *
     * @param x the value (starting with 0)
     * @return the position (0 is the first bit), or -1 if x is too large
     */
    public abstract long select(long x);

    public abstract long selectPair(long x);

}
