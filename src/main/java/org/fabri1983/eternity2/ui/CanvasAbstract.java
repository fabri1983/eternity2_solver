package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Consts;
import org.fabri1983.eternity2.core.neighbors.Neighbors;

public abstract class CanvasAbstract implements Canvas {

	public final static int BEACON_CURSOR_VALUE = -1;
	
	private int columns;
	private int rows;
	private Integer pieza_gris;
	private Integer pieza_beacon;
	
	public CanvasAbstract(int columns, int rows) {
		this.columns = columns;
		this.rows = rows;
		this.pieza_gris = Neighbors.asMergedInfo(Consts.GRIS, Consts.GRIS, Consts.GRIS, Consts.GRIS, (short)0);
		this.pieza_beacon = Neighbors.asMergedInfo((byte)0, (byte)0, (byte)0, (byte)0, (short)0);
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
	public Integer getPiezaInfoBeacon() {
		return pieza_beacon;
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
