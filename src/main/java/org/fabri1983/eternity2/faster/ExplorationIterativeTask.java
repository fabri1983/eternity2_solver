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

import org.fabri1983.eternity2.core.CommonFuncs;
import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.Neighbors;

public class ExplorationIterativeTask extends ExplorationTask {
	
	private int num_processes;
	private final int[] num_processes_orig = new int[Consts.MAX_PIEZAS - Consts.POSICION_TASK_DIVISION + 1];
	final int[] iter_hasta = new int[Consts.MAX_PIEZAS];
	
	public ExplorationIterativeTask(int _id, int _num_processes, CountDownLatch startSignal) {
		super(_id, startSignal);
		this.num_processes = _num_processes;
	}

	@Override
	public void resetForBenchmark(int _num_processes, CountDownLatch startSignal) {
		
		num_processes = _num_processes;
		
		for (int k=0; k < num_processes_orig.length; ++k) {
			num_processes_orig[k] = _num_processes;
		}
		
		for (int k=0; k < iter_hasta.length; ++k) {
			iter_hasta[k] = 0;
		}
		
		super.resetForBenchmark(_num_processes, startSignal);
	}

	@Override
	protected void atacar() {
				
		if (SolverFaster.flag_retroceder_externo) {
			SolverFaster.retrocederEstado(this);
			return;
		}
		
		long nowNanos = System.nanoTime();
		time_inicial = nowNanos;
		time_max_ciclos = nowNanos;
		
		explorar();
		
		System.out.println(ID + " >>> Exploracion Agotada.");
	}
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final void explorar()
	{
		boolean continueLoopNeighbors = false;
		
		while (true) {
			
			main_loop:
			while (true) {
		
				byte flagZona = CommonFuncs.matrix_zonas[cursor];
				
				// can be null when there is no neighbors and when cursor == Consts.PIEZA_CENTRAL_POS_TABLERO
				Neighbors nbs = Neighbors.neighbors(flagZona, cursor, tablero, SolverFaster.neighborStrategy);
				
				// if not continuing from a loop of neighbors then do some normal checks
				if (!continueLoopNeighbors) {
					
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
					
					// si la posicion cursor es una posicion fija no tengo que hacer la exploracion "standard"
					if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
						// seteo el contorno como usado
						contorno.toggleContorno(true, cursor, flagZona, tablero, tablero[cursor]);
						// at this point we have set all things up related to a fixed tile, so continue normally with next board position
						++cursor;
						// get new values
						flagZona = CommonFuncs.matrix_zonas[cursor];
						nbs = Neighbors.neighbors(flagZona, cursor, tablero, SolverFaster.neighborStrategy);
					}
					
					if (nbs == null)
						break; // exit the while-loop to back track a position
					
					// pregunto si el contorno superior de las posiciones subsecuentes generan un contorno ya usado
					if (contorno.esContornoSuperiorUsado(cursor, flagZona, tablero)) {
						break; // exit the while-loop to back track a position
					}
					
					int hasta = nbs.mergedInfo.length;
					iter_hasta[cursor] = hasta;
					
					// Task Division: establezco los limites de las piezas a explorar para este cursor y siguiente exploración (si aplica)
					if ((hasta > 1) & (cursor == Consts.POSICION_TASK_DIVISION + pos_multi_process_offset)) {
						setupMultiProcessesExploration();
					}
					
					// clean bits of this row
					colorRightExploredStrategy.cleanBorderColorCurrentRow(flagZona, cursor, tablero);
				}
				
				//###################################################################################################
				// Simulates the loop over the neighbors
				
				continueLoopNeighbors = false; // reset
				
				int desde = iter_desde[cursor];
				int hasta = iter_hasta[cursor];
				
				// Loop while selected neighbor cannot be placed as valid tile.
				// Exit the loop when we need to continue with next board position.
				while (true) {
					
					// did we exhaust all tiles from current neighbors?
					if (desde >= hasta) {
						int mergedInfo = tablero[cursor];
						// seteo el contorno como libre
						contorno.toggleContorno(false, cursor, flagZona, tablero, mergedInfo);
						short numero = Neighbors.numero(mergedInfo);
						usada[numero] = false; // pieza libre
						
						iter_desde[cursor] = 0;
						tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE; // tablero libre
						// restore multi processes variables
						if ((cursor >= Consts.POSICION_TASK_DIVISION) & (cursor <= Consts.POSICION_TASK_DIVISION + pos_multi_process_offset)) {
							num_processes = num_processes_orig[cursor - Consts.POSICION_TASK_DIVISION];
							if (pos_multi_process_offset > 0) {
								--pos_multi_process_offset;
							}
						}
						break main_loop; // exit neighbors while-loop to back track a position
					}
					
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
//					if (Consts.USE_FAIR_EXPERIMENT_GIF) {
//						if (CommonFuncs.testFairExperimentGif(flagZona, cursor, mergedInfo, tablero, usada)) {
//							++desde;
//							continue; // continúo con el siguiente neighbor
//						}
//					}
					
					// pregunto si el borde inferior que genero con la nueva pieza está siendo usado
//					@CONTORNO_INFERIOR
//					if (esContornoInferiorUsado(cursor, flagZona, contorno, tablero, mergedInfo)){
//						++desde;
//						continue; // continúo con el siguiente neighbor
//					}
					
					// seteo el contorno como usado
					contorno.toggleContorno(true, cursor, flagZona, tablero, mergedInfo);
					
					tablero[cursor] = mergedInfo;
					usada[numero] = true;
					iter_desde[cursor] = desde + 1;
					++count_cycles;
					++cursor;
					continue main_loop; // exit this while-loop and continue with next board position
				}
			}
			
			--cursor;
			
			if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
				// seteo el  contorno como libre
				contorno.toggleContorno(false, cursor, CommonFuncs.matrix_zonas[cursor], tablero, tablero[cursor]);
				--cursor;
				continue;
			}
			
