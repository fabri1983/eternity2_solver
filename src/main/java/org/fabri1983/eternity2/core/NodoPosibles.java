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

package org.fabri1983.eternity2.core;

/**
 * Contiene una lista de referencias de piezas.
 */
public final class NodoPosibles
{
	public short[] referencias;
	public byte[] rots;
	
	public static NodoPosibles newForKey(int key) {
		NodoPosibles np = new NodoPosibles();
		setSizeByKey(np, key);
		return np;
	}
	
	/**
	 * Agrega la pieza y la rotación a los arrays de NodoPosibles.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (final NodoPosibles np, final short p_index, byte rot)
	{
		// get next position with no data
		int nextIndex = getNextFreeIndex(np);
		
		np.referencias[nextIndex] = p_index;
		np.rots[nextIndex] = rot;
	}

	/**
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte top, final byte right, final byte bottom, final byte left)
	{
		// here we don't apply & since we assume that the color value is clean (has no 
		// leading 1s due to any previous shifting operation)
		return (top << 15) | (right << 10) | (bottom << 5) | left;
	}
	
	public static final byte getTop(final int key) {
		return (byte) ((key >> 15) & 31); // 5 bits only belongs to the color value => 31 = 11111
	}
	
	public static final byte getRight(final int key) {
		return (byte) ((key >> 10) & 31); // 5 bits only belongs to the color value => 31 = 11111
	}
	
	public static final byte getBottom(final int key) {
		return (byte) ((key >> 5) & 31); // 5 bits only belongs to the color value => 31 = 11111
	}
	
	public static final byte getLeft(final int key) {
		return (byte) ((key) & 31); // 5 bits only belongs to the color value => 31 = 11111
	}
	
	public static final short getUbicPieza(final NodoPosibles np, short index)
	{
		for (int i=0, c=np.referencias.length; i < c; ++i) {
			if (np.referencias[i] == index)
				return (short)i;
		}
		return 0;
	}

	private static void setSizeByKey(NodoPosibles np, int key) {
		int size = NodoPosiblesMapSizePerIndex.getSizeForKey(key);
		np.referencias = new short[size];
		np.rots = new byte[size];
		// initialize referencias with -1
		for (int i=0; i < size; ++i) {
			np.referencias[i] = -1;
		}
	}

	private static int getNextFreeIndex(final NodoPosibles np) {
		int nextIndex = 0;
		for (int c=np.referencias.length; nextIndex < c; ++nextIndex) {
//			if (np.referencias[nextIndex] == null)
			if (np.referencias[nextIndex] == -1)
				return nextIndex;
		}
		return nextIndex;
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
	 * @param b      bits The number of bits from lower to highest position to generate
	 * @return The 32-bit long bit string
	 */
	public static String intToBinaryString(int number, int b) {
		StringBuilder result = new StringBuilder(32);
		for (int i = 31; i >= 0; i--) {
			int mask = 1 << i;
			result.append((number & mask) != 0 ? "1" : "0");
		}
		return result.substring(result.length() - Math.min(b, 32), result.length());
	}
	
}
