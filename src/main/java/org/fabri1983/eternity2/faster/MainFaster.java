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

package org.fabri1983.eternity2.faster;

import java.util.Properties;
import java.util.ResourceBundle;

import org.fabri1983.eternity2.core.resourcereader.AppPropertiesReader;
import org.fabri1983.eternity2.core.resourcereader.ClassLoaderReaderForFile;

public final class MainFaster
{
	public static void main (String[] args)
	{
		BannerPrinterFaster.printBanner();
        
		try{
			Properties properties = AppPropertiesReader.readProperties();
			
			SolverFaster solver = SolverFaster.build(
					Long.parseLong(getProperty(properties,       AppPropertiesReader.MAX_CICLOS_PRINT_STATS)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.ON_MAX_REACHED_SAVE_STATUS)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.MIN_POS_SAVE_PARTIAL)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.EXPLORATION_LIMIT)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.TARGET_ROLLBACK_POS)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.NUM_TASKS)));

			// vamos a usar tablero gráfico? 
			boolean showUI = Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.UI_SHOW));
			if (showUI) {
				SolverFasterWithUI solverWithUI = SolverFasterWithUI.from(solver);
				int cellPixelsLado = Integer.parseInt(getProperty(properties, AppPropertiesReader.UI_CELL_SIZE));
				int boardRefreshMillis = Integer.parseInt(getProperty(properties, AppPropertiesReader.UI_REFRESH_MILLIS));
				solverWithUI.setupInicial(new ClassLoaderReaderForFile(), cellPixelsLado, boardRefreshMillis);
				ResourceBundle.clearCache();
				solverWithUI.atacar();
			} else {
				solver.setupInicial(new ClassLoaderReaderForFile());
				ResourceBundle.clearCache();
				solver.atacar();
			}
		}
		catch(Exception e) {
			System.out.println(System.lineSeparator() + "ERROR: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(System.lineSeparator() + "Programa terminado.");
	}

	private static String getProperty(Properties properties, String key) {
		return AppPropertiesReader.getProperty(properties, key);
	}
	
}
