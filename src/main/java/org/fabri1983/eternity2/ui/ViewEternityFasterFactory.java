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

package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.faster.ExploracionAction;

public class ViewEternityFasterFactory implements ViewEternityFactory {
	
	private int lado;
	private int cell_size_pixels;
	private int num_colours;
	private long refreshMillis;
	private int totalProcs;
	private ExploracionAction action;
	
	public ViewEternityFasterFactory(int lado, int cell_size_pixels, int num_colours, long refreshMillis,
			int totalProcs, ExploracionAction action) {
		super();
		this.lado = lado;
		this.cell_size_pixels = cell_size_pixels;
		this.num_colours = num_colours;
		this.refreshMillis = refreshMillis;
		this.totalProcs = totalProcs;
		this.action = action;
	}

	@Override
	public ViewEternityAbstract create() {
		return new ViewEternityFaster(refreshMillis, lado, cell_size_pixels, num_colours, action);
	}
	
	@Override
	public int getProc() {
		return action.id;
	}
	
	@Override
	public int getTotalProcs() {
		return totalProcs;
	}
}
