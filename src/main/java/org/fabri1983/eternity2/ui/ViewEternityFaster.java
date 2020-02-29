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
import org.fabri1983.eternity2.faster.ExplorationTask;
import org.fabri1983.eternity2.faster.SolverFaster;

public class ViewEternityFaster extends ViewEternityAbstract {

	private static final long serialVersionUID = 1L;
	
	private ExplorationTask action;
	
	private int cursor_mas_bajo = 0;
	private boolean cursor_mas_bajo_initialized;
	private static final int cursor_pos_for_mas_bajo = 96;
	
	public ViewEternityFaster(long p_refresh_milis, int pLado, int cell_size_pixels, int p_num_colours,
			ExplorationTask _action) {
		super(p_refresh_milis, pLado, cell_size_pixels, p_num_colours);
		action = _action;
	}

	@Override
	protected Canvas createCanvas(int rows, int cols) {
		return new CanvasFaster(rows, cols, Consts.PIEZA_CENTRAL_POS_TABLERO, action);
	}

	@Override
	protected long getAccum() {
		long accum = 0;
		for (int i = 0, c = SolverFaster.count_cycles.length; i < c; ++i) {
			accum += SolverFaster.count_cycles[i];
		}
		return accum;
	}

	@Override
	protected int getCursorTablero() {
		short cursor = action.cursor;
		
		if (!cursor_mas_bajo_initialized && cursor > cursor_pos_for_mas_bajo) {
			cursor_mas_bajo_initialized = true;
			cursor_mas_bajo = cursor;
		}
		if (cursor < cursor_mas_bajo)
			cursor_mas_bajo = cursor;
		
		return cursor;
	}

	@Override
	protected int getCursorMasBajo() {
		return cursor_mas_bajo;
	}

	@Override
	protected int getCursorMasLejano() {
		return SolverFaster.LIMITE_RESULTADO_PARCIAL;
	}

	@Override
	protected void shutdownSolver() {
		System.exit(0);
	}

}