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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.fabri1983.eternity2.core.CommonFuncs;
import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.SuperMatrizHashFunctionStrategy;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredAtomicStrategy;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public final class SolverFaster {
	
	static int POSICION_MULTI_PROCESSES = -1; //(99) posición del tablero en la que se aplica fork/join
	static int NUM_PROCESSES = 1;
	static ExploracionAction actions[];
	static CountDownLatch startSignal;
    
	static long MAX_CICLOS; // Número máximo de ciclos para guardar estado
	static int DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
	static int MAX_NUM_PARCIAL; // Número de archivos parciales que se generarón
	public static int LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia
	
	final static String NAME_FILE_SOLUCION = "solution/soluciones";
	final static String NAME_FILE_DISPOSICION = "solution/disposiciones";
	final static String NAME_FILE_STATUS = "status/status_saved";
	final static String NAME_FILE_PARCIAL_MAX = "status/parcialMAX";
	final static String NAME_FILE_DISPOSICIONES_MAX = "status/disposicionMAX";
	final static String NAME_FILE_PARCIAL = "status/parcial";
	final static String NAME_FILE_LIBRES_MAX = "status/libresMAX";
	
	static int LIMITE_RESULTADO_PARCIAL = 211; // por defecto
	
	public static long count_cycles[]; // count cycles per task when usarTableroGrafico is true
	
	final static NeighborStrategy neighborStrategy = new SuperMatrizHashFunctionStrategy();
	
	static ColorRightExploredStrategy colorRightExploredStrategy;
	
	static boolean retroceder, FairExperimentGif, usarTableroGrafico;
	static int cellPixelsLado, tableboardRefreshMillis;
	static boolean flag_retroceder_externo;
	
	private SolverFaster() {
	}
	
	/**
	 * @param m_ciclos: número máximo de ciclos para disparar guardar estado.
	 * @param lim_max_par: posición en tablero minima para guardar estado parcial máximo.
	 * @param lim_exploracion: indica hasta qué posición debe explorar esta instancia.
	 * @param max_parciales: indica hasta cuantos archivos de estado parcial voy a tener.
	 * @param destino_ret: posición en tablero hasta la cual se debe retroceder.
	 * @param usar_tableboard: indica si se mostrará el tablero gráfico.
	 * @param usar_multiples_boards: true para mostrar múltiples tableboards (1 per solver)
	 * @param cell_pixels_lado: numero de pixeles para el lado de cada pieza dibujada.
	 * @param p_refresh_millis: cada cuántos milisecs se refresca el tablero gráfico.
	 * @param p_fair_experiment_gif: dice si se implementa la poda de FairExperiment.gif.
	 * @param p_poda_color_right_explorado: poda donde solamente se permite explorar una sola vez el color right de la pieza en borde left.
	 * @param p_pos_multi_processes: posición en tablero donde inicia exploración multi threading.
	 * @param reader: implementation of the tiles file reader.
	 * @param totalProcesses: parallelism level for the fork-join pool.
	 */
	public static SolverFaster build(long m_ciclos, int lim_max_par, int lim_exploracion, int max_parciales, int destino_ret, 
			boolean usar_tableboard, boolean usar_multiples_boards, int cell_pixels_lado, int p_refresh_millis, 
			boolean p_fair_experiment_gif, boolean p_poda_color_right_explorado, int p_pos_multi_processes,
			int totalProcesses) {
		
		MAX_CICLOS = m_ciclos;
		
		POSICION_MULTI_PROCESSES = p_pos_multi_processes;
		NUM_PROCESSES = Math.min(Runtime.getRuntime().availableProcessors(), totalProcesses);
		
		// no tiene sentido usar varios threads si no se seteó correctamente la posición multi threading
		if (POSICION_MULTI_PROCESSES < 0)
			NUM_PROCESSES = 1;

		// cycles counter per task
		count_cycles = new long[NUM_PROCESSES];
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (Consts.MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL = lim_max_par;
		
		LIMITE_DE_EXPLORACION = lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia 
		
		FairExperimentGif = p_fair_experiment_gif;

		retroceder= false; //variable para indicar que debo volver estados de backtracking
		if (destino_ret >= 0) {
			DESTINO_RET = destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo = true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
		//indica si se usará la poda de colores right explorados en borde left
		if (p_poda_color_right_explorado)
			colorRightExploredStrategy = new ColorRightExploredAtomicStrategy();
		
		MAX_NUM_PARCIAL = max_parciales; //indica hasta cuantos archivos parcial.txt voy a tener
		
		usarTableroGrafico = usar_tableboard;
		cellPixelsLado = cell_pixels_lado;
		tableboardRefreshMillis = p_refresh_millis;

		createDirs();
		
		return new SolverFaster();
	}

	private final static void createDirs() {
		new File("solution").mkdirs();
		new File("status").mkdirs();
	}

	/**
	 * Carga el ultimo estado de exploración guardado para la action pasada como parámetro. 
	 * Si no existe tal estado* inicializa estructuras y variables para que la exploracion comienze desde cero.
	 */
	final static boolean cargarEstado(String n_file, ExploracionAction action)
	{
		BufferedReader reader = null;
		boolean status_cargado = false;
		
		try{
			File f = new File(n_file);
			if (!f.isFile()) {
				System.out.println(action.id + " >>> estado de exploracion no existe.");
				return status_cargado;
			}
			
			reader = new BufferedReader(new FileReader(f));
			String linea = reader.readLine();
			
			if (linea==null) {
				throw new Exception(action.id + " >>> First line is null.");
			}
			else{
				int sep,sep_ant;
				
				// contiene el valor de cursor mas bajo alcanzado en una vuelta de ciclo
				action.mas_bajo= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas alto alcanzado en una vuelta de ciclo
				linea= reader.readLine();
				action.mas_alto= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas lejano parcial alcanzado (aquel que graba parcial max)
				linea= reader.readLine();
				action.mas_lejano_parcial_max= Integer.parseInt(linea);
				
				// contiene la posición del cursor en el momento de guardar estado
				linea= reader.readLine();
				action.cursor= Integer.parseInt(linea);
				
				// recorro los numeros de piezas que estaban en tablero
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					action.tablero[k]= numPieza == -1 ? null : action.piezas[numPieza];
				}
				
				// recorro los valores de desde_saved[]
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					action.desde_saved[k] = numPieza;
				}
				
				// la siguiente línea indica si se estaba usando poda de color explorado
				linea= reader.readLine();
				// recorro los valores de matrix_color_explorado[]
				if (colorRightExploredStrategy != null){
					if (Boolean.parseBoolean(linea)){
						//leo la info de matriz_color_explorado
						linea= reader.readLine();
						sep=0; sep_ant=0;
						for (int k=0; k < Consts.LADO; ++k){
							if (k==(Consts.LADO-1))
								sep= linea.length();
							else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
							int val= Integer.parseInt(linea.substring(sep_ant,sep));
							sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
							colorRightExploredStrategy.set(k, val);
						}
					}
				}
				
				//las restantes MAX_PIEZAS lineas contienen el valor de rotación y usada de cada pieza
				int pos=0; //cuento cuantas lineas voy procesando
				String splitted[];
				linea= reader.readLine(); //info de la primer pieza
				while ((linea != null) && (pos < Consts.MAX_PIEZAS)){
					splitted = linea.split(Consts.SECCIONES_SEPARATOR_EN_FILE);
					Pieza.llevarArotacion(action.piezas[pos], Byte.parseByte(splitted[0]));
					action.piezas[pos].usada = Boolean.parseBoolean(splitted[1]);
					linea= reader.readLine();
					++pos;
				}
				if (pos != Consts.MAX_PIEZAS){
					System.out.println(action.id + " >>> ERROR. La cantidad de piezas en el archivo " + n_file + " no coincide con el numero de piezas que el juego espera.");
					throw new Exception("Inconsistent number of pieces.");
				}
				
				status_cargado=true;
				
				System.out.println(action.id + " >>> estado de exploracion (" + n_file + ") cargado.");
			}
		}
		catch(Exception e){
			System.out.println(action.id + " >>> estado de exploracion no existe o esta corrupto: " + e.getMessage());
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
	 * Si el programa es llamado con el argumento {@link SolverFaster#flag_retroceder_externo} en true, entonces
	 * debo volver la exploracion hasta cierta posicion y guardar estado. No explora.
	 */
	final static void retrocederEstado(ExploracionAction action) {
		
		action.retroceder= true;
		int cursor_destino = DESTINO_RET;
		
		while (action.cursor>=0)
		{
			if (!action.retroceder){
				action.mas_bajo_activo= true;
				action.mas_bajo= action.cursor;
				CommonFuncs.guardarEstado(action.statusFileName, action.id, action.piezas, action.tablero, action.cursor, 
						action.mas_bajo, action.mas_alto, action.mas_lejano_parcial_max, action.desde_saved, neighborStrategy, 
						colorRightExploredStrategy);
				action.sig_parcial = CommonFuncs.guardarResultadoParcial(false, action.id, action.piezas,
						action.tablero, action.sig_parcial, MAX_NUM_PARCIAL, action.parcialFileName,
						action.parcialMaxFileName, action.disposicionMaxFileName, action.libresMaxFileName);
				System.out.println(action.id + " >>> Exploracion retrocedio a la posicion " + action.cursor + ". Estado salvado.");
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			
			--action.cursor;
			
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (action.cursor < 0)
				break; //obliga a salir del while
			
			if (action.cursor != Consts.POSICION_CENTRAL){
				Pieza pzz = action.tablero[action.cursor];
				pzz.usada= false; //la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				action.tablero[action.cursor]= null;
			}
			
			//si retrocedá hasta el cursor destino, entonces no retrocedo mas
			if ((action.cursor+1) <= cursor_destino){
				action.retroceder= false;
				cursor_destino= Consts.CURSOR_INVALIDO;
			}
			
			//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
			if (action.retroceder)
				action.desde_saved[action.cursor] = 0; //la exploracion de posibles piezas para la posición cursor debe empezar desde la primer pieza
		}
	}
	
	/**
	 * Inicializa varias estructuras y flags
	 */
	public final void setupInicial(ReaderForFile readerForTilesFile) {
		
		// cargo en el arreglo matrix_zonas valores que me indiquen en que posición estoy (borde, esquina o interior) 
		CommonFuncs.inicializarMatrixZonas();
		
		// seteo las posiciones donde puedo setear un contorno como usado o libre
		CommonFuncs.inicializarZonaProcesoContornos();
		System.out.println("Usando restriccion de contornos de " + Contorno.MAX_COLS + " columnas.");
		
		// seteo las posiciones donde se puede preguntar por contorno superior usado
		CommonFuncs.inicializarZonaReadContornos();
		
		// creates the array of actions
		actions = new ExploracionAction[NUM_PROCESSES];
		// a start signal that prevents any ExplorationAction from proceeding until the orchestrator (this thread) is ready for them to proceed
		startSignal = new CountDownLatch(1);
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {

			ExploracionAction exploracionAction = new ExploracionAction(proc, NUM_PROCESSES, MAX_CICLOS,
					LIMITE_RESULTADO_PARCIAL, startSignal);
			
			exploracionAction.setupInicial(readerForTilesFile);
			
			actions[proc] = exploracionAction;
		}
		
		// cargar la super matriz (solo basta obtener las piezas de un exploracionAction)
		CommonFuncs.cargarSuperEstructura(actions[0].id, actions[0].piezas, FairExperimentGif, SolverFaster.neighborStrategy);
	}
	
	private final Thread[] createPoolAndStart() {
		
		Thread[] pool = new Thread[NUM_PROCESSES];
		
		// submit all threads tasks
		for (int i = 0, c = actions.length; i < c; ++i) {
			System.out.println("ExploracionAction " + i + " submitted");
			Thread thread = new Thread(actions[i]);
			pool[i] = thread;
			thread.start();
		}

		// let all tasks proceed
		startSignal.countDown();
		
		return pool;
	}
	
	/**
	 * Invoca al pool de threads con varias instancias de RecursiveAction: ExploracionAction.
	 * Cada action ejecuta una rama de la exploración asociada a su id. De esta manera se logra decidir 
	 * la rama a explorar y tmb qué siguiente rama explorar una vez finalizada la primer rama.
	 */
	public final void atacar() {
		
		Thread[] pool = createPoolAndStart();
		
		// wait until all tasks are done
		for (Thread t : pool) {
			try {
				t.join();
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public final void atacarForBenchmark(long timeoutTaskInSecs) {
		
		Thread[] pool = createPoolAndStart();
		
		try {
			// wait only for the first thread
			pool[0].join(timeoutTaskInSecs * 1000);
		} catch (InterruptedException ex) {
			// something caused this thread to be interrupted
		} finally {
			// interrupt all threads
			System.out.println("Interrupting tasks...");
			for (Thread t : pool) {
				try {
					t.interrupt();
					t.stop(); // if thread isn't stop then its runnable task continues running
				} catch (Exception ex) {
				}
			}
			System.out.println("Tasks interrupted.");
		}
	}
	
	public void resetForBenchmark() {
		
		neighborStrategy.resetForBenchmark();
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {
			ExploracionAction exploracionAction = actions[proc];
			exploracionAction.resetForBenchmark(NUM_PROCESSES, startSignal);
		}
	}
}