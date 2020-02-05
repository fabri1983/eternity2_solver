package org.fabri1983.eternity2.experimental.bitset.misc;

/**
 * A simplified and streamlined version of {@link java.util.BitSet}.
 */
public class FastlshBitSet {

	public final long[] bits;
	public final int numBits;

	/**
	 * Creates a BitSet with the specified number of bits.
	 * 
	 * @param numBits number of bits to store in the BitSet
	 */
	public FastlshBitSet(int numBits) {
		int numLongs = numBits >>> 6;
		if ((numBits & 0x3F) != 0) {
			numLongs++;
		}
		bits = new long[numLongs];
		this.numBits = numBits;
	}

	/**
	 * Gets the bit stored at a particular index.
	 * 
	 * @param index index whose bit is desired.
	 * @return
	 */
	public boolean get(int index) {
		// skipping range check for speed
		return (bits[index >>> 6] & 1L << (index & 0x3F)) != 0L;
	}

	/**
	 * Sets a value to be stored in a bit at a particular index.
	 * 
	 * @param index index to store the bit
	 * @param b     boolean encoding of the bit to be stored
	 */
	public void set(int index, boolean b) {
		if (b)
			bits[index >>> 6] |= 1L << (index & 0x3F);
		else
			bits[index >>> 6] &= ~(1L << (index & 0x3F));
	}

	/**
	 * Sets the bit at a specified index to 1.
	 * 
	 * @param index index whose value is to be set to 1
	 */
	public void set(int index) {
		// skipping range check for speed
		bits[index >>> 6] |= 1L << (index & 0x3F);
	}

	/**
	 * Sets the bit at a specified index to 0.
	 * 
	 * @param index
	 */
	public void clear(int index) {
		// skipping range check for speed
		bits[index >>> 6] &= ~(1L << (index & 0x3F));
	}

	/**
	 * Sets all the bits to 0.
	 */
	public void clear() {
		int length = bits.length;
		for (int i = 0; i < length; i++) {
			bits[i] = 0L;
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(64 * bits.length);
		for (long l : bits) {
			for (int j = 0; j < 64; j++) {
				result.append((l & 1L << j) == 0 ? '0' : '1');
			}
			result.append(' ');
		}
		return result.toString();
	}

}
