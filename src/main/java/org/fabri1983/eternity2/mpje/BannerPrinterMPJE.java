package org.fabri1983.eternity2.mpje;

import mpi.MPI;

public class BannerPrinterMPJE {

	public static void printBanner() {
		StringBuilder msgBuilder = new StringBuilder(64*10);
		String lineSeparator = System.lineSeparator();
		msgBuilder.append("#################################################################").append(lineSeparator);
		msgBuilder.append("##- Uso de MPJExpress en modo híbrido (multicore y cluster).  -##").append(lineSeparator);
		msgBuilder.append("##- Version con array MPHF 1-dimensional, Smart-Podas,        -##").append(lineSeparator);
		msgBuilder.append("##- Sparse Bit Set, Restriccion de Contornos de 2 colores, y  -##").append(lineSeparator);
		msgBuilder.append("##- Micro optimizaciones.                                     -##").append(lineSeparator);
		msgBuilder.append("#################################################################").append(lineSeparator);
		msgBuilder.append("-----------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append("    Copyright(c) 2020 Fabricio Lettieri (fabri1983@gmail.com)    ").append(lineSeparator);
		msgBuilder.append("-----------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append(lineSeparator);
		msgBuilder.append("Total procs: " + MPI.COMM_WORLD.Size()).append(lineSeparator);
		System.out.println(msgBuilder.toString());
	}
	
}
