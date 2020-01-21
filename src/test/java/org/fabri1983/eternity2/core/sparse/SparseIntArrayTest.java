package org.fabri1983.eternity2.core.sparse;

import org.fabri1983.eternity2.core.testutil.Blackhole;
import org.fabri1983.eternity2.core.testutil.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class SparseIntArrayTest {

	@Test
	public void testSparseIntArray() {
		
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("generating SparseIntArray... ");
		SparseIntArray sparseIntArray = new SparseIntArray(keys.length);
		// Note: append(k, v) used only when keys are traversed in ascendent order
		int i = 0;
		for (int k : keys) {
			sparseIntArray.append(k, i);
			++i;
		}
		System.out.println(String.format("done. Mappings=%s, Keys array length=%s, Values array length=%s", 
				sparseIntArray.numberOfMappings(), sparseIntArray.lengthOfKeysArray(), sparseIntArray.lengthOfValuesArrays()));
		
		System.out.print("evaluating... ");
        int[] set = new int[keys.length];
		for (int k : keys) {
			int bucket = sparseIntArray.get(k);
			if (set[bucket] != 0) {
				Assert.fail(String.format("Duplicated value %s for key %s", bucket, k));
			} else {
				set[bucket] = 1;
			}
		}
		System.out.println("done.");
		
		System.out.print("benchmarking... ");
		Blackhole blackhole = new Blackhole();
		int loops=5, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int k : keys) {
				int bucket = sparseIntArray.get(k);
				blackhole.consume(bucket);
			}
		}
		long timeEval = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int k : keys) {
				int bucket = sparseIntArray.get(k);
				blackhole.consume(bucket);
			}
		}
		long nanos = System.nanoTime() - timeEval;
		long nanosPerKey = (nanos/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
