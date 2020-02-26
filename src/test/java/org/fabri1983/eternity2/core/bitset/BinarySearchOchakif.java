package org.fabri1983.eternity2.core.bitset;

/**
 * Binary search intended to use with {@link CompressedQuickLongBitSet} class.
 * 
 * @author http://ochafik.com/p_106
 */
public class BinarySearchOchakif {

	private static final int MAX_SMART_PIVOT_USES = 1;
	
	/**
	 * In this binary search we return the index of the found target element + 1 so it acts as a factor.
	 * When target is not found it returns the current low limit.
	 * This way we simulate a range mapping.
	 * This implementation tries to take advantage of a smart pivot knowing the minimum and maximum value of the array.
	 * 
	 * IMPORTANT: this implementation is modified to return the value we are interesting on.
	 * 
	 * @param elems
	 * @param low
	 * @param high
	 * @param targetVal
	 * @return
	 * 
	 * @Author http://ochafik.com/p_106
	 */
	public static int binarySearch(int[] elems, int low, int high, int targetVal) {
		
		int nPreviousSteps = 0;
		int minVal = elems[low];
		int maxVal = elems[high];
		
		while (low <= high) {
			
			if (targetVal == minVal) {
				return low + 1; // key found, + 1 so it acts as a factor for our particular interest
			}
			else if (targetVal < minVal) {
				return low; // key not found, but we keep the current low limit
			}
			if (targetVal >= maxVal) {
				// here it doesn't matter if key matches the max value or is bigger than it
				return high + 1; // + 1 so it acts as a factor for our particular interest
			}
			
			// A typical binarySearch algorithm uses pivot = (min + max) / 2.
			// The pivot we use here tries to be smarter and to choose a pivot close to the expectable location of the key.
			// This reduces dramatically the number of steps needed to get to the key.
			// However, it does not work well with a logarithmic distribution of values, for instance.
			// When the key is not found quickly with the smart way, we switch to the standard pivot.
			int pivot;
			if (nPreviousSteps < MAX_SMART_PIVOT_USES) {
				pivot = low + (targetVal - minVal) / (maxVal - minVal) * (high - low);
				nPreviousSteps++;
			} else {
				pivot = (low + high) >>> 1; // divided by 2
			}
			
			int pivotVal = elems[pivot];
	
			if (pivotVal < targetVal) {
				low = pivot + 1; // shift forwards the lower limit to be ahead of pivot
				high--;
			} else if (pivotVal > targetVal) {
				high = pivot - 1; // shift backwards the higher limit to be before pivot
				low++;
			} else {
				return pivot + 1; // key found, + 1 so it acts as a factor for our particular interest
			}

			minVal = elems[low];
			maxVal = elems[high];
		}
		
		return low; // key not found, but we keep the current low limit
	}
	
}
