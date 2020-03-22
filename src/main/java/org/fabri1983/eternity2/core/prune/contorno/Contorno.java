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

package org.fabri1983.eternity2.core.prune.contorno;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.Neighbors;

/**
 * Esta clase ayuda a saber si un determinado contorno (superior o inferior) está siendo usado o no.
 * La teoria a la que concluí es: si un contorno aparece como usado, luego no existe combinacion de 
 * piezas libres talque se pueda completar otro contorno igual. Es decir, si el contorno ya está usado 
 * al momento de querer colocar una pieza => no es solución.
 * 
 * Nota: Usar un contorno por instancia de tablero.
 */
public class Contorno
{
	// El mejor número de columnas es 2 (es más rápido)
	public final static int MAX_COLUMNS = 2; // Search over the code before change this value
	
	/**
	 * Given the fact that we handle only inner colors we can discard those 5 colors belonging 
	 * to corners and borders. So ending up using 17 colors.
	 * For 3 colors (MAX_COLUMNS = 2): 17^3 = 4913.
	 */
	private final boolean[][][] used = new boolean
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
	
	public void toggleContorno(boolean value, short cursor, byte flagZona, int[] tablero, int mergedActual)
	{
		// CUDA: Set mergedXxx vars to Neighbors.asMergedInfo(Consts.FIRST_CORNER_OR_BORDER_COLOR, 0) when condition is not satisfied, using bitwise and similar.
		// CUDA: Also when condition is not valid also set used[][][]=false, instead of value, to be correctly used later in (esContornoSuperiorUsado) 
		// CUDA: Consider that it requires the matrix used[][][] to have +1 in each dimension.
		
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if ((flagZona & Consts.MASK_F_PROC_CONTORNO) == Consts.F_PROC_CONTORNO) {
			int mergedPrevious = tablero[cursor - 1];
			used[Neighbors.left(mergedPrevious)]
				[Neighbors.top(mergedPrevious)]
				[Neighbors.top(mergedActual)] = value;
		}
	}

	public boolean esContornoSuperiorUsado(short cursor, byte flagZona, int[] tablero)
	{
		// CUDA: Set mergedXxx vars to Neighbors.asMergedInfo(Consts.FIRST_CORNER_OR_BORDER_COLOR, 0) when condition is not satisfied, using bitwise and similar.
		
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if ((flagZona & Consts.MASK_F_READ_CONTORNO) == Consts.F_READ_CONTORNO) {
			int mergedInfoPrevious = tablero[cursor - 1];
			int mergedInfoPreviousLado = tablero[cursor - Consts.LADO];
			int mergedInfoPreviousLadoAnd1 = tablero[cursor - Consts.LADO + 1];
			return used	[Neighbors.right(mergedInfoPrevious)]
						[Neighbors.bottom(mergedInfoPreviousLado)]
						[Neighbors.bottom(mergedInfoPreviousLadoAnd1)];
		}
		return false;
	}
	
	/**
	 * Inicializa el arreglo de contornos usados poniendo como usados aquellos contornos que ya están en tablero.
	 */
	public void inicializarContornos (int[] tablero, short maxPiezas, short lado)
	{
		// el limite inicial y el final me evitan los bordes sup e inf
		for (short k=lado; k < (maxPiezas - lado); ++k)
		{
			// given the way we populate the board is from top-left to bottom-right, 
			// then if we find an empty slot it means there is no more pieces in the board
			if (tablero[k] == Consts.TABLERO_INFO_EMPTY_VALUE)
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
				if (tablero[k+a] == Consts.TABLERO_INFO_EMPTY_VALUE)
					return;
			}
			
			// Ahora k está en el interior del tablero
			
			// Saco el contorno superior e inferior y los seteo como usado.
			// El contorno inferior lo empiezo a contemplar a partir de fila_actual >= 2 porque necesito que 
			// existan piezas colocadas indicando que se ha formado el contorno inferior.
			used[Neighbors.left(tablero[k])][Neighbors.top(tablero[k])][Neighbors.top(tablero[k+1])] = true;
		}
	}
	
	public void resetContornos() {
		for (int i=0; i < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++i) {
			for (int j=0; j < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++j) {
				for (int k=0; k < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++k) {
					used[i][j][k] = false;
				}
			}
		}
	}

}
