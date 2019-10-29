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

package org.fabri1983.eternity2.forkjoin_solver;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.fabri1983.eternity2.core.tilesreader.ClassLoaderReaderForTilesFile;

public final class MainFaster
{
	/**
	 * @param args
	 */
	public static void main (String[] args)
	{
		printBanner();
        
		try{
			Properties properties = readProperties();
			SolverFaster solver = SolverFaster.build(
					Long.parseLong(getProperty(properties,       "max.ciclos.save_status")),
					Integer.parseInt(getProperty(properties,     "min.pos.save.partial")),
					Integer.parseInt(getProperty(properties,     "exploration.limit")),
					Integer.parseInt(getProperty(properties,     "max.partial.files")),
					Integer.parseInt(getProperty(properties,     "target.rollback.pos")),
					Boolean.parseBoolean(getProperty(properties, "ui.show")),
					Boolean.parseBoolean(getProperty(properties, "ui.per.proc")),
					Integer.parseInt(getProperty(properties,     "ui.cell.size")),
					Integer.parseInt(getProperty(properties,     "ui.refresh.millis")),
					Boolean.parseBoolean(getProperty(properties, "experimental.gif.fair")),
					Boolean.parseBoolean(getProperty(properties, "experimental.borde.left.explorado")),
					Integer.parseInt(getProperty(properties,     "task.distribution.pos")),
					new ClassLoaderReaderForTilesFile(),
					Integer.parseInt(getProperty(properties,     "forkjoin.num.processes")));

			properties = null;
			ResourceBundle.clearCache();

			// vamos a usar tablero gráfico? 
			if (SolverFaster.usarTableroGrafico && !SolverFaster.flag_retroceder_externo) {
				SolverFasterWithUI solverWithUI = SolverFasterWithUI.from(solver);
				solverWithUI.setupInicial();
				solverWithUI.atacar(0);
			} else {
				solver.setupInicial();
				solver.atacar(0);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nPrograma terminado.");
	}

	private static void printBanner() {
		StringBuilder msgBuilder = new StringBuilder(64*9);
		String lineSeparator = System.lineSeparator();
		msgBuilder.append("############################################################").append(lineSeparator);
		msgBuilder.append("##- Uso de fork-join para distribución de tareas.        -##").append(lineSeparator);
		msgBuilder.append("##- Version con Estructura 4-dimensional, Smart-Podas y  -##").append(lineSeparator);
		msgBuilder.append("##- Contornos de colores pre calculados.                 -##").append(lineSeparator);
		msgBuilder.append("##- Micro optimizaciones.                                -##").append(lineSeparator);
		msgBuilder.append("############################################################").append(lineSeparator);
		msgBuilder.append("------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append(" Copyright(c) 2019 Fabricio Lettieri (fabri1983@gmail.com)  ").append(lineSeparator);
		msgBuilder.append("------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append(lineSeparator);
		System.out.println(msgBuilder.toString());
	}
	
	private static final Properties readProperties() throws IOException {
		Properties properties = new Properties();
		String file = "application.properties";
		properties.load(MainFaster.class.getClassLoader().getResourceAsStream(file));
		return properties;
	}

	private static final String getProperty(Properties properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getProperty(key);
	}
}
