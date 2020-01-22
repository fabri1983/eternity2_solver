package org.fabri1983.eternity2.experimental.mph.recsplit;

/**
 * A sample hash implementation for int keys.
 */
public class IntHash {

	public static int universalHash(int o, int index) {
    	if (index == 0) {
            return o;
        } else if (index < 8) {
            long x = o;
            x += index;
            x = ((x >>> 32) ^ x) * 0x45d9f3b; // 73244475
            x = ((x >>> 32) ^ x) * 0x45d9f3b; // 73244475
            return (int)(x ^ (x >>> 32));
        }
        // get the lower or higher 16 bit depending on the index
        int shift = (index & 1) * 32;
        return o >>> shift;
    }
    
}
