/**
 * Copyright (c) 2015 Fabricio Lettieri fabri1983@gmail.com
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

import org.fabri1983.eternity2.forkjoin_solver.ExploracionAction;


/**
 * Esta clase ayuda a saber si un determinado contorno (superior o inferior) está siendo usado o no.
 * La teoria a la que concluí es: si un contorno aparece como usado, luego no existe combinacion de 
 * piezas libres talque se pueda completar otro contorno igual. Es decir, contorno usado => no solución.
 * 
 * Nota: Usar un contorno por tablero.
 * 
 * @author Fabricio Lettieri
 */
public final class Contorno
{
	// El mejor número de columnas es 2 (es más rápido)
	public final static byte MAX_COLS = 2;
	private final static byte MAX_COLORES = 23;
		
	/**
	 * Arreglo para saber si un contorno ha sido usado o no. La capacidad del arreglo se calcula con 2 niveles
	 * de sumatoria desglosados. Se usan 3 porque es el mejor número de columnas (un left y dos tops).
	 * Con 5 bits represento los 23 colores.
	 */
	public final boolean contornos_used[] = new boolean[
	   (int) ((MAX_COLORES * Math.pow(2, 5 * 0)) +
			   (MAX_COLORES * Math.pow(2, 5 * 1)) +
			   (MAX_COLORES * Math.pow(2, 5 * 2)))];
	
	/**
	 * Inicializa el arreglo de contornos usados poniendo como usados aquellos contornos que ya están en tablero
	 * Cada tablero tiene su instancia de Contorno.
	 */
	public final void inicializarContornos (ExploracionAction action)
	{
		Pieza tablero[] = action.tablero;
		int fila_actual, index;
		
		// los limites iniciales me evitan los bordes sup e inf
		for (int k=16; k < (256-16); ++k)
		{
			if (tablero[k] == null)
				return;
			//borde izquierdo
			if ((k % 16) == 0) continue;
			//borde derecho
			if (((k+1) % 16) == 0) continue;
			//(k + MAX_COLS) no debe llegar ni sobrepasar borde right
			fila_actual = k / 16;
			if (((k + MAX_COLS) / 16) != fila_actual)
				continue;
			//me fijo si de las posiciones que tengo que obtener el contorno alguna ya es libre
			for (int a=1; a < MAX_COLS; ++a)
				if (tablero[k+a] == null)
					return;
			
			//Ahora k está en el interior del tablero
			
			//Saco el contorno superior e inferior y los seteo como usado.
			//El contorno inferior lo empiezo a contemplar a partir de la fila 2 porque necesito que 
			//existan piezas colocadas indicando que se ha formado el contorno inferior.
			switch (MAX_COLS)
			{
			case 2: index = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top);
					contornos_used[index] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						index = getIndex(tablero[k+1-16].right, tablero[k+1].top, tablero[k].top);
						contornos_used[index] = true;
					}*/
					break;
			case 3: index = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top, tablero[k+2].top);
					contornos_used[index] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						index = getIndex(tablero[k+2-16].right, tablero[k+2].top, tablero[k+1].top, tablero[k].top);
						contornos_used[index] = true;
					}*/
					break;
			case 4: index = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top, tablero[k+2].top, tablero[k+3].top);
					contornos_used[index] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						index = getIndex(tablero[k+3-16].right, tablero[k+3].top, tablero[k+2].top, tablero[k+1].top, tablero[k].top);
						contornos_used[index] = true;
					}*/
					break;
			default: break;
			}
		}
	}
	
	/**
	 * Devuelve el indice de contorno asociado a la clave de 3 colores.
	 */
	public static final int getIndex (final byte pleft, final byte top1, final byte top2)
	{
		return (pleft << 10) | (top1 << 5) | top2;
	}
	
	/**
	 * Devuelve el indice de cntorno asociado a la clave de 4 colores.
	 */
	public static final int getIndex (final byte pleft, final byte top1, final byte top2, final byte top3)
	{
		return (pleft << 15) | (top1 << 10) | (top2 << 5) | top3;
	}
	
	/**
	 * Devuelve el indice de contorno asociado a la clave de 5 colores.
	 */
	public static final int getIndex (final byte pleft, final byte top1, final byte top2, final byte top3, final byte top4)
	{
		return (pleft << 20) | (top1 << 15) | (top2 << 10) | (top3 << 5) | top4;
	}
}
