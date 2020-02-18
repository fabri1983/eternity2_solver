package org.fabri1983.eternity2.core.mph;

/**
 * This class generated manually from the output algorithm in phash.c produced by the 
 * Bob Jenkins' Minimal Perfect Hash function algorithm, taking as input the misc/super_matriz_decimal.txt file.
 * See README.md file on how to generate that function.
 * 
 * PHASHRANGE - 1 is the greatest value produced by phash(), for the number of provided keys, which means super_matriz[] size must be PHASHRANGE.
 */
public class PerfectHashFunction2Corner {

	public static final int PHASHRANGE = 256;
	
	// PHASHLEN 0x10 = 16
	static short tab[] = { 132, 146, 124, 40, 183, 0, 233, 113, 125, 212, 213, 6, 138, 8, 209, 94 };

	public static int phash(int val) {
		// NOTE: in Java remember to replace >> by >>> to avoid carrying out the bit sign when you know some operations exceed 2^31 - 1
		// I decided to use >>> to always avoid carrying out the sign.
		
		val += 0x6a4bf61d; // PHASHSALT 0x6a4bf61d (31 bits!)
		val ^= (val >>> 16);
		
		/**
		 * IMPORTANT: val += (val << 8)
		 *  val << 8   This exceeds 31 bits for some values of val, and I suspect is down casted to int losing higher 32+ bits (if no later on).
		 *  val += ... This sum and assignment exceeds 31 bits as per before, and might be down casted twice: first at sum result and then at assignment step.
		 * 
		 * However this behavior works fine and produces correct results.
		 */
		val += (val << 8);
		
		val ^= (val >>> 4);
		int b = (val >>> 4) & 0xf; // 0xf = 15 => & 0xf is the fastest way of doing % 0x10 (PHASHLEN 16)
		int a = (val + (val << 12)) >>> 24;
		int rsl = (a ^ tab[b]);
		return rsl; // from 1 up to PHASHRANGE - 1
	}
	
}
