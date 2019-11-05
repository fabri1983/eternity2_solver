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

package org.fabri1983.eternity2.mpje;

import java.util.Properties;
import java.util.ResourceBundle;

import org.fabri1983.eternity2.core.resourcereader.AppPropertiesReader;

import mpi.MPI;

public final class MainFasterMPJE
{
	public static void main (String[] args) throws Exception
	{
		MPI.Init(args);
		
		int rank = MPI.COMM_WORLD.Rank();
		
		// imprimo una sola vez la portada
		if (rank == 0) {
			BannerPrinterMPJE.printBanner();
		}
		
		try {
			Properties properties = AppPropertiesReader.readProperties();

			// NOTA:
			//   para mpje multicore los primeros 3 parametros son para MPI
			//   para mpje cluster los primeros 8 parametros son para MPI
			
			SolverFasterMPJE sol = new SolverFasterMPJE(
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
					MPI.COMM_WORLD.Size());

			properties = null;
			ResourceBundle.clearCache();

			sol.setupInicial(); // ejecuto una inicializacion global
			sol.atacar();
			
		} catch(Exception e){
			System.out.println(System.lineSeparator() + "Rank " + rank + ": Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(System.lineSeparator() + "Rank " + rank + ": Programa terminado.");
		
		MPI.Finalize();
	}

	private static String getProperty(Properties properties, String key) {
		return AppPropertiesReader.getProperty(properties, key);
	}
	
}