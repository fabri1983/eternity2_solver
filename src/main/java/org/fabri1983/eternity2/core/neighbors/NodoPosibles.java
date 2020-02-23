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
 * It contains an array which have a mixture of number of tile and rotation value.
 * Bits from position 0..7 (8 bits) hold the number of tile.
 * Bits from position 8..9 (2 bits) hold the rotation of the tile.
 * One picture is better than a thousand of words:
 *  short (16 bits): 0000000000000000
 *                           ^^^^^^^^  <- number of tile (which is the index in piezas[])
 *                         ^^          <- rotation value
 */
public final class NodoPosibles
{
	public static final short MASK_PIEZA_INDEX = 0b11111111;
	public static final short MASK_PIEZA_ROT_SHIFT = 8;
	
	// Next constants are used for subtraction of generated key to lower the max key value, 
	// so we can use smaller Perfect Hash Function tabs and smaller bitsets.
	public static final int KEY_SUBTRACT_INTERIOR = 108; // minimum key value obtained from interior pieces
	public static final int KEY_SUBTRACT_BORDER = 18129; // minimum key value obtained from border pieces
	public static final int KEY_SUBTRACT_CORNER = 576214; // minimum key value obtained from corner pieces
	
	public short[] mergedInfo;
	
	private NodoPosibles(int size) {
		mergedInfo = new short[size];
	}
	
	public static NodoPosibles newForKey(int key) {
		int size = NodoPosiblesMapSizePerKey.getSizeForKey(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}
	
	public static NodoPosibles newForKey_interior(int key) {
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_interior(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}
	
	public static NodoPosibles newForKey_border(int key) {
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_border(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}
	
	public static NodoPosibles newForKey_corner(int key) {
		int size = NodoPosiblesMapSizePerKey.getSizeForKey_corner(key);
		NodoPosibles np = new NodoPosibles(size);
		resetReferencias(np);
		return np;
	}
	
	/**
	 * Agrega el numero de pieza pieza y la rotación mergeandolos con bitwise en una variable short.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (final NodoPosibles np, final short piezaIndex, byte rot) {
		// get next position with no data
		int nextIndex = getNextFreeIndex(np);
		np.mergedInfo[nextIndex] = (short) (piezaIndex | (rot << MASK_PIEZA_ROT_SHIFT));
	}

	private static int getNextFreeIndex(final NodoPosibles np) {
		int nextIndex = 0;
		for (int c=np.mergedInfo.length; nextIndex < c; ++nextIndex) {
//			if (np.referencias[nextIndex] == null)
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
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte a, final byte b, final byte c, final byte d)
	{
		// here we don't apply & since we assume that the color value is clean (has no 
		// leading 1s due to any previous shifting operation)
		return (a << 15) | (b << 10) | (c << 5) | d;
	}
	
//	public static final byte getA(final int key) {
//		return (byte) ((key >>> 15) & 31); // 5 bits only belongs to the color value => 31 = 11111
//	}
//	
//	public static final byte getB(final int key) {
//		return (byte) ((key >>> 10) & 31); // 5 bits only belongs to the color value => 31 = 11111
//	}
//	
//	public static final byte getC(final int key) {
//		return (byte) ((key >>> 5) & 31); // 5 bits only belongs to the color value => 31 = 11111
//	}
//	
//	public static final byte getD(final int key) {
//		return (byte) ((key) & 31); // 5 bits only belongs to the color value => 31 = 11111
//	}
	
	public static final short getUbicPieza(final NodoPosibles np, short numero)
	{
		for (int i=0, c=np.mergedInfo.length; i < c; ++i) {
			if ((np.mergedInfo[i] & MASK_PIEZA_INDEX) == numero)
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