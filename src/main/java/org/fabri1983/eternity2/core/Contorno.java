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
		
	/**
	 * Arreglo para saber si un contorno ha sido usado o no. 
	 * NOTA: Se usan 3 niveles de desglosamiento porque es el mejor número de columnas (un left y dos tops).
	 * Por lo tanto voy a usar un arreglo de size:
	 *  (int) ((MAX_COLORES * Math.pow(2, 5 * 0)) +
			   (MAX_COLORES * Math.pow(2, 5 * 1)) +
			   (MAX_COLORES * Math.pow(2, 5 * 2)))
	 *  donde MAX_COLORES = 23, y con 5 bits represento los 23 colores.
	 * Si fuera a usar 4 niveles de desglosamiento tengo q agregar una suma mas: (MAX_COLORES * Math.pow(2, 5 * 3))
	 * Si fuera a usar 5 niveles de desglosamiento tengo q agregar una suma mas: (MAX_COLORES * Math.pow(2, 5 * 4))
	 * Entonces:
	 *   con 3 niveles (MAX_COLS=2): size es 24311
	 *   con 4 niveles (MAX_COLS=3): size es 777975
	 *   con 5 niveles (MAX_COLS=4): size es 24895223
	 *   
	 * Improvement en array size: solo es valido cuando manejo colores con valores 0..22: 
	 *   22 en binario es 10110, y como voy usar 5 bits shifteados a derecha 0, 5, y 10 veces (y 15 si uso 4 niveles 
	 *   de desglosamiento, y 20 si suso 5 niveles), entonces el num maximo de combinación sería:
	 *     para 3 niveles (MAX_COLS=2): 10110 10110 10110 = 23254 y el size calculado era 24334.
	 *     	 => me ahorro 1080 indices
	 *     para 4 niveles (MAX_COLS=3): 10110 10110 10110 10110 = 744150 y el size calculado era 777975.
	 *     	 => me ahorro 33825 indices
	 *     para 5 niveles (MAX_COLS=4): 10110 10110 10110 10110 10110 = 23812822 y el size calculado era 24895223.
	 *     	=> me ahorro 1082401 indices
	 *     
	 * Some stats:
	 *   Para 3 niveles de desglosamiento: used slots = 3290 (got experimentally until position 211).
	 */
	public final boolean contornos_used[] = new boolean[23254 + 1]; // le sumo uno pues 23254 es el último indice válido en 0-based
	
	/**
	 * Inicializa el arreglo de contornos usados poniendo como usados aquellos contornos que ya están en tablero.
	 * Cada tablero tiene su instancia de Contorno.
	 */
	public static final void inicializarContornos (Contorno contorno, Pieza[] tablero, int maxPiezas)
	{
		// el limite inicial y el final me evitan los bordes sup e inf
		for (int k=16; k < (maxPiezas - 16); ++k)
		{
			// given the way we populate the board is from top-left to bottom-right, 
			// then if we find an empty slot it means there is no more pieces in the board
			if (tablero[k] == null)
				return;
			
			//borde izquierdo
			if ((k % 16) == 0) continue;
			//borde derecho
			if (((k+1) % 16) == 0) continue;
			//(k + MAX_COLS) no debe llegar ni sobrepasar borde right
			int fila_actual = k / 16;
			if (((k + MAX_COLS) / 16) != fila_actual)
				continue;
			//me fijo si de las posiciones que tengo que obtener el contorno alguna ya es libre
			for (int a=1; a < MAX_COLS; ++a) {
				if (tablero[k+a] == null)
					return;
			}
			
			//Ahora k está en el interior del tablero
			
			//Saco el contorno superior e inferior y los seteo como usado.
			//El contorno inferior lo empiezo a contemplar a partir de la fila 2 porque necesito que 
			//existan piezas colocadas indicando que se ha formado el contorno inferior.
			switch (MAX_COLS)
			{
			case 2: {
					int indexSup = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top);
					contorno.contornos_used[indexSup] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						int indexInf = getIndex(tablero[k+1-16].right, tablero[k+1].top, tablero[k].top);
						contorno.contornos_used[indexInf] = true;
					}*/
				}
				break;
			case 3: {
					int indexSup = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top, tablero[k+2].top);
					contorno.contornos_used[indexSup] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						int indexInf = getIndex(tablero[k+2-LADO].right, tablero[k+2].top, tablero[k+1].top, tablero[k].top);
						contorno.contornos_used[indexInf] = true;
					}*/
				}
				break;
			case 4: {
					int indexSup = getIndex(tablero[k].left, tablero[k].top, tablero[k+1].top, tablero[k+2].top, tablero[k+3].top);
					contorno.contornos_used[indexSup] = true;
					/*@CONTORNO_INFERIORif (fila_actual >= 2){
						int indexInf = getIndex(tablero[k+3-LADO].right, tablero[k+3].top, tablero[k+2].top, tablero[k+1].top, tablero[k].top);
						contorno.contornos_used[indexInf] = true;
					}*/
				}
				break;
			default: break;
			}
		}
	}
	
	public static final void resetContornos(Contorno contorno) {
		for (int k=0; k < contorno.contornos_used.length; ++k) {
			contorno.contornos_used[k] = false;
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
