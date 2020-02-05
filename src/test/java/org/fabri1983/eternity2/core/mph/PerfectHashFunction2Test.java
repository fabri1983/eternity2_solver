package org.fabri1983.eternity2.core.mph;

import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class PerfectHashFunction2Test {
	
	@Test
	public void testIndexUniquenessForKeys() {
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		int[] set = new int[PerfectHashFunction2.PHASHRANGE];
		
		System.out.print("evaluating keys set ... ");
		for (int key : keys) {
			int index = PerfectHashFunction2.phash(key);
			if (set[index] != 0) {
				Assert.fail(String.format("Duplicated index %s for key %s", index, key));
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
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				int bucket = PerfectHashFunction2.phash(key);
				blackhole.consume(bucket);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerKey = (nanosBench/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
