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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
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
			ResourceBundle properties = readProperties(args[0]);
			SolverFaster.build(
					Long.parseLong(getProperty(properties, "max.ciclos.save_status")),
					Integer.parseInt(getProperty(properties, "min.pos.save.partial")),
					Integer.parseInt(getProperty(properties, "exploration.limit")),
					Integer.parseInt(getProperty(properties, "max.partial.files")),
					Integer.parseInt(getProperty(properties, "target.rollback.pos")),
					Boolean.parseBoolean(getProperty(properties, "ui.show")),
					Boolean.parseBoolean(getProperty(properties, "ui.per.proc")),
					Integer.parseInt(getProperty(properties, "ui.cell.size")),
					Integer.parseInt(getProperty(properties, "ui.refresh.millis")),
					Boolean.parseBoolean(getProperty(properties, "experimental.gif.fair")),
					Boolean.parseBoolean(getProperty(properties, "experimental.borde.left.explorado")),
					Integer.parseInt(getProperty(properties, "task.distribution.pos")));

			properties = null;
			ResourceBundle.clearCache();

			SolverFaster.setupInicial();
			SolverFaster.atacar();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nPrograma terminado.");
	}

	private static ResourceBundle readProperties(String file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ResourceBundle r = new PropertyResourceBundle(fis);
		fis.close();
		return r;
	}

	private static String getProperty(ResourceBundle properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getString(key);
	}
}
