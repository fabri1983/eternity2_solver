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
import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.neighbors.Neighbors;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public class ExplorationTask implements Runnable {

	private int num_processes;
	private final int[] num_processes_orig = new int[Consts.MAX_PIEZAS];
	private int pos_multi_process_offset = 0; // usado con POSICION_MULTI_PROCESSES sirve para continuar haciendo los calculos de distribución de exploración
	
	String statusFileName, parcialFileName, parcialMaxFileName, disposicionMaxFileName, solucFileName, dispFileName;
	
	public final int[] tablero = new int[Consts.MAX_PIEZAS];
	public final boolean[] usada = new boolean[Consts.MAX_PIEZAS];
	private final Contorno contorno = new Contorno();
	
	final byte[] desde_saved = new byte[Consts.MAX_PIEZAS];
	
	public short cursor, mas_bajo, mas_alto, mas_lejano_parcial_max;
	int sig_parcial = 1; // esta variable indica el numero de archivo parcial siguiente a guardar
	
	private long count_cycles;
	
	boolean retroceder; // indica si debo volver estados de backtracking
	private boolean status_cargado; // inidica si se ha cargado estado inicial
	boolean mas_bajo_activo; // permite o no modificar el cursor mas_bajo
	
	private long time_inicial; // sirve para calcular el tiempo al hito de posición lejana
	private long time_max_ciclos; //usado para calcular el tiempo entre diferentes status saved
	
	// identificador 0-based para identificar la action y para saber qué rama de la exploración tomar cuando esté en POSICION_MULTI_PROCESSES
	public final int id;
	
	private StringBuilder printBuffer = new StringBuilder(64);
	
	private CountDownLatch startSignal;
	
	public ExplorationTask(int _id, int _num_processes, CountDownLatch startSignal) {
		
		id = _id;
		num_processes = _num_processes;
		
		statusFileName = SolverFaster.NAME_FILE_STATUS + "_" + id + Consts.FILE_EXT;
		parcialFileName = SolverFaster.NAME_FILE_PARCIAL + "_" + id + Consts.FILE_EXT;
		parcialMaxFileName = SolverFaster.NAME_FILE_PARCIAL_MAX + "_" + id + Consts.FILE_EXT;
		disposicionMaxFileName = SolverFaster.NAME_FILE_DISPOSICIONES_MAX + "_" + id + Consts.FILE_EXT;
		solucFileName = SolverFaster.NAME_FILE_SOLUCION + "_" + id + Consts.FILE_EXT;
		dispFileName = SolverFaster.NAME_FILE_DISPOSICION + "_" + id + Consts.FILE_EXT;

		this.startSignal = startSignal;
	}

	public void setupInicial(ReaderForFile readerForTilesFile) {
		
		// Pruebo cargar el primer status_saved
		status_cargado = SolverFaster.cargarEstado(statusFileName, this);
		
		// cargo las posiciones fijas
		CommonFuncs.ponerPiezasFijasEnTablero(id, SolverFaster.piezas, tablero, usada);
		
		// seteo como usados los contornos ya existentes en tablero
		Contorno.inicializarContornos(contorno, tablero, Consts.MAX_PIEZAS, Consts.LADO);
	}

	public void resetForBenchmark(int _num_processes, CountDownLatch startSignal) {
		
		count_cycles = 0;
		SolverFaster.count_cycles[id] = 0;
		
		cursor = mas_bajo = mas_alto = mas_lejano_parcial_max = 0;
		sig_parcial = 1;
		pos_multi_process_offset = 0;
		num_processes = _num_processes;
		
		for (int k=0; k < num_processes_orig.length; ++k) {
			num_processes_orig[k] = 0;
		}
		
		for (int k=0; k < desde_saved.length; ++k) {
			desde_saved[k] = 0;
		}
		
		this.startSignal = startSignal;

		cleanTablero();

		cleanUsada();
		
		Contorno.resetContornos(contorno);
	}

	private void cleanTablero() {
		for (int k=0, c=tablero.length; k < c; ++k) {
			tablero[k] = -1;
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
			doWork();
		} catch (InterruptedException e) {
			System.out.println("ExplorationAction interrupted.");
		}
	}

	private void doWork() {
				
		if (SolverFaster.flag_retroceder_externo) {
			SolverFaster.retrocederEstado(this);
			return;
		}
		
		System.out.println(id + " >>> Buscando soluciones...");
		
		long nowNanos = System.nanoTime();
		time_inicial = nowNanos;
		time_max_ciclos = nowNanos;
		
		//si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado)
			explorar(0);
		//se carga estado de exploración, debo proveer la posibilidad de volver estados anteriores de exploracion
		else {
			//ahora exploro comunmente y proveo una especie de recursividad para retroceder estados
			while (cursor >= 0) {
				if (!retroceder) {
					// pregunto si llegué al limite de esta instancia de exploracion
//					if (cursor <= SolverFaster.LIMITE_DE_EXPLORACION) {
//						CommonFuncs.operarSituacionLimiteAlcanzado(statusFileName, id, );
//						return;
//					}
					//creo una nueva instancia de exploracion
					explorar(desde_saved[cursor]);
				}
				--cursor;
				
				//si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				//seteo los contornos como libres
				CommonFuncs.toggleContorno(false, cursor, CommonFuncs.matrix_zonas[cursor], contorno, tablero, tablero[cursor]);

				// debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != Consts.PIEZA_CENTRAL_POS_TABLERO) {
					int mergedInfo = tablero[cursor];
					int numero = Neighbors.numero(mergedInfo);
					usada[numero] = false;
					tablero[cursor] = -1;
				}
				
				// si retrocedí hasta el cursor destino, entonces no retrocedo mas
//				@RETROCEDER
//				if (cursor <= cur_destino){
//					retroceder = false;
//					cur_destino = Consts.CURSOR_INVALIDO;
//				}
//				//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
//				if (retroceder)
//					desde_saved[cursor] = 0; //la exploracion de posibles piezas para la posicion cursor debe empezar desde la primer pieza
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println(id + " >>> exploracion agotada.");
	}
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 * 
	 * @param desde Es la posición desde donde empiezo a tomar las piezas de Neighbors.
	 */
	private final void explorar(int desde)
	{
//		@FILAS_PRECALCULADAS
//		if (!combs_hechas && cursor >= POSICION_CALCULAR_FILAS){
//			//genero el arreglo de combinaciones de filas
//			calcularFilasDePiezas();
//			combs_hechas = true;
//		}
//		else if (cursor < POSICION_CALCULAR_FILAS)
//			combs_hechas = false; //seteando en false obligo a que se recalculen las filas
			
		//#############################################################################################
		/**
		 * Cabeza de exploración.
		 * Representa las primeras sentencias del backtracking de exploracion. Pregunta
		 * algunas cositas antes de empezar una nueva instancia de exploracion.
		 */
		//#############################################################################################
		
		//si cursor se pasa del limite de piezas, significa que estoy en una solucion
		if (cursor >= Consts.MAX_PIEZAS) {
			CommonFuncs.guardarSolucion(id, tablero, solucFileName, dispFileName);
			System.out.println(id + " >>> Solucion Encontrada!!");
			return; // evito que la instancia de exploracion continue
		}
		
		//si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor > mas_lejano_parcial_max) {
			mas_lejano_parcial_max = cursor;
			if (cursor >= SolverFaster.LIMITE_RESULTADO_PARCIAL) {
				long time_final = System.nanoTime();
				printBuffer.setLength(0);
				printBuffer.append(id).append(" >>> ")
					.append(TimeUnit.MILLISECONDS.convert(time_final - time_inicial, TimeUnit.NANOSECONDS))
					.append(" ms, cursor ").append(cursor);
				System.out.println(printBuffer.toString());
				printBuffer.setLength(0);
				sig_parcial = CommonFuncs.guardarResultadoParcial(true, id, tablero, sig_parcial,
						SolverFaster.MAX_NUM_PARCIAL, parcialFileName, parcialMaxFileName, disposicionMaxFileName);
			}
		}
		
		//voy manteniendo el cursor mas alto para esta vuelta de ciclos
		if (cursor > mas_alto)
			mas_alto = cursor;
		//si cursor se encuentra en una posicion mas baja que la posicion mas baja alcanzada guardo ese valor
		if (cursor < mas_bajo)
			mas_bajo = cursor;
		//la siguiente condición se cumple una sola vez
		if (cursor > 100 && !mas_bajo_activo) {
			mas_bajo = Consts.MAX_PIEZAS;
			mas_bajo_activo = true;
		}
		
		//si llegué a MAX_CICLOS de ejecucion, guardo el estado de exploración
		if (count_cycles >= SolverFaster.MAX_CICLOS) {
			//calculo el tiempo transcurrido desd el último time_status_saved 
			long nanoTimeNow = System.nanoTime();
			long durationNanos = nanoTimeNow - time_max_ciclos;
			long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
			long piecesPerSec = count_cycles / TimeUnit.SECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
			
			count_cycles = 0;
			if (SolverFaster.usarTableroGrafico)
				SolverFaster.count_cycles[id] = 0;
			
			if (SolverFaster.SAVE_STATUS_ON_MAX_CYCLES) {
				CommonFuncs.guardarEstado(statusFileName, id, tablero, cursor, mas_bajo, mas_alto, mas_lejano_parcial_max,
						desde_saved, SolverFaster.neighborStrategy, SolverFaster.colorRightExploredStrategy);
				sig_parcial = CommonFuncs.guardarResultadoParcial(false, id, tablero, sig_parcial,
						SolverFaster.MAX_NUM_PARCIAL, parcialFileName, parcialMaxFileName, disposicionMaxFileName);
			}
			
			printBuffer.setLength(0);
			printBuffer.append(id).append(" >>> cursor ").append(cursor)
					.append(". Pos Min ").append(mas_bajo).append(", Pos Max ").append(mas_alto)
					.append(". Tiempo: ").append(durationMillis).append(" ms") 
					.append(", ").append(piecesPerSec).append(" pieces/sec");
			System.out.println(printBuffer.toString());
			printBuffer.setLength(0);
			
			time_max_ciclos = nanoTimeNow;
			
			//cuando se cumple el ciclo aumento de nuevo el valor de mas_bajo y disminuyo el de mas_alto
			mas_bajo = Consts.MAX_PIEZAS;
			mas_alto = 0;
		}
		//#############################################################################################
		
		
		//#############################################################################################
		/**
		 * Explorar pieza fija.
		 * Ya me encuentro en una posicion fija, entonces salteo esta posici�n y continuo.
		 * NOTA: por ahora solo se contempla la posicion 135 (136 real) como fija y no se permte rotarla.
		 */
		//#############################################################################################
		
		// Si la posicion cursor es una posicion fija no tengo que hacer la exploracion "estandar". 
		// Se supone que la pieza fija ya está debidamente colocada.
		if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
			
			int mergedActual = tablero[cursor];
			byte flagZona = CommonFuncs.matrix_zonas[cursor];
			
			//seteo los contornos como usados
			CommonFuncs.toggleContorno(true, cursor, flagZona, contorno, tablero, mergedActual);
			
			++cursor;
			explorar(0);
			--cursor;
			
			//seteo los contornoscomo libres
			CommonFuncs.toggleContorno(false, cursor, flagZona, contorno, tablero, mergedActual);
			
//			@RETROCEDER
//			if (cursor <= cur_destino){
//				retroceder= false;
//				cur_destino= Consts.CURSOR_INVALIDO;
//			}
			return;
		}
		//#############################################################################################
		
		
		//#############################################################################################
		/**
		 * Explorar otras consideraciones.
		 * Antes de comenzar a explorar me fijo algunas otras cositas.
		 */
		//#############################################################################################
		
		//pregunto si el contorno superior de las posiciones subsecuentes generan un contorno ya usado
		if (CommonFuncs.esContornoSuperiorUsado(cursor, CommonFuncs.matrix_zonas[cursor], contorno, tablero))
			return;

		//pregunto si estoy en una posicion donde puedo preguntar por filas libres y/o cargar fila
//		@FILAS_PRECALCULADAS
//		if (zonas_cargar_fila[cursor]){
//			//cargo fila precalculadas
//			if (cargarFilasGuardadas() == false)
//				return;
//		}
		
		//#############################################################################################
		
		
		//#############################################################################################
		
		//ahora hago la exploracion
		exploracionStandard(desde);
		
		//#############################################################################################
	}
	
	/**
	 * Realiza toda la exploracion standard: cicla sobre las posibles piezas para las
	 * posicon actual de cursor, y cicla sobre las posibles rotaciones de cada pieza.
	 * Aplica varias podas que solamente son validas en este nivel de exploracion.
	 * 
	 * @param desde Es la posición desde donde empiezo a tomar las piezas de Neighbors.
	 */
	private final void exploracionStandard(int desde)
	{
		byte flagZona = CommonFuncs.matrix_zonas[cursor];
		
		// voy a recorrer las posibles piezas que coinciden con los colores de las piezas alrededor de cursor
		Neighbors nbs = CommonFuncs.neighbors(flagZona, cursor, tablero, SolverFaster.neighborStrategy);
		if (nbs == null)
			return; // significa que no existen posibles piezas para la actual posicion de cursor y los colores vecinos

		int length_nbs = nbs.mergedInfo.length;
		
		num_processes_orig[cursor] = num_processes;

		// En modo multiproceso tengo que establecer los limites de las piezas a explorar para este proceso.
		// En este paso solo inicializo algunas variables para futuros cálculos.
		if (cursor == SolverFaster.POSICION_MULTI_PROCESSES + pos_multi_process_offset) {
			// en ciertas condiciones cuado se disminuye el num de procs, es necesario acomodar el concepto de this_proc para los calculos siguientes.
			int this_proc_absolute = id % num_processes;

			// caso 1: trivial. Cada proc toma una única rama de Neighbors
			if (num_processes == length_nbs) {
				desde = this_proc_absolute;
				length_nbs = this_proc_absolute + 1;
			}
			// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas
			else if (num_processes < length_nbs) {
				int span = (length_nbs + 1) / num_processes;
				desde = this_proc_absolute * span;
				if (desde >= length_nbs)
					desde = length_nbs - 1;
				else if (desde + span < length_nbs)
					length_nbs = desde + span;
			}
			// caso 3: existen mas procs que piezas a explorar, entonces hay que distribuir los procs y
			// aumentar el POSICION_MULTI_PROCESSES en uno asi el siguiente nivel tmb se continua la división.
			// Ahora la cantidad de procs se setea igual a length_nbs
			else {
				int divisor = (num_processes + 1) / length_nbs; // reparte los procs por posible pieza
				num_processes = length_nbs;
				desde = this_proc_absolute / divisor;
				if (desde >= length_nbs)
					desde = length_nbs - 1;
				length_nbs = desde + 1;
				++pos_multi_process_offset;
			}
			// System.out.println("Rank " + THIS_PROCESS + ":::: Total " + nbs.mergedInfo.length + ". Limites " + desde + "," + length_nbs);
		}
		
		for (; desde < length_nbs; ++desde) {
			
			// desde_saved[cursor]= desde; //actualizo la posicion en la que leo de neighbors
			int merged = nbs.mergedInfo[desde];
			int numero = Neighbors.numero(merged);
			
			// pregunto si la pieza candidata está siendo usada
			if (usada[numero])
				continue; //es usada, pruebo con la siguiente pieza/rotación
	
			++count_cycles;
			if (SolverFaster.usarTableroGrafico)
				++SolverFaster.count_cycles[id]; //incremento el contador de combinaciones de piezas
				
			// pregunto si está activada la poda del color right explorado en borde left
			if (SolverFaster.colorRightExploredStrategy != null) {
				if (CommonFuncs.testPodaColorRightExplorado(flagZona, cursor, Neighbors.right(merged), SolverFaster.colorRightExploredStrategy))
					continue;
			}
			
			//#### En este punto ya tengo la pieza correcta para poner en tablero[cursor] ####
			
			tablero[cursor] = merged;
			usada[numero] = true;
			
			//#### En este punto ya tengo la pieza colocada y rotada correctamente ####
			
			// una vez rotada adecuadamente la pieza pregunto si el borde inferior que genera está siendo usado
//			@CONTORNO_INFERIOR
//			if (esContornoInferiorUsado()){
//				p.usada = false; //la pieza ahora no es usada
//				continue;
//			}
			
			//FairExperiment.gif: color bottom repetido en sentido horizontal
			if (SolverFaster.FairExperimentGif) {
				if (CommonFuncs.testFairExperimentGif(flagZona, cursor, merged, tablero, usada))
					continue;
			}
	
			// seteo el contorno como usados
			CommonFuncs.toggleContorno(true, cursor, flagZona, contorno, tablero, merged);
				
			//##########################
			// Llamo una nueva instancia de epxloracion
			++cursor;
			explorar(0);
			--cursor;
			//##########################
				
			// seteo el contorno como libres
			CommonFuncs.toggleContorno(false, cursor, flagZona, contorno, tablero, merged);
			
			usada[numero] = false;
			
			// si retrocedí hasta la posicion destino, seteo la variable retroceder en false e invalído a cur_destino
//			@RETROCEDER
//			if (cursor <= cur_destino){
//				retroceder= false;
//				cur_destino= Consts.CURSOR_INVALIDO;
//			}
//			// caso contrario significa que todavia tengo que seguir retrocediendo
//			if (retroceder)
//				break;
		}
		
		desde_saved[cursor] = 0; //debo poner que el desde inicial para este cursor sea 0
		tablero[cursor] = -1; //dejo esta posicion de tablero libre

		// restore multi process variables
		num_processes = num_processes_orig[cursor];
		if (pos_multi_process_offset > 0)
			--pos_multi_process_offset;
	}

}