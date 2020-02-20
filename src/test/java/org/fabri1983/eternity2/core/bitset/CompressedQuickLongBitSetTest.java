package org.fabri1983.eternity2.core.bitset;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.fabri1983.eternity2.util.ArrayShuffler;
import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

public class CompressedQuickLongBitSetTest {

	@Test
	public void testBitAssignmentAndQuery() {
		
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("creating a " + CompressedQuickLongBitSet.class.getSimpleName() + " from hardcoded words[] and indexesForShift[] ... ");
		long timeEval = System.nanoTime();
		CompressedQuickLongBitSet cbs = new CompressedQuickLongBitSet(
				CompressedQuickLongBitSetHardcodedData.words, 
				CompressedQuickLongBitSetHardcodedData.indexesForShift);
		long microsEval = TimeUnit.MICROSECONDS.convert(System.nanoTime() - timeEval, TimeUnit.NANOSECONDS);
		System.out.println(String.format("done. %s micros. Array length: %s(longs)", microsEval, cbs.size()));
		
		System.out.print("evaluating " + CompressedQuickLongBitSet.class.getSimpleName() + " ... ");
		for (int key : keys) {
			boolean isSet = cbs.get(key);
			Assert.assertTrue(isSet);
		}
		System.out.println("done.");
		
		System.out.print("benchmarking quering random keys ... ");
		ArrayShuffler.shuffleArray(keys);
		Blackhole blackhole = new Blackhole();
		int loops=15, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int key : keys) {
				boolean isSet = cbs.get(key);
				blackhole.consume(isSet);
			}
		}
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				boolean isSet = cbs.get(key);
				blackhole.consume(isSet);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerKey = nanosBench/(keys.length*loops);
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
	@Test
	public void testCompressedQuickbitSetCreation() {
		
		// create normal bitset
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		QuickLongBitSet b = new QuickLongBitSet(keys[keys.length - 1] + 1);
		for (int key : keys) {
			b.set(key);
		}
		
		// create compressed bit set
		CompressedQuickLongBitSet cbs = CompressedQuickLongBitSet.createCompressed(b, true);
		
		// evaluate bits set in true
		for (int key : keys) {
			boolean expectedBit = b.get(key);
			boolean actualBit = cbs.get(key);
			if (expectedBit != actualBit) {
				Assert.fail(String.format("At key %s: expected %s, but was %s", key, expectedBit, actualBit));
			}
		}

		// get any other keys
		List<Integer> otherKeys = IntStream.range(0, keys[keys.length - 1])
				.boxed()
				.filter( n -> {
					for (int key : keys) {
						if (key == n.intValue()) {
							return false;
						}
					}
					return true;
				})
				.collect( Collectors.toList() );
		
		// evaluate any other bit not set as true
		for (int key : otherKeys) {
			boolean actualBit = cbs.get(key);
			if (actualBit != false) {
				Assert.fail(String.format("At (any other) key %s: expected %s, but was %s", key, false, actualBit));
			}
		}
	}

}
