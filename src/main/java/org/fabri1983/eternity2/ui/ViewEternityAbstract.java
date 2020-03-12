/**
 * Copyright (c) 2019 Fabricio Lettieri fabri1983@gmail.com
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
import java.text.NumberFormat;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class ViewEternityAbstract extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;
	
	private int lado;
	private boolean running = false;
	private boolean pauseAll = false;
	private boolean pauseGraphic = false;
	private long refresh_milis; // este controla el refresco de las piezas del tablero
	private long prevAccum = 0;
	private long prevTimeNanos;
	private static final int COUNT_PERIOD = 5; // periodo de refresco del contador de piezas aplicado a refresh_milis
	private int periodStepping = 1; // ayuda a  COUNT_PERIOD
	private String baseTitle = "";
	private NumberFormat myFormat = NumberFormat.getInstance();
    
	private EternityTable eternityJTable;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JPanel jPanel1 = new JPanel();

    private RefreshThread rt = null;

	public ViewEternityAbstract(long p_refresh_milis, int pLado, int cell_size_pixels, int p_num_colours) {
		super();

		refresh_milis = p_refresh_milis;
    	lado = pLado;
    	myFormat.setGroupingUsed(true);
    	
    	try {
            jbInit(pLado, cell_size_pixels, p_num_colours);
        } catch (Exception e) {
        	System.out.println("Problem when initializing JPnael and related components.");
            e.printStackTrace();
        }
	}
    
	private void jbInit(int lado, int cell_size, int num_colours) throws Exception {
    	
    	this.setSize(new Dimension((lado * cell_size) + 18, (lado * cell_size) + 56));

        eternityJTable = new EternityTable(cell_size, num_colours);
        eternityJTable.addKeyListener(this);
        
        jPanel1.setMaximumSize(new Dimension(0, 32767));
        jPanel1.setMinimumSize(new Dimension(0, 100));
        jPanel1.setPreferredSize(new Dimension(0, 100));
        jPanel1.setLayout(null);

		jScrollPane1.setSize(new Dimension(lado * cell_size, lado * cell_size));
		jScrollPane1.getViewport().add(eternityJTable, null);

		this.getContentPane().add(jPanel1, BorderLayout.CENTER);
		this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    }
    
    protected abstract Canvas createCanvas(int rows, int cols);

	/**
     * Inicializa el canvas y muestra el estado actual del tablero.
     */
    public void run() {
    	
		// creo el canvas y obtengo el estado actual del tablero
		Canvas c = createCanvas(lado, lado);
		eternityJTable.setCanvas(c);
		
		baseTitle = this.getTitle();
		running = true;
		pauseAll = false;
		rt = new RefreshThread(this, refresh_milis);
		
		// justo antes de empezar a dibujar obtengo el acumulado de cycles hasta ahora
		prevAccum = getAccum();
		prevTimeNanos = System.nanoTime();
		
		rt.start();
    }
    
    /**
     * Actualiza el tablero en pantalla.
     */
    public void refresh() {
    	
    	if (running == false)
    		return;
    	
    	updateCounter();
    	
    	if (pauseGraphic)
    		return;
    	
//    	eternityJTable.paintImmediately(eternityJTable.getBounds());
        
    	repaint();
    }
    
    protected abstract long getAccum();
    
    protected abstract void shutdownSolver();

    private void updateCounter () {
    	
    	if (periodStepping == COUNT_PERIOD) {
    		long timeLapsedNanos = System.nanoTime() - prevTimeNanos;
    		long accum = getAccum() - prevAccum;
			long piezasPerSec = accum * 1000000000 / timeLapsedNanos;
			String titleRefreshed = baseTitle + " - Total tiles/sec: " + myFormat.format(piezasPerSec);
	    	setTitle(titleRefreshed);
	    	prevAccum += accum;
	    	periodStepping = 1;
	    	prevTimeNanos = System.nanoTime();
	    }
    	else
    		++periodStepping;
    }
    
    @Override
	public void keyPressed(KeyEvent arg0) {
		
		switch (arg0.getKeyCode()) {
		
			case KeyEvent.VK_P: {
				pauseAll = !pauseAll;
				if (pauseAll)
					; // the park functionality is in the while-loop at the refresh thread
				else {
					periodStepping = 1;
					LockSupport.unpark(rt);
				}
				break;
			}
			case KeyEvent.VK_SPACE: {
				pauseGraphic = !pauseGraphic;
				break;
			}
			case KeyEvent.VK_UP: {
				refresh_milis += 50;
				if (refresh_milis > 1000)
					refresh_milis = 1000;
				rt.refresh_nanos = refresh_milis * 1000000;
				break;
			}
			case KeyEvent.VK_DOWN: {
				refresh_milis -= 50;
				if (refresh_milis <= 10)
					refresh_milis = 10;
				rt.refresh_nanos = refresh_milis * 1000000;
				break;
			}
			case KeyEvent.VK_ESCAPE: {
				pauseAll = false;
				running = false;
				shutdownSolver();
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
        
        private ViewEternityAbstract viewEternity;
        public long refresh_nanos;
        
        RefreshThread(ViewEternityAbstract viewEternity, long p_refresh_milis) {
            this.viewEternity = viewEternity;
            this.refresh_nanos = p_refresh_milis * 1000000;
        }
    
        @Override
        public void run() {
        	// this loop won't have a lock loop effect since we're using parking pattern
            while(viewEternity.running) {
            	while (!viewEternity.pauseAll) {
	                viewEternity.refresh();
	                // park this thread for a certain amount of time
	                LockSupport.parkNanos(this.refresh_nanos);
            	}
            	LockSupport.park();
            }
        }
    }
    
}
