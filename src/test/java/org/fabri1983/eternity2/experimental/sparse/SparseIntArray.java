package org.fabri1983.eternity2.experimental.sparse;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * SparseIntArrays map integers to integers. Unlike a normal array of integers,
 * there can be gaps in the indices. It is intended to be more efficient than
 * using a HashMap to map Integers to Integers.
 */
public class SparseIntArray {

	private int[] mKeys;
	private int[] mValues;
	private int mSize;

	public SparseIntArray(int initialCapacity) {
		initialCapacity = idealIntArraySize(initialCapacity);

		mKeys = new int[initialCapacity];
		mValues = new int[initialCapacity];
		mSize = 0;
	}

	/**
	 * Gets the int mapped from the specified key, or <code>-1</code> if no such
	 * mapping has been made.
	 */
	public int get(int key) {
		return get(key, -1);
	}

	/**
	 * Gets the int mapped from the specified key, or the specified value if no such
	 * mapping has been made.
	 */
	public int get(int key, int valueIfKeyNotFound) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i < 0) {
			return valueIfKeyNotFound;
		} else {
			return mValues[i];
		}
	}

	/**
	 * Adds a mapping from the specified key to the specified value, replacing the
	 * previous mapping from the specified key if there was one.
	 */
	public void put(int key, int value) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			mValues[i] = value;
		} else {
			i = ~i;

			if (mSize >= mKeys.length) {
				int n = idealIntArraySize(mSize + 1);

				int[] nkeys = new int[n];
				int[] nvalues = new int[n];

				// Log.e("SparseIntArray", "grow " + mKeys.length + " to " + n);
				System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
				System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

				mKeys = nkeys;
				mValues = nvalues;
			}

			if (mSize - i != 0) {
				// Log.e("SparseIntArray", "move " + (mSize - i));
				System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
				System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
			}

			mKeys[i] = key;
			mValues[i] = value;
			mSize++;
		}
	}

	/**
	 * Returns the number of key-value mappings that this SparseIntArray currently
	 * stores.
	 */
	public int numberOfMappings() {
		return mSize;
	}

	public int lengthOfKeysArray() {
		return mKeys.length;
	}
	
	public int lengthOfValuesArrays() {
		return mValues.length;
	}
	
	/**
	 * Puts a key/value pair into the array, optimizing for the case where the key
	 * is greater than all existing keys in the array.
	 */
	public void append(int key, int value) {
		if (mSize != 0 && key <= mKeys[mSize - 1]) {
			put(key, value);
			return;
		}

		int pos = mSize;
		if (pos >= mKeys.length) {
			int n = idealIntArraySize(pos + 1);

			int[] nkeys = new int[n];
			int[] nvalues = new int[n];

			// Log.e("SparseIntArray", "grow " + mKeys.length + " to " + n);
			System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
			System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

			mKeys = nkeys;
			mValues = nvalues;
		}

		mKeys[pos] = key;
		mValues[pos] = value;
		mSize = pos + 1;
	}

	private static int binarySearch(int[] a, int start, int len, int key) {
		int high = start + len, low = start - 1, guess;

		while (high - low > 1) {
			guess = (high + low) / 2;

			if (a[guess] < key)
				low = guess;
			else
				high = guess;
		}

		if (high == start + len)
			return ~(start + len);
		else if (a[high] == key)
			return high;
		else
			return ~high;
	}

	private static int idealIntArraySize(int need) {
		return idealByteArraySize(need * 4) / 4;
	}
	
	private static int idealByteArraySize(int need) {
		for (int i = 4; i < 32; i++)
			if (need <= (1 << i) - 12)
				return (1 << i) - 12;
		return need;
	}
	
}
