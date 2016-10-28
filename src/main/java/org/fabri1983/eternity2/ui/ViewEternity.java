/**
 * Copyright (c) 2015 Fabricio Lettieri fabri1983@gmail.com
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

package org.fabri1983.eternity2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fabri1983.eternity2.core.Pieza;
import org.fabri1983.eternity2.forkjoin_solver.ExploracionAction;
import org.fabri1983.eternity2.forkjoin_solver.SolverFaster;

public class ViewEternity extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;
	
	private static int LADO, LADO_SHIFT_FOR_DIV, cell_size, num_colours;
	private int last_superior;
	private boolean primera_vez = true;
	private Pieza pieza_gris;
	private boolean running = false;
	private boolean pauseAll = false;
	private boolean pauseGraphic = false;
	private long refresh_milis; // este controla el refresco de las piezas del tablero
	private long prevAccum = 0;
	private static final int COUNT_PERIOD = 5; // periodo de refresco del contador de piezas aplicado a refresh_milis
	private int periodStepping = 1; // ayuda a  COUNT_PERIOD
	private StringBuilder titleRefreshed = new StringBuilder(128);
	private String title = "";
	private static final String titleAdd = " - (Total)Pcs/sec: ";
	
	private EternityTable jTable1;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JPanel jPanel1 = new JPanel();

    private ExploracionAction action;
    private RefreshThread rt = null;

    public ViewEternity(long p_refresh_milis, int pLado, int cell_size_pixels, int p_num_colours, ExploracionAction _action) {
    	
    	refresh_milis = p_refresh_milis;
    	cell_size = cell_size_pixels;
    	num_colours = p_num_colours;
    	LADO = pLado;
		LADO_SHIFT_FOR_DIV = (int) (Math.log10(LADO) / Math.log10(2)); // siempre y cuando LADO sea potencia de 2
    	byte cero = 0;
    	pieza_gris = new Pieza(cero,cero,cero,cero,cero);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        action = _action;
    }

    private void jbInit() throws Exception {
    	
    	this.setSize(new Dimension((LADO * cell_size) + 13, (LADO * cell_size) + 50));

		jTable1 = new EternityTable(cell_size, num_colours);
        jTable1.addKeyListener(this);
        
        jPanel1.setMaximumSize(new Dimension(0, 32767));
        jPanel1.setMinimumSize(new Dimension(0, 100));
        jPanel1.setPreferredSize(new Dimension(0, 100));
        jPanel1.setLayout(null);

		jScrollPane1.setSize(new Dimension(LADO * cell_size, LADO * cell_size));
		jScrollPane1.getViewport().add(jTable1, null);

		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    }
    
    /**
     * Inicializa el canvas y muestra el estado actual del tablero.
     */
    public void run() {
    	
        //creo el canvas y obtengo el estado actual del tablero
        EternityCanvas c = new EternityCanvas(LADO, LADO);
        jTable1.setCanvas(c);
        running = true;
        pauseAll = false;
        title = this.getTitle();
        rt = new RefreshThread(this, refresh_milis);
        rt.start();
    }
    
    /**
     * Actualiza el tablero en pantalla.
     */
    public void refresh() {
    	
    	if (running == false)
    		return;
    	
    	updateTablero();
    	
        jTable1.paintImmediately(jTable1.getBounds());
        
        repaint();
    }
    
    /**
     * Actualiza el tablero a dibujar en pantalla.
     */
    private void updateTablero () {
    	
    	// Calculo piezas por segundo. 
    	// Dado que este metodo se invoca refresh_milis/1000 veces por seg, entonces estaría procesando: 
    	//    currentCount * 1 / (refresh_milis/1000) piezas por seg.
    	// Pero actualmente calculo la cantidad de piezas procesadas cada COUNT_PERIOD*refresh_milis/1000 segs
    	// entonces estaría procesando:
    	//    currentCount * 1 / (COUNT_PERIOD*refresh_milis/1000) piezas por seg.
    	
    	if (periodStepping == COUNT_PERIOD) {
    		periodStepping = 0; // lo reseteo 
    		long accum = 0;
			for (int i = SolverFaster.count_cycles.length - 1; i >= 0; --i)
				accum += SolverFaster.count_cycles[i]; // SolverFaster.count_cycles son acumuladores de piezas procesadas
			accum -= prevAccum;
			long piezasPerSeg =  (long) (accum * 1.0 / (COUNT_PERIOD * refresh_milis / 1000.0));
	    	titleRefreshed.delete(0, titleRefreshed.length());
	    	titleRefreshed.append(title).append(titleAdd).append(piezasPerSeg);
	    	this.setTitle(titleRefreshed.toString());
	    	prevAccum += accum;
	    }
    	else
    		++periodStepping;

    	// Actualizo el tablero si no se ha pausado
    	if (pauseGraphic)
    		return;
    	
    	int cursor = action.cursor - 1;
    	int inferior = primera_vez? action.mas_bajo : 0;
    	int superior = Math.max(cursor, action.mas_lejano_parcial_max);

    	//seteo las piezas desde cursor hasta inferior
    	for (int i=cursor; i >= inferior; --i){
    		//jTable1.canvas.viewPieces[i / LADO][i % LADO] = action.tablero[i];
    		// better performance for power of 2:
    		jTable1.canvas.viewPieces[i >> LADO_SHIFT_FOR_DIV][i & (LADO-1)] = action.tablero[i];
    	}
    	
    	//indico la posición de la pieza mas lejana
    	if (primera_vez)
    		last_superior = superior;
    	//jTable1.canvas.viewPieces[last_superior / LADO][last_superior % LADO] = null;
    	//jTable1.canvas.viewPieces[superior / LADO][superior % LADO] = pieza_gris;
    	// better performance for power of 2:
    	jTable1.canvas.viewPieces[last_superior >> LADO_SHIFT_FOR_DIV][last_superior & (LADO-1)] = null;
    	jTable1.canvas.viewPieces[superior >> LADO_SHIFT_FOR_DIV][superior & (LADO-1)] = pieza_gris; 	
    	
    	//seteo null desde cursor hacia adelante hasta la primer pieza de ViewPice null
    	for (int i=cursor+1; i < superior; ++i){
    		//int r = i / LADO, c = i % LADO;
    		// better performance for power of 2:
    		int r = i >> LADO_SHIFT_FOR_DIV, c = i & (LADO-1);
    		if (jTable1.canvas.viewPieces[r][c] == null)
    			break;
    		jTable1.canvas.viewPieces[r][c] = null;
    	}
    	
    	//seteo la pieza central
    	jTable1.canvas.viewPieces[SolverFaster.POS_FILA_P_CENTRAL][SolverFaster.POS_COL_P_CENTRAL] = action.piezas[SolverFaster.INDICE_P_CENTRAL];
    	
    	last_superior = superior;
    	primera_vez = false;
    }

	@Override
	public void keyPressed(KeyEvent arg0) {
		
		switch (arg0.getKeyCode()) {
		
			case KeyEvent.VK_P: {
				this.pauseAll = !this.pauseAll;
				if (pauseAll)
					; // the park functionality is in the while-loop at the refresh thread
				else
					LockSupport.unpark(rt);
				break;
			}
			case KeyEvent.VK_SPACE: {
				pauseGraphic = !pauseGraphic;
				break;
			}
			case KeyEvent.VK_UP: {
				refresh_milis += 25;
				if (refresh_milis > 1000)
					refresh_milis = 1000;
				rt.refresh_nanos = refresh_milis * 1000 * 1000;
				break;
			}
			case KeyEvent.VK_DOWN: {
				refresh_milis -= 25;
				if (refresh_milis <= 10)
					refresh_milis = 10;
				rt.refresh_nanos = refresh_milis * 1000 * 1000;
				break;
			}
			case KeyEvent.VK_ESCAPE: {
				this.pauseAll = false;
				this.running = false;
				System.exit(0);
				break;
			}
			default: break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
	
	/**
	 * Private inner class to refresh the board.
	 * @author user
	 *
	 */
    private class RefreshThread extends Thread {
        
        private ViewEternity viewEternity;
        public long refresh_nanos;
        
        RefreshThread(ViewEternity viewEternity, long p_refresh_milis) {
            this.viewEternity = viewEternity;
            this.refresh_nanos = p_refresh_milis * 1000000;
        }
    
        public void run() {
        	// this loop won't have a lock loop effect since we're using parking pattern
            while(viewEternity.running) {
            	while (!viewEternity.pauseAll) {
	                viewEternity.refresh();
	                // suspend this thread for a given time
	                LockSupport.parkNanos(this.refresh_nanos);
            	}
            	LockSupport.park();
            }
        }
    }

}