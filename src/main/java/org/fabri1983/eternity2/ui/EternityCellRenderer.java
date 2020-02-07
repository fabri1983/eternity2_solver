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

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.fabri1983.eternity2.core.Pieza;

public class EternityCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private Pieza p;
    private Image[] imageListTop, imageListRight, imageListBottom, imageListLeft;
    
    public EternityCellRenderer (int cellSize, int numColors) {
		// acoto el tamaño de celda
    	if (cellSize < 15)
    		cellSize = 15;
    	if (cellSize > 40)
    		cellSize = 40;
    	
        imageListTop = new Image[numColors];
        imageListRight = new Image[numColors];
        imageListBottom = new Image[numColors];
        imageListLeft = new Image[numColors];
        
		// para cada imagen voy haciendo una copia rotada, de esta forma no tengo que rotarla al redibujar
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (int i=0; i < numColors; i++) {
            try {
            	Image img = loadImage(i, classLoader).getScaledInstance(cellSize, cellSize, 0);
                imageListTop[i] = img;
                imageListRight[i] = rotarImg(img, 1, cellSize);
                imageListBottom[i] = rotarImg(img, 2, cellSize);
                imageListLeft[i] = rotarImg(img, 3, cellSize);
            } catch (IOException e) {}
        }
    }

    private Image loadImage (int index, ClassLoader classLoader) throws IOException {
		URL imageUrl = classLoader.getResource("imgs/i" + index + ".png");
        return ImageIO.read(imageUrl);  
    }

    private Image rotarImg (Image img, int rotacion, int cellSize) {
		// creo el buffer que contendrá los pixeles de Img
    	int buffer[] = new int[cellSize * cellSize];
		// objeto que llenará a buffer[] con los pixeles de Img
    	PixelGrabber grabber = new PixelGrabber(img, 0, 0, cellSize, cellSize, buffer, 0, cellSize);
    	//ejecuto el grabado en buffer[]
    	try {
            grabber.grabPixels();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        //voy rotando la imagen siempre 90 grados.
        for (int i=0; i < rotacion; i++) {
            buffer = rotatePixels(buffer, cellSize);
        }
        //creo la imagen a partir de buffer[]
        Image rotImg = createImage(new MemoryImageSource(cellSize, cellSize, buffer, 0, cellSize));
        
        return rotImg;
    }
    
    /**
     * Rota en sentido antihorario los pixeles de un buffer.
     * @param buffer
     * @return int[]
     */
    private int[] rotatePixels (int[] buffer, int cellSize) {
        int rotate[] = new int[cellSize * cellSize];
        for(int x = 0; x < cellSize; x++) {
            for(int y = 0; y < cellSize; y++) {
                rotate[((cellSize-x-1)*cellSize)+y] = buffer[(y*cellSize)+x];
            }
        }
        return rotate;
    }
    
    public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        p = (Pieza)value;
        return this;
    }
    
    public void paint (java.awt.Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        
        //tengo que preguntar constantemente si no es null debido a que al estar corriendo en otro Thread la pieza puede haber sido quitada
    	if (p != null)
            paintImage(g, p.top, 0);
    	if (p != null)
            paintImage(g, p.right, 1);
    	if (p != null)
            paintImage(g, p.bottom, 2);
    	if (p != null)
            paintImage(g, p.left, 3);
    }
    
    private void paintImage (java.awt.Graphics g, int imageIndex, int rot) {
		// NOTA! debido a que la rotación de los pixeles es en sentido antihoraria el siguiente switch tiene
		// modificaciones para left y right
		switch (rot){
    		case 0: g.drawImage(imageListTop[imageIndex], 0, 0, null); break;
    		case 1: g.drawImage(imageListLeft[imageIndex], 0, 0, null); break;
    		case 2: g.drawImage(imageListBottom[imageIndex], 0, 0, null); break;
    		case 3: g.drawImage(imageListRight[imageIndex], 0, 0, null); break;
    		default: break;
		}
    }

}
