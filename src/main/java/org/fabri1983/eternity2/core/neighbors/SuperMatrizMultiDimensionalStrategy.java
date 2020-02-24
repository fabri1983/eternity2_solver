package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.Pieza;

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
 */
public class SuperMatrizMultiDimensionalStrategy implements NeighborStrategy {

    public final static NodoPosibles[][][][] super_matriz = new NodoPosibles
            [Consts.MAX_COLORES+1][Consts.MAX_COLORES+1][Consts.MAX_COLORES+1][Consts.MAX_COLORES+1];
	
	@Override
	public NodoPosibles getNodoFromOriginalKey(byte top, byte right, byte bottom, byte left, Pieza p) {
		return super_matriz[top][right][bottom][left];
	}

	@Override
	public void setNewNodoP(byte top, byte right, byte bottom, byte left, Pieza p) {
		int key = NodoPosibles.getKey(top, right, bottom, left);
		NodoPosibles nodoPosibles = NodoPosibles.newForKey(key);
		super_matriz[top][right][bottom][left] = nodoPosibles;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte right, byte bottom, byte left) {
		return super_matriz[top][right][bottom][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border(byte top, byte right, byte bottom, byte left) {
		return super_matriz[top][right][bottom][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner(byte top, byte right, byte bottom, byte left) {
		return super_matriz[top][right][bottom][left];
	}

	@Override
	public boolean isPiezaCorrectType(byte flagZona, Pieza p) {
		switch (flagZona) {
		case Consts.F_INTERIOR: return Pieza.isInterior(p);
		case Consts.F_BORDE_RIGHT:
		case Consts.F_BORDE_LEFT:
		case Consts.F_BORDE_TOP:
		case Consts.F_BORDE_BOTTOM: return Pieza.isBorder(p);
		case Consts.F_ESQ_TOP_LEFT:
		case Consts.F_ESQ_TOP_RIGHT:
		case Consts.F_ESQ_BOTTOM_LEFT:
		case Consts.F_ESQ_BOTTOM_RIGHT: return Pieza.isCorner(p);
		default: return false;
		}
	}
	
	@Override
	public void resetForBenchmark() {
		for (int i = 0; i < super_matriz.length; ++i) {
			for (int j = 0; j < super_matriz.length; ++j) {
				for (int k = 0; k < super_matriz.length; ++k) {
					for (int m = 0; m < super_matriz.length; ++m) {
						if (super_matriz[i][j][k][m] != null)
							NodoPosibles.resetReferencias(super_matriz[i][j][k][m]);
					}
				}
			}
		}
	}
    
	@Override
	public void printMergedInfoSizes(boolean skipSizeOne) {
		for (byte i = 0; i < super_matriz.length; ++i) {
			for (byte j = 0; j < super_matriz.length; ++j) {
				for (byte k = 0; k < super_matriz.length; ++k) {
					for (byte m = 0; m < super_matriz.length; ++m) {
						NodoPosibles nodo = super_matriz[i][j][k][m];
						if (nodo != null) {
							int size = 0;
							for (short info : nodo.mergedInfo) {
								if (info != -1)
									++size;
							}
							if (skipSizeOne == false || (skipSizeOne == true && size > 1)) {
								System.out.println("case " + NodoPosibles.getKey(i, j, k, m) + ": return " + size + ";");
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void printKeys() {
		for (byte i = 0; i < super_matriz.length; ++i) {
			for (byte j = 0; j < super_matriz.length; ++j) {
				for (byte k = 0; k < super_matriz.length; ++k) {
					for (byte m = 0; m < super_matriz.length; ++m) {
						NodoPosibles nodo = super_matriz[i][j][k][m];
						if (nodo != null) {
							System.out.println(NodoPosibles.getKey(i, j, k, m));
						}
					}
				}
			}
		}
	}
	
}
