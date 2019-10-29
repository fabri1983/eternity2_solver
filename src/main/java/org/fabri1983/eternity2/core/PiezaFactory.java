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

	
	public static Pieza dummy()
	{
		Pieza p = new Pieza();
		
		p.top=0;
		p.right=0;
		p.bottom=0;
		p.left=0;
		contarGrises(p);
		p.numero=0;
		p.rotacion=0;
		p.usada=false;
		//p.pos= -1;
		setMatchCentral(p);
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
		
		return p;
	}

	public static Pieza from (String s, byte num)
	{
		Pieza p = new Pieza();
		
		// separo los 4 números que hay en s y se los asigno a c/u de los 4 triangulitos
		int primer_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, 0);
		int seg_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, primer_sep+1);
		int tercer_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, seg_sep+1);

		p.top= Byte.parseByte(s.substring(0,primer_sep));
		p.right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		p.bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		p.left= Byte.parseByte(s.substring(tercer_sep+1,s.length()));
		contarGrises(p);
		
		p.numero= num;
		p.rotacion=0;
		p.usada=false;
		//pos= -1;
		setMatchCentral(p);
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
		
		return p;
	}
	
	public static Pieza from (Pieza pz)
	{
		Pieza p = new Pieza();
		
		p.top= pz.top;
		p.right= pz.right;
		p.bottom= pz.bottom;
		p.left= pz.left;
		contarGrises(p);
		p.numero= pz.numero;
		p.rotacion= pz.rotacion;
		p.usada= pz.usada;
		//pos= pz.pos;
		setMatchCentral(p);
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
		
		return p;
	}
	
	/**
	 * Este constructor se encarga de extraer todos los parametros de una pieza
	 * contenidos en un String.
	 */
	public static Pieza from (String s)
	{
		Pieza p = new Pieza();
		
		// Primero: separo los valores de top, right, bottom y left
		int primer_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, 0);
		int seg_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, primer_sep+1);
		int tercer_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, seg_sep+1);
		int cuarto_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, tercer_sep+1);
		
		p.top= Byte.parseByte(s.substring(0,primer_sep));
		p.right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		p.bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		p.left= Byte.parseByte(s.substring(tercer_sep+1,cuarto_sep));
		contarGrises(p);
		
		//Segundo: separo el valor numerico de la pieza
		int quinto_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, cuarto_sep+1);
		p.numero= Byte.parseByte(s.substring(cuarto_sep+1,quinto_sep));
		
		// Tercero: separo el valor de rotación de la pieza
		int sexto_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, quinto_sep+1);
		p.rotacion= Byte.parseByte(s.substring(quinto_sep+1,sexto_sep));
		
		//Cuarto: separo el valor usada de la pieza
		int sept_sep= s.indexOf(Pieza.SECCIONES_SEPARATOR_EN_FILE, sexto_sep+1);
		p.usada= Boolean.parseBoolean(s.substring(sexto_sep+1,sept_sep));

		// Quinto: separo la posición en la que se encuentra la pieza
		//pos= Integer.parseInt(s.substring(sept_sep+1,s.length()));
		
		setMatchCentral(p);
		
		/*p.idUnico = p.countIdUnico;
		++p.countIdUnico;*/
		
		return p;
	}
	
	private static final void contarGrises(final Pieza p)
	{
		p.count_grises=0;
		if (p.top == Pieza.GRIS) ++p.count_grises;
		if (p.right == Pieza.GRIS) ++p.count_grises;
		if (p.bottom == Pieza.GRIS) ++p.count_grises;
		if (p.left == Pieza.GRIS) ++p.count_grises;
		
		p.es_esquina = false;
		p.es_borde = false;
		p.es_interior = false;
		if (p.count_grises==2)
			p.es_esquina = true;
		else if (p.count_grises==1)
			p.es_borde = true;
		else
			p.es_interior = true;
	}
	
	private static final void setMatchCentral (final Pieza p)
	{
		p.es_match_central= false;
		if (p.top==6 || p.top==11 || p.top==18)
			p.es_match_central=true;
		else if (p.right==6 || p.right==11 || p.right==18)
			p.es_match_central=true;
		else if (p.bottom==6 || p.bottom==11 || p.bottom==18)
			p.es_match_central=true;
		else if (p.left==6 || p.left==11 || p.left==18)
			p.es_match_central=true;
	}
	
}
