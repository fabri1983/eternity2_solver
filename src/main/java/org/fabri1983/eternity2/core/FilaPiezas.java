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

import org.fabri1983.eternity2.arrays.ObjectArrayList;


/**
 * Esta clase contiene un arreglo que representa una fila de piezas. El arreglo filadata[] contiene MAX_COLS celdas y
 * cada una de ellas contiene información sobre de una pieza en particular, y el arreglo referencias[] contiene la
 * referencia a la pieza.
 * 
 * @author Fabricio Lettieri
 */
public final class FilaPiezas
{
	public final static String PARAM_MAX_COLS = "max.cols.por.fila";
	
	private static byte MAX_COLS;
	private int iput; // usado como índice para poner info de pieza en la posición que corresponde de filadata[] y
						// referencias[]
	public int filadata[]; //arreglo de datos de las piezas (desde_saved)
	public Pieza referencias[]; // arreglo de referencias de piezas
	public int pos_carga; // a partir de qué posición en filadata[] cargo las piezas de esta fila.
	
	
	public FilaPiezas (final byte max_c)
	{
		MAX_COLS = max_c;
		filadata = new int[max_c];
		referencias = new Pieza[max_c];
		iput = 0;
		pos_carga = 0;
	}
	
	public static final void add (final FilaPiezas fp, final int desde, final Pieza referencia)
	{
		if (fp.iput < MAX_COLS){
			fp.filadata[fp.iput] = desde;
			fp.referencias[fp.iput] = referencia;
		}
		++fp.iput;
	}
	
	public static final void replace (final FilaPiezas fp, final int desde_comb, final Pieza referencia, final int pos)
	{
		if (pos < MAX_COLS){
			fp.filadata[pos] = desde_comb;
			fp.referencias[pos] = referencia;
		}
	}
	
	/**
	 * Indica si tiene al menos una pieza usada.
	 * @param array_fila
	 * @return
	 */
	public static final boolean tienePiezaUsada (final FilaPiezas fila)
	{
		switch (MAX_COLS){
			case 2: return fila.referencias[0].pusada.value || fila.referencias[1].pusada.value;
			case 3: return fila.referencias[0].pusada.value || fila.referencias[1].pusada.value || fila.referencias[2].pusada.value;
			case 4: return fila.referencias[0].pusada.value || fila.referencias[1].pusada.value || fila.referencias[2].pusada.value || fila.referencias[3].pusada.value;
		}
		return false;
	}
	
	/**
	 * Devuelve el índice de la primer fila con piezas libres para poner.
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 * @return El tamaño del arreglo array_fila
	 */
	public static final int primerFilaTotalmenteLibre (final ObjectArrayList array_fila)
	{
		FilaPiezas fila;

		//busco la primer fila con todas las piezas libres
		final int length = array_fila.size();
		for (int i=0; i < length; ++i)
		{
			fila = (FilaPiezas)array_fila.get(i);
			switch (MAX_COLS){
				case 2: if (fila.referencias[0].pusada.value || fila.referencias[1].pusada.value) break; else return i;
				case 3: if (fila.referencias[0].pusada.value || fila.referencias[1].pusada.value || fila.referencias[2].pusada.value) break; else return i;
				case 4: if (fila.referencias[0].pusada.value || fila.referencias[1].pusada.value || fila.referencias[2].pusada.value || fila.referencias[3].pusada.value) break; else return i;
			}
		}

		return length;
	}
	
	/**
	 * Reordena la ubicación de las filas de menor a mayor de acuerdo a las piezas de cada fila. Usa Bublesort ya que se
	 * ejeucta en la carga del juego.
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 */
	public static final void ordenarFilasBublesort (final ObjectArrayList array_fila)
	{		
		Object fp_aux;
		for (int i=0; i < array_fila.size(); ++i){
			for (int j=i; j < (array_fila.size()-1); ++j){
				//pregunto si la fila en j es mayor o igual a la fila en j+1
				if (FilaPiezas.menorFila((FilaPiezas)array_fila.get(j), (FilaPiezas)array_fila.get(j+1)) == false){
					//intercambio filas
					fp_aux = array_fila.get(j+1);
					array_fila.set(j+1, array_fila.get(j));
					array_fila.set(j, fp_aux);
				}
			}
		}
	}
	
	/**
	 * Reordena la ubicacion de las filas de menor a mayor de acuerdo a las piezas de cada fila. Usa Quicksort.
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 */
	public static final void ordenarFilasQuicksort (final ObjectArrayList array_fila)
	{
		FilaPiezas.quicksort(array_fila, 0, array_fila.size()-1);
	}
	
	/**
	 * Algoritmo Quicksort para arraeglo de FilaPiezas
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 * @param izq
	 * @param der
	 */
	private static final void quicksort (final ObjectArrayList array_fila, final int izq, final int der)
	{
		int i = izq;
	    int j = der;
	    Object pivote = array_fila.get((izq + der) / 2);
	    
	    do{
	    	while (FilaPiezas.menorFila((FilaPiezas)array_fila.get(i), (FilaPiezas)pivote)) {
	    		i++;
	    		if (i >= array_fila.size()){ i=array_fila.size()-1; break;}
			}
			while (FilaPiezas.mayorFila((FilaPiezas)array_fila.get(j), (FilaPiezas)pivote)) {
				j--;
				if (j < 0){ j=0; break;}
			}
			if (i <= j) {
				Object fp_aux = array_fila.get(i);
				array_fila.set(i, array_fila.get(j));
				array_fila.set(j, fp_aux);
				i++;
				j--;
			}
		}
	    while (i <= j);
	    
	    if (izq < j)
	    	quicksort(array_fila, izq, j);
	    if (i < der)
	    	quicksort(array_fila, i, der);
	}
	
