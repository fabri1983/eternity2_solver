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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class EternityII {

	private ViewEternity frame; // es la ventanita

	public EternityII(ViewEternityFactory viewFactory) {
		// lo siguiente es solamente para el tablero gráfico
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		frame = viewFactory.create();
		setupFrame(viewFactory.getProc(), viewFactory.getTotalProcs());
	}

	/**
	 * Se encarga de inicializar el canvas y de mostrar el estado actual del tablero.
	 */
	public void startPainting() {
		frame.setVisible(true);
		frame.run();
	}

	private void setupFrame(int proc, int totalProcs) {
		frame.setVisible(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setTitle("(" + proc + ") E2Solver");

		setLocation(totalProcs, proc % totalProcs);
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

}
