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


/**
 * Esta clase me encapsula un mapa de claves el cual servirá como acceso a las filas guardadas de las distintas
 * combinaciones de colores. Utilizado para obtener claves del tipo top-right-bottom-left, o cualq tipo de clave que se
 * defina externamente.
 * 
 * @author Fabricio Lettieri
 */
public final class MapaKeys {
	
	private final static byte MAX_COLS = 2; // usar valor entre 2 y 4
	
	private MapaKeys () {
	}
	
	/**
	 * Devuelve la clave para la combinación de colores pasada como parámetro. Si retorna 0 siginifica que no existe
	 * clave para esa combinación de colores.
	 */
	public static final int getKey (final byte pleft, final byte tops[])
	{
		switch (MAX_COLS)
		{
			case 2: return getKey(pleft, tops[0], tops[1]);
			case 3: return getKey(pleft, tops[0], tops[1], tops[2]);
			case 4: return getKey(pleft, tops[0], tops[1], tops[2], tops[3]);
			default: return 0;
		}
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 3 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2)
	{
		return (pleft << 10) | (top1 << 5) | top2;
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2, final byte top3)
	{
		return (pleft << 15) | (top1 << 10) | (top2 << 5) | top3;
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 5 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2, final byte top3, final byte top4)
	{
		return (pleft << 20) | (top1 << 15) | (top2 << 10) | (top3 << 5) | top4;
	}
	
}