	/**
	 * ....
	 * Usa Bucketsort.
	 * @param array_fila Arreglo de FilaPiezas
	 */
	public static final void ordenarFilasVertical (final ObjectArrayList array_fila)
	{
		
	}
	
	/**
	 * Calcula la posicion de carga de piezas en "fila_data[]" para cada fila de "array_fila".
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 */
	public static final void calcularPosCargaInicial (final ObjectArrayList array_fila)
	{
		FilaPiezas fila_actual, fila_ant;

		// Asigno el valor adecuado a la variable pos_carga que me dice a partir de qué posicién en filap[] cargo piezas
		for (int j=0; j < array_fila.size(); ++j){
			//al ser la primer fila en array_fila inicio la carga de piezas desde 0
			if (j == 0)
				((FilaPiezas)array_fila.get(j)).pos_carga = 0;
			else{
				fila_ant = (FilaPiezas)array_fila.get(j - 1); //tomo la fila anterior para poder comparar
				fila_actual = (FilaPiezas)array_fila.get(j); //tomo la fila actual
				for (int c=0; c < MAX_COLS; ++c){
					// si los numeros y las rotaciones son los mismos => seteo pos_carga en c+1
					if ( (fila_ant.referencias[c].numero == fila_actual.referencias[c].numero) && 
							(fila_ant.referencias[c].rotacion == fila_actual.referencias[c].rotacion) ) 
						fila_actual.pos_carga = c+1;
					else
						break;
				}
			}
		}	
	}
	
	/**
	 * Compara cada fila en array_fila con fp para comprobar si ya existe en array_fila.
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 * @param fp FilaPiezas
	 */
	public final static boolean existeFilaDePiezas (final ObjectArrayList array_fila, final FilaPiezas fp)
	{
		boolean esta = false;

		for (int v=0; (v < array_fila.size()) && !esta; ++v){
			if (FilaPiezas.equalsFila((FilaPiezas)array_fila.get(v), fp))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Devuelve true si la fila "fpthis" es igual a la fila "fp".
	 * 
	 * @return
	 */
	public static final boolean equalsFila (final FilaPiezas fpthis, final FilaPiezas fp)
	{
		for (int b=0; b < MAX_COLS; ++b)
			// si al menos para alguna posición las filas se diferenciasn entonces devuelvo false
			if (!FilaPiezas.equalsData(fpthis.filadata[b], fpthis.referencias[b], fp.filadata[b], fp.referencias[b]))
				return false;
		
		return true;
	}
	
	/**
	 * Devuelve true si la fila "fpthis" es menor a la fila "fp".
	 * @return
	 */
	public static final boolean menorFila (final FilaPiezas fpthis, final FilaPiezas fp)
	{
		if (FilaPiezas.equalsFila(fpthis, fp))
			return false;
		
		for (int b=0; b < MAX_COLS; ++b)
			//pregunto si fp.filadata[b] es mayor a fpthis.filadata[b]
			if (FilaPiezas.mayorData(fpthis.referencias[b], fp.referencias[b]))
				return true; //significa que fpthis es menor a fp
		
		return false;
	}
	
	/**
	 * Devuelve true si la fila "fpthis" es mayor a la fila "fp".
	 * @return
	 */
	public static final boolean mayorFila (final FilaPiezas fpthis, final FilaPiezas fp)
	{
		if (FilaPiezas.equalsFila(fpthis, fp))
			return false;
		
		for (int b=0; b < MAX_COLS; ++b)
			if (FilaPiezas.menorData(fpthis.referencias[b], fp.referencias[b]))
				return false; //significa que fpthis es menor a fp
		
		return true;
	}
	
	/**
	 * Devuelve true si numero, rotacion, y desde son iguales.
	 * 
	 * @return
	 */
	public static final boolean equalsData (final int desde1, final Pieza p1, final int desde2, final Pieza p2)
	{
		if (p1.numero != p2.numero)
			return false;
		if (p1.rotacion != p2.rotacion)
			return false;
		if (desde1 != desde2)
			return false;
		return true;
	}

	/**
	 * Devuelve true si num1 es menor que num2. Si son iguales compara con las rotaciones.
	 * @return
	 */
	public static final boolean menorData (final Pieza p1, final Pieza p2)
	{
		if (p1.numero < p2.numero)
			return true;
		else if (p1.numero > p2.numero)
			return false;
		// hasta aquí los numeros son los mismos
		if (p1.rotacion < p2.rotacion)
			return true;
		return false;
	}
	
	/**
	 * Devuelve true si num1 es mayor que num2. Si son iguales compara con las rotaciones.
	 * @return
	 */
	public static final boolean mayorData (final Pieza p1, final Pieza p2){
		if (p1.numero > p2.numero)
			return true;
		else if (p1.numero < p2.numero)
			return false;
		// hasta aquí los numeros son los mismos
		if (p1.rotacion > p2.rotacion)
			return true;
		return false;
	}
	
}
