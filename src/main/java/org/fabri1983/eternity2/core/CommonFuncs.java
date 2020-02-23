package org.fabri1983.eternity2.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.fabri1983.eternity2.core.neighbors.NeighborStrategy;
import org.fabri1983.eternity2.core.neighbors.NodoPosibles;
import org.fabri1983.eternity2.core.prune.color.ColorRightExploredStrategy;
import org.fabri1983.eternity2.core.resourcereader.ReaderForFile;

public class CommonFuncs {

	/**
	 * Zonas del tablero: las 4 esquinas, los 4 bordes, y la zona interior.
	 */
	public final static byte matrix_zonas[] = new byte[Consts.MAX_PIEZAS];
	/**
	 * Me dice en qué posiciones puedo procesar un contorno superior (e inferior) para 
	 * setearlo como usado o libre. Solamente sirve a dichos fines, y ningún otro.
	 */
	public final static boolean[] zona_proc_contorno = new boolean[Consts.MAX_PIEZAS];
	/**
	 * Me dice en qué posiciones ce tablero puedo leer un contorno para chequear si es usado o no.
	 */
	public final static boolean[] zona_read_contorno = new boolean[Consts.MAX_PIEZAS];
	
	public final static void inicializarMatrixZonas ()
	{		
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			matrix_zonas[k]= Consts.F_INTERIOR; //primero asumo que estoy en posicion interior
			//esquina top-left
			if (k == 0)
				matrix_zonas[k]= Consts.F_ESQ_TOP_LEFT;
			//esquina top-right
			else if (k == (Consts.LADO - 1))
				matrix_zonas[k]= Consts.F_ESQ_TOP_RIGHT;
			//esquina bottom-right
			else if (k == (Consts.MAX_PIEZAS - 1))
				matrix_zonas[k]= Consts.F_ESQ_BOTTOM_RIGHT;
			//esquina bottom-left
			else if (k == (Consts.MAX_PIEZAS - Consts.LADO))
				matrix_zonas[k]= Consts.F_ESQ_BOTTOM_LEFT;
			//borde top
			else if ((k > 0) && (k < (Consts.LADO - 1)))
				matrix_zonas[k]= Consts.F_BORDE_TOP;
			//borde right
			else if (((k+1) % Consts.LADO)==0){
				if ((k != (Consts.LADO - 1)) && (k != (Consts.MAX_PIEZAS - 1)))
					matrix_zonas[k]= Consts.F_BORDE_RIGHT;
			}
			//borde bottom
			else if ((k > (Consts.MAX_PIEZAS - Consts.LADO)) && (k < (Consts.MAX_PIEZAS - 1)))
				matrix_zonas[k]= Consts.F_BORDE_BOTTOM;
			//borde left
			else if ((k % Consts.LADO)==0){
				if ((k != 0) && (k != (Consts.MAX_PIEZAS - Consts.LADO)))
					matrix_zonas[k]= Consts.F_BORDE_LEFT;
			}
		}
	}
	
	public final static void inicializarZonaProcesoContornos()
	{
		// NOTA: para contorno inferior se debe chequear que cursor sea [33,238].
		
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté en borde left + (Contorno.MAX_COLS - 1)
			int fila_actual = k / Consts.LADO;
			if (((k - Contorno.MAX_COLS) / Consts.LADO) != fila_actual)
				continue;
			
			zona_proc_contorno[k] = true;
		}
	}
	
	public final static void inicializarZonaReadContornos()
	{	
		for (int k=0; k < Consts.MAX_PIEZAS; ++k)
		{
			//si estoy en borde top o bottom continuo con la siguiente posición
			if (k < Consts.LADO || k > (Consts.MAX_PIEZAS - Consts.LADO))
				continue;
			//si estoy en los bordes entonces continuo con la sig posición
			if ( (((k+1) % Consts.LADO)==0) || ((k % Consts.LADO)==0) )
				continue;
			
			//desde aqui estoy en el interior del tablero
			
			//me aseguro que no esté dentro de (Contorno.MAX_COLS - 1) posiciones antes de border right
			int fila_actual = k / Consts.LADO;
			if ((k + (Contorno.MAX_COLS-1)) < ((fila_actual * Consts.LADO) + (Consts.LADO - 1)))
				zona_read_contorno[k] = true;
		}
	}
	
	/**
	 * En este metodo se setean las piezas que son fijas al juego. Por ahora solo existe
	 * una sola pieza fija y es la pieza numero 139 en las posicion 136 real (135 para el
	 * algoritmo porque es 0-based)
	 */
	public final static void ponerPiezasFijasEnTablero(int processId, Pieza[] piezas, Pieza[] tablero) {
		
		Pieza piezaCentral = piezas[Consts.NUM_P_CENTRAL];
		piezaCentral.usada= true;
		tablero[Consts.POSICION_CENTRAL]= piezaCentral; // same value than INDICE_P_CENTRAL
		
		System.out.println(processId + " >>> Pieza Fija en posicion " + (Consts.POSICION_CENTRAL + 1) + " cargada en tablero");
	}
	
	/**
	 * Carga las piezas desde el archivo NAME_FILE_PIEZAS
	 */
	public final static void cargarPiezas(int processId, Pieza[] piezas, ReaderForFile readerForTilesFile) {
		
		BufferedReader reader = null;
		
		try{
			reader = readerForTilesFile.getReader(Consts.NAME_FILE_PIEZAS);
			String linea= reader.readLine();
			short num=0;
			
			while (linea != null){
				
				if (num >= Consts.MAX_PIEZAS)
					throw new Exception("El numero que ingresaste como num de piezas por lado (" + Consts.LADO + ") es distinto del que contiene el archivo");
				
				piezas[num]= PiezaFactory.from(linea, num);
                //PiezaFactory.setFromStringWithNum(linea, num, action.piezas[num]);
				linea= reader.readLine();
				++num;
			}

			if (num != Consts.MAX_PIEZAS)
				throw new Exception("El numero que ingresaste como num de piezas por lado (" + Consts.LADO + ") es distinto del que contiene el archivo");
		}
		catch (Exception exc){
			throw new RuntimeException(processId + " >>> ERROR. " + exc.getMessage());
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
	 * Verifica que no exista pieza extraña o que falte alguna pieza. 
	 * Solo se usa al cargar las piezas desde archivo o al cargar estado.
	 */
	public final static void verificarTiposDePieza(int processId, Pieza[] piezas) {
		
		int n_esq= 0;
		int n_bordes= 0;
		int n_interiores= 0;
	
		for (int g=0; g < Consts.MAX_PIEZAS; ++g)
		{
			Pieza pzx = piezas[g];
			if (Pieza.isInterior(pzx))
				++n_interiores;
			else if (Pieza.isBorder(pzx))
				++n_bordes;
			else if (Pieza.isCorner(pzx))
				++n_esq;
		}
		
		if ((n_esq != 4) || (n_bordes != (4*(Consts.LADO-2))) || (n_interiores != (Consts.MAX_PIEZAS - (n_esq + n_bordes)))) {
			throw new RuntimeException(processId + " >>> ERROR. Existe una o varias piezas incorrectas.");
		}
	}
	
	/**
	 * Carga cada entrada de la matriz con los indices de las piezas que 
	 * tienen tales colores en ese orden.
	 */
	public final static void cargarSuperEstructura(int processId, Pieza[] piezas, boolean useFairExperimentGif, NeighborStrategy neighborStrategy)
	{
		long startingTime = System.nanoTime();
		
		llenarSuperEstructura(piezas, useFairExperimentGif, neighborStrategy);
		
		long elapsedMicros = TimeUnit.MICROSECONDS.convert(System.nanoTime() - startingTime, TimeUnit.NANOSECONDS);
		System.out.println(processId + " >>> carga de super matriz finalizada (" + elapsedMicros + " micros)");
	}
	
	/**
	 * Para cada posible combinacion entre los colores de las secciones top, 
	 * right, bottom y left creo un vector que contendrá las piezas que tengan
	 * esa combinacion de colores en dichas secciones y ademas guardo en qué
	 * estado de rotacion la cumplen.
	 */
	private static final void llenarSuperEstructura (Pieza[] piezas, boolean useFairExperimentGif, NeighborStrategy neighborStrategy)
	{
		// itero sobre el arreglo de piezas
		for (short k = 0; k < Consts.MAX_PIEZAS; ++k) {
			
			if (k == Consts.NUM_P_CENTRAL)
				continue;
			
			Pieza pz = piezas[k];
			
			//guardo la rotación de la pieza
			byte temp_rot = pz.rotacion;
			//seteo su rotación en 0. Esto es para generar la matriz siempre en el mismo orden
			Pieza.llevarArotacion(pz, (byte)0);
			
			for (byte rot=0; rot < Consts.MAX_ESTADOS_ROTACION; ++rot, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (useFairExperimentGif && (pz.top == pz.bottom))
					continue;
				
				//este caso es cuando tengo los 4 colores
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, pz.bottom, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, pz.right, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, pz.bottom, pz.left, pz), k, rot);
				
				//tengo tres colores y uno faltante
				if (neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, pz.bottom, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(Consts.MAX_COLORES, pz.right, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, pz.bottom, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, pz.bottom, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, Consts.MAX_COLORES, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, pz.bottom, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, Consts.MAX_COLORES, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, pz.right, Consts.MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, Consts.MAX_COLORES, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top ,pz.right, pz.bottom, Consts.MAX_COLORES, pz) == null)
					neighborStrategy.setNewNodoP(pz.top ,pz.right, pz.bottom, Consts.MAX_COLORES, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top ,pz.right, pz.bottom, Consts.MAX_COLORES, pz), k, rot);
				
				//tengo dos colores y dos faltantes
				if (neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, Consts.MAX_COLORES, pz.bottom, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(Consts.MAX_COLORES, Consts.MAX_COLORES, pz.bottom, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, Consts.MAX_COLORES, pz.bottom, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, Consts.MAX_COLORES, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(Consts.MAX_COLORES, pz.right, Consts.MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, Consts.MAX_COLORES, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, pz.bottom, Consts.MAX_COLORES, pz) == null)
					neighborStrategy.setNewNodoP(Consts.MAX_COLORES, pz.right, pz.bottom, Consts.MAX_COLORES, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(Consts.MAX_COLORES, pz.right, pz.bottom, Consts.MAX_COLORES, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, Consts.MAX_COLORES, pz.left, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, Consts.MAX_COLORES, Consts.MAX_COLORES, pz.left, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, Consts.MAX_COLORES, pz.left, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, pz.bottom, Consts.MAX_COLORES, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, Consts.MAX_COLORES, pz.bottom, Consts.MAX_COLORES, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, Consts.MAX_COLORES, pz.bottom, Consts.MAX_COLORES, pz), k, rot);
				
				if (neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, Consts.MAX_COLORES, Consts.MAX_COLORES, pz) == null)
					neighborStrategy.setNewNodoP(pz.top, pz.right, Consts.MAX_COLORES, Consts.MAX_COLORES, pz);
				NodoPosibles.addReferencia(neighborStrategy.getNodoFromOriginalKey(pz.top, pz.right, Consts.MAX_COLORES, Consts.MAX_COLORES, pz), k, rot);

				//tengo un color y tres faltantes
				//(esta combinación no se usa en el juego)
			}
			
			//restauro la rotación
			Pieza.llevarArotacion(pz, temp_rot);
		}
	}

	/**
	 * Dada la posicion de cursor se fija cuáles colores tiene alrededor y devuelve una referencia de NodoPosibles 
	 * que contiene las piezas que cumplan con los colores en el orden top-right-bottom-left (sentido horario).
	 *  
	 * NOTA: saqué muchas sentencias porque solamente voy a tener una pieza fija (135 en tablero), por eso 
	 * este metodo solo contempla las piezas top y left, salvo en el vecindario de la pieza fija.
	 */
	public final static NodoPosibles obtenerPosiblesPiezas (int cursor, Pieza[] tablero, NeighborStrategy neighborStrategy)
	{
		switch (cursor) {
			// estoy en la posicion inmediatamente arriba de la posicion central
			case Consts.SOBRE_POSICION_CENTRAL:
				return neighborStrategy.getNodoIfKeyIsOriginal_interior(
						tablero[cursor - Consts.LADO].bottom, Consts.MAX_COLORES, Consts.PIEZA_CENTRAL_COLOR_TOP, tablero[cursor - 1].right);
			// estoy en la posicion inmediatamente a la izq de la posicion central
			case Consts.ANTE_POSICION_CENTRAL:
				return neighborStrategy.getNodoIfKeyIsOriginal_interior(
						tablero[cursor - Consts.LADO].bottom, Consts.PIEZA_CENTRAL_COLOR_LEFT, Consts.MAX_COLORES, tablero[cursor - 1].right);
		}
		
		switch (matrix_zonas[cursor]) {
			// interior de tablero
			case Consts.F_INTERIOR: 
				return neighborStrategy.getNodoIfKeyIsOriginal_interior(
						tablero[cursor - Consts.LADO].bottom, Consts.MAX_COLORES, Consts.MAX_COLORES, tablero[cursor - 1].right);
	
			// borde right
			case Consts.F_BORDE_RIGHT:
				return neighborStrategy.getNodoIfKeyIsOriginal_border(
						tablero[cursor - Consts.LADO].bottom, PiezaFactory.GRIS, Consts.MAX_COLORES, tablero[cursor - 1].right);
			// borde left
			case Consts.F_BORDE_LEFT:
				return neighborStrategy.getNodoIfKeyIsOriginal_border(
						tablero[cursor - Consts.LADO].bottom, Consts.MAX_COLORES, Consts.MAX_COLORES, PiezaFactory.GRIS);
			// borde top
			case Consts.F_BORDE_TOP:
				return neighborStrategy.getNodoIfKeyIsOriginal_border(PiezaFactory.GRIS, Consts.MAX_COLORES, Consts.MAX_COLORES, tablero[cursor - 1].right);
			// borde bottom
			case Consts.F_BORDE_BOTTOM:
				return neighborStrategy.getNodoIfKeyIsOriginal_border(
						tablero[cursor - Consts.LADO].bottom, Consts.MAX_COLORES, PiezaFactory.GRIS, tablero[cursor - 1].right);
		
			// esquina top-left
			case Consts.F_ESQ_TOP_LEFT:
				return neighborStrategy.getNodoIfKeyIsOriginal_corner(
						PiezaFactory.GRIS, Consts.MAX_COLORES, Consts.MAX_COLORES, PiezaFactory.GRIS);
			// esquina top-right
			case Consts.F_ESQ_TOP_RIGHT:
				return neighborStrategy.getNodoIfKeyIsOriginal_corner(
						PiezaFactory.GRIS, PiezaFactory.GRIS, Consts.MAX_COLORES, tablero[cursor - 1].right);
			// esquina bottom-left
			case Consts.F_ESQ_BOTTOM_LEFT: 
				return neighborStrategy.getNodoIfKeyIsOriginal_corner(
						tablero[cursor - Consts.LADO].bottom, Consts.MAX_COLORES, PiezaFactory.GRIS, PiezaFactory.GRIS);
			// esquina bottom-right
			case Consts.F_ESQ_BOTTOM_RIGHT:
				return neighborStrategy.getNodoIfKeyIsOriginal_corner(
						tablero[cursor - Consts.LADO].bottom, PiezaFactory.GRIS, PiezaFactory.GRIS, tablero[cursor - 1].right);
		}
		
		return null;
	}

	/**
	 * Me guarda en el archivo NAME_FILE_LIBRES_MAX las numeros de las piezas que quedaron libres.
	 */
	public final static void guardarLibres(int processId, Pieza[] piezas, String libresMaxFileName)
	{
		try{
			PrintWriter wLibres= new PrintWriter(new BufferedWriter(new FileWriter(libresMaxFileName)));
			StringBuilder wLibresBuffer= new StringBuilder(256 * 13);
			
			for (int b=0; b < Consts.MAX_PIEZAS; ++b) {
				Pieza pzx= piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(pzx.numero + 1).append("\n");
			}
			
			for (int b=0; b < Consts.MAX_PIEZAS; ++b) {
				Pieza pzx= piezas[b];
				if (pzx.usada == false)
					wLibresBuffer.append(PiezaStringer.toStringColores(pzx)).append("\n");
			}
			
			String sContent = wLibresBuffer.toString();
			wLibres.append(sContent);
			wLibres.flush();
			wLibres.close();
		}
		catch (Exception escp) {
			System.out.println(processId + " >>> ERROR: No se pudo generar el archivo " + libresMaxFileName + ". " + escp.getMessage());
		}
	}

	/**
	 * En el archivo NAME_FILE_SOLUCION se guardan los colores de cada pieza.
	 * En el archivo NAME_FILE_DISPOSICION se guarda el numero y rotacion de cada pieza.
	 */
	public final static void guardarSolucion (int processId, Pieza[] tablero, String solucFileName, String dispFileName)
	{
		try{
			PrintWriter wSol= new PrintWriter(new BufferedWriter(new FileWriter(solucFileName, true)));
			PrintWriter wDisp= new PrintWriter(new BufferedWriter(new FileWriter(dispFileName, true)));
			StringBuilder contenidoDisp= new StringBuilder(256 * 13);
			
			wSol.println("Solucion para " + Consts.MAX_PIEZAS + " piezas");
			wDisp.println("Disposicion para " + Consts.MAX_PIEZAS + " piezas.");
			contenidoDisp.append("Disposicion para " + Consts.MAX_PIEZAS + " piezas.\n");
			wDisp.println("(num pieza) (estado rotacion) (posicion en tablero real)");
			contenidoDisp.append("(num pieza) (estado rotacion) (posicion en tablero real)\n");
			
			for (int b=0; b < Consts.MAX_PIEZAS; ++b)
			{
				Pieza p= tablero[b];
				int pos= b+1;
				wSol.println(p.top + Consts.SECCIONES_SEPARATOR_EN_FILE + p.right + Consts.SECCIONES_SEPARATOR_EN_FILE + p.bottom + Consts.SECCIONES_SEPARATOR_EN_FILE + p.left);
				wDisp.println((p.numero + 1) + Consts.SECCIONES_SEPARATOR_EN_FILE + p.rotacion + Consts.SECCIONES_SEPARATOR_EN_FILE + pos);
				contenidoDisp.append(p.numero + 1).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(p.rotacion).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
			System.out.println(processId + " >>> ERROR: No se pudo guardar la solucion!! QUE MACANA!!! (guardarSolucion())");
			System.out.println(ex);
		}
	}
	
	/**
	 * Guarda las estructuras necesaria del algoritmo para poder continuar desde el actual estado de exploración.
	 */
	public final static void guardarEstado (String statusFileName, int processId, Pieza[] piezas, Pieza[] tablero, 
			int cursor, int mas_bajo, int mas_alto, int mas_lejano_parcial_max, short[] desde_saved, 
			NeighborStrategy neighborStrategy, ColorRightExploredStrategy colorRightExploredStrategy) {
		
		try{
			PrintWriter writer= new PrintWriter(new BufferedWriter(new FileWriter(statusFileName)));
			StringBuilder writerBuffer= new StringBuilder(256 * 13);
	
			//guardo el valor de mas_bajo
			writerBuffer.append(mas_bajo).append("\n");
			
			//guardo el valor de mas_alto
			writerBuffer.append(mas_alto).append("\n");
			
			//guardo el valor de mas_lejano 
			writerBuffer.append(mas_lejano_parcial_max).append("\n");
			
			//guardo el valor del cursor
			writerBuffer.append(cursor).append("\n");
			
			//guardo los indices de piezas de tablero[]
			for (int n=0; n < Consts.MAX_PIEZAS; ++n) {
				if (n==(Consts.MAX_PIEZAS - 1)) {
					if (tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append(tablero[n].numero).append("\n");
				}
				else {
					if (tablero[n] == null)
						writerBuffer.append("-1").append("\n");
					else
						writerBuffer.append(tablero[n].numero).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//########################################################################
			/**
			 * Calculo los valores para desde_saved[]
			 */
			//########################################################################
			int _cursor = 0;
			for (; _cursor < cursor; ++_cursor) {
				
				if (_cursor == Consts.POSICION_CENTRAL) //para la pieza central no se tiene en cuenta su valor desde_saved[] 
					continue;
				//tengo el valor para desde_saved[]
				desde_saved[_cursor] = NodoPosibles.getUbicPieza(
						obtenerPosiblesPiezas(_cursor, tablero, neighborStrategy), 
						tablero[_cursor].numero);
			}
			//ahora todo lo que está despues de cursor tiene que valer cero
			for (;_cursor < Consts.MAX_PIEZAS; ++_cursor)
				desde_saved[_cursor] = 0;
			//########################################################################
			
			//guardo las posiciones de posibles piezas (desde_saved[]) de cada nivel del backtracking
			for (int n=0; n < Consts.MAX_PIEZAS; ++n) {
				if (n==(Consts.MAX_PIEZAS-1))
					writerBuffer.append(desde_saved[n]).append("\n");
				else
					writerBuffer.append(desde_saved[n]).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
			}
			
			//indico si se utiliza poda de color explorado o no
			if (colorRightExploredStrategy != null)
				writerBuffer.append(Boolean.TRUE).append("\n");
			else
				writerBuffer.append(Boolean.FALSE).append("\n");
			
			//guardo el contenido de arr_color_rigth_explorado
			if (colorRightExploredStrategy != null)
			{
				for (int n=0; n < Consts.LADO; ++n) {
					if (n==(Consts.LADO-1))
						writerBuffer.append(colorRightExploredStrategy.get(n)).append("\n");
					else
						writerBuffer.append(colorRightExploredStrategy.get(n)).append(Consts.SECCIONES_SEPARATOR_EN_FILE);
				}
			}
			
			//guardo el estado de rotación y el valor de usada de cada pieza
			for (int n=0; n < Consts.MAX_PIEZAS; ++n)
			{
				writerBuffer.append(piezas[n].rotacion).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(String.valueOf(piezas[n].usada)).append("\n");
			}
			
			String sContent = writerBuffer.toString();
			writer.append(sContent);
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			System.out.println(processId + " >>> ERROR: No se pudo guardar el estado de la exploración.");
			System.out.println(e);
		}
	}

	/**
	 * La exploracion ha alcanzado su punto limite, ahora es necesario guardar estado
	 */
	public final static void operarSituacionLimiteAlcanzado(String statusFileName, int processId, Pieza[] piezas, Pieza[] tablero, 
			int cursor, int mas_bajo, int mas_alto, int mas_lejano_parcial_max, short[] desde_saved, 
			NeighborStrategy neighborStrategy, ColorRightExploredStrategy colorRightExploredStrategy) {
			
		guardarEstado(statusFileName, processId, piezas, tablero, cursor, mas_bajo, mas_alto, mas_lejano_parcial_max, desde_saved, 
				neighborStrategy, colorRightExploredStrategy);
		
		System.out.println(processId + " >>> ha llegado a su limite de exploracion. Exploracion finalizada forzosamente.");
	}
	
	/**
	 * Genera un archivo de piezas para leer con el editor visual e2editor.exe, otro archivo
	 * que contiene las disposiciones de cada pieza en el tablero, y otro archivo que me dice
	 * las piezas no usadas (generado solo si max es true).
	 * Si max es true, el archivo generado es el que tiene la mayor disposición de piezas encontrada.
	 * Si max es false, el archivo generado contiene la disposición de piezas en el instante cuando
	 * se guarda estado.
	 */
	public final static int guardarResultadoParcial(boolean max, int processId, Pieza[] piezas, Pieza[] tablero,
			int sig_parcial, int maxNumParcial, String parcialFileName, String parcialMaxFileName,
			String disposicionMaxFileName, String libresMaxFileName)
	{
		if (maxNumParcial == 0 && !max)
			return sig_parcial;
		
		try {
			PrintWriter wParcial= null;
			// si estamos en max instance tenemos q guardar las disposiciones de las piezas
			PrintWriter wDispMax = null;
			StringBuilder parcialBuffer= new StringBuilder(256 * 13);
			StringBuilder dispMaxBuff= new StringBuilder(256 * 13);
			
			if (max){
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialMaxFileName)));
				wDispMax= new PrintWriter(new BufferedWriter(new FileWriter(disposicionMaxFileName)));
				dispMaxBuff.append("(num pieza) (estado rotacion) (posicion en tablero real)").append("\n");
			}
			else{
				String parcialFName = parcialFileName.substring(0, parcialFileName.indexOf(Consts.FILE_EXT)) + "_" + sig_parcial + Consts.FILE_EXT;
				wParcial= new PrintWriter(new BufferedWriter(new FileWriter(parcialFName)));
				++sig_parcial;
				if (sig_parcial > maxNumParcial)
					sig_parcial= 1;
			}
			
			for (int b=0; b < Consts.MAX_PIEZAS; ++b) {
				int pos= b+1;
				Pieza p = tablero[b];
				if (p == null){
					parcialBuffer.append(PiezaFactory.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(PiezaFactory.GRIS).append("\n");
					if (max)
						dispMaxBuff.append("-").append(Consts.SECCIONES_SEPARATOR_EN_FILE).append("-").append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
				}
				else {
					parcialBuffer.append(p.top).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(p.right).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(p.bottom).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(p.left).append("\n");
					if (max)
						dispMaxBuff.append(p.numero + 1).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(p.rotacion).append(Consts.SECCIONES_SEPARATOR_EN_FILE).append(pos).append("\n");
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
				guardarLibres(processId, piezas, libresMaxFileName);
			
			return sig_parcial;
		}
		catch(Exception ex) {
			System.out.println(processId + " >>> ERROR: No se pudieron generar los archivos de resultado parcial. " + ex.getMessage());
			return sig_parcial;
		}
	}

	public final static boolean testPodaColorRightExplorado(int cursor, Pieza p, 
			ColorRightExploredStrategy colorRightExploredStrategy) {
		
		final int fila_actual = cursor >>> Consts.LADO_SHIFT_AS_DIVISION; // if divisor is power of 2 then we can use >>
		
		// For modulo try this for better performance only if divisor is power of 2 and dividend is positive: dividend & (divisor - 1)
		// old was: ((cursor+2) % LADO) == 0
		final boolean flag_antes_borde_right = ((cursor + 2) & (Consts.LADO - 1)) == 0;
		
		// si estoy antes del borde right limpio el arreglo de colores right usados
		if (flag_antes_borde_right)
			colorRightExploredStrategy.compareAndSet(fila_actual + 1, 0);
		
		if (matrix_zonas[cursor] == Consts.F_BORDE_LEFT)
		{
			final int mask = 1 << p.right;
			
			// pregunto si el color right de la pieza de borde left actual ya está explorado
			int color = colorRightExploredStrategy.get(fila_actual);
			if ((color & mask) != 0) {
				p.usada = false; //la pieza ahora no es usada
				return true; // sigo con otra pieza de borde
			}
			// si no es así entonces lo seteo como explorado
			else {
				// asignación en una sola operación, ya que el bit en p.right vale 0 (según la condición anterior)
				colorRightExploredStrategy.compareAndSet(fila_actual, color | mask);
				// int value = SolverFaster.arr_color_rigth_explorado.get(fila_actual) | 1 << p.right;
				// SolverFaster.arr_color_rigth_explorado.compareAndSet(fila_actual, value);
			}
		}
		
		return false;
	}

	public final static boolean testFairExperimentGif(int cursor, Pieza p, Pieza[] tablero) {
		
		byte flag_zona = matrix_zonas[cursor];
		
		if (flag_zona == Consts.F_INTERIOR || flag_zona == Consts.F_BORDE_TOP) {
			if (p.bottom == tablero[cursor-1].bottom){
				p.usada = false;
				return true;
			}
		}
		
		return false;
	}

	public final static void setContornoUsado(int cursor, Contorno contorno, Pieza[] tablero)
	{
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_proc_contorno[cursor] == true) {
			contorno.contornos_used[tablero[cursor-1].left][tablero[cursor-1].top][tablero[cursor].top] = true;
		}
	}
	
	public final static void setContornoLibre(int cursor, Contorno contorno, Pieza[] tablero)
	{
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_proc_contorno[cursor] == true) {
			contorno.contornos_used[tablero[cursor-1].left][tablero[cursor-1].top][tablero[cursor].top] = false;
		}
	}

	public final static boolean esContornoSuperiorUsado(int cursor, Contorno contorno, Pieza[] tablero)
	{
		// me fijo si estoy en la posición correcta para preguntar por contorno usado
		if (zona_read_contorno[cursor] == true) {
			return contorno.contornos_used[tablero[cursor-1].right][tablero[cursor - Consts.LADO].bottom][tablero[cursor - Consts.LADO + 1].bottom];
		}
		return false;
	}
	
}
