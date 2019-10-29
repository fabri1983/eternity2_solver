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

package org.fabri1983.eternity2.forkjoin_solver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.Contorno;
import org.fabri1983.eternity2.core.MapaKeys;
import org.fabri1983.eternity2.core.NodoPosibles;
import org.fabri1983.eternity2.core.Pieza;

/**
 * This action doesn't create sub actions (it doesn't fork/join)
 * Extends from RecursiveAction (do not yield a return value).
 */
public class ExploracionAction extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	
	protected String statusFileName, parcialFileName, parcialMaxFileName, 
			disposicionMaxFileName, libresMaxFileName, solucFileName, dispFileName;
	
	/**
	 * Cada indice del arreglo definido en el orden (top,right,bottom,left) contiene una lista de {@link NodoPosibles} 
	 * con las piezas que cumplen con esos colores.
	 * Para el cálculo de la capacidad de la matriz de combinaciones de colores se tienen en cuenta solo 
	 * combinaciones top,right,bottom,left de colores. 
	 */
	protected final NodoPosibles[] super_matriz = new NodoPosibles[
	    (SolverFaster.MAX_COLORES << 20) | 
	    (SolverFaster.MAX_COLORES << 15) | 
	    (SolverFaster.MAX_COLORES << 10) | 
	    (SolverFaster.MAX_COLORES << 5) | 
	    SolverFaster.MAX_COLORES
	];
	
	public final Pieza[] piezas = new Pieza[SolverFaster.MAX_PIEZAS];
	public final Pieza[] tablero = new Pieza[SolverFaster.MAX_PIEZAS];
	
	public int cursor, mas_bajo, mas_alto, mas_lejano_parcial_max;
	protected final byte[] desde_saved = new byte[SolverFaster.MAX_PIEZAS];
	private final Contorno contorno = new Contorno();
	protected boolean retroceder; // indica si debo volver estados de backtracking
	private boolean status_cargado; // inidica si se ha cargado estado inicial
	protected boolean mas_bajo_activo; // permite o no modificar el cursor mas_bajo
	protected int sig_parcial = 1; // esta variable indica el numero de archivo parcial siguiente a guardar
	
	private int index_sup; // empleados en varios métodos para pasar info

	private long time_inicial; // sirve para calcular el tiempo al hito de posición lejana
	private long time_status_saved; //usado para calcular el tiempo entre diferentes status saved
	
	// identificador 0-based para identificar la action y para saber qué rama de la exploración tomar cuando esté en POSICION_MULTI_PROCESSES
	public final int id;
	
	private final long MAX_CICLOS;
	private int num_processes;
	private final int POSICION_START_FORK_JOIN;
	private final int LIMITE_RESULTADO_PARCIAL;
	private final boolean usar_poda_color_explorado;
	private final boolean FairExperimentGif;
	private final boolean usarTableroGrafico;
	private long count_cycles;
	private final int[] num_processes_orig = new int[SolverFaster.MAX_PIEZAS];
	private int pos_multi_process_offset = 0; // usado con POSICION_MULTI_PROCESSES sirve para continuar haciendo los calculos de distribución de exploración
	
	private CountDownLatch startSignal;
	private CountDownLatch doneSignal;
	
	public ExploracionAction(int _id, int _num_processes, long _max_ciclos, int _pos_start_fork_join, int limite_resultado_parcial, 
			boolean _usar_poda_color_explorado, boolean _FairExperimentGif, boolean _usarTableroGrafico, 
			CountDownLatch startSignal, CountDownLatch doneSignal) {
		
		id = _id;
		MAX_CICLOS = _max_ciclos;
		num_processes = _num_processes;
		POSICION_START_FORK_JOIN = _pos_start_fork_join;
		LIMITE_RESULTADO_PARCIAL = limite_resultado_parcial;
		usar_poda_color_explorado = _usar_poda_color_explorado;
		FairExperimentGif = _FairExperimentGif;
		usarTableroGrafico = _usarTableroGrafico;
		
		statusFileName = SolverFaster.NAME_FILE_STATUS + "_" + id + SolverFaster.FILE_EXT;
		parcialFileName = SolverFaster.NAME_FILE_PARCIAL + "_" + id + SolverFaster.FILE_EXT;
		parcialMaxFileName = SolverFaster.NAME_FILE_PARCIAL_MAX + "_" + id + SolverFaster.FILE_EXT;
		disposicionMaxFileName = SolverFaster.NAME_FILE_DISPOSICIONES_MAX + "_" + id + SolverFaster.FILE_EXT;
		libresMaxFileName = SolverFaster.NAME_FILE_LIBRES_MAX + "_" + id + SolverFaster.FILE_EXT;
		solucFileName = SolverFaster.NAME_FILE_SOLUCION + "_" + id + SolverFaster.FILE_EXT;
		dispFileName = SolverFaster.NAME_FILE_DISPOSICION + "_" + id + SolverFaster.FILE_EXT;

		this.startSignal = startSignal;
		this.doneSignal = doneSignal;
	}

	public void setupInicial() {
		
		// cargo las piezas desde archivo de piezas
		SolverFaster.cargarPiezas(this);
		
		// hago una verificacion de las piezas cargadas
		SolverFaster.verificarTiposDePieza(this);
		
		// cargar la super estructura 4-dimensional
		SolverFaster.cargarSuperEstructura(this);
		
		// Pruebo cargar el primer status_saved
		status_cargado = SolverFaster.cargarEstado(statusFileName, this);
		
		// cargo las posiciones fijas
		SolverFaster.cargarPiezasFijas(this); // OJO! antes debo cargar matrix_zonas[]
		
		// seteo como usados los contornos ya existentes en tablero
		contorno.inicializarContornos(this.tablero);
		
		// this call avoids a OutOfHeapMemory error
		System.gc();
	}

	public void resetForAtaque(int _num_processes, CountDownLatch startSignal, CountDownLatch doneSignal) {
		
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
		this.doneSignal = doneSignal;

		for (int k=0; k < tablero.length; ++k) {
			tablero[k] = null;
		}
		
		SolverFaster.cargarPiezasFijas(this);
		
		contorno.resetContornos();
	}

	@Override
	public void compute() {
		try {
			// await for starting signal
			startSignal.await();
			// start working
			doWork();
		} catch (InterruptedException e) {
			System.out.println("ExplorationAction interrupted.");
		} finally {
			doneSignal.countDown();
		}
	}

	private void doWork() {
				
		if (SolverFaster.flag_retroceder_externo) {
			SolverFaster.retrocederEstado(this);
			return;
		}
		
		System.out.println(id + " >>> Buscando soluciones...");
		
		time_inicial = time_status_saved = System.nanoTime();
		
		//si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado)
			explorar();
		//se carga estado de exploración, debo proveer la posibilidad de volver estados anteriores de exploracion
		else {
			//ahora exploro comunmente y proveo una especie de recursividad para retroceder estados
			while (cursor >= 0) {
				if (!retroceder) {
					// pregunto si llegué al limite de esta instancia de exploracion
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
				
				//seteo los contornos como libres
				getIndexDeContornoYaPuesto();
				setContornoLibre();
				index_sup = -1;
				//index_inf = -1;

				// debo setear la pieza en cursor como no usada y sacarla del tablero
				if (cursor != SolverFaster.POSICION_CENTRAL) {
					tablero[cursor].usada = false;
					tablero[cursor]= null;
				}
				
				// si retrocedó hasta el cursor destino, entonces no retrocedo mas
				/*
				 * @RETROCEDER if (cursor <= cur_destino){ retroceder= false; cur_destino= CURSOR_INVALIDO; }
				 * 
				 * //si está activado el flag para retroceder niveles de exploracion entonces debo limpiar algunas cosas
				 * if (retroceder) desde_saved[cursor]= 0; //la exploracion de posibles piezas para la posicion cursor
				 * debe empezar desde la primer pieza
				 */
			}
		}
		
		//si llego hasta esta sentencia significa una sola cosa:
		System.out.println(id + " >>> exploracion agotada.");

//		if (send_mail) { // Envio un mail diciendo que no se encontró solución
//			SendMail em = new SendMail();
//			em.setDatos("Exploracion agotada para el caso " + CASO, "Sin solucion, caso " + CASO);
//			Thread t = new Thread(em);
//			t.start();
//		}
	}

	
	
	
	//##########################################################################//
	// METODO CENTRAL. ES EL BACKTRACKING QUE RECORRE EL TABLERO Y COLOCA PIEZAS
	//##########################################################################//

	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final void explorar()
	{	
		//#############################################################################################
		/**
		 * Cabeza de exploración.
		 * Representa las primeras sentencias del backtracking de exploracion. Pregunta
		 * algunas cositas antes de empezar una nueva instancia de exploracion.
		 */
		//#############################################################################################
		//si cursor se pasa del limite de piezas, significa que estoy en una solucion
		if (cursor >= SolverFaster.MAX_PIEZAS) {
			SolverFaster.guardarSolucion(this);
			System.out.println(id + " >>> Solucion Encontrada!!");
			return; //evito que la instancia de exploracion continue
		}
		
		//si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (cursor > mas_lejano_parcial_max) {
			mas_lejano_parcial_max = cursor;
			if (cursor >= LIMITE_RESULTADO_PARCIAL) {
				long time_final = System.nanoTime();
				System.out.println(id + " >>> "
						+ TimeUnit.MILLISECONDS.convert(time_final - time_inicial, TimeUnit.NANOSECONDS)
						+ " ms, cursor " + cursor);
				SolverFaster.guardarResultadoParcial(true, this);
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
			mas_bajo = SolverFaster.MAX_PIEZAS;
			mas_bajo_activo = true;
		}
		
		//si llegué a MAX_CICLOS de ejecucion, guardo el estado de exploración
		
		if (count_cycles >= MAX_CICLOS) {
			long currentCycles = count_cycles;
			count_cycles = 0;
			if (usarTableroGrafico)
				SolverFaster.count_cycles[id] = 0;
			//calculo el tiempo transcurrido desd el último time_status_saved 
			long nanoTimeNow = System.nanoTime();
			long durationNanos = nanoTimeNow - time_status_saved;
			long durationMillis = TimeUnit.MILLISECONDS.convert(durationNanos, TimeUnit.NANOSECONDS);
			SolverFaster.guardarEstado(statusFileName, this);
			SolverFaster.guardarResultadoParcial(false, this);
			long piecesPerSec = currentCycles * 1000000000L / durationNanos; // multiply by 10^9 to convert nanos into seconds
			System.out.println(id + " >>> Estado guardado en cursor " + cursor + ". Pos Min " + mas_bajo 
					+ ", Pos Max " + mas_alto + ". Tiempo: " + durationMillis + " ms" 
					+ ", " + piecesPerSec + " pieces/sec");
			time_status_saved = nanoTimeNow;
			//cuando se cumple el ciclo aumento de nuevo el valor de mas_bajo y disminuyo el de mas_alto
			mas_bajo = SolverFaster.MAX_PIEZAS;
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
		//si la posicion cursor es una posicion fija no tengo que hacer la exploracion "estandar". Se supone que la pieza fija ya est� debidamente colocada
		if (cursor == SolverFaster.POSICION_CENTRAL) {
			//seteo los contornos como usados
			getIndexDeContornoYaPuesto();
			setContornoUsado();
			int index_sup_aux = index_sup;
			//@CONTORNO_INFERIORint index_inf_aux = index_inf;
			++cursor;
			explorar();
			--cursor;
			//seteo los contornoscomo libres
			index_sup = index_sup_aux;
			//@CONTORNO_INFERIORindex_inf = index_inf_aux;
			setContornoLibre();
			/*@RETROCEDER
			if (cursor <= cur_destino){
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
		if (esContornoUsado())
			return;

		//#############################################################################################
		
		
		//#############################################################################################
		//ahora hago la exploracion
		exploracionStandard();
		//#############################################################################################
	}
	
	/**
	 * Realiza toda la exploracion standard: cicla sobre las posibles piezas para las
	 * posicon actual de cursor, y cicla sobre las posibles rotaciones de cada pieza.
	 * Aplica varias podas que solamente son validas en este nivel de exploracion.
	 */
	private final void exploracionStandard()
	{
		// voy a recorrer las posibles piezas que coinciden con los colores de las piezas alrededor de cursor
		NodoPosibles nodoPosibles = obtenerPosiblesPiezas();
		if (nodoPosibles == null)
			return; // significa que no existen posibles piezas para la actual posicion de cursor

		int desde = desde_saved[cursor];
		int length_posibles = nodoPosibles.referencias.length;
		final byte flag_zona = SolverFaster.matrix_zonas[cursor];
		int index_sup_aux;
		final int fila_actual = cursor >> SolverFaster.LADO_SHIFT_AS_DIVISION; // if divisor is power of 2 then we can use >>
		// For modulo try this for better performance only if divisor is power of 2: dividend & (divisor - 1)
		// old was: ((cursor+2) % LADO) == 0
		final boolean flag_antes_borde_right = ((cursor + 2) & (SolverFaster.LADO - 1)) == 0;
		
		num_processes_orig[cursor] = num_processes;

		// En modo multiproceso tengo que establecer los limites de las piezas a explorar para este proceso.
		// En este paso solo inicializo algunas variables para futuros cálculos.
		if (cursor == POSICION_START_FORK_JOIN + pos_multi_process_offset) {
			// en ciertas condiciones cuado se disminuye el num de procs, es necesario acomodar el concepto de this_proc
			// para los calculos siguientes
			int this_proc_absolute = id % num_processes;

			// caso 1: trivial. Cada proc toma una única rama de nodoPosibles
			if (num_processes == length_posibles) {
				desde = this_proc_absolute;
				length_posibles = this_proc_absolute + 1;
			}
			// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas
			else if (num_processes < length_posibles) {
				int span = (length_posibles + 1) / num_processes;
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
				int divisor = (num_processes + 1) / length_posibles; // reparte los procs por posible pieza
				num_processes = length_posibles;
				desde = this_proc_absolute / divisor;
				if (desde >= length_posibles)
					desde = length_posibles - 1;
				length_posibles = desde + 1;
				++pos_multi_process_offset;
			}
			// System.out.println("Rank " + THIS_PROCESS + ":::: Total " + posibles.referencias.length + ". Limites " +
			// desde + "," + length_posibles);
			// System.out.flush();
		}
		
		for (; desde < length_posibles; ++desde)
		{
			// desde_saved[cursor]= desde; //actualizo la posicion en la que leo de posibles
			Pieza p = nodoPosibles.referencias[desde];
			byte rot = nodoPosibles.rots[desde];
			
			// pregunto si la pieza candidata está siendo usada
			if (p.usada)
				continue; //es usada, pruebo con la siguiente pieza
	
			++count_cycles;
			if (usarTableroGrafico)
				++SolverFaster.count_cycles[id]; //incremento el contador de combinaciones de piezas
			
			// Pregunto si la pieza a poner es del tipo adecuado segun cursor.
			// Porque sucede que puedo obtener cualquier tipo de pieza de acuerdo a los colores que necesito empiezo con
			// la más común que es interior
			if (flag_zona == SolverFaster.F_INTERIOR ) {
				// si pieza actual no es interior
				if (p.feature != 0) continue;
			}
			// mayor a F_INTERIOR significa que estoy en borde
			else if (flag_zona > SolverFaster.F_INTERIOR) {
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
				// si estoy antes del borde right limpio el arreglo de colores right usados
				if (flag_antes_borde_right)
					SolverFaster.arr_color_rigth_explorado.getAndSet(fila_actual + 1, 0);
				
				if (flag_zona == SolverFaster.F_BORDE_LEFT)
				{
					final int mask = 1 << p.right;
					// pregunto si el color right de la pieza de borde left actual ya está explorado
					if ((SolverFaster.arr_color_rigth_explorado.get(fila_actual) & mask) != 0) {
						p.usada = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue; // sigo con otra pieza de borde
					}
					// si no es así entonces lo seteo como explorado
					else {
						// asignación en una sola operación, ya que el bit en p.right vale 0 (según la condición anterior)
						SolverFaster.arr_color_rigth_explorado.addAndGet(fila_actual, mask);
						// int value = SolverFaster.arr_color_rigth_explorado.get(fila_actual) | 1 << p.right;
						// SolverFaster.arr_color_rigth_explorado.getAndSet(fila_actual, value);
					}
				}
			}
			
			//#### En este punto ya tengo la pieza correcta para poner en tablero[cursor] ####
			
			tablero[cursor] = p; //en la posicion "cursor" del tablero pongo la pieza de indice "indice"
			p.usada = true; //en este punto la pieza va a ser usada
			Pieza.llevarARotacion(p, rot);
			//p.pos= cursor; //la pieza sera usada en la posicion cursor
			
			//#### En este punto ya tengo la pieza colocada y rotada correctamente ####
	
			// una vez rotada adecuadamente la pieza pregunto si el borde inferior que genera está siendo usado
			/*@CONTORNO_INFERIORif (esContornoInferiorUsado()){
				p.usada = false; //la pieza ahora no es usada
				//p.pos= -1;
				continue;
			}*/
			
			//FairExperiment.gif: color bottom repetido en sentido horizontal
			if (FairExperimentGif)
			{
				if (flag_zona == SolverFaster.F_INTERIOR || flag_zona == SolverFaster.F_BORDE_TOP)
					if (p.bottom == tablero[cursor-1].bottom)
					{
						p.usada = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue;
					}
			}
	
			//seteo los contornos como usados
			getIndexDeContornoYaPuesto();
			setContornoUsado();
			index_sup_aux = index_sup;
			//@CONTORNO_INFERIORindex_inf_aux = index_inf;
				
			//##########################
			//Llamo una nueva instancia
			++cursor;
			explorar();
			--cursor;
			//##########################
				
			//seteo los contornos como libres
			index_sup = index_sup_aux;
			//@CONTORNO_INFERIORindex_inf = index_inf_aux;
			setContornoLibre();
			
			p.usada = false; //la pieza ahora no es usada
			//p.pos= -1;
			
			//si retrocedí hasta la posicion destino, seteo la variable retroceder en false e invalído a cur_destino
			/*@RETROCEDER
			if (cursor <= cur_destino){
				retroceder= false;
				cur_destino=CURSOR_INVALIDO;
			}
			//caso contrario significa que todavia tengo que seguir retrocediendo
			if (retroceder)
				break;*/
		}//fin bucle posibles piezas
		
		desde_saved[cursor] = 0; //debo poner que el desde inicial para este cursor sea 0
		tablero[cursor] = null; //dejo esta posicion de tablero libre

		num_processes = num_processes_orig[cursor];
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
	 * NOTA: saqué muchas sentencias porque solamente voy a tener una pieza fija (en la pos 135), por eso 
	 * este metodo solo contempla las piezas top y left, salvo en el vecindario de la pieza fija.
	 */
	final NodoPosibles obtenerPosiblesPiezas()
	{
		switch (cursor) {
			//pregunto si me encuentro en la posicion inmediatamente arriba de la posicion central
			case SolverFaster.SOBRE_POSICION_CENTRAL:
				return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom,
						SolverFaster.MAX_COLORES, piezas[SolverFaster.INDICE_P_CENTRAL].top, tablero[cursor - 1].right)];
			//pregunto si me encuentro en la posicion inmediatamente a la izq de la posicion central
			case SolverFaster.ANTE_POSICION_CENTRAL:
				return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom, piezas[SolverFaster.INDICE_P_CENTRAL].left,
						SolverFaster.MAX_COLORES, tablero[cursor - 1].right)];
		}
		
		final int flag_m = SolverFaster.matrix_zonas[cursor];
		
		// estoy en interior de tablero?
		if (flag_m == SolverFaster.F_INTERIOR) 
			return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES,
					SolverFaster.MAX_COLORES, tablero[cursor - 1].right)];
		// mayor a F_INTERIOR significa que estoy en borde
		else if (flag_m > SolverFaster.F_INTERIOR) {
			switch (flag_m) {
				//borde right
				case SolverFaster.F_BORDE_RIGHT:
					return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom, SolverFaster.GRIS,
							SolverFaster.MAX_COLORES, tablero[cursor - 1].right)];
				//borde left
				case SolverFaster.F_BORDE_LEFT:
					return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom,
							SolverFaster.MAX_COLORES, SolverFaster.MAX_COLORES, SolverFaster.GRIS)];
				// borde top
				case SolverFaster.F_BORDE_TOP:
					return super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.MAX_COLORES,
							SolverFaster.MAX_COLORES, tablero[cursor - 1].right)];
				//borde bottom
				default:
					return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom,
							SolverFaster.MAX_COLORES, SolverFaster.GRIS, tablero[cursor - 1].right)];
			}
		}
		// menor a F_INTERIOR significa que estoy en esquina
		else {
			switch (flag_m) {
				//esquina top-left
				case SolverFaster.F_ESQ_TOP_LEFT:
					return super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.MAX_COLORES,
							SolverFaster.MAX_COLORES, SolverFaster.GRIS)];
				//esquina top-right
				case SolverFaster.F_ESQ_TOP_RIGHT:
					return super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.GRIS, SolverFaster.MAX_COLORES,
							tablero[cursor - 1].right)];
				//esquina bottom-left
				case SolverFaster.F_ESQ_BOTTOM_LEFT: 
					return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom,
							SolverFaster.MAX_COLORES, SolverFaster.GRIS, SolverFaster.GRIS)];
					//esquina bottom-right
				default:
					return super_matriz[MapaKeys.getKey(tablero[cursor - SolverFaster.LADO].bottom, SolverFaster.GRIS,
							SolverFaster.GRIS, tablero[cursor - 1].right)];
			}
		}
	}

	/**
	 * Usado para obtener los indices de los contornos que voy a setear como usados o como libres.
	 * NOTA: index_sup sirve para contorno superior e index_inf para contorno inferior.
	 * @return
	 */
	private final void getIndexDeContornoYaPuesto() {
		// primero me fijo si estoy en posición válida
		if (SolverFaster.zona_proc_contorno[cursor] == false) {
			index_sup = -1;
			//@CONTORNO_INFERIORindex_inf = -1;
			return;
		}
	
		//obtengo las claves de acceso
		switch (Contorno.MAX_COLS){
			case 2:
				index_sup = Contorno.getIndex(tablero[cursor - 1].left, tablero[cursor - 1].top, tablero[cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top);*/
				break;
			case 3:
				index_sup = Contorno.getIndex(tablero[cursor - 2].left, tablero[cursor - 2].top,
						tablero[cursor - 1].top, tablero[cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top, tablero[cursor-2].top);*/
				break;
			case 4:
				index_sup = Contorno.getIndex(tablero[cursor - 3].left, tablero[cursor - 3].top,
						tablero[cursor - 2].top, tablero[cursor - 1].top, tablero[cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top, tablero[cursor-2].top, tablero[cursor-3].top);*/
				break;
			default: break;
		}
	}

	/*@CONTORNO_INFERIOR
	public final boolean esContornoInferiorUsado(){
		//primero me fijo si estoy en la posición correcta para preguntar por contorno inferior usado
		if (zona_proc_contorno[cursor] == false)
			return false;
		//debo estar entre filas [2,13]
		if (cursor < 33 || cursor > 238)
			return false;
	
		//obtengo la clave del contorno inferior
		switch (Contorno.MAX_COLS){
			case 2:
				auxi = Contorno.getIndex(tablero[cursor].right, tablero[cursor].bottom, tablero[cursor-1].bottom);
				break;
			case 3:
				auxi = Contorno.getIndex(tablero[cursor].right, tablero[cursor].bottom, tablero[cursor-1].bottom, tablero[cursor-2].bottom);
				break;
			case 4:
				auxi = Contorno.getIndex(tablero[cursor].right, tablero[cursor].bottom, tablero[cursor-1].bottom, tablero[cursor-2].bottom, tablero[cursor-3].bottom);
				break;
			default: return false;
		}
	
		//si el contorno está siendo usado entonces devuelvo true
		if (Contorno.contornos_used[auxi])
			return true;
	
		return false;
	}*/
	
	private final void setContornoUsado()
	{
		if (index_sup != -1)
			contorno.contornos_used[index_sup] = true;
		/*@CONTORNO_INFERIORif (index_inf != -1)
			contorno.contornos_used[index_inf] = true;*/
	}

	private final void setContornoLibre()
	{
		if (index_sup != -1)
			contorno.contornos_used[index_sup] = false;
		/*@CONTORNO_INFERIORif (index_inf != -1)
			contorno.contornos_used[index_inf] = false;*/
	}

	private final boolean esContornoUsado()
	{
		// primero me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (SolverFaster.zona_read_contorno[cursor] == false)
			return false;
		
		// obtengo la clave del contorno superior
		int i_count = cursor - SolverFaster.LADO;
		int auxi;
		switch (Contorno.MAX_COLS) {
			case 2:
				auxi = Contorno.getIndex(tablero[cursor - 1].right, tablero[i_count].bottom,
						tablero[i_count + 1].bottom);
				break;
			case 3:
				auxi = Contorno.getIndex(tablero[cursor - 1].right, tablero[i_count].bottom,
						tablero[i_count + 1].bottom, tablero[i_count + 2].bottom);
				break;
			case 4:
				auxi = Contorno.getIndex(tablero[cursor - 1].right, tablero[i_count].bottom,
						tablero[i_count + 1].bottom, tablero[i_count + 2].bottom, tablero[i_count + 3].bottom);
				break;
			default: return false;
		}
		
		// si el contorno está siendo usado entonces devuelvo true
		if (contorno.contornos_used[auxi])
			return true;
		
		return false;
	}

}