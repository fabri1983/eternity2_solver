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
	
	/**
	 * Inicializa varias estructuras y flags.
	 */
	public final void setupInicial() {
		
		solver.setupInicial();
		
		// solo dibujar el board de la primer action: SolverFaster.actions[0]
		ViewEternityFactory viewFactory = new ViewEternityFasterFactory(SolverFaster.LADO, SolverFaster.cellPixelsLado, 
				SolverFaster.MAX_COLORES, (long)SolverFaster.tableboardRefreshMillis, 1, SolverFaster.actions[0]);
		EternityII tableboardE2 = new EternityII(viewFactory); 
		tableboardE2.startPainting();
	}
	
	/**
	 * Invoca al pool de fork-join con varias instancias de RecursiveAction: ExploracionAction.
	 * Cada action ejecuta una rama de la exploración asociada a su id. De esta manera se logra decidir 
	 * la rama a explorar y tmb qué siguiente rama explorar una vez finalizada la primer rama.
	 */
	public final void atacar(long timeoutTaskInSecs) {
		solver.atacar(timeoutTaskInSecs);
	}
	
}