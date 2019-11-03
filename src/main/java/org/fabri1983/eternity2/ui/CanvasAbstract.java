package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.PiezaFactory;

public abstract class CanvasAbstract implements Canvas {

	private int columns;
	private int rows;
	private Pieza[] viewPieces; // esta se usa para dibujar las piezas
	private int posCentral;
	private Pieza pieza_gris;

	public CanvasAbstract(int columns, int rows, int posCentral) {
		this.columns = columns;
		this.rows = rows;
		this.posCentral = posCentral;
		this.viewPieces = new Pieza[columns * rows];
		this.pieza_gris = PiezaFactory.dummy();
	}

	protected abstract Pieza getPiezaFromTablero(int cursorTablero);
	
	protected abstract Pieza getPiezaCentral();
	
	@Override
	public Pieza getPieza(int r, int c) {
		return viewPieces[r * rows + c];
	}

	@Override
	public void setPiezaFromTablero(int i) {
		viewPieces[i] = getPiezaFromTablero(i);
	}

	@Override
	public void setPiezaCentralFromTablero() {
		viewPieces[posCentral] = getPiezaCentral();
	}

	@Override
	public void setPiezaGris(int i) {
		viewPieces[i] = pieza_gris;
	}

	@Override
	public void setPiezaEmpty(int i) {
		viewPieces[i] = null;
	}

	@Override
	public boolean isEmpty(int i) {
		return viewPieces[i] == null;
	}

	@Override
	public int getColumns() {
		return columns;
	}

	@Override
	public int getRows() {
		return rows;
	}

}
