package org.fabri1983.eternity2.core.tilesreader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileReaderForTilesFile implements ReaderForTilesFile {

	@Override
	public BufferedReader getReader(String file) {
		try {
			return new BufferedReader(
					new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
}
