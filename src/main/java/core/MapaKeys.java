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

package core;


/**
 * Esta clase me encapsula un mapa de claves el cual servirá como acceso a las filas guardadas de las distintas
 * combinaciones de colores. Utilizado para obtener claves del tipo top-right-bottom-left, o cualq tipo de clave que se
 * defina externamente.
 * 
 * @author Fabricio Lettieri
 */
public final class MapaKeys
{
	private static byte left;	
	private static byte tops[];
	private final static byte MAX_COLS;
	
	// Nota: las siguientes variables "part" de clase son para no tener que crearlas cada vez que se llama a getKey, pero en 
	// el caso de usar multi-threading no se pueden usar mas como estáticas.
	private static int part1, part2, part3, part4, part5;
	
	static {
		byte maxCols = Byte.parseByte(System.getProperty(FilaPiezas.PARAM_MAX_COLS, "0"));
		if (maxCols < 2)
			maxCols = 0;
		else if (maxCols > 4)
			maxCols = 4;
		MAX_COLS = maxCols;
	}
	
	private MapaKeys (){
	}
	
	public static final int getKey (final byte pLeft, final byte pTops[])
	{
		left = pLeft;
		tops = pTops;
		return getKey();
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 3 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2)
	{
		part1 = pleft;
		part2 = top1;
		return (part1 << 10) | (part2 << 5) | top2;
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 4 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2, final byte top3)
	{
		part1 = pleft;
		part2 = top1;
		part3 = top2;
		return (part1 << 15) | (part2 << 10) | (part3 << 5) | top3;
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 5 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2, final byte top3, final byte top4)
	{
		part1 = pleft;
		part2 = top1;
		part3 = top2;
		part4 = top3;
		return (part1 << 20) | (part2 << 15) | (part3 << 10) | (part4 << 5) | top4;
	}
	
	/**
	 * Devuelve la clave asociada a esa combinación de 6 colores.
	 */
	public static final int getKey (final byte pleft, final byte top1, final byte top2, final byte top3, final byte top4, final byte top5)
	{
		part1 = pleft;
		part2 = top1;
		part3 = top2;
		part4 = top3;
		part5 = top4;
		return (part1 << 25) | (part2 << 20) | (part3 << 15) | (part4 << 10) | (part5 << 5) + top5;
	}
	
	/**
	 * Devuelve la clave para la combinación de colores pasada como parámetro. Si retorna 0 siginifica que no existe
	 * clave para esa combinación de colores.
	 */
	private static final int getKey ()
	{
		switch (MAX_COLS)
		{
			case 2: return getKey(left,tops[0],tops[1]);
			case 3: return getKey(left,tops[0],tops[1],tops[2]);
			case 4: return getKey(left,tops[0],tops[1],tops[2],tops[3]);
			case 5: return getKey(left,tops[0],tops[1],tops[2],tops[3],tops[4]);
			default: return 0;
		}
	}
}
