package org.fabri1983.eternity2.ui;

import org.fabri1983.eternity2.core.Pieza;

public interface Canvas {

	Pieza getPieza(int r, int c);

	void setPiezaFromTablero(int i);
	
	void setPiezaCentralFromTablero();

	void setPiezaGris(int i);
	
	void setPiezaEmpty(int i);
	
	boolean isEmpty(int i);
	
	int getColumns();

	int getRows();


}
