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
 * Esta clase me encapsula un mapa de claves el cual servirá como acceso a las filas guardadas de las distintas
 * combinaciones de colores. Utilizado para obtener claves del tipo top-right-bottom-left, o cualq tipo de clave que se
 * defina externamente.
 */
public final class NodoPosiblesKeys {
	
	private NodoPosiblesKeys () {
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte top, final byte right, final byte bottom, final byte left)
	{
		return (top << 15) | (right << 10) | (bottom << 5) | left;
	}
	
	/**
	 * Devuelve el address de 32 bits guardado en address_array[] para esa combinación de colores.
	 * Si el address es 0 significa que no se ha guardado address en esa combinación de colores.
	 */
	public static final int getAddress (final byte top, final byte right, final byte bottom, final byte left, 
			int[] address_array)
	{
		// NOTA: si address_array fuera de tipo bytes[] entonces tendría q usar << así:
		//  address_array[top] << 24
		//  address_array[right] << 16
		//  address_array[bottom] << 8
		//  address_array[left] << 0
		
		int address = address_array[top] // address bits 31..24
				| address_array[right]   // address bits 23..16
				| address_array[bottom]  // address bits 15..8
				| address_array[left];   // address bits  7..0
		return address;
	}
	
}
