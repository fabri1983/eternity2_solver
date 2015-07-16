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

package eternity_faster_pkg;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * This action doesn't create sub actions (it doesn't fork/join)
 * Extends from RecursiveAction (do not yield a return value).
 */
public class ExploracionAction extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	
	protected String statusFileName, parcialFileName, parcialMaxFileName, disposicionMaxFileName, libresMaxFileName, solucFileName, dispFileName;
	
	/**
	 * Cada indice del arreglo definido en el orden (top,right,bottom,left) contiene una lista de {@link NodoPosibles} con las piezas que cumplen con esos colores.
	 * Para el cálculo de la capacidad de la matriz de combinaciones de colores se tienen en cuenta solo combinaciones top,right,bottom,left de colores. 
	 */
	protected final NodoPosibles super_matriz[] = new NodoPosibles[(SolverFaster.MAX_COLORES << 20) | (SolverFaster.MAX_COLORES << 15) | (SolverFaster.MAX_COLORES << 10) | (SolverFaster.MAX_COLORES << 5) | SolverFaster.MAX_COLORES];
	
	public final Pieza piezas[] = new Pieza[SolverFaster.MAX_PIEZAS];
	public final Pieza tablero[] = new Pieza[SolverFaster.MAX_PIEZAS];
	
	public int cursor, mas_bajo, mas_alto, mas_lejano_parcial_max;
	protected final byte desde_saved[] = new byte[SolverFaster.MAX_PIEZAS];
	private Contorno contorno = new Contorno();
	protected boolean retroceder; // indica si debo volver estados de backtracking
	private boolean status_cargado; // inidica si se ha cargado estado inicial
	protected boolean mas_bajo_activo; // permite o no modificar el cursor mas_bajo
	protected int sig_parcial = 1; //esta variable indica el numero de archivo parcial siguiente a guardar
	
	//VARIABLES GLOBALES para evitar ser declaradas cada vez que se llame a determinado método
	protected Pieza pzxc; // se usa solamente en explorarPiezaCentral()
	private NodoPosibles xposibles; //empleado en explorar()
	private Pieza pieza_extern_loop; //empleada en atacar() y en obtenerPosPiezaFaltanteAnteCentral()
	private int index_sup; //empleados en varios m�todos para pasar info

	private long time_inicial, time_final; // sirven para calcular el tiempo al hito de posición lejana
	private long time_status_saved; //usado para calcular el tiempo entre diferentes status saved
	
	// identificador 0-based para identificar la action y para saber qué rama de la exploración tomar cuando esté en POSICION_MULTI_PROCESSES
	protected final int id; 
	
	public ExploracionAction(int _id) {
		id = _id;
		statusFileName = SolverFaster.NAME_FILE_STATUS + "_" + id + SolverFaster.FILE_EXT;
		parcialFileName = SolverFaster.NAME_FILE_PARCIAL + "_" + id + SolverFaster.FILE_EXT;
		parcialMaxFileName = SolverFaster.NAME_FILE_PARCIAL_MAX + "_" + id + SolverFaster.FILE_EXT;
		disposicionMaxFileName = SolverFaster.NAME_FILE_DISPOSICIONES_MAX + "_" + id + SolverFaster.FILE_EXT;
		libresMaxFileName = SolverFaster.NAME_FILE_LIBRES_MAX + "_" + id + SolverFaster.FILE_EXT;
		solucFileName = SolverFaster.NAME_FILE_SOLUCION + "_" + id + SolverFaster.FILE_EXT;
		dispFileName = SolverFaster.NAME_FILE_DISPOSICION + "_" + id + SolverFaster.FILE_EXT;
	}
	
	@Override
	public void compute() {
		
		// Pruebo cargar el primer status_saved
		status_cargado = SolverFaster.cargarEstado(statusFileName, this);
		
		// cargo las posiciones fijas
		SolverFaster.cargarPiezasFijas(this); // OJO! antes debo cargar matrix_zonas[]
		
		// seteo como usados los contornos ya existentes en tablero
		contorno.inicializarContornos(this);
				
		if (SolverFaster.flag_retroceder_externo) {
			SolverFaster.retrocederEstado(this);
			return;
		}
		
		System.out.println(id + " >>> Buscando soluciones...");
		
		time_inicial = time_status_saved = System.nanoTime();
		
		//si no se carga estado de exploracion, simplemente exploro desde el principio
		if (!status_cargado)
			explorar(this);
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
					explorar(this);
				}
				--cursor;
				
				//si me paso de la posicion inicial significa que no puedo volver mas estados de exploracion
				if (cursor < 0)
					break; //obliga a salir del while
				
				//seteo los contornos como libres
				getIndexDeContornoYaPuesto(this);
				setContornoLibre(this);
				index_sup = -1;
				//index_inf = -1;

				//debo setear la pieza en cursor como no usada
				if (cursor != SolverFaster.POSICION_CENTRAL){
					pieza_extern_loop= tablero[cursor];
					pieza_extern_loop.pusada.value = false; // la seteo como no usada xq sino la exploración pensará que
															// está usada (porque asi es como se guardó)
					//pzz.pos= -1;
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
		System.out.println(id + " >>> NO se ha encontrado solucion."); //ittai! (pero qué?!!)

		// if (send_mail){ //Envio un mail diciendo que no se encontró solución
		// SendMail em= new SendMail();
		// em.setDatos("NO se ha encontrado solucion para el caso " + CASO, "Sin solucion, caso " + CASO);
		// Thread t= new Thread(em);
		// t.start();
		// }
	}
	
	
	//##########################################################################//
	// METODO CENTRAL. ES EL BACKTRACKING QUE RECORRE EL TABLERO Y COLOCA PIEZAS
	//##########################################################################//

	/**
	 * Para cada posicion de cursor, busca una pieza que se adecue a esa posicion
	 * del tablero y que concuerde con las piezas vecinas. Aplica diferentes podas
	 * para acortar el número de intentos.
	 */
	private final static void explorar(ExploracionAction action)
	{	
		//#############################################################################################
		/**
		 * Cabeza de exploración.
		 * Representa las primeras sentencias del backtracking de exploracion. Pregunta
		 * algunas cositas antes de empezar una nueva instancia de exploracion.
		 */
		//#############################################################################################
		//si cursor se pasa del limite de piezas, significa que estoy en una solucion
		if (action.cursor >= SolverFaster.MAX_PIEZAS){
			SolverFaster.guardarSolucion(action);
			System.out.println(action.id + " >>> Solucion Encontrada!!");
			return; //evito que la instancia de exporacion continue
		}
		
		//si cursor pasó el cursor mas lejano hasta ahora alcanzado, guardo la solucion parcial hasta aqui lograda
		if (action.cursor > action.mas_lejano_parcial_max){
			action.mas_lejano_parcial_max= action.cursor;
			if (action.cursor >= SolverFaster.LIMITE_RESULTADO_PARCIAL){
				action.time_final= System.nanoTime();
				System.out.println(action.id + " >>> " + TimeUnit.MILLISECONDS.convert(action.time_final - action.time_inicial, TimeUnit.NANOSECONDS) + " ms, cursor " + action.cursor);
				SolverFaster.guardarResultadoParcial(true, action);
			}
		}
		
		//voy manteniendo el cursor mas alto para esta vuelta de ciclos
		if (action.cursor > action.mas_alto)
			action.mas_alto = action.cursor;
		//si cursor se encuentra en una posicion mas baja que la posicion mas baja alcanzada guardo ese valor
		if (action.cursor < action.mas_bajo)
			action.mas_bajo= action.cursor;
		//la siguiente condición se cumple una sola vez
		if (action.cursor > 100 && !action.mas_bajo_activo){
			action.mas_bajo= SolverFaster.MAX_PIEZAS;
			action.mas_bajo_activo= true;
		}
		
		//si llegué a MAX_CICLOS de ejecucion, guardo el estado de exploración
		if (SolverFaster.count_cycles[action.id] >= SolverFaster.MAX_CICLOS){
			SolverFaster.count_cycles[action.id] = 0;
			//calculo el tiempo entre status saved
			long mili_temp = System.nanoTime();
			SolverFaster.guardarEstado(action.statusFileName, action);
			SolverFaster.guardarResultadoParcial(false, action);
			System.out.println(action.id + " >>> Estado guardado en cursor " + action.cursor + ". Pos Min " + action.mas_bajo + ", Pos Max " + action.mas_alto + ". Tiempo: " + TimeUnit.MILLISECONDS.convert(mili_temp - action.time_status_saved, TimeUnit.NANOSECONDS) + " ms.");				
			action.time_status_saved = mili_temp;
			//cuando se cumple el ciclo aumento de nuevo el valor de mas_bajo y disminuyo el de mas_alto
			action.mas_bajo = SolverFaster.MAX_PIEZAS;
			action.mas_alto = 0;
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
		if (action.cursor == SolverFaster.POSICION_CENTRAL){
			//seteo los contornos como usados
			getIndexDeContornoYaPuesto(action);
			setContornoUsado(action);
			int index_sup_aux = action.index_sup;
			//@CONTORNO_INFERIORint index_inf_aux = index_inf;
			++action.cursor;
			explorar(action);
			--action.cursor;
			//seteo los contornoscomo libres
			action.index_sup = index_sup_aux;
			//@CONTORNO_INFERIORindex_inf = index_inf_aux;
			setContornoLibre(action);
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
		if (esContornoUsado(action))
			return;
		
		//voy a recorrer las posibles piezas que coinciden con los colores de las piezas alrededor de cursor
		action.xposibles= obtenerPosiblesPiezas(action);
		if (action.xposibles == null)
			return; //significa que no existen posibles piezas para la actual posicion de cursor

		//#############################################################################################
		
		
		//#############################################################################################
		//ahora hago la exploracion
		exploracionStandard(action);
		//#############################################################################################
	}
	
	/**
	 * Realiza toda la exploracion standard: cicla sobre las posibles piezas para las
	 * posicon actual de cursor, y cicla sobre las posibles rotaciones de cada pieza.
	 * Aplica varias podas que solamente son validas en este nivel de exploracion.
	 */
	private final static void exploracionStandard(ExploracionAction action)
	{
		//declaro las variables que usará esta instancia de la exploracion
		final NodoPosibles posibles = action.xposibles; //copio la referencia porque en el siguiente llamado xposibles cambia
		int desde = action.desde_saved[action.cursor];
		int length_posibles = posibles.referencias.length;
		final byte flag_zona = SolverFaster.matrix_zonas[action.cursor];
		int index_sup_aux;
		final int fila_actual = action.cursor >> SolverFaster.LADO_SHIFT_AS_DIVISION; // if divisor is power of 2 then we can use >>
		// for modulo try this for better performance only if divisor is power of 2: dividend & (divisor - 1)
		final boolean flag_antes_borde_right = ((action.cursor+2) & (SolverFaster.LADO - 1)) == 0; // ((cursor+2) % LADO) == 0
		
		// si estoy ejecutando modo multiproceso tengo que establecer los limites de las piezas a explorar para este proceso
		if (action.cursor == SolverFaster.POSICION_START_FORK_JOIN)
		{
			// primero preguntar si este proc no puede tomar una parte de la exploración (pues la toman los procs mas chicos)
			if (action.id >= length_posibles) // comparo usando >= para no restarle 1 a length_posibles
			{
				length_posibles = 0; // seteo 0 asi no explora
				//System.out.println(action.id + " :::: no procesa pues excede a length_posibles");
				//System.out.flush();
			}
			else
			{
				int resto = length_posibles % SolverFaster.NUM_PROCESSES;
				// valor inicial en caso de que length_posibles < num processes.
				// En ese caso debo explorar la parte que le corresponda a THIS_PROCESS
				int division = 1;
				
				if (length_posibles >= SolverFaster.NUM_PROCESSES)
					division = length_posibles / SolverFaster.NUM_PROCESSES;
				
				desde = action.id * division;
				
				// si es el último proc le agrego el resto (solo tiene efecto si la división no es exacta)
				if (action.id == (SolverFaster.NUM_PROCESSES - 1))
					division += resto;
				
				length_posibles = desde + division;
				//System.out.println(action.id + " :::: Total " + posibles.referencias.length + ". Limites " + desde + "," + length_posibles);
				//System.out.flush();
			}
		}
		
		for (; desde < length_posibles; ++desde)
		{
			//desde_saved[cursor]= desde; //actualizo la posicion en la que leo de posibles
			Pieza p = posibles.referencias[desde]; //el nodo contiene el indice de la pieza a probar y sus rotacs permitidas
			
			//pregunto si la pieza candidata est� siendo usada
			if (p.pusada.value)
				continue; //es usada, pruebo con la siguiente pieza
	
			++SolverFaster.count_cycles[action.id]; //incremento el contador de combinaciones de piezas
			
			//pregunto si la pieza a poner es del tipo adecuado segun cursor. Porque sucede que puedo obtener cualquier tipo de pieza de acuerdo a los colores que necesito
			// empiezo con la mas comun que es interior
			if (flag_zona == SolverFaster.F_INTERIOR ) {
				if (!p.es_interior) continue;
			}
			// mayor a F_INTERIOR significa que estoy en borde
			else if (flag_zona > SolverFaster.F_INTERIOR) {
				if (!p.es_borde) continue;
			}
			// menor a F_INTERIOR significa que estoy en esquina
			else {
				if (!p.es_esquina) continue;
			}
				
			//pregunto si est� activada la poda del color right explorado en borde left 
			if (SolverFaster.usar_poda_color_explorado)
			{
				//si estoy antes del borde right limpio el arreglo de colores right usados
				if (flag_antes_borde_right)
					SolverFaster.arr_color_rigth_explorado[fila_actual + 1] = 0;
				
				if (flag_zona == SolverFaster.F_BORDE_LEFT)
				{
					//pregunto si el color right de la pieza de borde left actual ya est� explorado
					if ((SolverFaster.arr_color_rigth_explorado[fila_actual] & (1 << p.right)) != 0){
						p.pusada.value = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue; //sigo con otra pieza de borde
					}
					//si no es as� entonces lo seteo como explorado
					else {
						final int value = SolverFaster.arr_color_rigth_explorado[fila_actual] | 1 << p.right; 
						SolverFaster.arr_color_rigth_explorado[fila_actual] = value;
					}
				}
			}
			
			//#### En este punto ya tengo la pieza correcta para poner en tablero[cursor] ####
			
			action.tablero[action.cursor] = p; //en la posicion "cursor" del tablero pongo la pieza de indice "indice"
			p.pusada.value = true; //en este punto la pieza va a ser usada
			//p.pos= cursor; //la pieza sera usada en la posicion cursor
			
			//#### En este punto ya tengo la pieza colocada y rotada correctamente ####
	
			//una vez rotada adecuadamente la pieza pregunto si el borde inferior que genera est� siendo usado
			/*@CONTORNO_INFERIORif (esContornoInferiorUsado()){
				p.pusada.value = false; //la pieza ahora no es usada
				//p.pos= -1;
				continue;
			}*/
			
			//FairExperiment.gif: color bottom repetido en sentido horizontal
			if (SolverFaster.FairExperimentGif)
			{
				if (flag_zona == SolverFaster.F_INTERIOR || flag_zona == SolverFaster.F_BORDE_TOP)
					if (p.bottom == action.tablero[action.cursor-1].bottom)
					{
						p.pusada.value = false; //la pieza ahora no es usada
						//p.pos= -1;
						continue;
					}
			}
	
			//seteo los contornos como usados
			getIndexDeContornoYaPuesto(action);
			setContornoUsado(action);
			index_sup_aux = action.index_sup;
			//@CONTORNO_INFERIORindex_inf_aux = index_inf;
				
			//##########################
			//Llamo una nueva instancia
			++action.cursor;
			explorar(action);
			--action.cursor;
			//##########################
				
			//seteo los contornos como libres
			action.index_sup = index_sup_aux;
			//@CONTORNO_INFERIORindex_inf = index_inf_aux;
			setContornoLibre(action);
			
			p.pusada.value = false; //la pieza ahora no es usada
			//p.pos= -1;
			
			//si retroced� hasta la posicion destino, seteo la variable retroceder en false e inval�do a cur_destino
			/*@RETROCEDER
			if (cursor <= cur_destino){
				retroceder= false;
				cur_destino=CURSOR_INVALIDO;
			}
			//caso contrario significa que todavia tengo que seguir retrocediendo
			if (retroceder)
				break;*/
		}//fin bucle posibles piezas
		
		action.desde_saved[action.cursor] = 0; //debo poner que el desde inicial para este cursor sea 0
		action.tablero[action.cursor] = null; //dejo esta posicion de tablero libre
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
	protected final static NodoPosibles obtenerPosiblesPiezas(ExploracionAction action)
	{
		switch (action.cursor) {
			//pregunto si me encuentro en la posicion inmediatamente arriba de la posicion central
			case SolverFaster.SOBRE_POSICION_CENTRAL:
				return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES, action.pzxc.top, action.tablero[action.cursor-1].right)];
			//pregunto si me encuentro en la posicion inmediatamente a la izq de la posicion central
			case SolverFaster.ANTE_POSICION_CENTRAL:
				return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, action.pzxc.left,SolverFaster.MAX_COLORES, action.tablero[action.cursor-1].right)];
		}
		
		final int flag_m = SolverFaster.matrix_zonas[action.cursor];
		
		// estoy en interior de tablero?
		if (flag_m == SolverFaster.F_INTERIOR) 
			return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES, SolverFaster.MAX_COLORES, action.tablero[action.cursor-1].right)];
		// mayor a F_INTERIOR significa que estoy en borde
		else if (flag_m > SolverFaster.F_INTERIOR) {
			switch (flag_m) {
				//borde right
				case SolverFaster.F_BORDE_RIGHT:
					return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.GRIS, SolverFaster.MAX_COLORES, action.tablero[action.cursor-1].right)];
				//borde left
				case SolverFaster.F_BORDE_LEFT:
					return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES, SolverFaster.MAX_COLORES, SolverFaster.GRIS)];
				// borde top
				case SolverFaster.F_BORDE_TOP:
					return action.super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.MAX_COLORES, SolverFaster.MAX_COLORES, action.tablero[action.cursor-1].right)];
				//borde bottom
				default:
					return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES, SolverFaster.GRIS, action.tablero[action.cursor-1].right)];
			}
		}
		// menor a F_INTERIOR significa que estoy en esquina
		else {
			switch (flag_m) {
				//esquina top-left
				case SolverFaster.F_ESQ_TOP_LEFT:
					return action.super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.MAX_COLORES, SolverFaster.MAX_COLORES, SolverFaster.GRIS)];
				//esquina top-right
				case SolverFaster.F_ESQ_TOP_RIGHT:
					return action.super_matriz[MapaKeys.getKey(SolverFaster.GRIS, SolverFaster.GRIS, SolverFaster.MAX_COLORES, action.tablero[action.cursor-1].right)];
				//esquina bottom-left
				case SolverFaster.F_ESQ_BOTTOM_LEFT: 
					return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.MAX_COLORES, SolverFaster.GRIS, SolverFaster.GRIS)];
					//esquina bottom-right
				default:
					return action.super_matriz[MapaKeys.getKey(action.tablero[action.cursor-SolverFaster.LADO].bottom, SolverFaster.GRIS, SolverFaster.GRIS, action.tablero[action.cursor-1].right)];
			}
		}
	}

	/**
	 * Usado para obtener los indices de los contornos que voy a setear como usados o como libres.
	 * NOTA: index_sup sirve para contorno superior e index_inf para contorno inferior.
	 * @return
	 */
	private final static void getIndexDeContornoYaPuesto(ExploracionAction action){
		//primero me fijo si estoy en posici�n v�lida
		if (SolverFaster.zona_proc_contorno[action.cursor] == false){
			action.index_sup = -1;
			//@CONTORNO_INFERIORindex_inf = -1;
			return;
		}
	
		//obtengo las claves de acceso
		switch (Contorno.MAX_COLS){
			case 2:
				action.index_sup = Contorno.getIndex(action.tablero[action.cursor-1].left, action.tablero[action.cursor-1].top, action.tablero[action.cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top);*/
				break;
			case 3:
				action.index_sup = Contorno.getIndex(action.tablero[action.cursor-2].left, action.tablero[action.cursor-2].top, action.tablero[action.cursor-1].top, action.tablero[action.cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top, tablero[cursor-2].top);*/
				break;
			case 4:
				action.index_sup = Contorno.getIndex(action.tablero[action.cursor-3].left, action.tablero[action.cursor-3].top, action.tablero[action.cursor-2].top, action.tablero[action.cursor-1].top, action.tablero[action.cursor].top);
				/*@CONTORNO_INFERIORif (cursor >= 33 && cursor <= 238)
					index_inf = Contorno.getIndex(tablero[cursor-LADO].right, tablero[cursor].top, tablero[cursor-1].top, tablero[cursor-2].top, tablero[cursor-3].top);*/
				break;
			default: break;
		}
	}

	/*@CONTORNO_INFERIOR
	public final static boolean esContornoInferiorUsado(){
		//primero me fijo si estoy en la posici�n correcta para preguntar por contorno inferior usado
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
	
		//si el contorno est� siendo usado entonces devuelvo true
		if (Contorno.contornos_used[auxi])
			return true;
	
		return false;
	}*/
	
	private final static void setContornoUsado(ExploracionAction action)
	{
		if (action.index_sup != -1)
			action.contorno.contornos_used[action.index_sup] = true;
		/*@CONTORNO_INFERIORif (index_inf != -1)
			action.contorno.contornos_used[index_inf] = true;*/
	}

	private final static void setContornoLibre(ExploracionAction action)
	{
		if (action.index_sup != -1)
			action.contorno.contornos_used[action.index_sup] = false;
		/*@CONTORNO_INFERIORif (index_inf != -1)
			action.contorno.contornos_used[index_inf] = false;*/
	}

	private final static boolean esContornoUsado(ExploracionAction action)
	{
		// primero me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (SolverFaster.zona_read_contorno[action.cursor] == false)
			return false;
		
		// obtengo la clave del contorno superior
		int i_count = action.cursor - SolverFaster.LADO;
		int auxi;
		switch (Contorno.MAX_COLS){
			case 2:
				auxi = Contorno.getIndex(action.tablero[action.cursor-1].right, action.tablero[i_count].bottom, action.tablero[i_count + 1].bottom);
				break;
			case 3:
				auxi = Contorno.getIndex(action.tablero[action.cursor-1].right, action.tablero[i_count].bottom, action.tablero[i_count + 1].bottom, action.tablero[i_count + 2].bottom);
				break;
			case 4:
				auxi = Contorno.getIndex(action.tablero[action.cursor-1].right, action.tablero[i_count].bottom, action.tablero[i_count + 1].bottom, action.tablero[i_count + 2].bottom, action.tablero[i_count + 3].bottom);
				break;
			default: return false;
		}
		
		// si el contorno está siendo usado entonces devuelvo true
		if (action.contorno.contornos_used[auxi])
			return true;
		
		return false;
	}

}