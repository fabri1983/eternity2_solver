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
        
        setLocation(Runtime.getRuntime().availableProcessors());
    }
    
    private void setLocation(int numProcesors) {
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        
        // if the number of process is 1 then locate the frame in the middle of the canvas
    	if (numProcesors == 1)
    		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    	// locate this frame in a particular position
    	else {
    		int middleWidth = (screenSize.width - frameSize.width) / 2;
    		int middleHeight = (screenSize.height - frameSize.height) / 2;
    		frame.setLocation( 
    				middleWidth + (int)((Math.random() * middleWidth) * ( ((Math.random() * 2) > 1) ? 1 : -1)), 
    				middleHeight + (int)((Math.random() * middleHeight) * ( ((Math.random() * 2) > 1) ? 1 : -1))
    				);
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