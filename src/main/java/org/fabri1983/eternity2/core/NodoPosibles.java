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

import org.fabri1983.eternity2.arrays.ObjectArrayList;

/**
 * Contiene una lista de referencias de piezas.
 * 
 * @author Fabricio Lettieri
 */
public final class NodoPosibles
{	
	private ObjectArrayList referencias_aux; //lista auxiliar de piezas
	public Pieza referencias[]; //arreglo de referencias a piezas
	public boolean util = false;
	
	
	public NodoPosibles() {
		referencias_aux = new ObjectArrayList();
	}
	
	/**
	 * Agrega la pieza referenciada por p_referencia.
	 */
	public static final void addReferencia (NodoPosibles np, final Pieza p_referencia)
	{
		np.util = true;
		np.referencias_aux.add(p_referencia);
	}
	
	/**
	 * Convierto las listas a arreglos.
	 */
	public static final void finalizar (NodoPosibles np)
	{
		int size = np.referencias_aux.size();
		np.referencias = new Pieza[size];
		
		for (int i=0; i < size; i++){
			if (np.referencias_aux.get(i) != null) {
				np.referencias[i] = (Pieza) np.referencias_aux.get(i);
			}
		}
		
		np.referencias_aux.clear();
		np.referencias_aux = null;
	}
	
	public static final int getUbicPieza(final NodoPosibles np, final int numero)
	{
		for (int i=np.referencias.length-1; i>=0; --i)
			if (np.referencias[i].numero == numero)
				return i;
		return 0;
	}
}
