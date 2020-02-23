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
import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.NodoPosibles;
import org.fabri1983.eternity2.core.neighbors.SuperMatrizHashFunctionStrategy;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredLocalStrategy;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;
import org.fabri1983.eternity2.ui.EternityII;
import org.fabri1983.eternity2.ui.ViewEternityFactory;
import org.fabri1983.eternity2.ui.ViewEternityMPJEFactory;

public final class SolverFasterMPJE {
	
	private static EternityII tableboardE2 = null; // instancia del tablero gráfico que se muestra en pantalla
	
	private static int POSICION_MULTI_PROCESSES = -1; // (99) posición del tablero en la que se usará comunicación multiprocesses
	private static int NUM_PROCESSES = mpi.MPI.COMM_WORLD.Size(); // número de procesos
	public final static int THIS_PROCESS = mpi.MPI.COMM_WORLD.Rank(); // id de proceso actual (0 base)
	private static int TAG_SINCRO = 333; // tags para identificar mensajes interprocesos
	private static int MESSAGE_HALT = 0, MESSAGE_SINCRO = 200; // mensajes para comunicar una acción o estado
	private static int[] mpi_send_info = new int[1]; // arreglo de envío de mensajes entre procesos
	private static mpi.Request mpi_requests[] = new mpi.Request[mpi.MPI.COMM_WORLD.Size()]; // arreglo para almacenar los requests que devuelven los Isend
	private static boolean sincronizar; // indica si se deden sincronizar los procesos antes de comenzar
	private static int[] num_processes_orig;
	private static int pos_multi_process_offset = 0; // usado con POSICION_MULTI_PROCESSES sirve para continuar haciendo los calculos de distribución de exploración
	
	private static long MAX_CICLOS; // Número máximo de ciclos para guardar estado
	private static int DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
	private static int MAX_NUM_PARCIAL; // Número de archivos parciales que se generarón
	//private static int LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia

	private final static String NAME_FILE_SOLUCION = "solution/soluciones_P" + THIS_PROCESS + Consts.FILE_EXT;
	private final static String NAME_FILE_DISPOSICION = "solution/disposiciones_P" + THIS_PROCESS + Consts.FILE_EXT;
	private final static String NAME_FILE_STATUS = "status/status_saved_P" + THIS_PROCESS + Consts.FILE_EXT;
	private final static String NAME_FILE_PARCIAL_MAX = "status/parcialMAX_P" + THIS_PROCESS + Consts.FILE_EXT;
	private final static String NAME_FILE_DISPOSICIONES_MAX = "status/disposicionMAX_P" + THIS_PROCESS + Consts.FILE_EXT;
	private final static String NAME_FILE_PARCIAL = "status/parcial_P" + THIS_PROCESS + "";
	private final static String NAME_FILE_LIBRES_MAX = "status/libresMAX_P" + THIS_PROCESS + Consts.FILE_EXT;
	
	private static int LIMITE_RESULTADO_PARCIAL = 211; // posición por defecto
	private static int sig_parcial, cur_destino;
	public static int cursor, mas_bajo, mas_alto, mas_lejano_parcial_max;
	
	public static long count_cycles; // count cycles for this only instance
	
	public final static Pieza[] piezas = new Pieza[Consts.MAX_PIEZAS];
	public final static Pieza[] tablero = new Pieza[Consts.MAX_PIEZAS];
	private final static short[] desde_saved = new short[Consts.MAX_PIEZAS];
	private final static Contorno contorno = new Contorno();
	
	private final static NeighborStrategy neighborStrategy = new SuperMatrizHashFunctionStrategy();
	
	private static ColorRightExploredStrategy colorRightExploredStrategy;
	
	private static boolean FairExperimentGif;
	private static boolean status_cargado, retroceder;
	private static boolean mas_bajo_activo, flag_retroceder_externo;
	
	private static long time_inicial; //sirven para calcular el tiempo al hito de posición lejana
	private static long time_status_saved; //usado para calcular el tiempo entre diferentes status saved

