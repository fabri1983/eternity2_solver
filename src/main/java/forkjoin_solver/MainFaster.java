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

package forkjoin_solver;

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
			if (args.length != 12)
				throw new Exception("Ingresaste mal los parametros. Hacelo asi: " +
						"maxCiclos limiteParcialMax minLimiteExploracion maxParciales destinoARetroceder InterfaceGrafica TableBoardMultiple " +
						"CellPixelesLado CanvasRefreshMillis PodaFairExperiment PodaColorBordeLeftExplorado PosicionInicioMultiThreading");
			
			int i = 0; // indice de lectura de parámetros para el solver
			SolverFaster.build(
					Long.parseLong(args[i++]),			// maxCiclos
					Integer.parseInt(args[i++]),		// limiteParcialMax
					Integer.parseInt(args[i++]),		// minLimiteExploracion
					Integer.parseInt(args[i++]),		// maxParciales
					Integer.parseInt(args[i++]),		// destinoARetroceder
					Boolean.parseBoolean(args[i++]),	// InterfaceGrafica
					Boolean.parseBoolean(args[i++]),	// TableBoardMultiple
					Integer.parseInt(args[i++]),		// CellPixelesLado
					Integer.parseInt(args[i++]),		// RefreshMillis
					Boolean.parseBoolean(args[i++]),	// PodaFairExperiment
					Boolean.parseBoolean(args[i++]),	// PodaColorBordeLeftExplorado
					Integer.parseInt(args[i++]));		// PosicionInicioMultiProcess
			SolverFaster.setupInicial();
			SolverFaster.atacar();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("\nPrograma terminado.");
	}
}
