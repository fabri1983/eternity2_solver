package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.core.PiezaFactory;

public abstract class CanvasAbstract implements Canvas {

	private int columns;
	private int rows;
	private Pieza pieza_gris;

	public CanvasAbstract(int columns, int rows, int posCentral) {
		this.columns = columns;
		this.rows = rows;
		this.pieza_gris = PiezaFactory.dummy();
	}

	protected abstract Pieza getPiezaFromTablero(int cursorTablero);
	
	protected abstract Pieza getPiezaCentral();
	
	@Override
	public Pieza getPieza(int r, int c) {
		return getPiezaFromTablero(r * rows + c);
	}

	@Override
	public Pieza getPiezaGris() {
		return pieza_gris;
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
