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

package org.fabri1983.eternity2.mpje_solver;

import java.util.ResourceBundle;

import javax.swing.UIManager;

import mpi.*;

public final class MainFasterMPJE
{
	private static SolverFasterMPJE sol;
	
	public static void main (String[] args) throws Exception
	{	
		MPI.Init(args);
		
		int rank = MPI.COMM_WORLD.Rank();

		//imprimo una sola vez la portada
		if (rank == 0){
			System.out.println("################################################################################");
			System.out.println("##- Uso de MPJ Express para ejecucion como Sistema Distribuído               -##");
			System.out.println("##- Version con Estructura 4-dimensional, Smart-Podas y Contornos de colores -##");
			System.out.println("##- Micro optimizaciones.                                                    -##");
			System.out.println("################################################################################");
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("         Copyright(c) 2015 Fabricio Lettieri (fabri1983@gmail.com)");
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println();
			System.out.println("Total procs: " + MPI.COMM_WORLD.Size() + "\n\n");
		}
		
		// lo siguiente es solamente para el tablero gráfico
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	System.out.println("Rank " + rank + ": Problema con llamada a setLookAndFeel() en metodo main()");
            e.printStackTrace();
        }
		
		try{
			// NOTA:
			//   para mpje multicore los primeros 3 parametros son para MPI
			//   para mpje cluster los primeros 8 parametros son para MPI
			// Mejor detectar mediante alguna propiedad de MPI si es cluster o multicore
			ResourceBundle properties = args.length == 4 ? ResourceBundle.getBundle(args[3].toLowerCase())
					: ResourceBundle.getBundle(args[8].toLowerCase());
			
			sol = new SolverFasterMPJE(
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
			sol.atacar();
			
		}catch(Exception e){
			System.out.println("\nRank " + rank + ": Problema!! " + e.getMessage()); 
			e.printStackTrace();
		}
		
		System.out.println("\nRank " + rank + ": Programa terminado.");
		
		MPI.Finalize();
	}
}
