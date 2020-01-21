package org.fabri1983.eternity2.core.mph;

import org.fabri1983.eternity2.core.testutil.Blackhole;
import org.fabri1983.eternity2.core.testutil.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class PerfectHashFunction2Test {

	@Test
	public void testIndexUniqueness() {
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		int[] set = new int[PerfectHashFunction2.PHASHRANGE];
		boolean doPrintAll = false;
		
		System.out.print("evaluating... ");
		for (int key : keys) {
			int index = PerfectHashFunction2.phash(key);
			if (doPrintAll) {
				System.out.println(String.format("%s %s", padRight(index+"", 8), key));
			}
			if (set[index] != 0) {
				Assert.fail(String.format("Duplicated index %s for key %s", index, key));
				System.exit(0);
			} else {
				set[index] = 1;
			}
		}
		System.out.println("done.");
		
		System.out.print("benchmarking... ");
		Blackhole blackhole = new Blackhole();
		int loops=5, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int key : keys) {
				int bucket = PerfectHashFunction2.phash(key);
				blackhole.consume(bucket);
			}
		}
		long timeEval = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				int bucket = PerfectHashFunction2.phash(key);
				blackhole.consume(bucket);
			}
		}
		long nanos = System.nanoTime() - timeEval;
		long nanosPerKey = (nanos/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
	private static String padRight(String inputString, int length) {
	    if (inputString.length() >= length) {
	        return inputString;
	    }
	    StringBuilder sb = new StringBuilder(inputString.length() + length);
	    sb.append(inputString);
	    while (sb.length() < length) {
	        sb.append(' ');
	    }
	 
	    return sb.toString();
	}
	
}
