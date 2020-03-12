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

import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.fabri1983.eternity2.core.neighbors.Neighbors;

public class EternityCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private Integer pinfo;
    private Image[] imagesTopOriented, imagesRightOriented, imagesBottomOriented, imagesLeftOriented;
    
    public EternityCellRenderer (int cellSize, int numColors) {
		// acoto el tamaño de celda
    	if (cellSize < 15)
    		cellSize = 15;
    	if (cellSize > 40)
    		cellSize = 40;
    	
        imagesTopOriented = new Image[numColors];
        imagesRightOriented = new Image[numColors];
        imagesBottomOriented = new Image[numColors];
        imagesLeftOriented = new Image[numColors];
        
		// para cada imagen voy haciendo una copia rotada, de esta forma no tengo que rotarla al redibujar
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (int i=0; i < numColors; i++) {
            try {
            	Image img = loadImage(i, classLoader).getScaledInstance(cellSize, cellSize, 0);
                imagesTopOriented[i] = img;
                imagesRightOriented[i] = rotarImg(img, 1, cellSize);
                imagesBottomOriented[i] = rotarImg(img, 2, cellSize);
                imagesLeftOriented[i] = rotarImg(img, 3, cellSize);
            } catch (IOException e) {}
        }
    }

    private Image loadImage (int index, ClassLoader classLoader) throws IOException {
		URL imageUrl = classLoader.getResource("imgs/i" + index + ".png");
        return ImageIO.read(imageUrl);  
    }

    private Image rotarImg (Image img, int rotacion, int cellSize) {
		// creo el buffer que contendrá los pixeles de Img
		int piexelBuffer[] = new int[cellSize * cellSize];
		// objeto que llenará a buffer[] con los pixeles de Img
		PixelGrabber grabber = new PixelGrabber(img, 0, 0, cellSize, cellSize, piexelBuffer, 0, cellSize);
		// ejecuto el grabado en buffer[]
		try {
			grabber.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// voy rotando la imagen siempre 90 grados antihorario
		for (int i = 0; i < rotacion; i++) {
			rotate2DArrayInPlace(piexelBuffer, cellSize);
		}
		// creo la imagen a partir de buffer[]
		Image rotImg = createImage(new MemoryImageSource(cellSize, cellSize, piexelBuffer, 0, cellSize));

		return rotImg;
    }
    
    /**
     * Rota 90 grados en sentido antihorario los pixeles de un buffer.
     */
	private void rotate2DArrayInPlace(int[] pixels, int N) {
		// Consider all squares one by one
		for (int x = 0; x < N / 2; x++) {
			// Consider elements in group of 4 in current square
			for (int y = x; y < N - x - 1; y++) {
				// store current cell in temp variable
				int temp = pixels[(x * N) + y];
				// move values from right to top
				pixels[(x * N) + y] = pixels[(y * N) + N - 1 - x];
				// move values from bottom to right
				pixels[(y * N) + N - 1 - x] = pixels[(N * (N - 1 - x)) + N - 1 - y];
				// move values from left to bottom
				pixels[(N * (N - 1 - x)) + N - 1 - y] = pixels[(N * (N - 1 - y)) + x];
				// assign temp to left
				pixels[(N * (N - 1 - y)) + x] = temp;
			}
		}
	}
    
    @Override
    public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, 
    		boolean hasFocus, int row, int column) {
    	pinfo = (Integer)value;
        return this;
    }
    
    @Override
	public void paint(java.awt.Graphics g) {
		super.paint(g);
		
		int mergedInfo = pinfo.intValue();
		
		int top = Neighbors.top(mergedInfo);
		int right = Neighbors.right(mergedInfo);
		int bottom = Neighbors.bottom(mergedInfo);
		int left = Neighbors.left(mergedInfo);

		// NOTE: la rotación de los pixeles es antihoraria, that's why right and left are inverted
		g.drawImage(imagesTopOriented[top], 0, 0, null);
		g.drawImage(imagesLeftOriented[right], 0, 0, null);
		g.drawImage(imagesBottomOriented[bottom], 0, 0, null);
		g.drawImage(imagesRightOriented[left], 0, 0, null);
	}

}
