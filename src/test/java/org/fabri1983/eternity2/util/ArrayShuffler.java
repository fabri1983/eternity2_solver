package org.fabri1983.eternity2.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ArrayShuffler {

	/**
	 * Fisher–Yates shuffle algorithm.
	 * 
	 * @param ar An int[] array.
	 */
	public static void shuffleArray(int[] ar) {
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);
			// Simple swap
			int a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
	
}
