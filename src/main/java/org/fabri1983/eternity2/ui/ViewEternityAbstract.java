package org.fabri1983.eternity2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class ViewEternityAbstract extends JFrame implements KeyListener {

	private static final long serialVersionUID = 1L;
	
	private int lado, cell_size, num_colours;
	private boolean running = false;
	private boolean pauseAll = false;
	private boolean pauseGraphic = false;
	private long refresh_milis; // este controla el refresco de las piezas del tablero
	private long prevAccum = 0;
	private static final int COUNT_PERIOD = 5; // periodo de refresco del contador de piezas aplicado a refresh_milis
	private int periodStepping = 1; // ayuda a  COUNT_PERIOD
	private StringBuilder titleRefreshed = new StringBuilder(64);
	private String title = "";
	
	private EternityTable eternityJTable;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JPanel jPanel1 = new JPanel();

    private RefreshThread rt = null;

	public ViewEternityAbstract(long p_refresh_milis, int pLado, int cell_size_pixels, int p_num_colours) {
		super();

		refresh_milis = p_refresh_milis;
    	cell_size = cell_size_pixels;
    	num_colours = p_num_colours;
    	lado = pLado;
        
    	try {
            jbInit();
        } catch (Exception e) {
        	System.out.println("Problem when initializing JPnael and related components.");
            e.printStackTrace();
        }
	}
    
	private void jbInit() throws Exception {
    	
    	this.setSize(new Dimension((lado * cell_size) + 13, (lado * cell_size) + 50));

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
    	
        //creo el canvas y obtengo el estado actual del tablero
        Canvas c = createCanvas(lado, lado);
        eternityJTable.setCanvas(c);
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
    	
    	if (pauseGraphic)
    		return;
    	
//    	eternityJTable.paintImmediately(eternityJTable.getBounds());
        
    	repaint();
    }
    
    protected abstract long getAccum();
    
	protected abstract int getCursorTablero();

	protected abstract int getCursorMasBajo();

	protected abstract int getCursorMasLejano();
    
    protected abstract void shutdownSolver();

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
    	//    currentCount * 1000 / (COUNT_PERIOD*refresh_milis) piezas por seg.
    	
    	if (periodStepping == COUNT_PERIOD) {
    		periodStepping = 0; // lo reseteo 
    		long accum = getAccum() - prevAccum;
			long piezasPerSec =  (accum * 1000) / (COUNT_PERIOD * refresh_milis); // multiplico por 1000 para pasar de millis to seconds
			titleRefreshed.setLength(0);
	    	titleRefreshed.append(title).append(" - (Total)Pcs/sec: ").append(piezasPerSec);
	    	this.setTitle(titleRefreshed.toString());
	    	titleRefreshed.setLength(0);
	    	prevAccum += accum;
	    }
    	else
    		++periodStepping;
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
	                // suspend this thread for a given time
	                LockSupport.parkNanos(this.refresh_nanos);
            	}
            	LockSupport.park();
            }
        }
    }
    
}
