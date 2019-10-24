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
 * cada una de ellas contiene información de una pieza en particular, y el arreglo referencias[] contiene la
 * referencia a la pieza.
 * 
 * @author Fabricio Lettieri
 */
public final class FilaPiezas
{
	private static final byte MAX_COLS = 2; // usar valor entre 2 y 4
	
	private int iput; // usado como índice para poner info de pieza en la posición que corresponde de filadata[] y
						// referencias[]
	public int[] filadata; //arreglo de datos de las piezas (desde_saved)
	public Pieza[] referencias; // arreglo de referencias de piezas
	public int pos_carga; // a partir de qué posición en filadata[] cargo las piezas de esta fila.
	
	public FilaPiezas ()
	{
		filadata = new int[MAX_COLS];
		referencias = new Pieza[MAX_COLS];
		iput = 0;
		pos_carga = 0;
	}
	
	public static final void add (FilaPiezas fp, int desde, Pieza referencia)
	{
		if (fp.iput < MAX_COLS){
			fp.filadata[fp.iput] = desde;
			fp.referencias[fp.iput] = referencia;
		}
		++fp.iput;
	}
	
	public static final void replace (FilaPiezas fp, int desde_comb, Pieza referencia, int pos)
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
	public static final boolean tienePiezaUsada (FilaPiezas fila)
	{
		switch (MAX_COLS){
			case 2: return fila.referencias[0].usada || fila.referencias[1].usada;
			case 3: return fila.referencias[0].usada || fila.referencias[1].usada || fila.referencias[2].usada;
			case 4: return fila.referencias[0].usada || fila.referencias[1].usada || fila.referencias[2].usada || fila.referencias[3].usada;
		}
		return false;
	}
	
	/**
	 * Devuelve el índice de la primer fila con piezas libres para poner.
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 * @return El tamaño del arreglo array_fila
	 */
	public static final int primerFilaTotalmenteLibre (ObjectArrayList<FilaPiezas> array_fila)
	{
		//busco la primer fila con todas las piezas libres
		final int length = array_fila.size();
		for (int i=0; i < length; ++i)
		{
			FilaPiezas fila = array_fila.get(i);
			switch (MAX_COLS){
				case 2: if (fila.referencias[0].usada || fila.referencias[1].usada) break; else return i;
				case 3: if (fila.referencias[0].usada || fila.referencias[1].usada || fila.referencias[2].usada) break; else return i;
				case 4: if (fila.referencias[0].usada || fila.referencias[1].usada || fila.referencias[2].usada || fila.referencias[3].usada) break; else return i;
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
	public static final void ordenarFilasBublesort (ObjectArrayList<FilaPiezas> array_fila)
	{		
		for (int i=0, c=array_fila.size(); i < c; ++i){
			for (int j=i; j < (c - 1); ++j){
				//pregunto si la fila en j es mayor o igual a la fila en j+1
				if (FilaPiezas.menorFila(array_fila.get(j), array_fila.get(j+1)) == false){
					//intercambio filas
					FilaPiezas fp_aux = array_fila.get(j+1);
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
	public static final void ordenarFilasQuicksort (ObjectArrayList<FilaPiezas> array_fila)
	{
		FilaPiezas.quicksort(array_fila, 0, array_fila.size()-1);
	}
	
	/**
	 * Algoritmo Quicksort para arreglo de FilaPiezas
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 * @param izq
	 * @param der
	 */
	private static final void quicksort (ObjectArrayList<FilaPiezas> array_fila, int izq, int der)
	{
		int i = izq;
	    int j = der;
	    FilaPiezas pivote = array_fila.get((izq + der) / 2);
	    
	    do{
	    	while (FilaPiezas.menorFila(array_fila.get(i), pivote)) {
	    		i++;
	    		if (i >= array_fila.size()){ i=array_fila.size()-1; break;}
			}
			while (FilaPiezas.mayorFila(array_fila.get(j), pivote)) {
				j--;
				if (j < 0){ j=0; break;}
			}
			if (i <= j) {
				FilaPiezas fp_aux = array_fila.get(i);
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
	public static final void ordenarFilasVertical (ObjectArrayList<FilaPiezas> array_fila)
	{
		
	}
	
	/**
	 * Calcula la posicion de carga de piezas en "fila_data[]" para cada fila de "array_fila".
	 * 
	 * @param array_fila Arreglo de FilaPiezas
	 */
	public static final void calcularPosCargaInicial (ObjectArrayList<FilaPiezas> array_fila)
	{
		// Asigno el valor adecuado a la variable pos_carga que me dice a partir de qué posicién en filap[] cargo piezas
		for (int j=0, cc=array_fila.size(); j < cc; ++j){
			//al ser la primer fila en array_fila inicio la carga de piezas desde 0
			if (j == 0)
				array_fila.get(j).pos_carga = 0;
			else{
				FilaPiezas fila_ant = array_fila.get(j - 1); //tomo la fila anterior para poder comparar
				FilaPiezas fila_actual = array_fila.get(j); //tomo la fila actual
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
	public final static boolean existeFilaDePiezas (ObjectArrayList<FilaPiezas> array_fila, FilaPiezas fp)
	{
		boolean esta = false;

		for (int v=0, c=array_fila.size(); (v < c) && !esta; ++v){
			if (FilaPiezas.equalsFila(array_fila.get(v), fp))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Devuelve true si la fila "fpthis" es igual a la fila "fp".
	 * 
	 * @return
	 */
	public static final boolean equalsFila (FilaPiezas fpthis, FilaPiezas fp)
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
	public static final boolean menorFila (FilaPiezas fpthis, FilaPiezas fp)
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
	public static final boolean mayorFila (FilaPiezas fpthis, FilaPiezas fp)
	{
		if (FilaPiezas.equalsFila(fpthis, fp))
			return false;
		
		for (int b=0; b < MAX_COLS; ++b) {
			if (FilaPiezas.menorData(fpthis.referencias[b], fp.referencias[b]))
				return false; //significa que fpthis es menor a fp
		}
		
		return true;
	}
	
	/**
	 * Devuelve true si numero, rotacion, y desde son iguales.
	 * 
	 * @return
	 */
	public static final boolean equalsData (int desde1, Pieza p1, int desde2, Pieza p2)
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
	public static final boolean menorData (Pieza p1, Pieza p2)
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
	public static final boolean mayorData (Pieza p1, Pieza p2){
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
