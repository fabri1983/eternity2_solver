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
	
	// NOTA: ubicación de datos en data
	// Los bits 0..4 color top.
	// Los bits 5..9 color right.
	// Los bits 10..14 color bottom.
	// Los bits 15..19 color left.
	// Los bits 20..21 son la rotación de la pieza.
	// Los bits 22..29 son el número de la pieza
	// El bit 30 es usada (0 false, 1 true)

//	public static final int MASCARA_COLOR = 31; //mascara para quedarme con los primeros 5 bits
//	public static final int MASCARA_ROTACION = 3; //mascara para quedarme con los primeros 2 bits
//	public static final int MASCARA_NUMERO = 255; //mascara para quedarme con los primeros 8 bits
//	public static final int MASCARA_USADA = 1; //mascara para quedarme con el primer bit
//	public static final int OFFSET_TOP = 0;
//	public static final int OFFSET_RIGHT = 5;
//	public static final int OFFSET_BOTTOM = 10;
//	public static final int OFFSET_LEFT = 15;
//	public static final int OFFSET_ROTACION = 20;
//	public static final int OFFSET_NUMERO = 22;
//	public static final int OFFSET_USADA = 30;
	
	public byte top,right,bottom,left;
	public short numero; // número que representa la pieza en el juego real (pero aqui es 0-based)
	public byte rotacion;
	public boolean usada;
	// public int pos; // indica la posición en el tablero real en la que iría la pieza
	public byte feature;// es_interior = 0 grises, es_borde = 1 gris, es_esquina = 2 grises
	
	// public int idUnico; // es un número para identificar unequivocamente la instancia de la pieza, pues se hacen copias 
//	private static int countIdUnico = 0;
	
	/**
	 * Clockwise rotation.
	 * 
	 * @param p
	 */
	public final static void rotar90 (final Pieza p)
	{
		final byte aux= p.left;
		p.left= p.bottom;
		p.bottom= p.right;
		p.right= p.top;
		p.top= aux;
		p.rotacion= (byte) ((p.rotacion + 1) & 3); // 4 max rotations using modulo reduction: x % 4 => x & 3
	}
	
	/**
	 * Mirror rotation.
	 * 
	 * @param p
	 */
	public final static void rotar180 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.right;
		p.right= aux;
		final byte aux2 = p.top;
		p.top= p.bottom;
		p.bottom= aux2;
		p.rotacion= (byte) ((p.rotacion + 2) & 3); // 4 max rotations using modulo reduction: x % 4 => x & 3
	}
	
	/**
	 * Counter Clockwise rotation.
	 * 
	 * @param p
	 */
	public final static void rotar270 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.top;
		p.top= p.right;
		p.right= p.bottom;
		p.bottom= aux;
		p.rotacion= (byte) ((p.rotacion + 3) & 3); // 4 max rotations using modulo reduction: x % 4 => x & 3
	}

	/**
	 * Esta funcion lleva el estado actual de rotacion de la pieza
	 * al estado indicado en el parametro rot.
	 */
	public static final void llevarArotacion (final Pieza p, byte rot)
	{
		/**
		 * This method has no if statements, which helps to avoid branching decisions on CPU.
		 * 
		 * We are going to loop clockwise in order to rotate the colors, so we need to calculate the number of loops.
		 *   rotLoops = (4 - p.rotacion + rot) % 4
		 *   rotLoops = (-p.rotacion + rot) % 4
		 *   rotLoops = (rot - p.rotacion) % 4
		 * Then using the bitwise modulo trick where M is a power of 2:
		 *   rotLoops = (rot - p.rotacion) & 3
		 * We may use the distributive of % operand:
		 *   (A + B) % M = ((A % M) + (B % M)) % M
		 *   rotLoops = ((rot & 3) - (p.rotacion & 3)) & 3
		 *   But in our case this involves more operations, so we discard it
		 */
		int rotLoops = (rot - p.rotacion) & 3;
		p.rotacion = rot;
		while (rotLoops > 0) {
			
			// using a temporal variable
			byte aux= p.left;
			p.left= p.bottom;
			p.bottom= p.right;
			p.right= p.top;
			p.top= aux;
			
			// using bitwise XOR
			/**
			 * original:
			 *         top
			 *   left       right
			 *        bottom
			 * 
			 * goal: (rotate 90 clockwise)
			 *         left
			 *   bottom     top
			 *         right
			 * 
			 * So we have to do 3 XOR swaps:
			 *   xorSwap(right, bottom):
			 *            top
			 *      left       bottom
			 *           right
			 *   xorSwap(bottom, left):
			 *            top
			 *     bottom     left
			 *           right
			 *   xorSwap(left,top):
			 *           left
			 *     bottom     top
			 *           right
			 */
			// Option 1 (seems faster)
//			p.right = (byte) (p.right ^ p.bottom);
//			p.bottom = (byte) (p.right ^ p.bottom);
//			p.right = (byte) (p.right ^ p.bottom);
//			p.right = (byte) (p.right ^ p.left);
//			p.left = (byte) (p.right ^ p.left);
//			p.right = (byte) (p.right ^ p.left);
//			p.right = (byte) (p.right ^ p.top);
//			p.top = (byte) (p.right ^ p.top);
//			p.right = (byte) (p.right ^ p.top);
			
			// Option 2
//		    // Store XOR of all variables in top
//			p.top = (byte) (p.top ^ p.right ^ p.bottom ^ p.left); 
//		    // After this, right has value of top 
//			p.right = (byte) (p.top ^ p.right ^ p.bottom ^ p.left); 
//		    // After this, bottom has value of right 
//			p.bottom = (byte) (p.top ^ p.right ^ p.bottom ^ p.left); 
//		    // After this, left has value of bottom 
//			p.left = (byte) (p.top ^ p.right ^ p.bottom ^ p.left);
//		    // After this, top has value of left 
//			p.top = (byte) (p.top ^ p.right ^ p.bottom ^ p.left);
			
			--rotLoops;
		}
	}

}