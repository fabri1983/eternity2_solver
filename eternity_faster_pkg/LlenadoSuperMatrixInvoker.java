package eternity_faster_pkg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public final class LlenadoSuperMatrixInvoker extends RecursiveAction {
	
	private static final long serialVersionUID = 1L;
	private ExploracionAction action;
	
	public LlenadoSuperMatrixInvoker(ExploracionAction _action) {
		action = _action;
	}
	
	public void compute() {
		
		/**
		 * IMPORTANTE: debido a la naturaleza no determin�stica de correr en paralelo la carga de la super matriz, no
		 * se est� manteniendo el orden de insercion de las piezas en objetos NodoPosibles, lo cual es fundamental
		 * para el save y posterior load del estado guardado.
		 * Por ello se decidi� setear availProcs = 1 para evitar dicho problema.
		 */
		int availProcs = 1;//Runtime.getRuntime().availableProcessors();
		
		// tama�o de cada loop
		int chunk_size = SolverFaster.MAX_PIEZAS;//0;
		if (availProcs % 2 == 0 || availProcs == 1)
			chunk_size = SolverFaster.MAX_PIEZAS / availProcs;
		else
			chunk_size = (SolverFaster.MAX_PIEZAS / availProcs) + 1;
		
		if (chunk_size < 0)
			chunk_size = 1;
		
		// lista de objetos fork/join
		List<LlenadoSuperMatrixTask> list = new ArrayList<LlenadoSuperMatrixTask>(availProcs);
		
		// loop que me cree las recursive actions
		for (int proc=0; proc < availProcs; ++proc)
			list.add(new LlenadoSuperMatrixTask(proc, chunk_size, action));
		
		invokeAll(list);
		
		action = null;
	}
}
