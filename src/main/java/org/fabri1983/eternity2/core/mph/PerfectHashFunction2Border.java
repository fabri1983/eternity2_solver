package org.fabri1983.eternity2.core.mph;

/**
 * This class generated manually from the output algorithm in phash.c produced by the 
 * Bob Jenkins' Minimal Perfect Hash function algorithm, taking as input the misc/super_matriz_decimal.txt file.
 * See README.md file on how to generate that function.
 * 
 * PHASHRANGE - 1 is the greatest value produced by phash(), for the number of provided keys, which means super_matriz[] size must be PHASHRANGE.
 */
public class PerfectHashFunction2Border {

	public static final int PHASHRANGE = 2048;
	
	// PHASHLEN 0x80 = 128
	static short tab[] = { 1041, 976, 1871, 814, 94, 395, 1413, 111, 1399, 34, 1721, 0, 1459, 823, 517, 506, 1204, 180,
			239, 1981, 917, 11, 1884, 711, 1665, 137, 1549, 321, 1732, 1544, 1841, 0, 630, 764, 1615, 21, 1791, 1409,
			764, 216, 83, 99, 274, 0, 1689, 823, 1543, 2014, 1791, 943, 1509, 239, 1919, 336, 821, 1738, 691, 11, 1312,
			1174, 894, 1654, 629, 0, 41, 341, 1213, 623, 553, 1042, 597, 736, 1045, 0, 323, 1620, 413, 338, 83, 231,
			1351, 1273, 597, 1095, 665, 753, 2040, 1705, 399, 870, 924, 1699, 211, 1044, 1090, 1203, 2014, 204, 1506,
			786, 1933, 885, 1855, 1628, 265, 1955, 399, 1497, 478, 605, 139, 1699, 937, 1042, 931, 372, 427, 271, 1226,
			1364, 242, 1312, 580, 572, 1407, 452, 403, 712 };

	public static int phash(int val) {
		// NOTE: in Java remember to replace >> by >>> to avoid carrying out the bit sign when you know some operations exceed 2^31 - 1
		// I decided to use >>> to always avoid carrying out the sign.
		
		val += 0x8a32851f; // PHASHSALT 0x8a32851f (31 bits!)
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
		int b = (val >>> 6) & 0x7f; // 0x7f = 127 => & 0xff is the fastest way of doing % 0x80 (PHASHLEN 128)
		int a = (val + (val << 8)) >>> 21;
		int rsl = (a ^ tab[b]);
		return rsl; // from 0 up to PHASHRANGE - 1
	}
	
}
