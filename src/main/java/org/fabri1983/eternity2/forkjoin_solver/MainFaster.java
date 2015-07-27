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

package org.fabri1983.eternity2.forkjoin_solver;

import java.util.ResourceBundle;

import javax.swing.UIManager;

public final class MainFaster
{
	/**
	 * @param args
	 */
	public static void main (String[] args)
	{
		System.out.println("################################################################################");
		System.out.println("##- Uso de fork-join para distribución de tareas.                            -##");
		System.out.println("##- Version con Estructura 4-dimensional, Smart-Podas y Contornos de colores -##");
		System.out.println("##- Micro optimizaciones.                                                    -##");
		System.out.println("################################################################################");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("         Copyright(c) 2015 Fabricio Lettieri (fabri1983@gmail.com)");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println();

		// lo siguiente es solamente para el tablero gráfico
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		try{
			ResourceBundle properties = ResourceBundle.getBundle(args[0].toLowerCase());
			SolverFaster.build(
					Long.parseLong(properties.getString("max.ciclos.save_status")),
					Integer.parseInt(properties.getString("min.pos.save.partial")),
					Integer.parseInt(properties.getString("exploration.limit")),
					Integer.parseInt(properties.getString("max.partial.files")),
					Integer.parseInt(properties.getString("target.rollback.pos")),
					Boolean.parseBoolean(properties.getString("ui.show")),
					Boolean.parseBoolean(properties.getString("ui.per.proc")),
					Integer.parseInt(properties.getString("ui.cell.size")),
					Integer.parseInt(properties.getString("ui.refresh.millis")),
					Boolean.parseBoolean(properties.getString("experimental.fair")),
					Boolean.parseBoolean(properties.getString("experimental.borde.left.explorado")),
					Integer.parseInt(properties.getString("task.distribution.pos")));
			SolverFaster.setupInicial();
			SolverFaster.atacar();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nPrograma terminado.");
	}
}
