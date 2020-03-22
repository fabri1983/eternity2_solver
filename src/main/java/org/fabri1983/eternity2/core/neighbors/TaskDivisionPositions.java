package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.CommonFuncs;
import org.fabri1983.eternity2.core.Consts;

public class TaskDivisionPositions {
	
	public static final int SHIFT_FOR_DESDE = 4;
	public static final int MASK_FOR_LENGTH = 4;
	
	/**
	 * 12 is the max value of involved lengths. See the structures created below.
	 */
	private static final int MAX_LENGTH_INVOLVED = 12;
	
	public static byte[][][] calculate(int numProcs) {
		
		CommonFuncs.inicializarMatrixZonas();
		
		/**
		 * We flag the existing lengths.
		 * Only list sizes for interior neighbors. See NeighborsSizeByKey.
		 */
		boolean[] length_interiors = new boolean[MAX_LENGTH_INVOLVED + 1];
		length_interiors[1] = true;
		length_interiors[2] = true;
		length_interiors[3] = true;
		length_interiors[4] = true;
		length_interiors[5] = true;
		length_interiors[6] = true;
		length_interiors[7] = true;
		
		/**
		 * We flag the existing lengths.
		 * Only list sizes for borders left, right, and bottom neighbors. See NeighborsSizeByKey.
		 */
		boolean[] length_borders_left_right_bottom = new boolean[MAX_LENGTH_INVOLVED + 1];
		length_borders_left_right_bottom[1] = true;
		length_borders_left_right_bottom[2] = true;
		length_borders_left_right_bottom[3] = true;
		length_borders_left_right_bottom[4] = true;
		length_borders_left_right_bottom[10] = true;
		length_borders_left_right_bottom[11] = true;
		length_borders_left_right_bottom[12] = true;
		
		// all previously set lengths
		int[] all_target_lengths = new int[] { 1, 2, 3, 4, 5, 6, 7, 10, 11, 12 };
		
		int max2ndDimensionValue = getMaxCursorReached(numProcs) - Consts.POSICION_TASK_DIVISION + 1;
		byte[][][] positionsForNeighbors = new byte[numProcs][max2ndDimensionValue][MAX_LENGTH_INVOLVED + 1];
		
		// initialize 
		for (int id = 0; id < numProcs; ++id) {
			simulateForProc(id, numProcs, all_target_lengths, length_interiors, length_borders_left_right_bottom,
					positionsForNeighbors);
		}
		
		return positionsForNeighbors;
	}

	/**
	 * Use this method to get the correct index to later use in the 2nd dimension access.
	 * 
	 * @param cursor
	 * @param maxCursorReached
	 * @return
	 */
	public static boolean inRange(short cursor, short maxCursorReached) {
		// TODO MEJORAR USANDO BITWISE O COMO SE HACE EN SHADERS, PARA NO USAR IFs
		// if cursor is out of range [Consts.POSICION_TASK_DIVISION, Consts.POSICION_TASK_DIVISION + maxCursorREached]
		if (cursor < Consts.POSICION_TASK_DIVISION) return false;
		if (cursor > maxCursorReached) return false;
		return true;
	}
	
