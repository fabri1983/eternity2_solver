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

package org.fabri1983.eternity2.core.prune.color;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.Neighbors;

public class ColorRightExploredStrategy {

	/**
	 * Cada celda del arreglo representa una fila del tablero, y cada fila es un
	 * entero donde se usan los bits 16..0 (17 colores internos).<br/>
	 * Un bit valdrá 0 si ese color (right en borde left) no ha sido exlorado para
	 * la fila actual, sino valdrá 1.
	 */
	private final int[] colorRightExploredBitSet = new int[Consts.LADO];
	
	public int get(int i) {
		return colorRightExploredBitSet[i];
	}

	public void set(int i, int val) {
		colorRightExploredBitSet[i] = val;
	}

	/**
	 * Set 1 to the bit at color position.<br/>
	 * Returns true if previous value bit was 0. False if it was 1.
	 * 
	 * @param i
	 * @param value
	 * @param color
	 * @return
	 */
	private int setIfClean(int i, byte color) {
		int previousRowValue = colorRightExploredBitSet[i];
		colorRightExploredBitSet[i] |= 1 << color;
		return (previousRowValue >>> color) & 1;
	}
	
	private void cleanBit(int i, byte color) {
		colorRightExploredBitSet[i] &= ~(1 << color);
	}
	
	/**
	 * Clean bits (set to 0) of next row only if cursor is right before right border.
	 *  
	 * @param cursor
	 */
	public void cleanNextRow(short cursor) {
		// estoy justo antes de borde right?
		if (((cursor + 2) & (Consts.LADO - 1)) == 0) {
			int fila_actual = cursor >>> Consts.LADO_FOR_SHIFT_DIVISION;
			// limpio los bits de colores right usados de la siguiente fila
			set(fila_actual + 1, 0);
		}
	}
	
	/**
	 * Clean bit (set to 0) of current row's border left right color if cursor is at right border.
	 * 
	 * @param flagZona
	 * @param cursor
	 * @param tablero
	 */
	public void cleanRow(byte flagZona, short cursor, int[] tablero) {
		if ((flagZona & Consts.MASK_F_TABLERO) == Consts.F_BORDE_RIGHT) {
			int fila_actual = cursor >>> Consts.LADO_FOR_SHIFT_DIVISION;
			// Limpio el bit del color right de la pieza en borde left para la fila actual.
			// Significa q fué posible completar la fila con ese color asique no aplica poda cuando haga
			// backtrack hasta borde left, porque puedo llegar hasta aquí nuevamente para ese color.
			cleanBit(fila_actual, Neighbors.right(tablero[cursor - (Consts.LADO - 1)]));
		}
	}
	
	/**
	 * When on left border, returns true if the right color of current tile is already set as explorer.
	 * 
	 * @param flagZona
	 * @param cursor
	 * @param mergedInfo
	 * @return
	 */
	public boolean run(byte flagZona, short cursor, int mergedInfo, int[] tablero) {
		if ((flagZona & Consts.MASK_F_TABLERO) == Consts.F_BORDE_LEFT) {
			int fila_actual = cursor >>> Consts.LADO_FOR_SHIFT_DIVISION;
			// Si el color right ya está explorado (el bit vale 1) entonces continuo con otra pieza de borde. 
			// Anyways dejo marcado el color como explorado para futura poda.
			return setIfClean(fila_actual, Neighbors.right(mergedInfo)) == 1;
		}
		return false;
	}
	
}
