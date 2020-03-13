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

package org.fabri1983.eternity2.mpje;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.CommonFuncs;
import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.neighbors.MultiDimensionalStrategy;
import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.Neighbors;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;
import org.fabri1983.eternity2.ui.EternityII;
import org.fabri1983.eternity2.ui.ViewEternityFactory;
import org.fabri1983.eternity2.ui.ViewEternityMPJEFactory;

public final class SolverFasterMPJE {
	
	private static EternityII tableboardE2 = null; // instancia del tablero gráfico que se muestra en pantalla
	
	private static int num_processes = mpi.MPI.COMM_WORLD.Size(); // número de procesos
	public final static int ID = mpi.MPI.COMM_WORLD.Rank(); // id de proceso actual (0 base)
	private static int TAG_SINCRO = 1; // tags para identificar mensajes interprocesos
	private static int MESSAGE_HALT = 0, MESSAGE_SINCRO = 2; // mensajes para comunicar una acción o estado
	private static int[] mpi_send_info = new int[1]; // arreglo de envío de mensajes entre procesos
	private static mpi.Request mpi_requests[] = new mpi.Request[mpi.MPI.COMM_WORLD.Size()]; // arreglo para almacenar los requests que devuelven los Isend
	private static boolean sincronizar; // indica si se deden sincronizar los procesos antes de comenzar
	private static int[] num_processes_orig = new int[Consts.MAX_PIEZAS - Consts.POSICION_MULTI_PROCESSES + 1];
	private static int pos_multi_process_offset = 0; // usado con POSICION_MULTI_PROCESSES sirve para continuar haciendo los calculos de distribución de exploración
	
	private static long MAX_CICLOS; // Número máximo de ciclos para imprimir stats
	private static boolean SAVE_STATUS_ON_MAX_CYCLES;
	private static short DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
//	private static short LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia

	private final static String NAME_FILE_STATUS = "status/status_saved_P" + ID + Consts.FILE_EXT;
	private final static String NAME_FILE_PARCIAL = "status/parcial_P" + ID + Consts.FILE_EXT;
	private final static String NAME_FILE_DISPOSICION = "solution/disposiciones_P" + ID + Consts.FILE_EXT;
	private final static String NAME_FILE_SOLUCION = "solution/soluciones_P" + ID + Consts.FILE_EXT;
	
	public static short LIMITE_RESULTADO_PARCIAL = 211; // posición por defecto
	
	public static short cursor;
	public static short cur_destino;
	
	public static long count_cycles; // count cycles for this only instance
	
	public final static Pieza[] piezas = new Pieza[Consts.MAX_PIEZAS];
	
	public final static int[] tablero = new int[Consts.MAX_PIEZAS];
	public final static boolean[] usada = new boolean[Consts.MAX_PIEZAS];
	private final static Contorno contorno = new Contorno();
	
	private final static int[] iter_desde = new int[Consts.MAX_PIEZAS];
	
	private final static NeighborStrategy neighborStrategy = new MultiDimensionalStrategy();
	
	private final static ColorRightExploredStrategy colorRightExploredStrategy = null;//new ColorRightExploredLocalStrategy();
	
	private static boolean flag_retroceder_externo;
	private static boolean status_cargado; // inidica si se ha cargado estado inicial
	
	private static long time_inicial; //sirven para calcular el tiempo al hito de posición lejana
	private static long time_max_ciclos; //usado para calcular el tiempo entre diferentes status saved

