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

import org.fabri1983.eternity2.core.Pieza;

public interface NeighborStrategy {
	
	void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p);

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
