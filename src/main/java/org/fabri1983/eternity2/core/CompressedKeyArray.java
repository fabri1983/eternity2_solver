package org.fabri1983.eternity2.core;

public final class CompressedKeyArray {

	public static final NodoPosibles[] setupCompressedkeyArray() {
		NodoPosibles[] compressedKeyArray = new NodoPosibles[6954 + 1]; // +1 because I start mapping from 1 instead of 0
		
		return compressedKeyArray;
	}
	
	/**
	 * Guarda la direccion address de 32 bits (int) dividida en 4 partes de 8 bits each en address_array 
	 * en las diferentes posiciones dadas por top, right, bottom, y left.
	 */
	public static final void addAddress (final byte top, final byte right, final byte bottom, final byte left, 
			int[] address_array, int address)
	{
		// NOTA: si address_array fuera de tipo bytes[] entonces tendría q usar >> así:
		//  para top: (byte) (address >> 24)
		//  para right: (byte) (address >> 16)
		//  para bottom: (byte) (address >> 8)
		//  para left: (byte) (address >> 0)
		//  No haría falta aplicar & porque luego del >> el casting a byte me toma los primeros 8 bits
		
		address_array[top] = address & 0xFF000000; // me quedo con los bits 31..24
		address_array[right] = address & 0xFF0000; // me quedo con los bits 23..16
		address_array[bottom] = address & 0xFF00;  // me quedo con los bits 15..8
		address_array[left] = address & 0xFF;      // me quedo con los bits  7..0
	}
	
	/**
	 * Devuelve el address de 32 bits (int) guardado en address_array[] para esa combinación de colores.
	 * 
	 * @return 0 si no se ha guardado address en esa combinación de colores.
	 */
	public static final int getAddress (final byte top, final byte right, final byte bottom, final byte left, 
			int[] address_array)
	{
		// NOTA: si address_array fuera de tipo bytes[] entonces tendría q usar << así:
		//  address_array[top] << 24
		//  address_array[right] << 16
		//  address_array[bottom] << 8
		//  address_array[left] << 0
		//  No haría falta aplicar & porque el << me completa deja 0s a la derecha
		
		int address = address_array[top] // address bits 31..24
				| address_array[right]   // address bits 23..16
				| address_array[bottom]  // address bits 15..8
				| address_array[left];   // address bits  7..0
									     // Nota: no uso << ni & porque los bits ya estan en su lugar correcto en el contexto de 32 bits.
		return address;
	}
	
}
