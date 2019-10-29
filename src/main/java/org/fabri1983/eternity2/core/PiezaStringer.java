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

package org.fabri1983.eternity2.core;

public class PiezaStringer {

	public static final String toString (Pieza p)
	{
		return p.top + Pieza.SECCIONES_SEPARATOR_EN_FILE + p.right + Pieza.SECCIONES_SEPARATOR_EN_FILE 
				+ p.bottom + Pieza.SECCIONES_SEPARATOR_EN_FILE + p.left + Pieza.SECCIONES_SEPARATOR_EN_FILE 
				+ p.numero + Pieza.SECCIONES_SEPARATOR_EN_FILE + p.rotacion + Pieza.SECCIONES_SEPARATOR_EN_FILE 
				+ String.valueOf(p.usada) /*+ SECCIONES_SEPARATOR_EN_FILE + pos*/;
	}
	
	public static final String toStringColores (Pieza p)
	{
		return p.top + Pieza.SECCIONES_SEPARATOR_EN_FILE + p.right + Pieza.SECCIONES_SEPARATOR_EN_FILE 
				+ p.bottom + Pieza.SECCIONES_SEPARATOR_EN_FILE + p.left;
	}
	
}