	/**
	 * @param m_ciclos: número máximo de ciclos para imprimir stats.
	 * @param save_status_on_max_cycles: guardar status cuando se alcanza MAX_CICLOS.
	 * @param lim_max_par: posición en tablero minima para guardar estado parcial máximo.
	 * @param lim_exploracion: indica hasta qué posición debe explorar esta instancia.
	 * @param destino_ret: posición en tablero hasta la cual se debe retroceder.
	 * @param usar_tableboard: indica si se mostrará el tablero gráfico.
	 * @param usar_multiples_boards: true para mostrar múltiples tableboards (1 per solver)
	 * @param cell_pixels_lado: numero de pixeles para el lado de cada pieza dibujada.
	 * @param p_refresh_millis: cada cuántos milisecs se refresca el tablero gráfico.
	 * @param reader: implementation of the tiles file reader.
	 * @param totalProcesses: total number of processes.
	 */
	public SolverFasterMPJE(long m_ciclos, boolean save_status_on_max_cycles, short lim_max_par, short lim_exploracion,
			short destino_ret, boolean usar_tableboard, boolean usar_multiples_boards, int cell_pixels_lado,
			int p_refresh_millis, int totalProcesses) {

		MAX_CICLOS = m_ciclos;
		SAVE_STATUS_ON_MAX_CYCLES = save_status_on_max_cycles;
		
		int procMultipleBoards = 0; // por default solo el primer proceso muestra el tableboard
		// si se quiere mostrar multiple tableboards entonces hacer que el target proc sea este mismo proceso 
		if (usar_multiples_boards)
			procMultipleBoards = ID;
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (Consts.MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL = lim_max_par;
		
//		LIMITE_DE_EXPLORACION = lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia
		
		if (destino_ret >= 0){
			DESTINO_RET = destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo= true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
		cur_destino = Consts.CURSOR_INVALIDO; //variable para indicar hasta que posicion debo retroceder
		
		if (usar_tableboard && !flag_retroceder_externo && ID == procMultipleBoards) {
			ViewEternityFactory viewFactory = new ViewEternityMPJEFactory(Consts.LADO, cell_pixels_lado, 
					Consts.MAX_COLORES, (long)p_refresh_millis, ID, totalProcesses);
			tableboardE2 = new EternityII(viewFactory);
		}

		createDirs();
	}
	
	private final static void createDirs() {
		new File("solution").mkdirs();
		new File("status").mkdirs();
	}

	/**
	 * Carga el ultimo estado de exploración guardado. Si no existe tal estado
	 * inicializa estructuras y variables para que la exploracion comienze 
	 * desde cero.
	 */
	private final static boolean cargarEstado (String n_file)
	{
		BufferedReader reader = null;
		boolean status_cargado = false;
		
		try{
			File f = new File(n_file);
			if (!f.isFile()) {
				System.out.println(ID + " >>> Estado de exploracion no existe.");
				return status_cargado;
			}
			
			reader= new BufferedReader(new FileReader(f));
			String linea= reader.readLine();
			
			if (linea==null) {
				throw new Exception("First line is null.");
			}
			else{
				int sep,sep_ant;
				
				// contiene el valor de resultado_parcial_count
				LIMITE_RESULTADO_PARCIAL = Short.parseShort(linea);
				
				// contiene la posición del cursor en el momento de guardar estado
				linea= reader.readLine();
				cursor= Short.parseShort(linea);
				
				// recorro la info que estaba en tablero
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (short k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					int mergedInfo= Integer.parseInt(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					tablero[k]= mergedInfo;
					if (mergedInfo != 0)
						usada[Neighbors.numero(mergedInfo)] = true;
				}
				
				// recorro los valores de iter_desde[]
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (short k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					int index= Integer.parseInt(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					iter_desde[k] = index;
				}
				
				// la siguiente línea indica si se estaba usando poda de color explorado
				linea= reader.readLine();
				// recorro los valores de matrix_color_explorado[]
				if (colorRightExploredStrategy != null){
					if (Boolean.parseBoolean(linea)){
						// leo la info de matriz_color_explorado
						linea= reader.readLine();
						sep=0; sep_ant=0;
						for (short k=0; k < Consts.LADO; ++k){
							if (k==(Consts.LADO-1))
								sep= linea.length();
							else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
							int val= Integer.parseInt(linea.substring(sep_ant,sep));
							sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
							colorRightExploredStrategy.set(k, val);
						}
					}
				}
				
				status_cargado=true;
				sincronizar = false;
				
				System.out.println(ID + " >>> estado de exploracion (" + n_file + ") cargado.");
			}
		}
		catch(Exception e){
			System.out.println(ID + " >>> estado de exploracion no existe o esta corrupto: " + e.getMessage());
		}
		finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {}
		}
		
		return status_cargado;
	}

	/**
	 * Si el programa es llamado con el argumento {@link SolverFasterMPJE#flag_retroceder_externo} en true, entonces
	 * debo volver la exploracion hasta cierta posicion y guardar estado. No explora.
	 */
	private final static void retrocederEstado () {
		
		boolean retroceder = true;
		cur_destino = DESTINO_RET;
		
		while (cursor>=0) {
			
			if (!retroceder){
				CommonFuncs.guardarEstado(NAME_FILE_STATUS, ID, tablero, cursor, LIMITE_RESULTADO_PARCIAL, iter_desde,
						neighborStrategy, colorRightExploredStrategy);
				CommonFuncs.guardarResultadoParcial(ID, tablero, NAME_FILE_PARCIAL);
				System.out.println(ID + " >>> Exploracion retrocedio a la posicion " + cursor + ". Estado salvado.");
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			
			--cursor;
			
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (cursor < 0)
				break; //obliga a salir del while
			
			if (cursor != Consts.PIEZA_CENTRAL_POS_TABLERO) {
				int mergedInfo = tablero[cursor];
				int numero = Neighbors.numero(mergedInfo);
				// la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				usada[numero] = false;
				tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
			}
			
			//si retrocedió hasta el cursor destino, entonces no retrocedo mas
			if ((cursor+1) <= cur_destino){
				retroceder= false;
				cur_destino= Consts.CURSOR_INVALIDO;
			}
			
			//si está activado el flag para retroceder niveles de exploración entonces debo limpiar algunas cosas
			if (retroceder)
				iter_desde[cursor]= 0; //la exploración de posibles piezas para la posicion cursor debe empezar desde la primer pieza
		}
	}
	
	public final void setupInicial (ReaderForFile readerForTilesFile) {
		
		CommonFuncs.inicializarMatrixZonas();
		
		CommonFuncs.inicializarZonaProcesoContornos();
		
		CommonFuncs.inicializarZonaReadContornos();
		
		CommonFuncs.cargarPiezas(ID, piezas, readerForTilesFile);
		
		CommonFuncs.verificarTiposDePieza(ID, piezas);
		
		CommonFuncs.cargarSuperEstructura(ID, piezas, neighborStrategy);
		
		status_cargado = cargarEstado(NAME_FILE_STATUS);
		if (!status_cargado) {
			sincronizar = true;
		}
		
		CommonFuncs.ponerPiezasFijasEnTablero(ID, piezas, tablero, usada);
		
		Contorno.inicializarContornos(contorno, tablero, Consts.MAX_PIEZAS, Consts.LADO);
		
		if (tableboardE2 != null) 
			tableboardE2.startPainting();
	}

	public final void atacar()
	{
		if (flag_retroceder_externo) {
			retrocederEstado();
			return;
		}
		
		// sincronización de los procesos (Knock Knock) una única vez
		if (sincronizar) {
			sincronizar = false; // no volver a sincronizar
			knocKnock();
		}
		
		long nowNanos = System.nanoTime();
		time_inicial = nowNanos;
		time_max_ciclos = nowNanos;
		
		// si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado) {
			explorar(0);
		}
		// se cargó estado de exploración, voy a simular pop del stack que cargué
		else {
			while (cursor >= 0) {
				
				explorar(iter_desde[cursor]);
				--cursor;
				
				// si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				// seteo el contorno como libre
				CommonFuncs.toggleContorno(false, cursor, CommonFuncs.matrix_zonas[cursor], contorno, tablero, tablero[cursor]);

				// debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != Consts.PIEZA_CENTRAL_POS_TABLERO) {
					int mergedInfo = tablero[cursor];
					int numero = Neighbors.numero(mergedInfo);
					usada[numero] = false;
					tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
				}
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println(ID + " >>> exploración agotada.");
	}
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final void explorar(int desde)
	{
		// si cursor se pasa del limite de piezas, significa que estoy en una solucion
		if (cursor == Consts.MAX_PIEZAS) {
			CommonFuncs.guardarSolucion(ID, tablero, NAME_FILE_SOLUCION, NAME_FILE_DISPOSICION);
			System.out.println(ID + " >>> Solucion Encontrada!!");
			return; // evito que la instancia de exploracion continue
		}
		
		// si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor >= LIMITE_RESULTADO_PARCIAL) {
			++LIMITE_RESULTADO_PARCIAL;
			CommonFuncs.maxLejanoParcialReached(ID, cursor, time_inicial, tablero, NAME_FILE_PARCIAL);
		}
		
		// si llegué a MAX_CICLOS de ejecucion, guardo el estado de exploración
		if (count_cycles >= MAX_CICLOS) {
			maxCyclesReached();
		}
		
		byte flagZona = CommonFuncs.matrix_zonas[cursor];
		
		// si la posicion cursor es una posicion fija no tengo que hacer la exploracion "standard"
		if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
			// seteo el contorno como usado
			CommonFuncs.toggleContorno(true, cursor, flagZona, contorno, tablero, tablero[cursor]);
			// at this point we have set all things up related to a fixed tile, so continue normally with next board position
			++cursor;
			explorar(0);
			--cursor;
			// seteo el contorno como libre
			CommonFuncs.toggleContorno(false, cursor, flagZona, contorno, tablero, tablero[cursor]);
			return;
		}

		// can be null when there is no neighbors and when cursor == Consts.PIEZA_CENTRAL_POS_TABLERO
		Neighbors nbs = CommonFuncs.neighbors(flagZona, cursor, tablero, neighborStrategy);
		
		// if no neighbors then backtrack
		if (nbs == null)
			return;
		
		// pregunto si el contorno superior de las posiciones subsecuentes generan un contorno ya usado
		if (CommonFuncs.esContornoSuperiorUsado(cursor, flagZona, contorno, tablero)) {
			return;
		}
		
		int length_nbs = nbs.mergedInfo.length;
		
		// En modo multiproceso tengo que establecer los limites de las piezas a explorar para este proceso y futuras divisiones.
		if (cursor == Consts.POSICION_MULTI_PROCESSES + pos_multi_process_offset) {
			
			// save the current value of num_processes, it might be changed
			num_processes_orig[cursor - Consts.POSICION_MULTI_PROCESSES] = num_processes;
			
			int this_proc_absolute = ID % num_processes;
			desde = this_proc_absolute;
			
			// caso 1: cada proc toma una única rama de Neighbors
			if (num_processes == length_nbs) {
				length_nbs = this_proc_absolute + 1;
			}
			// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas
			else if (num_processes < length_nbs) {
				int span = (length_nbs + 1) / num_processes;
				desde *= span;
				if (desde >= length_nbs)
					desde = length_nbs - 1;
				else if (desde + span < length_nbs)
					length_nbs = desde + span;
			}
			// caso 3: existen mas procs que piezas a explorar, entonces hay que distribuir los procs y
			// aumentar el POSICION_MULTI_PROCESSES en uno asi el siguiente nivel tmb continua la división.
			// Ahora la cantidad de procs se setea igual a length_nbs.
			else {
				++pos_multi_process_offset;
				int divisor = (num_processes + 1) / length_nbs; // reparte los procs por posible pieza
				num_processes = length_nbs;
				desde /= divisor;
				if (desde >= length_nbs)
					desde = length_nbs - 1;
				length_nbs = desde + 1;
			}
			
			// System.out.println("Rank " + ID + ":::: Total " + nbs.mergedInfo.length + ". Limites " + desde + "," + length_nbs);
		}
		
		while (desde < length_nbs) {
			
			int mergedInfo = nbs.mergedInfo[desde];
			short numero = Neighbors.numero(mergedInfo);
			
			if (usada[numero]) {
				++desde;
				continue; // continúo con el siguiente neighbor
			}
	
			++count_cycles;
				
			// pregunto si está activada la poda del color right explorado en borde left
//			if (colorRightExploredStrategy != null) {
//				if (CommonFuncs.testPodaColorRightExplorado(flagZona, cursor, Neighbors.right(mergedInfo),
//						colorRightExploredStrategy)) {
//					++desde;
//					continue; // continúo con el siguiente neighbor
//				}
//			}
			
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
			CommonFuncs.toggleContorno(true, cursor, flagZona, contorno, tablero, mergedInfo);
			
			tablero[cursor] = mergedInfo;
			usada[numero] = true;
			iter_desde[cursor] = desde + 1;
			
			++cursor;
			explorar(0);
			--cursor;
			
			usada[numero] = false;
//			tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
			
			// seteo el contorno como libre
			CommonFuncs.toggleContorno(false, cursor, flagZona, contorno, tablero, mergedInfo);
			
			++desde;
		}
		
		iter_desde[cursor] = 0;
		tablero[cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
		
		if (cursor >= Consts.POSICION_MULTI_PROCESSES && cursor <= Consts.POSICION_MULTI_PROCESSES + pos_multi_process_offset) {
			restoreMultiProcessesExploration();
		}
	}
	
	private static final void restoreMultiProcessesExploration() {
		num_processes = num_processes_orig[cursor - Consts.POSICION_MULTI_PROCESSES];
		// restore multi processes variables only when pos_multi_process_offset > 0, meaning that 
		// both pos_multi_process_offset and num_processes have been modified
		if (pos_multi_process_offset > 0) {
			--pos_multi_process_offset;
		}
	}
	
	private static final void maxCyclesReached() {
		long durationNanos = System.nanoTime() - time_max_ciclos;
		long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
		long piecesPerSec = count_cycles / TimeUnit.SECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
		
		System.out.println(ID + " >>> cursor " + cursor + ". Tiempo: " + durationMillis + " ms, " + piecesPerSec + " pieces/sec");

		count_cycles = 0;
		
		if (SAVE_STATUS_ON_MAX_CYCLES) {
			CommonFuncs.guardarEstado(NAME_FILE_STATUS, ID, tablero, cursor, LIMITE_RESULTADO_PARCIAL, iter_desde,
					neighborStrategy, colorRightExploredStrategy);
			CommonFuncs.guardarResultadoParcial(ID, tablero, NAME_FILE_PARCIAL);
		}
		
		time_max_ciclos = System.nanoTime();
	}
	
	private final static void knocKnock () {
		
		// only first process acts as the master
		if (ID == 0)
		{
			mpi_send_info[0] = MESSAGE_SINCRO;
			// sincronizo con los restantes procesos
			for (int rank=1; rank < num_processes; ++rank)
				mpi_requests[rank-1] = mpi.MPI.COMM_WORLD.Isend(mpi_send_info, 0, mpi_send_info.length, mpi.MPI.INT, rank, TAG_SINCRO); // el tag identifica al mensaje
			System.out.println("Rank 0: --- Sincronizando con todos. Sending msg " + mpi_send_info[0] + " ...");
			System.out.flush();
			// espero a que todas las peticiones se completen
			mpi.Request.Waitall(mpi_requests);
			System.out.println("Rank 0: --- Todos sincronizados.");
			System.out.flush();
		}
		// other pocesses act as slaves
		else
		{
			System.out.println(ID + " >>> --- Esperando sincronizarse...");
			System.out.flush();
			mpi_send_info[0] = MESSAGE_HALT;
			// espero recibir mensaje
			mpi.MPI.COMM_WORLD.Recv(mpi_send_info, 0, mpi_send_info.length, mpi.MPI.INT, mpi.MPI.ANY_SOURCE, TAG_SINCRO); // el tag identifica al mensaje
			if (mpi_send_info[0] == MESSAGE_SINCRO) {
				System.out.println(ID + " >>> --- Sincronizado with msg " + mpi_send_info[0]);
				System.out.flush();
			}
			else if (mpi_send_info[0] == MESSAGE_HALT)
				;
		}
		
		System.out.println(ID + " >>> --- Continua procesando.");
		System.out.flush();
		
		/*try {
			System.out.println("Waiting call...");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			;
		}
		System.exit(0);*/
	}
	
}
