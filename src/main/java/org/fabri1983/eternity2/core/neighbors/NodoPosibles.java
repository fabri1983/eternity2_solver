/**
 * Copyright (c) 2019 Fabricio Lettieri fabri1983@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fabri1983.eternity2.core.neighbors;

/**
 * It contains an array which have a mixture of number of tile and the colors top, right, bottom, left.
 * 
 * Bits from position  0..7  (8 bits) hold the number of tile.
 * Bits from position  8..12 (5 bits) hold left color.
 * Bits from position 13..17 (5 bits) hold bottom color.
 * Bits from position 18..22 (5 bits) hold right color.
 * Bits from position 23..27 (5 bits) hold top color.
 * 
 * One picture is better than a thousand of words:
 *  int (32 bits): bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
 *                                         ^^^^^^^^  <- number of tile (which is the index in piezas[])
 *                                    ^^^^^          <- left color
 *                               ^^^^^               <- bottom color
 *                          ^^^^^                    <- right color
 *                     ^^^^^                         <- top color
 */
public final class NodoPosibles
{
	private static final int MASK_PIEZA_INDEX = 0b11111111; // 8 bits
	private static final int MASK_COLOR = 0b11111; // 5 bits
	private static final int SHIFT_COLOR_LEFT = 0 + 8; // +8 because first bits are pieza index
	private static final int SHIFT_COLOR_BOTTOM = 5 + 8; // +8 because first bits are pieza index
	private static final int SHIFT_COLOR_RIGHT = 10 + 8; // +8 because first bits are pieza index
	private static final int SHIFT_COLOR_TOP = 15 + 8;
	
	public int[] mergedInfo;
	
	private NodoPosibles(int size) {
		mergedInfo = new int[size];
	}
	
	public static final NodoPosibles newForKey_interior(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_interior(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_interior_above_central(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_interior_above_central(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_interior_left_central(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_interior_left_central(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_border_right(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_border_right(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_border_left(byte a) {
		int key = a;
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_border_left(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_border_top(byte a) {
		int key = a;
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_border_top(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_border_bottom(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_border_bottom(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_corner_top_left() {
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_corner_top_left();
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_corner_top_right(byte a) {
		int key = a;
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_corner_top_right(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_corner_bottom_left(byte a) {
		int key = a;
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_corner_bottom_left(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final NodoPosibles newForKey_corner_bottom_right(byte a, byte b) {
		int key = colorsAsKey(a, b);
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_corner_bottom_right(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}

	public static final int numero(int mergedInfo) {
		return mergedInfo & NodoPosibles.MASK_PIEZA_INDEX;
	}
	
	public static final byte top(int mergedInfo) {
		return (byte) ((mergedInfo >>> NodoPosibles.SHIFT_COLOR_TOP) & NodoPosibles.MASK_COLOR);
	}
	
	public static final byte right(int mergedInfo) {
		return (byte) ((mergedInfo >>> NodoPosibles.SHIFT_COLOR_RIGHT) & NodoPosibles.MASK_COLOR);
	}
	
	public static final byte bottom(int mergedInfo) {
		return (byte) ((mergedInfo >>> NodoPosibles.SHIFT_COLOR_BOTTOM) & NodoPosibles.MASK_COLOR);
	}
	
	public static final byte left(int mergedInfo) {
		return (byte) ((mergedInfo >>> NodoPosibles.SHIFT_COLOR_LEFT) & NodoPosibles.MASK_COLOR);
	}
	
	/**
	 * Agrega el numero de pieza pieza y la rotación mergeandolos con bitwise en una variable short.
	 */
	public static final void addNeighbor (NodoPosibles np, byte top, byte right, byte bottom, byte left, short piezaIndex) {
		// get next position with no data
		int nextIndex = getNextFreeIndex(np);
		np.mergedInfo[nextIndex] = asMergedInfo(top, right, bottom, left, piezaIndex);
	}

	public static int asMergedInfo(byte top, byte right, byte bottom, byte left, short piezaIndex) {
		return (top << SHIFT_COLOR_TOP) | ( right << SHIFT_COLOR_RIGHT) | (bottom << SHIFT_COLOR_BOTTOM) | (left << SHIFT_COLOR_LEFT) | piezaIndex;
	}

	private static int getNextFreeIndex(NodoPosibles np) {
		int nextIndex = 0;
		for (int c=np.mergedInfo.length; nextIndex < c; ++nextIndex) {
			if (np.mergedInfo[nextIndex] == -1)
				return nextIndex;
		}
		return nextIndex;
	}

	public static void resetReferencias(NodoPosibles np) {
		for (int i=0, c=np.mergedInfo.length; i < c; ++i) {
			np.mergedInfo[i] = -1;
		}
	}

	/**
	 * Devuelve la clave asociada a esa combinación de 2 colores.
	 */
	public static final int colorsAsKey (byte a, byte b)
	{
		return (a << 5) | b;
	}
	
	public static final short getIndexMergedInfo(NodoPosibles np, int mergedInfo)
	{
		for (int i=0, c=np.mergedInfo.length; i < c; ++i) {
			if (np.mergedInfo[i] == mergedInfo)
				return (short)i;
		}
		return 0;
	}

	/**
	 * Converts an integer to a 32-bit binary string with the desired number of bits.
	 * 
	 * <pre>
	 * Eg:
	 * number = 5463, b = 32
	 * output = 00000000000000000001010101010111
	 * 
	 * Eg:
	 * number = 5463, b = 13
	 * output = 1010101010111
	 * </pre>
	 * 
	 * @param number The number to convert
	 * @param b      The number of bits from lower to highest position to generate
	 * @return The 32-bit long bit string
	 */
	public static String intToBinaryString(int number, int b) {
		return String.format("%" + b + "s", Integer.toBinaryString(number)).replaceAll(" ", "0");
	}
	
}
