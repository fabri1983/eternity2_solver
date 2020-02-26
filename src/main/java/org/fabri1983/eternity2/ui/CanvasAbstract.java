package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.NodoPosibles;

public abstract class CanvasAbstract implements Canvas {

	private int columns;
	private int rows;
	private Integer pieza_gris;

	public CanvasAbstract(int columns, int rows, int posCentral) {
		this.columns = columns;
		this.rows = rows;
		this.pieza_gris = NodoPosibles.asMergedInfo(Consts.GRIS, Consts.GRIS, Consts.GRIS, Consts.GRIS, (short)0);
	}

	protected abstract Integer getPiezaInfoFromTablero(int cursorTablero);
	
	@Override
	public Integer getPiezaInfo(int r, int c) {
		return getPiezaInfoFromTablero(r * rows + c);
	}

	@Override
	public Integer getPiezaInfoGris() {
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
