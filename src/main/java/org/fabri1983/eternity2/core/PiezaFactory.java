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

public final class PiezaFactory {
	
	public static final byte GRIS = 22;
	private static final String SECCIONES_SEPARATOR_EN_FILE= " ";
	
	public static Pieza dummy()
	{
		Pieza p = new Pieza();
		setAsDummy(p);
		return p;
	}

	public static void setAsDummy(Pieza p) {
		p.top=GRIS;
		p.right=GRIS;
		p.bottom=GRIS;
		p.left=GRIS;
		setFeature(p);
		p.numero=0;
		p.rotacion=0;
		p.usada=false;
		//p.pos= -1;
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
	}

	public static Pieza from (String s, short num)
	{
		Pieza p = new Pieza();
		setFromStringWithNum(s, num, p);
		return p;
	}

	public static void setFromStringWithNum(String s, short num, Pieza p) {
		// separo los 4 números que hay en s y se los asigno a c/u de los 4 triangulitos
		int primer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, 0);
		int seg_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, primer_sep+1);
		int tercer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, seg_sep+1);

		p.top= Byte.parseByte(s.substring(0,primer_sep));
		p.right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		p.bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		p.left= Byte.parseByte(s.substring(tercer_sep+1,s.length()));
		setFeature(p);
		
		p.numero= num;
		p.rotacion=0;
		p.usada=false;
		//p.pos= -1;
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
	}
	
	public static Pieza from (Pieza pz)
	{
		Pieza p = new Pieza();
		setFromPieza(pz, p);
		return p;
	}

	public static void setFromPieza(Pieza pz, Pieza p) {
		p.top= pz.top;
		p.right= pz.right;
		p.bottom= pz.bottom;
		p.left= pz.left;
		setFeature(p);
		p.numero= pz.numero;
		p.rotacion= pz.rotacion;
		p.usada= pz.usada;
		//p.pos= pz.pos;
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
	}
	
	/**
	 * Este constructor se encarga de extraer todos los parametros de una pieza
	 * contenidos en un String.
	 */
	public static Pieza from (String s)
	{
		Pieza p = new Pieza();
		setFromString(s, p);
		return p;
	}

	public static void setFromString(String s, Pieza p) {
		// Primero: separo los valores de top, right, bottom y left
		int primer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, 0);
		int seg_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, primer_sep+1);
		int tercer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, seg_sep+1);
		int cuarto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, tercer_sep+1);
		
		p.top= Byte.parseByte(s.substring(0,primer_sep));
		p.right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		p.bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		p.left= Byte.parseByte(s.substring(tercer_sep+1,cuarto_sep));
		setFeature(p);
		
		//Segundo: separo el valor numerico de la pieza
		int quinto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, cuarto_sep+1);
		p.numero= Short.parseShort(s.substring(cuarto_sep+1,quinto_sep));
		
		// Tercero: separo el valor de rotación de la pieza
		int sexto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, quinto_sep+1);
		p.rotacion= Byte.parseByte(s.substring(quinto_sep+1,sexto_sep));
		
		//Cuarto: separo el valor usada de la pieza
		int sept_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE, sexto_sep+1);
		p.usada= Boolean.parseBoolean(s.substring(sexto_sep+1,sept_sep));

		// Quinto: separo la posición en la que se encuentra la pieza
		//p.pos= Integer.parseInt(s.substring(sept_sep+1,s.length()));
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
	}
	
	private static final void setFeature(final Pieza p)
	{
		byte count_grises=0;
		if (p.top == GRIS) ++count_grises;
		if (p.right == GRIS) ++count_grises;
		if (p.bottom == GRIS) ++count_grises;
		if (p.left == GRIS) ++count_grises;
		
		p.feature = count_grises;
	}
	
}
