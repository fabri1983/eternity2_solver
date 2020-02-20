/**
 * Copyright (c) 2019 Fabricio Lettieri fabri1983@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fabri1983.eternity2.core.bitset;

import org.fabri1983.eternity2.arrays.Arrays;
import org.fabri1983.eternity2.arrays.IntArrayList;

public class CompressedQuickLongBitSet {

	private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    
    /**
     * How many consecutive rows of zeros we want to remove. 
     * Got experimentally from a very specific case. You'll have to experiment and see what number works for you.
     * Use the test CompressedQuickLongBitSetTest which prints some statistics.
     */
 	private static final int consecutiveRowsOfZeros = 12;
 	
    private long[] words;
    private int[] indexesForShift;
    
	public CompressedQuickLongBitSet(long[] bits, int[] indexesForShift) {
		this.words = bits;
		this.indexesForShift = indexesForShift;
	}
	
	public static CompressedQuickLongBitSet createCompressed(QuickLongBitSet bitSet, boolean printStats) {
		
		// collect all indices where start appearing consecutive zeros
		int[] indexesForShift = collectShiftedIndexes(bitSet.words, consecutiveRowsOfZeros);
		int biggerGapShiftIndexes = maxGap(indexesForShift);
		
		// print stats?
		if (printStats) {
			printStats(bitSet.size(), consecutiveRowsOfZeros, indexesForShift.length, biggerGapShiftIndexes);
		}
		
		// remove rows of 0s and shift the valid ones
		long[] newWords = shiftLongs(bitSet.words, consecutiveRowsOfZeros, indexesForShift);
		
		CompressedQuickLongBitSet cbs = new CompressedQuickLongBitSet(newWords, indexesForShift);
		return cbs;
	}
	
	private static int maxGap(int[] indexesForShift) {
		int maxGap = 0;
		for (int i=1; i < indexesForShift.length; ++i) {
			int n = Math.abs(indexesForShift[i] - indexesForShift[i - 1]);
			if (n > maxGap)
				maxGap = n;
		}
		return maxGap;
	}

	/**
	 * Search for positions in words[] having X consecutive rows of zeros. A row of zero is just a long value 0.
	 * 
	 * @param words
	 * @param consecutiveRowsOfZeros
	 * @return
	 */
	private static int[] collectShiftedIndexes(long[] words, int consecutiveRowsOfZeros) {
		
		// approximated size, it's an upper limit, never will be that value unless all array is full of 0s
		int initialCapacity = (words.length + 1) / consecutiveRowsOfZeros; 
		IntArrayList listIndexesForShift = new IntArrayList(initialCapacity);
		
		int partialCountRows = 0;
		for (int i=0; i < words.length; ++i) {
			long v = words[i];
			// reset
			if (v != 0) partialCountRows = 0;
			// keep counting
			else ++partialCountRows;
			
			// when reaching the limit and the index and reset the counter
			if (partialCountRows == consecutiveRowsOfZeros) {
				listIndexesForShift.add((i - consecutiveRowsOfZeros) + 1);
				partialCountRows = 0;
			}
		}
		
		return Arrays.trimToCapacity(listIndexesForShift.elements(), listIndexesForShift.size());
	}
	
	private static void printStats(int bitsetSize, int consecutiveRowsOfZeros, int totalGroups, int biggerGapShiftIndexes) {
		
		System.out.println(CompressedQuickLongBitSet.class.getSimpleName() + " stats:");
		System.out.println("  " + totalGroups + " groups (longs) of " + consecutiveRowsOfZeros + " consecutive 0s to remove. " 
				+ "Max Gap between indexes: " + biggerGapShiftIndexes);
		
		int removeFromCurrentBitset = consecutiveRowsOfZeros * totalGroups;
		int newBitSetSize = bitsetSize - removeFromCurrentBitset;
		System.out.println("  Can remove " + removeFromCurrentBitset + " longs from current " +
				QuickLongBitSet.class.getSimpleName() + " of " + bitsetSize + " longs. New size: " + newBitSetSize + " longs.");
		
		System.out.println("  But will use 1 additional array of size " + totalGroups + " to hold the shift indexes and do binary search.");
		
		int realGain = removeFromCurrentBitset - totalGroups;
		System.out.println("  So real gain is: " + realGain);
	}

	private static long[] shiftLongs(long[] words, int consecutiveRowsOfZeros, int[] indexesForShift) {
		
		int totalGroups = indexesForShift.length;
		int removeFromCurrentBitset = consecutiveRowsOfZeros * totalGroups;
		int newBitSetSize = words.length - removeFromCurrentBitset;
		long[] targetArray = new long[newBitSetSize];
		
		int pivotIndex = 0;
		int newIndex = 0;
		for (int i=0; i < indexesForShift.length; ++i) {
			
			int x = indexesForShift[i];
			
			// copy values from original array from [pivot, pivot + x)
			int lengthToCopy = x - pivotIndex;
			System.arraycopy(words, pivotIndex, targetArray, newIndex, lengthToCopy);
			
			// update index for target array
			newIndex += lengthToCopy;
			
			// make pivot skips all zeros from x + consecutiveRowsOfZeros
			// this way pivot is at next valid position
			pivotIndex = x + consecutiveRowsOfZeros;
		}
		
		// copy the remaining elements
		System.arraycopy(words, pivotIndex, targetArray, newIndex, words.length - pivotIndex);
		
		return targetArray;
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
    	// the expanded word index simulates the index as if it was in QuickLongBitSet
    	int expandedWordIndex = bitIndex >>> ADDRESS_BITS_PER_WORD;
		
		// binary search the shift factor to be applied over expanded word
		int factor = BinarySearch.binarySearch(indexesForShift, 0, indexesForShift.length - 1, expandedWordIndex);
		
		// if expanded word index is within the removed rows then it means the bitIndex was originally 0 (not set)
		if (factor > 0 && (expandedWordIndex - indexesForShift[factor - 1]) < consecutiveRowsOfZeros)
			return false;
		
		int wordIndex = expandedWordIndex - (factor * consecutiveRowsOfZeros);
        long mask = 1L << bitIndex; // here it seems the compiler does: 1L << (bitIndex & (BITS_PER_WORD - 1))
		return (words[wordIndex] & mask) != 0;
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

}
