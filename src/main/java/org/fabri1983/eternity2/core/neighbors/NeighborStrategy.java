package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Pieza;

public interface NeighborStrategy {
	
	NodoPosibles getNodoFromOriginalKey(byte top, byte right, byte bottom, byte left, Pieza p);
	
	void setNewNodoP(byte top, byte right, byte bottom, byte left, Pieza p);

	NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte right, byte bottom, byte left);

	NodoPosibles getNodoIfKeyIsOriginal_border(byte top, byte right, byte bottom, byte left);

	NodoPosibles getNodoIfKeyIsOriginal_corner(byte top, byte right, byte bottom, byte left);

	void resetForBenchmark();

	void printMergedInfoSizes(boolean skipSizeOne);

	void printKeys();

}
