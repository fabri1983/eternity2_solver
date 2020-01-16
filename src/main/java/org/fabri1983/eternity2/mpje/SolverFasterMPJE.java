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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.NodoPosibles;
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.PiezaFactory;
import org.fabri1983.eternity2.core.PiezaStringer;
import org.fabri1983.eternity2.core.SendMail;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction;
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
	private static int ESQUINA_TOP_RIGHT,ESQUINA_BOTTOM_RIGHT,ESQUINA_BOTTOM_LEFT;
	//private static int LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia
	private final static int LADO= 16;
	private final static int LADO_SHIFT_AS_DIVISION = 4;
	public final static int MAX_PIEZAS= 256;
	public final static int POSICION_CENTRAL= 135;
	public final static int INDICE_P_CENTRAL= 138; //es la ubicación de la pieza central en piezas[]
	private final static int ANTE_POSICION_CENTRAL= 134; //la posicion inmediatamente anterior a la posicion central
	private final static int SOBRE_POSICION_CENTRAL= 119; //la posicion arriba de la posicion central
	private final static byte F_ESQ_TOP_LEFT= 11;
	private final static byte F_ESQ_TOP_RIGHT= 22;
	private final static byte F_ESQ_BOTTOM_RIGHT= 33;
	private final static byte F_ESQ_BOTTOM_LEFT= 44;
	private final static byte F_INTERIOR= 55;
	private final static byte F_BORDE_TOP= 66;
	private final static byte F_BORDE_RIGHT= 77;
	private final static byte F_BORDE_BOTTOM= 88;
	private final static byte F_BORDE_LEFT= 99;
	private final static byte GRIS=0;
	private final static byte MAX_ESTADOS_ROTACION= 4;
	private final static int CURSOR_INVALIDO= -5;
	private final static byte MAX_COLORES= 23;
	private final static String SECCIONES_SEPARATOR_EN_FILE= " ";
	private final static String FILE_EXT = ".txt";
	private final static String NAME_FILE_PIEZAS = "e2pieces" + FILE_EXT;
	private final static String NAME_FILE_SOLUCION = "solution/soluciones_P" + THIS_PROCESS + FILE_EXT;
	private final static String NAME_FILE_DISPOSICION = "solution/disposiciones_P" + THIS_PROCESS + FILE_EXT;
	private final static String NAME_FILE_STATUS = "status/status_saved_P" + THIS_PROCESS + FILE_EXT;
	private final static String NAME_FILE_PARCIAL_MAX = "status/parcialMAX_P" + THIS_PROCESS + FILE_EXT;
	private final static String NAME_FILE_DISPOSICIONES_MAX = "status/disposicionMAX_P" + THIS_PROCESS + FILE_EXT;
	private final static String NAME_FILE_PARCIAL = "status/parcial_P" + THIS_PROCESS + "";
	private final static String NAME_FILE_LIBRES_MAX = "status/libresMAX_P" + THIS_PROCESS + FILE_EXT;
	
	private static int LIMITE_RESULTADO_PARCIAL = 211; // posición por defecto
	private static int sig_parcial, cur_destino;
	public static long count_cycles;
	public static int cursor, mas_bajo, mas_alto, mas_lejano_parcial_max;
	
	/**
	 * Calculo la capacidad de la matriz de combinaciones de colores, desglozando la distribución en 4 niveles.
	 * Son 4 niveles porque la matriz de colores solo contempla colores top,right,bottom,left.
	 * Cantidad de combinaciones:
	 *  (int) ((MAX_COLORES * Math.pow(2, 5 * 0)) +
				(MAX_COLORES * Math.pow(2, 5 * 1)) +
				(MAX_COLORES * Math.pow(2, 5 * 2)) +
				(MAX_COLORES * Math.pow(2, 5 * 3)))  = 777975
	 *  donde MAX_COLORES = 23, y usando 5 bits para representar los 23 colores.
	 * 
	 * Cada indice del arreglo definido en el orden (top,right,bottom,left) contiene instancia de NodoPosibles 
	 * la cual brinda arrays de piezas y rotaciones que cumplen con esa combinación particular de colores.
	 * 
	 * After getting some stats:
	 *   - array length          = 777975  (last used index is 777974)
	 *   - total empty indexes   = 771021
	 *   - total used indexes    =   6954
	 *   - wasted indexes        =  99.1%  <= but using an array has faster reads than a map :(
	 * Ver archivo misc/super_matriz_sizes_by_index.txt
	 * 
	 * IMPROVEMENT FINAL (CURRENT): 
	 * Then, I realize that just using a 4 dimensional array I end up with 331776‬ indexes which is the 43% of 777975.
	 * It uses less memory and the access time is the same than the previous big array.
	 * 
	 * IMPROVEMENT FINAL (50% less memory but too slow):
	 * Using a pre calculated Perfect Hash Function I ended up with an array size of PerfectHashFunction.PHASHRANGE.
	 */
