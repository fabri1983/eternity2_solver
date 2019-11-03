package org.fabri1983.eternity2.mpje_solver;

import mpi.MPI;

public class BannerPrinterForMPJE {

	public static void printBanner() {
		StringBuilder msgBuilder = new StringBuilder(64*10);
		String lineSeparator = System.lineSeparator();
		msgBuilder.append("##################################################################").append(lineSeparator);
		msgBuilder.append("##- Uso de MPJ Express en modo híbrido (multicore y cluster).  -##").append(lineSeparator);
		msgBuilder.append("##- Version con Estructura 4-dimensional, Smart-Podas y        -##").append(lineSeparator);
		msgBuilder.append("##- Contornos de colores pre calculados.                       -##").append(lineSeparator);
		msgBuilder.append("##- Micro optimizaciones.                                      -##").append(lineSeparator);
		msgBuilder.append("##################################################################").append(lineSeparator);
		msgBuilder.append("------------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append("    Copyright(c) 2019 Fabricio Lettieri (fabri1983@gmail.com)     ").append(lineSeparator);
		msgBuilder.append("------------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append(lineSeparator);
		msgBuilder.append("Total procs: " + MPI.COMM_WORLD.Size());
		msgBuilder.append(lineSeparator);
		System.out.println(msgBuilder.toString());
	}
	
}
