package org.fabri1983.eternity2.core.mph;

import java.util.ArrayList;
import java.util.List;

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
	
	@Test
	public void testDeflateInflateTab() {
		// Since integers x in PerfectHashFunction2.tab are in the range [0, 2^16] then we can pack 2 of them into 1 int (2^32).
		// We consider the fact that Java uses last high order bit as the bit sign so when inflating use >>> operand.
		
		// generate deflated array
		int[] tabDeflated = new int[PerfectHashFunction2.tab.length/2]; // tab[].length is ensured to be power of 2
		for (int i=0; i < tabDeflated.length; ++i) {
			int high = PerfectHashFunction2.tab[(2*i)];
			int low = PerfectHashFunction2.tab[(2*i) + 1];
			tabDeflated[i] = high << 16 | low;
		}
		
		boolean doPrint = false;
		if (doPrint) {
			List<Short> list = toListShort(PerfectHashFunction2.tab);
			list.forEach( n -> System.out.println(n + ","));
		}
		
		// evaluate inflated array
		for (int i=0; i < tabDeflated.length; ++i) {
			short high = PerfectHashFunction2.tab[(2*i)];
			short low = PerfectHashFunction2.tab[(2*i) + 1];
			int deflated = tabDeflated[i];
			int highInflated = deflated >>> 16;
			int lowInflated = deflated & 0xffff;
			Assert.assertEquals("high numbers don't match.", high, highInflated);
			Assert.assertEquals("low numbers don't match.", low, lowInflated);
		}
	}
	
	private List<Short> toListShort(short[] tab) {
		List<Short> list = new ArrayList<>(tab.length);
		for (short t : tab)
			list.add(t);
		return list;
	}
	
}
