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

public class Pieza {
	
	final static byte GRIS=0;
	final static String SECCIONES_SEPARATOR_EN_FILE= " ";
	final static byte MAX_ESTADOS_ROTACION = 4; // el número máximo de estados de rotación por pieza
	
	// NOTA: ubicación de datos en data
	// Los bits 0..4 color top.
	// Los bits 5..9 color right.
	// Los bits 10..14 color bottom.
	// Los bits 15..19 color left.
	// Los bits 20..21 son la rotación de la pieza.
	// Los bits 22..29 son el número de la pieza
	// El bit 30 es usada (0 false, 1 true)

	/*public static final int MASCARA_COLOR = 31; //mascara para quedarme con los primeros 5 bits
	public static final int MASCARA_ROTACION = 3; //mascara para quedarme con los primeros 2 bits
	public static final int MASCARA_NUMERO = 255; //mascara para quedarme con los primeros 8 bits
	public static final int MASCARA_USADA = 1; //mascara para quedarme con el primer bit
	public static final int OFFSET_TOP = 0;
	public static final int OFFSET_RIGHT = 5;
	public static final int OFFSET_BOTTOM = 10;
	public static final int OFFSET_LEFT = 15;
	public static final int OFFSET_ROTACION = 20;
	public static final int OFFSET_NUMERO = 22;
	public static final int OFFSET_USADA = 30;*/
	
	public byte top,right,bottom,left;
	public byte numero; // número que representa la pieza en el juego real
	public byte rotacion;
	public boolean usada;
	// public int pos; //indica la posición en tablero en la que se encuentra la pieza
	public byte count_grises;
	public boolean es_match_central; //me dice si tiene al menos uno de los colores de la pieza central (6, 11 o 18)
	public boolean es_esquina, es_borde, es_interior;
	
	// public int idUnico; // es un número para identificar unequivocamente la instancia de la pieza, pues se hacen copias 
//	private static int countIdUnico = 0;
	
	public static final boolean tieneColor (final Pieza p, int color)
	{
		if ((p.top==color) || (p.right==color) || (p.bottom==color) || (p.left==color))
			return true;
		return false;
	}
	
	public final static void rotar90 (final Pieza p)
	{
		final byte aux= p.left;
		p.left= p.bottom;
		p.bottom= p.right;
		p.right= p.top;
		p.top= aux;
		p.rotacion= (byte) ((p.rotacion + 1) & ~MAX_ESTADOS_ROTACION);
	}
	
	public final static void rotar180 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.right;
		p.right= aux;
		final byte aux2 = p.top;
		p.top= p.bottom;
		p.bottom= aux2;
		p.rotacion= (byte) ((p.rotacion + 2) & ~MAX_ESTADOS_ROTACION);
	}
	
	public final static void rotar270 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.top;
		p.top= p.right;
		p.right= p.bottom;
		p.bottom= aux;
		p.rotacion= (byte) ((p.rotacion + 3) & ~MAX_ESTADOS_ROTACION);
	}

	/**
	 * Esta funcion lleva el estado actual de rotacion de la pieza
	 * al estado indicado en el parametro rot.
	 */
	public static final void llevarARotacion (final Pieza p, int rot)
	{
		switch (p.rotacion){
			case 0: {
				switch (rot){
					case 0: break;
					case 1: rotar90(p); break;
					case 2: rotar180(p); break;
					case 3: rotar270(p); break;
				}
				return;
			}
			case 1: {
				switch (rot){
					case 0: rotar270(p); break;
					case 1: break;
					case 2: rotar90(p); break;
					case 3: rotar180(p); break;
				}
				return;
			}
			case 2: {
				switch (rot){
					case 0: rotar180(p); break;
					case 1: rotar270(p); break;
					case 2: break;
					case 3: rotar90(p); break;
				}
				return;
			}
			case 3: {
				switch (rot){
					case 0: rotar90(p); break;
					case 1: rotar180(p); break;
					case 2: rotar270(p); break;
					case 3: break;
				}
				return;
			}
		}
	}

}