/**
 * Copyright (c) 2015 Fabricio Lettieri fabri1983@gmail.com
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

package org.fabri1983.eternity2.forkjoin_solver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.MapaKeys;
import org.fabri1983.eternity2.core.NodoPosibles;
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.tilesreader.ReaderForTilesFile;

public final class SolverFaster {
	
	protected static int POSICION_START_FORK_JOIN = -1; //(99) posición del tablero en la que se aplica fork/join
	protected static int NUM_PROCESSES = 1;
	protected static ExploracionAction actions[];
	protected static CountDownLatch startSignal;
    protected static CountDownLatch doneSignal;
    
	protected static long MAX_CICLOS; // Número máximo de ciclos para guardar estado
	protected static int DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
	protected static int MAX_NUM_PARCIAL; // Número de archivos parciales que se generarón
	protected static int ESQUINA_TOP_RIGHT, ESQUINA_BOTTOM_RIGHT, ESQUINA_BOTTOM_LEFT;
	protected static int LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia
	protected final static int LADO= 16;
	protected final static int LADO_SHIFT_AS_DIVISION = 4;
	public final static int MAX_PIEZAS= 256;
	public final static int POSICION_CENTRAL= 135;
	public final static int POS_FILA_P_CENTRAL = 8;
	public final static int POS_COL_P_CENTRAL = 7;
	public final static int INDICE_P_CENTRAL= 138; // es la ubicación de la pieza central en piezas[]
	protected final static int ANTE_POSICION_CENTRAL= 134; // la posición inmediatamente anterior a la posicion central
	protected final static int SOBRE_POSICION_CENTRAL= 119; // la posición arriba de la posicion central
	protected final static byte F_ESQ_TOP_LEFT= 11;
	protected final static byte F_ESQ_TOP_RIGHT= 22;
	protected final static byte F_ESQ_BOTTOM_RIGHT= 33;
	protected final static byte F_ESQ_BOTTOM_LEFT= 44;
	protected final static byte F_INTERIOR= 55;
	protected final static byte F_BORDE_TOP= 66;
	protected final static byte F_BORDE_RIGHT= 77;
	protected final static byte F_BORDE_BOTTOM= 88;
	protected final static byte F_BORDE_LEFT= 99;
	protected final static byte GRIS=0;
	protected final static byte MAX_ESTADOS_ROTACION= 4;
	protected final static int CURSOR_INVALIDO= -5;
	protected final static byte MAX_COLORES= 23;
	protected final static String SECCIONES_SEPARATOR_EN_FILE= " ";
	protected final static String FILE_EXT = ".txt";
	protected final static String NAME_FILE_PIEZAS = "e2pieces" + FILE_EXT;
	protected final static String NAME_FILE_SOLUCION = "solution/soluciones";
	protected final static String NAME_FILE_DISPOSICION = "solution/disposiciones";
	protected final static String NAME_FILE_STATUS = "status/status_saved";
	protected final static String NAME_FILE_PARCIAL_MAX = "status/parcialMAX";
	protected final static String NAME_FILE_DISPOSICIONES_MAX = "status/disposicionMAX";
	protected final static String NAME_FILE_PARCIAL = "status/parcial";
	protected final static String NAME_FILE_LIBRES_MAX = "status/libresMAX";
	protected static int LIMITE_RESULTADO_PARCIAL = 211; // por defecto
	
	public static long count_cycles[]; // count cycles per task when usarTableroGrafico is true
	
	protected static long count_filas;
	protected final static byte matrix_zonas[] = new byte[MAX_PIEZAS];
	
	protected static AtomicIntegerArray arr_color_rigth_explorado; // cada posición es un entero donde se usan 23 bits para los colores donde un bit valdrá 0 si ese color (right en borde left) no ha sido exlorado para la fila actual, sino valdrá 1
	protected static boolean retroceder, FairExperimentGif, usarTableroGrafico;
	protected static int cellPixelsLado, tableboardRefreshMillis;
	protected static boolean flag_retroceder_externo, usar_poda_color_explorado;
	protected final static boolean zona_read_contorno[] = new boolean[MAX_PIEZAS]; // arreglo de zonas permitidas para preguntar por contorno used
	protected final static boolean zona_proc_contorno[] = new boolean[MAX_PIEZAS]; // arreglo de zonas permitidas para usar y liberar contornos
	
	private static ReaderForTilesFile readerForTilesFile;
	
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
	 * @param p_poda_color_explorado: poda donde solamente se permite explorar una sola vez el color right de la pieza en borde left.
	 * @param p_pos_fork_join: posición en tablero donde inicia exploración multi threading.
	 * @param reader: implementation of the tiles file reader.
	 * @param numProcesses: parallelism level for the fork-join pool.
	 */
	public static SolverFaster build(long m_ciclos, int lim_max_par, int lim_exploracion, int max_parciales, int destino_ret, 
			boolean usar_tableboard, boolean usar_multiples_boards, int cell_pixels_lado, int p_refresh_millis, 
			boolean p_fair_experiment_gif, boolean p_poda_color_explorado, int p_pos_fork_join,
			ReaderForTilesFile reader, int numProcesses) {
		
		readerForTilesFile = reader;
		
		MAX_CICLOS = m_ciclos;
		
		POSICION_START_FORK_JOIN = p_pos_fork_join;
		NUM_PROCESSES = numProcesses;
		// no tiene sentido usar varios threads si no se seteó correctamente la posición multi threading
		if (POSICION_START_FORK_JOIN < 0)
			NUM_PROCESSES = 1;

		// cycles counter per task
		count_cycles = new long[NUM_PROCESSES];
		
		// el limite para resultado parcial max no debe superar ciertos limites. Si sucede se usará el valor por defecto
		if ((lim_max_par > 0) && (lim_max_par < (MAX_PIEZAS-2)))
			LIMITE_RESULTADO_PARCIAL = lim_max_par;
		
		LIMITE_DE_EXPLORACION = lim_exploracion; //me dice hasta qué posicion debe explorar esta instancia 
		
		FairExperimentGif = p_fair_experiment_gif;

		if (destino_ret >= 0) {
			DESTINO_RET = destino_ret; //determina el valor hasta el cual debe retroceder cursor
			flag_retroceder_externo = true; //flag para saber si se debe retroceder al cursor antes de empezar a explorar
		}
		
		usar_poda_color_explorado = p_poda_color_explorado; //indica si se usará la poda de colores right explorados en borde left
		MAX_NUM_PARCIAL = max_parciales; //indica hasta cuantos archivos parcial.txt voy a tener
		ESQUINA_TOP_RIGHT = LADO - 1;
		ESQUINA_BOTTOM_RIGHT = MAX_PIEZAS - 1;
		ESQUINA_BOTTOM_LEFT = MAX_PIEZAS - LADO;
		
		if (usar_poda_color_explorado)
			arr_color_rigth_explorado = new AtomicIntegerArray(LADO);

		usarTableroGrafico = usar_tableboard;
		cellPixelsLado = cell_pixels_lado;
		tableboardRefreshMillis = p_refresh_millis;

		createDirs();
		
		return new SolverFaster();
	}
	
	
	//##########################################################################//
	//     INICIALIZACION DE CUALQUIER ESTRUCTURA QUE LO NECESITE
	//##########################################################################//

	private final static void createDirs() {
		new File("solution").mkdirs();
		new File("status").mkdirs();
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
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no llegue ni sobrepase el borde right
			if ((k + (Contorno.MAX_COLS-1)) < ((fila_actual*LADO) + (LADO-1)))
				zona_read_contorno[k] = true;
		}
	}
	
	/**
	 * El arreglo zonas_proc_contorno[] me dice en qué posiciones puedo procesar un contorno superior 
	 * e inferior para setearlo como usado o libre. Solamente sirve a dichos fines, y ningún otro.
	 * NOTA: para contorno inferior se debe chequear que cursor sea [33,238].
	 */
	private final static void inicializarZonaProcesoContornos()
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
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no est� cerca del borde left
			if (((k - Contorno.MAX_COLS) / LADO) != fila_actual)
				continue;
			
			zona_proc_contorno[k] = true;
		}
		
		System.out.println("Usando restriccion de contornos de " + Contorno.MAX_COLS + " columnas.");
	}
	
	/**
	 * Carga cada entrada de la matriz con los indices de las piezas que 
	 * tienen tales colores en ese orden.
	 */
	final static void cargarSuperEstructura(ExploracionAction action)
	{
		System.out.print(action.id + " >>> Cargando Estructura 4-Dimensional... ");
		long startingTime = System.nanoTime();
		
		/**
		 * Inicializo todo con una referencia válida.
		 * Ojo que hay celdas en la matriz que no se generan con las combinaciones de los colores de piezas, por ende pueden quedar celdas en null 
		 */
		for (byte i=0; i <= MAX_COLORES; ++i)
			for (byte j=0; j <= MAX_COLORES; ++j)
				for (byte k=0; k <= MAX_COLORES; ++k)
					for (byte l=0; l <= MAX_COLORES; ++l)
						action.super_matriz[MapaKeys.getKey(i,j,k,l)]= new NodoPosibles();
		
		/**
		 * Para cada posible combinacion entre los colores de las secciones top, 
		 * right, bottom y left creo un vector que contendrá las piezas que tengan
		 * esa combinacion de colores en dichas secciones y ademas guardo en qué
		 * estado de rotacion la cumplen.
		 */
		llenarSuperEstructura(action);
		
		/**
		 * Para cada entrada de la super estructura les acota el espacio de memoria empleado actualmente 
		 * por la carga de las posible Piezas y rotaciones. También setea como null aquellas entradas no útiles.
		 */
		for (int i=action.super_matriz.length-1; i >=0; --i) {
			if (action.super_matriz[i] == null)
				continue;
			else if (action.super_matriz[i].util == false) {
				NodoPosibles.disposeAll(action.super_matriz[i]);
				action.super_matriz[i] = null;
			}
			else
				NodoPosibles.finalizar(action.super_matriz[i]);
		}
		
		System.out.println("cargada (" + TimeUnit.MICROSECONDS.convert(System.nanoTime()-startingTime, TimeUnit.NANOSECONDS) + " microsecs)");
		System.gc();
	}
	
	private static final void llenarSuperEstructura (ExploracionAction action)
	{
		// itero sobre el arreglo de piezas
		for (int k = 0; k < MAX_PIEZAS; ++k) {
			
			if (k == INDICE_P_CENTRAL)
				continue;
			
			Pieza pz = action.piezas[k];
			
			//guardo la rotaciónn de la pieza
			byte temp_rot = pz.rotacion;
			//seteo su rotación en 0. Esto es para generar la matriz siempre en el mismo orden
			Pieza.llevarARotacion(pz, (byte)0);
			
			for (byte rot=0; rot < MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (FairExperimentGif && (pz.top == pz.bottom))
					continue;
				
				//este caso es cuando tengo los 4 colores
				int key1 = MapaKeys.getKey(pz.top, pz.right, pz.bottom, pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key1], pz, rot);
				
				//tengo tres colores y uno faltante
				int key2 = MapaKeys.getKey(MAX_COLORES,pz.right,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key2], pz, rot);
				int key3 = MapaKeys.getKey(pz.top,MAX_COLORES,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key3], pz, rot);
				int key4 = MapaKeys.getKey(pz.top,pz.right,MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key4], pz, rot);
				int key5 = MapaKeys.getKey(pz.top,pz.right,pz.bottom,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key5], pz, rot);
				
				//tengo dos colores y dos faltantes
				int key6 = MapaKeys.getKey(MAX_COLORES,MAX_COLORES,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key6], pz, rot);
				int key7 = MapaKeys.getKey(MAX_COLORES,pz.right,MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key7], pz, rot);
				int key8 = MapaKeys.getKey(MAX_COLORES,pz.right,pz.bottom,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key8], pz, rot);
				int key9 = MapaKeys.getKey(pz.top,MAX_COLORES,MAX_COLORES,pz.left);	
				NodoPosibles.addReferencia(action.super_matriz[key9], pz, rot);
				int key10 = MapaKeys.getKey(pz.top,MAX_COLORES,pz.bottom,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key10], pz, rot);
				int key11 = MapaKeys.getKey(pz.top,pz.right,MAX_COLORES,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key11], pz, rot);

				//tengo un color y tres faltantes
				int key12 = MapaKeys.getKey(pz.top,MAX_COLORES,MAX_COLORES,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key12], pz, rot);
				int key13 = MapaKeys.getKey(MAX_COLORES,pz.right,MAX_COLORES,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key13], pz, rot);
				int key14 = MapaKeys.getKey(MAX_COLORES,MAX_COLORES,pz.bottom,MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key14], pz, rot);
				int key15 = MapaKeys.getKey(MAX_COLORES,MAX_COLORES,MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key15], pz, rot);
			}
			
			//restauro la rotación
			Pieza.llevarARotacion(pz, temp_rot);
		}
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
	final static void cargarPiezas(ExploracionAction action) {
		
		BufferedReader reader = null;
		
		try{
			//verifico si no se han cargado ya las piezas en cargarEstado()
			boolean cargadas = true;
			for (int i=0; (i < MAX_PIEZAS) && cargadas; ++i){
				if (action.piezas[i] == null)
					cargadas = false;
			}
			
			if (cargadas)
				return;
			
			reader = readerForTilesFile.getReader(NAME_FILE_PIEZAS);
			String linea= reader.readLine();
			int num=0;
			while (linea != null){
				if (num >= MAX_PIEZAS)
					throw new Exception(action.id + " >>> ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
				action.piezas[num]= new Pieza(linea,num+1); 
				linea= reader.readLine();
				++num;
			}

			if (num != MAX_PIEZAS)
				throw new Exception(action.id + " >>> ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
		}
		catch (Exception exc){
			System.out.println(exc.getMessage());
		}
		finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {}
		}
	}
	
	/**
	 * En este metodo se setean las piezas que son fijas al juego. Por ahora solo existe
	 * una sola pieza fija y es la pieza numero 139 en las posicion 136 real (135 para el
	 * algoritmo porque es 0-based)
	 */
	final static void cargarPiezasFijas(ExploracionAction action) {
		
		Pieza piezaCentral = action.piezas[INDICE_P_CENTRAL];
		piezaCentral.usada= true;
		//piezaCentral.pos= POSICION_CENTRAL;
		action.tablero[POSICION_CENTRAL]= piezaCentral;
		
		System.out.println(action.id + " >>> Pieza Fija en posicion " + (POSICION_CENTRAL + 1) + " cargada!");
	}	

	/**
	 * Carga el ultimo estado de exploración guardado para la action pasada como parámetro. 
	 * Si no existe tal estado* inicializa estructuras y variables para que la exploracion comienze desde cero.
	 */
	final static boolean cargarEstado(String n_file, ExploracionAction action)
	{
		System.out.print(action.id + " >>> Cargando Estado de exploracion (" + n_file + ")... ");
		BufferedReader reader = null;
		boolean status_cargado = false;
		
		try{
			File f = new File(n_file);
			if (!f.isFile()) {
				System.out.println(" No existe.");
				return status_cargado;
			}
			
			reader = new BufferedReader(new FileReader(f));
			String linea = reader.readLine();
			int tablero_aux[] = new int[MAX_PIEZAS];
			
			if (linea==null) {
				throw new Exception(action.id + " >>> First line is null.");
			}
			else{
				int sep,sep_ant;
				byte valor;
				
				// contiene el valor de cursor mas bajo alcanzado en una vuelta de ciclo
				action.mas_bajo= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas alto alcanzado en una vuelta de ciclo
				linea= reader.readLine();
				action.mas_alto= Integer.parseInt(linea);
				
				// contiene el valor de cursor mas lejano parcial alcanzado (aquel que graba parcial max)
				linea= reader.readLine();
				action.mas_lejano_parcial_max= Integer.parseInt(linea);
				
				// contiene la posici�n del cursor en el momento de guardar estado
				linea= reader.readLine();
				action.cursor= Integer.parseInt(linea);
				
				// recorro los indices de las piezas que estaban en tablero
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < MAX_PIEZAS; ++k){
					if (k==(MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					valor= Byte.parseByte(linea.substring(sep_ant,sep));
					sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
					tablero_aux[k]=valor;
				}
				
				// recorro los valores de desde_saved[]
				linea= reader.readLine();
				sep=0; sep_ant=0;
				for (int k=0; k < MAX_PIEZAS; ++k){
					if (k==(MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					valor= Byte.parseByte(linea.substring(sep_ant,sep));
					sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
					action.desde_saved[k] = valor;
				}
				
				// la siguiente línea indica si se estaba usando poda de color explorado
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
							valor= Byte.parseByte(linea.substring(sep_ant,sep));
							sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
							arr_color_rigth_explorado.set(k, valor);
						}
					}
				}
				
				//las restantes MAX_PIEZAS lineas contienen el valor de rotación y usada de cada pieza
				int pos=0; //cuento cuantas lineas voy procesando
				String splitted[];
				linea= reader.readLine(); //info de la primer pieza
				while ((linea != null) && (pos < MAX_PIEZAS)){
					splitted = linea.split(SECCIONES_SEPARATOR_EN_FILE);
					Pieza.llevarARotacion(action.piezas[pos],Byte.parseByte(splitted[0]));
					action.piezas[pos].usada = Boolean.parseBoolean(splitted[1]);
					linea= reader.readLine();
					++pos;
				}
				if (pos != MAX_PIEZAS){
					System.out.println(action.id + " >>> ERROR. La cantidad de piezas en el archivo " + n_file + " no coincide con el numero de piezas que el juego espera.");
					throw new Exception("Inconsistent number of pieces.");
				}
				
				//obtengo las referencias de piezas en tablero[]
				for (int i=0; i < MAX_PIEZAS; ++i){
					if (tablero_aux[i] < 0)
						continue;
					action.tablero[i] = action.piezas[tablero_aux[i]];
				}
					
				status_cargado=true;
				System.out.println("cargado!");
			}
		}
		catch(Exception e){
			System.out.println(action.id + " >>> No existe o esta corrupto: " + e.getMessage());
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
	 * Verifica que no exista pieza extraña o que falte alguna pieza. 
	 * Solo se usa al cargar las piezas desde archivo o al cargar estado.
	 */
	final static void verificarTiposDePieza(ExploracionAction action) {
		
		int n_esq= 0;
		int n_bordes= 0;
		int n_centrales= 0;
	
		for (int g=0; g < MAX_PIEZAS; ++g)
		{
			if (action.piezas[g].es_esquina)
				++n_esq;
			if (action.piezas[g].es_borde)
				++n_bordes;
			if (action.piezas[g].es_interior)
				++n_centrales;
		}
		
		if ((n_esq != 4) || (n_bordes != (4*(LADO-2))) || (n_centrales != (MAX_PIEZAS - (n_esq + n_bordes))))
			System.out.println(action.id + " >>> ERROR. Existe una o varias piezas incorrectas.");
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
				guardarEstado(action.statusFileName, action);
				guardarResultadoParcial(false, action);
				System.out.println("Exploracion retrocedio a la posicion " + action.cursor + ". Estado salvado.");
				return; //alcanzada la posición destino y luego de salvar estado, salgo del programa
			}
			--action.cursor;
			//si me paso de la posición inicial significa que no puedo volver mas estados de exploración
			if (action.cursor < 0)
				break; //obliga a salir del while
			if (action.cursor != POSICION_CENTRAL){
				Pieza pzz = action.tablero[action.cursor];
				pzz.usada= false; //la seteo como no usada xq sino la exploración pensará que está usada (porque asi es como se guardó)
				//pzz.pos= -1;
				action.tablero[action.cursor]= null;
			}
			//si retrocedá hasta el cursor destino, entonces no retrocedo mas
			if ((action.cursor+1) <= cursor_destino){
				action.retroceder= false;
				cursor_destino= CURSOR_INVALIDO;
			}
			//si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
			if (action.retroceder)
				action.desde_saved[action.cursor] = 0; //la exploracion de posibles piezas para la posición cursor debe empezar desde la primer pieza
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
	final static void guardarResultadoParcial (final boolean max, ExploracionAction action)
	{
		if (MAX_NUM_PARCIAL == 0 && !max)
			return;
		
		try{
			PrintWriter wParcial= null;
			// si estamos en max instance tenemos q guardar las disposiciones de las piezas
			PrintWriter wDispMax = null;
			StringBuffer parcialBuffer= new StringBuffer(256 * 10);
			StringBuffer dispMaxBuff= new StringBuffer(256 * 10);
			
			if (max){
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(action.parcialMaxFileName)));
				wDispMax= new PrintWriter(new BufferedWriter(new FileWriter(action.disposicionMaxFileName)));
				dispMaxBuff.append("(num pieza) (estado rotacion) (posicion en tablero real)").append("\n");
			}
			else{
				String parcialFName = action.parcialFileName.substring(0, action.parcialFileName.indexOf(FILE_EXT)) + "_" + action.sig_parcial + FILE_EXT;
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialFName)));
				++action.sig_parcial;
				if (action.sig_parcial > MAX_NUM_PARCIAL)
					action.sig_parcial= 1;
			}
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				int pos= b+1;
				Pieza piezax = action.tablero[b];
				if (action.tablero[b] == null){
					parcialBuffer.append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(GRIS).append("\n");
					if (max)
						dispMaxBuff.append("-").append(SECCIONES_SEPARATOR_EN_FILE).append("-").append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
				}
				else{
					parcialBuffer.append(piezax.top).append(SECCIONES_SEPARATOR_EN_FILE).append(piezax.right).append(SECCIONES_SEPARATOR_EN_FILE).append(piezax.bottom).append(SECCIONES_SEPARATOR_EN_FILE).append(piezax.left).append("\n");
					if (max)
						dispMaxBuff.append(piezax.numero).append(SECCIONES_SEPARATOR_EN_FILE).append(piezax.rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
				guardarLibres(action);
			
			//solo para instancia max: enviar email
			/*if (send_mail && max){
				SendMail em1= new SendMail();
				SendMail em2= new SendMail();
				em1.setDatos(contenidoParcial.toString(),"ParcialMAX " + CASO);
				em2.setDatos(contenidoDisp.toString(),"ParcialMAX Disp " + CASO);
				Thread t1= new Thread(em1);
				Thread t2= new Thread(em2);
				t1.start();
				t2.start();
			}*/
		}
		catch(Exception ex) {
			System.out.println("ERROR: No se pudieron generar los archivos de resultado parcial.");
			System.out.println(ex);
		}
	}

	/**
	 * Me guarda en el archivo NAME_FILE_LIBRES_MAX las numeros de las piezas que quedaron libres.
	 */
	final static void guardarLibres(ExploracionAction action)
	{
		try{
			PrintWriter wLibres= new PrintWriter(new BufferedWriter(new FileWriter(action.libresMaxFileName)));
			StringBuffer wLibresBuffer= new StringBuffer(256 * 10);
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= action.piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(pzx.numero).append("\n");
			}
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= action.piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(pzx.toStringColores()).append("\n");
			}
			
			String sContent = wLibresBuffer.toString();
			wLibres.append(sContent);
			wLibres.flush();
			wLibres.close();
			
			/*if (send_mail){
				SendMail em= new SendMail();
				em.setDatos(sContent,"LibresMax" + CASO);
				Thread t= new Thread(em);
				t.start();
			}*/
		}
		catch (Exception escp) {
			System.out.println("ERROR: No se pudo generar el archivo " + action.libresMaxFileName);
			System.out.println(escp);
		}
	}

	/**
	 * En el archivo NAME_FILE_SOLUCION se guardan los colores de cada pieza.
	 * En el archivo NAME_FILE_DISPOSICION se guarda el numero y rotacion de cada pieza.
	 */
	final static void guardarSolucion (ExploracionAction action)
	{
		try{
			PrintWriter wSol= new PrintWriter(new BufferedWriter(new FileWriter(action.solucFileName,true)));
			PrintWriter wDisp= new PrintWriter(new BufferedWriter(new FileWriter(action.dispFileName,true)));
			StringBuffer contenidoDisp= new StringBuffer(256 * 10);
			
			wSol.println("Solucion para " + MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + MAX_PIEZAS + " piezas.");
			contenidoDisp.append("Disposicion para " + MAX_PIEZAS + " piezas.\n");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			contenidoDisp.append("(num pieza) (estado rotacion) (posicion en tablero real)\n");
			
			for (int b=0; b < MAX_PIEZAS; ++b)
			{
				Pieza piezax= action.tablero[b];
				int pos= b+1;
				wSol.println(piezax.top + SECCIONES_SEPARATOR_EN_FILE + piezax.right + SECCIONES_SEPARATOR_EN_FILE + piezax.bottom + SECCIONES_SEPARATOR_EN_FILE + piezax.left);
				wDisp.println(piezax.numero + SECCIONES_SEPARATOR_EN_FILE + piezax.rotacion + SECCIONES_SEPARATOR_EN_FILE + pos);
				contenidoDisp.append(piezax.numero + SECCIONES_SEPARATOR_EN_FILE + piezax.rotacion + SECCIONES_SEPARATOR_EN_FILE + pos + "\n");
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
			/*if (send_mail){
				SendMail em= new SendMail();
				em.setDatos(contenidoDisp.toString(),"Solucion caso" + CASO);
				Thread t= new Thread(em);
				t.start();
			}*/
		}catch(Exception ex)
		{
			System.out.println("ERROR: No se pudo guardar la solucion!! QUE MACANA!!! (guardarSolucion())");
			System.out.println(ex);
		}
	}

	/**
	 * Guarda las estructuras necesaria del algoritmo para poder continuar desde el actual estado de exploración.
	 */
	final static void guardarEstado (final String f_name, ExploracionAction action) {
		
		try{
			PrintWriter writer= new PrintWriter(new BufferedWriter(new FileWriter(f_name)));
			StringBuffer writerBuffer= new StringBuffer(256 * 10);
	
			//guardo el valor de mas_bajo
			writerBuffer.append(action.mas_bajo).append("\n");
			
			//guardo el valor de mas_alto
			writerBuffer.append(action.mas_alto).append("\n");
			
			//guardo el valor de mas_lejano 
			writerBuffer.append(action.mas_lejano_parcial_max).append("\n");
			
			//guardo el valor del cursor
			writerBuffer.append(action.cursor).append("\n");
			
			//guardo los indices de tablero[]
			for (int n=0; n < MAX_PIEZAS; ++n) {
				if (n==(MAX_PIEZAS - 1)){
					if (action.tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append((action.tablero[n].numero-1)).append("\n");
				}
				else{
					if (action.tablero[n] == null)
						writerBuffer.append("-1").append(SECCIONES_SEPARATOR_EN_FILE);
					else
						writerBuffer.append((action.tablero[n].numero-1)).append(SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//########################################################################
			/**
			 * Calculo los valores para desde_saved[]
			*/
			//########################################################################
			int cur_copy= action.cursor; //guardo una copia de cursor xq voy a usarlo
			for (action.cursor=0; action.cursor < cur_copy; ++action.cursor) {
				
				if (action.cursor == POSICION_CENTRAL) //para la pieza central no se tiene en cuenta su valor desde_saved[] 
					continue;
				//tengo el valor para desde_saved[]
				action.desde_saved[action.cursor] = (byte) (NodoPosibles.getUbicPieza(action.obtenerPosiblesPiezas(),
						action.tablero[action.cursor].numero) + 1);
			}
			//ahora todo lo que está despues de cursor tiene que valer cero
			for (;action.cursor < MAX_PIEZAS; ++action.cursor)
				action.desde_saved[action.cursor] = 0;
			action.cursor= cur_copy; //restauro el valor de cursor
			//########################################################################
			
			//guardo las posiciones de posibles piezas (desde_saved[]) de cada nivel del backtracking
			for (int n=0; n < MAX_PIEZAS; ++n) {
				if (n==(MAX_PIEZAS-1))
					writerBuffer.append(action.desde_saved[n]).append("\n");
				else
					writerBuffer.append(action.desde_saved[n]).append(SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//indico si se utiliza poda de color explorado o no
			writerBuffer.append(usar_poda_color_explorado).append("\n");
			
			//guardo el contenido de arr_color_rigth_explorado
			if (usar_poda_color_explorado)
			{
				for (int n=0; n < LADO; ++n) {
					if (n==(LADO-1))
						writerBuffer.append(arr_color_rigth_explorado.get(n)).append("\n");
					else
						writerBuffer.append(arr_color_rigth_explorado.get(n)).append(SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//guardo el estado de rotación y el valor de usada de cada pieza
			for (int n=0; n < MAX_PIEZAS; ++n)
			{
				writerBuffer.append(action.piezas[n].rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(String.valueOf(action.piezas[n].usada)).append("\n");
			}
			
			String sContent = writerBuffer.toString();
			writer.append(sContent);
			writer.flush();
			writer.close();
	
			//Sentencias para enviar email status_saved
			/*if (send_mail){
				SendMail em= new SendMail();
				em.setDatos(sContent, "status caso " + CASO);
				Thread t= new Thread(em);
				t.start();
			}*/
		}
		catch (Exception e) {
			System.out.println("ERROR: No se pudo guardar el estado de la exploración.");
			System.out.println(e);
		}
	}
	
	/**
	 * Inicializa varias estructuras y flags
	 */
	public final void setupInicial() {
		
		// cargo en el arreglo matrix_zonas valores que me indiquen en que posición estoy (borde, esquina o interior) 
		inicializarMatrixZonas();
		
		// seteo las posiciones donde se puede preguntar por contorno superior usado
		inicializarZonaReadContornos();
		
		// seteo las posiciones donde puedo setear un contorno como usado o libre
		inicializarZonaProcesoContornos();
		
		// creates the array of actions
		actions = new ExploracionAction[NUM_PROCESSES];
		// a start signal that prevents any ExplorationAction from proceeding until the orchestrator (this thread) is ready for them to proceed
		startSignal = new CountDownLatch(1);
		// a completion signal that allows the driver orchestrator (this thread) to wait until all ExplorationAction have completed
		doneSignal = new CountDownLatch(NUM_PROCESSES);
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {

			ExploracionAction exploracionAction = new ExploracionAction(proc, NUM_PROCESSES, MAX_CICLOS, POSICION_START_FORK_JOIN, 
					LIMITE_RESULTADO_PARCIAL, usar_poda_color_explorado, FairExperimentGif, usarTableroGrafico, 
					startSignal, doneSignal);
			
			exploracionAction.setupInicial();
			
			actions[proc] = exploracionAction;
		}
	}
	
	/**
	 * This method has sun.misc.Signal* api usage, which are not present in java.lib (at least on Windows) 
	 * so can not be used to native compilation.
	 */
//	public final void atacarWithSunMiscSignal() {
//		// Register a signal handler for Ctrl-C that runs the shutdown hooks
//		sun.misc.Signal.handle(new sun.misc.Signal("INT"), new sun.misc.SignalHandler() {
//			@Override
//			public void handle(Signal sig) {
//				System.exit(0);
//			}
//		});
//        
//		// Add a shutdown hook
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run() {
//				shutdown();
//			}
//        });
//        
//		atacar();
//	}
	
//	private synchronized void shutdown() {
//        for (int i = 0, c = NUM_PROCESSES; i < c; ++i) {
//        	doneSignal.countDown();
//			initialSetupSignal.countDown();
//        }
//        System.out.println("Shutdown called");
//    }
	
	/**
	 * Invoca al pool de fork-join con varias instancias de RecursiveAction: ExploracionAction.
	 * Cada action ejecuta una rama de la exploración asociada a su id. De esta manera se logra decidir 
	 * la rama a explorar y tmb qué siguiente rama explorar una vez finalizada la primer rama.
	 */
	public final void atacar(long timeoutTaskInSecs) {
		
		ForkJoinPool fjpool = new ForkJoinPool(NUM_PROCESSES, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, false);
		
		// submit all fork join tasks
		for (int i = 0, c = actions.length; i < c; ++i) {
			System.out.println("ExploracionAction " + i + " submitted");
			fjpool.submit(actions[i]);
		}
		
		// let all tasks proceed
		startSignal.countDown();
		
		// wait for all to finish
		try {
			if (timeoutTaskInSecs > 0) {
				doneSignal.await(timeoutTaskInSecs, TimeUnit.SECONDS);
			} else {
				doneSignal.await();
			}
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		} finally {
			System.out.println("Interrupting tasks...");
			fjpool.shutdownNow();
			System.out.println("Tasks interrupted.");
		}

		/*fjpool.invoke(new RecursiveAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void compute () {
				// Since there is no task division inside any ExploracionAction then we don't need to use join() to await for results.
				// So let's fork all actions so they are allocated inside the pool
				for (int i=0, c=actions.length; i < c; ++i) {
					if (i == (c-1))
						actions[i].compute(); // direct invoke so this thread is also used for computation
					else
						actions[i].fork();
				}
			}
		});*/
	}
	
	public void resetInternalStatus() {
		
		startSignal = new CountDownLatch(1);
		doneSignal = new CountDownLatch(NUM_PROCESSES);
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {
			ExploracionAction exploracionAction = actions[proc];
			exploracionAction.reinitialize();
			exploracionAction.resetForAtaque(NUM_PROCESSES, startSignal, doneSignal);
		}
	}
}