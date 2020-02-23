package org.fabri1983.eternity2.core.prune.color;

import java.util.concurrent.atomic.AtomicIntegerArray;

import org.fabri1983.eternity2.core.Consts;

public class ColorRightExploredAtomicStrategy implements ColorRightExploredStrategy {

	/**
	 * Cada posición es un entero donde se usan 23 bits para los colores donde un bit valdrá 0 si ese 
	 * color (right en borde left) no ha sido exlorado para la fila actual, sino valdrá 1.
	 */
	public final static AtomicIntegerArray arr_color_rigth_explorado = new AtomicIntegerArray(Consts.LADO);
	
	private boolean usar_poda_color_explorado;
	
	@Override
	public boolean usarPodaColorRightExpl() {
		return usar_poda_color_explorado;
	}

	@Override
	public int get(int i) {
		return arr_color_rigth_explorado.get(i);
	}

	@Override
	public void set(int i, int val) {
		arr_color_rigth_explorado.set(i, val);
	}

	@Override
	public void compareAndSet(int i, int val) {
		arr_color_rigth_explorado.compareAndSet(i, val, val);
	}
	
}
