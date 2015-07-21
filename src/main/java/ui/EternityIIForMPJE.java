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

package ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class EternityIIForMPJE {

	public ViewEternityForMPJE frame; //es la ventanita
	
    public EternityIIForMPJE(int lado, int cell_size_pixels, int num_colours, long refreshMillis, int proc) {
    	
        frame = new ViewEternityForMPJE(refreshMillis, lado, cell_size_pixels, num_colours);
        frame.setVisible(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setTitle("("+ proc + ") E2Solver MPJe");
        
        int numProcessors = Runtime.getRuntime().availableProcessors();
		setLocation(numProcessors, proc % numProcessors);
    }
    
	private void setLocation(int numProcesors, int procId) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize(); // returns a copy
        
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        
		// if only one processor then locate the frame in the middle of the canvas
    	if (numProcesors == 1)
    		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		// locate this frame in a tiled fashion according its id
    	else {
			int maxCols = 4;
			int maxRows = 2;
			int xPos = (procId % maxCols) * frameSize.width;
			int yPos = ((procId / maxCols) % maxRows) * frameSize.height;

			if (xPos + frameSize.width > screenSize.width)
				xPos -= (xPos + frameSize.width) - screenSize.width;
			if (yPos + frameSize.height > screenSize.height)
				yPos -= (xPos + frameSize.height) - screenSize.height;
			frame.setLocation(xPos, yPos);
    	}
    		
	}
    
    /**
     * Se encarga de inicializar el canvas y de mostrar el estado actual del tablero.
     */
    public void startPainting() {
    	frame.setVisible(true);
    	frame.run();
    }

}