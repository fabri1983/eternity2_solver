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

public class ViewEternityMPJEFactory implements ViewEternityFactory {
	
	private int lado;
	private int cell_size_pixels;
	private int num_colours;
	private long refreshMillis;
	private int proc;
	private int totalProcs;
	
	public ViewEternityMPJEFactory(int lado, int cell_size_pixels, int num_colours, long refreshMillis,
			int proc, int totalProcs) {
		super();
		this.lado = lado;
		this.cell_size_pixels = cell_size_pixels;
		this.num_colours = num_colours;
		this.refreshMillis = refreshMillis;
		this.proc = proc;
		this.totalProcs = totalProcs;
	}

	@Override
	public ViewEternity create() {
		return new ViewEternityForMPJE(refreshMillis, lado, cell_size_pixels, num_colours);
	}
	
	@Override
	public int getProc() {
		return proc;
	}
	
	@Override
	public int getTotalProcs() {
		return totalProcs;
	}
}
