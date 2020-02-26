package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Pieza;

public interface NeighborStrategy {
	
	void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p, short piezaIndex, byte rot);

	NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte left);
	
	NodoPosibles getNodoIfKeyIsOriginal_interior_above_central(byte top, byte left);

	NodoPosibles getNodoIfKeyIsOriginal_interior_left_central(byte top, byte left);
	
	NodoPosibles getNodoIfKeyIsOriginal_border_right(byte top, byte left);
	
	NodoPosibles getNodoIfKeyIsOriginal_border_left(byte top);
	
	NodoPosibles getNodoIfKeyIsOriginal_border_top(byte left);
	
	NodoPosibles getNodoIfKeyIsOriginal_border_bottom(byte top, byte left);

	NodoPosibles getNodoIfKeyIsOriginal_corner_top_left();
	
	NodoPosibles getNodoIfKeyIsOriginal_corner_top_right(byte left);
	
	NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_left(byte top);
	
	NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_right(byte top, byte left);

	void resetForBenchmark();

	void printMergedInfoSizes(boolean skipSizeOne);

	void printKeys();

}
