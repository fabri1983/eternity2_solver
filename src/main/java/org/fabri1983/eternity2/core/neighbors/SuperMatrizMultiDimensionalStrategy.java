package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.Pieza;

public class SuperMatrizMultiDimensionalStrategy implements NeighborStrategy {

	public final static NodoPosibles[][] m_interior = new NodoPosibles
    		[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
	
	public final static NodoPosibles[][] m_interior_above_central = new NodoPosibles
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static NodoPosibles[][] m_interior_left_central = new NodoPosibles
    		[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static NodoPosibles[][] m_border_right = new NodoPosibles
    		[Consts.NUM_OF_CORNER_AND_BORDER_COLORS][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static NodoPosibles[] m_border_left = new NodoPosibles
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
    
	public final static NodoPosibles[] m_border_top = new NodoPosibles
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static NodoPosibles[][] m_border_bottom = new NodoPosibles
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static NodoPosibles[] m_corner_top_left = new NodoPosibles
			[1];
	
	public final static NodoPosibles[] m_corner_top_right = new NodoPosibles
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static NodoPosibles[] m_corner_bottom_left = new NodoPosibles
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static NodoPosibles[][] m_corner_bottom_right = new NodoPosibles
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS][Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	@Override
	public void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p, short piezaIndex, byte rot) {
		if (Pieza.isInterior(p)) {
			// normal interior pieza
			NodoPosibles nodoP = getNodoIfKeyIsOriginal_interior(top, left);
			if (nodoP == null) {
				nodoP = NodoPosibles.newForKey_interior(top, left);
				m_interior[top][left] = nodoP;
			}
			NodoPosibles.addNeighbor(nodoP, piezaIndex, rot);
			// above pieza central?
			if (bottom == Consts.PIEZA_CENTRAL_COLOR_TOP) {
				NodoPosibles nodoP1 = getNodoIfKeyIsOriginal_interior_above_central(top, left);
				if (nodoP1 == null) {
					nodoP1 = NodoPosibles.newForKey_interior_above_central(top, left);
					m_interior_above_central[top][left] = nodoP1;
				}
				NodoPosibles.addNeighbor(nodoP1, piezaIndex, rot);
			}
			// left pieza central?
			if (right == Consts.PIEZA_CENTRAL_COLOR_LEFT) {
				NodoPosibles nodoP2 = getNodoIfKeyIsOriginal_interior_left_central(top, left);
				if (nodoP2 == null) {
					nodoP2 = NodoPosibles.newForKey_interior_left_central(top, left);
					m_interior_left_central[top][left] = nodoP2;
				}
				NodoPosibles.addNeighbor(nodoP2, piezaIndex, rot);
			}
		}
		else if (Pieza.isBorder(p)) {
			NodoPosibles nodoP = null;
			// border right?
			if (right == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_border_right(top, left);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_border_right((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR), left);
					m_border_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left] = nodoP;
				}
			}
			// border left?
			else if (left == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_border_left(top);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_border_left((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			// border top?
			else if (top == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_border_top(left);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_border_top((byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_top[left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			// border bottom?
			else if (bottom == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_border_bottom(top, left);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_border_bottom(top, (byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_bottom[top][left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			NodoPosibles.addNeighbor(nodoP, piezaIndex, rot);
		}
		else if (Pieza.isCorner(p)) {
			NodoPosibles nodoP = null;
			// top left corner?
			if (top == Consts.GRIS && left == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_corner_top_left();
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_corner_top_left();
					m_corner_top_left[0] = nodoP;
				}
			}
			// top right corner?
			else if (top == Consts.GRIS && right == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_corner_top_right(left);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_corner_top_right((byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_top_right[left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			// bottom left corner?
			else if (bottom == Consts.GRIS && left == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_corner_bottom_left(top);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_corner_bottom_left((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_bottom_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			// bottom right corner?
			else if (bottom == Consts.GRIS && right == Consts.GRIS) {
				nodoP = getNodoIfKeyIsOriginal_corner_bottom_right(top, left);
				if (nodoP == null) {
					nodoP = NodoPosibles.newForKey_corner_bottom_right((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR), (byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_bottom_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nodoP;
				}
			}
			NodoPosibles.addNeighbor(nodoP, piezaIndex, rot);
		}
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior(byte top, byte left) {
		return m_interior[top][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior_above_central(byte top, byte left) {
		// we assume the tiles in the NodoPosibles match their bottom color with Consts.PIEZA_CENTRAL_COLOR_TOP
		return m_interior_above_central[top][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_interior_left_central(byte top, byte left) {
		// we assume the tiles in the NodoPosibles match their bottom color with Consts.PIEZA_CENTRAL_COLOR_LEFT
		return m_interior_left_central[top][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_right(byte top, byte left) {
		// we assume the tiles in the NodoPosibles match their right color with Pieza.GRIS
		return m_border_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_left(byte top) {
		// we assume the tiles in the NodoPosibles match their left color with Pieza.GRIS
		return m_border_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_top(byte left) {
		// we assume the tiles in the NodoPosibles match their top color with Pieza.GRIS
		return m_border_top[left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_border_bottom(byte top, byte left) {
		// we assume the tiles in the NodoPosibles match their bottom color with Pieza.GRIS
		return m_border_bottom[top][left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_top_left() {
		// we assume the tiles in the NodoPosibles match their top and left color with Pieza.GRIS
		return m_corner_top_left[0];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_top_right(byte left) {
		// we assume the tiles in the NodoPosibles match their top and right color with Pieza.GRIS
		return m_corner_top_right[left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_left(byte top) {
		// we assume the tiles in the NodoPosibles match their bottom and left color with Pieza.GRIS
		return m_corner_bottom_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public NodoPosibles getNodoIfKeyIsOriginal_corner_bottom_right(byte top, byte left) {
		// we assume the tiles in the NodoPosibles match their right and bottom color with Pieza.GRIS
		return m_corner_bottom_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public void resetForBenchmark() {
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				m_interior[a][b] = null;
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				m_interior_above_central[a][b] = null;
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				m_interior_left_central[a][b] = null;
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				m_border_right[a][b] = null;
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			m_border_left[a] = null;
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			m_border_top[a] = null;
		}
		
		for (byte a=0; a < m_border_bottom.length; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				m_border_bottom[a][b] = null;
			}
		}
		
		for (byte a=0; a < m_corner_top_left.length; ++a) {
			m_corner_top_left[a] = null;
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			m_corner_top_right[a] = null;
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			m_corner_bottom_left[a] = null;
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				m_corner_bottom_right[a][b] = null;
			}
		}
	}
	
	@Override
	public void printMergedInfoSizes(boolean skipSizeOne) {
		
		System.out.println("Size for m_interior:");
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
		
		System.out.println("Size for m_interior_above_central:");
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior_above_central[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
		
		System.out.println("Size for m_interior_left_central:");
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior_left_central[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
		
		System.out.println("Size for m_border_right:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_border_right[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
		
		System.out.println("Size for m_border_left:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_border_left[a];
			if (nodoP != null) {
				int key = a;
				int size = 0;
				for (short info : nodoP.mergedInfo) {
					if (info != -1)
						++size;
					else
						break;
				}
				if (!skipSizeOne || (skipSizeOne && size > 1))
					System.out.println("case " + key + ": return " + size + ";");
			}
		}
		
		System.out.println("Size for m_border_top:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_border_top[a];
			if (nodoP != null) {
				int key = a;
				int size = 0;
				for (short info : nodoP.mergedInfo) {
					if (info != -1)
						++size;
					else
						break;
				}
				if (!skipSizeOne || (skipSizeOne && size > 1))
					System.out.println("case " + key + ": return " + size + ";");
			}
		}
		
		System.out.println("Size for m_border_bottom:");
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				NodoPosibles nodoP = m_border_bottom[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
		
		System.out.println("Size for m_corner_top_left:");
		for (byte a=0; a < m_corner_top_left.length; ++a) {
			NodoPosibles nodoP = m_corner_top_left[a];
			if (nodoP != null) {
				int key = a;
				int size = 0;
				for (short info : nodoP.mergedInfo) {
					if (info != -1)
						++size;
					else
						break;
				}
				if (!skipSizeOne || (skipSizeOne && size > 1))
					System.out.println("case " + key + ": return " + size + ";");
			}
		}
		
		System.out.println("Size for m_corner_top_right:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_corner_top_right[a];
			if (nodoP != null) {
				int key = a;
				int size = 0;
				for (short info : nodoP.mergedInfo) {
					if (info != -1)
						++size;
					else
						break;
				}
				if (!skipSizeOne || (skipSizeOne && size > 1))
					System.out.println("case " + key + ": return " + size + ";");
			}
		}
		
		System.out.println("Size for m_corner_bottom_left:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_corner_bottom_left[a];
			if (nodoP != null) {
				int key = a;
				int size = 0;
				for (short info : nodoP.mergedInfo) {
					if (info != -1)
						++size;
					else
						break;
				}
				if (!skipSizeOne || (skipSizeOne && size > 1))
					System.out.println("case " + key + ": return " + size + ";");
			}
		}
		
		System.out.println("Size for m_corner_bottom_right:");
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				NodoPosibles nodoP = m_corner_bottom_right[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					int size = 0;
					for (short info : nodoP.mergedInfo) {
						if (info != -1)
							++size;
						else
							break;
					}
					if (!skipSizeOne || (skipSizeOne && size > 1))
						System.out.println("case " + key + ": return " + size + ";");
				}
			}
		}
	}
	
	@Override
	public void printKeys() {
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior_above_central[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_interior_left_central[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				NodoPosibles nodoP = m_border_right[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_border_left[a];
			if (nodoP != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_border_top[a];
			if (nodoP != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				NodoPosibles nodoP = m_border_bottom[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < m_corner_top_left.length; ++a) {
			NodoPosibles nodoP = m_corner_top_left[a];
			if (nodoP != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_corner_top_right[a];
			if (nodoP != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			NodoPosibles nodoP = m_corner_bottom_left[a];
			if (nodoP != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				NodoPosibles nodoP = m_corner_bottom_right[a][b];
				if (nodoP != null) {
					int key = NodoPosibles.asKey(a, b);
					System.out.println(key);
				}
			}
		}
	}
	
}
