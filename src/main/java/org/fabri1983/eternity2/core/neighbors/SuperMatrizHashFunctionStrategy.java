package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.bitset.QuickLongBitSet;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Border;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Corner;
import org.fabri1983.eternity2.core.mph.PerfectHashFunction2Interior;

/**
 * This strategy is an improvement over {@link SuperMatrizMultipleDimensionalStrategy} with much less memory but slower access. <br/>
 * It uses a pre calculated Perfect Hash Function with an array of size PerfectHashFunction2.PHASHRANGE.
 */
public class SuperMatrizHashFunctionStrategy implements NeighborStrategy {

	private final static NodoPosibles[] super_matriz_interior = new NodoPosibles[PerfectHashFunction2Interior.PHASHRANGE];
	private final static NodoPosibles[] super_matriz_border = new NodoPosibles[PerfectHashFunction2Border.PHASHRANGE];
	private final static NodoPosibles[] super_matriz_corner = new NodoPosibles[PerfectHashFunction2Corner.PHASHRANGE];
	private final static QuickLongBitSet bitset = new QuickLongBitSet(777942 + 1); // value of biggest key + 1
	
	@Override
	public NodoPosibles getNodoFromOriginalKey(byte top, byte right, byte bottom, byte left, Pieza p) {
		
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

		// just in case
		return null;
	}
	
	@Override
	public void setNewNodoP(byte top, byte right, byte bottom, byte left, Pieza p)
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
	
	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte right, byte bottom, byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);

		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;

		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_INTERIOR;
		return super_matriz_interior[PerfectHashFunction2Interior.phash(keyDiff)];
	}
	
	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border(byte top, byte right, byte bottom, byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);

		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;

		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_BORDER;
		return super_matriz_border[PerfectHashFunction2Border.phash(keyDiff)];
	}
	
	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner(byte top, byte right, byte bottom, byte left)
	{
		int key = NodoPosibles.getKey(top, right, bottom, left);

		// check if key belongs to original keys set
		if (!bitset.get(key))
			return null;

		int keyDiff = key - NodoPosibles.KEY_SUBTRACT_CORNER;
		return super_matriz_corner[PerfectHashFunction2Corner.phash(keyDiff)];
	}

	@Override
	public void resetForBenchmark() {
		for (int i = 0; i < super_matriz_interior.length; ++i) {
			if (super_matriz_interior[i] != null)
				NodoPosibles.resetReferencias(super_matriz_interior[i]);
		}

		for (int i = 0; i < super_matriz_border.length; ++i) {
			if (super_matriz_border[i] != null)
				NodoPosibles.resetReferencias(super_matriz_border[i]);
		}

		for (int i = 0; i < super_matriz_corner.length; ++i) {
			if (super_matriz_corner[i] != null)
				NodoPosibles.resetReferencias(super_matriz_corner[i]);
		}
	}

	@Override
	public void printMergedInfoSizes(boolean skipSizeOne) {
		// TODO Auto-generated method stub
	}

	@Override
	public void printKeys() {
		// TODO Auto-generated method stub
	}

}