//    private final static NodoPosibles[][][][] super_matriz = new NodoPosibles
//            [MAX_COLORES+1][MAX_COLORES+1][MAX_COLORES+1][MAX_COLORES+1];
	private final static NodoPosibles[] super_matriz = new NodoPosibles[PerfectHashFunction.PHASHRANGE];
	
	public final static Pieza[] piezas = new Pieza[MAX_PIEZAS];
	public final static Pieza[] tablero = new Pieza[MAX_PIEZAS];
	private final static short[] desde_saved = new short[MAX_PIEZAS];
	private final static Contorno contorno = new Contorno();
	private final static byte[] matrix_zonas = new byte[MAX_PIEZAS];
	
	// cada posición es un entero donde se usan 23 bits para los colores donde un bit valdrá 0 si ese 
	// color (right en borde left) no ha sido exlorado para la fila actual, sino valdrá 1.
	private final static int[] arr_color_rigth_explorado = new int[LADO];
	
	private static boolean status_cargado, retroceder, FairExperimentGif;
	private static boolean mas_bajo_activo, flag_retroceder_externo, usar_poda_color_explorado;
	private static boolean send_mail = false;
	private final static boolean[] zona_read_contorno = new boolean[MAX_PIEZAS]; //arreglo de zonas permitidas para reguntar por contorno used
	private final static boolean[] zona_proc_contorno = new boolean[MAX_PIEZAS]; //arreglo de zonas permitidas para usar y liberar contornos
	
	private static long time_inicial; //sirven para calcular el tiempo al hito de posición lejana
	private static long time_status_saved; //usado para calcular el tiempo entre diferentes status saved

	private static StringBuilder printBuffer = new StringBuilder(64);
	
	/**
	 * Algoritmo backtracker.
	 * 
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
	 * @param p_poda_color_explorado: poda donde solamente se permite explorar una sola vez el color right de la pieza en borde left.
	 * @param p_pos_multi_process: posición en tablero donde inicia exploración multi threading.
	 * @param totalProcesses: total number of processes.
	 */
	public SolverFasterMPJE (long m_ciclos, int lim_max_par, int lim_exploracion, int max_parciales, int destino_ret, 
			boolean usar_tableboard, boolean usar_multiples_boards, int cell_pixels_lado, int p_refresh_millis, 
			boolean p_fair_experiment_gif, boolean p_poda_color_explorado, int p_pos_multi_process,
			int totalProcesses) {

		MAX_CICLOS= m_ciclos;
		
		POSICION_MULTI_PROCESSES = p_pos_multi_process;
		num_processes_orig = new int[MAX_PIEZAS];
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL= lim_max_par;
		
		//LIMITE_DE_EXPLORACION= lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia
		
		FairExperimentGif = p_fair_experiment_gif;
		
		retroceder= false; //variable para indicar que debo volver estados de backtracking
		if (destino_ret >= 0){
			DESTINO_RET= destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo= true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
		cur_destino= CURSOR_INVALIDO; //variable para indicar hasta que posicion debo retroceder
		mas_bajo_activo= false; //permite o no modificar el cursor mas_bajo
		usar_poda_color_explorado = p_poda_color_explorado; //indica si se usará la poda de colores right explorados en borde left
		sig_parcial= 1; //esta variable indica el numero de archivo parcial siguiente a guardar
		MAX_NUM_PARCIAL= max_parciales; //indica hasta cuantos archivos parcial.txt voy a tener
		ESQUINA_TOP_RIGHT= LADO - 1;
		ESQUINA_BOTTOM_RIGHT= MAX_PIEZAS - 1;
		ESQUINA_BOTTOM_LEFT= MAX_PIEZAS - LADO;
		
		int procMultipleBoards = 0; // por default solo el primer proceso muestra el tableboard
		// si se quiere mostrar multiple tableboards entonces hacer que el target proc sea este mismo proceso 
		if (usar_multiples_boards)
			procMultipleBoards = THIS_PROCESS;
		
		if (usar_tableboard && !flag_retroceder_externo && THIS_PROCESS == procMultipleBoards) {
			ViewEternityFactory viewFactory = new ViewEternityMPJEFactory(LADO, cell_pixels_lado, 
					MAX_COLORES, (long)p_refresh_millis, THIS_PROCESS, totalProcesses);
			tableboardE2 = new EternityII(viewFactory);
		}

		createDirs();
	}
	
	
	//##########################################################################//
	//     INICIALIZACION DE CUALQUIER ESTRUCTURA QUE LO NECESITE
	//##########################################################################//

	private final static void createDirs() {
		new File("solution").mkdirs();
		new File("status").mkdirs();
	}

	/**
	 * Inicializa varias estructuras y flags
	 */
	public final void setupInicial () {
		
		//cargo en el arreglo matrix_zonas valores que me indiquen en qué posición estoy (borde, esquina o interior) 
		inicializarMatrixZonas();
		
		//seteo las posiciones donde se puede preguntar por contorno superior usado
		inicializarZonaReadContornos();
		
		//seteo las posiciones donde puedo setear un contorno como usado o libre
		inicializarZonaProcContornos();
		
		cargarPiezas();
		
		//hago una verificacion de las piezas cargadas
		verificarTiposDePieza();
		
		//cargar la super estructura 4-dimensional que agiliza la búsqueda de piezas
		cargarSuperEstructura();
		
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
		cargarPiezasFijas();
		
		//seteo como usados los contornos ya existentes en tablero
		Contorno.inicializarContornos(contorno, tablero, MAX_PIEZAS);
		
		if (tableboardE2 != null) 
			tableboardE2.startPainting();
		
		// this call avoids a OutOfHeapMemory error
		System.gc();
	}

	/**
	 * Este arreglo representa las zonas del tablero: las 4 esquinas, los 4 bordes
	 * y la zona interior.
	 */
	private final static void inicializarMatrixZonas ()
	{
		for (int k=0; k < MAX_PIEZAS; ++k)
		{
			matrix_zonas[k]= F_INTERIOR; //primero asumo que estoy en posicion interior
			//esquina top-left
			if (k == 0)
				matrix_zonas[k]= F_ESQ_TOP_LEFT;
			//esquina top-right
			else if (k == ESQUINA_TOP_RIGHT)
				matrix_zonas[k]= F_ESQ_TOP_RIGHT;
			//esquina bottom-right
			else if (k == ESQUINA_BOTTOM_RIGHT)
				matrix_zonas[k]= F_ESQ_BOTTOM_RIGHT;
			//esquina bottom-left
			else if (k == ESQUINA_BOTTOM_LEFT)
				matrix_zonas[k]= F_ESQ_BOTTOM_LEFT;
			//borde top
			else if ((k > 0) && (k < ESQUINA_TOP_RIGHT))
				matrix_zonas[k]= F_BORDE_TOP;
			//borde right
			else if (((k+1) % LADO)==0){
				if ((k != ESQUINA_TOP_RIGHT) && (k != ESQUINA_BOTTOM_RIGHT))
					matrix_zonas[k]= F_BORDE_RIGHT;
			}
			//borde bottom
			else if ((k > ESQUINA_BOTTOM_LEFT) && (k < ESQUINA_BOTTOM_RIGHT))
				matrix_zonas[k]= F_BORDE_BOTTOM;
			//borde left
			else if ((k % LADO)==0){
				if ((k != 0) && (k != ESQUINA_BOTTOM_LEFT))
					matrix_zonas[k]= F_BORDE_LEFT;
			}
		}
	}
	
	/**
	 * El arreglo zona_read_contorno[] me dice en qué posiciones puedo leer un contorno para chequear si es usado o no.
	 */
	private final static void inicializarZonaReadContornos()
	{	
		for (int k=0; k < MAX_PIEZAS; ++k)
		{
			//inicializo en false
			zona_read_contorno[k] = false;
			int fila_actual = k / LADO;
			
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < LADO || k > (MAX_PIEZAS-LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % LADO)==0) || ((k % LADO)==0) )
				continue;
			
			//Hasta aqui estoy en el interior del tablero
			
			//me aseguro que no llegue ni sobrepase el borde right
			if ((k + (Contorno.MAX_COLS-1)) < ((fila_actual*LADO) + (LADO-1)))
				zona_read_contorno[k] = true;
		}
	}
	
	/**
	 * El arreglo zonas_proc_contorno[] me dice en qué posiciones puedo procesar un contorno superior 
	 * e inferior para setearlo como usado o libre. Solamente sirve a dichos fines, ningún otro.
	 * NOTA: para contorno inferior se debe chequear a parte que cursor sea [33,238].
	 */
	private final static void inicializarZonaProcContornos()
	{
		for (int k=0; k < MAX_PIEZAS; ++k)
		{
			//inicializo en false
			zona_proc_contorno[k] = false;
			int fila_actual = k / LADO;
			
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < LADO || k > (MAX_PIEZAS-LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % LADO)==0) || ((k % LADO)==0) )
				continue;
			
			//Hasta aqui estoy en el interior del tablero
			
			//me aseguro que no esté cerca del borde left
			if (((k - Contorno.MAX_COLS) / LADO) != fila_actual)
				continue;
			
			zona_proc_contorno[k] = true;
		}
		
		System.out.println("Rank " + THIS_PROCESS + ": Usando restriccion de contornos de " + Contorno.MAX_COLS + " columnas.");
		System.out.flush();
	}
	
	/**
	 * Carga cada entrada de la matriz con los indices de las piezas que 
	 * tienen tales colores en ese orden.
	 */
	private final static void cargarSuperEstructura ()
	{
		time_inicial=System.nanoTime();
		
		llenarSuperEstructura();
		
		System.out.println("Rank " + THIS_PROCESS + ": carga de estructura 4-Dimensional finalizada (" + TimeUnit.MICROSECONDS.convert(System.nanoTime() - time_inicial, TimeUnit.NANOSECONDS) + " microsecs)");
		
		// combinación de 2 colores consecutivos con mayor número de coincidencias para piezas interiores y para todos los tipos a la vez.
		/*int maxTotal = 0, maxInterior = 0, key = 0;
		for (int k=0; k < 4; ++k){
			for (int i=0; i < MAX_COLORES; ++i)
				for (int j=0; j < MAX_COLORES; ++j)
				{
					switch (k){
						case 0: key = MapaKeys.getKey(j,MAX_COLORES,MAX_COLORES,i); break;
						case 1: key = MapaKeys.getKey(i,j,MAX_COLORES,MAX_COLORES); break;
						case 2: key = MapaKeys.getKey(MAX_COLORES,i,j,MAX_COLORES); break;
						case 3: key = MapaKeys.getKey(MAX_COLORES,MAX_COLORES,i,j); break;
						default:break;
					}
					// al no filtrar los valores de i y j puedo tener keys inválidas
					if (key >= super_matriz.length)
						continue;
					if (super_matriz[key] != null){
						// cuento cuantas piezas son interiores
						Pieza[] arrRef = super_matriz[key].referencias;
						int parcialC = 0;
						for (int pp=0, ppn=arrRef.length; pp < ppn; ++pp)
							if (arrRef[pp].es_interior)
								++parcialC;
						maxInterior = Math.max(maxInterior, parcialC);
						// total de piezas de todo tipo
						maxTotal = Math.max(maxTotal, super_matriz[key].referencias.length);
					}
				}
		}
		System.out.println("Maximo num de piezas (incluso rotadas) para 2 colores consecutivos: Total " + maxTotal + ", Interiores " + maxInterior);*/
	}
	
	/**
	 * Para cada posible combinacion entre los colores de la secciones top, 
	 * right, bottom y left creo un vector que contendrá las piezas que tengan
	 * esa combinacion de colores en dichas secciones y ademas guardo en que
	 * estado de rotacion la cumplen.
	 */
	private static final void llenarSuperEstructura ()
	{
		// itero sobre el arreglo de piezas
		for (short k = 0; k < MAX_PIEZAS; ++k) {
			
			if (k == INDICE_P_CENTRAL)
				continue;
			
			Pieza pz = piezas[k];
			
			//guardo la rotación de la pieza
			byte temp_rot = pz.rotacion;
			//seteo su rotación en 0. Esto es para generar la matriz siempre en el mismo orden
			Pieza.llevarARotacion(pz, (byte)0);
			
			for (byte rot=0; rot < MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (FairExperimentGif && (pz.top == pz.bottom))
					continue;
				
				//este caso es cuando tengo los 4 colores
				if (getNodoP(pz.top, pz.right, pz.bottom, pz.left) == null)
					setNewNodoP(pz.top, pz.right, pz.bottom, pz.left);
				NodoPosibles.addReferencia(getNodoP(pz.top, pz.right, pz.bottom, pz.left), k, rot);
				
				//tengo tres colores y uno faltante
				if (getNodoP(MAX_COLORES, pz.right, pz.bottom, pz.left) == null)
					setNewNodoP(MAX_COLORES, pz.right, pz.bottom, pz.left);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, pz.right, pz.bottom, pz.left), k, rot);
				
				if (getNodoP(pz.top, MAX_COLORES, pz.bottom, pz.left) == null)
					setNewNodoP(pz.top, MAX_COLORES, pz.bottom, pz.left);
				NodoPosibles.addReferencia(getNodoP(pz.top, MAX_COLORES, pz.bottom, pz.left), k, rot);
				
				if (getNodoP(pz.top, pz.right, MAX_COLORES, pz.left) == null)
					setNewNodoP(pz.top, pz.right, MAX_COLORES, pz.left);
				NodoPosibles.addReferencia(getNodoP(pz.top, pz.right, MAX_COLORES, pz.left), k, rot);
				
				if (getNodoP(pz.top ,pz.right, pz.bottom, MAX_COLORES) == null)
					setNewNodoP(pz.top ,pz.right, pz.bottom, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(pz.top ,pz.right, pz.bottom, MAX_COLORES), k, rot);
				
				//tengo dos colores y dos faltantes
				if (getNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left) == null)
					setNewNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left), k, rot);
				
				if (getNodoP(MAX_COLORES, pz.right, MAX_COLORES, pz.left) == null)
					setNewNodoP(MAX_COLORES, pz.right, MAX_COLORES, pz.left);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, pz.right, MAX_COLORES, pz.left), k, rot);
				
				if (getNodoP(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES) == null)
					setNewNodoP(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES), k, rot);
				
				if (getNodoP(pz.top, MAX_COLORES, MAX_COLORES, pz.left) == null)
					setNewNodoP(pz.top, MAX_COLORES, MAX_COLORES, pz.left);
				NodoPosibles.addReferencia(getNodoP(pz.top, MAX_COLORES, MAX_COLORES, pz.left), k, rot);
				
				if (getNodoP(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES) == null)
					setNewNodoP(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES), k, rot);
				
				if (getNodoP(pz.top, pz.right, MAX_COLORES, MAX_COLORES) == null)
					setNewNodoP(pz.top, pz.right, MAX_COLORES, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(pz.top, pz.right, MAX_COLORES, MAX_COLORES), k, rot);

				//tengo un color y tres faltantes
				if (getNodoP(pz.top, MAX_COLORES, MAX_COLORES, MAX_COLORES) == null)
					setNewNodoP(pz.top, MAX_COLORES, MAX_COLORES, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(pz.top, MAX_COLORES, MAX_COLORES, MAX_COLORES), k, rot);
				
				if (getNodoP(MAX_COLORES,pz.right, MAX_COLORES, MAX_COLORES) == null)
					setNewNodoP(MAX_COLORES,pz.right, MAX_COLORES, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES,pz.right, MAX_COLORES, MAX_COLORES), k, rot);
				
				if (getNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, MAX_COLORES) == null)
					setNewNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, MAX_COLORES);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, MAX_COLORES), k, rot);
				
				if (getNodoP(MAX_COLORES, MAX_COLORES, MAX_COLORES, pz.left) == null)
					setNewNodoP(MAX_COLORES, MAX_COLORES, MAX_COLORES, pz.left);
				NodoPosibles.addReferencia(getNodoP(MAX_COLORES, MAX_COLORES, MAX_COLORES, pz.left), k, rot);
			}
			
			//restauro la rotación
			Pieza.llevarARotacion(pz, temp_rot);
		}
	}

	private final static NodoPosibles getNodoP(final byte top, final byte right, final byte bottom, final byte left)
	{
//		return super_matriz[top][right][bottom][left];
		int key = NodoPosibles.getKey(top, right, bottom, left);
		return super_matriz[PerfectHashFunction.hash(key)];
	}
	
	private final static void setNewNodoP(final byte top, final byte right, final byte bottom, final byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		NodoPosibles nodoPosibles = NodoPosibles.newForKey(key);
//		super_matriz[top][right][bottom][left] = nodoPosibles;
		super_matriz[PerfectHashFunction.hash(key)] = nodoPosibles;
	}
	
	/**
	 * La exploracion ha alcanzado su punto limite, ahora es necesario guardar
	 * estado, mandarlo por mail, y avisar tambien por mail que esta instancia
	 * ha finalizado su exploracion asignada.
	 */
	/*private final static void operarSituacionLimiteAlcanzado (){
		guardarEstado(NAME_FILE_STATUS);
		guardarEstado(NAME_FILE_STATUS_COPY);
		
		System.out.println("El caso " + CASO + " ha llegado a su limite de exploracion. Exploracion finalizada.");
		
		if (send_mail){
			SendMail em= new SendMail();
			em.setDatos("El caso " + CASO + " ha llegado a su limite de exploracion.", "Exploracion finalizada: " + CASO);
			Thread t= new Thread(em);
			t.start();
		}
	}*/
	
	/**
	 * Carga las piezas desde el archivo NAME_FILE_PIEZAS
	 */
	private final static void cargarPiezas () {
		
		BufferedReader reader = null;
		
		try {
			// reader= new BufferedReader(new FileReader(NAME_FILE_PIEZAS));
			reader = new BufferedReader(new InputStreamReader(SolverFasterMPJE.class.getClassLoader().getResourceAsStream(NAME_FILE_PIEZAS)));
			String linea= reader.readLine();
			short num=0;
			while (linea != null){
				if (num >= MAX_PIEZAS) 
					throw new Exception("ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
				piezas[num]= PiezaFactory.from(linea, num);
                //PiezaFactory.setFromStringWithNum(linea, num, piezas[num]);
				linea= reader.readLine();
				++num;
			}

			if (num != MAX_PIEZAS)
				throw new Exception("ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
		}
		catch (Exception exc){
			System.out.println(exc.getMessage());
			throw new RuntimeException(exc);
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
	 * En este metodo se setean las piezas que son fijas al juego. Por ahora solo existe
	 * una sola pieza fija y es la pieza numero 139 en las posicion 136 real (135 para el
	 * algoritmo)
	 */
	private final static void cargarPiezasFijas () {
		
		Pieza piezaCentral = piezas[INDICE_P_CENTRAL];
		piezaCentral.usada= true;
		//piezaCentral.pos= POSICION_CENTRAL;
		tablero[POSICION_CENTRAL]= piezaCentral; // same value than INDICE_P_CENTRAL
		
		System.out.println("Rank " + THIS_PROCESS + ": pieza Fija en posicion " + (POSICION_CENTRAL + 1) + " cargada en tablero");
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
				System.out.println("Rank " + THIS_PROCESS + " >>> estado de exploracion no existe.");
				System.out.flush();
				return status_cargado;
			}
			
			reader= new BufferedReader(new FileReader(f));
			String linea= reader.readLine();
			
			if (linea==null)
				throw new Exception("First line is null.");
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
				for (int k=0; k < MAX_PIEZAS; ++k){
					if (k==(MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
					tablero[k]= numPieza == -1 ? null : piezas[numPieza];
				}
				
				// recorro los valores de desde_saved[]
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < MAX_PIEZAS; ++k){
					if (k==(MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
					desde_saved[k] = numPieza;
				}
				
				//la siguiente línea indica si se estaba usando poda de color explorado
				linea= reader.readLine();
				// recorro los valores de matrix_color_explorado[]
				if (usar_poda_color_explorado){
					if (Boolean.parseBoolean(linea)){
						//leo la info de matriz_color_explorado
						linea= reader.readLine();
						sep=0; sep_ant=0;
						for (int k=0; k < LADO; ++k){
							if (k==(LADO-1))
								sep= linea.length();
							else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
							int val= Integer.parseInt(linea.substring(sep_ant,sep));
							sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
							arr_color_rigth_explorado[k] = val;
						}
					}
				}
				
				cargarPiezas(); //creo las piezas desde el archivo de piezas
				
				//las restantes MAX_PIEZAS lineas contienen el estado de rotación y de usada de cada pieza
				int pos=0; //cuento cuantas lineas voy procesando
				String splitted[];
				linea= reader.readLine(); //info de la primer pieza
				while ((linea != null) && (pos < MAX_PIEZAS)){
					splitted = linea.split(SECCIONES_SEPARATOR_EN_FILE);
					Pieza.llevarARotacion(piezas[pos],Byte.parseByte(splitted[0]));
					piezas[pos].usada = Boolean.parseBoolean(splitted[1]);
					linea= reader.readLine();
					++pos;
				}
				if (pos != MAX_PIEZAS){
					System.out.println("Rank " + THIS_PROCESS + ": ERROR. La cantidad de piezas en el archivo " + n_file + " no coincide con el numero de piezas que el juego espera.");
					throw new Exception("Inconsistent number of pieces.");
				}
					
				status_cargado=true;
				sincronizar = false;
				System.out.println("Rank " + THIS_PROCESS + ": estado de exploracion (" + n_file + ") cargado.");
				System.out.flush();
			}
		}
		catch(Exception e){
			System.out.println("Rank " + THIS_PROCESS + ": estado de exploracion no existe o esta corrupto: " + e.getMessage());
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
	private final static void retrocederEstado (){
		retroceder= true;
		cur_destino= DESTINO_RET;
		
		while (cursor>=0){
			if (!retroceder){
				mas_bajo_activo= true;
				mas_bajo= cursor;
				guardarEstado(NAME_FILE_STATUS);
				guardarResultadoParcial(false);
				System.out.println("Rank " + THIS_PROCESS + ": Exploracion retrocedio a la posicion " + cursor + ". Estado salvado.");
				System.out.flush();
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			--cursor;
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (cursor < 0)
				break; //obliga a salir del while
			if (cursor != POSICION_CENTRAL){
				Pieza pzz= tablero[cursor];
				pzz.usada= false; //la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				//pzz.pos= -1;
				tablero[cursor]= null;
			}
			//si retrocedió hasta el cursor destino, entonces no retrocedo mas
			if ((cursor+1) <= cur_destino){
				retroceder= false;
				cur_destino= CURSOR_INVALIDO;
			}
			//si está activado el flag para retroceder niveles de exploración entonces debo limpiar algunas cosas
			if (retroceder)
				desde_saved[cursor]= 0; //la exploración de posibles piezas para la posicion cursor debe empezar desde la primer pieza
		}
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
		
		time_inicial = time_status_saved = System.nanoTime();
		
		//si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado)
			explorar();
		//se carga estado de exploración, debo proveer la posibilidad de volver estados anteriores de exploracion
		else{
			//ahora exploro comunmente y proveo una especie de recursividad para retroceder estados
			while (cursor >= 0){
				if (!retroceder){
					//pregunto si llegué al limite de esta instancia de exploracion
					/*if (cursor <= LIMITE_DE_EXPLORACION){
						operarSituacionLimiteAlcanzado();
						return;
					}*/
					//creo una nueva instancia de exploracion
					explorar();
				}
				--cursor;
				
				//si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				//seteo los contornos como libres si no están usados
				setContornoLibre(cursor);

				//debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != POSICION_CENTRAL){
					Pieza p = tablero[cursor];
					p.usada= false;
					//p.pos= -1;
					tablero[cursor]= null;
				}
				
				//si retrocedí hasta el cursor destino, entonces no retrocedo mas
				/*if (cursor <= cur_destino){
					retroceder= false;
					cur_destino= CURSOR_INVALIDO;
				}
				//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
				if (retroceder)
					desde_saved[cursor]= 0;*/ //la exploracion de posibles piezas para la posicion cursor debe empezar desde la primer pieza
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println("Rank " + THIS_PROCESS + ": exploración agotada.");

		if (send_mail) { // Envio un mail diciendo que no se encontró solución
			SendMail em = new SendMail();
			em.setDatos("Rank " + THIS_PROCESS + ": exploración agotada.", "Rank " + THIS_PROCESS + " exploracion agotada");
			Thread t= new Thread(em);
			t.start();
		}
	}
	
	
	//##########################################################################//
	// METODO CENTRAL. ES EL BACKTRACKING QUE RECORRE EL TABLERO Y COLOCA PIEZAS
	//##########################################################################//
	
	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final static void explorar ()
	{
		/*@FILAS_PRECALCULADASif (!combs_hechas && cursor >= POSICION_CALCULAR_FILAS){
			//genero el arreglo de combinaciones de filas
			calcularFilasDePiezas();
			combs_hechas = true;
		}
		else if (cursor < POSICION_CALCULAR_FILAS)
			combs_hechas = false; //seteando en false obligo a que se recalculen las filas*/
		
		//#############################################################################################
		/**
		 * Cabeza de exploración.
		 * Representa las primeras sentencias del backtracking de exploracion. 
		 * Pregunta algunas cositas antes de empezar una nueva instancia de exploración.
		 */
		//#############################################################################################
		
		//si cursor se pasó del limite de piezas, significa que estoy en una solucion
		if (cursor >= MAX_PIEZAS){
			guardarSolucion();
			System.out.println("Rank " + THIS_PROCESS + ": Solucion Encontrada!!");
			System.out.flush();
			return; //evito que la instancia de exporacion continue
		}
		
		//si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor > mas_lejano_parcial_max){
			mas_lejano_parcial_max= cursor;
			if (cursor >= LIMITE_RESULTADO_PARCIAL){
				long time_final= System.nanoTime();
				printBuffer.setLength(0);
				printBuffer.append("Rank ").append(THIS_PROCESS).append(": ")
					.append(TimeUnit.MILLISECONDS.convert(time_final - time_inicial, TimeUnit.NANOSECONDS))
					.append(" ms, cursor ").append(cursor);
				System.out.println(printBuffer.toString());
				System.out.flush();
				printBuffer.setLength(0);
				guardarResultadoParcial(true);
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
			mas_bajo= MAX_PIEZAS;
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
			guardarEstado(NAME_FILE_STATUS);
			guardarResultadoParcial(false);
			printBuffer.setLength(0);
			printBuffer.append("Rank ").append(THIS_PROCESS).append(": Estado guardado en cursor ").append(cursor)
					.append(". Pos Min ").append(mas_bajo).append(", Pos Max ").append(mas_alto)
					.append(". Tiempo: ").append(durationMillis).append(" ms") 
					.append(", ").append(piecesPerSec).append(" pieces/sec");
			System.out.println(printBuffer.toString());
			System.out.flush();
			printBuffer.setLength(0);
			time_status_saved = nanoTimeNow;
			//cuando se cumple el ciclo aumento de nuevo el valor de mas_bajo y disminuyo el de mas_alto
			mas_bajo= MAX_PIEZAS;
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
		
		//si la posicion cursor es una posicion fija no tengo que hacer la exploracion "estandar". Se supone que la pieza fija ya está debidamente colocada
		if (cursor == POSICION_CENTRAL){
			//seteo los contornos como usados
			setContornoUsado(cursor);
			++cursor;
			explorar();
			--cursor;
			//seteo los contornoscomo libres
			setContornoLibre(cursor);
			/*if (cursor <= cur_destino){
				retroceder= false;
				cur_destino=CURSOR_INVALIDO;
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
		if (esContornoSuperiorUsado(cursor))
			return;
		
		// sincronización de los procesos (Knock Knock) una única vez
		if (sincronizar && (cursor == POSICION_MULTI_PROCESSES)) {
			sincronizar = false; // no volver a sincronizar
			knocKnock();
		}

		//pregunto si estoy en una posicion donde puedo preguntar por filas libres y/o cargar fila
		/*@FILAS_PRECALCULADASif (zonas_cargar_fila[cursor]){
			//cargo fila precalculadas
			if (cargarFilasGuardadas() == false)
				return;
		}*/
		
		//#############################################################################################
		
		//#############################################################################################
		//ahora hago la exploracion
		exploracionStandard();
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
			System.out.println("Rank " + THIS_PROCESS + ": --- Esperando sincronizarse...");
			System.out.flush();
			mpi_send_info[0] = MESSAGE_HALT;
			// espero recibir mensaje
			mpi.MPI.COMM_WORLD.Recv(mpi_send_info, 0, mpi_send_info.length, mpi.MPI.INT, mpi.MPI.ANY_SOURCE, TAG_SINCRO); //el tag identifica al mensaje
			if (mpi_send_info[0] == MESSAGE_SINCRO) {
				System.out.println("Rank " + THIS_PROCESS + ": --- Sincronizado with msg " + mpi_send_info[0]);
				System.out.flush();
			}
			else if (mpi_send_info[0] == MESSAGE_HALT)
				;
		}
		
		System.out.println("Rank " + THIS_PROCESS + ": --- Continua procesando.");
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
	 */
	private final static void exploracionStandard ()
	{
		// voy a recorrer las posibles piezas que coinciden con los colores de las piezas alrededor de cursor
		final NodoPosibles nodoPosibles = obtenerPosiblesPiezas(cursor);
		if (nodoPosibles == null)
			return; // significa que no existen posibles piezas para la actual posicion de cursor

		int desde = desde_saved[cursor];
		int length_posibles = nodoPosibles.referencias.length;
		final byte flag_zona = matrix_zonas[cursor];
		
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
			Pieza p = piezas[nodoPosibles.referencias[desde]];
			byte rot = nodoPosibles.rots[desde];
			
			// pregunto si la pieza candidata está siendo usada
			if (p.usada)
				continue; // es usada, pruebo con la siguiente pieza
		
			++count_cycles; // incremento el contador de combinaciones de piezas
			
			// Pregunto si la pieza a poner es del tipo adecuado segun cursor.
			// Porque sucede que puedo obtener cualquier tipo de pieza de acuerdo a los colores que necesito empiezo con
			// la más común que es interior
			if (flag_zona == F_INTERIOR ) {
				// si pieza actual no es interior
				if (p.feature != 0) continue;
			}
			// mayor a F_INTERIOR significa que estoy en borde
			else if (flag_zona > F_INTERIOR) {
				// si pieza actual no es borde
				if (p.feature != 1) continue;
			}
			// menor a F_INTERIOR significa que estoy en esquina
			else {
				// si pieza actual no es esquina
				if (p.feature != 2) continue;
			}
			
			// pregunto si está activada la poda del color right explorado en borde left
			if (usar_poda_color_explorado)
			{
				final int fila_actual = cursor >> LADO_SHIFT_AS_DIVISION; // if divisor is power of 2 then we can use >>
			
				// For modulo try this for better performance only if divisor is power of 2 and dividend is positive: dividend & (divisor - 1)
				// old was: ((cursor+2) % LADO) == 0
				final boolean flag_antes_borde_right = ((cursor + 2) & (LADO - 1)) == 0;

				// si estoy antes del borde right limpio el arreglo de colores right usados
				if (flag_antes_borde_right)
					arr_color_rigth_explorado[fila_actual + 1] = 0;
				
				if (flag_zona == F_BORDE_LEFT)
				{
					final int mask = 1 << p.right;
					
					// pregunto si el color right de la pieza de borde left actual ya está explorado
					if ((arr_color_rigth_explorado[fila_actual] & mask) != 0) {
						p.usada = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue; // sigo con otra pieza de borde
					}
					// si no es así entonces lo seteo como explorado
					else {
						arr_color_rigth_explorado[fila_actual] |= mask;
						// int value = arr_color_rigth_explorado.get(fila_actual) | 1 << p.right;
						// arr_color_rigth_explorado.getAndSet(fila_actual, value);
					}
				}
			}
			
			//#### En este punto ya tengo la pieza correcta para poner en tablero[cursor] ####
			
			tablero[cursor] = p; // en la posicion "cursor" del tablero pongo la pieza
			p.usada = true; // en este punto la pieza va a ser usada
			Pieza.llevarARotacion(p, rot);
			//p.pos= cursor; //la pieza sera usada en la posicion cursor
			
			//#### En este punto ya tengo la pieza colocada y rotada correctamente ####

			// una vez rotada adecuadamente la pieza pregunto si el borde inferior que genera está siendo usado
			/*@CONTORNO_INFERIORif (esContornoInferiorUsado(cursor)){
				p.usada = false; //la pieza ahora no es usada
				//p.pos= -1;
				continue;
			}*/
			
			// FairExperiment.gif: color bottom repetido en sentido horizontal
			if (FairExperimentGif){
				if (flag_zona == F_INTERIOR || flag_zona == F_BORDE_TOP)
					if (p.bottom == tablero[cursor-1].bottom){
						p.usada = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue;
					}
			}

			// seteo los contornos como usados
			setContornoUsado(cursor);
				
			//##########################
			// Llamo una nueva instancia
			++cursor;
			explorar();
			--cursor;
			//##########################
				
			// seteo los contornos como libres
			setContornoLibre(cursor);
			
			p.usada = false; //la pieza ahora no es usada
			//p.pos= -1;
			
			// si retrocedió hasta la posicion destino, seteo la variable retroceder en false e invalído a cur_destino
			/*if (cursor <= cur_destino){
				retroceder= false;
				cur_destino=CURSOR_INVALIDO;
			}
			// caso contrario significa que todavia tengo que seguir retrocediendo
			if (retroceder)
				break;*/
		}
		

		desde_saved[cursor] = 0; //debo poner que el desde inicial para este cursor sea 0
		tablero[cursor] = null; //dejo esta posicion de tablero libre

		NUM_PROCESSES = num_processes_orig[cursor];
		--pos_multi_process_offset;
		if (pos_multi_process_offset < 0)
			pos_multi_process_offset = 0;
	}
	
	//##########################################################################//
	//##########################################################################//
	//          ALGUNOS METODOS QUE HACEN COSAS PARA LA EXPLORACION
	//##########################################################################//
	//##########################################################################//

	/**
	 * Dada la posicion de cursor se fija cuáles colores tiene alrededor y devuelve una referencia de NodoPosibles 
	 * que contiene las piezas que cumplan con los colores en el orden top-right-bottom-left (sentido horario).
	 *  
	 * NOTA: saqué muchas sentencias porque solamente voy a tener una pieza fija (135 en tablero), por eso 
	 * este metodo solo contempla las piezas top y left, salvo en el vecindario de la pieza fija.
	 */
	private final static NodoPosibles obtenerPosiblesPiezas (int _cursor)
	{
		switch (_cursor) {
			// estoy en la posicion inmediatamente arriba de la posicion central
			case SOBRE_POSICION_CENTRAL:
				return getNodoP(tablero[_cursor - LADO].bottom, MAX_COLORES, piezas[INDICE_P_CENTRAL].top, tablero[_cursor - 1].right);
			// estoy en la posicion inmediatamente a la izq de la posicion central
			case ANTE_POSICION_CENTRAL:
				return getNodoP(tablero[_cursor - LADO].bottom, piezas[INDICE_P_CENTRAL].left, MAX_COLORES, tablero[_cursor - 1].right);
		}
		
		final int flag_m = matrix_zonas[_cursor];
		
		// estoy en interior de tablero?
		if (flag_m == F_INTERIOR) 
			return getNodoP(tablero[_cursor - LADO].bottom, MAX_COLORES, MAX_COLORES, tablero[_cursor - 1].right);
		// mayor a F_INTERIOR significa que estoy en borde
		else if (flag_m > F_INTERIOR) {
			switch (flag_m) {
				//borde right
				case F_BORDE_RIGHT:
					return getNodoP(tablero[_cursor - LADO].bottom, GRIS, MAX_COLORES, tablero[_cursor - 1].right);
				//borde left
				case F_BORDE_LEFT:
					return getNodoP(tablero[_cursor - LADO].bottom, MAX_COLORES, MAX_COLORES, GRIS);
				// borde top
				case F_BORDE_TOP:
					return getNodoP(GRIS, MAX_COLORES, MAX_COLORES, tablero[_cursor - 1].right);
				//borde bottom
				default:
					return getNodoP(tablero[_cursor - LADO].bottom, MAX_COLORES, GRIS, tablero[_cursor - 1].right);
			}
		}
		// menor a F_INTERIOR significa que estoy en esquina
		else {
			switch (flag_m) {
				//esquina top-left
				case F_ESQ_TOP_LEFT:
					return getNodoP(GRIS, MAX_COLORES, MAX_COLORES, GRIS);
				//esquina top-right
				case F_ESQ_TOP_RIGHT:
					return getNodoP(GRIS, GRIS, MAX_COLORES, tablero[_cursor - 1].right);
				//esquina bottom-left
				case F_ESQ_BOTTOM_LEFT: 
					return getNodoP(tablero[_cursor - LADO].bottom, MAX_COLORES, GRIS, GRIS);
					//esquina bottom-right
				default:
					return getNodoP(tablero[_cursor - LADO].bottom, GRIS, GRIS, tablero[_cursor - 1].right);
			}
		}
	}
	
	private final static void setContornoUsado(int _cursor)
	{
		// primero me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_proc_contorno[_cursor] == true) {
			contorno.contornos_used[tablero[_cursor-1].left][tablero[_cursor-1].top][tablero[_cursor].top] = true;
		}
	}
	
	private final static void setContornoLibre(int _cursor)
	{
		// primero me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_proc_contorno[_cursor] == true) {
			contorno.contornos_used[tablero[_cursor-1].left][tablero[_cursor-1].top][tablero[_cursor].top] = false;
		}
	}

	private final static boolean esContornoSuperiorUsado(int _cursor)
	{
		// primero me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_read_contorno[_cursor] == true) {
			return contorno.contornos_used[tablero[_cursor-1].right][tablero[_cursor-LADO].bottom][tablero[_cursor-LADO + 1].bottom];
		}
		return false;
	}
	
	/**
	 * Veririfica que no exista pieza extraña o que falte alguna pieza. 
	 * Solo se usa al cargar las piezas desde archivo o al cargar estado.
	 */
	private final static void verificarTiposDePieza (){
		int n_esq= 0;
		int n_bordes= 0;
		int n_interiores= 0;
	
		for (int g=0; g < MAX_PIEZAS; ++g){
			Pieza pzx = piezas[g];
			if (pzx.feature == 0)
				++n_interiores;
			else if (pzx.feature == 1)
				++n_bordes;
			else if (pzx.feature == 2)
				++n_esq;
		}
		if ((n_esq != 4) || (n_bordes != (4*(LADO-2))) || (n_interiores != (MAX_PIEZAS - (n_esq + n_bordes)))) {
			System.out.println("ERROR. Existe una o varias piezas incorrectas.");
			throw new RuntimeException("ERROR. Existe una o varias piezas incorrectas.");
		}
	}
	
	
	
	//##########################################################################//
	//##########################################################################//
	//         METODOS cuyo fin es GUARDAR algo en archivos o en memoria
	//##########################################################################//
	//##########################################################################//
	
	/**
	 * Genera un archivo de piezas para leer con el editor visual e2editor.exe, otro archivo
	 * que contiene las disposiciones de cada pieza en el tablero, y otro archivo que me dice
	 * las piezas no usadas (generado solo si max es true).
	 * Si max es true, el archivo generado es el que tiene la mayor disposición de piezas encontrada.
	 * Si max es false, el archivo generado contiene la disposición de piezas en el instante cuando
	 * se guarda estado.
	 */
	private final static void guardarResultadoParcial (final boolean max){
		
		if (MAX_NUM_PARCIAL == 0 && !max)
			return;
		
		try {
			PrintWriter wParcial= null;
			// si estamos en max instance tenemos q guardar las disposiciones de las piezas
			PrintWriter wDispMax= null;
			StringBuilder parcialBuffer= new StringBuilder(256 * 13);
			StringBuilder dispMaxBuff= new StringBuilder(256 * 13);
			
			if (max){
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_PARCIAL_MAX)));
				wDispMax= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_DISPOSICIONES_MAX)));
				dispMaxBuff.append("(num pieza) (estado rotacion) (posicion en tablero real)").append("\n");
			}
			else{
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_PARCIAL+"_"+sig_parcial+".txt")));
				++sig_parcial;
				if (sig_parcial > MAX_NUM_PARCIAL)
					sig_parcial= 1;
			}
			
			for (int b=0; b<MAX_PIEZAS; ++b){
				int pos= b+1;
				Pieza p= tablero[b];
				if (p == null){
					parcialBuffer.append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append("\n");
					if (max)
						dispMaxBuff.append("-").append(SECCIONES_SEPARATOR_EN_FILE).append("-").append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
				}
				else {
					parcialBuffer.append(p.top).append(SECCIONES_SEPARATOR_EN_FILE).append(p.right).append(SECCIONES_SEPARATOR_EN_FILE).append(p.bottom).append(SECCIONES_SEPARATOR_EN_FILE).append(p.left).append("\n");
					if (max)
						dispMaxBuff.append(p.numero + 1).append(SECCIONES_SEPARATOR_EN_FILE).append(p.rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
			
			// guardar los libres solo si es max instance
			if (max)
				guardarLibres();
			
			//solo para instancia max: enviar email
			if (send_mail && max) {
				SendMail em1= new SendMail();
				SendMail em2= new SendMail();
				em1.setDatos(sParcial, NAME_FILE_PARCIAL_MAX);
				em2.setDatos(sDispMax, NAME_FILE_DISPOSICIONES_MAX);
				Thread t1= new Thread(em1);
				Thread t2= new Thread(em2);
				t1.start();
				t2.start();
			}
		}
		catch(Exception ex) {
			System.out.println("Rank " + THIS_PROCESS + ": ERROR: No se pudieron generar los archivos de resultado parcial.");
			System.out.println(ex);
		}
	}
	
	/**
	 * Me guarda en el archivo NAME_FILE_LIBRES_MAX las numeros de las piezas que quedaron libres.
	 */
	private final static void guardarLibres (){
		try{
			PrintWriter wLibres= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_LIBRES_MAX)));
			StringBuilder wLibresBuffer= new StringBuilder(256 * 13);
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(pzx.numero + 1).append("\n");
			}
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(PiezaStringer.toStringColores(pzx)).append("\n");
			}
			
			String sContent = wLibresBuffer.toString();
			wLibres.append(sContent);
			wLibres.flush();
			wLibres.close();
			
			if (send_mail) {
				SendMail em= new SendMail();
				em.setDatos(sContent, NAME_FILE_LIBRES_MAX);
				Thread t= new Thread(em);
				t.start();
			}
		}
		catch (Exception escp) {
			System.out.println("ERROR: No se pudo generar el archivo " + NAME_FILE_LIBRES_MAX);
			System.out.println(escp);
		}
	}
	
	/**
	 * En el archivo NAME_FILE_SOLUCION se guardan los colores de cada pieza.
	 * En el archivo NAME_FILE_DISPOSICION se guarda el numero y rotacion de cada pieza.
	 */
	private final static void guardarSolucion (){
		try{
			PrintWriter wSol= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_SOLUCION,true)));
			PrintWriter wDisp= new PrintWriter(new BufferedWriter(new FileWriter(NAME_FILE_DISPOSICION,true)));
			StringBuilder contenidoDisp= new StringBuilder(256 * 13);
			
			wSol.println("Solucion para " + MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + MAX_PIEZAS + " piezas.");
			contenidoDisp.append("Disposicion para " + MAX_PIEZAS + " piezas.\n");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			contenidoDisp.append("(num pieza) (estado rotacion) (posicion en tablero real)\n");
			
			for (int b=0; b<MAX_PIEZAS; ++b){
				Pieza p= tablero[b];
				int pos= b+1;
				wSol.println(p.top + SECCIONES_SEPARATOR_EN_FILE + p.right + SECCIONES_SEPARATOR_EN_FILE + p.bottom + SECCIONES_SEPARATOR_EN_FILE + p.left);
				wDisp.println((p.numero + 1) + SECCIONES_SEPARATOR_EN_FILE + p.rotacion + SECCIONES_SEPARATOR_EN_FILE + pos);
				contenidoDisp.append(p.numero + 1).append(SECCIONES_SEPARATOR_EN_FILE).append(p.rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
			
			//Sentencias para enviar email solucion
			if (send_mail) {
				SendMail em= new SendMail();
				em.setDatos(contenidoDisp.toString(), NAME_FILE_SOLUCION);
				Thread t= new Thread(em);
				t.start();
			}
		}catch(Exception ex){
			System.out.println("ERROR: No se pudo guardar la solucion!! QUE MACANA!!! (guardarSolucion())");
			System.out.println(ex);
		}
	}
	
	/**
	 * Guarda las estructuras necesaria del algoritmo para poder continuar desde el actual estado de exploración.
	 */
	private final static void guardarEstado (String f_name)
	{
		try{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(f_name)));
			StringBuilder writerBuffer = new StringBuilder(256 * 13);

			//guardo el valor de mas_bajo
			writerBuffer.append(mas_bajo).append("\n");
			
			//guardo el valor de mas_alto
			writerBuffer.append(mas_alto).append("\n");
			
			//guardo el valor de mas_lejano 
			writerBuffer.append(mas_lejano_parcial_max).append("\n");
			
			//guardo el valor del cursor
			writerBuffer.append(cursor).append("\n");
			
			//guardo los indices de tablero[]
			for (int n=0; n < MAX_PIEZAS; ++n) {
				if (n==(MAX_PIEZAS-1)){
					if (tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append(tablero[n].numero).append("\n");
				}
				else {
					if (tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append((tablero[n])).append(SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//########################################################################
			/**
			 * Calculo los valores para desde_saved[]
			 */
			//########################################################################
			int _cursor=0;
			for (; _cursor < cursor; ++_cursor) {
				if (_cursor == POSICION_CENTRAL) //para la pieza central no se tiene en cuenta su valor desde_saved[] 
					continue;
				//tengo el valor para desde_saved[]
				desde_saved[_cursor] = NodoPosibles.getUbicPieza(obtenerPosiblesPiezas(_cursor), tablero[_cursor].numero);
			}
			//ahora todo lo que está despues de cursor tiene que valer cero
			for (;_cursor < MAX_PIEZAS; ++_cursor)
				desde_saved[_cursor] = 0;
			//########################################################################
			
			//guardo las posiciones de posibles piezas (desde_saved[]) de cada nivel del backtracking
			for (int n=0; n < MAX_PIEZAS; ++n) {
				if (n==(MAX_PIEZAS-1))
					writerBuffer.append(desde_saved[n]).append("\n");
				else
					writerBuffer.append(desde_saved[n]).append(SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//indico si se utiliza poda de color explorado o no
			writerBuffer.append(usar_poda_color_explorado).append("\n");
			
			//guardo el contenido de matrix_color_explorado
			if (usar_poda_color_explorado) {
				for (int n=0; n < LADO; ++n)
					if (n==(LADO-1))
						writerBuffer.append(arr_color_rigth_explorado[n]).append("\n");
					else
						writerBuffer.append(arr_color_rigth_explorado[n]).append(SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//guardo el estado de rotación y el valor de usada de cada pieza
			for (int n=0; n < MAX_PIEZAS; ++n)
			{
				writerBuffer.append(piezas[n].rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(String.valueOf(piezas[n].usada)).append("\n");
			}
			
			String sContent = writerBuffer.toString();
			writer.append(sContent);
			writer.flush();
			writer.close();

			//Sentencias para enviar email status_saved
			if (send_mail) {
				SendMail em= new SendMail();
				em.setDatos(sContent, f_name);
				Thread t= new Thread(em);
				t.start();
			}
		}
		catch(Exception e){
			System.out.println("ERROR: No se pudo guardar el estado de la exploración.");
			System.out.println(e);
		}
	}
}
