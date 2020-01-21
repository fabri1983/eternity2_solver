package org.fabri1983.eternity2.core.mph.recsplit;

import java.io.IOException;
import java.net.URISyntaxException;

import org.fabri1983.eternity2.core.testutil.Blackhole;
import org.fabri1983.eternity2.core.testutil.KeysLoader;
import org.junit.Assert;
import org.junit.Test;

/**
 * This unit test loads an existing mphf file produced from project minperf (https://github.com/thomasmueller/minperf) 
 * with manual modifications to use an IntHash function.
 */
public class IntRecSplitEvaluatorTest {

	@Test
	public void testIntRecSplitEvaluatorLoad() throws IOException, URISyntaxException {
		
		System.out.print("loading mphf file... ");
		IntRecSplitEvaluator eval = IntRecSplitLoad.load();
		System.out.println("done.");
		
		System.out.print("evaluating... ");
		int[] keys = KeysLoader.loadSuperMatrizKeys();
        int[] set = new int[keys.length];
		for (int k : keys) {
			int bucket = eval.evaluate(k);
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
				int bucket = eval.evaluate(k);
				blackhole.consume(bucket);
			}
		}
		long timeEval = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int k : keys) {
				int bucket = eval.evaluate(k);
				blackhole.consume(bucket);
			}
		}
		long nanos = System.nanoTime() - timeEval;
		long nanosPerKey = (nanos/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
}
