package org.fabri1983.eternity2.experimental.bitset.misc;

import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.util.ArrayShuffler;
import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class OpenBitSetTest {

	@Test
	public void testBitAssignmentAndQuery() {

		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("creating a Lucene's " + OpenBitSet.class.getSimpleName() + " from keys ... ");
		long timeEval = System.nanoTime();
		OpenBitSet b = new OpenBitSet(keys[keys.length - 1] + 1);
		for (int key : keys) {
			b.fastSet(key);
		}
		long microsEval = TimeUnit.MICROSECONDS.convert(System.nanoTime() - timeEval, TimeUnit.NANOSECONDS);
		System.out.println(String.format("done. %s micros. Array length: %s(longs)", microsEval, b.getNumWords()));
		
		System.out.print("evaluating Lucene's " + OpenBitSet.class.getSimpleName() + " ... ");
		for (int key : keys) {
			boolean isSet = b.fastGet(key);
			Assert.assertTrue(isSet);
		}
		System.out.println("done.");
		
		System.out.print("benchmarking quering random keys ... ");
		ArrayShuffler.shuffleArray(keys);
		Blackhole blackhole = new Blackhole();
		int loops=15, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int key : keys) {
				boolean isSet = b.fastGet(key);
				blackhole.consume(isSet);
			}
		}
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				boolean isSet = b.fastGet(key);
				blackhole.consume(isSet);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerKey = nanosBench/(keys.length*loops);
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
