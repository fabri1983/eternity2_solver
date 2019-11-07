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
	public Pieza[] referencias;
	public byte[] rots;
	
	public static NodoPosibles newForKey(int key) {
		NodoPosibles np = new NodoPosibles();
		setSizesByKey(np, key);
		return np;
	}
	
	/**
	 * Agrega la pieza y la rotación a los arrays de NodoPosibles.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (final NodoPosibles np, final Pieza p_referencia, byte rot)
	{
		// get next position with no data
		int nextIndex = getNextFreeIndex(np);
		
		np.referencias[nextIndex] = p_referencia;
		np.rots[nextIndex] = rot;
	}

	/**
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte top, final byte right, final byte bottom, final byte left)
	{
		return (top << 15) | (right << 10) | (bottom << 5) | left;
	}
	
	public static final byte getTop(final int key) {
		return (byte) (key >> 15);
	}
	
	public static final byte getRight(final int key) {
		return (byte) (key >> 10);
	}
	
	public static final byte getBottom(final int key) {
		return (byte) (key >> 5);
	}
	
	public static final byte getLeft(final int key) {
		return (byte) (key);
	}
	
	public static final short getUbicPieza(final NodoPosibles np, short numero)
	{
		for (short i=0, c=(short)np.referencias.length; i < c; ++i) {
			if (np.referencias[i].numero == numero)
				return i;
		}
		return 0;
	}

	private static void setSizesByKey(NodoPosibles np, int key) {
		int size = NodoPosiblesMapSizePerIndex.getInstance().getSizeForKey(key);
		np.referencias = new Pieza[size];
		np.rots = new byte[size];
	}

	private static int getNextFreeIndex(final NodoPosibles np) {
		int nextIndex = 0;
		for (int c=np.referencias.length; nextIndex < c; ++nextIndex) {
			if (np.referencias[nextIndex] == null)
				break;
		}
		return nextIndex;
	}
	
}
