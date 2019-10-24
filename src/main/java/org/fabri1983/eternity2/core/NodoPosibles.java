/**
 * Copyright (c) 2015 Fabricio Lettieri fabri1983@gmail.com
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

import org.fabri1983.eternity2.arrays.IntArrayList;
import org.fabri1983.eternity2.arrays.ObjectArrayList;

/**
 * Contiene una lista de referencias de piezas.
 * 
 * @author Fabricio Lettieri
 */
public final class NodoPosibles
{	
	private ObjectArrayList<Pieza> referencias_aux; //lista auxiliar de piezas
	private IntArrayList rots_aux; //lista auxiliar de piezas
	public Pieza[] referencias; //arreglo de referencias a piezas
	public byte[] rots;
	public boolean util = false;
	
	public NodoPosibles() {
		referencias_aux = new ObjectArrayList<Pieza>(64 + 1); // is the max size I got experimentally
		rots_aux = new IntArrayList(64 + 1); // is the max size I got experimentally
	}
	
	/**
	 * Agrega la pieza a la lista auxiliar.
	 * 
	 * @param rot 
	 */
	public static final void addReferencia (NodoPosibles np, final Pieza p_referencia, int rot)
	{
		np.util = true;
		np.rots_aux.add(rot);
		np.referencias_aux.add(p_referencia);
	}
	
	/**
	 * Convierto las listas a arreglos.
	 */
	public static final void finalizar (NodoPosibles np)
	{
		int size = np.referencias_aux.size();
		np.referencias = new Pieza[size];
		np.rots = new byte[size];
		
		for (int i=0; i < size; i++){
			np.referencias[i] = np.referencias_aux.get(i);
			np.rots[i] = (byte)np.rots_aux.get(i);
		}
		
		np.referencias_aux.clear();
		np.referencias_aux = null;
		np.rots_aux.clear();
		np.rots_aux = null;
	}
	
	public static final int getUbicPieza(final NodoPosibles np, final int numero)
	{
		for (int i=0, c=np.referencias.length; i < c; ++i) {
			if (np.referencias[i].numero == numero)
				return i;
		}
		return 0;
	}
}
