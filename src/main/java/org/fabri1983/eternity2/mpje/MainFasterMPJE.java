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
import org.fabri1983.eternity2.core.resourcereader.ClassLoaderReaderForFile;

import mpi.MPI;

public final class MainFasterMPJE
{
	public static void main (String[] args) throws Exception
	{
		MPI.Init(args);
		
		int rank = MPI.COMM_WORLD.Rank();
		
		// TODO fix this to be printed once in every machine in the cluster, 
		// taking into account that rank == 0 is only one machine in the cluster (I think so).
		// imprimo una sola vez la portada
		if (rank == 0) {
			BannerPrinterMPJE.printBanner();
		}
		
		try {
			Properties properties = AppPropertiesReader.readProperties();

			int numProcsCluster = MPI.COMM_WORLD.Size();
			
			// NOTA:
			//   para mpje multicore los primeros 3 parametros de args[] son para MPI
			//   para mpje cluster los primeros 8 parametros de args[] son para MPI
			
			SolverFasterMPJE sol = new SolverFasterMPJE(
					Long.parseLong(getProperty(properties,       AppPropertiesReader.MAX_CICLOS_PRINT_STATS)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.ON_MAX_REACHED_SAVE_STATUS)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.MIN_POS_SAVE_PARTIAL)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.EXPLORATION_LIMIT)),
					Short.parseShort(getProperty(properties,     AppPropertiesReader.TARGET_ROLLBACK_POS)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.UI_SHOW)),
					Boolean.parseBoolean(getProperty(properties, AppPropertiesReader.UI_PER_PROC)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.UI_CELL_SIZE)),
					Integer.parseInt(getProperty(properties,     AppPropertiesReader.UI_REFRESH_MILLIS)),
					numProcsCluster);

			sol.setupInicial(new ClassLoaderReaderForFile());
			ResourceBundle.clearCache();
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
