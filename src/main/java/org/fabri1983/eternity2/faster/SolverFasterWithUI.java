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

package org.fabri1983.eternity2.faster;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;
import org.fabri1983.eternity2.ui.EternityII;
import org.fabri1983.eternity2.ui.ViewEternityFactory;
import org.fabri1983.eternity2.ui.ViewEternityFasterFactory;

public final class SolverFasterWithUI {
	
	private SolverFaster solver;

	private SolverFasterWithUI() {	
	}
	
	public final static SolverFasterWithUI from(SolverFaster solver) {
		SolverFasterWithUI newObj = new SolverFasterWithUI();
		newObj.solver = solver;
		return newObj;
	}
	
	public final void setupInicial(ReaderForFile readerForTilesFile) {
		
		solver.setupInicial(readerForTilesFile);
		
		// solo dibujar el board de la primer action: SolverFaster.actions[0]
		ViewEternityFactory viewFactory = new ViewEternityFasterFactory(Consts.LADO, SolverFaster.cellPixelsLado, 
				Consts.MAX_COLORES, (long)SolverFaster.tableboardRefreshMillis, 1, SolverFaster.actions[0]);
		EternityII tableboardE2 = new EternityII(viewFactory); 
		tableboardE2.startPainting();
	}
	
	public final void atacar() {
		solver.atacar();
	}
	
}