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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.Neighbors;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public class CommonFuncs {

	/**
	 * Layout 1: Zonas del tablero: las 4 esquinas, los 4 bordes, y la zona interior.<br/>
	 * Layout 2: Zonas del tablero donse puedo setear contorno superior como usado o libre.<br/>
	 * Layout 3: Zonas del tablero para chequear si un contorno está usado o no.<br/>
	 */
	public final static byte matrix_zonas[] = new byte[Consts.MAX_PIEZAS];
	
	public final static void inicializarMatrixZonas ()
	{		
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			// esquina top-left
			if (k == 0)
				matrix_zonas[k] |= Consts.F_ESQ_TOP_LEFT;
			// esquina top-right
			else if (k == (Consts.LADO - 1))
				matrix_zonas[k] |= Consts.F_ESQ_TOP_RIGHT;
			// esquina bottom-right
			else if (k == (Consts.MAX_PIEZAS - 1))
				matrix_zonas[k] |= Consts.F_ESQ_BOTTOM_RIGHT;
			// esquina bottom-left
			else if (k == (Consts.MAX_PIEZAS - Consts.LADO))
				matrix_zonas[k] |= Consts.F_ESQ_BOTTOM_LEFT;
			// borde top
			else if ((k > 0) && (k < (Consts.LADO - 1)))
				matrix_zonas[k] |= Consts.F_BORDE_TOP;
			// borde bottom
			else if ((k > (Consts.MAX_PIEZAS - Consts.LADO)) && (k < (Consts.MAX_PIEZAS - 1)))
				matrix_zonas[k] |= Consts.F_BORDE_BOTTOM;
			// borde right
			else if (((k+1) % Consts.LADO)==0)
				matrix_zonas[k] |= Consts.F_BORDE_RIGHT;
			// borde left
			else if ((k % Consts.LADO)==0)
				matrix_zonas[k] |= Consts.F_BORDE_LEFT;
			// interior
			else
				matrix_zonas[k] |= Consts.F_INTERIOR;			
		}
	}
	
	public final static void inicializarZonaProcesoContornos()
	{
		// NOTA: para contorno inferior se debe chequear que cursor sea [33,238].
		
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté en borde left + (Contorno.MAX_COLS - 1)
			int fila_actual = k / Consts.LADO;
			if (((k - Contorno.MAX_COLUMNS) / Consts.LADO) != fila_actual)
				continue;
			
			matrix_zonas[k] |= Consts.F_PROC_CONTORNO;
		}
	}
	
	public final static void inicializarZonaReadContornos()
	{	
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté dentro de (Contorno.MAX_COLS - 1) posiciones antes de border right
			int fila_actual = k / Consts.LADO;
			if ((k + (Contorno.MAX_COLUMNS-1)) < ((fila_actual * Consts.LADO) + (Consts.LADO - 1)))
				matrix_zonas[k] |= Consts.F_READ_CONTORNO;
		}
	}
	
	public final static void ponerPiezasFijasEnTablero(int processId, Pieza[] piezas, int[] tablero, boolean[] usada) {
		Pieza p = piezas[Consts.NUM_P_CENTRAL];
		usada[p.numero] = true;
		tablero[Consts.PIEZA_CENTRAL_POS_TABLERO] = Neighbors.asMergedInfo(p.top, p.right, p.bottom, p.left, p.numero);
		System.out.println(processId + " >>> Pieza Fija en posicion " + Consts.PIEZA_CENTRAL_POS_TABLERO + " cargada en tablero");
	}
	
	public final static void cargarPiezas(int processId, Pieza[] piezas, ReaderForFile readerForTilesFile) {
		
		BufferedReader reader = null;
		
		try{
			reader = readerForTilesFile.getReader(Consts.NAME_FILE_PIEZAS);
			String linea= reader.readLine();
			short num=0;
			
			while (linea != null){
				
				if (num >= Consts.MAX_PIEZAS)
					throw new Exception("El numero que ingresaste como num de piezas por lado (" + Consts.LADO + ") es distinto del que contiene el archivo");
				
				piezas[num]= PiezaFactory.from(linea, num);
				linea= reader.readLine();
				++num;
			}

			if (num != Consts.MAX_PIEZAS)
				throw new Exception("El numero que ingresaste como num de piezas por lado (" + Consts.LADO + ") es distinto del que contiene el archivo");
		}
		catch (Exception exc){
			throw new RuntimeException(processId + " >>> ERROR. " + exc.getMessage());
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {}
			}
		}
	}
	
	/**
	 * Verifica que no exista pieza extraña o que falte alguna pieza. 
	 */
	public final static void verificarTiposDePieza(int processId, Pieza[] piezas) {
		
		// first check index matches with Pieza.numero
		for (int g=0; g < Consts.MAX_PIEZAS; ++g) {
			Pieza pzx = piezas[g];
			if (pzx.numero != g) {
				throw new RuntimeException(processId + " >>> ERROR. Pieza en indice " + g + " no coincide con su numero " + pzx.numero);
			}
		}

		// lastly check number of different types of Pieza
		int n_esq= 0;
		int n_bordes= 0;
		int n_interiores= 0;
		for (int g=0; g < Consts.MAX_PIEZAS; ++g) {
			Pieza pzx = piezas[g];
			if (Pieza.isInterior(pzx))
				++n_interiores;
			else if (Pieza.isBorder(pzx))
				++n_bordes;
			else if (Pieza.isCorner(pzx))
				++n_esq;
		}
		
		if ((n_esq != 4) || (n_bordes != (4*(Consts.LADO-2))) || (n_interiores != (Consts.MAX_PIEZAS - (n_esq + n_bordes)))) {
			throw new RuntimeException(processId + " >>> ERROR. Existe una o varias piezas incorrectas.");
		}
	}
	
	public final static void cargarSuperEstructura(int processId, Pieza[] piezas, boolean useFairExperimentGif, NeighborStrategy neighborStrategy)
	{
		long startingTime = System.nanoTime();
		
		llenarSuperEstructura(piezas, useFairExperimentGif, neighborStrategy);
		
		long elapsedMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startingTime, TimeUnit.NANOSECONDS);
		System.out.println(processId + " >>> carga de super matriz finalizada (" + elapsedMicros + " micros)");
	}
	
	private static final void llenarSuperEstructura (Pieza[] piezas, boolean useFairExperimentGif, NeighborStrategy neighborStrategy)
	{
		// itero sobre el arreglo de piezas
		for (short k = 0; k < Consts.MAX_PIEZAS; ++k) {
			
			// skip pieza central 
			if (k == Consts.NUM_P_CENTRAL)
				continue;
			
			Pieza pz = piezas[k];
			
			for (byte rot=0; rot < Consts.MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (useFairExperimentGif && (pz.top == pz.bottom))
					continue;
				
				neighborStrategy.addNeighbor(pz.top, pz.right, pz.bottom, pz.left, pz);
			}
		}
	}

	/**
	 * Dada la posicion de cursor se fija cuáles colores tiene alrededor y devuelve una referencia de Neighbors 
	 * que contiene las piezas que cumplan con los colores en el orden top-right-bottom-left (sentido horario).
	 *  
	 * NOTA: saqué muchas sentencias porque solamente voy a tener una pieza fija (135 en tablero), por eso 
	 * este metodo solo contempla las piezas top y left, salvo en el vecindario de la pieza fija.
	 */
	public final static Neighbors neighbors (byte flagZona, short cursor, int[] tablero, NeighborStrategy neighborStrategy)
	{
		// check for vicinity of fixed tiles positions
		switch (cursor) {
			// estoy en la posicion inmediatamente arriba de la posicion central
			case Consts.ABOVE_PIEZA_CENTRAL_POS_TABLERO:
				return neighborStrategy.interior_above_central(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
			// estoy en la posicion inmediatamente a la izq de la posicion central
			case Consts.BEFORE_PIEZA_CENTRAL_POS_TABLERO:
				return neighborStrategy.interior_left_central(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
			case Consts.BELOW_PIEZA_CENTRAL_POS_TABLERO:
				return neighborStrategy.interior(
						Consts.PIEZA_CENTRAL_COLOR_BOTTOM, Neighbors.right(tablero[cursor - 1]));
		}
		
		switch (flagZona & Consts.MASK_F_TABLERO) {
			// interior de tablero
			case Consts.F_INTERIOR: 
				return neighborStrategy.interior(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
	
			// borde right
			case Consts.F_BORDE_RIGHT:
				return neighborStrategy.border_right(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
			// borde left
			case Consts.F_BORDE_LEFT:
				return neighborStrategy.border_left(
						Neighbors.bottom(tablero[cursor - Consts.LADO]));
			// borde top
			case Consts.F_BORDE_TOP:
				return neighborStrategy.border_top(
						Neighbors.right(tablero[cursor - 1]));
			// borde bottom
			case Consts.F_BORDE_BOTTOM:
				return neighborStrategy.border_bottom(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
		
			// esquina top-left
			case Consts.F_ESQ_TOP_LEFT:
				return neighborStrategy.corner_top_left();
			// esquina top-right
			case Consts.F_ESQ_TOP_RIGHT:
				return neighborStrategy.corner_top_right(
						Neighbors.right(tablero[cursor - 1]));
			// esquina bottom-left
			case Consts.F_ESQ_BOTTOM_LEFT: 
				return neighborStrategy.corner_bottom_left(
						Neighbors.bottom(tablero[cursor - Consts.LADO]));
			// esquina bottom-right
			case Consts.F_ESQ_BOTTOM_RIGHT:
				return neighborStrategy.corner_bottom_right(
						Neighbors.bottom(tablero[cursor - Consts.LADO]), Neighbors.right(tablero[cursor - 1]));
		}
		
		return null;
	}

	/**
	 * En el archivo NAME_FILE_SOLUCION se guardan los colores de cada pieza.
	 * En el archivo NAME_FILE_DISPOSICION se guarda el numero y rotacion de cada pieza.
	 */
	public final static void guardarSolucion (int processId, int[] tablero, String solucFileName, String dispFileName)
	{
		try{
			PrintWriter wSol= new PrintWriter(new BufferedWriter(new FileWriter(solucFileName, true)));
			PrintWriter wDisp= new PrintWriter(new BufferedWriter(new FileWriter(dispFileName, true)));
			StringBuilder contenidoDisp= new StringBuilder(256 * 13);
			
			wSol.println("Solucion para " + Consts.MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + Consts.MAX_PIEZAS + " piezas.");
			contenidoDisp.append("Disposicion para " + Consts.MAX_PIEZAS + " piezas.\n");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			contenidoDisp.append("(num pieza) (estado rotacion) (posicion en tablero real)\n");
			
			for (int b=0; b < Consts.MAX_PIEZAS; ++b)
			{
				int pos= b+1;
				int merged = tablero[b];
				int numero = Neighbors.numero(merged);
				int top = Neighbors.top(merged);
				int right = Neighbors.right(merged);
				int bottom = Neighbors.bottom(merged);
				int left = Neighbors.left(merged);
				wSol.println(top + Consts.SECCIONES_SEPARATOR_EN_FILE + right + Consts.SECCIONES_SEPARATOR_EN_FILE + bottom + Consts.SECCIONES_SEPARATOR_EN_FILE + left);
				wDisp.println((numero + 1) + Consts.SECCIONES_SEPARATOR_EN_FILE + pos);
				contenidoDisp.append(numero + 1).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
			}
			
			wSol.println();
			wSol.println("-----------------------------------------------------------------");
			wSol.println();
			wDisp.println();
			wDisp.println("-----------------------------------------------------------------");
			wDisp.println();
			wSol.flush();
			wDisp.flush();
			wSol.close();
			wDisp.close();
		}
		catch(Exception ex)
		{
			System.out.println(processId + " >>> ERROR: No se pudo guardar la solucion!! QUE MACANA!!! (guardarSolucion())");
			System.out.println(ex);
		}
	}
	
	/**
	 * Guarda las estructuras necesaria del algoritmo para poder continuar desde el actual estado de exploración.
	 */
	public final static void guardarEstado(String statusFileName, int processId, int[] tablero, short cursor,
			int mas_bajo, int mas_alto, int mas_lejano_parcial_max, byte[] desde_saved,
			NeighborStrategy neighborStrategy, ColorRightExploredStrategy colorRightExploredStrategy) {
		
		try{
			PrintWriter writer= new PrintWriter(new BufferedWriter(new FileWriter(statusFileName)));
			StringBuilder writerBuffer= new StringBuilder(256 * 13);
	
			//guardo el valor de mas_bajo
			writerBuffer.append(mas_bajo).append("\n");
			
			//guardo el valor de mas_alto
			writerBuffer.append(mas_alto).append("\n");
			
			//guardo el valor de mas_lejano 
			writerBuffer.append(mas_lejano_parcial_max).append("\n");
			
			//guardo el valor del cursor
			writerBuffer.append(cursor).append("\n");
			
			//guardo los indices de piezas de tablero[]
			for (short n=0; n < Consts.MAX_PIEZAS; ++n) {
				if (n==(Consts.MAX_PIEZAS - 1)) {
					writerBuffer.append(tablero[n]).append("\n");
				}
				else {
					writerBuffer.append(tablero[n]).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//########################################################################
			/**
			 * Calculo los valores para desde_saved[]
			 */
			//########################################################################
			for (short _cursor = 0; _cursor < cursor; ++_cursor) {
				// para la pieza central no se tiene en cuenta su valor desde_saved[]
				if (_cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
					desde_saved[_cursor] = 0;
					continue;
				}
				// obtengo el valor para desde_saved[]
				int mergedInfo = tablero[_cursor];
				byte flagZona = matrix_zonas[_cursor];
				desde_saved[_cursor] = Neighbors.getIndexMergedInfo(
						neighbors(flagZona, _cursor, tablero, neighborStrategy), 
						mergedInfo);
			}
			//ahora todo lo que está despues de cursor tiene que valer cero
			for (short _cursor = cursor; _cursor < Consts.MAX_PIEZAS; ++_cursor)
				desde_saved[_cursor] = 0;
			//########################################################################
			
			//guardo las posiciones de posibles piezas (desde_saved[]) de cada nivel del backtracking
			for (short n=0; n < Consts.MAX_PIEZAS; ++n) {
				if (n==(Consts.MAX_PIEZAS-1))
					writerBuffer.append(desde_saved[n]).append("\n");
				else
					writerBuffer.append(desde_saved[n]).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//indico si se utiliza poda de color explorado o no
			if (colorRightExploredStrategy != null)
				writerBuffer.append(Boolean.TRUE).append("\n");
			else
				writerBuffer.append(Boolean.FALSE).append("\n");
			
			//guardo el contenido de arr_color_rigth_explorado
			if (colorRightExploredStrategy != null)
			{
				for (short n=0; n < Consts.LADO; ++n) {
					if (n==(Consts.LADO-1))
						writerBuffer.append(colorRightExploredStrategy.get(n)).append("\n");
					else
						writerBuffer.append(colorRightExploredStrategy.get(n)).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			String sContent = writerBuffer.toString();
			writer.append(sContent);
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			System.out.println(processId + " >>> ERROR: No se pudo guardar el estado de la exploración.");
			System.out.println(e);
		}
	}

	/**
	 * La exploracion ha alcanzado su punto limite, ahora es necesario guardar estado
	 */
	public final static void operarSituacionLimiteAlcanzado(String statusFileName, int processId, int[] tablero,
			short cursor, int mas_bajo, int mas_alto, int mas_lejano_parcial_max, byte[] desde_saved,
			NeighborStrategy neighborStrategy, ColorRightExploredStrategy colorRightExploredStrategy) {
			
		guardarEstado(statusFileName, processId, tablero, cursor, mas_bajo, mas_alto, mas_lejano_parcial_max, desde_saved, 
				neighborStrategy, colorRightExploredStrategy);
		
		System.out.println(processId + " >>> ha llegado a su limite de exploracion. Exploracion finalizada forzosamente.");
	}
	
	/**
	 * Genera un archivo de piezas para leer con el editor visual e2editor.exe, otro archivo
	 * que contiene las disposiciones de cada pieza en el tablero, y otro archivo que me dice
	 * las piezas no usadas (generado solo si max es true).
	 * Si max es true, el archivo generado es el que tiene la mayor disposición de piezas encontrada.
	 * Si max es false, el archivo generado contiene la disposición de piezas en el instante cuando
	 * se guarda estado.
	 */
	public final static int guardarResultadoParcial(boolean max, int processId, int[] tablero, int sig_parcial,
			int maxNumParcial, String parcialFileName, String parcialMaxFileName, String disposicionMaxFileName)
	{
		if (maxNumParcial == 0 && !max)
			return sig_parcial;
		
		try {
			PrintWriter wParcial= null;
			// si estamos en max instance tenemos q guardar las disposiciones de las piezas
			PrintWriter wDispMax = null;
			StringBuilder parcialBuffer= new StringBuilder(256 * 13);
			StringBuilder dispMaxBuff= new StringBuilder(256 * 13);
			
			if (max){
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialMaxFileName)));
				wDispMax= new PrintWriter(new BufferedWriter(new FileWriter(disposicionMaxFileName)));
				dispMaxBuff.append("(num pieza) (estado rotacion) (posicion en tablero real)").append("\n");
			}
			else{
				String parcialFName = parcialFileName.substring(0, parcialFileName.indexOf(Consts.FILE_EXT)) + "_" + sig_parcial + Consts.FILE_EXT;
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialFName)));
				++sig_parcial;
				if (sig_parcial > maxNumParcial)
					sig_parcial= 1;
			}
			
			for (short b=0; b < Consts.MAX_PIEZAS; ++b) {
				int pos= b+1;
				int merged = tablero[b];
				if (merged == -1){
					parcialBuffer.append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append("\n");
					if (max)
						dispMaxBuff.append("-").append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
				}
				else {
					short numero = Neighbors.numero(merged);
					byte top = Neighbors.top(merged);
					byte right = Neighbors.right(merged);
					byte bottom = Neighbors.bottom(merged);
					byte left = Neighbors.left(merged);
					parcialBuffer.append(top).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(right).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(bottom).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(left).append("\n");
					if (max)
						dispMaxBuff.append(numero + 1).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
				}
			}
			
			String sParcial = parcialBuffer.toString();
			String sDispMax = dispMaxBuff.toString();
			
			// parcial siempre se va a guardar
			wParcial.append(sParcial);
			wParcial.flush();
			wParcial.close();
			
			// solo guardamos max si es una instancia de max
			if (max){
				wDispMax.append(sDispMax);
				wDispMax.flush();
				wDispMax.close();
			}
			
			return sig_parcial;
		}
		catch(Exception ex) {
			System.out.println(processId + " >>> ERROR: No se pudieron generar los archivos de resultado parcial. " + ex.getMessage());
			return sig_parcial;
		}
	}

	public final static boolean testPodaColorRightExplorado(byte flagZona, short cursor, byte right,
			ColorRightExploredStrategy colorRightExploredStrategy) {
		
		int fila_actual = cursor >>> Consts.LADO_FOR_SHIFT_DIVISION; // if divisor is power of 2 then we can use >>
		
		// For modulo try this for better performance only if divisor is power of 2 and dividend is positive: dividend & (divisor - 1)
		// Old was: ((cursor+2) % LADO) == 0
		final boolean flag_antes_borde_right = ((cursor + 2) & (Consts.LADO - 1)) == 0;
		
		// si estoy antes del borde right limpio el arreglo de colores right usados
		if (flag_antes_borde_right)
			colorRightExploredStrategy.compareAndSet(fila_actual + 1, 0);
		
		if ((flagZona & Consts.MASK_F_TABLERO) == Consts.F_BORDE_LEFT)
		{
			final int mask = 1 << right;
			
			// pregunto si el color right de la pieza de borde left actual ya está explorado
			int color = colorRightExploredStrategy.get(fila_actual);
			if ((color & mask) != 0) {
				return true; // sigo con otra pieza de borde
			}
			// si no es así entonces lo seteo como explorado
			else {
				// asignación en una sola operación, ya que el bit en p.right vale 0 (según la condición anterior)
				colorRightExploredStrategy.compareAndSet(fila_actual, color | mask);
				// int value = SolverFaster.arr_color_rigth_explorado.get(fila_actual) | 1 << p.right;
				// SolverFaster.arr_color_rigth_explorado.compareAndSet(fila_actual, value);
			}
		}
		
		return false;
	}

	public final static boolean testFairExperimentGif(byte flagZona, short cursor, int merged, int[] tablero, boolean[] usada)
	{
		if ((flagZona & Consts.MASK_F_TABLERO) == Consts.F_INTERIOR || (flagZona & Consts.MASK_F_TABLERO) == Consts.F_BORDE_TOP) {
			byte bottom = Neighbors.bottom(merged);
			byte bottomAnterior = Neighbors.bottom(tablero[cursor - 1]);
			if (bottom == bottomAnterior){
				short numero = Neighbors.numero(merged);
				usada[numero] = false;
				return true;
			}
		}
		
		return false;
	}

	public final static void toggleContorno(boolean value, short cursor, byte flagZona, Contorno contorno, int[] tablero, int mergedActual)
	{
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if ((flagZona & Consts.MASK_F_PROC_CONTORNO) == Consts.F_PROC_CONTORNO) {
			int mergedAnterior = tablero[cursor - 1];
			contorno.used
					[Neighbors.left(mergedAnterior)]
					[Neighbors.top(mergedAnterior)]
					[Neighbors.top(mergedActual)] = value;
		}
	}

	public final static boolean esContornoSuperiorUsado(short cursor, byte flagZona, Contorno contorno, int[] tablero)
	{
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if ((flagZona & Consts.MASK_F_READ_CONTORNO) == Consts.F_READ_CONTORNO) {
			return contorno.used
					[Neighbors.right(tablero[cursor-1])]
					[Neighbors.bottom(tablero[cursor - Consts.LADO])]
					[Neighbors.bottom(tablero[cursor - Consts.LADO + 1])];
		}
		return false;
	}
	
}