			// asumo que estaba en el loop de neighbors -> limpio todo para continuar con next neighbor
			int mergedInfo = tablero[cursor];
			// seteo el contorno como libre
			contorno.toggleContorno(false, cursor, CommonFuncs.matrix_zonas[cursor], tablero, mergedInfo);
			short numero = Neighbors.numero(mergedInfo);
			usada[numero] = false; // pieza libre
//			tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE; // tablero libre
			// force to continue with loop of neighbors
			continueLoopNeighbors = true;
//			continue;
		}
	}

	/**
	 * A medida que voy haciendo el split del algoritmo, es necesario disminuir
	 * num_processes para dar con una correcta división de tareas.
	 */
	private void setupMultiProcessesExploration() {
		
		// NOTE: next conditions are such that they always assign work to processes, even when the task division is odd.
		
		// save the current value of num_processes, it might be changed
		num_processes_orig[cursor - Consts.POSICION_TASK_DIVISION] = num_processes;
		
		int hasta = iter_hasta[cursor];
		int desde;
		
		int thisProc = ID % num_processes;
		int comparison = num_processes - hasta;
		
		// caso 1: cada proc toma una única rama de Neighbors
		if (comparison == 0) {
			desde = thisProc;
			hasta = thisProc + 1;
		}
		// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas.
		else if (comparison < 0) {
			int span = (hasta + (num_processes - 1)) / num_processes;
			desde = thisProc * span;
			hasta = Math.min(hasta, (thisProc * span) + span);
		}
		// caso 3: existen mas procs que neighbors a explorar, entonces hay que distribuir los procs y
		// aumentar el pos_multi_process_offset en uno asi el siguiente nivel tmb continua la división.
		// Also num_processes changes, so next exploration level uses correct process ID.
		else {
			num_processes = hasta;
			desde = thisProc % hasta;
			hasta = desde + 1;
			++pos_multi_process_offset;
		}
		
		// System.out.println("Rank " + THIS_PROCESS + ":::: Total " + nbs.mergedInfo.length + ". Limites " + desde + "," + hasta);
		iter_desde[cursor] = desde;
		iter_hasta[cursor] = hasta;
	}

}