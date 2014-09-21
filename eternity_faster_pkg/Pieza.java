/**
 * Copyright (c) 2010 Fabricio Lettieri fabri1983@gmail.com
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

package eternity_faster_pkg;


public class Pieza {
	
	private final static byte GRIS=0;
	private final static String SECCIONES_SEPARATOR_EN_FILE= " ";
	private final static byte MAX_ESTADOS_ROTACION= 4; // el numero maximo de estados de rotacion por pieza
	
	/*
	 * NOTA: ubicación de datos en data
	 * Los bits 0..4 color top.
	 * Los bits 5..9 color right.
	 * Los bits 10..14 color bottom.
	 * Los bits 15..19 color left.
	 * Los bits 20..21 son la rotacion de la pieza.
	 * Los bits 22..29 son el número de la pieza
	 * El bit 30 es usada (0 false, 1 true)
	 */
	/*public static final int MASCARA_COLOR = 31; //mascara para quedarme con los primeros 5 bits
	public static final int MASCARA_ROTACION = 3; //mascara para quedarme con los primeros 2 bits
	public static final int MASCARA_NUMERO = 255; //mascara para quedarme con los primeros 8 bits
	public static final int MASCARA_USADA = 1; //mascara para quedarme con el primer bit
	public static final int OFFSET_TOP = 0;
	public static final int OFFSET_RIGHT = 5;
	public static final int OFFSET_BOTTOM = 10;
	public static final int OFFSET_LEFT = 15;
	public static final int OFFSET_ROTACION = 20;
	public static final int OFFSET_NUMERO = 22;
	public static final int OFFSET_USADA = 30;*/
	
	public byte top,right,bottom,left;
	public int numero; // numero que representa la pieza en el juego real
	public byte rotacion;
	public PUsada pusada;
	//public int pos; //indica la posicion en tablero en la que se encuentra la pieza
	public byte count_grises;
	public boolean es_match_central; //me dice si tiene al menos uno de los colores de la pieza central (6, 11 o 18)
	public boolean es_esquina, es_borde, es_interior;
	/*public int idUnico; // es un número para identificar unequivocamente la instancia de la pieza, pues se hacen copias
	private static int countIdUnico = 0;*/
	
	
	public Pieza (String s, int num)
	{
		// separo los 4 numeros que hay en s y se los asigno a c/u de los 4 triangulitos
		int primer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,0);
		int seg_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,primer_sep+1);
		int tercer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,seg_sep+1);

		top= Byte.parseByte(s.substring(0,primer_sep));
		right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		left= Byte.parseByte(s.substring(tercer_sep+1,s.length()));
		contarGrises(this);
		
		numero= num;
		rotacion=0;
		pusada=new PUsada();
		//pos= -1;
		setMatchCentral(this);
		
		/*idUnico = countIdUnico;
		++countIdUnico;*/
	}
	
	public Pieza (byte t, byte r, byte b, byte l, int num)
	{
		top=t;
		right=r;
		bottom=b;
		left=l;
		contarGrises(this);
		numero=num;
		rotacion=0;
		pusada=new PUsada();
		//pos= -1;
		setMatchCentral(this);
		
		/*idUnico = countIdUnico;
		++countIdUnico;*/
	}
	
	public Pieza (Pieza pz)
	{
		top= pz.top;
		right= pz.right;
		bottom= pz.bottom;
		left= pz.left;
		contarGrises(this);
		numero= pz.numero;
		rotacion= pz.rotacion;
		pusada= pz.pusada;
		//pos= pz.pos;
		setMatchCentral(this);
		
		/*idUnico = countIdUnico;
		++countIdUnico;*/
	}
	
	/**
	 * Este constructor se encarga de extraer todos los parametros de una pieza
	 * contenidos en un String.
	 */
	public Pieza (String s)
	{
		// Primero: separo los valores de top, right, bottom y left
		int primer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,0);
		int seg_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,primer_sep+1);
		int tercer_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,seg_sep+1);
		int cuarto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,tercer_sep+1);
		top= Byte.parseByte(s.substring(0,primer_sep));
		right= Byte.parseByte(s.substring(primer_sep+1,seg_sep));
		bottom= Byte.parseByte(s.substring(seg_sep+1,tercer_sep));
		left= Byte.parseByte(s.substring(tercer_sep+1,cuarto_sep));
		contarGrises(this);
		
		//Segundo: separo el valor numerico de la pieza
		int quinto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,cuarto_sep+1);
		numero= Integer.parseInt(s.substring(cuarto_sep+1,quinto_sep));
		
		//Tercero: separo el valor de rotacion de la pieza
		int sexto_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,quinto_sep+1);
		rotacion= Byte.parseByte(s.substring(quinto_sep+1,sexto_sep));
		
		//Cuarto: separo el valor usada de la pieza
		int sept_sep= s.indexOf(SECCIONES_SEPARATOR_EN_FILE,sexto_sep+1);
		pusada= new PUsada(Boolean.parseBoolean(s.substring(sexto_sep+1,sept_sep)));

		//Quinto: separo la posicion en la que se encuentra la pieza
		//pos= Integer.parseInt(s.substring(sept_sep+1,s.length()));
		
		setMatchCentral(this);
		
		/*idUnico = countIdUnico;
		++countIdUnico;*/
	}
	
	private static final void contarGrises(final Pieza p)
	{
		p.count_grises=0;
		if (p.top==GRIS) ++p.count_grises;
		if (p.right==GRIS) ++p.count_grises;
		if (p.bottom==GRIS) ++p.count_grises;
		if (p.left==GRIS) ++p.count_grises;
		
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
	
	public final String toString ()
	{
		return new String(top + SECCIONES_SEPARATOR_EN_FILE + right + SECCIONES_SEPARATOR_EN_FILE 
						+ bottom + SECCIONES_SEPARATOR_EN_FILE + left + SECCIONES_SEPARATOR_EN_FILE 
						+ numero + SECCIONES_SEPARATOR_EN_FILE + rotacion + SECCIONES_SEPARATOR_EN_FILE 
						+ String.valueOf(pusada.value) /*+ SECCIONES_SEPARATOR_EN_FILE + pos*/);
	}
	
	public final String toStringColores ()
	{
		return new String(top + SECCIONES_SEPARATOR_EN_FILE + right + SECCIONES_SEPARATOR_EN_FILE + bottom + SECCIONES_SEPARATOR_EN_FILE + left);
	}
	
	public static final boolean tieneColor (final Pieza p, final int color)
	{
		if ((p.top==color) || (p.right==color) || (p.bottom==color) || (p.left==color))
			return true;
		return false;
	}
	
	public final static void rotar90 (final Pieza p)
	{
		final byte aux= p.left;
		p.left= p.bottom;
		p.bottom= p.right;
		p.right= p.top;
		p.top= aux;
		p.rotacion= (byte) ((p.rotacion + 1) & ~MAX_ESTADOS_ROTACION);
	}
	
	public final static void rotar180 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.right;
		p.right= aux;
		final byte aux2 = p.top;
		p.top= p.bottom;
		p.bottom= aux2;
		p.rotacion= (byte) ((p.rotacion + 2) & ~MAX_ESTADOS_ROTACION);
	}
	
	public final static void rotar270 (final Pieza p)
	{
		final byte aux = p.left;
		p.left= p.top;
		p.top= p.right;
		p.right= p.bottom;
		p.bottom= aux;
		p.rotacion= (byte) ((p.rotacion + 3) & ~MAX_ESTADOS_ROTACION);
	}

	/**
	 * Esta funcion lleva el estado actual de rotacion de la pieza
	 * al estado indicado en el parametro rot.
	 */
	public static final void llevarARotacion (final Pieza p, final byte rot)
	{
		if (p.rotacion == 2) {
			switch (rot) {
				case 0: rotar180(p); break;
				case 1: rotar270(p); break;
				case 2: break;
				case 3: rotar90(p); break;
			}
		}
		else if (p.rotacion > 2) {
			switch (rot){
				case 0: rotar90(p); break;
				case 1: rotar180(p); break;
				case 2: rotar270(p); break;
				case 3: break;
			}
		}
		else if (p.rotacion == 1) {
			switch (rot){
				case 0: rotar270(p); break;
				case 1: break;
				case 2: rotar90(p); break;
				case 3: rotar180(p); break;
			}
		}
		else {
			switch (rot){
				case 0: break;
				case 1: rotar90(p); break;
				case 2: rotar180(p); break;
				case 3: rotar270(p); break;
			}
		}
		/*switch (p.rotacion){
			case 0:{
				switch (rot){
				case 0: break;
				case 1: rotar90(p); break;
				case 2: rotar180(p); break;
				case 3: rotar270(p); break;
				}
				break;
			}
			case 1:{
				switch (rot){
				case 0: rotar270(p); break;
				case 1: break;
				case 2: rotar90(p); break;
				case 3: rotar180(p); break;
				}
				break;
			}
			case 2:{
				switch (rot){
				case 0: rotar180(p); break;
				case 1: rotar270(p); break;
				case 2: break;
				case 3: rotar90(p); break;
				}
				break;
			}
			case 3:{
				switch (rot){
				case 0: rotar90(p); break;
				case 1: rotar180(p); break;
				case 2: rotar270(p); break;
				case 3: break;
				}
				break;
			}
		}*/
	}
	
	/**
	 * Pieza p es igual a esta instancia solo si el número es el mismo.
	 * @param p
	 * @return
	 */
	public final boolean equals (final Pieza p)
	{
		return p.numero == this.numero;
	}
	
	public final static Pieza copia (final Pieza p)
	{
		return new Pieza(p);
	}
}