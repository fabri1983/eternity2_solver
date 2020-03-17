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
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.neighbors.MultiDimensionalStrategy;
import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.Neighbors;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public final class SolverFaster {
	
	static int NUM_PROCESSES = 1;
	static ExplorationTask tasks[];
	static CountDownLatch startSignal;
    
	static long MAX_CICLOS; // Número máximo de ciclos para imprimir stats
	static boolean SAVE_STATUS_ON_MAX;
	static short DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
	public static short LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia
	
	static final String NAME_FILE_STATUS = "status/status_saved";
	static final String NAME_FILE_PARCIAL = "status/parcial";
	static final String NAME_FILE_DISPOSICION = "solution/disposiciones";
	static final String NAME_FILE_SOLUCION = "solution/soluciones";
	
	public static short LIMITE_RESULTADO_PARCIAL = 211; // por defecto
	
	public static final Pieza[] piezas = new Pieza[Consts.MAX_PIEZAS];
	
	static final NeighborStrategy neighborStrategy = new MultiDimensionalStrategy();
	
	static boolean flag_retroceder_externo;
	
	private SolverFaster() {
	}
	
	/**
	 * @param max_ciclos: número máximo de ciclos para imprimir stats.
	 * @param save_status_on_max: guardar status cuando se alcanza MAX_CICLOS o LIMITE_RESULTADO_PARCIAL superado.
	 * @param lim_max_par: posición en tablero minima para guardar estado parcial máximo.
	 * @param lim_exploracion: indica hasta qué posición debe explorar esta instancia.
	 * @param destino_ret: posición en tablero hasta la cual se debe retroceder.
	 * @param nun_tasks: parallelism level for the fork-join pool.
	 */
	public static SolverFaster build(long max_ciclos, boolean save_status_on_max, short lim_max_par,
			short lim_exploracion, short destino_ret, int num_tasks) {
		
		MAX_CICLOS = max_ciclos;
		SAVE_STATUS_ON_MAX = save_status_on_max;
		NUM_PROCESSES = num_tasks;
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (Consts.MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL = lim_max_par;
		
		LIMITE_DE_EXPLORACION = lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia 
		
		if (destino_ret >= 0) {
			DESTINO_RET = destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo = true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
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
	final static boolean cargarEstado(String n_file, ExplorationTask task)
	{
		BufferedReader reader = null;
		boolean status_cargado = false;
		
		try{
			File f = new File(n_file);
			if (!f.isFile()) {
				System.out.println(task.ID + " >>> Estado de exploracion no existe.");
				return status_cargado;
			}
			
			reader = new BufferedReader(new FileReader(f));
			String linea = reader.readLine();
			
			if (linea==null) {
				throw new Exception("First line is null.");
			}
			else{
				int sep,sep_ant;
				
				// contiene el valor de resultado_parcial_count
				SolverFaster.LIMITE_RESULTADO_PARCIAL= Short.parseShort(linea);
				
				// contiene la posición del cursor en el momento de guardar estado
				linea= reader.readLine();
				task.cursor= Short.parseShort(linea);
				
				// recorro la info que estaba en tablero
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (short k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					int mergedInfo= Integer.parseInt(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					task.tablero[k]= mergedInfo;
					if (mergedInfo != -1)
						task.usada[Neighbors.numero(mergedInfo)] = true;
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
					task.iter_desde[k] = index;
				}
				
 				// recorro los valores de matrix_color_explorado[]
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (short k=0; k < Consts.LADO; ++k){
					if (k==(Consts.LADO-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					int val= Integer.parseInt(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					task.colorRightExploredStrategy.set(k, val);
				}
				
				status_cargado = true;
				
				System.out.println(task.ID + " >>> estado de exploracion (" + n_file + ") cargado.");
			}
		}
		catch(Exception e){
			System.out.println(task.ID + " >>> estado de exploracion no existe o esta corrupto: " + e.getMessage());
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
	final static void retrocederEstado(ExplorationTask task) {
		
		boolean retroceder = true;
		short cursor_destino = DESTINO_RET;
		
		while (task.cursor>=0)
		{
			if (retroceder){
				CommonFuncs.guardarEstado(task.statusFileName, task.ID, task.tablero, task.cursor,
						SolverFaster.LIMITE_RESULTADO_PARCIAL, task.iter_desde, neighborStrategy,
						task.colorRightExploredStrategy);
				CommonFuncs.guardarResultadoParcial(task.ID, task.tablero, task.parcialFileName);
				System.out.println(task.ID + " >>> Exploracion retrocedio a la posicion " + task.cursor + ". Estado salvado.");
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			
			--task.cursor;
			
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (task.cursor < 0)
				break; //obliga a salir del while
			
			if (task.cursor != Consts.PIEZA_CENTRAL_POS_TABLERO) {
				int mergedInfo = task.tablero[task.cursor];
				int numero = Neighbors.numero(mergedInfo);
				// la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				task.usada[numero] = false;
				task.tablero[task.cursor] = Consts.TABLERO_INFO_EMPTY_VALUE;
			}
			
			//si retrocedá hasta el cursor destino, entonces no retrocedo mas
			if ((task.cursor+1) <= cursor_destino){
				retroceder = false;
				cursor_destino= Consts.CURSOR_INVALIDO;
			}
			
			//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
			if (retroceder)
				task.iter_desde[task.cursor] = 0; //la exploracion de posibles piezas para la posición cursor debe empezar desde la primer pieza
		}
	}
	
	public final void setupInicial(ReaderForFile readerForTilesFile) {
		
		CommonFuncs.inicializarMatrixZonas();
		
		CommonFuncs.inicializarZonaProcesoContornos();
		
		CommonFuncs.inicializarZonaReadContornos();
		
		CommonFuncs.cargarPiezas(-1, piezas, readerForTilesFile);
		
		CommonFuncs.verificarTiposDePieza(-1, piezas);
		
		// creates the array of tasks
		tasks = new ExplorationTask[NUM_PROCESSES];
		// a start signal that prevents any ExplorationAction from proceeding until the master (this thread) is ready for let them to proceed
		startSignal = new CountDownLatch(1);
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {
			ExplorationTask task = new ExplorationTask(proc, NUM_PROCESSES, startSignal);
			task.setupInicial(readerForTilesFile);
			tasks[proc] = task;
		}
		
		CommonFuncs.cargarSuperEstructura(-1, piezas, neighborStrategy);
	}
	
	private final Thread[] createPoolAndStart() {
		
		Thread[] pool = new Thread[NUM_PROCESSES];
		
		for (int i = 0, c = tasks.length; i < c; ++i) {
			System.out.println("Task submitted " + i);
			Thread thread = new Thread(tasks[i]);
			pool[i] = thread;
			thread.start();
		}

		// let all tasks proceed
		startSignal.countDown();
		
		return pool;
	}
	
	public final void atacar() {
		
		Thread[] pool = createPoolAndStart();
		
		// makes this thread to wait until all tasks are done
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
			System.out.println("All tasks interrupted.");
		}
	}
	
	public void resetForBenchmark() {
		
		neighborStrategy.resetForBenchmark();
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {
			ExplorationTask task = tasks[proc];
			task.resetForBenchmark(NUM_PROCESSES, startSignal);
		}
	}
	
}