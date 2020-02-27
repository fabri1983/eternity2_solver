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

public class Consts {

	/**
	 * This creates number 0x000000F0 (for LADO = 16) which will be used to mask cursor position to check if is in top or lower row. 
	 * The idea here is to create a number with log2(LADO) 0s as lower bits and then as much 1s to complete a size of byte (8 bits).
	 */
	public final static int maskForBorderTopAndBottom = 0x000000F0;
	
	public final static short LADO = 16;
	public final static short LADO_FOR_SHIFT_DIVISION = 4;
	public final static short MAX_PIEZAS = 256;
	public final static short NUM_P_CENTRAL = 138; // es la ubicación de la pieza central en piezas[]
	public final static short PIEZA_CENTRAL_POS_TABLERO = 135;
	public final static short ABOVE_PIEZA_CENTRAL_POS_TABLERO = PIEZA_CENTRAL_POS_TABLERO - LADO;
	public final static short BEFORE_PIEZA_CENTRAL_POS_TABLERO = PIEZA_CENTRAL_POS_TABLERO - 1;
	public final static short BELOW_PIEZA_CENTRAL_POS_TABLERO = PIEZA_CENTRAL_POS_TABLERO + LADO;
	public final static short CURSOR_INVALIDO = -1;
	
	public final static byte F_INTERIOR = 0;
	public final static byte F_BORDE_RIGHT = 1;
	public final static byte F_BORDE_LEFT = 2;
	public final static byte F_BORDE_TOP = 3;
	public final static byte F_BORDE_BOTTOM = 4;
	public final static byte F_ESQ_TOP_LEFT = 5;
	public final static byte F_ESQ_TOP_RIGHT = 6;
	public final static byte F_ESQ_BOTTOM_LEFT = 7;
	public final static byte F_ESQ_BOTTOM_RIGHT = 8;
	
	public final static String SECCIONES_SEPARATOR_EN_FILE = " ";
	public final static String FILE_EXT = ".txt";
	public final static String NAME_FILE_PIEZAS = "e2pieces" + FILE_EXT;
	
	public final static short FIRST_NUMERO_PIEZA_INTERIOR = 60;
	
	public final static byte MAX_ESTADOS_ROTACION = 4;
	public final static byte MAX_COLORES = 23;
	public final static byte PIEZA_CENTRAL_COLOR_TOP = 12;
	public final static byte PIEZA_CENTRAL_COLOR_BOTTOM = 3;
	public final static byte PIEZA_CENTRAL_COLOR_LEFT = 7;
	public static final byte GRIS = 22;
	public final static byte FIRST_CORNER_OR_BORDER_COLOR = 17; // See misc/NOTAS.txt
	public static final int NUM_OF_CORNER_AND_BORDER_COLORS = 5; // See misc/NOTAS.txt
	
}
