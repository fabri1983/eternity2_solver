package eternity_faster_pkg;

import java.util.concurrent.RecursiveAction;

/**
 * Clase auxiliar que hace de tarea para la carga de la super matrix, apta para usar en fork/join pool.
 */
public class LlenadoSuperMatrixTask extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private int base = 0;
	private int chunk_size = 1;
	private ExploracionAction action;
		
	public LlenadoSuperMatrixTask(int proc, int _chunk_size, ExploracionAction _action) {
		base = proc * _chunk_size;
		chunk_size = _chunk_size;
		action = _action;
	}
	
	public void compute() {

		Pieza pz;
		int key;
		
		// itero sobre el arreglo de piezas
		for (int k = base; k < (base + chunk_size); ++k) {
			
			if (k >= SolverFaster.MAX_PIEZAS)
				break;
			
			if (k == SolverFaster.INDICE_P_CENTRAL)
				continue;
			
			pz = action.piezas[k];
			
			//guardo la rotaci�n de la pieza
			byte temp_rot = pz.rotacion;
			//seteo su rotaci�n en 0. Esto es para generar la matriz siempre en el mismo orden
			Pieza.llevarARotacion(pz, (byte)0);
			
			for (int rt=0; rt < SolverFaster.MAX_ESTADOS_ROTACION; ++rt, Pieza.rotar90(pz))
			{
				//FairExperiment.gif: si la pieza tiene su top igual a su bottom => rechazo la pieza
				if (SolverFaster.FairExperimentGif && (pz.top == pz.bottom))
					continue;
				Pieza newp = Pieza.copia(pz);
				
				//este caso es cuando tengo los 4 colores
				key = MapaKeys.getKey(pz.top, pz.right, pz.bottom, pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				
				//tengo tres colores y uno faltante
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,pz.right,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,SolverFaster.MAX_COLORES,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,pz.right,SolverFaster.MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,pz.right,pz.bottom,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				
				//tengo dos colores y dos faltantes
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,pz.bottom,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,pz.right,SolverFaster.MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,pz.right,pz.bottom,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,pz.left);	
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,SolverFaster.MAX_COLORES,pz.bottom,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(pz.top,pz.right,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);

				//tengo un color y tres faltantes
				key = MapaKeys.getKey(pz.top,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,pz.right,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,pz.bottom,SolverFaster.MAX_COLORES);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
				key = MapaKeys.getKey(SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,SolverFaster.MAX_COLORES,pz.left);
				NodoPosibles.addReferencia(action.super_matriz[key], newp);
			}
			
			//restauro la rotaci�n
			Pieza.llevarARotacion(pz, temp_rot);
		}
		
		action = null;
	}
}
