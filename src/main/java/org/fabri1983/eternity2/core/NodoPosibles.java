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
	public Pieza[] referencias = new Pieza[64];
	public byte[] rots = new byte[64];
	
	public static NodoPosibles newForKey(int key) {
		NodoPosibles np = new NodoPosibles();
		setSizesByKey(np, key);
		return np;
	}
	
	private static void setSizesByKey(NodoPosibles np, int key) {
		int size = MapaArraySizePerIndex.getInstance().getSizeForKey(key);
		np.referencias = new Pieza[size];
		np.rots = new byte[size];
	}

	/**
	 * Agrega la pieza a la lista auxiliar.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (final NodoPosibles np, final Pieza p_referencia, byte rot)
	{
		int nextIndex = 0;
		for (int c=np.referencias.length; nextIndex < c; ++nextIndex) {
			if (np.referencias[nextIndex] == null)
				break;
		}
		
		np.referencias[nextIndex] = p_referencia;
		np.rots[nextIndex] = rot;
	}
	
	public static final byte getUbicPieza(final NodoPosibles np, byte numero)
	{
		for (byte i=0, c=(byte)np.referencias.length; i < c; ++i) {
			if (np.referencias[i].numero == numero)
				return i;
		}
		return 0;
	}
	
}