	private static void simulateForProc(int id, int numProcs, int[] all_target_lengths, boolean[] length_interiors,
			boolean[] length_borders_left_right_bottom, byte[][][] positionsForNeighbors) {
		
		int maxCursorReached = 0;
		
		// simulates length of neighbors for some board positions
		for (int lengthSimu : all_target_lengths) {
			
			boolean continueOnNextCursor = true;
			int num_processes = numProcs;
			
			// simulate placing tiles to generate task division scenarios
			for (int cursor = Consts.POSICION_TASK_DIVISION; continueOnNextCursor && cursor < Consts.MAX_PIEZAS; ++cursor) {
				
				// we are generating any tuple when cursor is in a fixed tile board position
				if (cursor == Consts.PIEZA_CENTRAL_POS_TABLERO) {
					continue;
				}
				
				switch (CommonFuncs.matrix_zonas[cursor] & Consts.MASK_F_TABLERO) {
					case Consts.F_INTERIOR: {
						// For interior position we have only certain neighbor lengths. 
						// If current length doesn't match any of them we can skip the simulation avoiding create useless tuples
						if (!length_interiors[lengthSimu]) {
							continue;
						}
						break; // exists the switch
					}
					case Consts.F_BORDE_RIGHT:
					case Consts.F_BORDE_LEFT:
					case Consts.F_BORDE_BOTTOM:
					case Consts.F_ESQ_BOTTOM_LEFT: {
						// For borders and corners position we have only certain neighbor lengths. 
						// If current length doesn't match any of them we can skip the simulation avoiding create useless tuples
						if (!length_borders_left_right_bottom[lengthSimu]) {
							continue;
						}
						break; // exists the switch
					}
				}
				
				// establezco los límites de los neighbors a explorar para esta id task y futuras divisiones
				num_processes = establecerLimites(id, num_processes, cursor, lengthSimu, positionsForNeighbors);
				
				// si num_processes sigue siendo el mismo quiere decir que la división no requiere una nueva división en siguiente cursor
				if (num_processes == numProcs) {
					continueOnNextCursor = false;
				}
				
				// keep track of the max cursor reached so far to use it later as pre calculated value for 2nd dimension. 
				if (cursor > maxCursorReached) {
					maxCursorReached = cursor;
				}
			}
		}
		
		System.out.println(numProcs + ": " + maxCursorReached);
	}

	private static int establecerLimites(int id, int num_processes, int cursor, int hastaSimu,
			byte[][][] positionsForNeighbors) {
		
		// NOTE: next conditions are such that they always set work to processes, even when the task division is odd.
		
		int hasta = hastaSimu;
		int desde;
		
		int thisProc = id % num_processes;
		
		// caso 1: cada proc toma una única rama de neighbors
		if (num_processes == hasta) {
			desde = thisProc;
			hasta = thisProc + 1;
		}
		// caso 2: existen mas piezas a explorar que procs, entonces se distribuyen las piezas.
		else if (num_processes < hasta) {
			int span = (hasta + 1) / num_processes;
			desde = thisProc * span;
			// considering cases when task division is odd:
			//  - normal task distribution while not being the last process: hasta = desde + span
			//  - when being the last process we need to cover all remaining tasks: hasta remains unchanged
			if (thisProc != (num_processes - 1)) // normal task distribution while not being the last process 
				hasta = thisProc * span + span;
		}
		// caso 3: existen mas procs que neighbors a explorar, entonces hay que distribuir los procs y
		// aumentar el pos_multi_process_offset en uno asi el siguiente nivel tmb continua la división.
		// Seteo num_processes = hasta, asi el siguiente nivel divide correctamente.
		else {
			int divisor = (num_processes + 1) / hasta; // reparte los procs por posible neighbor
			desde = thisProc / divisor;
			num_processes = hasta;
			if (desde < hasta)
				hasta = desde + 1;
			else
				desde = hasta - 1;
		}

		// cap desde and length_nbs since there is no sense if they are higher or equals than 12
		desde = Math.min(desde, MAX_LENGTH_INVOLVED);
		hasta = Math.min(hasta, MAX_LENGTH_INVOLVED);
		
		// Guardo desde y hasta
		byte positionsMerged = (byte) ((desde << SHIFT_FOR_DESDE) | hasta);
		positionsForNeighbors[id][cursor - Consts.POSICION_TASK_DIVISION][hastaSimu] = positionsMerged;
		
		return num_processes;
	}
	
	/**
	 * Pre calculated values for max cursor reached on simulations by number of total procs.
	 * 
	 * @param numProcs
	 * @return
	 */
	public static short getMaxCursorReached(int numProcs) {
		switch (numProcs) {
		case 8: return 111;
		default: return Consts.MAX_PIEZAS - 1; // by default the cursor reached till the end of the board
		}
	}
	
}
