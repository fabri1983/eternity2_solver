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
 * 
 * @author Fabricio Lettieri
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
	 * Guarda la direccion de 32 bits address dividida en 4 partes de 8 bits en address_array en las diferentes 
	 * posiciones dadas por top, right, bottom, y left.
	 */
	public static final void addAddress (final byte top, final byte right, final byte bottom, final byte left, 
			int[] address_array, int address)
	{
		// NOTA: si address_array fuera de tipo bytes[] entonces tendría q usar << así:
		//  para top: (byte) (address >> 24)
		//  para right: (byte) (address >> 16)
		//  para bottom: (byte) (address >> 8)
		//  para left: (byte) (address >> 0)
		
		address_array[top] = address & 0xFF000000; // me quedo con los bits 31..24
		address_array[right] = address & 0xFF0000; // me quedo con los bits 23..16
		address_array[bottom] = address & 0xFF00;  // me quedo con los bits 15..8
		address_array[left] = address & 0xFF;      // me quedo con los bits  7..0
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
		int size = MapaArraySizePerIndex.getInstance().getSizeForKey(key);
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
