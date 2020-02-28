/*
 * Copyright (c) 1995, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.fabri1983.eternity2.core.bitset;

/**
 * IMPORTANT: this is a modified version with no bounds checks and also only intended to use with the BitSet(n) constructor.
 * 
 * This class implements a vector of bits that grows as needed. Each
 * component of the bit set has a {@code boolean} value. The
 * bits of a {@code BitSet} are indexed by nonnegative integers.
 * Individual indexed bits can be examined, set, or cleared. One
 * {@code BitSet} may be used to modify the contents of another
 * {@code BitSet} through logical AND, logical inclusive OR, and
 * logical exclusive OR operations.
 *
 * <p>By default, all bits in the set initially have the value
 * {@code false}.
 *
 * <p>Every bit set has a current size, which is the number of bits
 * of space currently in use by the bit set. Note that the size is
 * related to the implementation of a bit set, so it may change with
 * implementation. The length of a bit set relates to logical length
 * of a bit set and is defined independently of implementation.
 *
 * <p>Unless otherwise noted, passing a null parameter to any of the
 * methods in a {@code BitSet} will result in a
 * {@code NullPointerException}.
 *
 * <p>A {@code BitSet} is not safe for multithreaded use without
 * external synchronization.
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @author  Martin Buchholz
 * @since   1.0
 */
public class QuickLongBitSet {

	/*
     * BitSets are packed into arrays of "words."  Currently a word is
     * a long, which consists of 64 bits, requiring 6 address bits.
     * The choice of word size is determined purely by performance concerns.
     */
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    
    long[] words;

	/**
	 * Creates a bit set whose initial size is large enough to explicitly
	 * represent bits with indices in the range {@code 0} through
	 * {@code nbits-1}. All bits are initially {@code false}.
	 *
	 * @param  nbits the initial size of the bit set
	 */
	public QuickLongBitSet(int nbits) {
	    initWords(nbits);
	}

	public QuickLongBitSet(long[] bits) {
		words = bits;
	}
	
	/**
     * Sets the bit at the specified index to {@code true}.
     * IMPORTANT: remember that setting a bit is from LSB (most right bit) to MSB (left most bit)
     *
     * @param  bitIndex a bit index
     * @since  1.0
     */
    public void set(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        long mask = 1L << bitIndex; // here it seems the compiler does: 1L << (bitIndex & (BITS_PER_WORD - 1))
		words[wordIndex] |= mask;
    }
    
    /**
     * Sets the bit at the specified index to {@code false}.
     * IMPORTANT: remember that setting a bit is from LSB (most right bit) to MSB (left most bit)
     *
     * @param  bitIndex a bit index
     * @since  1.0
     */
	public void clear(int bitIndex) {
		int wordIndex = wordIndex(bitIndex);
		words[wordIndex] &= ~(1L << bitIndex); // here it seems the compiler does: 1L << (bitIndex & (BITS_PER_WORD - 1))
	}
    
    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code BitSet}; otherwise, the result
     * is {@code false}.
     * IMPORTANT: remember that reading a bit is from LSB (most right bit) to MSB (left most bit)
     *
     * @param  bitIndex   the bit index
     * @return the value of the bit with the specified index
     */
    public boolean get(int bitIndex) {
        int wordIndex = wordIndex(bitIndex);
        long mask = 1L << bitIndex; // here it seems the compiler does: 1L << (bitIndex & (BITS_PER_WORD - 1))
		return (words[wordIndex] & mask) != 0;
    }
    
    /**
     * Returns the number of bits of space actually in use by this
     * {@code BitSet} to represent bit values.
     * The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set
     */
    public int sizeInBits() {
        return words.length * BITS_PER_WORD;
    }
    
    public int size() {
        return words.length;
    }
    
    public String toStringAll() {
    	StringBuilder builder = new StringBuilder(words.length * BITS_PER_WORD);
    	for (long num : words) {
    		builder.append(longToBinary(num)).append("\n");
    	}
    	return builder.toString();
    }
    
    private static String longToBinary(long number) {
    	return String.format("%64s", Long.toBinaryString(number)).replaceAll(" ", "0");
	}

	private void initWords(int nbits) {
	    words = new long[wordIndex(nbits-1) + 1];
	}

	/**
	 * Given a bit index, return word index containing it.
	 */
	private static int wordIndex(int bitIndex) {
	    return bitIndex >>> ADDRESS_BITS_PER_WORD;
	}

}
