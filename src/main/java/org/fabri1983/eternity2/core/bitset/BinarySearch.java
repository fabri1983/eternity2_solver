package org.fabri1983.eternity2.core.bitset;

/**
 * Binary search intended to use with {@link CompressedQuickLongBitSet} class.
 */
public class BinarySearch {

	/**
	 * In this binary search we return the index of the found target element + 1 so it acts as a factor.
	 * When target is not found it returns the current low limit.
	 * This way we simulate a range mapping.
	 * 
	 * IMPORTANT: this implementation is modified to return the value we are interesting on.
	 * 
	 * @param elems
	 * @param low
	 * @param high
	 * @param targeVal
	 * @return
	 */
	public static int binarySearch(int[] elems, int low, int high, int targeVal) {
		while (low <= high) {
			int pivot = (low + high) >>> 1; // divided by 2
			int pivotVal = elems[pivot];
	
			if (pivotVal < targeVal) {
				low = pivot + 1; // shift forwards the lower limit to be ahead of pivot
			} else if (pivotVal > targeVal) {
				high = pivot - 1; // shift backwards the higher limit to be before pivot
			} else {
				return pivot + 1; // key found, + 1 so it acts as a factor for our particular interest
			}
		}
		return low; // key not found, but we keep the current low limit
	}
	
}
