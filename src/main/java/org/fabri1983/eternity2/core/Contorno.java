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

/**
 * Esta clase ayuda a saber si un determinado contorno (superior o inferior) está siendo usado o no.
 * La teoria a la que concluí es: si un contorno aparece como usado, luego no existe combinacion de 
 * piezas libres talque se pueda completar otro contorno igual. Es decir, contorno usado => no solución.
 * 
 * Nota: Usar un contorno por tablero.
 */
public final class Contorno
{
	// El mejor número de columnas es 2 (es más rápido)
	public final static byte MAX_COLS = 2; // usar valor entre 2 y 4
	private final static int MAX_COLORES = 23;
	private final static int LADO = 16;
	
	/**
	 * Arreglo para saber si un contorno ha sido usado o no. 
	 * 
	 * Para 3 niveles (MAX_COLS=2): just using a 3-dimensional array I ended up with MAX_COLORES^3 = 12167 indexes which is the 52% of 23254.
	 * It uses less memory and the access time is the same than the previous big array.
	 * Para 4 niveles (MAX_COLS=3): idem but 4-dimensional array: MAX_COLORES^4
	 * Para 5 niveles (MAX_COLS=4): idem but 5-dimensional array: MAX_COLORES^5
	 * 
	 * NOTA: Se usan 3 niveles de desglosamiento porque es el mejor número de columnas (un left y dos tops).
	 *     
	 * Some stats:
	 *   Para 3 niveles de desglosamiento: used slots = 3290 (got experimentally until position 211).
	 */
	public final boolean[][][] contornos_used = new boolean[MAX_COLORES][MAX_COLORES][MAX_COLORES];
	
	/**
	 * Inicializa el arreglo de contornos usados poniendo como usados aquellos contornos que ya están en tablero.
	 * Cada tablero tiene su instancia de Contorno.
	 */
	public static final void inicializarContornos (Contorno contorno, Pieza[] tablero, int maxPiezas)
	{
		// el limite inicial y el final me evitan los bordes sup e inf
		for (int k=LADO; k < (maxPiezas - LADO); ++k)
		{
			// given the way we populate the board is from top-left to bottom-right, 
			// then if we find an empty slot it means there is no more pieces in the board
			if (tablero[k] == null)
				return;
			
			//borde izquierdo
			if ((k % LADO) == 0) continue;
			//borde derecho
			if (((k+1) % LADO) == 0) continue;
			//(k + MAX_COLS) no debe llegar ni sobrepasar borde right
			int fila_actual = k / LADO;
			if (((k + MAX_COLS) / LADO) != fila_actual)
				continue;
			//me fijo si de las posiciones que tengo que obtener el contorno alguna ya es libre
			for (int a=1; a < MAX_COLS; ++a) {
				if (tablero[k+a] == null)
					return;
			}
			
			//Ahora k está en el interior del tablero
			
			//Saco el contorno superior e inferior y los seteo como usado.
			//El contorno inferior lo empiezo a contemplar a partir de fila_actual >= 2 porque necesito que 
			//existan piezas colocadas indicando que se ha formado el contorno inferior.
			contorno.contornos_used[tablero[k].left][tablero[k].top][tablero[k+1].top] = true;
		}
	}
	
	public static final void resetContornos(Contorno contorno) {
		for (int i=0; i < MAX_COLORES; ++i) {
			for (int j=0; j < MAX_COLORES; ++j) {
				for (int k=0; k < MAX_COLORES; ++k) {
					contorno.contornos_used[i][j][k] = false;
				}
			}
		}
	}

}
