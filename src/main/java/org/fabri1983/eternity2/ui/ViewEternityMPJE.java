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

package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.mpje.SolverFasterMPJE;

public class ViewEternityMPJE extends ViewEternityAbstract {

	private static final long serialVersionUID = 1L;
	
	public ViewEternityMPJE(long p_refresh_milis, int pLado, int cell_size_pixels, int p_num_colours) {
		super(p_refresh_milis, pLado, cell_size_pixels, p_num_colours);
	}

	@Override
	protected Canvas createCanvas(int rows, int cols) {
		return new CanvasMPJE(rows, cols, Consts.POSICION_CENTRAL);
	}

	@Override
	protected long getAccum() {
		return SolverFasterMPJE.count_cycles;
	}

	@Override
	protected int getCursorTablero() {
		return SolverFasterMPJE.cursor;
	}

	@Override
	protected int getCursorMasBajo() {
		return SolverFasterMPJE.mas_bajo;
	}

	@Override
	protected int getCursorMasLejano() {
		return SolverFasterMPJE.mas_lejano_parcial_max;
	}

	@Override
	protected void shutdownSolver() {
		System.exit(0);
	}

}
