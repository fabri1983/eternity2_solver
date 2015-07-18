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

import javax.swing.JTable;

import javax.swing.table.DefaultTableColumnModel;

public class EternityTable extends JTable {

	private static final long serialVersionUID = 1L;
	public EternityCanvas canvas;
    private EternityModel eternityModel;
    private EternityCellRenderer cellRender;
    private static int cell_size, num_colours;
    
    public EternityTable(int p_cell_size, int p_num_colours) {
    	cell_size = p_cell_size;
    	num_colours = p_num_colours;
    }
    
    public void setCanvas(EternityCanvas canvas) {
        this.canvas = canvas;
        eternityModel = new EternityModel(canvas);
        cellRender = new EternityCellRenderer(cell_size, num_colours);
        
        setModel(eternityModel);
        DefaultTableColumnModel tm = (DefaultTableColumnModel)getColumnModel();
        for (int i = 0; i < getModel().getColumnCount(); i++) {
            tm.getColumn(i).setCellRenderer(cellRender);
            tm.getColumn(i).setWidth(cell_size);
            tm.getColumn(i).setMaxWidth(cell_size);
            tm.getColumn(i).setMinWidth(cell_size);
        }
        setRowHeight(cell_size);
        setSize(canvas.getColumns()*cell_size, canvas.getRows()*cell_size);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public EternityCanvas getCanvas() {
        return canvas;
    }
}