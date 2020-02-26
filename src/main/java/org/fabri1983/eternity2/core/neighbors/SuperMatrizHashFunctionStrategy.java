package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Pieza;

/**
 * This strategy is an improvement over {@link SuperMatrizMultiDimensionalStrategy} with much less memory but slower access.<br/>
 * It uses a pre calculated Perfect Hash Function with an array of size PerfectHashFunction2.PHASHRANGE.
 */
public class SuperMatrizHashFunctionStrategy implements NeighborStrategy {
	
	@Override
	public void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p, short piezaIndex, byte rot) {
		// TODO Auto-generated method stub
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior_above_central(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior_left_central(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_right(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_left(byte top) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_top(byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_bottom(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_top_left() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_top_right(byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_left(byte top) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_right(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetForBenchmark() {
	}

	@Override
	public void printMergedInfoSizes(boolean skipSizeOne) {
	}

	@Override
	public void printKeys() {
	}

}
