package org.fabri1983.eternity2.faster;

public class BannerPrinterFaster {

	public static void printBanner() {
		StringBuilder msgBuilder = new StringBuilder(64*9);
		String lineSeparator = System.lineSeparator();
		msgBuilder.append("##############################################################").append(lineSeparator);
		msgBuilder.append("##- Uso de thread pool para distribución de tareas.        -##").append(lineSeparator);
		msgBuilder.append("##- Version con array MPHF 1-dimensional, Smart-Podas,     -##").append(lineSeparator);
		msgBuilder.append("##- Sparse Bit Set, y Contornos de colores pre calculados. -##").append(lineSeparator);
		msgBuilder.append("##- Micro optimizaciones.                                  -##").append(lineSeparator);
		msgBuilder.append("##############################################################").append(lineSeparator);
		msgBuilder.append("--------------------------------------------------------------").append(lineSeparator);
		msgBuilder.append("  Copyright(c) 2020 Fabricio Lettieri (fabri1983@gmail.com)   ").append(lineSeparator);
		msgBuilder.append("--------------------------------------------------------------").append(lineSeparator);
		System.out.println(msgBuilder.toString());
	}
	
}
