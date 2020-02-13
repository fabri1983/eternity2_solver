package org.fabri1983.eternity2.core;

import org.fabri1983.eternity2.faster.SolverFaster;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContornoTest {

	static boolean zona_proc_contorno[] = new boolean[SolverFaster.MAX_PIEZAS];
	static boolean zona_read_contorno[] = new boolean[SolverFaster.MAX_PIEZAS];
	
	@BeforeClass
	public static void beforeClass() {
		inicializarZonaProcesoContornos();
		inicializarZonaReadContornos();
	}
	
	@Test
	public void testPosicionSetContorno() {
		
		/**
		 * Test use of math operations instead of querying an array position 
		 */
		
		for (int _cursor = 0; _cursor < SolverFaster.MAX_PIEZAS; ++_cursor) {
			
			boolean testSlow = 
					// Discard top and bottom rows 
					_cursor > SolverFaster.LADO && 
					_cursor < ((SolverFaster.MAX_PIEZAS - 1) - SolverFaster.LADO) &&
					// Discard borders
					(_cursor & (SolverFaster.LADO - 1)) != 0 && 
					((_cursor + 1) & (SolverFaster.LADO - 1)) != 0 &&
					// At this point cursor is in inner board (no corner no border).
					// Then we need to check cursor is not within Contorno.MAX_COLS - 1 positions after border left. 
					// IMPORTANT: Given the fact Contorno.MAX_COLS is 2 then we can use one condition.
					((_cursor - (Contorno.MAX_COLS - 1)) & (SolverFaster.LADO - 1)) != 0;
			
			Assert.assertEquals("(Slow) For cursor " + _cursor, zona_proc_contorno[_cursor], testSlow);
			
			// Discard corners and borders along with top and bottom rows.
			// Also discard if cursor is within Contorno.MAX_COLS -1 positions after border left.
			// IMPORTANT: Given the fact Contorno.MAX_COLS is 2 then we can use one condition.
//			boolean testFast = ((_cursor >>> 4) & ((_cursor + 1 - SolverFaster.MAX_PIEZAS + SolverFaster.LADO) >>> 4) 
//					& _cursor & (_cursor + 1) & (_cursor - Contorno.MAX_COLS + 1) & (SolverFaster.LADO - 1)) != 0;
//			
//			Assert.assertEquals("(Fast) For cursor " + _cursor, zona_proc_contorno[_cursor], testFast);
		}
	}
	
	@Test
	public void testPosicionReadContorno() {
	    
		/**
		 * Test use of math operations instead of querying an array position 
		 */
		
		for (int _cursor = 0; _cursor < SolverFaster.MAX_PIEZAS; ++_cursor) {
			
			boolean testSlow = 
					// Discard top and bottom rows 
					_cursor > SolverFaster.LADO && 
					_cursor < ((SolverFaster.MAX_PIEZAS - 1) - SolverFaster.LADO) &&
					// Discard borders
					(_cursor & (SolverFaster.LADO - 1)) != 0 && 
					((_cursor + 1) & (SolverFaster.LADO - 1)) != 0 &&
					// At this point cursor is in inner board (no corner no border).
					// Then we need to check cursor is not within Contorno.MAX_COLS - 1 positions before border right. 
					// IMPORTANT: Given the fact Contorno.MAX_COLS is 2 then we can use one condition. 
					((_cursor + Contorno.MAX_COLS - 1 + 1) & (SolverFaster.LADO - 1)) != 0;
			
			Assert.assertEquals("(Slow) For cursor " + _cursor, zona_read_contorno[_cursor], testSlow);
			
			// Discard corners and borders along with top and bottom rows.
			// Also discard if cursor is within Contorno.MAX_COLS - 1 positions before border right.
			// IMPORTANT: Given the fact Contorno.MAX_COLS is 2 then we can use one condition.
//			boolean testFast = ((_cursor >>> 4) & ((_cursor + 1 - SolverFaster.MAX_PIEZAS + SolverFaster.LADO) >>> 4) 
//					& _cursor & (_cursor + 1) & (_cursor + (Contorno.MAX_COLS - 1 + 1)) & (SolverFaster.LADO - 1)) != 0;
//
//			Assert.assertEquals("(Fast) For cursor " + _cursor, zona_read_contorno[_cursor], testFast);
		}
	}
	
    private static void inicializarZonaProcesoContornos()
	{
	    for (int k=0; k <SolverFaster. MAX_PIEZAS; ++k)
	    {
	        //si estoy en borde top o bottom continuo con la siguiente posición
	        if (k < SolverFaster.LADO || k > (SolverFaster.MAX_PIEZAS - SolverFaster.LADO))
	            continue;
	        //si estoy en los bordes entonces continuo con la sig posición
	        if ( (((k+1) % SolverFaster.LADO)==0) || ((k % SolverFaster.LADO)==0) )
	            continue;
	        
	        //desde aqui estoy en el interior del tablero
	        
	        //me aseguro que no esté en borde left + (Contorno.MAX_COLS - 1)
	        int fila_actual = k / SolverFaster.LADO;
	        if (((k - Contorno.MAX_COLS) / SolverFaster.LADO) != fila_actual)
	            continue;
	        
	        zona_proc_contorno[k] = true;
	    }
	}

	private static void inicializarZonaReadContornos()
    {    
        for (int k=0; k < SolverFaster.MAX_PIEZAS; ++k)
        {
            //si estoy en borde top o bottom continuo con la siguiente posición
            if (k < SolverFaster.LADO || k > (SolverFaster.MAX_PIEZAS - SolverFaster.LADO))
                continue;
            //si estoy en los bordes entonces continuo con la sig posición
            if ( (((k+1) % SolverFaster.LADO) == 0) || ((k % SolverFaster.LADO) == 0) )
                continue;
            
            //desde aqui estoy en el interior del tablero
            
            //me aseguro que no esté dentro de (Contorno.MAX_COLS - 1) posiciones antes de border right
            int fila_actual = k / SolverFaster.LADO;
            if ((k + (Contorno.MAX_COLS - 1)) < ((fila_actual * SolverFaster.LADO) + (SolverFaster.LADO - 1)))
                zona_read_contorno[k] = true;
        }
    }
}
