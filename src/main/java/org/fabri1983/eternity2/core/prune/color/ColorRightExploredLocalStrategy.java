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

public class ColorRightExploredLocalStrategy implements ColorRightExploredStrategy {

	/**
	 * Cada celda del arreglo representa una fila del tablero, y cada fila es un
	 * entero donde se usan los bits 16..0 (17 colores internos).<br/>
	 * Un bit valdrá 0 si ese color (right en borde left) no ha sido exlorado para
	 * la fila actual, sino valdrá 1.
	 */
	public final static int[] arr_color_rigth_explorado = new int[Consts.LADO];
	
	@Override
	public int get(int i) {
		return arr_color_rigth_explorado[i];
	}

	@Override
	public void set(int i, int val) {
		arr_color_rigth_explorado[i] = val;
	}

	@Override
	public int getAndMask(int i, int mask) {
		// TODO investigate if this can be made in 1 instruction. Maybe using XOR
		int prev = arr_color_rigth_explorado[i];
		arr_color_rigth_explorado[i] |= mask;
		return prev;
	}
	
}
