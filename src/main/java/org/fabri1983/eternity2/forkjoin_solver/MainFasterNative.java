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
import java.io.InputStream;
import java.util.Properties;

import org.fabri1983.eternity2.core.tilesreader.ClassLoaderReaderForTilesFile;

public final class MainFasterNative
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
		System.out.println("         Copyright(c) 2019 Fabricio Lettieri (fabri1983@gmail.com)");
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println();
        
		try{
			Properties properties = readProperties();
			SolverFaster solver = SolverFaster.build(
					Long.parseLong(getProperty(properties, "max.ciclos.save_status")),
					Integer.parseInt(getProperty(properties, "min.pos.save.partial")),
					Integer.parseInt(getProperty(properties, "exploration.limit")),
					Integer.parseInt(getProperty(properties, "max.partial.files")),
					Integer.parseInt(getProperty(properties, "target.rollback.pos")),
					false, // ui.show
					false, // ui.per.proc
					0, // ui.cell.size
					0, // ui.refresh.millis
					Boolean.parseBoolean(getProperty(properties, "experimental.gif.fair")),
					Boolean.parseBoolean(getProperty(properties, "experimental.borde.left.explorado")),
					Integer.parseInt(getProperty(properties, "task.distribution.pos")),
					new ClassLoaderReaderForTilesFile(), // the FileReaderForTilesFile() doesn't work in native mode :(
					Integer.parseInt(getProperty(properties, "forkjoin.num.processes")));

			properties = null;

			solver.setupInicial();
			solver.atacarNative();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nPrograma terminado.");
	}

	/**
	 * Directly read the properties file from disk instead of using ClassLoader.getResourceAsStream().
	 * 
	 * @return
	 * @throws IOException
	 */
	private static Properties readProperties() throws IOException {
		Properties properties = new Properties();
		String file = "application.properties";
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			properties.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	private static String getProperty(Properties properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getProperty(key);
	}
}
