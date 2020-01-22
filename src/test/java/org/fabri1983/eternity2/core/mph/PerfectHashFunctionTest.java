package org.fabri1983.eternity2.core.mph;

import java.util.ArrayList;
import java.util.List;

import org.fabri1983.eternity2.util.Blackhole;
import org.fabri1983.eternity2.util.KeysLoader;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PerfectHashFunctionTest {

	@Test
	public void testIndexUniqueness() {
		int[] keys = KeysLoader.loadSuperMatrizKeys();
		int[] set = new int[PerfectHashFunction.PHASHRANGE];
		boolean doPrintAll = false;
		
		System.out.print("evaluating... ");
		for (int key : keys) {
			int index = PerfectHashFunction.phash(key);
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
				int bucket = PerfectHashFunction.phash(key);
				blackhole.consume(bucket);
			}
		}
		long timeEval = System.nanoTime();
		for (int loop=0; loop < loops; ++loop) {
			for (int key : keys) {
				int bucket = PerfectHashFunction.phash(key);
				blackhole.consume(bucket);
			}
		}
		long nanos = System.nanoTime() - timeEval;
		long nanosPerKey = (nanos/keys.length)/loops;
		System.out.println("done. " + nanosPerKey + " nanos/key");
	}
	
	@Test
	public void testDeflateInflateTab() {
		// Since integers x in PerfectHashFunction.tab are in the range [0, 2^16] then we can pack 2 of them into 1 int (2^32).
		// We consider the fact that Java uses last high order bit as the bit sign so when inflating use >>> operand.
		
		// generate deflated array
		int[] tabDeflated = new int[PerfectHashFunction.tab.length/2]; // tab[].length is ensured to be power of 2
		for (int i=0; i < tabDeflated.length; ++i) {
			int high = PerfectHashFunction.tab[(2*i)];
			int low = PerfectHashFunction.tab[(2*i) + 1];
			tabDeflated[i] = high << 16 | low;
		}
		
		boolean doPrint = false;
		if (doPrint) {
			List<Short> list = toListShort(PerfectHashFunction.tab);
			list.forEach( n -> System.out.println(n + ","));
		}
		
		// evaluate inflated array
		for (int i=0; i < tabDeflated.length; ++i) {
			short high = PerfectHashFunction.tab[(2*i)];
			short low = PerfectHashFunction.tab[(2*i) + 1];
			int deflated = tabDeflated[i];
			int highInflated = deflated >>> 16;
			int lowInflated = deflated & 0xffff;
			Assert.assertEquals("high numbers don't match.", high, highInflated);
			Assert.assertEquals("low numbers don't match.", low, lowInflated);
		}
	}
	
	@Test
	public void testMinAndMaxIntegers() {
		boolean doPrint = false;
		
		List<Short> list = toListShort(PerfectHashFunction.tab);
		
		int min = list.stream()
			.filter( i -> i != 0 )
			.min( Integer::compare )
			.orElseThrow(); // 46
		
		if (doPrint)
			System.out.println(String.format("Min number in tab[] (not zero): %s", min));
		
		int max = list.stream()
			.max( Integer::compare )
			.orElseThrow(); // 16121
		
		if (doPrint)
			System.out.println(String.format("Max number in tab[]: %s", max));
	}

	private List<Short> toListShort(short[] tab) {
		List<Short> list = new ArrayList<>(tab.length);
		for (short t : tab)
			list.add(t);
		return list;
	}

	@Test
	@Ignore
	public void testDivisionReduction() {
		// Using int numbers:
		// ------------------
		// From https://www.avrfreaks.net/forum/division-only-bitwise-shifts
		//
        // 1/93 = 0.01075268817204301075268817204301
        // 1/93 is roughly 705/65536 = 705/2^16
		// 705 comes from 65536/93 = 2^16/93 = 704.69
		//
		// First find power of 2 numbers that add up to 704/65536:
		//   512/65536 + 128/65536 + 64/65536 = 704/65536
		// (Note here that 704/65536 = 0.0107421875 (error appears at 4th decimal position in comparison with 1/93))
		//
		// This leave us with next formula:
		//  x * 1/93 ~= x * ( 512/65536 + 128/65536 + 64/65536 )
		//
		// Then reduce the fractions since dividend and divisor are power of 2:
		//     1/128   +   1/512   +  1/1024‬
		//
		// There you have your shifting values, leaving us with the next formula:
		//    (x >> 7) +  (x >> 9) + (x >> 10) ~= x/93
		//
		// Ending with the formula:
		//    (x >> 7) +  (x >> 9) + (x >> 10)
		//
		// THIS I DON'T KNOW HOW IT COMES:
		// If you maintain that remainder at each step, the error is eliminated:
		// (note: now using << instead of >>)
		//    ((x << 11) + (x << 9) + (x << 8) + (x << 1) + x) >> 18
		// 
		// There are some limitations: since we start by shifting to the left by 11 bits, the operand must be constrained. 
		// If we employ 32-bit arithmetic, the operand would be limited to 21 bits. Not a problem if you're working with 16-bit operands. 
		// And of course, the need to work with 32-bit intermediate results will have a speed penalty over working with 16 bits.
		// 
		// Optimized formula:
		//    (x << 1) + x
		//    and
		//    (x << 9) + (x << 8)
		//    are the same number, just shifted one byte, witch is almost free.
		// So formula ends up being:
		//    temp = (x << 1) + x;
		//    result = ((x << 11) + (temp << 8) + temp) >> 18;
		
		for (int x = 93; x <= 16121; ++x) {
			int expected = x/93;
			int temp = (x << 1) + x;
			int actual = ((x << 11) + (temp << 8) + temp) >> 18;
			Assert.assertEquals(String.format("Values don't match for x=%s.", x), expected, actual);
		}
		
		/**
		 * Using long numbers:
		 * -------------------
		 * 1/93 = 0.01075268817204301075268817204301
		 * 1/93 is roughly 45100/2^22 = 45100/4194304 = 1/93,00008869
		 * 45100/4194304 = 0.01075267791
		 * This has the problem that you are dividing with more that 1/93, so 93 gives 0.999999046 and not 1.
		 * But if we use 45101 all numbers up to 16384 are correct.
		 * 
		 * First find power of 2 numbers that add up to 45100/2^22:
		 *    (2^15 + 2^13 + 2^12 + 2^5 + 2^3 + 2^2)/2^22 = 45100/2^22
		 * 
		 * This leave us with next formula:
		 *   x * 1/93 ~= x * (2^15 + 2^13 + 2^12 + 2^5 + 2^3 + 2^2)/2^22
		 * 
		 * Then reduce the fractions since dividend and divisor are power of 2:
		 *   1/2^7 + 1/2^9 + 1/2^10 + 1/2^17 + 1/2^20
		 *  
		 * There you have your shifting values, leaving us with the next formula:
		 *   (x >> 7) +  (x >> 9) + (x >> 10) + (x >> 17) + (x >> 20) ~= x/93
		 * 
		 * ... continue this ...
		 */
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
