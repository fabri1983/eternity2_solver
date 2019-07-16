package org.fabri1983.eternity2.core.tilesreader;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClassLoaderReaderForTilesFile implements ReaderForTilesFile {

	@Override
	public BufferedReader getReader(String file) {
		return new BufferedReader(
				new InputStreamReader(ClassLoaderReaderForTilesFile.class.getClassLoader().getResourceAsStream(file)));
	}
	
}