	private static StringBuilder printBuffer = new StringBuilder(64);
	
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
	 * @param totalProcesses: total number of processes.
	 */
	public SolverFasterMPJE (long m_ciclos, int lim_max_par, int lim_exploracion, int max_parciales, int destino_ret, 
			boolean usar_tableboard, boolean usar_multiples_boards, int cell_pixels_lado, int p_refresh_millis, 
			boolean p_fair_experiment_gif, boolean p_poda_color_right_explorado, int p_pos_multi_processes,
			int totalProcesses) {

		MAX_CICLOS= m_ciclos;
		
		POSICION_MULTI_PROCESSES = p_pos_multi_processes;
		num_processes_orig = new int[Consts.MAX_PIEZAS];
		
		int procMultipleBoards = 0; // por default solo el primer proceso muestra el tableboard
		// si se quiere mostrar multiple tableboards entonces hacer que el target proc sea este mismo proceso 
		if (usar_multiples_boards)
			procMultipleBoards = THIS_PROCESS;
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (Consts.MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL= lim_max_par;
		
		//LIMITE_DE_EXPLORACION= lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia
		
		FairExperimentGif = p_fair_experiment_gif;
		
		retroceder= false; //variable para indicar que debo volver estados de backtracking
		if (destino_ret >= 0){
			DESTINO_RET= destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo= true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
		//indica si se usará la poda de colores right explorados en borde left
		if (p_poda_color_right_explorado)
			colorRightExploredStrategy = new ColorRightExploredLocalStrategy();
		
		MAX_NUM_PARCIAL= max_parciales; //indica hasta cuantos archivos parcial.txt voy a tener
		
		cur_destino= Consts.CURSOR_INVALIDO; //variable para indicar hasta que posicion debo retroceder
		mas_bajo_activo= false; //permite o no modificar el cursor mas_bajo
		sig_parcial= 1; //esta variable indica el numero de archivo parcial siguiente a guardar
		
		if (usar_tableboard && !flag_retroceder_externo && THIS_PROCESS == procMultipleBoards) {
			ViewEternityFactory viewFactory = new ViewEternityMPJEFactory(Consts.LADO, cell_pixels_lado, 
					Consts.MAX_COLORES, (long)p_refresh_millis, THIS_PROCESS, totalProcesses);
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
			// first ask if file exists
			File f = new File(n_file);
			if (!f.isFile()) {
				System.out.println(THIS_PROCESS + " >>> estado de exploracion no existe.");
				System.out.flush();
				return status_cargado;
			}
			
			reader= new BufferedReader(new FileReader(f));
			String linea= reader.readLine();
			
			if (linea==null) {
				throw new Exception(THIS_PROCESS + " >>> First line is null.");
			}
			else{
				int sep,sep_ant;
				
				// contiene el valor de cursor mas bajo alcanzado en una vuelta de ciclo
				mas_bajo= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas alto alcanzado en una vuelta de ciclo
				linea= reader.readLine();
				mas_alto= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas lejano parcial alcanzado (aquel que graba parcial max)
				linea= reader.readLine();
				mas_lejano_parcial_max= Integer.parseInt(linea);
				
				// contiene la posición del cursor en el momento de guardar estado
				linea= reader.readLine();
				cursor= Integer.parseInt(linea);
				
				// recorro los indices de las piezas que estaban en tablero
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < Consts.MAX_PIEZAS; ++k){
					if (k==(Consts.MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+Consts.SECCIONES_SEPARATOR_EN_FILE.length();
					tablero[k]= numPieza == -1 ? null : piezas[numPieza];
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
					desde_saved[k] = numPieza;
				}
				
				//la siguiente línea indica si se estaba usando poda de color explorado
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
				
				//las restantes MAX_PIEZAS lineas contienen el estado de rotación y de usada de cada pieza
				int pos=0; //cuento cuantas lineas voy procesando
				String splitted[];
				linea= reader.readLine(); //info de la primer pieza
				while ((linea != null) && (pos < Consts.MAX_PIEZAS)){
					splitted = linea.split(Consts.SECCIONES_SEPARATOR_EN_FILE);
					Pieza.llevarArotacion(piezas[pos],Byte.parseByte(splitted[0]));
					piezas[pos].usada = Boolean.parseBoolean(splitted[1]);
					linea= reader.readLine();
					++pos;
				}
				if (pos != Consts.MAX_PIEZAS){
					System.out.println(THIS_PROCESS + " >>> ERROR. La cantidad de piezas en el archivo " + n_file + " no coincide con el numero de piezas que el juego espera.");
					throw new Exception("Inconsistent number of pieces.");
				}
					
				status_cargado=true;
				sincronizar = false;
				System.out.println(THIS_PROCESS + " >>> estado de exploracion (" + n_file + ") cargado.");
				System.out.flush();
			}
		}
		catch(Exception e){
			System.out.println(THIS_PROCESS + " >>> estado de exploracion no existe o esta corrupto: " + e.getMessage());
			System.out.flush();
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
	 * Si el programa es llamado con el argumento retroceder externo en true, entonces
	 * debo volver la exploracion hasta cierta posicion y guardar estado. No explora.
	 */
	private final static void retrocederEstado () {
		
		retroceder= true;
		cur_destino= DESTINO_RET;
		
		while (cursor>=0) {
			
			if (!retroceder){
				mas_bajo_activo= true;
				mas_bajo= cursor;
				CommonFuncs.guardarEstado(NAME_FILE_STATUS, THIS_PROCESS, piezas, tablero, cursor, mas_bajo, mas_alto,
						mas_lejano_parcial_max, desde_saved, neighborStrategy, colorRightExploredStrategy);
				sig_parcial = CommonFuncs.guardarResultadoParcial(false, THIS_PROCESS, piezas, tablero, sig_parcial,
						MAX_NUM_PARCIAL, NAME_FILE_PARCIAL, NAME_FILE_PARCIAL_MAX, NAME_FILE_DISPOSICIONES_MAX,
						NAME_FILE_LIBRES_MAX);
				System.out.println(THIS_PROCESS + " >>> Exploracion retrocedio a la posicion " + cursor + ". Estado salvado.");
				System.out.flush();
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			
			--cursor;
			
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (cursor < 0)
				break; //obliga a salir del while
			
			if (cursor != Consts.POSICION_CENTRAL){
				Pieza pzz= tablero[cursor];
				pzz.usada= false; //la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				tablero[cursor]= null;
			}
			
			//si retrocedió hasta el cursor destino, entonces no retrocedo mas
			if ((cursor+1) <= cur_destino){
				retroceder= false;
				cur_destino= Consts.CURSOR_INVALIDO;
			}
			
			//si está activado el flag para retroceder niveles de exploración entonces debo limpiar algunas cosas
			if (retroceder)
				desde_saved[cursor]= 0; //la exploración de posibles piezas para la posicion cursor debe empezar desde la primer pieza
		}
	}
	
	/**
	 * Inicializa varias estructuras y flags
	 */
	public final void setupInicial (ReaderForFile readerForTilesFile) {
		
		//cargo en el arreglo matrix_zonas valores que me indiquen en qué posición estoy (borde, esquina o interior) 
		CommonFuncs.inicializarMatrixZonas();
		
		//seteo las posiciones donde puedo setear un contorno como usado o libre
		CommonFuncs.inicializarZonaProcesoContornos();
		System.out.println(THIS_PROCESS + " >>> Usando restriccion de contornos de " + Contorno.MAX_COLS + " columnas.");
		System.out.flush();
		
		//seteo las posiciones donde se puede preguntar por contorno superior usado
		CommonFuncs.inicializarZonaReadContornos();
		
		CommonFuncs.cargarPiezas(THIS_PROCESS, piezas, readerForTilesFile);
		
		//hago una verificacion de las piezas cargadas
		CommonFuncs.verificarTiposDePieza(THIS_PROCESS, piezas);
		
		//cargar la super estructura 4-dimensional que agiliza la búsqueda de piezas
		CommonFuncs.cargarSuperEstructura(THIS_PROCESS, piezas, FairExperimentGif, neighborStrategy);
		
		// Pruebo cargar el primer status_saved
		status_cargado = cargarEstado(NAME_FILE_STATUS);
		if (!status_cargado) {
			cursor=0;
			mas_bajo= 0; //este valor se setea empiricamente
			mas_alto= 0;
			mas_lejano_parcial_max= 0;
			status_cargado=false;
			sincronizar = true;
		}
		
		//cargo las posiciones fijas
		CommonFuncs.ponerPiezasFijasEnTablero(THIS_PROCESS, piezas, tablero);
		
		//seteo como usados los contornos ya existentes en tablero
		Contorno.inicializarContornos(contorno, tablero, Consts.MAX_PIEZAS, Consts.LADO);
		
		if (tableboardE2 != null) 
			tableboardE2.startPainting();
	}

	/**
	 * Metodo que carga todo lo necesario antes de empezar la exploracion
	 * Ademas tiene la responsabilidad de permitir volver estados anteriores 
	 * de exploracion en el caso de cargar estado de exploracion
	 */
	public final void atacar ()
	{
		if (flag_retroceder_externo){
			retrocederEstado();
			return;
		}
		
		long nowNanos = System.nanoTime();
		time_inicial = nowNanos;
		time_status_saved = nowNanos;
		
		//si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado)
			explorar(0);
		//se carga estado de exploración, debo proveer la posibilidad de volver estados anteriores de exploracion
		else{
			//ahora exploro comunmente y proveo una especie de recursividad para retroceder estados
			while (cursor >= 0){
				if (!retroceder){
					//pregunto si llegué al limite de esta instancia de exploracion
					/*if (cursor <= LIMITE_DE_EXPLORACION){
						CommonFuncs.operarSituacionLimiteAlcanzado(NAME_FILE_STATUS, THIS_PROCESS, );
						return;
					}*/
					//creo una nueva instancia de exploracion
					explorar(desde_saved[cursor]);
				}
				--cursor;
				
				//si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				//seteo los contornos como libres si no están usados
				CommonFuncs.setContornoLibre(cursor, contorno, tablero);

				//debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != Consts.POSICION_CENTRAL){
					Pieza p = tablero[cursor];
					p.usada= false;
					tablero[cursor]= null;
				}
				
				//si retrocedí hasta el cursor destino, entonces no retrocedo mas
				/*@RETROCEDER
				if (cursor <= cur_destino){
					retroceder = false;
					cur_destino = Consts.CURSOR_INVALIDO;
				}
				//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
				if (retroceder)
					desde_saved[cursor] = 0; //la exploracion de posibles piezas para la posicion cursor debe empezar desde la primer pieza
				*/
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println(THIS_PROCESS + " >>> exploración agotada.");
	}
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 * 
	 * @param desde Es la posición desde donde empiezo a tomar las piezas de NodoPosibles.
	 */
	private final static void explorar (int desde)
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
		 * Representa las primeras sentencias del backtracking de exploracion. 
		 * Pregunta algunas cositas antes de empezar una nueva instancia de exploración.
		 */
		//#############################################################################################
		
		//si cursor se pasó del limite de piezas, significa que estoy en una solucion
		if (cursor >= Consts.MAX_PIEZAS){
			CommonFuncs.guardarSolucion(THIS_PROCESS, tablero, NAME_FILE_SOLUCION, NAME_FILE_DISPOSICION);
			System.out.println(THIS_PROCESS + " >>> Solucion Encontrada!!");
			System.out.flush();
			return; //evito que la instancia de exporacion continue
		}
		
		//si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor > mas_lejano_parcial_max){
			mas_lejano_parcial_max= cursor;
			if (cursor >= LIMITE_RESULTADO_PARCIAL){
				long time_final= System.nanoTime();
				printBuffer.setLength(0);
				printBuffer.append(THIS_PROCESS).append(" >>> ")
					.append(TimeUnit.MILLISECONDS.convert(time_final - time_inicial, TimeUnit.NANOSECONDS))
					.append(" ms, cursor ").append(cursor);
				System.out.println(printBuffer.toString());
				System.out.flush();
				printBuffer.setLength(0);
				sig_parcial = CommonFuncs.guardarResultadoParcial(true, THIS_PROCESS, piezas, tablero, sig_parcial,
						MAX_NUM_PARCIAL, NAME_FILE_PARCIAL, NAME_FILE_PARCIAL_MAX, NAME_FILE_DISPOSICIONES_MAX,
						NAME_FILE_LIBRES_MAX);
			}
		}
		
		//voy manteniendo el cursor mas alto para esta vuelta de ciclos
		if (cursor > mas_alto)
			mas_alto = cursor;
		//si cursor se encuentra en una posicion mas baja que la posicion mas baja alcanzada guardo ese valor
		if (cursor < mas_bajo)
			mas_bajo= cursor;
		//la siguiente condición se cumple una sola vez
		if (cursor > 100 && !mas_bajo_activo){
			mas_bajo= Consts.MAX_PIEZAS;
			mas_bajo_activo= true;
		}
		
		//si llegué a MAX_CICLOS de ejecución guardo el estado de exploración
		if (count_cycles >= MAX_CICLOS){
			//calculo el tiempo entre status saved
			long nanoTimeNow = System.nanoTime();
			long durationNanos = nanoTimeNow - time_status_saved;
			long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
			long piecesPerSec = count_cycles * 1000L / durationMillis; // conversion from millis to seconds
			count_cycles = 0;
			CommonFuncs.guardarEstado(NAME_FILE_STATUS, THIS_PROCESS, piezas, tablero, cursor, mas_bajo, mas_alto,
					mas_lejano_parcial_max, desde_saved, neighborStrategy, colorRightExploredStrategy);
			sig_parcial = CommonFuncs.guardarResultadoParcial(false, THIS_PROCESS, piezas, tablero, sig_parcial,
					MAX_NUM_PARCIAL, NAME_FILE_PARCIAL, NAME_FILE_PARCIAL_MAX, NAME_FILE_DISPOSICIONES_MAX,
					NAME_FILE_LIBRES_MAX);
			printBuffer.setLength(0);
			printBuffer.append(THIS_PROCESS).append(" >>> Estado guardado en cursor ").append(cursor)
					.append(". Pos Min ").append(mas_bajo).append(", Pos Max ").append(mas_alto)
					.append(". Tiempo: ").append(durationMillis).append(" ms") 
					.append(", ").append(piecesPerSec).append(" pieces/sec");
			System.out.println(printBuffer.toString());
			System.out.flush();
			printBuffer.setLength(0);
			time_status_saved = nanoTimeNow;
			//cuando se cumple el ciclo aumento de nuevo el valor de mas_bajo y disminuyo el de mas_alto
			mas_bajo= Consts.MAX_PIEZAS;
			mas_alto= 0;
		}
		
		//#############################################################################################
		
		
		//#############################################################################################
		/**
		 * Explorar pieza fija.
		 * Ya me encuentro en una posicion fija, entonces salteo esta posición y continuo.
		 * NOTA: por ahora solo se contempla la posicion 135 (136 real) como fija y no se permte rotarla.
		 */
		//#############################################################################################
		
		// Si la posicion cursor es una posicion fija no tengo que hacer la exploracion "estandar". 
		// Se supone que la pieza fija ya está debidamente colocada.
		if (cursor == Consts.POSICION_CENTRAL){
			
			//seteo los contornos como usados
			CommonFuncs.setContornoUsado(cursor, contorno, tablero);
			
			++cursor;
			explorar(0);
			--cursor;
			
			//seteo los contornoscomo libres
			CommonFuncs.setContornoLibre(cursor, contorno, tablero);
			/*@RETROCEDER
			if (cursor <= cur_destino){
				retroceder= false;
				cur_destino= Consts.CURSOR_INVALIDO;
			}*/
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
		if (CommonFuncs.esContornoSuperiorUsado(cursor, contorno, tablero))
			return;
		
		// sincronización de los procesos (Knock Knock) una única vez
		if (sincronizar && (cursor == POSICION_MULTI_PROCESSES)) {
			sincronizar = false; // no volver a sincronizar
			knocKnock();
		}

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
	
	private final static void knocKnock () {
		
		if (THIS_PROCESS == 0)
		{
			mpi_send_info[0] = MESSAGE_SINCRO;
			System.out.println("Rank 0: --- Sincronizando con todos. Sending msg " + mpi_send_info[0] + " ...");
			System.out.flush();
			//sincronizo con los restantes procesos
			for (int rank=1; rank < NUM_PROCESSES; ++rank)
				mpi_requests[rank-1] = mpi.MPI.COMM_WORLD.Isend(mpi_send_info, 0, mpi_send_info.length, mpi.MPI.INT, rank, TAG_SINCRO); //el tag identifica al mensaje
			//espero a que todas las peticiones se completen
			mpi.Request.Waitall(mpi_requests);
			System.out.println("Rank 0: --- Todos sincronizados.");
			System.out.flush();
		}
		else
		{
			System.out.println(THIS_PROCESS + " >>> --- Esperando sincronizarse...");
			System.out.flush();
			mpi_send_info[0] = MESSAGE_HALT;
			// espero recibir mensaje
			mpi.MPI.COMM_WORLD.Recv(mpi_send_info, 0, mpi_send_info.length, mpi.MPI.INT, mpi.MPI.ANY_SOURCE, TAG_SINCRO); //el tag identifica al mensaje
			if (mpi_send_info[0] == MESSAGE_SINCRO) {
				System.out.println(THIS_PROCESS + " >>> --- Sincronizado with msg " + mpi_send_info[0]);
				System.out.flush();
			}
			else if (mpi_send_info[0] == MESSAGE_HALT)
				;
		}
		
		System.out.println(THIS_PROCESS + " >>> --- Continua procesando.");
		System.out.flush();
		
		/*try {
			System.out.println("Waiting call...");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			;
		}
		System.exit(0);*/
	}
	
	/**
	 * Realiza toda la exploración standard: cicla sobre las posibles piezas para las
	 * posicon actual de cursor, y cicla sobre las posibles rotaciones de cada pieza.
	 * Aplica varias podas que solamente son validas en este nivel de exploracion.
	 * 
	 * @param desde Es la posición desde donde empiezo a tomar las piezas de NodoPosibles.
	 */
	private final static void exploracionStandard(int desde)
	{
		// voy a recorrer las posibles piezas que coinciden con los colores de las piezas alrededor de cursor
		NodoPosibles nodoPosibles = CommonFuncs.obtenerPosiblesPiezas(cursor, tablero, neighborStrategy);
		if (nodoPosibles == null)
			return; // significa que no existen posibles piezas para la actual posicion de cursor

		int length_posibles = nodoPosibles.mergedInfo.length;
		
		num_processes_orig[cursor] = NUM_PROCESSES;

		// En modo multiproceso tengo que establecer los limites de las piezas a explorar para este proceso.
		// En este paso solo inicializo algunas variables para futuros cálculos.
		if (cursor == POSICION_MULTI_PROCESSES + pos_multi_process_offset) {
			// en ciertas condiciones cuado se disminuye el num de procs, es necesario acomodar el concepto de this_proc para los calculos siguientes.
			int this_proc_absolute = THIS_PROCESS % NUM_PROCESSES;

			// caso 1: trivial. Cada proc toma una única rama de nodoPosibles
			if (NUM_PROCESSES == length_posibles) {
				desde = this_proc_absolute;
				length_posibles = this_proc_absolute + 1;
			}
			// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas
			else if (NUM_PROCESSES < length_posibles) {
				int span = (length_posibles + 1) / NUM_PROCESSES;
				desde = this_proc_absolute * span;
				if (desde >= length_posibles)
					desde = length_posibles - 1;
				else if (desde + span < length_posibles)
					length_posibles = desde + span;
			}
			// caso 3: existen mas procs que piezas a explorar, entonces hay que distribuir los procs y
			// aumentar el POSICION_MULTI_PROCESSES en uno asi el siguiente nivel tmb se continua la división.
			// Ahora la cantidad de procs se setea igual a length_posibles
            else {
				int divisor = (NUM_PROCESSES + 1) / length_posibles; // reparte los procs por posible pieza
				NUM_PROCESSES = length_posibles;
				desde = this_proc_absolute / divisor;
				if (desde >= length_posibles)
					desde = length_posibles - 1;
				length_posibles = desde + 1;
				++pos_multi_process_offset;
			}
		}
		
		for (; desde < length_posibles; ++desde) {
			
			//desde_saved[cursor]= desde; //actualizo la posicion en la que leo de posibles
			short merged = nodoPosibles.mergedInfo[desde];
			Pieza p = piezas[merged & NodoPosibles.MASK_PIEZA_INDEX];
			byte rot = (byte) (merged >>> NodoPosibles.MASK_PIEZA_ROT_SHIFT);
			
			// pregunto si la pieza candidata está siendo usada
			if (p.usada)
				continue; // es usada, pruebo con la siguiente pieza
		
			++count_cycles; // incremento el contador de combinaciones de piezas
			
			// pregunto si está activada la poda del color right explorado en borde left
			if (colorRightExploredStrategy != null) {
				CommonFuncs.testPodaColorRightExplorado(cursor, p, colorRightExploredStrategy);
			}
			
			//#### En este punto ya tengo la pieza correcta para poner en tablero[cursor] ####
			
			tablero[cursor] = p; // en la posicion "cursor" del tablero pongo la pieza
			p.usada = true; // en este punto la pieza va a ser usada
			Pieza.llevarArotacion(p, rot);
			
			//#### En este punto ya tengo la pieza colocada y rotada correctamente ####
			
			// una vez rotada adecuadamente la pieza pregunto si el borde inferior que genera está siendo usado
//			@CONTORNO_INFERIOR
//			if (esContornoInferiorUsado(cursor)){
//				p.usada = false; //la pieza ahora no es usada
//				continue;
//			}
			
			// FairExperiment.gif: color bottom repetido en sentido horizontal
			if (FairExperimentGif) {
				if (CommonFuncs.testFairExperimentGif(cursor, p, tablero))
					continue;
			}

			// seteo los contornos como usados
			CommonFuncs.setContornoUsado(cursor, contorno, tablero);
				
			//##########################
			// Llamo una nueva instancia
			++cursor;
			explorar(0);
			--cursor;
			//##########################
				
			// seteo los contornos como libres
			CommonFuncs.setContornoLibre(cursor, contorno, tablero);
			
			p.usada = false; //la pieza ahora no es usada
			
			// si retrocedió hasta la posicion destino, seteo la variable retroceder en false e invalído a cur_destino
//			@RETROCEDER
//			if (cursor <= cur_destino){
//				retroceder= false;
//				cur_destino=CURSOR_INVALIDO;
//			}
//			// caso contrario significa que todavia tengo que seguir retrocediendo
//			if (retroceder)
//				break;
		}

		desde_saved[cursor] = 0; //debo poner que el desde inicial para este cursor sea 0
		tablero[cursor] = null; //dejo esta posicion de tablero libre

		// restore multi process variables
		NUM_PROCESSES = num_processes_orig[cursor];
		--pos_multi_process_offset;
		if (pos_multi_process_offset < 0)
			pos_multi_process_offset = 0;
	}

}
