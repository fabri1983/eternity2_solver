package org.fabri1983.eternity2.experimental.bitset.misc;

/**
 * A simplified and streamlined version of {@link java.util.BitSet}.
 */
public class FastlshBitSet {

	public final long[] words;

	/**
	 * Creates a BitSet with the specified number of bits.
	 * 
	 * @param numBits number of bits to store in the BitSet
	 */
	public FastlshBitSet(int numBits) {
		int numLongs = numBits >>> 6;
		// 0x3F = 0b111111 = 63
		if ((numBits & 0x3F) != 0) {
			numLongs++;
		}
		words = new long[numLongs];
	}

	/**
	 * Gets the bit stored at a particular index.
	 * 
	 * @param index index whose bit is desired.
	 * @return
	 */
	public boolean get(int index) {
		// skipping range check for speed
		return (words[index >>> 6] & 1L << (index & 0x3F)) != 0L;
	}

	/**
	 * Sets a value to be stored in a bit at a particular index.
	 * 
	 * @param index index to store the bit
	 * @param b     boolean encoding of the bit to be stored
	 */
	public void set(int index, boolean b) {
		if (b)
			words[index >>> 6] |= 1L << (index & 0x3F);
		else
			words[index >>> 6] &= ~(1L << (index & 0x3F));
	}

	/**
	 * Sets the bit at a specified index to 1.
	 * 
	 * @param index index whose value is to be set to 1
	 */
	public void set(int index) {
		// skipping range check for speed
		words[index >>> 6] |= 1L << (index & 0x3F);
	}

	/**
	 * Sets the bit at a specified index to 0.
	 * 
	 * @param index
	 */
	public void clear(int index) {
		// skipping range check for speed
		words[index >>> 6] &= ~(1L << (index & 0x3F));
	}

	/**
	 * Sets all the bits to 0.
	 */
	public void clear() {
		int length = words.length;
		for (int i = 0; i < length; i++) {
			words[i] = 0L;
		}
	}
	
	public int getNumWords() {
		return words.length;
	}
	
	public String toStringAll() {
    	StringBuilder builder = new StringBuilder(words.length * 64);
    	for (long num : words) {
    		builder.append(longToBinary(num)).append("\n");
    	}
    	return builder.toString();
    }
    
    private String longToBinary(long number) {
    	return String.format("%64s", Long.toBinaryString(number)).replaceAll(" ", "0");
	}

}
