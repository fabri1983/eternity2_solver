package org.fabri1983.eternity2.experimental.bitset.javaewah;

import com.googlecode.javaewah32.EWAHCompressedBitmap32;

import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.util.ArrayShuffler;
import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Test;

public class JavaEwahTest {

	@Test
	public void testBitAssignmentAndQuery() {

		int[] keys = KeysLoader.loadSuperMatrizKeys();
		
		System.out.print("creating Java EWAH Bitmap from keys ... ");
		long timeEval = System.nanoTime();
		EWAHCompressedBitmap32 b = EWAHCompressedBitmap32.bitmapOf(keys);
		long microsEval = TimeUnit.MICROSECONDS.convert(System.nanoTime() - timeEval, TimeUnit.NANOSECONDS);
		System.out.println("done. " + microsEval + " micros");
		
		System.out.print("benchmarking quering random keys ... ");
		ArrayShuffler.shuffleArray(keys);
		Blackhole blackhole = new Blackhole();
		int loops=5, warmups=5;
		for (int loop=0; loop < warmups; ++loop) {
			for (int key : keys) {
				boolean isSet = b.get(key);
				blackhole.consume(isSet);
			}
		}
		long timeBench = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				boolean isSet = b.get(key);
				blackhole.consume(isSet);
			}
		}
		long nanosBench = System.nanoTime() - timeBench;
		long nanosPerKey = (nanosBench/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}