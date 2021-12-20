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

/**
 * This strategy is an improvement over {@link MultiDimensionalStrategy} with much less memory but slower access.<br/>
 * It uses a pre calculated Perfect Hash Function with an array of size PerfectHashFunction2.PHASHRANGE.
 */
public class HashFunctionStrategy implements NeighborStrategy {

	@Override
	public void addNeighbor(byte top, byte right, byte bottom, byte left, Pieza p) {
		// TODO Auto-generated method stub
	}

	@Override
	public Neighbors interior(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors interior_above_central(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors interior_left_central(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors border_right(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors border_left(byte top) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors border_top(byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors border_bottom(byte top, byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors corner_top_left() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors corner_top_right(byte left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors corner_bottom_left(byte top) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Neighbors corner_bottom_right(byte top, byte left) {
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
