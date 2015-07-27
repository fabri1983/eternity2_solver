#!/bin/sh
# El entero mas grande de 31 bits es 2147483648. Usar 2147483647 en Complemento a 2.
# El entero mas grande de 63 bits es 9223372036854775808. Usar 9223372036854775807 en Complemento a 2.
# para 2 colums tengo count maximo 29 con 34926 filas calculadas
# para 3 colums tengo count maximo 91 con 1556132 filas calculadas
# para 4 colums tengo count maximo 297 con 68981072 filas calculadas
# para 5 colums tengo count maximo ?? con 3042060164 filas calculadas

# Solver paramaters:
# maxCiclos limiteParcialMax minLimiteExploracion maxParciales destinoARetroceder InterfaceGrafica TableBoardMultiple
# CellPixelesLado CanvasRefreshMillis PodaFairExperiment PodaColorBordeLeftExplorado PosicionInicioMultiThreading

cd ../target

# 900m max usage for 8 threads
mem_alloc="900m"

java -XX:+AggressiveOpts -server -Xms$mem_alloc -Xmx$mem_alloc -XX:MaxPermSize=512m -jar e2solver.jar 12147483647 211 -1 2 -1 true false 28 100 false false 99