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
    private static int CELL_SIZE, NUM_COLOURS;
    
    public EternityCellRenderer (int p_cell_size, int p_num_colours) {
    	CELL_SIZE = p_cell_size;
		// acoto el tamaño de celda
    	if (CELL_SIZE < 15)
    		CELL_SIZE = 15;
    	if (CELL_SIZE > 40)
    		CELL_SIZE = 40;
    	NUM_COLOURS = p_num_colours;
        imageListTop = new Image[NUM_COLOURS];
        imageListRight = new Image[NUM_COLOURS];
        imageListBottom = new Image[NUM_COLOURS];
        imageListLeft = new Image[NUM_COLOURS];
        
		// para cada imagen voy haciendo una copia rotada, de esta forma no tengo que rotarla al redibujar
        for (int i=0; i < NUM_COLOURS; i++) {
            try {
            	Image img = loadImage(i).getScaledInstance(CELL_SIZE, CELL_SIZE, 0);
                imageListTop[i] = img;
                imageListRight[i] = rotarImg(img, 1);
                imageListBottom[i] = rotarImg(img, 2);
                imageListLeft[i] = rotarImg(img, 3);
            } catch (IOException e) {}
        }
    }

    private Image loadImage (int index) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL imageUrl = classLoader.getResource("imgs/i" + index + ".png");
        return ImageIO.read(imageUrl);  
    }

    private Image rotarImg (Image img, int rotacion) {
		// creo el buffer que contendrá los pixeles de Img
    	int buffer[] = new int[CELL_SIZE * CELL_SIZE];
		// objeto que llenará a buffer[] con los pixeles de Img
    	PixelGrabber grabber = new PixelGrabber(img, 0, 0, CELL_SIZE, CELL_SIZE, buffer, 0, CELL_SIZE);
    	//ejecuto el grabado en buffer[]
    	try {
            grabber.grabPixels();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        //voy rotando la imagen siempre 90 grados.
        for (int i=0; i < rotacion; i++) {
            buffer = rotatePixels(buffer);
        }
        //creo la imagen a partir de buffer[]
        Image rotImg = createImage(new MemoryImageSource(CELL_SIZE, CELL_SIZE, buffer, 0, CELL_SIZE));
        
        return rotImg;
    }
    
    /**
     * Rota en sentido antihorario los pixeles de un buffer.
     * @param buffer
     * @return int[]
     */
    private int[] rotatePixels (int[] buffer) {
        int rotate[] = new int[CELL_SIZE * CELL_SIZE];
        for(int x = 0; x < CELL_SIZE; x++) {
            for(int y = 0; y < CELL_SIZE; y++) {
                rotate[((CELL_SIZE-x-1)*CELL_SIZE)+y] = buffer[(y*CELL_SIZE)+x];
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
        
        //tengo que preguntar constantemente si no es null porque sino arroja NullPointerException
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
