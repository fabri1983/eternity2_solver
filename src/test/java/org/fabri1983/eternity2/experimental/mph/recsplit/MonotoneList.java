package org.fabri1983.eternity2.experimental.mph.recsplit;

/**
 * A list of monotone increasing values.
 */
public abstract class MonotoneList {

    public abstract int get(int i);

    public abstract long getPair(int i);

    public static MonotoneList generate(int[] data, BitBuffer buffer, boolean eliasFano) {
        return eliasFano ?
                EliasFanoMonotoneList.generate(data, buffer) :
                MultiStageMonotoneList.generate(data, buffer);
    }

    /**
     * Get the number of bits needed.
     *
     * @param data the data
     * @param eliasFano whether the Elias-Fano datastructure should be used
     * @return the number of bits
     */
    public static int getSize(int[] data, boolean eliasFano) {
        return eliasFano ?
                EliasFanoMonotoneList.getSize(data) :
                MultiStageMonotoneList.getSize(data);
    }

    public static MonotoneList load(BitBuffer buffer, boolean eliasFano) {
        return eliasFano ?
                EliasFanoMonotoneList.load(buffer) :
                MultiStageMonotoneList.load(buffer);
    }

}
