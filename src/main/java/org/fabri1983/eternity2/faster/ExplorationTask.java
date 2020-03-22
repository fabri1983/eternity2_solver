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

package org.fabri1983.eternity2.faster;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.CommonFuncs;
import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.Neighbors;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.prune.contorno.Contorno;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public class ExplorationTask implements Runnable {

	protected int pos_multi_process_offset = 0; // usado con POSICION_TASK_DIVISION sirve para continuar haciendo los calculos de distribución de exploración
	
	protected String statusFileName, parcialFileName, disposicionFileName, solucFileName;
	
	public final int[] tablero = new int[Consts.MAX_PIEZAS];
	public final boolean[] usada = new boolean[Consts.MAX_PIEZAS];
	protected final Contorno contorno = new Contorno();
	protected final int[] iter_desde = new int[Consts.MAX_PIEZAS];
	
	public short cursor;

	private boolean status_cargado; // inidica si se ha cargado estado inicial
	
	public long count_cycles;
	protected long time_inicial; // sirve para calcular el tiempo al hito de posición lejana
	protected long time_max_ciclos; //usado para calcular el tiempo entre diferentes status saved
	
	final ColorRightExploredStrategy colorRightExploredStrategy = new ColorRightExploredStrategy();
	
	/**
	 * 0-based para identificar la task y para saber qué rama de la exploración tomar cuando esté en POSICION_TASK_DIVISION
	 */
	public final int ID;
	
	protected CountDownLatch startSignal;
	
	public ExplorationTask(int _id, CountDownLatch startSignal) {
		
		ID = _id;
		
		statusFileName = SolverFaster.NAME_FILE_STATUS + "_" + ID + Consts.FILE_EXT;
		parcialFileName = SolverFaster.NAME_FILE_PARCIAL + "_" + ID + Consts.FILE_EXT;
		disposicionFileName = SolverFaster.NAME_FILE_DISPOSICION + "_" + ID + Consts.FILE_EXT;
		solucFileName = SolverFaster.NAME_FILE_SOLUCION + "_" + ID + Consts.FILE_EXT;

		this.startSignal = startSignal;
	}

	public void setupInicial(ReaderForFile readerForTilesFile) {
		
		// Pruebo cargar el primer status_saved
		status_cargado = SolverFaster.cargarEstado(statusFileName, this);
		
		// cargo las posiciones fijas
		CommonFuncs.ponerPiezasFijasEnTablero(ID, SolverFaster.piezas, tablero, usada);
		
		// seteo como usados los contornos ya existentes en tablero
		contorno.inicializarContornos(tablero, Consts.MAX_PIEZAS, Consts.LADO);
	}

	public void resetForBenchmark(int _num_processes, CountDownLatch startSignal) {
		
		count_cycles = 0;
		
		cursor = 0;
		pos_multi_process_offset = 0;
		
		for (int k=0; k < iter_desde.length; ++k) {
			iter_desde[k] = 0;
		}
		
		this.startSignal = startSignal;

		cleanTablero();

		cleanUsada();
		
		contorno.resetContornos();
	}

	private void cleanTablero() {
		for (int k=0, c=tablero.length; k < c; ++k) {
			tablero[k] = Consts.TABLERO_INFO_EMPTY_VALUE;
		}
	}

	private void cleanUsada() {
		for (int k=0, c=usada.length; k < c; ++k) {
			usada[k] = false;
		}
	}
	
	@Override
	public void run() {
		try {
			// await for starting signal
			startSignal.await();
			// start working
			atacar();
		} catch (InterruptedException e) {
			System.out.println(ID + " >>> task interrupted.");
		}
	}

	protected void atacar() {
				
		if (SolverFaster.flag_retroceder_externo) {
			SolverFaster.retrocederEstado(this);
			return;
		}
		
		long nowNanos = System.nanoTime();
		time_inicial = nowNanos;
		time_max_ciclos = nowNanos;
		
		// si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado) {
			explorar(0, SolverFaster.NUM_PROCESSES, 0);
		}
		// se cargó estado de exploración, voy a simular pop del stack que cargué
		else {
			while (cursor >= 0) {
				
				explorar(iter_desde[cursor], SolverFaster.NUM_PROCESSES, 0);
				--cursor;
				
				// si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				int mergedInfo = tablero[cursor];
				
				// seteo el contorno como libre
				contorno.toggleContorno(false, cursor, CommonFuncs.matrix_zonas[cursor], tablero, mergedInfo);

				// debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != Consts.PIEZA_CENTRAL_POS_TABLERO) {
					int numero = Neighbors.numero(mergedInfo);
					usada[numero] = false;
					tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
				}
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println(ID + " >>> Exploracion Agotada.");
	}
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final void explorar(int desde, int num_processes, int pos_multi_process_offset)
	{
		// si cursor se pasa del limite de piezas, significa que estoy en una solucion
		if (cursor == Consts.MAX_PIEZAS) {
			CommonFuncs.guardarSolucion(ID, tablero, solucFileName, disposicionFileName);
			System.out.println(ID + " >>> Solucion Encontrada!!");
			return; // evito que la instancia de exploracion continue
		}
		
		// si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor == SolverFaster.LIMITE_RESULTADO_PARCIAL) {
			++SolverFaster.LIMITE_RESULTADO_PARCIAL;
			CommonFuncs.maxLejanoParcialReached(ID, cursor, time_inicial, tablero, parcialFileName, SolverFaster.SAVE_STATUS_ON_MAX);
		}
		
		// si llegué a MAX_CICLOS de ejecucion, guardo el estado de exploración
		if (count_cycles == SolverFaster.MAX_CICLOS) {
			maxCyclesReached();
		}
		
		byte flagZona = CommonFuncs.matrix_zonas[cursor];
		
		// CUDA: This needs to be trasnformed to a normal exploration, so changes may be required.
		// si la posicion cursor es una posicion fija no tengo que hacer la exploracion "standard"
		if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
			// seteo el contorno como usado
			contorno.toggleContorno(true, cursor, flagZona, tablero, tablero[cursor]);
			// at this point we have set all things up related to a fixed tile, so continue normally with next board position
			++cursor;
			explorar(0, num_processes, pos_multi_process_offset);
			--cursor;
			// seteo el contorno como libre
			contorno.toggleContorno(false, cursor, flagZona, tablero, tablero[cursor]);
			return;
		}

		// can be null when there is no neighbors and when cursor == Consts.PIEZA_CENTRAL_POS_TABLERO
		Neighbors nbs = Neighbors.neighbors(flagZona, cursor, tablero, SolverFaster.neighborStrategy);
		
		// CUDA: I don't know how to transform this condition. Maybe the return is fine.
		// if no neighbors then backtrack
		if (nbs == null)
			return;
		
		// CUDA: I don't know how to transform this condition. Maybe the return is fine.
		// pregunto si el contorno superior de las posiciones subsecuentes generan un contorno ya usado
		if (contorno.esContornoSuperiorUsado(cursor, flagZona, tablero)) {
			return;
		}
		
		int hasta = nbs.mergedInfo.length;
		
		// CUDA: Convert condition to a variable valued as 0 or 1, and use it as factor to lately assign desde and hasta.
		// Task Division: establezco los limites de las piezas a explorar para este cursor y siguiente exploración (si aplica)
		if (cursor == Consts.POSICION_TASK_DIVISION + pos_multi_process_offset) {
			
			// NOTE: next conditions are such that they always set work to processes, even when the task division is odd.
			
			int thisProc = ID % num_processes;
			
			// caso 1: cada proc toma una única rama de Neighbors
			if (num_processes == hasta) {
				desde = thisProc;
				hasta = thisProc + 1;
			}
			// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas.
			else if (num_processes < hasta) {
				int span = (hasta + 1) / num_processes;
				desde = thisProc * span;
				// considering cases when task division is odd:
				//  - normal task distribution while not being the last process: hasta = desde + span
				//  - when being the last process we need to cover all remaining tasks: hasta remains unchanged
				if (thisProc != (num_processes - 1)) // normal task distribution while not being the last process 
					hasta = thisProc * span + span;
			}
			// caso 3: existen mas procs que neighbors a explorar, entonces hay que distribuir los procs y
			// aumentar el pos_multi_process_offset en uno asi el siguiente nivel tmb continua la división.
			// Seteo num_processes = hasta asi el siguiente nivel divide correctamente.
			else {
				int divisor = (num_processes + 1) / hasta; // reparte los procs por posible neighbor
				desde = thisProc / divisor;
				num_processes = hasta;
				if (desde < hasta)
					hasta = desde + 1;
				else
					desde = hasta - 1;
				++pos_multi_process_offset;
			}
			
			// CUDA: task division branchless
			// 0 when equals, 1 when neighbors > num_processes, -1 when neighbors < num_processes
//			int comparisonNumProcsAndLength = hasta - num_processes; // will only use 0 and 1
//			int moreProcsThanNeighbors = hasta - num_processes < 0 ? 0 : 1;
//			int isLastProc = thisProc == (num_processes - 1) ? 1 : 0;
//
//			int span = (1 - comparisonNumProcsAndLength) |
//				comparisonNumProcsAndLength * ((hasta + num_processes - 1) / num_processes);
//			desde = thisProc * span;
//			hasta = (1 - comparisonNumProcsAndLength) * (thisProc + 1) |
//				comparisonNumProcsAndLength * Math.max(isLastProc * hasta, (1 - isLastProc) * (thisProc * span + span));
			
//			System.out.println("Rank " + ID + ":::: Total " + nbs.mergedInfo.length + ". Limites " + desde + "," + hasta);
		}
		
		// clean bits of this row
		colorRightExploredStrategy.cleanBorderColorCurrentRow(flagZona, cursor, tablero);
		
		while (desde < hasta) {
			
			int mergedInfo = nbs.mergedInfo[desde];
			short numero = Neighbors.numero(mergedInfo);
			
			// clean bits next row, every time a new neighbor is selected
			colorRightExploredStrategy.cleanNextRow(cursor);
			
			if (usada[numero]) {
				++desde;
				continue; // continúo con el siguiente neighbor
			}
			
			if (colorRightExploredStrategy.run(flagZona, cursor, mergedInfo, tablero)) {
				++desde;
				continue; // continúo con el siguiente neighbor
			}
			
			// FairExperiment.gif: color bottom repetido en sentido horizontal
//			if (Consts.USE_FAIR_EXPERIMENT_GIF) {
//				if (CommonFuncs.testFairExperimentGif(flagZona, cursor, mergedInfo, tablero, usada)) {
//					++desde;
//					continue; // continúo con el siguiente neighbor
//				}
//			}
			
			// pregunto si el borde inferior que genero con la nueva pieza está siendo usado
//			@CONTORNO_INFERIOR
//			if (esContornoInferiorUsado(cursor, flagZona, contorno, tablero, mergedInfo)){
//				++desde;
//				continue; // continúo con el siguiente neighbor
//			}
			
			// seteo el contorno como usado
			contorno.toggleContorno(true, cursor, flagZona, tablero, mergedInfo);
			
			tablero[cursor] = mergedInfo;
			usada[numero] = true;
			iter_desde[cursor] = desde + 1;
			
			++count_cycles;
			
			++cursor;
			explorar(0, num_processes, pos_multi_process_offset);
			--cursor;
			
			usada[numero] = false;
//			tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
			
			// seteo el contorno como libre
			contorno.toggleContorno(false, cursor, flagZona, tablero, mergedInfo);
			
			++desde;
		}
		
		iter_desde[cursor] = 0;
		tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
	}

	protected void maxCyclesReached() {
		long durationNanos = System.nanoTime() - time_max_ciclos;
		long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
		long piecesPerSec = count_cycles / TimeUnit.SECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
		
		System.out.println(ID + " >>> cursor " + cursor + ". Tiempo: " + durationMillis + " ms, " + piecesPerSec + " pieces/sec");
		
		count_cycles = 0;
		
		if (SolverFaster.SAVE_STATUS_ON_MAX) {
			CommonFuncs.guardarEstado(statusFileName, ID, tablero, cursor, SolverFaster.LIMITE_RESULTADO_PARCIAL,
					iter_desde, SolverFaster.neighborStrategy, colorRightExploredStrategy);
			CommonFuncs.guardarResultadoParcial(ID, tablero, parcialFileName);
		}
		
		time_max_ciclos = System.nanoTime();
	}
	
}