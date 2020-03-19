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
import org.fabri1983.eternity2.core.prune.contorno.Contorno;
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
			// clean
			matrix_zonas[k] = 0;
			
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
			// si estoy en borde top o bottom (inclusive esquinas) continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			// si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			// desde aqui estoy en el interior del tablero
			
			// me aseguro que no esté en borde left + (Contorno.MAX_COLS - 1)
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
			// si estoy en borde top o bottom (inclusive esquinas) continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			// si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			// desde aqui estoy en el interior del tablero
			
			// me aseguro que no esté dentro de (Contorno.MAX_COLS - 1) posiciones antes de border right
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
	
	public final static void cargarSuperEstructura(int processId, Pieza[] piezas, NeighborStrategy neighborStrategy)
	{
		long startingTime = System.nanoTime();
		
		llenarSuperEstructura(piezas, neighborStrategy);
		
		long elapsedMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startingTime, TimeUnit.NANOSECONDS);
		System.out.println(processId + " >>> Carga de super matriz finalizada (" + elapsedMicros + " micros)");
	}
	
	private static final void llenarSuperEstructura (Pieza[] piezas, NeighborStrategy neighborStrategy)
	{
		// itero sobre el arreglo de piezas
		for (short k = 0; k < Consts.MAX_PIEZAS; ++k) {
			
			// skip pieza central 
			if (k == Consts.NUM_P_CENTRAL)
				continue;
			
			Pieza pz = piezas[k];
			
			for (byte rot=0; rot < Consts.MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				// FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (Consts.USE_FAIR_EXPERIMENT_GIF) {
					if (pz.top == pz.bottom)
						continue;
				}
				
				neighborStrategy.addNeighbor(pz.top, pz.right, pz.bottom, pz.left, pz);
			}
		}
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
			
			wSol.println("Solucion para " + Consts.MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + Consts.MAX_PIEZAS + " piezas.");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			
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
			short resultado_parcial_count, int[] iter_desde, NeighborStrategy neighborStrategy,
			ColorRightExploredStrategy colorRightExploredStrategy) {
		
		try{
			PrintWriter writer= new PrintWriter(new BufferedWriter(new FileWriter(statusFileName, false)));
			StringBuilder writerBuffer= new StringBuilder(256 * 13);
	
			//guardo el valor de resultado_parcial_count
			writerBuffer.append(resultado_parcial_count).append("\n");
			
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
			
			//guardo los valores de iter_desde[]
			for (short n=0; n < Consts.MAX_PIEZAS; ++n) {
				if (n==(Consts.MAX_PIEZAS-1))
					writerBuffer.append(iter_desde[n]).append("\n");
				else
					writerBuffer.append(iter_desde[n]).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//guardo el contenido de arr_color_rigth_explorado
			for (short n=0; n < Consts.LADO; ++n) {
				if (n==(Consts.LADO-1))
					writerBuffer.append(colorRightExploredStrategy.get(n)).append("\n");
				else
					writerBuffer.append(colorRightExploredStrategy.get(n)).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
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
	 * Genera un archivo de piezas para leer con el editor visual e2editor.exe, y otro archivo
	 * que contiene las disposiciones de cada pieza en el tablero.
	 */
	public final static void guardarResultadoParcial(int processId, int[] tablero, String parcialFileName)
	{
		try {
			PrintWriter wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialFileName, false)));
			StringBuilder parcialBuffer= new StringBuilder(256 * 13);
			
			for (short b=0; b < Consts.MAX_PIEZAS; ++b) {
				int merged = tablero[b];
				if (merged == Consts.TABLERO_INFO_EMPTY_VALUE){
					parcialBuffer.append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(Consts.GRIS).append("\n");
				}
				else {
					byte top = Neighbors.top(merged);
					byte right = Neighbors.right(merged);
					byte bottom = Neighbors.bottom(merged);
					byte left = Neighbors.left(merged);
					parcialBuffer.append(top).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(right).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(bottom).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(left).append("\n");
				}
			}
			
			String sParcial = parcialBuffer.toString();
			
			wParcial.append(sParcial);
			wParcial.flush();
			wParcial.close();
		}
		catch(Exception ex) {
			System.out.println(processId + " >>> ERROR: No se pudieron generar los archivos de resultado parcial. " + ex.getMessage());
		}
	}

	public final static void maxLejanoParcialReached(int processId, short cursor, long time_inicial, int[] tablero,
			String parcialFileName, boolean saveStatus) {
		
		long time_final = System.nanoTime();
		long timeMillis = TimeUnit.MILLISECONDS.convert(time_final - time_inicial, TimeUnit.NANOSECONDS);
		System.out.println(processId + " >>> " + timeMillis + " ms, cursor " + cursor);
		
		if (saveStatus) {
			CommonFuncs.guardarResultadoParcial(processId, tablero, parcialFileName);
		}
	}
	
	/**
	 * La exploracion ha alcanzado su punto limite, ahora es necesario guardar estado
	 */
	public final static void operarSituacionLimiteAlcanzado(String statusFileName, int processId, int[] tablero,
			short cursor, short resultado_parcial_count, int[] desde_saved, NeighborStrategy neighborStrategy,
			ColorRightExploredStrategy colorRightExploredStrategy) {
	
		guardarEstado(statusFileName, processId, tablero, cursor, resultado_parcial_count, desde_saved,
				neighborStrategy, colorRightExploredStrategy);
	
		System.out.println(processId + " >>> ha llegado a su limite de exploracion. Exploracion finalizada forzosamente.");
	}

	public final static boolean testFairExperimentGif(byte flagZona, short cursor, int merged, int[] tablero, boolean[] usada)
	{
		if ((flagZona & Consts.MASK_F_TABLERO) == Consts.F_INTERIOR || (flagZona & Consts.MASK_F_TABLERO) == Consts.F_BORDE_TOP)
		{
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
	
}
