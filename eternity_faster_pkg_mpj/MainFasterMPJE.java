/**
 * Copyright (c) 2010 Fabricio Lettieri fabri1983@gmail.com
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

package eternity_faster_pkg_mpj;

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
			System.out.println();
			System.out.println("################################################################################");
			System.out.println("##-                                                                          -##");
			System.out.println("##- Using MPJ Express v0.43                                                  -##");		
			System.out.println("##- Version con Estructura 4-dimensional, Smart-Podas y Contornos de colores -##");
			System.out.println("##-                                                                          -##");
			System.out.println("################################################################################");
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println("         Copyright(c) 2014 Fabricio Lettieri (fabri1983@gmail.com)");
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
			// NOTA: los primeros 3 parametros son para MPI
			if (args.length != 14) 
				throw new Exception("Ingresaste mal los parametros. Hacelo asi: " +
						"maxCiclos limiteParcialMax minLimiteExploracion maxParciales destinoARetroceder InterfaceGrafica " +
						"CellPixelesLado RefreshMillis PodaFairExperiment PodaColorBordeLeftExplorado PosicionInicioMultiProcess");
			
			sol = new SolverFasterMPJE(
					Integer.parseInt(args[3]), 
					Integer.parseInt(args[4]), 
					Integer.parseInt(args[5]), 
					Integer.parseInt(args[6]), 
					Integer.parseInt(args[7]), 
					Boolean.parseBoolean(args[8]), 
					Integer.parseInt(args[9]), 
					Integer.parseInt(args[10]), 
					Boolean.parseBoolean(args[11]), 
					Boolean.parseBoolean(args[12]), 
					Integer.parseInt(args[13]));
			sol.atacar();
			
		}catch(Exception e){
			System.out.println("\nRank " + rank + ": Problema!! " + e.getMessage()); 
			e.printStackTrace();
		}
		
		System.out.println("\nRank " + rank + ": Programa terminado.");
		
		MPI.Finalize();
	}
}
