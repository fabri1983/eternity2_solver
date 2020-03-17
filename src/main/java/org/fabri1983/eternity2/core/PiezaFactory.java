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

public class PiezaFactory {
	
	public static Pieza dummy() {
		Pieza p = new Pieza();
		setAsDummy(p);
		return p;
	}

	private static void setAsDummy(Pieza p) {
		p.top=Consts.GRIS;
		p.right=Consts.GRIS;
		p.bottom=Consts.GRIS;
		p.left=Consts.GRIS;
		p.numero=0;
		p.rotacion=0;
	}

	public static Pieza from (String s, short num) {
		Pieza p = new Pieza();
		setFromStringWithNum(s, num, p);
		return p;
	}

	private static void setFromStringWithNum(String s, short num, Pieza p) {
		// separo los 4 números que hay en s y se los asigno a c/u de los 4 triangulitos
		int primer_sep= s.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE, 0);
		int seg_sep= s.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE, primer_sep+1);
		int tercer_sep= s.indexOf(Consts.SECCIONES_SEPARATOR_EN_FILE, seg_sep+1);

		p.top= Byte.parseByte(s.substring(0,primer_sep));
		p.right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		p.bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		p.left= Byte.parseByte(s.substring(tercer_sep+1,s.length()));
		
		p.numero= num;
		p.rotacion=0;
	}
	
}
