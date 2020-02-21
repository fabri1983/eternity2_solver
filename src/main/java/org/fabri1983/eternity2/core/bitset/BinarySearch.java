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
	 * @param targetVal
	 * @return
	 */
	public static int binarySearch(int[] elems, int low, int high, int targetVal) {
		while (low <= high) {
			int pivot = (low + high) >>> 1; // divided by 2
			int pivotVal = elems[pivot];
	
			if (pivotVal < targetVal) {
				low = pivot + 1; // shift forwards the lower limit to be ahead of pivot
			} else if (pivotVal > targetVal) {
				high = pivot - 1; // shift backwards the higher limit to be before pivot
			} else {
				return pivot + 1; // key found, + 1 so it acts as a factor for our particular interest
			}
		}
		return low; // key not found, but we keep the current low limit
	}
	
	/**
	 * In this binary search we return the index of the found target element + 1 so it acts as a factor.
	 * When target is not found it returns the current low limit.
	 * This way we simulate a range mapping.
	 * 
	 * IMPORTANT: this implementation is modified to return the value we are interesting on.
	 * 
	 * @param elems
	 * @param higherIndex
	 * @param targetVal
	 * @return
	 */
	public static int binarySearch2(int[] elems, int higherIndex, int targetVal) {
		int arrayPointerShifter = 0;
		while (higherIndex > 0) {
			int mid = higherIndex >>> 1; // divided by 2
			if (elems[arrayPointerShifter + mid + 1] <= targetVal) {
				arrayPointerShifter += mid + 1;
				higherIndex -= mid + 1;
			} else 
				higherIndex = mid;
		}
		if (elems[arrayPointerShifter] <= targetVal)
			return arrayPointerShifter + 1; // + 1 so it acts as a factor for our particular interest
		return arrayPointerShifter; // key not found, but we keep the current limit
	}
	
}
