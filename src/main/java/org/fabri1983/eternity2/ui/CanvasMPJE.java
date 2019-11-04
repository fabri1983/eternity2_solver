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

import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.mpje.SolverFasterMPJE;

public class CanvasMPJE extends CanvasAbstract {
    
    public CanvasMPJE(int columns, int rows, int posCentral) {
    	super(columns, rows, posCentral);
    }

    @Override
	protected Pieza getPiezaFromTablero(int cursorTablero) {
    	short index = SolverFasterMPJE.tablero[cursorTablero];
    	if (index == -1) {
			return null;
		}
		return SolverFasterMPJE.piezas[index];
    }
    
    @Override
	protected Pieza getPiezaCentral() {
    	return SolverFasterMPJE.piezas[SolverFasterMPJE.INDICE_P_CENTRAL];
    }
    
}
