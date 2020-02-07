package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Pieza;

public interface Canvas {

	Pieza getPieza(int r, int c);
	
	Pieza getPiezaGris();
	
	int getColumns();

	int getRows();

}
