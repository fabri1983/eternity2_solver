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

package org.fabri1983.eternity2.core.neighbors;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.Pieza;

public class MultiDimensionalStrategy implements NeighborStrategy {

	public final static Neighbors[][] m_interior = new Neighbors
    		[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
	
	public final static Neighbors[][] m_interior_above_central = new Neighbors
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static Neighbors[][] m_interior_left_central = new Neighbors
    		[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static Neighbors[][] m_border_right = new Neighbors
    		[Consts.NUM_OF_CORNER_AND_BORDER_COLORS][Consts.FIRST_CORNER_OR_BORDER_COLOR];
    
	public final static Neighbors[] m_border_left = new Neighbors
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
    
	public final static Neighbors[] m_border_top = new Neighbors
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static Neighbors[][] m_border_bottom = new Neighbors
			[Consts.FIRST_CORNER_OR_BORDER_COLOR][Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static Neighbors[] m_corner_top_left = new Neighbors
			[1];
	
	public final static Neighbors[] m_corner_top_right = new Neighbors
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static Neighbors[] m_corner_bottom_left = new Neighbors
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	public final static Neighbors[][] m_corner_bottom_right = new Neighbors
			[Consts.NUM_OF_CORNER_AND_BORDER_COLORS][Consts.NUM_OF_CORNER_AND_BORDER_COLORS];
	
	@Override
	public void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p) {
		
		short piezaIndex = p.numero;
		
		if (Pieza.isInterior(p)) {
			// normal interior pieza
			Neighbors nbs = interior(top, left);
			if (nbs == null) {
				nbs = Neighbors.newForKey_interior(top, left);
				m_interior[top][left] = nbs;
			}
			Neighbors.addNeighbor(nbs, top, right, bottom, left, piezaIndex);
			// above pieza central?
			if (bottom == Consts.PIEZA_CENTRAL_COLOR_TOP) {
				Neighbors nbs1 = interior_above_central(top, left);
				if (nbs1 == null) {
					nbs1 = Neighbors.newForKey_interior_above_central(top, left);
					m_interior_above_central[top][left] = nbs1;
				}
				Neighbors.addNeighbor(nbs1, top, right, bottom, left, piezaIndex);
			}
			// left pieza central?
			if (right == Consts.PIEZA_CENTRAL_COLOR_LEFT) {
				Neighbors nbs2 = interior_left_central(top, left);
				if (nbs2 == null) {
					nbs2 = Neighbors.newForKey_interior_left_central(top, left);
					m_interior_left_central[top][left] = nbs2;
				}
				Neighbors.addNeighbor(nbs2, top, right, bottom, left, piezaIndex);
			}
		}
		else if (Pieza.isBorder(p)) {
			Neighbors nbs = null;
			// border right?
			if (right == Consts.GRIS) {
				nbs = border_right(top, left);
				if (nbs == null) {
					nbs = Neighbors.newForKey_border_right((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR), left);
					m_border_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left] = nbs;
				}
			}
			// border left?
			else if (left == Consts.GRIS) {
				nbs = border_left(top);
				if (nbs == null) {
					nbs = Neighbors.newForKey_border_left((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			// border top?
			else if (top == Consts.GRIS) {
				nbs = border_top(left);
				if (nbs == null) {
					nbs = Neighbors.newForKey_border_top((byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_top[left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			// border bottom?
			else if (bottom == Consts.GRIS) {
				nbs = border_bottom(top, left);
				if (nbs == null) {
					nbs = Neighbors.newForKey_border_bottom(top, (byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_border_bottom[top][left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			Neighbors.addNeighbor(nbs, top, right, bottom, left, piezaIndex);
		}
		else if (Pieza.isCorner(p)) {
			Neighbors nbs = null;
			// top left corner?
			if (top == Consts.GRIS && left == Consts.GRIS) {
				nbs = corner_top_left();
				if (nbs == null) {
					nbs = Neighbors.newForKey_corner_top_left();
					m_corner_top_left[0] = nbs;
				}
			}
			// top right corner?
			else if (top == Consts.GRIS && right == Consts.GRIS) {
				nbs = corner_top_right(left);
				if (nbs == null) {
					nbs = Neighbors.newForKey_corner_top_right((byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_top_right[left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			// bottom left corner?
			else if (bottom == Consts.GRIS && left == Consts.GRIS) {
				nbs = corner_bottom_left(top);
				if (nbs == null) {
					nbs = Neighbors.newForKey_corner_bottom_left((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_bottom_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			// bottom right corner?
			else if (bottom == Consts.GRIS && right == Consts.GRIS) {
				nbs = corner_bottom_right(top, left);
				if (nbs == null) {
					nbs = Neighbors.newForKey_corner_bottom_right((byte)(top - Consts.FIRST_CORNER_OR_BORDER_COLOR), (byte)(left - Consts.FIRST_CORNER_OR_BORDER_COLOR));
					m_corner_bottom_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left - Consts.FIRST_CORNER_OR_BORDER_COLOR] = nbs;
				}
			}
			Neighbors.addNeighbor(nbs, top, right, bottom, left, piezaIndex);
		}
	}

	@Override
	public Neighbors interior(byte top, byte left) {
		return m_interior[top][left];
	}

	@Override
	public Neighbors interior_above_central(byte top, byte left) {
		// we assume the tiles in the neighbors match their bottom color with Consts.PIEZA_CENTRAL_COLOR_TOP
		return m_interior_above_central[top][left];
	}

	@Override
	public Neighbors interior_left_central(byte top, byte left) {
		// we assume the tiles in the neighbors match their bottom color with Consts.PIEZA_CENTRAL_COLOR_LEFT
		return m_interior_left_central[top][left];
	}

	@Override
	public Neighbors border_right(byte top, byte left) {
		// we assume the tiles in the neighbors match their right color with Pieza.GRIS
		return m_border_right[top - Consts.FIRST_CORNER_OR_BORDER_COLOR][left];
	}

	@Override
	public Neighbors border_left(byte top) {
		// we assume the tiles in the neighbors match their left color with Pieza.GRIS
		return m_border_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public Neighbors border_top(byte left) {
		// we assume the tiles in the neighbors match their top color with Pieza.GRIS
		return m_border_top[left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public Neighbors border_bottom(byte top, byte left) {
		// we assume the tiles in the neighbors match their bottom color with Pieza.GRIS
		return m_border_bottom[top][left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public Neighbors corner_top_left() {
		// we assume the tiles in the neighbors match their top and left color with Pieza.GRIS
		return m_corner_top_left[0];
	}

	@Override
	public Neighbors corner_top_right(byte left) {
		// we assume the tiles in the neighbors match their top and right color with Pieza.GRIS
		return m_corner_top_right[left - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public Neighbors corner_bottom_left(byte top) {
		// we assume the tiles in the neighbors match their bottom and left color with Pieza.GRIS
		return m_corner_bottom_left[top - Consts.FIRST_CORNER_OR_BORDER_COLOR];
	}

	@Override
	public Neighbors corner_bottom_right(byte top, byte left) {
		// we assume the tiles in the neighbors match their right and bottom color with Pieza.GRIS
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
				Neighbors nbs = m_interior[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_interior_above_central[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_interior_left_central[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_border_right[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
			Neighbors nbs = m_border_left[a];
			if (nbs != null) {
				int key = a;
				int size = 0;
				for (int info : nbs.mergedInfo) {
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
			Neighbors nbs = m_border_top[a];
			if (nbs != null) {
				int key = a;
				int size = 0;
				for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_border_bottom[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
			Neighbors nbs = m_corner_top_left[a];
			if (nbs != null) {
				int key = a;
				int size = 0;
				for (int info : nbs.mergedInfo) {
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
			Neighbors nbs = m_corner_top_right[a];
			if (nbs != null) {
				int key = a;
				int size = 0;
				for (int info : nbs.mergedInfo) {
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
			Neighbors nbs = m_corner_bottom_left[a];
			if (nbs != null) {
				int key = a;
				int size = 0;
				for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_corner_bottom_right[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					int size = 0;
					for (int info : nbs.mergedInfo) {
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
				Neighbors nbs = m_interior[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				Neighbors nbs = m_interior_above_central[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				Neighbors nbs = m_interior_left_central[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++b) {
				Neighbors nbs = m_border_right[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			Neighbors nbs = m_border_left[a];
			if (nbs != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			Neighbors nbs = m_border_top[a];
			if (nbs != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.FIRST_CORNER_OR_BORDER_COLOR; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				Neighbors nbs = m_border_bottom[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
		
		for (byte a=0; a < m_corner_top_left.length; ++a) {
			Neighbors nbs = m_corner_top_left[a];
			if (nbs != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			Neighbors nbs = m_corner_top_right[a];
			if (nbs != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			Neighbors nbs = m_corner_bottom_left[a];
			if (nbs != null) {
				int key = a;
				System.out.println(key);
			}
		}
		
		for (byte a=0; a < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++a) {
			for (byte b=0; b < Consts.NUM_OF_CORNER_AND_BORDER_COLORS; ++b) {
				Neighbors nbs = m_corner_bottom_right[a][b];
				if (nbs != null) {
					int key = Neighbors.colorsAsKey(a, b);
					System.out.println(key);
				}
			}
		}
	}
	
}
