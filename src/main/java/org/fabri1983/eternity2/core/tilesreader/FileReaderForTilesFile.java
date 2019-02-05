package org.fabri1983.eternity2.core.tilesreader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileReaderForTilesFile implements ReaderForTilesFile {

	@Override
	public BufferedReader getReader(String file) {
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
}
