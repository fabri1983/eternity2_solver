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

import org.fabri1983.eternity2.core.neighbors.Neighbors;

/**
 * Esta clase ayuda a saber si un determinado contorno (superior o inferior) está siendo usado o no.
 * La teoria a la que concluí es: si un contorno aparece como usado, luego no existe combinacion de 
 * piezas libres talque se pueda completar otro contorno igual. Es decir, si el contorno ya está usado 
 * al momento de querer colocar una pieza => no es solución.
 * 
 * Nota: Usar un contorno por instancia de tablero.
 */
public final class Contorno
{
	// El mejor número de columnas es 2 (es más rápido)
	public final static int MAX_COLUMNS = 2; // Search over the code before change this value
	
	/**
	 * Arreglo para saber si un contorno ha sido usado o no. 
	 * 
	 * INITIAL VERSION: 
	 * (Assuming 23 = max colores)
	 * Para 3 colors (MAX_COLS=2): just using a 3-dimensional array I ended up with 23^3 =   12167 indexes.
	 * Para 4 colors (MAX_COLS=3): idem but 4-dimensional array:                    23^4 =  279841 indexes.
	 * Para 5 colors (MAX_COLS=4): idem but 5-dimensional array:                    23^5 = 6436343 indexes.
	 * 
	 * NOTA: Se usan 3 niveles de desglosamiento porque es el mejor número de columnas (un left y dos tops).
	 * 
	 * IMPROVEMENT FINAL:
	 * Given the fact that the Contorno data structure holds only inner colors we can discard those 5 colors belonging 
	 * to corners and borders. So ending up using 17 colors.
	 * Para 3 colors (MAX_COLS=2): 17^3 = 4913.
	 */
	public final boolean[][][] used = new boolean
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
	
	/**
	 * Inicializa el arreglo de contornos usados poniendo como usados aquellos contornos que ya están en tablero.
	 */
	public static final void inicializarContornos (Contorno contorno, int[] tablero, short maxPiezas, short lado)
	{
		// el limite inicial y el final me evitan los bordes sup e inf
		for (short k=lado; k < (maxPiezas - lado); ++k)
		{
			// given the way we populate the board is from top-left to bottom-right, 
			// then if we find an empty slot it means there is no more pieces in the board
			if (tablero[k] == -1)
				return;
			
			// borde left
			if ((k % lado) == 0) continue;
			
			// borde right
			if (((k+1) % lado) == 0) continue;
			
			// (k + MAX_COLS) no debe llegar ni sobrepasar borde right
			int fila_actual = k / lado;
			if (((k + MAX_COLUMNS) / lado) != fila_actual)
				continue;
			
			// me fijo si de las posiciones que tengo que obtener el contorno alguna está vacía
			for (int a=1; a < MAX_COLUMNS; ++a) {
				if (tablero[k+a] == -1)
					return;
			}
			
			// Ahora k está en el interior del tablero
			
			// Saco el contorno superior e inferior y los seteo como usado.
			// El contorno inferior lo empiezo a contemplar a partir de fila_actual >= 2 porque necesito que 
			// existan piezas colocadas indicando que se ha formado el contorno inferior.
			contorno.used[Neighbors.left(tablero[k])][Neighbors.top(tablero[k])][Neighbors.top(tablero[k+1])] = true;
		}
	}
	
	public static final void resetContornos(Contorno contorno) {
		for (int i=0; i < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++i) {
			for (int j=0; j < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++j) {
				for (int k=0; k < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++k) {
					contorno.used[i][j][k] = false;
				}
			}
		}
	}

}
