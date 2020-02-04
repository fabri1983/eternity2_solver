package org.fabri1983.eternity2.experimental.bitset;

import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.util.ArrayShuffler;
import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Test;

public class BooleanBitSetTest {

	@Test
	public void testBitAssignmentAndQuery() {

		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("creating a java boolean[] from keys ... ");
		long timeEval = System.nanoTime();
		boolean[] b = new boolean[keys[keys.length - 1] + 1];
		for (int key : keys) {
			b[key] = true;
		}
		long microsEval = TimeUnit.MICROSECONDS.convert(System.nanoTime() - timeEval, TimeUnit.NANOSECONDS);
		System.out.println("done. " + microsEval + " micros");
		
		System.out.print("benchmarking quering random keys ... ");
		ArrayShuffler.shuffleArray(keys);
		Blackhole blackhole = new Blackhole();
		int loops=5, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int key : keys) {
				boolean isSet = b[key];
				blackhole.consume(isSet);
			}
		}
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				boolean isSet = b[key];
				blackhole.consume(isSet);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerKey = (nanosBench/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
