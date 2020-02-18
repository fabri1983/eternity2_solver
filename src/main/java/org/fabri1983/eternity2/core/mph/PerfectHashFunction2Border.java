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
	
	// PHASHLEN 0x100 = 256
	static short tab[] = { 0, 1212, 0, 684, 0, 0, 1044, 11, 0, 891, 159, 629, 1140, 473, 605, 630, 1900, 1218, 1204,
			1140, 0, 1312, 1044, 348, 528, 0, 323, 1636, 1636, 0, 605, 1791, 0, 1639, 1654, 620, 517, 1550, 83, 197,
			323, 1869, 0, 1050, 764, 288, 1757, 988, 1836, 1295, 1432, 0, 197, 1279, 1689, 1896, 83, 702, 926, 0, 1618,
			11, 567, 1140, 508, 323, 0, 1453, 1295, 1107, 11, 1232, 0, 288, 580, 764, 0, 1834, 109, 1165, 629, 629,
			1689, 2014, 668, 0, 0, 83, 440, 0, 0, 0, 1064, 0, 1948, 336, 824, 1642, 0, 1613, 281, 1896, 83, 174, 1020,
			1839, 0, 1042, 281, 1618, 0, 1892, 517, 1218, 721, 0, 1019, 1042, 427, 83, 1949, 605, 764, 83, 1654, 1900,
			0, 0, 0, 630, 1312, 764, 0, 1791, 517, 427, 0, 1453, 0, 1822, 0, 1550, 1095, 288, 894, 0, 11, 760, 1639,
			1203, 986, 1105, 1295, 0, 1854, 0, 83, 0, 76, 1586, 674, 1805, 0, 1312, 1689, 895, 679, 304, 0, 1481, 0,
			1642, 605, 630, 0, 764, 884, 631, 762, 0, 197, 508, 1212, 0, 1109, 0, 94, 0, 864, 1689, 1654, 348, 1550,
			1116, 83, 1425, 0, 620, 1689, 2038, 0, 1042, 988, 83, 0, 477, 1550, 0, 399, 1213, 884, 1977, 546, 0, 1445,
			1213, 1312, 0, 1481, 239, 1951, 0, 83, 1481, 0, 517, 1044, 1639, 978, 1278, 1613, 0, 281, 1537, 517, 665,
			1549, 1892, 1204, 333, 358, 0, 1823, 721, 630, 1917, 0, 1312, 1654, 1074, 1689, 764, 767, 0, 1586, 0 };

	public static int phash(int val) {
		// NOTE: in Java remember to replace >> by >>> to avoid carrying out the bit sign when you know some operations exceed 2^31 - 1
		// I decided to use >>> to always avoid carrying out the sign.
		
		val += 0xb28698d; // PHASHSALT 0xb28698d (31 bits!)
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
		int b = (val >>> 1) & 0xff; // 0xff = 256 => & 0xff is the fastest way of doing % 0x100 (PHASHLEN 256)
		int a = (val + (val << 10)) >>> 21;
		int rsl = (a ^ tab[b]);
		return rsl; // from 1 up to PHASHRANGE - 1
	}
	
}
