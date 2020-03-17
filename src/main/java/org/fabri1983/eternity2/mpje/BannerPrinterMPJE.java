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

package org.fabri1983.eternity2.mpje;

import mpi.MPI;

public class BannerPrinterMPJE {

	public static void printBanner() {
		StringBuilder msgBuilder = new StringBuilder(64*10);
		String lineSeparator = System.lineSeparator();
		msgBuilder.append("#################################################################").append(lineSeparator);
		msgBuilder.append("##- Uso de MPJExpress en modo híbrido (multicore y cluster).  -##").append(lineSeparator);
		msgBuilder.append("##- Version con many multi dimensional arrays, Smart-Podas,   -##").append(lineSeparator);
		msgBuilder.append("##- Bit Set, Restriccion de Contornos de 2 colores.           -##").append(lineSeparator);
		msgBuilder.append("##- Micro optimizaciones: bitwise and reductions.             -##").append(lineSeparator);
		msgBuilder.append("#################################################################").append(lineSeparator);
		msgBuilder.append("-----------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append("    Copyright(c) 2020 Fabricio Lettieri (fabri1983@gmail.com)    ").append(lineSeparator);
		msgBuilder.append("-----------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append(lineSeparator);
		msgBuilder.append("Total procs: " + MPI.COMM_WORLD.Size()).append(lineSeparator);
		System.out.println(msgBuilder.toString());
	}
	
}
