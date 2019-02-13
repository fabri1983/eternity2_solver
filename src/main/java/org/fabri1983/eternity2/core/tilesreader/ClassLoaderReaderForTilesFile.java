package org.fabri1983.eternity2.core.tilesreader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.fabri1983.eternity2.forkjoin_solver.SolverFaster;

public class ClassLoaderReaderForTilesFile implements ReaderForTilesFile {

	@Override
	public BufferedReader getReader(String file) {
		return new BufferedReader(
				new InputStreamReader(SolverFaster.class.getClassLoader().getResourceAsStream(file)));
	}
	
}
