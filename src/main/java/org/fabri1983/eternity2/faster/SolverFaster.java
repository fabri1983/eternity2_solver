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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.NodoPosibles;
import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.PiezaFactory;
import org.fabri1983.eternity2.core.PiezaStringer;
import org.fabri1983.eternity2.core.bitset.QuickLongBitSet;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Border;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Corner;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Interior;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public final class SolverFaster {
	
	static int POSICION_START_FORK_JOIN = -1; //(99) posición del tablero en la que se aplica fork/join
	static int NUM_PROCESSES = 1;
	static ExploracionAction actions[];
	static CountDownLatch startSignal;
    
	static long MAX_CICLOS; // Número máximo de ciclos para guardar estado
	static int DESTINO_RET; // Posición de cursor hasta la cual debe retroceder cursor
	static int MAX_NUM_PARCIAL; // Número de archivos parciales que se generarón
	public static int LIMITE_DE_EXPLORACION; // me dice hasta qué posición debe explorar esta instancia
	public final static short LADO= 16;
	final static short LADO_SHIFT_AS_DIVISION = 4;
	public final static short MAX_PIEZAS= 256;
	public final static short POSICION_CENTRAL= 135; // es el indice en tablero[] donde se coloca la pieza central
	public final static short NUM_P_CENTRAL= 138; // es la ubicación de la pieza central en piezas[]
	final static short ANTE_POSICION_CENTRAL= 134; // la posición inmediatamente anterior a la posicion central
	final static short SOBRE_POSICION_CENTRAL= 119; // la posición arriba de la posicion central
	final static byte F_INTERIOR= 1;
	final static byte F_BORDE_RIGHT= 2;
	final static byte F_BORDE_LEFT= 3;
	final static byte F_BORDE_TOP= 4;
	final static byte F_BORDE_BOTTOM= 5;
	final static byte F_ESQ_TOP_LEFT= 6;
	final static byte F_ESQ_TOP_RIGHT= 7;
	final static byte F_ESQ_BOTTOM_LEFT= 8;
	final static byte F_ESQ_BOTTOM_RIGHT= 9;
	final static byte MAX_ESTADOS_ROTACION= 4;
	final static short CURSOR_INVALIDO= -5;
	final static byte MAX_COLORES= 23;
	final static String SECCIONES_SEPARATOR_EN_FILE= " ";
	final static String FILE_EXT = ".txt";
	final static String NAME_FILE_PIEZAS = "e2pieces" + FILE_EXT;
	final static String NAME_FILE_SOLUCION = "solution/soluciones";
	final static String NAME_FILE_DISPOSICION = "solution/disposiciones";
	final static String NAME_FILE_STATUS = "status/status_saved";
	final static String NAME_FILE_PARCIAL_MAX = "status/parcialMAX";
	final static String NAME_FILE_DISPOSICIONES_MAX = "status/disposicionMAX";
	final static String NAME_FILE_PARCIAL = "status/parcial";
	final static String NAME_FILE_LIBRES_MAX = "status/libresMAX";
	static int LIMITE_RESULTADO_PARCIAL = 211; // por defecto
	
	public static long count_cycles[]; // count cycles per task when usarTableroGrafico is true
	
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
	 *   - total used indexes    =   6862
	 *   - wasted indexes        = approx 99%  <= but using an array has faster reads than a map :(
	 * Ver archivo misc/super_matriz_sizes_by_index.txt
	 * 
	 * IMPROVEMENT (faster access but more memory consumption): 
	 * Then, I realize that just using a 4 dimensional array I end up with 331776‬ indexes which is the 43% of 777975.
	 * It uses less memory and the access time is the same than the previous big array.
	 * 
	 * IMPROVEMENT FINAL (much less memory but slower than an array access):
	 * Using a pre calculated Perfect Hash Function I ended up with an array size of PerfectHashFunction2.PHASHRANGE.
	 */
//    final static NodoPosibles[][][][] super_matriz = new NodoPosibles
//            [MAX_COLORES+1][MAX_COLORES+1][MAX_COLORES+1][MAX_COLORES+1];
	final static NodoPosibles[] super_matriz_interior = new NodoPosibles[PerfectHashFunction2Interior.PHASHRANGE];
	final static NodoPosibles[] super_matriz_border = new NodoPosibles[PerfectHashFunction2Border.PHASHRANGE];
	final static NodoPosibles[] super_matriz_corner = new NodoPosibles[PerfectHashFunction2Corner.PHASHRANGE];
	final static QuickLongBitSet bitset = new QuickLongBitSet(777942 + 1);
	
	final static byte matrix_zonas[] = new byte[MAX_PIEZAS];
	
	// cada posición es un entero donde se usan 23 bits para los colores donde un bit valdrá 0 si ese 
	// color (right en borde left) no ha sido exlorado para la fila actual, sino valdrá 1.
	final static AtomicIntegerArray arr_color_rigth_explorado = new AtomicIntegerArray(LADO);
	
	static boolean retroceder, FairExperimentGif, usarTableroGrafico;
	static int cellPixelsLado, tableboardRefreshMillis;
	static boolean flag_retroceder_externo, usar_poda_color_explorado;
	final static boolean zona_proc_contorno[] = new boolean[MAX_PIEZAS]; // arreglo de zonas permitidas para usar y liberar contornos
	final static boolean zona_read_contorno[] = new boolean[MAX_PIEZAS]; // arreglo de zonas permitidas para preguntar por contorno used
	
	private static ReaderForFile readerForTilesFile;
	
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
			ReaderForFile reader, int numProcesses) {
		
		readerForTilesFile = reader;
		
		MAX_CICLOS = m_ciclos;
		
		POSICION_START_FORK_JOIN = p_pos_fork_join;
		NUM_PROCESSES = Math.min(Runtime.getRuntime().availableProcessors(), numProcesses);
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
			else if (k == (LADO - 1))
				matrix_zonas[k]= F_ESQ_TOP_RIGHT;
			//esquina bottom-right
			else if (k == (MAX_PIEZAS - 1))
				matrix_zonas[k]= F_ESQ_BOTTOM_RIGHT;
			//esquina bottom-left
			else if (k == (MAX_PIEZAS - LADO))
				matrix_zonas[k]= F_ESQ_BOTTOM_LEFT;
			//borde top
			else if ((k > 0) && (k < (LADO - 1)))
				matrix_zonas[k]= F_BORDE_TOP;
			//borde right
			else if (((k+1) % LADO)==0){
				if ((k != (LADO - 1)) && (k != (MAX_PIEZAS - 1)))
					matrix_zonas[k]= F_BORDE_RIGHT;
			}
			//borde bottom
			else if ((k > (MAX_PIEZAS - LADO)) && (k < (MAX_PIEZAS - 1)))
				matrix_zonas[k]= F_BORDE_BOTTOM;
			//borde left
			else if ((k % LADO)==0){
				if ((k != 0) && (k != (MAX_PIEZAS - LADO)))
					matrix_zonas[k]= F_BORDE_LEFT;
			}
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
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < LADO || k > (MAX_PIEZAS-LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % LADO)==0) || ((k % LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté en borde left + (Contorno.MAX_COLS - 1)
			int fila_actual = k / LADO;
			if (((k - Contorno.MAX_COLS) / LADO) != fila_actual)
				continue;
			
			zona_proc_contorno[k] = true;
		}
		
		System.out.println("Usando restriccion de contornos de " + Contorno.MAX_COLS + " columnas.");
	}

	/**
	 * El arreglo zona_read_contorno[] me dice en qué posiciones puedo leer un contorno para chequear si es usado o no.
	 */
	private final static void inicializarZonaReadContornos()
	{	
		for (int k=0; k < MAX_PIEZAS; ++k)
		{
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < LADO || k > (MAX_PIEZAS-LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % LADO)==0) || ((k % LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté dentro de (Contorno.MAX_COLS - 1) posiciones antes de border right
			int fila_actual = k / LADO;
			if ((k + (Contorno.MAX_COLS-1)) < ((fila_actual*LADO) + (LADO-1)))
				zona_read_contorno[k] = true;
		}
	}
	
	/**
	 * Carga cada entrada de la matriz con los indices de las piezas que 
	 * tienen tales colores en ese orden.
	 */
	final static void cargarSuperEstructura(ExploracionAction action)
	{
		System.out.print(action.id + " >>> Cargando super matriz... ");
		long startingTime = System.nanoTime();
		
		llenarSuperEstructura(action);
		
		System.out.println("cargada (" + TimeUnit.MICROSECONDS.convert(System.nanoTime()-startingTime, TimeUnit.NANOSECONDS) + " micros)");
	}

	/**
	 * Para cada posible combinacion entre los colores de las secciones top, 
	 * right, bottom y left creo un vector que contendrá las piezas que tengan
	 * esa combinacion de colores en dichas secciones y ademas guardo en qué
	 * estado de rotacion la cumplen.
	 */
	private static final void llenarSuperEstructura (ExploracionAction action)
	{
		// itero sobre el arreglo de piezas
		for (short k = 0; k < MAX_PIEZAS; ++k) {
			
			if (k == NUM_P_CENTRAL)
				continue;
			
			Pieza pz = action.piezas[k];
			
			//guardo la rotación de la pieza
			byte temp_rot = pz.rotacion;
			//seteo su rotación en 0. Esto es para generar la matriz siempre en el mismo orden
			Pieza.llevarArotacion(pz, (byte)0);
			
			for (byte rot=0; rot < MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (FairExperimentGif && (pz.top == pz.bottom))
					continue;
				
				//este caso es cuando tengo los 4 colores
				if (getNodoFromOriginalKey(pz.top, pz.right, pz.bottom, pz.left, pz) == null)
					setNewNodoP(pz.top, pz.right, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, pz.right, pz.bottom, pz.left, pz), k, rot);
				
				//tengo tres colores y uno faltante
				if (getNodoFromOriginalKey(MAX_COLORES, pz.right, pz.bottom, pz.left, pz) == null)
					setNewNodoP(MAX_COLORES, pz.right, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(MAX_COLORES, pz.right, pz.bottom, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top, MAX_COLORES, pz.bottom, pz.left, pz) == null)
					setNewNodoP(pz.top, MAX_COLORES, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, MAX_COLORES, pz.bottom, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top, pz.right, MAX_COLORES, pz.left, pz) == null)
					setNewNodoP(pz.top, pz.right, MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, pz.right, MAX_COLORES, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top ,pz.right, pz.bottom, MAX_COLORES, pz) == null)
					setNewNodoP(pz.top ,pz.right, pz.bottom, MAX_COLORES, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top ,pz.right, pz.bottom, MAX_COLORES, pz), k, rot);
				
				//tengo dos colores y dos faltantes
				if (getNodoFromOriginalKey(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left, pz) == null)
					setNewNodoP(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(MAX_COLORES, MAX_COLORES, pz.bottom, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(MAX_COLORES, pz.right, MAX_COLORES, pz.left, pz) == null)
					setNewNodoP(MAX_COLORES, pz.right, MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(MAX_COLORES, pz.right, MAX_COLORES, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES, pz) == null)
					setNewNodoP(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(MAX_COLORES, pz.right, pz.bottom, MAX_COLORES, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top, MAX_COLORES, MAX_COLORES, pz.left, pz) == null)
					setNewNodoP(pz.top, MAX_COLORES, MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, MAX_COLORES, MAX_COLORES, pz.left, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES, pz) == null)
					setNewNodoP(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, MAX_COLORES, pz.bottom, MAX_COLORES, pz), k, rot);
				
				if (getNodoFromOriginalKey(pz.top, pz.right, MAX_COLORES, MAX_COLORES, pz) == null)
					setNewNodoP(pz.top, pz.right, MAX_COLORES, MAX_COLORES, pz);
				NodoPosibles.addReferencia(getNodoFromOriginalKey(pz.top, pz.right, MAX_COLORES, MAX_COLORES, pz), k, rot);

				//tengo un color y tres faltantes
				//(esta combinación no se usa)
			}
			
			//restauro la rotación
			Pieza.llevarArotacion(pz, temp_rot);
		}
	}

	private final static NodoPosibles getNodoFromOriginalKey(final byte top, final byte right, final byte bottom, final byte left, Pieza p)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		// get NodoPosibles according type of pieza
		if (Pieza.isInterior(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_INTERIOR;
			return super_matriz_interior[PerfectHashFunction2Interior.phash(keyDiff)];
		} else if (Pieza.isBorder(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_BORDER;
			return super_matriz_border[PerfectHashFunction2Border.phash(keyDiff)];
		} else if (Pieza.isCorner(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_CORNER;
			return super_matriz_corner[PerfectHashFunction2Corner.phash(keyDiff)];
		}
		return null;
	}
	
	final static NodoPosibles getNodoIfKeyIsOriginal_interior(final byte top, final byte right, final byte bottom, final byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;
		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_INTERIOR;
		return super_matriz_interior[PerfectHashFunction2Interior.phash(keyDiff)];
	}
	
	final static NodoPosibles getNodoIfKeyIsOriginal_border(final byte top, final byte right, final byte bottom, final byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;
		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_BORDER;
		return super_matriz_border[PerfectHashFunction2Border.phash(keyDiff)];
	}
	
	final static NodoPosibles getNodoIfKeyIsOriginal_corner(final byte top, final byte right, final byte bottom, final byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;
		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_CORNER;
		return super_matriz_corner[PerfectHashFunction2Corner.phash(keyDiff)];
	}
	
	private final static void setNewNodoP(final byte top, final byte right, final byte bottom, final byte left, Pieza p)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);
		// set key as a valid one
		bitset.set(key);
		// create a new NodoPosibles according the type of pieza
		if (Pieza.isInterior(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_INTERIOR;
			NodoPosibles nodoPosibles = NodoPosibles.newForKey_interior(keyDiff);
			super_matriz_interior[PerfectHashFunction2Interior.phash(keyDiff)] = nodoPosibles;
		} else if (Pieza.isBorder(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_BORDER;
			NodoPosibles nodoPosibles = NodoPosibles.newForKey_border(keyDiff);
			super_matriz_border[PerfectHashFunction2Border.phash(keyDiff)] = nodoPosibles;
		} else if (Pieza.isCorner(p)) {
			int keyDiff = key - NodoPosibles.KEY_SUBTRACT_CORNER;
			NodoPosibles nodoPosibles = NodoPosibles.newForKey_corner(keyDiff);
			super_matriz_corner[PerfectHashFunction2Corner.phash(keyDiff)] = nodoPosibles;
		}
	}
	
	/**
	 * La exploracion ha alcanzado su punto limite, ahora es necesario guardar estado
	 */
//	final static void operarSituacionLimiteAlcanzado(ExploracionAction action) {
//		guardarEstado(action.statusFileName, action);
//		
//		System.out.println("El caso " + action.id + " ha llegado a su limite de exploracion. Exploracion finalizada forzosamente.");
//	}
	
	/**
	 * Carga las piezas desde el archivo NAME_FILE_PIEZAS
	 */
	final static void cargarPiezas(ExploracionAction action) {
		
		BufferedReader reader = null;
		
		try{
			reader = readerForTilesFile.getReader(NAME_FILE_PIEZAS);
			String linea= reader.readLine();
			short num=0;
			while (linea != null){
				if (num >= MAX_PIEZAS)
					throw new Exception(action.id + " >>> ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
				action.piezas[num]= PiezaFactory.from(linea, num);
                //PiezaFactory.setFromStringWithNum(linea, num, action.piezas[num]);
				linea= reader.readLine();
				++num;
			}

			if (num != MAX_PIEZAS)
				throw new Exception(action.id + " >>> ERROR. El numero que ingresaste como num de piezas por lado (" + LADO + ") es distinto del que contiene el archivo");
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
	 * algoritmo porque es 0-based)
	 */
	final static void cargarPiezasFijas(ExploracionAction action) {
		
		Pieza piezaCentral = action.piezas[NUM_P_CENTRAL];
		piezaCentral.usada= true;
		//piezaCentral.pos= POSICION_CENTRAL;
		action.tablero[POSICION_CENTRAL]= piezaCentral; // same value than INDICE_P_CENTRAL
		
		System.out.println(action.id + " >>> Pieza Fija en posicion " + (POSICION_CENTRAL + 1) + " cargada en tablero");
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
				for (int k=0; k < MAX_PIEZAS; ++k){
					if (k==(MAX_PIEZAS-1))
						sep= linea.length();
					else sep= linea.indexOf(SECCIONES_SEPARATOR_EN_FILE,sep_ant);
					short numPieza= Short.parseShort(linea.substring(sep_ant,sep));
					sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
					action.tablero[k]= numPieza == -1 ? null : action.piezas[numPieza];
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
					action.desde_saved[k] = numPieza;
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
							int val= Integer.parseInt(linea.substring(sep_ant,sep));
							sep_ant= sep+SECCIONES_SEPARATOR_EN_FILE.length();
							arr_color_rigth_explorado.set(k, val);
						}
					}
				}
				
				//las restantes MAX_PIEZAS lineas contienen el valor de rotación y usada de cada pieza
				int pos=0; //cuento cuantas lineas voy procesando
				String splitted[];
				linea= reader.readLine(); //info de la primer pieza
				while ((linea != null) && (pos < MAX_PIEZAS)){
					splitted = linea.split(SECCIONES_SEPARATOR_EN_FILE);
					Pieza.llevarArotacion(action.piezas[pos], Byte.parseByte(splitted[0]));
					action.piezas[pos].usada = Boolean.parseBoolean(splitted[1]);
					linea= reader.readLine();
					++pos;
				}
				if (pos != MAX_PIEZAS){
					System.out.println(action.id + " >>> ERROR. La cantidad de piezas en el archivo " + n_file + " no coincide con el numero de piezas que el juego espera.");
					throw new Exception("Inconsistent number of pieces.");
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
		int n_interiores= 0;
	
		for (int g=0; g < MAX_PIEZAS; ++g)
		{
			Pieza pzx = action.piezas[g];
			if (Pieza.isInterior(pzx))
				++n_interiores;
			else if (Pieza.isBorder(pzx))
				++n_bordes;
			else if (Pieza.isCorner(pzx))
				++n_esq;
		}
		
		if ((n_esq != 4) || (n_bordes != (4*(LADO-2))) || (n_interiores != (MAX_PIEZAS - (n_esq + n_bordes)))) {
			System.out.println(action.id + " >>> ERROR. Existe una o varias piezas incorrectas.");
			throw new RuntimeException("ERROR. Existe una o varias piezas incorrectas.");
		}
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
				System.out.println(action.id + ": Exploracion retrocedio a la posicion " + action.cursor + ". Estado salvado.");
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
			StringBuilder parcialBuffer= new StringBuilder(256 * 13);
			StringBuilder dispMaxBuff= new StringBuilder(256 * 13);
			
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
				Pieza p = action.tablero[b];
				if (p == null){
					parcialBuffer.append(PiezaFactory.GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append(SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append("\n");
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
				guardarLibres(action);
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
			StringBuilder wLibresBuffer= new StringBuilder(256 * 13);
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= action.piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(pzx.numero + 1).append("\n");
			}
			
			for (int b=0; b < MAX_PIEZAS; ++b) {
				Pieza pzx= action.piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(PiezaStringer.toStringColores(pzx)).append("\n");
			}
			
			String sContent = wLibresBuffer.toString();
			wLibres.append(sContent);
			wLibres.flush();
			wLibres.close();
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
			StringBuilder contenidoDisp= new StringBuilder(256 * 13);
			
			wSol.println("Solucion para " + MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + MAX_PIEZAS + " piezas.");
			contenidoDisp.append("Disposicion para " + MAX_PIEZAS + " piezas.\n");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			contenidoDisp.append("(num pieza) (estado rotacion) (posicion en tablero real)\n");
			
			for (int b=0; b < MAX_PIEZAS; ++b)
			{
				Pieza p= action.tablero[b];
				int pos= b+1;
				wSol.println(p.top + SECCIONES_SEPARATOR_EN_FILE + p.right + SECCIONES_SEPARATOR_EN_FILE + p.bottom + SECCIONES_SEPARATOR_EN_FILE + p.left);
				wDisp.println((p.numero + 1) + SECCIONES_SEPARATOR_EN_FILE + p.rotacion + SECCIONES_SEPARATOR_EN_FILE + pos);
				contenidoDisp.append(p.numero + 1).append( SECCIONES_SEPARATOR_EN_FILE).append(p.rotacion).append(SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
			StringBuilder writerBuffer= new StringBuilder(256 * 13);
	
			//guardo el valor de mas_bajo
			writerBuffer.append(action.mas_bajo).append("\n");
			
			//guardo el valor de mas_alto
			writerBuffer.append(action.mas_alto).append("\n");
			
			//guardo el valor de mas_lejano 
			writerBuffer.append(action.mas_lejano_parcial_max).append("\n");
			
			//guardo el valor del cursor
			writerBuffer.append(action.cursor).append("\n");
			
			//guardo los indices de piezas de tablero[]
			for (int n=0; n < MAX_PIEZAS; ++n) {
				if (n==(MAX_PIEZAS - 1)) {
					if (action.tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append(action.tablero[n].numero).append("\n");
				}
				else {
					if (action.tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append(action.tablero[n].numero).append(SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//########################################################################
			/**
			 * Calculo los valores para desde_saved[]
			 */
			//########################################################################
			int _cursor = 0;
			for (; _cursor < action.cursor; ++_cursor) {
				
				if (_cursor == POSICION_CENTRAL) //para la pieza central no se tiene en cuenta su valor desde_saved[] 
					continue;
				//tengo el valor para desde_saved[]
				action.desde_saved[_cursor] = NodoPosibles.getUbicPieza(action.obtenerPosiblesPiezas(_cursor), action.tablero[_cursor].numero);
			}
			//ahora todo lo que está despues de cursor tiene que valer cero
			for (;_cursor < MAX_PIEZAS; ++_cursor)
				action.desde_saved[_cursor] = 0;
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
		
		// seteo las posiciones donde puedo setear un contorno como usado o libre
		inicializarZonaProcesoContornos();
		
		// seteo las posiciones donde se puede preguntar por contorno superior usado
		inicializarZonaReadContornos();
		
		// creates the array of actions
		actions = new ExploracionAction[NUM_PROCESSES];
		// a start signal that prevents any ExplorationAction from proceeding until the orchestrator (this thread) is ready for them to proceed
		startSignal = new CountDownLatch(1);
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {

			ExploracionAction exploracionAction = new ExploracionAction(proc, NUM_PROCESSES, MAX_CICLOS, POSICION_START_FORK_JOIN, 
					LIMITE_RESULTADO_PARCIAL, usar_poda_color_explorado, FairExperimentGif, usarTableroGrafico, 
					startSignal);
			
			exploracionAction.setupInicial();
			
			actions[proc] = exploracionAction;
		}
		
		// cargar la super matriz (solo basta obtener las piezas de un exploracionAction)
		cargarSuperEstructura(actions[0]);
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
		
		for (int i=0; i < super_matriz_interior.length; ++i) {
			if (super_matriz_interior[i] != null)
				NodoPosibles.resetReferencias(super_matriz_interior[i]);
		}
		
		for (int i=0; i < super_matriz_border.length; ++i) {
			if (super_matriz_border[i] != null)
				NodoPosibles.resetReferencias(super_matriz_border[i]);
		}
		
		for (int i=0; i < super_matriz_corner.length; ++i) {
			if (super_matriz_corner[i] != null)
				NodoPosibles.resetReferencias(super_matriz_corner[i]);
		}
		
		for (int proc=0; proc < NUM_PROCESSES; ++proc) {
			ExploracionAction exploracionAction = actions[proc];
			exploracionAction.resetForBenchmark(NUM_PROCESSES, startSignal);
		}
	}
}