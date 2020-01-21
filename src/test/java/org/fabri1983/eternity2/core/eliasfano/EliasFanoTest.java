package org.fabri1983.eternity2.core.eliasfano;

import org.fabri1983.eternity2.core.testutil.Blackhole;
import org.fabri1983.eternity2.core.testutil.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class EliasFanoTest {

	@Test
	public void testSparseIntArray() {
		
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("generating EliasFano compressed array... ");
		// compress the array
		byte[] compressed = EliasFano.compress(keys, 0, keys.length);
		int u = keys[keys.length - 1]; // the maximum value in a;
		int L = EliasFano.getL(u, keys.length); // the number of lower bits
		System.out.println(String.format("done. compressed array length=%s, lower bits=%s", 
				compressed.length, L));
		
		System.out.print("evaluating... ");
		// pre calculate some fixed values
		int keysSize = keys.length;
		final long lowBitsOffset = 0 * Byte.SIZE;
		final long highBitsOffset = EliasFano.roundUp(lowBitsOffset + (L * keysSize), Byte.SIZE);
		final int startOffset = (int) (highBitsOffset / Byte.SIZE);
		
		int[] set = new int[keys.length];
		for (int k : keys) {
			// get the index of the first element, in the compressed data, greater or equal than k
			int bucket = EliasFano.select2(compressed, 0, keysSize, L, k, lowBitsOffset, startOffset);
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
				// get the index of the first element, in the compressed data, greater or equal than k
				int bucket = EliasFano.select2(compressed, 0, keysSize, L, k, lowBitsOffset, startOffset);
				blackhole.consume(bucket);
			}
		}
		long timeEval = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int k : keys) {
				// get the index of the first element, in the compressed data, greater or equal than k
				int bucket = EliasFano.select2(compressed, 0, keysSize, L, k, lowBitsOffset, startOffset);
				blackhole.consume(bucket);
			}
		}
		long nanos = System.nanoTime() - timeEval;
		long nanosPerKey = (nanos/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
