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
	public boolean util = false;
	
	private int currentIndex = 0;
	
	/**
	 * Agrega la pieza a la lista auxiliar.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (final NodoPosibles np, final Pieza p_referencia, byte rot)
	{
		np.referencias[np.currentIndex] = p_referencia;
		np.rots[np.currentIndex] = rot;
		np.util = true;
		++np.currentIndex;
	}
	
	/**
	 * Descarta memoria no utilizada.
	 */
	public static final void finalizar (final NodoPosibles np)
	{
		Pieza[] newRefs = new Pieza[np.currentIndex];
		System.arraycopy(np.referencias, 0, newRefs, 0, np.currentIndex);
		np.referencias = newRefs;
		
		byte[] newRots = new byte[np.currentIndex];
		System.arraycopy(np.rots, 0, newRots, 0, np.currentIndex);
		np.rots = newRots;
	}
	
	public static void disposeAll(final NodoPosibles np) {
		np.referencias = null;
		np.rots = null;
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
